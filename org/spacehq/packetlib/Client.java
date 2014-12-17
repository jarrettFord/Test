/*  1:   */ package org.spacehq.packetlib;
/*  2:   */ 
/*  3:   */ import org.spacehq.packetlib.packet.PacketProtocol;
/*  4:   */ 
/*  5:   */ public class Client
/*  6:   */ {
/*  7:   */   private String host;
/*  8:   */   private int port;
/*  9:   */   private PacketProtocol protocol;
/* 10:   */   private Session session;
/* 11:   */   
/* 12:   */   public Client(String host, int port, PacketProtocol protocol, SessionFactory factory)
/* 13:   */   {
/* 14:16 */     this.host = host;
/* 15:17 */     this.port = port;
/* 16:18 */     this.protocol = protocol;
/* 17:19 */     this.session = factory.createClientSession(this);
/* 18:   */   }
/* 19:   */   
/* 20:   */   public String getHost()
/* 21:   */   {
/* 22:28 */     return this.host;
/* 23:   */   }
/* 24:   */   
/* 25:   */   public int getPort()
/* 26:   */   {
/* 27:37 */     return this.port;
/* 28:   */   }
/* 29:   */   
/* 30:   */   public PacketProtocol getPacketProtocol()
/* 31:   */   {
/* 32:46 */     return this.protocol;
/* 33:   */   }
/* 34:   */   
/* 35:   */   public Session getSession()
/* 36:   */   {
/* 37:55 */     return this.session;
/* 38:   */   }
/* 39:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.packetlib.Client
 * JD-Core Version:    0.7.0.1
 */