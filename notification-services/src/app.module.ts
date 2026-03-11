import { Module, OnModuleInit } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { connectDatabase } from './config/database.config';

@Module({
  imports: [],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule implements OnModuleInit {
  async onModuleInit() {
    await connectDatabase(
      process.env.DB_NOTIFICATION_URL,
      process.env.DB_NOTIFICATION_NAME,
      'notification-services',
    );
  }
}
