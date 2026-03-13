import {
  Injectable,
  Logger,
  OnModuleDestroy,
  OnModuleInit,
} from '@nestjs/common';
import amqplib, { Channel, ChannelModel, ConsumeMessage } from 'amqplib';

type EventEnvelope = {
  type: string;
  payload: Record<string, unknown>;
  timestamp: string;
};

@Injectable()
export class AppService implements OnModuleInit, OnModuleDestroy {
  private readonly logger = new Logger(AppService.name);
  private readonly exchangeName = 'chat.events';
  private readonly queueName = 'notification.events';
  private readonly routingKeys = [
    'friend.requested',
    'friend.accepted',
    'message.created',
  ];
  private readonly brokerUrl = process.env.MESSAGE_BROKER_URL;

  private connection: ChannelModel | null = null;
  private channel: Channel | null = null;
  private readonly events: EventEnvelope[] = [];

  getHello(): string {
    return 'Hello World!';
  }

  getRecentEvents() {
    return this.events;
  }

  async onModuleInit() {
    if (!this.brokerUrl) {
      this.logger.warn(
        'MESSAGE_BROKER_URL is missing, notification consumer is disabled',
      );
      return;
    }

    this.connection = await amqplib.connect(this.brokerUrl);
    const channel = await this.connection.createChannel();
    this.channel = channel;

    await channel.assertExchange(this.exchangeName, 'topic', { durable: true });
    await channel.assertQueue(this.queueName, { durable: true });

    for (const routingKey of this.routingKeys) {
      await channel.bindQueue(this.queueName, this.exchangeName, routingKey);
    }

    await channel.consume(this.queueName, (msg) => this.handleMessage(msg), {
      noAck: false,
    });

    this.logger.log(`RabbitMQ consumer connected: ${this.brokerUrl}`);
  }

  private handleMessage(msg: ConsumeMessage | null) {
    if (!msg || !this.channel) {
      return;
    }

    try {
      const event = JSON.parse(msg.content.toString()) as EventEnvelope;
      this.events.unshift(event);

      if (this.events.length > 100) {
        this.events.length = 100;
      }

      this.logger.log(`Event consumed: ${event.type}`);
      this.channel.ack(msg);
    } catch (error) {
      this.logger.error(
        'Failed to parse message from RabbitMQ',
        error as Error,
      );
      this.channel.nack(msg, false, false);
    }
  }

  async onModuleDestroy() {
    await this.channel?.close();
    await this.connection?.close();
  }
}
