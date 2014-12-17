/*  1:   */ package io.netty.util;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.PlatformDependent;
/*  4:   */ import java.util.concurrent.ConcurrentMap;
/*  5:   */ 
/*  6:   */ public final class Signal
/*  7:   */   extends Error
/*  8:   */ {
/*  9:   */   private static final long serialVersionUID = -221145131122459977L;
/* 10:31 */   private static final ConcurrentMap<String, Boolean> map = ;
/* 11:   */   private final UniqueName uname;
/* 12:   */   
/* 13:   */   public static Signal valueOf(String name)
/* 14:   */   {
/* 15:41 */     return new Signal(name);
/* 16:   */   }
/* 17:   */   
/* 18:   */   @Deprecated
/* 19:   */   public Signal(String name)
/* 20:   */   {
/* 21:49 */     super(name);
/* 22:50 */     this.uname = new UniqueName(map, name, new Object[0]);
/* 23:   */   }
/* 24:   */   
/* 25:   */   public void expect(Signal signal)
/* 26:   */   {
/* 27:58 */     if (this != signal) {
/* 28:59 */       throw new IllegalStateException("unexpected signal: " + signal);
/* 29:   */     }
/* 30:   */   }
/* 31:   */   
/* 32:   */   public Throwable initCause(Throwable cause)
/* 33:   */   {
/* 34:65 */     return this;
/* 35:   */   }
/* 36:   */   
/* 37:   */   public Throwable fillInStackTrace()
/* 38:   */   {
/* 39:70 */     return this;
/* 40:   */   }
/* 41:   */   
/* 42:   */   public String toString()
/* 43:   */   {
/* 44:75 */     return this.uname.name();
/* 45:   */   }
/* 46:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.Signal
 * JD-Core Version:    0.7.0.1
 */