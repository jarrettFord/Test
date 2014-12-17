/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.client.player;
/*  2:   */ 
/*  3:   */ public class ClientPlayerRotationPacket
/*  4:   */   extends ClientPlayerMovementPacket
/*  5:   */ {
/*  6:   */   protected ClientPlayerRotationPacket()
/*  7:   */   {
/*  8: 6 */     this.rot = true;
/*  9:   */   }
/* 10:   */   
/* 11:   */   public ClientPlayerRotationPacket(boolean onGround, float yaw, float pitch)
/* 12:   */   {
/* 13:10 */     super(onGround);
/* 14:11 */     this.rot = true;
/* 15:12 */     this.yaw = yaw;
/* 16:13 */     this.pitch = pitch;
/* 17:   */   }
/* 18:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.client.player.ClientPlayerRotationPacket
 * JD-Core Version:    0.7.0.1
 */