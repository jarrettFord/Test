/*  1:   */ package io.netty.handler.codec;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.buffer.Unpooled;
/*  5:   */ 
/*  6:   */ public final class Delimiters
/*  7:   */ {
/*  8:   */   public static ByteBuf[] nulDelimiter()
/*  9:   */   {
/* 10:31 */     return new ByteBuf[] { Unpooled.wrappedBuffer(new byte[] { 0 }) };
/* 11:   */   }
/* 12:   */   
/* 13:   */   public static ByteBuf[] lineDelimiter()
/* 14:   */   {
/* 15:40 */     return new ByteBuf[] { Unpooled.wrappedBuffer(new byte[] { 13, 10 }), Unpooled.wrappedBuffer(new byte[] { 10 }) };
/* 16:   */   }
/* 17:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.Delimiters
 * JD-Core Version:    0.7.0.1
 */