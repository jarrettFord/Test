/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server.entity;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ServerEntityHeadLookPacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   protected int entityId;
/* 12:   */   protected float headYaw;
/* 13:   */   
/* 14:   */   private ServerEntityHeadLookPacket() {}
/* 15:   */   
/* 16:   */   public ServerEntityHeadLookPacket(int entityId, float headYaw)
/* 17:   */   {
/* 18:19 */     this.entityId = entityId;
/* 19:20 */     this.headYaw = headYaw;
/* 20:   */   }
/* 21:   */   
/* 22:   */   public int getEntityId()
/* 23:   */   {
/* 24:24 */     return this.entityId;
/* 25:   */   }
/* 26:   */   
/* 27:   */   public float getHeadYaw()
/* 28:   */   {
/* 29:28 */     return this.headYaw;
/* 30:   */   }
/* 31:   */   
/* 32:   */   public void read(NetInput in)
/* 33:   */     throws IOException
/* 34:   */   {
/* 35:33 */     this.entityId = in.readInt();
/* 36:34 */     this.headYaw = (in.readByte() * 360 / 256.0F);
/* 37:   */   }
/* 38:   */   
/* 39:   */   public void write(NetOutput out)
/* 40:   */     throws IOException
/* 41:   */   {
/* 42:39 */     out.writeInt(this.entityId);
/* 43:40 */     out.writeByte((byte)(int)(this.headYaw * 256.0F / 360.0F));
/* 44:   */   }
/* 45:   */   
/* 46:   */   public boolean isPriority()
/* 47:   */   {
/* 48:45 */     return false;
/* 49:   */   }
/* 50:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityHeadLookPacket
 * JD-Core Version:    0.7.0.1
 */