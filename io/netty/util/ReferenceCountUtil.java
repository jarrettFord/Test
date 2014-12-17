/*   1:    */ package io.netty.util;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.StringUtil;
/*   4:    */ import io.netty.util.internal.logging.InternalLogger;
/*   5:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   6:    */ 
/*   7:    */ public final class ReferenceCountUtil
/*   8:    */ {
/*   9: 27 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(ReferenceCountUtil.class);
/*  10:    */   
/*  11:    */   public static <T> T retain(T msg)
/*  12:    */   {
/*  13: 35 */     if ((msg instanceof ReferenceCounted)) {
/*  14: 36 */       return ((ReferenceCounted)msg).retain();
/*  15:    */     }
/*  16: 38 */     return msg;
/*  17:    */   }
/*  18:    */   
/*  19:    */   public static <T> T retain(T msg, int increment)
/*  20:    */   {
/*  21: 47 */     if ((msg instanceof ReferenceCounted)) {
/*  22: 48 */       return ((ReferenceCounted)msg).retain(increment);
/*  23:    */     }
/*  24: 50 */     return msg;
/*  25:    */   }
/*  26:    */   
/*  27:    */   public static boolean release(Object msg)
/*  28:    */   {
/*  29: 58 */     if ((msg instanceof ReferenceCounted)) {
/*  30: 59 */       return ((ReferenceCounted)msg).release();
/*  31:    */     }
/*  32: 61 */     return false;
/*  33:    */   }
/*  34:    */   
/*  35:    */   public static boolean release(Object msg, int decrement)
/*  36:    */   {
/*  37: 69 */     if ((msg instanceof ReferenceCounted)) {
/*  38: 70 */       return ((ReferenceCounted)msg).release(decrement);
/*  39:    */     }
/*  40: 72 */     return false;
/*  41:    */   }
/*  42:    */   
/*  43:    */   public static <T> T releaseLater(T msg)
/*  44:    */   {
/*  45: 81 */     return releaseLater(msg, 1);
/*  46:    */   }
/*  47:    */   
/*  48:    */   public static <T> T releaseLater(T msg, int decrement)
/*  49:    */   {
/*  50: 90 */     if ((msg instanceof ReferenceCounted)) {
/*  51: 91 */       ThreadDeathWatcher.watch(Thread.currentThread(), new ReleasingTask((ReferenceCounted)msg, decrement));
/*  52:    */     }
/*  53: 93 */     return msg;
/*  54:    */   }
/*  55:    */   
/*  56:    */   private static final class ReleasingTask
/*  57:    */     implements Runnable
/*  58:    */   {
/*  59:    */     private final ReferenceCounted obj;
/*  60:    */     private final int decrement;
/*  61:    */     
/*  62:    */     ReleasingTask(ReferenceCounted obj, int decrement)
/*  63:    */     {
/*  64:105 */       this.obj = obj;
/*  65:106 */       this.decrement = decrement;
/*  66:    */     }
/*  67:    */     
/*  68:    */     public void run()
/*  69:    */     {
/*  70:    */       try
/*  71:    */       {
/*  72:112 */         if (!this.obj.release(this.decrement)) {
/*  73:113 */           ReferenceCountUtil.logger.warn("Non-zero refCnt: {}", this);
/*  74:    */         } else {
/*  75:115 */           ReferenceCountUtil.logger.debug("Released: {}", this);
/*  76:    */         }
/*  77:    */       }
/*  78:    */       catch (Exception ex)
/*  79:    */       {
/*  80:118 */         ReferenceCountUtil.logger.warn("Failed to release an object: {}", this.obj, ex);
/*  81:    */       }
/*  82:    */     }
/*  83:    */     
/*  84:    */     public String toString()
/*  85:    */     {
/*  86:124 */       return StringUtil.simpleClassName(this.obj) + ".release(" + this.decrement + ") refCnt: " + this.obj.refCnt();
/*  87:    */     }
/*  88:    */   }
/*  89:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.ReferenceCountUtil
 * JD-Core Version:    0.7.0.1
 */