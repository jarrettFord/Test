package io.netty.handler.codec.spdy;

public abstract interface SpdyGoAwayFrame
  extends SpdyFrame
{
  public abstract int lastGoodStreamId();
  
  public abstract SpdyGoAwayFrame setLastGoodStreamId(int paramInt);
  
  public abstract SpdySessionStatus status();
  
  public abstract SpdyGoAwayFrame setStatus(SpdySessionStatus paramSpdySessionStatus);
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.SpdyGoAwayFrame
 * JD-Core Version:    0.7.0.1
 */