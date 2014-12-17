/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ public final class ChannelPromiseNotifier
/*  4:   */   implements ChannelFutureListener
/*  5:   */ {
/*  6:   */   private final ChannelPromise[] promises;
/*  7:   */   
/*  8:   */   public ChannelPromiseNotifier(ChannelPromise... promises)
/*  9:   */   {
/* 10:31 */     if (promises == null) {
/* 11:32 */       throw new NullPointerException("promises");
/* 12:   */     }
/* 13:34 */     for (ChannelPromise promise : promises) {
/* 14:35 */       if (promise == null) {
/* 15:36 */         throw new IllegalArgumentException("promises contains null ChannelPromise");
/* 16:   */       }
/* 17:   */     }
/* 18:39 */     this.promises = ((ChannelPromise[])promises.clone());
/* 19:   */   }
/* 20:   */   
/* 21:   */   public void operationComplete(ChannelFuture cf)
/* 22:   */     throws Exception
/* 23:   */   {
/* 24:44 */     if (cf.isSuccess())
/* 25:   */     {
/* 26:45 */       for (ChannelPromise p : this.promises) {
/* 27:46 */         p.setSuccess();
/* 28:   */       }
/* 29:48 */       return;
/* 30:   */     }
/* 31:51 */     Throwable cause = cf.cause();
/* 32:52 */     for (ChannelPromise p : this.promises) {
/* 33:53 */       p.setFailure(cause);
/* 34:   */     }
/* 35:   */   }
/* 36:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.ChannelPromiseNotifier
 * JD-Core Version:    0.7.0.1
 */