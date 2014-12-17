/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.PlatformDependent;
/*   4:    */ import java.io.IOException;
/*   5:    */ import java.io.InputStream;
/*   6:    */ import java.io.OutputStream;
/*   7:    */ import java.nio.Buffer;
/*   8:    */ import java.nio.ByteBuffer;
/*   9:    */ import java.nio.ByteOrder;
/*  10:    */ import java.nio.channels.ClosedChannelException;
/*  11:    */ import java.nio.channels.GatheringByteChannel;
/*  12:    */ import java.nio.channels.ScatteringByteChannel;
/*  13:    */ 
/*  14:    */ public class UnpooledHeapByteBuf
/*  15:    */   extends AbstractReferenceCountedByteBuf
/*  16:    */ {
/*  17:    */   private final ByteBufAllocator alloc;
/*  18:    */   private byte[] array;
/*  19:    */   private ByteBuffer tmpNioBuf;
/*  20:    */   
/*  21:    */   protected UnpooledHeapByteBuf(ByteBufAllocator alloc, int initialCapacity, int maxCapacity)
/*  22:    */   {
/*  23: 45 */     this(alloc, new byte[initialCapacity], 0, 0, maxCapacity);
/*  24:    */   }
/*  25:    */   
/*  26:    */   protected UnpooledHeapByteBuf(ByteBufAllocator alloc, byte[] initialArray, int maxCapacity)
/*  27:    */   {
/*  28: 55 */     this(alloc, initialArray, 0, initialArray.length, maxCapacity);
/*  29:    */   }
/*  30:    */   
/*  31:    */   private UnpooledHeapByteBuf(ByteBufAllocator alloc, byte[] initialArray, int readerIndex, int writerIndex, int maxCapacity)
/*  32:    */   {
/*  33: 61 */     super(maxCapacity);
/*  34: 63 */     if (alloc == null) {
/*  35: 64 */       throw new NullPointerException("alloc");
/*  36:    */     }
/*  37: 66 */     if (initialArray == null) {
/*  38: 67 */       throw new NullPointerException("initialArray");
/*  39:    */     }
/*  40: 69 */     if (initialArray.length > maxCapacity) {
/*  41: 70 */       throw new IllegalArgumentException(String.format("initialCapacity(%d) > maxCapacity(%d)", new Object[] { Integer.valueOf(initialArray.length), Integer.valueOf(maxCapacity) }));
/*  42:    */     }
/*  43: 74 */     this.alloc = alloc;
/*  44: 75 */     setArray(initialArray);
/*  45: 76 */     setIndex(readerIndex, writerIndex);
/*  46:    */   }
/*  47:    */   
/*  48:    */   private void setArray(byte[] initialArray)
/*  49:    */   {
/*  50: 80 */     this.array = initialArray;
/*  51: 81 */     this.tmpNioBuf = null;
/*  52:    */   }
/*  53:    */   
/*  54:    */   public ByteBufAllocator alloc()
/*  55:    */   {
/*  56: 86 */     return this.alloc;
/*  57:    */   }
/*  58:    */   
/*  59:    */   public ByteOrder order()
/*  60:    */   {
/*  61: 91 */     return ByteOrder.BIG_ENDIAN;
/*  62:    */   }
/*  63:    */   
/*  64:    */   public boolean isDirect()
/*  65:    */   {
/*  66: 96 */     return false;
/*  67:    */   }
/*  68:    */   
/*  69:    */   public int capacity()
/*  70:    */   {
/*  71:101 */     ensureAccessible();
/*  72:102 */     return this.array.length;
/*  73:    */   }
/*  74:    */   
/*  75:    */   public ByteBuf capacity(int newCapacity)
/*  76:    */   {
/*  77:107 */     ensureAccessible();
/*  78:108 */     if ((newCapacity < 0) || (newCapacity > maxCapacity())) {
/*  79:109 */       throw new IllegalArgumentException("newCapacity: " + newCapacity);
/*  80:    */     }
/*  81:112 */     int oldCapacity = this.array.length;
/*  82:113 */     if (newCapacity > oldCapacity)
/*  83:    */     {
/*  84:114 */       byte[] newArray = new byte[newCapacity];
/*  85:115 */       System.arraycopy(this.array, 0, newArray, 0, this.array.length);
/*  86:116 */       setArray(newArray);
/*  87:    */     }
/*  88:117 */     else if (newCapacity < oldCapacity)
/*  89:    */     {
/*  90:118 */       byte[] newArray = new byte[newCapacity];
/*  91:119 */       int readerIndex = readerIndex();
/*  92:120 */       if (readerIndex < newCapacity)
/*  93:    */       {
/*  94:121 */         int writerIndex = writerIndex();
/*  95:122 */         if (writerIndex > newCapacity) {
/*  96:123 */           writerIndex(writerIndex = newCapacity);
/*  97:    */         }
/*  98:125 */         System.arraycopy(this.array, readerIndex, newArray, readerIndex, writerIndex - readerIndex);
/*  99:    */       }
/* 100:    */       else
/* 101:    */       {
/* 102:127 */         setIndex(newCapacity, newCapacity);
/* 103:    */       }
/* 104:129 */       setArray(newArray);
/* 105:    */     }
/* 106:131 */     return this;
/* 107:    */   }
/* 108:    */   
/* 109:    */   public boolean hasArray()
/* 110:    */   {
/* 111:136 */     return true;
/* 112:    */   }
/* 113:    */   
/* 114:    */   public byte[] array()
/* 115:    */   {
/* 116:141 */     ensureAccessible();
/* 117:142 */     return this.array;
/* 118:    */   }
/* 119:    */   
/* 120:    */   public int arrayOffset()
/* 121:    */   {
/* 122:147 */     return 0;
/* 123:    */   }
/* 124:    */   
/* 125:    */   public boolean hasMemoryAddress()
/* 126:    */   {
/* 127:152 */     return false;
/* 128:    */   }
/* 129:    */   
/* 130:    */   public long memoryAddress()
/* 131:    */   {
/* 132:157 */     throw new UnsupportedOperationException();
/* 133:    */   }
/* 134:    */   
/* 135:    */   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/* 136:    */   {
/* 137:162 */     checkDstIndex(index, length, dstIndex, dst.capacity());
/* 138:163 */     if (dst.hasMemoryAddress()) {
/* 139:164 */       PlatformDependent.copyMemory(this.array, index, dst.memoryAddress() + dstIndex, length);
/* 140:165 */     } else if (dst.hasArray()) {
/* 141:166 */       getBytes(index, dst.array(), dst.arrayOffset() + dstIndex, length);
/* 142:    */     } else {
/* 143:168 */       dst.setBytes(dstIndex, this.array, index, length);
/* 144:    */     }
/* 145:170 */     return this;
/* 146:    */   }
/* 147:    */   
/* 148:    */   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/* 149:    */   {
/* 150:175 */     checkDstIndex(index, length, dstIndex, dst.length);
/* 151:176 */     System.arraycopy(this.array, index, dst, dstIndex, length);
/* 152:177 */     return this;
/* 153:    */   }
/* 154:    */   
/* 155:    */   public ByteBuf getBytes(int index, ByteBuffer dst)
/* 156:    */   {
/* 157:182 */     ensureAccessible();
/* 158:183 */     dst.put(this.array, index, Math.min(capacity() - index, dst.remaining()));
/* 159:184 */     return this;
/* 160:    */   }
/* 161:    */   
/* 162:    */   public ByteBuf getBytes(int index, OutputStream out, int length)
/* 163:    */     throws IOException
/* 164:    */   {
/* 165:189 */     ensureAccessible();
/* 166:190 */     out.write(this.array, index, length);
/* 167:191 */     return this;
/* 168:    */   }
/* 169:    */   
/* 170:    */   public int getBytes(int index, GatheringByteChannel out, int length)
/* 171:    */     throws IOException
/* 172:    */   {
/* 173:196 */     ensureAccessible();
/* 174:197 */     return getBytes(index, out, length, false);
/* 175:    */   }
/* 176:    */   
/* 177:    */   private int getBytes(int index, GatheringByteChannel out, int length, boolean internal)
/* 178:    */     throws IOException
/* 179:    */   {
/* 180:201 */     ensureAccessible();
/* 181:    */     ByteBuffer tmpBuf;
/* 182:    */     ByteBuffer tmpBuf;
/* 183:203 */     if (internal) {
/* 184:204 */       tmpBuf = internalNioBuffer();
/* 185:    */     } else {
/* 186:206 */       tmpBuf = ByteBuffer.wrap(this.array);
/* 187:    */     }
/* 188:208 */     return out.write((ByteBuffer)tmpBuf.clear().position(index).limit(index + length));
/* 189:    */   }
/* 190:    */   
/* 191:    */   public int readBytes(GatheringByteChannel out, int length)
/* 192:    */     throws IOException
/* 193:    */   {
/* 194:213 */     checkReadableBytes(length);
/* 195:214 */     int readBytes = getBytes(this.readerIndex, out, length, true);
/* 196:215 */     this.readerIndex += readBytes;
/* 197:216 */     return readBytes;
/* 198:    */   }
/* 199:    */   
/* 200:    */   public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/* 201:    */   {
/* 202:221 */     checkSrcIndex(index, length, srcIndex, src.capacity());
/* 203:222 */     if (src.hasMemoryAddress()) {
/* 204:223 */       PlatformDependent.copyMemory(src.memoryAddress() + srcIndex, this.array, index, length);
/* 205:224 */     } else if (src.hasArray()) {
/* 206:225 */       setBytes(index, src.array(), src.arrayOffset() + srcIndex, length);
/* 207:    */     } else {
/* 208:227 */       src.getBytes(srcIndex, this.array, index, length);
/* 209:    */     }
/* 210:229 */     return this;
/* 211:    */   }
/* 212:    */   
/* 213:    */   public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/* 214:    */   {
/* 215:234 */     checkSrcIndex(index, length, srcIndex, src.length);
/* 216:235 */     System.arraycopy(src, srcIndex, this.array, index, length);
/* 217:236 */     return this;
/* 218:    */   }
/* 219:    */   
/* 220:    */   public ByteBuf setBytes(int index, ByteBuffer src)
/* 221:    */   {
/* 222:241 */     ensureAccessible();
/* 223:242 */     src.get(this.array, index, src.remaining());
/* 224:243 */     return this;
/* 225:    */   }
/* 226:    */   
/* 227:    */   public int setBytes(int index, InputStream in, int length)
/* 228:    */     throws IOException
/* 229:    */   {
/* 230:248 */     ensureAccessible();
/* 231:249 */     return in.read(this.array, index, length);
/* 232:    */   }
/* 233:    */   
/* 234:    */   public int setBytes(int index, ScatteringByteChannel in, int length)
/* 235:    */     throws IOException
/* 236:    */   {
/* 237:254 */     ensureAccessible();
/* 238:    */     try
/* 239:    */     {
/* 240:256 */       return in.read((ByteBuffer)internalNioBuffer().clear().position(index).limit(index + length));
/* 241:    */     }
/* 242:    */     catch (ClosedChannelException e) {}
/* 243:258 */     return -1;
/* 244:    */   }
/* 245:    */   
/* 246:    */   public int nioBufferCount()
/* 247:    */   {
/* 248:264 */     return 1;
/* 249:    */   }
/* 250:    */   
/* 251:    */   public ByteBuffer nioBuffer(int index, int length)
/* 252:    */   {
/* 253:269 */     ensureAccessible();
/* 254:270 */     return ByteBuffer.wrap(this.array, index, length).slice();
/* 255:    */   }
/* 256:    */   
/* 257:    */   public ByteBuffer[] nioBuffers(int index, int length)
/* 258:    */   {
/* 259:275 */     return new ByteBuffer[] { nioBuffer(index, length) };
/* 260:    */   }
/* 261:    */   
/* 262:    */   public ByteBuffer internalNioBuffer(int index, int length)
/* 263:    */   {
/* 264:280 */     checkIndex(index, length);
/* 265:281 */     return (ByteBuffer)internalNioBuffer().clear().position(index).limit(index + length);
/* 266:    */   }
/* 267:    */   
/* 268:    */   public byte getByte(int index)
/* 269:    */   {
/* 270:286 */     ensureAccessible();
/* 271:287 */     return _getByte(index);
/* 272:    */   }
/* 273:    */   
/* 274:    */   protected byte _getByte(int index)
/* 275:    */   {
/* 276:292 */     return this.array[index];
/* 277:    */   }
/* 278:    */   
/* 279:    */   public short getShort(int index)
/* 280:    */   {
/* 281:297 */     ensureAccessible();
/* 282:298 */     return _getShort(index);
/* 283:    */   }
/* 284:    */   
/* 285:    */   protected short _getShort(int index)
/* 286:    */   {
/* 287:303 */     return (short)(this.array[index] << 8 | this.array[(index + 1)] & 0xFF);
/* 288:    */   }
/* 289:    */   
/* 290:    */   public int getUnsignedMedium(int index)
/* 291:    */   {
/* 292:308 */     ensureAccessible();
/* 293:309 */     return _getUnsignedMedium(index);
/* 294:    */   }
/* 295:    */   
/* 296:    */   protected int _getUnsignedMedium(int index)
/* 297:    */   {
/* 298:314 */     return (this.array[index] & 0xFF) << 16 | (this.array[(index + 1)] & 0xFF) << 8 | this.array[(index + 2)] & 0xFF;
/* 299:    */   }
/* 300:    */   
/* 301:    */   public int getInt(int index)
/* 302:    */   {
/* 303:321 */     ensureAccessible();
/* 304:322 */     return _getInt(index);
/* 305:    */   }
/* 306:    */   
/* 307:    */   protected int _getInt(int index)
/* 308:    */   {
/* 309:327 */     return (this.array[index] & 0xFF) << 24 | (this.array[(index + 1)] & 0xFF) << 16 | (this.array[(index + 2)] & 0xFF) << 8 | this.array[(index + 3)] & 0xFF;
/* 310:    */   }
/* 311:    */   
/* 312:    */   public long getLong(int index)
/* 313:    */   {
/* 314:335 */     ensureAccessible();
/* 315:336 */     return _getLong(index);
/* 316:    */   }
/* 317:    */   
/* 318:    */   protected long _getLong(int index)
/* 319:    */   {
/* 320:341 */     return (this.array[index] & 0xFF) << 56 | (this.array[(index + 1)] & 0xFF) << 48 | (this.array[(index + 2)] & 0xFF) << 40 | (this.array[(index + 3)] & 0xFF) << 32 | (this.array[(index + 4)] & 0xFF) << 24 | (this.array[(index + 5)] & 0xFF) << 16 | (this.array[(index + 6)] & 0xFF) << 8 | this.array[(index + 7)] & 0xFF;
/* 321:    */   }
/* 322:    */   
/* 323:    */   public ByteBuf setByte(int index, int value)
/* 324:    */   {
/* 325:353 */     ensureAccessible();
/* 326:354 */     _setByte(index, value);
/* 327:355 */     return this;
/* 328:    */   }
/* 329:    */   
/* 330:    */   protected void _setByte(int index, int value)
/* 331:    */   {
/* 332:360 */     this.array[index] = ((byte)value);
/* 333:    */   }
/* 334:    */   
/* 335:    */   public ByteBuf setShort(int index, int value)
/* 336:    */   {
/* 337:365 */     ensureAccessible();
/* 338:366 */     _setShort(index, value);
/* 339:367 */     return this;
/* 340:    */   }
/* 341:    */   
/* 342:    */   protected void _setShort(int index, int value)
/* 343:    */   {
/* 344:372 */     this.array[index] = ((byte)(value >>> 8));
/* 345:373 */     this.array[(index + 1)] = ((byte)value);
/* 346:    */   }
/* 347:    */   
/* 348:    */   public ByteBuf setMedium(int index, int value)
/* 349:    */   {
/* 350:378 */     ensureAccessible();
/* 351:379 */     _setMedium(index, value);
/* 352:380 */     return this;
/* 353:    */   }
/* 354:    */   
/* 355:    */   protected void _setMedium(int index, int value)
/* 356:    */   {
/* 357:385 */     this.array[index] = ((byte)(value >>> 16));
/* 358:386 */     this.array[(index + 1)] = ((byte)(value >>> 8));
/* 359:387 */     this.array[(index + 2)] = ((byte)value);
/* 360:    */   }
/* 361:    */   
/* 362:    */   public ByteBuf setInt(int index, int value)
/* 363:    */   {
/* 364:392 */     ensureAccessible();
/* 365:393 */     _setInt(index, value);
/* 366:394 */     return this;
/* 367:    */   }
/* 368:    */   
/* 369:    */   protected void _setInt(int index, int value)
/* 370:    */   {
/* 371:399 */     this.array[index] = ((byte)(value >>> 24));
/* 372:400 */     this.array[(index + 1)] = ((byte)(value >>> 16));
/* 373:401 */     this.array[(index + 2)] = ((byte)(value >>> 8));
/* 374:402 */     this.array[(index + 3)] = ((byte)value);
/* 375:    */   }
/* 376:    */   
/* 377:    */   public ByteBuf setLong(int index, long value)
/* 378:    */   {
/* 379:407 */     ensureAccessible();
/* 380:408 */     _setLong(index, value);
/* 381:409 */     return this;
/* 382:    */   }
/* 383:    */   
/* 384:    */   protected void _setLong(int index, long value)
/* 385:    */   {
/* 386:414 */     this.array[index] = ((byte)(int)(value >>> 56));
/* 387:415 */     this.array[(index + 1)] = ((byte)(int)(value >>> 48));
/* 388:416 */     this.array[(index + 2)] = ((byte)(int)(value >>> 40));
/* 389:417 */     this.array[(index + 3)] = ((byte)(int)(value >>> 32));
/* 390:418 */     this.array[(index + 4)] = ((byte)(int)(value >>> 24));
/* 391:419 */     this.array[(index + 5)] = ((byte)(int)(value >>> 16));
/* 392:420 */     this.array[(index + 6)] = ((byte)(int)(value >>> 8));
/* 393:421 */     this.array[(index + 7)] = ((byte)(int)value);
/* 394:    */   }
/* 395:    */   
/* 396:    */   public ByteBuf copy(int index, int length)
/* 397:    */   {
/* 398:426 */     checkIndex(index, length);
/* 399:427 */     byte[] copiedArray = new byte[length];
/* 400:428 */     System.arraycopy(this.array, index, copiedArray, 0, length);
/* 401:429 */     return new UnpooledHeapByteBuf(alloc(), copiedArray, maxCapacity());
/* 402:    */   }
/* 403:    */   
/* 404:    */   private ByteBuffer internalNioBuffer()
/* 405:    */   {
/* 406:433 */     ByteBuffer tmpNioBuf = this.tmpNioBuf;
/* 407:434 */     if (tmpNioBuf == null) {
/* 408:435 */       this.tmpNioBuf = (tmpNioBuf = ByteBuffer.wrap(this.array));
/* 409:    */     }
/* 410:437 */     return tmpNioBuf;
/* 411:    */   }
/* 412:    */   
/* 413:    */   protected void deallocate()
/* 414:    */   {
/* 415:442 */     this.array = null;
/* 416:    */   }
/* 417:    */   
/* 418:    */   public ByteBuf unwrap()
/* 419:    */   {
/* 420:447 */     return null;
/* 421:    */   }
/* 422:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.UnpooledHeapByteBuf
 * JD-Core Version:    0.7.0.1
 */