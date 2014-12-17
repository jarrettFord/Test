/*  1:   */ package io.netty.buffer;
/*  2:   */ 
/*  3:   */ import java.nio.ByteOrder;
/*  4:   */ 
/*  5:   */ final class UnreleasableByteBuf
/*  6:   */   extends WrappedByteBuf
/*  7:   */ {
/*  8:   */   private SwappedByteBuf swappedBuf;
/*  9:   */   
/* 10:   */   UnreleasableByteBuf(ByteBuf buf)
/* 11:   */   {
/* 12:29 */     super(buf);
/* 13:   */   }
/* 14:   */   
/* 15:   */   public ByteBuf order(ByteOrder endianness)
/* 16:   */   {
/* 17:34 */     if (endianness == null) {
/* 18:35 */       throw new NullPointerException("endianness");
/* 19:   */     }
/* 20:37 */     if (endianness == order()) {
/* 21:38 */       return this;
/* 22:   */     }
/* 23:41 */     SwappedByteBuf swappedBuf = this.swappedBuf;
/* 24:42 */     if (swappedBuf == null) {
/* 25:43 */       this.swappedBuf = (swappedBuf = new SwappedByteBuf(this));
/* 26:   */     }
/* 27:45 */     return swappedBuf;
/* 28:   */   }
/* 29:   */   
/* 30:   */   public ByteBuf readSlice(int length)
/* 31:   */   {
/* 32:50 */     return new UnreleasableByteBuf(this.buf.readSlice(length));
/* 33:   */   }
/* 34:   */   
/* 35:   */   public ByteBuf slice()
/* 36:   */   {
/* 37:55 */     return new UnreleasableByteBuf(this.buf.slice());
/* 38:   */   }
/* 39:   */   
/* 40:   */   public ByteBuf slice(int index, int length)
/* 41:   */   {
/* 42:60 */     return new UnreleasableByteBuf(this.buf.slice(index, length));
/* 43:   */   }
/* 44:   */   
/* 45:   */   public ByteBuf duplicate()
/* 46:   */   {
/* 47:65 */     return new UnreleasableByteBuf(this.buf.duplicate());
/* 48:   */   }
/* 49:   */   
/* 50:   */   public ByteBuf retain(int increment)
/* 51:   */   {
/* 52:70 */     return this;
/* 53:   */   }
/* 54:   */   
/* 55:   */   public ByteBuf retain()
/* 56:   */   {
/* 57:75 */     return this;
/* 58:   */   }
/* 59:   */   
/* 60:   */   public boolean release()
/* 61:   */   {
/* 62:80 */     return false;
/* 63:   */   }
/* 64:   */   
/* 65:   */   public boolean release(int decrement)
/* 66:   */   {
/* 67:85 */     return false;
/* 68:   */   }
/* 69:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.UnreleasableByteBuf
 * JD-Core Version:    0.7.0.1
 */