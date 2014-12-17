/*   1:    */ package org.spacehq.mc.protocol.packet.ingame.client.window;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import org.spacehq.mc.protocol.data.game.ItemStack;
/*   5:    */ import org.spacehq.mc.protocol.util.NetUtil;
/*   6:    */ import org.spacehq.packetlib.io.NetInput;
/*   7:    */ import org.spacehq.packetlib.io.NetOutput;
/*   8:    */ import org.spacehq.packetlib.packet.Packet;
/*   9:    */ 
/*  10:    */ public class ClientWindowActionPacket
/*  11:    */   implements Packet
/*  12:    */ {
/*  13:    */   private int windowId;
/*  14:    */   private int slot;
/*  15:    */   private ActionParam param;
/*  16:    */   private int actionId;
/*  17:    */   private Action action;
/*  18:    */   private ItemStack clicked;
/*  19:    */   
/*  20:    */   private ClientWindowActionPacket() {}
/*  21:    */   
/*  22:    */   public ClientWindowActionPacket(int windowId, int actionId, int slot, ItemStack clicked, Action action, ActionParam param)
/*  23:    */   {
/*  24: 25 */     this.windowId = windowId;
/*  25: 26 */     this.actionId = actionId;
/*  26: 27 */     this.slot = slot;
/*  27: 28 */     this.clicked = clicked;
/*  28: 29 */     this.action = action;
/*  29: 30 */     this.param = param;
/*  30:    */   }
/*  31:    */   
/*  32:    */   public int getWindowId()
/*  33:    */   {
/*  34: 34 */     return this.windowId;
/*  35:    */   }
/*  36:    */   
/*  37:    */   public int getActionId()
/*  38:    */   {
/*  39: 38 */     return this.actionId;
/*  40:    */   }
/*  41:    */   
/*  42:    */   public int getSlot()
/*  43:    */   {
/*  44: 42 */     return this.slot;
/*  45:    */   }
/*  46:    */   
/*  47:    */   public ItemStack getClickedItem()
/*  48:    */   {
/*  49: 46 */     return this.clicked;
/*  50:    */   }
/*  51:    */   
/*  52:    */   public Action getAction()
/*  53:    */   {
/*  54: 50 */     return this.action;
/*  55:    */   }
/*  56:    */   
/*  57:    */   public ActionParam getParam()
/*  58:    */   {
/*  59: 54 */     return this.param;
/*  60:    */   }
/*  61:    */   
/*  62:    */   public void read(NetInput in)
/*  63:    */     throws IOException
/*  64:    */   {
/*  65: 59 */     this.windowId = in.readByte();
/*  66: 60 */     this.slot = in.readShort();
/*  67: 61 */     byte param = in.readByte();
/*  68: 62 */     this.actionId = in.readShort();
/*  69: 63 */     byte id = in.readByte();
/*  70: 64 */     this.action = Action.values()[id];
/*  71: 65 */     this.clicked = NetUtil.readItem(in);
/*  72: 66 */     this.param = valueToParam(param);
/*  73:    */   }
/*  74:    */   
/*  75:    */   public void write(NetOutput out)
/*  76:    */     throws IOException
/*  77:    */   {
/*  78: 71 */     out.writeByte(this.windowId);
/*  79: 72 */     out.writeShort(this.slot);
/*  80: 73 */     out.writeByte(paramToValue(this.param));
/*  81: 74 */     out.writeShort(this.actionId);
/*  82: 75 */     out.writeByte(this.action.ordinal());
/*  83: 76 */     NetUtil.writeItem(out, this.clicked);
/*  84:    */   }
/*  85:    */   
/*  86:    */   public boolean isPriority()
/*  87:    */   {
/*  88: 81 */     return false;
/*  89:    */   }
/*  90:    */   
/*  91:    */   private byte paramToValue(ActionParam param)
/*  92:    */     throws IOException
/*  93:    */   {
/*  94: 85 */     if (param == ClickItemParam.LEFT_CLICK) {
/*  95: 86 */       return 0;
/*  96:    */     }
/*  97: 87 */     if (param == ClickItemParam.RIGHT_CLICK) {
/*  98: 88 */       return 1;
/*  99:    */     }
/* 100: 91 */     if (param == ShiftClickItemParam.LEFT_CLICK) {
/* 101: 92 */       return 0;
/* 102:    */     }
/* 103: 93 */     if (param == ShiftClickItemParam.RIGHT_CLICK) {
/* 104: 94 */       return 1;
/* 105:    */     }
/* 106: 97 */     if (param == MoveToHotbarParam.SLOT_1) {
/* 107: 98 */       return 0;
/* 108:    */     }
/* 109: 99 */     if (param == MoveToHotbarParam.SLOT_2) {
/* 110:100 */       return 1;
/* 111:    */     }
/* 112:101 */     if (param == MoveToHotbarParam.SLOT_3) {
/* 113:102 */       return 2;
/* 114:    */     }
/* 115:103 */     if (param == MoveToHotbarParam.SLOT_4) {
/* 116:104 */       return 3;
/* 117:    */     }
/* 118:105 */     if (param == MoveToHotbarParam.SLOT_5) {
/* 119:106 */       return 4;
/* 120:    */     }
/* 121:107 */     if (param == MoveToHotbarParam.SLOT_6) {
/* 122:108 */       return 5;
/* 123:    */     }
/* 124:109 */     if (param == MoveToHotbarParam.SLOT_7) {
/* 125:110 */       return 6;
/* 126:    */     }
/* 127:111 */     if (param == MoveToHotbarParam.SLOT_8) {
/* 128:112 */       return 7;
/* 129:    */     }
/* 130:113 */     if (param == MoveToHotbarParam.SLOT_9) {
/* 131:114 */       return 8;
/* 132:    */     }
/* 133:117 */     if (param == CreativeGrabParam.GRAB) {
/* 134:118 */       return 2;
/* 135:    */     }
/* 136:121 */     if (param == DropItemParam.DROP_FROM_SELECTED) {
/* 137:122 */       return 0;
/* 138:    */     }
/* 139:123 */     if (param == DropItemParam.DROP_SELECTED_STACK) {
/* 140:124 */       return 1;
/* 141:    */     }
/* 142:125 */     if (param == DropItemParam.LEFT_CLICK_OUTSIDE_NOT_HOLDING) {
/* 143:126 */       return 0;
/* 144:    */     }
/* 145:127 */     if (param == DropItemParam.RIGHT_CLICK_OUTSIDE_NOT_HOLDING) {
/* 146:128 */       return 1;
/* 147:    */     }
/* 148:131 */     if (param == SpreadItemParam.LEFT_MOUSE_BEGIN_DRAG) {
/* 149:132 */       return 0;
/* 150:    */     }
/* 151:133 */     if (param == SpreadItemParam.LEFT_MOUSE_ADD_SLOT) {
/* 152:134 */       return 1;
/* 153:    */     }
/* 154:135 */     if (param == SpreadItemParam.LEFT_MOUSE_END_DRAG) {
/* 155:136 */       return 2;
/* 156:    */     }
/* 157:137 */     if (param == SpreadItemParam.RIGHT_MOUSE_BEGIN_DRAG) {
/* 158:138 */       return 4;
/* 159:    */     }
/* 160:139 */     if (param == SpreadItemParam.RIGHT_MOUSE_ADD_SLOT) {
/* 161:140 */       return 5;
/* 162:    */     }
/* 163:141 */     if (param == SpreadItemParam.RIGHT_MOUSE_END_DRAG) {
/* 164:142 */       return 6;
/* 165:    */     }
/* 166:145 */     if (param == FillStackParam.FILL) {
/* 167:146 */       return 0;
/* 168:    */     }
/* 169:149 */     throw new IOException("Unmapped action param: " + param);
/* 170:    */   }
/* 171:    */   
/* 172:    */   private ActionParam valueToParam(byte value)
/* 173:    */     throws IOException
/* 174:    */   {
/* 175:153 */     if (this.action == Action.CLICK_ITEM)
/* 176:    */     {
/* 177:154 */       if (value == 0) {
/* 178:155 */         return ClickItemParam.LEFT_CLICK;
/* 179:    */       }
/* 180:156 */       if (value == 1) {
/* 181:157 */         return ClickItemParam.RIGHT_CLICK;
/* 182:    */       }
/* 183:    */     }
/* 184:161 */     if (this.action == Action.SHIFT_CLICK_ITEM)
/* 185:    */     {
/* 186:162 */       if (value == 0) {
/* 187:163 */         return ShiftClickItemParam.LEFT_CLICK;
/* 188:    */       }
/* 189:164 */       if (value == 1) {
/* 190:165 */         return ShiftClickItemParam.RIGHT_CLICK;
/* 191:    */       }
/* 192:    */     }
/* 193:169 */     if (this.action == Action.MOVE_TO_HOTBAR_SLOT)
/* 194:    */     {
/* 195:170 */       if (value == 0) {
/* 196:171 */         return MoveToHotbarParam.SLOT_1;
/* 197:    */       }
/* 198:172 */       if (value == 1) {
/* 199:173 */         return MoveToHotbarParam.SLOT_2;
/* 200:    */       }
/* 201:174 */       if (value == 2) {
/* 202:175 */         return MoveToHotbarParam.SLOT_3;
/* 203:    */       }
/* 204:176 */       if (value == 3) {
/* 205:177 */         return MoveToHotbarParam.SLOT_4;
/* 206:    */       }
/* 207:178 */       if (value == 4) {
/* 208:179 */         return MoveToHotbarParam.SLOT_5;
/* 209:    */       }
/* 210:180 */       if (value == 5) {
/* 211:181 */         return MoveToHotbarParam.SLOT_6;
/* 212:    */       }
/* 213:182 */       if (value == 6) {
/* 214:183 */         return MoveToHotbarParam.SLOT_7;
/* 215:    */       }
/* 216:184 */       if (value == 7) {
/* 217:185 */         return MoveToHotbarParam.SLOT_8;
/* 218:    */       }
/* 219:186 */       if (value == 8) {
/* 220:187 */         return MoveToHotbarParam.SLOT_9;
/* 221:    */       }
/* 222:    */     }
/* 223:191 */     if ((this.action == Action.CREATIVE_GRAB_MAX_STACK) && 
/* 224:192 */       (value == 2)) {
/* 225:193 */       return CreativeGrabParam.GRAB;
/* 226:    */     }
/* 227:197 */     if (this.action == Action.DROP_ITEM) {
/* 228:198 */       if (this.slot == -999)
/* 229:    */       {
/* 230:199 */         if (value == 0) {
/* 231:200 */           return DropItemParam.LEFT_CLICK_OUTSIDE_NOT_HOLDING;
/* 232:    */         }
/* 233:201 */         if (value == 1) {
/* 234:202 */           return DropItemParam.RIGHT_CLICK_OUTSIDE_NOT_HOLDING;
/* 235:    */         }
/* 236:    */       }
/* 237:    */       else
/* 238:    */       {
/* 239:205 */         if (value == 0) {
/* 240:206 */           return DropItemParam.DROP_FROM_SELECTED;
/* 241:    */         }
/* 242:207 */         if (value == 1) {
/* 243:208 */           return DropItemParam.DROP_SELECTED_STACK;
/* 244:    */         }
/* 245:    */       }
/* 246:    */     }
/* 247:213 */     if (this.action == Action.SPREAD_ITEM)
/* 248:    */     {
/* 249:214 */       if (value == 0) {
/* 250:215 */         return SpreadItemParam.LEFT_MOUSE_BEGIN_DRAG;
/* 251:    */       }
/* 252:216 */       if (value == 1) {
/* 253:217 */         return SpreadItemParam.LEFT_MOUSE_ADD_SLOT;
/* 254:    */       }
/* 255:218 */       if (value == 2) {
/* 256:219 */         return SpreadItemParam.LEFT_MOUSE_END_DRAG;
/* 257:    */       }
/* 258:220 */       if (value == 4) {
/* 259:221 */         return SpreadItemParam.RIGHT_MOUSE_BEGIN_DRAG;
/* 260:    */       }
/* 261:222 */       if (value == 5) {
/* 262:223 */         return SpreadItemParam.RIGHT_MOUSE_ADD_SLOT;
/* 263:    */       }
/* 264:224 */       if (value == 6) {
/* 265:225 */         return SpreadItemParam.RIGHT_MOUSE_END_DRAG;
/* 266:    */       }
/* 267:    */     }
/* 268:229 */     if ((this.action == Action.FILL_STACK) && 
/* 269:230 */       (value == 0)) {
/* 270:231 */       return FillStackParam.FILL;
/* 271:    */     }
/* 272:235 */     throw new IOException("Unknown action param value: " + value);
/* 273:    */   }
/* 274:    */   
/* 275:    */   public static enum Action
/* 276:    */   {
/* 277:239 */     CLICK_ITEM,  SHIFT_CLICK_ITEM,  MOVE_TO_HOTBAR_SLOT,  CREATIVE_GRAB_MAX_STACK,  DROP_ITEM,  SPREAD_ITEM,  FILL_STACK;
/* 278:    */   }
/* 279:    */   
/* 280:    */   public static enum ClickItemParam
/* 281:    */     implements ClientWindowActionPacket.ActionParam
/* 282:    */   {
/* 283:252 */     LEFT_CLICK,  RIGHT_CLICK;
/* 284:    */   }
/* 285:    */   
/* 286:    */   public static enum ShiftClickItemParam
/* 287:    */     implements ClientWindowActionPacket.ActionParam
/* 288:    */   {
/* 289:257 */     LEFT_CLICK,  RIGHT_CLICK;
/* 290:    */   }
/* 291:    */   
/* 292:    */   public static enum MoveToHotbarParam
/* 293:    */     implements ClientWindowActionPacket.ActionParam
/* 294:    */   {
/* 295:262 */     SLOT_1,  SLOT_2,  SLOT_3,  SLOT_4,  SLOT_5,  SLOT_6,  SLOT_7,  SLOT_8,  SLOT_9;
/* 296:    */   }
/* 297:    */   
/* 298:    */   public static enum CreativeGrabParam
/* 299:    */     implements ClientWindowActionPacket.ActionParam
/* 300:    */   {
/* 301:274 */     GRAB;
/* 302:    */   }
/* 303:    */   
/* 304:    */   public static enum DropItemParam
/* 305:    */     implements ClientWindowActionPacket.ActionParam
/* 306:    */   {
/* 307:278 */     DROP_FROM_SELECTED,  DROP_SELECTED_STACK,  LEFT_CLICK_OUTSIDE_NOT_HOLDING,  RIGHT_CLICK_OUTSIDE_NOT_HOLDING;
/* 308:    */   }
/* 309:    */   
/* 310:    */   public static enum SpreadItemParam
/* 311:    */     implements ClientWindowActionPacket.ActionParam
/* 312:    */   {
/* 313:285 */     LEFT_MOUSE_BEGIN_DRAG,  LEFT_MOUSE_ADD_SLOT,  LEFT_MOUSE_END_DRAG,  RIGHT_MOUSE_BEGIN_DRAG,  RIGHT_MOUSE_ADD_SLOT,  RIGHT_MOUSE_END_DRAG;
/* 314:    */   }
/* 315:    */   
/* 316:    */   public static enum FillStackParam
/* 317:    */     implements ClientWindowActionPacket.ActionParam
/* 318:    */   {
/* 319:294 */     FILL;
/* 320:    */   }
/* 321:    */   
/* 322:    */   public static abstract interface ActionParam {}
/* 323:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.client.window.ClientWindowActionPacket
 * JD-Core Version:    0.7.0.1
 */