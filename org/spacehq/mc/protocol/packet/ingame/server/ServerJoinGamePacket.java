/*   1:    */ package org.spacehq.mc.protocol.packet.ingame.server;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import org.spacehq.packetlib.io.NetInput;
/*   5:    */ import org.spacehq.packetlib.io.NetOutput;
/*   6:    */ import org.spacehq.packetlib.packet.Packet;
/*   7:    */ 
/*   8:    */ public class ServerJoinGamePacket
/*   9:    */   implements Packet
/*  10:    */ {
/*  11:    */   private int entityId;
/*  12:    */   private boolean hardcore;
/*  13:    */   private GameMode gamemode;
/*  14:    */   private int dimension;
/*  15:    */   private Difficulty difficulty;
/*  16:    */   private int maxPlayers;
/*  17:    */   private WorldType worldType;
/*  18:    */   
/*  19:    */   private ServerJoinGamePacket() {}
/*  20:    */   
/*  21:    */   public ServerJoinGamePacket(int entityId, boolean hardcore, GameMode gamemode, int dimension, Difficulty difficulty, int maxPlayers, WorldType worldType)
/*  22:    */   {
/*  23: 24 */     this.entityId = entityId;
/*  24: 25 */     this.hardcore = hardcore;
/*  25: 26 */     this.gamemode = gamemode;
/*  26: 27 */     this.dimension = dimension;
/*  27: 28 */     this.difficulty = difficulty;
/*  28: 29 */     this.maxPlayers = maxPlayers;
/*  29: 30 */     this.worldType = worldType;
/*  30:    */   }
/*  31:    */   
/*  32:    */   public int getEntityId()
/*  33:    */   {
/*  34: 34 */     return this.entityId;
/*  35:    */   }
/*  36:    */   
/*  37:    */   public boolean getHardcore()
/*  38:    */   {
/*  39: 38 */     return this.hardcore;
/*  40:    */   }
/*  41:    */   
/*  42:    */   public GameMode getGameMode()
/*  43:    */   {
/*  44: 42 */     return this.gamemode;
/*  45:    */   }
/*  46:    */   
/*  47:    */   public int getDimension()
/*  48:    */   {
/*  49: 46 */     return this.dimension;
/*  50:    */   }
/*  51:    */   
/*  52:    */   public Difficulty getDifficulty()
/*  53:    */   {
/*  54: 50 */     return this.difficulty;
/*  55:    */   }
/*  56:    */   
/*  57:    */   public int getMaxPlayers()
/*  58:    */   {
/*  59: 54 */     return this.maxPlayers;
/*  60:    */   }
/*  61:    */   
/*  62:    */   public WorldType getWorldType()
/*  63:    */   {
/*  64: 58 */     return this.worldType;
/*  65:    */   }
/*  66:    */   
/*  67:    */   public void read(NetInput in)
/*  68:    */     throws IOException
/*  69:    */   {
/*  70: 63 */     this.entityId = in.readInt();
/*  71: 64 */     int gamemode = in.readUnsignedByte();
/*  72: 65 */     this.hardcore = ((gamemode & 0x8) == 8);
/*  73: 66 */     gamemode &= 0xFFFFFFF7;
/*  74: 67 */     this.gamemode = GameMode.values()[gamemode];
/*  75: 68 */     this.dimension = in.readByte();
/*  76: 69 */     this.difficulty = Difficulty.values()[in.readUnsignedByte()];
/*  77: 70 */     this.maxPlayers = in.readUnsignedByte();
/*  78: 71 */     this.worldType = nameToType(in.readString());
/*  79:    */   }
/*  80:    */   
/*  81:    */   public void write(NetOutput out)
/*  82:    */     throws IOException
/*  83:    */   {
/*  84: 76 */     out.writeInt(this.entityId);
/*  85: 77 */     int gamemode = this.gamemode.ordinal();
/*  86: 78 */     if (this.hardcore) {
/*  87: 79 */       gamemode |= 0x8;
/*  88:    */     }
/*  89: 82 */     out.writeByte(gamemode);
/*  90: 83 */     out.writeByte(this.dimension);
/*  91: 84 */     out.writeByte(this.difficulty.ordinal());
/*  92: 85 */     out.writeByte(this.maxPlayers);
/*  93: 86 */     out.writeString(typeToName(this.worldType));
/*  94:    */   }
/*  95:    */   
/*  96:    */   public boolean isPriority()
/*  97:    */   {
/*  98: 91 */     return false;
/*  99:    */   }
/* 100:    */   
/* 101:    */   private static String typeToName(WorldType type)
/* 102:    */     throws IOException
/* 103:    */   {
/* 104: 95 */     if (type == WorldType.DEFAULT) {
/* 105: 96 */       return "default";
/* 106:    */     }
/* 107: 97 */     if (type == WorldType.FLAT) {
/* 108: 98 */       return "flat";
/* 109:    */     }
/* 110: 99 */     if (type == WorldType.LARGE_BIOMES) {
/* 111:100 */       return "largeBiomes";
/* 112:    */     }
/* 113:101 */     if (type == WorldType.AMPLIFIED) {
/* 114:102 */       return "amplified";
/* 115:    */     }
/* 116:103 */     if (type == WorldType.DEFAULT_1_1) {
/* 117:104 */       return "default_1_1";
/* 118:    */     }
/* 119:106 */     throw new IOException("Unmapped world type: " + type);
/* 120:    */   }
/* 121:    */   
/* 122:    */   private static WorldType nameToType(String name)
/* 123:    */     throws IOException
/* 124:    */   {
/* 125:111 */     if (name.equalsIgnoreCase("default")) {
/* 126:112 */       return WorldType.DEFAULT;
/* 127:    */     }
/* 128:113 */     if (name.equalsIgnoreCase("flat")) {
/* 129:114 */       return WorldType.FLAT;
/* 130:    */     }
/* 131:115 */     if (name.equalsIgnoreCase("largeBiomes")) {
/* 132:116 */       return WorldType.LARGE_BIOMES;
/* 133:    */     }
/* 134:117 */     if (name.equalsIgnoreCase("amplified")) {
/* 135:118 */       return WorldType.AMPLIFIED;
/* 136:    */     }
/* 137:119 */     if (name.equalsIgnoreCase("default_1_1")) {
/* 138:120 */       return WorldType.DEFAULT_1_1;
/* 139:    */     }
/* 140:122 */     throw new IOException("Unknown world type: " + name);
/* 141:    */   }
/* 142:    */   
/* 143:    */   public static enum GameMode
/* 144:    */   {
/* 145:127 */     SURVIVAL,  CREATIVE,  ADVENTURE;
/* 146:    */   }
/* 147:    */   
/* 148:    */   public static enum Difficulty
/* 149:    */   {
/* 150:133 */     PEACEFUL,  EASY,  NORMAL,  HARD;
/* 151:    */   }
/* 152:    */   
/* 153:    */   public static enum WorldType
/* 154:    */   {
/* 155:140 */     DEFAULT,  FLAT,  LARGE_BIOMES,  AMPLIFIED,  DEFAULT_1_1;
/* 156:    */   }
/* 157:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.ServerJoinGamePacket
 * JD-Core Version:    0.7.0.1
 */