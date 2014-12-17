/*  1:   */ package io.netty.handler.codec.http;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.StringUtil;
/*  4:   */ 
/*  5:   */ public class DefaultHttpResponse
/*  6:   */   extends DefaultHttpMessage
/*  7:   */   implements HttpResponse
/*  8:   */ {
/*  9:   */   private HttpResponseStatus status;
/* 10:   */   
/* 11:   */   public DefaultHttpResponse(HttpVersion version, HttpResponseStatus status)
/* 12:   */   {
/* 13:34 */     this(version, status, true);
/* 14:   */   }
/* 15:   */   
/* 16:   */   public DefaultHttpResponse(HttpVersion version, HttpResponseStatus status, boolean validateHeaders)
/* 17:   */   {
/* 18:45 */     super(version, validateHeaders);
/* 19:46 */     if (status == null) {
/* 20:47 */       throw new NullPointerException("status");
/* 21:   */     }
/* 22:49 */     this.status = status;
/* 23:   */   }
/* 24:   */   
/* 25:   */   public HttpResponseStatus getStatus()
/* 26:   */   {
/* 27:54 */     return this.status;
/* 28:   */   }
/* 29:   */   
/* 30:   */   public HttpResponse setStatus(HttpResponseStatus status)
/* 31:   */   {
/* 32:59 */     if (status == null) {
/* 33:60 */       throw new NullPointerException("status");
/* 34:   */     }
/* 35:62 */     this.status = status;
/* 36:63 */     return this;
/* 37:   */   }
/* 38:   */   
/* 39:   */   public HttpResponse setProtocolVersion(HttpVersion version)
/* 40:   */   {
/* 41:68 */     super.setProtocolVersion(version);
/* 42:69 */     return this;
/* 43:   */   }
/* 44:   */   
/* 45:   */   public String toString()
/* 46:   */   {
/* 47:74 */     StringBuilder buf = new StringBuilder();
/* 48:75 */     buf.append(StringUtil.simpleClassName(this));
/* 49:76 */     buf.append("(decodeResult: ");
/* 50:77 */     buf.append(getDecoderResult());
/* 51:78 */     buf.append(')');
/* 52:79 */     buf.append(StringUtil.NEWLINE);
/* 53:80 */     buf.append(getProtocolVersion().text());
/* 54:81 */     buf.append(' ');
/* 55:82 */     buf.append(getStatus());
/* 56:83 */     buf.append(StringUtil.NEWLINE);
/* 57:84 */     appendHeaders(buf);
/* 58:   */     
/* 59:   */ 
/* 60:87 */     buf.setLength(buf.length() - StringUtil.NEWLINE.length());
/* 61:88 */     return buf.toString();
/* 62:   */   }
/* 63:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.DefaultHttpResponse
 * JD-Core Version:    0.7.0.1
 */