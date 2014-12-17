/*  1:   */ package io.netty.handler.codec.http;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.util.CharsetUtil;
/*  5:   */ 
/*  6:   */ public class HttpRequestEncoder
/*  7:   */   extends HttpObjectEncoder<HttpRequest>
/*  8:   */ {
/*  9:   */   private static final char SLASH = '/';
/* 10:29 */   private static final byte[] CRLF = { 13, 10 };
/* 11:   */   
/* 12:   */   public boolean acceptOutboundMessage(Object msg)
/* 13:   */     throws Exception
/* 14:   */   {
/* 15:33 */     return (super.acceptOutboundMessage(msg)) && (!(msg instanceof HttpResponse));
/* 16:   */   }
/* 17:   */   
/* 18:   */   protected void encodeInitialLine(ByteBuf buf, HttpRequest request)
/* 19:   */     throws Exception
/* 20:   */   {
/* 21:38 */     request.getMethod().encode(buf);
/* 22:39 */     buf.writeByte(32);
/* 23:   */     
/* 24:   */ 
/* 25:   */ 
/* 26:43 */     String uri = request.getUri();
/* 27:45 */     if (uri.length() == 0)
/* 28:   */     {
/* 29:46 */       uri = uri + '/';
/* 30:   */     }
/* 31:   */     else
/* 32:   */     {
/* 33:48 */       int start = uri.indexOf("://");
/* 34:49 */       if ((start != -1) && (uri.charAt(0) != '/'))
/* 35:   */       {
/* 36:50 */         int startIndex = start + 3;
/* 37:51 */         if (uri.lastIndexOf('/') <= startIndex) {
/* 38:52 */           uri = uri + '/';
/* 39:   */         }
/* 40:   */       }
/* 41:   */     }
/* 42:57 */     buf.writeBytes(uri.getBytes(CharsetUtil.UTF_8));
/* 43:   */     
/* 44:59 */     buf.writeByte(32);
/* 45:60 */     request.getProtocolVersion().encode(buf);
/* 46:61 */     buf.writeBytes(CRLF);
/* 47:   */   }
/* 48:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.HttpRequestEncoder
 * JD-Core Version:    0.7.0.1
 */