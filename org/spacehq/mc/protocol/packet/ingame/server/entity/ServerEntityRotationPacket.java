/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server.entity;
/*  2:   */ 
/*  3:   */ public class ServerEntityRotationPacket
/*  4:   */   extends ServerEntityMovementPacket
/*  5:   */ {
/*  6:   */   protected ServerEntityRotationPacket()
/*  7:   */   {
/*  8: 6 */     this.rot = true;
/*  9:   */   }
/* 10:   */   
/* 11:   */   public ServerEntityRotationPacket(int entityId, float yaw, float pitch)
/* 12:   */   {
/* 13:10 */     super(entityId);
/* 14:11 */     this.rot = true;
/* 15:12 */     this.yaw = yaw;
/* 16:13 */     this.pitch = pitch;
/* 17:   */   }
/* 18:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityRotationPacket
 * JD-Core Version:    0.7.0.1
 */