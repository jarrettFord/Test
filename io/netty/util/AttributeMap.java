package io.netty.util;

public abstract interface AttributeMap
{
  public abstract <T> Attribute<T> attr(AttributeKey<T> paramAttributeKey);
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.AttributeMap
 * JD-Core Version:    0.7.0.1
 */