/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import java.io.InputStream;
/*   5:    */ import java.io.OutputStream;
/*   6:    */ import java.nio.ByteBuffer;
/*   7:    */ import java.nio.ByteOrder;
/*   8:    */ import java.nio.channels.GatheringByteChannel;
/*   9:    */ import java.nio.channels.ScatteringByteChannel;
/*  10:    */ 
/*  11:    */ public class SlicedByteBuf
/*  12:    */   extends AbstractDerivedByteBuf
/*  13:    */ {
/*  14:    */   private final ByteBuf buffer;
/*  15:    */   private final int adjustment;
/*  16:    */   private final int length;
/*  17:    */   
/*  18:    */   public SlicedByteBuf(ByteBuf buffer, int index, int length)
/*  19:    */   {
/*  20: 40 */     super(length);
/*  21: 41 */     if ((index < 0) || (index > buffer.capacity() - length)) {
/*  22: 42 */       throw new IndexOutOfBoundsException(buffer.toString() + ".slice(" + index + ", " + length + ')');
/*  23:    */     }
/*  24: 45 */     if ((buffer instanceof SlicedByteBuf))
/*  25:    */     {
/*  26: 46 */       this.buffer = ((SlicedByteBuf)buffer).buffer;
/*  27: 47 */       this.adjustment = (((SlicedByteBuf)buffer).adjustment + index);
/*  28:    */     }
/*  29: 48 */     else if ((buffer instanceof DuplicatedByteBuf))
/*  30:    */     {
/*  31: 49 */       this.buffer = buffer.unwrap();
/*  32: 50 */       this.adjustment = index;
/*  33:    */     }
/*  34:    */     else
/*  35:    */     {
/*  36: 52 */       this.buffer = buffer;
/*  37: 53 */       this.adjustment = index;
/*  38:    */     }
/*  39: 55 */     this.length = length;
/*  40:    */     
/*  41: 57 */     writerIndex(length);
/*  42:    */   }
/*  43:    */   
/*  44:    */   public ByteBuf unwrap()
/*  45:    */   {
/*  46: 62 */     return this.buffer;
/*  47:    */   }
/*  48:    */   
/*  49:    */   public ByteBufAllocator alloc()
/*  50:    */   {
/*  51: 67 */     return this.buffer.alloc();
/*  52:    */   }
/*  53:    */   
/*  54:    */   public ByteOrder order()
/*  55:    */   {
/*  56: 72 */     return this.buffer.order();
/*  57:    */   }
/*  58:    */   
/*  59:    */   public boolean isDirect()
/*  60:    */   {
/*  61: 77 */     return this.buffer.isDirect();
/*  62:    */   }
/*  63:    */   
/*  64:    */   public int capacity()
/*  65:    */   {
/*  66: 82 */     return this.length;
/*  67:    */   }
/*  68:    */   
/*  69:    */   public ByteBuf capacity(int newCapacity)
/*  70:    */   {
/*  71: 87 */     throw new UnsupportedOperationException("sliced buffer");
/*  72:    */   }
/*  73:    */   
/*  74:    */   public boolean hasArray()
/*  75:    */   {
/*  76: 92 */     return this.buffer.hasArray();
/*  77:    */   }
/*  78:    */   
/*  79:    */   public byte[] array()
/*  80:    */   {
/*  81: 97 */     return this.buffer.array();
/*  82:    */   }
/*  83:    */   
/*  84:    */   public int arrayOffset()
/*  85:    */   {
/*  86:102 */     return this.buffer.arrayOffset() + this.adjustment;
/*  87:    */   }
/*  88:    */   
/*  89:    */   public boolean hasMemoryAddress()
/*  90:    */   {
/*  91:107 */     return this.buffer.hasMemoryAddress();
/*  92:    */   }
/*  93:    */   
/*  94:    */   public long memoryAddress()
/*  95:    */   {
/*  96:112 */     return this.buffer.memoryAddress() + this.adjustment;
/*  97:    */   }
/*  98:    */   
/*  99:    */   protected byte _getByte(int index)
/* 100:    */   {
/* 101:117 */     return this.buffer.getByte(index + this.adjustment);
/* 102:    */   }
/* 103:    */   
/* 104:    */   protected short _getShort(int index)
/* 105:    */   {
/* 106:122 */     return this.buffer.getShort(index + this.adjustment);
/* 107:    */   }
/* 108:    */   
/* 109:    */   protected int _getUnsignedMedium(int index)
/* 110:    */   {
/* 111:127 */     return this.buffer.getUnsignedMedium(index + this.adjustment);
/* 112:    */   }
/* 113:    */   
/* 114:    */   protected int _getInt(int index)
/* 115:    */   {
/* 116:132 */     return this.buffer.getInt(index + this.adjustment);
/* 117:    */   }
/* 118:    */   
/* 119:    */   protected long _getLong(int index)
/* 120:    */   {
/* 121:137 */     return this.buffer.getLong(index + this.adjustment);
/* 122:    */   }
/* 123:    */   
/* 124:    */   public ByteBuf duplicate()
/* 125:    */   {
/* 126:142 */     ByteBuf duplicate = this.buffer.slice(this.adjustment, this.length);
/* 127:143 */     duplicate.setIndex(readerIndex(), writerIndex());
/* 128:144 */     return duplicate;
/* 129:    */   }
/* 130:    */   
/* 131:    */   public ByteBuf copy(int index, int length)
/* 132:    */   {
/* 133:149 */     checkIndex(index, length);
/* 134:150 */     return this.buffer.copy(index + this.adjustment, length);
/* 135:    */   }
/* 136:    */   
/* 137:    */   public ByteBuf slice(int index, int length)
/* 138:    */   {
/* 139:155 */     checkIndex(index, length);
/* 140:156 */     if (length == 0) {
/* 141:157 */       return Unpooled.EMPTY_BUFFER;
/* 142:    */     }
/* 143:159 */     return this.buffer.slice(index + this.adjustment, length);
/* 144:    */   }
/* 145:    */   
/* 146:    */   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/* 147:    */   {
/* 148:164 */     checkIndex(index, length);
/* 149:165 */     this.buffer.getBytes(index + this.adjustment, dst, dstIndex, length);
/* 150:166 */     return this;
/* 151:    */   }
/* 152:    */   
/* 153:    */   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/* 154:    */   {
/* 155:171 */     checkIndex(index, length);
/* 156:172 */     this.buffer.getBytes(index + this.adjustment, dst, dstIndex, length);
/* 157:173 */     return this;
/* 158:    */   }
/* 159:    */   
/* 160:    */   public ByteBuf getBytes(int index, ByteBuffer dst)
/* 161:    */   {
/* 162:178 */     checkIndex(index, dst.remaining());
/* 163:179 */     this.buffer.getBytes(index + this.adjustment, dst);
/* 164:180 */     return this;
/* 165:    */   }
/* 166:    */   
/* 167:    */   protected void _setByte(int index, int value)
/* 168:    */   {
/* 169:185 */     this.buffer.setByte(index + this.adjustment, value);
/* 170:    */   }
/* 171:    */   
/* 172:    */   protected void _setShort(int index, int value)
/* 173:    */   {
/* 174:190 */     this.buffer.setShort(index + this.adjustment, value);
/* 175:    */   }
/* 176:    */   
/* 177:    */   protected void _setMedium(int index, int value)
/* 178:    */   {
/* 179:195 */     this.buffer.setMedium(index + this.adjustment, value);
/* 180:    */   }
/* 181:    */   
/* 182:    */   protected void _setInt(int index, int value)
/* 183:    */   {
/* 184:200 */     this.buffer.setInt(index + this.adjustment, value);
/* 185:    */   }
/* 186:    */   
/* 187:    */   protected void _setLong(int index, long value)
/* 188:    */   {
/* 189:205 */     this.buffer.setLong(index + this.adjustment, value);
/* 190:    */   }
/* 191:    */   
/* 192:    */   public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/* 193:    */   {
/* 194:210 */     checkIndex(index, length);
/* 195:211 */     this.buffer.setBytes(index + this.adjustment, src, srcIndex, length);
/* 196:212 */     return this;
/* 197:    */   }
/* 198:    */   
/* 199:    */   public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/* 200:    */   {
/* 201:217 */     checkIndex(index, length);
/* 202:218 */     this.buffer.setBytes(index + this.adjustment, src, srcIndex, length);
/* 203:219 */     return this;
/* 204:    */   }
/* 205:    */   
/* 206:    */   public ByteBuf setBytes(int index, ByteBuffer src)
/* 207:    */   {
/* 208:224 */     checkIndex(index, src.remaining());
/* 209:225 */     this.buffer.setBytes(index + this.adjustment, src);
/* 210:226 */     return this;
/* 211:    */   }
/* 212:    */   
/* 213:    */   public ByteBuf getBytes(int index, OutputStream out, int length)
/* 214:    */     throws IOException
/* 215:    */   {
/* 216:231 */     checkIndex(index, length);
/* 217:232 */     this.buffer.getBytes(index + this.adjustment, out, length);
/* 218:233 */     return this;
/* 219:    */   }
/* 220:    */   
/* 221:    */   public int getBytes(int index, GatheringByteChannel out, int length)
/* 222:    */     throws IOException
/* 223:    */   {
/* 224:238 */     checkIndex(index, length);
/* 225:239 */     return this.buffer.getBytes(index + this.adjustment, out, length);
/* 226:    */   }
/* 227:    */   
/* 228:    */   public int setBytes(int index, InputStream in, int length)
/* 229:    */     throws IOException
/* 230:    */   {
/* 231:244 */     checkIndex(index, length);
/* 232:245 */     return this.buffer.setBytes(index + this.adjustment, in, length);
/* 233:    */   }
/* 234:    */   
/* 235:    */   public int setBytes(int index, ScatteringByteChannel in, int length)
/* 236:    */     throws IOException
/* 237:    */   {
/* 238:250 */     checkIndex(index, length);
/* 239:251 */     return this.buffer.setBytes(index + this.adjustment, in, length);
/* 240:    */   }
/* 241:    */   
/* 242:    */   public int nioBufferCount()
/* 243:    */   {
/* 244:256 */     return this.buffer.nioBufferCount();
/* 245:    */   }
/* 246:    */   
/* 247:    */   public ByteBuffer nioBuffer(int index, int length)
/* 248:    */   {
/* 249:261 */     checkIndex(index, length);
/* 250:262 */     return this.buffer.nioBuffer(index + this.adjustment, length);
/* 251:    */   }
/* 252:    */   
/* 253:    */   public ByteBuffer[] nioBuffers(int index, int length)
/* 254:    */   {
/* 255:267 */     checkIndex(index, length);
/* 256:268 */     return this.buffer.nioBuffers(index + this.adjustment, length);
/* 257:    */   }
/* 258:    */   
/* 259:    */   public ByteBuffer internalNioBuffer(int index, int length)
/* 260:    */   {
/* 261:273 */     checkIndex(index, length);
/* 262:274 */     return nioBuffer(index, length);
/* 263:    */   }
/* 264:    */   
/* 265:    */   public int forEachByte(int index, int length, ByteBufProcessor processor)
/* 266:    */   {
/* 267:279 */     int ret = this.buffer.forEachByte(index + this.adjustment, length, processor);
/* 268:280 */     if (ret >= this.adjustment) {
/* 269:281 */       return ret - this.adjustment;
/* 270:    */     }
/* 271:283 */     return -1;
/* 272:    */   }
/* 273:    */   
/* 274:    */   public int forEachByteDesc(int index, int length, ByteBufProcessor processor)
/* 275:    */   {
/* 276:289 */     int ret = this.buffer.forEachByteDesc(index + this.adjustment, length, processor);
/* 277:290 */     if (ret >= this.adjustment) {
/* 278:291 */       return ret - this.adjustment;
/* 279:    */     }
/* 280:293 */     return -1;
/* 281:    */   }
/* 282:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.SlicedByteBuf
 * JD-Core Version:    0.7.0.1
 */