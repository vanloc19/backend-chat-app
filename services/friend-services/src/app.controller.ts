import { Body, Controller, Get, Post } from '@nestjs/common';
import { AppService } from './app.service';
import type { FriendRequestPayload } from './types/index.js';

@Controller()
export class AppController {
  constructor(private readonly appService: AppService) {}

  @Get()
  getHello(): string {
    return this.appService.getHello();
  }

  @Post('friends/request')
  async requestFriend(@Body() body: FriendRequestPayload) {
    return this.appService.publishFriendRequested({
      fromUserId: body.fromUserId,
      toUserId: body.toUserId,
      message: body.message,
    });
  }
}
