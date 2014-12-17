/*  1:   */ package io.netty.handler.codec.socks;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ 
/*  5:   */ public final class UnknownSocksResponse
/*  6:   */   extends SocksResponse
/*  7:   */ {
/*  8:   */   public UnknownSocksResponse()
/*  9:   */   {
/* 10:30 */     super(SocksResponseType.UNKNOWN);
/* 11:   */   }
/* 12:   */   
/* 13:   */   public void encodeAsByteBuf(ByteBuf byteBuf) {}
/* 14:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.socks.UnknownSocksResponse
 * JD-Core Version:    0.7.0.1
 */