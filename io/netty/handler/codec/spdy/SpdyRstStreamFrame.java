package io.netty.handler.codec.spdy;

public abstract interface SpdyRstStreamFrame
  extends SpdyStreamFrame
{
  public abstract SpdyStreamStatus status();
  
  public abstract SpdyRstStreamFrame setStatus(SpdyStreamStatus paramSpdyStreamStatus);
  
  public abstract SpdyRstStreamFrame setStreamId(int paramInt);
  
  public abstract SpdyRstStreamFrame setLast(boolean paramBoolean);
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.SpdyRstStreamFrame
 * JD-Core Version:    0.7.0.1
 */