/*  1:   */ package io.netty.handler.codec.socks;
/*  2:   */ 
/*  3:   */ public enum SocksSubnegotiationVersion
/*  4:   */ {
/*  5:20 */   AUTH_PASSWORD((byte)1),  UNKNOWN((byte)-1);
/*  6:   */   
/*  7:   */   private final byte b;
/*  8:   */   
/*  9:   */   private SocksSubnegotiationVersion(byte b)
/* 10:   */   {
/* 11:26 */     this.b = b;
/* 12:   */   }
/* 13:   */   
/* 14:   */   @Deprecated
/* 15:   */   public static SocksSubnegotiationVersion fromByte(byte b)
/* 16:   */   {
/* 17:34 */     return valueOf(b);
/* 18:   */   }
/* 19:   */   
/* 20:   */   public static SocksSubnegotiationVersion valueOf(byte b)
/* 21:   */   {
/* 22:38 */     for (SocksSubnegotiationVersion code : ) {
/* 23:39 */       if (code.b == b) {
/* 24:40 */         return code;
/* 25:   */       }
/* 26:   */     }
/* 27:43 */     return UNKNOWN;
/* 28:   */   }
/* 29:   */   
/* 30:   */   public byte byteValue()
/* 31:   */   {
/* 32:47 */     return this.b;
/* 33:   */   }
/* 34:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.socks.SocksSubnegotiationVersion
 * JD-Core Version:    0.7.0.1
 */