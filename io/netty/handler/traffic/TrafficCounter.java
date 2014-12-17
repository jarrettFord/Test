/*   1:    */ package io.netty.handler.traffic;
/*   2:    */ 
/*   3:    */ import java.util.concurrent.ScheduledExecutorService;
/*   4:    */ import java.util.concurrent.ScheduledFuture;
/*   5:    */ import java.util.concurrent.TimeUnit;
/*   6:    */ import java.util.concurrent.atomic.AtomicBoolean;
/*   7:    */ import java.util.concurrent.atomic.AtomicLong;
/*   8:    */ 
/*   9:    */ public class TrafficCounter
/*  10:    */ {
/*  11: 39 */   private final AtomicLong currentWrittenBytes = new AtomicLong();
/*  12: 44 */   private final AtomicLong currentReadBytes = new AtomicLong();
/*  13: 49 */   private final AtomicLong cumulativeWrittenBytes = new AtomicLong();
/*  14: 54 */   private final AtomicLong cumulativeReadBytes = new AtomicLong();
/*  15:    */   private long lastCumulativeTime;
/*  16:    */   private long lastWriteThroughput;
/*  17:    */   private long lastReadThroughput;
/*  18: 74 */   private final AtomicLong lastTime = new AtomicLong();
/*  19:    */   private long lastWrittenBytes;
/*  20:    */   private long lastReadBytes;
/*  21: 89 */   final AtomicLong checkInterval = new AtomicLong(1000L);
/*  22:    */   final String name;
/*  23:    */   private final AbstractTrafficShapingHandler trafficShapingHandler;
/*  24:    */   private final ScheduledExecutorService executor;
/*  25:    */   private Runnable monitor;
/*  26:    */   private volatile ScheduledFuture<?> scheduledFuture;
/*  27:120 */   final AtomicBoolean monitorActive = new AtomicBoolean();
/*  28:    */   
/*  29:    */   private static class TrafficMonitoringTask
/*  30:    */     implements Runnable
/*  31:    */   {
/*  32:    */     private final AbstractTrafficShapingHandler trafficShapingHandler1;
/*  33:    */     private final TrafficCounter counter;
/*  34:    */     
/*  35:    */     protected TrafficMonitoringTask(AbstractTrafficShapingHandler trafficShapingHandler, TrafficCounter counter)
/*  36:    */     {
/*  37:144 */       this.trafficShapingHandler1 = trafficShapingHandler;
/*  38:145 */       this.counter = counter;
/*  39:    */     }
/*  40:    */     
/*  41:    */     public void run()
/*  42:    */     {
/*  43:150 */       if (!this.counter.monitorActive.get()) {
/*  44:151 */         return;
/*  45:    */       }
/*  46:153 */       long endTime = System.currentTimeMillis();
/*  47:154 */       this.counter.resetAccounting(endTime);
/*  48:155 */       if (this.trafficShapingHandler1 != null) {
/*  49:156 */         this.trafficShapingHandler1.doAccounting(this.counter);
/*  50:    */       }
/*  51:158 */       this.counter.scheduledFuture = this.counter.executor.schedule(this, this.counter.checkInterval.get(), TimeUnit.MILLISECONDS);
/*  52:    */     }
/*  53:    */   }
/*  54:    */   
/*  55:    */   public void start()
/*  56:    */   {
/*  57:167 */     synchronized (this.lastTime)
/*  58:    */     {
/*  59:168 */       if (this.monitorActive.get()) {
/*  60:169 */         return;
/*  61:    */       }
/*  62:171 */       this.lastTime.set(System.currentTimeMillis());
/*  63:172 */       if (this.checkInterval.get() > 0L)
/*  64:    */       {
/*  65:173 */         this.monitorActive.set(true);
/*  66:174 */         this.monitor = new TrafficMonitoringTask(this.trafficShapingHandler, this);
/*  67:175 */         this.scheduledFuture = this.executor.schedule(this.monitor, this.checkInterval.get(), TimeUnit.MILLISECONDS);
/*  68:    */       }
/*  69:    */     }
/*  70:    */   }
/*  71:    */   
/*  72:    */   public void stop()
/*  73:    */   {
/*  74:185 */     synchronized (this.lastTime)
/*  75:    */     {
/*  76:186 */       if (!this.monitorActive.get()) {
/*  77:187 */         return;
/*  78:    */       }
/*  79:189 */       this.monitorActive.set(false);
/*  80:190 */       resetAccounting(System.currentTimeMillis());
/*  81:191 */       if (this.trafficShapingHandler != null) {
/*  82:192 */         this.trafficShapingHandler.doAccounting(this);
/*  83:    */       }
/*  84:194 */       if (this.scheduledFuture != null) {
/*  85:195 */         this.scheduledFuture.cancel(true);
/*  86:    */       }
/*  87:    */     }
/*  88:    */   }
/*  89:    */   
/*  90:    */   void resetAccounting(long newLastTime)
/*  91:    */   {
/*  92:206 */     synchronized (this.lastTime)
/*  93:    */     {
/*  94:207 */       long interval = newLastTime - this.lastTime.getAndSet(newLastTime);
/*  95:208 */       if (interval == 0L) {
/*  96:210 */         return;
/*  97:    */       }
/*  98:212 */       this.lastReadBytes = this.currentReadBytes.getAndSet(0L);
/*  99:213 */       this.lastWrittenBytes = this.currentWrittenBytes.getAndSet(0L);
/* 100:214 */       this.lastReadThroughput = (this.lastReadBytes / interval * 1000L);
/* 101:    */       
/* 102:216 */       this.lastWriteThroughput = (this.lastWrittenBytes / interval * 1000L);
/* 103:    */     }
/* 104:    */   }
/* 105:    */   
/* 106:    */   public TrafficCounter(AbstractTrafficShapingHandler trafficShapingHandler, ScheduledExecutorService executor, String name, long checkInterval)
/* 107:    */   {
/* 108:231 */     this.trafficShapingHandler = trafficShapingHandler;
/* 109:232 */     this.executor = executor;
/* 110:233 */     this.name = name;
/* 111:234 */     this.lastCumulativeTime = System.currentTimeMillis();
/* 112:235 */     configure(checkInterval);
/* 113:    */   }
/* 114:    */   
/* 115:    */   public void configure(long newcheckInterval)
/* 116:    */   {
/* 117:244 */     long newInterval = newcheckInterval / 10L * 10L;
/* 118:245 */     if (this.checkInterval.get() != newInterval)
/* 119:    */     {
/* 120:246 */       this.checkInterval.set(newInterval);
/* 121:247 */       if (newInterval <= 0L)
/* 122:    */       {
/* 123:248 */         stop();
/* 124:    */         
/* 125:250 */         this.lastTime.set(System.currentTimeMillis());
/* 126:    */       }
/* 127:    */       else
/* 128:    */       {
/* 129:253 */         start();
/* 130:    */       }
/* 131:    */     }
/* 132:    */   }
/* 133:    */   
/* 134:    */   void bytesRecvFlowControl(long recv)
/* 135:    */   {
/* 136:265 */     this.currentReadBytes.addAndGet(recv);
/* 137:266 */     this.cumulativeReadBytes.addAndGet(recv);
/* 138:    */   }
/* 139:    */   
/* 140:    */   void bytesWriteFlowControl(long write)
/* 141:    */   {
/* 142:276 */     this.currentWrittenBytes.addAndGet(write);
/* 143:277 */     this.cumulativeWrittenBytes.addAndGet(write);
/* 144:    */   }
/* 145:    */   
/* 146:    */   public long checkInterval()
/* 147:    */   {
/* 148:286 */     return this.checkInterval.get();
/* 149:    */   }
/* 150:    */   
/* 151:    */   public long lastReadThroughput()
/* 152:    */   {
/* 153:294 */     return this.lastReadThroughput;
/* 154:    */   }
/* 155:    */   
/* 156:    */   public long lastWriteThroughput()
/* 157:    */   {
/* 158:302 */     return this.lastWriteThroughput;
/* 159:    */   }
/* 160:    */   
/* 161:    */   public long lastReadBytes()
/* 162:    */   {
/* 163:310 */     return this.lastReadBytes;
/* 164:    */   }
/* 165:    */   
/* 166:    */   public long lastWrittenBytes()
/* 167:    */   {
/* 168:318 */     return this.lastWrittenBytes;
/* 169:    */   }
/* 170:    */   
/* 171:    */   public long currentReadBytes()
/* 172:    */   {
/* 173:326 */     return this.currentReadBytes.get();
/* 174:    */   }
/* 175:    */   
/* 176:    */   public long currentWrittenBytes()
/* 177:    */   {
/* 178:334 */     return this.currentWrittenBytes.get();
/* 179:    */   }
/* 180:    */   
/* 181:    */   public long lastTime()
/* 182:    */   {
/* 183:341 */     return this.lastTime.get();
/* 184:    */   }
/* 185:    */   
/* 186:    */   public long cumulativeWrittenBytes()
/* 187:    */   {
/* 188:348 */     return this.cumulativeWrittenBytes.get();
/* 189:    */   }
/* 190:    */   
/* 191:    */   public long cumulativeReadBytes()
/* 192:    */   {
/* 193:355 */     return this.cumulativeReadBytes.get();
/* 194:    */   }
/* 195:    */   
/* 196:    */   public long lastCumulativeTime()
/* 197:    */   {
/* 198:363 */     return this.lastCumulativeTime;
/* 199:    */   }
/* 200:    */   
/* 201:    */   public void resetCumulativeTime()
/* 202:    */   {
/* 203:370 */     this.lastCumulativeTime = System.currentTimeMillis();
/* 204:371 */     this.cumulativeReadBytes.set(0L);
/* 205:372 */     this.cumulativeWrittenBytes.set(0L);
/* 206:    */   }
/* 207:    */   
/* 208:    */   public String name()
/* 209:    */   {
/* 210:379 */     return this.name;
/* 211:    */   }
/* 212:    */   
/* 213:    */   public String toString()
/* 214:    */   {
/* 215:387 */     return "Monitor " + this.name + " Current Speed Read: " + (this.lastReadThroughput >> 10) + " KB/s, Write: " + (this.lastWriteThroughput >> 10) + " KB/s Current Read: " + (this.currentReadBytes.get() >> 10) + " KB Current Write: " + (this.currentWrittenBytes.get() >> 10) + " KB";
/* 216:    */   }
/* 217:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.traffic.TrafficCounter
 * JD-Core Version:    0.7.0.1
 */