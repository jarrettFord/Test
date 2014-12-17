/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.client.player;
/*  2:   */ 
/*  3:   */ public class ClientPlayerPositionPacket
/*  4:   */   extends ClientPlayerMovementPacket
/*  5:   */ {
/*  6:   */   protected ClientPlayerPositionPacket()
/*  7:   */   {
/*  8: 6 */     this.pos = true;
/*  9:   */   }
/* 10:   */   
/* 11:   */   public ClientPlayerPositionPacket(boolean onGround, double x, double feetY, double headY, double z)
/* 12:   */   {
/* 13:10 */     super(onGround);
/* 14:11 */     this.pos = true;
/* 15:12 */     this.x = x;
/* 16:13 */     this.feetY = feetY;
/* 17:14 */     this.headY = headY;
/* 18:15 */     this.z = z;
/* 19:   */   }
/* 20:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.client.player.ClientPlayerPositionPacket
 * JD-Core Version:    0.7.0.1
 */