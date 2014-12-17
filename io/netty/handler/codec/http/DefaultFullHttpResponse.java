/*   1:    */ package io.netty.handler.codec.http;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.Unpooled;
/*   5:    */ 
/*   6:    */ public class DefaultFullHttpResponse
/*   7:    */   extends DefaultHttpResponse
/*   8:    */   implements FullHttpResponse
/*   9:    */ {
/*  10:    */   private final ByteBuf content;
/*  11:    */   private final HttpHeaders trailingHeaders;
/*  12:    */   private final boolean validateHeaders;
/*  13:    */   
/*  14:    */   public DefaultFullHttpResponse(HttpVersion version, HttpResponseStatus status)
/*  15:    */   {
/*  16: 32 */     this(version, status, Unpooled.buffer(0));
/*  17:    */   }
/*  18:    */   
/*  19:    */   public DefaultFullHttpResponse(HttpVersion version, HttpResponseStatus status, ByteBuf content)
/*  20:    */   {
/*  21: 36 */     this(version, status, content, true);
/*  22:    */   }
/*  23:    */   
/*  24:    */   public DefaultFullHttpResponse(HttpVersion version, HttpResponseStatus status, ByteBuf content, boolean validateHeaders)
/*  25:    */   {
/*  26: 41 */     super(version, status, validateHeaders);
/*  27: 42 */     if (content == null) {
/*  28: 43 */       throw new NullPointerException("content");
/*  29:    */     }
/*  30: 45 */     this.content = content;
/*  31: 46 */     this.trailingHeaders = new DefaultHttpHeaders(validateHeaders);
/*  32: 47 */     this.validateHeaders = validateHeaders;
/*  33:    */   }
/*  34:    */   
/*  35:    */   public HttpHeaders trailingHeaders()
/*  36:    */   {
/*  37: 52 */     return this.trailingHeaders;
/*  38:    */   }
/*  39:    */   
/*  40:    */   public ByteBuf content()
/*  41:    */   {
/*  42: 57 */     return this.content;
/*  43:    */   }
/*  44:    */   
/*  45:    */   public int refCnt()
/*  46:    */   {
/*  47: 62 */     return this.content.refCnt();
/*  48:    */   }
/*  49:    */   
/*  50:    */   public FullHttpResponse retain()
/*  51:    */   {
/*  52: 67 */     this.content.retain();
/*  53: 68 */     return this;
/*  54:    */   }
/*  55:    */   
/*  56:    */   public FullHttpResponse retain(int increment)
/*  57:    */   {
/*  58: 73 */     this.content.retain(increment);
/*  59: 74 */     return this;
/*  60:    */   }
/*  61:    */   
/*  62:    */   public boolean release()
/*  63:    */   {
/*  64: 79 */     return this.content.release();
/*  65:    */   }
/*  66:    */   
/*  67:    */   public boolean release(int decrement)
/*  68:    */   {
/*  69: 84 */     return this.content.release(decrement);
/*  70:    */   }
/*  71:    */   
/*  72:    */   public FullHttpResponse setProtocolVersion(HttpVersion version)
/*  73:    */   {
/*  74: 89 */     super.setProtocolVersion(version);
/*  75: 90 */     return this;
/*  76:    */   }
/*  77:    */   
/*  78:    */   public FullHttpResponse setStatus(HttpResponseStatus status)
/*  79:    */   {
/*  80: 95 */     super.setStatus(status);
/*  81: 96 */     return this;
/*  82:    */   }
/*  83:    */   
/*  84:    */   public FullHttpResponse copy()
/*  85:    */   {
/*  86:101 */     DefaultFullHttpResponse copy = new DefaultFullHttpResponse(getProtocolVersion(), getStatus(), content().copy(), this.validateHeaders);
/*  87:    */     
/*  88:103 */     copy.headers().set(headers());
/*  89:104 */     copy.trailingHeaders().set(trailingHeaders());
/*  90:105 */     return copy;
/*  91:    */   }
/*  92:    */   
/*  93:    */   public FullHttpResponse duplicate()
/*  94:    */   {
/*  95:110 */     DefaultFullHttpResponse duplicate = new DefaultFullHttpResponse(getProtocolVersion(), getStatus(), content().duplicate(), this.validateHeaders);
/*  96:    */     
/*  97:112 */     duplicate.headers().set(headers());
/*  98:113 */     duplicate.trailingHeaders().set(trailingHeaders());
/*  99:114 */     return duplicate;
/* 100:    */   }
/* 101:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.DefaultFullHttpResponse
 * JD-Core Version:    0.7.0.1
 */