/*  1:   */ package io.netty.util.internal.logging;
/*  2:   */ 
/*  3:   */ public abstract class InternalLoggerFactory
/*  4:   */ {
/*  5:   */   private static volatile InternalLoggerFactory defaultFactory;
/*  6:   */   
/*  7:   */   static
/*  8:   */   {
/*  9:37 */     String name = InternalLoggerFactory.class.getName();
/* 10:   */     InternalLoggerFactory f;
/* 11:   */     try
/* 12:   */     {
/* 13:40 */       f = new Slf4JLoggerFactory(true);
/* 14:41 */       f.newInstance(name).debug("Using SLF4J as the default logging framework");
/* 15:42 */       defaultFactory = f;
/* 16:   */     }
/* 17:   */     catch (Throwable t1)
/* 18:   */     {
/* 19:   */       try
/* 20:   */       {
/* 21:45 */         f = new Log4JLoggerFactory();
/* 22:46 */         f.newInstance(name).debug("Using Log4J as the default logging framework");
/* 23:   */       }
/* 24:   */       catch (Throwable t2)
/* 25:   */       {
/* 26:48 */         f = new JdkLoggerFactory();
/* 27:49 */         f.newInstance(name).debug("Using java.util.logging as the default logging framework");
/* 28:   */       }
/* 29:   */     }
/* 30:53 */     defaultFactory = f;
/* 31:   */   }
/* 32:   */   
/* 33:   */   public static InternalLoggerFactory getDefaultFactory()
/* 34:   */   {
/* 35:61 */     return defaultFactory;
/* 36:   */   }
/* 37:   */   
/* 38:   */   public static void setDefaultFactory(InternalLoggerFactory defaultFactory)
/* 39:   */   {
/* 40:68 */     if (defaultFactory == null) {
/* 41:69 */       throw new NullPointerException("defaultFactory");
/* 42:   */     }
/* 43:71 */     defaultFactory = defaultFactory;
/* 44:   */   }
/* 45:   */   
/* 46:   */   public static InternalLogger getInstance(Class<?> clazz)
/* 47:   */   {
/* 48:78 */     return getInstance(clazz.getName());
/* 49:   */   }
/* 50:   */   
/* 51:   */   public static InternalLogger getInstance(String name)
/* 52:   */   {
/* 53:85 */     return getDefaultFactory().newInstance(name);
/* 54:   */   }
/* 55:   */   
/* 56:   */   protected abstract InternalLogger newInstance(String paramString);
/* 57:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.logging.InternalLoggerFactory
 * JD-Core Version:    0.7.0.1
 */