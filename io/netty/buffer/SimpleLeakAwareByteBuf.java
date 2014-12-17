/*  1:   */ package io.netty.buffer;
/*  2:   */ 
/*  3:   */ import io.netty.util.ResourceLeak;
/*  4:   */ import java.nio.ByteOrder;
/*  5:   */ 
/*  6:   */ final class SimpleLeakAwareByteBuf
/*  7:   */   extends WrappedByteBuf
/*  8:   */ {
/*  9:   */   private final ResourceLeak leak;
/* 10:   */   
/* 11:   */   SimpleLeakAwareByteBuf(ByteBuf buf, ResourceLeak leak)
/* 12:   */   {
/* 13:28 */     super(buf);
/* 14:29 */     this.leak = leak;
/* 15:   */   }
/* 16:   */   
/* 17:   */   public boolean release()
/* 18:   */   {
/* 19:34 */     boolean deallocated = super.release();
/* 20:35 */     if (deallocated) {
/* 21:36 */       this.leak.close();
/* 22:   */     }
/* 23:38 */     return deallocated;
/* 24:   */   }
/* 25:   */   
/* 26:   */   public boolean release(int decrement)
/* 27:   */   {
/* 28:43 */     boolean deallocated = super.release(decrement);
/* 29:44 */     if (deallocated) {
/* 30:45 */       this.leak.close();
/* 31:   */     }
/* 32:47 */     return deallocated;
/* 33:   */   }
/* 34:   */   
/* 35:   */   public ByteBuf order(ByteOrder endianness)
/* 36:   */   {
/* 37:52 */     this.leak.record();
/* 38:53 */     if (order() == endianness) {
/* 39:54 */       return this;
/* 40:   */     }
/* 41:56 */     return new SimpleLeakAwareByteBuf(super.order(endianness), this.leak);
/* 42:   */   }
/* 43:   */   
/* 44:   */   public ByteBuf slice()
/* 45:   */   {
/* 46:62 */     return new SimpleLeakAwareByteBuf(super.slice(), this.leak);
/* 47:   */   }
/* 48:   */   
/* 49:   */   public ByteBuf slice(int index, int length)
/* 50:   */   {
/* 51:67 */     return new SimpleLeakAwareByteBuf(super.slice(index, length), this.leak);
/* 52:   */   }
/* 53:   */   
/* 54:   */   public ByteBuf duplicate()
/* 55:   */   {
/* 56:72 */     return new SimpleLeakAwareByteBuf(super.duplicate(), this.leak);
/* 57:   */   }
/* 58:   */   
/* 59:   */   public ByteBuf readSlice(int length)
/* 60:   */   {
/* 61:77 */     return new SimpleLeakAwareByteBuf(super.readSlice(length), this.leak);
/* 62:   */   }
/* 63:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.SimpleLeakAwareByteBuf
 * JD-Core Version:    0.7.0.1
 */