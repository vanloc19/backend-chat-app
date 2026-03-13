/**
 * Structured JSON logger for NestJS services.
 * Outputs log lines compatible with log aggregators (Loki, CloudWatch, Datadog).
 *
 * Usage:
 *   private readonly logger = new AppLogger(MyService.name);
 *   this.logger.log('user registered', { userId });
 */

interface LogMeta {
  [key: string]: unknown;
}

type LogLevel = "info" | "warn" | "error" | "debug";

export class AppLogger {
  constructor(private readonly context?: string) {}

  private write(level: LogLevel, message: string, extra?: LogMeta): void {
    const line = JSON.stringify({
      level,
      message,
      context: this.context,
      ...extra,
      ts: new Date().toISOString(),
    });
    if (level === "error") {
      console.error(line);
    } else if (level === "warn") {
      console.warn(line);
    } else {
      console.log(line);
    }
  }

  log(message: string, meta?: LogMeta): void {
    this.write("info", message, meta);
  }

  error(message: string, traceOrMeta?: string | LogMeta, meta?: LogMeta): void {
    const trace = typeof traceOrMeta === "string" ? traceOrMeta : undefined;
    const extra = typeof traceOrMeta === "object" ? traceOrMeta : meta;
    this.write("error", message, { trace, ...extra });
  }

  warn(message: string, meta?: LogMeta): void {
    this.write("warn", message, meta);
  }

  debug(message: string, meta?: LogMeta): void {
    this.write("debug", message, meta);
  }
}
