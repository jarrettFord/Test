/*   1:    */ package io.netty.handler.codec.http.websocketx;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.Unpooled;
/*   5:    */ import io.netty.util.CharsetUtil;
/*   6:    */ 
/*   7:    */ public class TextWebSocketFrame
/*   8:    */   extends WebSocketFrame
/*   9:    */ {
/*  10:    */   public TextWebSocketFrame()
/*  11:    */   {
/*  12: 31 */     super(Unpooled.buffer(0));
/*  13:    */   }
/*  14:    */   
/*  15:    */   public TextWebSocketFrame(String text)
/*  16:    */   {
/*  17: 41 */     super(fromText(text));
/*  18:    */   }
/*  19:    */   
/*  20:    */   public TextWebSocketFrame(ByteBuf binaryData)
/*  21:    */   {
/*  22: 51 */     super(binaryData);
/*  23:    */   }
/*  24:    */   
/*  25:    */   public TextWebSocketFrame(boolean finalFragment, int rsv, String text)
/*  26:    */   {
/*  27: 65 */     super(finalFragment, rsv, fromText(text));
/*  28:    */   }
/*  29:    */   
/*  30:    */   private static ByteBuf fromText(String text)
/*  31:    */   {
/*  32: 69 */     if ((text == null) || (text.isEmpty())) {
/*  33: 70 */       return Unpooled.EMPTY_BUFFER;
/*  34:    */     }
/*  35: 72 */     return Unpooled.copiedBuffer(text, CharsetUtil.UTF_8);
/*  36:    */   }
/*  37:    */   
/*  38:    */   public TextWebSocketFrame(boolean finalFragment, int rsv, ByteBuf binaryData)
/*  39:    */   {
/*  40: 87 */     super(finalFragment, rsv, binaryData);
/*  41:    */   }
/*  42:    */   
/*  43:    */   public String text()
/*  44:    */   {
/*  45: 94 */     return content().toString(CharsetUtil.UTF_8);
/*  46:    */   }
/*  47:    */   
/*  48:    */   public TextWebSocketFrame copy()
/*  49:    */   {
/*  50: 99 */     return new TextWebSocketFrame(isFinalFragment(), rsv(), content().copy());
/*  51:    */   }
/*  52:    */   
/*  53:    */   public TextWebSocketFrame duplicate()
/*  54:    */   {
/*  55:104 */     return new TextWebSocketFrame(isFinalFragment(), rsv(), content().duplicate());
/*  56:    */   }
/*  57:    */   
/*  58:    */   public TextWebSocketFrame retain()
/*  59:    */   {
/*  60:109 */     super.retain();
/*  61:110 */     return this;
/*  62:    */   }
/*  63:    */   
/*  64:    */   public TextWebSocketFrame retain(int increment)
/*  65:    */   {
/*  66:115 */     super.retain(increment);
/*  67:116 */     return this;
/*  68:    */   }
/*  69:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.websocketx.TextWebSocketFrame
 * JD-Core Version:    0.7.0.1
 */