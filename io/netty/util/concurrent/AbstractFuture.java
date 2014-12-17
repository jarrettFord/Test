/*  1:   */ package io.netty.util.concurrent;
/*  2:   */ 
/*  3:   */ import java.util.concurrent.ExecutionException;
/*  4:   */ import java.util.concurrent.TimeUnit;
/*  5:   */ import java.util.concurrent.TimeoutException;
/*  6:   */ 
/*  7:   */ public abstract class AbstractFuture<V>
/*  8:   */   implements Future<V>
/*  9:   */ {
/* 10:   */   public V get()
/* 11:   */     throws InterruptedException, ExecutionException
/* 12:   */   {
/* 13:31 */     await();
/* 14:   */     
/* 15:33 */     Throwable cause = cause();
/* 16:34 */     if (cause == null) {
/* 17:35 */       return getNow();
/* 18:   */     }
/* 19:37 */     throw new ExecutionException(cause);
/* 20:   */   }
/* 21:   */   
/* 22:   */   public V get(long timeout, TimeUnit unit)
/* 23:   */     throws InterruptedException, ExecutionException, TimeoutException
/* 24:   */   {
/* 25:42 */     if (await(timeout, unit))
/* 26:   */     {
/* 27:43 */       Throwable cause = cause();
/* 28:44 */       if (cause == null) {
/* 29:45 */         return getNow();
/* 30:   */       }
/* 31:47 */       throw new ExecutionException(cause);
/* 32:   */     }
/* 33:49 */     throw new TimeoutException();
/* 34:   */   }
/* 35:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.concurrent.AbstractFuture
 * JD-Core Version:    0.7.0.1
 */