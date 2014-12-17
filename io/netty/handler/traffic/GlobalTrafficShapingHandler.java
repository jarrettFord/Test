/*   1:    */ package io.netty.handler.traffic;
/*   2:    */ 
/*   3:    */ import io.netty.channel.ChannelHandler.Sharable;
/*   4:    */ import io.netty.util.concurrent.EventExecutor;
/*   5:    */ import java.util.concurrent.ScheduledExecutorService;
/*   6:    */ 
/*   7:    */ @ChannelHandler.Sharable
/*   8:    */ public class GlobalTrafficShapingHandler
/*   9:    */   extends AbstractTrafficShapingHandler
/*  10:    */ {
/*  11:    */   void createGlobalTrafficCounter(ScheduledExecutorService executor)
/*  12:    */   {
/*  13: 59 */     if (executor == null) {
/*  14: 60 */       throw new NullPointerException("executor");
/*  15:    */     }
/*  16: 62 */     TrafficCounter tc = new TrafficCounter(this, executor, "GlobalTC", this.checkInterval);
/*  17:    */     
/*  18: 64 */     setTrafficCounter(tc);
/*  19: 65 */     tc.start();
/*  20:    */   }
/*  21:    */   
/*  22:    */   public GlobalTrafficShapingHandler(ScheduledExecutorService executor, long writeLimit, long readLimit, long checkInterval)
/*  23:    */   {
/*  24: 83 */     super(writeLimit, readLimit, checkInterval);
/*  25: 84 */     createGlobalTrafficCounter(executor);
/*  26:    */   }
/*  27:    */   
/*  28:    */   public GlobalTrafficShapingHandler(ScheduledExecutorService executor, long writeLimit, long readLimit)
/*  29:    */   {
/*  30: 99 */     super(writeLimit, readLimit);
/*  31:100 */     createGlobalTrafficCounter(executor);
/*  32:    */   }
/*  33:    */   
/*  34:    */   public GlobalTrafficShapingHandler(ScheduledExecutorService executor, long checkInterval)
/*  35:    */   {
/*  36:113 */     super(checkInterval);
/*  37:114 */     createGlobalTrafficCounter(executor);
/*  38:    */   }
/*  39:    */   
/*  40:    */   public GlobalTrafficShapingHandler(EventExecutor executor)
/*  41:    */   {
/*  42:124 */     createGlobalTrafficCounter(executor);
/*  43:    */   }
/*  44:    */   
/*  45:    */   public final void release()
/*  46:    */   {
/*  47:131 */     if (this.trafficCounter != null) {
/*  48:132 */       this.trafficCounter.stop();
/*  49:    */     }
/*  50:    */   }
/*  51:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.traffic.GlobalTrafficShapingHandler
 * JD-Core Version:    0.7.0.1
 */