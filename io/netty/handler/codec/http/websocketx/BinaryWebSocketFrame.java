/*  1:   */ package io.netty.handler.codec.http.websocketx;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.buffer.Unpooled;
/*  5:   */ 
/*  6:   */ public class BinaryWebSocketFrame
/*  7:   */   extends WebSocketFrame
/*  8:   */ {
/*  9:   */   public BinaryWebSocketFrame()
/* 10:   */   {
/* 11:30 */     super(Unpooled.buffer(0));
/* 12:   */   }
/* 13:   */   
/* 14:   */   public BinaryWebSocketFrame(ByteBuf binaryData)
/* 15:   */   {
/* 16:40 */     super(binaryData);
/* 17:   */   }
/* 18:   */   
/* 19:   */   public BinaryWebSocketFrame(boolean finalFragment, int rsv, ByteBuf binaryData)
/* 20:   */   {
/* 21:54 */     super(finalFragment, rsv, binaryData);
/* 22:   */   }
/* 23:   */   
/* 24:   */   public BinaryWebSocketFrame copy()
/* 25:   */   {
/* 26:59 */     return new BinaryWebSocketFrame(isFinalFragment(), rsv(), content().copy());
/* 27:   */   }
/* 28:   */   
/* 29:   */   public BinaryWebSocketFrame duplicate()
/* 30:   */   {
/* 31:64 */     return new BinaryWebSocketFrame(isFinalFragment(), rsv(), content().duplicate());
/* 32:   */   }
/* 33:   */   
/* 34:   */   public BinaryWebSocketFrame retain()
/* 35:   */   {
/* 36:69 */     super.retain();
/* 37:70 */     return this;
/* 38:   */   }
/* 39:   */   
/* 40:   */   public BinaryWebSocketFrame retain(int increment)
/* 41:   */   {
/* 42:75 */     super.retain(increment);
/* 43:76 */     return this;
/* 44:   */   }
/* 45:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
 * JD-Core Version:    0.7.0.1
 */