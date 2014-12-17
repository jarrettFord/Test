/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ import com.jcraft.jzlib.Deflater;
/*   4:    */ import com.jcraft.jzlib.JZlib;
/*   5:    */ import io.netty.buffer.ByteBuf;
/*   6:    */ import io.netty.buffer.Unpooled;
/*   7:    */ import io.netty.channel.Channel;
/*   8:    */ import io.netty.channel.ChannelFuture;
/*   9:    */ import io.netty.channel.ChannelFutureListener;
/*  10:    */ import io.netty.channel.ChannelHandlerContext;
/*  11:    */ import io.netty.channel.ChannelPromise;
/*  12:    */ import io.netty.channel.ChannelPromiseNotifier;
/*  13:    */ import io.netty.util.concurrent.EventExecutor;
/*  14:    */ import io.netty.util.internal.EmptyArrays;
/*  15:    */ import java.util.concurrent.TimeUnit;
/*  16:    */ 
/*  17:    */ public class JZlibEncoder
/*  18:    */   extends ZlibEncoder
/*  19:    */ {
/*  20:    */   private final int wrapperOverhead;
/*  21: 39 */   private final Deflater z = new Deflater();
/*  22:    */   private volatile boolean finished;
/*  23:    */   private volatile ChannelHandlerContext ctx;
/*  24:    */   
/*  25:    */   public JZlibEncoder()
/*  26:    */   {
/*  27: 51 */     this(6);
/*  28:    */   }
/*  29:    */   
/*  30:    */   public JZlibEncoder(int compressionLevel)
/*  31:    */   {
/*  32: 67 */     this(ZlibWrapper.ZLIB, compressionLevel);
/*  33:    */   }
/*  34:    */   
/*  35:    */   public JZlibEncoder(ZlibWrapper wrapper)
/*  36:    */   {
/*  37: 78 */     this(wrapper, 6);
/*  38:    */   }
/*  39:    */   
/*  40:    */   public JZlibEncoder(ZlibWrapper wrapper, int compressionLevel)
/*  41:    */   {
/*  42: 94 */     this(wrapper, compressionLevel, 15, 8);
/*  43:    */   }
/*  44:    */   
/*  45:    */   public JZlibEncoder(ZlibWrapper wrapper, int compressionLevel, int windowBits, int memLevel)
/*  46:    */   {
/*  47:121 */     if ((compressionLevel < 0) || (compressionLevel > 9)) {
/*  48:122 */       throw new IllegalArgumentException("compressionLevel: " + compressionLevel + " (expected: 0-9)");
/*  49:    */     }
/*  50:126 */     if ((windowBits < 9) || (windowBits > 15)) {
/*  51:127 */       throw new IllegalArgumentException("windowBits: " + windowBits + " (expected: 9-15)");
/*  52:    */     }
/*  53:130 */     if ((memLevel < 1) || (memLevel > 9)) {
/*  54:131 */       throw new IllegalArgumentException("memLevel: " + memLevel + " (expected: 1-9)");
/*  55:    */     }
/*  56:134 */     if (wrapper == null) {
/*  57:135 */       throw new NullPointerException("wrapper");
/*  58:    */     }
/*  59:137 */     if (wrapper == ZlibWrapper.ZLIB_OR_NONE) {
/*  60:138 */       throw new IllegalArgumentException("wrapper '" + ZlibWrapper.ZLIB_OR_NONE + "' is not " + "allowed for compression.");
/*  61:    */     }
/*  62:143 */     int resultCode = this.z.init(compressionLevel, windowBits, memLevel, ZlibUtil.convertWrapperType(wrapper));
/*  63:146 */     if (resultCode != 0) {
/*  64:147 */       ZlibUtil.fail(this.z, "initialization failure", resultCode);
/*  65:    */     }
/*  66:150 */     this.wrapperOverhead = ZlibUtil.wrapperOverhead(wrapper);
/*  67:    */   }
/*  68:    */   
/*  69:    */   public JZlibEncoder(byte[] dictionary)
/*  70:    */   {
/*  71:165 */     this(6, dictionary);
/*  72:    */   }
/*  73:    */   
/*  74:    */   public JZlibEncoder(int compressionLevel, byte[] dictionary)
/*  75:    */   {
/*  76:184 */     this(compressionLevel, 15, 8, dictionary);
/*  77:    */   }
/*  78:    */   
/*  79:    */   public JZlibEncoder(int compressionLevel, int windowBits, int memLevel, byte[] dictionary)
/*  80:    */   {
/*  81:213 */     if ((compressionLevel < 0) || (compressionLevel > 9)) {
/*  82:214 */       throw new IllegalArgumentException("compressionLevel: " + compressionLevel + " (expected: 0-9)");
/*  83:    */     }
/*  84:216 */     if ((windowBits < 9) || (windowBits > 15)) {
/*  85:217 */       throw new IllegalArgumentException("windowBits: " + windowBits + " (expected: 9-15)");
/*  86:    */     }
/*  87:220 */     if ((memLevel < 1) || (memLevel > 9)) {
/*  88:221 */       throw new IllegalArgumentException("memLevel: " + memLevel + " (expected: 1-9)");
/*  89:    */     }
/*  90:224 */     if (dictionary == null) {
/*  91:225 */       throw new NullPointerException("dictionary");
/*  92:    */     }
/*  93:228 */     int resultCode = this.z.deflateInit(compressionLevel, windowBits, memLevel, JZlib.W_ZLIB);
/*  94:231 */     if (resultCode != 0)
/*  95:    */     {
/*  96:232 */       ZlibUtil.fail(this.z, "initialization failure", resultCode);
/*  97:    */     }
/*  98:    */     else
/*  99:    */     {
/* 100:234 */       resultCode = this.z.deflateSetDictionary(dictionary, dictionary.length);
/* 101:235 */       if (resultCode != 0) {
/* 102:236 */         ZlibUtil.fail(this.z, "failed to set the dictionary", resultCode);
/* 103:    */       }
/* 104:    */     }
/* 105:240 */     this.wrapperOverhead = ZlibUtil.wrapperOverhead(ZlibWrapper.ZLIB);
/* 106:    */   }
/* 107:    */   
/* 108:    */   public ChannelFuture close()
/* 109:    */   {
/* 110:245 */     return close(ctx().channel().newPromise());
/* 111:    */   }
/* 112:    */   
/* 113:    */   public ChannelFuture close(final ChannelPromise promise)
/* 114:    */   {
/* 115:250 */     ChannelHandlerContext ctx = ctx();
/* 116:251 */     EventExecutor executor = ctx.executor();
/* 117:252 */     if (executor.inEventLoop()) {
/* 118:253 */       return finishEncode(ctx, promise);
/* 119:    */     }
/* 120:255 */     final ChannelPromise p = ctx.newPromise();
/* 121:256 */     executor.execute(new Runnable()
/* 122:    */     {
/* 123:    */       public void run()
/* 124:    */       {
/* 125:259 */         ChannelFuture f = JZlibEncoder.this.finishEncode(JZlibEncoder.access$000(JZlibEncoder.this), p);
/* 126:260 */         f.addListener(new ChannelPromiseNotifier(new ChannelPromise[] { promise }));
/* 127:    */       }
/* 128:262 */     });
/* 129:263 */     return p;
/* 130:    */   }
/* 131:    */   
/* 132:    */   private ChannelHandlerContext ctx()
/* 133:    */   {
/* 134:268 */     ChannelHandlerContext ctx = this.ctx;
/* 135:269 */     if (ctx == null) {
/* 136:270 */       throw new IllegalStateException("not added to a pipeline");
/* 137:    */     }
/* 138:272 */     return ctx;
/* 139:    */   }
/* 140:    */   
/* 141:    */   public boolean isClosed()
/* 142:    */   {
/* 143:277 */     return this.finished;
/* 144:    */   }
/* 145:    */   
/* 146:    */   protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out)
/* 147:    */     throws Exception
/* 148:    */   {
/* 149:282 */     if (this.finished) {
/* 150:283 */       return;
/* 151:    */     }
/* 152:    */     try
/* 153:    */     {
/* 154:288 */       int inputLength = in.readableBytes();
/* 155:289 */       boolean inHasArray = in.hasArray();
/* 156:290 */       this.z.avail_in = inputLength;
/* 157:291 */       if (inHasArray)
/* 158:    */       {
/* 159:292 */         this.z.next_in = in.array();
/* 160:293 */         this.z.next_in_index = (in.arrayOffset() + in.readerIndex());
/* 161:    */       }
/* 162:    */       else
/* 163:    */       {
/* 164:295 */         byte[] array = new byte[inputLength];
/* 165:296 */         in.getBytes(in.readerIndex(), array);
/* 166:297 */         this.z.next_in = array;
/* 167:298 */         this.z.next_in_index = 0;
/* 168:    */       }
/* 169:300 */       int oldNextInIndex = this.z.next_in_index;
/* 170:    */       
/* 171:    */ 
/* 172:303 */       int maxOutputLength = (int)Math.ceil(inputLength * 1.001D) + 12 + this.wrapperOverhead;
/* 173:304 */       out.ensureWritable(maxOutputLength);
/* 174:305 */       this.z.avail_out = maxOutputLength;
/* 175:306 */       this.z.next_out = out.array();
/* 176:307 */       this.z.next_out_index = (out.arrayOffset() + out.writerIndex());
/* 177:308 */       int oldNextOutIndex = this.z.next_out_index;
/* 178:    */       int resultCode;
/* 179:    */       try
/* 180:    */       {
/* 181:313 */         resultCode = this.z.deflate(2);
/* 182:    */       }
/* 183:    */       finally
/* 184:    */       {
/* 185:315 */         in.skipBytes(this.z.next_in_index - oldNextInIndex);
/* 186:    */       }
/* 187:318 */       if (resultCode != 0) {
/* 188:319 */         ZlibUtil.fail(this.z, "compression failure", resultCode);
/* 189:    */       }
/* 190:322 */       int outputLength = this.z.next_out_index - oldNextOutIndex;
/* 191:323 */       if (outputLength > 0) {
/* 192:324 */         out.writerIndex(out.writerIndex() + outputLength);
/* 193:    */       }
/* 194:    */     }
/* 195:    */     finally
/* 196:    */     {
/* 197:331 */       this.z.next_in = null;
/* 198:332 */       this.z.next_out = null;
/* 199:    */     }
/* 200:    */   }
/* 201:    */   
/* 202:    */   public void close(final ChannelHandlerContext ctx, final ChannelPromise promise)
/* 203:    */   {
/* 204:340 */     ChannelFuture f = finishEncode(ctx, ctx.newPromise());
/* 205:341 */     f.addListener(new ChannelFutureListener()
/* 206:    */     {
/* 207:    */       public void operationComplete(ChannelFuture f)
/* 208:    */         throws Exception
/* 209:    */       {
/* 210:344 */         ctx.close(promise);
/* 211:    */       }
/* 212:    */     });
/* 213:348 */     if (!f.isDone()) {
/* 214:350 */       ctx.executor().schedule(new Runnable()
/* 215:    */       {
/* 216:    */         public void run()
/* 217:    */         {
/* 218:353 */           ctx.close(promise);
/* 219:    */         }
/* 220:353 */       }, 10L, TimeUnit.SECONDS);
/* 221:    */     }
/* 222:    */   }
/* 223:    */   
/* 224:    */   private ChannelFuture finishEncode(ChannelHandlerContext ctx, ChannelPromise promise)
/* 225:    */   {
/* 226:360 */     if (this.finished)
/* 227:    */     {
/* 228:361 */       promise.setSuccess();
/* 229:362 */       return promise;
/* 230:    */     }
/* 231:364 */     this.finished = true;
/* 232:    */     ByteBuf footer;
/* 233:    */     try
/* 234:    */     {
/* 235:369 */       this.z.next_in = EmptyArrays.EMPTY_BYTES;
/* 236:370 */       this.z.next_in_index = 0;
/* 237:371 */       this.z.avail_in = 0;
/* 238:    */       
/* 239:    */ 
/* 240:374 */       byte[] out = new byte[32];
/* 241:375 */       this.z.next_out = out;
/* 242:376 */       this.z.next_out_index = 0;
/* 243:377 */       this.z.avail_out = out.length;
/* 244:    */       
/* 245:    */ 
/* 246:380 */       int resultCode = this.z.deflate(4);
/* 247:381 */       if ((resultCode != 0) && (resultCode != 1))
/* 248:    */       {
/* 249:382 */         promise.setFailure(ZlibUtil.deflaterException(this.z, "compression failure", resultCode));
/* 250:383 */         return promise;
/* 251:    */       }
/* 252:    */       ByteBuf footer;
/* 253:384 */       if (this.z.next_out_index != 0) {
/* 254:385 */         footer = Unpooled.wrappedBuffer(out, 0, this.z.next_out_index);
/* 255:    */       } else {
/* 256:387 */         footer = Unpooled.EMPTY_BUFFER;
/* 257:    */       }
/* 258:    */     }
/* 259:    */     finally
/* 260:    */     {
/* 261:390 */       this.z.deflateEnd();
/* 262:    */       
/* 263:    */ 
/* 264:    */ 
/* 265:    */ 
/* 266:    */ 
/* 267:396 */       this.z.next_in = null;
/* 268:397 */       this.z.next_out = null;
/* 269:    */     }
/* 270:399 */     return ctx.writeAndFlush(footer, promise);
/* 271:    */   }
/* 272:    */   
/* 273:    */   public void handlerAdded(ChannelHandlerContext ctx)
/* 274:    */     throws Exception
/* 275:    */   {
/* 276:404 */     this.ctx = ctx;
/* 277:    */   }
/* 278:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.compression.JZlibEncoder
 * JD-Core Version:    0.7.0.1
 */