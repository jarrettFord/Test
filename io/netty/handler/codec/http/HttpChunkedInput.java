/*  1:   */ package io.netty.handler.codec.http;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.channel.ChannelHandlerContext;
/*  5:   */ import io.netty.handler.stream.ChunkedInput;
/*  6:   */ 
/*  7:   */ public class HttpChunkedInput
/*  8:   */   implements ChunkedInput<HttpContent>
/*  9:   */ {
/* 10:   */   private final ChunkedInput<ByteBuf> input;
/* 11:   */   private final LastHttpContent lastHttpContent;
/* 12:   */   private boolean sentLastChunk;
/* 13:   */   
/* 14:   */   public HttpChunkedInput(ChunkedInput<ByteBuf> input)
/* 15:   */   {
/* 16:53 */     this.input = input;
/* 17:54 */     this.lastHttpContent = LastHttpContent.EMPTY_LAST_CONTENT;
/* 18:   */   }
/* 19:   */   
/* 20:   */   public HttpChunkedInput(ChunkedInput<ByteBuf> input, LastHttpContent lastHttpContent)
/* 21:   */   {
/* 22:65 */     this.input = input;
/* 23:66 */     this.lastHttpContent = lastHttpContent;
/* 24:   */   }
/* 25:   */   
/* 26:   */   public boolean isEndOfInput()
/* 27:   */     throws Exception
/* 28:   */   {
/* 29:71 */     if (this.input.isEndOfInput()) {
/* 30:73 */       return this.sentLastChunk;
/* 31:   */     }
/* 32:75 */     return false;
/* 33:   */   }
/* 34:   */   
/* 35:   */   public void close()
/* 36:   */     throws Exception
/* 37:   */   {
/* 38:81 */     this.input.close();
/* 39:   */   }
/* 40:   */   
/* 41:   */   public HttpContent readChunk(ChannelHandlerContext ctx)
/* 42:   */     throws Exception
/* 43:   */   {
/* 44:86 */     if (this.input.isEndOfInput())
/* 45:   */     {
/* 46:87 */       if (this.sentLastChunk) {
/* 47:88 */         return null;
/* 48:   */       }
/* 49:91 */       this.sentLastChunk = true;
/* 50:92 */       return this.lastHttpContent;
/* 51:   */     }
/* 52:95 */     ByteBuf buf = (ByteBuf)this.input.readChunk(ctx);
/* 53:96 */     return new DefaultHttpContent(buf);
/* 54:   */   }
/* 55:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.HttpChunkedInput
 * JD-Core Version:    0.7.0.1
 */