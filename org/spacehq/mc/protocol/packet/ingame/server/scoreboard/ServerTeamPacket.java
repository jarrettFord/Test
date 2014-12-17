/*   1:    */ package org.spacehq.mc.protocol.packet.ingame.server.scoreboard;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import org.spacehq.packetlib.io.NetInput;
/*   5:    */ import org.spacehq.packetlib.io.NetOutput;
/*   6:    */ import org.spacehq.packetlib.packet.Packet;
/*   7:    */ 
/*   8:    */ public class ServerTeamPacket
/*   9:    */   implements Packet
/*  10:    */ {
/*  11:    */   private String name;
/*  12:    */   private Action action;
/*  13:    */   private String displayName;
/*  14:    */   private String prefix;
/*  15:    */   private String suffix;
/*  16:    */   private FriendlyFireMode friendlyFire;
/*  17:    */   private String[] players;
/*  18:    */   
/*  19:    */   private ServerTeamPacket() {}
/*  20:    */   
/*  21:    */   public ServerTeamPacket(String name)
/*  22:    */   {
/*  23: 24 */     this.name = name;
/*  24: 25 */     this.action = Action.REMOVE;
/*  25:    */   }
/*  26:    */   
/*  27:    */   public ServerTeamPacket(String name, Action action, String[] players)
/*  28:    */   {
/*  29: 29 */     if ((action != Action.ADD_PLAYER) && (action != Action.REMOVE_PLAYER)) {
/*  30: 30 */       throw new IllegalArgumentException("(name, action, players) constructor only valid for adding and removing players.");
/*  31:    */     }
/*  32: 33 */     this.name = name;
/*  33: 34 */     this.action = action;
/*  34: 35 */     this.players = players;
/*  35:    */   }
/*  36:    */   
/*  37:    */   public ServerTeamPacket(String name, String displayName, String prefix, String suffix, FriendlyFireMode friendlyFire)
/*  38:    */   {
/*  39: 39 */     this.name = name;
/*  40: 40 */     this.displayName = displayName;
/*  41: 41 */     this.prefix = prefix;
/*  42: 42 */     this.suffix = suffix;
/*  43: 43 */     this.friendlyFire = friendlyFire;
/*  44: 44 */     this.action = Action.UPDATE;
/*  45:    */   }
/*  46:    */   
/*  47:    */   public ServerTeamPacket(String name, String displayName, String prefix, String suffix, FriendlyFireMode friendlyFire, String[] players)
/*  48:    */   {
/*  49: 48 */     this.name = name;
/*  50: 49 */     this.displayName = displayName;
/*  51: 50 */     this.prefix = prefix;
/*  52: 51 */     this.suffix = suffix;
/*  53: 52 */     this.friendlyFire = friendlyFire;
/*  54: 53 */     this.players = players;
/*  55: 54 */     this.action = Action.CREATE;
/*  56:    */   }
/*  57:    */   
/*  58:    */   public String getTeamName()
/*  59:    */   {
/*  60: 58 */     return this.name;
/*  61:    */   }
/*  62:    */   
/*  63:    */   public Action getAction()
/*  64:    */   {
/*  65: 62 */     return this.action;
/*  66:    */   }
/*  67:    */   
/*  68:    */   public String getDisplayName()
/*  69:    */   {
/*  70: 66 */     return this.displayName;
/*  71:    */   }
/*  72:    */   
/*  73:    */   public String getPrefix()
/*  74:    */   {
/*  75: 70 */     return this.prefix;
/*  76:    */   }
/*  77:    */   
/*  78:    */   public String getSuffix()
/*  79:    */   {
/*  80: 74 */     return this.suffix;
/*  81:    */   }
/*  82:    */   
/*  83:    */   public FriendlyFireMode getFriendlyFire()
/*  84:    */   {
/*  85: 78 */     return this.friendlyFire;
/*  86:    */   }
/*  87:    */   
/*  88:    */   public String[] getPlayers()
/*  89:    */   {
/*  90: 82 */     return this.players;
/*  91:    */   }
/*  92:    */   
/*  93:    */   public void read(NetInput in)
/*  94:    */     throws IOException
/*  95:    */   {
/*  96: 87 */     this.name = in.readString();
/*  97: 88 */     this.action = Action.values()[in.readByte()];
/*  98: 89 */     if ((this.action == Action.CREATE) || (this.action == Action.UPDATE))
/*  99:    */     {
/* 100: 90 */       this.displayName = in.readString();
/* 101: 91 */       this.prefix = in.readString();
/* 102: 92 */       this.suffix = in.readString();
/* 103: 93 */       byte friendlyFire = in.readByte();
/* 104: 94 */       this.friendlyFire = (friendlyFire == 3 ? FriendlyFireMode.FRIENDLY_INVISIBLES_VISIBLE : FriendlyFireMode.values()[friendlyFire]);
/* 105:    */     }
/* 106: 97 */     if ((this.action == Action.CREATE) || (this.action == Action.ADD_PLAYER) || (this.action == Action.REMOVE_PLAYER))
/* 107:    */     {
/* 108: 98 */       this.players = new String[in.readShort()];
/* 109: 99 */       for (int index = 0; index < this.players.length; index++) {
/* 110:100 */         this.players[index] = in.readString();
/* 111:    */       }
/* 112:    */     }
/* 113:    */   }
/* 114:    */   
/* 115:    */   public void write(NetOutput out)
/* 116:    */     throws IOException
/* 117:    */   {
/* 118:107 */     out.writeString(this.name);
/* 119:108 */     out.writeByte(this.action.ordinal());
/* 120:109 */     if ((this.action == Action.CREATE) || (this.action == Action.UPDATE))
/* 121:    */     {
/* 122:110 */       out.writeString(this.displayName);
/* 123:111 */       out.writeString(this.prefix);
/* 124:112 */       out.writeString(this.suffix);
/* 125:113 */       out.writeByte(this.friendlyFire == FriendlyFireMode.FRIENDLY_INVISIBLES_VISIBLE ? 3 : this.friendlyFire.ordinal());
/* 126:    */     }
/* 127:116 */     if ((this.action == Action.CREATE) || (this.action == Action.ADD_PLAYER) || (this.action == Action.REMOVE_PLAYER))
/* 128:    */     {
/* 129:117 */       out.writeShort(this.players.length);
/* 130:118 */       for (String player : this.players) {
/* 131:119 */         out.writeString(player);
/* 132:    */       }
/* 133:    */     }
/* 134:    */   }
/* 135:    */   
/* 136:    */   public boolean isPriority()
/* 137:    */   {
/* 138:126 */     return false;
/* 139:    */   }
/* 140:    */   
/* 141:    */   public static enum Action
/* 142:    */   {
/* 143:130 */     CREATE,  REMOVE,  UPDATE,  ADD_PLAYER,  REMOVE_PLAYER;
/* 144:    */   }
/* 145:    */   
/* 146:    */   public static enum FriendlyFireMode
/* 147:    */   {
/* 148:138 */     OFF,  ON,  FRIENDLY_INVISIBLES_VISIBLE;
/* 149:    */   }
/* 150:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.scoreboard.ServerTeamPacket
 * JD-Core Version:    0.7.0.1
 */