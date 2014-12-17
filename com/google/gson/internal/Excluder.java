/*   1:    */ package com.google.gson.internal;
/*   2:    */ 
/*   3:    */ import com.google.gson.ExclusionStrategy;
/*   4:    */ import com.google.gson.FieldAttributes;
/*   5:    */ import com.google.gson.Gson;
/*   6:    */ import com.google.gson.TypeAdapter;
/*   7:    */ import com.google.gson.TypeAdapterFactory;
/*   8:    */ import com.google.gson.annotations.Expose;
/*   9:    */ import com.google.gson.annotations.Since;
/*  10:    */ import com.google.gson.annotations.Until;
/*  11:    */ import com.google.gson.reflect.TypeToken;
/*  12:    */ import com.google.gson.stream.JsonReader;
/*  13:    */ import com.google.gson.stream.JsonWriter;
/*  14:    */ import java.io.IOException;
/*  15:    */ import java.lang.reflect.Field;
/*  16:    */ import java.util.ArrayList;
/*  17:    */ import java.util.Collections;
/*  18:    */ import java.util.List;
/*  19:    */ 
/*  20:    */ public final class Excluder
/*  21:    */   implements TypeAdapterFactory, Cloneable
/*  22:    */ {
/*  23:    */   private static final double IGNORE_VERSIONS = -1.0D;
/*  24: 52 */   public static final Excluder DEFAULT = new Excluder();
/*  25: 54 */   private double version = -1.0D;
/*  26: 55 */   private int modifiers = 136;
/*  27: 56 */   private boolean serializeInnerClasses = true;
/*  28:    */   private boolean requireExpose;
/*  29: 58 */   private List<ExclusionStrategy> serializationStrategies = Collections.emptyList();
/*  30: 59 */   private List<ExclusionStrategy> deserializationStrategies = Collections.emptyList();
/*  31:    */   
/*  32:    */   protected Excluder clone()
/*  33:    */   {
/*  34:    */     try
/*  35:    */     {
/*  36: 63 */       return (Excluder)super.clone();
/*  37:    */     }
/*  38:    */     catch (CloneNotSupportedException e)
/*  39:    */     {
/*  40: 65 */       throw new AssertionError();
/*  41:    */     }
/*  42:    */   }
/*  43:    */   
/*  44:    */   public Excluder withVersion(double ignoreVersionsAfter)
/*  45:    */   {
/*  46: 70 */     Excluder result = clone();
/*  47: 71 */     result.version = ignoreVersionsAfter;
/*  48: 72 */     return result;
/*  49:    */   }
/*  50:    */   
/*  51:    */   public Excluder withModifiers(int... modifiers)
/*  52:    */   {
/*  53: 76 */     Excluder result = clone();
/*  54: 77 */     result.modifiers = 0;
/*  55: 78 */     for (int modifier : modifiers) {
/*  56: 79 */       result.modifiers |= modifier;
/*  57:    */     }
/*  58: 81 */     return result;
/*  59:    */   }
/*  60:    */   
/*  61:    */   public Excluder disableInnerClassSerialization()
/*  62:    */   {
/*  63: 85 */     Excluder result = clone();
/*  64: 86 */     result.serializeInnerClasses = false;
/*  65: 87 */     return result;
/*  66:    */   }
/*  67:    */   
/*  68:    */   public Excluder excludeFieldsWithoutExposeAnnotation()
/*  69:    */   {
/*  70: 91 */     Excluder result = clone();
/*  71: 92 */     result.requireExpose = true;
/*  72: 93 */     return result;
/*  73:    */   }
/*  74:    */   
/*  75:    */   public Excluder withExclusionStrategy(ExclusionStrategy exclusionStrategy, boolean serialization, boolean deserialization)
/*  76:    */   {
/*  77: 98 */     Excluder result = clone();
/*  78: 99 */     if (serialization)
/*  79:    */     {
/*  80:100 */       result.serializationStrategies = new ArrayList(this.serializationStrategies);
/*  81:101 */       result.serializationStrategies.add(exclusionStrategy);
/*  82:    */     }
/*  83:103 */     if (deserialization)
/*  84:    */     {
/*  85:104 */       result.deserializationStrategies = new ArrayList(this.deserializationStrategies);
/*  86:    */       
/*  87:106 */       result.deserializationStrategies.add(exclusionStrategy);
/*  88:    */     }
/*  89:108 */     return result;
/*  90:    */   }
/*  91:    */   
/*  92:    */   public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> type)
/*  93:    */   {
/*  94:112 */     Class<?> rawType = type.getRawType();
/*  95:113 */     final boolean skipSerialize = excludeClass(rawType, true);
/*  96:114 */     final boolean skipDeserialize = excludeClass(rawType, false);
/*  97:116 */     if ((!skipSerialize) && (!skipDeserialize)) {
/*  98:117 */       return null;
/*  99:    */     }
/* 100:120 */     new TypeAdapter()
/* 101:    */     {
/* 102:    */       private TypeAdapter<T> delegate;
/* 103:    */       
/* 104:    */       public T read(JsonReader in)
/* 105:    */         throws IOException
/* 106:    */       {
/* 107:125 */         if (skipDeserialize)
/* 108:    */         {
/* 109:126 */           in.skipValue();
/* 110:127 */           return null;
/* 111:    */         }
/* 112:129 */         return delegate().read(in);
/* 113:    */       }
/* 114:    */       
/* 115:    */       public void write(JsonWriter out, T value)
/* 116:    */         throws IOException
/* 117:    */       {
/* 118:133 */         if (skipSerialize)
/* 119:    */         {
/* 120:134 */           out.nullValue();
/* 121:135 */           return;
/* 122:    */         }
/* 123:137 */         delegate().write(out, value);
/* 124:    */       }
/* 125:    */       
/* 126:    */       private TypeAdapter<T> delegate()
/* 127:    */       {
/* 128:141 */         TypeAdapter<T> d = this.delegate;
/* 129:142 */         return this.delegate = gson.getDelegateAdapter(Excluder.this, type);
/* 130:    */       }
/* 131:    */     };
/* 132:    */   }
/* 133:    */   
/* 134:    */   public boolean excludeField(Field field, boolean serialize)
/* 135:    */   {
/* 136:150 */     if ((this.modifiers & field.getModifiers()) != 0) {
/* 137:151 */       return true;
/* 138:    */     }
/* 139:154 */     if ((this.version != -1.0D) && (!isValidVersion((Since)field.getAnnotation(Since.class), (Until)field.getAnnotation(Until.class)))) {
/* 140:156 */       return true;
/* 141:    */     }
/* 142:159 */     if (field.isSynthetic()) {
/* 143:160 */       return true;
/* 144:    */     }
/* 145:163 */     if (this.requireExpose)
/* 146:    */     {
/* 147:164 */       Expose annotation = (Expose)field.getAnnotation(Expose.class);
/* 148:165 */       if ((annotation == null) || (serialize ? !annotation.serialize() : !annotation.deserialize())) {
/* 149:166 */         return true;
/* 150:    */       }
/* 151:    */     }
/* 152:170 */     if ((!this.serializeInnerClasses) && (isInnerClass(field.getType()))) {
/* 153:171 */       return true;
/* 154:    */     }
/* 155:174 */     if (isAnonymousOrLocal(field.getType())) {
/* 156:175 */       return true;
/* 157:    */     }
/* 158:178 */     List<ExclusionStrategy> list = serialize ? this.serializationStrategies : this.deserializationStrategies;
/* 159:    */     FieldAttributes fieldAttributes;
/* 160:179 */     if (!list.isEmpty())
/* 161:    */     {
/* 162:180 */       fieldAttributes = new FieldAttributes(field);
/* 163:181 */       for (ExclusionStrategy exclusionStrategy : list) {
/* 164:182 */         if (exclusionStrategy.shouldSkipField(fieldAttributes)) {
/* 165:183 */           return true;
/* 166:    */         }
/* 167:    */       }
/* 168:    */     }
/* 169:188 */     return false;
/* 170:    */   }
/* 171:    */   
/* 172:    */   public boolean excludeClass(Class<?> clazz, boolean serialize)
/* 173:    */   {
/* 174:192 */     if ((this.version != -1.0D) && (!isValidVersion((Since)clazz.getAnnotation(Since.class), (Until)clazz.getAnnotation(Until.class)))) {
/* 175:194 */       return true;
/* 176:    */     }
/* 177:197 */     if ((!this.serializeInnerClasses) && (isInnerClass(clazz))) {
/* 178:198 */       return true;
/* 179:    */     }
/* 180:201 */     if (isAnonymousOrLocal(clazz)) {
/* 181:202 */       return true;
/* 182:    */     }
/* 183:205 */     List<ExclusionStrategy> list = serialize ? this.serializationStrategies : this.deserializationStrategies;
/* 184:206 */     for (ExclusionStrategy exclusionStrategy : list) {
/* 185:207 */       if (exclusionStrategy.shouldSkipClass(clazz)) {
/* 186:208 */         return true;
/* 187:    */       }
/* 188:    */     }
/* 189:212 */     return false;
/* 190:    */   }
/* 191:    */   
/* 192:    */   private boolean isAnonymousOrLocal(Class<?> clazz)
/* 193:    */   {
/* 194:216 */     return (!Enum.class.isAssignableFrom(clazz)) && ((clazz.isAnonymousClass()) || (clazz.isLocalClass()));
/* 195:    */   }
/* 196:    */   
/* 197:    */   private boolean isInnerClass(Class<?> clazz)
/* 198:    */   {
/* 199:221 */     return (clazz.isMemberClass()) && (!isStatic(clazz));
/* 200:    */   }
/* 201:    */   
/* 202:    */   private boolean isStatic(Class<?> clazz)
/* 203:    */   {
/* 204:225 */     return (clazz.getModifiers() & 0x8) != 0;
/* 205:    */   }
/* 206:    */   
/* 207:    */   private boolean isValidVersion(Since since, Until until)
/* 208:    */   {
/* 209:229 */     return (isValidSince(since)) && (isValidUntil(until));
/* 210:    */   }
/* 211:    */   
/* 212:    */   private boolean isValidSince(Since annotation)
/* 213:    */   {
/* 214:233 */     if (annotation != null)
/* 215:    */     {
/* 216:234 */       double annotationVersion = annotation.value();
/* 217:235 */       if (annotationVersion > this.version) {
/* 218:236 */         return false;
/* 219:    */       }
/* 220:    */     }
/* 221:239 */     return true;
/* 222:    */   }
/* 223:    */   
/* 224:    */   private boolean isValidUntil(Until annotation)
/* 225:    */   {
/* 226:243 */     if (annotation != null)
/* 227:    */     {
/* 228:244 */       double annotationVersion = annotation.value();
/* 229:245 */       if (annotationVersion <= this.version) {
/* 230:246 */         return false;
/* 231:    */       }
/* 232:    */     }
/* 233:249 */     return true;
/* 234:    */   }
/* 235:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.internal.Excluder
 * JD-Core Version:    0.7.0.1
 */