/*  1:   */ package io.netty.util.internal;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.logging.InternalLogger;
/*  4:   */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  5:   */ import java.lang.reflect.Field;
/*  6:   */ import java.nio.ByteBuffer;
/*  7:   */ import sun.misc.Cleaner;
/*  8:   */ 
/*  9:   */ final class Cleaner0
/* 10:   */ {
/* 11:   */   private static final long CLEANER_FIELD_OFFSET;
/* 12:34 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(Cleaner0.class);
/* 13:   */   
/* 14:   */   static
/* 15:   */   {
/* 16:37 */     ByteBuffer direct = ByteBuffer.allocateDirect(1);
/* 17:   */     
/* 18:39 */     long fieldOffset = -1L;
/* 19:40 */     if (PlatformDependent0.hasUnsafe()) {
/* 20:   */       try
/* 21:   */       {
/* 22:42 */         Field cleanerField = direct.getClass().getDeclaredField("cleaner");
/* 23:43 */         cleanerField.setAccessible(true);
/* 24:44 */         Cleaner cleaner = (Cleaner)cleanerField.get(direct);
/* 25:45 */         cleaner.clean();
/* 26:46 */         fieldOffset = PlatformDependent0.objectFieldOffset(cleanerField);
/* 27:   */       }
/* 28:   */       catch (Throwable t)
/* 29:   */       {
/* 30:49 */         fieldOffset = -1L;
/* 31:   */       }
/* 32:   */     }
/* 33:52 */     logger.debug("java.nio.ByteBuffer.cleaner(): {}", fieldOffset != -1L ? "available" : "unavailable");
/* 34:53 */     CLEANER_FIELD_OFFSET = fieldOffset;
/* 35:   */     
/* 36:   */ 
/* 37:56 */     freeDirectBuffer(direct);
/* 38:   */   }
/* 39:   */   
/* 40:   */   static void freeDirectBuffer(ByteBuffer buffer)
/* 41:   */   {
/* 42:60 */     if ((CLEANER_FIELD_OFFSET == -1L) || (!buffer.isDirect())) {
/* 43:61 */       return;
/* 44:   */     }
/* 45:   */     try
/* 46:   */     {
/* 47:64 */       Cleaner cleaner = (Cleaner)PlatformDependent0.getObject(buffer, CLEANER_FIELD_OFFSET);
/* 48:65 */       if (cleaner != null) {
/* 49:66 */         cleaner.clean();
/* 50:   */       }
/* 51:   */     }
/* 52:   */     catch (Throwable t) {}
/* 53:   */   }
/* 54:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.Cleaner0
 * JD-Core Version:    0.7.0.1
 */