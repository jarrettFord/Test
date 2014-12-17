/*  1:   */ package io.netty.handler.codec.socks;
/*  2:   */ 
/*  3:   */ public enum SocksProtocolVersion
/*  4:   */ {
/*  5:20 */   SOCKS4a((byte)4),  SOCKS5((byte)5),  UNKNOWN((byte)-1);
/*  6:   */   
/*  7:   */   private final byte b;
/*  8:   */   
/*  9:   */   private SocksProtocolVersion(byte b)
/* 10:   */   {
/* 11:27 */     this.b = b;
/* 12:   */   }
/* 13:   */   
/* 14:   */   @Deprecated
/* 15:   */   public static SocksProtocolVersion fromByte(byte b)
/* 16:   */   {
/* 17:35 */     return valueOf(b);
/* 18:   */   }
/* 19:   */   
/* 20:   */   public static SocksProtocolVersion valueOf(byte b)
/* 21:   */   {
/* 22:39 */     for (SocksProtocolVersion code : ) {
/* 23:40 */       if (code.b == b) {
/* 24:41 */         return code;
/* 25:   */       }
/* 26:   */     }
/* 27:44 */     return UNKNOWN;
/* 28:   */   }
/* 29:   */   
/* 30:   */   public byte byteValue()
/* 31:   */   {
/* 32:48 */     return this.b;
/* 33:   */   }
/* 34:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.socks.SocksProtocolVersion
 * JD-Core Version:    0.7.0.1
 */