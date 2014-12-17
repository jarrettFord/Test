/*  1:   */ package io.netty.handler.codec.spdy;
/*  2:   */ 
/*  3:   */ public class SpdyProtocolException
/*  4:   */   extends Exception
/*  5:   */ {
/*  6:   */   private static final long serialVersionUID = 7870000537743847264L;
/*  7:   */   
/*  8:   */   public SpdyProtocolException() {}
/*  9:   */   
/* 10:   */   public SpdyProtocolException(String message, Throwable cause)
/* 11:   */   {
/* 12:31 */     super(message, cause);
/* 13:   */   }
/* 14:   */   
/* 15:   */   public SpdyProtocolException(String message)
/* 16:   */   {
/* 17:38 */     super(message);
/* 18:   */   }
/* 19:   */   
/* 20:   */   public SpdyProtocolException(Throwable cause)
/* 21:   */   {
/* 22:45 */     super(cause);
/* 23:   */   }
/* 24:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.SpdyProtocolException
 * JD-Core Version:    0.7.0.1
 */