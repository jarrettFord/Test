/*   1:    */ package com.google.gson.internal.bind;
/*   2:    */ 
/*   3:    */ import com.google.gson.Gson;
/*   4:    */ import com.google.gson.JsonElement;
/*   5:    */ import com.google.gson.JsonPrimitive;
/*   6:    */ import com.google.gson.JsonSyntaxException;
/*   7:    */ import com.google.gson.TypeAdapter;
/*   8:    */ import com.google.gson.TypeAdapterFactory;
/*   9:    */ import com.google.gson.internal..Gson.Types;
/*  10:    */ import com.google.gson.internal.ConstructorConstructor;
/*  11:    */ import com.google.gson.internal.JsonReaderInternalAccess;
/*  12:    */ import com.google.gson.internal.ObjectConstructor;
/*  13:    */ import com.google.gson.internal.Streams;
/*  14:    */ import com.google.gson.reflect.TypeToken;
/*  15:    */ import com.google.gson.stream.JsonReader;
/*  16:    */ import com.google.gson.stream.JsonToken;
/*  17:    */ import com.google.gson.stream.JsonWriter;
/*  18:    */ import java.io.IOException;
/*  19:    */ import java.lang.reflect.Type;
/*  20:    */ import java.util.ArrayList;
/*  21:    */ import java.util.List;
/*  22:    */ import java.util.Map;
/*  23:    */ import java.util.Map.Entry;
/*  24:    */ 
/*  25:    */ public final class MapTypeAdapterFactory
/*  26:    */   implements TypeAdapterFactory
/*  27:    */ {
/*  28:    */   private final ConstructorConstructor constructorConstructor;
/*  29:    */   private final boolean complexMapKeySerialization;
/*  30:    */   
/*  31:    */   public MapTypeAdapterFactory(ConstructorConstructor constructorConstructor, boolean complexMapKeySerialization)
/*  32:    */   {
/*  33:111 */     this.constructorConstructor = constructorConstructor;
/*  34:112 */     this.complexMapKeySerialization = complexMapKeySerialization;
/*  35:    */   }
/*  36:    */   
/*  37:    */   public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken)
/*  38:    */   {
/*  39:116 */     Type type = typeToken.getType();
/*  40:    */     
/*  41:118 */     Class<? super T> rawType = typeToken.getRawType();
/*  42:119 */     if (!Map.class.isAssignableFrom(rawType)) {
/*  43:120 */       return null;
/*  44:    */     }
/*  45:123 */     Class<?> rawTypeOfSrc = .Gson.Types.getRawType(type);
/*  46:124 */     Type[] keyAndValueTypes = .Gson.Types.getMapKeyAndValueTypes(type, rawTypeOfSrc);
/*  47:125 */     TypeAdapter<?> keyAdapter = getKeyAdapter(gson, keyAndValueTypes[0]);
/*  48:126 */     TypeAdapter<?> valueAdapter = gson.getAdapter(TypeToken.get(keyAndValueTypes[1]));
/*  49:127 */     ObjectConstructor<T> constructor = this.constructorConstructor.get(typeToken);
/*  50:    */     
/*  51:    */ 
/*  52:    */ 
/*  53:131 */     TypeAdapter<T> result = new Adapter(gson, keyAndValueTypes[0], keyAdapter, keyAndValueTypes[1], valueAdapter, constructor);
/*  54:    */     
/*  55:133 */     return result;
/*  56:    */   }
/*  57:    */   
/*  58:    */   private TypeAdapter<?> getKeyAdapter(Gson context, Type keyType)
/*  59:    */   {
/*  60:140 */     return (keyType == Boolean.TYPE) || (keyType == Boolean.class) ? TypeAdapters.BOOLEAN_AS_STRING : context.getAdapter(TypeToken.get(keyType));
/*  61:    */   }
/*  62:    */   
/*  63:    */   private final class Adapter<K, V>
/*  64:    */     extends TypeAdapter<Map<K, V>>
/*  65:    */   {
/*  66:    */     private final TypeAdapter<K> keyTypeAdapter;
/*  67:    */     private final TypeAdapter<V> valueTypeAdapter;
/*  68:    */     private final ObjectConstructor<? extends Map<K, V>> constructor;
/*  69:    */     
/*  70:    */     public Adapter(Type context, TypeAdapter<K> keyType, Type keyTypeAdapter, TypeAdapter<V> valueType, ObjectConstructor<? extends Map<K, V>> valueTypeAdapter)
/*  71:    */     {
/*  72:153 */       this.keyTypeAdapter = new TypeAdapterRuntimeTypeWrapper(context, keyTypeAdapter, keyType);
/*  73:    */       
/*  74:155 */       this.valueTypeAdapter = new TypeAdapterRuntimeTypeWrapper(context, valueTypeAdapter, valueType);
/*  75:    */       
/*  76:157 */       this.constructor = constructor;
/*  77:    */     }
/*  78:    */     
/*  79:    */     public Map<K, V> read(JsonReader in)
/*  80:    */       throws IOException
/*  81:    */     {
/*  82:161 */       JsonToken peek = in.peek();
/*  83:162 */       if (peek == JsonToken.NULL)
/*  84:    */       {
/*  85:163 */         in.nextNull();
/*  86:164 */         return null;
/*  87:    */       }
/*  88:167 */       Map<K, V> map = (Map)this.constructor.construct();
/*  89:169 */       if (peek == JsonToken.BEGIN_ARRAY)
/*  90:    */       {
/*  91:170 */         in.beginArray();
/*  92:171 */         while (in.hasNext())
/*  93:    */         {
/*  94:172 */           in.beginArray();
/*  95:173 */           K key = this.keyTypeAdapter.read(in);
/*  96:174 */           V value = this.valueTypeAdapter.read(in);
/*  97:175 */           V replaced = map.put(key, value);
/*  98:176 */           if (replaced != null) {
/*  99:177 */             throw new JsonSyntaxException("duplicate key: " + key);
/* 100:    */           }
/* 101:179 */           in.endArray();
/* 102:    */         }
/* 103:181 */         in.endArray();
/* 104:    */       }
/* 105:    */       else
/* 106:    */       {
/* 107:183 */         in.beginObject();
/* 108:184 */         while (in.hasNext())
/* 109:    */         {
/* 110:185 */           JsonReaderInternalAccess.INSTANCE.promoteNameToValue(in);
/* 111:186 */           K key = this.keyTypeAdapter.read(in);
/* 112:187 */           V value = this.valueTypeAdapter.read(in);
/* 113:188 */           V replaced = map.put(key, value);
/* 114:189 */           if (replaced != null) {
/* 115:190 */             throw new JsonSyntaxException("duplicate key: " + key);
/* 116:    */           }
/* 117:    */         }
/* 118:193 */         in.endObject();
/* 119:    */       }
/* 120:195 */       return map;
/* 121:    */     }
/* 122:    */     
/* 123:    */     public void write(JsonWriter out, Map<K, V> map)
/* 124:    */       throws IOException
/* 125:    */     {
/* 126:199 */       if (map == null)
/* 127:    */       {
/* 128:200 */         out.nullValue();
/* 129:201 */         return;
/* 130:    */       }
/* 131:204 */       if (!MapTypeAdapterFactory.this.complexMapKeySerialization)
/* 132:    */       {
/* 133:205 */         out.beginObject();
/* 134:206 */         for (Map.Entry<K, V> entry : map.entrySet())
/* 135:    */         {
/* 136:207 */           out.name(String.valueOf(entry.getKey()));
/* 137:208 */           this.valueTypeAdapter.write(out, entry.getValue());
/* 138:    */         }
/* 139:210 */         out.endObject();
/* 140:211 */         return;
/* 141:    */       }
/* 142:214 */       boolean hasComplexKeys = false;
/* 143:215 */       List<JsonElement> keys = new ArrayList(map.size());
/* 144:    */       
/* 145:217 */       List<V> values = new ArrayList(map.size());
/* 146:218 */       for (Map.Entry<K, V> entry : map.entrySet())
/* 147:    */       {
/* 148:219 */         JsonElement keyElement = this.keyTypeAdapter.toJsonTree(entry.getKey());
/* 149:220 */         keys.add(keyElement);
/* 150:221 */         values.add(entry.getValue());
/* 151:222 */         hasComplexKeys |= ((keyElement.isJsonArray()) || (keyElement.isJsonObject()));
/* 152:    */       }
/* 153:225 */       if (hasComplexKeys)
/* 154:    */       {
/* 155:226 */         out.beginArray();
/* 156:227 */         for (int i = 0; i < keys.size(); i++)
/* 157:    */         {
/* 158:228 */           out.beginArray();
/* 159:229 */           Streams.write((JsonElement)keys.get(i), out);
/* 160:230 */           this.valueTypeAdapter.write(out, values.get(i));
/* 161:231 */           out.endArray();
/* 162:    */         }
/* 163:233 */         out.endArray();
/* 164:    */       }
/* 165:    */       else
/* 166:    */       {
/* 167:235 */         out.beginObject();
/* 168:236 */         for (int i = 0; i < keys.size(); i++)
/* 169:    */         {
/* 170:237 */           JsonElement keyElement = (JsonElement)keys.get(i);
/* 171:238 */           out.name(keyToString(keyElement));
/* 172:239 */           this.valueTypeAdapter.write(out, values.get(i));
/* 173:    */         }
/* 174:241 */         out.endObject();
/* 175:    */       }
/* 176:    */     }
/* 177:    */     
/* 178:    */     private String keyToString(JsonElement keyElement)
/* 179:    */     {
/* 180:246 */       if (keyElement.isJsonPrimitive())
/* 181:    */       {
/* 182:247 */         JsonPrimitive primitive = keyElement.getAsJsonPrimitive();
/* 183:248 */         if (primitive.isNumber()) {
/* 184:249 */           return String.valueOf(primitive.getAsNumber());
/* 185:    */         }
/* 186:250 */         if (primitive.isBoolean()) {
/* 187:251 */           return Boolean.toString(primitive.getAsBoolean());
/* 188:    */         }
/* 189:252 */         if (primitive.isString()) {
/* 190:253 */           return primitive.getAsString();
/* 191:    */         }
/* 192:255 */         throw new AssertionError();
/* 193:    */       }
/* 194:257 */       if (keyElement.isJsonNull()) {
/* 195:258 */         return "null";
/* 196:    */       }
/* 197:260 */       throw new AssertionError();
/* 198:    */     }
/* 199:    */   }
/* 200:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.internal.bind.MapTypeAdapterFactory
 * JD-Core Version:    0.7.0.1
 */