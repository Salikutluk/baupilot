export const PROJECT_TABLES = ['members', 'trades', 'offers', 'documents', 'inspections', 'steps', 'defects', 'signatures', 'notifications', 'photos', 'protocols'];

const PAGE = 100;
const FILE_TABLES = { photos: 'photos', documents: 'pdfs', protocols: 'pdfs' };
const BUCKETS = ['photos', 'pdfs'];

async function listAllRows(sdk, table, projectId) {
  const out = [];
  let after;
  for (;;) {
    const { rows } = await sdk.listRows(table, { projectId, limit: PAGE, after });
    out.push(...rows);
    if (rows.length < PAGE) return out;
    after = rows[rows.length - 1].$id;
  }
}

async function deleteFileIfPresent(sdk, bucket, fileId) {
  try {
    await sdk.deleteFile(bucket, fileId);
    return true;
  } catch (e) {
    if (e?.code === 404) return false;
    throw e;
  }
}

/**
 * Removes every row and file of a project. `sdk` adapter:
 * `listRows(table, { projectId, limit, after })` -> `{ rows }`, `deleteRow(table, rowId)`,
 * `listFiles(bucket, { namePrefix, limit, after })` -> `{ files }`, `deleteFile(bucket, fileId)` (404 for a missing file).
 * Returns the number of rows and files actually deleted.
 */
export async function cleanupProject(sdk, projectId) {
  const fileIds = [];
  for (const [table, bucket] of Object.entries(FILE_TABLES)) {
    for (const row of await listAllRows(sdk, table, projectId)) {
      if (row.fileId) fileIds.push([bucket, row.fileId]);
    }
  }

  let rows = 0;
  for (const table of PROJECT_TABLES) {
    for (const row of await listAllRows(sdk, table, projectId)) {
      await sdk.deleteRow(table, row.$id);
      rows += 1;
    }
  }

  let files = 0;
  for (const [bucket, fileId] of fileIds) {
    if (await deleteFileIfPresent(sdk, bucket, fileId)) files += 1;
  }

  const namePrefix = `${projectId}/`;
  for (const bucket of BUCKETS) {
    const orphans = [];
    let after;
    for (;;) {
      const page = (await sdk.listFiles(bucket, { namePrefix, limit: PAGE, after })).files;
      orphans.push(...page);
      if (page.length < PAGE) break;
      after = page[page.length - 1].$id;
    }
    for (const file of orphans) {
      if (await deleteFileIfPresent(sdk, bucket, file.$id)) files += 1;
    }
  }
  return { rows, files };
}
