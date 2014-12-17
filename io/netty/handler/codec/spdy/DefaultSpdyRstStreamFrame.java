/*  1:   */ package io.netty.handler.codec.spdy;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.StringUtil;
/*  4:   */ 
/*  5:   */ public class DefaultSpdyRstStreamFrame
/*  6:   */   extends DefaultSpdyStreamFrame
/*  7:   */   implements SpdyRstStreamFrame
/*  8:   */ {
/*  9:   */   private SpdyStreamStatus status;
/* 10:   */   
/* 11:   */   public DefaultSpdyRstStreamFrame(int streamId, int statusCode)
/* 12:   */   {
/* 13:35 */     this(streamId, SpdyStreamStatus.valueOf(statusCode));
/* 14:   */   }
/* 15:   */   
/* 16:   */   public DefaultSpdyRstStreamFrame(int streamId, SpdyStreamStatus status)
/* 17:   */   {
/* 18:45 */     super(streamId);
/* 19:46 */     setStatus(status);
/* 20:   */   }
/* 21:   */   
/* 22:   */   public SpdyRstStreamFrame setStreamId(int streamId)
/* 23:   */   {
/* 24:51 */     super.setStreamId(streamId);
/* 25:52 */     return this;
/* 26:   */   }
/* 27:   */   
/* 28:   */   public SpdyRstStreamFrame setLast(boolean last)
/* 29:   */   {
/* 30:57 */     super.setLast(last);
/* 31:58 */     return this;
/* 32:   */   }
/* 33:   */   
/* 34:   */   public SpdyStreamStatus status()
/* 35:   */   {
/* 36:63 */     return this.status;
/* 37:   */   }
/* 38:   */   
/* 39:   */   public SpdyRstStreamFrame setStatus(SpdyStreamStatus status)
/* 40:   */   {
/* 41:68 */     this.status = status;
/* 42:69 */     return this;
/* 43:   */   }
/* 44:   */   
/* 45:   */   public String toString()
/* 46:   */   {
/* 47:74 */     StringBuilder buf = new StringBuilder();
/* 48:75 */     buf.append(StringUtil.simpleClassName(this));
/* 49:76 */     buf.append(StringUtil.NEWLINE);
/* 50:77 */     buf.append("--> Stream-ID = ");
/* 51:78 */     buf.append(streamId());
/* 52:79 */     buf.append(StringUtil.NEWLINE);
/* 53:80 */     buf.append("--> Status: ");
/* 54:81 */     buf.append(status());
/* 55:82 */     return buf.toString();
/* 56:   */   }
/* 57:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.DefaultSpdyRstStreamFrame
 * JD-Core Version:    0.7.0.1
 */