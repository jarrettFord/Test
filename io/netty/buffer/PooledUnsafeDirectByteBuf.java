/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.Recycler;
/*   4:    */ import io.netty.util.Recycler.Handle;
/*   5:    */ import io.netty.util.internal.PlatformDependent;
/*   6:    */ import java.io.IOException;
/*   7:    */ import java.io.InputStream;
/*   8:    */ import java.io.OutputStream;
/*   9:    */ import java.nio.Buffer;
/*  10:    */ import java.nio.ByteBuffer;
/*  11:    */ import java.nio.ByteOrder;
/*  12:    */ import java.nio.channels.ClosedChannelException;
/*  13:    */ import java.nio.channels.GatheringByteChannel;
/*  14:    */ import java.nio.channels.ScatteringByteChannel;
/*  15:    */ 
/*  16:    */ final class PooledUnsafeDirectByteBuf
/*  17:    */   extends PooledByteBuf<ByteBuffer>
/*  18:    */ {
/*  19: 33 */   private static final boolean NATIVE_ORDER = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
/*  20: 35 */   private static final Recycler<PooledUnsafeDirectByteBuf> RECYCLER = new Recycler()
/*  21:    */   {
/*  22:    */     protected PooledUnsafeDirectByteBuf newObject(Recycler.Handle handle)
/*  23:    */     {
/*  24: 38 */       return new PooledUnsafeDirectByteBuf(handle, 0, null);
/*  25:    */     }
/*  26:    */   };
/*  27:    */   private long memoryAddress;
/*  28:    */   
/*  29:    */   static PooledUnsafeDirectByteBuf newInstance(int maxCapacity)
/*  30:    */   {
/*  31: 43 */     PooledUnsafeDirectByteBuf buf = (PooledUnsafeDirectByteBuf)RECYCLER.get();
/*  32: 44 */     buf.setRefCnt(1);
/*  33: 45 */     buf.maxCapacity(maxCapacity);
/*  34: 46 */     return buf;
/*  35:    */   }
/*  36:    */   
/*  37:    */   private PooledUnsafeDirectByteBuf(Recycler.Handle recyclerHandle, int maxCapacity)
/*  38:    */   {
/*  39: 52 */     super(recyclerHandle, maxCapacity);
/*  40:    */   }
/*  41:    */   
/*  42:    */   void init(PoolChunk<ByteBuffer> chunk, long handle, int offset, int length, int maxLength)
/*  43:    */   {
/*  44: 57 */     super.init(chunk, handle, offset, length, maxLength);
/*  45: 58 */     initMemoryAddress();
/*  46:    */   }
/*  47:    */   
/*  48:    */   void initUnpooled(PoolChunk<ByteBuffer> chunk, int length)
/*  49:    */   {
/*  50: 63 */     super.initUnpooled(chunk, length);
/*  51: 64 */     initMemoryAddress();
/*  52:    */   }
/*  53:    */   
/*  54:    */   private void initMemoryAddress()
/*  55:    */   {
/*  56: 68 */     this.memoryAddress = (PlatformDependent.directBufferAddress((ByteBuffer)this.memory) + this.offset);
/*  57:    */   }
/*  58:    */   
/*  59:    */   protected ByteBuffer newInternalNioBuffer(ByteBuffer memory)
/*  60:    */   {
/*  61: 73 */     return memory.duplicate();
/*  62:    */   }
/*  63:    */   
/*  64:    */   public boolean isDirect()
/*  65:    */   {
/*  66: 78 */     return true;
/*  67:    */   }
/*  68:    */   
/*  69:    */   protected byte _getByte(int index)
/*  70:    */   {
/*  71: 83 */     return PlatformDependent.getByte(addr(index));
/*  72:    */   }
/*  73:    */   
/*  74:    */   protected short _getShort(int index)
/*  75:    */   {
/*  76: 88 */     short v = PlatformDependent.getShort(addr(index));
/*  77: 89 */     return NATIVE_ORDER ? v : Short.reverseBytes(v);
/*  78:    */   }
/*  79:    */   
/*  80:    */   protected int _getUnsignedMedium(int index)
/*  81:    */   {
/*  82: 94 */     long addr = addr(index);
/*  83: 95 */     return (PlatformDependent.getByte(addr) & 0xFF) << 16 | (PlatformDependent.getByte(addr + 1L) & 0xFF) << 8 | PlatformDependent.getByte(addr + 2L) & 0xFF;
/*  84:    */   }
/*  85:    */   
/*  86:    */   protected int _getInt(int index)
/*  87:    */   {
/*  88:102 */     int v = PlatformDependent.getInt(addr(index));
/*  89:103 */     return NATIVE_ORDER ? v : Integer.reverseBytes(v);
/*  90:    */   }
/*  91:    */   
/*  92:    */   protected long _getLong(int index)
/*  93:    */   {
/*  94:108 */     long v = PlatformDependent.getLong(addr(index));
/*  95:109 */     return NATIVE_ORDER ? v : Long.reverseBytes(v);
/*  96:    */   }
/*  97:    */   
/*  98:    */   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/*  99:    */   {
/* 100:114 */     checkIndex(index, length);
/* 101:115 */     if (dst == null) {
/* 102:116 */       throw new NullPointerException("dst");
/* 103:    */     }
/* 104:118 */     if ((dstIndex < 0) || (dstIndex > dst.capacity() - length)) {
/* 105:119 */       throw new IndexOutOfBoundsException("dstIndex: " + dstIndex);
/* 106:    */     }
/* 107:122 */     if (length != 0) {
/* 108:123 */       if (dst.hasMemoryAddress()) {
/* 109:124 */         PlatformDependent.copyMemory(addr(index), dst.memoryAddress() + dstIndex, length);
/* 110:125 */       } else if (dst.hasArray()) {
/* 111:126 */         PlatformDependent.copyMemory(addr(index), dst.array(), dst.arrayOffset() + dstIndex, length);
/* 112:    */       } else {
/* 113:128 */         dst.setBytes(dstIndex, this, index, length);
/* 114:    */       }
/* 115:    */     }
/* 116:131 */     return this;
/* 117:    */   }
/* 118:    */   
/* 119:    */   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/* 120:    */   {
/* 121:136 */     checkIndex(index, length);
/* 122:137 */     if (dst == null) {
/* 123:138 */       throw new NullPointerException("dst");
/* 124:    */     }
/* 125:140 */     if ((dstIndex < 0) || (dstIndex > dst.length - length)) {
/* 126:141 */       throw new IndexOutOfBoundsException("dstIndex: " + dstIndex);
/* 127:    */     }
/* 128:143 */     if (length != 0) {
/* 129:144 */       PlatformDependent.copyMemory(addr(index), dst, dstIndex, length);
/* 130:    */     }
/* 131:146 */     return this;
/* 132:    */   }
/* 133:    */   
/* 134:    */   public ByteBuf getBytes(int index, ByteBuffer dst)
/* 135:    */   {
/* 136:151 */     getBytes(index, dst, false);
/* 137:152 */     return this;
/* 138:    */   }
/* 139:    */   
/* 140:    */   private void getBytes(int index, ByteBuffer dst, boolean internal)
/* 141:    */   {
/* 142:156 */     checkIndex(index);
/* 143:157 */     int bytesToCopy = Math.min(capacity() - index, dst.remaining());
/* 144:    */     ByteBuffer tmpBuf;
/* 145:    */     ByteBuffer tmpBuf;
/* 146:159 */     if (internal) {
/* 147:160 */       tmpBuf = internalNioBuffer();
/* 148:    */     } else {
/* 149:162 */       tmpBuf = ((ByteBuffer)this.memory).duplicate();
/* 150:    */     }
/* 151:164 */     index = idx(index);
/* 152:165 */     tmpBuf.clear().position(index).limit(index + bytesToCopy);
/* 153:166 */     dst.put(tmpBuf);
/* 154:    */   }
/* 155:    */   
/* 156:    */   public ByteBuf readBytes(ByteBuffer dst)
/* 157:    */   {
/* 158:171 */     int length = dst.remaining();
/* 159:172 */     checkReadableBytes(length);
/* 160:173 */     getBytes(this.readerIndex, dst, true);
/* 161:174 */     this.readerIndex += length;
/* 162:175 */     return this;
/* 163:    */   }
/* 164:    */   
/* 165:    */   public ByteBuf getBytes(int index, OutputStream out, int length)
/* 166:    */     throws IOException
/* 167:    */   {
/* 168:180 */     checkIndex(index, length);
/* 169:181 */     if (length != 0)
/* 170:    */     {
/* 171:182 */       byte[] tmp = new byte[length];
/* 172:183 */       PlatformDependent.copyMemory(addr(index), tmp, 0, length);
/* 173:184 */       out.write(tmp);
/* 174:    */     }
/* 175:186 */     return this;
/* 176:    */   }
/* 177:    */   
/* 178:    */   public int getBytes(int index, GatheringByteChannel out, int length)
/* 179:    */     throws IOException
/* 180:    */   {
/* 181:191 */     return getBytes(index, out, length, false);
/* 182:    */   }
/* 183:    */   
/* 184:    */   private int getBytes(int index, GatheringByteChannel out, int length, boolean internal)
/* 185:    */     throws IOException
/* 186:    */   {
/* 187:195 */     checkIndex(index, length);
/* 188:196 */     if (length == 0) {
/* 189:197 */       return 0;
/* 190:    */     }
/* 191:    */     ByteBuffer tmpBuf;
/* 192:    */     ByteBuffer tmpBuf;
/* 193:201 */     if (internal) {
/* 194:202 */       tmpBuf = internalNioBuffer();
/* 195:    */     } else {
/* 196:204 */       tmpBuf = ((ByteBuffer)this.memory).duplicate();
/* 197:    */     }
/* 198:206 */     index = idx(index);
/* 199:207 */     tmpBuf.clear().position(index).limit(index + length);
/* 200:208 */     return out.write(tmpBuf);
/* 201:    */   }
/* 202:    */   
/* 203:    */   public int readBytes(GatheringByteChannel out, int length)
/* 204:    */     throws IOException
/* 205:    */   {
/* 206:214 */     checkReadableBytes(length);
/* 207:215 */     int readBytes = getBytes(this.readerIndex, out, length, true);
/* 208:216 */     this.readerIndex += readBytes;
/* 209:217 */     return readBytes;
/* 210:    */   }
/* 211:    */   
/* 212:    */   protected void _setByte(int index, int value)
/* 213:    */   {
/* 214:222 */     PlatformDependent.putByte(addr(index), (byte)value);
/* 215:    */   }
/* 216:    */   
/* 217:    */   protected void _setShort(int index, int value)
/* 218:    */   {
/* 219:227 */     PlatformDependent.putShort(addr(index), NATIVE_ORDER ? (short)value : Short.reverseBytes((short)value));
/* 220:    */   }
/* 221:    */   
/* 222:    */   protected void _setMedium(int index, int value)
/* 223:    */   {
/* 224:232 */     long addr = addr(index);
/* 225:233 */     PlatformDependent.putByte(addr, (byte)(value >>> 16));
/* 226:234 */     PlatformDependent.putByte(addr + 1L, (byte)(value >>> 8));
/* 227:235 */     PlatformDependent.putByte(addr + 2L, (byte)value);
/* 228:    */   }
/* 229:    */   
/* 230:    */   protected void _setInt(int index, int value)
/* 231:    */   {
/* 232:240 */     PlatformDependent.putInt(addr(index), NATIVE_ORDER ? value : Integer.reverseBytes(value));
/* 233:    */   }
/* 234:    */   
/* 235:    */   protected void _setLong(int index, long value)
/* 236:    */   {
/* 237:245 */     PlatformDependent.putLong(addr(index), NATIVE_ORDER ? value : Long.reverseBytes(value));
/* 238:    */   }
/* 239:    */   
/* 240:    */   public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/* 241:    */   {
/* 242:250 */     checkIndex(index, length);
/* 243:251 */     if (src == null) {
/* 244:252 */       throw new NullPointerException("src");
/* 245:    */     }
/* 246:254 */     if ((srcIndex < 0) || (srcIndex > src.capacity() - length)) {
/* 247:255 */       throw new IndexOutOfBoundsException("srcIndex: " + srcIndex);
/* 248:    */     }
/* 249:258 */     if (length != 0) {
/* 250:259 */       if (src.hasMemoryAddress()) {
/* 251:260 */         PlatformDependent.copyMemory(src.memoryAddress() + srcIndex, addr(index), length);
/* 252:261 */       } else if (src.hasArray()) {
/* 253:262 */         PlatformDependent.copyMemory(src.array(), src.arrayOffset() + srcIndex, addr(index), length);
/* 254:    */       } else {
/* 255:264 */         src.getBytes(srcIndex, this, index, length);
/* 256:    */       }
/* 257:    */     }
/* 258:267 */     return this;
/* 259:    */   }
/* 260:    */   
/* 261:    */   public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/* 262:    */   {
/* 263:272 */     checkIndex(index, length);
/* 264:273 */     if (length != 0) {
/* 265:274 */       PlatformDependent.copyMemory(src, srcIndex, addr(index), length);
/* 266:    */     }
/* 267:276 */     return this;
/* 268:    */   }
/* 269:    */   
/* 270:    */   public ByteBuf setBytes(int index, ByteBuffer src)
/* 271:    */   {
/* 272:281 */     checkIndex(index, src.remaining());
/* 273:282 */     ByteBuffer tmpBuf = internalNioBuffer();
/* 274:283 */     if (src == tmpBuf) {
/* 275:284 */       src = src.duplicate();
/* 276:    */     }
/* 277:287 */     index = idx(index);
/* 278:288 */     tmpBuf.clear().position(index).limit(index + src.remaining());
/* 279:289 */     tmpBuf.put(src);
/* 280:290 */     return this;
/* 281:    */   }
/* 282:    */   
/* 283:    */   public int setBytes(int index, InputStream in, int length)
/* 284:    */     throws IOException
/* 285:    */   {
/* 286:295 */     checkIndex(index, length);
/* 287:296 */     byte[] tmp = new byte[length];
/* 288:297 */     int readBytes = in.read(tmp);
/* 289:298 */     if (readBytes > 0) {
/* 290:299 */       PlatformDependent.copyMemory(tmp, 0, addr(index), readBytes);
/* 291:    */     }
/* 292:301 */     return readBytes;
/* 293:    */   }
/* 294:    */   
/* 295:    */   public int setBytes(int index, ScatteringByteChannel in, int length)
/* 296:    */     throws IOException
/* 297:    */   {
/* 298:306 */     checkIndex(index, length);
/* 299:307 */     ByteBuffer tmpBuf = internalNioBuffer();
/* 300:308 */     index = idx(index);
/* 301:309 */     tmpBuf.clear().position(index).limit(index + length);
/* 302:    */     try
/* 303:    */     {
/* 304:311 */       return in.read(tmpBuf);
/* 305:    */     }
/* 306:    */     catch (ClosedChannelException e) {}
/* 307:313 */     return -1;
/* 308:    */   }
/* 309:    */   
/* 310:    */   public ByteBuf copy(int index, int length)
/* 311:    */   {
/* 312:319 */     checkIndex(index, length);
/* 313:320 */     ByteBuf copy = alloc().directBuffer(length, maxCapacity());
/* 314:321 */     if (length != 0) {
/* 315:322 */       if (copy.hasMemoryAddress())
/* 316:    */       {
/* 317:323 */         PlatformDependent.copyMemory(addr(index), copy.memoryAddress(), length);
/* 318:324 */         copy.setIndex(0, length);
/* 319:    */       }
/* 320:    */       else
/* 321:    */       {
/* 322:326 */         copy.writeBytes(this, index, length);
/* 323:    */       }
/* 324:    */     }
/* 325:329 */     return copy;
/* 326:    */   }
/* 327:    */   
/* 328:    */   public int nioBufferCount()
/* 329:    */   {
/* 330:334 */     return 1;
/* 331:    */   }
/* 332:    */   
/* 333:    */   public ByteBuffer[] nioBuffers(int index, int length)
/* 334:    */   {
/* 335:339 */     return new ByteBuffer[] { nioBuffer(index, length) };
/* 336:    */   }
/* 337:    */   
/* 338:    */   public ByteBuffer nioBuffer(int index, int length)
/* 339:    */   {
/* 340:344 */     checkIndex(index, length);
/* 341:345 */     index = idx(index);
/* 342:346 */     return ((ByteBuffer)((ByteBuffer)this.memory).duplicate().position(index).limit(index + length)).slice();
/* 343:    */   }
/* 344:    */   
/* 345:    */   public ByteBuffer internalNioBuffer(int index, int length)
/* 346:    */   {
/* 347:351 */     checkIndex(index, length);
/* 348:352 */     index = idx(index);
/* 349:353 */     return (ByteBuffer)internalNioBuffer().clear().position(index).limit(index + length);
/* 350:    */   }
/* 351:    */   
/* 352:    */   public boolean hasArray()
/* 353:    */   {
/* 354:358 */     return false;
/* 355:    */   }
/* 356:    */   
/* 357:    */   public byte[] array()
/* 358:    */   {
/* 359:363 */     throw new UnsupportedOperationException("direct buffer");
/* 360:    */   }
/* 361:    */   
/* 362:    */   public int arrayOffset()
/* 363:    */   {
/* 364:368 */     throw new UnsupportedOperationException("direct buffer");
/* 365:    */   }
/* 366:    */   
/* 367:    */   public boolean hasMemoryAddress()
/* 368:    */   {
/* 369:373 */     return true;
/* 370:    */   }
/* 371:    */   
/* 372:    */   public long memoryAddress()
/* 373:    */   {
/* 374:378 */     return this.memoryAddress;
/* 375:    */   }
/* 376:    */   
/* 377:    */   private long addr(int index)
/* 378:    */   {
/* 379:382 */     return this.memoryAddress + index;
/* 380:    */   }
/* 381:    */   
/* 382:    */   protected Recycler<?> recycler()
/* 383:    */   {
/* 384:387 */     return RECYCLER;
/* 385:    */   }
/* 386:    */   
/* 387:    */   protected SwappedByteBuf newSwappedByteBuf()
/* 388:    */   {
/* 389:392 */     return new UnsafeDirectSwappedByteBuf(this);
/* 390:    */   }
/* 391:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.PooledUnsafeDirectByteBuf
 * JD-Core Version:    0.7.0.1
 */