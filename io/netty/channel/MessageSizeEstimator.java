package io.netty.channel;

public abstract interface MessageSizeEstimator
{
  public abstract Handle newHandle();
  
  public static abstract interface Handle
  {
    public abstract int size(Object paramObject);
  }
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.MessageSizeEstimator
 * JD-Core Version:    0.7.0.1
 */