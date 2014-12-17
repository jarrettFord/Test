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
/*  11:    */ public class DuplicatedByteBuf
/*  12:    */   extends AbstractDerivedByteBuf
/*  13:    */ {
/*  14:    */   private final ByteBuf buffer;
/*  15:    */   
/*  16:    */   public DuplicatedByteBuf(ByteBuf buffer)
/*  17:    */   {
/*  18: 37 */     super(buffer.maxCapacity());
/*  19: 39 */     if ((buffer instanceof DuplicatedByteBuf)) {
/*  20: 40 */       this.buffer = ((DuplicatedByteBuf)buffer).buffer;
/*  21:    */     } else {
/*  22: 42 */       this.buffer = buffer;
/*  23:    */     }
/*  24: 45 */     setIndex(buffer.readerIndex(), buffer.writerIndex());
/*  25:    */   }
/*  26:    */   
/*  27:    */   public ByteBuf unwrap()
/*  28:    */   {
/*  29: 50 */     return this.buffer;
/*  30:    */   }
/*  31:    */   
/*  32:    */   public ByteBufAllocator alloc()
/*  33:    */   {
/*  34: 55 */     return this.buffer.alloc();
/*  35:    */   }
/*  36:    */   
/*  37:    */   public ByteOrder order()
/*  38:    */   {
/*  39: 60 */     return this.buffer.order();
/*  40:    */   }
/*  41:    */   
/*  42:    */   public boolean isDirect()
/*  43:    */   {
/*  44: 65 */     return this.buffer.isDirect();
/*  45:    */   }
/*  46:    */   
/*  47:    */   public int capacity()
/*  48:    */   {
/*  49: 70 */     return this.buffer.capacity();
/*  50:    */   }
/*  51:    */   
/*  52:    */   public ByteBuf capacity(int newCapacity)
/*  53:    */   {
/*  54: 75 */     this.buffer.capacity(newCapacity);
/*  55: 76 */     return this;
/*  56:    */   }
/*  57:    */   
/*  58:    */   public boolean hasArray()
/*  59:    */   {
/*  60: 81 */     return this.buffer.hasArray();
/*  61:    */   }
/*  62:    */   
/*  63:    */   public byte[] array()
/*  64:    */   {
/*  65: 86 */     return this.buffer.array();
/*  66:    */   }
/*  67:    */   
/*  68:    */   public int arrayOffset()
/*  69:    */   {
/*  70: 91 */     return this.buffer.arrayOffset();
/*  71:    */   }
/*  72:    */   
/*  73:    */   public boolean hasMemoryAddress()
/*  74:    */   {
/*  75: 96 */     return this.buffer.hasMemoryAddress();
/*  76:    */   }
/*  77:    */   
/*  78:    */   public long memoryAddress()
/*  79:    */   {
/*  80:101 */     return this.buffer.memoryAddress();
/*  81:    */   }
/*  82:    */   
/*  83:    */   public byte getByte(int index)
/*  84:    */   {
/*  85:106 */     return _getByte(index);
/*  86:    */   }
/*  87:    */   
/*  88:    */   protected byte _getByte(int index)
/*  89:    */   {
/*  90:111 */     return this.buffer.getByte(index);
/*  91:    */   }
/*  92:    */   
/*  93:    */   public short getShort(int index)
/*  94:    */   {
/*  95:116 */     return _getShort(index);
/*  96:    */   }
/*  97:    */   
/*  98:    */   protected short _getShort(int index)
/*  99:    */   {
/* 100:121 */     return this.buffer.getShort(index);
/* 101:    */   }
/* 102:    */   
/* 103:    */   public int getUnsignedMedium(int index)
/* 104:    */   {
/* 105:126 */     return _getUnsignedMedium(index);
/* 106:    */   }
/* 107:    */   
/* 108:    */   protected int _getUnsignedMedium(int index)
/* 109:    */   {
/* 110:131 */     return this.buffer.getUnsignedMedium(index);
/* 111:    */   }
/* 112:    */   
/* 113:    */   public int getInt(int index)
/* 114:    */   {
/* 115:136 */     return _getInt(index);
/* 116:    */   }
/* 117:    */   
/* 118:    */   protected int _getInt(int index)
/* 119:    */   {
/* 120:141 */     return this.buffer.getInt(index);
/* 121:    */   }
/* 122:    */   
/* 123:    */   public long getLong(int index)
/* 124:    */   {
/* 125:146 */     return _getLong(index);
/* 126:    */   }
/* 127:    */   
/* 128:    */   protected long _getLong(int index)
/* 129:    */   {
/* 130:151 */     return this.buffer.getLong(index);
/* 131:    */   }
/* 132:    */   
/* 133:    */   public ByteBuf copy(int index, int length)
/* 134:    */   {
/* 135:156 */     return this.buffer.copy(index, length);
/* 136:    */   }
/* 137:    */   
/* 138:    */   public ByteBuf slice(int index, int length)
/* 139:    */   {
/* 140:161 */     return this.buffer.slice(index, length);
/* 141:    */   }
/* 142:    */   
/* 143:    */   public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length)
/* 144:    */   {
/* 145:166 */     this.buffer.getBytes(index, dst, dstIndex, length);
/* 146:167 */     return this;
/* 147:    */   }
/* 148:    */   
/* 149:    */   public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length)
/* 150:    */   {
/* 151:172 */     this.buffer.getBytes(index, dst, dstIndex, length);
/* 152:173 */     return this;
/* 153:    */   }
/* 154:    */   
/* 155:    */   public ByteBuf getBytes(int index, ByteBuffer dst)
/* 156:    */   {
/* 157:178 */     this.buffer.getBytes(index, dst);
/* 158:179 */     return this;
/* 159:    */   }
/* 160:    */   
/* 161:    */   public ByteBuf setByte(int index, int value)
/* 162:    */   {
/* 163:184 */     _setByte(index, value);
/* 164:185 */     return this;
/* 165:    */   }
/* 166:    */   
/* 167:    */   protected void _setByte(int index, int value)
/* 168:    */   {
/* 169:190 */     this.buffer.setByte(index, value);
/* 170:    */   }
/* 171:    */   
/* 172:    */   public ByteBuf setShort(int index, int value)
/* 173:    */   {
/* 174:195 */     _setShort(index, value);
/* 175:196 */     return this;
/* 176:    */   }
/* 177:    */   
/* 178:    */   protected void _setShort(int index, int value)
/* 179:    */   {
/* 180:201 */     this.buffer.setShort(index, value);
/* 181:    */   }
/* 182:    */   
/* 183:    */   public ByteBuf setMedium(int index, int value)
/* 184:    */   {
/* 185:206 */     _setMedium(index, value);
/* 186:207 */     return this;
/* 187:    */   }
/* 188:    */   
/* 189:    */   protected void _setMedium(int index, int value)
/* 190:    */   {
/* 191:212 */     this.buffer.setMedium(index, value);
/* 192:    */   }
/* 193:    */   
/* 194:    */   public ByteBuf setInt(int index, int value)
/* 195:    */   {
/* 196:217 */     _setInt(index, value);
/* 197:218 */     return this;
/* 198:    */   }
/* 199:    */   
/* 200:    */   protected void _setInt(int index, int value)
/* 201:    */   {
/* 202:223 */     this.buffer.setInt(index, value);
/* 203:    */   }
/* 204:    */   
/* 205:    */   public ByteBuf setLong(int index, long value)
/* 206:    */   {
/* 207:228 */     _setLong(index, value);
/* 208:229 */     return this;
/* 209:    */   }
/* 210:    */   
/* 211:    */   protected void _setLong(int index, long value)
/* 212:    */   {
/* 213:234 */     this.buffer.setLong(index, value);
/* 214:    */   }
/* 215:    */   
/* 216:    */   public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length)
/* 217:    */   {
/* 218:239 */     this.buffer.setBytes(index, src, srcIndex, length);
/* 219:240 */     return this;
/* 220:    */   }
/* 221:    */   
/* 222:    */   public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length)
/* 223:    */   {
/* 224:245 */     this.buffer.setBytes(index, src, srcIndex, length);
/* 225:246 */     return this;
/* 226:    */   }
/* 227:    */   
/* 228:    */   public ByteBuf setBytes(int index, ByteBuffer src)
/* 229:    */   {
/* 230:251 */     this.buffer.setBytes(index, src);
/* 231:252 */     return this;
/* 232:    */   }
/* 233:    */   
/* 234:    */   public ByteBuf getBytes(int index, OutputStream out, int length)
/* 235:    */     throws IOException
/* 236:    */   {
/* 237:258 */     this.buffer.getBytes(index, out, length);
/* 238:259 */     return this;
/* 239:    */   }
/* 240:    */   
/* 241:    */   public int getBytes(int index, GatheringByteChannel out, int length)
/* 242:    */     throws IOException
/* 243:    */   {
/* 244:265 */     return this.buffer.getBytes(index, out, length);
/* 245:    */   }
/* 246:    */   
/* 247:    */   public int setBytes(int index, InputStream in, int length)
/* 248:    */     throws IOException
/* 249:    */   {
/* 250:271 */     return this.buffer.setBytes(index, in, length);
/* 251:    */   }
/* 252:    */   
/* 253:    */   public int setBytes(int index, ScatteringByteChannel in, int length)
/* 254:    */     throws IOException
/* 255:    */   {
/* 256:277 */     return this.buffer.setBytes(index, in, length);
/* 257:    */   }
/* 258:    */   
/* 259:    */   public int nioBufferCount()
/* 260:    */   {
/* 261:282 */     return this.buffer.nioBufferCount();
/* 262:    */   }
/* 263:    */   
/* 264:    */   public ByteBuffer[] nioBuffers(int index, int length)
/* 265:    */   {
/* 266:287 */     return this.buffer.nioBuffers(index, length);
/* 267:    */   }
/* 268:    */   
/* 269:    */   public ByteBuffer internalNioBuffer(int index, int length)
/* 270:    */   {
/* 271:292 */     return nioBuffer(index, length);
/* 272:    */   }
/* 273:    */   
/* 274:    */   public int forEachByte(int index, int length, ByteBufProcessor processor)
/* 275:    */   {
/* 276:297 */     return this.buffer.forEachByte(index, length, processor);
/* 277:    */   }
/* 278:    */   
/* 279:    */   public int forEachByteDesc(int index, int length, ByteBufProcessor processor)
/* 280:    */   {
/* 281:302 */     return this.buffer.forEachByteDesc(index, length, processor);
/* 282:    */   }
/* 283:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.DuplicatedByteBuf
 * JD-Core Version:    0.7.0.1
 */