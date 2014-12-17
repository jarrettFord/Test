/*  1:   */ package io.netty.handler.codec.http;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.StringUtil;
/*  4:   */ import java.util.Map.Entry;
/*  5:   */ 
/*  6:   */ public abstract class DefaultHttpMessage
/*  7:   */   extends DefaultHttpObject
/*  8:   */   implements HttpMessage
/*  9:   */ {
/* 10:   */   private HttpVersion version;
/* 11:   */   private final HttpHeaders headers;
/* 12:   */   
/* 13:   */   protected DefaultHttpMessage(HttpVersion version)
/* 14:   */   {
/* 15:34 */     this(version, true);
/* 16:   */   }
/* 17:   */   
/* 18:   */   protected DefaultHttpMessage(HttpVersion version, boolean validate)
/* 19:   */   {
/* 20:38 */     if (version == null) {
/* 21:39 */       throw new NullPointerException("version");
/* 22:   */     }
/* 23:41 */     this.version = version;
/* 24:42 */     this.headers = new DefaultHttpHeaders(validate);
/* 25:   */   }
/* 26:   */   
/* 27:   */   public HttpHeaders headers()
/* 28:   */   {
/* 29:47 */     return this.headers;
/* 30:   */   }
/* 31:   */   
/* 32:   */   public HttpVersion getProtocolVersion()
/* 33:   */   {
/* 34:52 */     return this.version;
/* 35:   */   }
/* 36:   */   
/* 37:   */   public String toString()
/* 38:   */   {
/* 39:57 */     StringBuilder buf = new StringBuilder();
/* 40:58 */     buf.append(StringUtil.simpleClassName(this));
/* 41:59 */     buf.append("(version: ");
/* 42:60 */     buf.append(getProtocolVersion().text());
/* 43:61 */     buf.append(", keepAlive: ");
/* 44:62 */     buf.append(HttpHeaders.isKeepAlive(this));
/* 45:63 */     buf.append(')');
/* 46:64 */     buf.append(StringUtil.NEWLINE);
/* 47:65 */     appendHeaders(buf);
/* 48:   */     
/* 49:   */ 
/* 50:68 */     buf.setLength(buf.length() - StringUtil.NEWLINE.length());
/* 51:69 */     return buf.toString();
/* 52:   */   }
/* 53:   */   
/* 54:   */   public HttpMessage setProtocolVersion(HttpVersion version)
/* 55:   */   {
/* 56:74 */     if (version == null) {
/* 57:75 */       throw new NullPointerException("version");
/* 58:   */     }
/* 59:77 */     this.version = version;
/* 60:78 */     return this;
/* 61:   */   }
/* 62:   */   
/* 63:   */   void appendHeaders(StringBuilder buf)
/* 64:   */   {
/* 65:82 */     for (Map.Entry<String, String> e : headers())
/* 66:   */     {
/* 67:83 */       buf.append((String)e.getKey());
/* 68:84 */       buf.append(": ");
/* 69:85 */       buf.append((String)e.getValue());
/* 70:86 */       buf.append(StringUtil.NEWLINE);
/* 71:   */     }
/* 72:   */   }
/* 73:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.DefaultHttpMessage
 * JD-Core Version:    0.7.0.1
 */