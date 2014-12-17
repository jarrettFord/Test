/*   1:    */ package com.google.gson.reflect;
/*   2:    */ 
/*   3:    */ import com.google.gson.internal..Gson.Preconditions;
/*   4:    */ import com.google.gson.internal..Gson.Types;
/*   5:    */ import java.lang.reflect.GenericArrayType;
/*   6:    */ import java.lang.reflect.ParameterizedType;
/*   7:    */ import java.lang.reflect.Type;
/*   8:    */ import java.lang.reflect.TypeVariable;
/*   9:    */ import java.util.HashMap;
/*  10:    */ import java.util.Map;
/*  11:    */ 
/*  12:    */ public class TypeToken<T>
/*  13:    */ {
/*  14:    */   final Class<? super T> rawType;
/*  15:    */   final Type type;
/*  16:    */   final int hashCode;
/*  17:    */   
/*  18:    */   protected TypeToken()
/*  19:    */   {
/*  20: 62 */     this.type = getSuperclassTypeParameter(getClass());
/*  21: 63 */     this.rawType = .Gson.Types.getRawType(this.type);
/*  22: 64 */     this.hashCode = this.type.hashCode();
/*  23:    */   }
/*  24:    */   
/*  25:    */   TypeToken(Type type)
/*  26:    */   {
/*  27: 72 */     this.type = .Gson.Types.canonicalize((Type).Gson.Preconditions.checkNotNull(type));
/*  28: 73 */     this.rawType = .Gson.Types.getRawType(this.type);
/*  29: 74 */     this.hashCode = this.type.hashCode();
/*  30:    */   }
/*  31:    */   
/*  32:    */   static Type getSuperclassTypeParameter(Class<?> subclass)
/*  33:    */   {
/*  34: 82 */     Type superclass = subclass.getGenericSuperclass();
/*  35: 83 */     if ((superclass instanceof Class)) {
/*  36: 84 */       throw new RuntimeException("Missing type parameter.");
/*  37:    */     }
/*  38: 86 */     ParameterizedType parameterized = (ParameterizedType)superclass;
/*  39: 87 */     return .Gson.Types.canonicalize(parameterized.getActualTypeArguments()[0]);
/*  40:    */   }
/*  41:    */   
/*  42:    */   public final Class<? super T> getRawType()
/*  43:    */   {
/*  44: 94 */     return this.rawType;
/*  45:    */   }
/*  46:    */   
/*  47:    */   public final Type getType()
/*  48:    */   {
/*  49:101 */     return this.type;
/*  50:    */   }
/*  51:    */   
/*  52:    */   @Deprecated
/*  53:    */   public boolean isAssignableFrom(Class<?> cls)
/*  54:    */   {
/*  55:112 */     return isAssignableFrom(cls);
/*  56:    */   }
/*  57:    */   
/*  58:    */   @Deprecated
/*  59:    */   public boolean isAssignableFrom(Type from)
/*  60:    */   {
/*  61:123 */     if (from == null) {
/*  62:124 */       return false;
/*  63:    */     }
/*  64:127 */     if (this.type.equals(from)) {
/*  65:128 */       return true;
/*  66:    */     }
/*  67:131 */     if ((this.type instanceof Class)) {
/*  68:132 */       return this.rawType.isAssignableFrom(.Gson.Types.getRawType(from));
/*  69:    */     }
/*  70:133 */     if ((this.type instanceof ParameterizedType)) {
/*  71:134 */       return isAssignableFrom(from, (ParameterizedType)this.type, new HashMap());
/*  72:    */     }
/*  73:136 */     if ((this.type instanceof GenericArrayType)) {
/*  74:137 */       return (this.rawType.isAssignableFrom(.Gson.Types.getRawType(from))) && (isAssignableFrom(from, (GenericArrayType)this.type));
/*  75:    */     }
/*  76:140 */     throw buildUnexpectedTypeError(this.type, new Class[] { Class.class, ParameterizedType.class, GenericArrayType.class });
/*  77:    */   }
/*  78:    */   
/*  79:    */   @Deprecated
/*  80:    */   public boolean isAssignableFrom(TypeToken<?> token)
/*  81:    */   {
/*  82:153 */     return isAssignableFrom(token.getType());
/*  83:    */   }
/*  84:    */   
/*  85:    */   private static boolean isAssignableFrom(Type from, GenericArrayType to)
/*  86:    */   {
/*  87:161 */     Type toGenericComponentType = to.getGenericComponentType();
/*  88:162 */     if ((toGenericComponentType instanceof ParameterizedType))
/*  89:    */     {
/*  90:163 */       Type t = from;
/*  91:164 */       if ((from instanceof GenericArrayType))
/*  92:    */       {
/*  93:165 */         t = ((GenericArrayType)from).getGenericComponentType();
/*  94:    */       }
/*  95:166 */       else if ((from instanceof Class))
/*  96:    */       {
/*  97:167 */         Class<?> classType = (Class)from;
/*  98:168 */         while (classType.isArray()) {
/*  99:169 */           classType = classType.getComponentType();
/* 100:    */         }
/* 101:171 */         t = classType;
/* 102:    */       }
/* 103:173 */       return isAssignableFrom(t, (ParameterizedType)toGenericComponentType, new HashMap());
/* 104:    */     }
/* 105:178 */     return true;
/* 106:    */   }
/* 107:    */   
/* 108:    */   private static boolean isAssignableFrom(Type from, ParameterizedType to, Map<String, Type> typeVarMap)
/* 109:    */   {
/* 110:188 */     if (from == null) {
/* 111:189 */       return false;
/* 112:    */     }
/* 113:192 */     if (to.equals(from)) {
/* 114:193 */       return true;
/* 115:    */     }
/* 116:197 */     Class<?> clazz = .Gson.Types.getRawType(from);
/* 117:198 */     ParameterizedType ptype = null;
/* 118:199 */     if ((from instanceof ParameterizedType)) {
/* 119:200 */       ptype = (ParameterizedType)from;
/* 120:    */     }
/* 121:204 */     if (ptype != null)
/* 122:    */     {
/* 123:205 */       Type[] tArgs = ptype.getActualTypeArguments();
/* 124:206 */       TypeVariable<?>[] tParams = clazz.getTypeParameters();
/* 125:207 */       for (int i = 0; i < tArgs.length; i++)
/* 126:    */       {
/* 127:208 */         Type arg = tArgs[i];
/* 128:209 */         TypeVariable<?> var = tParams[i];
/* 129:210 */         while ((arg instanceof TypeVariable))
/* 130:    */         {
/* 131:211 */           TypeVariable<?> v = (TypeVariable)arg;
/* 132:212 */           arg = (Type)typeVarMap.get(v.getName());
/* 133:    */         }
/* 134:214 */         typeVarMap.put(var.getName(), arg);
/* 135:    */       }
/* 136:218 */       if (typeEquals(ptype, to, typeVarMap)) {
/* 137:219 */         return true;
/* 138:    */       }
/* 139:    */     }
/* 140:223 */     for (Type itype : clazz.getGenericInterfaces()) {
/* 141:224 */       if (isAssignableFrom(itype, to, new HashMap(typeVarMap))) {
/* 142:225 */         return true;
/* 143:    */       }
/* 144:    */     }
/* 145:230 */     Type sType = clazz.getGenericSuperclass();
/* 146:231 */     return isAssignableFrom(sType, to, new HashMap(typeVarMap));
/* 147:    */   }
/* 148:    */   
/* 149:    */   private static boolean typeEquals(ParameterizedType from, ParameterizedType to, Map<String, Type> typeVarMap)
/* 150:    */   {
/* 151:240 */     if (from.getRawType().equals(to.getRawType()))
/* 152:    */     {
/* 153:241 */       Type[] fromArgs = from.getActualTypeArguments();
/* 154:242 */       Type[] toArgs = to.getActualTypeArguments();
/* 155:243 */       for (int i = 0; i < fromArgs.length; i++) {
/* 156:244 */         if (!matches(fromArgs[i], toArgs[i], typeVarMap)) {
/* 157:245 */           return false;
/* 158:    */         }
/* 159:    */       }
/* 160:248 */       return true;
/* 161:    */     }
/* 162:250 */     return false;
/* 163:    */   }
/* 164:    */   
/* 165:    */   private static AssertionError buildUnexpectedTypeError(Type token, Class<?>... expected)
/* 166:    */   {
/* 167:257 */     StringBuilder exceptionMessage = new StringBuilder("Unexpected type. Expected one of: ");
/* 168:259 */     for (Class<?> clazz : expected) {
/* 169:260 */       exceptionMessage.append(clazz.getName()).append(", ");
/* 170:    */     }
/* 171:262 */     exceptionMessage.append("but got: ").append(token.getClass().getName()).append(", for type token: ").append(token.toString()).append('.');
/* 172:    */     
/* 173:    */ 
/* 174:265 */     return new AssertionError(exceptionMessage.toString());
/* 175:    */   }
/* 176:    */   
/* 177:    */   private static boolean matches(Type from, Type to, Map<String, Type> typeMap)
/* 178:    */   {
/* 179:273 */     return (to.equals(from)) || (((from instanceof TypeVariable)) && (to.equals(typeMap.get(((TypeVariable)from).getName()))));
/* 180:    */   }
/* 181:    */   
/* 182:    */   public final int hashCode()
/* 183:    */   {
/* 184:280 */     return this.hashCode;
/* 185:    */   }
/* 186:    */   
/* 187:    */   public final boolean equals(Object o)
/* 188:    */   {
/* 189:284 */     return ((o instanceof TypeToken)) && (.Gson.Types.equals(this.type, ((TypeToken)o).type));
/* 190:    */   }
/* 191:    */   
/* 192:    */   public final String toString()
/* 193:    */   {
/* 194:289 */     return .Gson.Types.typeToString(this.type);
/* 195:    */   }
/* 196:    */   
/* 197:    */   public static TypeToken<?> get(Type type)
/* 198:    */   {
/* 199:296 */     return new TypeToken(type);
/* 200:    */   }
/* 201:    */   
/* 202:    */   public static <T> TypeToken<T> get(Class<T> type)
/* 203:    */   {
/* 204:303 */     return new TypeToken(type);
/* 205:    */   }
/* 206:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.reflect.TypeToken
 * JD-Core Version:    0.7.0.1
 */