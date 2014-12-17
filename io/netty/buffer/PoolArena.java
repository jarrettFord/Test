/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.PlatformDependent;
/*   4:    */ import io.netty.util.internal.StringUtil;
/*   5:    */ import java.nio.Buffer;
/*   6:    */ import java.nio.ByteBuffer;
/*   7:    */ 
/*   8:    */ abstract class PoolArena<T>
/*   9:    */ {
/*  10:    */   static final int numTinySubpagePools = 32;
/*  11:    */   final PooledByteBufAllocator parent;
/*  12:    */   private final int maxOrder;
/*  13:    */   final int pageSize;
/*  14:    */   final int pageShifts;
/*  15:    */   final int chunkSize;
/*  16:    */   final int subpageOverflowMask;
/*  17:    */   final int numSmallSubpagePools;
/*  18:    */   private final PoolSubpage<T>[] tinySubpagePools;
/*  19:    */   private final PoolSubpage<T>[] smallSubpagePools;
/*  20:    */   private final PoolChunkList<T> q050;
/*  21:    */   private final PoolChunkList<T> q025;
/*  22:    */   private final PoolChunkList<T> q000;
/*  23:    */   private final PoolChunkList<T> qInit;
/*  24:    */   private final PoolChunkList<T> q075;
/*  25:    */   private final PoolChunkList<T> q100;
/*  26:    */   
/*  27:    */   protected PoolArena(PooledByteBufAllocator parent, int pageSize, int maxOrder, int pageShifts, int chunkSize)
/*  28:    */   {
/*  29: 50 */     this.parent = parent;
/*  30: 51 */     this.pageSize = pageSize;
/*  31: 52 */     this.maxOrder = maxOrder;
/*  32: 53 */     this.pageShifts = pageShifts;
/*  33: 54 */     this.chunkSize = chunkSize;
/*  34: 55 */     this.subpageOverflowMask = (pageSize - 1 ^ 0xFFFFFFFF);
/*  35: 56 */     this.tinySubpagePools = newSubpagePoolArray(32);
/*  36: 57 */     for (int i = 0; i < this.tinySubpagePools.length; i++) {
/*  37: 58 */       this.tinySubpagePools[i] = newSubpagePoolHead(pageSize);
/*  38:    */     }
/*  39: 61 */     this.numSmallSubpagePools = (pageShifts - 9);
/*  40: 62 */     this.smallSubpagePools = newSubpagePoolArray(this.numSmallSubpagePools);
/*  41: 63 */     for (int i = 0; i < this.smallSubpagePools.length; i++) {
/*  42: 64 */       this.smallSubpagePools[i] = newSubpagePoolHead(pageSize);
/*  43:    */     }
/*  44: 67 */     this.q100 = new PoolChunkList(this, null, 100, 2147483647);
/*  45: 68 */     this.q075 = new PoolChunkList(this, this.q100, 75, 100);
/*  46: 69 */     this.q050 = new PoolChunkList(this, this.q075, 50, 100);
/*  47: 70 */     this.q025 = new PoolChunkList(this, this.q050, 25, 75);
/*  48: 71 */     this.q000 = new PoolChunkList(this, this.q025, 1, 50);
/*  49: 72 */     this.qInit = new PoolChunkList(this, this.q000, -2147483648, 25);
/*  50:    */     
/*  51: 74 */     this.q100.prevList = this.q075;
/*  52: 75 */     this.q075.prevList = this.q050;
/*  53: 76 */     this.q050.prevList = this.q025;
/*  54: 77 */     this.q025.prevList = this.q000;
/*  55: 78 */     this.q000.prevList = null;
/*  56: 79 */     this.qInit.prevList = this.qInit;
/*  57:    */   }
/*  58:    */   
/*  59:    */   private PoolSubpage<T> newSubpagePoolHead(int pageSize)
/*  60:    */   {
/*  61: 83 */     PoolSubpage<T> head = new PoolSubpage(pageSize);
/*  62: 84 */     head.prev = head;
/*  63: 85 */     head.next = head;
/*  64: 86 */     return head;
/*  65:    */   }
/*  66:    */   
/*  67:    */   private PoolSubpage<T>[] newSubpagePoolArray(int size)
/*  68:    */   {
/*  69: 91 */     return new PoolSubpage[size];
/*  70:    */   }
/*  71:    */   
/*  72:    */   abstract boolean isDirect();
/*  73:    */   
/*  74:    */   PooledByteBuf<T> allocate(PoolThreadCache cache, int reqCapacity, int maxCapacity)
/*  75:    */   {
/*  76: 97 */     PooledByteBuf<T> buf = newByteBuf(maxCapacity);
/*  77: 98 */     allocate(cache, buf, reqCapacity);
/*  78: 99 */     return buf;
/*  79:    */   }
/*  80:    */   
/*  81:    */   static int tinyIdx(int normCapacity)
/*  82:    */   {
/*  83:103 */     return normCapacity >>> 4;
/*  84:    */   }
/*  85:    */   
/*  86:    */   static int smallIdx(int normCapacity)
/*  87:    */   {
/*  88:107 */     int tableIdx = 0;
/*  89:108 */     int i = normCapacity >>> 10;
/*  90:109 */     while (i != 0)
/*  91:    */     {
/*  92:110 */       i >>>= 1;
/*  93:111 */       tableIdx++;
/*  94:    */     }
/*  95:113 */     return tableIdx;
/*  96:    */   }
/*  97:    */   
/*  98:    */   boolean isTinyOrSmall(int normCapacity)
/*  99:    */   {
/* 100:118 */     return (normCapacity & this.subpageOverflowMask) == 0;
/* 101:    */   }
/* 102:    */   
/* 103:    */   static boolean isTiny(int normCapacity)
/* 104:    */   {
/* 105:123 */     return (normCapacity & 0xFFFFFE00) == 0;
/* 106:    */   }
/* 107:    */   
/* 108:    */   private void allocate(PoolThreadCache cache, PooledByteBuf<T> buf, int reqCapacity)
/* 109:    */   {
/* 110:127 */     int normCapacity = normalizeCapacity(reqCapacity);
/* 111:128 */     if (isTinyOrSmall(normCapacity))
/* 112:    */     {
/* 113:    */       PoolSubpage<T>[] table;
/* 114:    */       int tableIdx;
/* 115:    */       PoolSubpage<T>[] table;
/* 116:131 */       if (isTiny(normCapacity))
/* 117:    */       {
/* 118:132 */         if (cache.allocateTiny(this, buf, reqCapacity, normCapacity)) {
/* 119:134 */           return;
/* 120:    */         }
/* 121:136 */         int tableIdx = tinyIdx(normCapacity);
/* 122:137 */         table = this.tinySubpagePools;
/* 123:    */       }
/* 124:    */       else
/* 125:    */       {
/* 126:139 */         if (cache.allocateSmall(this, buf, reqCapacity, normCapacity)) {
/* 127:141 */           return;
/* 128:    */         }
/* 129:143 */         tableIdx = smallIdx(normCapacity);
/* 130:144 */         table = this.smallSubpagePools;
/* 131:    */       }
/* 132:147 */       synchronized (this)
/* 133:    */       {
/* 134:148 */         PoolSubpage<T> head = table[tableIdx];
/* 135:149 */         PoolSubpage<T> s = head.next;
/* 136:150 */         if (s != head)
/* 137:    */         {
/* 138:151 */           assert ((s.doNotDestroy) && (s.elemSize == normCapacity));
/* 139:152 */           long handle = s.allocate();
/* 140:153 */           assert (handle >= 0L);
/* 141:154 */           s.chunk.initBufWithSubpage(buf, handle, reqCapacity);
/* 142:155 */           return;
/* 143:    */         }
/* 144:    */       }
/* 145:    */     }
/* 146:158 */     else if (normCapacity <= this.chunkSize)
/* 147:    */     {
/* 148:159 */       if (!cache.allocateNormal(this, buf, reqCapacity, normCapacity)) {}
/* 149:    */     }
/* 150:    */     else
/* 151:    */     {
/* 152:165 */       allocateHuge(buf, reqCapacity);
/* 153:166 */       return;
/* 154:    */     }
/* 155:168 */     allocateNormal(buf, reqCapacity, normCapacity);
/* 156:    */   }
/* 157:    */   
/* 158:    */   private synchronized void allocateNormal(PooledByteBuf<T> buf, int reqCapacity, int normCapacity)
/* 159:    */   {
/* 160:172 */     if ((this.q050.allocate(buf, reqCapacity, normCapacity)) || (this.q025.allocate(buf, reqCapacity, normCapacity)) || (this.q000.allocate(buf, reqCapacity, normCapacity)) || (this.qInit.allocate(buf, reqCapacity, normCapacity)) || (this.q075.allocate(buf, reqCapacity, normCapacity)) || (this.q100.allocate(buf, reqCapacity, normCapacity))) {
/* 161:175 */       return;
/* 162:    */     }
/* 163:179 */     PoolChunk<T> c = newChunk(this.pageSize, this.maxOrder, this.pageShifts, this.chunkSize);
/* 164:180 */     long handle = c.allocate(normCapacity);
/* 165:181 */     assert (handle > 0L);
/* 166:182 */     c.initBuf(buf, handle, reqCapacity);
/* 167:183 */     this.qInit.add(c);
/* 168:    */   }
/* 169:    */   
/* 170:    */   private void allocateHuge(PooledByteBuf<T> buf, int reqCapacity)
/* 171:    */   {
/* 172:187 */     buf.initUnpooled(newUnpooledChunk(reqCapacity), reqCapacity);
/* 173:    */   }
/* 174:    */   
/* 175:    */   void free(PoolChunk<T> chunk, long handle, int normCapacity)
/* 176:    */   {
/* 177:191 */     if (chunk.unpooled)
/* 178:    */     {
/* 179:192 */       destroyChunk(chunk);
/* 180:    */     }
/* 181:    */     else
/* 182:    */     {
/* 183:194 */       PoolThreadCache cache = (PoolThreadCache)this.parent.threadCache.get();
/* 184:195 */       if (cache.add(this, chunk, handle, normCapacity)) {
/* 185:197 */         return;
/* 186:    */       }
/* 187:199 */       synchronized (this)
/* 188:    */       {
/* 189:200 */         chunk.parent.free(chunk, handle);
/* 190:    */       }
/* 191:    */     }
/* 192:    */   }
/* 193:    */   
/* 194:    */   PoolSubpage<T> findSubpagePoolHead(int elemSize)
/* 195:    */   {
/* 196:    */     PoolSubpage<T>[] table;
/* 197:    */     int tableIdx;
/* 198:    */     PoolSubpage<T>[] table;
/* 199:208 */     if (isTiny(elemSize))
/* 200:    */     {
/* 201:209 */       int tableIdx = elemSize >>> 4;
/* 202:210 */       table = this.tinySubpagePools;
/* 203:    */     }
/* 204:    */     else
/* 205:    */     {
/* 206:212 */       tableIdx = 0;
/* 207:213 */       elemSize >>>= 10;
/* 208:214 */       while (elemSize != 0)
/* 209:    */       {
/* 210:215 */         elemSize >>>= 1;
/* 211:216 */         tableIdx++;
/* 212:    */       }
/* 213:218 */       table = this.smallSubpagePools;
/* 214:    */     }
/* 215:221 */     return table[tableIdx];
/* 216:    */   }
/* 217:    */   
/* 218:    */   int normalizeCapacity(int reqCapacity)
/* 219:    */   {
/* 220:225 */     if (reqCapacity < 0) {
/* 221:226 */       throw new IllegalArgumentException("capacity: " + reqCapacity + " (expected: 0+)");
/* 222:    */     }
/* 223:228 */     if (reqCapacity >= this.chunkSize) {
/* 224:229 */       return reqCapacity;
/* 225:    */     }
/* 226:232 */     if (!isTiny(reqCapacity))
/* 227:    */     {
/* 228:235 */       int normalizedCapacity = reqCapacity;
/* 229:236 */       normalizedCapacity--;
/* 230:237 */       normalizedCapacity |= normalizedCapacity >>> 1;
/* 231:238 */       normalizedCapacity |= normalizedCapacity >>> 2;
/* 232:239 */       normalizedCapacity |= normalizedCapacity >>> 4;
/* 233:240 */       normalizedCapacity |= normalizedCapacity >>> 8;
/* 234:241 */       normalizedCapacity |= normalizedCapacity >>> 16;
/* 235:242 */       normalizedCapacity++;
/* 236:244 */       if (normalizedCapacity < 0) {
/* 237:245 */         normalizedCapacity >>>= 1;
/* 238:    */       }
/* 239:248 */       return normalizedCapacity;
/* 240:    */     }
/* 241:252 */     if ((reqCapacity & 0xF) == 0) {
/* 242:253 */       return reqCapacity;
/* 243:    */     }
/* 244:256 */     return (reqCapacity & 0xFFFFFFF0) + 16;
/* 245:    */   }
/* 246:    */   
/* 247:    */   void reallocate(PooledByteBuf<T> buf, int newCapacity, boolean freeOldMemory)
/* 248:    */   {
/* 249:260 */     if ((newCapacity < 0) || (newCapacity > buf.maxCapacity())) {
/* 250:261 */       throw new IllegalArgumentException("newCapacity: " + newCapacity);
/* 251:    */     }
/* 252:264 */     int oldCapacity = buf.length;
/* 253:265 */     if (oldCapacity == newCapacity) {
/* 254:266 */       return;
/* 255:    */     }
/* 256:269 */     PoolChunk<T> oldChunk = buf.chunk;
/* 257:270 */     long oldHandle = buf.handle;
/* 258:271 */     T oldMemory = buf.memory;
/* 259:272 */     int oldOffset = buf.offset;
/* 260:273 */     int oldMaxLength = buf.maxLength;
/* 261:274 */     int readerIndex = buf.readerIndex();
/* 262:275 */     int writerIndex = buf.writerIndex();
/* 263:    */     
/* 264:277 */     allocate((PoolThreadCache)this.parent.threadCache.get(), buf, newCapacity);
/* 265:278 */     if (newCapacity > oldCapacity) {
/* 266:279 */       memoryCopy(oldMemory, oldOffset, buf.memory, buf.offset, oldCapacity);
/* 267:282 */     } else if (newCapacity < oldCapacity) {
/* 268:283 */       if (readerIndex < newCapacity)
/* 269:    */       {
/* 270:284 */         if (writerIndex > newCapacity) {
/* 271:285 */           writerIndex = newCapacity;
/* 272:    */         }
/* 273:287 */         memoryCopy(oldMemory, oldOffset + readerIndex, buf.memory, buf.offset + readerIndex, writerIndex - readerIndex);
/* 274:    */       }
/* 275:    */       else
/* 276:    */       {
/* 277:291 */         readerIndex = writerIndex = newCapacity;
/* 278:    */       }
/* 279:    */     }
/* 280:295 */     buf.setIndex(readerIndex, writerIndex);
/* 281:297 */     if (freeOldMemory) {
/* 282:298 */       free(oldChunk, oldHandle, oldMaxLength);
/* 283:    */     }
/* 284:    */   }
/* 285:    */   
/* 286:    */   protected abstract PoolChunk<T> newChunk(int paramInt1, int paramInt2, int paramInt3, int paramInt4);
/* 287:    */   
/* 288:    */   protected abstract PoolChunk<T> newUnpooledChunk(int paramInt);
/* 289:    */   
/* 290:    */   protected abstract PooledByteBuf<T> newByteBuf(int paramInt);
/* 291:    */   
/* 292:    */   protected abstract void memoryCopy(T paramT1, int paramInt1, T paramT2, int paramInt2, int paramInt3);
/* 293:    */   
/* 294:    */   protected abstract void destroyChunk(PoolChunk<T> paramPoolChunk);
/* 295:    */   
/* 296:    */   public synchronized String toString()
/* 297:    */   {
/* 298:309 */     StringBuilder buf = new StringBuilder();
/* 299:310 */     buf.append("Chunk(s) at 0~25%:");
/* 300:311 */     buf.append(StringUtil.NEWLINE);
/* 301:312 */     buf.append(this.qInit);
/* 302:313 */     buf.append(StringUtil.NEWLINE);
/* 303:314 */     buf.append("Chunk(s) at 0~50%:");
/* 304:315 */     buf.append(StringUtil.NEWLINE);
/* 305:316 */     buf.append(this.q000);
/* 306:317 */     buf.append(StringUtil.NEWLINE);
/* 307:318 */     buf.append("Chunk(s) at 25~75%:");
/* 308:319 */     buf.append(StringUtil.NEWLINE);
/* 309:320 */     buf.append(this.q025);
/* 310:321 */     buf.append(StringUtil.NEWLINE);
/* 311:322 */     buf.append("Chunk(s) at 50~100%:");
/* 312:323 */     buf.append(StringUtil.NEWLINE);
/* 313:324 */     buf.append(this.q050);
/* 314:325 */     buf.append(StringUtil.NEWLINE);
/* 315:326 */     buf.append("Chunk(s) at 75~100%:");
/* 316:327 */     buf.append(StringUtil.NEWLINE);
/* 317:328 */     buf.append(this.q075);
/* 318:329 */     buf.append(StringUtil.NEWLINE);
/* 319:330 */     buf.append("Chunk(s) at 100%:");
/* 320:331 */     buf.append(StringUtil.NEWLINE);
/* 321:332 */     buf.append(this.q100);
/* 322:333 */     buf.append(StringUtil.NEWLINE);
/* 323:334 */     buf.append("tiny subpages:");
/* 324:335 */     for (int i = 1; i < this.tinySubpagePools.length; i++)
/* 325:    */     {
/* 326:336 */       PoolSubpage<T> head = this.tinySubpagePools[i];
/* 327:337 */       if (head.next != head)
/* 328:    */       {
/* 329:341 */         buf.append(StringUtil.NEWLINE);
/* 330:342 */         buf.append(i);
/* 331:343 */         buf.append(": ");
/* 332:344 */         PoolSubpage<T> s = head.next;
/* 333:    */         for (;;)
/* 334:    */         {
/* 335:346 */           buf.append(s);
/* 336:347 */           s = s.next;
/* 337:348 */           if (s == head) {
/* 338:    */             break;
/* 339:    */           }
/* 340:    */         }
/* 341:    */       }
/* 342:    */     }
/* 343:353 */     buf.append(StringUtil.NEWLINE);
/* 344:354 */     buf.append("small subpages:");
/* 345:355 */     for (int i = 1; i < this.smallSubpagePools.length; i++)
/* 346:    */     {
/* 347:356 */       PoolSubpage<T> head = this.smallSubpagePools[i];
/* 348:357 */       if (head.next != head)
/* 349:    */       {
/* 350:361 */         buf.append(StringUtil.NEWLINE);
/* 351:362 */         buf.append(i);
/* 352:363 */         buf.append(": ");
/* 353:364 */         PoolSubpage<T> s = head.next;
/* 354:    */         for (;;)
/* 355:    */         {
/* 356:366 */           buf.append(s);
/* 357:367 */           s = s.next;
/* 358:368 */           if (s == head) {
/* 359:    */             break;
/* 360:    */           }
/* 361:    */         }
/* 362:    */       }
/* 363:    */     }
/* 364:373 */     buf.append(StringUtil.NEWLINE);
/* 365:    */     
/* 366:375 */     return buf.toString();
/* 367:    */   }
/* 368:    */   
/* 369:    */   static final class HeapArena
/* 370:    */     extends PoolArena<byte[]>
/* 371:    */   {
/* 372:    */     HeapArena(PooledByteBufAllocator parent, int pageSize, int maxOrder, int pageShifts, int chunkSize)
/* 373:    */     {
/* 374:381 */       super(pageSize, maxOrder, pageShifts, chunkSize);
/* 375:    */     }
/* 376:    */     
/* 377:    */     boolean isDirect()
/* 378:    */     {
/* 379:386 */       return false;
/* 380:    */     }
/* 381:    */     
/* 382:    */     protected PoolChunk<byte[]> newChunk(int pageSize, int maxOrder, int pageShifts, int chunkSize)
/* 383:    */     {
/* 384:391 */       return new PoolChunk(this, new byte[chunkSize], pageSize, maxOrder, pageShifts, chunkSize);
/* 385:    */     }
/* 386:    */     
/* 387:    */     protected PoolChunk<byte[]> newUnpooledChunk(int capacity)
/* 388:    */     {
/* 389:396 */       return new PoolChunk(this, new byte[capacity], capacity);
/* 390:    */     }
/* 391:    */     
/* 392:    */     protected void destroyChunk(PoolChunk<byte[]> chunk) {}
/* 393:    */     
/* 394:    */     protected PooledByteBuf<byte[]> newByteBuf(int maxCapacity)
/* 395:    */     {
/* 396:406 */       return PooledHeapByteBuf.newInstance(maxCapacity);
/* 397:    */     }
/* 398:    */     
/* 399:    */     protected void memoryCopy(byte[] src, int srcOffset, byte[] dst, int dstOffset, int length)
/* 400:    */     {
/* 401:411 */       if (length == 0) {
/* 402:412 */         return;
/* 403:    */       }
/* 404:415 */       System.arraycopy(src, srcOffset, dst, dstOffset, length);
/* 405:    */     }
/* 406:    */   }
/* 407:    */   
/* 408:    */   static final class DirectArena
/* 409:    */     extends PoolArena<ByteBuffer>
/* 410:    */   {
/* 411:421 */     private static final boolean HAS_UNSAFE = ;
/* 412:    */     
/* 413:    */     DirectArena(PooledByteBufAllocator parent, int pageSize, int maxOrder, int pageShifts, int chunkSize)
/* 414:    */     {
/* 415:424 */       super(pageSize, maxOrder, pageShifts, chunkSize);
/* 416:    */     }
/* 417:    */     
/* 418:    */     boolean isDirect()
/* 419:    */     {
/* 420:429 */       return true;
/* 421:    */     }
/* 422:    */     
/* 423:    */     protected PoolChunk<ByteBuffer> newChunk(int pageSize, int maxOrder, int pageShifts, int chunkSize)
/* 424:    */     {
/* 425:434 */       return new PoolChunk(this, ByteBuffer.allocateDirect(chunkSize), pageSize, maxOrder, pageShifts, chunkSize);
/* 426:    */     }
/* 427:    */     
/* 428:    */     protected PoolChunk<ByteBuffer> newUnpooledChunk(int capacity)
/* 429:    */     {
/* 430:440 */       return new PoolChunk(this, ByteBuffer.allocateDirect(capacity), capacity);
/* 431:    */     }
/* 432:    */     
/* 433:    */     protected void destroyChunk(PoolChunk<ByteBuffer> chunk)
/* 434:    */     {
/* 435:445 */       PlatformDependent.freeDirectBuffer((ByteBuffer)chunk.memory);
/* 436:    */     }
/* 437:    */     
/* 438:    */     protected PooledByteBuf<ByteBuffer> newByteBuf(int maxCapacity)
/* 439:    */     {
/* 440:450 */       if (HAS_UNSAFE) {
/* 441:451 */         return PooledUnsafeDirectByteBuf.newInstance(maxCapacity);
/* 442:    */       }
/* 443:453 */       return PooledDirectByteBuf.newInstance(maxCapacity);
/* 444:    */     }
/* 445:    */     
/* 446:    */     protected void memoryCopy(ByteBuffer src, int srcOffset, ByteBuffer dst, int dstOffset, int length)
/* 447:    */     {
/* 448:459 */       if (length == 0) {
/* 449:460 */         return;
/* 450:    */       }
/* 451:463 */       if (HAS_UNSAFE)
/* 452:    */       {
/* 453:464 */         PlatformDependent.copyMemory(PlatformDependent.directBufferAddress(src) + srcOffset, PlatformDependent.directBufferAddress(dst) + dstOffset, length);
/* 454:    */       }
/* 455:    */       else
/* 456:    */       {
/* 457:469 */         src = src.duplicate();
/* 458:470 */         dst = dst.duplicate();
/* 459:471 */         src.position(srcOffset).limit(srcOffset + length);
/* 460:472 */         dst.position(dstOffset);
/* 461:473 */         dst.put(src);
/* 462:    */       }
/* 463:    */     }
/* 464:    */   }
/* 465:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.PoolArena
 * JD-Core Version:    0.7.0.1
 */