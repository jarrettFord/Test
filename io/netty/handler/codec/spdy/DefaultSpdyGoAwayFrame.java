/*  1:   */ package io.netty.handler.codec.spdy;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.StringUtil;
/*  4:   */ 
/*  5:   */ public class DefaultSpdyGoAwayFrame
/*  6:   */   implements SpdyGoAwayFrame
/*  7:   */ {
/*  8:   */   private int lastGoodStreamId;
/*  9:   */   private SpdySessionStatus status;
/* 10:   */   
/* 11:   */   public DefaultSpdyGoAwayFrame(int lastGoodStreamId)
/* 12:   */   {
/* 13:34 */     this(lastGoodStreamId, 0);
/* 14:   */   }
/* 15:   */   
/* 16:   */   public DefaultSpdyGoAwayFrame(int lastGoodStreamId, int statusCode)
/* 17:   */   {
/* 18:44 */     this(lastGoodStreamId, SpdySessionStatus.valueOf(statusCode));
/* 19:   */   }
/* 20:   */   
/* 21:   */   public DefaultSpdyGoAwayFrame(int lastGoodStreamId, SpdySessionStatus status)
/* 22:   */   {
/* 23:54 */     setLastGoodStreamId(lastGoodStreamId);
/* 24:55 */     setStatus(status);
/* 25:   */   }
/* 26:   */   
/* 27:   */   public int lastGoodStreamId()
/* 28:   */   {
/* 29:60 */     return this.lastGoodStreamId;
/* 30:   */   }
/* 31:   */   
/* 32:   */   public SpdyGoAwayFrame setLastGoodStreamId(int lastGoodStreamId)
/* 33:   */   {
/* 34:65 */     if (lastGoodStreamId < 0) {
/* 35:66 */       throw new IllegalArgumentException("Last-good-stream-ID cannot be negative: " + lastGoodStreamId);
/* 36:   */     }
/* 37:69 */     this.lastGoodStreamId = lastGoodStreamId;
/* 38:70 */     return this;
/* 39:   */   }
/* 40:   */   
/* 41:   */   public SpdySessionStatus status()
/* 42:   */   {
/* 43:75 */     return this.status;
/* 44:   */   }
/* 45:   */   
/* 46:   */   public SpdyGoAwayFrame setStatus(SpdySessionStatus status)
/* 47:   */   {
/* 48:80 */     this.status = status;
/* 49:81 */     return this;
/* 50:   */   }
/* 51:   */   
/* 52:   */   public String toString()
/* 53:   */   {
/* 54:86 */     StringBuilder buf = new StringBuilder();
/* 55:87 */     buf.append(StringUtil.simpleClassName(this));
/* 56:88 */     buf.append(StringUtil.NEWLINE);
/* 57:89 */     buf.append("--> Last-good-stream-ID = ");
/* 58:90 */     buf.append(lastGoodStreamId());
/* 59:91 */     buf.append(StringUtil.NEWLINE);
/* 60:92 */     buf.append("--> Status: ");
/* 61:93 */     buf.append(status());
/* 62:94 */     return buf.toString();
/* 63:   */   }
/* 64:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.DefaultSpdyGoAwayFrame
 * JD-Core Version:    0.7.0.1
 */