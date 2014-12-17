/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.Recycler;
/*   4:    */ import io.netty.util.Recycler.Handle;
/*   5:    */ import java.io.IOException;
/*   6:    */ import java.io.InputStream;
/*   7:    */ import java.io.OutputStream;
/*   8:    */ import java.nio.Buffer;
/*   9:    */ import java.nio.ByteBuffer;
/*  10:    */ import java.nio.channels.ClosedChannelException;
/*  11:    */ import java.nio.channels.GatheringByteChannel;
/*  12:    */ import java.nio.channels.ScatteringByteChannel;
/*  13:    */ 
/*  14:    */ final class PooledDirectByteBuf
/*  15:    */   extends PooledByteBuf<ByteBuffer>
/*  16:    */ {
/*  17: 31 */   private static final Recycler<PooledDirectByteBuf> RECYCLER = new Recycler()
/*  18:    */   {
/*  19:    */     protected PooledDirectByteBuf newObject(Recycler.Handle handle)
/*  20:    */     {
/*  21: 34 */       return new PooledDirectByteBuf(handle, 0, null);
/*  22:    */     }
/*  23:    */   };
/*  24:    */   
/*  25:    */   static PooledDirectByteBuf newInstance(int maxCapacity)
/*  26:    */   {
/*  27: 39 */     PooledDirectByteBuf buf = (PooledDirectByteBuf)RECYCLER.get();
/*  28: 40 */     buf.setRefCnt(1);
/*  29: 41 */     buf.maxCapacity(maxCapacity);
/*  30: 42 */     return buf;
/*  31:    */   }
/*  32:    */   
/*  33:    */   private PooledDirectByteBuf(Recycler.Handle recyclerHandle, int maxCapacity)
/*  34:    */   {
/*  35: 46 */     super(recyclerHandle, maxCapacity);
/*  36:    */   }
/*  37:    */   
/*  38:    */   protected ByteBuffer newInternalNioBuffer(ByteBuffer memory)
/*  39:    */   {
/*  40: 51 */     return memory.duplicate();
/*  41:    */   }
/*  42:    */   
/*  43:    */   public boolean isDirect()
/*  44:    */   {
/*  45: 56 */     return true;
/*  46:    */   }
/*  47:    */   
/*  48:    */   protected byte _getByte(int index)
/*  49:    */   {
/*  50: 61 */     return ((ByteBuffer)this.memory).get(idx(index));
/*  51:    */   }
/*  52:    */   
/*  53:    */   protected short _getShort(int index)
/*  54:    */   {
/*  55: 66 */     return ((ByteBuffer)this.memory).getShort(idx(index));
/*  56:    */   }
/*  57:    */   
/*  58:    */   protected int _getUnsignedMedium(int index)
/*  59:    */   {
/*  60: 71 */     index = idx(index);
/*  61: 72 */     return (((ByteBuffer)this.memory).get(index) & 0xFF) << 16 | (((ByteBuffer)this.memory).get(index + 1) & 0xFF) << 8 | ((ByteBuffer)this.memory).get(index + 2) & 0xFF;
/*  62:    */   }
/*  63:    */   
/*  64:    */   protected int _getInt(int index)
/*  65:    */   {
/*  66: 77 */     return ((ByteBuffer)this.memory).getInt(idx(index));
/*  67:    */   }
/*  68:    */   
/*  69:    */   protected long _getLong(int index)
/*  70:    */   {
/*  71: 82 */     return ((ByteBuffer)this.memory).getLong(idx(index));
/*  72:    */   }
/*  73:    */   
/*  74:    */   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/*  75:    */   {
/*  76: 87 */     checkDstIndex(index, length, dstIndex, dst.capacity());
/*  77: 88 */     if (dst.hasArray()) {
/*  78: 89 */       getBytes(index, dst.array(), dst.arrayOffset() + dstIndex, length);
/*  79: 90 */     } else if (dst.nioBufferCount() > 0) {
/*  80: 91 */       for (ByteBuffer bb : dst.nioBuffers(dstIndex, length))
/*  81:    */       {
/*  82: 92 */         int bbLen = bb.remaining();
/*  83: 93 */         getBytes(index, bb);
/*  84: 94 */         index += bbLen;
/*  85:    */       }
/*  86:    */     } else {
/*  87: 97 */       dst.setBytes(dstIndex, this, index, length);
/*  88:    */     }
/*  89: 99 */     return this;
/*  90:    */   }
/*  91:    */   
/*  92:    */   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/*  93:    */   {
/*  94:104 */     getBytes(index, dst, dstIndex, length, false);
/*  95:105 */     return this;
/*  96:    */   }
/*  97:    */   
/*  98:    */   private void getBytes(int index, byte[] dst, int dstIndex, int length, boolean internal)
/*  99:    */   {
/* 100:109 */     checkDstIndex(index, length, dstIndex, dst.length);
/* 101:    */     ByteBuffer tmpBuf;
/* 102:    */     ByteBuffer tmpBuf;
/* 103:111 */     if (internal) {
/* 104:112 */       tmpBuf = internalNioBuffer();
/* 105:    */     } else {
/* 106:114 */       tmpBuf = ((ByteBuffer)this.memory).duplicate();
/* 107:    */     }
/* 108:116 */     index = idx(index);
/* 109:117 */     tmpBuf.clear().position(index).limit(index + length);
/* 110:118 */     tmpBuf.get(dst, dstIndex, length);
/* 111:    */   }
/* 112:    */   
/* 113:    */   public ByteBuf readBytes(byte[] dst, int dstIndex, int length)
/* 114:    */   {
/* 115:123 */     checkReadableBytes(length);
/* 116:124 */     getBytes(this.readerIndex, dst, dstIndex, length, true);
/* 117:125 */     this.readerIndex += length;
/* 118:126 */     return this;
/* 119:    */   }
/* 120:    */   
/* 121:    */   public ByteBuf getBytes(int index, ByteBuffer dst)
/* 122:    */   {
/* 123:131 */     getBytes(index, dst, false);
/* 124:132 */     return this;
/* 125:    */   }
/* 126:    */   
/* 127:    */   private void getBytes(int index, ByteBuffer dst, boolean internal)
/* 128:    */   {
/* 129:136 */     checkIndex(index);
/* 130:137 */     int bytesToCopy = Math.min(capacity() - index, dst.remaining());
/* 131:    */     ByteBuffer tmpBuf;
/* 132:    */     ByteBuffer tmpBuf;
/* 133:139 */     if (internal) {
/* 134:140 */       tmpBuf = internalNioBuffer();
/* 135:    */     } else {
/* 136:142 */       tmpBuf = ((ByteBuffer)this.memory).duplicate();
/* 137:    */     }
/* 138:144 */     index = idx(index);
/* 139:145 */     tmpBuf.clear().position(index).limit(index + bytesToCopy);
/* 140:146 */     dst.put(tmpBuf);
/* 141:    */   }
/* 142:    */   
/* 143:    */   public ByteBuf readBytes(ByteBuffer dst)
/* 144:    */   {
/* 145:151 */     int length = dst.remaining();
/* 146:152 */     checkReadableBytes(length);
/* 147:153 */     getBytes(this.readerIndex, dst, true);
/* 148:154 */     this.readerIndex += length;
/* 149:155 */     return this;
/* 150:    */   }
/* 151:    */   
/* 152:    */   public ByteBuf getBytes(int index, OutputStream out, int length)
/* 153:    */     throws IOException
/* 154:    */   {
/* 155:160 */     getBytes(index, out, length, false);
/* 156:161 */     return this;
/* 157:    */   }
/* 158:    */   
/* 159:    */   private void getBytes(int index, OutputStream out, int length, boolean internal)
/* 160:    */     throws IOException
/* 161:    */   {
/* 162:165 */     checkIndex(index, length);
/* 163:166 */     if (length == 0) {
/* 164:167 */       return;
/* 165:    */     }
/* 166:170 */     byte[] tmp = new byte[length];
/* 167:    */     ByteBuffer tmpBuf;
/* 168:    */     ByteBuffer tmpBuf;
/* 169:172 */     if (internal) {
/* 170:173 */       tmpBuf = internalNioBuffer();
/* 171:    */     } else {
/* 172:175 */       tmpBuf = ((ByteBuffer)this.memory).duplicate();
/* 173:    */     }
/* 174:177 */     tmpBuf.clear().position(idx(index));
/* 175:178 */     tmpBuf.get(tmp);
/* 176:179 */     out.write(tmp);
/* 177:    */   }
/* 178:    */   
/* 179:    */   public ByteBuf readBytes(OutputStream out, int length)
/* 180:    */     throws IOException
/* 181:    */   {
/* 182:184 */     checkReadableBytes(length);
/* 183:185 */     getBytes(this.readerIndex, out, length, true);
/* 184:186 */     this.readerIndex += length;
/* 185:187 */     return this;
/* 186:    */   }
/* 187:    */   
/* 188:    */   public int getBytes(int index, GatheringByteChannel out, int length)
/* 189:    */     throws IOException
/* 190:    */   {
/* 191:192 */     return getBytes(index, out, length, false);
/* 192:    */   }
/* 193:    */   
/* 194:    */   private int getBytes(int index, GatheringByteChannel out, int length, boolean internal)
/* 195:    */     throws IOException
/* 196:    */   {
/* 197:196 */     checkIndex(index, length);
/* 198:197 */     if (length == 0) {
/* 199:198 */       return 0;
/* 200:    */     }
/* 201:    */     ByteBuffer tmpBuf;
/* 202:    */     ByteBuffer tmpBuf;
/* 203:202 */     if (internal) {
/* 204:203 */       tmpBuf = internalNioBuffer();
/* 205:    */     } else {
/* 206:205 */       tmpBuf = ((ByteBuffer)this.memory).duplicate();
/* 207:    */     }
/* 208:207 */     index = idx(index);
/* 209:208 */     tmpBuf.clear().position(index).limit(index + length);
/* 210:209 */     return out.write(tmpBuf);
/* 211:    */   }
/* 212:    */   
/* 213:    */   public int readBytes(GatheringByteChannel out, int length)
/* 214:    */     throws IOException
/* 215:    */   {
/* 216:214 */     checkReadableBytes(length);
/* 217:215 */     int readBytes = getBytes(this.readerIndex, out, length, true);
/* 218:216 */     this.readerIndex += readBytes;
/* 219:217 */     return readBytes;
/* 220:    */   }
/* 221:    */   
/* 222:    */   protected void _setByte(int index, int value)
/* 223:    */   {
/* 224:222 */     ((ByteBuffer)this.memory).put(idx(index), (byte)value);
/* 225:    */   }
/* 226:    */   
/* 227:    */   protected void _setShort(int index, int value)
/* 228:    */   {
/* 229:227 */     ((ByteBuffer)this.memory).putShort(idx(index), (short)value);
/* 230:    */   }
/* 231:    */   
/* 232:    */   protected void _setMedium(int index, int value)
/* 233:    */   {
/* 234:232 */     index = idx(index);
/* 235:233 */     ((ByteBuffer)this.memory).put(index, (byte)(value >>> 16));
/* 236:234 */     ((ByteBuffer)this.memory).put(index + 1, (byte)(value >>> 8));
/* 237:235 */     ((ByteBuffer)this.memory).put(index + 2, (byte)value);
/* 238:    */   }
/* 239:    */   
/* 240:    */   protected void _setInt(int index, int value)
/* 241:    */   {
/* 242:240 */     ((ByteBuffer)this.memory).putInt(idx(index), value);
/* 243:    */   }
/* 244:    */   
/* 245:    */   protected void _setLong(int index, long value)
/* 246:    */   {
/* 247:245 */     ((ByteBuffer)this.memory).putLong(idx(index), value);
/* 248:    */   }
/* 249:    */   
/* 250:    */   public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/* 251:    */   {
/* 252:250 */     checkSrcIndex(index, length, srcIndex, src.capacity());
/* 253:251 */     if (src.hasArray()) {
/* 254:252 */       setBytes(index, src.array(), src.arrayOffset() + srcIndex, length);
/* 255:253 */     } else if (src.nioBufferCount() > 0) {
/* 256:254 */       for (ByteBuffer bb : src.nioBuffers(srcIndex, length))
/* 257:    */       {
/* 258:255 */         int bbLen = bb.remaining();
/* 259:256 */         setBytes(index, bb);
/* 260:257 */         index += bbLen;
/* 261:    */       }
/* 262:    */     } else {
/* 263:260 */       src.getBytes(srcIndex, this, index, length);
/* 264:    */     }
/* 265:262 */     return this;
/* 266:    */   }
/* 267:    */   
/* 268:    */   public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/* 269:    */   {
/* 270:267 */     checkSrcIndex(index, length, srcIndex, src.length);
/* 271:268 */     ByteBuffer tmpBuf = internalNioBuffer();
/* 272:269 */     index = idx(index);
/* 273:270 */     tmpBuf.clear().position(index).limit(index + length);
/* 274:271 */     tmpBuf.put(src, srcIndex, length);
/* 275:272 */     return this;
/* 276:    */   }
/* 277:    */   
/* 278:    */   public ByteBuf setBytes(int index, ByteBuffer src)
/* 279:    */   {
/* 280:277 */     checkIndex(index, src.remaining());
/* 281:278 */     ByteBuffer tmpBuf = internalNioBuffer();
/* 282:279 */     if (src == tmpBuf) {
/* 283:280 */       src = src.duplicate();
/* 284:    */     }
/* 285:283 */     index = idx(index);
/* 286:284 */     tmpBuf.clear().position(index).limit(index + src.remaining());
/* 287:285 */     tmpBuf.put(src);
/* 288:286 */     return this;
/* 289:    */   }
/* 290:    */   
/* 291:    */   public int setBytes(int index, InputStream in, int length)
/* 292:    */     throws IOException
/* 293:    */   {
/* 294:291 */     checkIndex(index, length);
/* 295:292 */     byte[] tmp = new byte[length];
/* 296:293 */     int readBytes = in.read(tmp);
/* 297:294 */     if (readBytes <= 0) {
/* 298:295 */       return readBytes;
/* 299:    */     }
/* 300:297 */     ByteBuffer tmpBuf = internalNioBuffer();
/* 301:298 */     tmpBuf.clear().position(idx(index));
/* 302:299 */     tmpBuf.put(tmp, 0, readBytes);
/* 303:300 */     return readBytes;
/* 304:    */   }
/* 305:    */   
/* 306:    */   public int setBytes(int index, ScatteringByteChannel in, int length)
/* 307:    */     throws IOException
/* 308:    */   {
/* 309:305 */     checkIndex(index, length);
/* 310:306 */     ByteBuffer tmpBuf = internalNioBuffer();
/* 311:307 */     index = idx(index);
/* 312:308 */     tmpBuf.clear().position(index).limit(index + length);
/* 313:    */     try
/* 314:    */     {
/* 315:310 */       return in.read(tmpBuf);
/* 316:    */     }
/* 317:    */     catch (ClosedChannelException e) {}
/* 318:312 */     return -1;
/* 319:    */   }
/* 320:    */   
/* 321:    */   public ByteBuf copy(int index, int length)
/* 322:    */   {
/* 323:318 */     checkIndex(index, length);
/* 324:319 */     ByteBuf copy = alloc().directBuffer(length, maxCapacity());
/* 325:320 */     copy.writeBytes(this, index, length);
/* 326:321 */     return copy;
/* 327:    */   }
/* 328:    */   
/* 329:    */   public int nioBufferCount()
/* 330:    */   {
/* 331:326 */     return 1;
/* 332:    */   }
/* 333:    */   
/* 334:    */   public ByteBuffer nioBuffer(int index, int length)
/* 335:    */   {
/* 336:331 */     checkIndex(index, length);
/* 337:332 */     index = idx(index);
/* 338:333 */     return ((ByteBuffer)((ByteBuffer)this.memory).duplicate().position(index).limit(index + length)).slice();
/* 339:    */   }
/* 340:    */   
/* 341:    */   public ByteBuffer[] nioBuffers(int index, int length)
/* 342:    */   {
/* 343:338 */     return new ByteBuffer[] { nioBuffer(index, length) };
/* 344:    */   }
/* 345:    */   
/* 346:    */   public ByteBuffer internalNioBuffer(int index, int length)
/* 347:    */   {
/* 348:343 */     checkIndex(index, length);
/* 349:344 */     index = idx(index);
/* 350:345 */     return (ByteBuffer)internalNioBuffer().clear().position(index).limit(index + length);
/* 351:    */   }
/* 352:    */   
/* 353:    */   public boolean hasArray()
/* 354:    */   {
/* 355:350 */     return false;
/* 356:    */   }
/* 357:    */   
/* 358:    */   public byte[] array()
/* 359:    */   {
/* 360:355 */     throw new UnsupportedOperationException("direct buffer");
/* 361:    */   }
/* 362:    */   
/* 363:    */   public int arrayOffset()
/* 364:    */   {
/* 365:360 */     throw new UnsupportedOperationException("direct buffer");
/* 366:    */   }
/* 367:    */   
/* 368:    */   public boolean hasMemoryAddress()
/* 369:    */   {
/* 370:365 */     return false;
/* 371:    */   }
/* 372:    */   
/* 373:    */   public long memoryAddress()
/* 374:    */   {
/* 375:370 */     throw new UnsupportedOperationException();
/* 376:    */   }
/* 377:    */   
/* 378:    */   protected Recycler<?> recycler()
/* 379:    */   {
/* 380:375 */     return RECYCLER;
/* 381:    */   }
/* 382:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.PooledDirectByteBuf
 * JD-Core Version:    0.7.0.1
 */