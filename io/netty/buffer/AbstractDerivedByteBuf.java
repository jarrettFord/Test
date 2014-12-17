/*  1:   */ package io.netty.buffer;
/*  2:   */ 
/*  3:   */ import java.nio.ByteBuffer;
/*  4:   */ 
/*  5:   */ public abstract class AbstractDerivedByteBuf
/*  6:   */   extends AbstractByteBuf
/*  7:   */ {
/*  8:   */   protected AbstractDerivedByteBuf(int maxCapacity)
/*  9:   */   {
/* 10:28 */     super(maxCapacity);
/* 11:   */   }
/* 12:   */   
/* 13:   */   public final int refCnt()
/* 14:   */   {
/* 15:33 */     return unwrap().refCnt();
/* 16:   */   }
/* 17:   */   
/* 18:   */   public final ByteBuf retain()
/* 19:   */   {
/* 20:38 */     unwrap().retain();
/* 21:39 */     return this;
/* 22:   */   }
/* 23:   */   
/* 24:   */   public final ByteBuf retain(int increment)
/* 25:   */   {
/* 26:44 */     unwrap().retain(increment);
/* 27:45 */     return this;
/* 28:   */   }
/* 29:   */   
/* 30:   */   public final boolean release()
/* 31:   */   {
/* 32:50 */     return unwrap().release();
/* 33:   */   }
/* 34:   */   
/* 35:   */   public final boolean release(int decrement)
/* 36:   */   {
/* 37:55 */     return unwrap().release(decrement);
/* 38:   */   }
/* 39:   */   
/* 40:   */   public ByteBuffer internalNioBuffer(int index, int length)
/* 41:   */   {
/* 42:60 */     return nioBuffer(index, length);
/* 43:   */   }
/* 44:   */   
/* 45:   */   public ByteBuffer nioBuffer(int index, int length)
/* 46:   */   {
/* 47:65 */     return unwrap().nioBuffer(index, length);
/* 48:   */   }
/* 49:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.AbstractDerivedByteBuf
 * JD-Core Version:    0.7.0.1
 */