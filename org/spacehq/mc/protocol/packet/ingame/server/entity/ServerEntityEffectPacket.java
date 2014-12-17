/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server.entity;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ServerEntityEffectPacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private int entityId;
/* 12:   */   private Effect effect;
/* 13:   */   private int amplifier;
/* 14:   */   private int duration;
/* 15:   */   
/* 16:   */   private ServerEntityEffectPacket() {}
/* 17:   */   
/* 18:   */   public ServerEntityEffectPacket(int entityId, Effect effect, int amplifier, int duration)
/* 19:   */   {
/* 20:21 */     this.entityId = entityId;
/* 21:22 */     this.effect = effect;
/* 22:23 */     this.amplifier = amplifier;
/* 23:24 */     this.duration = duration;
/* 24:   */   }
/* 25:   */   
/* 26:   */   public int getEntityId()
/* 27:   */   {
/* 28:28 */     return this.entityId;
/* 29:   */   }
/* 30:   */   
/* 31:   */   public Effect getEffect()
/* 32:   */   {
/* 33:32 */     return this.effect;
/* 34:   */   }
/* 35:   */   
/* 36:   */   public int getAmplifier()
/* 37:   */   {
/* 38:36 */     return this.amplifier;
/* 39:   */   }
/* 40:   */   
/* 41:   */   public int getDuration()
/* 42:   */   {
/* 43:40 */     return this.duration;
/* 44:   */   }
/* 45:   */   
/* 46:   */   public void read(NetInput in)
/* 47:   */     throws IOException
/* 48:   */   {
/* 49:45 */     this.entityId = in.readInt();
/* 50:46 */     this.effect = Effect.values()[(in.readByte() - 1)];
/* 51:47 */     this.amplifier = in.readByte();
/* 52:48 */     this.duration = in.readShort();
/* 53:   */   }
/* 54:   */   
/* 55:   */   public void write(NetOutput out)
/* 56:   */     throws IOException
/* 57:   */   {
/* 58:53 */     out.writeInt(this.entityId);
/* 59:54 */     out.writeByte(this.effect.ordinal() + 1);
/* 60:55 */     out.writeByte(this.amplifier);
/* 61:56 */     out.writeShort(this.duration);
/* 62:   */   }
/* 63:   */   
/* 64:   */   public boolean isPriority()
/* 65:   */   {
/* 66:61 */     return false;
/* 67:   */   }
/* 68:   */   
/* 69:   */   public static enum Effect
/* 70:   */   {
/* 71:65 */     SPEED,  SLOWNESS,  DIG_SPEED,  DIG_SLOWNESS,  DAMAGE_BOOST,  HEAL,  DAMAGE,  ENHANCED_JUMP,  CONFUSION,  REGENERATION,  RESISTANCE,  FIRE_RESISTANCE,  WATER_BREATHING,  INVISIBILITY,  BLINDNESS,  NIGHT_VISION,  HUNGER,  WEAKNESS,  POISON,  WITHER_EFFECT,  HEALTH_BOOST,  ABSORPTION,  SATURATION;
/* 72:   */   }
/* 73:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityEffectPacket
 * JD-Core Version:    0.7.0.1
 */