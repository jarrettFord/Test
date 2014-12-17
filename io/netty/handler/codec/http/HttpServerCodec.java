/*  1:   */ package io.netty.handler.codec.http;
/*  2:   */ 
/*  3:   */ import io.netty.channel.CombinedChannelDuplexHandler;
/*  4:   */ 
/*  5:   */ public final class HttpServerCodec
/*  6:   */   extends CombinedChannelDuplexHandler<HttpRequestDecoder, HttpResponseEncoder>
/*  7:   */ {
/*  8:   */   public HttpServerCodec()
/*  9:   */   {
/* 10:36 */     this(4096, 8192, 8192);
/* 11:   */   }
/* 12:   */   
/* 13:   */   public HttpServerCodec(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize)
/* 14:   */   {
/* 15:43 */     super(new HttpRequestDecoder(maxInitialLineLength, maxHeaderSize, maxChunkSize), new HttpResponseEncoder());
/* 16:   */   }
/* 17:   */   
/* 18:   */   public HttpServerCodec(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean validateHeaders)
/* 19:   */   {
/* 20:50 */     super(new HttpRequestDecoder(maxInitialLineLength, maxHeaderSize, maxChunkSize, validateHeaders), new HttpResponseEncoder());
/* 21:   */   }
/* 22:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.HttpServerCodec
 * JD-Core Version:    0.7.0.1
 */