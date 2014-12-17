/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server.entity;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ServerCollectItemPacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private int collectedEntityId;
/* 12:   */   private int collectorEntityId;
/* 13:   */   
/* 14:   */   private ServerCollectItemPacket() {}
/* 15:   */   
/* 16:   */   public ServerCollectItemPacket(int collectedEntityId, int collectorEntityId)
/* 17:   */   {
/* 18:19 */     this.collectedEntityId = collectedEntityId;
/* 19:20 */     this.collectorEntityId = collectorEntityId;
/* 20:   */   }
/* 21:   */   
/* 22:   */   public int getCollectedEntityId()
/* 23:   */   {
/* 24:24 */     return this.collectedEntityId;
/* 25:   */   }
/* 26:   */   
/* 27:   */   public int getCollectorEntityId()
/* 28:   */   {
/* 29:28 */     return this.collectorEntityId;
/* 30:   */   }
/* 31:   */   
/* 32:   */   public void read(NetInput in)
/* 33:   */     throws IOException
/* 34:   */   {
/* 35:33 */     this.collectedEntityId = in.readInt();
/* 36:34 */     this.collectorEntityId = in.readInt();
/* 37:   */   }
/* 38:   */   
/* 39:   */   public void write(NetOutput out)
/* 40:   */     throws IOException
/* 41:   */   {
/* 42:39 */     out.writeInt(this.collectedEntityId);
/* 43:40 */     out.writeInt(this.collectorEntityId);
/* 44:   */   }
/* 45:   */   
/* 46:   */   public boolean isPriority()
/* 47:   */   {
/* 48:45 */     return false;
/* 49:   */   }
/* 50:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.entity.ServerCollectItemPacket
 * JD-Core Version:    0.7.0.1
 */