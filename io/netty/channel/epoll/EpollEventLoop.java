/*   1:    */ package io.netty.channel.epoll;
/*   2:    */ 
/*   3:    */ import io.netty.channel.Channel.Unsafe;
/*   4:    */ import io.netty.channel.EventLoopGroup;
/*   5:    */ import io.netty.channel.SingleThreadEventLoop;
/*   6:    */ import io.netty.util.collection.IntObjectHashMap;
/*   7:    */ import io.netty.util.collection.IntObjectMap;
/*   8:    */ import io.netty.util.collection.IntObjectMap.Entry;
/*   9:    */ import io.netty.util.internal.PlatformDependent;
/*  10:    */ import io.netty.util.internal.logging.InternalLogger;
/*  11:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  12:    */ import java.io.IOException;
/*  13:    */ import java.util.ArrayList;
/*  14:    */ import java.util.Collection;
/*  15:    */ import java.util.Queue;
/*  16:    */ import java.util.concurrent.ThreadFactory;
/*  17:    */ import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
/*  18:    */ 
/*  19:    */ final class EpollEventLoop
/*  20:    */   extends SingleThreadEventLoop
/*  21:    */ {
/*  22:    */   private static final InternalLogger logger;
/*  23:    */   private static final AtomicIntegerFieldUpdater<EpollEventLoop> WAKEN_UP_UPDATER;
/*  24:    */   private final int epollFd;
/*  25:    */   private final int eventFd;
/*  26:    */   
/*  27:    */   static
/*  28:    */   {
/*  29: 39 */     logger = InternalLoggerFactory.getInstance(EpollEventLoop.class);
/*  30:    */     
/*  31:    */ 
/*  32:    */ 
/*  33: 43 */     AtomicIntegerFieldUpdater<EpollEventLoop> updater = PlatformDependent.newAtomicIntegerFieldUpdater(EpollEventLoop.class, "wakenUp");
/*  34: 45 */     if (updater == null) {
/*  35: 46 */       updater = AtomicIntegerFieldUpdater.newUpdater(EpollEventLoop.class, "wakenUp");
/*  36:    */     }
/*  37: 48 */     WAKEN_UP_UPDATER = updater;
/*  38:    */   }
/*  39:    */   
/*  40: 53 */   private final IntObjectMap<AbstractEpollChannel> ids = new IntObjectHashMap();
/*  41:    */   private final long[] events;
/*  42:    */   private int id;
/*  43:    */   private int oldWakenUp;
/*  44:    */   private boolean overflown;
/*  45:    */   private volatile int wakenUp;
/*  46: 62 */   private volatile int ioRatio = 50;
/*  47:    */   
/*  48:    */   EpollEventLoop(EventLoopGroup parent, ThreadFactory threadFactory, int maxEvents)
/*  49:    */   {
/*  50: 65 */     super(parent, threadFactory, false);
/*  51: 66 */     this.events = new long[maxEvents];
/*  52: 67 */     boolean success = false;
/*  53: 68 */     int epollFd = -1;
/*  54: 69 */     int eventFd = -1;
/*  55:    */     try
/*  56:    */     {
/*  57: 71 */       this.epollFd = (epollFd = Native.epollCreate());
/*  58: 72 */       this.eventFd = (eventFd = Native.eventFd());
/*  59: 73 */       Native.epollCtlAdd(epollFd, eventFd, 1, 0);
/*  60: 74 */       success = true;
/*  61: 76 */       if (!success)
/*  62:    */       {
/*  63: 77 */         if (epollFd != -1) {
/*  64:    */           try
/*  65:    */           {
/*  66: 79 */             Native.close(epollFd);
/*  67:    */           }
/*  68:    */           catch (Exception e) {}
/*  69:    */         }
/*  70: 84 */         if (eventFd != -1) {
/*  71:    */           try
/*  72:    */           {
/*  73: 86 */             Native.close(eventFd);
/*  74:    */           }
/*  75:    */           catch (Exception e) {}
/*  76:    */         }
/*  77:    */       }
/*  78:    */       return;
/*  79:    */     }
/*  80:    */     finally
/*  81:    */     {
/*  82: 76 */       if (!success)
/*  83:    */       {
/*  84: 77 */         if (epollFd != -1) {
/*  85:    */           try
/*  86:    */           {
/*  87: 79 */             Native.close(epollFd);
/*  88:    */           }
/*  89:    */           catch (Exception e) {}
/*  90:    */         }
/*  91: 84 */         if (eventFd != -1) {
/*  92:    */           try
/*  93:    */           {
/*  94: 86 */             Native.close(eventFd);
/*  95:    */           }
/*  96:    */           catch (Exception e) {}
/*  97:    */         }
/*  98:    */       }
/*  99:    */     }
/* 100:    */   }
/* 101:    */   
/* 102:    */   private int nextId()
/* 103:    */   {
/* 104: 96 */     int id = this.id;
/* 105: 97 */     if (id == 2147483647)
/* 106:    */     {
/* 107: 98 */       this.overflown = true;
/* 108: 99 */       id = 0;
/* 109:    */     }
/* 110:101 */     if (this.overflown)
/* 111:    */     {
/* 112:105 */       while (this.ids.containsKey(++id)) {}
/* 113:106 */       this.id = id;
/* 114:    */     }
/* 115:    */     else
/* 116:    */     {
/* 117:111 */       this.id = (++id);
/* 118:    */     }
/* 119:113 */     return id;
/* 120:    */   }
/* 121:    */   
/* 122:    */   protected void wakeup(boolean inEventLoop)
/* 123:    */   {
/* 124:118 */     if ((!inEventLoop) && (WAKEN_UP_UPDATER.compareAndSet(this, 0, 1))) {
/* 125:120 */       Native.eventFdWrite(this.eventFd, 1L);
/* 126:    */     }
/* 127:    */   }
/* 128:    */   
/* 129:    */   void add(AbstractEpollChannel ch)
/* 130:    */   {
/* 131:128 */     assert (inEventLoop());
/* 132:129 */     int id = nextId();
/* 133:130 */     Native.epollCtlAdd(this.epollFd, ch.fd, ch.flags, id);
/* 134:131 */     ch.id = id;
/* 135:132 */     this.ids.put(id, ch);
/* 136:    */   }
/* 137:    */   
/* 138:    */   void modify(AbstractEpollChannel ch)
/* 139:    */   {
/* 140:139 */     assert (inEventLoop());
/* 141:140 */     Native.epollCtlMod(this.epollFd, ch.fd, ch.flags, ch.id);
/* 142:    */   }
/* 143:    */   
/* 144:    */   void remove(AbstractEpollChannel ch)
/* 145:    */   {
/* 146:147 */     assert (inEventLoop());
/* 147:148 */     if ((this.ids.remove(ch.id) != null) && (ch.isOpen())) {
/* 148:151 */       Native.epollCtlDel(this.epollFd, ch.fd);
/* 149:    */     }
/* 150:    */   }
/* 151:    */   
/* 152:    */   protected Queue<Runnable> newTaskQueue()
/* 153:    */   {
/* 154:158 */     return PlatformDependent.newMpscQueue();
/* 155:    */   }
/* 156:    */   
/* 157:    */   public int getIoRatio()
/* 158:    */   {
/* 159:165 */     return this.ioRatio;
/* 160:    */   }
/* 161:    */   
/* 162:    */   public void setIoRatio(int ioRatio)
/* 163:    */   {
/* 164:173 */     if ((ioRatio <= 0) || (ioRatio > 100)) {
/* 165:174 */       throw new IllegalArgumentException("ioRatio: " + ioRatio + " (expected: 0 < ioRatio <= 100)");
/* 166:    */     }
/* 167:176 */     this.ioRatio = ioRatio;
/* 168:    */   }
/* 169:    */   
/* 170:    */   private int epollWait()
/* 171:    */   {
/* 172:180 */     int selectCnt = 0;
/* 173:181 */     long currentTimeNanos = System.nanoTime();
/* 174:182 */     long selectDeadLineNanos = currentTimeNanos + delayNanos(currentTimeNanos);
/* 175:    */     for (;;)
/* 176:    */     {
/* 177:184 */       long timeoutMillis = (selectDeadLineNanos - currentTimeNanos + 500000L) / 1000000L;
/* 178:185 */       if (timeoutMillis <= 0L)
/* 179:    */       {
/* 180:186 */         if (selectCnt != 0) {
/* 181:    */           break;
/* 182:    */         }
/* 183:187 */         int ready = Native.epollWait(this.epollFd, this.events, 0);
/* 184:188 */         if (ready > 0) {
/* 185:189 */           return ready;
/* 186:    */         }
/* 187:191 */         break;
/* 188:    */       }
/* 189:195 */       int selectedKeys = Native.epollWait(this.epollFd, this.events, (int)timeoutMillis);
/* 190:196 */       selectCnt++;
/* 191:198 */       if ((selectedKeys != 0) || (this.oldWakenUp == 1) || (this.wakenUp == 1) || (hasTasks())) {
/* 192:202 */         return selectedKeys;
/* 193:    */       }
/* 194:204 */       currentTimeNanos = System.nanoTime();
/* 195:    */     }
/* 196:206 */     return 0;
/* 197:    */   }
/* 198:    */   
/* 199:    */   protected void run()
/* 200:    */   {
/* 201:    */     for (;;)
/* 202:    */     {
/* 203:212 */       this.oldWakenUp = WAKEN_UP_UPDATER.getAndSet(this, 0);
/* 204:    */       try
/* 205:    */       {
/* 206:    */         int ready;
/* 207:    */         int ready;
/* 208:215 */         if (hasTasks())
/* 209:    */         {
/* 210:217 */           ready = Native.epollWait(this.epollFd, this.events, 0);
/* 211:    */         }
/* 212:    */         else
/* 213:    */         {
/* 214:219 */           ready = epollWait();
/* 215:249 */           if (this.wakenUp == 1) {
/* 216:250 */             Native.eventFdWrite(this.eventFd, 1L);
/* 217:    */           }
/* 218:    */         }
/* 219:254 */         int ioRatio = this.ioRatio;
/* 220:255 */         if (ioRatio == 100)
/* 221:    */         {
/* 222:256 */           if (ready > 0) {
/* 223:257 */             processReady(this.events, ready);
/* 224:    */           }
/* 225:259 */           runAllTasks();
/* 226:    */         }
/* 227:    */         else
/* 228:    */         {
/* 229:261 */           long ioStartTime = System.nanoTime();
/* 230:263 */           if (ready > 0) {
/* 231:264 */             processReady(this.events, ready);
/* 232:    */           }
/* 233:267 */           long ioTime = System.nanoTime() - ioStartTime;
/* 234:268 */           runAllTasks(ioTime * (100 - ioRatio) / ioRatio);
/* 235:    */         }
/* 236:271 */         if (isShuttingDown())
/* 237:    */         {
/* 238:272 */           closeAll();
/* 239:273 */           if (confirmShutdown()) {
/* 240:    */             break;
/* 241:    */           }
/* 242:    */         }
/* 243:    */       }
/* 244:    */       catch (Throwable t)
/* 245:    */       {
/* 246:278 */         logger.warn("Unexpected exception in the selector loop.", t);
/* 247:    */         try
/* 248:    */         {
/* 249:283 */           Thread.sleep(1000L);
/* 250:    */         }
/* 251:    */         catch (InterruptedException e) {}
/* 252:    */       }
/* 253:    */     }
/* 254:    */   }
/* 255:    */   
/* 256:    */   private void closeAll()
/* 257:    */   {
/* 258:292 */     Native.epollWait(this.epollFd, this.events, 0);
/* 259:293 */     Collection<AbstractEpollChannel> channels = new ArrayList(this.ids.size());
/* 260:295 */     for (IntObjectMap.Entry<AbstractEpollChannel> entry : this.ids.entries()) {
/* 261:296 */       channels.add(entry.value());
/* 262:    */     }
/* 263:299 */     for (AbstractEpollChannel ch : channels) {
/* 264:300 */       ch.unsafe().close(ch.unsafe().voidPromise());
/* 265:    */     }
/* 266:    */   }
/* 267:    */   
/* 268:    */   private void processReady(long[] events, int ready)
/* 269:    */   {
/* 270:305 */     for (int i = 0; i < ready; i++)
/* 271:    */     {
/* 272:306 */       long ev = events[i];
/* 273:    */       
/* 274:308 */       int id = (int)(ev >> 32);
/* 275:309 */       if (id == 0)
/* 276:    */       {
/* 277:311 */         Native.eventFdRead(this.eventFd);
/* 278:    */       }
/* 279:    */       else
/* 280:    */       {
/* 281:313 */         boolean read = (ev & 1L) != 0L;
/* 282:314 */         boolean write = (ev & 0x2) != 0L;
/* 283:315 */         boolean close = (ev & 0x8) != 0L;
/* 284:    */         
/* 285:317 */         AbstractEpollChannel ch = (AbstractEpollChannel)this.ids.get(id);
/* 286:318 */         if (ch != null)
/* 287:    */         {
/* 288:319 */           AbstractEpollChannel.AbstractEpollUnsafe unsafe = (AbstractEpollChannel.AbstractEpollUnsafe)ch.unsafe();
/* 289:320 */           if ((write) && (ch.isOpen())) {
/* 290:322 */             unsafe.epollOutReady();
/* 291:    */           }
/* 292:324 */           if ((read) && (ch.isOpen())) {
/* 293:326 */             unsafe.epollInReady();
/* 294:    */           }
/* 295:328 */           if ((close) && (ch.isOpen())) {
/* 296:329 */             unsafe.epollRdHupReady();
/* 297:    */           }
/* 298:    */         }
/* 299:    */       }
/* 300:    */     }
/* 301:    */   }
/* 302:    */   
/* 303:    */   protected void cleanup()
/* 304:    */   {
/* 305:    */     try
/* 306:    */     {
/* 307:339 */       Native.close(this.epollFd);
/* 308:    */     }
/* 309:    */     catch (IOException e)
/* 310:    */     {
/* 311:341 */       logger.warn("Failed to close the epoll fd.", e);
/* 312:    */     }
/* 313:    */     try
/* 314:    */     {
/* 315:344 */       Native.close(this.eventFd);
/* 316:    */     }
/* 317:    */     catch (IOException e)
/* 318:    */     {
/* 319:346 */       logger.warn("Failed to close the event fd.", e);
/* 320:    */     }
/* 321:    */   }
/* 322:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.epoll.EpollEventLoop
 * JD-Core Version:    0.7.0.1
 */