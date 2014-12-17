/*  1:   */ package io.netty.handler.codec.http;
/*  2:   */ 
/*  3:   */ import io.netty.channel.ChannelHandler;
/*  4:   */ import io.netty.channel.embedded.EmbeddedChannel;
/*  5:   */ import io.netty.handler.codec.compression.ZlibCodecFactory;
/*  6:   */ import io.netty.handler.codec.compression.ZlibWrapper;
/*  7:   */ 
/*  8:   */ public class HttpContentDecompressor
/*  9:   */   extends HttpContentDecoder
/* 10:   */ {
/* 11:   */   private final boolean strict;
/* 12:   */   
/* 13:   */   public HttpContentDecompressor()
/* 14:   */   {
/* 15:35 */     this(false);
/* 16:   */   }
/* 17:   */   
/* 18:   */   public HttpContentDecompressor(boolean strict)
/* 19:   */   {
/* 20:45 */     this.strict = strict;
/* 21:   */   }
/* 22:   */   
/* 23:   */   protected EmbeddedChannel newContentDecoder(String contentEncoding)
/* 24:   */     throws Exception
/* 25:   */   {
/* 26:50 */     if (("gzip".equalsIgnoreCase(contentEncoding)) || ("x-gzip".equalsIgnoreCase(contentEncoding))) {
/* 27:51 */       return new EmbeddedChannel(new ChannelHandler[] { ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP) });
/* 28:   */     }
/* 29:53 */     if (("deflate".equalsIgnoreCase(contentEncoding)) || ("x-deflate".equalsIgnoreCase(contentEncoding)))
/* 30:   */     {
/* 31:   */       ZlibWrapper wrapper;
/* 32:   */       ZlibWrapper wrapper;
/* 33:55 */       if (this.strict) {
/* 34:56 */         wrapper = ZlibWrapper.ZLIB;
/* 35:   */       } else {
/* 36:58 */         wrapper = ZlibWrapper.ZLIB_OR_NONE;
/* 37:   */       }
/* 38:61 */       return new EmbeddedChannel(new ChannelHandler[] { ZlibCodecFactory.newZlibDecoder(wrapper) });
/* 39:   */     }
/* 40:65 */     return null;
/* 41:   */   }
/* 42:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.HttpContentDecompressor
 * JD-Core Version:    0.7.0.1
 */