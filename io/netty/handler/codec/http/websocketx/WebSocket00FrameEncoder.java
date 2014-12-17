/*  1:   */ package io.netty.handler.codec.http.websocketx;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.buffer.ByteBufAllocator;
/*  5:   */ import io.netty.buffer.Unpooled;
/*  6:   */ import io.netty.channel.ChannelHandler.Sharable;
/*  7:   */ import io.netty.channel.ChannelHandlerContext;
/*  8:   */ import io.netty.handler.codec.MessageToMessageEncoder;
/*  9:   */ import java.util.List;
/* 10:   */ 
/* 11:   */ @ChannelHandler.Sharable
/* 12:   */ public class WebSocket00FrameEncoder
/* 13:   */   extends MessageToMessageEncoder<WebSocketFrame>
/* 14:   */   implements WebSocketFrameEncoder
/* 15:   */ {
/* 16:34 */   private static final ByteBuf _0X00 = Unpooled.unreleasableBuffer(Unpooled.directBuffer(1, 1).writeByte(0));
/* 17:36 */   private static final ByteBuf _0XFF = Unpooled.unreleasableBuffer(Unpooled.directBuffer(1, 1).writeByte(-1));
/* 18:38 */   private static final ByteBuf _0XFF_0X00 = Unpooled.unreleasableBuffer(Unpooled.directBuffer(2, 2).writeByte(-1).writeByte(0));
/* 19:   */   
/* 20:   */   protected void encode(ChannelHandlerContext ctx, WebSocketFrame msg, List<Object> out)
/* 21:   */     throws Exception
/* 22:   */   {
/* 23:43 */     if ((msg instanceof TextWebSocketFrame))
/* 24:   */     {
/* 25:45 */       ByteBuf data = msg.content();
/* 26:   */       
/* 27:47 */       out.add(_0X00.duplicate());
/* 28:48 */       out.add(data.retain());
/* 29:49 */       out.add(_0XFF.duplicate());
/* 30:   */     }
/* 31:50 */     else if ((msg instanceof CloseWebSocketFrame))
/* 32:   */     {
/* 33:52 */       out.add(_0XFF_0X00);
/* 34:   */     }
/* 35:   */     else
/* 36:   */     {
/* 37:55 */       ByteBuf data = msg.content();
/* 38:56 */       int dataLen = data.readableBytes();
/* 39:   */       
/* 40:58 */       ByteBuf buf = ctx.alloc().buffer(5);
/* 41:59 */       boolean release = true;
/* 42:   */       try
/* 43:   */       {
/* 44:62 */         buf.writeByte(-128);
/* 45:   */         
/* 46:   */ 
/* 47:65 */         int b1 = dataLen >>> 28 & 0x7F;
/* 48:66 */         int b2 = dataLen >>> 14 & 0x7F;
/* 49:67 */         int b3 = dataLen >>> 7 & 0x7F;
/* 50:68 */         int b4 = dataLen & 0x7F;
/* 51:69 */         if (b1 == 0)
/* 52:   */         {
/* 53:70 */           if (b2 == 0)
/* 54:   */           {
/* 55:71 */             if (b3 == 0)
/* 56:   */             {
/* 57:72 */               buf.writeByte(b4);
/* 58:   */             }
/* 59:   */             else
/* 60:   */             {
/* 61:74 */               buf.writeByte(b3 | 0x80);
/* 62:75 */               buf.writeByte(b4);
/* 63:   */             }
/* 64:   */           }
/* 65:   */           else
/* 66:   */           {
/* 67:78 */             buf.writeByte(b2 | 0x80);
/* 68:79 */             buf.writeByte(b3 | 0x80);
/* 69:80 */             buf.writeByte(b4);
/* 70:   */           }
/* 71:   */         }
/* 72:   */         else
/* 73:   */         {
/* 74:83 */           buf.writeByte(b1 | 0x80);
/* 75:84 */           buf.writeByte(b2 | 0x80);
/* 76:85 */           buf.writeByte(b3 | 0x80);
/* 77:86 */           buf.writeByte(b4);
/* 78:   */         }
/* 79:90 */         out.add(buf);
/* 80:91 */         out.add(data.retain());
/* 81:92 */         release = false;
/* 82:   */       }
/* 83:   */       finally
/* 84:   */       {
/* 85:94 */         if (release) {
/* 86:95 */           buf.release();
/* 87:   */         }
/* 88:   */       }
/* 89:   */     }
/* 90:   */   }
/* 91:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.websocketx.WebSocket00FrameEncoder
 * JD-Core Version:    0.7.0.1
 */