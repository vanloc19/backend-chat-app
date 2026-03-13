/**
 * Structured JSON logger wrapper for NestJS services.
 * Outputs log lines compatible with log aggregators (Loki, CloudWatch, Datadog).
 *
 * Usage:
 *   private readonly logger = new AppLogger(MyService.name);
 *   this.logger.log('user registered', { userId });
 */

import { Logger } from '@nestjs/common';

interface LogMeta {
  [key: string]: unknown;
}

export class AppLogger extends Logger {
  override log(message: string, meta?: LogMeta): void {
    super.log(JSON.stringify({ level: 'info', message, ...meta, ts: new Date().toISOString() }));
  }

  override error(message: string, trace?: string, meta?: LogMeta): void {
    super.error(
      JSON.stringify({ level: 'error', message, trace, ...meta, ts: new Date().toISOString() }),
    );
  }

  override warn(message: string, meta?: LogMeta): void {
    super.warn(JSON.stringify({ level: 'warn', message, ...meta, ts: new Date().toISOString() }));
  }

  override debug(message: string, meta?: LogMeta): void {
    super.debug(JSON.stringify({ level: 'debug', message, ...meta, ts: new Date().toISOString() }));
  }
}
