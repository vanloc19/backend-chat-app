import { Logger } from '@nestjs/common';
import mongoose from 'mongoose';

const logger = new Logger('Database');

export async function connectDatabase(
  uri: string | undefined,
  serviceName: string,
): Promise<void> {
  if (!uri?.trim()) {
    logger.warn(`DB URI is not set for ${serviceName}`);
    return;
  }

  try {
    await mongoose.connect(uri.trim());
    logger.log(`Connected to MongoDB (${serviceName})`);
  } catch (err) {
    logger.error(`MongoDB connection failed for ${serviceName}`, err);
    throw err;
  }
}
