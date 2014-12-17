/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server.entity.player;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ServerSetExperiencePacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private float experience;
/* 12:   */   private int level;
/* 13:   */   private int totalExperience;
/* 14:   */   
/* 15:   */   private ServerSetExperiencePacket() {}
/* 16:   */   
/* 17:   */   public ServerSetExperiencePacket(float experience, int level, int totalExperience)
/* 18:   */   {
/* 19:20 */     this.experience = experience;
/* 20:21 */     this.level = level;
/* 21:22 */     this.totalExperience = totalExperience;
/* 22:   */   }
/* 23:   */   
/* 24:   */   public float getSlot()
/* 25:   */   {
/* 26:26 */     return this.experience;
/* 27:   */   }
/* 28:   */   
/* 29:   */   public int getLevel()
/* 30:   */   {
/* 31:30 */     return this.level;
/* 32:   */   }
/* 33:   */   
/* 34:   */   public int getTotalExperience()
/* 35:   */   {
/* 36:34 */     return this.totalExperience;
/* 37:   */   }
/* 38:   */   
/* 39:   */   public void read(NetInput in)
/* 40:   */     throws IOException
/* 41:   */   {
/* 42:39 */     this.experience = in.readFloat();
/* 43:40 */     this.level = in.readShort();
/* 44:41 */     this.totalExperience = in.readShort();
/* 45:   */   }
/* 46:   */   
/* 47:   */   public void write(NetOutput out)
/* 48:   */     throws IOException
/* 49:   */   {
/* 50:46 */     out.writeFloat(this.experience);
/* 51:47 */     out.writeShort(this.level);
/* 52:48 */     out.writeShort(this.totalExperience);
/* 53:   */   }
/* 54:   */   
/* 55:   */   public boolean isPriority()
/* 56:   */   {
/* 57:53 */     return false;
/* 58:   */   }
/* 59:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.entity.player.ServerSetExperiencePacket
 * JD-Core Version:    0.7.0.1
 */