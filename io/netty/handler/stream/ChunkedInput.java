package io.netty.handler.stream;

import io.netty.channel.ChannelHandlerContext;

public abstract interface ChunkedInput<B>
{
  public abstract boolean isEndOfInput()
    throws Exception;
  
  public abstract void close()
    throws Exception;
  
  public abstract B readChunk(ChannelHandlerContext paramChannelHandlerContext)
    throws Exception;
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.stream.ChunkedInput
 * JD-Core Version:    0.7.0.1
 */