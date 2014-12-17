/*  1:   */ package io.netty.channel.udt;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.buffer.DefaultByteBufHolder;
/*  5:   */ 
/*  6:   */ public final class UdtMessage
/*  7:   */   extends DefaultByteBufHolder
/*  8:   */ {
/*  9:   */   public UdtMessage(ByteBuf data)
/* 10:   */   {
/* 11:31 */     super(data);
/* 12:   */   }
/* 13:   */   
/* 14:   */   public UdtMessage copy()
/* 15:   */   {
/* 16:36 */     return new UdtMessage(content().copy());
/* 17:   */   }
/* 18:   */   
/* 19:   */   public UdtMessage duplicate()
/* 20:   */   {
/* 21:41 */     return new UdtMessage(content().duplicate());
/* 22:   */   }
/* 23:   */   
/* 24:   */   public UdtMessage retain()
/* 25:   */   {
/* 26:46 */     super.retain();
/* 27:47 */     return this;
/* 28:   */   }
/* 29:   */   
/* 30:   */   public UdtMessage retain(int increment)
/* 31:   */   {
/* 32:52 */     super.retain(increment);
/* 33:53 */     return this;
/* 34:   */   }
/* 35:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.udt.UdtMessage
 * JD-Core Version:    0.7.0.1
 */