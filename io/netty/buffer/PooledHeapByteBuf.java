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
/*  11:    */ import java.nio.channels.ClosedChannelException;
/*  12:    */ import java.nio.channels.GatheringByteChannel;
/*  13:    */ import java.nio.channels.ScatteringByteChannel;
/*  14:    */ 
/*  15:    */ final class PooledHeapByteBuf
/*  16:    */   extends PooledByteBuf<byte[]>
/*  17:    */ {
/*  18: 30 */   private static final Recycler<PooledHeapByteBuf> RECYCLER = new Recycler()
/*  19:    */   {
/*  20:    */     protected PooledHeapByteBuf newObject(Recycler.Handle handle)
/*  21:    */     {
/*  22: 33 */       return new PooledHeapByteBuf(handle, 0, null);
/*  23:    */     }
/*  24:    */   };
/*  25:    */   
/*  26:    */   static PooledHeapByteBuf newInstance(int maxCapacity)
/*  27:    */   {
/*  28: 38 */     PooledHeapByteBuf buf = (PooledHeapByteBuf)RECYCLER.get();
/*  29: 39 */     buf.setRefCnt(1);
/*  30: 40 */     buf.maxCapacity(maxCapacity);
/*  31: 41 */     return buf;
/*  32:    */   }
/*  33:    */   
/*  34:    */   private PooledHeapByteBuf(Recycler.Handle recyclerHandle, int maxCapacity)
/*  35:    */   {
/*  36: 45 */     super(recyclerHandle, maxCapacity);
/*  37:    */   }
/*  38:    */   
/*  39:    */   public boolean isDirect()
/*  40:    */   {
/*  41: 50 */     return false;
/*  42:    */   }
/*  43:    */   
/*  44:    */   protected byte _getByte(int index)
/*  45:    */   {
/*  46: 55 */     return ((byte[])this.memory)[idx(index)];
/*  47:    */   }
/*  48:    */   
/*  49:    */   protected short _getShort(int index)
/*  50:    */   {
/*  51: 60 */     index = idx(index);
/*  52: 61 */     return (short)(((byte[])this.memory)[index] << 8 | ((byte[])this.memory)[(index + 1)] & 0xFF);
/*  53:    */   }
/*  54:    */   
/*  55:    */   protected int _getUnsignedMedium(int index)
/*  56:    */   {
/*  57: 66 */     index = idx(index);
/*  58: 67 */     return (((byte[])this.memory)[index] & 0xFF) << 16 | (((byte[])this.memory)[(index + 1)] & 0xFF) << 8 | ((byte[])this.memory)[(index + 2)] & 0xFF;
/*  59:    */   }
/*  60:    */   
/*  61:    */   protected int _getInt(int index)
/*  62:    */   {
/*  63: 74 */     index = idx(index);
/*  64: 75 */     return (((byte[])this.memory)[index] & 0xFF) << 24 | (((byte[])this.memory)[(index + 1)] & 0xFF) << 16 | (((byte[])this.memory)[(index + 2)] & 0xFF) << 8 | ((byte[])this.memory)[(index + 3)] & 0xFF;
/*  65:    */   }
/*  66:    */   
/*  67:    */   protected long _getLong(int index)
/*  68:    */   {
/*  69: 83 */     index = idx(index);
/*  70: 84 */     return (((byte[])this.memory)[index] & 0xFF) << 56 | (((byte[])this.memory)[(index + 1)] & 0xFF) << 48 | (((byte[])this.memory)[(index + 2)] & 0xFF) << 40 | (((byte[])this.memory)[(index + 3)] & 0xFF) << 32 | (((byte[])this.memory)[(index + 4)] & 0xFF) << 24 | (((byte[])this.memory)[(index + 5)] & 0xFF) << 16 | (((byte[])this.memory)[(index + 6)] & 0xFF) << 8 | ((byte[])this.memory)[(index + 7)] & 0xFF;
/*  71:    */   }
/*  72:    */   
/*  73:    */   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/*  74:    */   {
/*  75: 96 */     checkDstIndex(index, length, dstIndex, dst.capacity());
/*  76: 97 */     if (dst.hasMemoryAddress()) {
/*  77: 98 */       PlatformDependent.copyMemory((byte[])this.memory, idx(index), dst.memoryAddress() + dstIndex, length);
/*  78: 99 */     } else if (dst.hasArray()) {
/*  79:100 */       getBytes(index, dst.array(), dst.arrayOffset() + dstIndex, length);
/*  80:    */     } else {
/*  81:102 */       dst.setBytes(dstIndex, (byte[])this.memory, idx(index), length);
/*  82:    */     }
/*  83:104 */     return this;
/*  84:    */   }
/*  85:    */   
/*  86:    */   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/*  87:    */   {
/*  88:109 */     checkDstIndex(index, length, dstIndex, dst.length);
/*  89:110 */     System.arraycopy(this.memory, idx(index), dst, dstIndex, length);
/*  90:111 */     return this;
/*  91:    */   }
/*  92:    */   
/*  93:    */   public ByteBuf getBytes(int index, ByteBuffer dst)
/*  94:    */   {
/*  95:116 */     checkIndex(index);
/*  96:117 */     dst.put((byte[])this.memory, idx(index), Math.min(capacity() - index, dst.remaining()));
/*  97:118 */     return this;
/*  98:    */   }
/*  99:    */   
/* 100:    */   public ByteBuf getBytes(int index, OutputStream out, int length)
/* 101:    */     throws IOException
/* 102:    */   {
/* 103:123 */     checkIndex(index, length);
/* 104:124 */     out.write((byte[])this.memory, idx(index), length);
/* 105:125 */     return this;
/* 106:    */   }
/* 107:    */   
/* 108:    */   public int getBytes(int index, GatheringByteChannel out, int length)
/* 109:    */     throws IOException
/* 110:    */   {
/* 111:130 */     return getBytes(index, out, length, false);
/* 112:    */   }
/* 113:    */   
/* 114:    */   private int getBytes(int index, GatheringByteChannel out, int length, boolean internal)
/* 115:    */     throws IOException
/* 116:    */   {
/* 117:134 */     checkIndex(index, length);
/* 118:135 */     index = idx(index);
/* 119:    */     ByteBuffer tmpBuf;
/* 120:    */     ByteBuffer tmpBuf;
/* 121:137 */     if (internal) {
/* 122:138 */       tmpBuf = internalNioBuffer();
/* 123:    */     } else {
/* 124:140 */       tmpBuf = ByteBuffer.wrap((byte[])this.memory);
/* 125:    */     }
/* 126:142 */     return out.write((ByteBuffer)tmpBuf.clear().position(index).limit(index + length));
/* 127:    */   }
/* 128:    */   
/* 129:    */   public int readBytes(GatheringByteChannel out, int length)
/* 130:    */     throws IOException
/* 131:    */   {
/* 132:147 */     checkReadableBytes(length);
/* 133:148 */     int readBytes = getBytes(this.readerIndex, out, length, true);
/* 134:149 */     this.readerIndex += readBytes;
/* 135:150 */     return readBytes;
/* 136:    */   }
/* 137:    */   
/* 138:    */   protected void _setByte(int index, int value)
/* 139:    */   {
/* 140:155 */     ((byte[])this.memory)[idx(index)] = ((byte)value);
/* 141:    */   }
/* 142:    */   
/* 143:    */   protected void _setShort(int index, int value)
/* 144:    */   {
/* 145:160 */     index = idx(index);
/* 146:161 */     ((byte[])this.memory)[index] = ((byte)(value >>> 8));
/* 147:162 */     ((byte[])this.memory)[(index + 1)] = ((byte)value);
/* 148:    */   }
/* 149:    */   
/* 150:    */   protected void _setMedium(int index, int value)
/* 151:    */   {
/* 152:167 */     index = idx(index);
/* 153:168 */     ((byte[])this.memory)[index] = ((byte)(value >>> 16));
/* 154:169 */     ((byte[])this.memory)[(index + 1)] = ((byte)(value >>> 8));
/* 155:170 */     ((byte[])this.memory)[(index + 2)] = ((byte)value);
/* 156:    */   }
/* 157:    */   
/* 158:    */   protected void _setInt(int index, int value)
/* 159:    */   {
/* 160:175 */     index = idx(index);
/* 161:176 */     ((byte[])this.memory)[index] = ((byte)(value >>> 24));
/* 162:177 */     ((byte[])this.memory)[(index + 1)] = ((byte)(value >>> 16));
/* 163:178 */     ((byte[])this.memory)[(index + 2)] = ((byte)(value >>> 8));
/* 164:179 */     ((byte[])this.memory)[(index + 3)] = ((byte)value);
/* 165:    */   }
/* 166:    */   
/* 167:    */   protected void _setLong(int index, long value)
/* 168:    */   {
/* 169:184 */     index = idx(index);
/* 170:185 */     ((byte[])this.memory)[index] = ((byte)(int)(value >>> 56));
/* 171:186 */     ((byte[])this.memory)[(index + 1)] = ((byte)(int)(value >>> 48));
/* 172:187 */     ((byte[])this.memory)[(index + 2)] = ((byte)(int)(value >>> 40));
/* 173:188 */     ((byte[])this.memory)[(index + 3)] = ((byte)(int)(value >>> 32));
/* 174:189 */     ((byte[])this.memory)[(index + 4)] = ((byte)(int)(value >>> 24));
/* 175:190 */     ((byte[])this.memory)[(index + 5)] = ((byte)(int)(value >>> 16));
/* 176:191 */     ((byte[])this.memory)[(index + 6)] = ((byte)(int)(value >>> 8));
/* 177:192 */     ((byte[])this.memory)[(index + 7)] = ((byte)(int)value);
/* 178:    */   }
/* 179:    */   
/* 180:    */   public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/* 181:    */   {
/* 182:197 */     checkSrcIndex(index, length, srcIndex, src.capacity());
/* 183:198 */     if (src.hasMemoryAddress()) {
/* 184:199 */       PlatformDependent.copyMemory(src.memoryAddress() + srcIndex, (byte[])this.memory, idx(index), length);
/* 185:200 */     } else if (src.hasArray()) {
/* 186:201 */       setBytes(index, src.array(), src.arrayOffset() + srcIndex, length);
/* 187:    */     } else {
/* 188:203 */       src.getBytes(srcIndex, (byte[])this.memory, idx(index), length);
/* 189:    */     }
/* 190:205 */     return this;
/* 191:    */   }
/* 192:    */   
/* 193:    */   public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/* 194:    */   {
/* 195:210 */     checkSrcIndex(index, length, srcIndex, src.length);
/* 196:211 */     System.arraycopy(src, srcIndex, this.memory, idx(index), length);
/* 197:212 */     return this;
/* 198:    */   }
/* 199:    */   
/* 200:    */   public ByteBuf setBytes(int index, ByteBuffer src)
/* 201:    */   {
/* 202:217 */     int length = src.remaining();
/* 203:218 */     checkIndex(index, length);
/* 204:219 */     src.get((byte[])this.memory, idx(index), length);
/* 205:220 */     return this;
/* 206:    */   }
/* 207:    */   
/* 208:    */   public int setBytes(int index, InputStream in, int length)
/* 209:    */     throws IOException
/* 210:    */   {
/* 211:225 */     checkIndex(index, length);
/* 212:226 */     return in.read((byte[])this.memory, idx(index), length);
/* 213:    */   }
/* 214:    */   
/* 215:    */   public int setBytes(int index, ScatteringByteChannel in, int length)
/* 216:    */     throws IOException
/* 217:    */   {
/* 218:231 */     checkIndex(index, length);
/* 219:232 */     index = idx(index);
/* 220:    */     try
/* 221:    */     {
/* 222:234 */       return in.read((ByteBuffer)internalNioBuffer().clear().position(index).limit(index + length));
/* 223:    */     }
/* 224:    */     catch (ClosedChannelException e) {}
/* 225:236 */     return -1;
/* 226:    */   }
/* 227:    */   
/* 228:    */   public ByteBuf copy(int index, int length)
/* 229:    */   {
/* 230:242 */     checkIndex(index, length);
/* 231:243 */     ByteBuf copy = alloc().heapBuffer(length, maxCapacity());
/* 232:244 */     copy.writeBytes((byte[])this.memory, idx(index), length);
/* 233:245 */     return copy;
/* 234:    */   }
/* 235:    */   
/* 236:    */   public int nioBufferCount()
/* 237:    */   {
/* 238:250 */     return 1;
/* 239:    */   }
/* 240:    */   
/* 241:    */   public ByteBuffer[] nioBuffers(int index, int length)
/* 242:    */   {
/* 243:255 */     return new ByteBuffer[] { nioBuffer(index, length) };
/* 244:    */   }
/* 245:    */   
/* 246:    */   public ByteBuffer nioBuffer(int index, int length)
/* 247:    */   {
/* 248:260 */     checkIndex(index, length);
/* 249:261 */     index = idx(index);
/* 250:262 */     ByteBuffer buf = ByteBuffer.wrap((byte[])this.memory, index, length);
/* 251:263 */     return buf.slice();
/* 252:    */   }
/* 253:    */   
/* 254:    */   public ByteBuffer internalNioBuffer(int index, int length)
/* 255:    */   {
/* 256:268 */     checkIndex(index, length);
/* 257:269 */     index = idx(index);
/* 258:270 */     return (ByteBuffer)internalNioBuffer().clear().position(index).limit(index + length);
/* 259:    */   }
/* 260:    */   
/* 261:    */   public boolean hasArray()
/* 262:    */   {
/* 263:275 */     return true;
/* 264:    */   }
/* 265:    */   
/* 266:    */   public byte[] array()
/* 267:    */   {
/* 268:280 */     return (byte[])this.memory;
/* 269:    */   }
/* 270:    */   
/* 271:    */   public int arrayOffset()
/* 272:    */   {
/* 273:285 */     return this.offset;
/* 274:    */   }
/* 275:    */   
/* 276:    */   public boolean hasMemoryAddress()
/* 277:    */   {
/* 278:290 */     return false;
/* 279:    */   }
/* 280:    */   
/* 281:    */   public long memoryAddress()
/* 282:    */   {
/* 283:295 */     throw new UnsupportedOperationException();
/* 284:    */   }
/* 285:    */   
/* 286:    */   protected ByteBuffer newInternalNioBuffer(byte[] memory)
/* 287:    */   {
/* 288:300 */     return ByteBuffer.wrap(memory);
/* 289:    */   }
/* 290:    */   
/* 291:    */   protected Recycler<?> recycler()
/* 292:    */   {
/* 293:305 */     return RECYCLER;
/* 294:    */   }
/* 295:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.PooledHeapByteBuf
 * JD-Core Version:    0.7.0.1
 */