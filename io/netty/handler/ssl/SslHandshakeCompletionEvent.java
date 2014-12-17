/*  1:   */ package io.netty.handler.ssl;
/*  2:   */ 
/*  3:   */ public final class SslHandshakeCompletionEvent
/*  4:   */ {
/*  5:25 */   public static final SslHandshakeCompletionEvent SUCCESS = new SslHandshakeCompletionEvent();
/*  6:   */   private final Throwable cause;
/*  7:   */   
/*  8:   */   private SslHandshakeCompletionEvent()
/*  9:   */   {
/* 10:33 */     this.cause = null;
/* 11:   */   }
/* 12:   */   
/* 13:   */   public SslHandshakeCompletionEvent(Throwable cause)
/* 14:   */   {
/* 15:41 */     if (cause == null) {
/* 16:42 */       throw new NullPointerException("cause");
/* 17:   */     }
/* 18:44 */     this.cause = cause;
/* 19:   */   }
/* 20:   */   
/* 21:   */   public boolean isSuccess()
/* 22:   */   {
/* 23:51 */     return this.cause == null;
/* 24:   */   }
/* 25:   */   
/* 26:   */   public Throwable cause()
/* 27:   */   {
/* 28:59 */     return this.cause;
/* 29:   */   }
/* 30:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.ssl.SslHandshakeCompletionEvent
 * JD-Core Version:    0.7.0.1
 */