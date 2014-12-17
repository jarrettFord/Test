package io.netty.util;

public abstract interface ResourceLeak
{
  public abstract void record();
  
  public abstract boolean close();
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.ResourceLeak
 * JD-Core Version:    0.7.0.1
 */