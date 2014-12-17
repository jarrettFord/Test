/*  1:   */ package io.netty.handler.codec.spdy;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.StringUtil;
/*  4:   */ 
/*  5:   */ public class DefaultSpdyPingFrame
/*  6:   */   implements SpdyPingFrame
/*  7:   */ {
/*  8:   */   private int id;
/*  9:   */   
/* 10:   */   public DefaultSpdyPingFrame(int id)
/* 11:   */   {
/* 12:33 */     setId(id);
/* 13:   */   }
/* 14:   */   
/* 15:   */   public int id()
/* 16:   */   {
/* 17:38 */     return this.id;
/* 18:   */   }
/* 19:   */   
/* 20:   */   public SpdyPingFrame setId(int id)
/* 21:   */   {
/* 22:43 */     this.id = id;
/* 23:44 */     return this;
/* 24:   */   }
/* 25:   */   
/* 26:   */   public String toString()
/* 27:   */   {
/* 28:49 */     StringBuilder buf = new StringBuilder();
/* 29:50 */     buf.append(StringUtil.simpleClassName(this));
/* 30:51 */     buf.append(StringUtil.NEWLINE);
/* 31:52 */     buf.append("--> ID = ");
/* 32:53 */     buf.append(id());
/* 33:54 */     return buf.toString();
/* 34:   */   }
/* 35:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.DefaultSpdyPingFrame
 * JD-Core Version:    0.7.0.1
 */