/*   1:    */ package io.netty.handler.codec.http;
/*   2:    */ 
/*   3:    */ public class HttpResponseDecoder
/*   4:    */   extends HttpObjectDecoder
/*   5:    */ {
/*   6: 86 */   private static final HttpResponseStatus UNKNOWN_STATUS = new HttpResponseStatus(999, "Unknown");
/*   7:    */   
/*   8:    */   public HttpResponseDecoder() {}
/*   9:    */   
/*  10:    */   public HttpResponseDecoder(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize)
/*  11:    */   {
/*  12:101 */     super(maxInitialLineLength, maxHeaderSize, maxChunkSize, true);
/*  13:    */   }
/*  14:    */   
/*  15:    */   public HttpResponseDecoder(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean validateHeaders)
/*  16:    */   {
/*  17:106 */     super(maxInitialLineLength, maxHeaderSize, maxChunkSize, true, validateHeaders);
/*  18:    */   }
/*  19:    */   
/*  20:    */   protected HttpMessage createMessage(String[] initialLine)
/*  21:    */   {
/*  22:111 */     return new DefaultHttpResponse(HttpVersion.valueOf(initialLine[0]), new HttpResponseStatus(Integer.valueOf(initialLine[1]).intValue(), initialLine[2]), this.validateHeaders);
/*  23:    */   }
/*  24:    */   
/*  25:    */   protected HttpMessage createInvalidMessage()
/*  26:    */   {
/*  27:118 */     return new DefaultHttpResponse(HttpVersion.HTTP_1_0, UNKNOWN_STATUS, this.validateHeaders);
/*  28:    */   }
/*  29:    */   
/*  30:    */   protected boolean isDecodingRequest()
/*  31:    */   {
/*  32:123 */     return false;
/*  33:    */   }
/*  34:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.HttpResponseDecoder
 * JD-Core Version:    0.7.0.1
 */