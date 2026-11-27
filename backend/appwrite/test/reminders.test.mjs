import { test } from 'node:test';
import assert from 'node:assert/strict';
import { berlinToday, selectDue, messageIdFor, composePush, runReminders } from '../functions/deadline-reminders/src/reminders.js';

test('berlinToday rolls over at Berlin midnight (winter and summer time)', () => {
  assert.equal(berlinToday(new Date('2026-12-18T23:30:00Z')), '2026-12-19');
  assert.equal(berlinToday(new Date('2026-12-18T22:30:00Z')), '2026-12-18');
  assert.equal(berlinToday(new Date('2026-06-30T22:30:00Z')), '2026-07-01');
  assert.equal(berlinToday(new Date('2026-06-30T21:30:00Z')), '2026-06-30');
});

test('selectDue: window boundaries, overdue, deleted, invalid dates', () => {
  const d = (id, deadline, extra = {}) => ({ $id: id, deadline, ...extra });
  const defects = [
    d('today', '2026-12-18'),
    d('plus2', '2026-12-20'),
    d('plus3', '2026-12-21'),
    d('overdue', '2026-11-01'),
    d('gone', '2026-12-18', { deleted: true }),
    d('live', '2026-12-18', { deleted: false }),
    d('bad', 'morgen'),
    d('badDay', '2026-02-30'),
    d('missing', undefined),
    d('null', null),
  ];
  assert.deepEqual(selectDue(defects, '2026-12-18').map((x) => x.$id), ['today', 'plus2', 'overdue', 'live']);
  assert.deepEqual(selectDue(defects, '2026-12-18', 0).map((x) => x.$id), ['today', 'overdue', 'live']);
  assert.deepEqual(selectDue([d('year', '2027-01-01')], '2026-12-30').map((x) => x.$id), ['year']);
  assert.deepEqual(selectDue([d('year', '2027-01-02')], '2026-12-30'), []);
});

test('messageIdFor: 36 chars of [a-z0-9-], stable per day and project', () => {
  const id = messageIdFor('2026-12-18', 'p-1');
  assert.equal(id.length, 36);
  assert.match(id, /^[a-z0-9-]{36}$/);
  assert.ok(id.startsWith('r20261218-'));
  assert.equal(id, messageIdFor('2026-12-18', 'p-1'));
  assert.notEqual(id, messageIdFor('2026-12-19', 'p-1'));
  assert.notEqual(id, messageIdFor('2026-12-18', 'p-2'));
  assert.equal(messageIdFor('2026-12-18', 'x'.repeat(200)).length, 36);
});

test('composePush: singular, plural, date format and no defect title (D20)', () => {
  assert.deepEqual(composePush(1, '2026-12-20'), { title: 'Fristen-Erinnerung', body: '1 Mangel mit Frist bis 20.12.2026' });
  assert.deepEqual(composePush(2, '2026-12-20'), { title: 'Fristen-Erinnerung', body: '2 Mängel mit Frist bis 20.12.2026' });
  assert.equal(composePush(5, '2027-01-02').body, '5 Mängel mit Frist bis 02.01.2027');
});

// Fake SDK over in-memory tables; pages like Appwrite (limit + after cursor).
function fakeSdk({ projects = [], defects = [], members = [], pushErrors = {} } = {}) {
  const tables = { projects, defects, members };
  const pushes = [];
  const calls = { listRows: [] };
  return {
    pushes,
    calls,
    async listRows(table, { projectId, limit = 25, after } = {}) {
      calls.listRows.push({ table, projectId, limit, after });
      let rows = tables[table].filter((r) => projectId === undefined || r.projectId === projectId);
      if (after) rows = rows.slice(rows.findIndex((r) => r.$id === after) + 1);
      return { rows: rows.slice(0, limit) };
    },
    async createPush(msg) {
      if (pushErrors[msg.messageId]) throw pushErrors[msg.messageId];
      pushes.push(msg);
    },
  };
}
const NOW = new Date('2026-12-18T05:00:00Z'); // Berlin 2026-12-18
const defect = (n, projectId, deadline = '2026-12-19', extra = {}) => ({ $id: `d${n}`, projectId, deadline, title: `Geheimer Titel ${n}`, ...extra });
const member = (n, projectId, userId) => ({ $id: `m${n}`, projectId, userId });

test('runReminders: one push per project with the member ids and the privacy-safe payload', async () => {
  const sdk = fakeSdk({
    projects: [{ $id: 'pA' }, { $id: 'pB' }],
    defects: [defect(1, 'pA'), defect(2, 'pA', '2026-12-20'), defect(3, 'pA', '2027-03-01'), defect(4, 'pB', '2026-12-18')],
    members: [member(1, 'pA', 'u1'), member(2, 'pA', 'u2'), member(3, 'pA', 'u1'), member(4, 'pB', 'u3')],
  });
  const result = await runReminders(sdk, NOW);
  assert.deepEqual(result, { projects: 2, pushed: 2, skipped: 0 });
  assert.equal(sdk.pushes.length, 2);
  const [a, b] = sdk.pushes;
  assert.equal(a.messageId, messageIdFor('2026-12-18', 'pA'));
  assert.deepEqual(a.users, ['u1', 'u2']);
  assert.equal(a.title, 'Fristen-Erinnerung');
  assert.equal(a.body, '2 Mängel mit Frist bis 20.12.2026');
  assert.deepEqual(a.data, { target: 'defects', projectId: 'pA' });
  assert.deepEqual(b.users, ['u3']);
  assert.equal(b.body, '1 Mangel mit Frist bis 20.12.2026');
  assert.ok(!JSON.stringify(sdk.pushes).includes('Geheimer Titel'));
});

test('runReminders pages over more than 100 defects, members and projects', async () => {
  const projects = Array.from({ length: 130 }, (_, i) => ({ $id: `p${i}` }));
  const defects = Array.from({ length: 250 }, (_, i) => defect(i, 'p0', i < 220 ? '2026-12-18' : '2030-01-01'));
  const members = Array.from({ length: 120 }, (_, i) => member(i, 'p0', `u${i}`));
  const sdk = fakeSdk({ projects, defects, members });
  const result = await runReminders(sdk, NOW);
  assert.deepEqual(result, { projects: 130, pushed: 1, skipped: 0 });
  assert.equal(sdk.pushes[0].users.length, 120);
  assert.equal(sdk.pushes[0].body, '220 Mängel mit Frist bis 20.12.2026');
  assert.ok(sdk.calls.listRows.every((c) => c.limit === 100));
  assert.ok(sdk.calls.listRows.filter((c) => c.table === 'defects').length >= 3);
});

test('runReminders sends nothing without members or without due defects', async () => {
  const sdk = fakeSdk({
    projects: [{ $id: 'pA' }, { $id: 'pB' }],
    defects: [defect(1, 'pA'), defect(2, 'pB', '2030-01-01')],
    members: [member(1, 'pB', 'u1')],
  });
  assert.deepEqual(await runReminders(sdk, NOW), { projects: 2, pushed: 0, skipped: 0 });
  assert.equal(sdk.pushes.length, 0);
});

test('runReminders counts a 409 as skipped and rethrows other errors', async () => {
  const projects = [{ $id: 'pA' }, { $id: 'pB' }];
  const defects = [defect(1, 'pA'), defect(2, 'pB')];
  const members = [member(1, 'pA', 'u1'), member(2, 'pB', 'u2')];
  const conflict = Object.assign(new Error('exists'), { code: 409 });
  const sdk = fakeSdk({ projects, defects, members, pushErrors: { [messageIdFor('2026-12-18', 'pA')]: conflict } });
  assert.deepEqual(await runReminders(sdk, NOW), { projects: 2, pushed: 1, skipped: 1 });
  const broken = fakeSdk({ projects, defects, members, pushErrors: { [messageIdFor('2026-12-18', 'pA')]: Object.assign(new Error('boom'), { code: 500 }) } });
  await assert.rejects(runReminders(broken, NOW), /boom/);
});
