package io.netty.buffer;

import io.netty.util.ReferenceCounted;

public abstract interface ByteBufHolder
  extends ReferenceCounted
{
  public abstract ByteBuf content();
  
  public abstract ByteBufHolder copy();
  
  public abstract ByteBufHolder duplicate();
  
  public abstract ByteBufHolder retain();
  
  public abstract ByteBufHolder retain(int paramInt);
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.ByteBufHolder
 * JD-Core Version:    0.7.0.1
 */