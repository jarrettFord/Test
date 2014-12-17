package io.netty.handler.codec.http;

public abstract interface HttpResponse
  extends HttpMessage
{
  public abstract HttpResponseStatus getStatus();
  
  public abstract HttpResponse setStatus(HttpResponseStatus paramHttpResponseStatus);
  
  public abstract HttpResponse setProtocolVersion(HttpVersion paramHttpVersion);
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.HttpResponse
 * JD-Core Version:    0.7.0.1
 */