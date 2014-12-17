/*  1:   */ package io.netty.handler.codec.spdy;
/*  2:   */ 
/*  3:   */ public enum SpdyVersion
/*  4:   */ {
/*  5:19 */   SPDY_3_1(3, 1);
/*  6:   */   
/*  7:   */   private final int version;
/*  8:   */   private final int minorVersion;
/*  9:   */   
/* 10:   */   private SpdyVersion(int version, int minorVersion)
/* 11:   */   {
/* 12:25 */     this.version = version;
/* 13:26 */     this.minorVersion = minorVersion;
/* 14:   */   }
/* 15:   */   
/* 16:   */   int getVersion()
/* 17:   */   {
/* 18:30 */     return this.version;
/* 19:   */   }
/* 20:   */   
/* 21:   */   int getMinorVersion()
/* 22:   */   {
/* 23:34 */     return this.minorVersion;
/* 24:   */   }
/* 25:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.SpdyVersion
 * JD-Core Version:    0.7.0.1
 */