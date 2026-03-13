/**
 * Shared HTTP middleware for NestJS services.
 * Keep this file framework-agnostic so it can be imported without extra deps.
 *
 * Example:
 *   export class AppModule implements NestModule {
 *     configure(consumer: MiddlewareConsumer) {
 *       consumer.apply(HttpLoggerMiddleware).forRoutes('*');
 *     }
 *   }
 */

import type {
  NextFunction,
  RequestLike,
  ResponseLike,
} from "../types/middleware/index.js";

export class HttpLoggerMiddleware {
  use(req: RequestLike, res: ResponseLike, next: NextFunction): void {
    const { method, originalUrl, ip } = req;
    const start = Date.now();

    res.on("finish", () => {
      const duration = Date.now() - start;
      const { statusCode } = res;
      console.log(
        `${method ?? "UNKNOWN"} ${originalUrl ?? req.url ?? "/"} ${statusCode ?? 0} ${duration}ms - ${ip ?? "unknown"}`,
      );
    });

    next();
  }
}
