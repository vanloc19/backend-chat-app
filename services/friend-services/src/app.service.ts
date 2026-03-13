import { Injectable, Logger, OnModuleDestroy } from '@nestjs/common';
import amqplib, { Channel, ChannelModel } from 'amqplib';
import type { FriendRequestedEvent } from './types/index.js';

@Injectable()
export class AppService implements OnModuleDestroy {
  private readonly logger = new Logger(AppService.name);
  private readonly exchangeName = 'chat.events';
  private readonly brokerUrl = process.env.MESSAGE_BROKER_URL;

  private connection: ChannelModel | null = null;
  private channel: Channel | null = null;

  getHello(): string {
    return 'Hello World!';
  }

  async publishFriendRequested(payload: FriendRequestedEvent['payload']) {
    const event: FriendRequestedEvent = {
      type: 'friend.requested',
      payload,
      timestamp: new Date().toISOString(),
    };

    if (!this.brokerUrl) {
      this.logger.warn(
        'MESSAGE_BROKER_URL is missing, skip publishing friend event',
      );
      return { published: false, reason: 'MESSAGE_BROKER_URL missing', event };
    }

    await this.ensureChannel();

    this.channel?.publish(
      this.exchangeName,
      event.type,
      Buffer.from(JSON.stringify(event)),
      {
        persistent: true,
        contentType: 'application/json',
      },
    );

    return { published: true, event };
  }

  private async ensureChannel() {
    if (this.channel) {
      return;
    }

    this.connection = await amqplib.connect(this.brokerUrl as string);
    const channel = await this.connection.createChannel();
    await channel.assertExchange(this.exchangeName, 'topic', { durable: true });
    this.channel = channel;

    this.logger.log(`RabbitMQ publisher connected: ${this.brokerUrl}`);
  }

  async onModuleDestroy() {
    await this.channel?.close();
    await this.connection?.close();
  }
}
