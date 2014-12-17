/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server.entity;
/*  2:   */ 
/*  3:   */ public class ServerEntityPositionRotationPacket
/*  4:   */   extends ServerEntityMovementPacket
/*  5:   */ {
/*  6:   */   protected ServerEntityPositionRotationPacket()
/*  7:   */   {
/*  8: 6 */     this.pos = true;
/*  9: 7 */     this.rot = true;
/* 10:   */   }
/* 11:   */   
/* 12:   */   public ServerEntityPositionRotationPacket(int entityId, double moveX, double moveY, double moveZ, float yaw, float pitch)
/* 13:   */   {
/* 14:11 */     super(entityId);
/* 15:12 */     this.pos = true;
/* 16:13 */     this.rot = true;
/* 17:14 */     this.moveX = moveX;
/* 18:15 */     this.moveY = moveY;
/* 19:16 */     this.moveZ = moveZ;
/* 20:17 */     this.yaw = yaw;
/* 21:18 */     this.pitch = pitch;
/* 22:   */   }
/* 23:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityPositionRotationPacket
 * JD-Core Version:    0.7.0.1
 */