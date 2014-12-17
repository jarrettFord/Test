/*  1:   */ package io.netty.handler.codec.rtsp;
/*  2:   */ 
/*  3:   */ import io.netty.handler.codec.http.HttpVersion;
/*  4:   */ 
/*  5:   */ public final class RtspVersions
/*  6:   */ {
/*  7:28 */   public static final HttpVersion RTSP_1_0 = new HttpVersion("RTSP", 1, 0, true);
/*  8:   */   
/*  9:   */   public static HttpVersion valueOf(String text)
/* 10:   */   {
/* 11:37 */     if (text == null) {
/* 12:38 */       throw new NullPointerException("text");
/* 13:   */     }
/* 14:41 */     text = text.trim().toUpperCase();
/* 15:42 */     if ("RTSP/1.0".equals(text)) {
/* 16:43 */       return RTSP_1_0;
/* 17:   */     }
/* 18:46 */     return new HttpVersion(text, true);
/* 19:   */   }
/* 20:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.rtsp.RtspVersions
 * JD-Core Version:    0.7.0.1
 */