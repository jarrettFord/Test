/*   1:    */ package org.spacehq.mc.protocol.packet.ingame.server.entity.spawn;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import org.spacehq.mc.protocol.data.game.EntityMetadata;
/*   5:    */ import org.spacehq.mc.protocol.util.NetUtil;
/*   6:    */ import org.spacehq.packetlib.io.NetInput;
/*   7:    */ import org.spacehq.packetlib.io.NetOutput;
/*   8:    */ import org.spacehq.packetlib.packet.Packet;
/*   9:    */ 
/*  10:    */ public class ServerSpawnMobPacket
/*  11:    */   implements Packet
/*  12:    */ {
/*  13:    */   private int entityId;
/*  14:    */   private Type type;
/*  15:    */   private double x;
/*  16:    */   private double y;
/*  17:    */   private double z;
/*  18:    */   private float pitch;
/*  19:    */   private float yaw;
/*  20:    */   private float headYaw;
/*  21:    */   private double motX;
/*  22:    */   private double motY;
/*  23:    */   private double motZ;
/*  24:    */   private EntityMetadata[] metadata;
/*  25:    */   
/*  26:    */   private ServerSpawnMobPacket() {}
/*  27:    */   
/*  28:    */   public ServerSpawnMobPacket(int entityId, Type type, double x, double y, double z, float yaw, float pitch, float headYaw, double motX, double motY, double motZ, EntityMetadata[] metadata)
/*  29:    */   {
/*  30: 31 */     this.entityId = entityId;
/*  31: 32 */     this.type = type;
/*  32: 33 */     this.x = x;
/*  33: 34 */     this.y = y;
/*  34: 35 */     this.z = z;
/*  35: 36 */     this.yaw = yaw;
/*  36: 37 */     this.pitch = pitch;
/*  37: 38 */     this.headYaw = headYaw;
/*  38: 39 */     this.motX = motX;
/*  39: 40 */     this.motY = motY;
/*  40: 41 */     this.motZ = motZ;
/*  41: 42 */     this.metadata = metadata;
/*  42:    */   }
/*  43:    */   
/*  44:    */   public int getEntityId()
/*  45:    */   {
/*  46: 46 */     return this.entityId;
/*  47:    */   }
/*  48:    */   
/*  49:    */   public Type getType()
/*  50:    */   {
/*  51: 50 */     return this.type;
/*  52:    */   }
/*  53:    */   
/*  54:    */   public double getX()
/*  55:    */   {
/*  56: 54 */     return this.x;
/*  57:    */   }
/*  58:    */   
/*  59:    */   public double getY()
/*  60:    */   {
/*  61: 58 */     return this.y;
/*  62:    */   }
/*  63:    */   
/*  64:    */   public double getZ()
/*  65:    */   {
/*  66: 62 */     return this.z;
/*  67:    */   }
/*  68:    */   
/*  69:    */   public float getYaw()
/*  70:    */   {
/*  71: 66 */     return this.yaw;
/*  72:    */   }
/*  73:    */   
/*  74:    */   public float getPitch()
/*  75:    */   {
/*  76: 70 */     return this.pitch;
/*  77:    */   }
/*  78:    */   
/*  79:    */   public float getHeadYaw()
/*  80:    */   {
/*  81: 74 */     return this.headYaw;
/*  82:    */   }
/*  83:    */   
/*  84:    */   public double getMotionX()
/*  85:    */   {
/*  86: 78 */     return this.motX;
/*  87:    */   }
/*  88:    */   
/*  89:    */   public double getMotionY()
/*  90:    */   {
/*  91: 82 */     return this.motY;
/*  92:    */   }
/*  93:    */   
/*  94:    */   public double getMotionZ()
/*  95:    */   {
/*  96: 86 */     return this.motZ;
/*  97:    */   }
/*  98:    */   
/*  99:    */   public EntityMetadata[] getMetadata()
/* 100:    */   {
/* 101: 90 */     return this.metadata;
/* 102:    */   }
/* 103:    */   
/* 104:    */   public void read(NetInput in)
/* 105:    */     throws IOException
/* 106:    */   {
/* 107: 95 */     this.entityId = in.readVarInt();
/* 108: 96 */     this.type = idToType(in.readByte());
/* 109: 97 */     this.x = (in.readInt() / 32.0D);
/* 110: 98 */     this.y = (in.readInt() / 32.0D);
/* 111: 99 */     this.z = (in.readInt() / 32.0D);
/* 112:100 */     this.yaw = (in.readByte() * 360 / 256.0F);
/* 113:101 */     this.pitch = (in.readByte() * 360 / 256.0F);
/* 114:102 */     this.headYaw = (in.readByte() * 360 / 256.0F);
/* 115:103 */     this.motX = (in.readShort() / 8000.0D);
/* 116:104 */     this.motY = (in.readShort() / 8000.0D);
/* 117:105 */     this.motZ = (in.readShort() / 8000.0D);
/* 118:106 */     this.metadata = NetUtil.readEntityMetadata(in);
/* 119:    */   }
/* 120:    */   
/* 121:    */   public void write(NetOutput out)
/* 122:    */     throws IOException
/* 123:    */   {
/* 124:111 */     out.writeVarInt(this.entityId);
/* 125:112 */     out.writeByte(typeToId(this.type));
/* 126:113 */     out.writeInt((int)(this.x * 32.0D));
/* 127:114 */     out.writeInt((int)(this.y * 32.0D));
/* 128:115 */     out.writeInt((int)(this.z * 32.0D));
/* 129:116 */     out.writeByte((byte)(int)(this.yaw * 256.0F / 360.0F));
/* 130:117 */     out.writeByte((byte)(int)(this.pitch * 256.0F / 360.0F));
/* 131:118 */     out.writeByte((byte)(int)(this.headYaw * 256.0F / 360.0F));
/* 132:119 */     out.writeShort((int)(this.motX * 8000.0D));
/* 133:120 */     out.writeShort((int)(this.motY * 8000.0D));
/* 134:121 */     out.writeShort((int)(this.motZ * 8000.0D));
/* 135:122 */     NetUtil.writeEntityMetadata(out, this.metadata);
/* 136:    */   }
/* 137:    */   
/* 138:    */   public boolean isPriority()
/* 139:    */   {
/* 140:127 */     return false;
/* 141:    */   }
/* 142:    */   
/* 143:    */   private static Type idToType(byte id)
/* 144:    */     throws IOException
/* 145:    */   {
/* 146:131 */     switch (id)
/* 147:    */     {
/* 148:    */     case 50: 
/* 149:133 */       return Type.CREEPER;
/* 150:    */     case 51: 
/* 151:135 */       return Type.SKELETON;
/* 152:    */     case 52: 
/* 153:137 */       return Type.SPIDER;
/* 154:    */     case 53: 
/* 155:139 */       return Type.GIANT_ZOMBIE;
/* 156:    */     case 54: 
/* 157:141 */       return Type.ZOMBIE;
/* 158:    */     case 55: 
/* 159:143 */       return Type.SLIME;
/* 160:    */     case 56: 
/* 161:145 */       return Type.GHAST;
/* 162:    */     case 57: 
/* 163:147 */       return Type.ZOMBIE_PIGMAN;
/* 164:    */     case 58: 
/* 165:149 */       return Type.ENDERMAN;
/* 166:    */     case 59: 
/* 167:151 */       return Type.CAVE_SPIDER;
/* 168:    */     case 60: 
/* 169:153 */       return Type.SILVERFISH;
/* 170:    */     case 61: 
/* 171:155 */       return Type.BLAZE;
/* 172:    */     case 62: 
/* 173:157 */       return Type.MAGMA_CUBE;
/* 174:    */     case 63: 
/* 175:159 */       return Type.ENDER_DRAGON;
/* 176:    */     case 64: 
/* 177:161 */       return Type.WITHER;
/* 178:    */     case 65: 
/* 179:163 */       return Type.BAT;
/* 180:    */     case 66: 
/* 181:165 */       return Type.WITCH;
/* 182:    */     case 90: 
/* 183:167 */       return Type.PIG;
/* 184:    */     case 91: 
/* 185:169 */       return Type.SHEEP;
/* 186:    */     case 92: 
/* 187:171 */       return Type.COW;
/* 188:    */     case 93: 
/* 189:173 */       return Type.CHICKEN;
/* 190:    */     case 94: 
/* 191:175 */       return Type.SQUID;
/* 192:    */     case 95: 
/* 193:177 */       return Type.WOLF;
/* 194:    */     case 96: 
/* 195:179 */       return Type.MOOSHROOM;
/* 196:    */     case 97: 
/* 197:181 */       return Type.SNOWMAN;
/* 198:    */     case 98: 
/* 199:183 */       return Type.OCELOT;
/* 200:    */     case 99: 
/* 201:185 */       return Type.IRON_GOLEM;
/* 202:    */     case 100: 
/* 203:187 */       return Type.HORSE;
/* 204:    */     case 120: 
/* 205:189 */       return Type.VILLAGER;
/* 206:    */     }
/* 207:191 */     throw new IOException("Unknown mob type id: " + id);
/* 208:    */   }
/* 209:    */   
/* 210:    */   private static byte typeToId(Type type)
/* 211:    */     throws IOException
/* 212:    */   {
/* 213:196 */     switch (type)
/* 214:    */     {
/* 215:    */     case BAT: 
/* 216:198 */       return 50;
/* 217:    */     case BLAZE: 
/* 218:200 */       return 51;
/* 219:    */     case CAVE_SPIDER: 
/* 220:202 */       return 52;
/* 221:    */     case CHICKEN: 
/* 222:204 */       return 53;
/* 223:    */     case COW: 
/* 224:206 */       return 54;
/* 225:    */     case CREEPER: 
/* 226:208 */       return 55;
/* 227:    */     case ENDERMAN: 
/* 228:210 */       return 56;
/* 229:    */     case ENDER_DRAGON: 
/* 230:212 */       return 57;
/* 231:    */     case GHAST: 
/* 232:214 */       return 58;
/* 233:    */     case GIANT_ZOMBIE: 
/* 234:216 */       return 59;
/* 235:    */     case HORSE: 
/* 236:218 */       return 60;
/* 237:    */     case IRON_GOLEM: 
/* 238:220 */       return 61;
/* 239:    */     case MAGMA_CUBE: 
/* 240:222 */       return 62;
/* 241:    */     case MOOSHROOM: 
/* 242:224 */       return 63;
/* 243:    */     case OCELOT: 
/* 244:226 */       return 64;
/* 245:    */     case PIG: 
/* 246:228 */       return 65;
/* 247:    */     case SHEEP: 
/* 248:230 */       return 66;
/* 249:    */     case SILVERFISH: 
/* 250:232 */       return 90;
/* 251:    */     case SKELETON: 
/* 252:234 */       return 91;
/* 253:    */     case SLIME: 
/* 254:236 */       return 92;
/* 255:    */     case SNOWMAN: 
/* 256:238 */       return 93;
/* 257:    */     case SPIDER: 
/* 258:240 */       return 94;
/* 259:    */     case SQUID: 
/* 260:242 */       return 95;
/* 261:    */     case VILLAGER: 
/* 262:244 */       return 96;
/* 263:    */     case WITCH: 
/* 264:246 */       return 97;
/* 265:    */     case WITHER: 
/* 266:248 */       return 98;
/* 267:    */     case WOLF: 
/* 268:250 */       return 99;
/* 269:    */     case ZOMBIE: 
/* 270:252 */       return 100;
/* 271:    */     case ZOMBIE_PIGMAN: 
/* 272:254 */       return 120;
/* 273:    */     }
/* 274:256 */     throw new IOException("Unmapped mob type: " + type);
/* 275:    */   }
/* 276:    */   
/* 277:    */   public static enum Type
/* 278:    */   {
/* 279:261 */     CREEPER,  SKELETON,  SPIDER,  GIANT_ZOMBIE,  ZOMBIE,  SLIME,  GHAST,  ZOMBIE_PIGMAN,  ENDERMAN,  CAVE_SPIDER,  SILVERFISH,  BLAZE,  MAGMA_CUBE,  ENDER_DRAGON,  WITHER,  BAT,  WITCH,  PIG,  SHEEP,  COW,  CHICKEN,  SQUID,  WOLF,  MOOSHROOM,  SNOWMAN,  OCELOT,  IRON_GOLEM,  HORSE,  VILLAGER;
/* 280:    */   }
/* 281:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnMobPacket
 * JD-Core Version:    0.7.0.1
 */