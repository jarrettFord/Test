/*   1:    */ package io.netty.handler.codec.spdy;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.StringUtil;
/*   4:    */ import java.util.Map;
/*   5:    */ import java.util.Map.Entry;
/*   6:    */ import java.util.Set;
/*   7:    */ import java.util.TreeMap;
/*   8:    */ 
/*   9:    */ public class DefaultSpdySettingsFrame
/*  10:    */   implements SpdySettingsFrame
/*  11:    */ {
/*  12:    */   private boolean clear;
/*  13:    */   private final Map<Integer, Setting> settingsMap;
/*  14:    */   
/*  15:    */   public DefaultSpdySettingsFrame()
/*  16:    */   {
/*  17: 30 */     this.settingsMap = new TreeMap();
/*  18:    */   }
/*  19:    */   
/*  20:    */   public Set<Integer> ids()
/*  21:    */   {
/*  22: 34 */     return this.settingsMap.keySet();
/*  23:    */   }
/*  24:    */   
/*  25:    */   public boolean isSet(int id)
/*  26:    */   {
/*  27: 39 */     Integer key = Integer.valueOf(id);
/*  28: 40 */     return this.settingsMap.containsKey(key);
/*  29:    */   }
/*  30:    */   
/*  31:    */   public int getValue(int id)
/*  32:    */   {
/*  33: 45 */     Integer key = Integer.valueOf(id);
/*  34: 46 */     if (this.settingsMap.containsKey(key)) {
/*  35: 47 */       return ((Setting)this.settingsMap.get(key)).getValue();
/*  36:    */     }
/*  37: 49 */     return -1;
/*  38:    */   }
/*  39:    */   
/*  40:    */   public SpdySettingsFrame setValue(int id, int value)
/*  41:    */   {
/*  42: 55 */     return setValue(id, value, false, false);
/*  43:    */   }
/*  44:    */   
/*  45:    */   public SpdySettingsFrame setValue(int id, int value, boolean persistValue, boolean persisted)
/*  46:    */   {
/*  47: 60 */     if ((id < 0) || (id > 16777215)) {
/*  48: 61 */       throw new IllegalArgumentException("Setting ID is not valid: " + id);
/*  49:    */     }
/*  50: 63 */     Integer key = Integer.valueOf(id);
/*  51: 64 */     if (this.settingsMap.containsKey(key))
/*  52:    */     {
/*  53: 65 */       Setting setting = (Setting)this.settingsMap.get(key);
/*  54: 66 */       setting.setValue(value);
/*  55: 67 */       setting.setPersist(persistValue);
/*  56: 68 */       setting.setPersisted(persisted);
/*  57:    */     }
/*  58:    */     else
/*  59:    */     {
/*  60: 70 */       this.settingsMap.put(key, new Setting(value, persistValue, persisted));
/*  61:    */     }
/*  62: 72 */     return this;
/*  63:    */   }
/*  64:    */   
/*  65:    */   public SpdySettingsFrame removeValue(int id)
/*  66:    */   {
/*  67: 77 */     Integer key = Integer.valueOf(id);
/*  68: 78 */     if (this.settingsMap.containsKey(key)) {
/*  69: 79 */       this.settingsMap.remove(key);
/*  70:    */     }
/*  71: 81 */     return this;
/*  72:    */   }
/*  73:    */   
/*  74:    */   public boolean isPersistValue(int id)
/*  75:    */   {
/*  76: 86 */     Integer key = Integer.valueOf(id);
/*  77: 87 */     if (this.settingsMap.containsKey(key)) {
/*  78: 88 */       return ((Setting)this.settingsMap.get(key)).isPersist();
/*  79:    */     }
/*  80: 90 */     return false;
/*  81:    */   }
/*  82:    */   
/*  83:    */   public SpdySettingsFrame setPersistValue(int id, boolean persistValue)
/*  84:    */   {
/*  85: 96 */     Integer key = Integer.valueOf(id);
/*  86: 97 */     if (this.settingsMap.containsKey(key)) {
/*  87: 98 */       ((Setting)this.settingsMap.get(key)).setPersist(persistValue);
/*  88:    */     }
/*  89:100 */     return this;
/*  90:    */   }
/*  91:    */   
/*  92:    */   public boolean isPersisted(int id)
/*  93:    */   {
/*  94:105 */     Integer key = Integer.valueOf(id);
/*  95:106 */     if (this.settingsMap.containsKey(key)) {
/*  96:107 */       return ((Setting)this.settingsMap.get(key)).isPersisted();
/*  97:    */     }
/*  98:109 */     return false;
/*  99:    */   }
/* 100:    */   
/* 101:    */   public SpdySettingsFrame setPersisted(int id, boolean persisted)
/* 102:    */   {
/* 103:115 */     Integer key = Integer.valueOf(id);
/* 104:116 */     if (this.settingsMap.containsKey(key)) {
/* 105:117 */       ((Setting)this.settingsMap.get(key)).setPersisted(persisted);
/* 106:    */     }
/* 107:119 */     return this;
/* 108:    */   }
/* 109:    */   
/* 110:    */   public boolean clearPreviouslyPersistedSettings()
/* 111:    */   {
/* 112:124 */     return this.clear;
/* 113:    */   }
/* 114:    */   
/* 115:    */   public SpdySettingsFrame setClearPreviouslyPersistedSettings(boolean clear)
/* 116:    */   {
/* 117:129 */     this.clear = clear;
/* 118:130 */     return this;
/* 119:    */   }
/* 120:    */   
/* 121:    */   private Set<Map.Entry<Integer, Setting>> getSettings()
/* 122:    */   {
/* 123:134 */     return this.settingsMap.entrySet();
/* 124:    */   }
/* 125:    */   
/* 126:    */   private void appendSettings(StringBuilder buf)
/* 127:    */   {
/* 128:138 */     for (Map.Entry<Integer, Setting> e : getSettings())
/* 129:    */     {
/* 130:139 */       Setting setting = (Setting)e.getValue();
/* 131:140 */       buf.append("--> ");
/* 132:141 */       buf.append(e.getKey());
/* 133:142 */       buf.append(':');
/* 134:143 */       buf.append(setting.getValue());
/* 135:144 */       buf.append(" (persist value: ");
/* 136:145 */       buf.append(setting.isPersist());
/* 137:146 */       buf.append("; persisted: ");
/* 138:147 */       buf.append(setting.isPersisted());
/* 139:148 */       buf.append(')');
/* 140:149 */       buf.append(StringUtil.NEWLINE);
/* 141:    */     }
/* 142:    */   }
/* 143:    */   
/* 144:    */   public String toString()
/* 145:    */   {
/* 146:155 */     StringBuilder buf = new StringBuilder();
/* 147:156 */     buf.append(StringUtil.simpleClassName(this));
/* 148:157 */     buf.append(StringUtil.NEWLINE);
/* 149:158 */     appendSettings(buf);
/* 150:159 */     buf.setLength(buf.length() - StringUtil.NEWLINE.length());
/* 151:160 */     return buf.toString();
/* 152:    */   }
/* 153:    */   
/* 154:    */   private static final class Setting
/* 155:    */   {
/* 156:    */     private int value;
/* 157:    */     private boolean persist;
/* 158:    */     private boolean persisted;
/* 159:    */     
/* 160:    */     Setting(int value, boolean persist, boolean persisted)
/* 161:    */     {
/* 162:170 */       this.value = value;
/* 163:171 */       this.persist = persist;
/* 164:172 */       this.persisted = persisted;
/* 165:    */     }
/* 166:    */     
/* 167:    */     int getValue()
/* 168:    */     {
/* 169:176 */       return this.value;
/* 170:    */     }
/* 171:    */     
/* 172:    */     void setValue(int value)
/* 173:    */     {
/* 174:180 */       this.value = value;
/* 175:    */     }
/* 176:    */     
/* 177:    */     boolean isPersist()
/* 178:    */     {
/* 179:184 */       return this.persist;
/* 180:    */     }
/* 181:    */     
/* 182:    */     void setPersist(boolean persist)
/* 183:    */     {
/* 184:188 */       this.persist = persist;
/* 185:    */     }
/* 186:    */     
/* 187:    */     boolean isPersisted()
/* 188:    */     {
/* 189:192 */       return this.persisted;
/* 190:    */     }
/* 191:    */     
/* 192:    */     void setPersisted(boolean persisted)
/* 193:    */     {
/* 194:196 */       this.persisted = persisted;
/* 195:    */     }
/* 196:    */   }
/* 197:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.DefaultSpdySettingsFrame
 * JD-Core Version:    0.7.0.1
 */