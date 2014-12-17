/*  1:   */ package io.netty.handler.timeout;
/*  2:   */ 
/*  3:   */ public final class IdleStateEvent
/*  4:   */ {
/*  5:24 */   public static final IdleStateEvent FIRST_READER_IDLE_STATE_EVENT = new IdleStateEvent(IdleState.READER_IDLE, true);
/*  6:25 */   public static final IdleStateEvent READER_IDLE_STATE_EVENT = new IdleStateEvent(IdleState.READER_IDLE, false);
/*  7:26 */   public static final IdleStateEvent FIRST_WRITER_IDLE_STATE_EVENT = new IdleStateEvent(IdleState.WRITER_IDLE, true);
/*  8:27 */   public static final IdleStateEvent WRITER_IDLE_STATE_EVENT = new IdleStateEvent(IdleState.WRITER_IDLE, false);
/*  9:28 */   public static final IdleStateEvent FIRST_ALL_IDLE_STATE_EVENT = new IdleStateEvent(IdleState.ALL_IDLE, true);
/* 10:29 */   public static final IdleStateEvent ALL_IDLE_STATE_EVENT = new IdleStateEvent(IdleState.ALL_IDLE, false);
/* 11:   */   private final IdleState state;
/* 12:   */   private final boolean first;
/* 13:   */   
/* 14:   */   private IdleStateEvent(IdleState state, boolean first)
/* 15:   */   {
/* 16:35 */     this.state = state;
/* 17:36 */     this.first = first;
/* 18:   */   }
/* 19:   */   
/* 20:   */   public IdleState state()
/* 21:   */   {
/* 22:43 */     return this.state;
/* 23:   */   }
/* 24:   */   
/* 25:   */   public boolean isFirst()
/* 26:   */   {
/* 27:50 */     return this.first;
/* 28:   */   }
/* 29:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.timeout.IdleStateEvent
 * JD-Core Version:    0.7.0.1
 */