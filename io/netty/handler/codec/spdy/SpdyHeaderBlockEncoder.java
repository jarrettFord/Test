/*  1:   */ package io.netty.handler.codec.spdy;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.util.internal.PlatformDependent;
/*  5:   */ 
/*  6:   */ abstract class SpdyHeaderBlockEncoder
/*  7:   */ {
/*  8:   */   static SpdyHeaderBlockEncoder newInstance(SpdyVersion version, int compressionLevel, int windowBits, int memLevel)
/*  9:   */   {
/* 10:26 */     if (PlatformDependent.javaVersion() >= 7) {
/* 11:27 */       return new SpdyHeaderBlockZlibEncoder(version, compressionLevel);
/* 12:   */     }
/* 13:30 */     return new SpdyHeaderBlockJZlibEncoder(version, compressionLevel, windowBits, memLevel);
/* 14:   */   }
/* 15:   */   
/* 16:   */   abstract ByteBuf encode(SpdyHeadersFrame paramSpdyHeadersFrame)
/* 17:   */     throws Exception;
/* 18:   */   
/* 19:   */   abstract void end();
/* 20:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.SpdyHeaderBlockEncoder
 * JD-Core Version:    0.7.0.1
 */