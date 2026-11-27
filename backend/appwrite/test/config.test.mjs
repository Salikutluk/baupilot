import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';

const raw = readFileSync(fileURLToPath(new URL('../appwrite.config.json', import.meta.url)), 'utf8');
const config = JSON.parse(raw);

const TABLE_IDS = ['projects', 'members', 'trades', 'offers', 'documents', 'inspections', 'steps', 'defects', 'signatures', 'notifications', 'photos', 'protocols'];
const TEAMS = ['bauherr', 'architekt', 'handwerker'];
const MB = 1024 * 1024;
// Appwrite event reference, TablesDB (server 1.8, CLI 13.3): databases.[databaseId].tables.[tableId].rows.[rowId].delete
const PROJECT_DELETE_EVENT = 'databases.baupilot.tables.projects.rows.*.delete';

const table = (id) => config.tables.find((t) => t.$id === id);
const column = (t, key) => t.columns.find((c) => c.key === key);
const perms = (t) => [...t.$permissions].sort();
const team = (right, ...ids) => ids.map((id) => `${right}("team:${id}")`);

test('endpoint is the Frankfurt region and the project id a placeholder', () => {
  assert.equal(new URL(config.endpoint).host, 'fra.cloud.appwrite.io');
  assert.equal(config.endpoint, 'https://fra.cloud.appwrite.io/v1');
  assert.equal(config.projectId, 'baupilot');
});

test('email/password auth is on', () => {
  assert.equal(config.settings.auth.methods['email-password'], true);
});

test('one database holds exactly the twelve tables', () => {
  assert.deepEqual(config.tablesDB, [{ $id: 'baupilot', name: 'baupilot', enabled: true }]);
  assert.deepEqual(config.tables.map((t) => t.$id).sort(), [...TABLE_IDS].sort());
  for (const t of config.tables) {
    assert.equal(t.databaseId, 'baupilot', t.$id);
    assert.equal(t.rowSecurity, false, t.$id);
  }
});

test('every table has projectId (indexed), sortKey, stamps and deleted', () => {
  for (const t of config.tables) {
    const keys = t.columns.map((c) => c.key);
    assert.equal(new Set(keys).size, keys.length, `${t.$id}: duplicate column`);
    assert.deepEqual([column(t, 'projectId').type, column(t, 'projectId').size, column(t, 'projectId').required], ['varchar', 64, true], t.$id);
    assert.equal(column(t, 'sortKey').type, 'integer', t.$id);
    assert.equal(column(t, 'stamps').type, 'longtext', t.$id);
    assert.equal(column(t, 'deleted').type, 'boolean', t.$id);
    assert.equal(column(t, 'deleted').default, false, t.$id);
    const idx = t.indexes.find((i) => i.key === 'idx_projectId');
    assert.deepEqual([idx.type, idx.columns], ['key', ['projectId']], t.$id);
  }
});

test('columns follow the CLI 13.3 schema', () => {
  const colKeys = ['key', 'type', 'required', 'array', 'size', 'default', 'min', 'max', 'format', 'elements', 'encrypt'];
  const types = ['varchar', 'longtext', 'integer', 'double', 'boolean'];
  for (const t of config.tables) {
    const indexNames = new Set(t.columns.map((c) => c.key));
    for (const c of t.columns) {
      assert.ok(Object.keys(c).every((k) => colKeys.includes(k)), `${t.$id}.${c.key}: unknown key`);
      assert.ok(types.includes(c.type), `${t.$id}.${c.key}: type ${c.type}`);
      if (c.type === 'varchar') assert.ok(Number.isInteger(c.size) && c.size > 0, `${t.$id}.${c.key}: size`);
      else assert.equal(c.size, undefined, `${t.$id}.${c.key}: size`);
      // CLI 13.3 rejects `required: true` unless `default` is explicitly null.
      if (c.required) assert.equal(c.default, null, `${t.$id}.${c.key}: required needs default null`);
    }
    for (const i of t.indexes) {
      assert.ok(['key', 'unique'].includes(i.type), `${t.$id}.${i.key}`);
      assert.ok(i.columns.every((k) => indexNames.has(k)), `${t.$id}.${i.key}: unknown column`);
    }
  }
});

test('steps and members have the unique index of the plan', () => {
  assert.deepEqual(table('steps').indexes.find((i) => i.type === 'unique').columns, ['projectId', 'stepKey']);
  assert.deepEqual(table('members').indexes.find((i) => i.type === 'unique').columns, ['projectId', 'userId']);
  for (const t of config.tables) {
    if (t.$id !== 'steps' && t.$id !== 'members') assert.equal(t.indexes.some((i) => i.type === 'unique'), false, t.$id);
  }
});

test('the fixture and extra fields of the plan exist with the planned kinds', () => {
  const expected = {
    projects: ['id', 'buildingType', 'address', 'constructionStart', 'moveIn', 'phase', 'unreadNotifications', 'owner'],
    trades: ['id', 'name', 'phase', 'window', 'scheduleState', 'askNow', 'weekLabel', 'statusLabel', 'detail'],
    offers: ['id', 'tradeId', 'firm', 'price', 'best', 'pending', 'meta', 'documentId'],
    documents: ['id', 'kind', 'tradeId', 'title', 'meta', 'amount', 'due', 'statusLabel', 'viewer', 'fileId', 'fileSha256', 'sizeBytes', 'origin', 'importedAt'],
    inspections: ['tradeId', 'name', 'sub', 'status', 'route', 'signedAt', 'protocolId'],
    steps: ['stepKey', 'tradeId', 'status', 'values', 'capturedAt'],
    defects: ['id', 'tradeId', 'trade', 'title', 'deadline', 'deadlineLabel', 'dueLabel', 'urgency', 'meta', 'photos', 'severity', 'description', 'elementId', 'responsible', 'notifyByEmail', 'stepKey', 'origin'],
    signatures: ['inspectionId', 'role', 'name', 'strokes', 'signedAt'],
    notifications: ['id', 'kind', 'title', 'text', 'when', 'unread', 'target', 'group'],
    photos: ['ownerTable', 'ownerId', 'fileId', 'sha256', 'takenAt', 'bytes'],
    protocols: ['inspectionId', 'fileId', 'fileSha256', 'contentSha256', 'contentJson', 'createdAt'],
    members: ['userId', 'role'],
  };
  for (const id of TABLE_IDS) {
    const keys = table(id).columns.map((c) => c.key).filter((k) => !['projectId', 'sortKey', 'stamps', 'deleted'].includes(k));
    assert.deepEqual(keys.sort(), [...expected[id]].sort(), id);
  }
  assert.equal(column(table('documents'), 'sizeBytes').type, 'integer');
  assert.equal(column(table('protocols'), 'contentJson').type, 'longtext');
  assert.equal(column(table('trades'), 'askNow').type, 'boolean');
  assert.equal(column(table('photos'), 'bytes').type, 'integer');
});

test('buckets photos and pdfs', () => {
  assert.deepEqual(config.buckets.map((b) => b.$id).sort(), ['pdfs', 'photos']);
  const photos = config.buckets.find((b) => b.$id === 'photos');
  const pdfs = config.buckets.find((b) => b.$id === 'pdfs');
  assert.deepEqual(photos.allowedFileExtensions, ['jpg', 'jpeg']);
  assert.equal(photos.maximumFileSize, 20 * MB);
  assert.deepEqual(pdfs.allowedFileExtensions, ['pdf']);
  assert.equal(pdfs.maximumFileSize, 50 * MB);
  for (const b of [photos, pdfs]) {
    assert.equal(b.encryption, true, b.$id);
    assert.equal(b.antivirus, true, b.$id);
    assert.equal(b.fileSecurity, false, b.$id);
    assert.equal(b.enabled, true, b.$id);
    assert.deepEqual([...b.$permissions].sort(), [...team('read', ...TEAMS), ...team('create', ...TEAMS)].sort(), b.$id);
  }
});

test('three teams', () => {
  assert.deepEqual(config.teams, [
    { $id: 'bauherr', name: 'Bauherr' },
    { $id: 'architekt', name: 'Architekt' },
    { $id: 'handwerker', name: 'Handwerker' },
  ]);
});

test('functions', () => {
  const reminders = config.functions.find((f) => f.$id === 'deadline-reminders');
  const cleanup = config.functions.find((f) => f.$id === 'project-cleanup');
  assert.equal(config.functions.length, 2);
  assert.equal(reminders.runtime, 'node-22');
  assert.equal(reminders.schedule, '0 6 * * *');
  assert.deepEqual(reminders.execute, []);
  assert.deepEqual(reminders.events, []);
  assert.deepEqual([...reminders.scopes].sort(), ['messages.write', 'rows.read', 'targets.read', 'users.read']);
  assert.equal(reminders.path, 'functions/deadline-reminders');
  assert.equal(cleanup.runtime, 'node-22');
  assert.deepEqual(cleanup.events, [PROJECT_DELETE_EVENT]);
  assert.deepEqual(cleanup.execute, []);
  assert.deepEqual([...cleanup.scopes].sort(), ['files.read', 'files.write', 'rows.read', 'rows.write']);
  assert.equal(cleanup.path, 'functions/project-cleanup');
  assert.ok(!cleanup.schedule);
});

test('permission matrix', () => {
  const all = TEAMS;
  const writers = ['steps', 'photos', 'signatures', 'protocols', 'defects', 'documents', 'members', 'inspections'];
  const leads = ['projects', 'trades', 'offers', 'notifications'];
  for (const id of TABLE_IDS) {
    const expected = [...team('read', ...all)];
    if (writers.includes(id)) expected.push(...team('create', ...all), ...team('update', ...all));
    if (leads.includes(id)) expected.push(...team('create', 'bauherr', 'architekt'), ...team('update', 'bauherr', 'architekt'));
    if (id === 'projects') expected.push(...team('delete', 'bauherr'));
    assert.deepEqual(perms(table(id)), expected.sort(), id);
  }
});

test('no vars, secrets or keys anywhere', () => {
  const walk = (node, path) => {
    if (Array.isArray(node)) return node.forEach((v, i) => walk(v, `${path}[${i}]`));
    if (node && typeof node === 'object') {
      for (const [k, v] of Object.entries(node)) {
        assert.ok(!/^vars$|secret|apikey|api_key|password$|token$|privatekey/i.test(k) || k === 'email-password', `${path}.${k}`);
        walk(v, `${path}.${k}`);
      }
      return;
    }
    if (typeof node === 'string') assert.ok(!/standard_[0-9a-f]{16}|BEGIN [A-Z ]*PRIVATE KEY|AIza[0-9A-Za-z_-]{20}/.test(node), path);
  };
  walk(config, '$');
  assert.ok(!/"vars"/.test(raw));
});
