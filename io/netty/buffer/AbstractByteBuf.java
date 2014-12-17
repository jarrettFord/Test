/*    1:     */ package io.netty.buffer;
/*    2:     */ 
/*    3:     */ import io.netty.util.IllegalReferenceCountException;
/*    4:     */ import io.netty.util.ResourceLeakDetector;
/*    5:     */ import io.netty.util.internal.PlatformDependent;
/*    6:     */ import io.netty.util.internal.StringUtil;
/*    7:     */ import java.io.IOException;
/*    8:     */ import java.io.InputStream;
/*    9:     */ import java.io.OutputStream;
/*   10:     */ import java.nio.ByteBuffer;
/*   11:     */ import java.nio.ByteOrder;
/*   12:     */ import java.nio.channels.GatheringByteChannel;
/*   13:     */ import java.nio.channels.ScatteringByteChannel;
/*   14:     */ import java.nio.charset.Charset;
/*   15:     */ 
/*   16:     */ public abstract class AbstractByteBuf
/*   17:     */   extends ByteBuf
/*   18:     */ {
/*   19:  38 */   static final ResourceLeakDetector<ByteBuf> leakDetector = new ResourceLeakDetector(ByteBuf.class);
/*   20:     */   int readerIndex;
/*   21:     */   int writerIndex;
/*   22:     */   private int markedReaderIndex;
/*   23:     */   private int markedWriterIndex;
/*   24:     */   private int maxCapacity;
/*   25:     */   private SwappedByteBuf swappedBuf;
/*   26:     */   
/*   27:     */   protected AbstractByteBuf(int maxCapacity)
/*   28:     */   {
/*   29:  50 */     if (maxCapacity < 0) {
/*   30:  51 */       throw new IllegalArgumentException("maxCapacity: " + maxCapacity + " (expected: >= 0)");
/*   31:     */     }
/*   32:  53 */     this.maxCapacity = maxCapacity;
/*   33:     */   }
/*   34:     */   
/*   35:     */   public int maxCapacity()
/*   36:     */   {
/*   37:  58 */     return this.maxCapacity;
/*   38:     */   }
/*   39:     */   
/*   40:     */   protected final void maxCapacity(int maxCapacity)
/*   41:     */   {
/*   42:  62 */     this.maxCapacity = maxCapacity;
/*   43:     */   }
/*   44:     */   
/*   45:     */   public int readerIndex()
/*   46:     */   {
/*   47:  67 */     return this.readerIndex;
/*   48:     */   }
/*   49:     */   
/*   50:     */   public ByteBuf readerIndex(int readerIndex)
/*   51:     */   {
/*   52:  72 */     if ((readerIndex < 0) || (readerIndex > this.writerIndex)) {
/*   53:  73 */       throw new IndexOutOfBoundsException(String.format("readerIndex: %d (expected: 0 <= readerIndex <= writerIndex(%d))", new Object[] { Integer.valueOf(readerIndex), Integer.valueOf(this.writerIndex) }));
/*   54:     */     }
/*   55:  76 */     this.readerIndex = readerIndex;
/*   56:  77 */     return this;
/*   57:     */   }
/*   58:     */   
/*   59:     */   public int writerIndex()
/*   60:     */   {
/*   61:  82 */     return this.writerIndex;
/*   62:     */   }
/*   63:     */   
/*   64:     */   public ByteBuf writerIndex(int writerIndex)
/*   65:     */   {
/*   66:  87 */     if ((writerIndex < this.readerIndex) || (writerIndex > capacity())) {
/*   67:  88 */       throw new IndexOutOfBoundsException(String.format("writerIndex: %d (expected: readerIndex(%d) <= writerIndex <= capacity(%d))", new Object[] { Integer.valueOf(writerIndex), Integer.valueOf(this.readerIndex), Integer.valueOf(capacity()) }));
/*   68:     */     }
/*   69:  92 */     this.writerIndex = writerIndex;
/*   70:  93 */     return this;
/*   71:     */   }
/*   72:     */   
/*   73:     */   public ByteBuf setIndex(int readerIndex, int writerIndex)
/*   74:     */   {
/*   75:  98 */     if ((readerIndex < 0) || (readerIndex > writerIndex) || (writerIndex > capacity())) {
/*   76:  99 */       throw new IndexOutOfBoundsException(String.format("readerIndex: %d, writerIndex: %d (expected: 0 <= readerIndex <= writerIndex <= capacity(%d))", new Object[] { Integer.valueOf(readerIndex), Integer.valueOf(writerIndex), Integer.valueOf(capacity()) }));
/*   77:     */     }
/*   78: 103 */     this.readerIndex = readerIndex;
/*   79: 104 */     this.writerIndex = writerIndex;
/*   80: 105 */     return this;
/*   81:     */   }
/*   82:     */   
/*   83:     */   public ByteBuf clear()
/*   84:     */   {
/*   85: 110 */     this.readerIndex = (this.writerIndex = 0);
/*   86: 111 */     return this;
/*   87:     */   }
/*   88:     */   
/*   89:     */   public boolean isReadable()
/*   90:     */   {
/*   91: 116 */     return this.writerIndex > this.readerIndex;
/*   92:     */   }
/*   93:     */   
/*   94:     */   public boolean isReadable(int numBytes)
/*   95:     */   {
/*   96: 121 */     return this.writerIndex - this.readerIndex >= numBytes;
/*   97:     */   }
/*   98:     */   
/*   99:     */   public boolean isWritable()
/*  100:     */   {
/*  101: 126 */     return capacity() > this.writerIndex;
/*  102:     */   }
/*  103:     */   
/*  104:     */   public boolean isWritable(int numBytes)
/*  105:     */   {
/*  106: 131 */     return capacity() - this.writerIndex >= numBytes;
/*  107:     */   }
/*  108:     */   
/*  109:     */   public int readableBytes()
/*  110:     */   {
/*  111: 136 */     return this.writerIndex - this.readerIndex;
/*  112:     */   }
/*  113:     */   
/*  114:     */   public int writableBytes()
/*  115:     */   {
/*  116: 141 */     return capacity() - this.writerIndex;
/*  117:     */   }
/*  118:     */   
/*  119:     */   public int maxWritableBytes()
/*  120:     */   {
/*  121: 146 */     return maxCapacity() - this.writerIndex;
/*  122:     */   }
/*  123:     */   
/*  124:     */   public ByteBuf markReaderIndex()
/*  125:     */   {
/*  126: 151 */     this.markedReaderIndex = this.readerIndex;
/*  127: 152 */     return this;
/*  128:     */   }
/*  129:     */   
/*  130:     */   public ByteBuf resetReaderIndex()
/*  131:     */   {
/*  132: 157 */     readerIndex(this.markedReaderIndex);
/*  133: 158 */     return this;
/*  134:     */   }
/*  135:     */   
/*  136:     */   public ByteBuf markWriterIndex()
/*  137:     */   {
/*  138: 163 */     this.markedWriterIndex = this.writerIndex;
/*  139: 164 */     return this;
/*  140:     */   }
/*  141:     */   
/*  142:     */   public ByteBuf resetWriterIndex()
/*  143:     */   {
/*  144: 169 */     this.writerIndex = this.markedWriterIndex;
/*  145: 170 */     return this;
/*  146:     */   }
/*  147:     */   
/*  148:     */   public ByteBuf discardReadBytes()
/*  149:     */   {
/*  150: 175 */     ensureAccessible();
/*  151: 176 */     if (this.readerIndex == 0) {
/*  152: 177 */       return this;
/*  153:     */     }
/*  154: 180 */     if (this.readerIndex != this.writerIndex)
/*  155:     */     {
/*  156: 181 */       setBytes(0, this, this.readerIndex, this.writerIndex - this.readerIndex);
/*  157: 182 */       this.writerIndex -= this.readerIndex;
/*  158: 183 */       adjustMarkers(this.readerIndex);
/*  159: 184 */       this.readerIndex = 0;
/*  160:     */     }
/*  161:     */     else
/*  162:     */     {
/*  163: 186 */       adjustMarkers(this.readerIndex);
/*  164: 187 */       this.writerIndex = (this.readerIndex = 0);
/*  165:     */     }
/*  166: 189 */     return this;
/*  167:     */   }
/*  168:     */   
/*  169:     */   public ByteBuf discardSomeReadBytes()
/*  170:     */   {
/*  171: 194 */     ensureAccessible();
/*  172: 195 */     if (this.readerIndex == 0) {
/*  173: 196 */       return this;
/*  174:     */     }
/*  175: 199 */     if (this.readerIndex == this.writerIndex)
/*  176:     */     {
/*  177: 200 */       adjustMarkers(this.readerIndex);
/*  178: 201 */       this.writerIndex = (this.readerIndex = 0);
/*  179: 202 */       return this;
/*  180:     */     }
/*  181: 205 */     if (this.readerIndex >= capacity() >>> 1)
/*  182:     */     {
/*  183: 206 */       setBytes(0, this, this.readerIndex, this.writerIndex - this.readerIndex);
/*  184: 207 */       this.writerIndex -= this.readerIndex;
/*  185: 208 */       adjustMarkers(this.readerIndex);
/*  186: 209 */       this.readerIndex = 0;
/*  187:     */     }
/*  188: 211 */     return this;
/*  189:     */   }
/*  190:     */   
/*  191:     */   protected final void adjustMarkers(int decrement)
/*  192:     */   {
/*  193: 215 */     int markedReaderIndex = this.markedReaderIndex;
/*  194: 216 */     if (markedReaderIndex <= decrement)
/*  195:     */     {
/*  196: 217 */       this.markedReaderIndex = 0;
/*  197: 218 */       int markedWriterIndex = this.markedWriterIndex;
/*  198: 219 */       if (markedWriterIndex <= decrement) {
/*  199: 220 */         this.markedWriterIndex = 0;
/*  200:     */       } else {
/*  201: 222 */         this.markedWriterIndex = (markedWriterIndex - decrement);
/*  202:     */       }
/*  203:     */     }
/*  204:     */     else
/*  205:     */     {
/*  206: 225 */       this.markedReaderIndex = (markedReaderIndex - decrement);
/*  207: 226 */       this.markedWriterIndex -= decrement;
/*  208:     */     }
/*  209:     */   }
/*  210:     */   
/*  211:     */   public ByteBuf ensureWritable(int minWritableBytes)
/*  212:     */   {
/*  213: 232 */     ensureAccessible();
/*  214: 233 */     if (minWritableBytes < 0) {
/*  215: 234 */       throw new IllegalArgumentException(String.format("minWritableBytes: %d (expected: >= 0)", new Object[] { Integer.valueOf(minWritableBytes) }));
/*  216:     */     }
/*  217: 238 */     if (minWritableBytes <= writableBytes()) {
/*  218: 239 */       return this;
/*  219:     */     }
/*  220: 242 */     if (minWritableBytes > this.maxCapacity - this.writerIndex) {
/*  221: 243 */       throw new IndexOutOfBoundsException(String.format("writerIndex(%d) + minWritableBytes(%d) exceeds maxCapacity(%d): %s", new Object[] { Integer.valueOf(this.writerIndex), Integer.valueOf(minWritableBytes), Integer.valueOf(this.maxCapacity), this }));
/*  222:     */     }
/*  223: 249 */     int newCapacity = calculateNewCapacity(this.writerIndex + minWritableBytes);
/*  224:     */     
/*  225:     */ 
/*  226: 252 */     capacity(newCapacity);
/*  227: 253 */     return this;
/*  228:     */   }
/*  229:     */   
/*  230:     */   public int ensureWritable(int minWritableBytes, boolean force)
/*  231:     */   {
/*  232: 258 */     if (minWritableBytes < 0) {
/*  233: 259 */       throw new IllegalArgumentException(String.format("minWritableBytes: %d (expected: >= 0)", new Object[] { Integer.valueOf(minWritableBytes) }));
/*  234:     */     }
/*  235: 263 */     if (minWritableBytes <= writableBytes()) {
/*  236: 264 */       return 0;
/*  237:     */     }
/*  238: 267 */     if ((minWritableBytes > this.maxCapacity - this.writerIndex) && 
/*  239: 268 */       (force))
/*  240:     */     {
/*  241: 269 */       if (capacity() == maxCapacity()) {
/*  242: 270 */         return 1;
/*  243:     */       }
/*  244: 273 */       capacity(maxCapacity());
/*  245: 274 */       return 3;
/*  246:     */     }
/*  247: 279 */     int newCapacity = calculateNewCapacity(this.writerIndex + minWritableBytes);
/*  248:     */     
/*  249:     */ 
/*  250: 282 */     capacity(newCapacity);
/*  251: 283 */     return 2;
/*  252:     */   }
/*  253:     */   
/*  254:     */   private int calculateNewCapacity(int minNewCapacity)
/*  255:     */   {
/*  256: 287 */     int maxCapacity = this.maxCapacity;
/*  257: 288 */     int threshold = 4194304;
/*  258: 290 */     if (minNewCapacity == 4194304) {
/*  259: 291 */       return 4194304;
/*  260:     */     }
/*  261: 295 */     if (minNewCapacity > 4194304)
/*  262:     */     {
/*  263: 296 */       int newCapacity = minNewCapacity / 4194304 * 4194304;
/*  264: 297 */       if (newCapacity > maxCapacity - 4194304) {
/*  265: 298 */         newCapacity = maxCapacity;
/*  266:     */       } else {
/*  267: 300 */         newCapacity += 4194304;
/*  268:     */       }
/*  269: 302 */       return newCapacity;
/*  270:     */     }
/*  271: 306 */     int newCapacity = 64;
/*  272: 307 */     while (newCapacity < minNewCapacity) {
/*  273: 308 */       newCapacity <<= 1;
/*  274:     */     }
/*  275: 311 */     return Math.min(newCapacity, maxCapacity);
/*  276:     */   }
/*  277:     */   
/*  278:     */   public ByteBuf order(ByteOrder endianness)
/*  279:     */   {
/*  280: 316 */     if (endianness == null) {
/*  281: 317 */       throw new NullPointerException("endianness");
/*  282:     */     }
/*  283: 319 */     if (endianness == order()) {
/*  284: 320 */       return this;
/*  285:     */     }
/*  286: 323 */     SwappedByteBuf swappedBuf = this.swappedBuf;
/*  287: 324 */     if (swappedBuf == null) {
/*  288: 325 */       this.swappedBuf = (swappedBuf = newSwappedByteBuf());
/*  289:     */     }
/*  290: 327 */     return swappedBuf;
/*  291:     */   }
/*  292:     */   
/*  293:     */   protected SwappedByteBuf newSwappedByteBuf()
/*  294:     */   {
/*  295: 334 */     return new SwappedByteBuf(this);
/*  296:     */   }
/*  297:     */   
/*  298:     */   public byte getByte(int index)
/*  299:     */   {
/*  300: 339 */     checkIndex(index);
/*  301: 340 */     return _getByte(index);
/*  302:     */   }
/*  303:     */   
/*  304:     */   protected abstract byte _getByte(int paramInt);
/*  305:     */   
/*  306:     */   public boolean getBoolean(int index)
/*  307:     */   {
/*  308: 347 */     return getByte(index) != 0;
/*  309:     */   }
/*  310:     */   
/*  311:     */   public short getUnsignedByte(int index)
/*  312:     */   {
/*  313: 352 */     return (short)(getByte(index) & 0xFF);
/*  314:     */   }
/*  315:     */   
/*  316:     */   public short getShort(int index)
/*  317:     */   {
/*  318: 357 */     checkIndex(index, 2);
/*  319: 358 */     return _getShort(index);
/*  320:     */   }
/*  321:     */   
/*  322:     */   protected abstract short _getShort(int paramInt);
/*  323:     */   
/*  324:     */   public int getUnsignedShort(int index)
/*  325:     */   {
/*  326: 365 */     return getShort(index) & 0xFFFF;
/*  327:     */   }
/*  328:     */   
/*  329:     */   public int getUnsignedMedium(int index)
/*  330:     */   {
/*  331: 370 */     checkIndex(index, 3);
/*  332: 371 */     return _getUnsignedMedium(index);
/*  333:     */   }
/*  334:     */   
/*  335:     */   protected abstract int _getUnsignedMedium(int paramInt);
/*  336:     */   
/*  337:     */   public int getMedium(int index)
/*  338:     */   {
/*  339: 378 */     int value = getUnsignedMedium(index);
/*  340: 379 */     if ((value & 0x800000) != 0) {
/*  341: 380 */       value |= 0xFF000000;
/*  342:     */     }
/*  343: 382 */     return value;
/*  344:     */   }
/*  345:     */   
/*  346:     */   public int getInt(int index)
/*  347:     */   {
/*  348: 387 */     checkIndex(index, 4);
/*  349: 388 */     return _getInt(index);
/*  350:     */   }
/*  351:     */   
/*  352:     */   protected abstract int _getInt(int paramInt);
/*  353:     */   
/*  354:     */   public long getUnsignedInt(int index)
/*  355:     */   {
/*  356: 395 */     return getInt(index) & 0xFFFFFFFF;
/*  357:     */   }
/*  358:     */   
/*  359:     */   public long getLong(int index)
/*  360:     */   {
/*  361: 400 */     checkIndex(index, 8);
/*  362: 401 */     return _getLong(index);
/*  363:     */   }
/*  364:     */   
/*  365:     */   protected abstract long _getLong(int paramInt);
/*  366:     */   
/*  367:     */   public char getChar(int index)
/*  368:     */   {
/*  369: 408 */     return (char)getShort(index);
/*  370:     */   }
/*  371:     */   
/*  372:     */   public float getFloat(int index)
/*  373:     */   {
/*  374: 413 */     return Float.intBitsToFloat(getInt(index));
/*  375:     */   }
/*  376:     */   
/*  377:     */   public double getDouble(int index)
/*  378:     */   {
/*  379: 418 */     return Double.longBitsToDouble(getLong(index));
/*  380:     */   }
/*  381:     */   
/*  382:     */   public ByteBuf getBytes(int index, byte[] dst)
/*  383:     */   {
/*  384: 423 */     getBytes(index, dst, 0, dst.length);
/*  385: 424 */     return this;
/*  386:     */   }
/*  387:     */   
/*  388:     */   public ByteBuf getBytes(int index, ByteBuf dst)
/*  389:     */   {
/*  390: 429 */     getBytes(index, dst, dst.writableBytes());
/*  391: 430 */     return this;
/*  392:     */   }
/*  393:     */   
/*  394:     */   public ByteBuf getBytes(int index, ByteBuf dst, int length)
/*  395:     */   {
/*  396: 435 */     getBytes(index, dst, dst.writerIndex(), length);
/*  397: 436 */     dst.writerIndex(dst.writerIndex() + length);
/*  398: 437 */     return this;
/*  399:     */   }
/*  400:     */   
/*  401:     */   public ByteBuf setByte(int index, int value)
/*  402:     */   {
/*  403: 442 */     checkIndex(index);
/*  404: 443 */     _setByte(index, value);
/*  405: 444 */     return this;
/*  406:     */   }
/*  407:     */   
/*  408:     */   protected abstract void _setByte(int paramInt1, int paramInt2);
/*  409:     */   
/*  410:     */   public ByteBuf setBoolean(int index, boolean value)
/*  411:     */   {
/*  412: 451 */     setByte(index, value ? 1 : 0);
/*  413: 452 */     return this;
/*  414:     */   }
/*  415:     */   
/*  416:     */   public ByteBuf setShort(int index, int value)
/*  417:     */   {
/*  418: 457 */     checkIndex(index, 2);
/*  419: 458 */     _setShort(index, value);
/*  420: 459 */     return this;
/*  421:     */   }
/*  422:     */   
/*  423:     */   protected abstract void _setShort(int paramInt1, int paramInt2);
/*  424:     */   
/*  425:     */   public ByteBuf setChar(int index, int value)
/*  426:     */   {
/*  427: 466 */     setShort(index, value);
/*  428: 467 */     return this;
/*  429:     */   }
/*  430:     */   
/*  431:     */   public ByteBuf setMedium(int index, int value)
/*  432:     */   {
/*  433: 472 */     checkIndex(index, 3);
/*  434: 473 */     _setMedium(index, value);
/*  435: 474 */     return this;
/*  436:     */   }
/*  437:     */   
/*  438:     */   protected abstract void _setMedium(int paramInt1, int paramInt2);
/*  439:     */   
/*  440:     */   public ByteBuf setInt(int index, int value)
/*  441:     */   {
/*  442: 481 */     checkIndex(index, 4);
/*  443: 482 */     _setInt(index, value);
/*  444: 483 */     return this;
/*  445:     */   }
/*  446:     */   
/*  447:     */   protected abstract void _setInt(int paramInt1, int paramInt2);
/*  448:     */   
/*  449:     */   public ByteBuf setFloat(int index, float value)
/*  450:     */   {
/*  451: 490 */     setInt(index, Float.floatToRawIntBits(value));
/*  452: 491 */     return this;
/*  453:     */   }
/*  454:     */   
/*  455:     */   public ByteBuf setLong(int index, long value)
/*  456:     */   {
/*  457: 496 */     checkIndex(index, 8);
/*  458: 497 */     _setLong(index, value);
/*  459: 498 */     return this;
/*  460:     */   }
/*  461:     */   
/*  462:     */   protected abstract void _setLong(int paramInt, long paramLong);
/*  463:     */   
/*  464:     */   public ByteBuf setDouble(int index, double value)
/*  465:     */   {
/*  466: 505 */     setLong(index, Double.doubleToRawLongBits(value));
/*  467: 506 */     return this;
/*  468:     */   }
/*  469:     */   
/*  470:     */   public ByteBuf setBytes(int index, byte[] src)
/*  471:     */   {
/*  472: 511 */     setBytes(index, src, 0, src.length);
/*  473: 512 */     return this;
/*  474:     */   }
/*  475:     */   
/*  476:     */   public ByteBuf setBytes(int index, ByteBuf src)
/*  477:     */   {
/*  478: 517 */     setBytes(index, src, src.readableBytes());
/*  479: 518 */     return this;
/*  480:     */   }
/*  481:     */   
/*  482:     */   public ByteBuf setBytes(int index, ByteBuf src, int length)
/*  483:     */   {
/*  484: 523 */     checkIndex(index, length);
/*  485: 524 */     if (src == null) {
/*  486: 525 */       throw new NullPointerException("src");
/*  487:     */     }
/*  488: 527 */     if (length > src.readableBytes()) {
/*  489: 528 */       throw new IndexOutOfBoundsException(String.format("length(%d) exceeds src.readableBytes(%d) where src is: %s", new Object[] { Integer.valueOf(length), Integer.valueOf(src.readableBytes()), src }));
/*  490:     */     }
/*  491: 532 */     setBytes(index, src, src.readerIndex(), length);
/*  492: 533 */     src.readerIndex(src.readerIndex() + length);
/*  493: 534 */     return this;
/*  494:     */   }
/*  495:     */   
/*  496:     */   public ByteBuf setZero(int index, int length)
/*  497:     */   {
/*  498: 539 */     if (length == 0) {
/*  499: 540 */       return this;
/*  500:     */     }
/*  501: 543 */     checkIndex(index, length);
/*  502:     */     
/*  503: 545 */     int nLong = length >>> 3;
/*  504: 546 */     int nBytes = length & 0x7;
/*  505: 547 */     for (int i = nLong; i > 0; i--)
/*  506:     */     {
/*  507: 548 */       setLong(index, 0L);
/*  508: 549 */       index += 8;
/*  509:     */     }
/*  510: 551 */     if (nBytes == 4)
/*  511:     */     {
/*  512: 552 */       setInt(index, 0);
/*  513:     */     }
/*  514: 553 */     else if (nBytes < 4)
/*  515:     */     {
/*  516: 554 */       for (int i = nBytes; i > 0; i--)
/*  517:     */       {
/*  518: 555 */         setByte(index, 0);
/*  519: 556 */         index++;
/*  520:     */       }
/*  521:     */     }
/*  522:     */     else
/*  523:     */     {
/*  524: 559 */       setInt(index, 0);
/*  525: 560 */       index += 4;
/*  526: 561 */       for (int i = nBytes - 4; i > 0; i--)
/*  527:     */       {
/*  528: 562 */         setByte(index, 0);
/*  529: 563 */         index++;
/*  530:     */       }
/*  531:     */     }
/*  532: 566 */     return this;
/*  533:     */   }
/*  534:     */   
/*  535:     */   public byte readByte()
/*  536:     */   {
/*  537: 571 */     checkReadableBytes(1);
/*  538: 572 */     int i = this.readerIndex;
/*  539: 573 */     byte b = getByte(i);
/*  540: 574 */     this.readerIndex = (i + 1);
/*  541: 575 */     return b;
/*  542:     */   }
/*  543:     */   
/*  544:     */   public boolean readBoolean()
/*  545:     */   {
/*  546: 580 */     return readByte() != 0;
/*  547:     */   }
/*  548:     */   
/*  549:     */   public short readUnsignedByte()
/*  550:     */   {
/*  551: 585 */     return (short)(readByte() & 0xFF);
/*  552:     */   }
/*  553:     */   
/*  554:     */   public short readShort()
/*  555:     */   {
/*  556: 590 */     checkReadableBytes(2);
/*  557: 591 */     short v = _getShort(this.readerIndex);
/*  558: 592 */     this.readerIndex += 2;
/*  559: 593 */     return v;
/*  560:     */   }
/*  561:     */   
/*  562:     */   public int readUnsignedShort()
/*  563:     */   {
/*  564: 598 */     return readShort() & 0xFFFF;
/*  565:     */   }
/*  566:     */   
/*  567:     */   public int readMedium()
/*  568:     */   {
/*  569: 603 */     int value = readUnsignedMedium();
/*  570: 604 */     if ((value & 0x800000) != 0) {
/*  571: 605 */       value |= 0xFF000000;
/*  572:     */     }
/*  573: 607 */     return value;
/*  574:     */   }
/*  575:     */   
/*  576:     */   public int readUnsignedMedium()
/*  577:     */   {
/*  578: 612 */     checkReadableBytes(3);
/*  579: 613 */     int v = _getUnsignedMedium(this.readerIndex);
/*  580: 614 */     this.readerIndex += 3;
/*  581: 615 */     return v;
/*  582:     */   }
/*  583:     */   
/*  584:     */   public int readInt()
/*  585:     */   {
/*  586: 620 */     checkReadableBytes(4);
/*  587: 621 */     int v = _getInt(this.readerIndex);
/*  588: 622 */     this.readerIndex += 4;
/*  589: 623 */     return v;
/*  590:     */   }
/*  591:     */   
/*  592:     */   public long readUnsignedInt()
/*  593:     */   {
/*  594: 628 */     return readInt() & 0xFFFFFFFF;
/*  595:     */   }
/*  596:     */   
/*  597:     */   public long readLong()
/*  598:     */   {
/*  599: 633 */     checkReadableBytes(8);
/*  600: 634 */     long v = _getLong(this.readerIndex);
/*  601: 635 */     this.readerIndex += 8;
/*  602: 636 */     return v;
/*  603:     */   }
/*  604:     */   
/*  605:     */   public char readChar()
/*  606:     */   {
/*  607: 641 */     return (char)readShort();
/*  608:     */   }
/*  609:     */   
/*  610:     */   public float readFloat()
/*  611:     */   {
/*  612: 646 */     return Float.intBitsToFloat(readInt());
/*  613:     */   }
/*  614:     */   
/*  615:     */   public double readDouble()
/*  616:     */   {
/*  617: 651 */     return Double.longBitsToDouble(readLong());
/*  618:     */   }
/*  619:     */   
/*  620:     */   public ByteBuf readBytes(int length)
/*  621:     */   {
/*  622: 656 */     checkReadableBytes(length);
/*  623: 657 */     if (length == 0) {
/*  624: 658 */       return Unpooled.EMPTY_BUFFER;
/*  625:     */     }
/*  626: 662 */     ByteBuf buf = Unpooled.buffer(length, this.maxCapacity);
/*  627: 663 */     buf.writeBytes(this, this.readerIndex, length);
/*  628: 664 */     this.readerIndex += length;
/*  629: 665 */     return buf;
/*  630:     */   }
/*  631:     */   
/*  632:     */   public ByteBuf readSlice(int length)
/*  633:     */   {
/*  634: 670 */     ByteBuf slice = slice(this.readerIndex, length);
/*  635: 671 */     this.readerIndex += length;
/*  636: 672 */     return slice;
/*  637:     */   }
/*  638:     */   
/*  639:     */   public ByteBuf readBytes(byte[] dst, int dstIndex, int length)
/*  640:     */   {
/*  641: 677 */     checkReadableBytes(length);
/*  642: 678 */     getBytes(this.readerIndex, dst, dstIndex, length);
/*  643: 679 */     this.readerIndex += length;
/*  644: 680 */     return this;
/*  645:     */   }
/*  646:     */   
/*  647:     */   public ByteBuf readBytes(byte[] dst)
/*  648:     */   {
/*  649: 685 */     readBytes(dst, 0, dst.length);
/*  650: 686 */     return this;
/*  651:     */   }
/*  652:     */   
/*  653:     */   public ByteBuf readBytes(ByteBuf dst)
/*  654:     */   {
/*  655: 691 */     readBytes(dst, dst.writableBytes());
/*  656: 692 */     return this;
/*  657:     */   }
/*  658:     */   
/*  659:     */   public ByteBuf readBytes(ByteBuf dst, int length)
/*  660:     */   {
/*  661: 697 */     if (length > dst.writableBytes()) {
/*  662: 698 */       throw new IndexOutOfBoundsException(String.format("length(%d) exceeds dst.writableBytes(%d) where dst is: %s", new Object[] { Integer.valueOf(length), Integer.valueOf(dst.writableBytes()), dst }));
/*  663:     */     }
/*  664: 701 */     readBytes(dst, dst.writerIndex(), length);
/*  665: 702 */     dst.writerIndex(dst.writerIndex() + length);
/*  666: 703 */     return this;
/*  667:     */   }
/*  668:     */   
/*  669:     */   public ByteBuf readBytes(ByteBuf dst, int dstIndex, int length)
/*  670:     */   {
/*  671: 708 */     checkReadableBytes(length);
/*  672: 709 */     getBytes(this.readerIndex, dst, dstIndex, length);
/*  673: 710 */     this.readerIndex += length;
/*  674: 711 */     return this;
/*  675:     */   }
/*  676:     */   
/*  677:     */   public ByteBuf readBytes(ByteBuffer dst)
/*  678:     */   {
/*  679: 716 */     int length = dst.remaining();
/*  680: 717 */     checkReadableBytes(length);
/*  681: 718 */     getBytes(this.readerIndex, dst);
/*  682: 719 */     this.readerIndex += length;
/*  683: 720 */     return this;
/*  684:     */   }
/*  685:     */   
/*  686:     */   public int readBytes(GatheringByteChannel out, int length)
/*  687:     */     throws IOException
/*  688:     */   {
/*  689: 726 */     checkReadableBytes(length);
/*  690: 727 */     int readBytes = getBytes(this.readerIndex, out, length);
/*  691: 728 */     this.readerIndex += readBytes;
/*  692: 729 */     return readBytes;
/*  693:     */   }
/*  694:     */   
/*  695:     */   public ByteBuf readBytes(OutputStream out, int length)
/*  696:     */     throws IOException
/*  697:     */   {
/*  698: 734 */     checkReadableBytes(length);
/*  699: 735 */     getBytes(this.readerIndex, out, length);
/*  700: 736 */     this.readerIndex += length;
/*  701: 737 */     return this;
/*  702:     */   }
/*  703:     */   
/*  704:     */   public ByteBuf skipBytes(int length)
/*  705:     */   {
/*  706: 742 */     checkReadableBytes(length);
/*  707:     */     
/*  708: 744 */     int newReaderIndex = this.readerIndex + length;
/*  709: 745 */     if (newReaderIndex > this.writerIndex) {
/*  710: 746 */       throw new IndexOutOfBoundsException(String.format("length: %d (expected: readerIndex(%d) + length <= writerIndex(%d))", new Object[] { Integer.valueOf(length), Integer.valueOf(this.readerIndex), Integer.valueOf(this.writerIndex) }));
/*  711:     */     }
/*  712: 750 */     this.readerIndex = newReaderIndex;
/*  713: 751 */     return this;
/*  714:     */   }
/*  715:     */   
/*  716:     */   public ByteBuf writeBoolean(boolean value)
/*  717:     */   {
/*  718: 756 */     writeByte(value ? 1 : 0);
/*  719: 757 */     return this;
/*  720:     */   }
/*  721:     */   
/*  722:     */   public ByteBuf writeByte(int value)
/*  723:     */   {
/*  724: 762 */     ensureWritable(1);
/*  725: 763 */     setByte(this.writerIndex++, value);
/*  726: 764 */     return this;
/*  727:     */   }
/*  728:     */   
/*  729:     */   public ByteBuf writeShort(int value)
/*  730:     */   {
/*  731: 769 */     ensureWritable(2);
/*  732: 770 */     _setShort(this.writerIndex, value);
/*  733: 771 */     this.writerIndex += 2;
/*  734: 772 */     return this;
/*  735:     */   }
/*  736:     */   
/*  737:     */   public ByteBuf writeMedium(int value)
/*  738:     */   {
/*  739: 777 */     ensureWritable(3);
/*  740: 778 */     _setMedium(this.writerIndex, value);
/*  741: 779 */     this.writerIndex += 3;
/*  742: 780 */     return this;
/*  743:     */   }
/*  744:     */   
/*  745:     */   public ByteBuf writeInt(int value)
/*  746:     */   {
/*  747: 785 */     ensureWritable(4);
/*  748: 786 */     _setInt(this.writerIndex, value);
/*  749: 787 */     this.writerIndex += 4;
/*  750: 788 */     return this;
/*  751:     */   }
/*  752:     */   
/*  753:     */   public ByteBuf writeLong(long value)
/*  754:     */   {
/*  755: 793 */     ensureWritable(8);
/*  756: 794 */     _setLong(this.writerIndex, value);
/*  757: 795 */     this.writerIndex += 8;
/*  758: 796 */     return this;
/*  759:     */   }
/*  760:     */   
/*  761:     */   public ByteBuf writeChar(int value)
/*  762:     */   {
/*  763: 801 */     writeShort(value);
/*  764: 802 */     return this;
/*  765:     */   }
/*  766:     */   
/*  767:     */   public ByteBuf writeFloat(float value)
/*  768:     */   {
/*  769: 807 */     writeInt(Float.floatToRawIntBits(value));
/*  770: 808 */     return this;
/*  771:     */   }
/*  772:     */   
/*  773:     */   public ByteBuf writeDouble(double value)
/*  774:     */   {
/*  775: 813 */     writeLong(Double.doubleToRawLongBits(value));
/*  776: 814 */     return this;
/*  777:     */   }
/*  778:     */   
/*  779:     */   public ByteBuf writeBytes(byte[] src, int srcIndex, int length)
/*  780:     */   {
/*  781: 819 */     ensureWritable(length);
/*  782: 820 */     setBytes(this.writerIndex, src, srcIndex, length);
/*  783: 821 */     this.writerIndex += length;
/*  784: 822 */     return this;
/*  785:     */   }
/*  786:     */   
/*  787:     */   public ByteBuf writeBytes(byte[] src)
/*  788:     */   {
/*  789: 827 */     writeBytes(src, 0, src.length);
/*  790: 828 */     return this;
/*  791:     */   }
/*  792:     */   
/*  793:     */   public ByteBuf writeBytes(ByteBuf src)
/*  794:     */   {
/*  795: 833 */     writeBytes(src, src.readableBytes());
/*  796: 834 */     return this;
/*  797:     */   }
/*  798:     */   
/*  799:     */   public ByteBuf writeBytes(ByteBuf src, int length)
/*  800:     */   {
/*  801: 839 */     if (length > src.readableBytes()) {
/*  802: 840 */       throw new IndexOutOfBoundsException(String.format("length(%d) exceeds src.readableBytes(%d) where src is: %s", new Object[] { Integer.valueOf(length), Integer.valueOf(src.readableBytes()), src }));
/*  803:     */     }
/*  804: 843 */     writeBytes(src, src.readerIndex(), length);
/*  805: 844 */     src.readerIndex(src.readerIndex() + length);
/*  806: 845 */     return this;
/*  807:     */   }
/*  808:     */   
/*  809:     */   public ByteBuf writeBytes(ByteBuf src, int srcIndex, int length)
/*  810:     */   {
/*  811: 850 */     ensureWritable(length);
/*  812: 851 */     setBytes(this.writerIndex, src, srcIndex, length);
/*  813: 852 */     this.writerIndex += length;
/*  814: 853 */     return this;
/*  815:     */   }
/*  816:     */   
/*  817:     */   public ByteBuf writeBytes(ByteBuffer src)
/*  818:     */   {
/*  819: 858 */     int length = src.remaining();
/*  820: 859 */     ensureWritable(length);
/*  821: 860 */     setBytes(this.writerIndex, src);
/*  822: 861 */     this.writerIndex += length;
/*  823: 862 */     return this;
/*  824:     */   }
/*  825:     */   
/*  826:     */   public int writeBytes(InputStream in, int length)
/*  827:     */     throws IOException
/*  828:     */   {
/*  829: 868 */     ensureWritable(length);
/*  830: 869 */     int writtenBytes = setBytes(this.writerIndex, in, length);
/*  831: 870 */     if (writtenBytes > 0) {
/*  832: 871 */       this.writerIndex += writtenBytes;
/*  833:     */     }
/*  834: 873 */     return writtenBytes;
/*  835:     */   }
/*  836:     */   
/*  837:     */   public int writeBytes(ScatteringByteChannel in, int length)
/*  838:     */     throws IOException
/*  839:     */   {
/*  840: 878 */     ensureWritable(length);
/*  841: 879 */     int writtenBytes = setBytes(this.writerIndex, in, length);
/*  842: 880 */     if (writtenBytes > 0) {
/*  843: 881 */       this.writerIndex += writtenBytes;
/*  844:     */     }
/*  845: 883 */     return writtenBytes;
/*  846:     */   }
/*  847:     */   
/*  848:     */   public ByteBuf writeZero(int length)
/*  849:     */   {
/*  850: 888 */     if (length == 0) {
/*  851: 889 */       return this;
/*  852:     */     }
/*  853: 892 */     ensureWritable(length);
/*  854: 893 */     checkIndex(this.writerIndex, length);
/*  855:     */     
/*  856: 895 */     int nLong = length >>> 3;
/*  857: 896 */     int nBytes = length & 0x7;
/*  858: 897 */     for (int i = nLong; i > 0; i--) {
/*  859: 898 */       writeLong(0L);
/*  860:     */     }
/*  861: 900 */     if (nBytes == 4)
/*  862:     */     {
/*  863: 901 */       writeInt(0);
/*  864:     */     }
/*  865: 902 */     else if (nBytes < 4)
/*  866:     */     {
/*  867: 903 */       for (int i = nBytes; i > 0; i--) {
/*  868: 904 */         writeByte(0);
/*  869:     */       }
/*  870:     */     }
/*  871:     */     else
/*  872:     */     {
/*  873: 907 */       writeInt(0);
/*  874: 908 */       for (int i = nBytes - 4; i > 0; i--) {
/*  875: 909 */         writeByte(0);
/*  876:     */       }
/*  877:     */     }
/*  878: 912 */     return this;
/*  879:     */   }
/*  880:     */   
/*  881:     */   public ByteBuf copy()
/*  882:     */   {
/*  883: 917 */     return copy(this.readerIndex, readableBytes());
/*  884:     */   }
/*  885:     */   
/*  886:     */   public ByteBuf duplicate()
/*  887:     */   {
/*  888: 922 */     return new DuplicatedByteBuf(this);
/*  889:     */   }
/*  890:     */   
/*  891:     */   public ByteBuf slice()
/*  892:     */   {
/*  893: 927 */     return slice(this.readerIndex, readableBytes());
/*  894:     */   }
/*  895:     */   
/*  896:     */   public ByteBuf slice(int index, int length)
/*  897:     */   {
/*  898: 932 */     if (length == 0) {
/*  899: 933 */       return Unpooled.EMPTY_BUFFER;
/*  900:     */     }
/*  901: 936 */     return new SlicedByteBuf(this, index, length);
/*  902:     */   }
/*  903:     */   
/*  904:     */   public ByteBuffer nioBuffer()
/*  905:     */   {
/*  906: 941 */     return nioBuffer(this.readerIndex, readableBytes());
/*  907:     */   }
/*  908:     */   
/*  909:     */   public ByteBuffer[] nioBuffers()
/*  910:     */   {
/*  911: 946 */     return nioBuffers(this.readerIndex, readableBytes());
/*  912:     */   }
/*  913:     */   
/*  914:     */   public String toString(Charset charset)
/*  915:     */   {
/*  916: 951 */     return toString(this.readerIndex, readableBytes(), charset);
/*  917:     */   }
/*  918:     */   
/*  919:     */   public String toString(int index, int length, Charset charset)
/*  920:     */   {
/*  921: 956 */     if (length == 0) {
/*  922: 957 */       return "";
/*  923:     */     }
/*  924:     */     ByteBuffer nioBuffer;
/*  925:     */     ByteBuffer nioBuffer;
/*  926: 961 */     if (nioBufferCount() == 1)
/*  927:     */     {
/*  928: 962 */       nioBuffer = nioBuffer(index, length);
/*  929:     */     }
/*  930:     */     else
/*  931:     */     {
/*  932: 964 */       nioBuffer = ByteBuffer.allocate(length);
/*  933: 965 */       getBytes(index, nioBuffer);
/*  934: 966 */       nioBuffer.flip();
/*  935:     */     }
/*  936: 969 */     return ByteBufUtil.decodeString(nioBuffer, charset);
/*  937:     */   }
/*  938:     */   
/*  939:     */   public int indexOf(int fromIndex, int toIndex, byte value)
/*  940:     */   {
/*  941: 974 */     return ByteBufUtil.indexOf(this, fromIndex, toIndex, value);
/*  942:     */   }
/*  943:     */   
/*  944:     */   public int bytesBefore(byte value)
/*  945:     */   {
/*  946: 979 */     return bytesBefore(readerIndex(), readableBytes(), value);
/*  947:     */   }
/*  948:     */   
/*  949:     */   public int bytesBefore(int length, byte value)
/*  950:     */   {
/*  951: 984 */     checkReadableBytes(length);
/*  952: 985 */     return bytesBefore(readerIndex(), length, value);
/*  953:     */   }
/*  954:     */   
/*  955:     */   public int bytesBefore(int index, int length, byte value)
/*  956:     */   {
/*  957: 990 */     int endIndex = indexOf(index, index + length, value);
/*  958: 991 */     if (endIndex < 0) {
/*  959: 992 */       return -1;
/*  960:     */     }
/*  961: 994 */     return endIndex - index;
/*  962:     */   }
/*  963:     */   
/*  964:     */   public int forEachByte(ByteBufProcessor processor)
/*  965:     */   {
/*  966: 999 */     int index = this.readerIndex;
/*  967:1000 */     int length = this.writerIndex - index;
/*  968:1001 */     return forEachByteAsc0(index, length, processor);
/*  969:     */   }
/*  970:     */   
/*  971:     */   public int forEachByte(int index, int length, ByteBufProcessor processor)
/*  972:     */   {
/*  973:1006 */     return forEachByteAsc0(index, length, processor);
/*  974:     */   }
/*  975:     */   
/*  976:     */   private int forEachByteAsc0(int index, int length, ByteBufProcessor processor)
/*  977:     */   {
/*  978:1010 */     checkIndex(index, length);
/*  979:1012 */     if (processor == null) {
/*  980:1013 */       throw new NullPointerException("processor");
/*  981:     */     }
/*  982:1016 */     if (length == 0) {
/*  983:1017 */       return -1;
/*  984:     */     }
/*  985:1020 */     int endIndex = index + length;
/*  986:1021 */     int i = index;
/*  987:     */     try
/*  988:     */     {
/*  989:     */       do
/*  990:     */       {
/*  991:1024 */         if (processor.process(_getByte(i))) {
/*  992:1025 */           i++;
/*  993:     */         } else {
/*  994:1027 */           return i;
/*  995:     */         }
/*  996:1029 */       } while (i < endIndex);
/*  997:     */     }
/*  998:     */     catch (Exception e)
/*  999:     */     {
/* 1000:1031 */       PlatformDependent.throwException(e);
/* 1001:     */     }
/* 1002:1034 */     return -1;
/* 1003:     */   }
/* 1004:     */   
/* 1005:     */   public int forEachByteDesc(ByteBufProcessor processor)
/* 1006:     */   {
/* 1007:1039 */     int index = this.readerIndex;
/* 1008:1040 */     int length = this.writerIndex - index;
/* 1009:1041 */     return forEachByteDesc0(index, length, processor);
/* 1010:     */   }
/* 1011:     */   
/* 1012:     */   public int forEachByteDesc(int index, int length, ByteBufProcessor processor)
/* 1013:     */   {
/* 1014:1046 */     return forEachByteDesc0(index, length, processor);
/* 1015:     */   }
/* 1016:     */   
/* 1017:     */   private int forEachByteDesc0(int index, int length, ByteBufProcessor processor)
/* 1018:     */   {
/* 1019:1050 */     checkIndex(index, length);
/* 1020:1052 */     if (processor == null) {
/* 1021:1053 */       throw new NullPointerException("processor");
/* 1022:     */     }
/* 1023:1056 */     if (length == 0) {
/* 1024:1057 */       return -1;
/* 1025:     */     }
/* 1026:1060 */     int i = index + length - 1;
/* 1027:     */     try
/* 1028:     */     {
/* 1029:     */       do
/* 1030:     */       {
/* 1031:1063 */         if (processor.process(_getByte(i))) {
/* 1032:1064 */           i--;
/* 1033:     */         } else {
/* 1034:1066 */           return i;
/* 1035:     */         }
/* 1036:1068 */       } while (i >= index);
/* 1037:     */     }
/* 1038:     */     catch (Exception e)
/* 1039:     */     {
/* 1040:1070 */       PlatformDependent.throwException(e);
/* 1041:     */     }
/* 1042:1073 */     return -1;
/* 1043:     */   }
/* 1044:     */   
/* 1045:     */   public int hashCode()
/* 1046:     */   {
/* 1047:1078 */     return ByteBufUtil.hashCode(this);
/* 1048:     */   }
/* 1049:     */   
/* 1050:     */   public boolean equals(Object o)
/* 1051:     */   {
/* 1052:1083 */     if (this == o) {
/* 1053:1084 */       return true;
/* 1054:     */     }
/* 1055:1086 */     if ((o instanceof ByteBuf)) {
/* 1056:1087 */       return ByteBufUtil.equals(this, (ByteBuf)o);
/* 1057:     */     }
/* 1058:1089 */     return false;
/* 1059:     */   }
/* 1060:     */   
/* 1061:     */   public int compareTo(ByteBuf that)
/* 1062:     */   {
/* 1063:1094 */     return ByteBufUtil.compare(this, that);
/* 1064:     */   }
/* 1065:     */   
/* 1066:     */   public String toString()
/* 1067:     */   {
/* 1068:1099 */     if (refCnt() == 0) {
/* 1069:1100 */       return StringUtil.simpleClassName(this) + "(freed)";
/* 1070:     */     }
/* 1071:1103 */     StringBuilder buf = new StringBuilder();
/* 1072:1104 */     buf.append(StringUtil.simpleClassName(this));
/* 1073:1105 */     buf.append("(ridx: ");
/* 1074:1106 */     buf.append(this.readerIndex);
/* 1075:1107 */     buf.append(", widx: ");
/* 1076:1108 */     buf.append(this.writerIndex);
/* 1077:1109 */     buf.append(", cap: ");
/* 1078:1110 */     buf.append(capacity());
/* 1079:1111 */     if (this.maxCapacity != 2147483647)
/* 1080:     */     {
/* 1081:1112 */       buf.append('/');
/* 1082:1113 */       buf.append(this.maxCapacity);
/* 1083:     */     }
/* 1084:1116 */     ByteBuf unwrapped = unwrap();
/* 1085:1117 */     if (unwrapped != null)
/* 1086:     */     {
/* 1087:1118 */       buf.append(", unwrapped: ");
/* 1088:1119 */       buf.append(unwrapped);
/* 1089:     */     }
/* 1090:1121 */     buf.append(')');
/* 1091:1122 */     return buf.toString();
/* 1092:     */   }
/* 1093:     */   
/* 1094:     */   protected final void checkIndex(int index)
/* 1095:     */   {
/* 1096:1126 */     ensureAccessible();
/* 1097:1127 */     if ((index < 0) || (index >= capacity())) {
/* 1098:1128 */       throw new IndexOutOfBoundsException(String.format("index: %d (expected: range(0, %d))", new Object[] { Integer.valueOf(index), Integer.valueOf(capacity()) }));
/* 1099:     */     }
/* 1100:     */   }
/* 1101:     */   
/* 1102:     */   protected final void checkIndex(int index, int fieldLength)
/* 1103:     */   {
/* 1104:1134 */     ensureAccessible();
/* 1105:1135 */     if (fieldLength < 0) {
/* 1106:1136 */       throw new IllegalArgumentException("length: " + fieldLength + " (expected: >= 0)");
/* 1107:     */     }
/* 1108:1138 */     if ((index < 0) || (index > capacity() - fieldLength)) {
/* 1109:1139 */       throw new IndexOutOfBoundsException(String.format("index: %d, length: %d (expected: range(0, %d))", new Object[] { Integer.valueOf(index), Integer.valueOf(fieldLength), Integer.valueOf(capacity()) }));
/* 1110:     */     }
/* 1111:     */   }
/* 1112:     */   
/* 1113:     */   protected final void checkSrcIndex(int index, int length, int srcIndex, int srcCapacity)
/* 1114:     */   {
/* 1115:1145 */     checkIndex(index, length);
/* 1116:1146 */     if ((srcIndex < 0) || (srcIndex > srcCapacity - length)) {
/* 1117:1147 */       throw new IndexOutOfBoundsException(String.format("srcIndex: %d, length: %d (expected: range(0, %d))", new Object[] { Integer.valueOf(srcIndex), Integer.valueOf(length), Integer.valueOf(srcCapacity) }));
/* 1118:     */     }
/* 1119:     */   }
/* 1120:     */   
/* 1121:     */   protected final void checkDstIndex(int index, int length, int dstIndex, int dstCapacity)
/* 1122:     */   {
/* 1123:1153 */     checkIndex(index, length);
/* 1124:1154 */     if ((dstIndex < 0) || (dstIndex > dstCapacity - length)) {
/* 1125:1155 */       throw new IndexOutOfBoundsException(String.format("dstIndex: %d, length: %d (expected: range(0, %d))", new Object[] { Integer.valueOf(dstIndex), Integer.valueOf(length), Integer.valueOf(dstCapacity) }));
/* 1126:     */     }
/* 1127:     */   }
/* 1128:     */   
/* 1129:     */   protected final void checkReadableBytes(int minimumReadableBytes)
/* 1130:     */   {
/* 1131:1166 */     ensureAccessible();
/* 1132:1167 */     if (minimumReadableBytes < 0) {
/* 1133:1168 */       throw new IllegalArgumentException("minimumReadableBytes: " + minimumReadableBytes + " (expected: >= 0)");
/* 1134:     */     }
/* 1135:1170 */     if (this.readerIndex > this.writerIndex - minimumReadableBytes) {
/* 1136:1171 */       throw new IndexOutOfBoundsException(String.format("readerIndex(%d) + length(%d) exceeds writerIndex(%d): %s", new Object[] { Integer.valueOf(this.readerIndex), Integer.valueOf(minimumReadableBytes), Integer.valueOf(this.writerIndex), this }));
/* 1137:     */     }
/* 1138:     */   }
/* 1139:     */   
/* 1140:     */   protected final void ensureAccessible()
/* 1141:     */   {
/* 1142:1182 */     if (refCnt() == 0) {
/* 1143:1183 */       throw new IllegalReferenceCountException(0);
/* 1144:     */     }
/* 1145:     */   }
/* 1146:     */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.AbstractByteBuf
 * JD-Core Version:    0.7.0.1
 */