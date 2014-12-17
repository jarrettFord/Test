/*  1:   */ package io.netty.handler.codec.http.websocketx;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.buffer.DefaultByteBufHolder;
/*  5:   */ import io.netty.util.internal.StringUtil;
/*  6:   */ 
/*  7:   */ public abstract class WebSocketFrame
/*  8:   */   extends DefaultByteBufHolder
/*  9:   */ {
/* 10:   */   private final boolean finalFragment;
/* 11:   */   private final int rsv;
/* 12:   */   
/* 13:   */   protected WebSocketFrame(ByteBuf binaryData)
/* 14:   */   {
/* 15:39 */     this(true, 0, binaryData);
/* 16:   */   }
/* 17:   */   
/* 18:   */   protected WebSocketFrame(boolean finalFragment, int rsv, ByteBuf binaryData)
/* 19:   */   {
/* 20:43 */     super(binaryData);
/* 21:44 */     this.finalFragment = finalFragment;
/* 22:45 */     this.rsv = rsv;
/* 23:   */   }
/* 24:   */   
/* 25:   */   public boolean isFinalFragment()
/* 26:   */   {
/* 27:53 */     return this.finalFragment;
/* 28:   */   }
/* 29:   */   
/* 30:   */   public int rsv()
/* 31:   */   {
/* 32:60 */     return this.rsv;
/* 33:   */   }
/* 34:   */   
/* 35:   */   public abstract WebSocketFrame copy();
/* 36:   */   
/* 37:   */   public abstract WebSocketFrame duplicate();
/* 38:   */   
/* 39:   */   public String toString()
/* 40:   */   {
/* 41:71 */     return StringUtil.simpleClassName(this) + "(data: " + content().toString() + ')';
/* 42:   */   }
/* 43:   */   
/* 44:   */   public WebSocketFrame retain()
/* 45:   */   {
/* 46:76 */     super.retain();
/* 47:77 */     return this;
/* 48:   */   }
/* 49:   */   
/* 50:   */   public WebSocketFrame retain(int increment)
/* 51:   */   {
/* 52:82 */     super.retain(increment);
/* 53:83 */     return this;
/* 54:   */   }
/* 55:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.websocketx.WebSocketFrame
 * JD-Core Version:    0.7.0.1
 */