/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server.entity;
/*  2:   */ 
/*  3:   */ public class ServerEntityPositionPacket
/*  4:   */   extends ServerEntityMovementPacket
/*  5:   */ {
/*  6:   */   protected ServerEntityPositionPacket()
/*  7:   */   {
/*  8: 6 */     this.pos = true;
/*  9:   */   }
/* 10:   */   
/* 11:   */   public ServerEntityPositionPacket(int entityId, double moveX, double moveY, double moveZ)
/* 12:   */   {
/* 13:10 */     super(entityId);
/* 14:11 */     this.pos = true;
/* 15:12 */     this.moveX = moveX;
/* 16:13 */     this.moveY = moveY;
/* 17:14 */     this.moveZ = moveZ;
/* 18:   */   }
/* 19:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityPositionPacket
 * JD-Core Version:    0.7.0.1
 */