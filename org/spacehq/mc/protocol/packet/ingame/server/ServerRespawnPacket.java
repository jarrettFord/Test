/*   1:    */ package org.spacehq.mc.protocol.packet.ingame.server;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import org.spacehq.packetlib.io.NetInput;
/*   5:    */ import org.spacehq.packetlib.io.NetOutput;
/*   6:    */ import org.spacehq.packetlib.packet.Packet;
/*   7:    */ 
/*   8:    */ public class ServerRespawnPacket
/*   9:    */   implements Packet
/*  10:    */ {
/*  11:    */   private int dimension;
/*  12:    */   private Difficulty difficulty;
/*  13:    */   private GameMode gamemode;
/*  14:    */   private WorldType worldType;
/*  15:    */   
/*  16:    */   private ServerRespawnPacket() {}
/*  17:    */   
/*  18:    */   public ServerRespawnPacket(int dimension, Difficulty difficulty, GameMode gamemode, WorldType worldType)
/*  19:    */   {
/*  20: 21 */     this.dimension = dimension;
/*  21: 22 */     this.difficulty = difficulty;
/*  22: 23 */     this.gamemode = gamemode;
/*  23: 24 */     this.worldType = worldType;
/*  24:    */   }
/*  25:    */   
/*  26:    */   public int getDimension()
/*  27:    */   {
/*  28: 28 */     return this.dimension;
/*  29:    */   }
/*  30:    */   
/*  31:    */   public Difficulty getDifficulty()
/*  32:    */   {
/*  33: 32 */     return this.difficulty;
/*  34:    */   }
/*  35:    */   
/*  36:    */   public GameMode getGameMode()
/*  37:    */   {
/*  38: 36 */     return this.gamemode;
/*  39:    */   }
/*  40:    */   
/*  41:    */   public WorldType getWorldType()
/*  42:    */   {
/*  43: 40 */     return this.worldType;
/*  44:    */   }
/*  45:    */   
/*  46:    */   public void read(NetInput in)
/*  47:    */     throws IOException
/*  48:    */   {
/*  49: 45 */     this.dimension = in.readInt();
/*  50: 46 */     this.difficulty = Difficulty.values()[in.readUnsignedByte()];
/*  51: 47 */     this.gamemode = GameMode.values()[in.readUnsignedByte()];
/*  52: 48 */     this.worldType = nameToType(in.readString());
/*  53:    */   }
/*  54:    */   
/*  55:    */   public void write(NetOutput out)
/*  56:    */     throws IOException
/*  57:    */   {
/*  58: 53 */     out.writeInt(this.dimension);
/*  59: 54 */     out.writeByte(this.difficulty.ordinal());
/*  60: 55 */     out.writeByte(this.gamemode.ordinal());
/*  61: 56 */     out.writeString(typeToName(this.worldType));
/*  62:    */   }
/*  63:    */   
/*  64:    */   public boolean isPriority()
/*  65:    */   {
/*  66: 61 */     return false;
/*  67:    */   }
/*  68:    */   
/*  69:    */   private static String typeToName(WorldType type)
/*  70:    */     throws IOException
/*  71:    */   {
/*  72: 65 */     if (type == WorldType.DEFAULT) {
/*  73: 66 */       return "default";
/*  74:    */     }
/*  75: 67 */     if (type == WorldType.FLAT) {
/*  76: 68 */       return "flat";
/*  77:    */     }
/*  78: 69 */     if (type == WorldType.LARGE_BIOMES) {
/*  79: 70 */       return "largeBiomes";
/*  80:    */     }
/*  81: 71 */     if (type == WorldType.AMPLIFIED) {
/*  82: 72 */       return "amplified";
/*  83:    */     }
/*  84: 73 */     if (type == WorldType.DEFAULT_1_1) {
/*  85: 74 */       return "default_1_1";
/*  86:    */     }
/*  87: 76 */     throw new IOException("Unmapped world type: " + type);
/*  88:    */   }
/*  89:    */   
/*  90:    */   private static WorldType nameToType(String name)
/*  91:    */     throws IOException
/*  92:    */   {
/*  93: 81 */     if (name.equalsIgnoreCase("default")) {
/*  94: 82 */       return WorldType.DEFAULT;
/*  95:    */     }
/*  96: 83 */     if (name.equalsIgnoreCase("flat")) {
/*  97: 84 */       return WorldType.FLAT;
/*  98:    */     }
/*  99: 85 */     if (name.equalsIgnoreCase("largeBiomes")) {
/* 100: 86 */       return WorldType.LARGE_BIOMES;
/* 101:    */     }
/* 102: 87 */     if (name.equalsIgnoreCase("amplified")) {
/* 103: 88 */       return WorldType.AMPLIFIED;
/* 104:    */     }
/* 105: 89 */     if (name.equalsIgnoreCase("default_1_1")) {
/* 106: 90 */       return WorldType.DEFAULT_1_1;
/* 107:    */     }
/* 108: 92 */     throw new IOException("Unknown world type: " + name);
/* 109:    */   }
/* 110:    */   
/* 111:    */   public static enum GameMode
/* 112:    */   {
/* 113: 97 */     SURVIVAL,  CREATIVE,  ADVENTURE;
/* 114:    */   }
/* 115:    */   
/* 116:    */   public static enum Difficulty
/* 117:    */   {
/* 118:103 */     PEACEFUL,  EASY,  NORMAL,  HARD;
/* 119:    */   }
/* 120:    */   
/* 121:    */   public static enum WorldType
/* 122:    */   {
/* 123:110 */     DEFAULT,  FLAT,  LARGE_BIOMES,  AMPLIFIED,  DEFAULT_1_1;
/* 124:    */   }
/* 125:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.ServerRespawnPacket
 * JD-Core Version:    0.7.0.1
 */