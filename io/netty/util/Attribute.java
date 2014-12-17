package io.netty.util;

public abstract interface Attribute<T>
{
  public abstract AttributeKey<T> key();
  
  public abstract T get();
  
  public abstract void set(T paramT);
  
  public abstract T getAndSet(T paramT);
  
  public abstract T setIfAbsent(T paramT);
  
  public abstract T getAndRemove();
  
  public abstract boolean compareAndSet(T paramT1, T paramT2);
  
  public abstract void remove();
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.Attribute
 * JD-Core Version:    0.7.0.1
 */