/*   1:    */ package org.spacehq.mc.protocol.packet.ingame.server.world;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import java.util.zip.DataFormatException;
/*   5:    */ import java.util.zip.Deflater;
/*   6:    */ import java.util.zip.Inflater;
/*   7:    */ import org.spacehq.mc.protocol.data.game.Chunk;
/*   8:    */ import org.spacehq.mc.protocol.util.NetUtil;
/*   9:    */ import org.spacehq.mc.protocol.util.NetworkChunkData;
/*  10:    */ import org.spacehq.mc.protocol.util.ParsedChunkData;
/*  11:    */ import org.spacehq.packetlib.io.NetInput;
/*  12:    */ import org.spacehq.packetlib.io.NetOutput;
/*  13:    */ import org.spacehq.packetlib.packet.Packet;
/*  14:    */ 
/*  15:    */ public class ServerMultiChunkDataPacket
/*  16:    */   implements Packet
/*  17:    */ {
/*  18:    */   private int[] x;
/*  19:    */   private int[] z;
/*  20:    */   private Chunk[][] chunks;
/*  21:    */   private byte[][] biomeData;
/*  22:    */   
/*  23:    */   private ServerMultiChunkDataPacket() {}
/*  24:    */   
/*  25:    */   public ServerMultiChunkDataPacket(int[] x, int[] z, Chunk[][] chunks, byte[][] biomeData)
/*  26:    */   {
/*  27: 28 */     if (biomeData == null) {
/*  28: 29 */       throw new IllegalArgumentException("BiomeData cannot be null.");
/*  29:    */     }
/*  30: 32 */     if ((x.length != chunks.length) || (z.length != chunks.length)) {
/*  31: 33 */       throw new IllegalArgumentException("X, Z, and Chunk arrays must be equal in length.");
/*  32:    */     }
/*  33: 36 */     boolean noSkylight = false;
/*  34: 37 */     boolean skylight = false;
/*  35: 38 */     for (int index = 0; index < chunks.length; index++)
/*  36:    */     {
/*  37: 39 */       Chunk[] column = chunks[index];
/*  38: 40 */       if (column.length != 16) {
/*  39: 41 */         throw new IllegalArgumentException("Chunk columns must contain 16 chunks each.");
/*  40:    */       }
/*  41: 44 */       for (int y = 0; y < column.length; y++) {
/*  42: 45 */         if (column[y] != null) {
/*  43: 46 */           if (column[y].getSkyLight() == null) {
/*  44: 47 */             noSkylight = true;
/*  45:    */           } else {
/*  46: 49 */             skylight = true;
/*  47:    */           }
/*  48:    */         }
/*  49:    */       }
/*  50:    */     }
/*  51: 55 */     if ((noSkylight) && (skylight)) {
/*  52: 56 */       throw new IllegalArgumentException("Either all chunks must have skylight values or none must have them.");
/*  53:    */     }
/*  54: 59 */     this.x = x;
/*  55: 60 */     this.z = z;
/*  56: 61 */     this.chunks = chunks;
/*  57: 62 */     this.biomeData = biomeData;
/*  58:    */   }
/*  59:    */   
/*  60:    */   public int getColumns()
/*  61:    */   {
/*  62: 66 */     return this.chunks.length;
/*  63:    */   }
/*  64:    */   
/*  65:    */   public int getX(int column)
/*  66:    */   {
/*  67: 70 */     return this.x[column];
/*  68:    */   }
/*  69:    */   
/*  70:    */   public int getZ(int column)
/*  71:    */   {
/*  72: 74 */     return this.z[column];
/*  73:    */   }
/*  74:    */   
/*  75:    */   public Chunk[] getChunks(int column)
/*  76:    */   {
/*  77: 78 */     return this.chunks[column];
/*  78:    */   }
/*  79:    */   
/*  80:    */   public byte[] getBiomeData(int column)
/*  81:    */   {
/*  82: 82 */     return this.biomeData[column];
/*  83:    */   }
/*  84:    */   
/*  85:    */   public void read(NetInput in)
/*  86:    */     throws IOException
/*  87:    */   {
/*  88: 88 */     short columns = in.readShort();
/*  89: 89 */     int deflatedLength = in.readInt();
/*  90: 90 */     boolean skylight = in.readBoolean();
/*  91: 91 */     byte[] deflatedBytes = in.readBytes(deflatedLength);
/*  92:    */     
/*  93: 93 */     byte[] inflated = new byte[196864 * columns];
/*  94: 94 */     Inflater inflater = new Inflater();
/*  95: 95 */     inflater.setInput(deflatedBytes, 0, deflatedLength);
/*  96:    */     try
/*  97:    */     {
/*  98: 97 */       inflater.inflate(inflated);
/*  99:    */     }
/* 100:    */     catch (DataFormatException e)
/* 101:    */     {
/* 102: 99 */       throw new IOException("Bad compressed data format");
/* 103:    */     }
/* 104:    */     finally
/* 105:    */     {
/* 106:101 */       inflater.end();
/* 107:    */     }
/* 108:104 */     this.x = new int[columns];
/* 109:105 */     this.z = new int[columns];
/* 110:106 */     this.chunks = new Chunk[columns][];
/* 111:107 */     this.biomeData = new byte[columns][];
/* 112:    */     
/* 113:109 */     int pos = 0;
/* 114:110 */     for (int count = 0; count < columns; count++)
/* 115:    */     {
/* 116:112 */       int x = in.readInt();
/* 117:113 */       int z = in.readInt();
/* 118:114 */       int chunkMask = in.readShort();
/* 119:115 */       int extendedChunkMask = in.readShort();
/* 120:    */       
/* 121:117 */       int chunks = 0;
/* 122:118 */       int extended = 0;
/* 123:119 */       for (int ch = 0; ch < 16; ch++)
/* 124:    */       {
/* 125:120 */         chunks += (chunkMask >> ch & 0x1);
/* 126:121 */         extended += (extendedChunkMask >> ch & 0x1);
/* 127:    */       }
/* 128:124 */       int length = 8192 * chunks + 256 + 2048 * extended;
/* 129:125 */       if (skylight) {
/* 130:126 */         length += 2048 * chunks;
/* 131:    */       }
/* 132:130 */       byte[] dat = new byte[length];
/* 133:131 */       System.arraycopy(inflated, pos, dat, 0, length);
/* 134:    */       
/* 135:133 */       ParsedChunkData chunkData = NetUtil.dataToChunks(new NetworkChunkData(chunkMask, extendedChunkMask, true, skylight, dat));
/* 136:134 */       this.x[count] = x;
/* 137:135 */       this.z[count] = z;
/* 138:136 */       this.chunks[count] = chunkData.getChunks();
/* 139:137 */       this.biomeData[count] = chunkData.getBiomes();
/* 140:138 */       pos += length;
/* 141:    */     }
/* 142:    */   }
/* 143:    */   
/* 144:    */   public void write(NetOutput out)
/* 145:    */     throws IOException
/* 146:    */   {
/* 147:145 */     int[] chunkMask = new int[this.chunks.length];
/* 148:146 */     int[] extendedChunkMask = new int[this.chunks.length];
/* 149:    */     
/* 150:148 */     int pos = 0;
/* 151:149 */     byte[] bytes = new byte[0];
/* 152:150 */     boolean skylight = false;
/* 153:151 */     for (int count = 0; count < this.chunks.length; count++)
/* 154:    */     {
/* 155:152 */       Chunk[] column = this.chunks[count];
/* 156:    */       
/* 157:154 */       NetworkChunkData netData = NetUtil.chunksToData(new ParsedChunkData(column, this.biomeData[count]));
/* 158:155 */       if (bytes.length < pos + netData.getData().length)
/* 159:    */       {
/* 160:156 */         newArray = new byte[pos + netData.getData().length];
/* 161:157 */         System.arraycopy(bytes, 0, newArray, 0, bytes.length);
/* 162:158 */         bytes = newArray;
/* 163:    */       }
/* 164:161 */       if (netData.hasSkyLight()) {
/* 165:162 */         skylight = true;
/* 166:    */       }
/* 167:166 */       System.arraycopy(netData.getData(), 0, bytes, pos, netData.getData().length);
/* 168:167 */       pos += netData.getData().length;
/* 169:    */       
/* 170:169 */       chunkMask[count] = netData.getMask();
/* 171:170 */       extendedChunkMask[count] = netData.getExtendedMask();
/* 172:    */     }
/* 173:174 */     Deflater deflater = new Deflater(-1);
/* 174:175 */     byte[] deflatedData = new byte[pos];
/* 175:176 */     int deflatedLength = pos;
/* 176:    */     try
/* 177:    */     {
/* 178:178 */       deflater.setInput(bytes, 0, pos);
/* 179:179 */       deflater.finish();
/* 180:180 */       deflatedLength = deflater.deflate(deflatedData);
/* 181:    */     }
/* 182:    */     finally
/* 183:    */     {
/* 184:182 */       deflater.end();
/* 185:    */     }
/* 186:186 */     out.writeShort(this.chunks.length);
/* 187:187 */     out.writeInt(deflatedLength);
/* 188:188 */     out.writeBoolean(skylight);
/* 189:189 */     out.writeBytes(deflatedData, deflatedLength);
/* 190:190 */     for (int count = 0; count < this.chunks.length; count++)
/* 191:    */     {
/* 192:191 */       out.writeInt(this.x[count]);
/* 193:192 */       out.writeInt(this.z[count]);
/* 194:193 */       out.writeShort((short)(chunkMask[count] & 0xFFFF));
/* 195:194 */       out.writeShort((short)(extendedChunkMask[count] & 0xFFFF));
/* 196:    */     }
/* 197:    */   }
/* 198:    */   
/* 199:    */   public boolean isPriority()
/* 200:    */   {
/* 201:200 */     return false;
/* 202:    */   }
/* 203:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.world.ServerMultiChunkDataPacket
 * JD-Core Version:    0.7.0.1
 */