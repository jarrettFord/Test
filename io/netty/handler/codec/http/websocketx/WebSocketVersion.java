/*  1:   */ package io.netty.handler.codec.http.websocketx;
/*  2:   */ 
/*  3:   */ public enum WebSocketVersion
/*  4:   */ {
/*  5:28 */   UNKNOWN,  V00,  V07,  V08,  V13;
/*  6:   */   
/*  7:   */   private WebSocketVersion() {}
/*  8:   */   
/*  9:   */   public String toHttpHeaderValue()
/* 10:   */   {
/* 11:59 */     if (this == V00) {
/* 12:60 */       return "0";
/* 13:   */     }
/* 14:62 */     if (this == V07) {
/* 15:63 */       return "7";
/* 16:   */     }
/* 17:65 */     if (this == V08) {
/* 18:66 */       return "8";
/* 19:   */     }
/* 20:68 */     if (this == V13) {
/* 21:69 */       return "13";
/* 22:   */     }
/* 23:71 */     throw new IllegalStateException("Unknown web socket version: " + this);
/* 24:   */   }
/* 25:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.websocketx.WebSocketVersion
 * JD-Core Version:    0.7.0.1
 */