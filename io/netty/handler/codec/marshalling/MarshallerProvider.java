package io.netty.handler.codec.marshalling;

import io.netty.channel.ChannelHandlerContext;
import org.jboss.marshalling.Marshaller;

public abstract interface MarshallerProvider
{
  public abstract Marshaller getMarshaller(ChannelHandlerContext paramChannelHandlerContext)
    throws Exception;
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.marshalling.MarshallerProvider
 * JD-Core Version:    0.7.0.1
 */