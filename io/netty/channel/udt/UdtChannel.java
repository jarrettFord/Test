package io.netty.channel.udt;

import io.netty.channel.Channel;
import java.net.InetSocketAddress;

public abstract interface UdtChannel
  extends Channel
{
  public abstract UdtChannelConfig config();
  
  public abstract InetSocketAddress localAddress();
  
  public abstract InetSocketAddress remoteAddress();
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.udt.UdtChannel
 * JD-Core Version:    0.7.0.1
 */