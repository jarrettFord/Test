/*    1:     */ package io.netty.buffer;
/*    2:     */ 
/*    3:     */ import io.netty.util.ResourceLeak;
/*    4:     */ import io.netty.util.ResourceLeakDetector;
/*    5:     */ import io.netty.util.internal.EmptyArrays;
/*    6:     */ import java.io.IOException;
/*    7:     */ import java.io.InputStream;
/*    8:     */ import java.io.OutputStream;
/*    9:     */ import java.nio.ByteBuffer;
/*   10:     */ import java.nio.ByteOrder;
/*   11:     */ import java.nio.channels.GatheringByteChannel;
/*   12:     */ import java.nio.channels.ScatteringByteChannel;
/*   13:     */ import java.util.ArrayList;
/*   14:     */ import java.util.Collection;
/*   15:     */ import java.util.Collections;
/*   16:     */ import java.util.Iterator;
/*   17:     */ import java.util.List;
/*   18:     */ import java.util.ListIterator;
/*   19:     */ 
/*   20:     */ public class CompositeByteBuf
/*   21:     */   extends AbstractReferenceCountedByteBuf
/*   22:     */ {
/*   23:     */   private final ResourceLeak leak;
/*   24:     */   private final ByteBufAllocator alloc;
/*   25:     */   private final boolean direct;
/*   26:  45 */   private final List<Component> components = new ArrayList();
/*   27:     */   private final int maxNumComponents;
/*   28:  47 */   private static final ByteBuffer FULL_BYTEBUFFER = (ByteBuffer)ByteBuffer.allocate(1).position(1);
/*   29:     */   private boolean freed;
/*   30:     */   
/*   31:     */   public CompositeByteBuf(ByteBufAllocator alloc, boolean direct, int maxNumComponents)
/*   32:     */   {
/*   33:  52 */     super(2147483647);
/*   34:  53 */     if (alloc == null) {
/*   35:  54 */       throw new NullPointerException("alloc");
/*   36:     */     }
/*   37:  56 */     this.alloc = alloc;
/*   38:  57 */     this.direct = direct;
/*   39:  58 */     this.maxNumComponents = maxNumComponents;
/*   40:  59 */     this.leak = leakDetector.open(this);
/*   41:     */   }
/*   42:     */   
/*   43:     */   public CompositeByteBuf(ByteBufAllocator alloc, boolean direct, int maxNumComponents, ByteBuf... buffers)
/*   44:     */   {
/*   45:  63 */     super(2147483647);
/*   46:  64 */     if (alloc == null) {
/*   47:  65 */       throw new NullPointerException("alloc");
/*   48:     */     }
/*   49:  67 */     if (maxNumComponents < 2) {
/*   50:  68 */       throw new IllegalArgumentException("maxNumComponents: " + maxNumComponents + " (expected: >= 2)");
/*   51:     */     }
/*   52:  72 */     this.alloc = alloc;
/*   53:  73 */     this.direct = direct;
/*   54:  74 */     this.maxNumComponents = maxNumComponents;
/*   55:     */     
/*   56:  76 */     addComponents0(0, buffers);
/*   57:  77 */     consolidateIfNeeded();
/*   58:  78 */     setIndex(0, capacity());
/*   59:  79 */     this.leak = leakDetector.open(this);
/*   60:     */   }
/*   61:     */   
/*   62:     */   public CompositeByteBuf(ByteBufAllocator alloc, boolean direct, int maxNumComponents, Iterable<ByteBuf> buffers)
/*   63:     */   {
/*   64:  84 */     super(2147483647);
/*   65:  85 */     if (alloc == null) {
/*   66:  86 */       throw new NullPointerException("alloc");
/*   67:     */     }
/*   68:  88 */     if (maxNumComponents < 2) {
/*   69:  89 */       throw new IllegalArgumentException("maxNumComponents: " + maxNumComponents + " (expected: >= 2)");
/*   70:     */     }
/*   71:  93 */     this.alloc = alloc;
/*   72:  94 */     this.direct = direct;
/*   73:  95 */     this.maxNumComponents = maxNumComponents;
/*   74:  96 */     addComponents0(0, buffers);
/*   75:  97 */     consolidateIfNeeded();
/*   76:  98 */     setIndex(0, capacity());
/*   77:  99 */     this.leak = leakDetector.open(this);
/*   78:     */   }
/*   79:     */   
/*   80:     */   public CompositeByteBuf addComponent(ByteBuf buffer)
/*   81:     */   {
/*   82: 111 */     addComponent0(this.components.size(), buffer);
/*   83: 112 */     consolidateIfNeeded();
/*   84: 113 */     return this;
/*   85:     */   }
/*   86:     */   
/*   87:     */   public CompositeByteBuf addComponents(ByteBuf... buffers)
/*   88:     */   {
/*   89: 125 */     addComponents0(this.components.size(), buffers);
/*   90: 126 */     consolidateIfNeeded();
/*   91: 127 */     return this;
/*   92:     */   }
/*   93:     */   
/*   94:     */   public CompositeByteBuf addComponents(Iterable<ByteBuf> buffers)
/*   95:     */   {
/*   96: 139 */     addComponents0(this.components.size(), buffers);
/*   97: 140 */     consolidateIfNeeded();
/*   98: 141 */     return this;
/*   99:     */   }
/*  100:     */   
/*  101:     */   public CompositeByteBuf addComponent(int cIndex, ByteBuf buffer)
/*  102:     */   {
/*  103: 154 */     addComponent0(cIndex, buffer);
/*  104: 155 */     consolidateIfNeeded();
/*  105: 156 */     return this;
/*  106:     */   }
/*  107:     */   
/*  108:     */   private int addComponent0(int cIndex, ByteBuf buffer)
/*  109:     */   {
/*  110: 160 */     checkComponentIndex(cIndex);
/*  111: 162 */     if (buffer == null) {
/*  112: 163 */       throw new NullPointerException("buffer");
/*  113:     */     }
/*  114: 166 */     int readableBytes = buffer.readableBytes();
/*  115: 167 */     if (readableBytes == 0) {
/*  116: 168 */       return cIndex;
/*  117:     */     }
/*  118: 172 */     Component c = new Component(buffer.order(ByteOrder.BIG_ENDIAN).slice());
/*  119: 173 */     if (cIndex == this.components.size())
/*  120:     */     {
/*  121: 174 */       this.components.add(c);
/*  122: 175 */       if (cIndex == 0)
/*  123:     */       {
/*  124: 176 */         c.endOffset = readableBytes;
/*  125:     */       }
/*  126:     */       else
/*  127:     */       {
/*  128: 178 */         Component prev = (Component)this.components.get(cIndex - 1);
/*  129: 179 */         c.offset = prev.endOffset;
/*  130: 180 */         c.endOffset = (c.offset + readableBytes);
/*  131:     */       }
/*  132:     */     }
/*  133:     */     else
/*  134:     */     {
/*  135: 183 */       this.components.add(cIndex, c);
/*  136: 184 */       updateComponentOffsets(cIndex);
/*  137:     */     }
/*  138: 186 */     return cIndex;
/*  139:     */   }
/*  140:     */   
/*  141:     */   public CompositeByteBuf addComponents(int cIndex, ByteBuf... buffers)
/*  142:     */   {
/*  143: 199 */     addComponents0(cIndex, buffers);
/*  144: 200 */     consolidateIfNeeded();
/*  145: 201 */     return this;
/*  146:     */   }
/*  147:     */   
/*  148:     */   private int addComponents0(int cIndex, ByteBuf... buffers)
/*  149:     */   {
/*  150: 205 */     checkComponentIndex(cIndex);
/*  151: 207 */     if (buffers == null) {
/*  152: 208 */       throw new NullPointerException("buffers");
/*  153:     */     }
/*  154: 211 */     int readableBytes = 0;
/*  155: 212 */     for (ByteBuf b : buffers)
/*  156:     */     {
/*  157: 213 */       if (b == null) {
/*  158:     */         break;
/*  159:     */       }
/*  160: 216 */       readableBytes += b.readableBytes();
/*  161:     */     }
/*  162: 219 */     if (readableBytes == 0) {
/*  163: 220 */       return cIndex;
/*  164:     */     }
/*  165: 224 */     for (ByteBuf b : buffers)
/*  166:     */     {
/*  167: 225 */       if (b == null) {
/*  168:     */         break;
/*  169:     */       }
/*  170: 228 */       if (b.isReadable())
/*  171:     */       {
/*  172: 229 */         cIndex = addComponent0(cIndex, b) + 1;
/*  173: 230 */         int size = this.components.size();
/*  174: 231 */         if (cIndex > size) {
/*  175: 232 */           cIndex = size;
/*  176:     */         }
/*  177:     */       }
/*  178:     */       else
/*  179:     */       {
/*  180: 235 */         b.release();
/*  181:     */       }
/*  182:     */     }
/*  183: 238 */     return cIndex;
/*  184:     */   }
/*  185:     */   
/*  186:     */   public CompositeByteBuf addComponents(int cIndex, Iterable<ByteBuf> buffers)
/*  187:     */   {
/*  188: 251 */     addComponents0(cIndex, buffers);
/*  189: 252 */     consolidateIfNeeded();
/*  190: 253 */     return this;
/*  191:     */   }
/*  192:     */   
/*  193:     */   private int addComponents0(int cIndex, Iterable<ByteBuf> buffers)
/*  194:     */   {
/*  195: 257 */     if (buffers == null) {
/*  196: 258 */       throw new NullPointerException("buffers");
/*  197:     */     }
/*  198: 261 */     if ((buffers instanceof ByteBuf)) {
/*  199: 263 */       return addComponent0(cIndex, (ByteBuf)buffers);
/*  200:     */     }
/*  201: 266 */     if (!(buffers instanceof Collection))
/*  202:     */     {
/*  203: 267 */       List<ByteBuf> list = new ArrayList();
/*  204: 268 */       for (ByteBuf b : buffers) {
/*  205: 269 */         list.add(b);
/*  206:     */       }
/*  207: 271 */       buffers = list;
/*  208:     */     }
/*  209: 274 */     Collection<ByteBuf> col = (Collection)buffers;
/*  210: 275 */     return addComponents0(cIndex, (ByteBuf[])col.toArray(new ByteBuf[col.size()]));
/*  211:     */   }
/*  212:     */   
/*  213:     */   private void consolidateIfNeeded()
/*  214:     */   {
/*  215: 285 */     int numComponents = this.components.size();
/*  216: 286 */     if (numComponents > this.maxNumComponents)
/*  217:     */     {
/*  218: 287 */       int capacity = ((Component)this.components.get(numComponents - 1)).endOffset;
/*  219:     */       
/*  220: 289 */       ByteBuf consolidated = allocBuffer(capacity);
/*  221: 293 */       for (int i = 0; i < numComponents; i++)
/*  222:     */       {
/*  223: 294 */         Component c = (Component)this.components.get(i);
/*  224: 295 */         ByteBuf b = c.buf;
/*  225: 296 */         consolidated.writeBytes(b);
/*  226: 297 */         c.freeIfNecessary();
/*  227:     */       }
/*  228: 299 */       Component c = new Component(consolidated);
/*  229: 300 */       c.endOffset = c.length;
/*  230: 301 */       this.components.clear();
/*  231: 302 */       this.components.add(c);
/*  232:     */     }
/*  233:     */   }
/*  234:     */   
/*  235:     */   private void checkComponentIndex(int cIndex)
/*  236:     */   {
/*  237: 307 */     ensureAccessible();
/*  238: 308 */     if ((cIndex < 0) || (cIndex > this.components.size())) {
/*  239: 309 */       throw new IndexOutOfBoundsException(String.format("cIndex: %d (expected: >= 0 && <= numComponents(%d))", new Object[] { Integer.valueOf(cIndex), Integer.valueOf(this.components.size()) }));
/*  240:     */     }
/*  241:     */   }
/*  242:     */   
/*  243:     */   private void checkComponentIndex(int cIndex, int numComponents)
/*  244:     */   {
/*  245: 316 */     ensureAccessible();
/*  246: 317 */     if ((cIndex < 0) || (cIndex + numComponents > this.components.size())) {
/*  247: 318 */       throw new IndexOutOfBoundsException(String.format("cIndex: %d, numComponents: %d (expected: cIndex >= 0 && cIndex + numComponents <= totalNumComponents(%d))", new Object[] { Integer.valueOf(cIndex), Integer.valueOf(numComponents), Integer.valueOf(this.components.size()) }));
/*  248:     */     }
/*  249:     */   }
/*  250:     */   
/*  251:     */   private void updateComponentOffsets(int cIndex)
/*  252:     */   {
/*  253: 326 */     int size = this.components.size();
/*  254: 327 */     if (size <= cIndex) {
/*  255: 328 */       return;
/*  256:     */     }
/*  257: 331 */     Component c = (Component)this.components.get(cIndex);
/*  258: 332 */     if (cIndex == 0)
/*  259:     */     {
/*  260: 333 */       c.offset = 0;
/*  261: 334 */       c.endOffset = c.length;
/*  262: 335 */       cIndex++;
/*  263:     */     }
/*  264: 338 */     for (int i = cIndex; i < size; i++)
/*  265:     */     {
/*  266: 339 */       Component prev = (Component)this.components.get(i - 1);
/*  267: 340 */       Component cur = (Component)this.components.get(i);
/*  268: 341 */       cur.offset = prev.endOffset;
/*  269: 342 */       cur.endOffset = (cur.offset + cur.length);
/*  270:     */     }
/*  271:     */   }
/*  272:     */   
/*  273:     */   public CompositeByteBuf removeComponent(int cIndex)
/*  274:     */   {
/*  275: 352 */     checkComponentIndex(cIndex);
/*  276: 353 */     ((Component)this.components.remove(cIndex)).freeIfNecessary();
/*  277: 354 */     updateComponentOffsets(cIndex);
/*  278: 355 */     return this;
/*  279:     */   }
/*  280:     */   
/*  281:     */   public CompositeByteBuf removeComponents(int cIndex, int numComponents)
/*  282:     */   {
/*  283: 365 */     checkComponentIndex(cIndex, numComponents);
/*  284:     */     
/*  285: 367 */     List<Component> toRemove = this.components.subList(cIndex, cIndex + numComponents);
/*  286: 368 */     for (Component c : toRemove) {
/*  287: 369 */       c.freeIfNecessary();
/*  288:     */     }
/*  289: 371 */     toRemove.clear();
/*  290:     */     
/*  291: 373 */     updateComponentOffsets(cIndex);
/*  292: 374 */     return this;
/*  293:     */   }
/*  294:     */   
/*  295:     */   public Iterator<ByteBuf> iterator()
/*  296:     */   {
/*  297: 378 */     ensureAccessible();
/*  298: 379 */     List<ByteBuf> list = new ArrayList(this.components.size());
/*  299: 380 */     for (Component c : this.components) {
/*  300: 381 */       list.add(c.buf);
/*  301:     */     }
/*  302: 383 */     return list.iterator();
/*  303:     */   }
/*  304:     */   
/*  305:     */   public List<ByteBuf> decompose(int offset, int length)
/*  306:     */   {
/*  307: 390 */     checkIndex(offset, length);
/*  308: 391 */     if (length == 0) {
/*  309: 392 */       return Collections.emptyList();
/*  310:     */     }
/*  311: 395 */     int componentId = toComponentIndex(offset);
/*  312: 396 */     List<ByteBuf> slice = new ArrayList(this.components.size());
/*  313:     */     
/*  314:     */ 
/*  315: 399 */     Component firstC = (Component)this.components.get(componentId);
/*  316: 400 */     ByteBuf first = firstC.buf.duplicate();
/*  317: 401 */     first.readerIndex(offset - firstC.offset);
/*  318:     */     
/*  319: 403 */     ByteBuf buf = first;
/*  320: 404 */     int bytesToSlice = length;
/*  321:     */     do
/*  322:     */     {
/*  323: 406 */       int readableBytes = buf.readableBytes();
/*  324: 407 */       if (bytesToSlice <= readableBytes)
/*  325:     */       {
/*  326: 409 */         buf.writerIndex(buf.readerIndex() + bytesToSlice);
/*  327: 410 */         slice.add(buf);
/*  328: 411 */         break;
/*  329:     */       }
/*  330: 414 */       slice.add(buf);
/*  331: 415 */       bytesToSlice -= readableBytes;
/*  332: 416 */       componentId++;
/*  333:     */       
/*  334:     */ 
/*  335: 419 */       buf = ((Component)this.components.get(componentId)).buf.duplicate();
/*  336: 421 */     } while (bytesToSlice > 0);
/*  337: 424 */     for (int i = 0; i < slice.size(); i++) {
/*  338: 425 */       slice.set(i, ((ByteBuf)slice.get(i)).slice());
/*  339:     */     }
/*  340: 428 */     return slice;
/*  341:     */   }
/*  342:     */   
/*  343:     */   public boolean isDirect()
/*  344:     */   {
/*  345: 433 */     int size = this.components.size();
/*  346: 434 */     if (size == 0) {
/*  347: 435 */       return false;
/*  348:     */     }
/*  349: 437 */     for (int i = 0; i < size; i++) {
/*  350: 438 */       if (!((Component)this.components.get(i)).buf.isDirect()) {
/*  351: 439 */         return false;
/*  352:     */       }
/*  353:     */     }
/*  354: 442 */     return true;
/*  355:     */   }
/*  356:     */   
/*  357:     */   public boolean hasArray()
/*  358:     */   {
/*  359: 447 */     if (this.components.size() == 1) {
/*  360: 448 */       return ((Component)this.components.get(0)).buf.hasArray();
/*  361:     */     }
/*  362: 450 */     return false;
/*  363:     */   }
/*  364:     */   
/*  365:     */   public byte[] array()
/*  366:     */   {
/*  367: 455 */     if (this.components.size() == 1) {
/*  368: 456 */       return ((Component)this.components.get(0)).buf.array();
/*  369:     */     }
/*  370: 458 */     throw new UnsupportedOperationException();
/*  371:     */   }
/*  372:     */   
/*  373:     */   public int arrayOffset()
/*  374:     */   {
/*  375: 463 */     if (this.components.size() == 1) {
/*  376: 464 */       return ((Component)this.components.get(0)).buf.arrayOffset();
/*  377:     */     }
/*  378: 466 */     throw new UnsupportedOperationException();
/*  379:     */   }
/*  380:     */   
/*  381:     */   public boolean hasMemoryAddress()
/*  382:     */   {
/*  383: 471 */     if (this.components.size() == 1) {
/*  384: 472 */       return ((Component)this.components.get(0)).buf.hasMemoryAddress();
/*  385:     */     }
/*  386: 474 */     return false;
/*  387:     */   }
/*  388:     */   
/*  389:     */   public long memoryAddress()
/*  390:     */   {
/*  391: 479 */     if (this.components.size() == 1) {
/*  392: 480 */       return ((Component)this.components.get(0)).buf.memoryAddress();
/*  393:     */     }
/*  394: 482 */     throw new UnsupportedOperationException();
/*  395:     */   }
/*  396:     */   
/*  397:     */   public int capacity()
/*  398:     */   {
/*  399: 487 */     if (this.components.isEmpty()) {
/*  400: 488 */       return 0;
/*  401:     */     }
/*  402: 490 */     return ((Component)this.components.get(this.components.size() - 1)).endOffset;
/*  403:     */   }
/*  404:     */   
/*  405:     */   public CompositeByteBuf capacity(int newCapacity)
/*  406:     */   {
/*  407: 495 */     ensureAccessible();
/*  408: 496 */     if ((newCapacity < 0) || (newCapacity > maxCapacity())) {
/*  409: 497 */       throw new IllegalArgumentException("newCapacity: " + newCapacity);
/*  410:     */     }
/*  411: 500 */     int oldCapacity = capacity();
/*  412: 501 */     if (newCapacity > oldCapacity)
/*  413:     */     {
/*  414: 502 */       int paddingLength = newCapacity - oldCapacity;
/*  415:     */       
/*  416: 504 */       int nComponents = this.components.size();
/*  417: 505 */       if (nComponents < this.maxNumComponents)
/*  418:     */       {
/*  419: 506 */         ByteBuf padding = allocBuffer(paddingLength);
/*  420: 507 */         padding.setIndex(0, paddingLength);
/*  421: 508 */         addComponent0(this.components.size(), padding);
/*  422:     */       }
/*  423:     */       else
/*  424:     */       {
/*  425: 510 */         ByteBuf padding = allocBuffer(paddingLength);
/*  426: 511 */         padding.setIndex(0, paddingLength);
/*  427:     */         
/*  428:     */ 
/*  429: 514 */         addComponent0(this.components.size(), padding);
/*  430: 515 */         consolidateIfNeeded();
/*  431:     */       }
/*  432:     */     }
/*  433: 517 */     else if (newCapacity < oldCapacity)
/*  434:     */     {
/*  435: 518 */       int bytesToTrim = oldCapacity - newCapacity;
/*  436: 519 */       for (ListIterator<Component> i = this.components.listIterator(this.components.size()); i.hasPrevious();)
/*  437:     */       {
/*  438: 520 */         Component c = (Component)i.previous();
/*  439: 521 */         if (bytesToTrim >= c.length)
/*  440:     */         {
/*  441: 522 */           bytesToTrim -= c.length;
/*  442: 523 */           i.remove();
/*  443:     */         }
/*  444:     */         else
/*  445:     */         {
/*  446: 528 */           Component newC = new Component(c.buf.slice(0, c.length - bytesToTrim));
/*  447: 529 */           newC.offset = c.offset;
/*  448: 530 */           newC.endOffset = (newC.offset + newC.length);
/*  449: 531 */           i.set(newC);
/*  450:     */         }
/*  451:     */       }
/*  452: 535 */       if (readerIndex() > newCapacity) {
/*  453: 536 */         setIndex(newCapacity, newCapacity);
/*  454: 537 */       } else if (writerIndex() > newCapacity) {
/*  455: 538 */         writerIndex(newCapacity);
/*  456:     */       }
/*  457:     */     }
/*  458: 541 */     return this;
/*  459:     */   }
/*  460:     */   
/*  461:     */   public ByteBufAllocator alloc()
/*  462:     */   {
/*  463: 546 */     return this.alloc;
/*  464:     */   }
/*  465:     */   
/*  466:     */   public ByteOrder order()
/*  467:     */   {
/*  468: 551 */     return ByteOrder.BIG_ENDIAN;
/*  469:     */   }
/*  470:     */   
/*  471:     */   public int numComponents()
/*  472:     */   {
/*  473: 558 */     return this.components.size();
/*  474:     */   }
/*  475:     */   
/*  476:     */   public int maxNumComponents()
/*  477:     */   {
/*  478: 565 */     return this.maxNumComponents;
/*  479:     */   }
/*  480:     */   
/*  481:     */   public int toComponentIndex(int offset)
/*  482:     */   {
/*  483: 572 */     checkIndex(offset);
/*  484:     */     
/*  485: 574 */     int low = 0;
/*  486: 574 */     for (int high = this.components.size(); low <= high;)
/*  487:     */     {
/*  488: 575 */       int mid = low + high >>> 1;
/*  489: 576 */       Component c = (Component)this.components.get(mid);
/*  490: 577 */       if (offset >= c.endOffset) {
/*  491: 578 */         low = mid + 1;
/*  492: 579 */       } else if (offset < c.offset) {
/*  493: 580 */         high = mid - 1;
/*  494:     */       } else {
/*  495: 582 */         return mid;
/*  496:     */       }
/*  497:     */     }
/*  498: 586 */     throw new Error("should not reach here");
/*  499:     */   }
/*  500:     */   
/*  501:     */   public int toByteIndex(int cIndex)
/*  502:     */   {
/*  503: 590 */     checkComponentIndex(cIndex);
/*  504: 591 */     return ((Component)this.components.get(cIndex)).offset;
/*  505:     */   }
/*  506:     */   
/*  507:     */   public byte getByte(int index)
/*  508:     */   {
/*  509: 596 */     return _getByte(index);
/*  510:     */   }
/*  511:     */   
/*  512:     */   protected byte _getByte(int index)
/*  513:     */   {
/*  514: 601 */     Component c = findComponent(index);
/*  515: 602 */     return c.buf.getByte(index - c.offset);
/*  516:     */   }
/*  517:     */   
/*  518:     */   protected short _getShort(int index)
/*  519:     */   {
/*  520: 607 */     Component c = findComponent(index);
/*  521: 608 */     if (index + 2 <= c.endOffset) {
/*  522: 609 */       return c.buf.getShort(index - c.offset);
/*  523:     */     }
/*  524: 610 */     if (order() == ByteOrder.BIG_ENDIAN) {
/*  525: 611 */       return (short)((_getByte(index) & 0xFF) << 8 | _getByte(index + 1) & 0xFF);
/*  526:     */     }
/*  527: 613 */     return (short)(_getByte(index) & 0xFF | (_getByte(index + 1) & 0xFF) << 8);
/*  528:     */   }
/*  529:     */   
/*  530:     */   protected int _getUnsignedMedium(int index)
/*  531:     */   {
/*  532: 619 */     Component c = findComponent(index);
/*  533: 620 */     if (index + 3 <= c.endOffset) {
/*  534: 621 */       return c.buf.getUnsignedMedium(index - c.offset);
/*  535:     */     }
/*  536: 622 */     if (order() == ByteOrder.BIG_ENDIAN) {
/*  537: 623 */       return (_getShort(index) & 0xFFFF) << 8 | _getByte(index + 2) & 0xFF;
/*  538:     */     }
/*  539: 625 */     return _getShort(index) & 0xFFFF | (_getByte(index + 2) & 0xFF) << 16;
/*  540:     */   }
/*  541:     */   
/*  542:     */   protected int _getInt(int index)
/*  543:     */   {
/*  544: 631 */     Component c = findComponent(index);
/*  545: 632 */     if (index + 4 <= c.endOffset) {
/*  546: 633 */       return c.buf.getInt(index - c.offset);
/*  547:     */     }
/*  548: 634 */     if (order() == ByteOrder.BIG_ENDIAN) {
/*  549: 635 */       return (_getShort(index) & 0xFFFF) << 16 | _getShort(index + 2) & 0xFFFF;
/*  550:     */     }
/*  551: 637 */     return _getShort(index) & 0xFFFF | (_getShort(index + 2) & 0xFFFF) << 16;
/*  552:     */   }
/*  553:     */   
/*  554:     */   protected long _getLong(int index)
/*  555:     */   {
/*  556: 643 */     Component c = findComponent(index);
/*  557: 644 */     if (index + 8 <= c.endOffset) {
/*  558: 645 */       return c.buf.getLong(index - c.offset);
/*  559:     */     }
/*  560: 646 */     if (order() == ByteOrder.BIG_ENDIAN) {
/*  561: 647 */       return (_getInt(index) & 0xFFFFFFFF) << 32 | _getInt(index + 4) & 0xFFFFFFFF;
/*  562:     */     }
/*  563: 649 */     return _getInt(index) & 0xFFFFFFFF | (_getInt(index + 4) & 0xFFFFFFFF) << 32;
/*  564:     */   }
/*  565:     */   
/*  566:     */   public CompositeByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/*  567:     */   {
/*  568: 655 */     checkDstIndex(index, length, dstIndex, dst.length);
/*  569: 656 */     if (length == 0) {
/*  570: 657 */       return this;
/*  571:     */     }
/*  572: 660 */     int i = toComponentIndex(index);
/*  573: 661 */     while (length > 0)
/*  574:     */     {
/*  575: 662 */       Component c = (Component)this.components.get(i);
/*  576: 663 */       ByteBuf s = c.buf;
/*  577: 664 */       int adjustment = c.offset;
/*  578: 665 */       int localLength = Math.min(length, s.capacity() - (index - adjustment));
/*  579: 666 */       s.getBytes(index - adjustment, dst, dstIndex, localLength);
/*  580: 667 */       index += localLength;
/*  581: 668 */       dstIndex += localLength;
/*  582: 669 */       length -= localLength;
/*  583: 670 */       i++;
/*  584:     */     }
/*  585: 672 */     return this;
/*  586:     */   }
/*  587:     */   
/*  588:     */   public CompositeByteBuf getBytes(int index, ByteBuffer dst)
/*  589:     */   {
/*  590: 677 */     int limit = dst.limit();
/*  591: 678 */     int length = dst.remaining();
/*  592:     */     
/*  593: 680 */     checkIndex(index, length);
/*  594: 681 */     if (length == 0) {
/*  595: 682 */       return this;
/*  596:     */     }
/*  597: 685 */     int i = toComponentIndex(index);
/*  598:     */     try
/*  599:     */     {
/*  600: 687 */       while (length > 0)
/*  601:     */       {
/*  602: 688 */         Component c = (Component)this.components.get(i);
/*  603: 689 */         ByteBuf s = c.buf;
/*  604: 690 */         int adjustment = c.offset;
/*  605: 691 */         int localLength = Math.min(length, s.capacity() - (index - adjustment));
/*  606: 692 */         dst.limit(dst.position() + localLength);
/*  607: 693 */         s.getBytes(index - adjustment, dst);
/*  608: 694 */         index += localLength;
/*  609: 695 */         length -= localLength;
/*  610: 696 */         i++;
/*  611:     */       }
/*  612:     */     }
/*  613:     */     finally
/*  614:     */     {
/*  615: 699 */       dst.limit(limit);
/*  616:     */     }
/*  617: 701 */     return this;
/*  618:     */   }
/*  619:     */   
/*  620:     */   public CompositeByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/*  621:     */   {
/*  622: 706 */     checkDstIndex(index, length, dstIndex, dst.capacity());
/*  623: 707 */     if (length == 0) {
/*  624: 708 */       return this;
/*  625:     */     }
/*  626: 711 */     int i = toComponentIndex(index);
/*  627: 712 */     while (length > 0)
/*  628:     */     {
/*  629: 713 */       Component c = (Component)this.components.get(i);
/*  630: 714 */       ByteBuf s = c.buf;
/*  631: 715 */       int adjustment = c.offset;
/*  632: 716 */       int localLength = Math.min(length, s.capacity() - (index - adjustment));
/*  633: 717 */       s.getBytes(index - adjustment, dst, dstIndex, localLength);
/*  634: 718 */       index += localLength;
/*  635: 719 */       dstIndex += localLength;
/*  636: 720 */       length -= localLength;
/*  637: 721 */       i++;
/*  638:     */     }
/*  639: 723 */     return this;
/*  640:     */   }
/*  641:     */   
/*  642:     */   public int getBytes(int index, GatheringByteChannel out, int length)
/*  643:     */     throws IOException
/*  644:     */   {
/*  645: 729 */     int count = nioBufferCount();
/*  646: 730 */     if (count == 1) {
/*  647: 731 */       return out.write(internalNioBuffer(index, length));
/*  648:     */     }
/*  649: 733 */     long writtenBytes = out.write(nioBuffers(index, length));
/*  650: 734 */     if (writtenBytes > 2147483647L) {
/*  651: 735 */       return 2147483647;
/*  652:     */     }
/*  653: 737 */     return (int)writtenBytes;
/*  654:     */   }
/*  655:     */   
/*  656:     */   public CompositeByteBuf getBytes(int index, OutputStream out, int length)
/*  657:     */     throws IOException
/*  658:     */   {
/*  659: 744 */     checkIndex(index, length);
/*  660: 745 */     if (length == 0) {
/*  661: 746 */       return this;
/*  662:     */     }
/*  663: 749 */     int i = toComponentIndex(index);
/*  664: 750 */     while (length > 0)
/*  665:     */     {
/*  666: 751 */       Component c = (Component)this.components.get(i);
/*  667: 752 */       ByteBuf s = c.buf;
/*  668: 753 */       int adjustment = c.offset;
/*  669: 754 */       int localLength = Math.min(length, s.capacity() - (index - adjustment));
/*  670: 755 */       s.getBytes(index - adjustment, out, localLength);
/*  671: 756 */       index += localLength;
/*  672: 757 */       length -= localLength;
/*  673: 758 */       i++;
/*  674:     */     }
/*  675: 760 */     return this;
/*  676:     */   }
/*  677:     */   
/*  678:     */   public CompositeByteBuf setByte(int index, int value)
/*  679:     */   {
/*  680: 765 */     Component c = findComponent(index);
/*  681: 766 */     c.buf.setByte(index - c.offset, value);
/*  682: 767 */     return this;
/*  683:     */   }
/*  684:     */   
/*  685:     */   protected void _setByte(int index, int value)
/*  686:     */   {
/*  687: 772 */     setByte(index, value);
/*  688:     */   }
/*  689:     */   
/*  690:     */   public CompositeByteBuf setShort(int index, int value)
/*  691:     */   {
/*  692: 777 */     return (CompositeByteBuf)super.setShort(index, value);
/*  693:     */   }
/*  694:     */   
/*  695:     */   protected void _setShort(int index, int value)
/*  696:     */   {
/*  697: 782 */     Component c = findComponent(index);
/*  698: 783 */     if (index + 2 <= c.endOffset)
/*  699:     */     {
/*  700: 784 */       c.buf.setShort(index - c.offset, value);
/*  701:     */     }
/*  702: 785 */     else if (order() == ByteOrder.BIG_ENDIAN)
/*  703:     */     {
/*  704: 786 */       _setByte(index, (byte)(value >>> 8));
/*  705: 787 */       _setByte(index + 1, (byte)value);
/*  706:     */     }
/*  707:     */     else
/*  708:     */     {
/*  709: 789 */       _setByte(index, (byte)value);
/*  710: 790 */       _setByte(index + 1, (byte)(value >>> 8));
/*  711:     */     }
/*  712:     */   }
/*  713:     */   
/*  714:     */   public CompositeByteBuf setMedium(int index, int value)
/*  715:     */   {
/*  716: 796 */     return (CompositeByteBuf)super.setMedium(index, value);
/*  717:     */   }
/*  718:     */   
/*  719:     */   protected void _setMedium(int index, int value)
/*  720:     */   {
/*  721: 801 */     Component c = findComponent(index);
/*  722: 802 */     if (index + 3 <= c.endOffset)
/*  723:     */     {
/*  724: 803 */       c.buf.setMedium(index - c.offset, value);
/*  725:     */     }
/*  726: 804 */     else if (order() == ByteOrder.BIG_ENDIAN)
/*  727:     */     {
/*  728: 805 */       _setShort(index, (short)(value >> 8));
/*  729: 806 */       _setByte(index + 2, (byte)value);
/*  730:     */     }
/*  731:     */     else
/*  732:     */     {
/*  733: 808 */       _setShort(index, (short)value);
/*  734: 809 */       _setByte(index + 2, (byte)(value >>> 16));
/*  735:     */     }
/*  736:     */   }
/*  737:     */   
/*  738:     */   public CompositeByteBuf setInt(int index, int value)
/*  739:     */   {
/*  740: 815 */     return (CompositeByteBuf)super.setInt(index, value);
/*  741:     */   }
/*  742:     */   
/*  743:     */   protected void _setInt(int index, int value)
/*  744:     */   {
/*  745: 820 */     Component c = findComponent(index);
/*  746: 821 */     if (index + 4 <= c.endOffset)
/*  747:     */     {
/*  748: 822 */       c.buf.setInt(index - c.offset, value);
/*  749:     */     }
/*  750: 823 */     else if (order() == ByteOrder.BIG_ENDIAN)
/*  751:     */     {
/*  752: 824 */       _setShort(index, (short)(value >>> 16));
/*  753: 825 */       _setShort(index + 2, (short)value);
/*  754:     */     }
/*  755:     */     else
/*  756:     */     {
/*  757: 827 */       _setShort(index, (short)value);
/*  758: 828 */       _setShort(index + 2, (short)(value >>> 16));
/*  759:     */     }
/*  760:     */   }
/*  761:     */   
/*  762:     */   public CompositeByteBuf setLong(int index, long value)
/*  763:     */   {
/*  764: 834 */     return (CompositeByteBuf)super.setLong(index, value);
/*  765:     */   }
/*  766:     */   
/*  767:     */   protected void _setLong(int index, long value)
/*  768:     */   {
/*  769: 839 */     Component c = findComponent(index);
/*  770: 840 */     if (index + 8 <= c.endOffset)
/*  771:     */     {
/*  772: 841 */       c.buf.setLong(index - c.offset, value);
/*  773:     */     }
/*  774: 842 */     else if (order() == ByteOrder.BIG_ENDIAN)
/*  775:     */     {
/*  776: 843 */       _setInt(index, (int)(value >>> 32));
/*  777: 844 */       _setInt(index + 4, (int)value);
/*  778:     */     }
/*  779:     */     else
/*  780:     */     {
/*  781: 846 */       _setInt(index, (int)value);
/*  782: 847 */       _setInt(index + 4, (int)(value >>> 32));
/*  783:     */     }
/*  784:     */   }
/*  785:     */   
/*  786:     */   public CompositeByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/*  787:     */   {
/*  788: 853 */     checkSrcIndex(index, length, srcIndex, src.length);
/*  789: 854 */     if (length == 0) {
/*  790: 855 */       return this;
/*  791:     */     }
/*  792: 858 */     int i = toComponentIndex(index);
/*  793: 859 */     while (length > 0)
/*  794:     */     {
/*  795: 860 */       Component c = (Component)this.components.get(i);
/*  796: 861 */       ByteBuf s = c.buf;
/*  797: 862 */       int adjustment = c.offset;
/*  798: 863 */       int localLength = Math.min(length, s.capacity() - (index - adjustment));
/*  799: 864 */       s.setBytes(index - adjustment, src, srcIndex, localLength);
/*  800: 865 */       index += localLength;
/*  801: 866 */       srcIndex += localLength;
/*  802: 867 */       length -= localLength;
/*  803: 868 */       i++;
/*  804:     */     }
/*  805: 870 */     return this;
/*  806:     */   }
/*  807:     */   
/*  808:     */   public CompositeByteBuf setBytes(int index, ByteBuffer src)
/*  809:     */   {
/*  810: 875 */     int limit = src.limit();
/*  811: 876 */     int length = src.remaining();
/*  812:     */     
/*  813: 878 */     checkIndex(index, length);
/*  814: 879 */     if (length == 0) {
/*  815: 880 */       return this;
/*  816:     */     }
/*  817: 883 */     int i = toComponentIndex(index);
/*  818:     */     try
/*  819:     */     {
/*  820: 885 */       while (length > 0)
/*  821:     */       {
/*  822: 886 */         Component c = (Component)this.components.get(i);
/*  823: 887 */         ByteBuf s = c.buf;
/*  824: 888 */         int adjustment = c.offset;
/*  825: 889 */         int localLength = Math.min(length, s.capacity() - (index - adjustment));
/*  826: 890 */         src.limit(src.position() + localLength);
/*  827: 891 */         s.setBytes(index - adjustment, src);
/*  828: 892 */         index += localLength;
/*  829: 893 */         length -= localLength;
/*  830: 894 */         i++;
/*  831:     */       }
/*  832:     */     }
/*  833:     */     finally
/*  834:     */     {
/*  835: 897 */       src.limit(limit);
/*  836:     */     }
/*  837: 899 */     return this;
/*  838:     */   }
/*  839:     */   
/*  840:     */   public CompositeByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/*  841:     */   {
/*  842: 904 */     checkSrcIndex(index, length, srcIndex, src.capacity());
/*  843: 905 */     if (length == 0) {
/*  844: 906 */       return this;
/*  845:     */     }
/*  846: 909 */     int i = toComponentIndex(index);
/*  847: 910 */     while (length > 0)
/*  848:     */     {
/*  849: 911 */       Component c = (Component)this.components.get(i);
/*  850: 912 */       ByteBuf s = c.buf;
/*  851: 913 */       int adjustment = c.offset;
/*  852: 914 */       int localLength = Math.min(length, s.capacity() - (index - adjustment));
/*  853: 915 */       s.setBytes(index - adjustment, src, srcIndex, localLength);
/*  854: 916 */       index += localLength;
/*  855: 917 */       srcIndex += localLength;
/*  856: 918 */       length -= localLength;
/*  857: 919 */       i++;
/*  858:     */     }
/*  859: 921 */     return this;
/*  860:     */   }
/*  861:     */   
/*  862:     */   public int setBytes(int index, InputStream in, int length)
/*  863:     */     throws IOException
/*  864:     */   {
/*  865: 926 */     checkIndex(index, length);
/*  866: 927 */     if (length == 0) {
/*  867: 928 */       return in.read(EmptyArrays.EMPTY_BYTES);
/*  868:     */     }
/*  869: 931 */     int i = toComponentIndex(index);
/*  870: 932 */     int readBytes = 0;
/*  871:     */     do
/*  872:     */     {
/*  873: 935 */       Component c = (Component)this.components.get(i);
/*  874: 936 */       ByteBuf s = c.buf;
/*  875: 937 */       int adjustment = c.offset;
/*  876: 938 */       int localLength = Math.min(length, s.capacity() - (index - adjustment));
/*  877: 939 */       int localReadBytes = s.setBytes(index - adjustment, in, localLength);
/*  878: 940 */       if (localReadBytes < 0)
/*  879:     */       {
/*  880: 941 */         if (readBytes != 0) {
/*  881:     */           break;
/*  882:     */         }
/*  883: 942 */         return -1;
/*  884:     */       }
/*  885: 948 */       if (localReadBytes == localLength)
/*  886:     */       {
/*  887: 949 */         index += localLength;
/*  888: 950 */         length -= localLength;
/*  889: 951 */         readBytes += localLength;
/*  890: 952 */         i++;
/*  891:     */       }
/*  892:     */       else
/*  893:     */       {
/*  894: 954 */         index += localReadBytes;
/*  895: 955 */         length -= localReadBytes;
/*  896: 956 */         readBytes += localReadBytes;
/*  897:     */       }
/*  898: 958 */     } while (length > 0);
/*  899: 960 */     return readBytes;
/*  900:     */   }
/*  901:     */   
/*  902:     */   public int setBytes(int index, ScatteringByteChannel in, int length)
/*  903:     */     throws IOException
/*  904:     */   {
/*  905: 965 */     checkIndex(index, length);
/*  906: 966 */     if (length == 0) {
/*  907: 967 */       return in.read(FULL_BYTEBUFFER);
/*  908:     */     }
/*  909: 970 */     int i = toComponentIndex(index);
/*  910: 971 */     int readBytes = 0;
/*  911:     */     do
/*  912:     */     {
/*  913: 973 */       Component c = (Component)this.components.get(i);
/*  914: 974 */       ByteBuf s = c.buf;
/*  915: 975 */       int adjustment = c.offset;
/*  916: 976 */       int localLength = Math.min(length, s.capacity() - (index - adjustment));
/*  917: 977 */       int localReadBytes = s.setBytes(index - adjustment, in, localLength);
/*  918: 979 */       if (localReadBytes == 0) {
/*  919:     */         break;
/*  920:     */       }
/*  921: 983 */       if (localReadBytes < 0)
/*  922:     */       {
/*  923: 984 */         if (readBytes != 0) {
/*  924:     */           break;
/*  925:     */         }
/*  926: 985 */         return -1;
/*  927:     */       }
/*  928: 991 */       if (localReadBytes == localLength)
/*  929:     */       {
/*  930: 992 */         index += localLength;
/*  931: 993 */         length -= localLength;
/*  932: 994 */         readBytes += localLength;
/*  933: 995 */         i++;
/*  934:     */       }
/*  935:     */       else
/*  936:     */       {
/*  937: 997 */         index += localReadBytes;
/*  938: 998 */         length -= localReadBytes;
/*  939: 999 */         readBytes += localReadBytes;
/*  940:     */       }
/*  941:1001 */     } while (length > 0);
/*  942:1003 */     return readBytes;
/*  943:     */   }
/*  944:     */   
/*  945:     */   public ByteBuf copy(int index, int length)
/*  946:     */   {
/*  947:1008 */     checkIndex(index, length);
/*  948:1009 */     ByteBuf dst = Unpooled.buffer(length);
/*  949:1010 */     if (length != 0) {
/*  950:1011 */       copyTo(index, length, toComponentIndex(index), dst);
/*  951:     */     }
/*  952:1013 */     return dst;
/*  953:     */   }
/*  954:     */   
/*  955:     */   private void copyTo(int index, int length, int componentId, ByteBuf dst)
/*  956:     */   {
/*  957:1017 */     int dstIndex = 0;
/*  958:1018 */     int i = componentId;
/*  959:1020 */     while (length > 0)
/*  960:     */     {
/*  961:1021 */       Component c = (Component)this.components.get(i);
/*  962:1022 */       ByteBuf s = c.buf;
/*  963:1023 */       int adjustment = c.offset;
/*  964:1024 */       int localLength = Math.min(length, s.capacity() - (index - adjustment));
/*  965:1025 */       s.getBytes(index - adjustment, dst, dstIndex, localLength);
/*  966:1026 */       index += localLength;
/*  967:1027 */       dstIndex += localLength;
/*  968:1028 */       length -= localLength;
/*  969:1029 */       i++;
/*  970:     */     }
/*  971:1032 */     dst.writerIndex(dst.capacity());
/*  972:     */   }
/*  973:     */   
/*  974:     */   public ByteBuf component(int cIndex)
/*  975:     */   {
/*  976:1042 */     return internalComponent(cIndex).duplicate();
/*  977:     */   }
/*  978:     */   
/*  979:     */   public ByteBuf componentAtOffset(int offset)
/*  980:     */   {
/*  981:1052 */     return internalComponentAtOffset(offset).duplicate();
/*  982:     */   }
/*  983:     */   
/*  984:     */   public ByteBuf internalComponent(int cIndex)
/*  985:     */   {
/*  986:1062 */     checkComponentIndex(cIndex);
/*  987:1063 */     return ((Component)this.components.get(cIndex)).buf;
/*  988:     */   }
/*  989:     */   
/*  990:     */   public ByteBuf internalComponentAtOffset(int offset)
/*  991:     */   {
/*  992:1073 */     return findComponent(offset).buf;
/*  993:     */   }
/*  994:     */   
/*  995:     */   private Component findComponent(int offset)
/*  996:     */   {
/*  997:1077 */     checkIndex(offset);
/*  998:     */     
/*  999:1079 */     int low = 0;
/* 1000:1079 */     for (int high = this.components.size(); low <= high;)
/* 1001:     */     {
/* 1002:1080 */       int mid = low + high >>> 1;
/* 1003:1081 */       Component c = (Component)this.components.get(mid);
/* 1004:1082 */       if (offset >= c.endOffset) {
/* 1005:1083 */         low = mid + 1;
/* 1006:1084 */       } else if (offset < c.offset) {
/* 1007:1085 */         high = mid - 1;
/* 1008:     */       } else {
/* 1009:1087 */         return c;
/* 1010:     */       }
/* 1011:     */     }
/* 1012:1091 */     throw new Error("should not reach here");
/* 1013:     */   }
/* 1014:     */   
/* 1015:     */   public int nioBufferCount()
/* 1016:     */   {
/* 1017:1096 */     if (this.components.size() == 1) {
/* 1018:1097 */       return ((Component)this.components.get(0)).buf.nioBufferCount();
/* 1019:     */     }
/* 1020:1099 */     int count = 0;
/* 1021:1100 */     int componentsCount = this.components.size();
/* 1022:1102 */     for (int i = 0; i < componentsCount; i++)
/* 1023:     */     {
/* 1024:1103 */       Component c = (Component)this.components.get(i);
/* 1025:1104 */       count += c.buf.nioBufferCount();
/* 1026:     */     }
/* 1027:1106 */     return count;
/* 1028:     */   }
/* 1029:     */   
/* 1030:     */   public ByteBuffer internalNioBuffer(int index, int length)
/* 1031:     */   {
/* 1032:1112 */     if (this.components.size() == 1) {
/* 1033:1113 */       return ((Component)this.components.get(0)).buf.internalNioBuffer(index, length);
/* 1034:     */     }
/* 1035:1115 */     throw new UnsupportedOperationException();
/* 1036:     */   }
/* 1037:     */   
/* 1038:     */   public ByteBuffer nioBuffer(int index, int length)
/* 1039:     */   {
/* 1040:1120 */     if (this.components.size() == 1)
/* 1041:     */     {
/* 1042:1121 */       ByteBuf buf = ((Component)this.components.get(0)).buf;
/* 1043:1122 */       if (buf.nioBufferCount() == 1) {
/* 1044:1123 */         return ((Component)this.components.get(0)).buf.nioBuffer(index, length);
/* 1045:     */       }
/* 1046:     */     }
/* 1047:1126 */     ByteBuffer merged = ByteBuffer.allocate(length).order(order());
/* 1048:1127 */     ByteBuffer[] buffers = nioBuffers(index, length);
/* 1049:1130 */     for (int i = 0; i < buffers.length; i++) {
/* 1050:1131 */       merged.put(buffers[i]);
/* 1051:     */     }
/* 1052:1134 */     merged.flip();
/* 1053:1135 */     return merged;
/* 1054:     */   }
/* 1055:     */   
/* 1056:     */   public ByteBuffer[] nioBuffers(int index, int length)
/* 1057:     */   {
/* 1058:1140 */     checkIndex(index, length);
/* 1059:1141 */     if (length == 0) {
/* 1060:1142 */       return EmptyArrays.EMPTY_BYTE_BUFFERS;
/* 1061:     */     }
/* 1062:1145 */     List<ByteBuffer> buffers = new ArrayList(this.components.size());
/* 1063:1146 */     int i = toComponentIndex(index);
/* 1064:1147 */     while (length > 0)
/* 1065:     */     {
/* 1066:1148 */       Component c = (Component)this.components.get(i);
/* 1067:1149 */       ByteBuf s = c.buf;
/* 1068:1150 */       int adjustment = c.offset;
/* 1069:1151 */       int localLength = Math.min(length, s.capacity() - (index - adjustment));
/* 1070:1152 */       switch (s.nioBufferCount())
/* 1071:     */       {
/* 1072:     */       case 0: 
/* 1073:1154 */         throw new UnsupportedOperationException();
/* 1074:     */       case 1: 
/* 1075:1156 */         buffers.add(s.nioBuffer(index - adjustment, localLength));
/* 1076:1157 */         break;
/* 1077:     */       default: 
/* 1078:1159 */         Collections.addAll(buffers, s.nioBuffers(index - adjustment, localLength));
/* 1079:     */       }
/* 1080:1162 */       index += localLength;
/* 1081:1163 */       length -= localLength;
/* 1082:1164 */       i++;
/* 1083:     */     }
/* 1084:1167 */     return (ByteBuffer[])buffers.toArray(new ByteBuffer[buffers.size()]);
/* 1085:     */   }
/* 1086:     */   
/* 1087:     */   public CompositeByteBuf consolidate()
/* 1088:     */   {
/* 1089:1174 */     ensureAccessible();
/* 1090:1175 */     int numComponents = numComponents();
/* 1091:1176 */     if (numComponents <= 1) {
/* 1092:1177 */       return this;
/* 1093:     */     }
/* 1094:1180 */     Component last = (Component)this.components.get(numComponents - 1);
/* 1095:1181 */     int capacity = last.endOffset;
/* 1096:1182 */     ByteBuf consolidated = allocBuffer(capacity);
/* 1097:1184 */     for (int i = 0; i < numComponents; i++)
/* 1098:     */     {
/* 1099:1185 */       Component c = (Component)this.components.get(i);
/* 1100:1186 */       ByteBuf b = c.buf;
/* 1101:1187 */       consolidated.writeBytes(b);
/* 1102:1188 */       c.freeIfNecessary();
/* 1103:     */     }
/* 1104:1191 */     this.components.clear();
/* 1105:1192 */     this.components.add(new Component(consolidated));
/* 1106:1193 */     updateComponentOffsets(0);
/* 1107:1194 */     return this;
/* 1108:     */   }
/* 1109:     */   
/* 1110:     */   public CompositeByteBuf consolidate(int cIndex, int numComponents)
/* 1111:     */   {
/* 1112:1204 */     checkComponentIndex(cIndex, numComponents);
/* 1113:1205 */     if (numComponents <= 1) {
/* 1114:1206 */       return this;
/* 1115:     */     }
/* 1116:1209 */     int endCIndex = cIndex + numComponents;
/* 1117:1210 */     Component last = (Component)this.components.get(endCIndex - 1);
/* 1118:1211 */     int capacity = last.endOffset - ((Component)this.components.get(cIndex)).offset;
/* 1119:1212 */     ByteBuf consolidated = allocBuffer(capacity);
/* 1120:1214 */     for (int i = cIndex; i < endCIndex; i++)
/* 1121:     */     {
/* 1122:1215 */       Component c = (Component)this.components.get(i);
/* 1123:1216 */       ByteBuf b = c.buf;
/* 1124:1217 */       consolidated.writeBytes(b);
/* 1125:1218 */       c.freeIfNecessary();
/* 1126:     */     }
/* 1127:1221 */     this.components.subList(cIndex + 1, endCIndex).clear();
/* 1128:1222 */     this.components.set(cIndex, new Component(consolidated));
/* 1129:1223 */     updateComponentOffsets(cIndex);
/* 1130:1224 */     return this;
/* 1131:     */   }
/* 1132:     */   
/* 1133:     */   public CompositeByteBuf discardReadComponents()
/* 1134:     */   {
/* 1135:1231 */     ensureAccessible();
/* 1136:1232 */     int readerIndex = readerIndex();
/* 1137:1233 */     if (readerIndex == 0) {
/* 1138:1234 */       return this;
/* 1139:     */     }
/* 1140:1238 */     int writerIndex = writerIndex();
/* 1141:1239 */     if ((readerIndex == writerIndex) && (writerIndex == capacity()))
/* 1142:     */     {
/* 1143:1240 */       for (Component c : this.components) {
/* 1144:1241 */         c.freeIfNecessary();
/* 1145:     */       }
/* 1146:1243 */       this.components.clear();
/* 1147:1244 */       setIndex(0, 0);
/* 1148:1245 */       adjustMarkers(readerIndex);
/* 1149:1246 */       return this;
/* 1150:     */     }
/* 1151:1250 */     int firstComponentId = toComponentIndex(readerIndex);
/* 1152:1251 */     for (int i = 0; i < firstComponentId; i++) {
/* 1153:1252 */       ((Component)this.components.get(i)).freeIfNecessary();
/* 1154:     */     }
/* 1155:1254 */     this.components.subList(0, firstComponentId).clear();
/* 1156:     */     
/* 1157:     */ 
/* 1158:1257 */     Component first = (Component)this.components.get(0);
/* 1159:1258 */     int offset = first.offset;
/* 1160:1259 */     updateComponentOffsets(0);
/* 1161:1260 */     setIndex(readerIndex - offset, writerIndex - offset);
/* 1162:1261 */     adjustMarkers(offset);
/* 1163:1262 */     return this;
/* 1164:     */   }
/* 1165:     */   
/* 1166:     */   public CompositeByteBuf discardReadBytes()
/* 1167:     */   {
/* 1168:1267 */     ensureAccessible();
/* 1169:1268 */     int readerIndex = readerIndex();
/* 1170:1269 */     if (readerIndex == 0) {
/* 1171:1270 */       return this;
/* 1172:     */     }
/* 1173:1274 */     int writerIndex = writerIndex();
/* 1174:1275 */     if ((readerIndex == writerIndex) && (writerIndex == capacity()))
/* 1175:     */     {
/* 1176:1276 */       for (Component c : this.components) {
/* 1177:1277 */         c.freeIfNecessary();
/* 1178:     */       }
/* 1179:1279 */       this.components.clear();
/* 1180:1280 */       setIndex(0, 0);
/* 1181:1281 */       adjustMarkers(readerIndex);
/* 1182:1282 */       return this;
/* 1183:     */     }
/* 1184:1286 */     int firstComponentId = toComponentIndex(readerIndex);
/* 1185:1287 */     for (int i = 0; i < firstComponentId; i++) {
/* 1186:1288 */       ((Component)this.components.get(i)).freeIfNecessary();
/* 1187:     */     }
/* 1188:1290 */     this.components.subList(0, firstComponentId).clear();
/* 1189:     */     
/* 1190:     */ 
/* 1191:1293 */     Component c = (Component)this.components.get(0);
/* 1192:1294 */     int adjustment = readerIndex - c.offset;
/* 1193:1295 */     if (adjustment == c.length)
/* 1194:     */     {
/* 1195:1297 */       this.components.remove(0);
/* 1196:     */     }
/* 1197:     */     else
/* 1198:     */     {
/* 1199:1299 */       Component newC = new Component(c.buf.slice(adjustment, c.length - adjustment));
/* 1200:1300 */       this.components.set(0, newC);
/* 1201:     */     }
/* 1202:1304 */     updateComponentOffsets(0);
/* 1203:1305 */     setIndex(0, writerIndex - readerIndex);
/* 1204:1306 */     adjustMarkers(readerIndex);
/* 1205:1307 */     return this;
/* 1206:     */   }
/* 1207:     */   
/* 1208:     */   private ByteBuf allocBuffer(int capacity)
/* 1209:     */   {
/* 1210:1311 */     if (this.direct) {
/* 1211:1312 */       return alloc().directBuffer(capacity);
/* 1212:     */     }
/* 1213:1314 */     return alloc().heapBuffer(capacity);
/* 1214:     */   }
/* 1215:     */   
/* 1216:     */   public String toString()
/* 1217:     */   {
/* 1218:1319 */     String result = super.toString();
/* 1219:1320 */     result = result.substring(0, result.length() - 1);
/* 1220:1321 */     return result + ", components=" + this.components.size() + ')';
/* 1221:     */   }
/* 1222:     */   
/* 1223:     */   private final class Component
/* 1224:     */   {
/* 1225:     */     final ByteBuf buf;
/* 1226:     */     final int length;
/* 1227:     */     int offset;
/* 1228:     */     int endOffset;
/* 1229:     */     
/* 1230:     */     Component(ByteBuf buf)
/* 1231:     */     {
/* 1232:1331 */       this.buf = buf;
/* 1233:1332 */       this.length = buf.readableBytes();
/* 1234:     */     }
/* 1235:     */     
/* 1236:     */     void freeIfNecessary()
/* 1237:     */     {
/* 1238:1337 */       this.buf.release();
/* 1239:     */     }
/* 1240:     */   }
/* 1241:     */   
/* 1242:     */   public CompositeByteBuf readerIndex(int readerIndex)
/* 1243:     */   {
/* 1244:1343 */     return (CompositeByteBuf)super.readerIndex(readerIndex);
/* 1245:     */   }
/* 1246:     */   
/* 1247:     */   public CompositeByteBuf writerIndex(int writerIndex)
/* 1248:     */   {
/* 1249:1348 */     return (CompositeByteBuf)super.writerIndex(writerIndex);
/* 1250:     */   }
/* 1251:     */   
/* 1252:     */   public CompositeByteBuf setIndex(int readerIndex, int writerIndex)
/* 1253:     */   {
/* 1254:1353 */     return (CompositeByteBuf)super.setIndex(readerIndex, writerIndex);
/* 1255:     */   }
/* 1256:     */   
/* 1257:     */   public CompositeByteBuf clear()
/* 1258:     */   {
/* 1259:1358 */     return (CompositeByteBuf)super.clear();
/* 1260:     */   }
/* 1261:     */   
/* 1262:     */   public CompositeByteBuf markReaderIndex()
/* 1263:     */   {
/* 1264:1363 */     return (CompositeByteBuf)super.markReaderIndex();
/* 1265:     */   }
/* 1266:     */   
/* 1267:     */   public CompositeByteBuf resetReaderIndex()
/* 1268:     */   {
/* 1269:1368 */     return (CompositeByteBuf)super.resetReaderIndex();
/* 1270:     */   }
/* 1271:     */   
/* 1272:     */   public CompositeByteBuf markWriterIndex()
/* 1273:     */   {
/* 1274:1373 */     return (CompositeByteBuf)super.markWriterIndex();
/* 1275:     */   }
/* 1276:     */   
/* 1277:     */   public CompositeByteBuf resetWriterIndex()
/* 1278:     */   {
/* 1279:1378 */     return (CompositeByteBuf)super.resetWriterIndex();
/* 1280:     */   }
/* 1281:     */   
/* 1282:     */   public CompositeByteBuf ensureWritable(int minWritableBytes)
/* 1283:     */   {
/* 1284:1383 */     return (CompositeByteBuf)super.ensureWritable(minWritableBytes);
/* 1285:     */   }
/* 1286:     */   
/* 1287:     */   public CompositeByteBuf getBytes(int index, ByteBuf dst)
/* 1288:     */   {
/* 1289:1388 */     return (CompositeByteBuf)super.getBytes(index, dst);
/* 1290:     */   }
/* 1291:     */   
/* 1292:     */   public CompositeByteBuf getBytes(int index, ByteBuf dst, int length)
/* 1293:     */   {
/* 1294:1393 */     return (CompositeByteBuf)super.getBytes(index, dst, length);
/* 1295:     */   }
/* 1296:     */   
/* 1297:     */   public CompositeByteBuf getBytes(int index, byte[] dst)
/* 1298:     */   {
/* 1299:1398 */     return (CompositeByteBuf)super.getBytes(index, dst);
/* 1300:     */   }
/* 1301:     */   
/* 1302:     */   public CompositeByteBuf setBoolean(int index, boolean value)
/* 1303:     */   {
/* 1304:1403 */     return (CompositeByteBuf)super.setBoolean(index, value);
/* 1305:     */   }
/* 1306:     */   
/* 1307:     */   public CompositeByteBuf setChar(int index, int value)
/* 1308:     */   {
/* 1309:1408 */     return (CompositeByteBuf)super.setChar(index, value);
/* 1310:     */   }
/* 1311:     */   
/* 1312:     */   public CompositeByteBuf setFloat(int index, float value)
/* 1313:     */   {
/* 1314:1413 */     return (CompositeByteBuf)super.setFloat(index, value);
/* 1315:     */   }
/* 1316:     */   
/* 1317:     */   public CompositeByteBuf setDouble(int index, double value)
/* 1318:     */   {
/* 1319:1418 */     return (CompositeByteBuf)super.setDouble(index, value);
/* 1320:     */   }
/* 1321:     */   
/* 1322:     */   public CompositeByteBuf setBytes(int index, ByteBuf src)
/* 1323:     */   {
/* 1324:1423 */     return (CompositeByteBuf)super.setBytes(index, src);
/* 1325:     */   }
/* 1326:     */   
/* 1327:     */   public CompositeByteBuf setBytes(int index, ByteBuf src, int length)
/* 1328:     */   {
/* 1329:1428 */     return (CompositeByteBuf)super.setBytes(index, src, length);
/* 1330:     */   }
/* 1331:     */   
/* 1332:     */   public CompositeByteBuf setBytes(int index, byte[] src)
/* 1333:     */   {
/* 1334:1433 */     return (CompositeByteBuf)super.setBytes(index, src);
/* 1335:     */   }
/* 1336:     */   
/* 1337:     */   public CompositeByteBuf setZero(int index, int length)
/* 1338:     */   {
/* 1339:1438 */     return (CompositeByteBuf)super.setZero(index, length);
/* 1340:     */   }
/* 1341:     */   
/* 1342:     */   public CompositeByteBuf readBytes(ByteBuf dst)
/* 1343:     */   {
/* 1344:1443 */     return (CompositeByteBuf)super.readBytes(dst);
/* 1345:     */   }
/* 1346:     */   
/* 1347:     */   public CompositeByteBuf readBytes(ByteBuf dst, int length)
/* 1348:     */   {
/* 1349:1448 */     return (CompositeByteBuf)super.readBytes(dst, length);
/* 1350:     */   }
/* 1351:     */   
/* 1352:     */   public CompositeByteBuf readBytes(ByteBuf dst, int dstIndex, int length)
/* 1353:     */   {
/* 1354:1453 */     return (CompositeByteBuf)super.readBytes(dst, dstIndex, length);
/* 1355:     */   }
/* 1356:     */   
/* 1357:     */   public CompositeByteBuf readBytes(byte[] dst)
/* 1358:     */   {
/* 1359:1458 */     return (CompositeByteBuf)super.readBytes(dst);
/* 1360:     */   }
/* 1361:     */   
/* 1362:     */   public CompositeByteBuf readBytes(byte[] dst, int dstIndex, int length)
/* 1363:     */   {
/* 1364:1463 */     return (CompositeByteBuf)super.readBytes(dst, dstIndex, length);
/* 1365:     */   }
/* 1366:     */   
/* 1367:     */   public CompositeByteBuf readBytes(ByteBuffer dst)
/* 1368:     */   {
/* 1369:1468 */     return (CompositeByteBuf)super.readBytes(dst);
/* 1370:     */   }
/* 1371:     */   
/* 1372:     */   public CompositeByteBuf readBytes(OutputStream out, int length)
/* 1373:     */     throws IOException
/* 1374:     */   {
/* 1375:1473 */     return (CompositeByteBuf)super.readBytes(out, length);
/* 1376:     */   }
/* 1377:     */   
/* 1378:     */   public CompositeByteBuf skipBytes(int length)
/* 1379:     */   {
/* 1380:1478 */     return (CompositeByteBuf)super.skipBytes(length);
/* 1381:     */   }
/* 1382:     */   
/* 1383:     */   public CompositeByteBuf writeBoolean(boolean value)
/* 1384:     */   {
/* 1385:1483 */     return (CompositeByteBuf)super.writeBoolean(value);
/* 1386:     */   }
/* 1387:     */   
/* 1388:     */   public CompositeByteBuf writeByte(int value)
/* 1389:     */   {
/* 1390:1488 */     return (CompositeByteBuf)super.writeByte(value);
/* 1391:     */   }
/* 1392:     */   
/* 1393:     */   public CompositeByteBuf writeShort(int value)
/* 1394:     */   {
/* 1395:1493 */     return (CompositeByteBuf)super.writeShort(value);
/* 1396:     */   }
/* 1397:     */   
/* 1398:     */   public CompositeByteBuf writeMedium(int value)
/* 1399:     */   {
/* 1400:1498 */     return (CompositeByteBuf)super.writeMedium(value);
/* 1401:     */   }
/* 1402:     */   
/* 1403:     */   public CompositeByteBuf writeInt(int value)
/* 1404:     */   {
/* 1405:1503 */     return (CompositeByteBuf)super.writeInt(value);
/* 1406:     */   }
/* 1407:     */   
/* 1408:     */   public CompositeByteBuf writeLong(long value)
/* 1409:     */   {
/* 1410:1508 */     return (CompositeByteBuf)super.writeLong(value);
/* 1411:     */   }
/* 1412:     */   
/* 1413:     */   public CompositeByteBuf writeChar(int value)
/* 1414:     */   {
/* 1415:1513 */     return (CompositeByteBuf)super.writeChar(value);
/* 1416:     */   }
/* 1417:     */   
/* 1418:     */   public CompositeByteBuf writeFloat(float value)
/* 1419:     */   {
/* 1420:1518 */     return (CompositeByteBuf)super.writeFloat(value);
/* 1421:     */   }
/* 1422:     */   
/* 1423:     */   public CompositeByteBuf writeDouble(double value)
/* 1424:     */   {
/* 1425:1523 */     return (CompositeByteBuf)super.writeDouble(value);
/* 1426:     */   }
/* 1427:     */   
/* 1428:     */   public CompositeByteBuf writeBytes(ByteBuf src)
/* 1429:     */   {
/* 1430:1528 */     return (CompositeByteBuf)super.writeBytes(src);
/* 1431:     */   }
/* 1432:     */   
/* 1433:     */   public CompositeByteBuf writeBytes(ByteBuf src, int length)
/* 1434:     */   {
/* 1435:1533 */     return (CompositeByteBuf)super.writeBytes(src, length);
/* 1436:     */   }
/* 1437:     */   
/* 1438:     */   public CompositeByteBuf writeBytes(ByteBuf src, int srcIndex, int length)
/* 1439:     */   {
/* 1440:1538 */     return (CompositeByteBuf)super.writeBytes(src, srcIndex, length);
/* 1441:     */   }
/* 1442:     */   
/* 1443:     */   public CompositeByteBuf writeBytes(byte[] src)
/* 1444:     */   {
/* 1445:1543 */     return (CompositeByteBuf)super.writeBytes(src);
/* 1446:     */   }
/* 1447:     */   
/* 1448:     */   public CompositeByteBuf writeBytes(byte[] src, int srcIndex, int length)
/* 1449:     */   {
/* 1450:1548 */     return (CompositeByteBuf)super.writeBytes(src, srcIndex, length);
/* 1451:     */   }
/* 1452:     */   
/* 1453:     */   public CompositeByteBuf writeBytes(ByteBuffer src)
/* 1454:     */   {
/* 1455:1553 */     return (CompositeByteBuf)super.writeBytes(src);
/* 1456:     */   }
/* 1457:     */   
/* 1458:     */   public CompositeByteBuf writeZero(int length)
/* 1459:     */   {
/* 1460:1558 */     return (CompositeByteBuf)super.writeZero(length);
/* 1461:     */   }
/* 1462:     */   
/* 1463:     */   public CompositeByteBuf retain(int increment)
/* 1464:     */   {
/* 1465:1563 */     return (CompositeByteBuf)super.retain(increment);
/* 1466:     */   }
/* 1467:     */   
/* 1468:     */   public CompositeByteBuf retain()
/* 1469:     */   {
/* 1470:1568 */     return (CompositeByteBuf)super.retain();
/* 1471:     */   }
/* 1472:     */   
/* 1473:     */   public ByteBuffer[] nioBuffers()
/* 1474:     */   {
/* 1475:1573 */     return nioBuffers(readerIndex(), readableBytes());
/* 1476:     */   }
/* 1477:     */   
/* 1478:     */   public CompositeByteBuf discardSomeReadBytes()
/* 1479:     */   {
/* 1480:1578 */     return discardReadComponents();
/* 1481:     */   }
/* 1482:     */   
/* 1483:     */   protected void deallocate()
/* 1484:     */   {
/* 1485:1583 */     if (this.freed) {
/* 1486:1584 */       return;
/* 1487:     */     }
/* 1488:1587 */     this.freed = true;
/* 1489:1588 */     for (Component c : this.components) {
/* 1490:1589 */       c.freeIfNecessary();
/* 1491:     */     }
/* 1492:1592 */     if (this.leak != null) {
/* 1493:1593 */       this.leak.close();
/* 1494:     */     }
/* 1495:     */   }
/* 1496:     */   
/* 1497:     */   public ByteBuf unwrap()
/* 1498:     */   {
/* 1499:1599 */     return null;
/* 1500:     */   }
/* 1501:     */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.CompositeByteBuf
 * JD-Core Version:    0.7.0.1
 */