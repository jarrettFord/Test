/*   1:    */ package com.google.gson;
/*   2:    */ 
/*   3:    */ import java.math.BigDecimal;
/*   4:    */ import java.math.BigInteger;
/*   5:    */ import java.util.ArrayList;
/*   6:    */ import java.util.Iterator;
/*   7:    */ import java.util.List;
/*   8:    */ 
/*   9:    */ public final class JsonArray
/*  10:    */   extends JsonElement
/*  11:    */   implements Iterable<JsonElement>
/*  12:    */ {
/*  13:    */   private final List<JsonElement> elements;
/*  14:    */   
/*  15:    */   public JsonArray()
/*  16:    */   {
/*  17: 40 */     this.elements = new ArrayList();
/*  18:    */   }
/*  19:    */   
/*  20:    */   JsonArray deepCopy()
/*  21:    */   {
/*  22: 45 */     JsonArray result = new JsonArray();
/*  23: 46 */     for (JsonElement element : this.elements) {
/*  24: 47 */       result.add(element.deepCopy());
/*  25:    */     }
/*  26: 49 */     return result;
/*  27:    */   }
/*  28:    */   
/*  29:    */   public void add(JsonElement element)
/*  30:    */   {
/*  31: 58 */     if (element == null) {
/*  32: 59 */       element = JsonNull.INSTANCE;
/*  33:    */     }
/*  34: 61 */     this.elements.add(element);
/*  35:    */   }
/*  36:    */   
/*  37:    */   public void addAll(JsonArray array)
/*  38:    */   {
/*  39: 70 */     this.elements.addAll(array.elements);
/*  40:    */   }
/*  41:    */   
/*  42:    */   public int size()
/*  43:    */   {
/*  44: 79 */     return this.elements.size();
/*  45:    */   }
/*  46:    */   
/*  47:    */   public Iterator<JsonElement> iterator()
/*  48:    */   {
/*  49: 89 */     return this.elements.iterator();
/*  50:    */   }
/*  51:    */   
/*  52:    */   public JsonElement get(int i)
/*  53:    */   {
/*  54:101 */     return (JsonElement)this.elements.get(i);
/*  55:    */   }
/*  56:    */   
/*  57:    */   public Number getAsNumber()
/*  58:    */   {
/*  59:114 */     if (this.elements.size() == 1) {
/*  60:115 */       return ((JsonElement)this.elements.get(0)).getAsNumber();
/*  61:    */     }
/*  62:117 */     throw new IllegalStateException();
/*  63:    */   }
/*  64:    */   
/*  65:    */   public String getAsString()
/*  66:    */   {
/*  67:130 */     if (this.elements.size() == 1) {
/*  68:131 */       return ((JsonElement)this.elements.get(0)).getAsString();
/*  69:    */     }
/*  70:133 */     throw new IllegalStateException();
/*  71:    */   }
/*  72:    */   
/*  73:    */   public double getAsDouble()
/*  74:    */   {
/*  75:146 */     if (this.elements.size() == 1) {
/*  76:147 */       return ((JsonElement)this.elements.get(0)).getAsDouble();
/*  77:    */     }
/*  78:149 */     throw new IllegalStateException();
/*  79:    */   }
/*  80:    */   
/*  81:    */   public BigDecimal getAsBigDecimal()
/*  82:    */   {
/*  83:163 */     if (this.elements.size() == 1) {
/*  84:164 */       return ((JsonElement)this.elements.get(0)).getAsBigDecimal();
/*  85:    */     }
/*  86:166 */     throw new IllegalStateException();
/*  87:    */   }
/*  88:    */   
/*  89:    */   public BigInteger getAsBigInteger()
/*  90:    */   {
/*  91:180 */     if (this.elements.size() == 1) {
/*  92:181 */       return ((JsonElement)this.elements.get(0)).getAsBigInteger();
/*  93:    */     }
/*  94:183 */     throw new IllegalStateException();
/*  95:    */   }
/*  96:    */   
/*  97:    */   public float getAsFloat()
/*  98:    */   {
/*  99:196 */     if (this.elements.size() == 1) {
/* 100:197 */       return ((JsonElement)this.elements.get(0)).getAsFloat();
/* 101:    */     }
/* 102:199 */     throw new IllegalStateException();
/* 103:    */   }
/* 104:    */   
/* 105:    */   public long getAsLong()
/* 106:    */   {
/* 107:212 */     if (this.elements.size() == 1) {
/* 108:213 */       return ((JsonElement)this.elements.get(0)).getAsLong();
/* 109:    */     }
/* 110:215 */     throw new IllegalStateException();
/* 111:    */   }
/* 112:    */   
/* 113:    */   public int getAsInt()
/* 114:    */   {
/* 115:228 */     if (this.elements.size() == 1) {
/* 116:229 */       return ((JsonElement)this.elements.get(0)).getAsInt();
/* 117:    */     }
/* 118:231 */     throw new IllegalStateException();
/* 119:    */   }
/* 120:    */   
/* 121:    */   public byte getAsByte()
/* 122:    */   {
/* 123:236 */     if (this.elements.size() == 1) {
/* 124:237 */       return ((JsonElement)this.elements.get(0)).getAsByte();
/* 125:    */     }
/* 126:239 */     throw new IllegalStateException();
/* 127:    */   }
/* 128:    */   
/* 129:    */   public char getAsCharacter()
/* 130:    */   {
/* 131:244 */     if (this.elements.size() == 1) {
/* 132:245 */       return ((JsonElement)this.elements.get(0)).getAsCharacter();
/* 133:    */     }
/* 134:247 */     throw new IllegalStateException();
/* 135:    */   }
/* 136:    */   
/* 137:    */   public short getAsShort()
/* 138:    */   {
/* 139:260 */     if (this.elements.size() == 1) {
/* 140:261 */       return ((JsonElement)this.elements.get(0)).getAsShort();
/* 141:    */     }
/* 142:263 */     throw new IllegalStateException();
/* 143:    */   }
/* 144:    */   
/* 145:    */   public boolean getAsBoolean()
/* 146:    */   {
/* 147:276 */     if (this.elements.size() == 1) {
/* 148:277 */       return ((JsonElement)this.elements.get(0)).getAsBoolean();
/* 149:    */     }
/* 150:279 */     throw new IllegalStateException();
/* 151:    */   }
/* 152:    */   
/* 153:    */   public boolean equals(Object o)
/* 154:    */   {
/* 155:284 */     return (o == this) || (((o instanceof JsonArray)) && (((JsonArray)o).elements.equals(this.elements)));
/* 156:    */   }
/* 157:    */   
/* 158:    */   public int hashCode()
/* 159:    */   {
/* 160:289 */     return this.elements.hashCode();
/* 161:    */   }
/* 162:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.JsonArray
 * JD-Core Version:    0.7.0.1
 */