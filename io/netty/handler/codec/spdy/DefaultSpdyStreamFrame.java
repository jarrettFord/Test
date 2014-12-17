/*  1:   */ package io.netty.handler.codec.spdy;
/*  2:   */ 
/*  3:   */ public abstract class DefaultSpdyStreamFrame
/*  4:   */   implements SpdyStreamFrame
/*  5:   */ {
/*  6:   */   private int streamId;
/*  7:   */   private boolean last;
/*  8:   */   
/*  9:   */   protected DefaultSpdyStreamFrame(int streamId)
/* 10:   */   {
/* 11:32 */     setStreamId(streamId);
/* 12:   */   }
/* 13:   */   
/* 14:   */   public int streamId()
/* 15:   */   {
/* 16:37 */     return this.streamId;
/* 17:   */   }
/* 18:   */   
/* 19:   */   public SpdyStreamFrame setStreamId(int streamId)
/* 20:   */   {
/* 21:42 */     if (streamId <= 0) {
/* 22:43 */       throw new IllegalArgumentException("Stream-ID must be positive: " + streamId);
/* 23:   */     }
/* 24:46 */     this.streamId = streamId;
/* 25:47 */     return this;
/* 26:   */   }
/* 27:   */   
/* 28:   */   public boolean isLast()
/* 29:   */   {
/* 30:52 */     return this.last;
/* 31:   */   }
/* 32:   */   
/* 33:   */   public SpdyStreamFrame setLast(boolean last)
/* 34:   */   {
/* 35:57 */     this.last = last;
/* 36:58 */     return this;
/* 37:   */   }
/* 38:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.DefaultSpdyStreamFrame
 * JD-Core Version:    0.7.0.1
 */