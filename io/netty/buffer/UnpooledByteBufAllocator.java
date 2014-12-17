/*  1:   */ package io.netty.buffer;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.PlatformDependent;
/*  4:   */ 
/*  5:   */ public final class UnpooledByteBufAllocator
/*  6:   */   extends AbstractByteBufAllocator
/*  7:   */ {
/*  8:28 */   public static final UnpooledByteBufAllocator DEFAULT = new UnpooledByteBufAllocator(PlatformDependent.directBufferPreferred());
/*  9:   */   
/* 10:   */   public UnpooledByteBufAllocator(boolean preferDirect)
/* 11:   */   {
/* 12:38 */     super(preferDirect);
/* 13:   */   }
/* 14:   */   
/* 15:   */   protected ByteBuf newHeapBuffer(int initialCapacity, int maxCapacity)
/* 16:   */   {
/* 17:43 */     return new UnpooledHeapByteBuf(this, initialCapacity, maxCapacity);
/* 18:   */   }
/* 19:   */   
/* 20:   */   protected ByteBuf newDirectBuffer(int initialCapacity, int maxCapacity)
/* 21:   */   {
/* 22:   */     ByteBuf buf;
/* 23:   */     ByteBuf buf;
/* 24:49 */     if (PlatformDependent.hasUnsafe()) {
/* 25:50 */       buf = new UnpooledUnsafeDirectByteBuf(this, initialCapacity, maxCapacity);
/* 26:   */     } else {
/* 27:52 */       buf = new UnpooledDirectByteBuf(this, initialCapacity, maxCapacity);
/* 28:   */     }
/* 29:55 */     return toLeakAwareBuffer(buf);
/* 30:   */   }
/* 31:   */   
/* 32:   */   public boolean isDirectBufferPooled()
/* 33:   */   {
/* 34:60 */     return false;
/* 35:   */   }
/* 36:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.UnpooledByteBufAllocator
 * JD-Core Version:    0.7.0.1
 */