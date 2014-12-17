/*   1:    */ package io.netty.channel.socket.nio;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.Channel;
/*   5:    */ import io.netty.channel.ChannelException;
/*   6:    */ import io.netty.channel.ChannelFuture;
/*   7:    */ import io.netty.channel.ChannelMetadata;
/*   8:    */ import io.netty.channel.ChannelOutboundBuffer;
/*   9:    */ import io.netty.channel.ChannelPromise;
/*  10:    */ import io.netty.channel.EventLoop;
/*  11:    */ import io.netty.channel.FileRegion;
/*  12:    */ import io.netty.channel.nio.AbstractNioByteChannel;
/*  13:    */ import io.netty.channel.socket.DefaultSocketChannelConfig;
/*  14:    */ import io.netty.channel.socket.ServerSocketChannel;
/*  15:    */ import io.netty.channel.socket.SocketChannelConfig;
/*  16:    */ import io.netty.util.internal.OneTimeTask;
/*  17:    */ import java.io.IOException;
/*  18:    */ import java.net.InetSocketAddress;
/*  19:    */ import java.net.Socket;
/*  20:    */ import java.net.SocketAddress;
/*  21:    */ import java.nio.ByteBuffer;
/*  22:    */ import java.nio.channels.SelectionKey;
/*  23:    */ import java.nio.channels.spi.SelectorProvider;
/*  24:    */ 
/*  25:    */ public class NioSocketChannel
/*  26:    */   extends AbstractNioByteChannel
/*  27:    */   implements io.netty.channel.socket.SocketChannel
/*  28:    */ {
/*  29: 47 */   private static final ChannelMetadata METADATA = new ChannelMetadata(false);
/*  30: 48 */   private static final SelectorProvider DEFAULT_SELECTOR_PROVIDER = SelectorProvider.provider();
/*  31:    */   private final SocketChannelConfig config;
/*  32:    */   
/*  33:    */   private static java.nio.channels.SocketChannel newSocket(SelectorProvider provider)
/*  34:    */   {
/*  35:    */     try
/*  36:    */     {
/*  37: 58 */       return provider.openSocketChannel();
/*  38:    */     }
/*  39:    */     catch (IOException e)
/*  40:    */     {
/*  41: 60 */       throw new ChannelException("Failed to open a socket.", e);
/*  42:    */     }
/*  43:    */   }
/*  44:    */   
/*  45:    */   public NioSocketChannel()
/*  46:    */   {
/*  47: 70 */     this(newSocket(DEFAULT_SELECTOR_PROVIDER));
/*  48:    */   }
/*  49:    */   
/*  50:    */   public NioSocketChannel(SelectorProvider provider)
/*  51:    */   {
/*  52: 77 */     this(newSocket(provider));
/*  53:    */   }
/*  54:    */   
/*  55:    */   public NioSocketChannel(java.nio.channels.SocketChannel socket)
/*  56:    */   {
/*  57: 84 */     this(null, socket);
/*  58:    */   }
/*  59:    */   
/*  60:    */   public NioSocketChannel(Channel parent, java.nio.channels.SocketChannel socket)
/*  61:    */   {
/*  62: 94 */     super(parent, socket);
/*  63: 95 */     this.config = new NioSocketChannelConfig(this, socket.socket(), null);
/*  64:    */   }
/*  65:    */   
/*  66:    */   public ServerSocketChannel parent()
/*  67:    */   {
/*  68:100 */     return (ServerSocketChannel)super.parent();
/*  69:    */   }
/*  70:    */   
/*  71:    */   public ChannelMetadata metadata()
/*  72:    */   {
/*  73:105 */     return METADATA;
/*  74:    */   }
/*  75:    */   
/*  76:    */   public SocketChannelConfig config()
/*  77:    */   {
/*  78:110 */     return this.config;
/*  79:    */   }
/*  80:    */   
/*  81:    */   protected java.nio.channels.SocketChannel javaChannel()
/*  82:    */   {
/*  83:115 */     return (java.nio.channels.SocketChannel)super.javaChannel();
/*  84:    */   }
/*  85:    */   
/*  86:    */   public boolean isActive()
/*  87:    */   {
/*  88:120 */     java.nio.channels.SocketChannel ch = javaChannel();
/*  89:121 */     return (ch.isOpen()) && (ch.isConnected());
/*  90:    */   }
/*  91:    */   
/*  92:    */   public boolean isInputShutdown()
/*  93:    */   {
/*  94:126 */     return super.isInputShutdown();
/*  95:    */   }
/*  96:    */   
/*  97:    */   public InetSocketAddress localAddress()
/*  98:    */   {
/*  99:131 */     return (InetSocketAddress)super.localAddress();
/* 100:    */   }
/* 101:    */   
/* 102:    */   public InetSocketAddress remoteAddress()
/* 103:    */   {
/* 104:136 */     return (InetSocketAddress)super.remoteAddress();
/* 105:    */   }
/* 106:    */   
/* 107:    */   public boolean isOutputShutdown()
/* 108:    */   {
/* 109:141 */     return (javaChannel().socket().isOutputShutdown()) || (!isActive());
/* 110:    */   }
/* 111:    */   
/* 112:    */   public ChannelFuture shutdownOutput()
/* 113:    */   {
/* 114:146 */     return shutdownOutput(newPromise());
/* 115:    */   }
/* 116:    */   
/* 117:    */   public ChannelFuture shutdownOutput(final ChannelPromise promise)
/* 118:    */   {
/* 119:151 */     EventLoop loop = eventLoop();
/* 120:152 */     if (loop.inEventLoop()) {
/* 121:    */       try
/* 122:    */       {
/* 123:154 */         javaChannel().socket().shutdownOutput();
/* 124:155 */         promise.setSuccess();
/* 125:    */       }
/* 126:    */       catch (Throwable t)
/* 127:    */       {
/* 128:157 */         promise.setFailure(t);
/* 129:    */       }
/* 130:    */     } else {
/* 131:160 */       loop.execute(new OneTimeTask()
/* 132:    */       {
/* 133:    */         public void run()
/* 134:    */         {
/* 135:163 */           NioSocketChannel.this.shutdownOutput(promise);
/* 136:    */         }
/* 137:    */       });
/* 138:    */     }
/* 139:167 */     return promise;
/* 140:    */   }
/* 141:    */   
/* 142:    */   protected SocketAddress localAddress0()
/* 143:    */   {
/* 144:172 */     return javaChannel().socket().getLocalSocketAddress();
/* 145:    */   }
/* 146:    */   
/* 147:    */   protected SocketAddress remoteAddress0()
/* 148:    */   {
/* 149:177 */     return javaChannel().socket().getRemoteSocketAddress();
/* 150:    */   }
/* 151:    */   
/* 152:    */   protected void doBind(SocketAddress localAddress)
/* 153:    */     throws Exception
/* 154:    */   {
/* 155:182 */     javaChannel().socket().bind(localAddress);
/* 156:    */   }
/* 157:    */   
/* 158:    */   protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress)
/* 159:    */     throws Exception
/* 160:    */   {
/* 161:187 */     if (localAddress != null) {
/* 162:188 */       javaChannel().socket().bind(localAddress);
/* 163:    */     }
/* 164:191 */     boolean success = false;
/* 165:    */     try
/* 166:    */     {
/* 167:193 */       boolean connected = javaChannel().connect(remoteAddress);
/* 168:194 */       if (!connected) {
/* 169:195 */         selectionKey().interestOps(8);
/* 170:    */       }
/* 171:197 */       success = true;
/* 172:198 */       return connected;
/* 173:    */     }
/* 174:    */     finally
/* 175:    */     {
/* 176:200 */       if (!success) {
/* 177:201 */         doClose();
/* 178:    */       }
/* 179:    */     }
/* 180:    */   }
/* 181:    */   
/* 182:    */   protected void doFinishConnect()
/* 183:    */     throws Exception
/* 184:    */   {
/* 185:208 */     if (!javaChannel().finishConnect()) {
/* 186:209 */       throw new Error();
/* 187:    */     }
/* 188:    */   }
/* 189:    */   
/* 190:    */   protected void doDisconnect()
/* 191:    */     throws Exception
/* 192:    */   {
/* 193:215 */     doClose();
/* 194:    */   }
/* 195:    */   
/* 196:    */   protected void doClose()
/* 197:    */     throws Exception
/* 198:    */   {
/* 199:220 */     javaChannel().close();
/* 200:    */   }
/* 201:    */   
/* 202:    */   protected int doReadBytes(ByteBuf byteBuf)
/* 203:    */     throws Exception
/* 204:    */   {
/* 205:225 */     return byteBuf.writeBytes(javaChannel(), byteBuf.writableBytes());
/* 206:    */   }
/* 207:    */   
/* 208:    */   protected int doWriteBytes(ByteBuf buf)
/* 209:    */     throws Exception
/* 210:    */   {
/* 211:230 */     int expectedWrittenBytes = buf.readableBytes();
/* 212:231 */     int writtenBytes = buf.readBytes(javaChannel(), expectedWrittenBytes);
/* 213:232 */     return writtenBytes;
/* 214:    */   }
/* 215:    */   
/* 216:    */   protected long doWriteFileRegion(FileRegion region)
/* 217:    */     throws Exception
/* 218:    */   {
/* 219:237 */     long position = region.transfered();
/* 220:238 */     long writtenBytes = region.transferTo(javaChannel(), position);
/* 221:239 */     return writtenBytes;
/* 222:    */   }
/* 223:    */   
/* 224:    */   protected void doWrite(ChannelOutboundBuffer in)
/* 225:    */     throws Exception
/* 226:    */   {
/* 227:    */     for (;;)
/* 228:    */     {
/* 229:246 */       int msgCount = in.size();
/* 230:247 */       if (msgCount <= 1)
/* 231:    */       {
/* 232:248 */         super.doWrite(in);
/* 233:249 */         return;
/* 234:    */       }
/* 235:253 */       ByteBuffer[] nioBuffers = in.nioBuffers();
/* 236:254 */       if (nioBuffers == null)
/* 237:    */       {
/* 238:255 */         super.doWrite(in);
/* 239:256 */         return;
/* 240:    */       }
/* 241:259 */       int nioBufferCnt = in.nioBufferCount();
/* 242:260 */       long expectedWrittenBytes = in.nioBufferSize();
/* 243:    */       
/* 244:262 */       java.nio.channels.SocketChannel ch = javaChannel();
/* 245:263 */       long writtenBytes = 0L;
/* 246:264 */       boolean done = false;
/* 247:265 */       boolean setOpWrite = false;
/* 248:266 */       for (int i = config().getWriteSpinCount() - 1; i >= 0; i--)
/* 249:    */       {
/* 250:267 */         long localWrittenBytes = ch.write(nioBuffers, 0, nioBufferCnt);
/* 251:268 */         if (localWrittenBytes == 0L)
/* 252:    */         {
/* 253:269 */           setOpWrite = true;
/* 254:270 */           break;
/* 255:    */         }
/* 256:272 */         expectedWrittenBytes -= localWrittenBytes;
/* 257:273 */         writtenBytes += localWrittenBytes;
/* 258:274 */         if (expectedWrittenBytes == 0L)
/* 259:    */         {
/* 260:275 */           done = true;
/* 261:276 */           break;
/* 262:    */         }
/* 263:    */       }
/* 264:280 */       if (done)
/* 265:    */       {
/* 266:282 */         for (int i = msgCount; i > 0; i--) {
/* 267:283 */           in.remove();
/* 268:    */         }
/* 269:287 */         if (in.isEmpty())
/* 270:    */         {
/* 271:288 */           clearOpWrite();
/* 272:289 */           break;
/* 273:    */         }
/* 274:    */       }
/* 275:    */       else
/* 276:    */       {
/* 277:295 */         for (int i = msgCount; i > 0; i--)
/* 278:    */         {
/* 279:296 */           ByteBuf buf = (ByteBuf)in.current();
/* 280:297 */           int readerIndex = buf.readerIndex();
/* 281:298 */           int readableBytes = buf.writerIndex() - readerIndex;
/* 282:300 */           if (readableBytes < writtenBytes)
/* 283:    */           {
/* 284:301 */             in.progress(readableBytes);
/* 285:302 */             in.remove();
/* 286:303 */             writtenBytes -= readableBytes;
/* 287:    */           }
/* 288:    */           else
/* 289:    */           {
/* 290:304 */             if (readableBytes > writtenBytes)
/* 291:    */             {
/* 292:305 */               buf.readerIndex(readerIndex + (int)writtenBytes);
/* 293:306 */               in.progress(writtenBytes);
/* 294:307 */               break;
/* 295:    */             }
/* 296:309 */             in.progress(readableBytes);
/* 297:310 */             in.remove();
/* 298:311 */             break;
/* 299:    */           }
/* 300:    */         }
/* 301:315 */         incompleteWrite(setOpWrite);
/* 302:316 */         break;
/* 303:    */       }
/* 304:    */     }
/* 305:    */   }
/* 306:    */   
/* 307:    */   private final class NioSocketChannelConfig
/* 308:    */     extends DefaultSocketChannelConfig
/* 309:    */   {
/* 310:    */     private NioSocketChannelConfig(NioSocketChannel channel, Socket javaSocket)
/* 311:    */     {
/* 312:323 */       super(javaSocket);
/* 313:    */     }
/* 314:    */     
/* 315:    */     protected void autoReadCleared()
/* 316:    */     {
/* 317:328 */       NioSocketChannel.this.setReadPending(false);
/* 318:    */     }
/* 319:    */   }
/* 320:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.socket.nio.NioSocketChannel
 * JD-Core Version:    0.7.0.1
 */