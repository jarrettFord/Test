package io.netty.bootstrap;

import io.netty.channel.Channel;

public abstract interface ChannelFactory<T extends Channel>
{
  public abstract T newChannel();
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.bootstrap.ChannelFactory
 * JD-Core Version:    0.7.0.1
 */