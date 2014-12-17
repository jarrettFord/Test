/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.client.player;
/*  2:   */ 
/*  3:   */ public class ClientPlayerPositionRotationPacket
/*  4:   */   extends ClientPlayerMovementPacket
/*  5:   */ {
/*  6:   */   protected ClientPlayerPositionRotationPacket()
/*  7:   */   {
/*  8: 6 */     this.pos = true;
/*  9: 7 */     this.rot = true;
/* 10:   */   }
/* 11:   */   
/* 12:   */   public ClientPlayerPositionRotationPacket(boolean onGround, double x, double feetY, double headY, double z, float yaw, float pitch)
/* 13:   */   {
/* 14:11 */     super(onGround);
/* 15:12 */     this.pos = true;
/* 16:13 */     this.rot = true;
/* 17:14 */     this.x = x;
/* 18:15 */     this.feetY = feetY;
/* 19:16 */     this.headY = headY;
/* 20:17 */     this.z = z;
/* 21:18 */     this.yaw = yaw;
/* 22:19 */     this.pitch = pitch;
/* 23:   */   }
/* 24:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.client.player.ClientPlayerPositionRotationPacket
 * JD-Core Version:    0.7.0.1
 */