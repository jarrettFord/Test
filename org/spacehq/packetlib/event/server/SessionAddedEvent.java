/*  1:   */ package org.spacehq.packetlib.event.server;
/*  2:   */ 
/*  3:   */ import org.spacehq.packetlib.Server;
/*  4:   */ import org.spacehq.packetlib.Session;
/*  5:   */ 
/*  6:   */ public class SessionAddedEvent
/*  7:   */   implements ServerEvent
/*  8:   */ {
/*  9:   */   private Server server;
/* 10:   */   private Session session;
/* 11:   */   
/* 12:   */   public SessionAddedEvent(Server server, Session session)
/* 13:   */   {
/* 14:15 */     this.server = server;
/* 15:16 */     this.session = session;
/* 16:   */   }
/* 17:   */   
/* 18:   */   public Server getServer()
/* 19:   */   {
/* 20:25 */     return this.server;
/* 21:   */   }
/* 22:   */   
/* 23:   */   public Session getSession()
/* 24:   */   {
/* 25:34 */     return this.session;
/* 26:   */   }
/* 27:   */   
/* 28:   */   public void call(ServerListener listener)
/* 29:   */   {
/* 30:39 */     listener.sessionAdded(this);
/* 31:   */   }
/* 32:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.packetlib.event.server.SessionAddedEvent
 * JD-Core Version:    0.7.0.1
 */