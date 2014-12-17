package io.netty.handler.codec.serialization;

public abstract interface ClassResolver
{
  public abstract Class<?> resolve(String paramString)
    throws ClassNotFoundException;
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.serialization.ClassResolver
 * JD-Core Version:    0.7.0.1
 */