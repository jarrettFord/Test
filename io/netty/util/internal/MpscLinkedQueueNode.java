/*  1:   */ package io.netty.util.internal;
/*  2:   */ 
/*  3:   */ import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
/*  4:   */ 
/*  5:   */ public abstract class MpscLinkedQueueNode<T>
/*  6:   */ {
/*  7:   */   private static final AtomicReferenceFieldUpdater<MpscLinkedQueueNode, MpscLinkedQueueNode> nextUpdater;
/*  8:   */   private volatile MpscLinkedQueueNode<T> next;
/*  9:   */   
/* 10:   */   static
/* 11:   */   {
/* 12:30 */     AtomicReferenceFieldUpdater<MpscLinkedQueueNode, MpscLinkedQueueNode> u = PlatformDependent.newAtomicReferenceFieldUpdater(MpscLinkedQueueNode.class, "next");
/* 13:31 */     if (u == null) {
/* 14:32 */       u = AtomicReferenceFieldUpdater.newUpdater(MpscLinkedQueueNode.class, MpscLinkedQueueNode.class, "next");
/* 15:   */     }
/* 16:34 */     nextUpdater = u;
/* 17:   */   }
/* 18:   */   
/* 19:   */   final MpscLinkedQueueNode<T> next()
/* 20:   */   {
/* 21:41 */     return this.next;
/* 22:   */   }
/* 23:   */   
/* 24:   */   final void setNext(MpscLinkedQueueNode<T> newNext)
/* 25:   */   {
/* 26:47 */     nextUpdater.lazySet(this, newNext);
/* 27:   */   }
/* 28:   */   
/* 29:   */   protected T clearMaybe()
/* 30:   */   {
/* 31:56 */     return value();
/* 32:   */   }
/* 33:   */   
/* 34:   */   void unlink()
/* 35:   */   {
/* 36:63 */     setNext(null);
/* 37:   */   }
/* 38:   */   
/* 39:   */   public abstract T value();
/* 40:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.MpscLinkedQueueNode
 * JD-Core Version:    0.7.0.1
 */