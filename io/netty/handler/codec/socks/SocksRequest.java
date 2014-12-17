/*  1:   */ package io.netty.handler.codec.socks;
/*  2:   */ 
/*  3:   */ public abstract class SocksRequest
/*  4:   */   extends SocksMessage
/*  5:   */ {
/*  6:   */   private final SocksRequestType requestType;
/*  7:   */   
/*  8:   */   protected SocksRequest(SocksRequestType requestType)
/*  9:   */   {
/* 10:31 */     super(SocksMessageType.REQUEST);
/* 11:32 */     if (requestType == null) {
/* 12:33 */       throw new NullPointerException("requestType");
/* 13:   */     }
/* 14:35 */     this.requestType = requestType;
/* 15:   */   }
/* 16:   */   
/* 17:   */   public SocksRequestType requestType()
/* 18:   */   {
/* 19:44 */     return this.requestType;
/* 20:   */   }
/* 21:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.socks.SocksRequest
 * JD-Core Version:    0.7.0.1
 */