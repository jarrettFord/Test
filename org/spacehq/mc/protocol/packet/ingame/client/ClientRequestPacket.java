/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.client;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ClientRequestPacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private Request request;
/* 12:   */   
/* 13:   */   private ClientRequestPacket() {}
/* 14:   */   
/* 15:   */   public ClientRequestPacket(Request request)
/* 16:   */   {
/* 17:18 */     this.request = request;
/* 18:   */   }
/* 19:   */   
/* 20:   */   public Request getRequest()
/* 21:   */   {
/* 22:22 */     return this.request;
/* 23:   */   }
/* 24:   */   
/* 25:   */   public void read(NetInput in)
/* 26:   */     throws IOException
/* 27:   */   {
/* 28:27 */     this.request = Request.values()[in.readByte()];
/* 29:   */   }
/* 30:   */   
/* 31:   */   public void write(NetOutput out)
/* 32:   */     throws IOException
/* 33:   */   {
/* 34:32 */     out.writeByte(this.request.ordinal());
/* 35:   */   }
/* 36:   */   
/* 37:   */   public boolean isPriority()
/* 38:   */   {
/* 39:37 */     return false;
/* 40:   */   }
/* 41:   */   
/* 42:   */   public static enum Request
/* 43:   */   {
/* 44:41 */     RESPAWN,  STATS,  OPEN_INVENTORY_ACHIEVEMENT;
/* 45:   */   }
/* 46:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.client.ClientRequestPacket
 * JD-Core Version:    0.7.0.1
 */