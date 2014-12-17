package io.netty.handler.codec.spdy;

public abstract interface SpdyPingFrame
  extends SpdyFrame
{
  public abstract int id();
  
  public abstract SpdyPingFrame setId(int paramInt);
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.SpdyPingFrame
 * JD-Core Version:    0.7.0.1
 */