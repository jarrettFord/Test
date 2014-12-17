/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import java.io.DataInput;
/*   4:    */ import java.io.DataInputStream;
/*   5:    */ import java.io.EOFException;
/*   6:    */ import java.io.IOException;
/*   7:    */ import java.io.InputStream;
/*   8:    */ 
/*   9:    */ public class ByteBufInputStream
/*  10:    */   extends InputStream
/*  11:    */   implements DataInput
/*  12:    */ {
/*  13:    */   private final ByteBuf buffer;
/*  14:    */   private final int startIndex;
/*  15:    */   private final int endIndex;
/*  16:    */   
/*  17:    */   public ByteBufInputStream(ByteBuf buffer)
/*  18:    */   {
/*  19: 52 */     this(buffer, buffer.readableBytes());
/*  20:    */   }
/*  21:    */   
/*  22:    */   public ByteBufInputStream(ByteBuf buffer, int length)
/*  23:    */   {
/*  24: 65 */     if (buffer == null) {
/*  25: 66 */       throw new NullPointerException("buffer");
/*  26:    */     }
/*  27: 68 */     if (length < 0) {
/*  28: 69 */       throw new IllegalArgumentException("length: " + length);
/*  29:    */     }
/*  30: 71 */     if (length > buffer.readableBytes()) {
/*  31: 72 */       throw new IndexOutOfBoundsException("Too many bytes to be read - Needs " + length + ", maximum is " + buffer.readableBytes());
/*  32:    */     }
/*  33: 76 */     this.buffer = buffer;
/*  34: 77 */     this.startIndex = buffer.readerIndex();
/*  35: 78 */     this.endIndex = (this.startIndex + length);
/*  36: 79 */     buffer.markReaderIndex();
/*  37:    */   }
/*  38:    */   
/*  39:    */   public int readBytes()
/*  40:    */   {
/*  41: 86 */     return this.buffer.readerIndex() - this.startIndex;
/*  42:    */   }
/*  43:    */   
/*  44:    */   public int available()
/*  45:    */     throws IOException
/*  46:    */   {
/*  47: 91 */     return this.endIndex - this.buffer.readerIndex();
/*  48:    */   }
/*  49:    */   
/*  50:    */   public void mark(int readlimit)
/*  51:    */   {
/*  52: 96 */     this.buffer.markReaderIndex();
/*  53:    */   }
/*  54:    */   
/*  55:    */   public boolean markSupported()
/*  56:    */   {
/*  57:101 */     return true;
/*  58:    */   }
/*  59:    */   
/*  60:    */   public int read()
/*  61:    */     throws IOException
/*  62:    */   {
/*  63:106 */     if (!this.buffer.isReadable()) {
/*  64:107 */       return -1;
/*  65:    */     }
/*  66:109 */     return this.buffer.readByte() & 0xFF;
/*  67:    */   }
/*  68:    */   
/*  69:    */   public int read(byte[] b, int off, int len)
/*  70:    */     throws IOException
/*  71:    */   {
/*  72:114 */     int available = available();
/*  73:115 */     if (available == 0) {
/*  74:116 */       return -1;
/*  75:    */     }
/*  76:119 */     len = Math.min(available, len);
/*  77:120 */     this.buffer.readBytes(b, off, len);
/*  78:121 */     return len;
/*  79:    */   }
/*  80:    */   
/*  81:    */   public void reset()
/*  82:    */     throws IOException
/*  83:    */   {
/*  84:126 */     this.buffer.resetReaderIndex();
/*  85:    */   }
/*  86:    */   
/*  87:    */   public long skip(long n)
/*  88:    */     throws IOException
/*  89:    */   {
/*  90:131 */     if (n > 2147483647L) {
/*  91:132 */       return skipBytes(2147483647);
/*  92:    */     }
/*  93:134 */     return skipBytes((int)n);
/*  94:    */   }
/*  95:    */   
/*  96:    */   public boolean readBoolean()
/*  97:    */     throws IOException
/*  98:    */   {
/*  99:140 */     checkAvailable(1);
/* 100:141 */     return read() != 0;
/* 101:    */   }
/* 102:    */   
/* 103:    */   public byte readByte()
/* 104:    */     throws IOException
/* 105:    */   {
/* 106:146 */     if (!this.buffer.isReadable()) {
/* 107:147 */       throw new EOFException();
/* 108:    */     }
/* 109:149 */     return this.buffer.readByte();
/* 110:    */   }
/* 111:    */   
/* 112:    */   public char readChar()
/* 113:    */     throws IOException
/* 114:    */   {
/* 115:154 */     return (char)readShort();
/* 116:    */   }
/* 117:    */   
/* 118:    */   public double readDouble()
/* 119:    */     throws IOException
/* 120:    */   {
/* 121:159 */     return Double.longBitsToDouble(readLong());
/* 122:    */   }
/* 123:    */   
/* 124:    */   public float readFloat()
/* 125:    */     throws IOException
/* 126:    */   {
/* 127:164 */     return Float.intBitsToFloat(readInt());
/* 128:    */   }
/* 129:    */   
/* 130:    */   public void readFully(byte[] b)
/* 131:    */     throws IOException
/* 132:    */   {
/* 133:169 */     readFully(b, 0, b.length);
/* 134:    */   }
/* 135:    */   
/* 136:    */   public void readFully(byte[] b, int off, int len)
/* 137:    */     throws IOException
/* 138:    */   {
/* 139:174 */     checkAvailable(len);
/* 140:175 */     this.buffer.readBytes(b, off, len);
/* 141:    */   }
/* 142:    */   
/* 143:    */   public int readInt()
/* 144:    */     throws IOException
/* 145:    */   {
/* 146:180 */     checkAvailable(4);
/* 147:181 */     return this.buffer.readInt();
/* 148:    */   }
/* 149:    */   
/* 150:184 */   private final StringBuilder lineBuf = new StringBuilder();
/* 151:    */   
/* 152:    */   public String readLine()
/* 153:    */     throws IOException
/* 154:    */   {
/* 155:188 */     this.lineBuf.setLength(0);
/* 156:    */     for (;;)
/* 157:    */     {
/* 158:191 */       if (!this.buffer.isReadable()) {
/* 159:192 */         return this.lineBuf.length() > 0 ? this.lineBuf.toString() : null;
/* 160:    */       }
/* 161:195 */       int c = this.buffer.readUnsignedByte();
/* 162:196 */       switch (c)
/* 163:    */       {
/* 164:    */       case 10: 
/* 165:    */         break;
/* 166:    */       case 13: 
/* 167:201 */         if ((!this.buffer.isReadable()) || (this.buffer.getUnsignedByte(this.buffer.readerIndex()) != 10)) {
/* 168:    */           break;
/* 169:    */         }
/* 170:202 */         this.buffer.skipBytes(1); break;
/* 171:    */       default: 
/* 172:207 */         this.lineBuf.append((char)c);
/* 173:    */       }
/* 174:    */     }
/* 175:211 */     return this.lineBuf.toString();
/* 176:    */   }
/* 177:    */   
/* 178:    */   public long readLong()
/* 179:    */     throws IOException
/* 180:    */   {
/* 181:216 */     checkAvailable(8);
/* 182:217 */     return this.buffer.readLong();
/* 183:    */   }
/* 184:    */   
/* 185:    */   public short readShort()
/* 186:    */     throws IOException
/* 187:    */   {
/* 188:222 */     checkAvailable(2);
/* 189:223 */     return this.buffer.readShort();
/* 190:    */   }
/* 191:    */   
/* 192:    */   public String readUTF()
/* 193:    */     throws IOException
/* 194:    */   {
/* 195:228 */     return DataInputStream.readUTF(this);
/* 196:    */   }
/* 197:    */   
/* 198:    */   public int readUnsignedByte()
/* 199:    */     throws IOException
/* 200:    */   {
/* 201:233 */     return readByte() & 0xFF;
/* 202:    */   }
/* 203:    */   
/* 204:    */   public int readUnsignedShort()
/* 205:    */     throws IOException
/* 206:    */   {
/* 207:238 */     return readShort() & 0xFFFF;
/* 208:    */   }
/* 209:    */   
/* 210:    */   public int skipBytes(int n)
/* 211:    */     throws IOException
/* 212:    */   {
/* 213:243 */     int nBytes = Math.min(available(), n);
/* 214:244 */     this.buffer.skipBytes(nBytes);
/* 215:245 */     return nBytes;
/* 216:    */   }
/* 217:    */   
/* 218:    */   private void checkAvailable(int fieldSize)
/* 219:    */     throws IOException
/* 220:    */   {
/* 221:249 */     if (fieldSize < 0) {
/* 222:250 */       throw new IndexOutOfBoundsException("fieldSize cannot be a negative number");
/* 223:    */     }
/* 224:252 */     if (fieldSize > available()) {
/* 225:253 */       throw new EOFException("fieldSize is too long! Length is " + fieldSize + ", but maximum is " + available());
/* 226:    */     }
/* 227:    */   }
/* 228:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.ByteBufInputStream
 * JD-Core Version:    0.7.0.1
 */