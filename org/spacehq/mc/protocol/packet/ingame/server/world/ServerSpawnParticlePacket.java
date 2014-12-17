/*   1:    */ package org.spacehq.mc.protocol.packet.ingame.server.world;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import org.spacehq.packetlib.io.NetInput;
/*   5:    */ import org.spacehq.packetlib.io.NetOutput;
/*   6:    */ import org.spacehq.packetlib.packet.Packet;
/*   7:    */ 
/*   8:    */ public class ServerSpawnParticlePacket
/*   9:    */   implements Packet
/*  10:    */ {
/*  11:    */   private String particle;
/*  12:    */   private float x;
/*  13:    */   private float y;
/*  14:    */   private float z;
/*  15:    */   private float offsetX;
/*  16:    */   private float offsetY;
/*  17:    */   private float offsetZ;
/*  18:    */   private float velocityOffset;
/*  19:    */   private int amount;
/*  20:    */   
/*  21:    */   private ServerSpawnParticlePacket() {}
/*  22:    */   
/*  23:    */   public ServerSpawnParticlePacket(String particle, float x, float y, float z, float offsetX, float offsetY, float offsetZ, float velocityOffset, int amount)
/*  24:    */   {
/*  25: 26 */     this.particle = particle;
/*  26: 27 */     this.x = x;
/*  27: 28 */     this.y = y;
/*  28: 29 */     this.z = z;
/*  29: 30 */     this.offsetX = offsetX;
/*  30: 31 */     this.offsetY = offsetY;
/*  31: 32 */     this.offsetZ = offsetZ;
/*  32: 33 */     this.velocityOffset = velocityOffset;
/*  33: 34 */     this.amount = amount;
/*  34:    */   }
/*  35:    */   
/*  36:    */   public String getParticle()
/*  37:    */   {
/*  38: 38 */     return this.particle;
/*  39:    */   }
/*  40:    */   
/*  41:    */   public float getX()
/*  42:    */   {
/*  43: 42 */     return this.x;
/*  44:    */   }
/*  45:    */   
/*  46:    */   public float getY()
/*  47:    */   {
/*  48: 46 */     return this.y;
/*  49:    */   }
/*  50:    */   
/*  51:    */   public float getZ()
/*  52:    */   {
/*  53: 50 */     return this.z;
/*  54:    */   }
/*  55:    */   
/*  56:    */   public float getOffsetX()
/*  57:    */   {
/*  58: 54 */     return this.offsetX;
/*  59:    */   }
/*  60:    */   
/*  61:    */   public float getOffsetY()
/*  62:    */   {
/*  63: 58 */     return this.offsetY;
/*  64:    */   }
/*  65:    */   
/*  66:    */   public float getOffsetZ()
/*  67:    */   {
/*  68: 62 */     return this.offsetZ;
/*  69:    */   }
/*  70:    */   
/*  71:    */   public float getVelocityOffset()
/*  72:    */   {
/*  73: 66 */     return this.velocityOffset;
/*  74:    */   }
/*  75:    */   
/*  76:    */   public int getAmount()
/*  77:    */   {
/*  78: 70 */     return this.amount;
/*  79:    */   }
/*  80:    */   
/*  81:    */   public void read(NetInput in)
/*  82:    */     throws IOException
/*  83:    */   {
/*  84: 75 */     this.particle = in.readString();
/*  85: 76 */     this.x = in.readFloat();
/*  86: 77 */     this.y = in.readFloat();
/*  87: 78 */     this.z = in.readFloat();
/*  88: 79 */     this.offsetX = in.readFloat();
/*  89: 80 */     this.offsetY = in.readFloat();
/*  90: 81 */     this.offsetZ = in.readFloat();
/*  91: 82 */     this.velocityOffset = in.readFloat();
/*  92: 83 */     this.amount = in.readInt();
/*  93:    */   }
/*  94:    */   
/*  95:    */   public void write(NetOutput out)
/*  96:    */     throws IOException
/*  97:    */   {
/*  98: 88 */     out.writeString(this.particle);
/*  99: 89 */     out.writeFloat(this.x);
/* 100: 90 */     out.writeFloat(this.y);
/* 101: 91 */     out.writeFloat(this.z);
/* 102: 92 */     out.writeFloat(this.offsetX);
/* 103: 93 */     out.writeFloat(this.offsetY);
/* 104: 94 */     out.writeFloat(this.offsetZ);
/* 105: 95 */     out.writeFloat(this.velocityOffset);
/* 106: 96 */     out.writeInt(this.amount);
/* 107:    */   }
/* 108:    */   
/* 109:    */   public boolean isPriority()
/* 110:    */   {
/* 111:101 */     return false;
/* 112:    */   }
/* 113:    */   
/* 114:    */   public static class Particle
/* 115:    */   {
/* 116:    */     public static final String HUGE_EXPLOSION = "hugeexplosion";
/* 117:    */     public static final String LARGE_EXPLOSION = "largeexplode";
/* 118:    */     public static final String FIREWORKS_SPARK = "fireworksSpark";
/* 119:    */     public static final String LIQUID_PARTICLES = "suspended";
/* 120:    */     public static final String DEPTH_PARTICLES = "depthsuspend";
/* 121:    */     public static final String MYCELIUM_PARTICLES = "townaura";
/* 122:    */     public static final String CRITICAL_HIT = "crit";
/* 123:    */     public static final String ENCHANTED_CRITICAL_HIT = "magicCrit";
/* 124:    */     public static final String SMOKE = "smoke";
/* 125:    */     public static final String MOB_POTION_EFFECT = "mobSpell";
/* 126:    */     public static final String MOB_POTION_EFFECT_AMBIENT = "mobSpellAmbient";
/* 127:    */     public static final String POTION_EFFECT = "spell";
/* 128:    */     public static final String INSTANT_POTION_EFFECT = "instantSpell";
/* 129:    */     public static final String WITCH_PARTICLES = "witchMagic";
/* 130:    */     public static final String NOTE = "note";
/* 131:    */     public static final String PORTAL = "portal";
/* 132:    */     public static final String ENCHANTMENT_TABLE_LETTERS = "enchantmenttable";
/* 133:    */     public static final String EXPLOSION = "explode";
/* 134:    */     public static final String FLAME = "flame";
/* 135:    */     public static final String LAVA_PARTICLES = "lava";
/* 136:    */     public static final String FOOTSTEP_PARTICLES = "footstep";
/* 137:    */     public static final String SPLASH = "splash";
/* 138:    */     public static final String FISH_HOOK_WAKE = "wake";
/* 139:    */     public static final String LARGE_SMOKE = "largesmoke";
/* 140:    */     public static final String CLOUD = "cloud";
/* 141:    */     public static final String REDSTONE_PARTICLES = "reddust";
/* 142:    */     public static final String BREAKING_SNOWBALL = "snowballpoof";
/* 143:    */     public static final String DRIP_WATER = "dripWater";
/* 144:    */     public static final String DRIP_LAVA = "dripLava";
/* 145:    */     public static final String SHOVEL_SNOW = "snowshovel";
/* 146:    */     public static final String SLIME = "slime";
/* 147:    */     public static final String HEART = "heart";
/* 148:    */     public static final String ANGRY_VILLAGER = "angryVillager";
/* 149:    */     public static final String HAPPY_VILLAGER = "happyVillager";
/* 150:    */     private static final String ITEM_BREAK_PREFIX = "iconcrack_";
/* 151:    */     private static final String BLOCK_BREAK_PREFIX = "blockcrack_";
/* 152:    */     private static final String BLOCK_IMPACT_PREFIX = "blockdust_";
/* 153:    */     
/* 154:    */     public static final String ITEM_BREAK_PARTICLES(int id)
/* 155:    */     {
/* 156:145 */       return ITEM_BREAK_PARTICLES(id, -1);
/* 157:    */     }
/* 158:    */     
/* 159:    */     public static final String ITEM_BREAK_PARTICLES(int id, int data)
/* 160:    */     {
/* 161:149 */       return "iconcrack_" + id + (data != -1 ? "_" + data : "");
/* 162:    */     }
/* 163:    */     
/* 164:    */     public static final String BLOCK_BREAK_PARTICLES(int id)
/* 165:    */     {
/* 166:153 */       return BLOCK_BREAK_PARTICLES(id, -1);
/* 167:    */     }
/* 168:    */     
/* 169:    */     public static final String BLOCK_BREAK_PARTICLES(int id, int data)
/* 170:    */     {
/* 171:157 */       return "blockcrack_" + id + (data != -1 ? "_" + data : "");
/* 172:    */     }
/* 173:    */     
/* 174:    */     public static final String BLOCK_IMPACT_PARTICLES(int id)
/* 175:    */     {
/* 176:161 */       return BLOCK_IMPACT_PARTICLES(id, -1);
/* 177:    */     }
/* 178:    */     
/* 179:    */     public static final String BLOCK_IMPACT_PARTICLES(int id, int data)
/* 180:    */     {
/* 181:165 */       return "blockdust_" + id + (data != -1 ? "_" + data : "");
/* 182:    */     }
/* 183:    */   }
/* 184:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.world.ServerSpawnParticlePacket
 * JD-Core Version:    0.7.0.1
 */