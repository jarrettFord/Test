/*  1:   */ package io.netty.handler.codec.socks;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ 
/*  5:   */ public final class SocksAuthResponse
/*  6:   */   extends SocksResponse
/*  7:   */ {
/*  8:27 */   private static final SocksSubnegotiationVersion SUBNEGOTIATION_VERSION = SocksSubnegotiationVersion.AUTH_PASSWORD;
/*  9:   */   private final SocksAuthStatus authStatus;
/* 10:   */   
/* 11:   */   public SocksAuthResponse(SocksAuthStatus authStatus)
/* 12:   */   {
/* 13:31 */     super(SocksResponseType.AUTH);
/* 14:32 */     if (authStatus == null) {
/* 15:33 */       throw new NullPointerException("authStatus");
/* 16:   */     }
/* 17:35 */     this.authStatus = authStatus;
/* 18:   */   }
/* 19:   */   
/* 20:   */   public SocksAuthStatus authStatus()
/* 21:   */   {
/* 22:44 */     return this.authStatus;
/* 23:   */   }
/* 24:   */   
/* 25:   */   public void encodeAsByteBuf(ByteBuf byteBuf)
/* 26:   */   {
/* 27:49 */     byteBuf.writeByte(SUBNEGOTIATION_VERSION.byteValue());
/* 28:50 */     byteBuf.writeByte(this.authStatus.byteValue());
/* 29:   */   }
/* 30:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.socks.SocksAuthResponse
 * JD-Core Version:    0.7.0.1
 */