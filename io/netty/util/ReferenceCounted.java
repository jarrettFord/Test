package io.netty.util;

public abstract interface ReferenceCounted
{
  public abstract int refCnt();
  
  public abstract ReferenceCounted retain();
  
  public abstract ReferenceCounted retain(int paramInt);
  
  public abstract boolean release();
  
  public abstract boolean release(int paramInt);
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.ReferenceCounted
 * JD-Core Version:    0.7.0.1
 */