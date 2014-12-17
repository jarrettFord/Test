/*   1:    */ package com.google.gson;
/*   2:    */ 
/*   3:    */ import com.google.gson.internal..Gson.Preconditions;
/*   4:    */ import com.google.gson.internal.Excluder;
/*   5:    */ import com.google.gson.internal.bind.TypeAdapters;
/*   6:    */ import com.google.gson.reflect.TypeToken;
/*   7:    */ import java.lang.reflect.Type;
/*   8:    */ import java.sql.Timestamp;
/*   9:    */ import java.util.ArrayList;
/*  10:    */ import java.util.Collections;
/*  11:    */ import java.util.HashMap;
/*  12:    */ import java.util.List;
/*  13:    */ import java.util.Map;
/*  14:    */ 
/*  15:    */ public final class GsonBuilder
/*  16:    */ {
/*  17: 69 */   private Excluder excluder = Excluder.DEFAULT;
/*  18: 70 */   private LongSerializationPolicy longSerializationPolicy = LongSerializationPolicy.DEFAULT;
/*  19: 71 */   private FieldNamingStrategy fieldNamingPolicy = FieldNamingPolicy.IDENTITY;
/*  20: 72 */   private final Map<Type, InstanceCreator<?>> instanceCreators = new HashMap();
/*  21: 74 */   private final List<TypeAdapterFactory> factories = new ArrayList();
/*  22: 76 */   private final List<TypeAdapterFactory> hierarchyFactories = new ArrayList();
/*  23:    */   private boolean serializeNulls;
/*  24:    */   private String datePattern;
/*  25: 79 */   private int dateStyle = 2;
/*  26: 80 */   private int timeStyle = 2;
/*  27:    */   private boolean complexMapKeySerialization;
/*  28:    */   private boolean serializeSpecialFloatingPointValues;
/*  29: 83 */   private boolean escapeHtmlChars = true;
/*  30:    */   private boolean prettyPrinting;
/*  31:    */   private boolean generateNonExecutableJson;
/*  32:    */   
/*  33:    */   public GsonBuilder setVersion(double ignoreVersionsAfter)
/*  34:    */   {
/*  35:104 */     this.excluder = this.excluder.withVersion(ignoreVersionsAfter);
/*  36:105 */     return this;
/*  37:    */   }
/*  38:    */   
/*  39:    */   public GsonBuilder excludeFieldsWithModifiers(int... modifiers)
/*  40:    */   {
/*  41:120 */     this.excluder = this.excluder.withModifiers(modifiers);
/*  42:121 */     return this;
/*  43:    */   }
/*  44:    */   
/*  45:    */   public GsonBuilder generateNonExecutableJson()
/*  46:    */   {
/*  47:134 */     this.generateNonExecutableJson = true;
/*  48:135 */     return this;
/*  49:    */   }
/*  50:    */   
/*  51:    */   public GsonBuilder excludeFieldsWithoutExposeAnnotation()
/*  52:    */   {
/*  53:145 */     this.excluder = this.excluder.excludeFieldsWithoutExposeAnnotation();
/*  54:146 */     return this;
/*  55:    */   }
/*  56:    */   
/*  57:    */   public GsonBuilder serializeNulls()
/*  58:    */   {
/*  59:157 */     this.serializeNulls = true;
/*  60:158 */     return this;
/*  61:    */   }
/*  62:    */   
/*  63:    */   public GsonBuilder enableComplexMapKeySerialization()
/*  64:    */   {
/*  65:238 */     this.complexMapKeySerialization = true;
/*  66:239 */     return this;
/*  67:    */   }
/*  68:    */   
/*  69:    */   public GsonBuilder disableInnerClassSerialization()
/*  70:    */   {
/*  71:249 */     this.excluder = this.excluder.disableInnerClassSerialization();
/*  72:250 */     return this;
/*  73:    */   }
/*  74:    */   
/*  75:    */   public GsonBuilder setLongSerializationPolicy(LongSerializationPolicy serializationPolicy)
/*  76:    */   {
/*  77:262 */     this.longSerializationPolicy = serializationPolicy;
/*  78:263 */     return this;
/*  79:    */   }
/*  80:    */   
/*  81:    */   public GsonBuilder setFieldNamingPolicy(FieldNamingPolicy namingConvention)
/*  82:    */   {
/*  83:275 */     this.fieldNamingPolicy = namingConvention;
/*  84:276 */     return this;
/*  85:    */   }
/*  86:    */   
/*  87:    */   public GsonBuilder setFieldNamingStrategy(FieldNamingStrategy fieldNamingStrategy)
/*  88:    */   {
/*  89:288 */     this.fieldNamingPolicy = fieldNamingStrategy;
/*  90:289 */     return this;
/*  91:    */   }
/*  92:    */   
/*  93:    */   public GsonBuilder setExclusionStrategies(ExclusionStrategy... strategies)
/*  94:    */   {
/*  95:303 */     for (ExclusionStrategy strategy : strategies) {
/*  96:304 */       this.excluder = this.excluder.withExclusionStrategy(strategy, true, true);
/*  97:    */     }
/*  98:306 */     return this;
/*  99:    */   }
/* 100:    */   
/* 101:    */   public GsonBuilder addSerializationExclusionStrategy(ExclusionStrategy strategy)
/* 102:    */   {
/* 103:322 */     this.excluder = this.excluder.withExclusionStrategy(strategy, true, false);
/* 104:323 */     return this;
/* 105:    */   }
/* 106:    */   
/* 107:    */   public GsonBuilder addDeserializationExclusionStrategy(ExclusionStrategy strategy)
/* 108:    */   {
/* 109:339 */     this.excluder = this.excluder.withExclusionStrategy(strategy, false, true);
/* 110:340 */     return this;
/* 111:    */   }
/* 112:    */   
/* 113:    */   public GsonBuilder setPrettyPrinting()
/* 114:    */   {
/* 115:350 */     this.prettyPrinting = true;
/* 116:351 */     return this;
/* 117:    */   }
/* 118:    */   
/* 119:    */   public GsonBuilder disableHtmlEscaping()
/* 120:    */   {
/* 121:362 */     this.escapeHtmlChars = false;
/* 122:363 */     return this;
/* 123:    */   }
/* 124:    */   
/* 125:    */   public GsonBuilder setDateFormat(String pattern)
/* 126:    */   {
/* 127:384 */     this.datePattern = pattern;
/* 128:385 */     return this;
/* 129:    */   }
/* 130:    */   
/* 131:    */   public GsonBuilder setDateFormat(int style)
/* 132:    */   {
/* 133:403 */     this.dateStyle = style;
/* 134:404 */     this.datePattern = null;
/* 135:405 */     return this;
/* 136:    */   }
/* 137:    */   
/* 138:    */   public GsonBuilder setDateFormat(int dateStyle, int timeStyle)
/* 139:    */   {
/* 140:424 */     this.dateStyle = dateStyle;
/* 141:425 */     this.timeStyle = timeStyle;
/* 142:426 */     this.datePattern = null;
/* 143:427 */     return this;
/* 144:    */   }
/* 145:    */   
/* 146:    */   public GsonBuilder registerTypeAdapter(Type type, Object typeAdapter)
/* 147:    */   {
/* 148:448 */     .Gson.Preconditions.checkArgument(((typeAdapter instanceof JsonSerializer)) || ((typeAdapter instanceof JsonDeserializer)) || ((typeAdapter instanceof InstanceCreator)) || ((typeAdapter instanceof TypeAdapter)));
/* 149:452 */     if ((typeAdapter instanceof InstanceCreator)) {
/* 150:453 */       this.instanceCreators.put(type, (InstanceCreator)typeAdapter);
/* 151:    */     }
/* 152:455 */     if (((typeAdapter instanceof JsonSerializer)) || ((typeAdapter instanceof JsonDeserializer)))
/* 153:    */     {
/* 154:456 */       TypeToken<?> typeToken = TypeToken.get(type);
/* 155:457 */       this.factories.add(TreeTypeAdapter.newFactoryWithMatchRawType(typeToken, typeAdapter));
/* 156:    */     }
/* 157:459 */     if ((typeAdapter instanceof TypeAdapter)) {
/* 158:460 */       this.factories.add(TypeAdapters.newFactory(TypeToken.get(type), (TypeAdapter)typeAdapter));
/* 159:    */     }
/* 160:462 */     return this;
/* 161:    */   }
/* 162:    */   
/* 163:    */   public GsonBuilder registerTypeAdapterFactory(TypeAdapterFactory factory)
/* 164:    */   {
/* 165:474 */     this.factories.add(factory);
/* 166:475 */     return this;
/* 167:    */   }
/* 168:    */   
/* 169:    */   public GsonBuilder registerTypeHierarchyAdapter(Class<?> baseType, Object typeAdapter)
/* 170:    */   {
/* 171:494 */     .Gson.Preconditions.checkArgument(((typeAdapter instanceof JsonSerializer)) || ((typeAdapter instanceof JsonDeserializer)) || ((typeAdapter instanceof TypeAdapter)));
/* 172:497 */     if (((typeAdapter instanceof JsonDeserializer)) || ((typeAdapter instanceof JsonSerializer))) {
/* 173:498 */       this.hierarchyFactories.add(0, TreeTypeAdapter.newTypeHierarchyFactory(baseType, typeAdapter));
/* 174:    */     }
/* 175:501 */     if ((typeAdapter instanceof TypeAdapter)) {
/* 176:502 */       this.factories.add(TypeAdapters.newTypeHierarchyFactory(baseType, (TypeAdapter)typeAdapter));
/* 177:    */     }
/* 178:504 */     return this;
/* 179:    */   }
/* 180:    */   
/* 181:    */   public GsonBuilder serializeSpecialFloatingPointValues()
/* 182:    */   {
/* 183:528 */     this.serializeSpecialFloatingPointValues = true;
/* 184:529 */     return this;
/* 185:    */   }
/* 186:    */   
/* 187:    */   public Gson create()
/* 188:    */   {
/* 189:539 */     List<TypeAdapterFactory> factories = new ArrayList();
/* 190:540 */     factories.addAll(this.factories);
/* 191:541 */     Collections.reverse(factories);
/* 192:542 */     factories.addAll(this.hierarchyFactories);
/* 193:543 */     addTypeAdaptersForDate(this.datePattern, this.dateStyle, this.timeStyle, factories);
/* 194:    */     
/* 195:545 */     return new Gson(this.excluder, this.fieldNamingPolicy, this.instanceCreators, this.serializeNulls, this.complexMapKeySerialization, this.generateNonExecutableJson, this.escapeHtmlChars, this.prettyPrinting, this.serializeSpecialFloatingPointValues, this.longSerializationPolicy, factories);
/* 196:    */   }
/* 197:    */   
/* 198:    */   private void addTypeAdaptersForDate(String datePattern, int dateStyle, int timeStyle, List<TypeAdapterFactory> factories)
/* 199:    */   {
/* 200:    */     DefaultDateTypeAdapter dateTypeAdapter;
/* 201:554 */     if ((datePattern != null) && (!"".equals(datePattern.trim())))
/* 202:    */     {
/* 203:555 */       dateTypeAdapter = new DefaultDateTypeAdapter(datePattern);
/* 204:    */     }
/* 205:    */     else
/* 206:    */     {
/* 207:    */       DefaultDateTypeAdapter dateTypeAdapter;
/* 208:556 */       if ((dateStyle != 2) && (timeStyle != 2)) {
/* 209:557 */         dateTypeAdapter = new DefaultDateTypeAdapter(dateStyle, timeStyle);
/* 210:    */       } else {
/* 211:    */         return;
/* 212:    */       }
/* 213:    */     }
/* 214:    */     DefaultDateTypeAdapter dateTypeAdapter;
/* 215:562 */     factories.add(TreeTypeAdapter.newFactory(TypeToken.get(java.util.Date.class), dateTypeAdapter));
/* 216:563 */     factories.add(TreeTypeAdapter.newFactory(TypeToken.get(Timestamp.class), dateTypeAdapter));
/* 217:564 */     factories.add(TreeTypeAdapter.newFactory(TypeToken.get(java.sql.Date.class), dateTypeAdapter));
/* 218:    */   }
/* 219:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.GsonBuilder
 * JD-Core Version:    0.7.0.1
 */