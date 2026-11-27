import { Client, Messaging, Query, TablesDB } from 'node-appwrite';
import { runReminders } from './reminders.js';

const DATABASE_ID = 'baupilot';

export default async ({ req, res, log }) => {
  const client = new Client()
    .setEndpoint(process.env.APPWRITE_FUNCTION_API_ENDPOINT)
    .setProject(process.env.APPWRITE_FUNCTION_PROJECT_ID)
    .setKey(req.headers['x-appwrite-key']);
  const tables = new TablesDB(client);
  const messaging = new Messaging(client);

  const sdk = {
    async listRows(table, { projectId, limit, after }) {
      const queries = [Query.limit(limit)];
      if (projectId !== undefined) queries.push(Query.equal('projectId', projectId));
      if (after) queries.push(Query.cursorAfter(after));
      return { rows: (await tables.listRows({ databaseId: DATABASE_ID, tableId: table, queries })).rows };
    },
    async createPush({ messageId, title, body, users, data }) {
      await messaging.createPush({ messageId, title, body, users, data });
    },
  };

  const result = await runReminders(sdk, new Date());
  log(JSON.stringify(result));
  return res.json(result);
};
