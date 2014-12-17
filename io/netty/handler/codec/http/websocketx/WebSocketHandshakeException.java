/*  1:   */ package io.netty.handler.codec.http.websocketx;
/*  2:   */ 
/*  3:   */ public class WebSocketHandshakeException
/*  4:   */   extends RuntimeException
/*  5:   */ {
/*  6:   */   private static final long serialVersionUID = 1L;
/*  7:   */   
/*  8:   */   public WebSocketHandshakeException(String s)
/*  9:   */   {
/* 10:26 */     super(s);
/* 11:   */   }
/* 12:   */   
/* 13:   */   public WebSocketHandshakeException(String s, Throwable throwable)
/* 14:   */   {
/* 15:30 */     super(s, throwable);
/* 16:   */   }
/* 17:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.websocketx.WebSocketHandshakeException
 * JD-Core Version:    0.7.0.1
 */