/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server.entity;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ServerEntityRemoveEffectPacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private int entityId;
/* 12:   */   private Effect effect;
/* 13:   */   
/* 14:   */   private ServerEntityRemoveEffectPacket() {}
/* 15:   */   
/* 16:   */   public ServerEntityRemoveEffectPacket(int entityId, Effect effect)
/* 17:   */   {
/* 18:19 */     this.entityId = entityId;
/* 19:20 */     this.effect = effect;
/* 20:   */   }
/* 21:   */   
/* 22:   */   public int getEntityId()
/* 23:   */   {
/* 24:24 */     return this.entityId;
/* 25:   */   }
/* 26:   */   
/* 27:   */   public Effect getEffect()
/* 28:   */   {
/* 29:28 */     return this.effect;
/* 30:   */   }
/* 31:   */   
/* 32:   */   public void read(NetInput in)
/* 33:   */     throws IOException
/* 34:   */   {
/* 35:33 */     this.entityId = in.readInt();
/* 36:34 */     this.effect = Effect.values()[(in.readByte() - 1)];
/* 37:   */   }
/* 38:   */   
/* 39:   */   public void write(NetOutput out)
/* 40:   */     throws IOException
/* 41:   */   {
/* 42:39 */     out.writeInt(this.entityId);
/* 43:40 */     out.writeByte(this.effect.ordinal() + 1);
/* 44:   */   }
/* 45:   */   
/* 46:   */   public boolean isPriority()
/* 47:   */   {
/* 48:45 */     return false;
/* 49:   */   }
/* 50:   */   
/* 51:   */   public static enum Effect
/* 52:   */   {
/* 53:49 */     SPEED,  SLOWNESS,  DIG_SPEED,  DIG_SLOWNESS,  DAMAGE_BOOST,  HEAL,  DAMAGE,  ENHANCED_JUMP,  CONFUSION,  REGENERATION,  RESISTANCE,  FIRE_RESISTANCE,  WATER_BREATHING,  INVISIBILITY,  BLINDNESS,  NIGHT_VISION,  HUNGER,  WEAKNESS,  POISON,  WITHER_EFFECT,  HEALTH_BOOST,  ABSORPTION,  SATURATION;
/* 54:   */   }
/* 55:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.entity.ServerEntityRemoveEffectPacket
 * JD-Core Version:    0.7.0.1
 */