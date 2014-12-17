/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.channel.ChannelHandlerContext;
/*   6:    */ import java.util.List;
/*   7:    */ import java.util.zip.CRC32;
/*   8:    */ import java.util.zip.DataFormatException;
/*   9:    */ import java.util.zip.Inflater;
/*  10:    */ 
/*  11:    */ public class JdkZlibDecoder
/*  12:    */   extends ZlibDecoder
/*  13:    */ {
/*  14:    */   private static final int FHCRC = 2;
/*  15:    */   private static final int FEXTRA = 4;
/*  16:    */   private static final int FNAME = 8;
/*  17:    */   private static final int FCOMMENT = 16;
/*  18:    */   private static final int FRESERVED = 224;
/*  19:    */   private Inflater inflater;
/*  20:    */   private final byte[] dictionary;
/*  21:    */   private final CRC32 crc;
/*  22:    */   
/*  23:    */   private static enum GzipState
/*  24:    */   {
/*  25: 45 */     HEADER_START,  HEADER_END,  FLG_READ,  XLEN_READ,  SKIP_FNAME,  SKIP_COMMENT,  PROCESS_FHCRC,  FOOTER_START;
/*  26:    */     
/*  27:    */     private GzipState() {}
/*  28:    */   }
/*  29:    */   
/*  30: 55 */   private GzipState gzipState = GzipState.HEADER_START;
/*  31: 56 */   private int flags = -1;
/*  32: 57 */   private int xlen = -1;
/*  33:    */   private volatile boolean finished;
/*  34:    */   private boolean decideZlibOrNone;
/*  35:    */   
/*  36:    */   public JdkZlibDecoder()
/*  37:    */   {
/*  38: 67 */     this(ZlibWrapper.ZLIB, null);
/*  39:    */   }
/*  40:    */   
/*  41:    */   public JdkZlibDecoder(byte[] dictionary)
/*  42:    */   {
/*  43: 76 */     this(ZlibWrapper.ZLIB, dictionary);
/*  44:    */   }
/*  45:    */   
/*  46:    */   public JdkZlibDecoder(ZlibWrapper wrapper)
/*  47:    */   {
/*  48: 85 */     this(wrapper, null);
/*  49:    */   }
/*  50:    */   
/*  51:    */   private JdkZlibDecoder(ZlibWrapper wrapper, byte[] dictionary)
/*  52:    */   {
/*  53: 89 */     if (wrapper == null) {
/*  54: 90 */       throw new NullPointerException("wrapper");
/*  55:    */     }
/*  56: 92 */     switch (1.$SwitchMap$io$netty$handler$codec$compression$ZlibWrapper[wrapper.ordinal()])
/*  57:    */     {
/*  58:    */     case 1: 
/*  59: 94 */       this.inflater = new Inflater(true);
/*  60: 95 */       this.crc = new CRC32();
/*  61: 96 */       break;
/*  62:    */     case 2: 
/*  63: 98 */       this.inflater = new Inflater(true);
/*  64: 99 */       this.crc = null;
/*  65:100 */       break;
/*  66:    */     case 3: 
/*  67:102 */       this.inflater = new Inflater();
/*  68:103 */       this.crc = null;
/*  69:104 */       break;
/*  70:    */     case 4: 
/*  71:107 */       this.decideZlibOrNone = true;
/*  72:108 */       this.crc = null;
/*  73:109 */       break;
/*  74:    */     default: 
/*  75:111 */       throw new IllegalArgumentException("Only GZIP or ZLIB is supported, but you used " + wrapper);
/*  76:    */     }
/*  77:113 */     this.dictionary = dictionary;
/*  78:    */   }
/*  79:    */   
/*  80:    */   public boolean isClosed()
/*  81:    */   {
/*  82:118 */     return this.finished;
/*  83:    */   }
/*  84:    */   
/*  85:    */   protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/*  86:    */     throws Exception
/*  87:    */   {
/*  88:123 */     if (this.finished)
/*  89:    */     {
/*  90:125 */       in.skipBytes(in.readableBytes());
/*  91:126 */       return;
/*  92:    */     }
/*  93:129 */     if (!in.isReadable()) {
/*  94:130 */       return;
/*  95:    */     }
/*  96:133 */     if (this.decideZlibOrNone)
/*  97:    */     {
/*  98:135 */       if (in.readableBytes() < 2) {
/*  99:136 */         return;
/* 100:    */       }
/* 101:139 */       boolean nowrap = !looksLikeZlib(in.getShort(0));
/* 102:140 */       this.inflater = new Inflater(nowrap);
/* 103:141 */       this.decideZlibOrNone = false;
/* 104:    */     }
/* 105:144 */     if (this.crc != null)
/* 106:    */     {
/* 107:145 */       switch (this.gzipState)
/* 108:    */       {
/* 109:    */       case FOOTER_START: 
/* 110:147 */         if (readGZIPFooter(in)) {
/* 111:148 */           this.finished = true;
/* 112:    */         }
/* 113:150 */         return;
/* 114:    */       }
/* 115:152 */       if ((this.gzipState != GzipState.HEADER_END) && 
/* 116:153 */         (!readGZIPHeader(in))) {
/* 117:154 */         return;
/* 118:    */       }
/* 119:    */     }
/* 120:160 */     int readableBytes = in.readableBytes();
/* 121:161 */     if (in.hasArray())
/* 122:    */     {
/* 123:162 */       this.inflater.setInput(in.array(), in.arrayOffset() + in.readerIndex(), in.readableBytes());
/* 124:    */     }
/* 125:    */     else
/* 126:    */     {
/* 127:164 */       byte[] array = new byte[in.readableBytes()];
/* 128:165 */       in.getBytes(in.readerIndex(), array);
/* 129:166 */       this.inflater.setInput(array);
/* 130:    */     }
/* 131:169 */     int maxOutputLength = this.inflater.getRemaining() << 1;
/* 132:170 */     ByteBuf decompressed = ctx.alloc().heapBuffer(maxOutputLength);
/* 133:    */     try
/* 134:    */     {
/* 135:172 */       boolean readFooter = false;
/* 136:173 */       byte[] outArray = decompressed.array();
/* 137:174 */       while (!this.inflater.needsInput())
/* 138:    */       {
/* 139:175 */         int writerIndex = decompressed.writerIndex();
/* 140:176 */         int outIndex = decompressed.arrayOffset() + writerIndex;
/* 141:177 */         int length = decompressed.writableBytes();
/* 142:179 */         if (length == 0)
/* 143:    */         {
/* 144:181 */           out.add(decompressed);
/* 145:182 */           decompressed = ctx.alloc().heapBuffer(maxOutputLength);
/* 146:183 */           outArray = decompressed.array();
/* 147:    */         }
/* 148:    */         else
/* 149:    */         {
/* 150:187 */           int outputLength = this.inflater.inflate(outArray, outIndex, length);
/* 151:188 */           if (outputLength > 0)
/* 152:    */           {
/* 153:189 */             decompressed.writerIndex(writerIndex + outputLength);
/* 154:190 */             if (this.crc != null) {
/* 155:191 */               this.crc.update(outArray, outIndex, outputLength);
/* 156:    */             }
/* 157:    */           }
/* 158:194 */           else if (this.inflater.needsDictionary())
/* 159:    */           {
/* 160:195 */             if (this.dictionary == null) {
/* 161:196 */               throw new DecompressionException("decompression failure, unable to set dictionary as non was specified");
/* 162:    */             }
/* 163:199 */             this.inflater.setDictionary(this.dictionary);
/* 164:    */           }
/* 165:203 */           if (this.inflater.finished())
/* 166:    */           {
/* 167:204 */             if (this.crc == null)
/* 168:    */             {
/* 169:205 */               this.finished = true; break;
/* 170:    */             }
/* 171:207 */             readFooter = true;
/* 172:    */             
/* 173:209 */             break;
/* 174:    */           }
/* 175:    */         }
/* 176:    */       }
/* 177:213 */       in.skipBytes(readableBytes - this.inflater.getRemaining());
/* 178:215 */       if (readFooter)
/* 179:    */       {
/* 180:216 */         this.gzipState = GzipState.FOOTER_START;
/* 181:217 */         if (readGZIPFooter(in)) {
/* 182:218 */           this.finished = true;
/* 183:    */         }
/* 184:    */       }
/* 185:    */     }
/* 186:    */     catch (DataFormatException e)
/* 187:    */     {
/* 188:222 */       throw new DecompressionException("decompression failure", e);
/* 189:    */     }
/* 190:    */     finally
/* 191:    */     {
/* 192:225 */       if (decompressed.isReadable()) {
/* 193:226 */         out.add(decompressed);
/* 194:    */       } else {
/* 195:228 */         decompressed.release();
/* 196:    */       }
/* 197:    */     }
/* 198:    */   }
/* 199:    */   
/* 200:    */   protected void handlerRemoved0(ChannelHandlerContext ctx)
/* 201:    */     throws Exception
/* 202:    */   {
/* 203:235 */     super.handlerRemoved0(ctx);
/* 204:236 */     if (this.inflater != null) {
/* 205:237 */       this.inflater.end();
/* 206:    */     }
/* 207:    */   }
/* 208:    */   
/* 209:    */   private boolean readGZIPHeader(ByteBuf in)
/* 210:    */   {
/* 211:242 */     switch (1.$SwitchMap$io$netty$handler$codec$compression$JdkZlibDecoder$GzipState[this.gzipState.ordinal()])
/* 212:    */     {
/* 213:    */     case 2: 
/* 214:244 */       if (in.readableBytes() < 10) {
/* 215:245 */         return false;
/* 216:    */       }
/* 217:248 */       int magic0 = in.readByte();
/* 218:249 */       int magic1 = in.readByte();
/* 219:251 */       if (magic0 != 31) {
/* 220:252 */         throw new CompressionException("Input is not in the GZIP format");
/* 221:    */       }
/* 222:254 */       this.crc.update(magic0);
/* 223:255 */       this.crc.update(magic1);
/* 224:    */       
/* 225:257 */       int method = in.readUnsignedByte();
/* 226:258 */       if (method != 8) {
/* 227:259 */         throw new CompressionException("Unsupported compression method " + method + " in the GZIP header");
/* 228:    */       }
/* 229:262 */       this.crc.update(method);
/* 230:    */       
/* 231:264 */       this.flags = in.readUnsignedByte();
/* 232:265 */       this.crc.update(this.flags);
/* 233:267 */       if ((this.flags & 0xE0) != 0) {
/* 234:268 */         throw new CompressionException("Reserved flags are set in the GZIP header");
/* 235:    */       }
/* 236:273 */       this.crc.update(in.readByte());
/* 237:274 */       this.crc.update(in.readByte());
/* 238:275 */       this.crc.update(in.readByte());
/* 239:276 */       this.crc.update(in.readByte());
/* 240:    */       
/* 241:278 */       this.crc.update(in.readUnsignedByte());
/* 242:279 */       this.crc.update(in.readUnsignedByte());
/* 243:    */       
/* 244:281 */       this.gzipState = GzipState.FLG_READ;
/* 245:    */     case 3: 
/* 246:283 */       if ((this.flags & 0x4) != 0)
/* 247:    */       {
/* 248:284 */         if (in.readableBytes() < 2) {
/* 249:285 */           return false;
/* 250:    */         }
/* 251:287 */         int xlen1 = in.readUnsignedByte();
/* 252:288 */         int xlen2 = in.readUnsignedByte();
/* 253:289 */         this.crc.update(xlen1);
/* 254:290 */         this.crc.update(xlen2);
/* 255:    */         
/* 256:292 */         this.xlen |= xlen1 << 8 | xlen2;
/* 257:    */       }
/* 258:294 */       this.gzipState = GzipState.XLEN_READ;
/* 259:    */     case 4: 
/* 260:296 */       if (this.xlen != -1)
/* 261:    */       {
/* 262:297 */         if (in.readableBytes() < this.xlen) {
/* 263:298 */           return false;
/* 264:    */         }
/* 265:300 */         byte[] xtra = new byte[this.xlen];
/* 266:301 */         in.readBytes(xtra);
/* 267:302 */         this.crc.update(xtra);
/* 268:    */       }
/* 269:304 */       this.gzipState = GzipState.SKIP_FNAME;
/* 270:    */     case 5: 
/* 271:306 */       if ((this.flags & 0x8) != 0)
/* 272:    */       {
/* 273:307 */         if (!in.isReadable()) {
/* 274:308 */           return false;
/* 275:    */         }
/* 276:    */         int b;
/* 277:    */         do
/* 278:    */         {
/* 279:311 */           b = in.readUnsignedByte();
/* 280:312 */           this.crc.update(b);
/* 281:313 */         } while ((b != 0) && 
/* 282:    */         
/* 283:    */ 
/* 284:316 */           (in.isReadable()));
/* 285:    */       }
/* 286:318 */       this.gzipState = GzipState.SKIP_COMMENT;
/* 287:    */     case 6: 
/* 288:320 */       if ((this.flags & 0x10) != 0)
/* 289:    */       {
/* 290:321 */         if (!in.isReadable()) {
/* 291:322 */           return false;
/* 292:    */         }
/* 293:    */         int b;
/* 294:    */         do
/* 295:    */         {
/* 296:325 */           b = in.readUnsignedByte();
/* 297:326 */           this.crc.update(b);
/* 298:327 */         } while ((b != 0) && 
/* 299:    */         
/* 300:    */ 
/* 301:330 */           (in.isReadable()));
/* 302:    */       }
/* 303:332 */       this.gzipState = GzipState.PROCESS_FHCRC;
/* 304:    */     case 7: 
/* 305:334 */       if ((this.flags & 0x2) != 0)
/* 306:    */       {
/* 307:335 */         if (in.readableBytes() < 4) {
/* 308:336 */           return false;
/* 309:    */         }
/* 310:338 */         verifyCrc(in);
/* 311:    */       }
/* 312:340 */       this.crc.reset();
/* 313:341 */       this.gzipState = GzipState.HEADER_END;
/* 314:    */     case 8: 
/* 315:343 */       return true;
/* 316:    */     }
/* 317:345 */     throw new IllegalStateException();
/* 318:    */   }
/* 319:    */   
/* 320:    */   private boolean readGZIPFooter(ByteBuf buf)
/* 321:    */   {
/* 322:350 */     if (buf.readableBytes() < 8) {
/* 323:351 */       return false;
/* 324:    */     }
/* 325:354 */     verifyCrc(buf);
/* 326:    */     
/* 327:    */ 
/* 328:357 */     int dataLength = 0;
/* 329:358 */     for (int i = 0; i < 4; i++) {
/* 330:359 */       dataLength |= buf.readUnsignedByte() << i * 8;
/* 331:    */     }
/* 332:361 */     int readLength = this.inflater.getTotalOut();
/* 333:362 */     if (dataLength != readLength) {
/* 334:363 */       throw new CompressionException("Number of bytes mismatch. Expected: " + dataLength + ", Got: " + readLength);
/* 335:    */     }
/* 336:366 */     return true;
/* 337:    */   }
/* 338:    */   
/* 339:    */   private void verifyCrc(ByteBuf in)
/* 340:    */   {
/* 341:370 */     long crcValue = 0L;
/* 342:371 */     for (int i = 0; i < 4; i++) {
/* 343:372 */       crcValue |= in.readUnsignedByte() << i * 8;
/* 344:    */     }
/* 345:374 */     long readCrc = this.crc.getValue();
/* 346:375 */     if (crcValue != readCrc) {
/* 347:376 */       throw new CompressionException("CRC value missmatch. Expected: " + crcValue + ", Got: " + readCrc);
/* 348:    */     }
/* 349:    */   }
/* 350:    */   
/* 351:    */   private static boolean looksLikeZlib(short cmf_flg)
/* 352:    */   {
/* 353:389 */     return ((cmf_flg & 0x7800) == 30720) && (cmf_flg % 31 == 0);
/* 354:    */   }
/* 355:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.compression.JdkZlibDecoder
 * JD-Core Version:    0.7.0.1
 */