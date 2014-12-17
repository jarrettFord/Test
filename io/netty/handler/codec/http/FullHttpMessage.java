package io.netty.handler.codec.http;

public abstract interface FullHttpMessage
  extends HttpMessage, LastHttpContent
{
  public abstract FullHttpMessage copy();
  
  public abstract FullHttpMessage retain(int paramInt);
  
  public abstract FullHttpMessage retain();
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.FullHttpMessage
 * JD-Core Version:    0.7.0.1
 */