/*  1:   */ package io.netty.util.internal;
/*  2:   */ 
/*  3:   */ import io.netty.util.Recycler;
/*  4:   */ import io.netty.util.Recycler.Handle;
/*  5:   */ import io.netty.util.ReferenceCountUtil;
/*  6:   */ import io.netty.util.concurrent.Promise;
/*  7:   */ 
/*  8:   */ public final class PendingWrite
/*  9:   */ {
/* 10:26 */   private static final Recycler<PendingWrite> RECYCLER = new Recycler()
/* 11:   */   {
/* 12:   */     protected PendingWrite newObject(Recycler.Handle handle)
/* 13:   */     {
/* 14:29 */       return new PendingWrite(handle, null);
/* 15:   */     }
/* 16:   */   };
/* 17:   */   private final Recycler.Handle handle;
/* 18:   */   private Object msg;
/* 19:   */   private Promise<Void> promise;
/* 20:   */   
/* 21:   */   public static PendingWrite newInstance(Object msg, Promise<Void> promise)
/* 22:   */   {
/* 23:37 */     PendingWrite pending = (PendingWrite)RECYCLER.get();
/* 24:38 */     pending.msg = msg;
/* 25:39 */     pending.promise = promise;
/* 26:40 */     return pending;
/* 27:   */   }
/* 28:   */   
/* 29:   */   private PendingWrite(Recycler.Handle handle)
/* 30:   */   {
/* 31:48 */     this.handle = handle;
/* 32:   */   }
/* 33:   */   
/* 34:   */   public boolean recycle()
/* 35:   */   {
/* 36:55 */     this.msg = null;
/* 37:56 */     this.promise = null;
/* 38:57 */     return RECYCLER.recycle(this, this.handle);
/* 39:   */   }
/* 40:   */   
/* 41:   */   public boolean failAndRecycle(Throwable cause)
/* 42:   */   {
/* 43:64 */     ReferenceCountUtil.release(this.msg);
/* 44:65 */     if (this.promise != null) {
/* 45:66 */       this.promise.setFailure(cause);
/* 46:   */     }
/* 47:68 */     return recycle();
/* 48:   */   }
/* 49:   */   
/* 50:   */   public boolean successAndRecycle()
/* 51:   */   {
/* 52:75 */     if (this.promise != null) {
/* 53:76 */       this.promise.setSuccess(null);
/* 54:   */     }
/* 55:78 */     return recycle();
/* 56:   */   }
/* 57:   */   
/* 58:   */   public Object msg()
/* 59:   */   {
/* 60:82 */     return this.msg;
/* 61:   */   }
/* 62:   */   
/* 63:   */   public Promise<Void> promise()
/* 64:   */   {
/* 65:86 */     return this.promise;
/* 66:   */   }
/* 67:   */   
/* 68:   */   public Promise<Void> recycleAndGet()
/* 69:   */   {
/* 70:93 */     Promise<Void> promise = this.promise;
/* 71:94 */     recycle();
/* 72:95 */     return promise;
/* 73:   */   }
/* 74:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.PendingWrite
 * JD-Core Version:    0.7.0.1
 */