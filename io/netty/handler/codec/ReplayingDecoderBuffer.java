/*    1:     */ package io.netty.handler.codec;
/*    2:     */ 
/*    3:     */ import io.netty.buffer.ByteBuf;
/*    4:     */ import io.netty.buffer.ByteBufAllocator;
/*    5:     */ import io.netty.buffer.ByteBufProcessor;
/*    6:     */ import io.netty.buffer.SwappedByteBuf;
/*    7:     */ import io.netty.buffer.Unpooled;
/*    8:     */ import io.netty.util.Signal;
/*    9:     */ import io.netty.util.internal.StringUtil;
/*   10:     */ import java.io.InputStream;
/*   11:     */ import java.io.OutputStream;
/*   12:     */ import java.nio.ByteBuffer;
/*   13:     */ import java.nio.ByteOrder;
/*   14:     */ import java.nio.channels.GatheringByteChannel;
/*   15:     */ import java.nio.channels.ScatteringByteChannel;
/*   16:     */ import java.nio.charset.Charset;
/*   17:     */ 
/*   18:     */ final class ReplayingDecoderBuffer
/*   19:     */   extends ByteBuf
/*   20:     */ {
/*   21:  39 */   private static final Signal REPLAY = ReplayingDecoder.REPLAY;
/*   22:     */   private ByteBuf buffer;
/*   23:     */   private boolean terminated;
/*   24:     */   private SwappedByteBuf swapped;
/*   25:  45 */   static final ReplayingDecoderBuffer EMPTY_BUFFER = new ReplayingDecoderBuffer(Unpooled.EMPTY_BUFFER);
/*   26:     */   
/*   27:     */   static
/*   28:     */   {
/*   29:  48 */     EMPTY_BUFFER.terminate();
/*   30:     */   }
/*   31:     */   
/*   32:     */   ReplayingDecoderBuffer(ByteBuf buffer)
/*   33:     */   {
/*   34:  54 */     setCumulation(buffer);
/*   35:     */   }
/*   36:     */   
/*   37:     */   void setCumulation(ByteBuf buffer)
/*   38:     */   {
/*   39:  58 */     this.buffer = buffer;
/*   40:     */   }
/*   41:     */   
/*   42:     */   void terminate()
/*   43:     */   {
/*   44:  62 */     this.terminated = true;
/*   45:     */   }
/*   46:     */   
/*   47:     */   public int capacity()
/*   48:     */   {
/*   49:  67 */     if (this.terminated) {
/*   50:  68 */       return this.buffer.capacity();
/*   51:     */     }
/*   52:  70 */     return 2147483647;
/*   53:     */   }
/*   54:     */   
/*   55:     */   public ByteBuf capacity(int newCapacity)
/*   56:     */   {
/*   57:  76 */     reject();
/*   58:  77 */     return this;
/*   59:     */   }
/*   60:     */   
/*   61:     */   public int maxCapacity()
/*   62:     */   {
/*   63:  82 */     return capacity();
/*   64:     */   }
/*   65:     */   
/*   66:     */   public ByteBufAllocator alloc()
/*   67:     */   {
/*   68:  87 */     return this.buffer.alloc();
/*   69:     */   }
/*   70:     */   
/*   71:     */   public boolean isDirect()
/*   72:     */   {
/*   73:  92 */     return this.buffer.isDirect();
/*   74:     */   }
/*   75:     */   
/*   76:     */   public boolean hasArray()
/*   77:     */   {
/*   78:  97 */     return false;
/*   79:     */   }
/*   80:     */   
/*   81:     */   public byte[] array()
/*   82:     */   {
/*   83: 102 */     throw new UnsupportedOperationException();
/*   84:     */   }
/*   85:     */   
/*   86:     */   public int arrayOffset()
/*   87:     */   {
/*   88: 107 */     throw new UnsupportedOperationException();
/*   89:     */   }
/*   90:     */   
/*   91:     */   public boolean hasMemoryAddress()
/*   92:     */   {
/*   93: 112 */     return false;
/*   94:     */   }
/*   95:     */   
/*   96:     */   public long memoryAddress()
/*   97:     */   {
/*   98: 117 */     throw new UnsupportedOperationException();
/*   99:     */   }
/*  100:     */   
/*  101:     */   public ByteBuf clear()
/*  102:     */   {
/*  103: 122 */     reject();
/*  104: 123 */     return this;
/*  105:     */   }
/*  106:     */   
/*  107:     */   public boolean equals(Object obj)
/*  108:     */   {
/*  109: 128 */     return this == obj;
/*  110:     */   }
/*  111:     */   
/*  112:     */   public int compareTo(ByteBuf buffer)
/*  113:     */   {
/*  114: 133 */     reject();
/*  115: 134 */     return 0;
/*  116:     */   }
/*  117:     */   
/*  118:     */   public ByteBuf copy()
/*  119:     */   {
/*  120: 139 */     reject();
/*  121: 140 */     return this;
/*  122:     */   }
/*  123:     */   
/*  124:     */   public ByteBuf copy(int index, int length)
/*  125:     */   {
/*  126: 145 */     checkIndex(index, length);
/*  127: 146 */     return this.buffer.copy(index, length);
/*  128:     */   }
/*  129:     */   
/*  130:     */   public ByteBuf discardReadBytes()
/*  131:     */   {
/*  132: 151 */     reject();
/*  133: 152 */     return this;
/*  134:     */   }
/*  135:     */   
/*  136:     */   public ByteBuf ensureWritable(int writableBytes)
/*  137:     */   {
/*  138: 157 */     reject();
/*  139: 158 */     return this;
/*  140:     */   }
/*  141:     */   
/*  142:     */   public int ensureWritable(int minWritableBytes, boolean force)
/*  143:     */   {
/*  144: 163 */     reject();
/*  145: 164 */     return 0;
/*  146:     */   }
/*  147:     */   
/*  148:     */   public ByteBuf duplicate()
/*  149:     */   {
/*  150: 169 */     reject();
/*  151: 170 */     return this;
/*  152:     */   }
/*  153:     */   
/*  154:     */   public boolean getBoolean(int index)
/*  155:     */   {
/*  156: 175 */     checkIndex(index, 1);
/*  157: 176 */     return this.buffer.getBoolean(index);
/*  158:     */   }
/*  159:     */   
/*  160:     */   public byte getByte(int index)
/*  161:     */   {
/*  162: 181 */     checkIndex(index, 1);
/*  163: 182 */     return this.buffer.getByte(index);
/*  164:     */   }
/*  165:     */   
/*  166:     */   public short getUnsignedByte(int index)
/*  167:     */   {
/*  168: 187 */     checkIndex(index, 1);
/*  169: 188 */     return this.buffer.getUnsignedByte(index);
/*  170:     */   }
/*  171:     */   
/*  172:     */   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/*  173:     */   {
/*  174: 193 */     checkIndex(index, length);
/*  175: 194 */     this.buffer.getBytes(index, dst, dstIndex, length);
/*  176: 195 */     return this;
/*  177:     */   }
/*  178:     */   
/*  179:     */   public ByteBuf getBytes(int index, byte[] dst)
/*  180:     */   {
/*  181: 200 */     checkIndex(index, dst.length);
/*  182: 201 */     this.buffer.getBytes(index, dst);
/*  183: 202 */     return this;
/*  184:     */   }
/*  185:     */   
/*  186:     */   public ByteBuf getBytes(int index, ByteBuffer dst)
/*  187:     */   {
/*  188: 207 */     reject();
/*  189: 208 */     return this;
/*  190:     */   }
/*  191:     */   
/*  192:     */   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/*  193:     */   {
/*  194: 213 */     checkIndex(index, length);
/*  195: 214 */     this.buffer.getBytes(index, dst, dstIndex, length);
/*  196: 215 */     return this;
/*  197:     */   }
/*  198:     */   
/*  199:     */   public ByteBuf getBytes(int index, ByteBuf dst, int length)
/*  200:     */   {
/*  201: 220 */     reject();
/*  202: 221 */     return this;
/*  203:     */   }
/*  204:     */   
/*  205:     */   public ByteBuf getBytes(int index, ByteBuf dst)
/*  206:     */   {
/*  207: 226 */     reject();
/*  208: 227 */     return this;
/*  209:     */   }
/*  210:     */   
/*  211:     */   public int getBytes(int index, GatheringByteChannel out, int length)
/*  212:     */   {
/*  213: 232 */     reject();
/*  214: 233 */     return 0;
/*  215:     */   }
/*  216:     */   
/*  217:     */   public ByteBuf getBytes(int index, OutputStream out, int length)
/*  218:     */   {
/*  219: 238 */     reject();
/*  220: 239 */     return this;
/*  221:     */   }
/*  222:     */   
/*  223:     */   public int getInt(int index)
/*  224:     */   {
/*  225: 244 */     checkIndex(index, 4);
/*  226: 245 */     return this.buffer.getInt(index);
/*  227:     */   }
/*  228:     */   
/*  229:     */   public long getUnsignedInt(int index)
/*  230:     */   {
/*  231: 250 */     checkIndex(index, 4);
/*  232: 251 */     return this.buffer.getUnsignedInt(index);
/*  233:     */   }
/*  234:     */   
/*  235:     */   public long getLong(int index)
/*  236:     */   {
/*  237: 256 */     checkIndex(index, 8);
/*  238: 257 */     return this.buffer.getLong(index);
/*  239:     */   }
/*  240:     */   
/*  241:     */   public int getMedium(int index)
/*  242:     */   {
/*  243: 262 */     checkIndex(index, 3);
/*  244: 263 */     return this.buffer.getMedium(index);
/*  245:     */   }
/*  246:     */   
/*  247:     */   public int getUnsignedMedium(int index)
/*  248:     */   {
/*  249: 268 */     checkIndex(index, 3);
/*  250: 269 */     return this.buffer.getUnsignedMedium(index);
/*  251:     */   }
/*  252:     */   
/*  253:     */   public short getShort(int index)
/*  254:     */   {
/*  255: 274 */     checkIndex(index, 2);
/*  256: 275 */     return this.buffer.getShort(index);
/*  257:     */   }
/*  258:     */   
/*  259:     */   public int getUnsignedShort(int index)
/*  260:     */   {
/*  261: 280 */     checkIndex(index, 2);
/*  262: 281 */     return this.buffer.getUnsignedShort(index);
/*  263:     */   }
/*  264:     */   
/*  265:     */   public char getChar(int index)
/*  266:     */   {
/*  267: 286 */     checkIndex(index, 2);
/*  268: 287 */     return this.buffer.getChar(index);
/*  269:     */   }
/*  270:     */   
/*  271:     */   public float getFloat(int index)
/*  272:     */   {
/*  273: 292 */     checkIndex(index, 4);
/*  274: 293 */     return this.buffer.getFloat(index);
/*  275:     */   }
/*  276:     */   
/*  277:     */   public double getDouble(int index)
/*  278:     */   {
/*  279: 298 */     checkIndex(index, 8);
/*  280: 299 */     return this.buffer.getDouble(index);
/*  281:     */   }
/*  282:     */   
/*  283:     */   public int hashCode()
/*  284:     */   {
/*  285: 304 */     reject();
/*  286: 305 */     return 0;
/*  287:     */   }
/*  288:     */   
/*  289:     */   public int indexOf(int fromIndex, int toIndex, byte value)
/*  290:     */   {
/*  291: 310 */     if (fromIndex == toIndex) {
/*  292: 311 */       return -1;
/*  293:     */     }
/*  294: 314 */     if (Math.max(fromIndex, toIndex) > this.buffer.writerIndex()) {
/*  295: 315 */       throw REPLAY;
/*  296:     */     }
/*  297: 318 */     return this.buffer.indexOf(fromIndex, toIndex, value);
/*  298:     */   }
/*  299:     */   
/*  300:     */   public int bytesBefore(byte value)
/*  301:     */   {
/*  302: 323 */     int bytes = this.buffer.bytesBefore(value);
/*  303: 324 */     if (bytes < 0) {
/*  304: 325 */       throw REPLAY;
/*  305:     */     }
/*  306: 327 */     return bytes;
/*  307:     */   }
/*  308:     */   
/*  309:     */   public int bytesBefore(int length, byte value)
/*  310:     */   {
/*  311: 332 */     int readerIndex = this.buffer.readerIndex();
/*  312: 333 */     return bytesBefore(readerIndex, this.buffer.writerIndex() - readerIndex, value);
/*  313:     */   }
/*  314:     */   
/*  315:     */   public int bytesBefore(int index, int length, byte value)
/*  316:     */   {
/*  317: 338 */     int writerIndex = this.buffer.writerIndex();
/*  318: 339 */     if (index >= writerIndex) {
/*  319: 340 */       throw REPLAY;
/*  320:     */     }
/*  321: 343 */     if (index <= writerIndex - length) {
/*  322: 344 */       return this.buffer.bytesBefore(index, length, value);
/*  323:     */     }
/*  324: 347 */     int res = this.buffer.bytesBefore(index, writerIndex - index, value);
/*  325: 348 */     if (res < 0) {
/*  326: 349 */       throw REPLAY;
/*  327:     */     }
/*  328: 351 */     return res;
/*  329:     */   }
/*  330:     */   
/*  331:     */   public int forEachByte(ByteBufProcessor processor)
/*  332:     */   {
/*  333: 357 */     int ret = this.buffer.forEachByte(processor);
/*  334: 358 */     if (ret < 0) {
/*  335: 359 */       throw REPLAY;
/*  336:     */     }
/*  337: 361 */     return ret;
/*  338:     */   }
/*  339:     */   
/*  340:     */   public int forEachByte(int index, int length, ByteBufProcessor processor)
/*  341:     */   {
/*  342: 367 */     int writerIndex = this.buffer.writerIndex();
/*  343: 368 */     if (index >= writerIndex) {
/*  344: 369 */       throw REPLAY;
/*  345:     */     }
/*  346: 372 */     if (index <= writerIndex - length) {
/*  347: 373 */       return this.buffer.forEachByte(index, length, processor);
/*  348:     */     }
/*  349: 376 */     int ret = this.buffer.forEachByte(index, writerIndex - index, processor);
/*  350: 377 */     if (ret < 0) {
/*  351: 378 */       throw REPLAY;
/*  352:     */     }
/*  353: 380 */     return ret;
/*  354:     */   }
/*  355:     */   
/*  356:     */   public int forEachByteDesc(ByteBufProcessor processor)
/*  357:     */   {
/*  358: 386 */     if (this.terminated) {
/*  359: 387 */       return this.buffer.forEachByteDesc(processor);
/*  360:     */     }
/*  361: 389 */     reject();
/*  362: 390 */     return 0;
/*  363:     */   }
/*  364:     */   
/*  365:     */   public int forEachByteDesc(int index, int length, ByteBufProcessor processor)
/*  366:     */   {
/*  367: 396 */     if (index + length > this.buffer.writerIndex()) {
/*  368: 397 */       throw REPLAY;
/*  369:     */     }
/*  370: 400 */     return this.buffer.forEachByteDesc(index, length, processor);
/*  371:     */   }
/*  372:     */   
/*  373:     */   public ByteBuf markReaderIndex()
/*  374:     */   {
/*  375: 405 */     this.buffer.markReaderIndex();
/*  376: 406 */     return this;
/*  377:     */   }
/*  378:     */   
/*  379:     */   public ByteBuf markWriterIndex()
/*  380:     */   {
/*  381: 411 */     reject();
/*  382: 412 */     return this;
/*  383:     */   }
/*  384:     */   
/*  385:     */   public ByteOrder order()
/*  386:     */   {
/*  387: 417 */     return this.buffer.order();
/*  388:     */   }
/*  389:     */   
/*  390:     */   public ByteBuf order(ByteOrder endianness)
/*  391:     */   {
/*  392: 422 */     if (endianness == null) {
/*  393: 423 */       throw new NullPointerException("endianness");
/*  394:     */     }
/*  395: 425 */     if (endianness == order()) {
/*  396: 426 */       return this;
/*  397:     */     }
/*  398: 429 */     SwappedByteBuf swapped = this.swapped;
/*  399: 430 */     if (swapped == null) {
/*  400: 431 */       this.swapped = (swapped = new SwappedByteBuf(this));
/*  401:     */     }
/*  402: 433 */     return swapped;
/*  403:     */   }
/*  404:     */   
/*  405:     */   public boolean isReadable()
/*  406:     */   {
/*  407: 438 */     return this.terminated ? this.buffer.isReadable() : true;
/*  408:     */   }
/*  409:     */   
/*  410:     */   public boolean isReadable(int size)
/*  411:     */   {
/*  412: 443 */     return this.terminated ? this.buffer.isReadable(size) : true;
/*  413:     */   }
/*  414:     */   
/*  415:     */   public int readableBytes()
/*  416:     */   {
/*  417: 448 */     if (this.terminated) {
/*  418: 449 */       return this.buffer.readableBytes();
/*  419:     */     }
/*  420: 451 */     return 2147483647 - this.buffer.readerIndex();
/*  421:     */   }
/*  422:     */   
/*  423:     */   public boolean readBoolean()
/*  424:     */   {
/*  425: 457 */     checkReadableBytes(1);
/*  426: 458 */     return this.buffer.readBoolean();
/*  427:     */   }
/*  428:     */   
/*  429:     */   public byte readByte()
/*  430:     */   {
/*  431: 463 */     checkReadableBytes(1);
/*  432: 464 */     return this.buffer.readByte();
/*  433:     */   }
/*  434:     */   
/*  435:     */   public short readUnsignedByte()
/*  436:     */   {
/*  437: 469 */     checkReadableBytes(1);
/*  438: 470 */     return this.buffer.readUnsignedByte();
/*  439:     */   }
/*  440:     */   
/*  441:     */   public ByteBuf readBytes(byte[] dst, int dstIndex, int length)
/*  442:     */   {
/*  443: 475 */     checkReadableBytes(length);
/*  444: 476 */     this.buffer.readBytes(dst, dstIndex, length);
/*  445: 477 */     return this;
/*  446:     */   }
/*  447:     */   
/*  448:     */   public ByteBuf readBytes(byte[] dst)
/*  449:     */   {
/*  450: 482 */     checkReadableBytes(dst.length);
/*  451: 483 */     this.buffer.readBytes(dst);
/*  452: 484 */     return this;
/*  453:     */   }
/*  454:     */   
/*  455:     */   public ByteBuf readBytes(ByteBuffer dst)
/*  456:     */   {
/*  457: 489 */     reject();
/*  458: 490 */     return this;
/*  459:     */   }
/*  460:     */   
/*  461:     */   public ByteBuf readBytes(ByteBuf dst, int dstIndex, int length)
/*  462:     */   {
/*  463: 495 */     checkReadableBytes(length);
/*  464: 496 */     this.buffer.readBytes(dst, dstIndex, length);
/*  465: 497 */     return this;
/*  466:     */   }
/*  467:     */   
/*  468:     */   public ByteBuf readBytes(ByteBuf dst, int length)
/*  469:     */   {
/*  470: 502 */     reject();
/*  471: 503 */     return this;
/*  472:     */   }
/*  473:     */   
/*  474:     */   public ByteBuf readBytes(ByteBuf dst)
/*  475:     */   {
/*  476: 508 */     checkReadableBytes(dst.writableBytes());
/*  477: 509 */     this.buffer.readBytes(dst);
/*  478: 510 */     return this;
/*  479:     */   }
/*  480:     */   
/*  481:     */   public int readBytes(GatheringByteChannel out, int length)
/*  482:     */   {
/*  483: 515 */     reject();
/*  484: 516 */     return 0;
/*  485:     */   }
/*  486:     */   
/*  487:     */   public ByteBuf readBytes(int length)
/*  488:     */   {
/*  489: 521 */     checkReadableBytes(length);
/*  490: 522 */     return this.buffer.readBytes(length);
/*  491:     */   }
/*  492:     */   
/*  493:     */   public ByteBuf readSlice(int length)
/*  494:     */   {
/*  495: 527 */     checkReadableBytes(length);
/*  496: 528 */     return this.buffer.readSlice(length);
/*  497:     */   }
/*  498:     */   
/*  499:     */   public ByteBuf readBytes(OutputStream out, int length)
/*  500:     */   {
/*  501: 533 */     reject();
/*  502: 534 */     return this;
/*  503:     */   }
/*  504:     */   
/*  505:     */   public int readerIndex()
/*  506:     */   {
/*  507: 539 */     return this.buffer.readerIndex();
/*  508:     */   }
/*  509:     */   
/*  510:     */   public ByteBuf readerIndex(int readerIndex)
/*  511:     */   {
/*  512: 544 */     this.buffer.readerIndex(readerIndex);
/*  513: 545 */     return this;
/*  514:     */   }
/*  515:     */   
/*  516:     */   public int readInt()
/*  517:     */   {
/*  518: 550 */     checkReadableBytes(4);
/*  519: 551 */     return this.buffer.readInt();
/*  520:     */   }
/*  521:     */   
/*  522:     */   public long readUnsignedInt()
/*  523:     */   {
/*  524: 556 */     checkReadableBytes(4);
/*  525: 557 */     return this.buffer.readUnsignedInt();
/*  526:     */   }
/*  527:     */   
/*  528:     */   public long readLong()
/*  529:     */   {
/*  530: 562 */     checkReadableBytes(8);
/*  531: 563 */     return this.buffer.readLong();
/*  532:     */   }
/*  533:     */   
/*  534:     */   public int readMedium()
/*  535:     */   {
/*  536: 568 */     checkReadableBytes(3);
/*  537: 569 */     return this.buffer.readMedium();
/*  538:     */   }
/*  539:     */   
/*  540:     */   public int readUnsignedMedium()
/*  541:     */   {
/*  542: 574 */     checkReadableBytes(3);
/*  543: 575 */     return this.buffer.readUnsignedMedium();
/*  544:     */   }
/*  545:     */   
/*  546:     */   public short readShort()
/*  547:     */   {
/*  548: 580 */     checkReadableBytes(2);
/*  549: 581 */     return this.buffer.readShort();
/*  550:     */   }
/*  551:     */   
/*  552:     */   public int readUnsignedShort()
/*  553:     */   {
/*  554: 586 */     checkReadableBytes(2);
/*  555: 587 */     return this.buffer.readUnsignedShort();
/*  556:     */   }
/*  557:     */   
/*  558:     */   public char readChar()
/*  559:     */   {
/*  560: 592 */     checkReadableBytes(2);
/*  561: 593 */     return this.buffer.readChar();
/*  562:     */   }
/*  563:     */   
/*  564:     */   public float readFloat()
/*  565:     */   {
/*  566: 598 */     checkReadableBytes(4);
/*  567: 599 */     return this.buffer.readFloat();
/*  568:     */   }
/*  569:     */   
/*  570:     */   public double readDouble()
/*  571:     */   {
/*  572: 604 */     checkReadableBytes(8);
/*  573: 605 */     return this.buffer.readDouble();
/*  574:     */   }
/*  575:     */   
/*  576:     */   public ByteBuf resetReaderIndex()
/*  577:     */   {
/*  578: 610 */     this.buffer.resetReaderIndex();
/*  579: 611 */     return this;
/*  580:     */   }
/*  581:     */   
/*  582:     */   public ByteBuf resetWriterIndex()
/*  583:     */   {
/*  584: 616 */     reject();
/*  585: 617 */     return this;
/*  586:     */   }
/*  587:     */   
/*  588:     */   public ByteBuf setBoolean(int index, boolean value)
/*  589:     */   {
/*  590: 622 */     reject();
/*  591: 623 */     return this;
/*  592:     */   }
/*  593:     */   
/*  594:     */   public ByteBuf setByte(int index, int value)
/*  595:     */   {
/*  596: 628 */     reject();
/*  597: 629 */     return this;
/*  598:     */   }
/*  599:     */   
/*  600:     */   public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/*  601:     */   {
/*  602: 634 */     reject();
/*  603: 635 */     return this;
/*  604:     */   }
/*  605:     */   
/*  606:     */   public ByteBuf setBytes(int index, byte[] src)
/*  607:     */   {
/*  608: 640 */     reject();
/*  609: 641 */     return this;
/*  610:     */   }
/*  611:     */   
/*  612:     */   public ByteBuf setBytes(int index, ByteBuffer src)
/*  613:     */   {
/*  614: 646 */     reject();
/*  615: 647 */     return this;
/*  616:     */   }
/*  617:     */   
/*  618:     */   public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/*  619:     */   {
/*  620: 652 */     reject();
/*  621: 653 */     return this;
/*  622:     */   }
/*  623:     */   
/*  624:     */   public ByteBuf setBytes(int index, ByteBuf src, int length)
/*  625:     */   {
/*  626: 658 */     reject();
/*  627: 659 */     return this;
/*  628:     */   }
/*  629:     */   
/*  630:     */   public ByteBuf setBytes(int index, ByteBuf src)
/*  631:     */   {
/*  632: 664 */     reject();
/*  633: 665 */     return this;
/*  634:     */   }
/*  635:     */   
/*  636:     */   public int setBytes(int index, InputStream in, int length)
/*  637:     */   {
/*  638: 670 */     reject();
/*  639: 671 */     return 0;
/*  640:     */   }
/*  641:     */   
/*  642:     */   public ByteBuf setZero(int index, int length)
/*  643:     */   {
/*  644: 676 */     reject();
/*  645: 677 */     return this;
/*  646:     */   }
/*  647:     */   
/*  648:     */   public int setBytes(int index, ScatteringByteChannel in, int length)
/*  649:     */   {
/*  650: 682 */     reject();
/*  651: 683 */     return 0;
/*  652:     */   }
/*  653:     */   
/*  654:     */   public ByteBuf setIndex(int readerIndex, int writerIndex)
/*  655:     */   {
/*  656: 688 */     reject();
/*  657: 689 */     return this;
/*  658:     */   }
/*  659:     */   
/*  660:     */   public ByteBuf setInt(int index, int value)
/*  661:     */   {
/*  662: 694 */     reject();
/*  663: 695 */     return this;
/*  664:     */   }
/*  665:     */   
/*  666:     */   public ByteBuf setLong(int index, long value)
/*  667:     */   {
/*  668: 700 */     reject();
/*  669: 701 */     return this;
/*  670:     */   }
/*  671:     */   
/*  672:     */   public ByteBuf setMedium(int index, int value)
/*  673:     */   {
/*  674: 706 */     reject();
/*  675: 707 */     return this;
/*  676:     */   }
/*  677:     */   
/*  678:     */   public ByteBuf setShort(int index, int value)
/*  679:     */   {
/*  680: 712 */     reject();
/*  681: 713 */     return this;
/*  682:     */   }
/*  683:     */   
/*  684:     */   public ByteBuf setChar(int index, int value)
/*  685:     */   {
/*  686: 718 */     reject();
/*  687: 719 */     return this;
/*  688:     */   }
/*  689:     */   
/*  690:     */   public ByteBuf setFloat(int index, float value)
/*  691:     */   {
/*  692: 724 */     reject();
/*  693: 725 */     return this;
/*  694:     */   }
/*  695:     */   
/*  696:     */   public ByteBuf setDouble(int index, double value)
/*  697:     */   {
/*  698: 730 */     reject();
/*  699: 731 */     return this;
/*  700:     */   }
/*  701:     */   
/*  702:     */   public ByteBuf skipBytes(int length)
/*  703:     */   {
/*  704: 736 */     checkReadableBytes(length);
/*  705: 737 */     this.buffer.skipBytes(length);
/*  706: 738 */     return this;
/*  707:     */   }
/*  708:     */   
/*  709:     */   public ByteBuf slice()
/*  710:     */   {
/*  711: 743 */     reject();
/*  712: 744 */     return this;
/*  713:     */   }
/*  714:     */   
/*  715:     */   public ByteBuf slice(int index, int length)
/*  716:     */   {
/*  717: 749 */     checkIndex(index, length);
/*  718: 750 */     return this.buffer.slice(index, length);
/*  719:     */   }
/*  720:     */   
/*  721:     */   public int nioBufferCount()
/*  722:     */   {
/*  723: 755 */     return this.buffer.nioBufferCount();
/*  724:     */   }
/*  725:     */   
/*  726:     */   public ByteBuffer nioBuffer()
/*  727:     */   {
/*  728: 760 */     reject();
/*  729: 761 */     return null;
/*  730:     */   }
/*  731:     */   
/*  732:     */   public ByteBuffer nioBuffer(int index, int length)
/*  733:     */   {
/*  734: 766 */     checkIndex(index, length);
/*  735: 767 */     return this.buffer.nioBuffer(index, length);
/*  736:     */   }
/*  737:     */   
/*  738:     */   public ByteBuffer[] nioBuffers()
/*  739:     */   {
/*  740: 772 */     reject();
/*  741: 773 */     return null;
/*  742:     */   }
/*  743:     */   
/*  744:     */   public ByteBuffer[] nioBuffers(int index, int length)
/*  745:     */   {
/*  746: 778 */     checkIndex(index, length);
/*  747: 779 */     return this.buffer.nioBuffers(index, length);
/*  748:     */   }
/*  749:     */   
/*  750:     */   public ByteBuffer internalNioBuffer(int index, int length)
/*  751:     */   {
/*  752: 784 */     checkIndex(index, length);
/*  753: 785 */     return this.buffer.internalNioBuffer(index, length);
/*  754:     */   }
/*  755:     */   
/*  756:     */   public String toString(int index, int length, Charset charset)
/*  757:     */   {
/*  758: 790 */     checkIndex(index, length);
/*  759: 791 */     return this.buffer.toString(index, length, charset);
/*  760:     */   }
/*  761:     */   
/*  762:     */   public String toString(Charset charsetName)
/*  763:     */   {
/*  764: 796 */     reject();
/*  765: 797 */     return null;
/*  766:     */   }
/*  767:     */   
/*  768:     */   public String toString()
/*  769:     */   {
/*  770: 802 */     return StringUtil.simpleClassName(this) + '(' + "ridx=" + readerIndex() + ", " + "widx=" + writerIndex() + ')';
/*  771:     */   }
/*  772:     */   
/*  773:     */   public boolean isWritable()
/*  774:     */   {
/*  775: 813 */     return false;
/*  776:     */   }
/*  777:     */   
/*  778:     */   public boolean isWritable(int size)
/*  779:     */   {
/*  780: 818 */     return false;
/*  781:     */   }
/*  782:     */   
/*  783:     */   public int writableBytes()
/*  784:     */   {
/*  785: 823 */     return 0;
/*  786:     */   }
/*  787:     */   
/*  788:     */   public int maxWritableBytes()
/*  789:     */   {
/*  790: 828 */     return 0;
/*  791:     */   }
/*  792:     */   
/*  793:     */   public ByteBuf writeBoolean(boolean value)
/*  794:     */   {
/*  795: 833 */     reject();
/*  796: 834 */     return this;
/*  797:     */   }
/*  798:     */   
/*  799:     */   public ByteBuf writeByte(int value)
/*  800:     */   {
/*  801: 839 */     reject();
/*  802: 840 */     return this;
/*  803:     */   }
/*  804:     */   
/*  805:     */   public ByteBuf writeBytes(byte[] src, int srcIndex, int length)
/*  806:     */   {
/*  807: 845 */     reject();
/*  808: 846 */     return this;
/*  809:     */   }
/*  810:     */   
/*  811:     */   public ByteBuf writeBytes(byte[] src)
/*  812:     */   {
/*  813: 851 */     reject();
/*  814: 852 */     return this;
/*  815:     */   }
/*  816:     */   
/*  817:     */   public ByteBuf writeBytes(ByteBuffer src)
/*  818:     */   {
/*  819: 857 */     reject();
/*  820: 858 */     return this;
/*  821:     */   }
/*  822:     */   
/*  823:     */   public ByteBuf writeBytes(ByteBuf src, int srcIndex, int length)
/*  824:     */   {
/*  825: 863 */     reject();
/*  826: 864 */     return this;
/*  827:     */   }
/*  828:     */   
/*  829:     */   public ByteBuf writeBytes(ByteBuf src, int length)
/*  830:     */   {
/*  831: 869 */     reject();
/*  832: 870 */     return this;
/*  833:     */   }
/*  834:     */   
/*  835:     */   public ByteBuf writeBytes(ByteBuf src)
/*  836:     */   {
/*  837: 875 */     reject();
/*  838: 876 */     return this;
/*  839:     */   }
/*  840:     */   
/*  841:     */   public int writeBytes(InputStream in, int length)
/*  842:     */   {
/*  843: 881 */     reject();
/*  844: 882 */     return 0;
/*  845:     */   }
/*  846:     */   
/*  847:     */   public int writeBytes(ScatteringByteChannel in, int length)
/*  848:     */   {
/*  849: 887 */     reject();
/*  850: 888 */     return 0;
/*  851:     */   }
/*  852:     */   
/*  853:     */   public ByteBuf writeInt(int value)
/*  854:     */   {
/*  855: 893 */     reject();
/*  856: 894 */     return this;
/*  857:     */   }
/*  858:     */   
/*  859:     */   public ByteBuf writeLong(long value)
/*  860:     */   {
/*  861: 899 */     reject();
/*  862: 900 */     return this;
/*  863:     */   }
/*  864:     */   
/*  865:     */   public ByteBuf writeMedium(int value)
/*  866:     */   {
/*  867: 905 */     reject();
/*  868: 906 */     return this;
/*  869:     */   }
/*  870:     */   
/*  871:     */   public ByteBuf writeZero(int length)
/*  872:     */   {
/*  873: 911 */     reject();
/*  874: 912 */     return this;
/*  875:     */   }
/*  876:     */   
/*  877:     */   public int writerIndex()
/*  878:     */   {
/*  879: 917 */     return this.buffer.writerIndex();
/*  880:     */   }
/*  881:     */   
/*  882:     */   public ByteBuf writerIndex(int writerIndex)
/*  883:     */   {
/*  884: 922 */     reject();
/*  885: 923 */     return this;
/*  886:     */   }
/*  887:     */   
/*  888:     */   public ByteBuf writeShort(int value)
/*  889:     */   {
/*  890: 928 */     reject();
/*  891: 929 */     return this;
/*  892:     */   }
/*  893:     */   
/*  894:     */   public ByteBuf writeChar(int value)
/*  895:     */   {
/*  896: 934 */     reject();
/*  897: 935 */     return this;
/*  898:     */   }
/*  899:     */   
/*  900:     */   public ByteBuf writeFloat(float value)
/*  901:     */   {
/*  902: 940 */     reject();
/*  903: 941 */     return this;
/*  904:     */   }
/*  905:     */   
/*  906:     */   public ByteBuf writeDouble(double value)
/*  907:     */   {
/*  908: 946 */     reject();
/*  909: 947 */     return this;
/*  910:     */   }
/*  911:     */   
/*  912:     */   private void checkIndex(int index, int length)
/*  913:     */   {
/*  914: 951 */     if (index + length > this.buffer.writerIndex()) {
/*  915: 952 */       throw REPLAY;
/*  916:     */     }
/*  917:     */   }
/*  918:     */   
/*  919:     */   private void checkReadableBytes(int readableBytes)
/*  920:     */   {
/*  921: 957 */     if (this.buffer.readableBytes() < readableBytes) {
/*  922: 958 */       throw REPLAY;
/*  923:     */     }
/*  924:     */   }
/*  925:     */   
/*  926:     */   public ByteBuf discardSomeReadBytes()
/*  927:     */   {
/*  928: 964 */     reject();
/*  929: 965 */     return this;
/*  930:     */   }
/*  931:     */   
/*  932:     */   public int refCnt()
/*  933:     */   {
/*  934: 970 */     return this.buffer.refCnt();
/*  935:     */   }
/*  936:     */   
/*  937:     */   public ByteBuf retain()
/*  938:     */   {
/*  939: 975 */     reject();
/*  940: 976 */     return this;
/*  941:     */   }
/*  942:     */   
/*  943:     */   public ByteBuf retain(int increment)
/*  944:     */   {
/*  945: 981 */     reject();
/*  946: 982 */     return this;
/*  947:     */   }
/*  948:     */   
/*  949:     */   public boolean release()
/*  950:     */   {
/*  951: 987 */     reject();
/*  952: 988 */     return false;
/*  953:     */   }
/*  954:     */   
/*  955:     */   public boolean release(int decrement)
/*  956:     */   {
/*  957: 993 */     reject();
/*  958: 994 */     return false;
/*  959:     */   }
/*  960:     */   
/*  961:     */   public ByteBuf unwrap()
/*  962:     */   {
/*  963: 999 */     reject();
/*  964:1000 */     return this;
/*  965:     */   }
/*  966:     */   
/*  967:     */   private static void reject()
/*  968:     */   {
/*  969:1004 */     throw new UnsupportedOperationException("not a replayable operation");
/*  970:     */   }
/*  971:     */   
/*  972:     */   ReplayingDecoderBuffer() {}
/*  973:     */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.ReplayingDecoderBuffer
 * JD-Core Version:    0.7.0.1
 */