/*   1:    */ package io.netty.channel.nio;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.channel.Channel;
/*   6:    */ import io.netty.channel.ChannelConfig;
/*   7:    */ import io.netty.channel.ChannelOption;
/*   8:    */ import io.netty.channel.ChannelOutboundBuffer;
/*   9:    */ import io.netty.channel.ChannelPipeline;
/*  10:    */ import io.netty.channel.FileRegion;
/*  11:    */ import io.netty.channel.RecvByteBufAllocator;
/*  12:    */ import io.netty.channel.RecvByteBufAllocator.Handle;
/*  13:    */ import io.netty.channel.socket.ChannelInputShutdownEvent;
/*  14:    */ import io.netty.util.internal.StringUtil;
/*  15:    */ import java.io.IOException;
/*  16:    */ import java.nio.channels.SelectableChannel;
/*  17:    */ import java.nio.channels.SelectionKey;
/*  18:    */ 
/*  19:    */ public abstract class AbstractNioByteChannel
/*  20:    */   extends AbstractNioChannel
/*  21:    */ {
/*  22:    */   private Runnable flushTask;
/*  23:    */   
/*  24:    */   protected AbstractNioByteChannel(Channel parent, SelectableChannel ch)
/*  25:    */   {
/*  26: 47 */     super(parent, ch, 1);
/*  27:    */   }
/*  28:    */   
/*  29:    */   protected AbstractNioChannel.AbstractNioUnsafe newUnsafe()
/*  30:    */   {
/*  31: 52 */     return new NioByteUnsafe(null);
/*  32:    */   }
/*  33:    */   
/*  34:    */   private final class NioByteUnsafe
/*  35:    */     extends AbstractNioChannel.AbstractNioUnsafe
/*  36:    */   {
/*  37:    */     private RecvByteBufAllocator.Handle allocHandle;
/*  38:    */     
/*  39:    */     private NioByteUnsafe()
/*  40:    */     {
/*  41: 55 */       super();
/*  42:    */     }
/*  43:    */     
/*  44:    */     private void closeOnRead(ChannelPipeline pipeline)
/*  45:    */     {
/*  46: 59 */       SelectionKey key = AbstractNioByteChannel.this.selectionKey();
/*  47: 60 */       AbstractNioByteChannel.this.setInputShutdown();
/*  48: 61 */       if (AbstractNioByteChannel.this.isOpen()) {
/*  49: 62 */         if (Boolean.TRUE.equals(AbstractNioByteChannel.this.config().getOption(ChannelOption.ALLOW_HALF_CLOSURE)))
/*  50:    */         {
/*  51: 63 */           key.interestOps(key.interestOps() & (AbstractNioByteChannel.this.readInterestOp ^ 0xFFFFFFFF));
/*  52: 64 */           pipeline.fireUserEventTriggered(ChannelInputShutdownEvent.INSTANCE);
/*  53:    */         }
/*  54:    */         else
/*  55:    */         {
/*  56: 66 */           close(voidPromise());
/*  57:    */         }
/*  58:    */       }
/*  59:    */     }
/*  60:    */     
/*  61:    */     private void handleReadException(ChannelPipeline pipeline, ByteBuf byteBuf, Throwable cause, boolean close)
/*  62:    */     {
/*  63: 73 */       if (byteBuf != null) {
/*  64: 74 */         if (byteBuf.isReadable())
/*  65:    */         {
/*  66: 75 */           AbstractNioByteChannel.this.setReadPending(false);
/*  67: 76 */           pipeline.fireChannelRead(byteBuf);
/*  68:    */         }
/*  69:    */         else
/*  70:    */         {
/*  71: 78 */           byteBuf.release();
/*  72:    */         }
/*  73:    */       }
/*  74: 81 */       pipeline.fireChannelReadComplete();
/*  75: 82 */       pipeline.fireExceptionCaught(cause);
/*  76: 83 */       if ((close) || ((cause instanceof IOException))) {
/*  77: 84 */         closeOnRead(pipeline);
/*  78:    */       }
/*  79:    */     }
/*  80:    */     
/*  81:    */     public void read()
/*  82:    */     {
/*  83: 90 */       ChannelConfig config = AbstractNioByteChannel.this.config();
/*  84: 91 */       if ((!config.isAutoRead()) && (!AbstractNioByteChannel.this.isReadPending()))
/*  85:    */       {
/*  86: 93 */         removeReadOp();
/*  87: 94 */         return;
/*  88:    */       }
/*  89: 97 */       ChannelPipeline pipeline = AbstractNioByteChannel.this.pipeline();
/*  90: 98 */       ByteBufAllocator allocator = config.getAllocator();
/*  91: 99 */       int maxMessagesPerRead = config.getMaxMessagesPerRead();
/*  92:100 */       RecvByteBufAllocator.Handle allocHandle = this.allocHandle;
/*  93:101 */       if (allocHandle == null) {
/*  94:102 */         this.allocHandle = (allocHandle = config.getRecvByteBufAllocator().newHandle());
/*  95:    */       }
/*  96:105 */       ByteBuf byteBuf = null;
/*  97:106 */       int messages = 0;
/*  98:107 */       boolean close = false;
/*  99:    */       try
/* 100:    */       {
/* 101:109 */         int totalReadAmount = 0;
/* 102:110 */         boolean readPendingReset = false;
/* 103:    */         do
/* 104:    */         {
/* 105:112 */           byteBuf = allocHandle.allocate(allocator);
/* 106:113 */           int writable = byteBuf.writableBytes();
/* 107:114 */           int localReadAmount = AbstractNioByteChannel.this.doReadBytes(byteBuf);
/* 108:115 */           if (localReadAmount <= 0)
/* 109:    */           {
/* 110:117 */             byteBuf.release();
/* 111:118 */             close = localReadAmount < 0;
/* 112:119 */             break;
/* 113:    */           }
/* 114:121 */           if (!readPendingReset)
/* 115:    */           {
/* 116:122 */             readPendingReset = true;
/* 117:123 */             AbstractNioByteChannel.this.setReadPending(false);
/* 118:    */           }
/* 119:125 */           pipeline.fireChannelRead(byteBuf);
/* 120:126 */           byteBuf = null;
/* 121:128 */           if (totalReadAmount >= 2147483647 - localReadAmount)
/* 122:    */           {
/* 123:130 */             totalReadAmount = 2147483647;
/* 124:131 */             break;
/* 125:    */           }
/* 126:134 */           totalReadAmount += localReadAmount;
/* 127:137 */           if (!config.isAutoRead()) {
/* 128:    */             break;
/* 129:    */           }
/* 130:141 */           if (localReadAmount < writable) {
/* 131:    */             break;
/* 132:    */           }
/* 133:146 */           messages++;
/* 134:146 */         } while (messages < maxMessagesPerRead);
/* 135:148 */         pipeline.fireChannelReadComplete();
/* 136:149 */         allocHandle.record(totalReadAmount);
/* 137:151 */         if (close)
/* 138:    */         {
/* 139:152 */           closeOnRead(pipeline);
/* 140:153 */           close = false;
/* 141:    */         }
/* 142:    */       }
/* 143:    */       catch (Throwable t)
/* 144:    */       {
/* 145:156 */         handleReadException(pipeline, byteBuf, t, close);
/* 146:    */       }
/* 147:    */       finally
/* 148:    */       {
/* 149:164 */         if ((!config.isAutoRead()) && (!AbstractNioByteChannel.this.isReadPending())) {
/* 150:165 */           removeReadOp();
/* 151:    */         }
/* 152:    */       }
/* 153:    */     }
/* 154:    */   }
/* 155:    */   
/* 156:    */   protected void doWrite(ChannelOutboundBuffer in)
/* 157:    */     throws Exception
/* 158:    */   {
/* 159:173 */     int writeSpinCount = -1;
/* 160:    */     for (;;)
/* 161:    */     {
/* 162:176 */       Object msg = in.current();
/* 163:177 */       if (msg == null)
/* 164:    */       {
/* 165:179 */         clearOpWrite();
/* 166:180 */         break;
/* 167:    */       }
/* 168:183 */       if ((msg instanceof ByteBuf))
/* 169:    */       {
/* 170:184 */         ByteBuf buf = (ByteBuf)msg;
/* 171:185 */         int readableBytes = buf.readableBytes();
/* 172:186 */         if (readableBytes == 0)
/* 173:    */         {
/* 174:187 */           in.remove();
/* 175:    */         }
/* 176:    */         else
/* 177:    */         {
/* 178:191 */           if (!buf.isDirect())
/* 179:    */           {
/* 180:192 */             ByteBufAllocator alloc = alloc();
/* 181:193 */             if (alloc.isDirectBufferPooled())
/* 182:    */             {
/* 183:197 */               buf = alloc.directBuffer(readableBytes).writeBytes(buf);
/* 184:198 */               in.current(buf);
/* 185:    */             }
/* 186:    */           }
/* 187:202 */           boolean setOpWrite = false;
/* 188:203 */           boolean done = false;
/* 189:204 */           long flushedAmount = 0L;
/* 190:205 */           if (writeSpinCount == -1) {
/* 191:206 */             writeSpinCount = config().getWriteSpinCount();
/* 192:    */           }
/* 193:208 */           for (int i = writeSpinCount - 1; i >= 0; i--)
/* 194:    */           {
/* 195:209 */             int localFlushedAmount = doWriteBytes(buf);
/* 196:210 */             if (localFlushedAmount == 0)
/* 197:    */             {
/* 198:211 */               setOpWrite = true;
/* 199:212 */               break;
/* 200:    */             }
/* 201:215 */             flushedAmount += localFlushedAmount;
/* 202:216 */             if (!buf.isReadable())
/* 203:    */             {
/* 204:217 */               done = true;
/* 205:218 */               break;
/* 206:    */             }
/* 207:    */           }
/* 208:222 */           in.progress(flushedAmount);
/* 209:224 */           if (done)
/* 210:    */           {
/* 211:225 */             in.remove();
/* 212:    */           }
/* 213:    */           else
/* 214:    */           {
/* 215:227 */             incompleteWrite(setOpWrite);
/* 216:228 */             break;
/* 217:    */           }
/* 218:    */         }
/* 219:    */       }
/* 220:230 */       else if ((msg instanceof FileRegion))
/* 221:    */       {
/* 222:231 */         FileRegion region = (FileRegion)msg;
/* 223:232 */         boolean setOpWrite = false;
/* 224:233 */         boolean done = false;
/* 225:234 */         long flushedAmount = 0L;
/* 226:235 */         if (writeSpinCount == -1) {
/* 227:236 */           writeSpinCount = config().getWriteSpinCount();
/* 228:    */         }
/* 229:238 */         for (int i = writeSpinCount - 1; i >= 0; i--)
/* 230:    */         {
/* 231:239 */           long localFlushedAmount = doWriteFileRegion(region);
/* 232:240 */           if (localFlushedAmount == 0L)
/* 233:    */           {
/* 234:241 */             setOpWrite = true;
/* 235:242 */             break;
/* 236:    */           }
/* 237:245 */           flushedAmount += localFlushedAmount;
/* 238:246 */           if (region.transfered() >= region.count())
/* 239:    */           {
/* 240:247 */             done = true;
/* 241:248 */             break;
/* 242:    */           }
/* 243:    */         }
/* 244:252 */         in.progress(flushedAmount);
/* 245:254 */         if (done)
/* 246:    */         {
/* 247:255 */           in.remove();
/* 248:    */         }
/* 249:    */         else
/* 250:    */         {
/* 251:257 */           incompleteWrite(setOpWrite);
/* 252:258 */           break;
/* 253:    */         }
/* 254:    */       }
/* 255:    */       else
/* 256:    */       {
/* 257:261 */         throw new UnsupportedOperationException("unsupported message type: " + StringUtil.simpleClassName(msg));
/* 258:    */       }
/* 259:    */     }
/* 260:    */   }
/* 261:    */   
/* 262:    */   protected final void incompleteWrite(boolean setOpWrite)
/* 263:    */   {
/* 264:268 */     if (setOpWrite)
/* 265:    */     {
/* 266:269 */       setOpWrite();
/* 267:    */     }
/* 268:    */     else
/* 269:    */     {
/* 270:272 */       Runnable flushTask = this.flushTask;
/* 271:273 */       if (flushTask == null) {
/* 272:274 */         flushTask = this.flushTask = new Runnable()
/* 273:    */         {
/* 274:    */           public void run()
/* 275:    */           {
/* 276:277 */             AbstractNioByteChannel.this.flush();
/* 277:    */           }
/* 278:    */         };
/* 279:    */       }
/* 280:281 */       eventLoop().execute(flushTask);
/* 281:    */     }
/* 282:    */   }
/* 283:    */   
/* 284:    */   protected abstract long doWriteFileRegion(FileRegion paramFileRegion)
/* 285:    */     throws Exception;
/* 286:    */   
/* 287:    */   protected abstract int doReadBytes(ByteBuf paramByteBuf)
/* 288:    */     throws Exception;
/* 289:    */   
/* 290:    */   protected abstract int doWriteBytes(ByteBuf paramByteBuf)
/* 291:    */     throws Exception;
/* 292:    */   
/* 293:    */   protected final void setOpWrite()
/* 294:    */   {
/* 295:306 */     SelectionKey key = selectionKey();
/* 296:310 */     if (!key.isValid()) {
/* 297:311 */       return;
/* 298:    */     }
/* 299:313 */     int interestOps = key.interestOps();
/* 300:314 */     if ((interestOps & 0x4) == 0) {
/* 301:315 */       key.interestOps(interestOps | 0x4);
/* 302:    */     }
/* 303:    */   }
/* 304:    */   
/* 305:    */   protected final void clearOpWrite()
/* 306:    */   {
/* 307:320 */     SelectionKey key = selectionKey();
/* 308:324 */     if (!key.isValid()) {
/* 309:325 */       return;
/* 310:    */     }
/* 311:327 */     int interestOps = key.interestOps();
/* 312:328 */     if ((interestOps & 0x4) != 0) {
/* 313:329 */       key.interestOps(interestOps & 0xFFFFFFFB);
/* 314:    */     }
/* 315:    */   }
/* 316:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.nio.AbstractNioByteChannel
 * JD-Core Version:    0.7.0.1
 */