/*  1:   */ package io.netty.util.internal;
/*  2:   */ 
/*  3:   */ import java.io.Serializable;
/*  4:   */ import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
/*  5:   */ 
/*  6:   */ abstract class MpscLinkedQueueHeadRef<E>
/*  7:   */   extends MpscLinkedQueuePad0<E>
/*  8:   */   implements Serializable
/*  9:   */ {
/* 10:   */   private static final long serialVersionUID = 8467054865577874285L;
/* 11:   */   private static final AtomicReferenceFieldUpdater<MpscLinkedQueueHeadRef, MpscLinkedQueueNode> UPDATER;
/* 12:   */   private volatile transient MpscLinkedQueueNode<E> headRef;
/* 13:   */   
/* 14:   */   static
/* 15:   */   {
/* 16:33 */     AtomicReferenceFieldUpdater<MpscLinkedQueueHeadRef, MpscLinkedQueueNode> updater = PlatformDependent.newAtomicReferenceFieldUpdater(MpscLinkedQueueHeadRef.class, "headRef");
/* 17:34 */     if (updater == null) {
/* 18:35 */       updater = AtomicReferenceFieldUpdater.newUpdater(MpscLinkedQueueHeadRef.class, MpscLinkedQueueNode.class, "headRef");
/* 19:   */     }
/* 20:38 */     UPDATER = updater;
/* 21:   */   }
/* 22:   */   
/* 23:   */   protected final MpscLinkedQueueNode<E> headRef()
/* 24:   */   {
/* 25:44 */     return this.headRef;
/* 26:   */   }
/* 27:   */   
/* 28:   */   protected final void setHeadRef(MpscLinkedQueueNode<E> headRef)
/* 29:   */   {
/* 30:48 */     this.headRef = headRef;
/* 31:   */   }
/* 32:   */   
/* 33:   */   protected final void lazySetHeadRef(MpscLinkedQueueNode<E> headRef)
/* 34:   */   {
/* 35:52 */     UPDATER.lazySet(this, headRef);
/* 36:   */   }
/* 37:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.MpscLinkedQueueHeadRef
 * JD-Core Version:    0.7.0.1
 */