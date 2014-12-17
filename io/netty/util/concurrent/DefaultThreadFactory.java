/*   1:    */ package io.netty.util.concurrent;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.StringUtil;
/*   4:    */ import java.util.Locale;
/*   5:    */ import java.util.concurrent.ThreadFactory;
/*   6:    */ import java.util.concurrent.atomic.AtomicInteger;
/*   7:    */ 
/*   8:    */ public class DefaultThreadFactory
/*   9:    */   implements ThreadFactory
/*  10:    */ {
/*  11: 30 */   private static final AtomicInteger poolId = new AtomicInteger();
/*  12: 32 */   private final AtomicInteger nextId = new AtomicInteger();
/*  13:    */   private final String prefix;
/*  14:    */   private final boolean daemon;
/*  15:    */   private final int priority;
/*  16:    */   
/*  17:    */   public DefaultThreadFactory(Class<?> poolType)
/*  18:    */   {
/*  19: 38 */     this(poolType, false, 5);
/*  20:    */   }
/*  21:    */   
/*  22:    */   public DefaultThreadFactory(String poolName)
/*  23:    */   {
/*  24: 42 */     this(poolName, false, 5);
/*  25:    */   }
/*  26:    */   
/*  27:    */   public DefaultThreadFactory(Class<?> poolType, boolean daemon)
/*  28:    */   {
/*  29: 46 */     this(poolType, daemon, 5);
/*  30:    */   }
/*  31:    */   
/*  32:    */   public DefaultThreadFactory(String poolName, boolean daemon)
/*  33:    */   {
/*  34: 50 */     this(poolName, daemon, 5);
/*  35:    */   }
/*  36:    */   
/*  37:    */   public DefaultThreadFactory(Class<?> poolType, int priority)
/*  38:    */   {
/*  39: 54 */     this(poolType, false, priority);
/*  40:    */   }
/*  41:    */   
/*  42:    */   public DefaultThreadFactory(String poolName, int priority)
/*  43:    */   {
/*  44: 58 */     this(poolName, false, priority);
/*  45:    */   }
/*  46:    */   
/*  47:    */   public DefaultThreadFactory(Class<?> poolType, boolean daemon, int priority)
/*  48:    */   {
/*  49: 62 */     this(toPoolName(poolType), daemon, priority);
/*  50:    */   }
/*  51:    */   
/*  52:    */   private static String toPoolName(Class<?> poolType)
/*  53:    */   {
/*  54: 66 */     if (poolType == null) {
/*  55: 67 */       throw new NullPointerException("poolType");
/*  56:    */     }
/*  57: 70 */     String poolName = StringUtil.simpleClassName(poolType);
/*  58: 71 */     switch (poolName.length())
/*  59:    */     {
/*  60:    */     case 0: 
/*  61: 73 */       return "unknown";
/*  62:    */     case 1: 
/*  63: 75 */       return poolName.toLowerCase(Locale.US);
/*  64:    */     }
/*  65: 77 */     if ((Character.isUpperCase(poolName.charAt(0))) && (Character.isLowerCase(poolName.charAt(1)))) {
/*  66: 78 */       return Character.toLowerCase(poolName.charAt(0)) + poolName.substring(1);
/*  67:    */     }
/*  68: 80 */     return poolName;
/*  69:    */   }
/*  70:    */   
/*  71:    */   public DefaultThreadFactory(String poolName, boolean daemon, int priority)
/*  72:    */   {
/*  73: 86 */     if (poolName == null) {
/*  74: 87 */       throw new NullPointerException("poolName");
/*  75:    */     }
/*  76: 89 */     if ((priority < 1) || (priority > 10)) {
/*  77: 90 */       throw new IllegalArgumentException("priority: " + priority + " (expected: Thread.MIN_PRIORITY <= priority <= Thread.MAX_PRIORITY)");
/*  78:    */     }
/*  79: 94 */     this.prefix = (poolName + '-' + poolId.incrementAndGet() + '-');
/*  80: 95 */     this.daemon = daemon;
/*  81: 96 */     this.priority = priority;
/*  82:    */   }
/*  83:    */   
/*  84:    */   public Thread newThread(Runnable r)
/*  85:    */   {
/*  86:101 */     Thread t = newThread(new DefaultRunnableDecorator(r), this.prefix + this.nextId.incrementAndGet());
/*  87:    */     try
/*  88:    */     {
/*  89:103 */       if (t.isDaemon())
/*  90:    */       {
/*  91:104 */         if (!this.daemon) {
/*  92:105 */           t.setDaemon(false);
/*  93:    */         }
/*  94:    */       }
/*  95:108 */       else if (this.daemon) {
/*  96:109 */         t.setDaemon(true);
/*  97:    */       }
/*  98:113 */       if (t.getPriority() != this.priority) {
/*  99:114 */         t.setPriority(this.priority);
/* 100:    */       }
/* 101:    */     }
/* 102:    */     catch (Exception ignored) {}
/* 103:119 */     return t;
/* 104:    */   }
/* 105:    */   
/* 106:    */   protected Thread newThread(Runnable r, String name)
/* 107:    */   {
/* 108:123 */     return new FastThreadLocalThread(r, name);
/* 109:    */   }
/* 110:    */   
/* 111:    */   private static final class DefaultRunnableDecorator
/* 112:    */     implements Runnable
/* 113:    */   {
/* 114:    */     private final Runnable r;
/* 115:    */     
/* 116:    */     DefaultRunnableDecorator(Runnable r)
/* 117:    */     {
/* 118:131 */       this.r = r;
/* 119:    */     }
/* 120:    */     
/* 121:    */     public void run()
/* 122:    */     {
/* 123:    */       try
/* 124:    */       {
/* 125:137 */         this.r.run();
/* 126:    */       }
/* 127:    */       finally
/* 128:    */       {
/* 129:139 */         FastThreadLocal.removeAll();
/* 130:    */       }
/* 131:    */     }
/* 132:    */   }
/* 133:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.concurrent.DefaultThreadFactory
 * JD-Core Version:    0.7.0.1
 */