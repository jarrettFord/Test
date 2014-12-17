/*   1:    */ package org.spacehq.mc.protocol.packet.ingame.server.entity.spawn;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import org.spacehq.packetlib.io.NetInput;
/*   5:    */ import org.spacehq.packetlib.io.NetOutput;
/*   6:    */ import org.spacehq.packetlib.packet.Packet;
/*   7:    */ 
/*   8:    */ public class ServerSpawnObjectPacket
/*   9:    */   implements Packet
/*  10:    */ {
/*  11:    */   private int entityId;
/*  12:    */   private Type type;
/*  13:    */   private double x;
/*  14:    */   private double y;
/*  15:    */   private double z;
/*  16:    */   private float pitch;
/*  17:    */   private float yaw;
/*  18:    */   private int data;
/*  19:    */   private double motX;
/*  20:    */   private double motY;
/*  21:    */   private double motZ;
/*  22:    */   
/*  23:    */   private ServerSpawnObjectPacket() {}
/*  24:    */   
/*  25:    */   public ServerSpawnObjectPacket(int entityId, Type type, double x, double y, double z, float yaw, float pitch)
/*  26:    */   {
/*  27: 28 */     this(entityId, type, 0, x, y, z, yaw, pitch, 0.0D, 0.0D, 0.0D);
/*  28:    */   }
/*  29:    */   
/*  30:    */   public ServerSpawnObjectPacket(int entityId, Type type, int data, double x, double y, double z, float yaw, float pitch, double motX, double motY, double motZ)
/*  31:    */   {
/*  32: 32 */     this.entityId = entityId;
/*  33: 33 */     this.type = type;
/*  34: 34 */     this.data = data;
/*  35: 35 */     this.x = x;
/*  36: 36 */     this.y = y;
/*  37: 37 */     this.z = z;
/*  38: 38 */     this.yaw = yaw;
/*  39: 39 */     this.pitch = pitch;
/*  40: 40 */     this.motX = motX;
/*  41: 41 */     this.motY = motY;
/*  42: 42 */     this.motZ = motZ;
/*  43:    */   }
/*  44:    */   
/*  45:    */   public int getEntityId()
/*  46:    */   {
/*  47: 46 */     return this.entityId;
/*  48:    */   }
/*  49:    */   
/*  50:    */   public Type getType()
/*  51:    */   {
/*  52: 50 */     return this.type;
/*  53:    */   }
/*  54:    */   
/*  55:    */   public int getData()
/*  56:    */   {
/*  57: 54 */     return this.data;
/*  58:    */   }
/*  59:    */   
/*  60:    */   public double getX()
/*  61:    */   {
/*  62: 58 */     return this.x;
/*  63:    */   }
/*  64:    */   
/*  65:    */   public double getY()
/*  66:    */   {
/*  67: 62 */     return this.y;
/*  68:    */   }
/*  69:    */   
/*  70:    */   public double getZ()
/*  71:    */   {
/*  72: 66 */     return this.z;
/*  73:    */   }
/*  74:    */   
/*  75:    */   public float getYaw()
/*  76:    */   {
/*  77: 70 */     return this.yaw;
/*  78:    */   }
/*  79:    */   
/*  80:    */   public float getPitch()
/*  81:    */   {
/*  82: 74 */     return this.pitch;
/*  83:    */   }
/*  84:    */   
/*  85:    */   public double getMotionX()
/*  86:    */   {
/*  87: 78 */     return this.motX;
/*  88:    */   }
/*  89:    */   
/*  90:    */   public double getMotionY()
/*  91:    */   {
/*  92: 82 */     return this.motY;
/*  93:    */   }
/*  94:    */   
/*  95:    */   public double getMotionZ()
/*  96:    */   {
/*  97: 86 */     return this.motZ;
/*  98:    */   }
/*  99:    */   
/* 100:    */   public void read(NetInput in)
/* 101:    */     throws IOException
/* 102:    */   {
/* 103: 91 */     this.entityId = in.readVarInt();
/* 104: 92 */     this.type = idToType(in.readByte());
/* 105: 93 */     this.x = (in.readInt() / 32.0D);
/* 106: 94 */     this.y = (in.readInt() / 32.0D);
/* 107: 95 */     this.z = (in.readInt() / 32.0D);
/* 108: 96 */     this.pitch = (in.readByte() * 360 / 256.0F);
/* 109: 97 */     this.yaw = (in.readByte() * 360 / 256.0F);
/* 110: 98 */     this.data = in.readInt();
/* 111: 99 */     if (this.data > 0)
/* 112:    */     {
/* 113:100 */       this.motX = (in.readShort() / 8000.0D);
/* 114:101 */       this.motY = (in.readShort() / 8000.0D);
/* 115:102 */       this.motZ = (in.readShort() / 8000.0D);
/* 116:    */     }
/* 117:    */   }
/* 118:    */   
/* 119:    */   public void write(NetOutput out)
/* 120:    */     throws IOException
/* 121:    */   {
/* 122:108 */     out.writeVarInt(this.entityId);
/* 123:109 */     out.writeByte(typeToId(this.type));
/* 124:110 */     out.writeInt((int)(this.x * 32.0D));
/* 125:111 */     out.writeInt((int)(this.y * 32.0D));
/* 126:112 */     out.writeInt((int)(this.z * 32.0D));
/* 127:113 */     out.writeByte((byte)(int)(this.pitch * 256.0F / 360.0F));
/* 128:114 */     out.writeByte((byte)(int)(this.yaw * 256.0F / 360.0F));
/* 129:115 */     out.writeInt(this.data);
/* 130:116 */     if (this.data > 0)
/* 131:    */     {
/* 132:117 */       out.writeShort((int)(this.motX * 8000.0D));
/* 133:118 */       out.writeShort((int)(this.motY * 8000.0D));
/* 134:119 */       out.writeShort((int)(this.motZ * 8000.0D));
/* 135:    */     }
/* 136:    */   }
/* 137:    */   
/* 138:    */   public boolean isPriority()
/* 139:    */   {
/* 140:125 */     return false;
/* 141:    */   }
/* 142:    */   
/* 143:    */   private static Type idToType(byte id)
/* 144:    */     throws IOException
/* 145:    */   {
/* 146:129 */     switch (id)
/* 147:    */     {
/* 148:    */     case 1: 
/* 149:131 */       return Type.BOAT;
/* 150:    */     case 2: 
/* 151:133 */       return Type.ITEM;
/* 152:    */     case 10: 
/* 153:135 */       return Type.MINECART;
/* 154:    */     case 50: 
/* 155:137 */       return Type.PRIMED_TNT;
/* 156:    */     case 51: 
/* 157:139 */       return Type.ENDER_CRYSTAL;
/* 158:    */     case 60: 
/* 159:141 */       return Type.ARROW;
/* 160:    */     case 61: 
/* 161:143 */       return Type.SNOWBALL;
/* 162:    */     case 62: 
/* 163:145 */       return Type.EGG;
/* 164:    */     case 63: 
/* 165:147 */       return Type.GHAST_FIREBALL;
/* 166:    */     case 64: 
/* 167:149 */       return Type.BLAZE_FIREBALL;
/* 168:    */     case 65: 
/* 169:151 */       return Type.ENDER_PEARL;
/* 170:    */     case 66: 
/* 171:153 */       return Type.WITHER_HEAD_PROJECTILE;
/* 172:    */     case 70: 
/* 173:155 */       return Type.FALLING_BLOCK;
/* 174:    */     case 71: 
/* 175:157 */       return Type.ITEM_FRAME;
/* 176:    */     case 72: 
/* 177:159 */       return Type.EYE_OF_ENDER;
/* 178:    */     case 73: 
/* 179:161 */       return Type.POTION;
/* 180:    */     case 75: 
/* 181:163 */       return Type.EXP_BOTTLE;
/* 182:    */     case 76: 
/* 183:165 */       return Type.FIREWORK_ROCKET;
/* 184:    */     case 77: 
/* 185:167 */       return Type.LEASH_KNOT;
/* 186:    */     case 90: 
/* 187:169 */       return Type.FISH_HOOK;
/* 188:    */     }
/* 189:171 */     throw new IOException("Unknown object type id: " + id);
/* 190:    */   }
/* 191:    */   
/* 192:    */   private static byte typeToId(Type type)
/* 193:    */     throws IOException
/* 194:    */   {
/* 195:176 */     switch (type)
/* 196:    */     {
/* 197:    */     case ARROW: 
/* 198:178 */       return 1;
/* 199:    */     case BLAZE_FIREBALL: 
/* 200:180 */       return 2;
/* 201:    */     case BOAT: 
/* 202:182 */       return 10;
/* 203:    */     case EGG: 
/* 204:184 */       return 50;
/* 205:    */     case ENDER_CRYSTAL: 
/* 206:186 */       return 51;
/* 207:    */     case ENDER_PEARL: 
/* 208:188 */       return 60;
/* 209:    */     case EXP_BOTTLE: 
/* 210:190 */       return 61;
/* 211:    */     case EYE_OF_ENDER: 
/* 212:192 */       return 62;
/* 213:    */     case FALLING_BLOCK: 
/* 214:194 */       return 63;
/* 215:    */     case FIREWORK_ROCKET: 
/* 216:196 */       return 64;
/* 217:    */     case FISH_HOOK: 
/* 218:198 */       return 65;
/* 219:    */     case GHAST_FIREBALL: 
/* 220:200 */       return 66;
/* 221:    */     case ITEM: 
/* 222:202 */       return 70;
/* 223:    */     case ITEM_FRAME: 
/* 224:204 */       return 71;
/* 225:    */     case LEASH_KNOT: 
/* 226:206 */       return 72;
/* 227:    */     case MINECART: 
/* 228:208 */       return 73;
/* 229:    */     case POTION: 
/* 230:210 */       return 75;
/* 231:    */     case PRIMED_TNT: 
/* 232:212 */       return 76;
/* 233:    */     case SNOWBALL: 
/* 234:214 */       return 77;
/* 235:    */     case WITHER_HEAD_PROJECTILE: 
/* 236:216 */       return 90;
/* 237:    */     }
/* 238:218 */     throw new IOException("Unmapped object type: " + type);
/* 239:    */   }
/* 240:    */   
/* 241:    */   public static enum Type
/* 242:    */   {
/* 243:223 */     BOAT,  ITEM,  MINECART,  PRIMED_TNT,  ENDER_CRYSTAL,  ARROW,  SNOWBALL,  EGG,  GHAST_FIREBALL,  BLAZE_FIREBALL,  ENDER_PEARL,  WITHER_HEAD_PROJECTILE,  FALLING_BLOCK,  ITEM_FRAME,  EYE_OF_ENDER,  POTION,  EXP_BOTTLE,  FIREWORK_ROCKET,  LEASH_KNOT,  FISH_HOOK;
/* 244:    */   }
/* 245:    */   
/* 246:    */   public static class FallingBlockData
/* 247:    */   {
/* 248:    */     public static final int BLOCK_TYPE_TO_DATA(ServerSpawnObjectPacket.FallingBlockType type)
/* 249:    */     {
/* 250:264 */       return BLOCK_TYPE_TO_DATA(type.getId(), type.getMetadata());
/* 251:    */     }
/* 252:    */     
/* 253:    */     public static final int BLOCK_TYPE_TO_DATA(int block, int metadata)
/* 254:    */     {
/* 255:268 */       return block | metadata << 16;
/* 256:    */     }
/* 257:    */     
/* 258:    */     public static final ServerSpawnObjectPacket.FallingBlockType DATA_TO_BLOCK_TYPE(int data)
/* 259:    */     {
/* 260:272 */       return new ServerSpawnObjectPacket.FallingBlockType(data & 0xFFFF, data >> 16);
/* 261:    */     }
/* 262:    */   }
/* 263:    */   
/* 264:    */   public static class FallingBlockType
/* 265:    */   {
/* 266:    */     private int id;
/* 267:    */     private int metadata;
/* 268:    */     
/* 269:    */     public FallingBlockType(int id, int metadata)
/* 270:    */     {
/* 271:281 */       this.id = id;
/* 272:282 */       this.metadata = metadata;
/* 273:    */     }
/* 274:    */     
/* 275:    */     public int getId()
/* 276:    */     {
/* 277:286 */       return this.id;
/* 278:    */     }
/* 279:    */     
/* 280:    */     public int getMetadata()
/* 281:    */     {
/* 282:290 */       return this.metadata;
/* 283:    */     }
/* 284:    */   }
/* 285:    */   
/* 286:    */   public static class ItemFrameDirection
/* 287:    */   {
/* 288:    */     public static final int SOUTH = 0;
/* 289:    */     public static final int WEST = 1;
/* 290:    */     public static final int NORTH = 2;
/* 291:    */     public static final int EAST = 3;
/* 292:    */   }
/* 293:    */   
/* 294:    */   public static class MinecartType
/* 295:    */   {
/* 296:    */     public static final int NORMAL = 0;
/* 297:    */     public static final int CHEST = 1;
/* 298:    */     public static final int POWERED = 2;
/* 299:    */     public static final int TNT = 3;
/* 300:    */     public static final int MOB_SPAWNER = 4;
/* 301:    */     public static final int HOPPER = 5;
/* 302:    */     public static final int COMMAND_BLOCK = 6;
/* 303:    */   }
/* 304:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnObjectPacket
 * JD-Core Version:    0.7.0.1
 */