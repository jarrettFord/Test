/*   1:    */ package io.netty.handler.codec.http;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.Unpooled;
/*   5:    */ import io.netty.util.internal.StringUtil;
/*   6:    */ import java.util.Map.Entry;
/*   7:    */ 
/*   8:    */ public class DefaultLastHttpContent
/*   9:    */   extends DefaultHttpContent
/*  10:    */   implements LastHttpContent
/*  11:    */ {
/*  12:    */   private final HttpHeaders trailingHeaders;
/*  13:    */   private final boolean validateHeaders;
/*  14:    */   
/*  15:    */   public DefaultLastHttpContent()
/*  16:    */   {
/*  17: 33 */     this(Unpooled.buffer(0));
/*  18:    */   }
/*  19:    */   
/*  20:    */   public DefaultLastHttpContent(ByteBuf content)
/*  21:    */   {
/*  22: 37 */     this(content, true);
/*  23:    */   }
/*  24:    */   
/*  25:    */   public DefaultLastHttpContent(ByteBuf content, boolean validateHeaders)
/*  26:    */   {
/*  27: 41 */     super(content);
/*  28: 42 */     this.trailingHeaders = new TrailingHeaders(validateHeaders);
/*  29: 43 */     this.validateHeaders = validateHeaders;
/*  30:    */   }
/*  31:    */   
/*  32:    */   public LastHttpContent copy()
/*  33:    */   {
/*  34: 48 */     DefaultLastHttpContent copy = new DefaultLastHttpContent(content().copy(), this.validateHeaders);
/*  35: 49 */     copy.trailingHeaders().set(trailingHeaders());
/*  36: 50 */     return copy;
/*  37:    */   }
/*  38:    */   
/*  39:    */   public LastHttpContent duplicate()
/*  40:    */   {
/*  41: 55 */     DefaultLastHttpContent copy = new DefaultLastHttpContent(content().duplicate(), this.validateHeaders);
/*  42: 56 */     copy.trailingHeaders().set(trailingHeaders());
/*  43: 57 */     return copy;
/*  44:    */   }
/*  45:    */   
/*  46:    */   public LastHttpContent retain(int increment)
/*  47:    */   {
/*  48: 62 */     super.retain(increment);
/*  49: 63 */     return this;
/*  50:    */   }
/*  51:    */   
/*  52:    */   public LastHttpContent retain()
/*  53:    */   {
/*  54: 68 */     super.retain();
/*  55: 69 */     return this;
/*  56:    */   }
/*  57:    */   
/*  58:    */   public HttpHeaders trailingHeaders()
/*  59:    */   {
/*  60: 74 */     return this.trailingHeaders;
/*  61:    */   }
/*  62:    */   
/*  63:    */   public String toString()
/*  64:    */   {
/*  65: 79 */     StringBuilder buf = new StringBuilder(super.toString());
/*  66: 80 */     buf.append(StringUtil.NEWLINE);
/*  67: 81 */     appendHeaders(buf);
/*  68:    */     
/*  69:    */ 
/*  70: 84 */     buf.setLength(buf.length() - StringUtil.NEWLINE.length());
/*  71: 85 */     return buf.toString();
/*  72:    */   }
/*  73:    */   
/*  74:    */   private void appendHeaders(StringBuilder buf)
/*  75:    */   {
/*  76: 89 */     for (Map.Entry<String, String> e : trailingHeaders())
/*  77:    */     {
/*  78: 90 */       buf.append((String)e.getKey());
/*  79: 91 */       buf.append(": ");
/*  80: 92 */       buf.append((String)e.getValue());
/*  81: 93 */       buf.append(StringUtil.NEWLINE);
/*  82:    */     }
/*  83:    */   }
/*  84:    */   
/*  85:    */   private static final class TrailingHeaders
/*  86:    */     extends DefaultHttpHeaders
/*  87:    */   {
/*  88:    */     TrailingHeaders(boolean validate)
/*  89:    */     {
/*  90: 99 */       super();
/*  91:    */     }
/*  92:    */     
/*  93:    */     void validateHeaderName0(CharSequence name)
/*  94:    */     {
/*  95:104 */       super.validateHeaderName0(name);
/*  96:105 */       if ((HttpHeaders.equalsIgnoreCase("Content-Length", name)) || (HttpHeaders.equalsIgnoreCase("Transfer-Encoding", name)) || (HttpHeaders.equalsIgnoreCase("Trailer", name))) {
/*  97:108 */         throw new IllegalArgumentException("prohibited trailing header: " + name);
/*  98:    */       }
/*  99:    */     }
/* 100:    */   }
/* 101:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.DefaultLastHttpContent
 * JD-Core Version:    0.7.0.1
 */