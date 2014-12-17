/*  1:   */ package io.netty.handler.codec.spdy;
/*  2:   */ 
/*  3:   */ import io.netty.util.internal.StringUtil;
/*  4:   */ 
/*  5:   */ public class DefaultSpdyWindowUpdateFrame
/*  6:   */   implements SpdyWindowUpdateFrame
/*  7:   */ {
/*  8:   */   private int streamId;
/*  9:   */   private int deltaWindowSize;
/* 10:   */   
/* 11:   */   public DefaultSpdyWindowUpdateFrame(int streamId, int deltaWindowSize)
/* 12:   */   {
/* 13:35 */     setStreamId(streamId);
/* 14:36 */     setDeltaWindowSize(deltaWindowSize);
/* 15:   */   }
/* 16:   */   
/* 17:   */   public int streamId()
/* 18:   */   {
/* 19:41 */     return this.streamId;
/* 20:   */   }
/* 21:   */   
/* 22:   */   public SpdyWindowUpdateFrame setStreamId(int streamId)
/* 23:   */   {
/* 24:46 */     if (streamId < 0) {
/* 25:47 */       throw new IllegalArgumentException("Stream-ID cannot be negative: " + streamId);
/* 26:   */     }
/* 27:50 */     this.streamId = streamId;
/* 28:51 */     return this;
/* 29:   */   }
/* 30:   */   
/* 31:   */   public int deltaWindowSize()
/* 32:   */   {
/* 33:56 */     return this.deltaWindowSize;
/* 34:   */   }
/* 35:   */   
/* 36:   */   public SpdyWindowUpdateFrame setDeltaWindowSize(int deltaWindowSize)
/* 37:   */   {
/* 38:61 */     if (deltaWindowSize <= 0) {
/* 39:62 */       throw new IllegalArgumentException("Delta-Window-Size must be positive: " + deltaWindowSize);
/* 40:   */     }
/* 41:66 */     this.deltaWindowSize = deltaWindowSize;
/* 42:67 */     return this;
/* 43:   */   }
/* 44:   */   
/* 45:   */   public String toString()
/* 46:   */   {
/* 47:72 */     StringBuilder buf = new StringBuilder();
/* 48:73 */     buf.append(StringUtil.simpleClassName(this));
/* 49:74 */     buf.append(StringUtil.NEWLINE);
/* 50:75 */     buf.append("--> Stream-ID = ");
/* 51:76 */     buf.append(streamId());
/* 52:77 */     buf.append(StringUtil.NEWLINE);
/* 53:78 */     buf.append("--> Delta-Window-Size = ");
/* 54:79 */     buf.append(deltaWindowSize());
/* 55:80 */     return buf.toString();
/* 56:   */   }
/* 57:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.DefaultSpdyWindowUpdateFrame
 * JD-Core Version:    0.7.0.1
 */