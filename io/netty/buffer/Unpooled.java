/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.PlatformDependent;
/*   4:    */ import java.nio.ByteBuffer;
/*   5:    */ import java.nio.ByteOrder;
/*   6:    */ import java.nio.CharBuffer;
/*   7:    */ import java.nio.charset.Charset;
/*   8:    */ import java.util.ArrayList;
/*   9:    */ import java.util.List;
/*  10:    */ 
/*  11:    */ public final class Unpooled
/*  12:    */ {
/*  13: 79 */   private static final ByteBufAllocator ALLOC = UnpooledByteBufAllocator.DEFAULT;
/*  14: 84 */   public static final ByteOrder BIG_ENDIAN = ByteOrder.BIG_ENDIAN;
/*  15: 89 */   public static final ByteOrder LITTLE_ENDIAN = ByteOrder.LITTLE_ENDIAN;
/*  16: 94 */   public static final ByteBuf EMPTY_BUFFER = ALLOC.buffer(0, 0);
/*  17:    */   
/*  18:    */   public static ByteBuf buffer()
/*  19:    */   {
/*  20:101 */     return ALLOC.heapBuffer();
/*  21:    */   }
/*  22:    */   
/*  23:    */   public static ByteBuf directBuffer()
/*  24:    */   {
/*  25:109 */     return ALLOC.directBuffer();
/*  26:    */   }
/*  27:    */   
/*  28:    */   public static ByteBuf buffer(int initialCapacity)
/*  29:    */   {
/*  30:118 */     return ALLOC.heapBuffer(initialCapacity);
/*  31:    */   }
/*  32:    */   
/*  33:    */   public static ByteBuf directBuffer(int initialCapacity)
/*  34:    */   {
/*  35:127 */     return ALLOC.directBuffer(initialCapacity);
/*  36:    */   }
/*  37:    */   
/*  38:    */   public static ByteBuf buffer(int initialCapacity, int maxCapacity)
/*  39:    */   {
/*  40:137 */     return ALLOC.heapBuffer(initialCapacity, maxCapacity);
/*  41:    */   }
/*  42:    */   
/*  43:    */   public static ByteBuf directBuffer(int initialCapacity, int maxCapacity)
/*  44:    */   {
/*  45:147 */     return ALLOC.directBuffer(initialCapacity, maxCapacity);
/*  46:    */   }
/*  47:    */   
/*  48:    */   public static ByteBuf wrappedBuffer(byte[] array)
/*  49:    */   {
/*  50:156 */     if (array.length == 0) {
/*  51:157 */       return EMPTY_BUFFER;
/*  52:    */     }
/*  53:159 */     return new UnpooledHeapByteBuf(ALLOC, array, array.length);
/*  54:    */   }
/*  55:    */   
/*  56:    */   public static ByteBuf wrappedBuffer(byte[] array, int offset, int length)
/*  57:    */   {
/*  58:168 */     if (length == 0) {
/*  59:169 */       return EMPTY_BUFFER;
/*  60:    */     }
/*  61:172 */     if ((offset == 0) && (length == array.length)) {
/*  62:173 */       return wrappedBuffer(array);
/*  63:    */     }
/*  64:176 */     return wrappedBuffer(array).slice(offset, length);
/*  65:    */   }
/*  66:    */   
/*  67:    */   public static ByteBuf wrappedBuffer(ByteBuffer buffer)
/*  68:    */   {
/*  69:185 */     if (!buffer.hasRemaining()) {
/*  70:186 */       return EMPTY_BUFFER;
/*  71:    */     }
/*  72:188 */     if (buffer.hasArray()) {
/*  73:189 */       return wrappedBuffer(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining()).order(buffer.order());
/*  74:    */     }
/*  75:193 */     if (PlatformDependent.hasUnsafe())
/*  76:    */     {
/*  77:194 */       if (buffer.isReadOnly())
/*  78:    */       {
/*  79:195 */         if (buffer.isDirect()) {
/*  80:196 */           return new ReadOnlyUnsafeDirectByteBuf(ALLOC, buffer);
/*  81:    */         }
/*  82:198 */         return new ReadOnlyByteBufferBuf(ALLOC, buffer);
/*  83:    */       }
/*  84:201 */       return new UnpooledUnsafeDirectByteBuf(ALLOC, buffer, buffer.remaining());
/*  85:    */     }
/*  86:204 */     if (buffer.isReadOnly()) {
/*  87:205 */       return new ReadOnlyByteBufferBuf(ALLOC, buffer);
/*  88:    */     }
/*  89:207 */     return new UnpooledDirectByteBuf(ALLOC, buffer, buffer.remaining());
/*  90:    */   }
/*  91:    */   
/*  92:    */   public static ByteBuf wrappedBuffer(ByteBuf buffer)
/*  93:    */   {
/*  94:218 */     if (buffer.isReadable()) {
/*  95:219 */       return buffer.slice();
/*  96:    */     }
/*  97:221 */     return EMPTY_BUFFER;
/*  98:    */   }
/*  99:    */   
/* 100:    */   public static ByteBuf wrappedBuffer(byte[]... arrays)
/* 101:    */   {
/* 102:231 */     return wrappedBuffer(16, arrays);
/* 103:    */   }
/* 104:    */   
/* 105:    */   public static ByteBuf wrappedBuffer(ByteBuf... buffers)
/* 106:    */   {
/* 107:240 */     return wrappedBuffer(16, buffers);
/* 108:    */   }
/* 109:    */   
/* 110:    */   public static ByteBuf wrappedBuffer(ByteBuffer... buffers)
/* 111:    */   {
/* 112:249 */     return wrappedBuffer(16, buffers);
/* 113:    */   }
/* 114:    */   
/* 115:    */   public static ByteBuf wrappedBuffer(int maxNumComponents, byte[]... arrays)
/* 116:    */   {
/* 117:258 */     switch (arrays.length)
/* 118:    */     {
/* 119:    */     case 0: 
/* 120:    */       break;
/* 121:    */     case 1: 
/* 122:262 */       if (arrays[0].length != 0) {
/* 123:263 */         return wrappedBuffer(arrays[0]);
/* 124:    */       }
/* 125:    */       break;
/* 126:    */     default: 
/* 127:268 */       List<ByteBuf> components = new ArrayList(arrays.length);
/* 128:269 */       for (byte[] a : arrays)
/* 129:    */       {
/* 130:270 */         if (a == null) {
/* 131:    */           break;
/* 132:    */         }
/* 133:273 */         if (a.length > 0) {
/* 134:274 */           components.add(wrappedBuffer(a));
/* 135:    */         }
/* 136:    */       }
/* 137:278 */       if (!components.isEmpty()) {
/* 138:279 */         return new CompositeByteBuf(ALLOC, false, maxNumComponents, components);
/* 139:    */       }
/* 140:    */       break;
/* 141:    */     }
/* 142:283 */     return EMPTY_BUFFER;
/* 143:    */   }
/* 144:    */   
/* 145:    */   public static ByteBuf wrappedBuffer(int maxNumComponents, ByteBuf... buffers)
/* 146:    */   {
/* 147:292 */     switch (buffers.length)
/* 148:    */     {
/* 149:    */     case 0: 
/* 150:    */       break;
/* 151:    */     case 1: 
/* 152:296 */       if (buffers[0].isReadable()) {
/* 153:297 */         return wrappedBuffer(buffers[0].order(BIG_ENDIAN));
/* 154:    */       }
/* 155:    */       break;
/* 156:    */     default: 
/* 157:301 */       for (ByteBuf b : buffers) {
/* 158:302 */         if (b.isReadable()) {
/* 159:303 */           return new CompositeByteBuf(ALLOC, false, maxNumComponents, buffers);
/* 160:    */         }
/* 161:    */       }
/* 162:    */     }
/* 163:307 */     return EMPTY_BUFFER;
/* 164:    */   }
/* 165:    */   
/* 166:    */   public static ByteBuf wrappedBuffer(int maxNumComponents, ByteBuffer... buffers)
/* 167:    */   {
/* 168:316 */     switch (buffers.length)
/* 169:    */     {
/* 170:    */     case 0: 
/* 171:    */       break;
/* 172:    */     case 1: 
/* 173:320 */       if (buffers[0].hasRemaining()) {
/* 174:321 */         return wrappedBuffer(buffers[0].order(BIG_ENDIAN));
/* 175:    */       }
/* 176:    */       break;
/* 177:    */     default: 
/* 178:326 */       List<ByteBuf> components = new ArrayList(buffers.length);
/* 179:327 */       for (ByteBuffer b : buffers)
/* 180:    */       {
/* 181:328 */         if (b == null) {
/* 182:    */           break;
/* 183:    */         }
/* 184:331 */         if (b.remaining() > 0) {
/* 185:332 */           components.add(wrappedBuffer(b.order(BIG_ENDIAN)));
/* 186:    */         }
/* 187:    */       }
/* 188:336 */       if (!components.isEmpty()) {
/* 189:337 */         return new CompositeByteBuf(ALLOC, false, maxNumComponents, components);
/* 190:    */       }
/* 191:    */       break;
/* 192:    */     }
/* 193:341 */     return EMPTY_BUFFER;
/* 194:    */   }
/* 195:    */   
/* 196:    */   public static CompositeByteBuf compositeBuffer()
/* 197:    */   {
/* 198:348 */     return compositeBuffer(16);
/* 199:    */   }
/* 200:    */   
/* 201:    */   public static CompositeByteBuf compositeBuffer(int maxNumComponents)
/* 202:    */   {
/* 203:355 */     return new CompositeByteBuf(ALLOC, false, maxNumComponents);
/* 204:    */   }
/* 205:    */   
/* 206:    */   public static ByteBuf copiedBuffer(byte[] array)
/* 207:    */   {
/* 208:364 */     if (array.length == 0) {
/* 209:365 */       return EMPTY_BUFFER;
/* 210:    */     }
/* 211:367 */     return wrappedBuffer((byte[])array.clone());
/* 212:    */   }
/* 213:    */   
/* 214:    */   public static ByteBuf copiedBuffer(byte[] array, int offset, int length)
/* 215:    */   {
/* 216:377 */     if (length == 0) {
/* 217:378 */       return EMPTY_BUFFER;
/* 218:    */     }
/* 219:380 */     byte[] copy = new byte[length];
/* 220:381 */     System.arraycopy(array, offset, copy, 0, length);
/* 221:382 */     return wrappedBuffer(copy);
/* 222:    */   }
/* 223:    */   
/* 224:    */   public static ByteBuf copiedBuffer(ByteBuffer buffer)
/* 225:    */   {
/* 226:392 */     int length = buffer.remaining();
/* 227:393 */     if (length == 0) {
/* 228:394 */       return EMPTY_BUFFER;
/* 229:    */     }
/* 230:396 */     byte[] copy = new byte[length];
/* 231:397 */     int position = buffer.position();
/* 232:    */     try
/* 233:    */     {
/* 234:399 */       buffer.get(copy);
/* 235:    */     }
/* 236:    */     finally
/* 237:    */     {
/* 238:401 */       buffer.position(position);
/* 239:    */     }
/* 240:403 */     return wrappedBuffer(copy).order(buffer.order());
/* 241:    */   }
/* 242:    */   
/* 243:    */   public static ByteBuf copiedBuffer(ByteBuf buffer)
/* 244:    */   {
/* 245:413 */     int readable = buffer.readableBytes();
/* 246:414 */     if (readable > 0)
/* 247:    */     {
/* 248:    */       ByteBuf copy;
/* 249:    */       ByteBuf copy;
/* 250:416 */       if (buffer.isDirect()) {
/* 251:417 */         copy = directBuffer(readable);
/* 252:    */       } else {
/* 253:419 */         copy = buffer(readable);
/* 254:    */       }
/* 255:421 */       copy.writeBytes(buffer, buffer.readerIndex(), readable);
/* 256:422 */       return copy;
/* 257:    */     }
/* 258:424 */     return EMPTY_BUFFER;
/* 259:    */   }
/* 260:    */   
/* 261:    */   public static ByteBuf copiedBuffer(byte[]... arrays)
/* 262:    */   {
/* 263:435 */     switch (arrays.length)
/* 264:    */     {
/* 265:    */     case 0: 
/* 266:437 */       return EMPTY_BUFFER;
/* 267:    */     case 1: 
/* 268:439 */       if (arrays[0].length == 0) {
/* 269:440 */         return EMPTY_BUFFER;
/* 270:    */       }
/* 271:442 */       return copiedBuffer(arrays[0]);
/* 272:    */     }
/* 273:447 */     int length = 0;
/* 274:448 */     for (byte[] a : arrays)
/* 275:    */     {
/* 276:449 */       if (2147483647 - length < a.length) {
/* 277:450 */         throw new IllegalArgumentException("The total length of the specified arrays is too big.");
/* 278:    */       }
/* 279:453 */       length += a.length;
/* 280:    */     }
/* 281:456 */     if (length == 0) {
/* 282:457 */       return EMPTY_BUFFER;
/* 283:    */     }
/* 284:460 */     byte[] mergedArray = new byte[length];
/* 285:461 */     int i = 0;
/* 286:461 */     for (int j = 0; i < arrays.length; i++)
/* 287:    */     {
/* 288:462 */       byte[] a = arrays[i];
/* 289:463 */       System.arraycopy(a, 0, mergedArray, j, a.length);
/* 290:464 */       j += a.length;
/* 291:    */     }
/* 292:467 */     return wrappedBuffer(mergedArray);
/* 293:    */   }
/* 294:    */   
/* 295:    */   public static ByteBuf copiedBuffer(ByteBuf... buffers)
/* 296:    */   {
/* 297:481 */     switch (buffers.length)
/* 298:    */     {
/* 299:    */     case 0: 
/* 300:483 */       return EMPTY_BUFFER;
/* 301:    */     case 1: 
/* 302:485 */       return copiedBuffer(buffers[0]);
/* 303:    */     }
/* 304:489 */     ByteOrder order = null;
/* 305:490 */     int length = 0;
/* 306:491 */     for (ByteBuf b : buffers)
/* 307:    */     {
/* 308:492 */       int bLen = b.readableBytes();
/* 309:493 */       if (bLen > 0)
/* 310:    */       {
/* 311:496 */         if (2147483647 - length < bLen) {
/* 312:497 */           throw new IllegalArgumentException("The total length of the specified buffers is too big.");
/* 313:    */         }
/* 314:500 */         length += bLen;
/* 315:501 */         if (order != null)
/* 316:    */         {
/* 317:502 */           if (!order.equals(b.order())) {
/* 318:503 */             throw new IllegalArgumentException("inconsistent byte order");
/* 319:    */           }
/* 320:    */         }
/* 321:    */         else {
/* 322:506 */           order = b.order();
/* 323:    */         }
/* 324:    */       }
/* 325:    */     }
/* 326:510 */     if (length == 0) {
/* 327:511 */       return EMPTY_BUFFER;
/* 328:    */     }
/* 329:514 */     byte[] mergedArray = new byte[length];
/* 330:515 */     int i = 0;
/* 331:515 */     for (int j = 0; i < buffers.length; i++)
/* 332:    */     {
/* 333:516 */       ByteBuf b = buffers[i];
/* 334:517 */       int bLen = b.readableBytes();
/* 335:518 */       b.getBytes(b.readerIndex(), mergedArray, j, bLen);
/* 336:519 */       j += bLen;
/* 337:    */     }
/* 338:522 */     return wrappedBuffer(mergedArray).order(order);
/* 339:    */   }
/* 340:    */   
/* 341:    */   public static ByteBuf copiedBuffer(ByteBuffer... buffers)
/* 342:    */   {
/* 343:536 */     switch (buffers.length)
/* 344:    */     {
/* 345:    */     case 0: 
/* 346:538 */       return EMPTY_BUFFER;
/* 347:    */     case 1: 
/* 348:540 */       return copiedBuffer(buffers[0]);
/* 349:    */     }
/* 350:544 */     ByteOrder order = null;
/* 351:545 */     int length = 0;
/* 352:546 */     for (ByteBuffer b : buffers)
/* 353:    */     {
/* 354:547 */       int bLen = b.remaining();
/* 355:548 */       if (bLen > 0)
/* 356:    */       {
/* 357:551 */         if (2147483647 - length < bLen) {
/* 358:552 */           throw new IllegalArgumentException("The total length of the specified buffers is too big.");
/* 359:    */         }
/* 360:555 */         length += bLen;
/* 361:556 */         if (order != null)
/* 362:    */         {
/* 363:557 */           if (!order.equals(b.order())) {
/* 364:558 */             throw new IllegalArgumentException("inconsistent byte order");
/* 365:    */           }
/* 366:    */         }
/* 367:    */         else {
/* 368:561 */           order = b.order();
/* 369:    */         }
/* 370:    */       }
/* 371:    */     }
/* 372:565 */     if (length == 0) {
/* 373:566 */       return EMPTY_BUFFER;
/* 374:    */     }
/* 375:569 */     byte[] mergedArray = new byte[length];
/* 376:570 */     int i = 0;
/* 377:570 */     for (int j = 0; i < buffers.length; i++)
/* 378:    */     {
/* 379:571 */       ByteBuffer b = buffers[i];
/* 380:572 */       int bLen = b.remaining();
/* 381:573 */       int oldPos = b.position();
/* 382:574 */       b.get(mergedArray, j, bLen);
/* 383:575 */       b.position(oldPos);
/* 384:576 */       j += bLen;
/* 385:    */     }
/* 386:579 */     return wrappedBuffer(mergedArray).order(order);
/* 387:    */   }
/* 388:    */   
/* 389:    */   public static ByteBuf copiedBuffer(CharSequence string, Charset charset)
/* 390:    */   {
/* 391:589 */     if (string == null) {
/* 392:590 */       throw new NullPointerException("string");
/* 393:    */     }
/* 394:593 */     if ((string instanceof CharBuffer)) {
/* 395:594 */       return copiedBuffer((CharBuffer)string, charset);
/* 396:    */     }
/* 397:597 */     return copiedBuffer(CharBuffer.wrap(string), charset);
/* 398:    */   }
/* 399:    */   
/* 400:    */   public static ByteBuf copiedBuffer(CharSequence string, int offset, int length, Charset charset)
/* 401:    */   {
/* 402:608 */     if (string == null) {
/* 403:609 */       throw new NullPointerException("string");
/* 404:    */     }
/* 405:611 */     if (length == 0) {
/* 406:612 */       return EMPTY_BUFFER;
/* 407:    */     }
/* 408:615 */     if ((string instanceof CharBuffer))
/* 409:    */     {
/* 410:616 */       CharBuffer buf = (CharBuffer)string;
/* 411:617 */       if (buf.hasArray()) {
/* 412:618 */         return copiedBuffer(buf.array(), buf.arrayOffset() + buf.position() + offset, length, charset);
/* 413:    */       }
/* 414:624 */       buf = buf.slice();
/* 415:625 */       buf.limit(length);
/* 416:626 */       buf.position(offset);
/* 417:627 */       return copiedBuffer(buf, charset);
/* 418:    */     }
/* 419:630 */     return copiedBuffer(CharBuffer.wrap(string, offset, offset + length), charset);
/* 420:    */   }
/* 421:    */   
/* 422:    */   public static ByteBuf copiedBuffer(char[] array, Charset charset)
/* 423:    */   {
/* 424:640 */     return copiedBuffer(array, 0, array.length, charset);
/* 425:    */   }
/* 426:    */   
/* 427:    */   public static ByteBuf copiedBuffer(char[] array, int offset, int length, Charset charset)
/* 428:    */   {
/* 429:650 */     if (array == null) {
/* 430:651 */       throw new NullPointerException("array");
/* 431:    */     }
/* 432:653 */     if (length == 0) {
/* 433:654 */       return EMPTY_BUFFER;
/* 434:    */     }
/* 435:656 */     return copiedBuffer(CharBuffer.wrap(array, offset, length), charset);
/* 436:    */   }
/* 437:    */   
/* 438:    */   private static ByteBuf copiedBuffer(CharBuffer buffer, Charset charset)
/* 439:    */   {
/* 440:660 */     return ByteBufUtil.encodeString(ALLOC, buffer, charset);
/* 441:    */   }
/* 442:    */   
/* 443:    */   public static ByteBuf unmodifiableBuffer(ByteBuf buffer)
/* 444:    */   {
/* 445:670 */     ByteOrder endianness = buffer.order();
/* 446:671 */     if (endianness == BIG_ENDIAN) {
/* 447:672 */       return new ReadOnlyByteBuf(buffer);
/* 448:    */     }
/* 449:675 */     return new ReadOnlyByteBuf(buffer.order(BIG_ENDIAN)).order(LITTLE_ENDIAN);
/* 450:    */   }
/* 451:    */   
/* 452:    */   public static ByteBuf copyInt(int value)
/* 453:    */   {
/* 454:682 */     ByteBuf buf = buffer(4);
/* 455:683 */     buf.writeInt(value);
/* 456:684 */     return buf;
/* 457:    */   }
/* 458:    */   
/* 459:    */   public static ByteBuf copyInt(int... values)
/* 460:    */   {
/* 461:691 */     if ((values == null) || (values.length == 0)) {
/* 462:692 */       return EMPTY_BUFFER;
/* 463:    */     }
/* 464:694 */     ByteBuf buffer = buffer(values.length * 4);
/* 465:695 */     for (int v : values) {
/* 466:696 */       buffer.writeInt(v);
/* 467:    */     }
/* 468:698 */     return buffer;
/* 469:    */   }
/* 470:    */   
/* 471:    */   public static ByteBuf copyShort(int value)
/* 472:    */   {
/* 473:705 */     ByteBuf buf = buffer(2);
/* 474:706 */     buf.writeShort(value);
/* 475:707 */     return buf;
/* 476:    */   }
/* 477:    */   
/* 478:    */   public static ByteBuf copyShort(short... values)
/* 479:    */   {
/* 480:714 */     if ((values == null) || (values.length == 0)) {
/* 481:715 */       return EMPTY_BUFFER;
/* 482:    */     }
/* 483:717 */     ByteBuf buffer = buffer(values.length * 2);
/* 484:718 */     for (int v : values) {
/* 485:719 */       buffer.writeShort(v);
/* 486:    */     }
/* 487:721 */     return buffer;
/* 488:    */   }
/* 489:    */   
/* 490:    */   public static ByteBuf copyShort(int... values)
/* 491:    */   {
/* 492:728 */     if ((values == null) || (values.length == 0)) {
/* 493:729 */       return EMPTY_BUFFER;
/* 494:    */     }
/* 495:731 */     ByteBuf buffer = buffer(values.length * 2);
/* 496:732 */     for (int v : values) {
/* 497:733 */       buffer.writeShort(v);
/* 498:    */     }
/* 499:735 */     return buffer;
/* 500:    */   }
/* 501:    */   
/* 502:    */   public static ByteBuf copyMedium(int value)
/* 503:    */   {
/* 504:742 */     ByteBuf buf = buffer(3);
/* 505:743 */     buf.writeMedium(value);
/* 506:744 */     return buf;
/* 507:    */   }
/* 508:    */   
/* 509:    */   public static ByteBuf copyMedium(int... values)
/* 510:    */   {
/* 511:751 */     if ((values == null) || (values.length == 0)) {
/* 512:752 */       return EMPTY_BUFFER;
/* 513:    */     }
/* 514:754 */     ByteBuf buffer = buffer(values.length * 3);
/* 515:755 */     for (int v : values) {
/* 516:756 */       buffer.writeMedium(v);
/* 517:    */     }
/* 518:758 */     return buffer;
/* 519:    */   }
/* 520:    */   
/* 521:    */   public static ByteBuf copyLong(long value)
/* 522:    */   {
/* 523:765 */     ByteBuf buf = buffer(8);
/* 524:766 */     buf.writeLong(value);
/* 525:767 */     return buf;
/* 526:    */   }
/* 527:    */   
/* 528:    */   public static ByteBuf copyLong(long... values)
/* 529:    */   {
/* 530:774 */     if ((values == null) || (values.length == 0)) {
/* 531:775 */       return EMPTY_BUFFER;
/* 532:    */     }
/* 533:777 */     ByteBuf buffer = buffer(values.length * 8);
/* 534:778 */     for (long v : values) {
/* 535:779 */       buffer.writeLong(v);
/* 536:    */     }
/* 537:781 */     return buffer;
/* 538:    */   }
/* 539:    */   
/* 540:    */   public static ByteBuf copyBoolean(boolean value)
/* 541:    */   {
/* 542:788 */     ByteBuf buf = buffer(1);
/* 543:789 */     buf.writeBoolean(value);
/* 544:790 */     return buf;
/* 545:    */   }
/* 546:    */   
/* 547:    */   public static ByteBuf copyBoolean(boolean... values)
/* 548:    */   {
/* 549:797 */     if ((values == null) || (values.length == 0)) {
/* 550:798 */       return EMPTY_BUFFER;
/* 551:    */     }
/* 552:800 */     ByteBuf buffer = buffer(values.length);
/* 553:801 */     for (boolean v : values) {
/* 554:802 */       buffer.writeBoolean(v);
/* 555:    */     }
/* 556:804 */     return buffer;
/* 557:    */   }
/* 558:    */   
/* 559:    */   public static ByteBuf copyFloat(float value)
/* 560:    */   {
/* 561:811 */     ByteBuf buf = buffer(4);
/* 562:812 */     buf.writeFloat(value);
/* 563:813 */     return buf;
/* 564:    */   }
/* 565:    */   
/* 566:    */   public static ByteBuf copyFloat(float... values)
/* 567:    */   {
/* 568:820 */     if ((values == null) || (values.length == 0)) {
/* 569:821 */       return EMPTY_BUFFER;
/* 570:    */     }
/* 571:823 */     ByteBuf buffer = buffer(values.length * 4);
/* 572:824 */     for (float v : values) {
/* 573:825 */       buffer.writeFloat(v);
/* 574:    */     }
/* 575:827 */     return buffer;
/* 576:    */   }
/* 577:    */   
/* 578:    */   public static ByteBuf copyDouble(double value)
/* 579:    */   {
/* 580:834 */     ByteBuf buf = buffer(8);
/* 581:835 */     buf.writeDouble(value);
/* 582:836 */     return buf;
/* 583:    */   }
/* 584:    */   
/* 585:    */   public static ByteBuf copyDouble(double... values)
/* 586:    */   {
/* 587:843 */     if ((values == null) || (values.length == 0)) {
/* 588:844 */       return EMPTY_BUFFER;
/* 589:    */     }
/* 590:846 */     ByteBuf buffer = buffer(values.length * 8);
/* 591:847 */     for (double v : values) {
/* 592:848 */       buffer.writeDouble(v);
/* 593:    */     }
/* 594:850 */     return buffer;
/* 595:    */   }
/* 596:    */   
/* 597:    */   public static ByteBuf unreleasableBuffer(ByteBuf buf)
/* 598:    */   {
/* 599:857 */     return new UnreleasableByteBuf(buf);
/* 600:    */   }
/* 601:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.Unpooled
 * JD-Core Version:    0.7.0.1
 */