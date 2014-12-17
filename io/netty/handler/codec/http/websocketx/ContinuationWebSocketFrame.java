/*   1:    */ package io.netty.handler.codec.http.websocketx;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.Unpooled;
/*   5:    */ import io.netty.util.CharsetUtil;
/*   6:    */ 
/*   7:    */ public class ContinuationWebSocketFrame
/*   8:    */   extends WebSocketFrame
/*   9:    */ {
/*  10:    */   public ContinuationWebSocketFrame()
/*  11:    */   {
/*  12: 32 */     this(Unpooled.buffer(0));
/*  13:    */   }
/*  14:    */   
/*  15:    */   public ContinuationWebSocketFrame(ByteBuf binaryData)
/*  16:    */   {
/*  17: 42 */     super(binaryData);
/*  18:    */   }
/*  19:    */   
/*  20:    */   public ContinuationWebSocketFrame(boolean finalFragment, int rsv, ByteBuf binaryData)
/*  21:    */   {
/*  22: 56 */     super(finalFragment, rsv, binaryData);
/*  23:    */   }
/*  24:    */   
/*  25:    */   public ContinuationWebSocketFrame(boolean finalFragment, int rsv, String text)
/*  26:    */   {
/*  27: 70 */     this(finalFragment, rsv, fromText(text));
/*  28:    */   }
/*  29:    */   
/*  30:    */   public String text()
/*  31:    */   {
/*  32: 77 */     return content().toString(CharsetUtil.UTF_8);
/*  33:    */   }
/*  34:    */   
/*  35:    */   private static ByteBuf fromText(String text)
/*  36:    */   {
/*  37: 87 */     if ((text == null) || (text.isEmpty())) {
/*  38: 88 */       return Unpooled.EMPTY_BUFFER;
/*  39:    */     }
/*  40: 90 */     return Unpooled.copiedBuffer(text, CharsetUtil.UTF_8);
/*  41:    */   }
/*  42:    */   
/*  43:    */   public ContinuationWebSocketFrame copy()
/*  44:    */   {
/*  45: 96 */     return new ContinuationWebSocketFrame(isFinalFragment(), rsv(), content().copy());
/*  46:    */   }
/*  47:    */   
/*  48:    */   public ContinuationWebSocketFrame duplicate()
/*  49:    */   {
/*  50:101 */     return new ContinuationWebSocketFrame(isFinalFragment(), rsv(), content().duplicate());
/*  51:    */   }
/*  52:    */   
/*  53:    */   public ContinuationWebSocketFrame retain()
/*  54:    */   {
/*  55:106 */     super.retain();
/*  56:107 */     return this;
/*  57:    */   }
/*  58:    */   
/*  59:    */   public ContinuationWebSocketFrame retain(int increment)
/*  60:    */   {
/*  61:112 */     super.retain(increment);
/*  62:113 */     return this;
/*  63:    */   }
/*  64:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame
 * JD-Core Version:    0.7.0.1
 */