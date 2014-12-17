/*   1:    */ package io.netty.handler.codec.http.websocketx;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.Unpooled;
/*   5:    */ import io.netty.util.CharsetUtil;
/*   6:    */ import io.netty.util.internal.EmptyArrays;
/*   7:    */ 
/*   8:    */ public class CloseWebSocketFrame
/*   9:    */   extends WebSocketFrame
/*  10:    */ {
/*  11:    */   public CloseWebSocketFrame()
/*  12:    */   {
/*  13: 32 */     super(Unpooled.buffer(0));
/*  14:    */   }
/*  15:    */   
/*  16:    */   public CloseWebSocketFrame(int statusCode, String reasonText)
/*  17:    */   {
/*  18: 45 */     this(true, 0, statusCode, reasonText);
/*  19:    */   }
/*  20:    */   
/*  21:    */   public CloseWebSocketFrame(boolean finalFragment, int rsv)
/*  22:    */   {
/*  23: 57 */     this(finalFragment, rsv, null);
/*  24:    */   }
/*  25:    */   
/*  26:    */   public CloseWebSocketFrame(boolean finalFragment, int rsv, int statusCode, String reasonText)
/*  27:    */   {
/*  28: 74 */     super(finalFragment, rsv, newBinaryData(statusCode, reasonText));
/*  29:    */   }
/*  30:    */   
/*  31:    */   private static ByteBuf newBinaryData(int statusCode, String reasonText)
/*  32:    */   {
/*  33: 78 */     byte[] reasonBytes = EmptyArrays.EMPTY_BYTES;
/*  34: 79 */     if (reasonText != null) {
/*  35: 80 */       reasonBytes = reasonText.getBytes(CharsetUtil.UTF_8);
/*  36:    */     }
/*  37: 83 */     ByteBuf binaryData = Unpooled.buffer(2 + reasonBytes.length);
/*  38: 84 */     binaryData.writeShort(statusCode);
/*  39: 85 */     if (reasonBytes.length > 0) {
/*  40: 86 */       binaryData.writeBytes(reasonBytes);
/*  41:    */     }
/*  42: 89 */     binaryData.readerIndex(0);
/*  43: 90 */     return binaryData;
/*  44:    */   }
/*  45:    */   
/*  46:    */   public CloseWebSocketFrame(boolean finalFragment, int rsv, ByteBuf binaryData)
/*  47:    */   {
/*  48:104 */     super(finalFragment, rsv, binaryData);
/*  49:    */   }
/*  50:    */   
/*  51:    */   public int statusCode()
/*  52:    */   {
/*  53:112 */     ByteBuf binaryData = content();
/*  54:113 */     if ((binaryData == null) || (binaryData.capacity() == 0)) {
/*  55:114 */       return -1;
/*  56:    */     }
/*  57:117 */     binaryData.readerIndex(0);
/*  58:118 */     int statusCode = binaryData.readShort();
/*  59:119 */     binaryData.readerIndex(0);
/*  60:    */     
/*  61:121 */     return statusCode;
/*  62:    */   }
/*  63:    */   
/*  64:    */   public String reasonText()
/*  65:    */   {
/*  66:129 */     ByteBuf binaryData = content();
/*  67:130 */     if ((binaryData == null) || (binaryData.capacity() <= 2)) {
/*  68:131 */       return "";
/*  69:    */     }
/*  70:134 */     binaryData.readerIndex(2);
/*  71:135 */     String reasonText = binaryData.toString(CharsetUtil.UTF_8);
/*  72:136 */     binaryData.readerIndex(0);
/*  73:    */     
/*  74:138 */     return reasonText;
/*  75:    */   }
/*  76:    */   
/*  77:    */   public CloseWebSocketFrame copy()
/*  78:    */   {
/*  79:143 */     return new CloseWebSocketFrame(isFinalFragment(), rsv(), content().copy());
/*  80:    */   }
/*  81:    */   
/*  82:    */   public CloseWebSocketFrame duplicate()
/*  83:    */   {
/*  84:148 */     return new CloseWebSocketFrame(isFinalFragment(), rsv(), content().duplicate());
/*  85:    */   }
/*  86:    */   
/*  87:    */   public CloseWebSocketFrame retain()
/*  88:    */   {
/*  89:153 */     super.retain();
/*  90:154 */     return this;
/*  91:    */   }
/*  92:    */   
/*  93:    */   public CloseWebSocketFrame retain(int increment)
/*  94:    */   {
/*  95:159 */     super.retain(increment);
/*  96:160 */     return this;
/*  97:    */   }
/*  98:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.websocketx.CloseWebSocketFrame
 * JD-Core Version:    0.7.0.1
 */