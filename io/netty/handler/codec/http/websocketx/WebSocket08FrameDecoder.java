/*   1:    */ package io.netty.handler.codec.http.websocketx;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.buffer.Unpooled;
/*   6:    */ import io.netty.channel.Channel;
/*   7:    */ import io.netty.channel.ChannelFuture;
/*   8:    */ import io.netty.channel.ChannelFutureListener;
/*   9:    */ import io.netty.channel.ChannelHandlerContext;
/*  10:    */ import io.netty.handler.codec.CorruptedFrameException;
/*  11:    */ import io.netty.handler.codec.ReplayingDecoder;
/*  12:    */ import io.netty.handler.codec.TooLongFrameException;
/*  13:    */ import io.netty.util.internal.logging.InternalLogger;
/*  14:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  15:    */ import java.util.List;
/*  16:    */ 
/*  17:    */ public class WebSocket08FrameDecoder
/*  18:    */   extends ReplayingDecoder<State>
/*  19:    */   implements WebSocketFrameDecoder
/*  20:    */ {
/*  21: 75 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(WebSocket08FrameDecoder.class);
/*  22:    */   private static final byte OPCODE_CONT = 0;
/*  23:    */   private static final byte OPCODE_TEXT = 1;
/*  24:    */   private static final byte OPCODE_BINARY = 2;
/*  25:    */   private static final byte OPCODE_CLOSE = 8;
/*  26:    */   private static final byte OPCODE_PING = 9;
/*  27:    */   private static final byte OPCODE_PONG = 10;
/*  28:    */   private int fragmentedFramesCount;
/*  29:    */   private final long maxFramePayloadLength;
/*  30:    */   private boolean frameFinalFlag;
/*  31:    */   private int frameRsv;
/*  32:    */   private int frameOpcode;
/*  33:    */   private long framePayloadLength;
/*  34:    */   private ByteBuf framePayload;
/*  35:    */   private int framePayloadBytesRead;
/*  36:    */   private byte[] maskingKey;
/*  37:    */   private ByteBuf payloadBuffer;
/*  38:    */   private final boolean allowExtensions;
/*  39:    */   private final boolean maskedPayload;
/*  40:    */   private boolean receivedClosingHandshake;
/*  41:    */   private Utf8Validator utf8Validator;
/*  42:    */   
/*  43:    */   static enum State
/*  44:    */   {
/*  45:100 */     FRAME_START,  MASKING_KEY,  PAYLOAD,  CORRUPT;
/*  46:    */     
/*  47:    */     private State() {}
/*  48:    */   }
/*  49:    */   
/*  50:    */   public WebSocket08FrameDecoder(boolean maskedPayload, boolean allowExtensions, int maxFramePayloadLength)
/*  51:    */   {
/*  52:116 */     super(State.FRAME_START);
/*  53:117 */     this.maskedPayload = maskedPayload;
/*  54:118 */     this.allowExtensions = allowExtensions;
/*  55:119 */     this.maxFramePayloadLength = maxFramePayloadLength;
/*  56:    */   }
/*  57:    */   
/*  58:    */   protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/*  59:    */     throws Exception
/*  60:    */   {
/*  61:126 */     if (this.receivedClosingHandshake)
/*  62:    */     {
/*  63:127 */       in.skipBytes(actualReadableBytes());
/*  64:128 */       return;
/*  65:    */     }
/*  66:    */     try
/*  67:    */     {
/*  68:132 */       switch (1.$SwitchMap$io$netty$handler$codec$http$websocketx$WebSocket08FrameDecoder$State[((State)state()).ordinal()])
/*  69:    */       {
/*  70:    */       case 1: 
/*  71:134 */         this.framePayloadBytesRead = 0;
/*  72:135 */         this.framePayloadLength = -1L;
/*  73:136 */         this.framePayload = null;
/*  74:137 */         this.payloadBuffer = null;
/*  75:    */         
/*  76:    */ 
/*  77:140 */         byte b = in.readByte();
/*  78:141 */         this.frameFinalFlag = ((b & 0x80) != 0);
/*  79:142 */         this.frameRsv = ((b & 0x70) >> 4);
/*  80:143 */         this.frameOpcode = (b & 0xF);
/*  81:145 */         if (logger.isDebugEnabled()) {
/*  82:146 */           logger.debug("Decoding WebSocket Frame opCode={}", Integer.valueOf(this.frameOpcode));
/*  83:    */         }
/*  84:150 */         b = in.readByte();
/*  85:151 */         boolean frameMasked = (b & 0x80) != 0;
/*  86:152 */         int framePayloadLen1 = b & 0x7F;
/*  87:154 */         if ((this.frameRsv != 0) && (!this.allowExtensions))
/*  88:    */         {
/*  89:155 */           protocolViolation(ctx, "RSV != 0 and no extension negotiated, RSV:" + this.frameRsv);
/*  90:156 */           return;
/*  91:    */         }
/*  92:159 */         if ((this.maskedPayload) && (!frameMasked))
/*  93:    */         {
/*  94:160 */           protocolViolation(ctx, "unmasked client to server frame");
/*  95:161 */           return;
/*  96:    */         }
/*  97:163 */         if (this.frameOpcode > 7)
/*  98:    */         {
/*  99:166 */           if (!this.frameFinalFlag)
/* 100:    */           {
/* 101:167 */             protocolViolation(ctx, "fragmented control frame");
/* 102:168 */             return;
/* 103:    */           }
/* 104:172 */           if (framePayloadLen1 > 125)
/* 105:    */           {
/* 106:173 */             protocolViolation(ctx, "control frame with payload length > 125 octets");
/* 107:174 */             return;
/* 108:    */           }
/* 109:178 */           if ((this.frameOpcode != 8) && (this.frameOpcode != 9) && (this.frameOpcode != 10))
/* 110:    */           {
/* 111:180 */             protocolViolation(ctx, "control frame using reserved opcode " + this.frameOpcode);
/* 112:181 */             return;
/* 113:    */           }
/* 114:187 */           if ((this.frameOpcode == 8) && (framePayloadLen1 == 1)) {
/* 115:188 */             protocolViolation(ctx, "received close control frame with payload len 1");
/* 116:    */           }
/* 117:    */         }
/* 118:    */         else
/* 119:    */         {
/* 120:193 */           if ((this.frameOpcode != 0) && (this.frameOpcode != 1) && (this.frameOpcode != 2))
/* 121:    */           {
/* 122:195 */             protocolViolation(ctx, "data frame using reserved opcode " + this.frameOpcode);
/* 123:196 */             return;
/* 124:    */           }
/* 125:200 */           if ((this.fragmentedFramesCount == 0) && (this.frameOpcode == 0))
/* 126:    */           {
/* 127:201 */             protocolViolation(ctx, "received continuation data frame outside fragmented message");
/* 128:202 */             return;
/* 129:    */           }
/* 130:206 */           if ((this.fragmentedFramesCount != 0) && (this.frameOpcode != 0) && (this.frameOpcode != 9))
/* 131:    */           {
/* 132:207 */             protocolViolation(ctx, "received non-continuation data frame while inside fragmented message");
/* 133:    */             
/* 134:209 */             return;
/* 135:    */           }
/* 136:    */         }
/* 137:214 */         if (framePayloadLen1 == 126)
/* 138:    */         {
/* 139:215 */           this.framePayloadLength = in.readUnsignedShort();
/* 140:216 */           if (this.framePayloadLength < 126L) {
/* 141:217 */             protocolViolation(ctx, "invalid data frame length (not using minimal length encoding)");
/* 142:    */           }
/* 143:    */         }
/* 144:220 */         else if (framePayloadLen1 == 127)
/* 145:    */         {
/* 146:221 */           this.framePayloadLength = in.readLong();
/* 147:225 */           if (this.framePayloadLength < 65536L) {
/* 148:226 */             protocolViolation(ctx, "invalid data frame length (not using minimal length encoding)");
/* 149:    */           }
/* 150:    */         }
/* 151:    */         else
/* 152:    */         {
/* 153:230 */           this.framePayloadLength = framePayloadLen1;
/* 154:    */         }
/* 155:233 */         if (this.framePayloadLength > this.maxFramePayloadLength)
/* 156:    */         {
/* 157:234 */           protocolViolation(ctx, "Max frame length of " + this.maxFramePayloadLength + " has been exceeded.");
/* 158:235 */           return;
/* 159:    */         }
/* 160:238 */         if (logger.isDebugEnabled()) {
/* 161:239 */           logger.debug("Decoding WebSocket Frame length={}", Long.valueOf(this.framePayloadLength));
/* 162:    */         }
/* 163:242 */         checkpoint(State.MASKING_KEY);
/* 164:    */       case 2: 
/* 165:244 */         if (this.maskedPayload)
/* 166:    */         {
/* 167:245 */           if (this.maskingKey == null) {
/* 168:246 */             this.maskingKey = new byte[4];
/* 169:    */           }
/* 170:248 */           in.readBytes(this.maskingKey);
/* 171:    */         }
/* 172:250 */         checkpoint(State.PAYLOAD);
/* 173:    */       case 3: 
/* 174:254 */         int rbytes = actualReadableBytes();
/* 175:    */         
/* 176:256 */         long willHaveReadByteCount = this.framePayloadBytesRead + rbytes;
/* 177:260 */         if (willHaveReadByteCount == this.framePayloadLength)
/* 178:    */         {
/* 179:262 */           this.payloadBuffer = ctx.alloc().buffer(rbytes);
/* 180:263 */           this.payloadBuffer.writeBytes(in, rbytes);
/* 181:    */         }
/* 182:    */         else
/* 183:    */         {
/* 184:264 */           if (willHaveReadByteCount < this.framePayloadLength)
/* 185:    */           {
/* 186:268 */             if (this.framePayload == null) {
/* 187:269 */               this.framePayload = ctx.alloc().buffer(toFrameLength(this.framePayloadLength));
/* 188:    */             }
/* 189:271 */             this.framePayload.writeBytes(in, rbytes);
/* 190:272 */             this.framePayloadBytesRead += rbytes;
/* 191:    */             
/* 192:    */ 
/* 193:275 */             return;
/* 194:    */           }
/* 195:276 */           if (willHaveReadByteCount > this.framePayloadLength)
/* 196:    */           {
/* 197:279 */             if (this.framePayload == null) {
/* 198:280 */               this.framePayload = ctx.alloc().buffer(toFrameLength(this.framePayloadLength));
/* 199:    */             }
/* 200:282 */             this.framePayload.writeBytes(in, toFrameLength(this.framePayloadLength - this.framePayloadBytesRead));
/* 201:    */           }
/* 202:    */         }
/* 203:287 */         checkpoint(State.FRAME_START);
/* 204:290 */         if (this.framePayload == null)
/* 205:    */         {
/* 206:291 */           this.framePayload = this.payloadBuffer;
/* 207:292 */           this.payloadBuffer = null;
/* 208:    */         }
/* 209:293 */         else if (this.payloadBuffer != null)
/* 210:    */         {
/* 211:294 */           this.framePayload.writeBytes(this.payloadBuffer);
/* 212:295 */           this.payloadBuffer.release();
/* 213:296 */           this.payloadBuffer = null;
/* 214:    */         }
/* 215:300 */         if (this.maskedPayload) {
/* 216:301 */           unmask(this.framePayload);
/* 217:    */         }
/* 218:306 */         if (this.frameOpcode == 9)
/* 219:    */         {
/* 220:307 */           out.add(new PingWebSocketFrame(this.frameFinalFlag, this.frameRsv, this.framePayload));
/* 221:308 */           this.framePayload = null;
/* 222:309 */           return;
/* 223:    */         }
/* 224:311 */         if (this.frameOpcode == 10)
/* 225:    */         {
/* 226:312 */           out.add(new PongWebSocketFrame(this.frameFinalFlag, this.frameRsv, this.framePayload));
/* 227:313 */           this.framePayload = null;
/* 228:314 */           return;
/* 229:    */         }
/* 230:316 */         if (this.frameOpcode == 8)
/* 231:    */         {
/* 232:317 */           checkCloseFrameBody(ctx, this.framePayload);
/* 233:318 */           this.receivedClosingHandshake = true;
/* 234:319 */           out.add(new CloseWebSocketFrame(this.frameFinalFlag, this.frameRsv, this.framePayload));
/* 235:320 */           this.framePayload = null;
/* 236:321 */           return;
/* 237:    */         }
/* 238:326 */         if (this.frameFinalFlag)
/* 239:    */         {
/* 240:329 */           if (this.frameOpcode != 9)
/* 241:    */           {
/* 242:330 */             this.fragmentedFramesCount = 0;
/* 243:333 */             if ((this.frameOpcode == 1) || ((this.utf8Validator != null) && (this.utf8Validator.isChecking())))
/* 244:    */             {
/* 245:336 */               checkUTF8String(ctx, this.framePayload);
/* 246:    */               
/* 247:    */ 
/* 248:    */ 
/* 249:340 */               this.utf8Validator.finish();
/* 250:    */             }
/* 251:    */           }
/* 252:    */         }
/* 253:    */         else
/* 254:    */         {
/* 255:346 */           if (this.fragmentedFramesCount == 0)
/* 256:    */           {
/* 257:348 */             if (this.frameOpcode == 1) {
/* 258:349 */               checkUTF8String(ctx, this.framePayload);
/* 259:    */             }
/* 260:    */           }
/* 261:353 */           else if ((this.utf8Validator != null) && (this.utf8Validator.isChecking())) {
/* 262:354 */             checkUTF8String(ctx, this.framePayload);
/* 263:    */           }
/* 264:359 */           this.fragmentedFramesCount += 1;
/* 265:    */         }
/* 266:363 */         if (this.frameOpcode == 1)
/* 267:    */         {
/* 268:364 */           out.add(new TextWebSocketFrame(this.frameFinalFlag, this.frameRsv, this.framePayload));
/* 269:365 */           this.framePayload = null;
/* 270:366 */           return;
/* 271:    */         }
/* 272:367 */         if (this.frameOpcode == 2)
/* 273:    */         {
/* 274:368 */           out.add(new BinaryWebSocketFrame(this.frameFinalFlag, this.frameRsv, this.framePayload));
/* 275:369 */           this.framePayload = null;
/* 276:370 */           return;
/* 277:    */         }
/* 278:371 */         if (this.frameOpcode == 0)
/* 279:    */         {
/* 280:372 */           out.add(new ContinuationWebSocketFrame(this.frameFinalFlag, this.frameRsv, this.framePayload));
/* 281:373 */           this.framePayload = null;
/* 282:374 */           return;
/* 283:    */         }
/* 284:376 */         throw new UnsupportedOperationException("Cannot decode web socket frame with opcode: " + this.frameOpcode);
/* 285:    */       case 4: 
/* 286:382 */         in.readByte();
/* 287:383 */         return;
/* 288:    */       }
/* 289:385 */       throw new Error("Shouldn't reach here.");
/* 290:    */     }
/* 291:    */     catch (Exception e)
/* 292:    */     {
/* 293:388 */       if (this.payloadBuffer != null)
/* 294:    */       {
/* 295:389 */         if (this.payloadBuffer.refCnt() > 0) {
/* 296:390 */           this.payloadBuffer.release();
/* 297:    */         }
/* 298:392 */         this.payloadBuffer = null;
/* 299:    */       }
/* 300:394 */       if (this.framePayload != null)
/* 301:    */       {
/* 302:395 */         if (this.framePayload.refCnt() > 0) {
/* 303:396 */           this.framePayload.release();
/* 304:    */         }
/* 305:398 */         this.framePayload = null;
/* 306:    */       }
/* 307:400 */       throw e;
/* 308:    */     }
/* 309:    */   }
/* 310:    */   
/* 311:    */   private void unmask(ByteBuf frame)
/* 312:    */   {
/* 313:405 */     for (int i = frame.readerIndex(); i < frame.writerIndex(); i++) {
/* 314:406 */       frame.setByte(i, frame.getByte(i) ^ this.maskingKey[(i % 4)]);
/* 315:    */     }
/* 316:    */   }
/* 317:    */   
/* 318:    */   private void protocolViolation(ChannelHandlerContext ctx, String reason)
/* 319:    */   {
/* 320:411 */     protocolViolation(ctx, new CorruptedFrameException(reason));
/* 321:    */   }
/* 322:    */   
/* 323:    */   private void protocolViolation(ChannelHandlerContext ctx, CorruptedFrameException ex)
/* 324:    */   {
/* 325:415 */     checkpoint(State.CORRUPT);
/* 326:416 */     if (ctx.channel().isActive()) {
/* 327:417 */       ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
/* 328:    */     }
/* 329:419 */     throw ex;
/* 330:    */   }
/* 331:    */   
/* 332:    */   private static int toFrameLength(long l)
/* 333:    */   {
/* 334:423 */     if (l > 2147483647L) {
/* 335:424 */       throw new TooLongFrameException("Length:" + l);
/* 336:    */     }
/* 337:426 */     return (int)l;
/* 338:    */   }
/* 339:    */   
/* 340:    */   private void checkUTF8String(ChannelHandlerContext ctx, ByteBuf buffer)
/* 341:    */   {
/* 342:    */     try
/* 343:    */     {
/* 344:432 */       if (this.utf8Validator == null) {
/* 345:433 */         this.utf8Validator = new Utf8Validator();
/* 346:    */       }
/* 347:435 */       this.utf8Validator.check(buffer);
/* 348:    */     }
/* 349:    */     catch (CorruptedFrameException ex)
/* 350:    */     {
/* 351:437 */       protocolViolation(ctx, ex);
/* 352:    */     }
/* 353:    */   }
/* 354:    */   
/* 355:    */   protected void checkCloseFrameBody(ChannelHandlerContext ctx, ByteBuf buffer)
/* 356:    */   {
/* 357:444 */     if ((buffer == null) || (!buffer.isReadable())) {
/* 358:445 */       return;
/* 359:    */     }
/* 360:447 */     if (buffer.readableBytes() == 1) {
/* 361:448 */       protocolViolation(ctx, "Invalid close frame body");
/* 362:    */     }
/* 363:452 */     int idx = buffer.readerIndex();
/* 364:453 */     buffer.readerIndex(0);
/* 365:    */     
/* 366:    */ 
/* 367:456 */     int statusCode = buffer.readShort();
/* 368:457 */     if (((statusCode >= 0) && (statusCode <= 999)) || ((statusCode >= 1004) && (statusCode <= 1006)) || ((statusCode >= 1012) && (statusCode <= 2999))) {
/* 369:459 */       protocolViolation(ctx, "Invalid close frame getStatus code: " + statusCode);
/* 370:    */     }
/* 371:463 */     if (buffer.isReadable()) {
/* 372:    */       try
/* 373:    */       {
/* 374:465 */         new Utf8Validator().check(buffer);
/* 375:    */       }
/* 376:    */       catch (CorruptedFrameException ex)
/* 377:    */       {
/* 378:467 */         protocolViolation(ctx, ex);
/* 379:    */       }
/* 380:    */     }
/* 381:472 */     buffer.readerIndex(idx);
/* 382:    */   }
/* 383:    */   
/* 384:    */   public void channelInactive(ChannelHandlerContext ctx)
/* 385:    */     throws Exception
/* 386:    */   {
/* 387:477 */     super.channelInactive(ctx);
/* 388:481 */     if (this.framePayload != null) {
/* 389:482 */       this.framePayload.release();
/* 390:    */     }
/* 391:484 */     if (this.payloadBuffer != null) {
/* 392:485 */       this.payloadBuffer.release();
/* 393:    */     }
/* 394:    */   }
/* 395:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.websocketx.WebSocket08FrameDecoder
 * JD-Core Version:    0.7.0.1
 */