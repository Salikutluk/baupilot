import { createHash } from 'node:crypto';

const PAGE = 100;
const ISO_DAY = /^(\d{4})-(\d{2})-(\d{2})$/;

/** Calendar day in Europe/Berlin as "YYYY-MM-DD". */
export function berlinToday(nowUtc) {
  const parts = new Intl.DateTimeFormat('en-US', {
    timeZone: 'Europe/Berlin',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).formatToParts(nowUtc);
  const get = (type) => parts.find((p) => p.type === type).value;
  return `${get('year')}-${get('month')}-${get('day')}`;
}

function dayNumber(iso) {
  if (typeof iso !== 'string') return null;
  const m = ISO_DAY.exec(iso);
  if (!m) return null;
  const ms = Date.UTC(Number(m[1]), Number(m[2]) - 1, Number(m[3]));
  const back = new Date(ms).toISOString().slice(0, 10);
  return back === iso ? ms / 86_400_000 : null;
}

export function addDays(iso, days) {
  return new Date((dayNumber(iso) + days) * 86_400_000).toISOString().slice(0, 10);
}

/** Live defects whose valid ISO deadline is at most `windowDays` after today (overdue included). */
export function selectDue(defects, todayIso, windowDays = 2) {
  const limit = dayNumber(todayIso) + windowDays;
  return defects.filter((d) => {
    if (d.deleted === true) return false;
    const day = dayNumber(d.deadline);
    return day !== null && day <= limit;
  });
}

/** Deterministic Appwrite message id: one per day and project, 36 chars of [a-z0-9-]. */
export function messageIdFor(todayIso, projectId) {
  const digest = createHash('sha256').update(projectId).digest('hex').slice(0, 26);
  return `r${todayIso.replaceAll('-', '')}-${digest}`;
}

/** Push text with a count and a date only (D20): no title, address or name. */
export function composePush(count, untilIso) {
  const [y, m, d] = untilIso.split('-');
  return {
    title: 'Fristen-Erinnerung',
    body: `${count} ${count === 1 ? 'Mangel' : 'Mängel'} mit Frist bis ${d}.${m}.${y}`,
  };
}

async function listAll(sdk, table, projectId) {
  const out = [];
  let after;
  for (;;) {
    const { rows } = await sdk.listRows(table, { projectId, limit: PAGE, after });
    out.push(...rows);
    if (rows.length < PAGE) return out;
    after = rows[rows.length - 1].$id;
  }
}

/**
 * Sends one push per project with due defects to the project's members.
 * `sdk`: `listRows(table, { projectId?, limit, after })` -> `{ rows }` and
 * `createPush({ messageId, title, body, users, data })`, which throws an error with `code: 409` for a known id.
 */
export async function runReminders(sdk, nowUtc) {
  const today = berlinToday(nowUtc);
  const until = addDays(today, 2);
  const result = { projects: 0, pushed: 0, skipped: 0 };
  for (const project of await listAll(sdk, 'projects')) {
    result.projects += 1;
    const due = selectDue(await listAll(sdk, 'defects', project.$id), today);
    if (due.length === 0) continue;
    const users = [...new Set((await listAll(sdk, 'members', project.$id)).map((m) => m.userId).filter(Boolean))];
    if (users.length === 0) continue;
    const { title, body } = composePush(due.length, until);
    try {
      await sdk.createPush({
        messageId: messageIdFor(today, project.$id),
        title,
        body,
        users,
        data: { target: 'defects', projectId: project.$id },
      });
      result.pushed += 1;
    } catch (e) {
      if (e?.code !== 409) throw e;
      result.skipped += 1;
    }
  }
  return result;
}
