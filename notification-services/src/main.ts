import 'dotenv/config';
import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);
  const rawPort = process.env.NOTIFICATION_PORT;
  if (!rawPort) {
    throw new Error('NOTIFICATION_PORT is not set');
  }
  const port = parseInt(rawPort, 10);
  if (Number.isNaN(port)) {
    throw new Error('NOTIFICATION_PORT must be a number');
  }
  await app.listen(port);
}
bootstrap();
