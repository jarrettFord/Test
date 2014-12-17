/*   1:    */ package io.netty.util.internal;
/*   2:    */ 
/*   3:    */ import java.lang.reflect.Array;
/*   4:    */ import java.lang.reflect.GenericArrayType;
/*   5:    */ import java.lang.reflect.ParameterizedType;
/*   6:    */ import java.lang.reflect.Type;
/*   7:    */ import java.lang.reflect.TypeVariable;
/*   8:    */ import java.util.HashMap;
/*   9:    */ import java.util.Map;
/*  10:    */ 
/*  11:    */ public abstract class TypeParameterMatcher
/*  12:    */ {
/*  13: 29 */   private static final TypeParameterMatcher NOOP = new NoOpTypeParameterMatcher();
/*  14: 30 */   private static final Object TEST_OBJECT = new Object();
/*  15:    */   
/*  16:    */   public static TypeParameterMatcher get(Class<?> parameterType)
/*  17:    */   {
/*  18: 33 */     Map<Class<?>, TypeParameterMatcher> getCache = InternalThreadLocalMap.get().typeParameterMatcherGetCache();
/*  19:    */     
/*  20:    */ 
/*  21: 36 */     TypeParameterMatcher matcher = (TypeParameterMatcher)getCache.get(parameterType);
/*  22: 37 */     if (matcher == null)
/*  23:    */     {
/*  24: 38 */       if (parameterType == Object.class) {
/*  25: 39 */         matcher = NOOP;
/*  26: 40 */       } else if (PlatformDependent.hasJavassist()) {
/*  27:    */         try
/*  28:    */         {
/*  29: 42 */           matcher = JavassistTypeParameterMatcherGenerator.generate(parameterType);
/*  30: 43 */           matcher.match(TEST_OBJECT);
/*  31:    */         }
/*  32:    */         catch (IllegalAccessError e)
/*  33:    */         {
/*  34: 46 */           matcher = null;
/*  35:    */         }
/*  36:    */         catch (Exception e)
/*  37:    */         {
/*  38: 49 */           matcher = null;
/*  39:    */         }
/*  40:    */       }
/*  41: 53 */       if (matcher == null) {
/*  42: 54 */         matcher = new ReflectiveMatcher(parameterType);
/*  43:    */       }
/*  44: 57 */       getCache.put(parameterType, matcher);
/*  45:    */     }
/*  46: 60 */     return matcher;
/*  47:    */   }
/*  48:    */   
/*  49:    */   public static TypeParameterMatcher find(Object object, Class<?> parameterizedSuperclass, String typeParamName)
/*  50:    */   {
/*  51: 66 */     Map<Class<?>, Map<String, TypeParameterMatcher>> findCache = InternalThreadLocalMap.get().typeParameterMatcherFindCache();
/*  52:    */     
/*  53: 68 */     Class<?> thisClass = object.getClass();
/*  54:    */     
/*  55: 70 */     Map<String, TypeParameterMatcher> map = (Map)findCache.get(thisClass);
/*  56: 71 */     if (map == null)
/*  57:    */     {
/*  58: 72 */       map = new HashMap();
/*  59: 73 */       findCache.put(thisClass, map);
/*  60:    */     }
/*  61: 76 */     TypeParameterMatcher matcher = (TypeParameterMatcher)map.get(typeParamName);
/*  62: 77 */     if (matcher == null)
/*  63:    */     {
/*  64: 78 */       matcher = get(find0(object, parameterizedSuperclass, typeParamName));
/*  65: 79 */       map.put(typeParamName, matcher);
/*  66:    */     }
/*  67: 82 */     return matcher;
/*  68:    */   }
/*  69:    */   
/*  70:    */   private static Class<?> find0(Object object, Class<?> parameterizedSuperclass, String typeParamName)
/*  71:    */   {
/*  72: 88 */     Class<?> thisClass = object.getClass();
/*  73: 89 */     Class<?> currentClass = thisClass;
/*  74:    */     do
/*  75:    */     {
/*  76: 91 */       while (currentClass.getSuperclass() == parameterizedSuperclass)
/*  77:    */       {
/*  78: 92 */         int typeParamIndex = -1;
/*  79: 93 */         TypeVariable<?>[] typeParams = currentClass.getSuperclass().getTypeParameters();
/*  80: 94 */         for (int i = 0; i < typeParams.length; i++) {
/*  81: 95 */           if (typeParamName.equals(typeParams[i].getName()))
/*  82:    */           {
/*  83: 96 */             typeParamIndex = i;
/*  84: 97 */             break;
/*  85:    */           }
/*  86:    */         }
/*  87:101 */         if (typeParamIndex < 0) {
/*  88:102 */           throw new IllegalStateException("unknown type parameter '" + typeParamName + "': " + parameterizedSuperclass);
/*  89:    */         }
/*  90:106 */         Type genericSuperType = currentClass.getGenericSuperclass();
/*  91:107 */         if (!(genericSuperType instanceof ParameterizedType)) {
/*  92:108 */           return Object.class;
/*  93:    */         }
/*  94:111 */         Type[] actualTypeParams = ((ParameterizedType)genericSuperType).getActualTypeArguments();
/*  95:    */         
/*  96:113 */         Type actualTypeParam = actualTypeParams[typeParamIndex];
/*  97:114 */         if ((actualTypeParam instanceof ParameterizedType)) {
/*  98:115 */           actualTypeParam = ((ParameterizedType)actualTypeParam).getRawType();
/*  99:    */         }
/* 100:117 */         if ((actualTypeParam instanceof Class)) {
/* 101:118 */           return (Class)actualTypeParam;
/* 102:    */         }
/* 103:120 */         if ((actualTypeParam instanceof GenericArrayType))
/* 104:    */         {
/* 105:121 */           Type componentType = ((GenericArrayType)actualTypeParam).getGenericComponentType();
/* 106:122 */           if ((componentType instanceof ParameterizedType)) {
/* 107:123 */             componentType = ((ParameterizedType)componentType).getRawType();
/* 108:    */           }
/* 109:125 */           if ((componentType instanceof Class)) {
/* 110:126 */             return Array.newInstance((Class)componentType, 0).getClass();
/* 111:    */           }
/* 112:    */         }
/* 113:129 */         if ((actualTypeParam instanceof TypeVariable))
/* 114:    */         {
/* 115:131 */           TypeVariable<?> v = (TypeVariable)actualTypeParam;
/* 116:132 */           currentClass = thisClass;
/* 117:133 */           if (!(v.getGenericDeclaration() instanceof Class)) {
/* 118:134 */             return Object.class;
/* 119:    */           }
/* 120:137 */           parameterizedSuperclass = (Class)v.getGenericDeclaration();
/* 121:138 */           typeParamName = v.getName();
/* 122:139 */           if (!parameterizedSuperclass.isAssignableFrom(thisClass)) {
/* 123:142 */             return Object.class;
/* 124:    */           }
/* 125:    */         }
/* 126:    */         else
/* 127:    */         {
/* 128:146 */           return fail(thisClass, typeParamName);
/* 129:    */         }
/* 130:    */       }
/* 131:148 */       currentClass = currentClass.getSuperclass();
/* 132:149 */     } while (currentClass != null);
/* 133:150 */     return fail(thisClass, typeParamName);
/* 134:    */   }
/* 135:    */   
/* 136:    */   private static Class<?> fail(Class<?> type, String typeParamName)
/* 137:    */   {
/* 138:156 */     throw new IllegalStateException("cannot determine the type of the type parameter '" + typeParamName + "': " + type);
/* 139:    */   }
/* 140:    */   
/* 141:    */   public abstract boolean match(Object paramObject);
/* 142:    */   
/* 143:    */   private static final class ReflectiveMatcher
/* 144:    */     extends TypeParameterMatcher
/* 145:    */   {
/* 146:    */     private final Class<?> type;
/* 147:    */     
/* 148:    */     ReflectiveMatcher(Class<?> type)
/* 149:    */     {
/* 150:166 */       this.type = type;
/* 151:    */     }
/* 152:    */     
/* 153:    */     public boolean match(Object msg)
/* 154:    */     {
/* 155:171 */       return this.type.isInstance(msg);
/* 156:    */     }
/* 157:    */   }
/* 158:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.TypeParameterMatcher
 * JD-Core Version:    0.7.0.1
 */