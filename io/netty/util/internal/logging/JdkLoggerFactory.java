/*  1:   */ package io.netty.util.internal.logging;
/*  2:   */ 
/*  3:   */ import java.util.logging.Logger;
/*  4:   */ 
/*  5:   */ public class JdkLoggerFactory
/*  6:   */   extends InternalLoggerFactory
/*  7:   */ {
/*  8:   */   public InternalLogger newInstance(String name)
/*  9:   */   {
/* 10:30 */     return new JdkLogger(Logger.getLogger(name));
/* 11:   */   }
/* 12:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.logging.JdkLoggerFactory
 * JD-Core Version:    0.7.0.1
 */