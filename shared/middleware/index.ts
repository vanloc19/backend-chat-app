/**
 * Shared HTTP middleware for NestJS services.
 * Import and register in each service's AppModule.
 *
 * Example:
 *   export class AppModule implements NestModule {
 *     configure(consumer: MiddlewareConsumer) {
 *       consumer.apply(HttpLoggerMiddleware).forRoutes('*');
 *     }
 *   }
 */

import { Injectable, NestMiddleware, Logger } from '@nestjs/common';
import { Request, Response, NextFunction } from 'express';

@Injectable()
export class HttpLoggerMiddleware implements NestMiddleware {
  private readonly logger = new Logger('HTTP');

  use(req: Request, res: Response, next: NextFunction): void {
    const { method, originalUrl, ip } = req;
    const start = Date.now();

    res.on('finish', () => {
      const duration = Date.now() - start;
      const { statusCode } = res;
      this.logger.log(`${method} ${originalUrl} ${statusCode} ${duration}ms — ${ip}`);
    });

    next();
  }
}
