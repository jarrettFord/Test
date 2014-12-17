/*  1:   */ package io.netty.handler.codec.socks;
/*  2:   */ 
/*  3:   */ public enum SocksAuthStatus
/*  4:   */ {
/*  5:20 */   SUCCESS((byte)0),  FAILURE((byte)-1);
/*  6:   */   
/*  7:   */   private final byte b;
/*  8:   */   
/*  9:   */   private SocksAuthStatus(byte b)
/* 10:   */   {
/* 11:26 */     this.b = b;
/* 12:   */   }
/* 13:   */   
/* 14:   */   @Deprecated
/* 15:   */   public static SocksAuthStatus fromByte(byte b)
/* 16:   */   {
/* 17:34 */     return valueOf(b);
/* 18:   */   }
/* 19:   */   
/* 20:   */   public static SocksAuthStatus valueOf(byte b)
/* 21:   */   {
/* 22:38 */     for (SocksAuthStatus code : ) {
/* 23:39 */       if (code.b == b) {
/* 24:40 */         return code;
/* 25:   */       }
/* 26:   */     }
/* 27:43 */     return FAILURE;
/* 28:   */   }
/* 29:   */   
/* 30:   */   public byte byteValue()
/* 31:   */   {
/* 32:47 */     return this.b;
/* 33:   */   }
/* 34:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.socks.SocksAuthStatus
 * JD-Core Version:    0.7.0.1
 */