import { Logger } from '@nestjs/common';
import mongoose from 'mongoose';

const logger = new Logger('Database');

function parseDbNameFromMongoUri(uri: string): string | undefined {
  const schemeIdx = uri.indexOf('://');
  if (schemeIdx < 0) return undefined;
  const firstSlashAfterHost = uri.indexOf('/', schemeIdx + 3);
  if (firstSlashAfterHost < 0) return undefined;
  const qIdx = uri.indexOf('?', firstSlashAfterHost + 1);
  const db =
    qIdx < 0
      ? uri.slice(firstSlashAfterHost + 1)
      : uri.slice(firstSlashAfterHost + 1, qIdx);
  return db.trim() ? db.trim() : undefined;
}

export async function connectDatabase(
  uri: string | undefined,
  dbName: string | undefined,
  serviceName: string,
): Promise<void> {
  if (!uri?.trim()) {
    throw new Error(`DB URI is not set for ${serviceName}`);
  }

  const parsedDbName = parseDbNameFromMongoUri(uri.trim());
  const finalDbName = parsedDbName ?? dbName?.trim();
  if (!finalDbName) {
    throw new Error(
      `MongoDB database name is missing for ${serviceName}. Add '/<db>' to DB URL or set DB_NOTIFICATION_NAME in .env`,
    );
  }

  try {
    await mongoose.connect(uri.trim(), { dbName: finalDbName });
    logger.log(`Connected to MongoDB (${serviceName})`);
  } catch (err) {
    logger.error(`MongoDB connection failed for ${serviceName}`, err);
    throw err;
  }
}
