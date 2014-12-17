/*   1:    */ package com.google.gson;
/*   2:    */ 
/*   3:    */ import com.google.gson.internal.Streams;
/*   4:    */ import com.google.gson.stream.JsonWriter;
/*   5:    */ import java.io.IOException;
/*   6:    */ import java.io.StringWriter;
/*   7:    */ import java.math.BigDecimal;
/*   8:    */ import java.math.BigInteger;
/*   9:    */ 
/*  10:    */ public abstract class JsonElement
/*  11:    */ {
/*  12:    */   abstract JsonElement deepCopy();
/*  13:    */   
/*  14:    */   public boolean isJsonArray()
/*  15:    */   {
/*  16: 46 */     return this instanceof JsonArray;
/*  17:    */   }
/*  18:    */   
/*  19:    */   public boolean isJsonObject()
/*  20:    */   {
/*  21: 55 */     return this instanceof JsonObject;
/*  22:    */   }
/*  23:    */   
/*  24:    */   public boolean isJsonPrimitive()
/*  25:    */   {
/*  26: 64 */     return this instanceof JsonPrimitive;
/*  27:    */   }
/*  28:    */   
/*  29:    */   public boolean isJsonNull()
/*  30:    */   {
/*  31: 74 */     return this instanceof JsonNull;
/*  32:    */   }
/*  33:    */   
/*  34:    */   public JsonObject getAsJsonObject()
/*  35:    */   {
/*  36: 87 */     if (isJsonObject()) {
/*  37: 88 */       return (JsonObject)this;
/*  38:    */     }
/*  39: 90 */     throw new IllegalStateException("Not a JSON Object: " + this);
/*  40:    */   }
/*  41:    */   
/*  42:    */   public JsonArray getAsJsonArray()
/*  43:    */   {
/*  44:103 */     if (isJsonArray()) {
/*  45:104 */       return (JsonArray)this;
/*  46:    */     }
/*  47:106 */     throw new IllegalStateException("This is not a JSON Array.");
/*  48:    */   }
/*  49:    */   
/*  50:    */   public JsonPrimitive getAsJsonPrimitive()
/*  51:    */   {
/*  52:119 */     if (isJsonPrimitive()) {
/*  53:120 */       return (JsonPrimitive)this;
/*  54:    */     }
/*  55:122 */     throw new IllegalStateException("This is not a JSON Primitive.");
/*  56:    */   }
/*  57:    */   
/*  58:    */   public JsonNull getAsJsonNull()
/*  59:    */   {
/*  60:136 */     if (isJsonNull()) {
/*  61:137 */       return (JsonNull)this;
/*  62:    */     }
/*  63:139 */     throw new IllegalStateException("This is not a JSON Null.");
/*  64:    */   }
/*  65:    */   
/*  66:    */   public boolean getAsBoolean()
/*  67:    */   {
/*  68:152 */     throw new UnsupportedOperationException(getClass().getSimpleName());
/*  69:    */   }
/*  70:    */   
/*  71:    */   Boolean getAsBooleanWrapper()
/*  72:    */   {
/*  73:165 */     throw new UnsupportedOperationException(getClass().getSimpleName());
/*  74:    */   }
/*  75:    */   
/*  76:    */   public Number getAsNumber()
/*  77:    */   {
/*  78:178 */     throw new UnsupportedOperationException(getClass().getSimpleName());
/*  79:    */   }
/*  80:    */   
/*  81:    */   public String getAsString()
/*  82:    */   {
/*  83:191 */     throw new UnsupportedOperationException(getClass().getSimpleName());
/*  84:    */   }
/*  85:    */   
/*  86:    */   public double getAsDouble()
/*  87:    */   {
/*  88:204 */     throw new UnsupportedOperationException(getClass().getSimpleName());
/*  89:    */   }
/*  90:    */   
/*  91:    */   public float getAsFloat()
/*  92:    */   {
/*  93:217 */     throw new UnsupportedOperationException(getClass().getSimpleName());
/*  94:    */   }
/*  95:    */   
/*  96:    */   public long getAsLong()
/*  97:    */   {
/*  98:230 */     throw new UnsupportedOperationException(getClass().getSimpleName());
/*  99:    */   }
/* 100:    */   
/* 101:    */   public int getAsInt()
/* 102:    */   {
/* 103:243 */     throw new UnsupportedOperationException(getClass().getSimpleName());
/* 104:    */   }
/* 105:    */   
/* 106:    */   public byte getAsByte()
/* 107:    */   {
/* 108:257 */     throw new UnsupportedOperationException(getClass().getSimpleName());
/* 109:    */   }
/* 110:    */   
/* 111:    */   public char getAsCharacter()
/* 112:    */   {
/* 113:271 */     throw new UnsupportedOperationException(getClass().getSimpleName());
/* 114:    */   }
/* 115:    */   
/* 116:    */   public BigDecimal getAsBigDecimal()
/* 117:    */   {
/* 118:285 */     throw new UnsupportedOperationException(getClass().getSimpleName());
/* 119:    */   }
/* 120:    */   
/* 121:    */   public BigInteger getAsBigInteger()
/* 122:    */   {
/* 123:299 */     throw new UnsupportedOperationException(getClass().getSimpleName());
/* 124:    */   }
/* 125:    */   
/* 126:    */   public short getAsShort()
/* 127:    */   {
/* 128:312 */     throw new UnsupportedOperationException(getClass().getSimpleName());
/* 129:    */   }
/* 130:    */   
/* 131:    */   public String toString()
/* 132:    */   {
/* 133:    */     try
/* 134:    */     {
/* 135:321 */       StringWriter stringWriter = new StringWriter();
/* 136:322 */       JsonWriter jsonWriter = new JsonWriter(stringWriter);
/* 137:323 */       jsonWriter.setLenient(true);
/* 138:324 */       Streams.write(this, jsonWriter);
/* 139:325 */       return stringWriter.toString();
/* 140:    */     }
/* 141:    */     catch (IOException e)
/* 142:    */     {
/* 143:327 */       throw new AssertionError(e);
/* 144:    */     }
/* 145:    */   }
/* 146:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.JsonElement
 * JD-Core Version:    0.7.0.1
 */