/*   1:    */ package io.netty.handler.codec.http.websocketx;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.channel.ChannelHandlerContext;
/*   6:    */ import io.netty.handler.codec.ReplayingDecoder;
/*   7:    */ import io.netty.handler.codec.TooLongFrameException;
/*   8:    */ import java.util.List;
/*   9:    */ 
/*  10:    */ public class WebSocket00FrameDecoder
/*  11:    */   extends ReplayingDecoder<Void>
/*  12:    */   implements WebSocketFrameDecoder
/*  13:    */ {
/*  14:    */   static final int DEFAULT_MAX_FRAME_SIZE = 16384;
/*  15:    */   private final long maxFrameSize;
/*  16:    */   private boolean receivedClosingHandshake;
/*  17:    */   
/*  18:    */   public WebSocket00FrameDecoder()
/*  19:    */   {
/*  20: 39 */     this(16384);
/*  21:    */   }
/*  22:    */   
/*  23:    */   public WebSocket00FrameDecoder(int maxFrameSize)
/*  24:    */   {
/*  25: 50 */     this.maxFrameSize = maxFrameSize;
/*  26:    */   }
/*  27:    */   
/*  28:    */   protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/*  29:    */     throws Exception
/*  30:    */   {
/*  31: 56 */     if (this.receivedClosingHandshake)
/*  32:    */     {
/*  33: 57 */       in.skipBytes(actualReadableBytes());
/*  34: 58 */       return;
/*  35:    */     }
/*  36: 62 */     byte type = in.readByte();
/*  37: 63 */     if ((type & 0x80) == 128) {
/*  38: 65 */       out.add(decodeBinaryFrame(ctx, type, in));
/*  39:    */     } else {
/*  40: 68 */       out.add(decodeTextFrame(ctx, in));
/*  41:    */     }
/*  42:    */   }
/*  43:    */   
/*  44:    */   private WebSocketFrame decodeBinaryFrame(ChannelHandlerContext ctx, byte type, ByteBuf buffer)
/*  45:    */   {
/*  46: 73 */     long frameSize = 0L;
/*  47: 74 */     int lengthFieldSize = 0;
/*  48:    */     byte b;
/*  49:    */     do
/*  50:    */     {
/*  51: 77 */       b = buffer.readByte();
/*  52: 78 */       frameSize <<= 7;
/*  53: 79 */       frameSize |= b & 0x7F;
/*  54: 80 */       if (frameSize > this.maxFrameSize) {
/*  55: 81 */         throw new TooLongFrameException();
/*  56:    */       }
/*  57: 83 */       lengthFieldSize++;
/*  58: 84 */       if (lengthFieldSize > 8) {
/*  59: 86 */         throw new TooLongFrameException();
/*  60:    */       }
/*  61: 88 */     } while ((b & 0x80) == 128);
/*  62: 90 */     if ((type == -1) && (frameSize == 0L))
/*  63:    */     {
/*  64: 91 */       this.receivedClosingHandshake = true;
/*  65: 92 */       return new CloseWebSocketFrame();
/*  66:    */     }
/*  67: 94 */     ByteBuf payload = ctx.alloc().buffer((int)frameSize);
/*  68: 95 */     buffer.readBytes(payload);
/*  69: 96 */     return new BinaryWebSocketFrame(payload);
/*  70:    */   }
/*  71:    */   
/*  72:    */   private WebSocketFrame decodeTextFrame(ChannelHandlerContext ctx, ByteBuf buffer)
/*  73:    */   {
/*  74:100 */     int ridx = buffer.readerIndex();
/*  75:101 */     int rbytes = actualReadableBytes();
/*  76:102 */     int delimPos = buffer.indexOf(ridx, ridx + rbytes, (byte)-1);
/*  77:103 */     if (delimPos == -1)
/*  78:    */     {
/*  79:105 */       if (rbytes > this.maxFrameSize) {
/*  80:107 */         throw new TooLongFrameException();
/*  81:    */       }
/*  82:110 */       return null;
/*  83:    */     }
/*  84:114 */     int frameSize = delimPos - ridx;
/*  85:115 */     if (frameSize > this.maxFrameSize) {
/*  86:116 */       throw new TooLongFrameException();
/*  87:    */     }
/*  88:119 */     ByteBuf binaryData = ctx.alloc().buffer(frameSize);
/*  89:120 */     buffer.readBytes(binaryData);
/*  90:121 */     buffer.skipBytes(1);
/*  91:    */     
/*  92:123 */     int ffDelimPos = binaryData.indexOf(binaryData.readerIndex(), binaryData.writerIndex(), (byte)-1);
/*  93:124 */     if (ffDelimPos >= 0) {
/*  94:125 */       throw new IllegalArgumentException("a text frame should not contain 0xFF.");
/*  95:    */     }
/*  96:128 */     return new TextWebSocketFrame(binaryData);
/*  97:    */   }
/*  98:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.websocketx.WebSocket00FrameDecoder
 * JD-Core Version:    0.7.0.1
 */