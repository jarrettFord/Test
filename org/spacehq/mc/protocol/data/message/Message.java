/*   1:    */ package org.spacehq.mc.protocol.data.message;
/*   2:    */ 
/*   3:    */ import com.google.gson.Gson;
/*   4:    */ import com.google.gson.JsonArray;
/*   5:    */ import com.google.gson.JsonElement;
/*   6:    */ import com.google.gson.JsonObject;
/*   7:    */ import java.util.ArrayList;
/*   8:    */ import java.util.List;
/*   9:    */ 
/*  10:    */ public abstract class Message
/*  11:    */   implements Cloneable
/*  12:    */ {
/*  13: 13 */   private MessageStyle style = new MessageStyle();
/*  14: 14 */   private List<Message> extra = new ArrayList();
/*  15:    */   
/*  16:    */   public abstract String getText();
/*  17:    */   
/*  18:    */   public String getFullText()
/*  19:    */   {
/*  20: 19 */     StringBuilder build = new StringBuilder(getText());
/*  21: 20 */     for (Message msg : this.extra) {
/*  22: 21 */       build.append(msg.getFullText());
/*  23:    */     }
/*  24: 24 */     return build.toString();
/*  25:    */   }
/*  26:    */   
/*  27:    */   public MessageStyle getStyle()
/*  28:    */   {
/*  29: 28 */     return this.style;
/*  30:    */   }
/*  31:    */   
/*  32:    */   public List<Message> getExtra()
/*  33:    */   {
/*  34: 32 */     return new ArrayList(this.extra);
/*  35:    */   }
/*  36:    */   
/*  37:    */   public Message setStyle(MessageStyle style)
/*  38:    */   {
/*  39: 36 */     this.style = style;
/*  40: 37 */     return this;
/*  41:    */   }
/*  42:    */   
/*  43:    */   public Message setExtra(List<Message> extra)
/*  44:    */   {
/*  45: 41 */     this.extra = new ArrayList(extra);
/*  46: 42 */     for (Message msg : this.extra) {
/*  47: 43 */       msg.getStyle().setParent(this.style);
/*  48:    */     }
/*  49: 46 */     return this;
/*  50:    */   }
/*  51:    */   
/*  52:    */   public Message addExtra(Message message)
/*  53:    */   {
/*  54: 50 */     this.extra.add(message);
/*  55: 51 */     message.getStyle().setParent(this.style);
/*  56: 52 */     return this;
/*  57:    */   }
/*  58:    */   
/*  59:    */   public Message removeExtra(Message message)
/*  60:    */   {
/*  61: 56 */     this.extra.remove(message);
/*  62: 57 */     message.getStyle().setParent(new MessageStyle());
/*  63: 58 */     return this;
/*  64:    */   }
/*  65:    */   
/*  66:    */   public Message clearExtra()
/*  67:    */   {
/*  68: 62 */     for (Message msg : this.extra) {
/*  69: 63 */       msg.getStyle().setParent(new MessageStyle());
/*  70:    */     }
/*  71: 66 */     this.extra.clear();
/*  72: 67 */     return this;
/*  73:    */   }
/*  74:    */   
/*  75:    */   public String toString()
/*  76:    */   {
/*  77: 72 */     return getFullText();
/*  78:    */   }
/*  79:    */   
/*  80:    */   public abstract Message clone();
/*  81:    */   
/*  82:    */   public String toJsonString()
/*  83:    */   {
/*  84: 79 */     return toJson().toString();
/*  85:    */   }
/*  86:    */   
/*  87:    */   public JsonElement toJson()
/*  88:    */   {
/*  89: 83 */     JsonObject json = new JsonObject();
/*  90: 84 */     json.addProperty("color", this.style.getColor().toString());
/*  91: 85 */     for (ChatFormat format : this.style.getFormats()) {
/*  92: 86 */       json.addProperty(format.toString(), Boolean.valueOf(true));
/*  93:    */     }
/*  94: 89 */     if (this.style.getClickEvent() != null)
/*  95:    */     {
/*  96: 90 */       JsonObject click = new JsonObject();
/*  97: 91 */       click.addProperty("action", this.style.getClickEvent().getAction().toString());
/*  98: 92 */       click.addProperty("value", this.style.getClickEvent().getValue());
/*  99: 93 */       json.add("clickEvent", click);
/* 100:    */     }
/* 101: 96 */     if (this.style.getHoverEvent() != null)
/* 102:    */     {
/* 103: 97 */       JsonObject hover = new JsonObject();
/* 104: 98 */       hover.addProperty("action", this.style.getHoverEvent().getAction().toString());
/* 105: 99 */       hover.add("value", this.style.getHoverEvent().getValue().toJson());
/* 106:100 */       json.add("hoverEvent", hover);
/* 107:    */     }
/* 108:103 */     if (this.style.getInsertion() != null) {
/* 109:104 */       json.addProperty("insertion", this.style.getInsertion());
/* 110:    */     }
/* 111:107 */     if (this.extra.size() > 0)
/* 112:    */     {
/* 113:108 */       JsonArray extra = new JsonArray();
/* 114:109 */       for (Message msg : this.extra) {
/* 115:110 */         extra.add(msg.toJson());
/* 116:    */       }
/* 117:113 */       json.add("extra", extra);
/* 118:    */     }
/* 119:116 */     return json;
/* 120:    */   }
/* 121:    */   
/* 122:    */   public static Message fromString(String str)
/* 123:    */   {
/* 124:    */     try
/* 125:    */     {
/* 126:121 */       return fromJson((JsonElement)new Gson().fromJson(str, JsonObject.class));
/* 127:    */     }
/* 128:    */     catch (Exception e) {}
/* 129:123 */     return new TextMessage(str);
/* 130:    */   }
/* 131:    */   
/* 132:    */   public static Message fromJson(JsonElement e)
/* 133:    */   {
/* 134:128 */     if (e.isJsonPrimitive()) {
/* 135:129 */       return new TextMessage(e.getAsString());
/* 136:    */     }
/* 137:130 */     if (e.isJsonObject())
/* 138:    */     {
/* 139:131 */       JsonObject json = e.getAsJsonObject();
/* 140:132 */       Message msg = null;
/* 141:    */       int index;
/* 142:    */       JsonElement el;
/* 143:133 */       if (json.has("text"))
/* 144:    */       {
/* 145:134 */         msg = new TextMessage(json.get("text").getAsString());
/* 146:    */       }
/* 147:135 */       else if (json.has("translate"))
/* 148:    */       {
/* 149:136 */         Message[] with = new Message[0];
/* 150:137 */         if (json.has("with"))
/* 151:    */         {
/* 152:138 */           JsonArray withJson = json.get("with").getAsJsonArray();
/* 153:139 */           with = new Message[withJson.size()];
/* 154:140 */           for (index = 0; index < withJson.size(); index++)
/* 155:    */           {
/* 156:141 */             el = withJson.get(index);
/* 157:142 */             if (el.isJsonPrimitive()) {
/* 158:143 */               with[index] = new TextMessage(el.getAsString());
/* 159:    */             } else {
/* 160:145 */               with[index] = fromJson(el.getAsJsonObject());
/* 161:    */             }
/* 162:    */           }
/* 163:    */         }
/* 164:150 */         msg = new TranslationMessage(json.get("translate").getAsString(), with);
/* 165:    */       }
/* 166:    */       else
/* 167:    */       {
/* 168:152 */         throw new IllegalArgumentException("Unknown message type in json: " + json.toString());
/* 169:    */       }
/* 170:155 */       MessageStyle style = new MessageStyle();
/* 171:156 */       if (json.has("color")) {
/* 172:157 */         style.setColor(ChatColor.byName(json.get("color").getAsString()));
/* 173:    */       }
/* 174:160 */       for (ChatFormat format : ChatFormat.values()) {
/* 175:161 */         if ((json.has(format.toString())) && (json.get(format.toString()).getAsBoolean())) {
/* 176:162 */           style.addFormat(format);
/* 177:    */         }
/* 178:    */       }
/* 179:166 */       if (json.has("clickEvent"))
/* 180:    */       {
/* 181:167 */         JsonObject click = json.get("clickEvent").getAsJsonObject();
/* 182:168 */         style.setClickEvent(new ClickEvent(ClickAction.byName(click.get("action").getAsString()), click.get("value").getAsString()));
/* 183:    */       }
/* 184:171 */       if (json.has("hoverEvent"))
/* 185:    */       {
/* 186:172 */         JsonObject hover = json.get("hoverEvent").getAsJsonObject();
/* 187:173 */         style.setHoverEvent(new HoverEvent(HoverAction.byName(hover.get("action").getAsString()), fromJson(hover.get("value"))));
/* 188:    */       }
/* 189:176 */       if (json.has("insertion")) {
/* 190:177 */         style.setInsertion(json.get("insertion").getAsString());
/* 191:    */       }
/* 192:180 */       msg.setStyle(style);
/* 193:181 */       if (json.has("extra"))
/* 194:    */       {
/* 195:182 */         JsonArray extraJson = json.get("extra").getAsJsonArray();
/* 196:183 */         List<Message> extra = new ArrayList();
/* 197:184 */         for (int index = 0; index < extraJson.size(); index++)
/* 198:    */         {
/* 199:185 */           JsonElement el = extraJson.get(index);
/* 200:186 */           if (el.isJsonPrimitive()) {
/* 201:187 */             extra.add(new TextMessage(el.getAsString()));
/* 202:    */           } else {
/* 203:189 */             extra.add(fromJson(el.getAsJsonObject()));
/* 204:    */           }
/* 205:    */         }
/* 206:193 */         msg.setExtra(extra);
/* 207:    */       }
/* 208:196 */       return msg;
/* 209:    */     }
/* 210:198 */     throw new IllegalArgumentException("Cannot convert " + e.getClass().getSimpleName() + " to a message.");
/* 211:    */   }
/* 212:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.data.message.Message
 * JD-Core Version:    0.7.0.1
 */