/*  1:   */ package io.netty.handler.codec.rtsp;
/*  2:   */ 
/*  3:   */ import io.netty.handler.codec.http.DefaultHttpResponse;
/*  4:   */ import io.netty.handler.codec.http.HttpMessage;
/*  5:   */ import io.netty.handler.codec.http.HttpResponseStatus;
/*  6:   */ 
/*  7:   */ public class RtspResponseDecoder
/*  8:   */   extends RtspObjectDecoder
/*  9:   */ {
/* 10:54 */   private static final HttpResponseStatus UNKNOWN_STATUS = new HttpResponseStatus(999, "Unknown");
/* 11:   */   
/* 12:   */   public RtspResponseDecoder() {}
/* 13:   */   
/* 14:   */   public RtspResponseDecoder(int maxInitialLineLength, int maxHeaderSize, int maxContentLength)
/* 15:   */   {
/* 16:69 */     super(maxInitialLineLength, maxHeaderSize, maxContentLength);
/* 17:   */   }
/* 18:   */   
/* 19:   */   public RtspResponseDecoder(int maxInitialLineLength, int maxHeaderSize, int maxContentLength, boolean validateHeaders)
/* 20:   */   {
/* 21:74 */     super(maxInitialLineLength, maxHeaderSize, maxContentLength, validateHeaders);
/* 22:   */   }
/* 23:   */   
/* 24:   */   protected HttpMessage createMessage(String[] initialLine)
/* 25:   */     throws Exception
/* 26:   */   {
/* 27:79 */     return new DefaultHttpResponse(RtspVersions.valueOf(initialLine[0]), new HttpResponseStatus(Integer.valueOf(initialLine[1]).intValue(), initialLine[2]), this.validateHeaders);
/* 28:   */   }
/* 29:   */   
/* 30:   */   protected HttpMessage createInvalidMessage()
/* 31:   */   {
/* 32:86 */     return new DefaultHttpResponse(RtspVersions.RTSP_1_0, UNKNOWN_STATUS, this.validateHeaders);
/* 33:   */   }
/* 34:   */   
/* 35:   */   protected boolean isDecodingRequest()
/* 36:   */   {
/* 37:91 */     return false;
/* 38:   */   }
/* 39:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.rtsp.RtspResponseDecoder
 * JD-Core Version:    0.7.0.1
 */