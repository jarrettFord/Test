package io.netty.handler.codec.http;

public abstract interface HttpRequest
  extends HttpMessage
{
  public abstract HttpMethod getMethod();
  
  public abstract HttpRequest setMethod(HttpMethod paramHttpMethod);
  
  public abstract String getUri();
  
  public abstract HttpRequest setUri(String paramString);
  
  public abstract HttpRequest setProtocolVersion(HttpVersion paramHttpVersion);
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.HttpRequest
 * JD-Core Version:    0.7.0.1
 */