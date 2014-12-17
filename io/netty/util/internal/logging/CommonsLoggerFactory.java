/*  1:   */ package io.netty.util.internal.logging;
/*  2:   */ 
/*  3:   */ import java.util.HashMap;
/*  4:   */ import java.util.Map;
/*  5:   */ import org.apache.commons.logging.LogFactory;
/*  6:   */ 
/*  7:   */ public class CommonsLoggerFactory
/*  8:   */   extends InternalLoggerFactory
/*  9:   */ {
/* 10:31 */   Map<String, InternalLogger> loggerMap = new HashMap();
/* 11:   */   
/* 12:   */   public InternalLogger newInstance(String name)
/* 13:   */   {
/* 14:35 */     return new CommonsLogger(LogFactory.getLog(name), name);
/* 15:   */   }
/* 16:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.logging.CommonsLoggerFactory
 * JD-Core Version:    0.7.0.1
 */