package io.netty.handler.codec.http;

import io.netty.handler.codec.DecoderResult;

public abstract interface HttpObject
{
  public abstract DecoderResult getDecoderResult();
  
  public abstract void setDecoderResult(DecoderResult paramDecoderResult);
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.HttpObject
 * JD-Core Version:    0.7.0.1
 */