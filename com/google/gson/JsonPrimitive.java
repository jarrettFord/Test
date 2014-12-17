/*   1:    */ package com.google.gson;
/*   2:    */ 
/*   3:    */ import com.google.gson.internal..Gson.Preconditions;
/*   4:    */ import com.google.gson.internal.LazilyParsedNumber;
/*   5:    */ import java.math.BigDecimal;
/*   6:    */ import java.math.BigInteger;
/*   7:    */ 
/*   8:    */ public final class JsonPrimitive
/*   9:    */   extends JsonElement
/*  10:    */ {
/*  11: 35 */   private static final Class<?>[] PRIMITIVE_TYPES = { Integer.TYPE, Long.TYPE, Short.TYPE, Float.TYPE, Double.TYPE, Byte.TYPE, Boolean.TYPE, Character.TYPE, Integer.class, Long.class, Short.class, Float.class, Double.class, Byte.class, Boolean.class, Character.class };
/*  12:    */   private Object value;
/*  13:    */   
/*  14:    */   public JsonPrimitive(Boolean bool)
/*  15:    */   {
/*  16: 47 */     setValue(bool);
/*  17:    */   }
/*  18:    */   
/*  19:    */   public JsonPrimitive(Number number)
/*  20:    */   {
/*  21: 56 */     setValue(number);
/*  22:    */   }
/*  23:    */   
/*  24:    */   public JsonPrimitive(String string)
/*  25:    */   {
/*  26: 65 */     setValue(string);
/*  27:    */   }
/*  28:    */   
/*  29:    */   public JsonPrimitive(Character c)
/*  30:    */   {
/*  31: 75 */     setValue(c);
/*  32:    */   }
/*  33:    */   
/*  34:    */   JsonPrimitive(Object primitive)
/*  35:    */   {
/*  36: 85 */     setValue(primitive);
/*  37:    */   }
/*  38:    */   
/*  39:    */   JsonPrimitive deepCopy()
/*  40:    */   {
/*  41: 90 */     return this;
/*  42:    */   }
/*  43:    */   
/*  44:    */   void setValue(Object primitive)
/*  45:    */   {
/*  46: 94 */     if ((primitive instanceof Character))
/*  47:    */     {
/*  48: 97 */       char c = ((Character)primitive).charValue();
/*  49: 98 */       this.value = String.valueOf(c);
/*  50:    */     }
/*  51:    */     else
/*  52:    */     {
/*  53:100 */       .Gson.Preconditions.checkArgument(((primitive instanceof Number)) || (isPrimitiveOrString(primitive)));
/*  54:    */       
/*  55:102 */       this.value = primitive;
/*  56:    */     }
/*  57:    */   }
/*  58:    */   
/*  59:    */   public boolean isBoolean()
/*  60:    */   {
/*  61:112 */     return this.value instanceof Boolean;
/*  62:    */   }
/*  63:    */   
/*  64:    */   Boolean getAsBooleanWrapper()
/*  65:    */   {
/*  66:122 */     return (Boolean)this.value;
/*  67:    */   }
/*  68:    */   
/*  69:    */   public boolean getAsBoolean()
/*  70:    */   {
/*  71:132 */     if (isBoolean()) {
/*  72:133 */       return getAsBooleanWrapper().booleanValue();
/*  73:    */     }
/*  74:136 */     return Boolean.parseBoolean(getAsString());
/*  75:    */   }
/*  76:    */   
/*  77:    */   public boolean isNumber()
/*  78:    */   {
/*  79:146 */     return this.value instanceof Number;
/*  80:    */   }
/*  81:    */   
/*  82:    */   public Number getAsNumber()
/*  83:    */   {
/*  84:157 */     return (this.value instanceof String) ? new LazilyParsedNumber((String)this.value) : (Number)this.value;
/*  85:    */   }
/*  86:    */   
/*  87:    */   public boolean isString()
/*  88:    */   {
/*  89:166 */     return this.value instanceof String;
/*  90:    */   }
/*  91:    */   
/*  92:    */   public String getAsString()
/*  93:    */   {
/*  94:176 */     if (isNumber()) {
/*  95:177 */       return getAsNumber().toString();
/*  96:    */     }
/*  97:178 */     if (isBoolean()) {
/*  98:179 */       return getAsBooleanWrapper().toString();
/*  99:    */     }
/* 100:181 */     return (String)this.value;
/* 101:    */   }
/* 102:    */   
/* 103:    */   public double getAsDouble()
/* 104:    */   {
/* 105:193 */     return isNumber() ? getAsNumber().doubleValue() : Double.parseDouble(getAsString());
/* 106:    */   }
/* 107:    */   
/* 108:    */   public BigDecimal getAsBigDecimal()
/* 109:    */   {
/* 110:204 */     return (this.value instanceof BigDecimal) ? (BigDecimal)this.value : new BigDecimal(this.value.toString());
/* 111:    */   }
/* 112:    */   
/* 113:    */   public BigInteger getAsBigInteger()
/* 114:    */   {
/* 115:215 */     return (this.value instanceof BigInteger) ? (BigInteger)this.value : new BigInteger(this.value.toString());
/* 116:    */   }
/* 117:    */   
/* 118:    */   public float getAsFloat()
/* 119:    */   {
/* 120:227 */     return isNumber() ? getAsNumber().floatValue() : Float.parseFloat(getAsString());
/* 121:    */   }
/* 122:    */   
/* 123:    */   public long getAsLong()
/* 124:    */   {
/* 125:238 */     return isNumber() ? getAsNumber().longValue() : Long.parseLong(getAsString());
/* 126:    */   }
/* 127:    */   
/* 128:    */   public short getAsShort()
/* 129:    */   {
/* 130:249 */     return isNumber() ? getAsNumber().shortValue() : Short.parseShort(getAsString());
/* 131:    */   }
/* 132:    */   
/* 133:    */   public int getAsInt()
/* 134:    */   {
/* 135:260 */     return isNumber() ? getAsNumber().intValue() : Integer.parseInt(getAsString());
/* 136:    */   }
/* 137:    */   
/* 138:    */   public byte getAsByte()
/* 139:    */   {
/* 140:265 */     return isNumber() ? getAsNumber().byteValue() : Byte.parseByte(getAsString());
/* 141:    */   }
/* 142:    */   
/* 143:    */   public char getAsCharacter()
/* 144:    */   {
/* 145:270 */     return getAsString().charAt(0);
/* 146:    */   }
/* 147:    */   
/* 148:    */   private static boolean isPrimitiveOrString(Object target)
/* 149:    */   {
/* 150:274 */     if ((target instanceof String)) {
/* 151:275 */       return true;
/* 152:    */     }
/* 153:278 */     Class<?> classOfPrimitive = target.getClass();
/* 154:279 */     for (Class<?> standardPrimitive : PRIMITIVE_TYPES) {
/* 155:280 */       if (standardPrimitive.isAssignableFrom(classOfPrimitive)) {
/* 156:281 */         return true;
/* 157:    */       }
/* 158:    */     }
/* 159:284 */     return false;
/* 160:    */   }
/* 161:    */   
/* 162:    */   public int hashCode()
/* 163:    */   {
/* 164:289 */     if (this.value == null) {
/* 165:290 */       return 31;
/* 166:    */     }
/* 167:293 */     if (isIntegral(this))
/* 168:    */     {
/* 169:294 */       long value = getAsNumber().longValue();
/* 170:295 */       return (int)(value ^ value >>> 32);
/* 171:    */     }
/* 172:297 */     if ((this.value instanceof Number))
/* 173:    */     {
/* 174:298 */       long value = Double.doubleToLongBits(getAsNumber().doubleValue());
/* 175:299 */       return (int)(value ^ value >>> 32);
/* 176:    */     }
/* 177:301 */     return this.value.hashCode();
/* 178:    */   }
/* 179:    */   
/* 180:    */   public boolean equals(Object obj)
/* 181:    */   {
/* 182:306 */     if (this == obj) {
/* 183:307 */       return true;
/* 184:    */     }
/* 185:309 */     if ((obj == null) || (getClass() != obj.getClass())) {
/* 186:310 */       return false;
/* 187:    */     }
/* 188:312 */     JsonPrimitive other = (JsonPrimitive)obj;
/* 189:313 */     if (this.value == null) {
/* 190:314 */       return other.value == null;
/* 191:    */     }
/* 192:316 */     if ((isIntegral(this)) && (isIntegral(other))) {
/* 193:317 */       return getAsNumber().longValue() == other.getAsNumber().longValue();
/* 194:    */     }
/* 195:319 */     if (((this.value instanceof Number)) && ((other.value instanceof Number)))
/* 196:    */     {
/* 197:320 */       double a = getAsNumber().doubleValue();
/* 198:    */       
/* 199:    */ 
/* 200:323 */       double b = other.getAsNumber().doubleValue();
/* 201:324 */       return (a == b) || ((Double.isNaN(a)) && (Double.isNaN(b)));
/* 202:    */     }
/* 203:326 */     return this.value.equals(other.value);
/* 204:    */   }
/* 205:    */   
/* 206:    */   private static boolean isIntegral(JsonPrimitive primitive)
/* 207:    */   {
/* 208:334 */     if ((primitive.value instanceof Number))
/* 209:    */     {
/* 210:335 */       Number number = (Number)primitive.value;
/* 211:336 */       return ((number instanceof BigInteger)) || ((number instanceof Long)) || ((number instanceof Integer)) || ((number instanceof Short)) || ((number instanceof Byte));
/* 212:    */     }
/* 213:339 */     return false;
/* 214:    */   }
/* 215:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.JsonPrimitive
 * JD-Core Version:    0.7.0.1
 */