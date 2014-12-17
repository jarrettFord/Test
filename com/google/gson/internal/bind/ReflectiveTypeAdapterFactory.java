/*   1:    */ package com.google.gson.internal.bind;
/*   2:    */ 
/*   3:    */ import com.google.gson.FieldNamingStrategy;
/*   4:    */ import com.google.gson.Gson;
/*   5:    */ import com.google.gson.JsonSyntaxException;
/*   6:    */ import com.google.gson.TypeAdapter;
/*   7:    */ import com.google.gson.TypeAdapterFactory;
/*   8:    */ import com.google.gson.annotations.SerializedName;
/*   9:    */ import com.google.gson.internal..Gson.Types;
/*  10:    */ import com.google.gson.internal.ConstructorConstructor;
/*  11:    */ import com.google.gson.internal.Excluder;
/*  12:    */ import com.google.gson.internal.ObjectConstructor;
/*  13:    */ import com.google.gson.internal.Primitives;
/*  14:    */ import com.google.gson.reflect.TypeToken;
/*  15:    */ import com.google.gson.stream.JsonReader;
/*  16:    */ import com.google.gson.stream.JsonToken;
/*  17:    */ import com.google.gson.stream.JsonWriter;
/*  18:    */ import java.io.IOException;
/*  19:    */ import java.lang.reflect.Field;
/*  20:    */ import java.lang.reflect.Type;
/*  21:    */ import java.util.LinkedHashMap;
/*  22:    */ import java.util.Map;
/*  23:    */ 
/*  24:    */ public final class ReflectiveTypeAdapterFactory
/*  25:    */   implements TypeAdapterFactory
/*  26:    */ {
/*  27:    */   private final ConstructorConstructor constructorConstructor;
/*  28:    */   private final FieldNamingStrategy fieldNamingPolicy;
/*  29:    */   private final Excluder excluder;
/*  30:    */   
/*  31:    */   public ReflectiveTypeAdapterFactory(ConstructorConstructor constructorConstructor, FieldNamingStrategy fieldNamingPolicy, Excluder excluder)
/*  32:    */   {
/*  33: 50 */     this.constructorConstructor = constructorConstructor;
/*  34: 51 */     this.fieldNamingPolicy = fieldNamingPolicy;
/*  35: 52 */     this.excluder = excluder;
/*  36:    */   }
/*  37:    */   
/*  38:    */   public boolean excludeField(Field f, boolean serialize)
/*  39:    */   {
/*  40: 56 */     return (!this.excluder.excludeClass(f.getType(), serialize)) && (!this.excluder.excludeField(f, serialize));
/*  41:    */   }
/*  42:    */   
/*  43:    */   private String getFieldName(Field f)
/*  44:    */   {
/*  45: 60 */     SerializedName serializedName = (SerializedName)f.getAnnotation(SerializedName.class);
/*  46: 61 */     return serializedName == null ? this.fieldNamingPolicy.translateName(f) : serializedName.value();
/*  47:    */   }
/*  48:    */   
/*  49:    */   public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type)
/*  50:    */   {
/*  51: 65 */     Class<? super T> raw = type.getRawType();
/*  52: 67 */     if (!Object.class.isAssignableFrom(raw)) {
/*  53: 68 */       return null;
/*  54:    */     }
/*  55: 71 */     ObjectConstructor<T> constructor = this.constructorConstructor.get(type);
/*  56: 72 */     return new Adapter(constructor, getBoundFields(gson, type, raw), null);
/*  57:    */   }
/*  58:    */   
/*  59:    */   private BoundField createBoundField(final Gson context, final Field field, String name, final TypeToken<?> fieldType, boolean serialize, boolean deserialize)
/*  60:    */   {
/*  61: 78 */     final boolean isPrimitive = Primitives.isPrimitive(fieldType.getRawType());
/*  62:    */     
/*  63:    */ 
/*  64: 81 */     new BoundField(name, serialize, deserialize)
/*  65:    */     {
/*  66: 82 */       final TypeAdapter<?> typeAdapter = context.getAdapter(fieldType);
/*  67:    */       
/*  68:    */       void write(JsonWriter writer, Object value)
/*  69:    */         throws IOException, IllegalAccessException
/*  70:    */       {
/*  71: 86 */         Object fieldValue = field.get(value);
/*  72: 87 */         TypeAdapter t = new TypeAdapterRuntimeTypeWrapper(context, this.typeAdapter, fieldType.getType());
/*  73:    */         
/*  74: 89 */         t.write(writer, fieldValue);
/*  75:    */       }
/*  76:    */       
/*  77:    */       void read(JsonReader reader, Object value)
/*  78:    */         throws IOException, IllegalAccessException
/*  79:    */       {
/*  80: 93 */         Object fieldValue = this.typeAdapter.read(reader);
/*  81: 94 */         if ((fieldValue != null) || (!isPrimitive)) {
/*  82: 95 */           field.set(value, fieldValue);
/*  83:    */         }
/*  84:    */       }
/*  85:    */     };
/*  86:    */   }
/*  87:    */   
/*  88:    */   private Map<String, BoundField> getBoundFields(Gson context, TypeToken<?> type, Class<?> raw)
/*  89:    */   {
/*  90:102 */     Map<String, BoundField> result = new LinkedHashMap();
/*  91:103 */     if (raw.isInterface()) {
/*  92:104 */       return result;
/*  93:    */     }
/*  94:107 */     Type declaredType = type.getType();
/*  95:108 */     while (raw != Object.class)
/*  96:    */     {
/*  97:109 */       Field[] fields = raw.getDeclaredFields();
/*  98:110 */       for (Field field : fields)
/*  99:    */       {
/* 100:111 */         boolean serialize = excludeField(field, true);
/* 101:112 */         boolean deserialize = excludeField(field, false);
/* 102:113 */         if ((serialize) || (deserialize))
/* 103:    */         {
/* 104:116 */           field.setAccessible(true);
/* 105:117 */           Type fieldType = .Gson.Types.resolve(type.getType(), raw, field.getGenericType());
/* 106:118 */           BoundField boundField = createBoundField(context, field, getFieldName(field), TypeToken.get(fieldType), serialize, deserialize);
/* 107:    */           
/* 108:120 */           BoundField previous = (BoundField)result.put(boundField.name, boundField);
/* 109:121 */           if (previous != null) {
/* 110:122 */             throw new IllegalArgumentException(declaredType + " declares multiple JSON fields named " + previous.name);
/* 111:    */           }
/* 112:    */         }
/* 113:    */       }
/* 114:126 */       type = TypeToken.get(.Gson.Types.resolve(type.getType(), raw, raw.getGenericSuperclass()));
/* 115:127 */       raw = type.getRawType();
/* 116:    */     }
/* 117:129 */     return result;
/* 118:    */   }
/* 119:    */   
/* 120:    */   static abstract class BoundField
/* 121:    */   {
/* 122:    */     final String name;
/* 123:    */     final boolean serialized;
/* 124:    */     final boolean deserialized;
/* 125:    */     
/* 126:    */     protected BoundField(String name, boolean serialized, boolean deserialized)
/* 127:    */     {
/* 128:138 */       this.name = name;
/* 129:139 */       this.serialized = serialized;
/* 130:140 */       this.deserialized = deserialized;
/* 131:    */     }
/* 132:    */     
/* 133:    */     abstract void write(JsonWriter paramJsonWriter, Object paramObject)
/* 134:    */       throws IOException, IllegalAccessException;
/* 135:    */     
/* 136:    */     abstract void read(JsonReader paramJsonReader, Object paramObject)
/* 137:    */       throws IOException, IllegalAccessException;
/* 138:    */   }
/* 139:    */   
/* 140:    */   public static final class Adapter<T>
/* 141:    */     extends TypeAdapter<T>
/* 142:    */   {
/* 143:    */     private final ObjectConstructor<T> constructor;
/* 144:    */     private final Map<String, ReflectiveTypeAdapterFactory.BoundField> boundFields;
/* 145:    */     
/* 146:    */     private Adapter(ObjectConstructor<T> constructor, Map<String, ReflectiveTypeAdapterFactory.BoundField> boundFields)
/* 147:    */     {
/* 148:152 */       this.constructor = constructor;
/* 149:153 */       this.boundFields = boundFields;
/* 150:    */     }
/* 151:    */     
/* 152:    */     public T read(JsonReader in)
/* 153:    */       throws IOException
/* 154:    */     {
/* 155:157 */       if (in.peek() == JsonToken.NULL)
/* 156:    */       {
/* 157:158 */         in.nextNull();
/* 158:159 */         return null;
/* 159:    */       }
/* 160:162 */       T instance = this.constructor.construct();
/* 161:    */       try
/* 162:    */       {
/* 163:165 */         in.beginObject();
/* 164:166 */         while (in.hasNext())
/* 165:    */         {
/* 166:167 */           String name = in.nextName();
/* 167:168 */           ReflectiveTypeAdapterFactory.BoundField field = (ReflectiveTypeAdapterFactory.BoundField)this.boundFields.get(name);
/* 168:169 */           if ((field == null) || (!field.deserialized)) {
/* 169:170 */             in.skipValue();
/* 170:    */           } else {
/* 171:172 */             field.read(in, instance);
/* 172:    */           }
/* 173:    */         }
/* 174:    */       }
/* 175:    */       catch (IllegalStateException e)
/* 176:    */       {
/* 177:176 */         throw new JsonSyntaxException(e);
/* 178:    */       }
/* 179:    */       catch (IllegalAccessException e)
/* 180:    */       {
/* 181:178 */         throw new AssertionError(e);
/* 182:    */       }
/* 183:180 */       in.endObject();
/* 184:181 */       return instance;
/* 185:    */     }
/* 186:    */     
/* 187:    */     public void write(JsonWriter out, T value)
/* 188:    */       throws IOException
/* 189:    */     {
/* 190:185 */       if (value == null)
/* 191:    */       {
/* 192:186 */         out.nullValue();
/* 193:187 */         return;
/* 194:    */       }
/* 195:190 */       out.beginObject();
/* 196:    */       try
/* 197:    */       {
/* 198:192 */         for (ReflectiveTypeAdapterFactory.BoundField boundField : this.boundFields.values()) {
/* 199:193 */           if (boundField.serialized)
/* 200:    */           {
/* 201:194 */             out.name(boundField.name);
/* 202:195 */             boundField.write(out, value);
/* 203:    */           }
/* 204:    */         }
/* 205:    */       }
/* 206:    */       catch (IllegalAccessException e)
/* 207:    */       {
/* 208:199 */         throw new AssertionError();
/* 209:    */       }
/* 210:201 */       out.endObject();
/* 211:    */     }
/* 212:    */   }
/* 213:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.internal.bind.ReflectiveTypeAdapterFactory
 * JD-Core Version:    0.7.0.1
 */