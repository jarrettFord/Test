package io.netty.handler.codec.http;

public abstract interface HttpMessage
  extends HttpObject
{
  public abstract HttpVersion getProtocolVersion();
  
  public abstract HttpMessage setProtocolVersion(HttpVersion paramHttpVersion);
  
  public abstract HttpHeaders headers();
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.HttpMessage
 * JD-Core Version:    0.7.0.1
 */