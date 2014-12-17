package io.netty.channel;

import io.netty.util.concurrent.EventExecutor;

public abstract interface EventLoop
  extends EventExecutor, EventLoopGroup
{
  public abstract EventLoopGroup parent();
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.EventLoop
 * JD-Core Version:    0.7.0.1
 */