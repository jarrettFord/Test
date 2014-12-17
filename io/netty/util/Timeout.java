package io.netty.util;

public abstract interface Timeout
{
  public abstract Timer timer();
  
  public abstract TimerTask task();
  
  public abstract boolean isExpired();
  
  public abstract boolean isCancelled();
  
  public abstract boolean cancel();
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.Timeout
 * JD-Core Version:    0.7.0.1
 */