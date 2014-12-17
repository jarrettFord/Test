/*  1:   */ package org.spacehq.packetlib.event.session;
/*  2:   */ 
/*  3:   */ import org.spacehq.packetlib.Session;
/*  4:   */ 
/*  5:   */ public class ConnectedEvent
/*  6:   */   implements SessionEvent
/*  7:   */ {
/*  8:   */   private Session session;
/*  9:   */   
/* 10:   */   public ConnectedEvent(Session session)
/* 11:   */   {
/* 12:13 */     this.session = session;
/* 13:   */   }
/* 14:   */   
/* 15:   */   public Session getSession()
/* 16:   */   {
/* 17:22 */     return this.session;
/* 18:   */   }
/* 19:   */   
/* 20:   */   public void call(SessionListener listener)
/* 21:   */   {
/* 22:27 */     listener.connected(this);
/* 23:   */   }
/* 24:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.packetlib.event.session.ConnectedEvent
 * JD-Core Version:    0.7.0.1
 */