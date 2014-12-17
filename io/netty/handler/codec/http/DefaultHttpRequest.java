/*   1:    */ package io.netty.handler.codec.http;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.StringUtil;
/*   4:    */ 
/*   5:    */ public class DefaultHttpRequest
/*   6:    */   extends DefaultHttpMessage
/*   7:    */   implements HttpRequest
/*   8:    */ {
/*   9:    */   private HttpMethod method;
/*  10:    */   private String uri;
/*  11:    */   
/*  12:    */   public DefaultHttpRequest(HttpVersion httpVersion, HttpMethod method, String uri)
/*  13:    */   {
/*  14: 36 */     this(httpVersion, method, uri, true);
/*  15:    */   }
/*  16:    */   
/*  17:    */   public DefaultHttpRequest(HttpVersion httpVersion, HttpMethod method, String uri, boolean validateHeaders)
/*  18:    */   {
/*  19: 48 */     super(httpVersion, validateHeaders);
/*  20: 49 */     if (method == null) {
/*  21: 50 */       throw new NullPointerException("method");
/*  22:    */     }
/*  23: 52 */     if (uri == null) {
/*  24: 53 */       throw new NullPointerException("uri");
/*  25:    */     }
/*  26: 55 */     this.method = method;
/*  27: 56 */     this.uri = uri;
/*  28:    */   }
/*  29:    */   
/*  30:    */   public HttpMethod getMethod()
/*  31:    */   {
/*  32: 61 */     return this.method;
/*  33:    */   }
/*  34:    */   
/*  35:    */   public String getUri()
/*  36:    */   {
/*  37: 66 */     return this.uri;
/*  38:    */   }
/*  39:    */   
/*  40:    */   public HttpRequest setMethod(HttpMethod method)
/*  41:    */   {
/*  42: 71 */     if (method == null) {
/*  43: 72 */       throw new NullPointerException("method");
/*  44:    */     }
/*  45: 74 */     this.method = method;
/*  46: 75 */     return this;
/*  47:    */   }
/*  48:    */   
/*  49:    */   public HttpRequest setUri(String uri)
/*  50:    */   {
/*  51: 80 */     if (uri == null) {
/*  52: 81 */       throw new NullPointerException("uri");
/*  53:    */     }
/*  54: 83 */     this.uri = uri;
/*  55: 84 */     return this;
/*  56:    */   }
/*  57:    */   
/*  58:    */   public HttpRequest setProtocolVersion(HttpVersion version)
/*  59:    */   {
/*  60: 89 */     super.setProtocolVersion(version);
/*  61: 90 */     return this;
/*  62:    */   }
/*  63:    */   
/*  64:    */   public String toString()
/*  65:    */   {
/*  66: 95 */     StringBuilder buf = new StringBuilder();
/*  67: 96 */     buf.append(StringUtil.simpleClassName(this));
/*  68: 97 */     buf.append("(decodeResult: ");
/*  69: 98 */     buf.append(getDecoderResult());
/*  70: 99 */     buf.append(')');
/*  71:100 */     buf.append(StringUtil.NEWLINE);
/*  72:101 */     buf.append(getMethod());
/*  73:102 */     buf.append(' ');
/*  74:103 */     buf.append(getUri());
/*  75:104 */     buf.append(' ');
/*  76:105 */     buf.append(getProtocolVersion().text());
/*  77:106 */     buf.append(StringUtil.NEWLINE);
/*  78:107 */     appendHeaders(buf);
/*  79:    */     
/*  80:    */ 
/*  81:110 */     buf.setLength(buf.length() - StringUtil.NEWLINE.length());
/*  82:111 */     return buf.toString();
/*  83:    */   }
/*  84:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.DefaultHttpRequest
 * JD-Core Version:    0.7.0.1
 */