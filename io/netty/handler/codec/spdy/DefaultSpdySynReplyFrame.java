/*  1:   */ package io.netty.handler.codec.spdy;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.StringUtil;
/*  4:   */ 
/*  5:   */ public class DefaultSpdySynReplyFrame
/*  6:   */   extends DefaultSpdyHeadersFrame
/*  7:   */   implements SpdySynReplyFrame
/*  8:   */ {
/*  9:   */   public DefaultSpdySynReplyFrame(int streamId)
/* 10:   */   {
/* 11:32 */     super(streamId);
/* 12:   */   }
/* 13:   */   
/* 14:   */   public SpdySynReplyFrame setStreamId(int streamId)
/* 15:   */   {
/* 16:37 */     super.setStreamId(streamId);
/* 17:38 */     return this;
/* 18:   */   }
/* 19:   */   
/* 20:   */   public SpdySynReplyFrame setLast(boolean last)
/* 21:   */   {
/* 22:43 */     super.setLast(last);
/* 23:44 */     return this;
/* 24:   */   }
/* 25:   */   
/* 26:   */   public SpdySynReplyFrame setInvalid()
/* 27:   */   {
/* 28:49 */     super.setInvalid();
/* 29:50 */     return this;
/* 30:   */   }
/* 31:   */   
/* 32:   */   public String toString()
/* 33:   */   {
/* 34:55 */     StringBuilder buf = new StringBuilder();
/* 35:56 */     buf.append(StringUtil.simpleClassName(this));
/* 36:57 */     buf.append("(last: ");
/* 37:58 */     buf.append(isLast());
/* 38:59 */     buf.append(')');
/* 39:60 */     buf.append(StringUtil.NEWLINE);
/* 40:61 */     buf.append("--> Stream-ID = ");
/* 41:62 */     buf.append(streamId());
/* 42:63 */     buf.append(StringUtil.NEWLINE);
/* 43:64 */     buf.append("--> Headers:");
/* 44:65 */     buf.append(StringUtil.NEWLINE);
/* 45:66 */     appendHeaders(buf);
/* 46:   */     
/* 47:   */ 
/* 48:69 */     buf.setLength(buf.length() - StringUtil.NEWLINE.length());
/* 49:70 */     return buf.toString();
/* 50:   */   }
/* 51:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.DefaultSpdySynReplyFrame
 * JD-Core Version:    0.7.0.1
 */