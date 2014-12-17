/*  1:   */ package io.netty.handler.codec.http;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.buffer.Unpooled;
/*  5:   */ import io.netty.handler.codec.DecoderResult;
/*  6:   */ 
/*  7:   */ public abstract interface LastHttpContent
/*  8:   */   extends HttpContent
/*  9:   */ {
/* 10:30 */   public static final LastHttpContent EMPTY_LAST_CONTENT = new LastHttpContent()
/* 11:   */   {
/* 12:   */     public ByteBuf content()
/* 13:   */     {
/* 14:34 */       return Unpooled.EMPTY_BUFFER;
/* 15:   */     }
/* 16:   */     
/* 17:   */     public LastHttpContent copy()
/* 18:   */     {
/* 19:39 */       return EMPTY_LAST_CONTENT;
/* 20:   */     }
/* 21:   */     
/* 22:   */     public LastHttpContent duplicate()
/* 23:   */     {
/* 24:44 */       return this;
/* 25:   */     }
/* 26:   */     
/* 27:   */     public HttpHeaders trailingHeaders()
/* 28:   */     {
/* 29:49 */       return HttpHeaders.EMPTY_HEADERS;
/* 30:   */     }
/* 31:   */     
/* 32:   */     public DecoderResult getDecoderResult()
/* 33:   */     {
/* 34:54 */       return DecoderResult.SUCCESS;
/* 35:   */     }
/* 36:   */     
/* 37:   */     public void setDecoderResult(DecoderResult result)
/* 38:   */     {
/* 39:59 */       throw new UnsupportedOperationException("read only");
/* 40:   */     }
/* 41:   */     
/* 42:   */     public int refCnt()
/* 43:   */     {
/* 44:64 */       return 1;
/* 45:   */     }
/* 46:   */     
/* 47:   */     public LastHttpContent retain()
/* 48:   */     {
/* 49:69 */       return this;
/* 50:   */     }
/* 51:   */     
/* 52:   */     public LastHttpContent retain(int increment)
/* 53:   */     {
/* 54:74 */       return this;
/* 55:   */     }
/* 56:   */     
/* 57:   */     public boolean release()
/* 58:   */     {
/* 59:79 */       return false;
/* 60:   */     }
/* 61:   */     
/* 62:   */     public boolean release(int decrement)
/* 63:   */     {
/* 64:84 */       return false;
/* 65:   */     }
/* 66:   */     
/* 67:   */     public String toString()
/* 68:   */     {
/* 69:89 */       return "EmptyLastHttpContent";
/* 70:   */     }
/* 71:   */   };
/* 72:   */   
/* 73:   */   public abstract HttpHeaders trailingHeaders();
/* 74:   */   
/* 75:   */   public abstract LastHttpContent copy();
/* 76:   */   
/* 77:   */   public abstract LastHttpContent retain(int paramInt);
/* 78:   */   
/* 79:   */   public abstract LastHttpContent retain();
/* 80:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.LastHttpContent
 * JD-Core Version:    0.7.0.1
 */