/*   1:    */ package com.google.gson.internal.bind;
/*   2:    */ 
/*   3:    */ import com.google.gson.JsonArray;
/*   4:    */ import com.google.gson.JsonElement;
/*   5:    */ import com.google.gson.JsonNull;
/*   6:    */ import com.google.gson.JsonObject;
/*   7:    */ import com.google.gson.JsonPrimitive;
/*   8:    */ import com.google.gson.stream.JsonWriter;
/*   9:    */ import java.io.IOException;
/*  10:    */ import java.io.Writer;
/*  11:    */ import java.util.ArrayList;
/*  12:    */ import java.util.List;
/*  13:    */ 
/*  14:    */ public final class JsonTreeWriter
/*  15:    */   extends JsonWriter
/*  16:    */ {
/*  17: 34 */   private static final Writer UNWRITABLE_WRITER = new Writer()
/*  18:    */   {
/*  19:    */     public void write(char[] buffer, int offset, int counter)
/*  20:    */     {
/*  21: 36 */       throw new AssertionError();
/*  22:    */     }
/*  23:    */     
/*  24:    */     public void flush()
/*  25:    */       throws IOException
/*  26:    */     {
/*  27: 39 */       throw new AssertionError();
/*  28:    */     }
/*  29:    */     
/*  30:    */     public void close()
/*  31:    */       throws IOException
/*  32:    */     {
/*  33: 42 */       throw new AssertionError();
/*  34:    */     }
/*  35:    */   };
/*  36: 46 */   private static final JsonPrimitive SENTINEL_CLOSED = new JsonPrimitive("closed");
/*  37: 49 */   private final List<JsonElement> stack = new ArrayList();
/*  38:    */   private String pendingName;
/*  39: 55 */   private JsonElement product = JsonNull.INSTANCE;
/*  40:    */   
/*  41:    */   public JsonTreeWriter()
/*  42:    */   {
/*  43: 58 */     super(UNWRITABLE_WRITER);
/*  44:    */   }
/*  45:    */   
/*  46:    */   public JsonElement get()
/*  47:    */   {
/*  48: 65 */     if (!this.stack.isEmpty()) {
/*  49: 66 */       throw new IllegalStateException("Expected one JSON element but was " + this.stack);
/*  50:    */     }
/*  51: 68 */     return this.product;
/*  52:    */   }
/*  53:    */   
/*  54:    */   private JsonElement peek()
/*  55:    */   {
/*  56: 72 */     return (JsonElement)this.stack.get(this.stack.size() - 1);
/*  57:    */   }
/*  58:    */   
/*  59:    */   private void put(JsonElement value)
/*  60:    */   {
/*  61: 76 */     if (this.pendingName != null)
/*  62:    */     {
/*  63: 77 */       if ((!value.isJsonNull()) || (getSerializeNulls()))
/*  64:    */       {
/*  65: 78 */         JsonObject object = (JsonObject)peek();
/*  66: 79 */         object.add(this.pendingName, value);
/*  67:    */       }
/*  68: 81 */       this.pendingName = null;
/*  69:    */     }
/*  70: 82 */     else if (this.stack.isEmpty())
/*  71:    */     {
/*  72: 83 */       this.product = value;
/*  73:    */     }
/*  74:    */     else
/*  75:    */     {
/*  76: 85 */       JsonElement element = peek();
/*  77: 86 */       if ((element instanceof JsonArray)) {
/*  78: 87 */         ((JsonArray)element).add(value);
/*  79:    */       } else {
/*  80: 89 */         throw new IllegalStateException();
/*  81:    */       }
/*  82:    */     }
/*  83:    */   }
/*  84:    */   
/*  85:    */   public JsonWriter beginArray()
/*  86:    */     throws IOException
/*  87:    */   {
/*  88: 95 */     JsonArray array = new JsonArray();
/*  89: 96 */     put(array);
/*  90: 97 */     this.stack.add(array);
/*  91: 98 */     return this;
/*  92:    */   }
/*  93:    */   
/*  94:    */   public JsonWriter endArray()
/*  95:    */     throws IOException
/*  96:    */   {
/*  97:102 */     if ((this.stack.isEmpty()) || (this.pendingName != null)) {
/*  98:103 */       throw new IllegalStateException();
/*  99:    */     }
/* 100:105 */     JsonElement element = peek();
/* 101:106 */     if ((element instanceof JsonArray))
/* 102:    */     {
/* 103:107 */       this.stack.remove(this.stack.size() - 1);
/* 104:108 */       return this;
/* 105:    */     }
/* 106:110 */     throw new IllegalStateException();
/* 107:    */   }
/* 108:    */   
/* 109:    */   public JsonWriter beginObject()
/* 110:    */     throws IOException
/* 111:    */   {
/* 112:114 */     JsonObject object = new JsonObject();
/* 113:115 */     put(object);
/* 114:116 */     this.stack.add(object);
/* 115:117 */     return this;
/* 116:    */   }
/* 117:    */   
/* 118:    */   public JsonWriter endObject()
/* 119:    */     throws IOException
/* 120:    */   {
/* 121:121 */     if ((this.stack.isEmpty()) || (this.pendingName != null)) {
/* 122:122 */       throw new IllegalStateException();
/* 123:    */     }
/* 124:124 */     JsonElement element = peek();
/* 125:125 */     if ((element instanceof JsonObject))
/* 126:    */     {
/* 127:126 */       this.stack.remove(this.stack.size() - 1);
/* 128:127 */       return this;
/* 129:    */     }
/* 130:129 */     throw new IllegalStateException();
/* 131:    */   }
/* 132:    */   
/* 133:    */   public JsonWriter name(String name)
/* 134:    */     throws IOException
/* 135:    */   {
/* 136:133 */     if ((this.stack.isEmpty()) || (this.pendingName != null)) {
/* 137:134 */       throw new IllegalStateException();
/* 138:    */     }
/* 139:136 */     JsonElement element = peek();
/* 140:137 */     if ((element instanceof JsonObject))
/* 141:    */     {
/* 142:138 */       this.pendingName = name;
/* 143:139 */       return this;
/* 144:    */     }
/* 145:141 */     throw new IllegalStateException();
/* 146:    */   }
/* 147:    */   
/* 148:    */   public JsonWriter value(String value)
/* 149:    */     throws IOException
/* 150:    */   {
/* 151:145 */     if (value == null) {
/* 152:146 */       return nullValue();
/* 153:    */     }
/* 154:148 */     put(new JsonPrimitive(value));
/* 155:149 */     return this;
/* 156:    */   }
/* 157:    */   
/* 158:    */   public JsonWriter nullValue()
/* 159:    */     throws IOException
/* 160:    */   {
/* 161:153 */     put(JsonNull.INSTANCE);
/* 162:154 */     return this;
/* 163:    */   }
/* 164:    */   
/* 165:    */   public JsonWriter value(boolean value)
/* 166:    */     throws IOException
/* 167:    */   {
/* 168:158 */     put(new JsonPrimitive(Boolean.valueOf(value)));
/* 169:159 */     return this;
/* 170:    */   }
/* 171:    */   
/* 172:    */   public JsonWriter value(double value)
/* 173:    */     throws IOException
/* 174:    */   {
/* 175:163 */     if ((!isLenient()) && ((Double.isNaN(value)) || (Double.isInfinite(value)))) {
/* 176:164 */       throw new IllegalArgumentException("JSON forbids NaN and infinities: " + value);
/* 177:    */     }
/* 178:166 */     put(new JsonPrimitive(Double.valueOf(value)));
/* 179:167 */     return this;
/* 180:    */   }
/* 181:    */   
/* 182:    */   public JsonWriter value(long value)
/* 183:    */     throws IOException
/* 184:    */   {
/* 185:171 */     put(new JsonPrimitive(Long.valueOf(value)));
/* 186:172 */     return this;
/* 187:    */   }
/* 188:    */   
/* 189:    */   public JsonWriter value(Number value)
/* 190:    */     throws IOException
/* 191:    */   {
/* 192:176 */     if (value == null) {
/* 193:177 */       return nullValue();
/* 194:    */     }
/* 195:180 */     if (!isLenient())
/* 196:    */     {
/* 197:181 */       double d = value.doubleValue();
/* 198:182 */       if ((Double.isNaN(d)) || (Double.isInfinite(d))) {
/* 199:183 */         throw new IllegalArgumentException("JSON forbids NaN and infinities: " + value);
/* 200:    */       }
/* 201:    */     }
/* 202:187 */     put(new JsonPrimitive(value));
/* 203:188 */     return this;
/* 204:    */   }
/* 205:    */   
/* 206:    */   public void flush()
/* 207:    */     throws IOException
/* 208:    */   {}
/* 209:    */   
/* 210:    */   public void close()
/* 211:    */     throws IOException
/* 212:    */   {
/* 213:195 */     if (!this.stack.isEmpty()) {
/* 214:196 */       throw new IOException("Incomplete document");
/* 215:    */     }
/* 216:198 */     this.stack.add(SENTINEL_CLOSED);
/* 217:    */   }
/* 218:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.internal.bind.JsonTreeWriter
 * JD-Core Version:    0.7.0.1
 */