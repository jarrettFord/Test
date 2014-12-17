/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server.entity;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ServerDestroyEntitiesPacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private int[] entityIds;
/* 12:   */   
/* 13:   */   private ServerDestroyEntitiesPacket() {}
/* 14:   */   
/* 15:   */   public ServerDestroyEntitiesPacket(int... entityIds)
/* 16:   */   {
/* 17:18 */     this.entityIds = entityIds;
/* 18:   */   }
/* 19:   */   
/* 20:   */   public int[] getEntityIds()
/* 21:   */   {
/* 22:22 */     return this.entityIds;
/* 23:   */   }
/* 24:   */   
/* 25:   */   public void read(NetInput in)
/* 26:   */     throws IOException
/* 27:   */   {
/* 28:27 */     this.entityIds = new int[in.readByte()];
/* 29:28 */     for (int index = 0; index < this.entityIds.length; index++) {
/* 30:29 */       this.entityIds[index] = in.readInt();
/* 31:   */     }
/* 32:   */   }
/* 33:   */   
/* 34:   */   public void write(NetOutput out)
/* 35:   */     throws IOException
/* 36:   */   {
/* 37:35 */     out.writeByte(this.entityIds.length);
/* 38:36 */     for (int entityId : this.entityIds) {
/* 39:37 */       out.writeInt(entityId);
/* 40:   */     }
/* 41:   */   }
/* 42:   */   
/* 43:   */   public boolean isPriority()
/* 44:   */   {
/* 45:43 */     return false;
/* 46:   */   }
/* 47:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.entity.ServerDestroyEntitiesPacket
 * JD-Core Version:    0.7.0.1
 */