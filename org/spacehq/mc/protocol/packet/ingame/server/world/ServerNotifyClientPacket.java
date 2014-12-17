/*   1:    */ package org.spacehq.mc.protocol.packet.ingame.server.world;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import org.spacehq.packetlib.io.NetInput;
/*   5:    */ import org.spacehq.packetlib.io.NetOutput;
/*   6:    */ import org.spacehq.packetlib.packet.Packet;
/*   7:    */ 
/*   8:    */ public class ServerNotifyClientPacket
/*   9:    */   implements Packet
/*  10:    */ {
/*  11:    */   private Notification notification;
/*  12:    */   private NotificationValue value;
/*  13:    */   
/*  14:    */   private ServerNotifyClientPacket() {}
/*  15:    */   
/*  16:    */   public ServerNotifyClientPacket(Notification notification, NotificationValue value)
/*  17:    */   {
/*  18: 19 */     this.notification = notification;
/*  19: 20 */     this.value = value;
/*  20:    */   }
/*  21:    */   
/*  22:    */   public Notification getNotification()
/*  23:    */   {
/*  24: 24 */     return this.notification;
/*  25:    */   }
/*  26:    */   
/*  27:    */   public NotificationValue getValue()
/*  28:    */   {
/*  29: 28 */     return this.value;
/*  30:    */   }
/*  31:    */   
/*  32:    */   public void read(NetInput in)
/*  33:    */     throws IOException
/*  34:    */   {
/*  35: 33 */     this.notification = Notification.values()[in.readUnsignedByte()];
/*  36: 34 */     this.value = floatToValue(in.readFloat());
/*  37:    */   }
/*  38:    */   
/*  39:    */   public void write(NetOutput out)
/*  40:    */     throws IOException
/*  41:    */   {
/*  42: 39 */     out.writeByte(this.notification.ordinal());
/*  43: 40 */     out.writeFloat(valueToFloat(this.value));
/*  44:    */   }
/*  45:    */   
/*  46:    */   public boolean isPriority()
/*  47:    */   {
/*  48: 45 */     return false;
/*  49:    */   }
/*  50:    */   
/*  51:    */   private NotificationValue floatToValue(float f)
/*  52:    */   {
/*  53: 49 */     if (this.notification == Notification.CHANGE_GAMEMODE)
/*  54:    */     {
/*  55: 50 */       if (f == 0.0F) {
/*  56: 51 */         return GameModeValue.SURVIVAL;
/*  57:    */       }
/*  58: 52 */       if (f == 1.0F) {
/*  59: 53 */         return GameModeValue.CREATIVE;
/*  60:    */       }
/*  61: 54 */       if (f == 2.0F) {
/*  62: 55 */         return GameModeValue.ADVENTURE;
/*  63:    */       }
/*  64:    */     }
/*  65: 57 */     else if (this.notification == Notification.DEMO_MESSAGE)
/*  66:    */     {
/*  67: 58 */       if (f == 0.0F) {
/*  68: 59 */         return DemoMessageValue.WELCOME;
/*  69:    */       }
/*  70: 60 */       if (f == 101.0F) {
/*  71: 61 */         return DemoMessageValue.MOVEMENT_CONTROLS;
/*  72:    */       }
/*  73: 62 */       if (f == 102.0F) {
/*  74: 63 */         return DemoMessageValue.JUMP_CONTROL;
/*  75:    */       }
/*  76: 64 */       if (f == 103.0F) {
/*  77: 65 */         return DemoMessageValue.INVENTORY_CONTROL;
/*  78:    */       }
/*  79:    */     }
/*  80:    */     else
/*  81:    */     {
/*  82: 67 */       if (this.notification == Notification.RAIN_STRENGTH) {
/*  83: 68 */         return new RainStrengthValue((int)f);
/*  84:    */       }
/*  85: 69 */       if (this.notification == Notification.THUNDER_STRENGTH) {
/*  86: 70 */         return new ThunderStrengthValue((int)f);
/*  87:    */       }
/*  88:    */     }
/*  89: 73 */     return null;
/*  90:    */   }
/*  91:    */   
/*  92:    */   private float valueToFloat(NotificationValue value)
/*  93:    */   {
/*  94: 77 */     if (value == GameModeValue.SURVIVAL) {
/*  95: 78 */       return 0.0F;
/*  96:    */     }
/*  97: 79 */     if (value == GameModeValue.CREATIVE) {
/*  98: 80 */       return 1.0F;
/*  99:    */     }
/* 100: 81 */     if (value == GameModeValue.ADVENTURE) {
/* 101: 82 */       return 2.0F;
/* 102:    */     }
/* 103: 85 */     if (value == DemoMessageValue.WELCOME) {
/* 104: 86 */       return 0.0F;
/* 105:    */     }
/* 106: 87 */     if (value == DemoMessageValue.MOVEMENT_CONTROLS) {
/* 107: 88 */       return 101.0F;
/* 108:    */     }
/* 109: 89 */     if (value == DemoMessageValue.JUMP_CONTROL) {
/* 110: 90 */       return 102.0F;
/* 111:    */     }
/* 112: 91 */     if (value == DemoMessageValue.INVENTORY_CONTROL) {
/* 113: 92 */       return 103.0F;
/* 114:    */     }
/* 115: 95 */     if ((value instanceof RainStrengthValue)) {
/* 116: 96 */       return ((RainStrengthValue)value).getStrength();
/* 117:    */     }
/* 118: 99 */     if ((value instanceof ThunderStrengthValue)) {
/* 119:100 */       return ((ThunderStrengthValue)value).getStrength();
/* 120:    */     }
/* 121:103 */     return 0.0F;
/* 122:    */   }
/* 123:    */   
/* 124:    */   public static enum Notification
/* 125:    */   {
/* 126:107 */     INVALID_BED,  START_RAIN,  STOP_RAIN,  CHANGE_GAMEMODE,  ENTER_CREDITS,  DEMO_MESSAGE,  ARROW_HIT_PLAYER,  RAIN_STRENGTH,  THUNDER_STRENGTH;
/* 127:    */   }
/* 128:    */   
/* 129:    */   public static enum GameModeValue
/* 130:    */     implements ServerNotifyClientPacket.NotificationValue
/* 131:    */   {
/* 132:122 */     SURVIVAL,  CREATIVE,  ADVENTURE;
/* 133:    */   }
/* 134:    */   
/* 135:    */   public static enum DemoMessageValue
/* 136:    */     implements ServerNotifyClientPacket.NotificationValue
/* 137:    */   {
/* 138:128 */     WELCOME,  MOVEMENT_CONTROLS,  JUMP_CONTROL,  INVENTORY_CONTROL;
/* 139:    */   }
/* 140:    */   
/* 141:    */   public static class RainStrengthValue
/* 142:    */     implements ServerNotifyClientPacket.NotificationValue
/* 143:    */   {
/* 144:    */     private float strength;
/* 145:    */     
/* 146:    */     public RainStrengthValue(float strength)
/* 147:    */     {
/* 148:138 */       if (strength > 1.0F) {
/* 149:139 */         strength = 1.0F;
/* 150:    */       }
/* 151:142 */       if (strength < 0.0F) {
/* 152:143 */         strength = 0.0F;
/* 153:    */       }
/* 154:146 */       this.strength = strength;
/* 155:    */     }
/* 156:    */     
/* 157:    */     public float getStrength()
/* 158:    */     {
/* 159:150 */       return this.strength;
/* 160:    */     }
/* 161:    */   }
/* 162:    */   
/* 163:    */   public static class ThunderStrengthValue
/* 164:    */     implements ServerNotifyClientPacket.NotificationValue
/* 165:    */   {
/* 166:    */     private float strength;
/* 167:    */     
/* 168:    */     public ThunderStrengthValue(float strength)
/* 169:    */     {
/* 170:158 */       if (strength > 1.0F) {
/* 171:159 */         strength = 1.0F;
/* 172:    */       }
/* 173:162 */       if (strength < 0.0F) {
/* 174:163 */         strength = 0.0F;
/* 175:    */       }
/* 176:166 */       this.strength = strength;
/* 177:    */     }
/* 178:    */     
/* 179:    */     public float getStrength()
/* 180:    */     {
/* 181:170 */       return this.strength;
/* 182:    */     }
/* 183:    */   }
/* 184:    */   
/* 185:    */   public static abstract interface NotificationValue {}
/* 186:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.world.ServerNotifyClientPacket
 * JD-Core Version:    0.7.0.1
 */