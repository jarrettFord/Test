package io.netty.handler.codec.marshalling;

import io.netty.channel.ChannelHandlerContext;
import org.jboss.marshalling.Unmarshaller;

public abstract interface UnmarshallerProvider
{
  public abstract Unmarshaller getUnmarshaller(ChannelHandlerContext paramChannelHandlerContext)
    throws Exception;
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.marshalling.UnmarshallerProvider
 * JD-Core Version:    0.7.0.1
 */