/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import org.apache.tomcat.jni.SSLContext;
/*   4:    */ 
/*   5:    */ public final class OpenSslSessionStats
/*   6:    */ {
/*   7:    */   private final long context;
/*   8:    */   
/*   9:    */   OpenSslSessionStats(long context)
/*  10:    */   {
/*  11: 31 */     this.context = context;
/*  12:    */   }
/*  13:    */   
/*  14:    */   public long number()
/*  15:    */   {
/*  16: 38 */     return SSLContext.sessionNumber(this.context);
/*  17:    */   }
/*  18:    */   
/*  19:    */   public long connect()
/*  20:    */   {
/*  21: 45 */     return SSLContext.sessionConnect(this.context);
/*  22:    */   }
/*  23:    */   
/*  24:    */   public long connectGood()
/*  25:    */   {
/*  26: 52 */     return SSLContext.sessionConnectGood(this.context);
/*  27:    */   }
/*  28:    */   
/*  29:    */   public long connectRenegotiate()
/*  30:    */   {
/*  31: 59 */     return SSLContext.sessionConnectRenegotiate(this.context);
/*  32:    */   }
/*  33:    */   
/*  34:    */   public long accept()
/*  35:    */   {
/*  36: 66 */     return SSLContext.sessionAccept(this.context);
/*  37:    */   }
/*  38:    */   
/*  39:    */   public long acceptGood()
/*  40:    */   {
/*  41: 73 */     return SSLContext.sessionAcceptGood(this.context);
/*  42:    */   }
/*  43:    */   
/*  44:    */   public long acceptRenegotiate()
/*  45:    */   {
/*  46: 80 */     return SSLContext.sessionAcceptRenegotiate(this.context);
/*  47:    */   }
/*  48:    */   
/*  49:    */   public long hits()
/*  50:    */   {
/*  51: 89 */     return SSLContext.sessionHits(this.context);
/*  52:    */   }
/*  53:    */   
/*  54:    */   public long cbHits()
/*  55:    */   {
/*  56: 96 */     return SSLContext.sessionCbHits(this.context);
/*  57:    */   }
/*  58:    */   
/*  59:    */   public long misses()
/*  60:    */   {
/*  61:104 */     return SSLContext.sessionMisses(this.context);
/*  62:    */   }
/*  63:    */   
/*  64:    */   public long timeouts()
/*  65:    */   {
/*  66:113 */     return SSLContext.sessionTimeouts(this.context);
/*  67:    */   }
/*  68:    */   
/*  69:    */   public long cacheFull()
/*  70:    */   {
/*  71:120 */     return SSLContext.sessionCacheFull(this.context);
/*  72:    */   }
/*  73:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.ssl.OpenSslSessionStats
 * JD-Core Version:    0.7.0.1
 */