/*   1:    */ package io.netty.handler.codec.http.websocketx;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.buffer.Unpooled;
/*   6:    */ import io.netty.channel.ChannelHandlerContext;
/*   7:    */ import io.netty.handler.codec.MessageToMessageEncoder;
/*   8:    */ import io.netty.handler.codec.TooLongFrameException;
/*   9:    */ import io.netty.util.internal.logging.InternalLogger;
/*  10:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  11:    */ import java.nio.ByteBuffer;
/*  12:    */ import java.util.List;
/*  13:    */ 
/*  14:    */ public class WebSocket08FrameEncoder
/*  15:    */   extends MessageToMessageEncoder<WebSocketFrame>
/*  16:    */   implements WebSocketFrameEncoder
/*  17:    */ {
/*  18: 75 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(WebSocket08FrameEncoder.class);
/*  19:    */   private static final byte OPCODE_CONT = 0;
/*  20:    */   private static final byte OPCODE_TEXT = 1;
/*  21:    */   private static final byte OPCODE_BINARY = 2;
/*  22:    */   private static final byte OPCODE_CLOSE = 8;
/*  23:    */   private static final byte OPCODE_PING = 9;
/*  24:    */   private static final byte OPCODE_PONG = 10;
/*  25:    */   private final boolean maskPayload;
/*  26:    */   
/*  27:    */   public WebSocket08FrameEncoder(boolean maskPayload)
/*  28:    */   {
/*  29: 94 */     this.maskPayload = maskPayload;
/*  30:    */   }
/*  31:    */   
/*  32:    */   protected void encode(ChannelHandlerContext ctx, WebSocketFrame msg, List<Object> out)
/*  33:    */     throws Exception
/*  34:    */   {
/*  35:102 */     ByteBuf data = msg.content();
/*  36:103 */     if (data == null) {
/*  37:104 */       data = Unpooled.EMPTY_BUFFER;
/*  38:    */     }
/*  39:    */     byte opcode;
/*  40:108 */     if ((msg instanceof TextWebSocketFrame))
/*  41:    */     {
/*  42:109 */       opcode = 1;
/*  43:    */     }
/*  44:    */     else
/*  45:    */     {
/*  46:    */       byte opcode;
/*  47:110 */       if ((msg instanceof PingWebSocketFrame))
/*  48:    */       {
/*  49:111 */         opcode = 9;
/*  50:    */       }
/*  51:    */       else
/*  52:    */       {
/*  53:    */         byte opcode;
/*  54:112 */         if ((msg instanceof PongWebSocketFrame))
/*  55:    */         {
/*  56:113 */           opcode = 10;
/*  57:    */         }
/*  58:    */         else
/*  59:    */         {
/*  60:    */           byte opcode;
/*  61:114 */           if ((msg instanceof CloseWebSocketFrame))
/*  62:    */           {
/*  63:115 */             opcode = 8;
/*  64:    */           }
/*  65:    */           else
/*  66:    */           {
/*  67:    */             byte opcode;
/*  68:116 */             if ((msg instanceof BinaryWebSocketFrame))
/*  69:    */             {
/*  70:117 */               opcode = 2;
/*  71:    */             }
/*  72:    */             else
/*  73:    */             {
/*  74:    */               byte opcode;
/*  75:118 */               if ((msg instanceof ContinuationWebSocketFrame)) {
/*  76:119 */                 opcode = 0;
/*  77:    */               } else {
/*  78:121 */                 throw new UnsupportedOperationException("Cannot encode frame of type: " + msg.getClass().getName());
/*  79:    */               }
/*  80:    */             }
/*  81:    */           }
/*  82:    */         }
/*  83:    */       }
/*  84:    */     }
/*  85:    */     byte opcode;
/*  86:124 */     int length = data.readableBytes();
/*  87:126 */     if (logger.isDebugEnabled()) {
/*  88:127 */       logger.debug("Encoding WebSocket Frame opCode=" + opcode + " length=" + length);
/*  89:    */     }
/*  90:130 */     int b0 = 0;
/*  91:131 */     if (msg.isFinalFragment()) {
/*  92:132 */       b0 |= 0x80;
/*  93:    */     }
/*  94:134 */     b0 |= msg.rsv() % 8 << 4;
/*  95:135 */     b0 |= opcode % 128;
/*  96:137 */     if ((opcode == 9) && (length > 125)) {
/*  97:138 */       throw new TooLongFrameException("invalid payload for PING (payload length must be <= 125, was " + length);
/*  98:    */     }
/*  99:142 */     boolean release = true;
/* 100:143 */     ByteBuf buf = null;
/* 101:    */     try
/* 102:    */     {
/* 103:145 */       int maskLength = this.maskPayload ? 4 : 0;
/* 104:146 */       if (length <= 125)
/* 105:    */       {
/* 106:147 */         int size = 2 + maskLength;
/* 107:148 */         if (this.maskPayload) {
/* 108:149 */           size += length;
/* 109:    */         }
/* 110:151 */         buf = ctx.alloc().buffer(size);
/* 111:152 */         buf.writeByte(b0);
/* 112:153 */         byte b = (byte)(this.maskPayload ? 0x80 | (byte)length : (byte)length);
/* 113:154 */         buf.writeByte(b);
/* 114:    */       }
/* 115:155 */       else if (length <= 65535)
/* 116:    */       {
/* 117:156 */         int size = 4 + maskLength;
/* 118:157 */         if (this.maskPayload) {
/* 119:158 */           size += length;
/* 120:    */         }
/* 121:160 */         buf = ctx.alloc().buffer(size);
/* 122:161 */         buf.writeByte(b0);
/* 123:162 */         buf.writeByte(this.maskPayload ? 254 : 126);
/* 124:163 */         buf.writeByte(length >>> 8 & 0xFF);
/* 125:164 */         buf.writeByte(length & 0xFF);
/* 126:    */       }
/* 127:    */       else
/* 128:    */       {
/* 129:166 */         int size = 10 + maskLength;
/* 130:167 */         if (this.maskPayload) {
/* 131:168 */           size += length;
/* 132:    */         }
/* 133:170 */         buf = ctx.alloc().buffer(size);
/* 134:171 */         buf.writeByte(b0);
/* 135:172 */         buf.writeByte(this.maskPayload ? 255 : 127);
/* 136:173 */         buf.writeLong(length);
/* 137:    */       }
/* 138:177 */       if (this.maskPayload)
/* 139:    */       {
/* 140:178 */         int random = (int)(Math.random() * 2147483647.0D);
/* 141:179 */         byte[] mask = ByteBuffer.allocate(4).putInt(random).array();
/* 142:180 */         buf.writeBytes(mask);
/* 143:    */         
/* 144:182 */         int counter = 0;
/* 145:183 */         for (int i = data.readerIndex(); i < data.writerIndex(); i++)
/* 146:    */         {
/* 147:184 */           byte byteData = data.getByte(i);
/* 148:185 */           buf.writeByte(byteData ^ mask[(counter++ % 4)]);
/* 149:    */         }
/* 150:187 */         out.add(buf);
/* 151:    */       }
/* 152:189 */       else if (buf.writableBytes() >= data.readableBytes())
/* 153:    */       {
/* 154:191 */         buf.writeBytes(data);
/* 155:192 */         out.add(buf);
/* 156:    */       }
/* 157:    */       else
/* 158:    */       {
/* 159:194 */         out.add(buf);
/* 160:195 */         out.add(data.retain());
/* 161:    */       }
/* 162:198 */       release = false;
/* 163:    */     }
/* 164:    */     finally
/* 165:    */     {
/* 166:200 */       if ((release) && (buf != null)) {
/* 167:201 */         buf.release();
/* 168:    */       }
/* 169:    */     }
/* 170:    */   }
/* 171:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.websocketx.WebSocket08FrameEncoder
 * JD-Core Version:    0.7.0.1
 */