/*   1:    */ package org.spacehq.mc.protocol.util;
/*   2:    */ 
/*   3:    */ import java.io.ByteArrayInputStream;
/*   4:    */ import java.io.ByteArrayOutputStream;
/*   5:    */ import java.io.DataInputStream;
/*   6:    */ import java.io.DataOutputStream;
/*   7:    */ import java.io.IOException;
/*   8:    */ import java.util.ArrayList;
/*   9:    */ import java.util.List;
/*  10:    */ import java.util.zip.GZIPInputStream;
/*  11:    */ import java.util.zip.GZIPOutputStream;
/*  12:    */ import org.spacehq.mc.protocol.data.game.ByteArray3d;
/*  13:    */ import org.spacehq.mc.protocol.data.game.Chunk;
/*  14:    */ import org.spacehq.mc.protocol.data.game.Coordinates;
/*  15:    */ import org.spacehq.mc.protocol.data.game.EntityMetadata;
/*  16:    */ import org.spacehq.mc.protocol.data.game.EntityMetadata.Type;
/*  17:    */ import org.spacehq.mc.protocol.data.game.ItemStack;
/*  18:    */ import org.spacehq.mc.protocol.data.game.NibbleArray3d;
/*  19:    */ import org.spacehq.opennbt.NBTIO;
/*  20:    */ import org.spacehq.opennbt.tag.builtin.CompoundTag;
/*  21:    */ import org.spacehq.packetlib.io.NetInput;
/*  22:    */ import org.spacehq.packetlib.io.NetOutput;
/*  23:    */ 
/*  24:    */ public class NetUtil
/*  25:    */ {
/*  26:    */   public static CompoundTag readNBT(NetInput in)
/*  27:    */     throws IOException
/*  28:    */   {
/*  29: 18 */     short length = in.readShort();
/*  30: 19 */     if (length < 0) {
/*  31: 20 */       return null;
/*  32:    */     }
/*  33: 22 */     return (CompoundTag)NBTIO.readTag(new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(in.readBytes(length)))));
/*  34:    */   }
/*  35:    */   
/*  36:    */   public static void writeNBT(NetOutput out, CompoundTag tag)
/*  37:    */     throws IOException
/*  38:    */   {
/*  39: 27 */     if (tag == null)
/*  40:    */     {
/*  41: 28 */       out.writeShort(-1);
/*  42:    */     }
/*  43:    */     else
/*  44:    */     {
/*  45: 30 */       ByteArrayOutputStream output = new ByteArrayOutputStream();
/*  46: 31 */       GZIPOutputStream gzip = new GZIPOutputStream(output);
/*  47: 32 */       NBTIO.writeTag(new DataOutputStream(gzip), tag);
/*  48: 33 */       gzip.close();
/*  49: 34 */       output.close();
/*  50: 35 */       byte[] bytes = output.toByteArray();
/*  51: 36 */       out.writeShort((short)bytes.length);
/*  52: 37 */       out.writeBytes(bytes);
/*  53:    */     }
/*  54:    */   }
/*  55:    */   
/*  56:    */   public static ItemStack readItem(NetInput in)
/*  57:    */     throws IOException
/*  58:    */   {
/*  59: 42 */     short item = in.readShort();
/*  60: 43 */     if (item < 0) {
/*  61: 44 */       return null;
/*  62:    */     }
/*  63: 46 */     return new ItemStack(item, in.readByte(), in.readShort(), readNBT(in));
/*  64:    */   }
/*  65:    */   
/*  66:    */   public static void writeItem(NetOutput out, ItemStack item)
/*  67:    */     throws IOException
/*  68:    */   {
/*  69: 51 */     if (item == null)
/*  70:    */     {
/*  71: 52 */       out.writeShort(-1);
/*  72:    */     }
/*  73:    */     else
/*  74:    */     {
/*  75: 54 */       out.writeShort(item.getId());
/*  76: 55 */       out.writeByte(item.getAmount());
/*  77: 56 */       out.writeShort(item.getData());
/*  78: 57 */       writeNBT(out, item.getNBT());
/*  79:    */     }
/*  80:    */   }
/*  81:    */   
/*  82:    */   public static EntityMetadata[] readEntityMetadata(NetInput in)
/*  83:    */     throws IOException
/*  84:    */   {
/*  85: 62 */     List<EntityMetadata> ret = new ArrayList();
/*  86:    */     byte b;
/*  87: 64 */     while ((b = in.readByte()) != 127)
/*  88:    */     {
/*  89:    */       byte b;
/*  90: 65 */       int typeId = (b & 0xE0) >> 5;
/*  91: 66 */       EntityMetadata.Type type = EntityMetadata.Type.values()[typeId];
/*  92: 67 */       int id = b & 0x1F;
/*  93: 68 */       Object value = null;
/*  94: 69 */       switch (type)
/*  95:    */       {
/*  96:    */       case BYTE: 
/*  97: 71 */         value = Byte.valueOf(in.readByte());
/*  98: 72 */         break;
/*  99:    */       case COORDINATES: 
/* 100: 74 */         value = Short.valueOf(in.readShort());
/* 101: 75 */         break;
/* 102:    */       case FLOAT: 
/* 103: 77 */         value = Integer.valueOf(in.readInt());
/* 104: 78 */         break;
/* 105:    */       case INT: 
/* 106: 80 */         value = Float.valueOf(in.readFloat());
/* 107: 81 */         break;
/* 108:    */       case ITEM: 
/* 109: 83 */         value = in.readString();
/* 110: 84 */         break;
/* 111:    */       case SHORT: 
/* 112: 86 */         value = readItem(in);
/* 113: 87 */         break;
/* 114:    */       case STRING: 
/* 115: 89 */         value = new Coordinates(in.readInt(), in.readInt(), in.readInt());
/* 116: 90 */         break;
/* 117:    */       default: 
/* 118: 92 */         throw new IOException("Unknown metadata type id: " + typeId);
/* 119:    */       }
/* 120: 95 */       ret.add(new EntityMetadata(id, type, value));
/* 121:    */     }
/* 122: 98 */     return (EntityMetadata[])ret.toArray(new EntityMetadata[ret.size()]);
/* 123:    */   }
/* 124:    */   
/* 125:    */   public static void writeEntityMetadata(NetOutput out, EntityMetadata[] metadata)
/* 126:    */     throws IOException
/* 127:    */   {
/* 128:102 */     for (EntityMetadata meta : metadata)
/* 129:    */     {
/* 130:103 */       int id = (meta.getType().ordinal() << 5 | meta.getId() & 0x1F) & 0xFF;
/* 131:104 */       out.writeByte(id);
/* 132:105 */       switch (meta.getType())
/* 133:    */       {
/* 134:    */       case BYTE: 
/* 135:107 */         out.writeByte(((Byte)meta.getValue()).byteValue());
/* 136:108 */         break;
/* 137:    */       case COORDINATES: 
/* 138:110 */         out.writeShort(((Short)meta.getValue()).shortValue());
/* 139:111 */         break;
/* 140:    */       case FLOAT: 
/* 141:113 */         out.writeInt(((Integer)meta.getValue()).intValue());
/* 142:114 */         break;
/* 143:    */       case INT: 
/* 144:116 */         out.writeFloat(((Float)meta.getValue()).floatValue());
/* 145:117 */         break;
/* 146:    */       case ITEM: 
/* 147:119 */         out.writeString((String)meta.getValue());
/* 148:120 */         break;
/* 149:    */       case SHORT: 
/* 150:122 */         writeItem(out, (ItemStack)meta.getValue());
/* 151:123 */         break;
/* 152:    */       case STRING: 
/* 153:125 */         Coordinates coords = (Coordinates)meta.getValue();
/* 154:126 */         out.writeInt(coords.getX());
/* 155:127 */         out.writeInt(coords.getY());
/* 156:128 */         out.writeInt(coords.getZ());
/* 157:129 */         break;
/* 158:    */       default: 
/* 159:131 */         throw new IOException("Unmapped metadata type: " + meta.getType());
/* 160:    */       }
/* 161:    */     }
/* 162:135 */     out.writeByte(127);
/* 163:    */   }
/* 164:    */   
/* 165:    */   public static ParsedChunkData dataToChunks(NetworkChunkData data)
/* 166:    */   {
/* 167:139 */     Chunk[] chunks = new Chunk[16];
/* 168:140 */     int pos = 0;
/* 169:141 */     int expected = 0;
/* 170:142 */     boolean sky = false;
/* 171:149 */     for (int pass = 0; pass < 5; pass++)
/* 172:    */     {
/* 173:150 */       for (int ind = 0; ind < 16; ind++)
/* 174:    */       {
/* 175:151 */         if ((data.getMask() & 1 << ind) != 0)
/* 176:    */         {
/* 177:152 */           if (pass == 0)
/* 178:    */           {
/* 179:153 */             expected += 10240;
/* 180:154 */             if ((data.getExtendedMask() & 1 << ind) != 0) {
/* 181:155 */               expected += 2048;
/* 182:    */             }
/* 183:    */           }
/* 184:159 */           if (pass == 1)
/* 185:    */           {
/* 186:160 */             chunks[ind] = new Chunk((sky) || (data.hasSkyLight()), (data.getExtendedMask() & 1 << ind) != 0);
/* 187:161 */             ByteArray3d blocks = chunks[ind].getBlocks();
/* 188:162 */             System.arraycopy(data.getData(), pos, blocks.getData(), 0, blocks.getData().length);
/* 189:163 */             pos += blocks.getData().length;
/* 190:    */           }
/* 191:166 */           if (pass == 2)
/* 192:    */           {
/* 193:167 */             NibbleArray3d metadata = chunks[ind].getMetadata();
/* 194:168 */             System.arraycopy(data.getData(), pos, metadata.getData(), 0, metadata.getData().length);
/* 195:169 */             pos += metadata.getData().length;
/* 196:    */           }
/* 197:172 */           if (pass == 3)
/* 198:    */           {
/* 199:173 */             NibbleArray3d blocklight = chunks[ind].getBlockLight();
/* 200:174 */             System.arraycopy(data.getData(), pos, blocklight.getData(), 0, blocklight.getData().length);
/* 201:175 */             pos += blocklight.getData().length;
/* 202:    */           }
/* 203:178 */           if ((pass == 4) && ((sky) || (data.hasSkyLight())))
/* 204:    */           {
/* 205:179 */             NibbleArray3d skylight = chunks[ind].getSkyLight();
/* 206:180 */             System.arraycopy(data.getData(), pos, skylight.getData(), 0, skylight.getData().length);
/* 207:181 */             pos += skylight.getData().length;
/* 208:    */           }
/* 209:    */         }
/* 210:185 */         if ((pass == 5) && 
/* 211:186 */           ((data.getExtendedMask() & 1 << ind) != 0)) {
/* 212:187 */           if (chunks[ind] == null)
/* 213:    */           {
/* 214:188 */             pos += 2048;
/* 215:    */           }
/* 216:    */           else
/* 217:    */           {
/* 218:190 */             NibbleArray3d extended = chunks[ind].getExtendedBlocks();
/* 219:191 */             System.arraycopy(data.getData(), pos, extended.getData(), 0, extended.getData().length);
/* 220:192 */             pos += extended.getData().length;
/* 221:    */           }
/* 222:    */         }
/* 223:    */       }
/* 224:198 */       if ((pass == 0) && 
/* 225:199 */         (data.getData().length >= expected)) {
/* 226:200 */         sky = true;
/* 227:    */       }
/* 228:    */     }
/* 229:205 */     byte[] biomeData = null;
/* 230:206 */     if (data.isFullChunk())
/* 231:    */     {
/* 232:207 */       biomeData = new byte[256];
/* 233:208 */       System.arraycopy(data.getData(), pos, biomeData, 0, biomeData.length);
/* 234:209 */       pos += biomeData.length;
/* 235:    */     }
/* 236:212 */     return new ParsedChunkData(chunks, biomeData);
/* 237:    */   }
/* 238:    */   
/* 239:    */   public static NetworkChunkData chunksToData(ParsedChunkData chunks)
/* 240:    */   {
/* 241:216 */     int chunkMask = 0;
/* 242:217 */     int extendedChunkMask = 0;
/* 243:218 */     boolean fullChunk = chunks.getBiomes() != null;
/* 244:219 */     boolean sky = false;
/* 245:220 */     int length = fullChunk ? chunks.getBiomes().length : 0;
/* 246:221 */     byte[] data = null;
/* 247:222 */     int pos = 0;
/* 248:229 */     for (int pass = 0; pass < 6; pass++)
/* 249:    */     {
/* 250:230 */       for (int ind = 0; ind < chunks.getChunks().length; ind++)
/* 251:    */       {
/* 252:231 */         Chunk chunk = chunks.getChunks()[ind];
/* 253:232 */         if ((chunk != null) && ((!fullChunk) || (!chunk.isEmpty())))
/* 254:    */         {
/* 255:233 */           if (pass == 0)
/* 256:    */           {
/* 257:234 */             chunkMask |= 1 << ind;
/* 258:235 */             if (chunk.getExtendedBlocks() != null) {
/* 259:236 */               extendedChunkMask |= 1 << ind;
/* 260:    */             }
/* 261:239 */             length += chunk.getBlocks().getData().length;
/* 262:240 */             length += chunk.getMetadata().getData().length;
/* 263:241 */             length += chunk.getBlockLight().getData().length;
/* 264:242 */             if (chunk.getSkyLight() != null) {
/* 265:243 */               length += chunk.getSkyLight().getData().length;
/* 266:    */             }
/* 267:246 */             if (chunk.getExtendedBlocks() != null) {
/* 268:247 */               length += chunk.getExtendedBlocks().getData().length;
/* 269:    */             }
/* 270:    */           }
/* 271:251 */           if (pass == 1)
/* 272:    */           {
/* 273:252 */             ByteArray3d blocks = chunk.getBlocks();
/* 274:253 */             System.arraycopy(blocks.getData(), 0, data, pos, blocks.getData().length);
/* 275:254 */             pos += blocks.getData().length;
/* 276:    */           }
/* 277:257 */           if (pass == 2)
/* 278:    */           {
/* 279:258 */             byte[] meta = chunk.getMetadata().getData();
/* 280:259 */             System.arraycopy(meta, 0, data, pos, meta.length);
/* 281:260 */             pos += meta.length;
/* 282:    */           }
/* 283:263 */           if (pass == 3)
/* 284:    */           {
/* 285:264 */             byte[] blocklight = chunk.getBlockLight().getData();
/* 286:265 */             System.arraycopy(blocklight, 0, data, pos, blocklight.length);
/* 287:266 */             pos += blocklight.length;
/* 288:    */           }
/* 289:269 */           if ((pass == 4) && (chunk.getSkyLight() != null))
/* 290:    */           {
/* 291:270 */             byte[] skylight = chunk.getSkyLight().getData();
/* 292:271 */             System.arraycopy(skylight, 0, data, pos, skylight.length);
/* 293:272 */             pos += skylight.length;
/* 294:273 */             sky = true;
/* 295:    */           }
/* 296:276 */           if ((pass == 5) && (chunk.getExtendedBlocks() != null))
/* 297:    */           {
/* 298:277 */             byte[] extended = chunk.getExtendedBlocks().getData();
/* 299:278 */             System.arraycopy(extended, 0, data, pos, extended.length);
/* 300:279 */             pos += extended.length;
/* 301:    */           }
/* 302:    */         }
/* 303:    */       }
/* 304:284 */       if (pass == 0) {
/* 305:285 */         data = new byte[length];
/* 306:    */       }
/* 307:    */     }
/* 308:290 */     if (fullChunk)
/* 309:    */     {
/* 310:291 */       System.arraycopy(chunks.getBiomes(), 0, data, pos, chunks.getBiomes().length);
/* 311:292 */       pos += chunks.getBiomes().length;
/* 312:    */     }
/* 313:295 */     return new NetworkChunkData(chunkMask, extendedChunkMask, fullChunk, sky, data);
/* 314:    */   }
/* 315:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.util.NetUtil
 * JD-Core Version:    0.7.0.1
 */