/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ import java.util.LinkedHashSet;
/*  4:   */ import java.util.Set;
/*  5:   */ 
/*  6:   */ public final class ChannelPromiseAggregator
/*  7:   */   implements ChannelFutureListener
/*  8:   */ {
/*  9:   */   private final ChannelPromise aggregatePromise;
/* 10:   */   private Set<ChannelPromise> pendingPromises;
/* 11:   */   
/* 12:   */   public ChannelPromiseAggregator(ChannelPromise aggregatePromise)
/* 13:   */   {
/* 14:39 */     if (aggregatePromise == null) {
/* 15:40 */       throw new NullPointerException("aggregatePromise");
/* 16:   */     }
/* 17:42 */     this.aggregatePromise = aggregatePromise;
/* 18:   */   }
/* 19:   */   
/* 20:   */   public ChannelPromiseAggregator add(ChannelPromise... promises)
/* 21:   */   {
/* 22:49 */     if (promises == null) {
/* 23:50 */       throw new NullPointerException("promises");
/* 24:   */     }
/* 25:52 */     if (promises.length == 0) {
/* 26:53 */       return this;
/* 27:   */     }
/* 28:55 */     synchronized (this)
/* 29:   */     {
/* 30:56 */       if (this.pendingPromises == null)
/* 31:   */       {
/* 32:   */         int size;
/* 33:   */         int size;
/* 34:58 */         if (promises.length > 1) {
/* 35:59 */           size = promises.length;
/* 36:   */         } else {
/* 37:61 */           size = 2;
/* 38:   */         }
/* 39:63 */         this.pendingPromises = new LinkedHashSet(size);
/* 40:   */       }
/* 41:65 */       for (ChannelPromise p : promises) {
/* 42:66 */         if (p != null)
/* 43:   */         {
/* 44:69 */           this.pendingPromises.add(p);
/* 45:70 */           p.addListener(this);
/* 46:   */         }
/* 47:   */       }
/* 48:   */     }
/* 49:73 */     return this;
/* 50:   */   }
/* 51:   */   
/* 52:   */   public synchronized void operationComplete(ChannelFuture future)
/* 53:   */     throws Exception
/* 54:   */   {
/* 55:78 */     if (this.pendingPromises == null)
/* 56:   */     {
/* 57:79 */       this.aggregatePromise.setSuccess();
/* 58:   */     }
/* 59:   */     else
/* 60:   */     {
/* 61:81 */       this.pendingPromises.remove(future);
/* 62:82 */       if (!future.isSuccess())
/* 63:   */       {
/* 64:83 */         this.aggregatePromise.setFailure(future.cause());
/* 65:84 */         for (ChannelPromise pendingFuture : this.pendingPromises) {
/* 66:85 */           pendingFuture.setFailure(future.cause());
/* 67:   */         }
/* 68:   */       }
/* 69:88 */       else if (this.pendingPromises.isEmpty())
/* 70:   */       {
/* 71:89 */         this.aggregatePromise.setSuccess();
/* 72:   */       }
/* 73:   */     }
/* 74:   */   }
/* 75:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.ChannelPromiseAggregator
 * JD-Core Version:    0.7.0.1
 */