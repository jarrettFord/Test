/*  1:   */ package io.netty.util.internal;
/*  2:   */ 
/*  3:   */ public abstract class OneTimeTask
/*  4:   */   extends MpscLinkedQueueNode<Runnable>
/*  5:   */   implements Runnable
/*  6:   */ {
/*  7:   */   public Runnable value()
/*  8:   */   {
/*  9:30 */     return this;
/* 10:   */   }
/* 11:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.OneTimeTask
 * JD-Core Version:    0.7.0.1
 */