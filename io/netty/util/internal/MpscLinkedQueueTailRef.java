/*  1:   */ package io.netty.util.internal;
/*  2:   */ 
/*  3:   */ import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
/*  4:   */ 
/*  5:   */ abstract class MpscLinkedQueueTailRef<E>
/*  6:   */   extends MpscLinkedQueuePad1<E>
/*  7:   */ {
/*  8:   */   private static final long serialVersionUID = 8717072462993327429L;
/*  9:   */   private static final AtomicReferenceFieldUpdater<MpscLinkedQueueTailRef, MpscLinkedQueueNode> UPDATER;
/* 10:   */   private volatile transient MpscLinkedQueueNode<E> tailRef;
/* 11:   */   
/* 12:   */   static
/* 13:   */   {
/* 14:31 */     AtomicReferenceFieldUpdater<MpscLinkedQueueTailRef, MpscLinkedQueueNode> updater = PlatformDependent.newAtomicReferenceFieldUpdater(MpscLinkedQueueTailRef.class, "tailRef");
/* 15:32 */     if (updater == null) {
/* 16:33 */       updater = AtomicReferenceFieldUpdater.newUpdater(MpscLinkedQueueTailRef.class, MpscLinkedQueueNode.class, "tailRef");
/* 17:   */     }
/* 18:36 */     UPDATER = updater;
/* 19:   */   }
/* 20:   */   
/* 21:   */   protected final MpscLinkedQueueNode<E> tailRef()
/* 22:   */   {
/* 23:42 */     return this.tailRef;
/* 24:   */   }
/* 25:   */   
/* 26:   */   protected final void setTailRef(MpscLinkedQueueNode<E> tailRef)
/* 27:   */   {
/* 28:46 */     this.tailRef = tailRef;
/* 29:   */   }
/* 30:   */   
/* 31:   */   protected final MpscLinkedQueueNode<E> getAndSetTailRef(MpscLinkedQueueNode<E> tailRef)
/* 32:   */   {
/* 33:52 */     return (MpscLinkedQueueNode)UPDATER.getAndSet(this, tailRef);
/* 34:   */   }
/* 35:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.MpscLinkedQueueTailRef
 * JD-Core Version:    0.7.0.1
 */