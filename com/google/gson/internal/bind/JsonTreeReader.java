/*   1:    */ package com.google.gson.internal.bind;
/*   2:    */ 
/*   3:    */ import com.google.gson.JsonArray;
/*   4:    */ import com.google.gson.JsonElement;
/*   5:    */ import com.google.gson.JsonNull;
/*   6:    */ import com.google.gson.JsonObject;
/*   7:    */ import com.google.gson.JsonPrimitive;
/*   8:    */ import com.google.gson.stream.JsonReader;
/*   9:    */ import com.google.gson.stream.JsonToken;
/*  10:    */ import java.io.IOException;
/*  11:    */ import java.io.Reader;
/*  12:    */ import java.util.ArrayList;
/*  13:    */ import java.util.Iterator;
/*  14:    */ import java.util.List;
/*  15:    */ import java.util.Map.Entry;
/*  16:    */ import java.util.Set;
/*  17:    */ 
/*  18:    */ public final class JsonTreeReader
/*  19:    */   extends JsonReader
/*  20:    */ {
/*  21: 40 */   private static final Reader UNREADABLE_READER = new Reader()
/*  22:    */   {
/*  23:    */     public int read(char[] buffer, int offset, int count)
/*  24:    */       throws IOException
/*  25:    */     {
/*  26: 42 */       throw new AssertionError();
/*  27:    */     }
/*  28:    */     
/*  29:    */     public void close()
/*  30:    */       throws IOException
/*  31:    */     {
/*  32: 45 */       throw new AssertionError();
/*  33:    */     }
/*  34:    */   };
/*  35: 48 */   private static final Object SENTINEL_CLOSED = new Object();
/*  36: 50 */   private final List<Object> stack = new ArrayList();
/*  37:    */   
/*  38:    */   public JsonTreeReader(JsonElement element)
/*  39:    */   {
/*  40: 53 */     super(UNREADABLE_READER);
/*  41: 54 */     this.stack.add(element);
/*  42:    */   }
/*  43:    */   
/*  44:    */   public void beginArray()
/*  45:    */     throws IOException
/*  46:    */   {
/*  47: 58 */     expect(JsonToken.BEGIN_ARRAY);
/*  48: 59 */     JsonArray array = (JsonArray)peekStack();
/*  49: 60 */     this.stack.add(array.iterator());
/*  50:    */   }
/*  51:    */   
/*  52:    */   public void endArray()
/*  53:    */     throws IOException
/*  54:    */   {
/*  55: 64 */     expect(JsonToken.END_ARRAY);
/*  56: 65 */     popStack();
/*  57: 66 */     popStack();
/*  58:    */   }
/*  59:    */   
/*  60:    */   public void beginObject()
/*  61:    */     throws IOException
/*  62:    */   {
/*  63: 70 */     expect(JsonToken.BEGIN_OBJECT);
/*  64: 71 */     JsonObject object = (JsonObject)peekStack();
/*  65: 72 */     this.stack.add(object.entrySet().iterator());
/*  66:    */   }
/*  67:    */   
/*  68:    */   public void endObject()
/*  69:    */     throws IOException
/*  70:    */   {
/*  71: 76 */     expect(JsonToken.END_OBJECT);
/*  72: 77 */     popStack();
/*  73: 78 */     popStack();
/*  74:    */   }
/*  75:    */   
/*  76:    */   public boolean hasNext()
/*  77:    */     throws IOException
/*  78:    */   {
/*  79: 82 */     JsonToken token = peek();
/*  80: 83 */     return (token != JsonToken.END_OBJECT) && (token != JsonToken.END_ARRAY);
/*  81:    */   }
/*  82:    */   
/*  83:    */   public JsonToken peek()
/*  84:    */     throws IOException
/*  85:    */   {
/*  86: 87 */     if (this.stack.isEmpty()) {
/*  87: 88 */       return JsonToken.END_DOCUMENT;
/*  88:    */     }
/*  89: 91 */     Object o = peekStack();
/*  90: 92 */     if ((o instanceof Iterator))
/*  91:    */     {
/*  92: 93 */       boolean isObject = this.stack.get(this.stack.size() - 2) instanceof JsonObject;
/*  93: 94 */       Iterator<?> iterator = (Iterator)o;
/*  94: 95 */       if (iterator.hasNext())
/*  95:    */       {
/*  96: 96 */         if (isObject) {
/*  97: 97 */           return JsonToken.NAME;
/*  98:    */         }
/*  99: 99 */         this.stack.add(iterator.next());
/* 100:100 */         return peek();
/* 101:    */       }
/* 102:103 */       return isObject ? JsonToken.END_OBJECT : JsonToken.END_ARRAY;
/* 103:    */     }
/* 104:105 */     if ((o instanceof JsonObject)) {
/* 105:106 */       return JsonToken.BEGIN_OBJECT;
/* 106:    */     }
/* 107:107 */     if ((o instanceof JsonArray)) {
/* 108:108 */       return JsonToken.BEGIN_ARRAY;
/* 109:    */     }
/* 110:109 */     if ((o instanceof JsonPrimitive))
/* 111:    */     {
/* 112:110 */       JsonPrimitive primitive = (JsonPrimitive)o;
/* 113:111 */       if (primitive.isString()) {
/* 114:112 */         return JsonToken.STRING;
/* 115:    */       }
/* 116:113 */       if (primitive.isBoolean()) {
/* 117:114 */         return JsonToken.BOOLEAN;
/* 118:    */       }
/* 119:115 */       if (primitive.isNumber()) {
/* 120:116 */         return JsonToken.NUMBER;
/* 121:    */       }
/* 122:118 */       throw new AssertionError();
/* 123:    */     }
/* 124:120 */     if ((o instanceof JsonNull)) {
/* 125:121 */       return JsonToken.NULL;
/* 126:    */     }
/* 127:122 */     if (o == SENTINEL_CLOSED) {
/* 128:123 */       throw new IllegalStateException("JsonReader is closed");
/* 129:    */     }
/* 130:125 */     throw new AssertionError();
/* 131:    */   }
/* 132:    */   
/* 133:    */   private Object peekStack()
/* 134:    */   {
/* 135:130 */     return this.stack.get(this.stack.size() - 1);
/* 136:    */   }
/* 137:    */   
/* 138:    */   private Object popStack()
/* 139:    */   {
/* 140:134 */     return this.stack.remove(this.stack.size() - 1);
/* 141:    */   }
/* 142:    */   
/* 143:    */   private void expect(JsonToken expected)
/* 144:    */     throws IOException
/* 145:    */   {
/* 146:138 */     if (peek() != expected) {
/* 147:139 */       throw new IllegalStateException("Expected " + expected + " but was " + peek());
/* 148:    */     }
/* 149:    */   }
/* 150:    */   
/* 151:    */   public String nextName()
/* 152:    */     throws IOException
/* 153:    */   {
/* 154:144 */     expect(JsonToken.NAME);
/* 155:145 */     Iterator<?> i = (Iterator)peekStack();
/* 156:146 */     Map.Entry<?, ?> entry = (Map.Entry)i.next();
/* 157:147 */     this.stack.add(entry.getValue());
/* 158:148 */     return (String)entry.getKey();
/* 159:    */   }
/* 160:    */   
/* 161:    */   public String nextString()
/* 162:    */     throws IOException
/* 163:    */   {
/* 164:152 */     JsonToken token = peek();
/* 165:153 */     if ((token != JsonToken.STRING) && (token != JsonToken.NUMBER)) {
/* 166:154 */       throw new IllegalStateException("Expected " + JsonToken.STRING + " but was " + token);
/* 167:    */     }
/* 168:156 */     return ((JsonPrimitive)popStack()).getAsString();
/* 169:    */   }
/* 170:    */   
/* 171:    */   public boolean nextBoolean()
/* 172:    */     throws IOException
/* 173:    */   {
/* 174:160 */     expect(JsonToken.BOOLEAN);
/* 175:161 */     return ((JsonPrimitive)popStack()).getAsBoolean();
/* 176:    */   }
/* 177:    */   
/* 178:    */   public void nextNull()
/* 179:    */     throws IOException
/* 180:    */   {
/* 181:165 */     expect(JsonToken.NULL);
/* 182:166 */     popStack();
/* 183:    */   }
/* 184:    */   
/* 185:    */   public double nextDouble()
/* 186:    */     throws IOException
/* 187:    */   {
/* 188:170 */     JsonToken token = peek();
/* 189:171 */     if ((token != JsonToken.NUMBER) && (token != JsonToken.STRING)) {
/* 190:172 */       throw new IllegalStateException("Expected " + JsonToken.NUMBER + " but was " + token);
/* 191:    */     }
/* 192:174 */     double result = ((JsonPrimitive)peekStack()).getAsDouble();
/* 193:175 */     if ((!isLenient()) && ((Double.isNaN(result)) || (Double.isInfinite(result)))) {
/* 194:176 */       throw new NumberFormatException("JSON forbids NaN and infinities: " + result);
/* 195:    */     }
/* 196:178 */     popStack();
/* 197:179 */     return result;
/* 198:    */   }
/* 199:    */   
/* 200:    */   public long nextLong()
/* 201:    */     throws IOException
/* 202:    */   {
/* 203:183 */     JsonToken token = peek();
/* 204:184 */     if ((token != JsonToken.NUMBER) && (token != JsonToken.STRING)) {
/* 205:185 */       throw new IllegalStateException("Expected " + JsonToken.NUMBER + " but was " + token);
/* 206:    */     }
/* 207:187 */     long result = ((JsonPrimitive)peekStack()).getAsLong();
/* 208:188 */     popStack();
/* 209:189 */     return result;
/* 210:    */   }
/* 211:    */   
/* 212:    */   public int nextInt()
/* 213:    */     throws IOException
/* 214:    */   {
/* 215:193 */     JsonToken token = peek();
/* 216:194 */     if ((token != JsonToken.NUMBER) && (token != JsonToken.STRING)) {
/* 217:195 */       throw new IllegalStateException("Expected " + JsonToken.NUMBER + " but was " + token);
/* 218:    */     }
/* 219:197 */     int result = ((JsonPrimitive)peekStack()).getAsInt();
/* 220:198 */     popStack();
/* 221:199 */     return result;
/* 222:    */   }
/* 223:    */   
/* 224:    */   public void close()
/* 225:    */     throws IOException
/* 226:    */   {
/* 227:203 */     this.stack.clear();
/* 228:204 */     this.stack.add(SENTINEL_CLOSED);
/* 229:    */   }
/* 230:    */   
/* 231:    */   public void skipValue()
/* 232:    */     throws IOException
/* 233:    */   {
/* 234:208 */     if (peek() == JsonToken.NAME) {
/* 235:209 */       nextName();
/* 236:    */     } else {
/* 237:211 */       popStack();
/* 238:    */     }
/* 239:    */   }
/* 240:    */   
/* 241:    */   public String toString()
/* 242:    */   {
/* 243:216 */     return getClass().getSimpleName();
/* 244:    */   }
/* 245:    */   
/* 246:    */   public void promoteNameToValue()
/* 247:    */     throws IOException
/* 248:    */   {
/* 249:220 */     expect(JsonToken.NAME);
/* 250:221 */     Iterator<?> i = (Iterator)peekStack();
/* 251:222 */     Map.Entry<?, ?> entry = (Map.Entry)i.next();
/* 252:223 */     this.stack.add(entry.getValue());
/* 253:224 */     this.stack.add(new JsonPrimitive((String)entry.getKey()));
/* 254:    */   }
/* 255:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.internal.bind.JsonTreeReader
 * JD-Core Version:    0.7.0.1
 */