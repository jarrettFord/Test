/*  1:   */ package io.netty.handler.codec.spdy;
/*  2:   */ 
/*  3:   */ import io.netty.channel.CombinedChannelDuplexHandler;
/*  4:   */ 
/*  5:   */ public final class SpdyHttpCodec
/*  6:   */   extends CombinedChannelDuplexHandler<SpdyHttpDecoder, SpdyHttpEncoder>
/*  7:   */ {
/*  8:   */   public SpdyHttpCodec(SpdyVersion version, int maxContentLength)
/*  9:   */   {
/* 10:29 */     super(new SpdyHttpDecoder(version, maxContentLength), new SpdyHttpEncoder(version));
/* 11:   */   }
/* 12:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.SpdyHttpCodec
 * JD-Core Version:    0.7.0.1
 */