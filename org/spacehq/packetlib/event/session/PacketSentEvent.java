/*  1:   */ package org.spacehq.packetlib.event.session;
/*  2:   */ 
/*  3:   */ import org.spacehq.packetlib.Session;
/*  4:   */ import org.spacehq.packetlib.packet.Packet;
/*  5:   */ 
/*  6:   */ public class PacketSentEvent
/*  7:   */   implements SessionEvent
/*  8:   */ {
/*  9:   */   private Session session;
/* 10:   */   private Packet packet;
/* 11:   */   
/* 12:   */   public PacketSentEvent(Session session, Packet packet)
/* 13:   */   {
/* 14:15 */     this.session = session;
/* 15:16 */     this.packet = packet;
/* 16:   */   }
/* 17:   */   
/* 18:   */   public Session getSession()
/* 19:   */   {
/* 20:25 */     return this.session;
/* 21:   */   }
/* 22:   */   
/* 23:   */   public <T extends Packet> T getPacket()
/* 24:   */   {
/* 25:   */     try
/* 26:   */     {
/* 27:37 */       return this.packet;
/* 28:   */     }
/* 29:   */     catch (ClassCastException e)
/* 30:   */     {
/* 31:39 */       throw new IllegalStateException("Tried to get packet as the wrong type. Actual type: " + this.packet.getClass().getName());
/* 32:   */     }
/* 33:   */   }
/* 34:   */   
/* 35:   */   public void call(SessionListener listener)
/* 36:   */   {
/* 37:45 */     listener.packetSent(this);
/* 38:   */   }
/* 39:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.packetlib.event.session.PacketSentEvent
 * JD-Core Version:    0.7.0.1
 */