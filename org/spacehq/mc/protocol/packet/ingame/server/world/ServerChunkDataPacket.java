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
/*  15:    */ public class ServerChunkDataPacket
/*  16:    */   implements Packet
/*  17:    */ {
/*  18:    */   private int x;
/*  19:    */   private int z;
/*  20:    */   private Chunk[] chunks;
/*  21:    */   private byte[] biomeData;
/*  22:    */   
/*  23:    */   private ServerChunkDataPacket() {}
/*  24:    */   
/*  25:    */   public ServerChunkDataPacket(int x, int z)
/*  26:    */   {
/*  27: 33 */     this(x, z, new Chunk[16], new byte[256]);
/*  28:    */   }
/*  29:    */   
/*  30:    */   public ServerChunkDataPacket(int x, int z, Chunk[] chunks)
/*  31:    */   {
/*  32: 44 */     this(x, z, chunks, null);
/*  33:    */   }
/*  34:    */   
/*  35:    */   public ServerChunkDataPacket(int x, int z, Chunk[] chunks, byte[] biomeData)
/*  36:    */   {
/*  37: 56 */     if (chunks.length != 16) {
/*  38: 57 */       throw new IllegalArgumentException("Chunks length must be 16.");
/*  39:    */     }
/*  40: 60 */     boolean noSkylight = false;
/*  41: 61 */     boolean skylight = false;
/*  42: 62 */     for (int index = 0; index < chunks.length; index++) {
/*  43: 63 */       if (chunks[index] != null) {
/*  44: 64 */         if (chunks[index].getSkyLight() == null) {
/*  45: 65 */           noSkylight = true;
/*  46:    */         } else {
/*  47: 67 */           skylight = true;
/*  48:    */         }
/*  49:    */       }
/*  50:    */     }
/*  51: 72 */     if ((noSkylight) && (skylight)) {
/*  52: 73 */       throw new IllegalArgumentException("Either all chunks must have skylight values or none must have them.");
/*  53:    */     }
/*  54: 76 */     this.x = x;
/*  55: 77 */     this.z = z;
/*  56: 78 */     this.chunks = chunks;
/*  57: 79 */     this.biomeData = biomeData;
/*  58:    */   }
/*  59:    */   
/*  60:    */   public int getX()
/*  61:    */   {
/*  62: 83 */     return this.x;
/*  63:    */   }
/*  64:    */   
/*  65:    */   public int getZ()
/*  66:    */   {
/*  67: 87 */     return this.z;
/*  68:    */   }
/*  69:    */   
/*  70:    */   public Chunk[] getChunks()
/*  71:    */   {
/*  72: 91 */     return this.chunks;
/*  73:    */   }
/*  74:    */   
/*  75:    */   public byte[] getBiomeData()
/*  76:    */   {
/*  77: 95 */     return this.biomeData;
/*  78:    */   }
/*  79:    */   
/*  80:    */   public boolean isFullChunk()
/*  81:    */   {
/*  82: 99 */     return this.biomeData != null;
/*  83:    */   }
/*  84:    */   
/*  85:    */   public void read(NetInput in)
/*  86:    */     throws IOException
/*  87:    */   {
/*  88:105 */     this.x = in.readInt();
/*  89:106 */     this.z = in.readInt();
/*  90:107 */     boolean fullChunk = in.readBoolean();
/*  91:108 */     int chunkMask = in.readShort();
/*  92:109 */     int extendedChunkMask = in.readShort();
/*  93:110 */     byte[] deflated = in.readBytes(in.readInt());
/*  94:    */     
/*  95:112 */     int chunkCount = 0;
/*  96:113 */     for (int count = 0; count < 16; count++) {
/*  97:114 */       chunkCount += (chunkMask >> count & 0x1);
/*  98:    */     }
/*  99:117 */     int len = 12288 * chunkCount;
/* 100:118 */     if (fullChunk) {
/* 101:119 */       len += 256;
/* 102:    */     }
/* 103:122 */     byte[] data = new byte[len];
/* 104:    */     
/* 105:124 */     Inflater inflater = new Inflater();
/* 106:125 */     inflater.setInput(deflated, 0, deflated.length);
/* 107:    */     try
/* 108:    */     {
/* 109:127 */       inflater.inflate(data);
/* 110:    */     }
/* 111:    */     catch (DataFormatException e)
/* 112:    */     {
/* 113:129 */       throw new IOException("Bad compressed data format");
/* 114:    */     }
/* 115:    */     finally
/* 116:    */     {
/* 117:131 */       inflater.end();
/* 118:    */     }
/* 119:135 */     ParsedChunkData chunkData = NetUtil.dataToChunks(new NetworkChunkData(chunkMask, extendedChunkMask, fullChunk, false, data));
/* 120:136 */     this.chunks = chunkData.getChunks();
/* 121:137 */     this.biomeData = chunkData.getBiomes();
/* 122:    */   }
/* 123:    */   
/* 124:    */   public void write(NetOutput out)
/* 125:    */     throws IOException
/* 126:    */   {
/* 127:143 */     NetworkChunkData data = NetUtil.chunksToData(new ParsedChunkData(this.chunks, this.biomeData));
/* 128:    */     
/* 129:145 */     Deflater deflater = new Deflater(-1);
/* 130:146 */     byte[] deflated = new byte[data.getData().length];
/* 131:147 */     int len = data.getData().length;
/* 132:    */     try
/* 133:    */     {
/* 134:149 */       deflater.setInput(data.getData(), 0, data.getData().length);
/* 135:150 */       deflater.finish();
/* 136:151 */       len = deflater.deflate(deflated);
/* 137:    */     }
/* 138:    */     finally
/* 139:    */     {
/* 140:153 */       deflater.end();
/* 141:    */     }
/* 142:157 */     out.writeInt(this.x);
/* 143:158 */     out.writeInt(this.z);
/* 144:159 */     out.writeBoolean(data.isFullChunk());
/* 145:160 */     out.writeShort(data.getMask());
/* 146:161 */     out.writeShort(data.getExtendedMask());
/* 147:162 */     out.writeInt(len);
/* 148:163 */     out.writeBytes(deflated, len);
/* 149:    */   }
/* 150:    */   
/* 151:    */   public boolean isPriority()
/* 152:    */   {
/* 153:168 */     return false;
/* 154:    */   }
/* 155:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.world.ServerChunkDataPacket
 * JD-Core Version:    0.7.0.1
 */