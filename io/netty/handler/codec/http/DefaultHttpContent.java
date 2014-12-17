/*  1:   */ package io.netty.handler.codec.http;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.util.internal.StringUtil;
/*  5:   */ 
/*  6:   */ public class DefaultHttpContent
/*  7:   */   extends DefaultHttpObject
/*  8:   */   implements HttpContent
/*  9:   */ {
/* 10:   */   private final ByteBuf content;
/* 11:   */   
/* 12:   */   public DefaultHttpContent(ByteBuf content)
/* 13:   */   {
/* 14:32 */     if (content == null) {
/* 15:33 */       throw new NullPointerException("content");
/* 16:   */     }
/* 17:35 */     this.content = content;
/* 18:   */   }
/* 19:   */   
/* 20:   */   public ByteBuf content()
/* 21:   */   {
/* 22:40 */     return this.content;
/* 23:   */   }
/* 24:   */   
/* 25:   */   public HttpContent copy()
/* 26:   */   {
/* 27:45 */     return new DefaultHttpContent(this.content.copy());
/* 28:   */   }
/* 29:   */   
/* 30:   */   public HttpContent duplicate()
/* 31:   */   {
/* 32:50 */     return new DefaultHttpContent(this.content.duplicate());
/* 33:   */   }
/* 34:   */   
/* 35:   */   public int refCnt()
/* 36:   */   {
/* 37:55 */     return this.content.refCnt();
/* 38:   */   }
/* 39:   */   
/* 40:   */   public HttpContent retain()
/* 41:   */   {
/* 42:60 */     this.content.retain();
/* 43:61 */     return this;
/* 44:   */   }
/* 45:   */   
/* 46:   */   public HttpContent retain(int increment)
/* 47:   */   {
/* 48:66 */     this.content.retain(increment);
/* 49:67 */     return this;
/* 50:   */   }
/* 51:   */   
/* 52:   */   public boolean release()
/* 53:   */   {
/* 54:72 */     return this.content.release();
/* 55:   */   }
/* 56:   */   
/* 57:   */   public boolean release(int decrement)
/* 58:   */   {
/* 59:77 */     return this.content.release(decrement);
/* 60:   */   }
/* 61:   */   
/* 62:   */   public String toString()
/* 63:   */   {
/* 64:82 */     return StringUtil.simpleClassName(this) + "(data: " + content() + ", decoderResult: " + getDecoderResult() + ')';
/* 65:   */   }
/* 66:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.DefaultHttpContent
 * JD-Core Version:    0.7.0.1
 */