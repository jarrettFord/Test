/*  1:   */ package io.netty.util.internal.logging;
/*  2:   */ 
/*  3:   */ import java.io.OutputStream;
/*  4:   */ import java.io.PrintStream;
/*  5:   */ import java.io.UnsupportedEncodingException;
/*  6:   */ import org.slf4j.LoggerFactory;
/*  7:   */ import org.slf4j.helpers.NOPLoggerFactory;
/*  8:   */ 
/*  9:   */ public class Slf4JLoggerFactory
/* 10:   */   extends InternalLoggerFactory
/* 11:   */ {
/* 12:   */   public Slf4JLoggerFactory() {}
/* 13:   */   
/* 14:   */   Slf4JLoggerFactory(boolean failIfNOP)
/* 15:   */   {
/* 16:36 */     assert (failIfNOP);
/* 17:   */     
/* 18:   */ 
/* 19:   */ 
/* 20:40 */     final StringBuffer buf = new StringBuffer();
/* 21:41 */     PrintStream err = System.err;
/* 22:   */     try
/* 23:   */     {
/* 24:43 */       System.setErr(new PrintStream(new OutputStream()
/* 25:   */       {
/* 26:   */         public void write(int b)
/* 27:   */         {
/* 28:46 */           buf.append((char)b);
/* 29:   */         }
/* 30:46 */       }, true, "US-ASCII"));
/* 31:   */     }
/* 32:   */     catch (UnsupportedEncodingException e)
/* 33:   */     {
/* 34:50 */       throw new Error(e);
/* 35:   */     }
/* 36:   */     try
/* 37:   */     {
/* 38:54 */       if ((LoggerFactory.getILoggerFactory() instanceof NOPLoggerFactory)) {
/* 39:55 */         throw new NoClassDefFoundError(buf.toString());
/* 40:   */       }
/* 41:57 */       err.print(buf.toString());
/* 42:58 */       err.flush();
/* 43:   */     }
/* 44:   */     finally
/* 45:   */     {
/* 46:61 */       System.setErr(err);
/* 47:   */     }
/* 48:   */   }
/* 49:   */   
/* 50:   */   public InternalLogger newInstance(String name)
/* 51:   */   {
/* 52:67 */     return new Slf4JLogger(LoggerFactory.getLogger(name));
/* 53:   */   }
/* 54:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.logging.Slf4JLoggerFactory
 * JD-Core Version:    0.7.0.1
 */