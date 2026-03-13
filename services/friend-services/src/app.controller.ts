import { Body, Controller, Get, Post } from '@nestjs/common';
import { AppService } from './app.service';

type FriendRequestBody = {
  fromUserId: string;
  toUserId: string;
  message?: string;
};

@Controller()
export class AppController {
  constructor(private readonly appService: AppService) {}

  @Get()
  getHello(): string {
    return this.appService.getHello();
  }

  @Post('friends/request')
  async requestFriend(@Body() body: FriendRequestBody) {
    return this.appService.publishFriendRequested({
      fromUserId: body.fromUserId,
      toUserId: body.toUserId,
      message: body.message,
    });
  }
}
