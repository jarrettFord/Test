/*  1:   */ package io.netty.handler.codec.spdy;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ 
/*  5:   */ abstract class SpdyHeaderBlockDecoder
/*  6:   */ {
/*  7:   */   static SpdyHeaderBlockDecoder newInstance(SpdyVersion spdyVersion, int maxHeaderSize)
/*  8:   */   {
/*  9:23 */     return new SpdyHeaderBlockZlibDecoder(spdyVersion, maxHeaderSize);
/* 10:   */   }
/* 11:   */   
/* 12:   */   abstract void decode(ByteBuf paramByteBuf, SpdyHeadersFrame paramSpdyHeadersFrame)
/* 13:   */     throws Exception;
/* 14:   */   
/* 15:   */   abstract void endHeaderBlock(SpdyHeadersFrame paramSpdyHeadersFrame)
/* 16:   */     throws Exception;
/* 17:   */   
/* 18:   */   abstract void end();
/* 19:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.SpdyHeaderBlockDecoder
 * JD-Core Version:    0.7.0.1
 */