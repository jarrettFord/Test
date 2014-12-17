/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import io.netty.util.concurrent.AbstractEventExecutorGroup;
/*   4:    */ import io.netty.util.concurrent.DefaultPromise;
/*   5:    */ import io.netty.util.concurrent.EventExecutor;
/*   6:    */ import io.netty.util.concurrent.Future;
/*   7:    */ import io.netty.util.concurrent.FutureListener;
/*   8:    */ import io.netty.util.concurrent.GlobalEventExecutor;
/*   9:    */ import io.netty.util.concurrent.Promise;
/*  10:    */ import io.netty.util.internal.EmptyArrays;
/*  11:    */ import io.netty.util.internal.PlatformDependent;
/*  12:    */ import io.netty.util.internal.ReadOnlyIterator;
/*  13:    */ import java.util.Collections;
/*  14:    */ import java.util.Iterator;
/*  15:    */ import java.util.Queue;
/*  16:    */ import java.util.Set;
/*  17:    */ import java.util.concurrent.ConcurrentLinkedQueue;
/*  18:    */ import java.util.concurrent.Executors;
/*  19:    */ import java.util.concurrent.RejectedExecutionException;
/*  20:    */ import java.util.concurrent.ThreadFactory;
/*  21:    */ import java.util.concurrent.TimeUnit;
/*  22:    */ 
/*  23:    */ public class ThreadPerChannelEventLoopGroup
/*  24:    */   extends AbstractEventExecutorGroup
/*  25:    */   implements EventLoopGroup
/*  26:    */ {
/*  27:    */   private final Object[] childArgs;
/*  28:    */   private final int maxChannels;
/*  29:    */   final ThreadFactory threadFactory;
/*  30: 48 */   final Set<ThreadPerChannelEventLoop> activeChildren = Collections.newSetFromMap(PlatformDependent.newConcurrentHashMap());
/*  31: 50 */   final Queue<ThreadPerChannelEventLoop> idleChildren = new ConcurrentLinkedQueue();
/*  32:    */   private final ChannelException tooManyChannels;
/*  33:    */   private volatile boolean shuttingDown;
/*  34: 54 */   private final Promise<?> terminationFuture = new DefaultPromise(GlobalEventExecutor.INSTANCE);
/*  35: 55 */   private final FutureListener<Object> childTerminationListener = new FutureListener()
/*  36:    */   {
/*  37:    */     public void operationComplete(Future<Object> future)
/*  38:    */       throws Exception
/*  39:    */     {
/*  40: 59 */       if (ThreadPerChannelEventLoopGroup.this.isTerminated()) {
/*  41: 60 */         ThreadPerChannelEventLoopGroup.this.terminationFuture.trySuccess(null);
/*  42:    */       }
/*  43:    */     }
/*  44:    */   };
/*  45:    */   
/*  46:    */   protected ThreadPerChannelEventLoopGroup()
/*  47:    */   {
/*  48: 69 */     this(0);
/*  49:    */   }
/*  50:    */   
/*  51:    */   protected ThreadPerChannelEventLoopGroup(int maxChannels)
/*  52:    */   {
/*  53: 82 */     this(maxChannels, Executors.defaultThreadFactory(), new Object[0]);
/*  54:    */   }
/*  55:    */   
/*  56:    */   protected ThreadPerChannelEventLoopGroup(int maxChannels, ThreadFactory threadFactory, Object... args)
/*  57:    */   {
/*  58: 98 */     if (maxChannels < 0) {
/*  59: 99 */       throw new IllegalArgumentException(String.format("maxChannels: %d (expected: >= 0)", new Object[] { Integer.valueOf(maxChannels) }));
/*  60:    */     }
/*  61:102 */     if (threadFactory == null) {
/*  62:103 */       throw new NullPointerException("threadFactory");
/*  63:    */     }
/*  64:106 */     if (args == null) {
/*  65:107 */       this.childArgs = EmptyArrays.EMPTY_OBJECTS;
/*  66:    */     } else {
/*  67:109 */       this.childArgs = ((Object[])args.clone());
/*  68:    */     }
/*  69:112 */     this.maxChannels = maxChannels;
/*  70:113 */     this.threadFactory = threadFactory;
/*  71:    */     
/*  72:115 */     this.tooManyChannels = new ChannelException("too many channels (max: " + maxChannels + ')');
/*  73:116 */     this.tooManyChannels.setStackTrace(EmptyArrays.EMPTY_STACK_TRACE);
/*  74:    */   }
/*  75:    */   
/*  76:    */   protected ThreadPerChannelEventLoop newChild(Object... args)
/*  77:    */     throws Exception
/*  78:    */   {
/*  79:124 */     return new ThreadPerChannelEventLoop(this);
/*  80:    */   }
/*  81:    */   
/*  82:    */   public Iterator<EventExecutor> iterator()
/*  83:    */   {
/*  84:129 */     return new ReadOnlyIterator(this.activeChildren.iterator());
/*  85:    */   }
/*  86:    */   
/*  87:    */   public EventLoop next()
/*  88:    */   {
/*  89:134 */     throw new UnsupportedOperationException();
/*  90:    */   }
/*  91:    */   
/*  92:    */   public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit)
/*  93:    */   {
/*  94:139 */     this.shuttingDown = true;
/*  95:141 */     for (EventLoop l : this.activeChildren) {
/*  96:142 */       l.shutdownGracefully(quietPeriod, timeout, unit);
/*  97:    */     }
/*  98:144 */     for (EventLoop l : this.idleChildren) {
/*  99:145 */       l.shutdownGracefully(quietPeriod, timeout, unit);
/* 100:    */     }
/* 101:149 */     if (isTerminated()) {
/* 102:150 */       this.terminationFuture.trySuccess(null);
/* 103:    */     }
/* 104:153 */     return terminationFuture();
/* 105:    */   }
/* 106:    */   
/* 107:    */   public Future<?> terminationFuture()
/* 108:    */   {
/* 109:158 */     return this.terminationFuture;
/* 110:    */   }
/* 111:    */   
/* 112:    */   @Deprecated
/* 113:    */   public void shutdown()
/* 114:    */   {
/* 115:164 */     this.shuttingDown = true;
/* 116:166 */     for (EventLoop l : this.activeChildren) {
/* 117:167 */       l.shutdown();
/* 118:    */     }
/* 119:169 */     for (EventLoop l : this.idleChildren) {
/* 120:170 */       l.shutdown();
/* 121:    */     }
/* 122:174 */     if (isTerminated()) {
/* 123:175 */       this.terminationFuture.trySuccess(null);
/* 124:    */     }
/* 125:    */   }
/* 126:    */   
/* 127:    */   public boolean isShuttingDown()
/* 128:    */   {
/* 129:181 */     for (EventLoop l : this.activeChildren) {
/* 130:182 */       if (!l.isShuttingDown()) {
/* 131:183 */         return false;
/* 132:    */       }
/* 133:    */     }
/* 134:186 */     for (EventLoop l : this.idleChildren) {
/* 135:187 */       if (!l.isShuttingDown()) {
/* 136:188 */         return false;
/* 137:    */       }
/* 138:    */     }
/* 139:191 */     return true;
/* 140:    */   }
/* 141:    */   
/* 142:    */   public boolean isShutdown()
/* 143:    */   {
/* 144:196 */     for (EventLoop l : this.activeChildren) {
/* 145:197 */       if (!l.isShutdown()) {
/* 146:198 */         return false;
/* 147:    */       }
/* 148:    */     }
/* 149:201 */     for (EventLoop l : this.idleChildren) {
/* 150:202 */       if (!l.isShutdown()) {
/* 151:203 */         return false;
/* 152:    */       }
/* 153:    */     }
/* 154:206 */     return true;
/* 155:    */   }
/* 156:    */   
/* 157:    */   public boolean isTerminated()
/* 158:    */   {
/* 159:211 */     for (EventLoop l : this.activeChildren) {
/* 160:212 */       if (!l.isTerminated()) {
/* 161:213 */         return false;
/* 162:    */       }
/* 163:    */     }
/* 164:216 */     for (EventLoop l : this.idleChildren) {
/* 165:217 */       if (!l.isTerminated()) {
/* 166:218 */         return false;
/* 167:    */       }
/* 168:    */     }
/* 169:221 */     return true;
/* 170:    */   }
/* 171:    */   
/* 172:    */   public boolean awaitTermination(long timeout, TimeUnit unit)
/* 173:    */     throws InterruptedException
/* 174:    */   {
/* 175:227 */     long deadline = System.nanoTime() + unit.toNanos(timeout);
/* 176:228 */     for (EventLoop l : this.activeChildren) {
/* 177:    */       for (;;)
/* 178:    */       {
/* 179:230 */         long timeLeft = deadline - System.nanoTime();
/* 180:231 */         if (timeLeft <= 0L) {
/* 181:232 */           return isTerminated();
/* 182:    */         }
/* 183:234 */         if (l.awaitTermination(timeLeft, TimeUnit.NANOSECONDS)) {
/* 184:    */           break;
/* 185:    */         }
/* 186:    */       }
/* 187:    */     }
/* 188:239 */     for (EventLoop l : this.idleChildren) {
/* 189:    */       for (;;)
/* 190:    */       {
/* 191:241 */         long timeLeft = deadline - System.nanoTime();
/* 192:242 */         if (timeLeft <= 0L) {
/* 193:243 */           return isTerminated();
/* 194:    */         }
/* 195:245 */         if (l.awaitTermination(timeLeft, TimeUnit.NANOSECONDS)) {
/* 196:    */           break;
/* 197:    */         }
/* 198:    */       }
/* 199:    */     }
/* 200:250 */     return isTerminated();
/* 201:    */   }
/* 202:    */   
/* 203:    */   public ChannelFuture register(Channel channel)
/* 204:    */   {
/* 205:255 */     if (channel == null) {
/* 206:256 */       throw new NullPointerException("channel");
/* 207:    */     }
/* 208:    */     try
/* 209:    */     {
/* 210:259 */       EventLoop l = nextChild();
/* 211:260 */       return l.register(channel, new DefaultChannelPromise(channel, l));
/* 212:    */     }
/* 213:    */     catch (Throwable t)
/* 214:    */     {
/* 215:262 */       return new FailedChannelFuture(channel, GlobalEventExecutor.INSTANCE, t);
/* 216:    */     }
/* 217:    */   }
/* 218:    */   
/* 219:    */   public ChannelFuture register(Channel channel, ChannelPromise promise)
/* 220:    */   {
/* 221:268 */     if (channel == null) {
/* 222:269 */       throw new NullPointerException("channel");
/* 223:    */     }
/* 224:    */     try
/* 225:    */     {
/* 226:272 */       return nextChild().register(channel, promise);
/* 227:    */     }
/* 228:    */     catch (Throwable t)
/* 229:    */     {
/* 230:274 */       promise.setFailure(t);
/* 231:    */     }
/* 232:275 */     return promise;
/* 233:    */   }
/* 234:    */   
/* 235:    */   private EventLoop nextChild()
/* 236:    */     throws Exception
/* 237:    */   {
/* 238:280 */     if (this.shuttingDown) {
/* 239:281 */       throw new RejectedExecutionException("shutting down");
/* 240:    */     }
/* 241:284 */     ThreadPerChannelEventLoop loop = (ThreadPerChannelEventLoop)this.idleChildren.poll();
/* 242:285 */     if (loop == null)
/* 243:    */     {
/* 244:286 */       if ((this.maxChannels > 0) && (this.activeChildren.size() >= this.maxChannels)) {
/* 245:287 */         throw this.tooManyChannels;
/* 246:    */       }
/* 247:289 */       loop = newChild(this.childArgs);
/* 248:290 */       loop.terminationFuture().addListener(this.childTerminationListener);
/* 249:    */     }
/* 250:292 */     this.activeChildren.add(loop);
/* 251:293 */     return loop;
/* 252:    */   }
/* 253:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.ThreadPerChannelEventLoopGroup
 * JD-Core Version:    0.7.0.1
 */