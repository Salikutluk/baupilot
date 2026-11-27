import { Client, Query, Storage, TablesDB } from 'node-appwrite';
import { cleanupProject } from './cleanup.js';

const DATABASE_ID = 'baupilot';

export default async ({ req, res, log, error }) => {
  const projectId = req.bodyJson?.$id;
  if (typeof projectId !== 'string' || projectId === '') {
    error('delete event without a project id');
    return res.json({ ok: false }, 400);
  }
  const client = new Client()
    .setEndpoint(process.env.APPWRITE_FUNCTION_API_ENDPOINT)
    .setProject(process.env.APPWRITE_FUNCTION_PROJECT_ID)
    .setKey(req.headers['x-appwrite-key']);
  const tables = new TablesDB(client);
  const storage = new Storage(client);

  const sdk = {
    async listRows(table, { projectId: id, limit, after }) {
      const queries = [Query.equal('projectId', id), Query.limit(limit)];
      if (after) queries.push(Query.cursorAfter(after));
      return { rows: (await tables.listRows({ databaseId: DATABASE_ID, tableId: table, queries })).rows };
    },
    async deleteRow(table, rowId) {
      await tables.deleteRow({ databaseId: DATABASE_ID, tableId: table, rowId });
    },
    async listFiles(bucket, { namePrefix, limit, after }) {
      const queries = [Query.startsWith('name', namePrefix), Query.limit(limit)];
      if (after) queries.push(Query.cursorAfter(after));
      return { files: (await storage.listFiles({ bucketId: bucket, queries })).files };
    },
    async deleteFile(bucket, fileId) {
      await storage.deleteFile({ bucketId: bucket, fileId });
    },
  };

  const result = await cleanupProject(sdk, projectId);
  log(JSON.stringify({ projectId, ...result }));
  return res.json({ ok: true, ...result });
};
