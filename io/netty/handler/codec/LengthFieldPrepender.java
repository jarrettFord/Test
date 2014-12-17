/*   1:    */ package io.netty.handler.codec;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.ChannelHandler.Sharable;
/*   5:    */ import io.netty.channel.ChannelHandlerContext;
/*   6:    */ 
/*   7:    */ @ChannelHandler.Sharable
/*   8:    */ public class LengthFieldPrepender
/*   9:    */   extends MessageToByteEncoder<ByteBuf>
/*  10:    */ {
/*  11:    */   private final int lengthFieldLength;
/*  12:    */   private final boolean lengthIncludesLengthFieldLength;
/*  13:    */   private final int lengthAdjustment;
/*  14:    */   
/*  15:    */   public LengthFieldPrepender(int lengthFieldLength)
/*  16:    */   {
/*  17: 66 */     this(lengthFieldLength, false);
/*  18:    */   }
/*  19:    */   
/*  20:    */   public LengthFieldPrepender(int lengthFieldLength, boolean lengthIncludesLengthFieldLength)
/*  21:    */   {
/*  22: 83 */     this(lengthFieldLength, 0, lengthIncludesLengthFieldLength);
/*  23:    */   }
/*  24:    */   
/*  25:    */   public LengthFieldPrepender(int lengthFieldLength, int lengthAdjustment)
/*  26:    */   {
/*  27: 98 */     this(lengthFieldLength, lengthAdjustment, false);
/*  28:    */   }
/*  29:    */   
/*  30:    */   public LengthFieldPrepender(int lengthFieldLength, int lengthAdjustment, boolean lengthIncludesLengthFieldLength)
/*  31:    */   {
/*  32:117 */     if ((lengthFieldLength != 1) && (lengthFieldLength != 2) && (lengthFieldLength != 3) && (lengthFieldLength != 4) && (lengthFieldLength != 8)) {
/*  33:120 */       throw new IllegalArgumentException("lengthFieldLength must be either 1, 2, 3, 4, or 8: " + lengthFieldLength);
/*  34:    */     }
/*  35:125 */     this.lengthFieldLength = lengthFieldLength;
/*  36:126 */     this.lengthIncludesLengthFieldLength = lengthIncludesLengthFieldLength;
/*  37:127 */     this.lengthAdjustment = lengthAdjustment;
/*  38:    */   }
/*  39:    */   
/*  40:    */   protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out)
/*  41:    */     throws Exception
/*  42:    */   {
/*  43:132 */     int length = msg.readableBytes() + this.lengthAdjustment;
/*  44:133 */     if (this.lengthIncludesLengthFieldLength) {
/*  45:134 */       length += this.lengthFieldLength;
/*  46:    */     }
/*  47:137 */     if (length < 0) {
/*  48:138 */       throw new IllegalArgumentException("Adjusted frame length (" + length + ") is less than zero");
/*  49:    */     }
/*  50:142 */     switch (this.lengthFieldLength)
/*  51:    */     {
/*  52:    */     case 1: 
/*  53:144 */       if (length >= 256) {
/*  54:145 */         throw new IllegalArgumentException("length does not fit into a byte: " + length);
/*  55:    */       }
/*  56:148 */       out.writeByte((byte)length);
/*  57:149 */       break;
/*  58:    */     case 2: 
/*  59:151 */       if (length >= 65536) {
/*  60:152 */         throw new IllegalArgumentException("length does not fit into a short integer: " + length);
/*  61:    */       }
/*  62:155 */       out.writeShort((short)length);
/*  63:156 */       break;
/*  64:    */     case 3: 
/*  65:158 */       if (length >= 16777216) {
/*  66:159 */         throw new IllegalArgumentException("length does not fit into a medium integer: " + length);
/*  67:    */       }
/*  68:162 */       out.writeMedium(length);
/*  69:163 */       break;
/*  70:    */     case 4: 
/*  71:165 */       out.writeInt(length);
/*  72:166 */       break;
/*  73:    */     case 8: 
/*  74:168 */       out.writeLong(length);
/*  75:169 */       break;
/*  76:    */     case 5: 
/*  77:    */     case 6: 
/*  78:    */     case 7: 
/*  79:    */     default: 
/*  80:171 */       throw new Error("should not reach here");
/*  81:    */     }
/*  82:174 */     out.writeBytes(msg, msg.readerIndex(), msg.readableBytes());
/*  83:    */   }
/*  84:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.LengthFieldPrepender
 * JD-Core Version:    0.7.0.1
 */