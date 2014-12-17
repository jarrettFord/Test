/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.channel.ChannelFuture;
/*   6:    */ import io.netty.channel.ChannelFutureListener;
/*   7:    */ import io.netty.channel.ChannelHandlerContext;
/*   8:    */ import io.netty.channel.ChannelPromise;
/*   9:    */ import io.netty.channel.ChannelPromiseNotifier;
/*  10:    */ import io.netty.util.concurrent.EventExecutor;
/*  11:    */ import java.util.concurrent.TimeUnit;
/*  12:    */ import java.util.zip.CRC32;
/*  13:    */ import java.util.zip.Deflater;
/*  14:    */ 
/*  15:    */ public class JdkZlibEncoder
/*  16:    */   extends ZlibEncoder
/*  17:    */ {
/*  18:    */   private final ZlibWrapper wrapper;
/*  19:    */   private final Deflater deflater;
/*  20:    */   private volatile boolean finished;
/*  21:    */   private volatile ChannelHandlerContext ctx;
/*  22: 44 */   private final CRC32 crc = new CRC32();
/*  23: 45 */   private static final byte[] gzipHeader = { 31, -117, 8, 0, 0, 0, 0, 0, 0, 0 };
/*  24: 46 */   private boolean writeHeader = true;
/*  25:    */   
/*  26:    */   public JdkZlibEncoder()
/*  27:    */   {
/*  28: 55 */     this(6);
/*  29:    */   }
/*  30:    */   
/*  31:    */   public JdkZlibEncoder(int compressionLevel)
/*  32:    */   {
/*  33: 70 */     this(ZlibWrapper.ZLIB, compressionLevel);
/*  34:    */   }
/*  35:    */   
/*  36:    */   public JdkZlibEncoder(ZlibWrapper wrapper)
/*  37:    */   {
/*  38: 80 */     this(wrapper, 6);
/*  39:    */   }
/*  40:    */   
/*  41:    */   public JdkZlibEncoder(ZlibWrapper wrapper, int compressionLevel)
/*  42:    */   {
/*  43: 95 */     if ((compressionLevel < 0) || (compressionLevel > 9)) {
/*  44: 96 */       throw new IllegalArgumentException("compressionLevel: " + compressionLevel + " (expected: 0-9)");
/*  45:    */     }
/*  46: 99 */     if (wrapper == null) {
/*  47:100 */       throw new NullPointerException("wrapper");
/*  48:    */     }
/*  49:102 */     if (wrapper == ZlibWrapper.ZLIB_OR_NONE) {
/*  50:103 */       throw new IllegalArgumentException("wrapper '" + ZlibWrapper.ZLIB_OR_NONE + "' is not " + "allowed for compression.");
/*  51:    */     }
/*  52:108 */     this.wrapper = wrapper;
/*  53:109 */     this.deflater = new Deflater(compressionLevel, wrapper != ZlibWrapper.ZLIB);
/*  54:    */   }
/*  55:    */   
/*  56:    */   public JdkZlibEncoder(byte[] dictionary)
/*  57:    */   {
/*  58:123 */     this(6, dictionary);
/*  59:    */   }
/*  60:    */   
/*  61:    */   public JdkZlibEncoder(int compressionLevel, byte[] dictionary)
/*  62:    */   {
/*  63:141 */     if ((compressionLevel < 0) || (compressionLevel > 9)) {
/*  64:142 */       throw new IllegalArgumentException("compressionLevel: " + compressionLevel + " (expected: 0-9)");
/*  65:    */     }
/*  66:145 */     if (dictionary == null) {
/*  67:146 */       throw new NullPointerException("dictionary");
/*  68:    */     }
/*  69:149 */     this.wrapper = ZlibWrapper.ZLIB;
/*  70:150 */     this.deflater = new Deflater(compressionLevel);
/*  71:151 */     this.deflater.setDictionary(dictionary);
/*  72:    */   }
/*  73:    */   
/*  74:    */   public ChannelFuture close()
/*  75:    */   {
/*  76:156 */     return close(ctx().newPromise());
/*  77:    */   }
/*  78:    */   
/*  79:    */   public ChannelFuture close(final ChannelPromise promise)
/*  80:    */   {
/*  81:161 */     ChannelHandlerContext ctx = ctx();
/*  82:162 */     EventExecutor executor = ctx.executor();
/*  83:163 */     if (executor.inEventLoop()) {
/*  84:164 */       return finishEncode(ctx, promise);
/*  85:    */     }
/*  86:166 */     final ChannelPromise p = ctx.newPromise();
/*  87:167 */     executor.execute(new Runnable()
/*  88:    */     {
/*  89:    */       public void run()
/*  90:    */       {
/*  91:170 */         ChannelFuture f = JdkZlibEncoder.this.finishEncode(JdkZlibEncoder.access$000(JdkZlibEncoder.this), p);
/*  92:171 */         f.addListener(new ChannelPromiseNotifier(new ChannelPromise[] { promise }));
/*  93:    */       }
/*  94:173 */     });
/*  95:174 */     return p;
/*  96:    */   }
/*  97:    */   
/*  98:    */   private ChannelHandlerContext ctx()
/*  99:    */   {
/* 100:179 */     ChannelHandlerContext ctx = this.ctx;
/* 101:180 */     if (ctx == null) {
/* 102:181 */       throw new IllegalStateException("not added to a pipeline");
/* 103:    */     }
/* 104:183 */     return ctx;
/* 105:    */   }
/* 106:    */   
/* 107:    */   public boolean isClosed()
/* 108:    */   {
/* 109:188 */     return this.finished;
/* 110:    */   }
/* 111:    */   
/* 112:    */   protected void encode(ChannelHandlerContext ctx, ByteBuf uncompressed, ByteBuf out)
/* 113:    */     throws Exception
/* 114:    */   {
/* 115:193 */     if (this.finished)
/* 116:    */     {
/* 117:194 */       out.writeBytes(uncompressed);
/* 118:195 */       return;
/* 119:    */     }
/* 120:198 */     int len = uncompressed.readableBytes();
/* 121:    */     byte[] inAry;
/* 122:    */     int offset;
/* 123:201 */     if (uncompressed.hasArray())
/* 124:    */     {
/* 125:203 */       byte[] inAry = uncompressed.array();
/* 126:204 */       int offset = uncompressed.arrayOffset() + uncompressed.readerIndex();
/* 127:    */       
/* 128:206 */       uncompressed.skipBytes(len);
/* 129:    */     }
/* 130:    */     else
/* 131:    */     {
/* 132:208 */       inAry = new byte[len];
/* 133:209 */       uncompressed.readBytes(inAry);
/* 134:210 */       offset = 0;
/* 135:    */     }
/* 136:213 */     if (this.writeHeader)
/* 137:    */     {
/* 138:214 */       this.writeHeader = false;
/* 139:215 */       if (this.wrapper == ZlibWrapper.GZIP) {
/* 140:216 */         out.writeBytes(gzipHeader);
/* 141:    */       }
/* 142:    */     }
/* 143:220 */     if (this.wrapper == ZlibWrapper.GZIP) {
/* 144:221 */       this.crc.update(inAry, offset, len);
/* 145:    */     }
/* 146:224 */     this.deflater.setInput(inAry, offset, len);
/* 147:225 */     while (!this.deflater.needsInput()) {
/* 148:226 */       deflate(out);
/* 149:    */     }
/* 150:    */   }
/* 151:    */   
/* 152:    */   protected final ByteBuf allocateBuffer(ChannelHandlerContext ctx, ByteBuf msg, boolean preferDirect)
/* 153:    */     throws Exception
/* 154:    */   {
/* 155:233 */     int sizeEstimate = (int)Math.ceil(msg.readableBytes() * 1.001D) + 12;
/* 156:234 */     if (this.writeHeader) {
/* 157:235 */       switch (4.$SwitchMap$io$netty$handler$codec$compression$ZlibWrapper[this.wrapper.ordinal()])
/* 158:    */       {
/* 159:    */       case 1: 
/* 160:237 */         sizeEstimate += gzipHeader.length;
/* 161:238 */         break;
/* 162:    */       case 2: 
/* 163:240 */         sizeEstimate += 2;
/* 164:    */       }
/* 165:    */     }
/* 166:244 */     return ctx.alloc().heapBuffer(sizeEstimate);
/* 167:    */   }
/* 168:    */   
/* 169:    */   public void close(final ChannelHandlerContext ctx, final ChannelPromise promise)
/* 170:    */     throws Exception
/* 171:    */   {
/* 172:249 */     ChannelFuture f = finishEncode(ctx, ctx.newPromise());
/* 173:250 */     f.addListener(new ChannelFutureListener()
/* 174:    */     {
/* 175:    */       public void operationComplete(ChannelFuture f)
/* 176:    */         throws Exception
/* 177:    */       {
/* 178:253 */         ctx.close(promise);
/* 179:    */       }
/* 180:    */     });
/* 181:257 */     if (!f.isDone()) {
/* 182:259 */       ctx.executor().schedule(new Runnable()
/* 183:    */       {
/* 184:    */         public void run()
/* 185:    */         {
/* 186:262 */           ctx.close(promise);
/* 187:    */         }
/* 188:262 */       }, 10L, TimeUnit.SECONDS);
/* 189:    */     }
/* 190:    */   }
/* 191:    */   
/* 192:    */   private ChannelFuture finishEncode(ChannelHandlerContext ctx, ChannelPromise promise)
/* 193:    */   {
/* 194:269 */     if (this.finished)
/* 195:    */     {
/* 196:270 */       promise.setSuccess();
/* 197:271 */       return promise;
/* 198:    */     }
/* 199:274 */     this.finished = true;
/* 200:275 */     ByteBuf footer = ctx.alloc().heapBuffer();
/* 201:276 */     if ((this.writeHeader) && (this.wrapper == ZlibWrapper.GZIP))
/* 202:    */     {
/* 203:278 */       this.writeHeader = false;
/* 204:279 */       footer.writeBytes(gzipHeader);
/* 205:    */     }
/* 206:282 */     this.deflater.finish();
/* 207:284 */     while (!this.deflater.finished())
/* 208:    */     {
/* 209:285 */       deflate(footer);
/* 210:286 */       if (!footer.isWritable())
/* 211:    */       {
/* 212:288 */         ctx.write(footer);
/* 213:289 */         footer = ctx.alloc().heapBuffer();
/* 214:    */       }
/* 215:    */     }
/* 216:292 */     if (this.wrapper == ZlibWrapper.GZIP)
/* 217:    */     {
/* 218:293 */       int crcValue = (int)this.crc.getValue();
/* 219:294 */       int uncBytes = this.deflater.getTotalIn();
/* 220:295 */       footer.writeByte(crcValue);
/* 221:296 */       footer.writeByte(crcValue >>> 8);
/* 222:297 */       footer.writeByte(crcValue >>> 16);
/* 223:298 */       footer.writeByte(crcValue >>> 24);
/* 224:299 */       footer.writeByte(uncBytes);
/* 225:300 */       footer.writeByte(uncBytes >>> 8);
/* 226:301 */       footer.writeByte(uncBytes >>> 16);
/* 227:302 */       footer.writeByte(uncBytes >>> 24);
/* 228:    */     }
/* 229:304 */     this.deflater.end();
/* 230:305 */     return ctx.writeAndFlush(footer, promise);
/* 231:    */   }
/* 232:    */   
/* 233:    */   private void deflate(ByteBuf out)
/* 234:    */   {
/* 235:    */     int numBytes;
/* 236:    */     do
/* 237:    */     {
/* 238:311 */       int writerIndex = out.writerIndex();
/* 239:312 */       numBytes = this.deflater.deflate(out.array(), out.arrayOffset() + writerIndex, out.writableBytes(), 2);
/* 240:    */       
/* 241:314 */       out.writerIndex(writerIndex + numBytes);
/* 242:315 */     } while (numBytes > 0);
/* 243:    */   }
/* 244:    */   
/* 245:    */   public void handlerAdded(ChannelHandlerContext ctx)
/* 246:    */     throws Exception
/* 247:    */   {
/* 248:320 */     this.ctx = ctx;
/* 249:    */   }
/* 250:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.compression.JdkZlibEncoder
 * JD-Core Version:    0.7.0.1
 */