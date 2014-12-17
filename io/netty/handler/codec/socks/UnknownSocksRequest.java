/*  1:   */ package io.netty.handler.codec.socks;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ 
/*  5:   */ public final class UnknownSocksRequest
/*  6:   */   extends SocksRequest
/*  7:   */ {
/*  8:   */   public UnknownSocksRequest()
/*  9:   */   {
/* 10:30 */     super(SocksRequestType.UNKNOWN);
/* 11:   */   }
/* 12:   */   
/* 13:   */   public void encodeAsByteBuf(ByteBuf byteBuf) {}
/* 14:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.socks.UnknownSocksRequest
 * JD-Core Version:    0.7.0.1
 */