/*   1:    */ package org.spacehq.mc.protocol.packet.ingame.server.world;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import org.spacehq.packetlib.io.NetInput;
/*   5:    */ import org.spacehq.packetlib.io.NetOutput;
/*   6:    */ import org.spacehq.packetlib.packet.Packet;
/*   7:    */ 
/*   8:    */ public class ServerBlockValuePacket
/*   9:    */   implements Packet
/*  10:    */ {
/*  11:    */   private static final int NOTE_BLOCK = 25;
/*  12:    */   private static final int STICKY_PISTON = 29;
/*  13:    */   private static final int PISTON = 33;
/*  14:    */   private static final int MOB_SPAWNER = 52;
/*  15:    */   private static final int CHEST = 54;
/*  16:    */   private static final int ENDER_CHEST = 130;
/*  17:    */   private static final int TRAPPED_CHEST = 146;
/*  18:    */   private int x;
/*  19:    */   private int y;
/*  20:    */   private int z;
/*  21:    */   private ValueType type;
/*  22:    */   private Value value;
/*  23:    */   private int blockId;
/*  24:    */   
/*  25:    */   private ServerBlockValuePacket() {}
/*  26:    */   
/*  27:    */   public ServerBlockValuePacket(int x, int y, int z, ValueType type, Value value, int blockId)
/*  28:    */   {
/*  29: 31 */     this.x = x;
/*  30: 32 */     this.y = y;
/*  31: 33 */     this.z = z;
/*  32: 34 */     this.type = type;
/*  33: 35 */     this.value = value;
/*  34: 36 */     this.blockId = blockId;
/*  35:    */   }
/*  36:    */   
/*  37:    */   public int getX()
/*  38:    */   {
/*  39: 40 */     return this.x;
/*  40:    */   }
/*  41:    */   
/*  42:    */   public int getY()
/*  43:    */   {
/*  44: 44 */     return this.y;
/*  45:    */   }
/*  46:    */   
/*  47:    */   public int getZ()
/*  48:    */   {
/*  49: 48 */     return this.z;
/*  50:    */   }
/*  51:    */   
/*  52:    */   public ValueType getType()
/*  53:    */   {
/*  54: 52 */     return this.type;
/*  55:    */   }
/*  56:    */   
/*  57:    */   public Value getValue()
/*  58:    */   {
/*  59: 56 */     return this.value;
/*  60:    */   }
/*  61:    */   
/*  62:    */   public int getBlockId()
/*  63:    */   {
/*  64: 60 */     return this.blockId;
/*  65:    */   }
/*  66:    */   
/*  67:    */   public void read(NetInput in)
/*  68:    */     throws IOException
/*  69:    */   {
/*  70: 65 */     this.x = in.readInt();
/*  71: 66 */     this.y = in.readShort();
/*  72: 67 */     this.z = in.readInt();
/*  73: 68 */     this.type = intToType(in.readUnsignedByte());
/*  74: 69 */     this.value = intToValue(in.readUnsignedByte());
/*  75: 70 */     this.blockId = (in.readVarInt() & 0xFFF);
/*  76:    */   }
/*  77:    */   
/*  78:    */   public void write(NetOutput out)
/*  79:    */     throws IOException
/*  80:    */   {
/*  81: 75 */     out.writeInt(this.x);
/*  82: 76 */     out.writeShort(this.y);
/*  83: 77 */     out.writeInt(this.z);
/*  84: 78 */     out.writeByte(typeToInt(this.type));
/*  85: 79 */     out.writeByte(valueToInt(this.value));
/*  86: 80 */     out.writeVarInt(this.blockId & 0xFFF);
/*  87:    */   }
/*  88:    */   
/*  89:    */   public boolean isPriority()
/*  90:    */   {
/*  91: 85 */     return false;
/*  92:    */   }
/*  93:    */   
/*  94:    */   private ValueType intToType(int value)
/*  95:    */     throws IOException
/*  96:    */   {
/*  97: 89 */     if (this.blockId == 25)
/*  98:    */     {
/*  99: 90 */       if (value == 0) {
/* 100: 91 */         return NoteBlockValueType.HARP;
/* 101:    */       }
/* 102: 92 */       if (value == 1) {
/* 103: 93 */         return NoteBlockValueType.DOUBLE_BASS;
/* 104:    */       }
/* 105: 94 */       if (value == 2) {
/* 106: 95 */         return NoteBlockValueType.SNARE_DRUM;
/* 107:    */       }
/* 108: 96 */       if (value == 3) {
/* 109: 97 */         return NoteBlockValueType.HI_HAT;
/* 110:    */       }
/* 111: 98 */       if (value == 4) {
/* 112: 99 */         return NoteBlockValueType.BASS_DRUM;
/* 113:    */       }
/* 114:    */     }
/* 115:101 */     else if ((this.blockId == 29) || (this.blockId == 33))
/* 116:    */     {
/* 117:102 */       if (value == 0) {
/* 118:103 */         return PistonValueType.PUSHING;
/* 119:    */       }
/* 120:104 */       if (value == 1) {
/* 121:105 */         return PistonValueType.PULLING;
/* 122:    */       }
/* 123:    */     }
/* 124:107 */     else if (this.blockId == 52)
/* 125:    */     {
/* 126:108 */       if (value == 1) {
/* 127:109 */         return MobSpawnerValueType.RESET_DELAY;
/* 128:    */       }
/* 129:    */     }
/* 130:111 */     else if ((this.blockId == 54) || (this.blockId == 130) || (this.blockId == 146))
/* 131:    */     {
/* 132:112 */       if (value == 1) {
/* 133:113 */         return ChestValueType.VIEWING_PLAYER_COUNT;
/* 134:    */       }
/* 135:    */     }
/* 136:    */     else
/* 137:    */     {
/* 138:116 */       return GenericValueType.GENERIC;
/* 139:    */     }
/* 140:119 */     throw new IOException("Unknown value type id: " + value + ", " + this.blockId);
/* 141:    */   }
/* 142:    */   
/* 143:    */   private int typeToInt(ValueType type)
/* 144:    */     throws IOException
/* 145:    */   {
/* 146:123 */     if (type == NoteBlockValueType.HARP) {
/* 147:124 */       return 0;
/* 148:    */     }
/* 149:125 */     if (type == NoteBlockValueType.DOUBLE_BASS) {
/* 150:126 */       return 1;
/* 151:    */     }
/* 152:127 */     if (type == NoteBlockValueType.SNARE_DRUM) {
/* 153:128 */       return 2;
/* 154:    */     }
/* 155:129 */     if (type == NoteBlockValueType.HI_HAT) {
/* 156:130 */       return 3;
/* 157:    */     }
/* 158:131 */     if (type == NoteBlockValueType.BASS_DRUM) {
/* 159:132 */       return 4;
/* 160:    */     }
/* 161:135 */     if (type == PistonValueType.PUSHING) {
/* 162:136 */       return 0;
/* 163:    */     }
/* 164:137 */     if (type == PistonValueType.PULLING) {
/* 165:138 */       return 1;
/* 166:    */     }
/* 167:141 */     if (type == MobSpawnerValueType.RESET_DELAY) {
/* 168:142 */       return 1;
/* 169:    */     }
/* 170:145 */     if (type == ChestValueType.VIEWING_PLAYER_COUNT) {
/* 171:146 */       return 1;
/* 172:    */     }
/* 173:149 */     if (type == GenericValueType.GENERIC) {
/* 174:150 */       return 0;
/* 175:    */     }
/* 176:153 */     throw new IOException("Unmapped value type: " + type);
/* 177:    */   }
/* 178:    */   
/* 179:    */   private Value intToValue(int value)
/* 180:    */     throws IOException
/* 181:    */   {
/* 182:157 */     if (this.blockId == 25) {
/* 183:158 */       return new NoteBlockValue(value);
/* 184:    */     }
/* 185:159 */     if ((this.blockId == 29) || (this.blockId == 33))
/* 186:    */     {
/* 187:160 */       if (value == 0) {
/* 188:161 */         return PistonValue.DOWN;
/* 189:    */       }
/* 190:162 */       if (value == 1) {
/* 191:163 */         return PistonValue.UP;
/* 192:    */       }
/* 193:164 */       if (value == 2) {
/* 194:165 */         return PistonValue.SOUTH;
/* 195:    */       }
/* 196:166 */       if (value == 3) {
/* 197:167 */         return PistonValue.WEST;
/* 198:    */       }
/* 199:168 */       if (value == 4) {
/* 200:169 */         return PistonValue.NORTH;
/* 201:    */       }
/* 202:170 */       if (value == 5) {
/* 203:171 */         return PistonValue.EAST;
/* 204:    */       }
/* 205:    */     }
/* 206:173 */     else if (this.blockId == 52)
/* 207:    */     {
/* 208:174 */       if (value == 0) {
/* 209:175 */         return MobSpawnerValue.VALUE;
/* 210:    */       }
/* 211:    */     }
/* 212:    */     else
/* 213:    */     {
/* 214:177 */       if ((this.blockId == 54) || (this.blockId == 130) || (this.blockId == 146)) {
/* 215:178 */         return new ChestValue(value);
/* 216:    */       }
/* 217:180 */       return new GenericValue(value);
/* 218:    */     }
/* 219:183 */     throw new IOException("Unknown value id: " + value + ", " + this.blockId);
/* 220:    */   }
/* 221:    */   
/* 222:    */   private int valueToInt(Value value)
/* 223:    */     throws IOException
/* 224:    */   {
/* 225:187 */     if ((value instanceof NoteBlockValue)) {
/* 226:188 */       return ((NoteBlockValue)value).getPitch();
/* 227:    */     }
/* 228:191 */     if (value == PistonValue.DOWN) {
/* 229:192 */       return 0;
/* 230:    */     }
/* 231:193 */     if (value == PistonValue.UP) {
/* 232:194 */       return 1;
/* 233:    */     }
/* 234:195 */     if (value == PistonValue.SOUTH) {
/* 235:196 */       return 2;
/* 236:    */     }
/* 237:197 */     if (value == PistonValue.WEST) {
/* 238:198 */       return 3;
/* 239:    */     }
/* 240:199 */     if (value == PistonValue.NORTH) {
/* 241:200 */       return 4;
/* 242:    */     }
/* 243:201 */     if (value == PistonValue.EAST) {
/* 244:202 */       return 5;
/* 245:    */     }
/* 246:205 */     if (value == MobSpawnerValue.VALUE) {
/* 247:206 */       return 0;
/* 248:    */     }
/* 249:209 */     if ((value instanceof ChestValue)) {
/* 250:210 */       return ((ChestValue)value).getViewers();
/* 251:    */     }
/* 252:213 */     if ((value instanceof GenericValue)) {
/* 253:214 */       return ((GenericValue)value).getValue();
/* 254:    */     }
/* 255:217 */     throw new IOException("Unmapped value: " + value);
/* 256:    */   }
/* 257:    */   
/* 258:    */   public static enum GenericValueType
/* 259:    */     implements ServerBlockValuePacket.ValueType
/* 260:    */   {
/* 261:224 */     GENERIC;
/* 262:    */   }
/* 263:    */   
/* 264:    */   public static enum NoteBlockValueType
/* 265:    */     implements ServerBlockValuePacket.ValueType
/* 266:    */   {
/* 267:228 */     HARP,  DOUBLE_BASS,  SNARE_DRUM,  HI_HAT,  BASS_DRUM;
/* 268:    */   }
/* 269:    */   
/* 270:    */   public static enum PistonValueType
/* 271:    */     implements ServerBlockValuePacket.ValueType
/* 272:    */   {
/* 273:236 */     PUSHING,  PULLING;
/* 274:    */   }
/* 275:    */   
/* 276:    */   public static enum ChestValueType
/* 277:    */     implements ServerBlockValuePacket.ValueType
/* 278:    */   {
/* 279:241 */     VIEWING_PLAYER_COUNT;
/* 280:    */   }
/* 281:    */   
/* 282:    */   public static enum MobSpawnerValueType
/* 283:    */     implements ServerBlockValuePacket.ValueType
/* 284:    */   {
/* 285:245 */     RESET_DELAY;
/* 286:    */   }
/* 287:    */   
/* 288:    */   public static class GenericValue
/* 289:    */     implements ServerBlockValuePacket.Value
/* 290:    */   {
/* 291:    */     private int value;
/* 292:    */     
/* 293:    */     public GenericValue(int value)
/* 294:    */     {
/* 295:255 */       this.value = value;
/* 296:    */     }
/* 297:    */     
/* 298:    */     public int getValue()
/* 299:    */     {
/* 300:259 */       return this.value;
/* 301:    */     }
/* 302:    */   }
/* 303:    */   
/* 304:    */   public static class NoteBlockValue
/* 305:    */     implements ServerBlockValuePacket.Value
/* 306:    */   {
/* 307:    */     private int pitch;
/* 308:    */     
/* 309:    */     public NoteBlockValue(int pitch)
/* 310:    */     {
/* 311:267 */       if ((pitch < 0) || (pitch > 24)) {
/* 312:268 */         throw new IllegalArgumentException("Pitch must be between 0 and 24.");
/* 313:    */       }
/* 314:271 */       this.pitch = pitch;
/* 315:    */     }
/* 316:    */     
/* 317:    */     public int getPitch()
/* 318:    */     {
/* 319:275 */       return this.pitch;
/* 320:    */     }
/* 321:    */   }
/* 322:    */   
/* 323:    */   public static enum PistonValue
/* 324:    */     implements ServerBlockValuePacket.Value
/* 325:    */   {
/* 326:280 */     DOWN,  UP,  SOUTH,  WEST,  NORTH,  EAST;
/* 327:    */   }
/* 328:    */   
/* 329:    */   public static class ChestValue
/* 330:    */     implements ServerBlockValuePacket.Value
/* 331:    */   {
/* 332:    */     private int viewers;
/* 333:    */     
/* 334:    */     public ChestValue(int viewers)
/* 335:    */     {
/* 336:292 */       this.viewers = viewers;
/* 337:    */     }
/* 338:    */     
/* 339:    */     public int getViewers()
/* 340:    */     {
/* 341:296 */       return this.viewers;
/* 342:    */     }
/* 343:    */   }
/* 344:    */   
/* 345:    */   public static enum MobSpawnerValue
/* 346:    */     implements ServerBlockValuePacket.Value
/* 347:    */   {
/* 348:301 */     VALUE;
/* 349:    */   }
/* 350:    */   
/* 351:    */   public static abstract interface Value {}
/* 352:    */   
/* 353:    */   public static abstract interface ValueType {}
/* 354:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.world.ServerBlockValuePacket
 * JD-Core Version:    0.7.0.1
 */