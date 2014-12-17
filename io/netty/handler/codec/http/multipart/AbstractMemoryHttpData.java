/*   1:    */ package io.netty.handler.codec.http.multipart;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.CompositeByteBuf;
/*   5:    */ import io.netty.buffer.Unpooled;
/*   6:    */ import io.netty.handler.codec.http.HttpConstants;
/*   7:    */ import java.io.File;
/*   8:    */ import java.io.FileInputStream;
/*   9:    */ import java.io.FileOutputStream;
/*  10:    */ import java.io.IOException;
/*  11:    */ import java.io.InputStream;
/*  12:    */ import java.nio.ByteBuffer;
/*  13:    */ import java.nio.channels.FileChannel;
/*  14:    */ import java.nio.charset.Charset;
/*  15:    */ 
/*  16:    */ public abstract class AbstractMemoryHttpData
/*  17:    */   extends AbstractHttpData
/*  18:    */ {
/*  19:    */   private ByteBuf byteBuf;
/*  20:    */   private int chunkPosition;
/*  21:    */   protected boolean isRenamed;
/*  22:    */   
/*  23:    */   protected AbstractMemoryHttpData(String name, Charset charset, long size)
/*  24:    */   {
/*  25: 43 */     super(name, charset, size);
/*  26:    */   }
/*  27:    */   
/*  28:    */   public void setContent(ByteBuf buffer)
/*  29:    */     throws IOException
/*  30:    */   {
/*  31: 48 */     if (buffer == null) {
/*  32: 49 */       throw new NullPointerException("buffer");
/*  33:    */     }
/*  34: 51 */     long localsize = buffer.readableBytes();
/*  35: 52 */     if ((this.definedSize > 0L) && (this.definedSize < localsize)) {
/*  36: 53 */       throw new IOException("Out of size: " + localsize + " > " + this.definedSize);
/*  37:    */     }
/*  38: 56 */     if (this.byteBuf != null) {
/*  39: 57 */       this.byteBuf.release();
/*  40:    */     }
/*  41: 59 */     this.byteBuf = buffer;
/*  42: 60 */     this.size = localsize;
/*  43: 61 */     this.completed = true;
/*  44:    */   }
/*  45:    */   
/*  46:    */   public void setContent(InputStream inputStream)
/*  47:    */     throws IOException
/*  48:    */   {
/*  49: 66 */     if (inputStream == null) {
/*  50: 67 */       throw new NullPointerException("inputStream");
/*  51:    */     }
/*  52: 69 */     ByteBuf buffer = Unpooled.buffer();
/*  53: 70 */     byte[] bytes = new byte[16384];
/*  54: 71 */     int read = inputStream.read(bytes);
/*  55: 72 */     int written = 0;
/*  56: 73 */     while (read > 0)
/*  57:    */     {
/*  58: 74 */       buffer.writeBytes(bytes, 0, read);
/*  59: 75 */       written += read;
/*  60: 76 */       read = inputStream.read(bytes);
/*  61:    */     }
/*  62: 78 */     this.size = written;
/*  63: 79 */     if ((this.definedSize > 0L) && (this.definedSize < this.size)) {
/*  64: 80 */       throw new IOException("Out of size: " + this.size + " > " + this.definedSize);
/*  65:    */     }
/*  66: 82 */     if (this.byteBuf != null) {
/*  67: 83 */       this.byteBuf.release();
/*  68:    */     }
/*  69: 85 */     this.byteBuf = buffer;
/*  70: 86 */     this.completed = true;
/*  71:    */   }
/*  72:    */   
/*  73:    */   public void addContent(ByteBuf buffer, boolean last)
/*  74:    */     throws IOException
/*  75:    */   {
/*  76: 92 */     if (buffer != null)
/*  77:    */     {
/*  78: 93 */       long localsize = buffer.readableBytes();
/*  79: 94 */       if ((this.definedSize > 0L) && (this.definedSize < this.size + localsize)) {
/*  80: 95 */         throw new IOException("Out of size: " + (this.size + localsize) + " > " + this.definedSize);
/*  81:    */       }
/*  82: 98 */       this.size += localsize;
/*  83: 99 */       if (this.byteBuf == null)
/*  84:    */       {
/*  85:100 */         this.byteBuf = buffer;
/*  86:    */       }
/*  87:101 */       else if ((this.byteBuf instanceof CompositeByteBuf))
/*  88:    */       {
/*  89:102 */         CompositeByteBuf cbb = (CompositeByteBuf)this.byteBuf;
/*  90:103 */         cbb.addComponent(buffer);
/*  91:104 */         cbb.writerIndex(cbb.writerIndex() + buffer.readableBytes());
/*  92:    */       }
/*  93:    */       else
/*  94:    */       {
/*  95:106 */         CompositeByteBuf cbb = Unpooled.compositeBuffer(2147483647);
/*  96:107 */         cbb.addComponents(new ByteBuf[] { this.byteBuf, buffer });
/*  97:108 */         cbb.writerIndex(this.byteBuf.readableBytes() + buffer.readableBytes());
/*  98:109 */         this.byteBuf = cbb;
/*  99:    */       }
/* 100:    */     }
/* 101:112 */     if (last) {
/* 102:113 */       this.completed = true;
/* 103:115 */     } else if (buffer == null) {
/* 104:116 */       throw new NullPointerException("buffer");
/* 105:    */     }
/* 106:    */   }
/* 107:    */   
/* 108:    */   public void setContent(File file)
/* 109:    */     throws IOException
/* 110:    */   {
/* 111:123 */     if (file == null) {
/* 112:124 */       throw new NullPointerException("file");
/* 113:    */     }
/* 114:126 */     long newsize = file.length();
/* 115:127 */     if (newsize > 2147483647L) {
/* 116:128 */       throw new IllegalArgumentException("File too big to be loaded in memory");
/* 117:    */     }
/* 118:131 */     FileInputStream inputStream = new FileInputStream(file);
/* 119:132 */     FileChannel fileChannel = inputStream.getChannel();
/* 120:133 */     byte[] array = new byte[(int)newsize];
/* 121:134 */     ByteBuffer byteBuffer = ByteBuffer.wrap(array);
/* 122:135 */     int read = 0;
/* 123:136 */     while (read < newsize) {
/* 124:137 */       read += fileChannel.read(byteBuffer);
/* 125:    */     }
/* 126:139 */     fileChannel.close();
/* 127:140 */     inputStream.close();
/* 128:141 */     byteBuffer.flip();
/* 129:142 */     if (this.byteBuf != null) {
/* 130:143 */       this.byteBuf.release();
/* 131:    */     }
/* 132:145 */     this.byteBuf = Unpooled.wrappedBuffer(2147483647, new ByteBuffer[] { byteBuffer });
/* 133:146 */     this.size = newsize;
/* 134:147 */     this.completed = true;
/* 135:    */   }
/* 136:    */   
/* 137:    */   public void delete()
/* 138:    */   {
/* 139:152 */     if (this.byteBuf != null)
/* 140:    */     {
/* 141:153 */       this.byteBuf.release();
/* 142:154 */       this.byteBuf = null;
/* 143:    */     }
/* 144:    */   }
/* 145:    */   
/* 146:    */   public byte[] get()
/* 147:    */   {
/* 148:160 */     if (this.byteBuf == null) {
/* 149:161 */       return Unpooled.EMPTY_BUFFER.array();
/* 150:    */     }
/* 151:163 */     byte[] array = new byte[this.byteBuf.readableBytes()];
/* 152:164 */     this.byteBuf.getBytes(this.byteBuf.readerIndex(), array);
/* 153:165 */     return array;
/* 154:    */   }
/* 155:    */   
/* 156:    */   public String getString()
/* 157:    */   {
/* 158:170 */     return getString(HttpConstants.DEFAULT_CHARSET);
/* 159:    */   }
/* 160:    */   
/* 161:    */   public String getString(Charset encoding)
/* 162:    */   {
/* 163:175 */     if (this.byteBuf == null) {
/* 164:176 */       return "";
/* 165:    */     }
/* 166:178 */     if (encoding == null) {
/* 167:179 */       encoding = HttpConstants.DEFAULT_CHARSET;
/* 168:    */     }
/* 169:181 */     return this.byteBuf.toString(encoding);
/* 170:    */   }
/* 171:    */   
/* 172:    */   public ByteBuf getByteBuf()
/* 173:    */   {
/* 174:191 */     return this.byteBuf;
/* 175:    */   }
/* 176:    */   
/* 177:    */   public ByteBuf getChunk(int length)
/* 178:    */     throws IOException
/* 179:    */   {
/* 180:196 */     if ((this.byteBuf == null) || (length == 0) || (this.byteBuf.readableBytes() == 0))
/* 181:    */     {
/* 182:197 */       this.chunkPosition = 0;
/* 183:198 */       return Unpooled.EMPTY_BUFFER;
/* 184:    */     }
/* 185:200 */     int sizeLeft = this.byteBuf.readableBytes() - this.chunkPosition;
/* 186:201 */     if (sizeLeft == 0)
/* 187:    */     {
/* 188:202 */       this.chunkPosition = 0;
/* 189:203 */       return Unpooled.EMPTY_BUFFER;
/* 190:    */     }
/* 191:205 */     int sliceLength = length;
/* 192:206 */     if (sizeLeft < length) {
/* 193:207 */       sliceLength = sizeLeft;
/* 194:    */     }
/* 195:209 */     ByteBuf chunk = this.byteBuf.slice(this.chunkPosition, sliceLength).retain();
/* 196:210 */     this.chunkPosition += sliceLength;
/* 197:211 */     return chunk;
/* 198:    */   }
/* 199:    */   
/* 200:    */   public boolean isInMemory()
/* 201:    */   {
/* 202:216 */     return true;
/* 203:    */   }
/* 204:    */   
/* 205:    */   public boolean renameTo(File dest)
/* 206:    */     throws IOException
/* 207:    */   {
/* 208:221 */     if (dest == null) {
/* 209:222 */       throw new NullPointerException("dest");
/* 210:    */     }
/* 211:224 */     if (this.byteBuf == null)
/* 212:    */     {
/* 213:226 */       dest.createNewFile();
/* 214:227 */       this.isRenamed = true;
/* 215:228 */       return true;
/* 216:    */     }
/* 217:230 */     int length = this.byteBuf.readableBytes();
/* 218:231 */     FileOutputStream outputStream = new FileOutputStream(dest);
/* 219:232 */     FileChannel fileChannel = outputStream.getChannel();
/* 220:233 */     int written = 0;
/* 221:234 */     if (this.byteBuf.nioBufferCount() == 1)
/* 222:    */     {
/* 223:235 */       ByteBuffer byteBuffer = this.byteBuf.nioBuffer();
/* 224:236 */       while (written < length) {
/* 225:237 */         written += fileChannel.write(byteBuffer);
/* 226:    */       }
/* 227:    */     }
/* 228:    */     else
/* 229:    */     {
/* 230:240 */       ByteBuffer[] byteBuffers = this.byteBuf.nioBuffers();
/* 231:241 */       while (written < length) {
/* 232:242 */         written = (int)(written + fileChannel.write(byteBuffers));
/* 233:    */       }
/* 234:    */     }
/* 235:246 */     fileChannel.force(false);
/* 236:247 */     fileChannel.close();
/* 237:248 */     outputStream.close();
/* 238:249 */     this.isRenamed = true;
/* 239:250 */     return written == length;
/* 240:    */   }
/* 241:    */   
/* 242:    */   public File getFile()
/* 243:    */     throws IOException
/* 244:    */   {
/* 245:255 */     throw new IOException("Not represented by a file");
/* 246:    */   }
/* 247:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.multipart.AbstractMemoryHttpData
 * JD-Core Version:    0.7.0.1
 */