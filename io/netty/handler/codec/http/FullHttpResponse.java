package io.netty.handler.codec.http;

public abstract interface FullHttpResponse
  extends HttpResponse, FullHttpMessage
{
  public abstract FullHttpResponse copy();
  
  public abstract FullHttpResponse retain(int paramInt);
  
  public abstract FullHttpResponse retain();
  
  public abstract FullHttpResponse setProtocolVersion(HttpVersion paramHttpVersion);
  
  public abstract FullHttpResponse setStatus(HttpResponseStatus paramHttpResponseStatus);
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.FullHttpResponse
 * JD-Core Version:    0.7.0.1
 */