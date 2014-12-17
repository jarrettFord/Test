/*  1:   */ package io.netty.handler.codec.socks;
/*  2:   */ 
/*  3:   */ public enum SocksAuthScheme
/*  4:   */ {
/*  5:20 */   NO_AUTH((byte)0),  AUTH_GSSAPI((byte)1),  AUTH_PASSWORD((byte)2),  UNKNOWN((byte)-1);
/*  6:   */   
/*  7:   */   private final byte b;
/*  8:   */   
/*  9:   */   private SocksAuthScheme(byte b)
/* 10:   */   {
/* 11:28 */     this.b = b;
/* 12:   */   }
/* 13:   */   
/* 14:   */   @Deprecated
/* 15:   */   public static SocksAuthScheme fromByte(byte b)
/* 16:   */   {
/* 17:36 */     return valueOf(b);
/* 18:   */   }
/* 19:   */   
/* 20:   */   public static SocksAuthScheme valueOf(byte b)
/* 21:   */   {
/* 22:40 */     for (SocksAuthScheme code : ) {
/* 23:41 */       if (code.b == b) {
/* 24:42 */         return code;
/* 25:   */       }
/* 26:   */     }
/* 27:45 */     return UNKNOWN;
/* 28:   */   }
/* 29:   */   
/* 30:   */   public byte byteValue()
/* 31:   */   {
/* 32:49 */     return this.b;
/* 33:   */   }
/* 34:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.socks.SocksAuthScheme
 * JD-Core Version:    0.7.0.1
 */