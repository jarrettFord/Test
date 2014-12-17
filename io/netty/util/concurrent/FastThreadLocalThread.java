/*  1:   */ package io.netty.util.concurrent;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.InternalThreadLocalMap;
/*  4:   */ 
/*  5:   */ public class FastThreadLocalThread
/*  6:   */   extends Thread
/*  7:   */ {
/*  8:   */   private InternalThreadLocalMap threadLocalMap;
/*  9:   */   
/* 10:   */   public FastThreadLocalThread() {}
/* 11:   */   
/* 12:   */   public FastThreadLocalThread(Runnable target)
/* 13:   */   {
/* 14:30 */     super(target);
/* 15:   */   }
/* 16:   */   
/* 17:   */   public FastThreadLocalThread(ThreadGroup group, Runnable target)
/* 18:   */   {
/* 19:34 */     super(group, target);
/* 20:   */   }
/* 21:   */   
/* 22:   */   public FastThreadLocalThread(String name)
/* 23:   */   {
/* 24:38 */     super(name);
/* 25:   */   }
/* 26:   */   
/* 27:   */   public FastThreadLocalThread(ThreadGroup group, String name)
/* 28:   */   {
/* 29:42 */     super(group, name);
/* 30:   */   }
/* 31:   */   
/* 32:   */   public FastThreadLocalThread(Runnable target, String name)
/* 33:   */   {
/* 34:46 */     super(target, name);
/* 35:   */   }
/* 36:   */   
/* 37:   */   public FastThreadLocalThread(ThreadGroup group, Runnable target, String name)
/* 38:   */   {
/* 39:50 */     super(group, target, name);
/* 40:   */   }
/* 41:   */   
/* 42:   */   public FastThreadLocalThread(ThreadGroup group, Runnable target, String name, long stackSize)
/* 43:   */   {
/* 44:54 */     super(group, target, name, stackSize);
/* 45:   */   }
/* 46:   */   
/* 47:   */   public final InternalThreadLocalMap threadLocalMap()
/* 48:   */   {
/* 49:62 */     return this.threadLocalMap;
/* 50:   */   }
/* 51:   */   
/* 52:   */   public final void setThreadLocalMap(InternalThreadLocalMap threadLocalMap)
/* 53:   */   {
/* 54:70 */     this.threadLocalMap = threadLocalMap;
/* 55:   */   }
/* 56:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.concurrent.FastThreadLocalThread
 * JD-Core Version:    0.7.0.1
 */