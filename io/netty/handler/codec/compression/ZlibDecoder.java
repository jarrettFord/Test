package io.netty.handler.codec.compression;

import io.netty.handler.codec.ByteToMessageDecoder;

public abstract class ZlibDecoder
  extends ByteToMessageDecoder
{
  public abstract boolean isClosed();
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.compression.ZlibDecoder
 * JD-Core Version:    0.7.0.1
 */