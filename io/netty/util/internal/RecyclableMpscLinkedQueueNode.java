/*  1:   */ package io.netty.util.internal;
/*  2:   */ 
/*  3:   */ import io.netty.util.Recycler.Handle;
/*  4:   */ 
/*  5:   */ public abstract class RecyclableMpscLinkedQueueNode<T>
/*  6:   */   extends MpscLinkedQueueNode<T>
/*  7:   */ {
/*  8:   */   private final Recycler.Handle handle;
/*  9:   */   
/* 10:   */   protected RecyclableMpscLinkedQueueNode(Recycler.Handle handle)
/* 11:   */   {
/* 12:29 */     if (handle == null) {
/* 13:30 */       throw new NullPointerException("handle");
/* 14:   */     }
/* 15:32 */     this.handle = handle;
/* 16:   */   }
/* 17:   */   
/* 18:   */   final void unlink()
/* 19:   */   {
/* 20:37 */     super.unlink();
/* 21:38 */     recycle(this.handle);
/* 22:   */   }
/* 23:   */   
/* 24:   */   protected abstract void recycle(Recycler.Handle paramHandle);
/* 25:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.RecyclableMpscLinkedQueueNode
 * JD-Core Version:    0.7.0.1
 */