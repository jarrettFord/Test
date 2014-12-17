/*   1:    */ package io.netty.handler.codec.http;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufProcessor;
/*   5:    */ import io.netty.buffer.ByteBufUtil;
/*   6:    */ import io.netty.buffer.Unpooled;
/*   7:    */ import io.netty.channel.ChannelHandlerContext;
/*   8:    */ import io.netty.handler.codec.DecoderResult;
/*   9:    */ import io.netty.handler.codec.ReplayingDecoder;
/*  10:    */ import io.netty.handler.codec.TooLongFrameException;
/*  11:    */ import io.netty.util.internal.AppendableCharSequence;
/*  12:    */ import java.util.List;
/*  13:    */ 
/*  14:    */ public abstract class HttpObjectDecoder
/*  15:    */   extends ReplayingDecoder<State>
/*  16:    */ {
/*  17:    */   private final int maxInitialLineLength;
/*  18:    */   private final int maxHeaderSize;
/*  19:    */   private final int maxChunkSize;
/*  20:    */   private final boolean chunkedSupported;
/*  21:    */   protected final boolean validateHeaders;
/*  22:110 */   private final AppendableCharSequence seq = new AppendableCharSequence(128);
/*  23:111 */   private final HeaderParser headerParser = new HeaderParser(this.seq);
/*  24:112 */   private final LineParser lineParser = new LineParser(this.seq);
/*  25:    */   private HttpMessage message;
/*  26:    */   private long chunkSize;
/*  27:    */   private int headerSize;
/*  28:117 */   private long contentLength = -9223372036854775808L;
/*  29:    */   
/*  30:    */   static enum State
/*  31:    */   {
/*  32:124 */     SKIP_CONTROL_CHARS,  READ_INITIAL,  READ_HEADER,  READ_VARIABLE_LENGTH_CONTENT,  READ_FIXED_LENGTH_CONTENT,  READ_CHUNK_SIZE,  READ_CHUNKED_CONTENT,  READ_CHUNK_DELIMITER,  READ_CHUNK_FOOTER,  BAD_MESSAGE,  UPGRADED;
/*  33:    */     
/*  34:    */     private State() {}
/*  35:    */   }
/*  36:    */   
/*  37:    */   protected HttpObjectDecoder()
/*  38:    */   {
/*  39:143 */     this(4096, 8192, 8192, true);
/*  40:    */   }
/*  41:    */   
/*  42:    */   protected HttpObjectDecoder(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean chunkedSupported)
/*  43:    */   {
/*  44:151 */     this(maxInitialLineLength, maxHeaderSize, maxChunkSize, chunkedSupported, true);
/*  45:    */   }
/*  46:    */   
/*  47:    */   protected HttpObjectDecoder(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean chunkedSupported, boolean validateHeaders)
/*  48:    */   {
/*  49:161 */     super(State.SKIP_CONTROL_CHARS);
/*  50:163 */     if (maxInitialLineLength <= 0) {
/*  51:164 */       throw new IllegalArgumentException("maxInitialLineLength must be a positive integer: " + maxInitialLineLength);
/*  52:    */     }
/*  53:168 */     if (maxHeaderSize <= 0) {
/*  54:169 */       throw new IllegalArgumentException("maxHeaderSize must be a positive integer: " + maxHeaderSize);
/*  55:    */     }
/*  56:173 */     if (maxChunkSize <= 0) {
/*  57:174 */       throw new IllegalArgumentException("maxChunkSize must be a positive integer: " + maxChunkSize);
/*  58:    */     }
/*  59:178 */     this.maxInitialLineLength = maxInitialLineLength;
/*  60:179 */     this.maxHeaderSize = maxHeaderSize;
/*  61:180 */     this.maxChunkSize = maxChunkSize;
/*  62:181 */     this.chunkedSupported = chunkedSupported;
/*  63:182 */     this.validateHeaders = validateHeaders;
/*  64:    */   }
/*  65:    */   
/*  66:    */   protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out)
/*  67:    */     throws Exception
/*  68:    */   {
/*  69:187 */     switch (1.$SwitchMap$io$netty$handler$codec$http$HttpObjectDecoder$State[((State)state()).ordinal()])
/*  70:    */     {
/*  71:    */     case 1: 
/*  72:    */       try
/*  73:    */       {
/*  74:190 */         skipControlCharacters(buffer);
/*  75:191 */         checkpoint(State.READ_INITIAL);
/*  76:    */       }
/*  77:    */       finally
/*  78:    */       {
/*  79:193 */         checkpoint();
/*  80:    */       }
/*  81:    */     case 2: 
/*  82:    */       try
/*  83:    */       {
/*  84:197 */         String[] initialLine = splitInitialLine(this.lineParser.parse(buffer));
/*  85:198 */         if (initialLine.length < 3)
/*  86:    */         {
/*  87:200 */           checkpoint(State.SKIP_CONTROL_CHARS);
/*  88:201 */           return;
/*  89:    */         }
/*  90:204 */         this.message = createMessage(initialLine);
/*  91:205 */         checkpoint(State.READ_HEADER);
/*  92:    */       }
/*  93:    */       catch (Exception e)
/*  94:    */       {
/*  95:208 */         out.add(invalidMessage(e));
/*  96:209 */         return;
/*  97:    */       }
/*  98:    */     case 3: 
/*  99:    */       try
/* 100:    */       {
/* 101:212 */         State nextState = readHeaders(buffer);
/* 102:213 */         checkpoint(nextState);
/* 103:214 */         if (nextState == State.READ_CHUNK_SIZE)
/* 104:    */         {
/* 105:215 */           if (!this.chunkedSupported) {
/* 106:216 */             throw new IllegalArgumentException("Chunked messages not supported");
/* 107:    */           }
/* 108:219 */           out.add(this.message);
/* 109:220 */           return;
/* 110:    */         }
/* 111:222 */         if (nextState == State.SKIP_CONTROL_CHARS)
/* 112:    */         {
/* 113:224 */           out.add(this.message);
/* 114:225 */           out.add(LastHttpContent.EMPTY_LAST_CONTENT);
/* 115:226 */           reset();
/* 116:227 */           return;
/* 117:    */         }
/* 118:229 */         long contentLength = contentLength();
/* 119:230 */         if ((contentLength == 0L) || ((contentLength == -1L) && (isDecodingRequest())))
/* 120:    */         {
/* 121:231 */           out.add(this.message);
/* 122:232 */           out.add(LastHttpContent.EMPTY_LAST_CONTENT);
/* 123:233 */           reset();
/* 124:234 */           return;
/* 125:    */         }
/* 126:237 */         assert ((nextState == State.READ_FIXED_LENGTH_CONTENT) || (nextState == State.READ_VARIABLE_LENGTH_CONTENT));
/* 127:    */         
/* 128:239 */         out.add(this.message);
/* 129:241 */         if (nextState == State.READ_FIXED_LENGTH_CONTENT) {
/* 130:243 */           this.chunkSize = contentLength;
/* 131:    */         }
/* 132:247 */         return;
/* 133:    */       }
/* 134:    */       catch (Exception e)
/* 135:    */       {
/* 136:249 */         out.add(invalidMessage(e));
/* 137:250 */         return;
/* 138:    */       }
/* 139:    */     case 4: 
/* 140:254 */       int toRead = Math.min(actualReadableBytes(), this.maxChunkSize);
/* 141:255 */       if (toRead > 0)
/* 142:    */       {
/* 143:256 */         ByteBuf content = ByteBufUtil.readBytes(ctx.alloc(), buffer, toRead);
/* 144:257 */         if (buffer.isReadable())
/* 145:    */         {
/* 146:258 */           out.add(new DefaultHttpContent(content));
/* 147:    */         }
/* 148:    */         else
/* 149:    */         {
/* 150:261 */           out.add(new DefaultLastHttpContent(content, this.validateHeaders));
/* 151:262 */           reset();
/* 152:    */         }
/* 153:    */       }
/* 154:264 */       else if (!buffer.isReadable())
/* 155:    */       {
/* 156:266 */         out.add(LastHttpContent.EMPTY_LAST_CONTENT);
/* 157:267 */         reset();
/* 158:    */       }
/* 159:269 */       return;
/* 160:    */     case 5: 
/* 161:272 */       int readLimit = actualReadableBytes();
/* 162:280 */       if (readLimit == 0) {
/* 163:281 */         return;
/* 164:    */       }
/* 165:284 */       int toRead = Math.min(readLimit, this.maxChunkSize);
/* 166:285 */       if (toRead > this.chunkSize) {
/* 167:286 */         toRead = (int)this.chunkSize;
/* 168:    */       }
/* 169:288 */       ByteBuf content = ByteBufUtil.readBytes(ctx.alloc(), buffer, toRead);
/* 170:289 */       this.chunkSize -= toRead;
/* 171:291 */       if (this.chunkSize == 0L)
/* 172:    */       {
/* 173:293 */         out.add(new DefaultLastHttpContent(content, this.validateHeaders));
/* 174:294 */         reset();
/* 175:    */       }
/* 176:    */       else
/* 177:    */       {
/* 178:296 */         out.add(new DefaultHttpContent(content));
/* 179:    */       }
/* 180:298 */       return;
/* 181:    */     case 6: 
/* 182:    */       try
/* 183:    */       {
/* 184:305 */         AppendableCharSequence line = this.lineParser.parse(buffer);
/* 185:306 */         int chunkSize = getChunkSize(line.toString());
/* 186:307 */         this.chunkSize = chunkSize;
/* 187:308 */         if (chunkSize == 0)
/* 188:    */         {
/* 189:309 */           checkpoint(State.READ_CHUNK_FOOTER);
/* 190:310 */           return;
/* 191:    */         }
/* 192:312 */         checkpoint(State.READ_CHUNKED_CONTENT);
/* 193:    */       }
/* 194:    */       catch (Exception e)
/* 195:    */       {
/* 196:315 */         out.add(invalidChunk(e));
/* 197:316 */         return;
/* 198:    */       }
/* 199:    */     case 7: 
/* 200:319 */       assert (this.chunkSize <= 2147483647L);
/* 201:320 */       int toRead = Math.min((int)this.chunkSize, this.maxChunkSize);
/* 202:    */       
/* 203:322 */       HttpContent chunk = new DefaultHttpContent(ByteBufUtil.readBytes(ctx.alloc(), buffer, toRead));
/* 204:323 */       this.chunkSize -= toRead;
/* 205:    */       
/* 206:325 */       out.add(chunk);
/* 207:327 */       if (this.chunkSize == 0L) {
/* 208:329 */         checkpoint(State.READ_CHUNK_DELIMITER);
/* 209:    */       } else {
/* 210:    */         return;
/* 211:    */       }
/* 212:    */     case 8: 
/* 213:    */       for (;;)
/* 214:    */       {
/* 215:336 */         byte next = buffer.readByte();
/* 216:337 */         if (next == 13)
/* 217:    */         {
/* 218:338 */           if (buffer.readByte() == 10) {
/* 219:339 */             checkpoint(State.READ_CHUNK_SIZE);
/* 220:    */           }
/* 221:    */         }
/* 222:    */         else
/* 223:    */         {
/* 224:342 */           if (next == 10)
/* 225:    */           {
/* 226:343 */             checkpoint(State.READ_CHUNK_SIZE);
/* 227:344 */             return;
/* 228:    */           }
/* 229:346 */           checkpoint();
/* 230:    */         }
/* 231:    */       }
/* 232:    */     case 9: 
/* 233:    */       try
/* 234:    */       {
/* 235:351 */         LastHttpContent trailer = readTrailingHeaders(buffer);
/* 236:352 */         out.add(trailer);
/* 237:353 */         reset();
/* 238:354 */         return;
/* 239:    */       }
/* 240:    */       catch (Exception e)
/* 241:    */       {
/* 242:356 */         out.add(invalidChunk(e));
/* 243:357 */         return;
/* 244:    */       }
/* 245:    */     case 10: 
/* 246:361 */       buffer.skipBytes(actualReadableBytes());
/* 247:362 */       break;
/* 248:    */     case 11: 
/* 249:365 */       int readableBytes = actualReadableBytes();
/* 250:366 */       if (readableBytes > 0) {
/* 251:371 */         out.add(buffer.readBytes(actualReadableBytes()));
/* 252:    */       }
/* 253:    */       break;
/* 254:    */     }
/* 255:    */   }
/* 256:    */   
/* 257:    */   protected void decodeLast(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/* 258:    */     throws Exception
/* 259:    */   {
/* 260:380 */     decode(ctx, in, out);
/* 261:383 */     if (this.message != null)
/* 262:    */     {
/* 263:    */       boolean prematureClosure;
/* 264:    */       boolean prematureClosure;
/* 265:387 */       if (isDecodingRequest()) {
/* 266:389 */         prematureClosure = true;
/* 267:    */       } else {
/* 268:394 */         prematureClosure = contentLength() > 0L;
/* 269:    */       }
/* 270:396 */       reset();
/* 271:398 */       if (!prematureClosure) {
/* 272:399 */         out.add(LastHttpContent.EMPTY_LAST_CONTENT);
/* 273:    */       }
/* 274:    */     }
/* 275:    */   }
/* 276:    */   
/* 277:    */   protected boolean isContentAlwaysEmpty(HttpMessage msg)
/* 278:    */   {
/* 279:405 */     if ((msg instanceof HttpResponse))
/* 280:    */     {
/* 281:406 */       HttpResponse res = (HttpResponse)msg;
/* 282:407 */       int code = res.getStatus().code();
/* 283:414 */       if ((code >= 100) && (code < 200)) {
/* 284:416 */         return (code != 101) || (res.headers().contains("Sec-WebSocket-Accept"));
/* 285:    */       }
/* 286:419 */       switch (code)
/* 287:    */       {
/* 288:    */       case 204: 
/* 289:    */       case 205: 
/* 290:    */       case 304: 
/* 291:421 */         return true;
/* 292:    */       }
/* 293:    */     }
/* 294:424 */     return false;
/* 295:    */   }
/* 296:    */   
/* 297:    */   private void reset()
/* 298:    */   {
/* 299:428 */     HttpMessage message = this.message;
/* 300:429 */     this.message = null;
/* 301:430 */     this.contentLength = -9223372036854775808L;
/* 302:431 */     if (!isDecodingRequest())
/* 303:    */     {
/* 304:432 */       HttpResponse res = (HttpResponse)message;
/* 305:433 */       if ((res != null) && (res.getStatus().code() == 101))
/* 306:    */       {
/* 307:434 */         checkpoint(State.UPGRADED);
/* 308:435 */         return;
/* 309:    */       }
/* 310:    */     }
/* 311:439 */     checkpoint(State.SKIP_CONTROL_CHARS);
/* 312:    */   }
/* 313:    */   
/* 314:    */   private HttpMessage invalidMessage(Exception cause)
/* 315:    */   {
/* 316:443 */     checkpoint(State.BAD_MESSAGE);
/* 317:444 */     if (this.message != null)
/* 318:    */     {
/* 319:445 */       this.message.setDecoderResult(DecoderResult.failure(cause));
/* 320:    */     }
/* 321:    */     else
/* 322:    */     {
/* 323:447 */       this.message = createInvalidMessage();
/* 324:448 */       this.message.setDecoderResult(DecoderResult.failure(cause));
/* 325:    */     }
/* 326:451 */     HttpMessage ret = this.message;
/* 327:452 */     this.message = null;
/* 328:453 */     return ret;
/* 329:    */   }
/* 330:    */   
/* 331:    */   private HttpContent invalidChunk(Exception cause)
/* 332:    */   {
/* 333:457 */     checkpoint(State.BAD_MESSAGE);
/* 334:458 */     HttpContent chunk = new DefaultLastHttpContent(Unpooled.EMPTY_BUFFER);
/* 335:459 */     chunk.setDecoderResult(DecoderResult.failure(cause));
/* 336:460 */     this.message = null;
/* 337:461 */     return chunk;
/* 338:    */   }
/* 339:    */   
/* 340:    */   private static void skipControlCharacters(ByteBuf buffer)
/* 341:    */   {
/* 342:    */     for (;;)
/* 343:    */     {
/* 344:466 */       char c = (char)buffer.readUnsignedByte();
/* 345:467 */       if ((!Character.isISOControl(c)) && (!Character.isWhitespace(c)))
/* 346:    */       {
/* 347:469 */         buffer.readerIndex(buffer.readerIndex() - 1);
/* 348:470 */         break;
/* 349:    */       }
/* 350:    */     }
/* 351:    */   }
/* 352:    */   
/* 353:    */   private State readHeaders(ByteBuf buffer)
/* 354:    */   {
/* 355:476 */     this.headerSize = 0;
/* 356:477 */     HttpMessage message = this.message;
/* 357:478 */     HttpHeaders headers = message.headers();
/* 358:    */     
/* 359:480 */     AppendableCharSequence line = this.headerParser.parse(buffer);
/* 360:481 */     String name = null;
/* 361:482 */     String value = null;
/* 362:483 */     if (line.length() > 0)
/* 363:    */     {
/* 364:484 */       headers.clear();
/* 365:    */       do
/* 366:    */       {
/* 367:486 */         char firstChar = line.charAt(0);
/* 368:487 */         if ((name != null) && ((firstChar == ' ') || (firstChar == '\t')))
/* 369:    */         {
/* 370:488 */           value = value + ' ' + line.toString().trim();
/* 371:    */         }
/* 372:    */         else
/* 373:    */         {
/* 374:490 */           if (name != null) {
/* 375:491 */             headers.add(name, value);
/* 376:    */           }
/* 377:493 */           String[] header = splitHeader(line);
/* 378:494 */           name = header[0];
/* 379:495 */           value = header[1];
/* 380:    */         }
/* 381:498 */         line = this.headerParser.parse(buffer);
/* 382:499 */       } while (line.length() > 0);
/* 383:502 */       if (name != null) {
/* 384:503 */         headers.add(name, value);
/* 385:    */       }
/* 386:    */     }
/* 387:    */     State nextState;
/* 388:    */     State nextState;
/* 389:509 */     if (isContentAlwaysEmpty(message))
/* 390:    */     {
/* 391:510 */       HttpHeaders.removeTransferEncodingChunked(message);
/* 392:511 */       nextState = State.SKIP_CONTROL_CHARS;
/* 393:    */     }
/* 394:    */     else
/* 395:    */     {
/* 396:    */       State nextState;
/* 397:512 */       if (HttpHeaders.isTransferEncodingChunked(message))
/* 398:    */       {
/* 399:513 */         nextState = State.READ_CHUNK_SIZE;
/* 400:    */       }
/* 401:    */       else
/* 402:    */       {
/* 403:    */         State nextState;
/* 404:514 */         if (contentLength() >= 0L) {
/* 405:515 */           nextState = State.READ_FIXED_LENGTH_CONTENT;
/* 406:    */         } else {
/* 407:517 */           nextState = State.READ_VARIABLE_LENGTH_CONTENT;
/* 408:    */         }
/* 409:    */       }
/* 410:    */     }
/* 411:519 */     return nextState;
/* 412:    */   }
/* 413:    */   
/* 414:    */   private long contentLength()
/* 415:    */   {
/* 416:523 */     if (this.contentLength == -9223372036854775808L) {
/* 417:524 */       this.contentLength = HttpHeaders.getContentLength(this.message, -1L);
/* 418:    */     }
/* 419:526 */     return this.contentLength;
/* 420:    */   }
/* 421:    */   
/* 422:    */   private LastHttpContent readTrailingHeaders(ByteBuf buffer)
/* 423:    */   {
/* 424:530 */     this.headerSize = 0;
/* 425:531 */     AppendableCharSequence line = this.headerParser.parse(buffer);
/* 426:532 */     String lastHeader = null;
/* 427:533 */     if (line.length() > 0)
/* 428:    */     {
/* 429:534 */       LastHttpContent trailer = new DefaultLastHttpContent(Unpooled.EMPTY_BUFFER, this.validateHeaders);
/* 430:    */       do
/* 431:    */       {
/* 432:536 */         char firstChar = line.charAt(0);
/* 433:537 */         if ((lastHeader != null) && ((firstChar == ' ') || (firstChar == '\t')))
/* 434:    */         {
/* 435:538 */           List<String> current = trailer.trailingHeaders().getAll(lastHeader);
/* 436:539 */           if (!current.isEmpty())
/* 437:    */           {
/* 438:540 */             int lastPos = current.size() - 1;
/* 439:541 */             String newString = (String)current.get(lastPos) + line.toString().trim();
/* 440:542 */             current.set(lastPos, newString);
/* 441:    */           }
/* 442:    */         }
/* 443:    */         else
/* 444:    */         {
/* 445:547 */           String[] header = splitHeader(line);
/* 446:548 */           String name = header[0];
/* 447:549 */           if ((!HttpHeaders.equalsIgnoreCase(name, "Content-Length")) && (!HttpHeaders.equalsIgnoreCase(name, "Transfer-Encoding")) && (!HttpHeaders.equalsIgnoreCase(name, "Trailer"))) {
/* 448:552 */             trailer.trailingHeaders().add(name, header[1]);
/* 449:    */           }
/* 450:554 */           lastHeader = name;
/* 451:    */         }
/* 452:557 */         line = this.headerParser.parse(buffer);
/* 453:558 */       } while (line.length() > 0);
/* 454:560 */       return trailer;
/* 455:    */     }
/* 456:563 */     return LastHttpContent.EMPTY_LAST_CONTENT;
/* 457:    */   }
/* 458:    */   
/* 459:    */   protected abstract boolean isDecodingRequest();
/* 460:    */   
/* 461:    */   protected abstract HttpMessage createMessage(String[] paramArrayOfString)
/* 462:    */     throws Exception;
/* 463:    */   
/* 464:    */   protected abstract HttpMessage createInvalidMessage();
/* 465:    */   
/* 466:    */   private static int getChunkSize(String hex)
/* 467:    */   {
/* 468:571 */     hex = hex.trim();
/* 469:572 */     for (int i = 0; i < hex.length(); i++)
/* 470:    */     {
/* 471:573 */       char c = hex.charAt(i);
/* 472:574 */       if ((c == ';') || (Character.isWhitespace(c)) || (Character.isISOControl(c)))
/* 473:    */       {
/* 474:575 */         hex = hex.substring(0, i);
/* 475:576 */         break;
/* 476:    */       }
/* 477:    */     }
/* 478:580 */     return Integer.parseInt(hex, 16);
/* 479:    */   }
/* 480:    */   
/* 481:    */   private static String[] splitInitialLine(AppendableCharSequence sb)
/* 482:    */   {
/* 483:591 */     int aStart = findNonWhitespace(sb, 0);
/* 484:592 */     int aEnd = findWhitespace(sb, aStart);
/* 485:    */     
/* 486:594 */     int bStart = findNonWhitespace(sb, aEnd);
/* 487:595 */     int bEnd = findWhitespace(sb, bStart);
/* 488:    */     
/* 489:597 */     int cStart = findNonWhitespace(sb, bEnd);
/* 490:598 */     int cEnd = findEndOfString(sb);
/* 491:    */     
/* 492:600 */     return new String[] { sb.substring(aStart, aEnd), sb.substring(bStart, bEnd), cStart < cEnd ? sb.substring(cStart, cEnd) : "" };
/* 493:    */   }
/* 494:    */   
/* 495:    */   private static String[] splitHeader(AppendableCharSequence sb)
/* 496:    */   {
/* 497:607 */     int length = sb.length();
/* 498:    */     
/* 499:    */ 
/* 500:    */ 
/* 501:    */ 
/* 502:    */ 
/* 503:    */ 
/* 504:614 */     int nameStart = findNonWhitespace(sb, 0);
/* 505:615 */     for (int nameEnd = nameStart; nameEnd < length; nameEnd++)
/* 506:    */     {
/* 507:616 */       char ch = sb.charAt(nameEnd);
/* 508:617 */       if ((ch == ':') || (Character.isWhitespace(ch))) {
/* 509:    */         break;
/* 510:    */       }
/* 511:    */     }
/* 512:622 */     for (int colonEnd = nameEnd; colonEnd < length; colonEnd++) {
/* 513:623 */       if (sb.charAt(colonEnd) == ':')
/* 514:    */       {
/* 515:624 */         colonEnd++;
/* 516:625 */         break;
/* 517:    */       }
/* 518:    */     }
/* 519:629 */     int valueStart = findNonWhitespace(sb, colonEnd);
/* 520:630 */     if (valueStart == length) {
/* 521:631 */       return new String[] { sb.substring(nameStart, nameEnd), "" };
/* 522:    */     }
/* 523:637 */     int valueEnd = findEndOfString(sb);
/* 524:638 */     return new String[] { sb.substring(nameStart, nameEnd), sb.substring(valueStart, valueEnd) };
/* 525:    */   }
/* 526:    */   
/* 527:    */   private static int findNonWhitespace(CharSequence sb, int offset)
/* 528:    */   {
/* 529:646 */     for (int result = offset; result < sb.length(); result++) {
/* 530:647 */       if (!Character.isWhitespace(sb.charAt(result))) {
/* 531:    */         break;
/* 532:    */       }
/* 533:    */     }
/* 534:651 */     return result;
/* 535:    */   }
/* 536:    */   
/* 537:    */   private static int findWhitespace(CharSequence sb, int offset)
/* 538:    */   {
/* 539:656 */     for (int result = offset; result < sb.length(); result++) {
/* 540:657 */       if (Character.isWhitespace(sb.charAt(result))) {
/* 541:    */         break;
/* 542:    */       }
/* 543:    */     }
/* 544:661 */     return result;
/* 545:    */   }
/* 546:    */   
/* 547:    */   private static int findEndOfString(CharSequence sb)
/* 548:    */   {
/* 549:666 */     for (int result = sb.length(); result > 0; result--) {
/* 550:667 */       if (!Character.isWhitespace(sb.charAt(result - 1))) {
/* 551:    */         break;
/* 552:    */       }
/* 553:    */     }
/* 554:671 */     return result;
/* 555:    */   }
/* 556:    */   
/* 557:    */   private final class HeaderParser
/* 558:    */     implements ByteBufProcessor
/* 559:    */   {
/* 560:    */     private final AppendableCharSequence seq;
/* 561:    */     
/* 562:    */     HeaderParser(AppendableCharSequence seq)
/* 563:    */     {
/* 564:678 */       this.seq = seq;
/* 565:    */     }
/* 566:    */     
/* 567:    */     public AppendableCharSequence parse(ByteBuf buffer)
/* 568:    */     {
/* 569:682 */       this.seq.reset();
/* 570:683 */       HttpObjectDecoder.this.headerSize = 0;
/* 571:684 */       int i = buffer.forEachByte(this);
/* 572:685 */       buffer.readerIndex(i + 1);
/* 573:686 */       return this.seq;
/* 574:    */     }
/* 575:    */     
/* 576:    */     public boolean process(byte value)
/* 577:    */       throws Exception
/* 578:    */     {
/* 579:691 */       char nextByte = (char)value;
/* 580:692 */       HttpObjectDecoder.access$008(HttpObjectDecoder.this);
/* 581:693 */       if (nextByte == '\r') {
/* 582:694 */         return true;
/* 583:    */       }
/* 584:696 */       if (nextByte == '\n') {
/* 585:697 */         return false;
/* 586:    */       }
/* 587:701 */       if (HttpObjectDecoder.this.headerSize >= HttpObjectDecoder.this.maxHeaderSize) {
/* 588:706 */         throw new TooLongFrameException("HTTP header is larger than " + HttpObjectDecoder.this.maxHeaderSize + " bytes.");
/* 589:    */       }
/* 590:711 */       this.seq.append(nextByte);
/* 591:712 */       return true;
/* 592:    */     }
/* 593:    */   }
/* 594:    */   
/* 595:    */   private final class LineParser
/* 596:    */     implements ByteBufProcessor
/* 597:    */   {
/* 598:    */     private final AppendableCharSequence seq;
/* 599:    */     private int size;
/* 600:    */     
/* 601:    */     LineParser(AppendableCharSequence seq)
/* 602:    */     {
/* 603:721 */       this.seq = seq;
/* 604:    */     }
/* 605:    */     
/* 606:    */     public AppendableCharSequence parse(ByteBuf buffer)
/* 607:    */     {
/* 608:725 */       this.seq.reset();
/* 609:726 */       this.size = 0;
/* 610:727 */       int i = buffer.forEachByte(this);
/* 611:728 */       buffer.readerIndex(i + 1);
/* 612:729 */       return this.seq;
/* 613:    */     }
/* 614:    */     
/* 615:    */     public boolean process(byte value)
/* 616:    */       throws Exception
/* 617:    */     {
/* 618:734 */       char nextByte = (char)value;
/* 619:735 */       if (nextByte == '\r') {
/* 620:736 */         return true;
/* 621:    */       }
/* 622:737 */       if (nextByte == '\n') {
/* 623:738 */         return false;
/* 624:    */       }
/* 625:740 */       if (this.size >= HttpObjectDecoder.this.maxInitialLineLength) {
/* 626:745 */         throw new TooLongFrameException("An HTTP line is larger than " + HttpObjectDecoder.this.maxInitialLineLength + " bytes.");
/* 627:    */       }
/* 628:749 */       this.size += 1;
/* 629:750 */       this.seq.append(nextByte);
/* 630:751 */       return true;
/* 631:    */     }
/* 632:    */   }
/* 633:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.HttpObjectDecoder
 * JD-Core Version:    0.7.0.1
 */