/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.StringUtil;
/*   4:    */ import java.io.IOException;
/*   5:    */ import java.io.InputStream;
/*   6:    */ import java.io.OutputStream;
/*   7:    */ import java.nio.Buffer;
/*   8:    */ import java.nio.ByteBuffer;
/*   9:    */ import java.nio.ByteOrder;
/*  10:    */ import java.nio.ReadOnlyBufferException;
/*  11:    */ import java.nio.channels.GatheringByteChannel;
/*  12:    */ import java.nio.channels.ScatteringByteChannel;
/*  13:    */ 
/*  14:    */ class ReadOnlyByteBufferBuf
/*  15:    */   extends AbstractReferenceCountedByteBuf
/*  16:    */ {
/*  17:    */   protected final ByteBuffer buffer;
/*  18:    */   private final ByteBufAllocator allocator;
/*  19:    */   private ByteBuffer tmpNioBuf;
/*  20:    */   
/*  21:    */   public ReadOnlyByteBufferBuf(ByteBufAllocator allocator, ByteBuffer buffer)
/*  22:    */   {
/*  23: 40 */     super(buffer.remaining());
/*  24: 41 */     if (!buffer.isReadOnly()) {
/*  25: 42 */       throw new IllegalArgumentException("must be a readonly buffer: " + StringUtil.simpleClassName(buffer));
/*  26:    */     }
/*  27: 45 */     this.allocator = allocator;
/*  28: 46 */     this.buffer = buffer.slice().order(ByteOrder.BIG_ENDIAN);
/*  29: 47 */     writerIndex(this.buffer.limit());
/*  30:    */   }
/*  31:    */   
/*  32:    */   protected void deallocate() {}
/*  33:    */   
/*  34:    */   public byte getByte(int index)
/*  35:    */   {
/*  36: 55 */     ensureAccessible();
/*  37: 56 */     return _getByte(index);
/*  38:    */   }
/*  39:    */   
/*  40:    */   protected byte _getByte(int index)
/*  41:    */   {
/*  42: 61 */     return this.buffer.get(index);
/*  43:    */   }
/*  44:    */   
/*  45:    */   public short getShort(int index)
/*  46:    */   {
/*  47: 66 */     ensureAccessible();
/*  48: 67 */     return _getShort(index);
/*  49:    */   }
/*  50:    */   
/*  51:    */   protected short _getShort(int index)
/*  52:    */   {
/*  53: 72 */     return this.buffer.getShort(index);
/*  54:    */   }
/*  55:    */   
/*  56:    */   public int getUnsignedMedium(int index)
/*  57:    */   {
/*  58: 77 */     ensureAccessible();
/*  59: 78 */     return _getUnsignedMedium(index);
/*  60:    */   }
/*  61:    */   
/*  62:    */   protected int _getUnsignedMedium(int index)
/*  63:    */   {
/*  64: 83 */     return (getByte(index) & 0xFF) << 16 | (getByte(index + 1) & 0xFF) << 8 | getByte(index + 2) & 0xFF;
/*  65:    */   }
/*  66:    */   
/*  67:    */   public int getInt(int index)
/*  68:    */   {
/*  69: 88 */     ensureAccessible();
/*  70: 89 */     return _getInt(index);
/*  71:    */   }
/*  72:    */   
/*  73:    */   protected int _getInt(int index)
/*  74:    */   {
/*  75: 94 */     return this.buffer.getInt(index);
/*  76:    */   }
/*  77:    */   
/*  78:    */   public long getLong(int index)
/*  79:    */   {
/*  80: 99 */     ensureAccessible();
/*  81:100 */     return _getLong(index);
/*  82:    */   }
/*  83:    */   
/*  84:    */   protected long _getLong(int index)
/*  85:    */   {
/*  86:105 */     return this.buffer.getLong(index);
/*  87:    */   }
/*  88:    */   
/*  89:    */   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/*  90:    */   {
/*  91:110 */     checkDstIndex(index, length, dstIndex, dst.capacity());
/*  92:111 */     if (dst.hasArray()) {
/*  93:112 */       getBytes(index, dst.array(), dst.arrayOffset() + dstIndex, length);
/*  94:113 */     } else if (dst.nioBufferCount() > 0) {
/*  95:114 */       for (ByteBuffer bb : dst.nioBuffers(dstIndex, length))
/*  96:    */       {
/*  97:115 */         int bbLen = bb.remaining();
/*  98:116 */         getBytes(index, bb);
/*  99:117 */         index += bbLen;
/* 100:    */       }
/* 101:    */     } else {
/* 102:120 */       dst.setBytes(dstIndex, this, index, length);
/* 103:    */     }
/* 104:122 */     return this;
/* 105:    */   }
/* 106:    */   
/* 107:    */   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/* 108:    */   {
/* 109:127 */     checkDstIndex(index, length, dstIndex, dst.length);
/* 110:129 */     if ((dstIndex < 0) || (dstIndex > dst.length - length)) {
/* 111:130 */       throw new IndexOutOfBoundsException(String.format("dstIndex: %d, length: %d (expected: range(0, %d))", new Object[] { Integer.valueOf(dstIndex), Integer.valueOf(length), Integer.valueOf(dst.length) }));
/* 112:    */     }
/* 113:134 */     ByteBuffer tmpBuf = internalNioBuffer();
/* 114:135 */     tmpBuf.clear().position(index).limit(index + length);
/* 115:136 */     tmpBuf.get(dst, dstIndex, length);
/* 116:137 */     return this;
/* 117:    */   }
/* 118:    */   
/* 119:    */   public ByteBuf getBytes(int index, ByteBuffer dst)
/* 120:    */   {
/* 121:142 */     checkIndex(index);
/* 122:143 */     if (dst == null) {
/* 123:144 */       throw new NullPointerException("dst");
/* 124:    */     }
/* 125:147 */     int bytesToCopy = Math.min(capacity() - index, dst.remaining());
/* 126:148 */     ByteBuffer tmpBuf = internalNioBuffer();
/* 127:149 */     tmpBuf.clear().position(index).limit(index + bytesToCopy);
/* 128:150 */     dst.put(tmpBuf);
/* 129:151 */     return this;
/* 130:    */   }
/* 131:    */   
/* 132:    */   protected void _setByte(int index, int value)
/* 133:    */   {
/* 134:156 */     throw new ReadOnlyBufferException();
/* 135:    */   }
/* 136:    */   
/* 137:    */   protected void _setShort(int index, int value)
/* 138:    */   {
/* 139:161 */     throw new ReadOnlyBufferException();
/* 140:    */   }
/* 141:    */   
/* 142:    */   protected void _setMedium(int index, int value)
/* 143:    */   {
/* 144:166 */     throw new ReadOnlyBufferException();
/* 145:    */   }
/* 146:    */   
/* 147:    */   protected void _setInt(int index, int value)
/* 148:    */   {
/* 149:171 */     throw new ReadOnlyBufferException();
/* 150:    */   }
/* 151:    */   
/* 152:    */   protected void _setLong(int index, long value)
/* 153:    */   {
/* 154:176 */     throw new ReadOnlyBufferException();
/* 155:    */   }
/* 156:    */   
/* 157:    */   public int capacity()
/* 158:    */   {
/* 159:181 */     return maxCapacity();
/* 160:    */   }
/* 161:    */   
/* 162:    */   public ByteBuf capacity(int newCapacity)
/* 163:    */   {
/* 164:186 */     throw new ReadOnlyBufferException();
/* 165:    */   }
/* 166:    */   
/* 167:    */   public ByteBufAllocator alloc()
/* 168:    */   {
/* 169:191 */     return this.allocator;
/* 170:    */   }
/* 171:    */   
/* 172:    */   public ByteOrder order()
/* 173:    */   {
/* 174:196 */     return ByteOrder.BIG_ENDIAN;
/* 175:    */   }
/* 176:    */   
/* 177:    */   public ByteBuf unwrap()
/* 178:    */   {
/* 179:201 */     return null;
/* 180:    */   }
/* 181:    */   
/* 182:    */   public boolean isDirect()
/* 183:    */   {
/* 184:206 */     return this.buffer.isDirect();
/* 185:    */   }
/* 186:    */   
/* 187:    */   public ByteBuf getBytes(int index, OutputStream out, int length)
/* 188:    */     throws IOException
/* 189:    */   {
/* 190:211 */     ensureAccessible();
/* 191:212 */     if (length == 0) {
/* 192:213 */       return this;
/* 193:    */     }
/* 194:216 */     if (this.buffer.hasArray())
/* 195:    */     {
/* 196:217 */       out.write(this.buffer.array(), index + this.buffer.arrayOffset(), length);
/* 197:    */     }
/* 198:    */     else
/* 199:    */     {
/* 200:219 */       byte[] tmp = new byte[length];
/* 201:220 */       ByteBuffer tmpBuf = internalNioBuffer();
/* 202:221 */       tmpBuf.clear().position(index);
/* 203:222 */       tmpBuf.get(tmp);
/* 204:223 */       out.write(tmp);
/* 205:    */     }
/* 206:225 */     return this;
/* 207:    */   }
/* 208:    */   
/* 209:    */   public int getBytes(int index, GatheringByteChannel out, int length)
/* 210:    */     throws IOException
/* 211:    */   {
/* 212:230 */     ensureAccessible();
/* 213:231 */     if (length == 0) {
/* 214:232 */       return 0;
/* 215:    */     }
/* 216:235 */     ByteBuffer tmpBuf = internalNioBuffer();
/* 217:236 */     tmpBuf.clear().position(index).limit(index + length);
/* 218:237 */     return out.write(tmpBuf);
/* 219:    */   }
/* 220:    */   
/* 221:    */   public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/* 222:    */   {
/* 223:242 */     throw new ReadOnlyBufferException();
/* 224:    */   }
/* 225:    */   
/* 226:    */   public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/* 227:    */   {
/* 228:247 */     throw new ReadOnlyBufferException();
/* 229:    */   }
/* 230:    */   
/* 231:    */   public ByteBuf setBytes(int index, ByteBuffer src)
/* 232:    */   {
/* 233:252 */     throw new ReadOnlyBufferException();
/* 234:    */   }
/* 235:    */   
/* 236:    */   public int setBytes(int index, InputStream in, int length)
/* 237:    */     throws IOException
/* 238:    */   {
/* 239:257 */     throw new ReadOnlyBufferException();
/* 240:    */   }
/* 241:    */   
/* 242:    */   public int setBytes(int index, ScatteringByteChannel in, int length)
/* 243:    */     throws IOException
/* 244:    */   {
/* 245:262 */     throw new ReadOnlyBufferException();
/* 246:    */   }
/* 247:    */   
/* 248:    */   protected final ByteBuffer internalNioBuffer()
/* 249:    */   {
/* 250:266 */     ByteBuffer tmpNioBuf = this.tmpNioBuf;
/* 251:267 */     if (tmpNioBuf == null) {
/* 252:268 */       this.tmpNioBuf = (tmpNioBuf = this.buffer.duplicate());
/* 253:    */     }
/* 254:270 */     return tmpNioBuf;
/* 255:    */   }
/* 256:    */   
/* 257:    */   public ByteBuf copy(int index, int length)
/* 258:    */   {
/* 259:275 */     ensureAccessible();
/* 260:    */     ByteBuffer src;
/* 261:    */     try
/* 262:    */     {
/* 263:278 */       src = (ByteBuffer)internalNioBuffer().clear().position(index).limit(index + length);
/* 264:    */     }
/* 265:    */     catch (IllegalArgumentException e)
/* 266:    */     {
/* 267:280 */       throw new IndexOutOfBoundsException("Too many bytes to read - Need " + (index + length));
/* 268:    */     }
/* 269:283 */     ByteBuffer dst = ByteBuffer.allocateDirect(length);
/* 270:284 */     dst.put(src);
/* 271:285 */     dst.order(order());
/* 272:286 */     dst.clear();
/* 273:287 */     return new UnpooledDirectByteBuf(alloc(), dst, maxCapacity());
/* 274:    */   }
/* 275:    */   
/* 276:    */   public int nioBufferCount()
/* 277:    */   {
/* 278:292 */     return 1;
/* 279:    */   }
/* 280:    */   
/* 281:    */   public ByteBuffer[] nioBuffers(int index, int length)
/* 282:    */   {
/* 283:297 */     return new ByteBuffer[] { nioBuffer(index, length) };
/* 284:    */   }
/* 285:    */   
/* 286:    */   public ByteBuffer nioBuffer(int index, int length)
/* 287:    */   {
/* 288:302 */     return (ByteBuffer)this.buffer.duplicate().position(index).limit(index + length);
/* 289:    */   }
/* 290:    */   
/* 291:    */   public ByteBuffer internalNioBuffer(int index, int length)
/* 292:    */   {
/* 293:307 */     ensureAccessible();
/* 294:308 */     return (ByteBuffer)internalNioBuffer().clear().position(index).limit(index + length);
/* 295:    */   }
/* 296:    */   
/* 297:    */   public boolean hasArray()
/* 298:    */   {
/* 299:313 */     return this.buffer.hasArray();
/* 300:    */   }
/* 301:    */   
/* 302:    */   public byte[] array()
/* 303:    */   {
/* 304:318 */     return this.buffer.array();
/* 305:    */   }
/* 306:    */   
/* 307:    */   public int arrayOffset()
/* 308:    */   {
/* 309:323 */     return this.buffer.arrayOffset();
/* 310:    */   }
/* 311:    */   
/* 312:    */   public boolean hasMemoryAddress()
/* 313:    */   {
/* 314:328 */     return false;
/* 315:    */   }
/* 316:    */   
/* 317:    */   public long memoryAddress()
/* 318:    */   {
/* 319:333 */     throw new UnsupportedOperationException();
/* 320:    */   }
/* 321:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.ReadOnlyByteBufferBuf
 * JD-Core Version:    0.7.0.1
 */