/*   1:    */ package io.netty.handler.codec.http.multipart;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.Unpooled;
/*   5:    */ import io.netty.handler.codec.http.HttpConstants;
/*   6:    */ import io.netty.util.internal.EmptyArrays;
/*   7:    */ import io.netty.util.internal.logging.InternalLogger;
/*   8:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   9:    */ import java.io.File;
/*  10:    */ import java.io.FileInputStream;
/*  11:    */ import java.io.FileOutputStream;
/*  12:    */ import java.io.IOException;
/*  13:    */ import java.io.InputStream;
/*  14:    */ import java.nio.Buffer;
/*  15:    */ import java.nio.ByteBuffer;
/*  16:    */ import java.nio.channels.FileChannel;
/*  17:    */ import java.nio.charset.Charset;
/*  18:    */ 
/*  19:    */ public abstract class AbstractDiskHttpData
/*  20:    */   extends AbstractHttpData
/*  21:    */ {
/*  22: 40 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractDiskHttpData.class);
/*  23:    */   protected File file;
/*  24:    */   private boolean isRenamed;
/*  25:    */   private FileChannel fileChannel;
/*  26:    */   
/*  27:    */   protected AbstractDiskHttpData(String name, Charset charset, long size)
/*  28:    */   {
/*  29: 47 */     super(name, charset, size);
/*  30:    */   }
/*  31:    */   
/*  32:    */   protected abstract String getDiskFilename();
/*  33:    */   
/*  34:    */   protected abstract String getPrefix();
/*  35:    */   
/*  36:    */   protected abstract String getBaseDirectory();
/*  37:    */   
/*  38:    */   protected abstract String getPostfix();
/*  39:    */   
/*  40:    */   protected abstract boolean deleteOnExit();
/*  41:    */   
/*  42:    */   private File tempFile()
/*  43:    */     throws IOException
/*  44:    */   {
/*  45: 81 */     String diskFilename = getDiskFilename();
/*  46:    */     String newpostfix;
/*  47:    */     String newpostfix;
/*  48: 82 */     if (diskFilename != null) {
/*  49: 83 */       newpostfix = '_' + diskFilename;
/*  50:    */     } else {
/*  51: 85 */       newpostfix = getPostfix();
/*  52:    */     }
/*  53:    */     File tmpFile;
/*  54:    */     File tmpFile;
/*  55: 88 */     if (getBaseDirectory() == null) {
/*  56: 90 */       tmpFile = File.createTempFile(getPrefix(), newpostfix);
/*  57:    */     } else {
/*  58: 92 */       tmpFile = File.createTempFile(getPrefix(), newpostfix, new File(getBaseDirectory()));
/*  59:    */     }
/*  60: 95 */     if (deleteOnExit()) {
/*  61: 96 */       tmpFile.deleteOnExit();
/*  62:    */     }
/*  63: 98 */     return tmpFile;
/*  64:    */   }
/*  65:    */   
/*  66:    */   public void setContent(ByteBuf buffer)
/*  67:    */     throws IOException
/*  68:    */   {
/*  69:103 */     if (buffer == null) {
/*  70:104 */       throw new NullPointerException("buffer");
/*  71:    */     }
/*  72:    */     try
/*  73:    */     {
/*  74:107 */       this.size = buffer.readableBytes();
/*  75:108 */       if ((this.definedSize > 0L) && (this.definedSize < this.size)) {
/*  76:109 */         throw new IOException("Out of size: " + this.size + " > " + this.definedSize);
/*  77:    */       }
/*  78:111 */       if (this.file == null) {
/*  79:112 */         this.file = tempFile();
/*  80:    */       }
/*  81:114 */       if (buffer.readableBytes() == 0)
/*  82:    */       {
/*  83:116 */         this.file.createNewFile();
/*  84:    */       }
/*  85:    */       else
/*  86:    */       {
/*  87:119 */         FileOutputStream outputStream = new FileOutputStream(this.file);
/*  88:120 */         FileChannel localfileChannel = outputStream.getChannel();
/*  89:121 */         ByteBuffer byteBuffer = buffer.nioBuffer();
/*  90:122 */         int written = 0;
/*  91:123 */         while (written < this.size) {
/*  92:124 */           written += localfileChannel.write(byteBuffer);
/*  93:    */         }
/*  94:126 */         buffer.readerIndex(buffer.readerIndex() + written);
/*  95:127 */         localfileChannel.force(false);
/*  96:128 */         localfileChannel.close();
/*  97:129 */         outputStream.close();
/*  98:130 */         this.completed = true;
/*  99:    */       }
/* 100:    */     }
/* 101:    */     finally
/* 102:    */     {
/* 103:134 */       buffer.release();
/* 104:    */     }
/* 105:    */   }
/* 106:    */   
/* 107:    */   public void addContent(ByteBuf buffer, boolean last)
/* 108:    */     throws IOException
/* 109:    */   {
/* 110:141 */     if (buffer != null) {
/* 111:    */       try
/* 112:    */       {
/* 113:143 */         int localsize = buffer.readableBytes();
/* 114:144 */         if ((this.definedSize > 0L) && (this.definedSize < this.size + localsize)) {
/* 115:145 */           throw new IOException("Out of size: " + (this.size + localsize) + " > " + this.definedSize);
/* 116:    */         }
/* 117:148 */         ByteBuffer byteBuffer = buffer.nioBufferCount() == 1 ? buffer.nioBuffer() : buffer.copy().nioBuffer();
/* 118:149 */         int written = 0;
/* 119:150 */         if (this.file == null) {
/* 120:151 */           this.file = tempFile();
/* 121:    */         }
/* 122:153 */         if (this.fileChannel == null)
/* 123:    */         {
/* 124:154 */           FileOutputStream outputStream = new FileOutputStream(this.file);
/* 125:155 */           this.fileChannel = outputStream.getChannel();
/* 126:    */         }
/* 127:157 */         while (written < localsize) {
/* 128:158 */           written += this.fileChannel.write(byteBuffer);
/* 129:    */         }
/* 130:160 */         this.size += localsize;
/* 131:161 */         buffer.readerIndex(buffer.readerIndex() + written);
/* 132:    */       }
/* 133:    */       finally
/* 134:    */       {
/* 135:165 */         buffer.release();
/* 136:    */       }
/* 137:    */     }
/* 138:168 */     if (last)
/* 139:    */     {
/* 140:169 */       if (this.file == null) {
/* 141:170 */         this.file = tempFile();
/* 142:    */       }
/* 143:172 */       if (this.fileChannel == null)
/* 144:    */       {
/* 145:173 */         FileOutputStream outputStream = new FileOutputStream(this.file);
/* 146:174 */         this.fileChannel = outputStream.getChannel();
/* 147:    */       }
/* 148:176 */       this.fileChannel.force(false);
/* 149:177 */       this.fileChannel.close();
/* 150:178 */       this.fileChannel = null;
/* 151:179 */       this.completed = true;
/* 152:    */     }
/* 153:181 */     else if (buffer == null)
/* 154:    */     {
/* 155:182 */       throw new NullPointerException("buffer");
/* 156:    */     }
/* 157:    */   }
/* 158:    */   
/* 159:    */   public void setContent(File file)
/* 160:    */     throws IOException
/* 161:    */   {
/* 162:189 */     if (this.file != null) {
/* 163:190 */       delete();
/* 164:    */     }
/* 165:192 */     this.file = file;
/* 166:193 */     this.size = file.length();
/* 167:194 */     this.isRenamed = true;
/* 168:195 */     this.completed = true;
/* 169:    */   }
/* 170:    */   
/* 171:    */   public void setContent(InputStream inputStream)
/* 172:    */     throws IOException
/* 173:    */   {
/* 174:200 */     if (inputStream == null) {
/* 175:201 */       throw new NullPointerException("inputStream");
/* 176:    */     }
/* 177:203 */     if (this.file != null) {
/* 178:204 */       delete();
/* 179:    */     }
/* 180:206 */     this.file = tempFile();
/* 181:207 */     FileOutputStream outputStream = new FileOutputStream(this.file);
/* 182:208 */     FileChannel localfileChannel = outputStream.getChannel();
/* 183:209 */     byte[] bytes = new byte[16384];
/* 184:210 */     ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
/* 185:211 */     int read = inputStream.read(bytes);
/* 186:212 */     int written = 0;
/* 187:213 */     while (read > 0)
/* 188:    */     {
/* 189:214 */       byteBuffer.position(read).flip();
/* 190:215 */       written += localfileChannel.write(byteBuffer);
/* 191:216 */       read = inputStream.read(bytes);
/* 192:    */     }
/* 193:218 */     localfileChannel.force(false);
/* 194:219 */     localfileChannel.close();
/* 195:220 */     this.size = written;
/* 196:221 */     if ((this.definedSize > 0L) && (this.definedSize < this.size))
/* 197:    */     {
/* 198:222 */       this.file.delete();
/* 199:223 */       this.file = null;
/* 200:224 */       throw new IOException("Out of size: " + this.size + " > " + this.definedSize);
/* 201:    */     }
/* 202:226 */     this.isRenamed = true;
/* 203:227 */     this.completed = true;
/* 204:    */   }
/* 205:    */   
/* 206:    */   public void delete()
/* 207:    */   {
/* 208:232 */     if (this.fileChannel != null)
/* 209:    */     {
/* 210:    */       try
/* 211:    */       {
/* 212:234 */         this.fileChannel.force(false);
/* 213:235 */         this.fileChannel.close();
/* 214:    */       }
/* 215:    */       catch (IOException e)
/* 216:    */       {
/* 217:237 */         logger.warn("Failed to close a file.", e);
/* 218:    */       }
/* 219:239 */       this.fileChannel = null;
/* 220:    */     }
/* 221:241 */     if (!this.isRenamed)
/* 222:    */     {
/* 223:242 */       if ((this.file != null) && (this.file.exists())) {
/* 224:243 */         this.file.delete();
/* 225:    */       }
/* 226:245 */       this.file = null;
/* 227:    */     }
/* 228:    */   }
/* 229:    */   
/* 230:    */   public byte[] get()
/* 231:    */     throws IOException
/* 232:    */   {
/* 233:251 */     if (this.file == null) {
/* 234:252 */       return EmptyArrays.EMPTY_BYTES;
/* 235:    */     }
/* 236:254 */     return readFrom(this.file);
/* 237:    */   }
/* 238:    */   
/* 239:    */   public ByteBuf getByteBuf()
/* 240:    */     throws IOException
/* 241:    */   {
/* 242:259 */     if (this.file == null) {
/* 243:260 */       return Unpooled.EMPTY_BUFFER;
/* 244:    */     }
/* 245:262 */     byte[] array = readFrom(this.file);
/* 246:263 */     return Unpooled.wrappedBuffer(array);
/* 247:    */   }
/* 248:    */   
/* 249:    */   public ByteBuf getChunk(int length)
/* 250:    */     throws IOException
/* 251:    */   {
/* 252:268 */     if ((this.file == null) || (length == 0)) {
/* 253:269 */       return Unpooled.EMPTY_BUFFER;
/* 254:    */     }
/* 255:271 */     if (this.fileChannel == null)
/* 256:    */     {
/* 257:272 */       FileInputStream inputStream = new FileInputStream(this.file);
/* 258:273 */       this.fileChannel = inputStream.getChannel();
/* 259:    */     }
/* 260:275 */     int read = 0;
/* 261:276 */     ByteBuffer byteBuffer = ByteBuffer.allocate(length);
/* 262:277 */     while (read < length)
/* 263:    */     {
/* 264:278 */       int readnow = this.fileChannel.read(byteBuffer);
/* 265:279 */       if (readnow == -1)
/* 266:    */       {
/* 267:280 */         this.fileChannel.close();
/* 268:281 */         this.fileChannel = null;
/* 269:282 */         break;
/* 270:    */       }
/* 271:284 */       read += readnow;
/* 272:    */     }
/* 273:287 */     if (read == 0) {
/* 274:288 */       return Unpooled.EMPTY_BUFFER;
/* 275:    */     }
/* 276:290 */     byteBuffer.flip();
/* 277:291 */     ByteBuf buffer = Unpooled.wrappedBuffer(byteBuffer);
/* 278:292 */     buffer.readerIndex(0);
/* 279:293 */     buffer.writerIndex(read);
/* 280:294 */     return buffer;
/* 281:    */   }
/* 282:    */   
/* 283:    */   public String getString()
/* 284:    */     throws IOException
/* 285:    */   {
/* 286:299 */     return getString(HttpConstants.DEFAULT_CHARSET);
/* 287:    */   }
/* 288:    */   
/* 289:    */   public String getString(Charset encoding)
/* 290:    */     throws IOException
/* 291:    */   {
/* 292:304 */     if (this.file == null) {
/* 293:305 */       return "";
/* 294:    */     }
/* 295:307 */     if (encoding == null)
/* 296:    */     {
/* 297:308 */       byte[] array = readFrom(this.file);
/* 298:309 */       return new String(array, HttpConstants.DEFAULT_CHARSET.name());
/* 299:    */     }
/* 300:311 */     byte[] array = readFrom(this.file);
/* 301:312 */     return new String(array, encoding.name());
/* 302:    */   }
/* 303:    */   
/* 304:    */   public boolean isInMemory()
/* 305:    */   {
/* 306:317 */     return false;
/* 307:    */   }
/* 308:    */   
/* 309:    */   public boolean renameTo(File dest)
/* 310:    */     throws IOException
/* 311:    */   {
/* 312:322 */     if (dest == null) {
/* 313:323 */       throw new NullPointerException("dest");
/* 314:    */     }
/* 315:325 */     if (this.file == null) {
/* 316:326 */       throw new IOException("No file defined so cannot be renamed");
/* 317:    */     }
/* 318:328 */     if (!this.file.renameTo(dest))
/* 319:    */     {
/* 320:330 */       FileInputStream inputStream = new FileInputStream(this.file);
/* 321:331 */       FileOutputStream outputStream = new FileOutputStream(dest);
/* 322:332 */       FileChannel in = inputStream.getChannel();
/* 323:333 */       FileChannel out = outputStream.getChannel();
/* 324:334 */       int chunkSize = 8196;
/* 325:335 */       long position = 0L;
/* 326:336 */       while (position < this.size)
/* 327:    */       {
/* 328:337 */         if (chunkSize < this.size - position) {
/* 329:338 */           chunkSize = (int)(this.size - position);
/* 330:    */         }
/* 331:340 */         position += in.transferTo(position, chunkSize, out);
/* 332:    */       }
/* 333:342 */       in.close();
/* 334:343 */       out.close();
/* 335:344 */       if (position == this.size)
/* 336:    */       {
/* 337:345 */         this.file.delete();
/* 338:346 */         this.file = dest;
/* 339:347 */         this.isRenamed = true;
/* 340:348 */         return true;
/* 341:    */       }
/* 342:350 */       dest.delete();
/* 343:351 */       return false;
/* 344:    */     }
/* 345:354 */     this.file = dest;
/* 346:355 */     this.isRenamed = true;
/* 347:356 */     return true;
/* 348:    */   }
/* 349:    */   
/* 350:    */   private static byte[] readFrom(File src)
/* 351:    */     throws IOException
/* 352:    */   {
/* 353:364 */     long srcsize = src.length();
/* 354:365 */     if (srcsize > 2147483647L) {
/* 355:366 */       throw new IllegalArgumentException("File too big to be loaded in memory");
/* 356:    */     }
/* 357:369 */     FileInputStream inputStream = new FileInputStream(src);
/* 358:370 */     FileChannel fileChannel = inputStream.getChannel();
/* 359:371 */     byte[] array = new byte[(int)srcsize];
/* 360:372 */     ByteBuffer byteBuffer = ByteBuffer.wrap(array);
/* 361:373 */     int read = 0;
/* 362:374 */     while (read < srcsize) {
/* 363:375 */       read += fileChannel.read(byteBuffer);
/* 364:    */     }
/* 365:377 */     fileChannel.close();
/* 366:378 */     return array;
/* 367:    */   }
/* 368:    */   
/* 369:    */   public File getFile()
/* 370:    */     throws IOException
/* 371:    */   {
/* 372:383 */     return this.file;
/* 373:    */   }
/* 374:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.multipart.AbstractDiskHttpData
 * JD-Core Version:    0.7.0.1
 */