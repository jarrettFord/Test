/*   1:    */ package io.netty.handler.codec.spdy;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.buffer.Unpooled;
/*   6:    */ 
/*   7:    */ public class SpdyFrameDecoder
/*   8:    */ {
/*   9:    */   private final int spdyVersion;
/*  10:    */   private final int maxChunkSize;
/*  11:    */   private final SpdyFrameDecoderDelegate delegate;
/*  12:    */   private State state;
/*  13:    */   private byte flags;
/*  14:    */   private int length;
/*  15:    */   private int streamId;
/*  16:    */   private int numSettings;
/*  17:    */   
/*  18:    */   private static enum State
/*  19:    */   {
/*  20: 64 */     READ_COMMON_HEADER,  READ_DATA_FRAME,  READ_SYN_STREAM_FRAME,  READ_SYN_REPLY_FRAME,  READ_RST_STREAM_FRAME,  READ_SETTINGS_FRAME,  READ_SETTING,  READ_PING_FRAME,  READ_GOAWAY_FRAME,  READ_HEADERS_FRAME,  READ_WINDOW_UPDATE_FRAME,  READ_HEADER_BLOCK,  DISCARD_FRAME,  FRAME_ERROR;
/*  21:    */     
/*  22:    */     private State() {}
/*  23:    */   }
/*  24:    */   
/*  25:    */   public SpdyFrameDecoder(SpdyVersion spdyVersion, SpdyFrameDecoderDelegate delegate)
/*  26:    */   {
/*  27: 85 */     this(spdyVersion, delegate, 8192);
/*  28:    */   }
/*  29:    */   
/*  30:    */   public SpdyFrameDecoder(SpdyVersion spdyVersion, SpdyFrameDecoderDelegate delegate, int maxChunkSize)
/*  31:    */   {
/*  32: 92 */     if (spdyVersion == null) {
/*  33: 93 */       throw new NullPointerException("spdyVersion");
/*  34:    */     }
/*  35: 95 */     if (delegate == null) {
/*  36: 96 */       throw new NullPointerException("delegate");
/*  37:    */     }
/*  38: 98 */     if (maxChunkSize <= 0) {
/*  39: 99 */       throw new IllegalArgumentException("maxChunkSize must be a positive integer: " + maxChunkSize);
/*  40:    */     }
/*  41:102 */     this.spdyVersion = spdyVersion.getVersion();
/*  42:103 */     this.delegate = delegate;
/*  43:104 */     this.maxChunkSize = maxChunkSize;
/*  44:105 */     this.state = State.READ_COMMON_HEADER;
/*  45:    */   }
/*  46:    */   
/*  47:    */   public void decode(ByteBuf buffer)
/*  48:    */   {
/*  49:    */     for (;;)
/*  50:    */     {
/*  51:    */       boolean last;
/*  52:    */       int statusCode;
/*  53:113 */       switch (1.$SwitchMap$io$netty$handler$codec$spdy$SpdyFrameDecoder$State[this.state.ordinal()])
/*  54:    */       {
/*  55:    */       case 1: 
/*  56:115 */         if (buffer.readableBytes() < 8) {
/*  57:116 */           return;
/*  58:    */         }
/*  59:119 */         int frameOffset = buffer.readerIndex();
/*  60:120 */         int flagsOffset = frameOffset + 4;
/*  61:121 */         int lengthOffset = frameOffset + 5;
/*  62:122 */         buffer.skipBytes(8);
/*  63:    */         
/*  64:124 */         boolean control = (buffer.getByte(frameOffset) & 0x80) != 0;
/*  65:    */         int version;
/*  66:    */         int type;
/*  67:128 */         if (control)
/*  68:    */         {
/*  69:130 */           int version = SpdyCodecUtil.getUnsignedShort(buffer, frameOffset) & 0x7FFF;
/*  70:131 */           int type = SpdyCodecUtil.getUnsignedShort(buffer, frameOffset + 2);
/*  71:132 */           this.streamId = 0;
/*  72:    */         }
/*  73:    */         else
/*  74:    */         {
/*  75:135 */           version = this.spdyVersion;
/*  76:136 */           type = 0;
/*  77:137 */           this.streamId = SpdyCodecUtil.getUnsignedInt(buffer, frameOffset);
/*  78:    */         }
/*  79:140 */         this.flags = buffer.getByte(flagsOffset);
/*  80:141 */         this.length = SpdyCodecUtil.getUnsignedMedium(buffer, lengthOffset);
/*  81:144 */         if (version != this.spdyVersion)
/*  82:    */         {
/*  83:145 */           this.state = State.FRAME_ERROR;
/*  84:146 */           this.delegate.readFrameError("Invalid SPDY Version");
/*  85:    */         }
/*  86:147 */         else if (!isValidFrameHeader(this.streamId, type, this.flags, this.length))
/*  87:    */         {
/*  88:148 */           this.state = State.FRAME_ERROR;
/*  89:149 */           this.delegate.readFrameError("Invalid Frame Error");
/*  90:    */         }
/*  91:    */         else
/*  92:    */         {
/*  93:151 */           this.state = getNextState(type, this.length);
/*  94:    */         }
/*  95:153 */         break;
/*  96:    */       case 2: 
/*  97:156 */         if (this.length == 0)
/*  98:    */         {
/*  99:157 */           this.state = State.READ_COMMON_HEADER;
/* 100:158 */           this.delegate.readDataFrame(this.streamId, hasFlag(this.flags, (byte)1), Unpooled.buffer(0));
/* 101:    */         }
/* 102:    */         else
/* 103:    */         {
/* 104:163 */           int dataLength = Math.min(this.maxChunkSize, this.length);
/* 105:166 */           if (buffer.readableBytes() < dataLength) {
/* 106:167 */             return;
/* 107:    */           }
/* 108:170 */           ByteBuf data = buffer.alloc().buffer(dataLength);
/* 109:171 */           data.writeBytes(buffer, dataLength);
/* 110:172 */           this.length -= dataLength;
/* 111:174 */           if (this.length == 0) {
/* 112:175 */             this.state = State.READ_COMMON_HEADER;
/* 113:    */           }
/* 114:178 */           last = (this.length == 0) && (hasFlag(this.flags, (byte)1));
/* 115:    */           
/* 116:180 */           this.delegate.readDataFrame(this.streamId, last, data);
/* 117:    */         }
/* 118:181 */         break;
/* 119:    */       case 3: 
/* 120:184 */         if (buffer.readableBytes() < 10) {
/* 121:185 */           return;
/* 122:    */         }
/* 123:188 */         int offset = buffer.readerIndex();
/* 124:189 */         this.streamId = SpdyCodecUtil.getUnsignedInt(buffer, offset);
/* 125:190 */         int associatedToStreamId = SpdyCodecUtil.getUnsignedInt(buffer, offset + 4);
/* 126:191 */         byte priority = (byte)(buffer.getByte(offset + 8) >> 5 & 0x7);
/* 127:192 */         last = hasFlag(this.flags, (byte)1);
/* 128:193 */         boolean unidirectional = hasFlag(this.flags, (byte)2);
/* 129:194 */         buffer.skipBytes(10);
/* 130:195 */         this.length -= 10;
/* 131:197 */         if (this.streamId == 0)
/* 132:    */         {
/* 133:198 */           this.state = State.FRAME_ERROR;
/* 134:199 */           this.delegate.readFrameError("Invalid SYN_STREAM Frame");
/* 135:    */         }
/* 136:    */         else
/* 137:    */         {
/* 138:201 */           this.state = State.READ_HEADER_BLOCK;
/* 139:202 */           this.delegate.readSynStreamFrame(this.streamId, associatedToStreamId, priority, last, unidirectional);
/* 140:    */         }
/* 141:204 */         break;
/* 142:    */       case 4: 
/* 143:207 */         if (buffer.readableBytes() < 4) {
/* 144:208 */           return;
/* 145:    */         }
/* 146:211 */         this.streamId = SpdyCodecUtil.getUnsignedInt(buffer, buffer.readerIndex());
/* 147:212 */         last = hasFlag(this.flags, (byte)1);
/* 148:    */         
/* 149:214 */         buffer.skipBytes(4);
/* 150:215 */         this.length -= 4;
/* 151:217 */         if (this.streamId == 0)
/* 152:    */         {
/* 153:218 */           this.state = State.FRAME_ERROR;
/* 154:219 */           this.delegate.readFrameError("Invalid SYN_REPLY Frame");
/* 155:    */         }
/* 156:    */         else
/* 157:    */         {
/* 158:221 */           this.state = State.READ_HEADER_BLOCK;
/* 159:222 */           this.delegate.readSynReplyFrame(this.streamId, last);
/* 160:    */         }
/* 161:224 */         break;
/* 162:    */       case 5: 
/* 163:227 */         if (buffer.readableBytes() < 8) {
/* 164:228 */           return;
/* 165:    */         }
/* 166:231 */         this.streamId = SpdyCodecUtil.getUnsignedInt(buffer, buffer.readerIndex());
/* 167:232 */         statusCode = SpdyCodecUtil.getSignedInt(buffer, buffer.readerIndex() + 4);
/* 168:233 */         buffer.skipBytes(8);
/* 169:235 */         if ((this.streamId == 0) || (statusCode == 0))
/* 170:    */         {
/* 171:236 */           this.state = State.FRAME_ERROR;
/* 172:237 */           this.delegate.readFrameError("Invalid RST_STREAM Frame");
/* 173:    */         }
/* 174:    */         else
/* 175:    */         {
/* 176:239 */           this.state = State.READ_COMMON_HEADER;
/* 177:240 */           this.delegate.readRstStreamFrame(this.streamId, statusCode);
/* 178:    */         }
/* 179:242 */         break;
/* 180:    */       case 6: 
/* 181:245 */         if (buffer.readableBytes() < 4) {
/* 182:246 */           return;
/* 183:    */         }
/* 184:249 */         boolean clear = hasFlag(this.flags, (byte)1);
/* 185:    */         
/* 186:251 */         this.numSettings = SpdyCodecUtil.getUnsignedInt(buffer, buffer.readerIndex());
/* 187:252 */         buffer.skipBytes(4);
/* 188:253 */         this.length -= 4;
/* 189:256 */         if (((this.length & 0x7) != 0) || (this.length >> 3 != this.numSettings))
/* 190:    */         {
/* 191:257 */           this.state = State.FRAME_ERROR;
/* 192:258 */           this.delegate.readFrameError("Invalid SETTINGS Frame");
/* 193:    */         }
/* 194:    */         else
/* 195:    */         {
/* 196:260 */           this.state = State.READ_SETTING;
/* 197:261 */           this.delegate.readSettingsFrame(clear);
/* 198:    */         }
/* 199:263 */         break;
/* 200:    */       case 7: 
/* 201:266 */         if (this.numSettings == 0)
/* 202:    */         {
/* 203:267 */           this.state = State.READ_COMMON_HEADER;
/* 204:268 */           this.delegate.readSettingsEnd();
/* 205:    */         }
/* 206:    */         else
/* 207:    */         {
/* 208:272 */           if (buffer.readableBytes() < 8) {
/* 209:273 */             return;
/* 210:    */           }
/* 211:276 */           byte settingsFlags = buffer.getByte(buffer.readerIndex());
/* 212:277 */           int id = SpdyCodecUtil.getUnsignedMedium(buffer, buffer.readerIndex() + 1);
/* 213:278 */           int value = SpdyCodecUtil.getSignedInt(buffer, buffer.readerIndex() + 4);
/* 214:279 */           boolean persistValue = hasFlag(settingsFlags, (byte)1);
/* 215:280 */           boolean persisted = hasFlag(settingsFlags, (byte)2);
/* 216:281 */           buffer.skipBytes(8);
/* 217:    */           
/* 218:283 */           this.numSettings -= 1;
/* 219:    */           
/* 220:285 */           this.delegate.readSetting(id, value, persistValue, persisted);
/* 221:    */         }
/* 222:286 */         break;
/* 223:    */       case 8: 
/* 224:289 */         if (buffer.readableBytes() < 4) {
/* 225:290 */           return;
/* 226:    */         }
/* 227:293 */         int pingId = SpdyCodecUtil.getSignedInt(buffer, buffer.readerIndex());
/* 228:294 */         buffer.skipBytes(4);
/* 229:    */         
/* 230:296 */         this.state = State.READ_COMMON_HEADER;
/* 231:297 */         this.delegate.readPingFrame(pingId);
/* 232:298 */         break;
/* 233:    */       case 9: 
/* 234:301 */         if (buffer.readableBytes() < 8) {
/* 235:302 */           return;
/* 236:    */         }
/* 237:305 */         int lastGoodStreamId = SpdyCodecUtil.getUnsignedInt(buffer, buffer.readerIndex());
/* 238:306 */         statusCode = SpdyCodecUtil.getSignedInt(buffer, buffer.readerIndex() + 4);
/* 239:307 */         buffer.skipBytes(8);
/* 240:    */         
/* 241:309 */         this.state = State.READ_COMMON_HEADER;
/* 242:310 */         this.delegate.readGoAwayFrame(lastGoodStreamId, statusCode);
/* 243:311 */         break;
/* 244:    */       case 10: 
/* 245:314 */         if (buffer.readableBytes() < 4) {
/* 246:315 */           return;
/* 247:    */         }
/* 248:318 */         this.streamId = SpdyCodecUtil.getUnsignedInt(buffer, buffer.readerIndex());
/* 249:319 */         last = hasFlag(this.flags, (byte)1);
/* 250:    */         
/* 251:321 */         buffer.skipBytes(4);
/* 252:322 */         this.length -= 4;
/* 253:324 */         if (this.streamId == 0)
/* 254:    */         {
/* 255:325 */           this.state = State.FRAME_ERROR;
/* 256:326 */           this.delegate.readFrameError("Invalid HEADERS Frame");
/* 257:    */         }
/* 258:    */         else
/* 259:    */         {
/* 260:328 */           this.state = State.READ_HEADER_BLOCK;
/* 261:329 */           this.delegate.readHeadersFrame(this.streamId, last);
/* 262:    */         }
/* 263:331 */         break;
/* 264:    */       case 11: 
/* 265:334 */         if (buffer.readableBytes() < 8) {
/* 266:335 */           return;
/* 267:    */         }
/* 268:338 */         this.streamId = SpdyCodecUtil.getUnsignedInt(buffer, buffer.readerIndex());
/* 269:339 */         int deltaWindowSize = SpdyCodecUtil.getUnsignedInt(buffer, buffer.readerIndex() + 4);
/* 270:340 */         buffer.skipBytes(8);
/* 271:342 */         if (deltaWindowSize == 0)
/* 272:    */         {
/* 273:343 */           this.state = State.FRAME_ERROR;
/* 274:344 */           this.delegate.readFrameError("Invalid WINDOW_UPDATE Frame");
/* 275:    */         }
/* 276:    */         else
/* 277:    */         {
/* 278:346 */           this.state = State.READ_COMMON_HEADER;
/* 279:347 */           this.delegate.readWindowUpdateFrame(this.streamId, deltaWindowSize);
/* 280:    */         }
/* 281:349 */         break;
/* 282:    */       case 12: 
/* 283:352 */         if (this.length == 0)
/* 284:    */         {
/* 285:353 */           this.state = State.READ_COMMON_HEADER;
/* 286:354 */           this.delegate.readHeaderBlockEnd();
/* 287:    */         }
/* 288:    */         else
/* 289:    */         {
/* 290:358 */           if (!buffer.isReadable()) {
/* 291:359 */             return;
/* 292:    */           }
/* 293:362 */           int compressedBytes = Math.min(buffer.readableBytes(), this.length);
/* 294:363 */           ByteBuf headerBlock = buffer.alloc().buffer(compressedBytes);
/* 295:364 */           headerBlock.writeBytes(buffer, compressedBytes);
/* 296:365 */           this.length -= compressedBytes;
/* 297:    */           
/* 298:367 */           this.delegate.readHeaderBlock(headerBlock);
/* 299:    */         }
/* 300:368 */         break;
/* 301:    */       case 13: 
/* 302:371 */         int numBytes = Math.min(buffer.readableBytes(), this.length);
/* 303:372 */         buffer.skipBytes(numBytes);
/* 304:373 */         this.length -= numBytes;
/* 305:374 */         if (this.length == 0) {
/* 306:375 */           this.state = State.READ_COMMON_HEADER;
/* 307:    */         } else {
/* 308:378 */           return;
/* 309:    */         }
/* 310:    */         break;
/* 311:    */       case 14: 
/* 312:381 */         buffer.skipBytes(buffer.readableBytes());
/* 313:382 */         return;
/* 314:    */       default: 
/* 315:385 */         throw new Error("Shouldn't reach here.");
/* 316:    */       }
/* 317:    */     }
/* 318:    */   }
/* 319:    */   
/* 320:    */   private static boolean hasFlag(byte flags, byte flag)
/* 321:    */   {
/* 322:391 */     return (flags & flag) != 0;
/* 323:    */   }
/* 324:    */   
/* 325:    */   private static State getNextState(int type, int length)
/* 326:    */   {
/* 327:395 */     switch (type)
/* 328:    */     {
/* 329:    */     case 0: 
/* 330:397 */       return State.READ_DATA_FRAME;
/* 331:    */     case 1: 
/* 332:400 */       return State.READ_SYN_STREAM_FRAME;
/* 333:    */     case 2: 
/* 334:403 */       return State.READ_SYN_REPLY_FRAME;
/* 335:    */     case 3: 
/* 336:406 */       return State.READ_RST_STREAM_FRAME;
/* 337:    */     case 4: 
/* 338:409 */       return State.READ_SETTINGS_FRAME;
/* 339:    */     case 6: 
/* 340:412 */       return State.READ_PING_FRAME;
/* 341:    */     case 7: 
/* 342:415 */       return State.READ_GOAWAY_FRAME;
/* 343:    */     case 8: 
/* 344:418 */       return State.READ_HEADERS_FRAME;
/* 345:    */     case 9: 
/* 346:421 */       return State.READ_WINDOW_UPDATE_FRAME;
/* 347:    */     }
/* 348:424 */     if (length != 0) {
/* 349:425 */       return State.DISCARD_FRAME;
/* 350:    */     }
/* 351:427 */     return State.READ_COMMON_HEADER;
/* 352:    */   }
/* 353:    */   
/* 354:    */   private static boolean isValidFrameHeader(int streamId, int type, byte flags, int length)
/* 355:    */   {
/* 356:433 */     switch (type)
/* 357:    */     {
/* 358:    */     case 0: 
/* 359:435 */       return streamId != 0;
/* 360:    */     case 1: 
/* 361:438 */       return length >= 10;
/* 362:    */     case 2: 
/* 363:441 */       return length >= 4;
/* 364:    */     case 3: 
/* 365:444 */       return (flags == 0) && (length == 8);
/* 366:    */     case 4: 
/* 367:447 */       return length >= 4;
/* 368:    */     case 6: 
/* 369:450 */       return length == 4;
/* 370:    */     case 7: 
/* 371:453 */       return length == 8;
/* 372:    */     case 8: 
/* 373:456 */       return length >= 4;
/* 374:    */     case 9: 
/* 375:459 */       return length == 8;
/* 376:    */     }
/* 377:462 */     return true;
/* 378:    */   }
/* 379:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.SpdyFrameDecoder
 * JD-Core Version:    0.7.0.1
 */