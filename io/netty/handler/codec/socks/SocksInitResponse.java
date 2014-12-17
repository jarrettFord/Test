/*  1:   */ package io.netty.handler.codec.socks;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ 
/*  5:   */ public final class SocksInitResponse
/*  6:   */   extends SocksResponse
/*  7:   */ {
/*  8:   */   private final SocksAuthScheme authScheme;
/*  9:   */   
/* 10:   */   public SocksInitResponse(SocksAuthScheme authScheme)
/* 11:   */   {
/* 12:30 */     super(SocksResponseType.INIT);
/* 13:31 */     if (authScheme == null) {
/* 14:32 */       throw new NullPointerException("authScheme");
/* 15:   */     }
/* 16:34 */     this.authScheme = authScheme;
/* 17:   */   }
/* 18:   */   
/* 19:   */   public SocksAuthScheme authScheme()
/* 20:   */   {
/* 21:43 */     return this.authScheme;
/* 22:   */   }
/* 23:   */   
/* 24:   */   public void encodeAsByteBuf(ByteBuf byteBuf)
/* 25:   */   {
/* 26:48 */     byteBuf.writeByte(protocolVersion().byteValue());
/* 27:49 */     byteBuf.writeByte(this.authScheme.byteValue());
/* 28:   */   }
/* 29:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.socks.SocksInitResponse
 * JD-Core Version:    0.7.0.1
 */