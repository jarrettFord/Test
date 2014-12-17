/*  1:   */ package io.netty.channel;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.buffer.ByteBufAllocator;
/*  5:   */ 
/*  6:   */ public class FixedRecvByteBufAllocator
/*  7:   */   implements RecvByteBufAllocator
/*  8:   */ {
/*  9:   */   private final RecvByteBufAllocator.Handle handle;
/* 10:   */   
/* 11:   */   private static final class HandleImpl
/* 12:   */     implements RecvByteBufAllocator.Handle
/* 13:   */   {
/* 14:   */     private final int bufferSize;
/* 15:   */     
/* 16:   */     HandleImpl(int bufferSize)
/* 17:   */     {
/* 18:33 */       this.bufferSize = bufferSize;
/* 19:   */     }
/* 20:   */     
/* 21:   */     public ByteBuf allocate(ByteBufAllocator alloc)
/* 22:   */     {
/* 23:38 */       return alloc.ioBuffer(this.bufferSize);
/* 24:   */     }
/* 25:   */     
/* 26:   */     public int guess()
/* 27:   */     {
/* 28:43 */       return this.bufferSize;
/* 29:   */     }
/* 30:   */     
/* 31:   */     public void record(int actualReadBytes) {}
/* 32:   */   }
/* 33:   */   
/* 34:   */   public FixedRecvByteBufAllocator(int bufferSize)
/* 35:   */   {
/* 36:57 */     if (bufferSize <= 0) {
/* 37:58 */       throw new IllegalArgumentException("bufferSize must greater than 0: " + bufferSize);
/* 38:   */     }
/* 39:62 */     this.handle = new HandleImpl(bufferSize);
/* 40:   */   }
/* 41:   */   
/* 42:   */   public RecvByteBufAllocator.Handle newHandle()
/* 43:   */   {
/* 44:67 */     return this.handle;
/* 45:   */   }
/* 46:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.FixedRecvByteBufAllocator
 * JD-Core Version:    0.7.0.1
 */