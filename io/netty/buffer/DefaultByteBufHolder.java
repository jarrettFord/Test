/*  1:   */ package io.netty.buffer;
/*  2:   */ 
/*  3:   */ import io.netty.util.IllegalReferenceCountException;
/*  4:   */ import io.netty.util.internal.StringUtil;
/*  5:   */ 
/*  6:   */ public class DefaultByteBufHolder
/*  7:   */   implements ByteBufHolder
/*  8:   */ {
/*  9:   */   private final ByteBuf data;
/* 10:   */   
/* 11:   */   public DefaultByteBufHolder(ByteBuf data)
/* 12:   */   {
/* 13:30 */     if (data == null) {
/* 14:31 */       throw new NullPointerException("data");
/* 15:   */     }
/* 16:33 */     this.data = data;
/* 17:   */   }
/* 18:   */   
/* 19:   */   public ByteBuf content()
/* 20:   */   {
/* 21:38 */     if (this.data.refCnt() <= 0) {
/* 22:39 */       throw new IllegalReferenceCountException(this.data.refCnt());
/* 23:   */     }
/* 24:41 */     return this.data;
/* 25:   */   }
/* 26:   */   
/* 27:   */   public ByteBufHolder copy()
/* 28:   */   {
/* 29:46 */     return new DefaultByteBufHolder(this.data.copy());
/* 30:   */   }
/* 31:   */   
/* 32:   */   public ByteBufHolder duplicate()
/* 33:   */   {
/* 34:51 */     return new DefaultByteBufHolder(this.data.duplicate());
/* 35:   */   }
/* 36:   */   
/* 37:   */   public int refCnt()
/* 38:   */   {
/* 39:56 */     return this.data.refCnt();
/* 40:   */   }
/* 41:   */   
/* 42:   */   public ByteBufHolder retain()
/* 43:   */   {
/* 44:61 */     this.data.retain();
/* 45:62 */     return this;
/* 46:   */   }
/* 47:   */   
/* 48:   */   public ByteBufHolder retain(int increment)
/* 49:   */   {
/* 50:67 */     this.data.retain(increment);
/* 51:68 */     return this;
/* 52:   */   }
/* 53:   */   
/* 54:   */   public boolean release()
/* 55:   */   {
/* 56:73 */     return this.data.release();
/* 57:   */   }
/* 58:   */   
/* 59:   */   public boolean release(int decrement)
/* 60:   */   {
/* 61:78 */     return this.data.release(decrement);
/* 62:   */   }
/* 63:   */   
/* 64:   */   public String toString()
/* 65:   */   {
/* 66:83 */     return StringUtil.simpleClassName(this) + '(' + content().toString() + ')';
/* 67:   */   }
/* 68:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.DefaultByteBufHolder
 * JD-Core Version:    0.7.0.1
 */