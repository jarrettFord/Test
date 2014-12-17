/*  1:   */ package io.netty.handler.codec.socks;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ 
/*  5:   */ public abstract class SocksMessage
/*  6:   */ {
/*  7:   */   private final SocksMessageType type;
/*  8:30 */   private final SocksProtocolVersion protocolVersion = SocksProtocolVersion.SOCKS5;
/*  9:   */   
/* 10:   */   protected SocksMessage(SocksMessageType type)
/* 11:   */   {
/* 12:33 */     if (type == null) {
/* 13:34 */       throw new NullPointerException("type");
/* 14:   */     }
/* 15:36 */     this.type = type;
/* 16:   */   }
/* 17:   */   
/* 18:   */   public SocksMessageType type()
/* 19:   */   {
/* 20:45 */     return this.type;
/* 21:   */   }
/* 22:   */   
/* 23:   */   public SocksProtocolVersion protocolVersion()
/* 24:   */   {
/* 25:54 */     return this.protocolVersion;
/* 26:   */   }
/* 27:   */   
/* 28:   */   @Deprecated
/* 29:   */   public abstract void encodeAsByteBuf(ByteBuf paramByteBuf);
/* 30:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.socks.SocksMessage
 * JD-Core Version:    0.7.0.1
 */