/*   1:    */ package org.spacehq.mc.protocol.packet.ingame.server.world;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import org.spacehq.packetlib.io.NetInput;
/*   5:    */ import org.spacehq.packetlib.io.NetOutput;
/*   6:    */ import org.spacehq.packetlib.packet.Packet;
/*   7:    */ 
/*   8:    */ public class ServerPlayEffectPacket
/*   9:    */   implements Packet
/*  10:    */ {
/*  11:    */   private Effect effect;
/*  12:    */   private int x;
/*  13:    */   private int y;
/*  14:    */   private int z;
/*  15:    */   private EffectData data;
/*  16:    */   private boolean broadcast;
/*  17:    */   
/*  18:    */   private ServerPlayEffectPacket() {}
/*  19:    */   
/*  20:    */   public ServerPlayEffectPacket(Effect effect, int x, int y, int z, EffectData data)
/*  21:    */   {
/*  22: 23 */     this(effect, x, y, z, data, false);
/*  23:    */   }
/*  24:    */   
/*  25:    */   public ServerPlayEffectPacket(Effect effect, int x, int y, int z, EffectData data, boolean broadcast)
/*  26:    */   {
/*  27: 27 */     this.effect = effect;
/*  28: 28 */     this.x = x;
/*  29: 29 */     this.y = y;
/*  30: 30 */     this.z = z;
/*  31: 31 */     this.data = data;
/*  32: 32 */     this.broadcast = broadcast;
/*  33:    */   }
/*  34:    */   
/*  35:    */   public Effect getEffect()
/*  36:    */   {
/*  37: 36 */     return this.effect;
/*  38:    */   }
/*  39:    */   
/*  40:    */   public int getX()
/*  41:    */   {
/*  42: 40 */     return this.x;
/*  43:    */   }
/*  44:    */   
/*  45:    */   public int getY()
/*  46:    */   {
/*  47: 44 */     return this.y;
/*  48:    */   }
/*  49:    */   
/*  50:    */   public int getZ()
/*  51:    */   {
/*  52: 48 */     return this.z;
/*  53:    */   }
/*  54:    */   
/*  55:    */   public EffectData getData()
/*  56:    */   {
/*  57: 52 */     return this.data;
/*  58:    */   }
/*  59:    */   
/*  60:    */   public boolean getBroadcast()
/*  61:    */   {
/*  62: 56 */     return this.broadcast;
/*  63:    */   }
/*  64:    */   
/*  65:    */   public void read(NetInput in)
/*  66:    */     throws IOException
/*  67:    */   {
/*  68: 61 */     this.effect = idToEffect(in.readInt());
/*  69: 62 */     this.x = in.readInt();
/*  70: 63 */     this.y = in.readUnsignedByte();
/*  71: 64 */     this.z = in.readInt();
/*  72: 65 */     this.data = valueToData(in.readInt());
/*  73: 66 */     this.broadcast = in.readBoolean();
/*  74:    */   }
/*  75:    */   
/*  76:    */   public void write(NetOutput out)
/*  77:    */     throws IOException
/*  78:    */   {
/*  79: 71 */     out.writeInt(effectToId(this.effect));
/*  80: 72 */     out.writeInt(this.x);
/*  81: 73 */     out.writeByte(this.y);
/*  82: 74 */     out.writeInt(this.z);
/*  83: 75 */     out.writeInt(dataToValue(this.data));
/*  84: 76 */     out.writeBoolean(this.broadcast);
/*  85:    */   }
/*  86:    */   
/*  87:    */   public boolean isPriority()
/*  88:    */   {
/*  89: 81 */     return false;
/*  90:    */   }
/*  91:    */   
/*  92:    */   private Effect idToEffect(int id)
/*  93:    */     throws IOException
/*  94:    */   {
/*  95: 85 */     switch (id)
/*  96:    */     {
/*  97:    */     case 1000: 
/*  98: 87 */       return SoundEffect.CLICK;
/*  99:    */     case 1001: 
/* 100: 89 */       return SoundEffect.EMPTY_DISPENSER_CLICK;
/* 101:    */     case 1002: 
/* 102: 91 */       return SoundEffect.FIRE_PROJECTILE;
/* 103:    */     case 1003: 
/* 104: 93 */       return SoundEffect.DOOR;
/* 105:    */     case 1004: 
/* 106: 95 */       return SoundEffect.FIZZLE;
/* 107:    */     case 1005: 
/* 108: 97 */       return SoundEffect.PLAY_RECORD;
/* 109:    */     case 1007: 
/* 110: 99 */       return SoundEffect.GHAST_CHARGE;
/* 111:    */     case 1008: 
/* 112:101 */       return SoundEffect.GHAST_FIRE;
/* 113:    */     case 1009: 
/* 114:103 */       return SoundEffect.BLAZE_FIRE;
/* 115:    */     case 1010: 
/* 116:105 */       return SoundEffect.POUND_WOODEN_DOOR;
/* 117:    */     case 1011: 
/* 118:107 */       return SoundEffect.POUND_METAL_DOOR;
/* 119:    */     case 1012: 
/* 120:109 */       return SoundEffect.BREAK_WOODEN_DOOR;
/* 121:    */     case 1014: 
/* 122:111 */       return SoundEffect.WITHER_SHOOT;
/* 123:    */     case 1015: 
/* 124:113 */       return SoundEffect.BAT_TAKE_OFF;
/* 125:    */     case 1016: 
/* 126:115 */       return SoundEffect.INFECT_VILLAGER;
/* 127:    */     case 1017: 
/* 128:117 */       return SoundEffect.DISINFECT_VILLAGER;
/* 129:    */     case 1018: 
/* 130:119 */       return SoundEffect.ENDER_DRAGON_DEATH;
/* 131:    */     case 1020: 
/* 132:121 */       return SoundEffect.ANVIL_BREAK;
/* 133:    */     case 1021: 
/* 134:123 */       return SoundEffect.ANVIL_USE;
/* 135:    */     case 1022: 
/* 136:125 */       return SoundEffect.ANVIL_LAND;
/* 137:    */     }
/* 138:128 */     switch (id)
/* 139:    */     {
/* 140:    */     case 2000: 
/* 141:130 */       return ParticleEffect.SMOKE;
/* 142:    */     case 2001: 
/* 143:132 */       return ParticleEffect.BREAK_BLOCK;
/* 144:    */     case 2002: 
/* 145:134 */       return ParticleEffect.BREAK_SPLASH_POTION;
/* 146:    */     case 2003: 
/* 147:136 */       return ParticleEffect.BREAK_EYE_OF_ENDER;
/* 148:    */     case 2004: 
/* 149:138 */       return ParticleEffect.MOB_SPAWN;
/* 150:    */     case 2005: 
/* 151:140 */       return ParticleEffect.BONEMEAL_GROW;
/* 152:    */     case 2006: 
/* 153:142 */       return ParticleEffect.HARD_LANDING_DUST;
/* 154:    */     }
/* 155:145 */     throw new IOException("Unknown effect id: " + id);
/* 156:    */   }
/* 157:    */   
/* 158:    */   private int effectToId(Effect effect)
/* 159:    */     throws IOException
/* 160:    */   {
/* 161:149 */     if (effect == SoundEffect.CLICK) {
/* 162:150 */       return 1000;
/* 163:    */     }
/* 164:151 */     if (effect == SoundEffect.EMPTY_DISPENSER_CLICK) {
/* 165:152 */       return 1001;
/* 166:    */     }
/* 167:153 */     if (effect == SoundEffect.FIRE_PROJECTILE) {
/* 168:154 */       return 1002;
/* 169:    */     }
/* 170:155 */     if (effect == SoundEffect.DOOR) {
/* 171:156 */       return 1003;
/* 172:    */     }
/* 173:157 */     if (effect == SoundEffect.FIZZLE) {
/* 174:158 */       return 1004;
/* 175:    */     }
/* 176:159 */     if (effect == SoundEffect.PLAY_RECORD) {
/* 177:160 */       return 1005;
/* 178:    */     }
/* 179:161 */     if (effect == SoundEffect.GHAST_CHARGE) {
/* 180:162 */       return 1007;
/* 181:    */     }
/* 182:163 */     if (effect == SoundEffect.GHAST_FIRE) {
/* 183:164 */       return 1008;
/* 184:    */     }
/* 185:165 */     if (effect == SoundEffect.BLAZE_FIRE) {
/* 186:166 */       return 1009;
/* 187:    */     }
/* 188:167 */     if (effect == SoundEffect.POUND_WOODEN_DOOR) {
/* 189:168 */       return 1010;
/* 190:    */     }
/* 191:169 */     if (effect == SoundEffect.POUND_METAL_DOOR) {
/* 192:170 */       return 1011;
/* 193:    */     }
/* 194:171 */     if (effect == SoundEffect.BREAK_WOODEN_DOOR) {
/* 195:172 */       return 1012;
/* 196:    */     }
/* 197:173 */     if (effect == SoundEffect.WITHER_SHOOT) {
/* 198:174 */       return 1014;
/* 199:    */     }
/* 200:175 */     if (effect == SoundEffect.BAT_TAKE_OFF) {
/* 201:176 */       return 1015;
/* 202:    */     }
/* 203:177 */     if (effect == SoundEffect.INFECT_VILLAGER) {
/* 204:178 */       return 1016;
/* 205:    */     }
/* 206:179 */     if (effect == SoundEffect.DISINFECT_VILLAGER) {
/* 207:180 */       return 1017;
/* 208:    */     }
/* 209:181 */     if (effect == SoundEffect.ENDER_DRAGON_DEATH) {
/* 210:182 */       return 1018;
/* 211:    */     }
/* 212:183 */     if (effect == SoundEffect.ANVIL_BREAK) {
/* 213:184 */       return 1020;
/* 214:    */     }
/* 215:185 */     if (effect == SoundEffect.ANVIL_USE) {
/* 216:186 */       return 1021;
/* 217:    */     }
/* 218:187 */     if (effect == SoundEffect.ANVIL_LAND) {
/* 219:188 */       return 1022;
/* 220:    */     }
/* 221:191 */     if (effect == ParticleEffect.SMOKE) {
/* 222:192 */       return 2000;
/* 223:    */     }
/* 224:193 */     if (effect == ParticleEffect.BREAK_BLOCK) {
/* 225:194 */       return 2001;
/* 226:    */     }
/* 227:195 */     if (effect == ParticleEffect.BREAK_SPLASH_POTION) {
/* 228:196 */       return 2002;
/* 229:    */     }
/* 230:197 */     if (effect == ParticleEffect.BREAK_EYE_OF_ENDER) {
/* 231:198 */       return 2003;
/* 232:    */     }
/* 233:199 */     if (effect == ParticleEffect.MOB_SPAWN) {
/* 234:200 */       return 2004;
/* 235:    */     }
/* 236:201 */     if (effect == ParticleEffect.BONEMEAL_GROW) {
/* 237:202 */       return 2005;
/* 238:    */     }
/* 239:203 */     if (effect == ParticleEffect.HARD_LANDING_DUST) {
/* 240:204 */       return 2006;
/* 241:    */     }
/* 242:207 */     throw new IOException("Unmapped effect: " + effect);
/* 243:    */   }
/* 244:    */   
/* 245:    */   private EffectData valueToData(int value)
/* 246:    */   {
/* 247:211 */     if (this.effect == SoundEffect.PLAY_RECORD) {
/* 248:212 */       return new RecordData(value);
/* 249:    */     }
/* 250:213 */     if (this.effect == ParticleEffect.SMOKE)
/* 251:    */     {
/* 252:214 */       if (value == 0) {
/* 253:215 */         return SmokeData.SOUTH_EAST;
/* 254:    */       }
/* 255:216 */       if (value == 1) {
/* 256:217 */         return SmokeData.SOUTH;
/* 257:    */       }
/* 258:218 */       if (value == 2) {
/* 259:219 */         return SmokeData.SOUTH_WEST;
/* 260:    */       }
/* 261:220 */       if (value == 3) {
/* 262:221 */         return SmokeData.EAST;
/* 263:    */       }
/* 264:222 */       if (value == 4) {
/* 265:223 */         return SmokeData.UP;
/* 266:    */       }
/* 267:224 */       if (value == 5) {
/* 268:225 */         return SmokeData.WEST;
/* 269:    */       }
/* 270:226 */       if (value == 6) {
/* 271:227 */         return SmokeData.NORTH_EAST;
/* 272:    */       }
/* 273:228 */       if (value == 7) {
/* 274:229 */         return SmokeData.NORTH;
/* 275:    */       }
/* 276:230 */       if (value == 8) {
/* 277:231 */         return SmokeData.NORTH_WEST;
/* 278:    */       }
/* 279:    */     }
/* 280:    */     else
/* 281:    */     {
/* 282:233 */       if (this.effect == ParticleEffect.BREAK_BLOCK) {
/* 283:234 */         return new BreakBlockData(value);
/* 284:    */       }
/* 285:235 */       if (this.effect == ParticleEffect.BREAK_SPLASH_POTION) {
/* 286:236 */         return new BreakPotionData(value);
/* 287:    */       }
/* 288:237 */       if (this.effect == ParticleEffect.HARD_LANDING_DUST) {
/* 289:238 */         return new HardLandingData(value);
/* 290:    */       }
/* 291:    */     }
/* 292:241 */     return null;
/* 293:    */   }
/* 294:    */   
/* 295:    */   private int dataToValue(EffectData data)
/* 296:    */   {
/* 297:245 */     if ((data instanceof RecordData)) {
/* 298:246 */       return ((RecordData)data).getRecordId();
/* 299:    */     }
/* 300:249 */     if ((data instanceof SmokeData))
/* 301:    */     {
/* 302:250 */       if (data == SmokeData.SOUTH_EAST) {
/* 303:251 */         return 0;
/* 304:    */       }
/* 305:252 */       if (data == SmokeData.SOUTH) {
/* 306:253 */         return 1;
/* 307:    */       }
/* 308:254 */       if (data == SmokeData.SOUTH_WEST) {
/* 309:255 */         return 2;
/* 310:    */       }
/* 311:256 */       if (data == SmokeData.EAST) {
/* 312:257 */         return 3;
/* 313:    */       }
/* 314:258 */       if (data == SmokeData.UP) {
/* 315:259 */         return 4;
/* 316:    */       }
/* 317:260 */       if (data == SmokeData.WEST) {
/* 318:261 */         return 5;
/* 319:    */       }
/* 320:262 */       if (data == SmokeData.NORTH_EAST) {
/* 321:263 */         return 6;
/* 322:    */       }
/* 323:264 */       if (data == SmokeData.NORTH) {
/* 324:265 */         return 7;
/* 325:    */       }
/* 326:266 */       if (data == SmokeData.NORTH_WEST) {
/* 327:267 */         return 8;
/* 328:    */       }
/* 329:    */     }
/* 330:271 */     if ((data instanceof BreakBlockData)) {
/* 331:272 */       return ((BreakBlockData)data).getBlockId();
/* 332:    */     }
/* 333:275 */     if ((data instanceof BreakPotionData)) {
/* 334:276 */       return ((BreakPotionData)data).getPotionId();
/* 335:    */     }
/* 336:279 */     if ((data instanceof HardLandingData)) {
/* 337:280 */       return ((HardLandingData)data).getDamagingDistance();
/* 338:    */     }
/* 339:283 */     return 0;
/* 340:    */   }
/* 341:    */   
/* 342:    */   public static enum SoundEffect
/* 343:    */     implements ServerPlayEffectPacket.Effect
/* 344:    */   {
/* 345:290 */     CLICK,  EMPTY_DISPENSER_CLICK,  FIRE_PROJECTILE,  DOOR,  FIZZLE,  PLAY_RECORD,  GHAST_CHARGE,  GHAST_FIRE,  BLAZE_FIRE,  POUND_WOODEN_DOOR,  POUND_METAL_DOOR,  BREAK_WOODEN_DOOR,  WITHER_SHOOT,  BAT_TAKE_OFF,  INFECT_VILLAGER,  DISINFECT_VILLAGER,  ENDER_DRAGON_DEATH,  ANVIL_BREAK,  ANVIL_USE,  ANVIL_LAND;
/* 346:    */   }
/* 347:    */   
/* 348:    */   public static enum ParticleEffect
/* 349:    */     implements ServerPlayEffectPacket.Effect
/* 350:    */   {
/* 351:313 */     SMOKE,  BREAK_BLOCK,  BREAK_SPLASH_POTION,  BREAK_EYE_OF_ENDER,  MOB_SPAWN,  BONEMEAL_GROW,  HARD_LANDING_DUST;
/* 352:    */   }
/* 353:    */   
/* 354:    */   public static class RecordData
/* 355:    */     implements ServerPlayEffectPacket.EffectData
/* 356:    */   {
/* 357:    */     private int recordId;
/* 358:    */     
/* 359:    */     public RecordData(int recordId)
/* 360:    */     {
/* 361:329 */       this.recordId = recordId;
/* 362:    */     }
/* 363:    */     
/* 364:    */     public int getRecordId()
/* 365:    */     {
/* 366:333 */       return this.recordId;
/* 367:    */     }
/* 368:    */   }
/* 369:    */   
/* 370:    */   public static enum SmokeData
/* 371:    */     implements ServerPlayEffectPacket.EffectData
/* 372:    */   {
/* 373:338 */     SOUTH_EAST,  SOUTH,  SOUTH_WEST,  EAST,  UP,  WEST,  NORTH_EAST,  NORTH,  NORTH_WEST;
/* 374:    */   }
/* 375:    */   
/* 376:    */   public static class BreakBlockData
/* 377:    */     implements ServerPlayEffectPacket.EffectData
/* 378:    */   {
/* 379:    */     private int blockId;
/* 380:    */     
/* 381:    */     public BreakBlockData(int blockId)
/* 382:    */     {
/* 383:353 */       this.blockId = blockId;
/* 384:    */     }
/* 385:    */     
/* 386:    */     public int getBlockId()
/* 387:    */     {
/* 388:357 */       return this.blockId;
/* 389:    */     }
/* 390:    */   }
/* 391:    */   
/* 392:    */   public static class BreakPotionData
/* 393:    */     implements ServerPlayEffectPacket.EffectData
/* 394:    */   {
/* 395:    */     private int potionId;
/* 396:    */     
/* 397:    */     public BreakPotionData(int potionId)
/* 398:    */     {
/* 399:365 */       this.potionId = potionId;
/* 400:    */     }
/* 401:    */     
/* 402:    */     public int getPotionId()
/* 403:    */     {
/* 404:369 */       return this.potionId;
/* 405:    */     }
/* 406:    */   }
/* 407:    */   
/* 408:    */   public static class HardLandingData
/* 409:    */     implements ServerPlayEffectPacket.EffectData
/* 410:    */   {
/* 411:    */     private int damagingDistance;
/* 412:    */     
/* 413:    */     public HardLandingData(int damagingDistance)
/* 414:    */     {
/* 415:377 */       this.damagingDistance = damagingDistance;
/* 416:    */     }
/* 417:    */     
/* 418:    */     public int getDamagingDistance()
/* 419:    */     {
/* 420:381 */       return this.damagingDistance;
/* 421:    */     }
/* 422:    */   }
/* 423:    */   
/* 424:    */   public static abstract interface Effect {}
/* 425:    */   
/* 426:    */   public static abstract interface EffectData {}
/* 427:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.world.ServerPlayEffectPacket
 * JD-Core Version:    0.7.0.1
 */