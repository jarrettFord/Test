/*  1:   */ package org.spacehq.packetlib.event.server;
/*  2:   */ 
/*  3:   */ import org.spacehq.packetlib.Server;
/*  4:   */ 
/*  5:   */ public class ServerClosedEvent
/*  6:   */   implements ServerEvent
/*  7:   */ {
/*  8:   */   private Server server;
/*  9:   */   
/* 10:   */   public ServerClosedEvent(Server server)
/* 11:   */   {
/* 12:13 */     this.server = server;
/* 13:   */   }
/* 14:   */   
/* 15:   */   public Server getServer()
/* 16:   */   {
/* 17:22 */     return this.server;
/* 18:   */   }
/* 19:   */   
/* 20:   */   public void call(ServerListener listener)
/* 21:   */   {
/* 22:27 */     listener.serverClosed(this);
/* 23:   */   }
/* 24:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.packetlib.event.server.ServerClosedEvent
 * JD-Core Version:    0.7.0.1
 */