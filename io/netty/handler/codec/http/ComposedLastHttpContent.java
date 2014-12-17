/*  1:   */ package io.netty.handler.codec.http;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.buffer.Unpooled;
/*  5:   */ import io.netty.handler.codec.DecoderResult;
/*  6:   */ 
/*  7:   */ final class ComposedLastHttpContent
/*  8:   */   implements LastHttpContent
/*  9:   */ {
/* 10:   */   private final HttpHeaders trailingHeaders;
/* 11:   */   private DecoderResult result;
/* 12:   */   
/* 13:   */   ComposedLastHttpContent(HttpHeaders trailingHeaders)
/* 14:   */   {
/* 15:28 */     this.trailingHeaders = trailingHeaders;
/* 16:   */   }
/* 17:   */   
/* 18:   */   public HttpHeaders trailingHeaders()
/* 19:   */   {
/* 20:32 */     return this.trailingHeaders;
/* 21:   */   }
/* 22:   */   
/* 23:   */   public LastHttpContent copy()
/* 24:   */   {
/* 25:37 */     LastHttpContent content = new DefaultLastHttpContent(Unpooled.EMPTY_BUFFER);
/* 26:38 */     content.trailingHeaders().set(trailingHeaders());
/* 27:39 */     return content;
/* 28:   */   }
/* 29:   */   
/* 30:   */   public LastHttpContent retain(int increment)
/* 31:   */   {
/* 32:44 */     return this;
/* 33:   */   }
/* 34:   */   
/* 35:   */   public LastHttpContent retain()
/* 36:   */   {
/* 37:49 */     return this;
/* 38:   */   }
/* 39:   */   
/* 40:   */   public HttpContent duplicate()
/* 41:   */   {
/* 42:54 */     return copy();
/* 43:   */   }
/* 44:   */   
/* 45:   */   public ByteBuf content()
/* 46:   */   {
/* 47:59 */     return Unpooled.EMPTY_BUFFER;
/* 48:   */   }
/* 49:   */   
/* 50:   */   public DecoderResult getDecoderResult()
/* 51:   */   {
/* 52:64 */     return this.result;
/* 53:   */   }
/* 54:   */   
/* 55:   */   public void setDecoderResult(DecoderResult result)
/* 56:   */   {
/* 57:69 */     this.result = result;
/* 58:   */   }
/* 59:   */   
/* 60:   */   public int refCnt()
/* 61:   */   {
/* 62:74 */     return 1;
/* 63:   */   }
/* 64:   */   
/* 65:   */   public boolean release()
/* 66:   */   {
/* 67:79 */     return false;
/* 68:   */   }
/* 69:   */   
/* 70:   */   public boolean release(int decrement)
/* 71:   */   {
/* 72:84 */     return false;
/* 73:   */   }
/* 74:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.ComposedLastHttpContent
 * JD-Core Version:    0.7.0.1
 */