/*   1:    */ package org.spacehq.mc.protocol.packet.ingame.server.world;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import java.util.ArrayList;
/*   5:    */ import java.util.List;
/*   6:    */ import org.spacehq.packetlib.io.NetInput;
/*   7:    */ import org.spacehq.packetlib.io.NetOutput;
/*   8:    */ import org.spacehq.packetlib.packet.Packet;
/*   9:    */ 
/*  10:    */ public class ServerMapDataPacket
/*  11:    */   implements Packet
/*  12:    */ {
/*  13:    */   private int mapId;
/*  14:    */   private Type type;
/*  15:    */   private MapData data;
/*  16:    */   
/*  17:    */   private ServerMapDataPacket() {}
/*  18:    */   
/*  19:    */   public ServerMapDataPacket(int mapId, Type type, MapData data)
/*  20:    */   {
/*  21: 22 */     this.mapId = mapId;
/*  22: 23 */     this.type = type;
/*  23: 24 */     this.data = data;
/*  24:    */   }
/*  25:    */   
/*  26:    */   public int getMapId()
/*  27:    */   {
/*  28: 28 */     return this.mapId;
/*  29:    */   }
/*  30:    */   
/*  31:    */   public Type getType()
/*  32:    */   {
/*  33: 32 */     return this.type;
/*  34:    */   }
/*  35:    */   
/*  36:    */   public MapData getData()
/*  37:    */   {
/*  38: 36 */     return this.data;
/*  39:    */   }
/*  40:    */   
/*  41:    */   public void read(NetInput in)
/*  42:    */     throws IOException
/*  43:    */   {
/*  44: 41 */     this.mapId = in.readVarInt();
/*  45: 42 */     byte[] data = in.readBytes(in.readShort());
/*  46: 43 */     this.type = Type.values()[data[0]];
/*  47: 44 */     switch (this.type)
/*  48:    */     {
/*  49:    */     case IMAGE: 
/*  50: 46 */       int x = data[1] & 0xFF;
/*  51: 47 */       int y = data[2] & 0xFF;
/*  52: 48 */       int height = data.length - 3;
/*  53: 49 */       byte[] colors = new byte[height];
/*  54: 50 */       for (int index = 0; index < height; index++) {
/*  55: 51 */         colors[index] = data[(index + 3)];
/*  56:    */       }
/*  57: 54 */       this.data = new MapColumnUpdate(x, y, height, colors);
/*  58: 55 */       break;
/*  59:    */     case PLAYERS: 
/*  60: 57 */       List<MapPlayer> players = new ArrayList();
/*  61: 58 */       for (int index = 0; index < (data.length - 1) / 3; index++)
/*  62:    */       {
/*  63: 59 */         int sizeRot = data[(index * 3 + 1)] & 0xFF;
/*  64: 60 */         int iconSize = sizeRot >> 4 & 0xFF;
/*  65: 61 */         int iconRotation = sizeRot & 0xF & 0xFF;
/*  66: 62 */         int centerX = data[(index * 3 + 2)] & 0xFF;
/*  67: 63 */         int centerY = data[(index * 3 + 3)] & 0xFF;
/*  68: 64 */         players.add(new MapPlayer(iconSize, iconRotation, centerX, centerY));
/*  69:    */       }
/*  70: 67 */       this.data = new MapPlayers(players);
/*  71: 68 */       break;
/*  72:    */     case SCALE: 
/*  73: 70 */       this.data = new MapScale(data[1] & 0xFF);
/*  74:    */     }
/*  75:    */   }
/*  76:    */   
/*  77:    */   public void write(NetOutput out)
/*  78:    */     throws IOException
/*  79:    */   {
/*  80: 77 */     out.writeVarInt(this.mapId);
/*  81: 78 */     byte[] data = null;
/*  82: 79 */     switch (this.type)
/*  83:    */     {
/*  84:    */     case IMAGE: 
/*  85: 81 */       MapColumnUpdate column = (MapColumnUpdate)this.data;
/*  86: 82 */       data = new byte[column.getHeight() + 3];
/*  87: 83 */       data[0] = 0;
/*  88: 84 */       data[1] = ((byte)column.getX());
/*  89: 85 */       data[2] = ((byte)column.getY());
/*  90: 86 */       for (int index = 3; index < data.length; index++) {
/*  91: 87 */         data[index] = column.getColors()[(index - 3)];
/*  92:    */       }
/*  93: 90 */       break;
/*  94:    */     case PLAYERS: 
/*  95: 92 */       MapPlayers players = (MapPlayers)this.data;
/*  96: 93 */       data = new byte[players.getPlayers().size() * 3 + 1];
/*  97: 94 */       data[0] = 1;
/*  98: 95 */       for (int index = 0; index < players.getPlayers().size(); index++)
/*  99:    */       {
/* 100: 96 */         MapPlayer player = (MapPlayer)players.getPlayers().get(index);
/* 101: 97 */         data[(index * 3 + 1)] = ((byte)((byte)player.getIconSize() << 4 | (byte)player.getIconRotation() & 0xF));
/* 102: 98 */         data[(index * 3 + 2)] = ((byte)player.getCenterX());
/* 103: 99 */         data[(index * 3 + 3)] = ((byte)player.getCenterZ());
/* 104:    */       }
/* 105:102 */       break;
/* 106:    */     case SCALE: 
/* 107:104 */       MapScale scale = (MapScale)this.data;
/* 108:105 */       data = new byte[2];
/* 109:106 */       data[0] = 2;
/* 110:107 */       data[1] = ((byte)scale.getScale());
/* 111:    */     }
/* 112:111 */     out.writeShort(data.length);
/* 113:112 */     out.writeBytes(data);
/* 114:    */   }
/* 115:    */   
/* 116:    */   public boolean isPriority()
/* 117:    */   {
/* 118:117 */     return false;
/* 119:    */   }
/* 120:    */   
/* 121:    */   public static enum Type
/* 122:    */   {
/* 123:121 */     IMAGE,  PLAYERS,  SCALE;
/* 124:    */   }
/* 125:    */   
/* 126:    */   public static class MapColumnUpdate
/* 127:    */     implements ServerMapDataPacket.MapData
/* 128:    */   {
/* 129:    */     private int x;
/* 130:    */     private int y;
/* 131:    */     private int height;
/* 132:    */     private byte[] colors;
/* 133:    */     
/* 134:    */     public MapColumnUpdate(int x, int y, int height, byte[] colors)
/* 135:    */     {
/* 136:143 */       this.x = x;
/* 137:144 */       this.y = y;
/* 138:145 */       this.height = height;
/* 139:146 */       this.colors = colors;
/* 140:    */     }
/* 141:    */     
/* 142:    */     public int getX()
/* 143:    */     {
/* 144:150 */       return this.x;
/* 145:    */     }
/* 146:    */     
/* 147:    */     public int getY()
/* 148:    */     {
/* 149:154 */       return this.y;
/* 150:    */     }
/* 151:    */     
/* 152:    */     public int getHeight()
/* 153:    */     {
/* 154:158 */       return this.height;
/* 155:    */     }
/* 156:    */     
/* 157:    */     public byte[] getColors()
/* 158:    */     {
/* 159:162 */       return this.colors;
/* 160:    */     }
/* 161:    */   }
/* 162:    */   
/* 163:    */   public static class MapPlayers
/* 164:    */     implements ServerMapDataPacket.MapData
/* 165:    */   {
/* 166:167 */     private List<ServerMapDataPacket.MapPlayer> players = new ArrayList();
/* 167:    */     
/* 168:    */     public MapPlayers(List<ServerMapDataPacket.MapPlayer> players)
/* 169:    */     {
/* 170:170 */       this.players = players;
/* 171:    */     }
/* 172:    */     
/* 173:    */     public List<ServerMapDataPacket.MapPlayer> getPlayers()
/* 174:    */     {
/* 175:174 */       return new ArrayList(this.players);
/* 176:    */     }
/* 177:    */   }
/* 178:    */   
/* 179:    */   public static class MapPlayer
/* 180:    */   {
/* 181:    */     private int iconSize;
/* 182:    */     private int iconRotation;
/* 183:    */     private int centerX;
/* 184:    */     private int centerZ;
/* 185:    */     
/* 186:    */     public MapPlayer(int iconSize, int iconRotation, int centerX, int centerZ)
/* 187:    */     {
/* 188:185 */       this.iconSize = iconSize;
/* 189:186 */       this.iconRotation = iconRotation;
/* 190:187 */       this.centerX = centerX;
/* 191:188 */       this.centerZ = centerZ;
/* 192:    */     }
/* 193:    */     
/* 194:    */     public int getIconSize()
/* 195:    */     {
/* 196:192 */       return this.iconSize;
/* 197:    */     }
/* 198:    */     
/* 199:    */     public int getIconRotation()
/* 200:    */     {
/* 201:196 */       return this.iconRotation;
/* 202:    */     }
/* 203:    */     
/* 204:    */     public int getCenterX()
/* 205:    */     {
/* 206:200 */       return this.centerX;
/* 207:    */     }
/* 208:    */     
/* 209:    */     public int getCenterZ()
/* 210:    */     {
/* 211:204 */       return this.centerZ;
/* 212:    */     }
/* 213:    */   }
/* 214:    */   
/* 215:    */   public static class MapScale
/* 216:    */     implements ServerMapDataPacket.MapData
/* 217:    */   {
/* 218:    */     private int scale;
/* 219:    */     
/* 220:    */     public MapScale(int scale)
/* 221:    */     {
/* 222:212 */       this.scale = scale;
/* 223:    */     }
/* 224:    */     
/* 225:    */     public int getScale()
/* 226:    */     {
/* 227:216 */       return this.scale;
/* 228:    */     }
/* 229:    */   }
/* 230:    */   
/* 231:    */   public static abstract interface MapData {}
/* 232:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.world.ServerMapDataPacket
 * JD-Core Version:    0.7.0.1
 */