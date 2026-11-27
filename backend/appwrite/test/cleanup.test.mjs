import { test } from 'node:test';
import assert from 'node:assert/strict';
import { PROJECT_TABLES, cleanupProject } from '../functions/project-cleanup/src/cleanup.js';

const BUCKETS = { photos: 'photos', pdfs: 'pdfs' };

// In-memory backend. Pages like Appwrite: limit + after cursor; deleting rows shifts the first page.
function fakeSdk() {
  const tables = Object.fromEntries(PROJECT_TABLES.map((t) => [t, []]));
  const files = { photos: [], pdfs: [] };
  const log = { deleteFile: [] };
  return {
    tables,
    files,
    log,
    async listRows(table, { projectId, limit = 25, after } = {}) {
      let rows = tables[table].filter((r) => r.projectId === projectId);
      if (after) rows = rows.slice(rows.findIndex((r) => r.$id === after) + 1);
      return { rows: rows.slice(0, limit) };
    },
    async deleteRow(table, rowId) {
      tables[table] = tables[table].filter((r) => r.$id !== rowId);
    },
    async listFiles(bucket, { namePrefix, limit = 25, after } = {}) {
      let list = files[bucket].filter((f) => f.name.startsWith(namePrefix));
      if (after) list = list.slice(list.findIndex((f) => f.$id === after) + 1);
      return { files: list.slice(0, limit) };
    },
    async deleteFile(bucket, fileId) {
      log.deleteFile.push([bucket, fileId]);
      if (!files[bucket].some((f) => f.$id === fileId)) throw Object.assign(new Error('not found'), { code: 404 });
      files[bucket] = files[bucket].filter((f) => f.$id !== fileId);
    },
  };
}

function seed(sdk, projectId, perTable = 250) {
  for (const t of PROJECT_TABLES) {
    for (let i = 0; i < perTable; i++) {
      const row = { $id: `${projectId}-${t}-${i}`, projectId };
      if (t === 'photos') row.fileId = `${projectId}-ph-${i}`;
      if (t === 'documents') row.fileId = i < 10 ? `${projectId}-doc-${i}` : '';
      if (t === 'protocols') row.fileId = `${projectId}-pr-${i}`;
      sdk.tables[t].push(row);
    }
  }
  for (let i = 0; i < perTable; i++) sdk.files.photos.push({ $id: `${projectId}-ph-${i}`, name: `${projectId}/ph-${i}.jpg` });
  for (let i = 0; i < Math.min(10, perTable); i++) sdk.files.pdfs.push({ $id: `${projectId}-doc-${i}`, name: `${projectId}/doc-${i}.pdf` });
  for (let i = 0; i < perTable; i++) sdk.files.pdfs.push({ $id: `${projectId}-pr-${i}`, name: `${projectId}/pr-${i}.pdf` });
}

const rowCount = (sdk, projectId) => PROJECT_TABLES.reduce((n, t) => n + sdk.tables[t].filter((r) => r.projectId === projectId).length, 0);
const fileCount = (sdk, projectId) => sdk.files.photos.concat(sdk.files.pdfs).filter((f) => f.name.startsWith(`${projectId}/`)).length;

test('the eleven project tables', () => {
  assert.deepEqual(PROJECT_TABLES, ['members', 'trades', 'offers', 'documents', 'inspections', 'steps', 'defects', 'signatures', 'notifications', 'photos', 'protocols']);
});

test('removes every row and file of project A (250 rows per table) and leaves B untouched', async () => {
  const sdk = fakeSdk();
  seed(sdk, 'A');
  seed(sdk, 'B', 30);
  const result = await cleanupProject(sdk, 'A');
  assert.equal(rowCount(sdk, 'A'), 0);
  assert.equal(fileCount(sdk, 'A'), 0);
  assert.equal(rowCount(sdk, 'B'), 11 * 30);
  assert.equal(fileCount(sdk, 'B'), 30 + 10 + 30);
  assert.deepEqual(result, { rows: 11 * 250, files: 250 + 10 + 250 });
});

test('is idempotent', async () => {
  const sdk = fakeSdk();
  seed(sdk, 'A', 120);
  await cleanupProject(sdk, 'A');
  assert.deepEqual(await cleanupProject(sdk, 'A'), { rows: 0, files: 0 });
});

test('a file 404 is ignored and not counted', async () => {
  const sdk = fakeSdk();
  seed(sdk, 'A', 5);
  sdk.tables.photos.push({ $id: 'A-ghost', projectId: 'A', fileId: 'A-ghost-file' });
  const result = await cleanupProject(sdk, 'A');
  assert.ok(sdk.log.deleteFile.some(([, id]) => id === 'A-ghost-file'));
  assert.equal(rowCount(sdk, 'A'), 0);
  assert.equal(fileCount(sdk, 'A'), 0);
  assert.deepEqual(result, { rows: 11 * 5 + 1, files: 5 + 5 + 5 });
});

test('other file errors are not swallowed', async () => {
  const sdk = fakeSdk();
  seed(sdk, 'A', 2);
  sdk.deleteFile = async () => { throw Object.assign(new Error('server down'), { code: 500 }); };
  await assert.rejects(cleanupProject(sdk, 'A'), /server down/);
});

test('the backstop removes a file that exists only by its name prefix', async () => {
  const sdk = fakeSdk();
  sdk.files.photos.push({ $id: 'orphan', name: 'A/orphan.jpg' });
  sdk.files.pdfs.push({ $id: 'orphan2', name: 'A/sub/orphan.pdf' });
  sdk.files.photos.push({ $id: 'other', name: 'AB/keep.jpg' });
  const result = await cleanupProject(sdk, 'A');
  assert.deepEqual(result, { rows: 0, files: 2 });
  assert.deepEqual(sdk.files.photos.map((f) => f.$id), ['other']);
  assert.deepEqual(sdk.files.pdfs, []);
});

test('rows are collected with a cursor page of 100', async () => {
  const sdk = fakeSdk();
  seed(sdk, 'A', 150);
  const limits = [];
  const inner = sdk.listRows;
  sdk.listRows = (t, o) => { limits.push(o.limit); return inner(t, o); };
  await cleanupProject(sdk, 'A');
  assert.ok(limits.length > 0 && limits.every((l) => l === 100));
});
