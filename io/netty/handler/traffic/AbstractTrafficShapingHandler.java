/*   1:    */ package io.netty.handler.traffic;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufHolder;
/*   5:    */ import io.netty.channel.ChannelDuplexHandler;
/*   6:    */ import io.netty.channel.ChannelHandlerContext;
/*   7:    */ import io.netty.channel.ChannelPromise;
/*   8:    */ import io.netty.util.Attribute;
/*   9:    */ import io.netty.util.AttributeKey;
/*  10:    */ import io.netty.util.concurrent.EventExecutor;
/*  11:    */ import java.util.concurrent.TimeUnit;
/*  12:    */ 
/*  13:    */ public abstract class AbstractTrafficShapingHandler
/*  14:    */   extends ChannelDuplexHandler
/*  15:    */ {
/*  16:    */   public static final long DEFAULT_CHECK_INTERVAL = 1000L;
/*  17:    */   private static final long MINIMAL_WAIT = 10L;
/*  18:    */   protected TrafficCounter trafficCounter;
/*  19:    */   private long writeLimit;
/*  20:    */   private long readLimit;
/*  21: 74 */   protected long checkInterval = 1000L;
/*  22: 76 */   private static final AttributeKey<Boolean> READ_SUSPENDED = AttributeKey.valueOf(AbstractTrafficShapingHandler.class.getName() + ".READ_SUSPENDED");
/*  23: 78 */   private static final AttributeKey<Runnable> REOPEN_TASK = AttributeKey.valueOf(AbstractTrafficShapingHandler.class.getName() + ".REOPEN_TASK");
/*  24:    */   
/*  25:    */   void setTrafficCounter(TrafficCounter newTrafficCounter)
/*  26:    */   {
/*  27: 86 */     this.trafficCounter = newTrafficCounter;
/*  28:    */   }
/*  29:    */   
/*  30:    */   protected AbstractTrafficShapingHandler(long writeLimit, long readLimit, long checkInterval)
/*  31:    */   {
/*  32:100 */     this.writeLimit = writeLimit;
/*  33:101 */     this.readLimit = readLimit;
/*  34:102 */     this.checkInterval = checkInterval;
/*  35:    */   }
/*  36:    */   
/*  37:    */   protected AbstractTrafficShapingHandler(long writeLimit, long readLimit)
/*  38:    */   {
/*  39:114 */     this(writeLimit, readLimit, 1000L);
/*  40:    */   }
/*  41:    */   
/*  42:    */   protected AbstractTrafficShapingHandler()
/*  43:    */   {
/*  44:121 */     this(0L, 0L, 1000L);
/*  45:    */   }
/*  46:    */   
/*  47:    */   protected AbstractTrafficShapingHandler(long checkInterval)
/*  48:    */   {
/*  49:132 */     this(0L, 0L, checkInterval);
/*  50:    */   }
/*  51:    */   
/*  52:    */   public void configure(long newWriteLimit, long newReadLimit, long newCheckInterval)
/*  53:    */   {
/*  54:144 */     configure(newWriteLimit, newReadLimit);
/*  55:145 */     configure(newCheckInterval);
/*  56:    */   }
/*  57:    */   
/*  58:    */   public void configure(long newWriteLimit, long newReadLimit)
/*  59:    */   {
/*  60:155 */     this.writeLimit = newWriteLimit;
/*  61:156 */     this.readLimit = newReadLimit;
/*  62:157 */     if (this.trafficCounter != null) {
/*  63:158 */       this.trafficCounter.resetAccounting(System.currentTimeMillis() + 1L);
/*  64:    */     }
/*  65:    */   }
/*  66:    */   
/*  67:    */   public void configure(long newCheckInterval)
/*  68:    */   {
/*  69:168 */     this.checkInterval = newCheckInterval;
/*  70:169 */     if (this.trafficCounter != null) {
/*  71:170 */       this.trafficCounter.configure(this.checkInterval);
/*  72:    */     }
/*  73:    */   }
/*  74:    */   
/*  75:    */   protected void doAccounting(TrafficCounter counter) {}
/*  76:    */   
/*  77:    */   private static final class ReopenReadTimerTask
/*  78:    */     implements Runnable
/*  79:    */   {
/*  80:    */     final ChannelHandlerContext ctx;
/*  81:    */     
/*  82:    */     ReopenReadTimerTask(ChannelHandlerContext ctx)
/*  83:    */     {
/*  84:192 */       this.ctx = ctx;
/*  85:    */     }
/*  86:    */     
/*  87:    */     public void run()
/*  88:    */     {
/*  89:197 */       this.ctx.attr(AbstractTrafficShapingHandler.READ_SUSPENDED).set(Boolean.valueOf(false));
/*  90:198 */       this.ctx.read();
/*  91:    */     }
/*  92:    */   }
/*  93:    */   
/*  94:    */   private static long getTimeToWait(long limit, long bytes, long lastTime, long curtime)
/*  95:    */   {
/*  96:206 */     long interval = curtime - lastTime;
/*  97:207 */     if (interval <= 0L) {
/*  98:209 */       return 0L;
/*  99:    */     }
/* 100:211 */     return (bytes * 1000L / limit - interval) / 10L * 10L;
/* 101:    */   }
/* 102:    */   
/* 103:    */   public void channelRead(ChannelHandlerContext ctx, Object msg)
/* 104:    */     throws Exception
/* 105:    */   {
/* 106:216 */     long size = calculateSize(msg);
/* 107:217 */     long curtime = System.currentTimeMillis();
/* 108:219 */     if (this.trafficCounter != null)
/* 109:    */     {
/* 110:220 */       this.trafficCounter.bytesRecvFlowControl(size);
/* 111:221 */       if (this.readLimit == 0L)
/* 112:    */       {
/* 113:223 */         ctx.fireChannelRead(msg);
/* 114:    */         
/* 115:225 */         return;
/* 116:    */       }
/* 117:229 */       long wait = getTimeToWait(this.readLimit, this.trafficCounter.currentReadBytes(), this.trafficCounter.lastTime(), curtime);
/* 118:232 */       if (wait >= 10L) {
/* 119:235 */         if (!isSuspended(ctx))
/* 120:    */         {
/* 121:236 */           ctx.attr(READ_SUSPENDED).set(Boolean.valueOf(true));
/* 122:    */           
/* 123:    */ 
/* 124:    */ 
/* 125:240 */           Attribute<Runnable> attr = ctx.attr(REOPEN_TASK);
/* 126:241 */           Runnable reopenTask = (Runnable)attr.get();
/* 127:242 */           if (reopenTask == null)
/* 128:    */           {
/* 129:243 */             reopenTask = new ReopenReadTimerTask(ctx);
/* 130:244 */             attr.set(reopenTask);
/* 131:    */           }
/* 132:246 */           ctx.executor().schedule(reopenTask, wait, TimeUnit.MILLISECONDS);
/* 133:    */         }
/* 134:    */       }
/* 135:    */     }
/* 136:251 */     ctx.fireChannelRead(msg);
/* 137:    */   }
/* 138:    */   
/* 139:    */   public void read(ChannelHandlerContext ctx)
/* 140:    */   {
/* 141:256 */     if (!isSuspended(ctx)) {
/* 142:257 */       ctx.read();
/* 143:    */     }
/* 144:    */   }
/* 145:    */   
/* 146:    */   private static boolean isSuspended(ChannelHandlerContext ctx)
/* 147:    */   {
/* 148:262 */     Boolean suspended = (Boolean)ctx.attr(READ_SUSPENDED).get();
/* 149:263 */     if ((suspended == null) || (Boolean.FALSE.equals(suspended))) {
/* 150:264 */       return false;
/* 151:    */     }
/* 152:266 */     return true;
/* 153:    */   }
/* 154:    */   
/* 155:    */   public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise)
/* 156:    */     throws Exception
/* 157:    */   {
/* 158:272 */     long curtime = System.currentTimeMillis();
/* 159:273 */     long size = calculateSize(msg);
/* 160:275 */     if ((size > -1L) && (this.trafficCounter != null))
/* 161:    */     {
/* 162:276 */       this.trafficCounter.bytesWriteFlowControl(size);
/* 163:277 */       if (this.writeLimit == 0L)
/* 164:    */       {
/* 165:278 */         ctx.write(msg, promise);
/* 166:279 */         return;
/* 167:    */       }
/* 168:283 */       long wait = getTimeToWait(this.writeLimit, this.trafficCounter.currentWrittenBytes(), this.trafficCounter.lastTime(), curtime);
/* 169:286 */       if (wait >= 10L)
/* 170:    */       {
/* 171:287 */         ctx.executor().schedule(new Runnable()
/* 172:    */         {
/* 173:    */           public void run()
/* 174:    */           {
/* 175:290 */             ctx.write(msg, promise);
/* 176:    */           }
/* 177:290 */         }, wait, TimeUnit.MILLISECONDS);
/* 178:    */         
/* 179:    */ 
/* 180:293 */         return;
/* 181:    */       }
/* 182:    */     }
/* 183:296 */     ctx.write(msg, promise);
/* 184:    */   }
/* 185:    */   
/* 186:    */   public TrafficCounter trafficCounter()
/* 187:    */   {
/* 188:305 */     return this.trafficCounter;
/* 189:    */   }
/* 190:    */   
/* 191:    */   public String toString()
/* 192:    */   {
/* 193:310 */     return "TrafficShaping with Write Limit: " + this.writeLimit + " Read Limit: " + this.readLimit + " and Counter: " + (this.trafficCounter != null ? this.trafficCounter.toString() : "none");
/* 194:    */   }
/* 195:    */   
/* 196:    */   protected long calculateSize(Object msg)
/* 197:    */   {
/* 198:323 */     if ((msg instanceof ByteBuf)) {
/* 199:324 */       return ((ByteBuf)msg).readableBytes();
/* 200:    */     }
/* 201:326 */     if ((msg instanceof ByteBufHolder)) {
/* 202:327 */       return ((ByteBufHolder)msg).content().readableBytes();
/* 203:    */     }
/* 204:329 */     return -1L;
/* 205:    */   }
/* 206:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.traffic.AbstractTrafficShapingHandler
 * JD-Core Version:    0.7.0.1
 */