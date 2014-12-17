package io.netty.util.concurrent;

public abstract interface GenericProgressiveFutureListener<F extends ProgressiveFuture<?>>
  extends GenericFutureListener<F>
{
  public abstract void operationProgressed(F paramF, long paramLong1, long paramLong2)
    throws Exception;
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.concurrent.GenericProgressiveFutureListener
 * JD-Core Version:    0.7.0.1
 */