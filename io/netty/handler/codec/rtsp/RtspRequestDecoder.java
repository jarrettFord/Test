/*  1:   */ package io.netty.handler.codec.rtsp;
/*  2:   */ 
/*  3:   */ import io.netty.handler.codec.http.DefaultHttpRequest;
/*  4:   */ import io.netty.handler.codec.http.HttpMessage;
/*  5:   */ 
/*  6:   */ public class RtspRequestDecoder
/*  7:   */   extends RtspObjectDecoder
/*  8:   */ {
/*  9:   */   public RtspRequestDecoder() {}
/* 10:   */   
/* 11:   */   public RtspRequestDecoder(int maxInitialLineLength, int maxHeaderSize, int maxContentLength)
/* 12:   */   {
/* 13:65 */     super(maxInitialLineLength, maxHeaderSize, maxContentLength);
/* 14:   */   }
/* 15:   */   
/* 16:   */   public RtspRequestDecoder(int maxInitialLineLength, int maxHeaderSize, int maxContentLength, boolean validateHeaders)
/* 17:   */   {
/* 18:70 */     super(maxInitialLineLength, maxHeaderSize, maxContentLength, validateHeaders);
/* 19:   */   }
/* 20:   */   
/* 21:   */   protected HttpMessage createMessage(String[] initialLine)
/* 22:   */     throws Exception
/* 23:   */   {
/* 24:75 */     return new DefaultHttpRequest(RtspVersions.valueOf(initialLine[2]), RtspMethods.valueOf(initialLine[0]), initialLine[1], this.validateHeaders);
/* 25:   */   }
/* 26:   */   
/* 27:   */   protected HttpMessage createInvalidMessage()
/* 28:   */   {
/* 29:81 */     return new DefaultHttpRequest(RtspVersions.RTSP_1_0, RtspMethods.OPTIONS, "/bad-request", this.validateHeaders);
/* 30:   */   }
/* 31:   */   
/* 32:   */   protected boolean isDecodingRequest()
/* 33:   */   {
/* 34:86 */     return true;
/* 35:   */   }
/* 36:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.rtsp.RtspRequestDecoder
 * JD-Core Version:    0.7.0.1
 */