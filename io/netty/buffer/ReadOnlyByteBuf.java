/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import java.io.InputStream;
/*   5:    */ import java.io.OutputStream;
/*   6:    */ import java.nio.ByteBuffer;
/*   7:    */ import java.nio.ByteOrder;
/*   8:    */ import java.nio.ReadOnlyBufferException;
/*   9:    */ import java.nio.channels.GatheringByteChannel;
/*  10:    */ import java.nio.channels.ScatteringByteChannel;
/*  11:    */ 
/*  12:    */ public class ReadOnlyByteBuf
/*  13:    */   extends AbstractDerivedByteBuf
/*  14:    */ {
/*  15:    */   private final ByteBuf buffer;
/*  16:    */   
/*  17:    */   public ReadOnlyByteBuf(ByteBuf buffer)
/*  18:    */   {
/*  19: 37 */     super(buffer.maxCapacity());
/*  20: 39 */     if (((buffer instanceof ReadOnlyByteBuf)) || ((buffer instanceof DuplicatedByteBuf))) {
/*  21: 40 */       this.buffer = buffer.unwrap();
/*  22:    */     } else {
/*  23: 42 */       this.buffer = buffer;
/*  24:    */     }
/*  25: 44 */     setIndex(buffer.readerIndex(), buffer.writerIndex());
/*  26:    */   }
/*  27:    */   
/*  28:    */   public boolean isWritable()
/*  29:    */   {
/*  30: 49 */     return false;
/*  31:    */   }
/*  32:    */   
/*  33:    */   public boolean isWritable(int numBytes)
/*  34:    */   {
/*  35: 54 */     return false;
/*  36:    */   }
/*  37:    */   
/*  38:    */   public ByteBuf unwrap()
/*  39:    */   {
/*  40: 59 */     return this.buffer;
/*  41:    */   }
/*  42:    */   
/*  43:    */   public ByteBufAllocator alloc()
/*  44:    */   {
/*  45: 64 */     return this.buffer.alloc();
/*  46:    */   }
/*  47:    */   
/*  48:    */   public ByteOrder order()
/*  49:    */   {
/*  50: 69 */     return this.buffer.order();
/*  51:    */   }
/*  52:    */   
/*  53:    */   public boolean isDirect()
/*  54:    */   {
/*  55: 74 */     return this.buffer.isDirect();
/*  56:    */   }
/*  57:    */   
/*  58:    */   public boolean hasArray()
/*  59:    */   {
/*  60: 79 */     return false;
/*  61:    */   }
/*  62:    */   
/*  63:    */   public byte[] array()
/*  64:    */   {
/*  65: 84 */     throw new ReadOnlyBufferException();
/*  66:    */   }
/*  67:    */   
/*  68:    */   public int arrayOffset()
/*  69:    */   {
/*  70: 89 */     throw new ReadOnlyBufferException();
/*  71:    */   }
/*  72:    */   
/*  73:    */   public boolean hasMemoryAddress()
/*  74:    */   {
/*  75: 94 */     return false;
/*  76:    */   }
/*  77:    */   
/*  78:    */   public long memoryAddress()
/*  79:    */   {
/*  80: 99 */     throw new ReadOnlyBufferException();
/*  81:    */   }
/*  82:    */   
/*  83:    */   public ByteBuf discardReadBytes()
/*  84:    */   {
/*  85:104 */     throw new ReadOnlyBufferException();
/*  86:    */   }
/*  87:    */   
/*  88:    */   public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/*  89:    */   {
/*  90:109 */     throw new ReadOnlyBufferException();
/*  91:    */   }
/*  92:    */   
/*  93:    */   public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/*  94:    */   {
/*  95:114 */     throw new ReadOnlyBufferException();
/*  96:    */   }
/*  97:    */   
/*  98:    */   public ByteBuf setBytes(int index, ByteBuffer src)
/*  99:    */   {
/* 100:119 */     throw new ReadOnlyBufferException();
/* 101:    */   }
/* 102:    */   
/* 103:    */   public ByteBuf setByte(int index, int value)
/* 104:    */   {
/* 105:124 */     throw new ReadOnlyBufferException();
/* 106:    */   }
/* 107:    */   
/* 108:    */   protected void _setByte(int index, int value)
/* 109:    */   {
/* 110:129 */     throw new ReadOnlyBufferException();
/* 111:    */   }
/* 112:    */   
/* 113:    */   public ByteBuf setShort(int index, int value)
/* 114:    */   {
/* 115:134 */     throw new ReadOnlyBufferException();
/* 116:    */   }
/* 117:    */   
/* 118:    */   protected void _setShort(int index, int value)
/* 119:    */   {
/* 120:139 */     throw new ReadOnlyBufferException();
/* 121:    */   }
/* 122:    */   
/* 123:    */   public ByteBuf setMedium(int index, int value)
/* 124:    */   {
/* 125:144 */     throw new ReadOnlyBufferException();
/* 126:    */   }
/* 127:    */   
/* 128:    */   protected void _setMedium(int index, int value)
/* 129:    */   {
/* 130:149 */     throw new ReadOnlyBufferException();
/* 131:    */   }
/* 132:    */   
/* 133:    */   public ByteBuf setInt(int index, int value)
/* 134:    */   {
/* 135:154 */     throw new ReadOnlyBufferException();
/* 136:    */   }
/* 137:    */   
/* 138:    */   protected void _setInt(int index, int value)
/* 139:    */   {
/* 140:159 */     throw new ReadOnlyBufferException();
/* 141:    */   }
/* 142:    */   
/* 143:    */   public ByteBuf setLong(int index, long value)
/* 144:    */   {
/* 145:164 */     throw new ReadOnlyBufferException();
/* 146:    */   }
/* 147:    */   
/* 148:    */   protected void _setLong(int index, long value)
/* 149:    */   {
/* 150:169 */     throw new ReadOnlyBufferException();
/* 151:    */   }
/* 152:    */   
/* 153:    */   public int setBytes(int index, InputStream in, int length)
/* 154:    */   {
/* 155:174 */     throw new ReadOnlyBufferException();
/* 156:    */   }
/* 157:    */   
/* 158:    */   public int setBytes(int index, ScatteringByteChannel in, int length)
/* 159:    */   {
/* 160:179 */     throw new ReadOnlyBufferException();
/* 161:    */   }
/* 162:    */   
/* 163:    */   public int getBytes(int index, GatheringByteChannel out, int length)
/* 164:    */     throws IOException
/* 165:    */   {
/* 166:185 */     return this.buffer.getBytes(index, out, length);
/* 167:    */   }
/* 168:    */   
/* 169:    */   public ByteBuf getBytes(int index, OutputStream out, int length)
/* 170:    */     throws IOException
/* 171:    */   {
/* 172:191 */     this.buffer.getBytes(index, out, length);
/* 173:192 */     return this;
/* 174:    */   }
/* 175:    */   
/* 176:    */   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/* 177:    */   {
/* 178:197 */     this.buffer.getBytes(index, dst, dstIndex, length);
/* 179:198 */     return this;
/* 180:    */   }
/* 181:    */   
/* 182:    */   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/* 183:    */   {
/* 184:203 */     this.buffer.getBytes(index, dst, dstIndex, length);
/* 185:204 */     return this;
/* 186:    */   }
/* 187:    */   
/* 188:    */   public ByteBuf getBytes(int index, ByteBuffer dst)
/* 189:    */   {
/* 190:209 */     this.buffer.getBytes(index, dst);
/* 191:210 */     return this;
/* 192:    */   }
/* 193:    */   
/* 194:    */   public ByteBuf duplicate()
/* 195:    */   {
/* 196:215 */     return new ReadOnlyByteBuf(this);
/* 197:    */   }
/* 198:    */   
/* 199:    */   public ByteBuf copy(int index, int length)
/* 200:    */   {
/* 201:220 */     return this.buffer.copy(index, length);
/* 202:    */   }
/* 203:    */   
/* 204:    */   public ByteBuf slice(int index, int length)
/* 205:    */   {
/* 206:225 */     return Unpooled.unmodifiableBuffer(this.buffer.slice(index, length));
/* 207:    */   }
/* 208:    */   
/* 209:    */   public byte getByte(int index)
/* 210:    */   {
/* 211:230 */     return _getByte(index);
/* 212:    */   }
/* 213:    */   
/* 214:    */   protected byte _getByte(int index)
/* 215:    */   {
/* 216:235 */     return this.buffer.getByte(index);
/* 217:    */   }
/* 218:    */   
/* 219:    */   public short getShort(int index)
/* 220:    */   {
/* 221:240 */     return _getShort(index);
/* 222:    */   }
/* 223:    */   
/* 224:    */   protected short _getShort(int index)
/* 225:    */   {
/* 226:245 */     return this.buffer.getShort(index);
/* 227:    */   }
/* 228:    */   
/* 229:    */   public int getUnsignedMedium(int index)
/* 230:    */   {
/* 231:250 */     return _getUnsignedMedium(index);
/* 232:    */   }
/* 233:    */   
/* 234:    */   protected int _getUnsignedMedium(int index)
/* 235:    */   {
/* 236:255 */     return this.buffer.getUnsignedMedium(index);
/* 237:    */   }
/* 238:    */   
/* 239:    */   public int getInt(int index)
/* 240:    */   {
/* 241:260 */     return _getInt(index);
/* 242:    */   }
/* 243:    */   
/* 244:    */   protected int _getInt(int index)
/* 245:    */   {
/* 246:265 */     return this.buffer.getInt(index);
/* 247:    */   }
/* 248:    */   
/* 249:    */   public long getLong(int index)
/* 250:    */   {
/* 251:270 */     return _getLong(index);
/* 252:    */   }
/* 253:    */   
/* 254:    */   protected long _getLong(int index)
/* 255:    */   {
/* 256:275 */     return this.buffer.getLong(index);
/* 257:    */   }
/* 258:    */   
/* 259:    */   public int nioBufferCount()
/* 260:    */   {
/* 261:280 */     return this.buffer.nioBufferCount();
/* 262:    */   }
/* 263:    */   
/* 264:    */   public ByteBuffer nioBuffer(int index, int length)
/* 265:    */   {
/* 266:285 */     return this.buffer.nioBuffer(index, length).asReadOnlyBuffer();
/* 267:    */   }
/* 268:    */   
/* 269:    */   public ByteBuffer[] nioBuffers(int index, int length)
/* 270:    */   {
/* 271:290 */     return this.buffer.nioBuffers(index, length);
/* 272:    */   }
/* 273:    */   
/* 274:    */   public ByteBuffer internalNioBuffer(int index, int length)
/* 275:    */   {
/* 276:295 */     return nioBuffer(index, length);
/* 277:    */   }
/* 278:    */   
/* 279:    */   public int forEachByte(int index, int length, ByteBufProcessor processor)
/* 280:    */   {
/* 281:300 */     return this.buffer.forEachByte(index, length, processor);
/* 282:    */   }
/* 283:    */   
/* 284:    */   public int forEachByteDesc(int index, int length, ByteBufProcessor processor)
/* 285:    */   {
/* 286:305 */     return this.buffer.forEachByteDesc(index, length, processor);
/* 287:    */   }
/* 288:    */   
/* 289:    */   public int capacity()
/* 290:    */   {
/* 291:310 */     return this.buffer.capacity();
/* 292:    */   }
/* 293:    */   
/* 294:    */   public ByteBuf capacity(int newCapacity)
/* 295:    */   {
/* 296:315 */     throw new ReadOnlyBufferException();
/* 297:    */   }
/* 298:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.ReadOnlyByteBuf
 * JD-Core Version:    0.7.0.1
 */