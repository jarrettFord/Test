/*   1:    */ package io.netty.handler.codec.http;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.Unpooled;
/*   5:    */ 
/*   6:    */ public class DefaultFullHttpRequest
/*   7:    */   extends DefaultHttpRequest
/*   8:    */   implements FullHttpRequest
/*   9:    */ {
/*  10:    */   private final ByteBuf content;
/*  11:    */   private final HttpHeaders trailingHeader;
/*  12:    */   private final boolean validateHeaders;
/*  13:    */   
/*  14:    */   public DefaultFullHttpRequest(HttpVersion httpVersion, HttpMethod method, String uri)
/*  15:    */   {
/*  16: 30 */     this(httpVersion, method, uri, Unpooled.buffer(0));
/*  17:    */   }
/*  18:    */   
/*  19:    */   public DefaultFullHttpRequest(HttpVersion httpVersion, HttpMethod method, String uri, ByteBuf content)
/*  20:    */   {
/*  21: 34 */     this(httpVersion, method, uri, content, true);
/*  22:    */   }
/*  23:    */   
/*  24:    */   public DefaultFullHttpRequest(HttpVersion httpVersion, HttpMethod method, String uri, ByteBuf content, boolean validateHeaders)
/*  25:    */   {
/*  26: 39 */     super(httpVersion, method, uri, validateHeaders);
/*  27: 40 */     if (content == null) {
/*  28: 41 */       throw new NullPointerException("content");
/*  29:    */     }
/*  30: 43 */     this.content = content;
/*  31: 44 */     this.trailingHeader = new DefaultHttpHeaders(validateHeaders);
/*  32: 45 */     this.validateHeaders = validateHeaders;
/*  33:    */   }
/*  34:    */   
/*  35:    */   public HttpHeaders trailingHeaders()
/*  36:    */   {
/*  37: 50 */     return this.trailingHeader;
/*  38:    */   }
/*  39:    */   
/*  40:    */   public ByteBuf content()
/*  41:    */   {
/*  42: 55 */     return this.content;
/*  43:    */   }
/*  44:    */   
/*  45:    */   public int refCnt()
/*  46:    */   {
/*  47: 60 */     return this.content.refCnt();
/*  48:    */   }
/*  49:    */   
/*  50:    */   public FullHttpRequest retain()
/*  51:    */   {
/*  52: 65 */     this.content.retain();
/*  53: 66 */     return this;
/*  54:    */   }
/*  55:    */   
/*  56:    */   public FullHttpRequest retain(int increment)
/*  57:    */   {
/*  58: 71 */     this.content.retain(increment);
/*  59: 72 */     return this;
/*  60:    */   }
/*  61:    */   
/*  62:    */   public boolean release()
/*  63:    */   {
/*  64: 77 */     return this.content.release();
/*  65:    */   }
/*  66:    */   
/*  67:    */   public boolean release(int decrement)
/*  68:    */   {
/*  69: 82 */     return this.content.release(decrement);
/*  70:    */   }
/*  71:    */   
/*  72:    */   public FullHttpRequest setProtocolVersion(HttpVersion version)
/*  73:    */   {
/*  74: 87 */     super.setProtocolVersion(version);
/*  75: 88 */     return this;
/*  76:    */   }
/*  77:    */   
/*  78:    */   public FullHttpRequest setMethod(HttpMethod method)
/*  79:    */   {
/*  80: 93 */     super.setMethod(method);
/*  81: 94 */     return this;
/*  82:    */   }
/*  83:    */   
/*  84:    */   public FullHttpRequest setUri(String uri)
/*  85:    */   {
/*  86: 99 */     super.setUri(uri);
/*  87:100 */     return this;
/*  88:    */   }
/*  89:    */   
/*  90:    */   public FullHttpRequest copy()
/*  91:    */   {
/*  92:105 */     DefaultFullHttpRequest copy = new DefaultFullHttpRequest(getProtocolVersion(), getMethod(), getUri(), content().copy(), this.validateHeaders);
/*  93:    */     
/*  94:107 */     copy.headers().set(headers());
/*  95:108 */     copy.trailingHeaders().set(trailingHeaders());
/*  96:109 */     return copy;
/*  97:    */   }
/*  98:    */   
/*  99:    */   public FullHttpRequest duplicate()
/* 100:    */   {
/* 101:114 */     DefaultFullHttpRequest duplicate = new DefaultFullHttpRequest(getProtocolVersion(), getMethod(), getUri(), content().duplicate(), this.validateHeaders);
/* 102:    */     
/* 103:116 */     duplicate.headers().set(headers());
/* 104:117 */     duplicate.trailingHeaders().set(trailingHeaders());
/* 105:118 */     return duplicate;
/* 106:    */   }
/* 107:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.DefaultFullHttpRequest
 * JD-Core Version:    0.7.0.1
 */