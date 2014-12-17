package io.netty.channel;

import io.netty.util.ReferenceCounted;
import java.net.SocketAddress;

public abstract interface AddressedEnvelope<M, A extends SocketAddress>
  extends ReferenceCounted
{
  public abstract M content();
  
  public abstract A sender();
  
  public abstract A recipient();
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.AddressedEnvelope
 * JD-Core Version:    0.7.0.1
 */