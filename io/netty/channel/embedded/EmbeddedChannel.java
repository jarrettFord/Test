/*   1:    */ package io.netty.channel.embedded;
/*   2:    */ 
/*   3:    */ import io.netty.channel.AbstractChannel;
/*   4:    */ import io.netty.channel.AbstractChannel.AbstractUnsafe;
/*   5:    */ import io.netty.channel.ChannelConfig;
/*   6:    */ import io.netty.channel.ChannelFuture;
/*   7:    */ import io.netty.channel.ChannelHandler;
/*   8:    */ import io.netty.channel.ChannelHandlerContext;
/*   9:    */ import io.netty.channel.ChannelInboundHandlerAdapter;
/*  10:    */ import io.netty.channel.ChannelMetadata;
/*  11:    */ import io.netty.channel.ChannelOutboundBuffer;
/*  12:    */ import io.netty.channel.ChannelPipeline;
/*  13:    */ import io.netty.channel.ChannelPromise;
/*  14:    */ import io.netty.channel.DefaultChannelConfig;
/*  15:    */ import io.netty.channel.EventLoop;
/*  16:    */ import io.netty.util.ReferenceCountUtil;
/*  17:    */ import io.netty.util.internal.PlatformDependent;
/*  18:    */ import io.netty.util.internal.RecyclableArrayList;
/*  19:    */ import io.netty.util.internal.logging.InternalLogger;
/*  20:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  21:    */ import java.net.SocketAddress;
/*  22:    */ import java.nio.channels.ClosedChannelException;
/*  23:    */ import java.util.ArrayDeque;
/*  24:    */ import java.util.Queue;
/*  25:    */ 
/*  26:    */ public class EmbeddedChannel
/*  27:    */   extends AbstractChannel
/*  28:    */ {
/*  29: 47 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(EmbeddedChannel.class);
/*  30: 49 */   private static final ChannelMetadata METADATA = new ChannelMetadata(false);
/*  31: 51 */   private final EmbeddedEventLoop loop = new EmbeddedEventLoop();
/*  32: 52 */   private final ChannelConfig config = new DefaultChannelConfig(this);
/*  33: 53 */   private final SocketAddress localAddress = new EmbeddedSocketAddress();
/*  34: 54 */   private final SocketAddress remoteAddress = new EmbeddedSocketAddress();
/*  35: 55 */   private final Queue<Object> inboundMessages = new ArrayDeque();
/*  36: 56 */   private final Queue<Object> outboundMessages = new ArrayDeque();
/*  37:    */   private Throwable lastException;
/*  38:    */   private int state;
/*  39:    */   
/*  40:    */   public EmbeddedChannel(ChannelHandler... handlers)
/*  41:    */   {
/*  42: 66 */     super(null);
/*  43: 68 */     if (handlers == null) {
/*  44: 69 */       throw new NullPointerException("handlers");
/*  45:    */     }
/*  46: 72 */     int nHandlers = 0;
/*  47: 73 */     ChannelPipeline p = pipeline();
/*  48: 74 */     for (ChannelHandler h : handlers)
/*  49:    */     {
/*  50: 75 */       if (h == null) {
/*  51:    */         break;
/*  52:    */       }
/*  53: 78 */       nHandlers++;
/*  54: 79 */       p.addLast(new ChannelHandler[] { h });
/*  55:    */     }
/*  56: 82 */     if (nHandlers == 0) {
/*  57: 83 */       throw new IllegalArgumentException("handlers is empty.");
/*  58:    */     }
/*  59: 86 */     p.addLast(new ChannelHandler[] { new LastInboundHandler(null) });
/*  60: 87 */     this.loop.register(this);
/*  61:    */   }
/*  62:    */   
/*  63:    */   public ChannelMetadata metadata()
/*  64:    */   {
/*  65: 92 */     return METADATA;
/*  66:    */   }
/*  67:    */   
/*  68:    */   public ChannelConfig config()
/*  69:    */   {
/*  70: 97 */     return this.config;
/*  71:    */   }
/*  72:    */   
/*  73:    */   public boolean isOpen()
/*  74:    */   {
/*  75:102 */     return this.state < 2;
/*  76:    */   }
/*  77:    */   
/*  78:    */   public boolean isActive()
/*  79:    */   {
/*  80:107 */     return this.state == 1;
/*  81:    */   }
/*  82:    */   
/*  83:    */   public Queue<Object> inboundMessages()
/*  84:    */   {
/*  85:114 */     return this.inboundMessages;
/*  86:    */   }
/*  87:    */   
/*  88:    */   @Deprecated
/*  89:    */   public Queue<Object> lastInboundBuffer()
/*  90:    */   {
/*  91:122 */     return inboundMessages();
/*  92:    */   }
/*  93:    */   
/*  94:    */   public Queue<Object> outboundMessages()
/*  95:    */   {
/*  96:129 */     return this.outboundMessages;
/*  97:    */   }
/*  98:    */   
/*  99:    */   @Deprecated
/* 100:    */   public Queue<Object> lastOutboundBuffer()
/* 101:    */   {
/* 102:137 */     return outboundMessages();
/* 103:    */   }
/* 104:    */   
/* 105:    */   public Object readInbound()
/* 106:    */   {
/* 107:144 */     return this.inboundMessages.poll();
/* 108:    */   }
/* 109:    */   
/* 110:    */   public Object readOutbound()
/* 111:    */   {
/* 112:151 */     return this.outboundMessages.poll();
/* 113:    */   }
/* 114:    */   
/* 115:    */   public boolean writeInbound(Object... msgs)
/* 116:    */   {
/* 117:162 */     ensureOpen();
/* 118:163 */     if (msgs.length == 0) {
/* 119:164 */       return !this.inboundMessages.isEmpty();
/* 120:    */     }
/* 121:167 */     ChannelPipeline p = pipeline();
/* 122:168 */     for (Object m : msgs) {
/* 123:169 */       p.fireChannelRead(m);
/* 124:    */     }
/* 125:171 */     p.fireChannelReadComplete();
/* 126:172 */     runPendingTasks();
/* 127:173 */     checkException();
/* 128:174 */     return !this.inboundMessages.isEmpty();
/* 129:    */   }
/* 130:    */   
/* 131:    */   public boolean writeOutbound(Object... msgs)
/* 132:    */   {
/* 133:184 */     ensureOpen();
/* 134:185 */     if (msgs.length == 0) {
/* 135:186 */       return !this.outboundMessages.isEmpty();
/* 136:    */     }
/* 137:189 */     RecyclableArrayList futures = RecyclableArrayList.newInstance(msgs.length);
/* 138:    */     try
/* 139:    */     {
/* 140:191 */       for (Object m : msgs)
/* 141:    */       {
/* 142:192 */         if (m == null) {
/* 143:    */           break;
/* 144:    */         }
/* 145:195 */         futures.add(write(m));
/* 146:    */       }
/* 147:198 */       flush();
/* 148:    */       
/* 149:200 */       int size = futures.size();
/* 150:201 */       for (int i = 0; i < size; i++)
/* 151:    */       {
/* 152:202 */         ChannelFuture future = (ChannelFuture)futures.get(i);
/* 153:203 */         assert (future.isDone());
/* 154:204 */         if (future.cause() != null) {
/* 155:205 */           recordException(future.cause());
/* 156:    */         }
/* 157:    */       }
/* 158:209 */       runPendingTasks();
/* 159:210 */       checkException();
/* 160:211 */       return !this.outboundMessages.isEmpty() ? 1 : 0;
/* 161:    */     }
/* 162:    */     finally
/* 163:    */     {
/* 164:213 */       futures.recycle();
/* 165:    */     }
/* 166:    */   }
/* 167:    */   
/* 168:    */   public boolean finish()
/* 169:    */   {
/* 170:224 */     close();
/* 171:225 */     runPendingTasks();
/* 172:226 */     checkException();
/* 173:227 */     return (!this.inboundMessages.isEmpty()) || (!this.outboundMessages.isEmpty());
/* 174:    */   }
/* 175:    */   
/* 176:    */   public void runPendingTasks()
/* 177:    */   {
/* 178:    */     try
/* 179:    */     {
/* 180:235 */       this.loop.runTasks();
/* 181:    */     }
/* 182:    */     catch (Exception e)
/* 183:    */     {
/* 184:237 */       recordException(e);
/* 185:    */     }
/* 186:    */   }
/* 187:    */   
/* 188:    */   private void recordException(Throwable cause)
/* 189:    */   {
/* 190:242 */     if (this.lastException == null) {
/* 191:243 */       this.lastException = cause;
/* 192:    */     } else {
/* 193:245 */       logger.warn("More than one exception was raised. Will report only the first one and log others.", cause);
/* 194:    */     }
/* 195:    */   }
/* 196:    */   
/* 197:    */   public void checkException()
/* 198:    */   {
/* 199:255 */     Throwable t = this.lastException;
/* 200:256 */     if (t == null) {
/* 201:257 */       return;
/* 202:    */     }
/* 203:260 */     this.lastException = null;
/* 204:    */     
/* 205:262 */     PlatformDependent.throwException(t);
/* 206:    */   }
/* 207:    */   
/* 208:    */   protected final void ensureOpen()
/* 209:    */   {
/* 210:269 */     if (!isOpen())
/* 211:    */     {
/* 212:270 */       recordException(new ClosedChannelException());
/* 213:271 */       checkException();
/* 214:    */     }
/* 215:    */   }
/* 216:    */   
/* 217:    */   protected boolean isCompatible(EventLoop loop)
/* 218:    */   {
/* 219:277 */     return loop instanceof EmbeddedEventLoop;
/* 220:    */   }
/* 221:    */   
/* 222:    */   protected SocketAddress localAddress0()
/* 223:    */   {
/* 224:282 */     return isActive() ? this.localAddress : null;
/* 225:    */   }
/* 226:    */   
/* 227:    */   protected SocketAddress remoteAddress0()
/* 228:    */   {
/* 229:287 */     return isActive() ? this.remoteAddress : null;
/* 230:    */   }
/* 231:    */   
/* 232:    */   protected void doRegister()
/* 233:    */     throws Exception
/* 234:    */   {
/* 235:292 */     this.state = 1;
/* 236:    */   }
/* 237:    */   
/* 238:    */   protected void doBind(SocketAddress localAddress)
/* 239:    */     throws Exception
/* 240:    */   {}
/* 241:    */   
/* 242:    */   protected void doDisconnect()
/* 243:    */     throws Exception
/* 244:    */   {
/* 245:302 */     doClose();
/* 246:    */   }
/* 247:    */   
/* 248:    */   protected void doClose()
/* 249:    */     throws Exception
/* 250:    */   {
/* 251:307 */     this.state = 2;
/* 252:    */   }
/* 253:    */   
/* 254:    */   protected void doBeginRead()
/* 255:    */     throws Exception
/* 256:    */   {}
/* 257:    */   
/* 258:    */   protected AbstractChannel.AbstractUnsafe newUnsafe()
/* 259:    */   {
/* 260:317 */     return new DefaultUnsafe(null);
/* 261:    */   }
/* 262:    */   
/* 263:    */   protected void doWrite(ChannelOutboundBuffer in)
/* 264:    */     throws Exception
/* 265:    */   {
/* 266:    */     for (;;)
/* 267:    */     {
/* 268:323 */       Object msg = in.current();
/* 269:324 */       if (msg == null) {
/* 270:    */         break;
/* 271:    */       }
/* 272:328 */       ReferenceCountUtil.retain(msg);
/* 273:329 */       this.outboundMessages.add(msg);
/* 274:330 */       in.remove();
/* 275:    */     }
/* 276:    */   }
/* 277:    */   
/* 278:    */   private class DefaultUnsafe
/* 279:    */     extends AbstractChannel.AbstractUnsafe
/* 280:    */   {
/* 281:    */     private DefaultUnsafe()
/* 282:    */     {
/* 283:334 */       super();
/* 284:    */     }
/* 285:    */     
/* 286:    */     public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
/* 287:    */     {
/* 288:337 */       safeSetSuccess(promise);
/* 289:    */     }
/* 290:    */   }
/* 291:    */   
/* 292:    */   private final class LastInboundHandler
/* 293:    */     extends ChannelInboundHandlerAdapter
/* 294:    */   {
/* 295:    */     private LastInboundHandler() {}
/* 296:    */     
/* 297:    */     public void channelRead(ChannelHandlerContext ctx, Object msg)
/* 298:    */       throws Exception
/* 299:    */     {
/* 300:344 */       EmbeddedChannel.this.inboundMessages.add(msg);
/* 301:    */     }
/* 302:    */     
/* 303:    */     public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
/* 304:    */       throws Exception
/* 305:    */     {
/* 306:349 */       EmbeddedChannel.this.recordException(cause);
/* 307:    */     }
/* 308:    */   }
/* 309:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.embedded.EmbeddedChannel
 * JD-Core Version:    0.7.0.1
 */