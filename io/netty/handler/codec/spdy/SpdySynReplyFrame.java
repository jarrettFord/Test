package io.netty.handler.codec.spdy;

public abstract interface SpdySynReplyFrame
  extends SpdyHeadersFrame
{
  public abstract SpdySynReplyFrame setStreamId(int paramInt);
  
  public abstract SpdySynReplyFrame setLast(boolean paramBoolean);
  
  public abstract SpdySynReplyFrame setInvalid();
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.SpdySynReplyFrame
 * JD-Core Version:    0.7.0.1
 */