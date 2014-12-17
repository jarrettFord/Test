/*  1:   */ package io.netty.handler.codec.socks;
/*  2:   */ 
/*  3:   */ public abstract class SocksResponse
/*  4:   */   extends SocksMessage
/*  5:   */ {
/*  6:   */   private final SocksResponseType responseType;
/*  7:   */   
/*  8:   */   protected SocksResponse(SocksResponseType responseType)
/*  9:   */   {
/* 10:31 */     super(SocksMessageType.RESPONSE);
/* 11:32 */     if (responseType == null) {
/* 12:33 */       throw new NullPointerException("responseType");
/* 13:   */     }
/* 14:35 */     this.responseType = responseType;
/* 15:   */   }
/* 16:   */   
/* 17:   */   public SocksResponseType responseType()
/* 18:   */   {
/* 19:44 */     return this.responseType;
/* 20:   */   }
/* 21:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.socks.SocksResponse
 * JD-Core Version:    0.7.0.1
 */