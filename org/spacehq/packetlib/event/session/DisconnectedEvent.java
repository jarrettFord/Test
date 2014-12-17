/*  1:   */ package org.spacehq.packetlib.event.session;
/*  2:   */ 
/*  3:   */ import org.spacehq.packetlib.Session;
/*  4:   */ 
/*  5:   */ public class DisconnectedEvent
/*  6:   */   implements SessionEvent
/*  7:   */ {
/*  8:   */   private Session session;
/*  9:   */   private String reason;
/* 10:   */   
/* 11:   */   public DisconnectedEvent(Session session, String reason)
/* 12:   */   {
/* 13:14 */     this.session = session;
/* 14:15 */     this.reason = reason;
/* 15:   */   }
/* 16:   */   
/* 17:   */   public Session getSession()
/* 18:   */   {
/* 19:24 */     return this.session;
/* 20:   */   }
/* 21:   */   
/* 22:   */   public String getReason()
/* 23:   */   {
/* 24:33 */     return this.reason;
/* 25:   */   }
/* 26:   */   
/* 27:   */   public void call(SessionListener listener)
/* 28:   */   {
/* 29:38 */     listener.disconnected(this);
/* 30:   */   }
/* 31:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.packetlib.event.session.DisconnectedEvent
 * JD-Core Version:    0.7.0.1
 */