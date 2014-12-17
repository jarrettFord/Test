/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.client.player;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ClientPlayerAbilitiesPacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private boolean invincible;
/* 12:   */   private boolean canFly;
/* 13:   */   private boolean flying;
/* 14:   */   private boolean creative;
/* 15:   */   private float flySpeed;
/* 16:   */   private float walkSpeed;
/* 17:   */   
/* 18:   */   private ClientPlayerAbilitiesPacket() {}
/* 19:   */   
/* 20:   */   public ClientPlayerAbilitiesPacket(boolean invincible, boolean canFly, boolean flying, boolean creative, float flySpeed, float walkSpeed)
/* 21:   */   {
/* 22:23 */     this.invincible = invincible;
/* 23:24 */     this.canFly = canFly;
/* 24:25 */     this.flying = flying;
/* 25:26 */     this.creative = creative;
/* 26:27 */     this.flySpeed = flySpeed;
/* 27:28 */     this.walkSpeed = walkSpeed;
/* 28:   */   }
/* 29:   */   
/* 30:   */   public boolean getInvincible()
/* 31:   */   {
/* 32:32 */     return this.invincible;
/* 33:   */   }
/* 34:   */   
/* 35:   */   public boolean getCanFly()
/* 36:   */   {
/* 37:36 */     return this.canFly;
/* 38:   */   }
/* 39:   */   
/* 40:   */   public boolean getFlying()
/* 41:   */   {
/* 42:40 */     return this.flying;
/* 43:   */   }
/* 44:   */   
/* 45:   */   public boolean getCreative()
/* 46:   */   {
/* 47:44 */     return this.creative;
/* 48:   */   }
/* 49:   */   
/* 50:   */   public float getFlySpeed()
/* 51:   */   {
/* 52:48 */     return this.flySpeed;
/* 53:   */   }
/* 54:   */   
/* 55:   */   public float getWalkSpeed()
/* 56:   */   {
/* 57:52 */     return this.walkSpeed;
/* 58:   */   }
/* 59:   */   
/* 60:   */   public void read(NetInput in)
/* 61:   */     throws IOException
/* 62:   */   {
/* 63:57 */     byte flags = in.readByte();
/* 64:58 */     this.invincible = ((flags & 0x1) > 0);
/* 65:59 */     this.canFly = ((flags & 0x2) > 0);
/* 66:60 */     this.flying = ((flags & 0x4) > 0);
/* 67:61 */     this.creative = ((flags & 0x8) > 0);
/* 68:62 */     this.flySpeed = in.readFloat();
/* 69:63 */     this.walkSpeed = in.readFloat();
/* 70:   */   }
/* 71:   */   
/* 72:   */   public void write(NetOutput out)
/* 73:   */     throws IOException
/* 74:   */   {
/* 75:68 */     byte flags = 0;
/* 76:69 */     if (this.invincible) {
/* 77:70 */       flags = (byte)(flags | 0x1);
/* 78:   */     }
/* 79:73 */     if (this.canFly) {
/* 80:74 */       flags = (byte)(flags | 0x2);
/* 81:   */     }
/* 82:77 */     if (this.flying) {
/* 83:78 */       flags = (byte)(flags | 0x4);
/* 84:   */     }
/* 85:81 */     if (this.creative) {
/* 86:82 */       flags = (byte)(flags | 0x8);
/* 87:   */     }
/* 88:85 */     out.writeByte(flags);
/* 89:86 */     out.writeFloat(this.flySpeed);
/* 90:87 */     out.writeFloat(this.walkSpeed);
/* 91:   */   }
/* 92:   */   
/* 93:   */   public boolean isPriority()
/* 94:   */   {
/* 95:92 */     return false;
/* 96:   */   }
/* 97:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.client.player.ClientPlayerAbilitiesPacket
 * JD-Core Version:    0.7.0.1
 */