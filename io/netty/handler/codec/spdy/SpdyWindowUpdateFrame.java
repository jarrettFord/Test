package io.netty.handler.codec.spdy;

public abstract interface SpdyWindowUpdateFrame
  extends SpdyFrame
{
  public abstract int streamId();
  
  public abstract SpdyWindowUpdateFrame setStreamId(int paramInt);
  
  public abstract int deltaWindowSize();
  
  public abstract SpdyWindowUpdateFrame setDeltaWindowSize(int paramInt);
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.SpdyWindowUpdateFrame
 * JD-Core Version:    0.7.0.1
 */