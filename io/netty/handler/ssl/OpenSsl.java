/*  1:   */ package io.netty.handler.ssl;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.NativeLibraryLoader;
/*  4:   */ import io.netty.util.internal.logging.InternalLogger;
/*  5:   */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  6:   */ import org.apache.tomcat.jni.Library;
/*  7:   */ import org.apache.tomcat.jni.SSL;
/*  8:   */ 
/*  9:   */ public final class OpenSsl
/* 10:   */ {
/* 11:31 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(OpenSsl.class);
/* 12:   */   private static final Throwable UNAVAILABILITY_CAUSE;
/* 13:   */   static final String IGNORABLE_ERROR_PREFIX = "error:00000000:";
/* 14:   */   
/* 15:   */   static
/* 16:   */   {
/* 17:37 */     Throwable cause = null;
/* 18:   */     try
/* 19:   */     {
/* 20:39 */       NativeLibraryLoader.load("netty-tcnative", SSL.class.getClassLoader());
/* 21:40 */       Library.initialize("provided");
/* 22:41 */       SSL.initialize(null);
/* 23:   */     }
/* 24:   */     catch (Throwable t)
/* 25:   */     {
/* 26:43 */       cause = t;
/* 27:44 */       logger.debug("Failed to load netty-tcnative; " + OpenSslEngine.class.getSimpleName() + " will be unavailable.", t);
/* 28:   */     }
/* 29:48 */     UNAVAILABILITY_CAUSE = cause;
/* 30:   */   }
/* 31:   */   
/* 32:   */   public static boolean isAvailable()
/* 33:   */   {
/* 34:57 */     return UNAVAILABILITY_CAUSE == null;
/* 35:   */   }
/* 36:   */   
/* 37:   */   public static void ensureAvailability()
/* 38:   */   {
/* 39:67 */     if (UNAVAILABILITY_CAUSE != null) {
/* 40:68 */       throw ((Error)new UnsatisfiedLinkError("failed to load the required native library").initCause(UNAVAILABILITY_CAUSE));
/* 41:   */     }
/* 42:   */   }
/* 43:   */   
/* 44:   */   public static Throwable unavailabilityCause()
/* 45:   */   {
/* 46:80 */     return UNAVAILABILITY_CAUSE;
/* 47:   */   }
/* 48:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.ssl.OpenSsl
 * JD-Core Version:    0.7.0.1
 */