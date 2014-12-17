/*  1:   */ package io.netty.util.internal.logging;
/*  2:   */ 
/*  3:   */ import org.apache.log4j.Logger;
/*  4:   */ 
/*  5:   */ public class Log4JLoggerFactory
/*  6:   */   extends InternalLoggerFactory
/*  7:   */ {
/*  8:   */   public InternalLogger newInstance(String name)
/*  9:   */   {
/* 10:29 */     return new Log4JLogger(Logger.getLogger(name));
/* 11:   */   }
/* 12:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.logging.Log4JLoggerFactory
 * JD-Core Version:    0.7.0.1
 */