/*  1:   */ package io.netty.handler.codec.rtsp;
/*  2:   */ 
/*  3:   */ import io.netty.handler.codec.http.HttpHeaders;
/*  4:   */ import io.netty.handler.codec.http.HttpMessage;
/*  5:   */ import io.netty.handler.codec.http.HttpObjectDecoder;
/*  6:   */ 
/*  7:   */ public abstract class RtspObjectDecoder
/*  8:   */   extends HttpObjectDecoder
/*  9:   */ {
/* 10:   */   protected RtspObjectDecoder()
/* 11:   */   {
/* 12:59 */     this(4096, 8192, 8192);
/* 13:   */   }
/* 14:   */   
/* 15:   */   protected RtspObjectDecoder(int maxInitialLineLength, int maxHeaderSize, int maxContentLength)
/* 16:   */   {
/* 17:66 */     super(maxInitialLineLength, maxHeaderSize, maxContentLength * 2, false);
/* 18:   */   }
/* 19:   */   
/* 20:   */   protected RtspObjectDecoder(int maxInitialLineLength, int maxHeaderSize, int maxContentLength, boolean validateHeaders)
/* 21:   */   {
/* 22:71 */     super(maxInitialLineLength, maxHeaderSize, maxContentLength * 2, false, validateHeaders);
/* 23:   */   }
/* 24:   */   
/* 25:   */   protected boolean isContentAlwaysEmpty(HttpMessage msg)
/* 26:   */   {
/* 27:78 */     boolean empty = super.isContentAlwaysEmpty(msg);
/* 28:79 */     if (empty) {
/* 29:80 */       return true;
/* 30:   */     }
/* 31:82 */     if (!msg.headers().contains("Content-Length")) {
/* 32:83 */       return true;
/* 33:   */     }
/* 34:85 */     return empty;
/* 35:   */   }
/* 36:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.rtsp.RtspObjectDecoder
 * JD-Core Version:    0.7.0.1
 */