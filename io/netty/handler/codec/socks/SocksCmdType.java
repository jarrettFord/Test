/*  1:   */ package io.netty.handler.codec.socks;
/*  2:   */ 
/*  3:   */ public enum SocksCmdType
/*  4:   */ {
/*  5:20 */   CONNECT((byte)1),  BIND((byte)2),  UDP((byte)3),  UNKNOWN((byte)-1);
/*  6:   */   
/*  7:   */   private final byte b;
/*  8:   */   
/*  9:   */   private SocksCmdType(byte b)
/* 10:   */   {
/* 11:28 */     this.b = b;
/* 12:   */   }
/* 13:   */   
/* 14:   */   @Deprecated
/* 15:   */   public static SocksCmdType fromByte(byte b)
/* 16:   */   {
/* 17:36 */     return valueOf(b);
/* 18:   */   }
/* 19:   */   
/* 20:   */   public static SocksCmdType valueOf(byte b)
/* 21:   */   {
/* 22:40 */     for (SocksCmdType code : ) {
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
 * Qualified Name:     io.netty.handler.codec.socks.SocksCmdType
 * JD-Core Version:    0.7.0.1
 */