/*  1:   */ package io.netty.handler.codec.http;
/*  2:   */ 
/*  3:   */ public class HttpRequestDecoder
/*  4:   */   extends HttpObjectDecoder
/*  5:   */ {
/*  6:   */   public HttpRequestDecoder() {}
/*  7:   */   
/*  8:   */   public HttpRequestDecoder(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize)
/*  9:   */   {
/* 10:70 */     super(maxInitialLineLength, maxHeaderSize, maxChunkSize, true);
/* 11:   */   }
/* 12:   */   
/* 13:   */   public HttpRequestDecoder(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean validateHeaders)
/* 14:   */   {
/* 15:75 */     super(maxInitialLineLength, maxHeaderSize, maxChunkSize, true, validateHeaders);
/* 16:   */   }
/* 17:   */   
/* 18:   */   protected HttpMessage createMessage(String[] initialLine)
/* 19:   */     throws Exception
/* 20:   */   {
/* 21:80 */     return new DefaultHttpRequest(HttpVersion.valueOf(initialLine[2]), HttpMethod.valueOf(initialLine[0]), initialLine[1], this.validateHeaders);
/* 22:   */   }
/* 23:   */   
/* 24:   */   protected HttpMessage createInvalidMessage()
/* 25:   */   {
/* 26:87 */     return new DefaultHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, "/bad-request", this.validateHeaders);
/* 27:   */   }
/* 28:   */   
/* 29:   */   protected boolean isDecodingRequest()
/* 30:   */   {
/* 31:92 */     return true;
/* 32:   */   }
/* 33:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.HttpRequestDecoder
 * JD-Core Version:    0.7.0.1
 */