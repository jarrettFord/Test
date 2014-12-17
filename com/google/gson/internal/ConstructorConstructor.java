/*   1:    */ package com.google.gson.internal;
/*   2:    */ 
/*   3:    */ import com.google.gson.InstanceCreator;
/*   4:    */ import com.google.gson.JsonIOException;
/*   5:    */ import com.google.gson.reflect.TypeToken;
/*   6:    */ import java.lang.reflect.Constructor;
/*   7:    */ import java.lang.reflect.InvocationTargetException;
/*   8:    */ import java.lang.reflect.ParameterizedType;
/*   9:    */ import java.lang.reflect.Type;
/*  10:    */ import java.util.ArrayList;
/*  11:    */ import java.util.Collection;
/*  12:    */ import java.util.EnumSet;
/*  13:    */ import java.util.LinkedHashMap;
/*  14:    */ import java.util.LinkedHashSet;
/*  15:    */ import java.util.LinkedList;
/*  16:    */ import java.util.Map;
/*  17:    */ import java.util.Queue;
/*  18:    */ import java.util.Set;
/*  19:    */ import java.util.SortedMap;
/*  20:    */ import java.util.SortedSet;
/*  21:    */ import java.util.TreeMap;
/*  22:    */ import java.util.TreeSet;
/*  23:    */ 
/*  24:    */ public final class ConstructorConstructor
/*  25:    */ {
/*  26:    */   private final Map<Type, InstanceCreator<?>> instanceCreators;
/*  27:    */   
/*  28:    */   public ConstructorConstructor(Map<Type, InstanceCreator<?>> instanceCreators)
/*  29:    */   {
/*  30: 48 */     this.instanceCreators = instanceCreators;
/*  31:    */   }
/*  32:    */   
/*  33:    */   public <T> ObjectConstructor<T> get(TypeToken<T> typeToken)
/*  34:    */   {
/*  35: 52 */     final Type type = typeToken.getType();
/*  36: 53 */     Class<? super T> rawType = typeToken.getRawType();
/*  37:    */     
/*  38:    */ 
/*  39:    */ 
/*  40:    */ 
/*  41: 58 */     final InstanceCreator<T> typeCreator = (InstanceCreator)this.instanceCreators.get(type);
/*  42: 59 */     if (typeCreator != null) {
/*  43: 60 */       new ObjectConstructor()
/*  44:    */       {
/*  45:    */         public T construct()
/*  46:    */         {
/*  47: 62 */           return typeCreator.createInstance(type);
/*  48:    */         }
/*  49:    */       };
/*  50:    */     }
/*  51: 69 */     final InstanceCreator<T> rawTypeCreator = (InstanceCreator)this.instanceCreators.get(rawType);
/*  52: 71 */     if (rawTypeCreator != null) {
/*  53: 72 */       new ObjectConstructor()
/*  54:    */       {
/*  55:    */         public T construct()
/*  56:    */         {
/*  57: 74 */           return rawTypeCreator.createInstance(type);
/*  58:    */         }
/*  59:    */       };
/*  60:    */     }
/*  61: 79 */     ObjectConstructor<T> defaultConstructor = newDefaultConstructor(rawType);
/*  62: 80 */     if (defaultConstructor != null) {
/*  63: 81 */       return defaultConstructor;
/*  64:    */     }
/*  65: 84 */     ObjectConstructor<T> defaultImplementation = newDefaultImplementationConstructor(type, rawType);
/*  66: 85 */     if (defaultImplementation != null) {
/*  67: 86 */       return defaultImplementation;
/*  68:    */     }
/*  69: 90 */     return newUnsafeAllocator(type, rawType);
/*  70:    */   }
/*  71:    */   
/*  72:    */   private <T> ObjectConstructor<T> newDefaultConstructor(Class<? super T> rawType)
/*  73:    */   {
/*  74:    */     try
/*  75:    */     {
/*  76: 95 */       final Constructor<? super T> constructor = rawType.getDeclaredConstructor(new Class[0]);
/*  77: 96 */       if (!constructor.isAccessible()) {
/*  78: 97 */         constructor.setAccessible(true);
/*  79:    */       }
/*  80: 99 */       new ObjectConstructor()
/*  81:    */       {
/*  82:    */         public T construct()
/*  83:    */         {
/*  84:    */           try
/*  85:    */           {
/*  86:103 */             Object[] args = null;
/*  87:104 */             return constructor.newInstance(args);
/*  88:    */           }
/*  89:    */           catch (InstantiationException e)
/*  90:    */           {
/*  91:107 */             throw new RuntimeException("Failed to invoke " + constructor + " with no args", e);
/*  92:    */           }
/*  93:    */           catch (InvocationTargetException e)
/*  94:    */           {
/*  95:111 */             throw new RuntimeException("Failed to invoke " + constructor + " with no args", e.getTargetException());
/*  96:    */           }
/*  97:    */           catch (IllegalAccessException e)
/*  98:    */           {
/*  99:114 */             throw new AssertionError(e);
/* 100:    */           }
/* 101:    */         }
/* 102:    */       };
/* 103:    */     }
/* 104:    */     catch (NoSuchMethodException e) {}
/* 105:119 */     return null;
/* 106:    */   }
/* 107:    */   
/* 108:    */   private <T> ObjectConstructor<T> newDefaultImplementationConstructor(final Type type, Class<? super T> rawType)
/* 109:    */   {
/* 110:130 */     if (Collection.class.isAssignableFrom(rawType))
/* 111:    */     {
/* 112:131 */       if (SortedSet.class.isAssignableFrom(rawType)) {
/* 113:132 */         new ObjectConstructor()
/* 114:    */         {
/* 115:    */           public T construct()
/* 116:    */           {
/* 117:134 */             return new TreeSet();
/* 118:    */           }
/* 119:    */         };
/* 120:    */       }
/* 121:137 */       if (EnumSet.class.isAssignableFrom(rawType)) {
/* 122:138 */         new ObjectConstructor()
/* 123:    */         {
/* 124:    */           public T construct()
/* 125:    */           {
/* 126:141 */             if ((type instanceof ParameterizedType))
/* 127:    */             {
/* 128:142 */               Type elementType = ((ParameterizedType)type).getActualTypeArguments()[0];
/* 129:143 */               if ((elementType instanceof Class)) {
/* 130:144 */                 return EnumSet.noneOf((Class)elementType);
/* 131:    */               }
/* 132:146 */               throw new JsonIOException("Invalid EnumSet type: " + type.toString());
/* 133:    */             }
/* 134:149 */             throw new JsonIOException("Invalid EnumSet type: " + type.toString());
/* 135:    */           }
/* 136:    */         };
/* 137:    */       }
/* 138:153 */       if (Set.class.isAssignableFrom(rawType)) {
/* 139:154 */         new ObjectConstructor()
/* 140:    */         {
/* 141:    */           public T construct()
/* 142:    */           {
/* 143:156 */             return new LinkedHashSet();
/* 144:    */           }
/* 145:    */         };
/* 146:    */       }
/* 147:159 */       if (Queue.class.isAssignableFrom(rawType)) {
/* 148:160 */         new ObjectConstructor()
/* 149:    */         {
/* 150:    */           public T construct()
/* 151:    */           {
/* 152:162 */             return new LinkedList();
/* 153:    */           }
/* 154:    */         };
/* 155:    */       }
/* 156:166 */       new ObjectConstructor()
/* 157:    */       {
/* 158:    */         public T construct()
/* 159:    */         {
/* 160:168 */           return new ArrayList();
/* 161:    */         }
/* 162:    */       };
/* 163:    */     }
/* 164:174 */     if (Map.class.isAssignableFrom(rawType))
/* 165:    */     {
/* 166:175 */       if (SortedMap.class.isAssignableFrom(rawType)) {
/* 167:176 */         new ObjectConstructor()
/* 168:    */         {
/* 169:    */           public T construct()
/* 170:    */           {
/* 171:178 */             return new TreeMap();
/* 172:    */           }
/* 173:    */         };
/* 174:    */       }
/* 175:181 */       if (((type instanceof ParameterizedType)) && (!String.class.isAssignableFrom(TypeToken.get(((ParameterizedType)type).getActualTypeArguments()[0]).getRawType()))) {
/* 176:183 */         new ObjectConstructor()
/* 177:    */         {
/* 178:    */           public T construct()
/* 179:    */           {
/* 180:185 */             return new LinkedHashMap();
/* 181:    */           }
/* 182:    */         };
/* 183:    */       }
/* 184:189 */       new ObjectConstructor()
/* 185:    */       {
/* 186:    */         public T construct()
/* 187:    */         {
/* 188:191 */           return new LinkedTreeMap();
/* 189:    */         }
/* 190:    */       };
/* 191:    */     }
/* 192:197 */     return null;
/* 193:    */   }
/* 194:    */   
/* 195:    */   private <T> ObjectConstructor<T> newUnsafeAllocator(final Type type, final Class<? super T> rawType)
/* 196:    */   {
/* 197:202 */     new ObjectConstructor()
/* 198:    */     {
/* 199:203 */       private final UnsafeAllocator unsafeAllocator = UnsafeAllocator.create();
/* 200:    */       
/* 201:    */       public T construct()
/* 202:    */       {
/* 203:    */         try
/* 204:    */         {
/* 205:207 */           return this.unsafeAllocator.newInstance(rawType);
/* 206:    */         }
/* 207:    */         catch (Exception e)
/* 208:    */         {
/* 209:210 */           throw new RuntimeException("Unable to invoke no-args constructor for " + type + ". " + "Register an InstanceCreator with Gson for this type may fix this problem.", e);
/* 210:    */         }
/* 211:    */       }
/* 212:    */     };
/* 213:    */   }
/* 214:    */   
/* 215:    */   public String toString()
/* 216:    */   {
/* 217:218 */     return this.instanceCreators.toString();
/* 218:    */   }
/* 219:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.internal.ConstructorConstructor
 * JD-Core Version:    0.7.0.1
 */