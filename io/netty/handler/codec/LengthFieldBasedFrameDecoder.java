/*   1:    */ package io.netty.handler.codec;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.channel.ChannelHandlerContext;
/*   6:    */ import java.nio.ByteOrder;
/*   7:    */ import java.util.List;
/*   8:    */ 
/*   9:    */ public class LengthFieldBasedFrameDecoder
/*  10:    */   extends ByteToMessageDecoder
/*  11:    */ {
/*  12:    */   private final ByteOrder byteOrder;
/*  13:    */   private final int maxFrameLength;
/*  14:    */   private final int lengthFieldOffset;
/*  15:    */   private final int lengthFieldLength;
/*  16:    */   private final int lengthFieldEndOffset;
/*  17:    */   private final int lengthAdjustment;
/*  18:    */   private final int initialBytesToStrip;
/*  19:    */   private final boolean failFast;
/*  20:    */   private boolean discardingTooLongFrame;
/*  21:    */   private long tooLongFrameLength;
/*  22:    */   private long bytesToDiscard;
/*  23:    */   
/*  24:    */   public LengthFieldBasedFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength)
/*  25:    */   {
/*  26:213 */     this(maxFrameLength, lengthFieldOffset, lengthFieldLength, 0, 0);
/*  27:    */   }
/*  28:    */   
/*  29:    */   public LengthFieldBasedFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip)
/*  30:    */   {
/*  31:236 */     this(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, true);
/*  32:    */   }
/*  33:    */   
/*  34:    */   public LengthFieldBasedFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip, boolean failFast)
/*  35:    */   {
/*  36:268 */     this(ByteOrder.BIG_ENDIAN, maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast);
/*  37:    */   }
/*  38:    */   
/*  39:    */   public LengthFieldBasedFrameDecoder(ByteOrder byteOrder, int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip, boolean failFast)
/*  40:    */   {
/*  41:301 */     if (byteOrder == null) {
/*  42:302 */       throw new NullPointerException("byteOrder");
/*  43:    */     }
/*  44:305 */     if (maxFrameLength <= 0) {
/*  45:306 */       throw new IllegalArgumentException("maxFrameLength must be a positive integer: " + maxFrameLength);
/*  46:    */     }
/*  47:311 */     if (lengthFieldOffset < 0) {
/*  48:312 */       throw new IllegalArgumentException("lengthFieldOffset must be a non-negative integer: " + lengthFieldOffset);
/*  49:    */     }
/*  50:317 */     if (initialBytesToStrip < 0) {
/*  51:318 */       throw new IllegalArgumentException("initialBytesToStrip must be a non-negative integer: " + initialBytesToStrip);
/*  52:    */     }
/*  53:323 */     if (lengthFieldOffset > maxFrameLength - lengthFieldLength) {
/*  54:324 */       throw new IllegalArgumentException("maxFrameLength (" + maxFrameLength + ") " + "must be equal to or greater than " + "lengthFieldOffset (" + lengthFieldOffset + ") + " + "lengthFieldLength (" + lengthFieldLength + ").");
/*  55:    */     }
/*  56:331 */     this.byteOrder = byteOrder;
/*  57:332 */     this.maxFrameLength = maxFrameLength;
/*  58:333 */     this.lengthFieldOffset = lengthFieldOffset;
/*  59:334 */     this.lengthFieldLength = lengthFieldLength;
/*  60:335 */     this.lengthAdjustment = lengthAdjustment;
/*  61:336 */     this.lengthFieldEndOffset = (lengthFieldOffset + lengthFieldLength);
/*  62:337 */     this.initialBytesToStrip = initialBytesToStrip;
/*  63:338 */     this.failFast = failFast;
/*  64:    */   }
/*  65:    */   
/*  66:    */   protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/*  67:    */     throws Exception
/*  68:    */   {
/*  69:343 */     Object decoded = decode(ctx, in);
/*  70:344 */     if (decoded != null) {
/*  71:345 */       out.add(decoded);
/*  72:    */     }
/*  73:    */   }
/*  74:    */   
/*  75:    */   protected Object decode(ChannelHandlerContext ctx, ByteBuf in)
/*  76:    */     throws Exception
/*  77:    */   {
/*  78:358 */     if (this.discardingTooLongFrame)
/*  79:    */     {
/*  80:359 */       long bytesToDiscard = this.bytesToDiscard;
/*  81:360 */       int localBytesToDiscard = (int)Math.min(bytesToDiscard, in.readableBytes());
/*  82:361 */       in.skipBytes(localBytesToDiscard);
/*  83:362 */       bytesToDiscard -= localBytesToDiscard;
/*  84:363 */       this.bytesToDiscard = bytesToDiscard;
/*  85:    */       
/*  86:365 */       failIfNecessary(false);
/*  87:    */     }
/*  88:368 */     if (in.readableBytes() < this.lengthFieldEndOffset) {
/*  89:369 */       return null;
/*  90:    */     }
/*  91:372 */     int actualLengthFieldOffset = in.readerIndex() + this.lengthFieldOffset;
/*  92:373 */     long frameLength = getUnadjustedFrameLength(in, actualLengthFieldOffset, this.lengthFieldLength, this.byteOrder);
/*  93:375 */     if (frameLength < 0L)
/*  94:    */     {
/*  95:376 */       in.skipBytes(this.lengthFieldEndOffset);
/*  96:377 */       throw new CorruptedFrameException("negative pre-adjustment length field: " + frameLength);
/*  97:    */     }
/*  98:381 */     frameLength += this.lengthAdjustment + this.lengthFieldEndOffset;
/*  99:383 */     if (frameLength < this.lengthFieldEndOffset)
/* 100:    */     {
/* 101:384 */       in.skipBytes(this.lengthFieldEndOffset);
/* 102:385 */       throw new CorruptedFrameException("Adjusted frame length (" + frameLength + ") is less " + "than lengthFieldEndOffset: " + this.lengthFieldEndOffset);
/* 103:    */     }
/* 104:390 */     if (frameLength > this.maxFrameLength)
/* 105:    */     {
/* 106:391 */       long discard = frameLength - in.readableBytes();
/* 107:392 */       this.tooLongFrameLength = frameLength;
/* 108:394 */       if (discard < 0L)
/* 109:    */       {
/* 110:396 */         in.skipBytes((int)frameLength);
/* 111:    */       }
/* 112:    */       else
/* 113:    */       {
/* 114:399 */         this.discardingTooLongFrame = true;
/* 115:400 */         this.bytesToDiscard = discard;
/* 116:401 */         in.skipBytes(in.readableBytes());
/* 117:    */       }
/* 118:403 */       failIfNecessary(true);
/* 119:404 */       return null;
/* 120:    */     }
/* 121:408 */     int frameLengthInt = (int)frameLength;
/* 122:409 */     if (in.readableBytes() < frameLengthInt) {
/* 123:410 */       return null;
/* 124:    */     }
/* 125:413 */     if (this.initialBytesToStrip > frameLengthInt)
/* 126:    */     {
/* 127:414 */       in.skipBytes(frameLengthInt);
/* 128:415 */       throw new CorruptedFrameException("Adjusted frame length (" + frameLength + ") is less " + "than initialBytesToStrip: " + this.initialBytesToStrip);
/* 129:    */     }
/* 130:419 */     in.skipBytes(this.initialBytesToStrip);
/* 131:    */     
/* 132:    */ 
/* 133:422 */     int readerIndex = in.readerIndex();
/* 134:423 */     int actualFrameLength = frameLengthInt - this.initialBytesToStrip;
/* 135:424 */     ByteBuf frame = extractFrame(ctx, in, readerIndex, actualFrameLength);
/* 136:425 */     in.readerIndex(readerIndex + actualFrameLength);
/* 137:426 */     return frame;
/* 138:    */   }
/* 139:    */   
/* 140:    */   protected long getUnadjustedFrameLength(ByteBuf buf, int offset, int length, ByteOrder order)
/* 141:    */   {
/* 142:438 */     buf = buf.order(order);
/* 143:    */     long frameLength;
/* 144:440 */     switch (length)
/* 145:    */     {
/* 146:    */     case 1: 
/* 147:442 */       frameLength = buf.getUnsignedByte(offset);
/* 148:443 */       break;
/* 149:    */     case 2: 
/* 150:445 */       frameLength = buf.getUnsignedShort(offset);
/* 151:446 */       break;
/* 152:    */     case 3: 
/* 153:448 */       frameLength = buf.getUnsignedMedium(offset);
/* 154:449 */       break;
/* 155:    */     case 4: 
/* 156:451 */       frameLength = buf.getUnsignedInt(offset);
/* 157:452 */       break;
/* 158:    */     case 8: 
/* 159:454 */       frameLength = buf.getLong(offset);
/* 160:455 */       break;
/* 161:    */     case 5: 
/* 162:    */     case 6: 
/* 163:    */     case 7: 
/* 164:    */     default: 
/* 165:457 */       throw new DecoderException("unsupported lengthFieldLength: " + this.lengthFieldLength + " (expected: 1, 2, 3, 4, or 8)");
/* 166:    */     }
/* 167:460 */     return frameLength;
/* 168:    */   }
/* 169:    */   
/* 170:    */   private void failIfNecessary(boolean firstDetectionOfTooLongFrame)
/* 171:    */   {
/* 172:464 */     if (this.bytesToDiscard == 0L)
/* 173:    */     {
/* 174:467 */       long tooLongFrameLength = this.tooLongFrameLength;
/* 175:468 */       this.tooLongFrameLength = 0L;
/* 176:469 */       this.discardingTooLongFrame = false;
/* 177:470 */       if ((!this.failFast) || ((this.failFast) && (firstDetectionOfTooLongFrame))) {
/* 178:472 */         fail(tooLongFrameLength);
/* 179:    */       }
/* 180:    */     }
/* 181:476 */     else if ((this.failFast) && (firstDetectionOfTooLongFrame))
/* 182:    */     {
/* 183:477 */       fail(this.tooLongFrameLength);
/* 184:    */     }
/* 185:    */   }
/* 186:    */   
/* 187:    */   protected ByteBuf extractFrame(ChannelHandlerContext ctx, ByteBuf buffer, int index, int length)
/* 188:    */   {
/* 189:494 */     ByteBuf frame = ctx.alloc().buffer(length);
/* 190:495 */     frame.writeBytes(buffer, index, length);
/* 191:496 */     return frame;
/* 192:    */   }
/* 193:    */   
/* 194:    */   private void fail(long frameLength)
/* 195:    */   {
/* 196:500 */     if (frameLength > 0L) {
/* 197:501 */       throw new TooLongFrameException("Adjusted frame length exceeds " + this.maxFrameLength + ": " + frameLength + " - discarded");
/* 198:    */     }
/* 199:505 */     throw new TooLongFrameException("Adjusted frame length exceeds " + this.maxFrameLength + " - discarding");
/* 200:    */   }
/* 201:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.LengthFieldBasedFrameDecoder
 * JD-Core Version:    0.7.0.1
 */