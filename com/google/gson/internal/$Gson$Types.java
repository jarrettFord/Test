/*   1:    */ package com.google.gson.internal;
/*   2:    */ 
/*   3:    */ import java.io.Serializable;
/*   4:    */ import java.lang.reflect.Array;
/*   5:    */ import java.lang.reflect.GenericArrayType;
/*   6:    */ import java.lang.reflect.GenericDeclaration;
/*   7:    */ import java.lang.reflect.ParameterizedType;
/*   8:    */ import java.lang.reflect.Type;
/*   9:    */ import java.lang.reflect.TypeVariable;
/*  10:    */ import java.lang.reflect.WildcardType;
/*  11:    */ import java.util.Arrays;
/*  12:    */ import java.util.Collection;
/*  13:    */ import java.util.Map;
/*  14:    */ import java.util.NoSuchElementException;
/*  15:    */ import java.util.Properties;
/*  16:    */ 
/*  17:    */ public final class $Gson$Types
/*  18:    */ {
/*  19: 43 */   static final Type[] EMPTY_TYPE_ARRAY = new Type[0];
/*  20:    */   
/*  21:    */   public static ParameterizedType newParameterizedTypeWithOwner(Type ownerType, Type rawType, Type... typeArguments)
/*  22:    */   {
/*  23: 55 */     return new ParameterizedTypeImpl(ownerType, rawType, typeArguments);
/*  24:    */   }
/*  25:    */   
/*  26:    */   public static GenericArrayType arrayOf(Type componentType)
/*  27:    */   {
/*  28: 65 */     return new GenericArrayTypeImpl(componentType);
/*  29:    */   }
/*  30:    */   
/*  31:    */   public static WildcardType subtypeOf(Type bound)
/*  32:    */   {
/*  33: 75 */     return new WildcardTypeImpl(new Type[] { bound }, EMPTY_TYPE_ARRAY);
/*  34:    */   }
/*  35:    */   
/*  36:    */   public static WildcardType supertypeOf(Type bound)
/*  37:    */   {
/*  38: 84 */     return new WildcardTypeImpl(new Type[] { Object.class }, new Type[] { bound });
/*  39:    */   }
/*  40:    */   
/*  41:    */   public static Type canonicalize(Type type)
/*  42:    */   {
/*  43: 93 */     if ((type instanceof Class))
/*  44:    */     {
/*  45: 94 */       Class<?> c = (Class)type;
/*  46: 95 */       return c.isArray() ? new GenericArrayTypeImpl(canonicalize(c.getComponentType())) : c;
/*  47:    */     }
/*  48: 97 */     if ((type instanceof ParameterizedType))
/*  49:    */     {
/*  50: 98 */       ParameterizedType p = (ParameterizedType)type;
/*  51: 99 */       return new ParameterizedTypeImpl(p.getOwnerType(), p.getRawType(), p.getActualTypeArguments());
/*  52:    */     }
/*  53:102 */     if ((type instanceof GenericArrayType))
/*  54:    */     {
/*  55:103 */       GenericArrayType g = (GenericArrayType)type;
/*  56:104 */       return new GenericArrayTypeImpl(g.getGenericComponentType());
/*  57:    */     }
/*  58:106 */     if ((type instanceof WildcardType))
/*  59:    */     {
/*  60:107 */       WildcardType w = (WildcardType)type;
/*  61:108 */       return new WildcardTypeImpl(w.getUpperBounds(), w.getLowerBounds());
/*  62:    */     }
/*  63:112 */     return type;
/*  64:    */   }
/*  65:    */   
/*  66:    */   public static Class<?> getRawType(Type type)
/*  67:    */   {
/*  68:117 */     if ((type instanceof Class)) {
/*  69:119 */       return (Class)type;
/*  70:    */     }
/*  71:121 */     if ((type instanceof ParameterizedType))
/*  72:    */     {
/*  73:122 */       ParameterizedType parameterizedType = (ParameterizedType)type;
/*  74:    */       
/*  75:    */ 
/*  76:    */ 
/*  77:    */ 
/*  78:127 */       Type rawType = parameterizedType.getRawType();
/*  79:128 */       .Gson.Preconditions.checkArgument(rawType instanceof Class);
/*  80:129 */       return (Class)rawType;
/*  81:    */     }
/*  82:131 */     if ((type instanceof GenericArrayType))
/*  83:    */     {
/*  84:132 */       Type componentType = ((GenericArrayType)type).getGenericComponentType();
/*  85:133 */       return Array.newInstance(getRawType(componentType), 0).getClass();
/*  86:    */     }
/*  87:135 */     if ((type instanceof TypeVariable)) {
/*  88:138 */       return Object.class;
/*  89:    */     }
/*  90:140 */     if ((type instanceof WildcardType)) {
/*  91:141 */       return getRawType(((WildcardType)type).getUpperBounds()[0]);
/*  92:    */     }
/*  93:144 */     String className = type == null ? "null" : type.getClass().getName();
/*  94:145 */     throw new IllegalArgumentException("Expected a Class, ParameterizedType, or GenericArrayType, but <" + type + "> is of type " + className);
/*  95:    */   }
/*  96:    */   
/*  97:    */   static boolean equal(Object a, Object b)
/*  98:    */   {
/*  99:151 */     return (a == b) || ((a != null) && (a.equals(b)));
/* 100:    */   }
/* 101:    */   
/* 102:    */   public static boolean equals(Type a, Type b)
/* 103:    */   {
/* 104:158 */     if (a == b) {
/* 105:160 */       return true;
/* 106:    */     }
/* 107:162 */     if ((a instanceof Class)) {
/* 108:164 */       return a.equals(b);
/* 109:    */     }
/* 110:166 */     if ((a instanceof ParameterizedType))
/* 111:    */     {
/* 112:167 */       if (!(b instanceof ParameterizedType)) {
/* 113:168 */         return false;
/* 114:    */       }
/* 115:172 */       ParameterizedType pa = (ParameterizedType)a;
/* 116:173 */       ParameterizedType pb = (ParameterizedType)b;
/* 117:174 */       return (equal(pa.getOwnerType(), pb.getOwnerType())) && (pa.getRawType().equals(pb.getRawType())) && (Arrays.equals(pa.getActualTypeArguments(), pb.getActualTypeArguments()));
/* 118:    */     }
/* 119:178 */     if ((a instanceof GenericArrayType))
/* 120:    */     {
/* 121:179 */       if (!(b instanceof GenericArrayType)) {
/* 122:180 */         return false;
/* 123:    */       }
/* 124:183 */       GenericArrayType ga = (GenericArrayType)a;
/* 125:184 */       GenericArrayType gb = (GenericArrayType)b;
/* 126:185 */       return equals(ga.getGenericComponentType(), gb.getGenericComponentType());
/* 127:    */     }
/* 128:187 */     if ((a instanceof WildcardType))
/* 129:    */     {
/* 130:188 */       if (!(b instanceof WildcardType)) {
/* 131:189 */         return false;
/* 132:    */       }
/* 133:192 */       WildcardType wa = (WildcardType)a;
/* 134:193 */       WildcardType wb = (WildcardType)b;
/* 135:194 */       return (Arrays.equals(wa.getUpperBounds(), wb.getUpperBounds())) && (Arrays.equals(wa.getLowerBounds(), wb.getLowerBounds()));
/* 136:    */     }
/* 137:197 */     if ((a instanceof TypeVariable))
/* 138:    */     {
/* 139:198 */       if (!(b instanceof TypeVariable)) {
/* 140:199 */         return false;
/* 141:    */       }
/* 142:201 */       TypeVariable<?> va = (TypeVariable)a;
/* 143:202 */       TypeVariable<?> vb = (TypeVariable)b;
/* 144:203 */       return (va.getGenericDeclaration() == vb.getGenericDeclaration()) && (va.getName().equals(vb.getName()));
/* 145:    */     }
/* 146:208 */     return false;
/* 147:    */   }
/* 148:    */   
/* 149:    */   private static int hashCodeOrZero(Object o)
/* 150:    */   {
/* 151:213 */     return o != null ? o.hashCode() : 0;
/* 152:    */   }
/* 153:    */   
/* 154:    */   public static String typeToString(Type type)
/* 155:    */   {
/* 156:217 */     return (type instanceof Class) ? ((Class)type).getName() : type.toString();
/* 157:    */   }
/* 158:    */   
/* 159:    */   static Type getGenericSupertype(Type context, Class<?> rawType, Class<?> toResolve)
/* 160:    */   {
/* 161:226 */     if (toResolve == rawType) {
/* 162:227 */       return context;
/* 163:    */     }
/* 164:231 */     if (toResolve.isInterface())
/* 165:    */     {
/* 166:232 */       Class<?>[] interfaces = rawType.getInterfaces();
/* 167:233 */       int i = 0;
/* 168:233 */       for (int length = interfaces.length; i < length; i++)
/* 169:    */       {
/* 170:234 */         if (interfaces[i] == toResolve) {
/* 171:235 */           return rawType.getGenericInterfaces()[i];
/* 172:    */         }
/* 173:236 */         if (toResolve.isAssignableFrom(interfaces[i])) {
/* 174:237 */           return getGenericSupertype(rawType.getGenericInterfaces()[i], interfaces[i], toResolve);
/* 175:    */         }
/* 176:    */       }
/* 177:    */     }
/* 178:243 */     if (!rawType.isInterface()) {
/* 179:244 */       while (rawType != Object.class)
/* 180:    */       {
/* 181:245 */         Class<?> rawSupertype = rawType.getSuperclass();
/* 182:246 */         if (rawSupertype == toResolve) {
/* 183:247 */           return rawType.getGenericSuperclass();
/* 184:    */         }
/* 185:248 */         if (toResolve.isAssignableFrom(rawSupertype)) {
/* 186:249 */           return getGenericSupertype(rawType.getGenericSuperclass(), rawSupertype, toResolve);
/* 187:    */         }
/* 188:251 */         rawType = rawSupertype;
/* 189:    */       }
/* 190:    */     }
/* 191:256 */     return toResolve;
/* 192:    */   }
/* 193:    */   
/* 194:    */   static Type getSupertype(Type context, Class<?> contextRawType, Class<?> supertype)
/* 195:    */   {
/* 196:267 */     .Gson.Preconditions.checkArgument(supertype.isAssignableFrom(contextRawType));
/* 197:268 */     return resolve(context, contextRawType, getGenericSupertype(context, contextRawType, supertype));
/* 198:    */   }
/* 199:    */   
/* 200:    */   public static Type getArrayComponentType(Type array)
/* 201:    */   {
/* 202:277 */     return (array instanceof GenericArrayType) ? ((GenericArrayType)array).getGenericComponentType() : ((Class)array).getComponentType();
/* 203:    */   }
/* 204:    */   
/* 205:    */   public static Type getCollectionElementType(Type context, Class<?> contextRawType)
/* 206:    */   {
/* 207:287 */     Type collectionType = getSupertype(context, contextRawType, Collection.class);
/* 208:289 */     if ((collectionType instanceof WildcardType)) {
/* 209:290 */       collectionType = ((WildcardType)collectionType).getUpperBounds()[0];
/* 210:    */     }
/* 211:292 */     if ((collectionType instanceof ParameterizedType)) {
/* 212:293 */       return ((ParameterizedType)collectionType).getActualTypeArguments()[0];
/* 213:    */     }
/* 214:295 */     return Object.class;
/* 215:    */   }
/* 216:    */   
/* 217:    */   public static Type[] getMapKeyAndValueTypes(Type context, Class<?> contextRawType)
/* 218:    */   {
/* 219:308 */     if (context == Properties.class) {
/* 220:309 */       return new Type[] { String.class, String.class };
/* 221:    */     }
/* 222:312 */     Type mapType = getSupertype(context, contextRawType, Map.class);
/* 223:314 */     if ((mapType instanceof ParameterizedType))
/* 224:    */     {
/* 225:315 */       ParameterizedType mapParameterizedType = (ParameterizedType)mapType;
/* 226:316 */       return mapParameterizedType.getActualTypeArguments();
/* 227:    */     }
/* 228:318 */     return new Type[] { Object.class, Object.class };
/* 229:    */   }
/* 230:    */   
/* 231:    */   public static Type resolve(Type context, Class<?> contextRawType, Type toResolve)
/* 232:    */   {
/* 233:324 */     while ((toResolve instanceof TypeVariable))
/* 234:    */     {
/* 235:325 */       TypeVariable<?> typeVariable = (TypeVariable)toResolve;
/* 236:326 */       toResolve = resolveTypeVariable(context, contextRawType, typeVariable);
/* 237:327 */       if (toResolve == typeVariable) {
/* 238:328 */         return toResolve;
/* 239:    */       }
/* 240:    */     }
/* 241:331 */     if (((toResolve instanceof Class)) && (((Class)toResolve).isArray()))
/* 242:    */     {
/* 243:332 */       Class<?> original = (Class)toResolve;
/* 244:333 */       Type componentType = original.getComponentType();
/* 245:334 */       Type newComponentType = resolve(context, contextRawType, componentType);
/* 246:335 */       return componentType == newComponentType ? original : arrayOf(newComponentType);
/* 247:    */     }
/* 248:339 */     if ((toResolve instanceof GenericArrayType))
/* 249:    */     {
/* 250:340 */       GenericArrayType original = (GenericArrayType)toResolve;
/* 251:341 */       Type componentType = original.getGenericComponentType();
/* 252:342 */       Type newComponentType = resolve(context, contextRawType, componentType);
/* 253:343 */       return componentType == newComponentType ? original : arrayOf(newComponentType);
/* 254:    */     }
/* 255:347 */     if ((toResolve instanceof ParameterizedType))
/* 256:    */     {
/* 257:348 */       ParameterizedType original = (ParameterizedType)toResolve;
/* 258:349 */       Type ownerType = original.getOwnerType();
/* 259:350 */       Type newOwnerType = resolve(context, contextRawType, ownerType);
/* 260:351 */       boolean changed = newOwnerType != ownerType;
/* 261:    */       
/* 262:353 */       Type[] args = original.getActualTypeArguments();
/* 263:354 */       int t = 0;
/* 264:354 */       for (int length = args.length; t < length; t++)
/* 265:    */       {
/* 266:355 */         Type resolvedTypeArgument = resolve(context, contextRawType, args[t]);
/* 267:356 */         if (resolvedTypeArgument != args[t])
/* 268:    */         {
/* 269:357 */           if (!changed)
/* 270:    */           {
/* 271:358 */             args = (Type[])args.clone();
/* 272:359 */             changed = true;
/* 273:    */           }
/* 274:361 */           args[t] = resolvedTypeArgument;
/* 275:    */         }
/* 276:    */       }
/* 277:365 */       return changed ? newParameterizedTypeWithOwner(newOwnerType, original.getRawType(), args) : original;
/* 278:    */     }
/* 279:369 */     if ((toResolve instanceof WildcardType))
/* 280:    */     {
/* 281:370 */       WildcardType original = (WildcardType)toResolve;
/* 282:371 */       Type[] originalLowerBound = original.getLowerBounds();
/* 283:372 */       Type[] originalUpperBound = original.getUpperBounds();
/* 284:374 */       if (originalLowerBound.length == 1)
/* 285:    */       {
/* 286:375 */         Type lowerBound = resolve(context, contextRawType, originalLowerBound[0]);
/* 287:376 */         if (lowerBound != originalLowerBound[0]) {
/* 288:377 */           return supertypeOf(lowerBound);
/* 289:    */         }
/* 290:    */       }
/* 291:379 */       else if (originalUpperBound.length == 1)
/* 292:    */       {
/* 293:380 */         Type upperBound = resolve(context, contextRawType, originalUpperBound[0]);
/* 294:381 */         if (upperBound != originalUpperBound[0]) {
/* 295:382 */           return subtypeOf(upperBound);
/* 296:    */         }
/* 297:    */       }
/* 298:385 */       return original;
/* 299:    */     }
/* 300:388 */     return toResolve;
/* 301:    */   }
/* 302:    */   
/* 303:    */   static Type resolveTypeVariable(Type context, Class<?> contextRawType, TypeVariable<?> unknown)
/* 304:    */   {
/* 305:394 */     Class<?> declaredByRaw = declaringClassOf(unknown);
/* 306:397 */     if (declaredByRaw == null) {
/* 307:398 */       return unknown;
/* 308:    */     }
/* 309:401 */     Type declaredBy = getGenericSupertype(context, contextRawType, declaredByRaw);
/* 310:402 */     if ((declaredBy instanceof ParameterizedType))
/* 311:    */     {
/* 312:403 */       int index = indexOf(declaredByRaw.getTypeParameters(), unknown);
/* 313:404 */       return ((ParameterizedType)declaredBy).getActualTypeArguments()[index];
/* 314:    */     }
/* 315:407 */     return unknown;
/* 316:    */   }
/* 317:    */   
/* 318:    */   private static int indexOf(Object[] array, Object toFind)
/* 319:    */   {
/* 320:411 */     for (int i = 0; i < array.length; i++) {
/* 321:412 */       if (toFind.equals(array[i])) {
/* 322:413 */         return i;
/* 323:    */       }
/* 324:    */     }
/* 325:416 */     throw new NoSuchElementException();
/* 326:    */   }
/* 327:    */   
/* 328:    */   private static Class<?> declaringClassOf(TypeVariable<?> typeVariable)
/* 329:    */   {
/* 330:424 */     GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration();
/* 331:425 */     return (genericDeclaration instanceof Class) ? (Class)genericDeclaration : null;
/* 332:    */   }
/* 333:    */   
/* 334:    */   private static void checkNotPrimitive(Type type)
/* 335:    */   {
/* 336:431 */     .Gson.Preconditions.checkArgument((!(type instanceof Class)) || (!((Class)type).isPrimitive()));
/* 337:    */   }
/* 338:    */   
/* 339:    */   private static final class ParameterizedTypeImpl
/* 340:    */     implements ParameterizedType, Serializable
/* 341:    */   {
/* 342:    */     private final Type ownerType;
/* 343:    */     private final Type rawType;
/* 344:    */     private final Type[] typeArguments;
/* 345:    */     private static final long serialVersionUID = 0L;
/* 346:    */     
/* 347:    */     public ParameterizedTypeImpl(Type ownerType, Type rawType, Type... typeArguments)
/* 348:    */     {
/* 349:441 */       if ((rawType instanceof Class))
/* 350:    */       {
/* 351:442 */         Class<?> rawTypeAsClass = (Class)rawType;
/* 352:443 */         .Gson.Preconditions.checkArgument((ownerType != null) || (rawTypeAsClass.getEnclosingClass() == null));
/* 353:444 */         .Gson.Preconditions.checkArgument((ownerType == null) || (rawTypeAsClass.getEnclosingClass() != null));
/* 354:    */       }
/* 355:447 */       this.ownerType = (ownerType == null ? null : .Gson.Types.canonicalize(ownerType));
/* 356:448 */       this.rawType = .Gson.Types.canonicalize(rawType);
/* 357:449 */       this.typeArguments = ((Type[])typeArguments.clone());
/* 358:450 */       for (int t = 0; t < this.typeArguments.length; t++)
/* 359:    */       {
/* 360:451 */         .Gson.Preconditions.checkNotNull(this.typeArguments[t]);
/* 361:452 */         .Gson.Types.checkNotPrimitive(this.typeArguments[t]);
/* 362:453 */         this.typeArguments[t] = .Gson.Types.canonicalize(this.typeArguments[t]);
/* 363:    */       }
/* 364:    */     }
/* 365:    */     
/* 366:    */     public Type[] getActualTypeArguments()
/* 367:    */     {
/* 368:458 */       return (Type[])this.typeArguments.clone();
/* 369:    */     }
/* 370:    */     
/* 371:    */     public Type getRawType()
/* 372:    */     {
/* 373:462 */       return this.rawType;
/* 374:    */     }
/* 375:    */     
/* 376:    */     public Type getOwnerType()
/* 377:    */     {
/* 378:466 */       return this.ownerType;
/* 379:    */     }
/* 380:    */     
/* 381:    */     public boolean equals(Object other)
/* 382:    */     {
/* 383:470 */       return ((other instanceof ParameterizedType)) && (.Gson.Types.equals(this, (ParameterizedType)other));
/* 384:    */     }
/* 385:    */     
/* 386:    */     public int hashCode()
/* 387:    */     {
/* 388:475 */       return Arrays.hashCode(this.typeArguments) ^ this.rawType.hashCode() ^ .Gson.Types.hashCodeOrZero(this.ownerType);
/* 389:    */     }
/* 390:    */     
/* 391:    */     public String toString()
/* 392:    */     {
/* 393:481 */       StringBuilder stringBuilder = new StringBuilder(30 * (this.typeArguments.length + 1));
/* 394:482 */       stringBuilder.append(.Gson.Types.typeToString(this.rawType));
/* 395:484 */       if (this.typeArguments.length == 0) {
/* 396:485 */         return stringBuilder.toString();
/* 397:    */       }
/* 398:488 */       stringBuilder.append("<").append(.Gson.Types.typeToString(this.typeArguments[0]));
/* 399:489 */       for (int i = 1; i < this.typeArguments.length; i++) {
/* 400:490 */         stringBuilder.append(", ").append(.Gson.Types.typeToString(this.typeArguments[i]));
/* 401:    */       }
/* 402:492 */       return ">";
/* 403:    */     }
/* 404:    */   }
/* 405:    */   
/* 406:    */   private static final class GenericArrayTypeImpl
/* 407:    */     implements GenericArrayType, Serializable
/* 408:    */   {
/* 409:    */     private final Type componentType;
/* 410:    */     private static final long serialVersionUID = 0L;
/* 411:    */     
/* 412:    */     public GenericArrayTypeImpl(Type componentType)
/* 413:    */     {
/* 414:502 */       this.componentType = .Gson.Types.canonicalize(componentType);
/* 415:    */     }
/* 416:    */     
/* 417:    */     public Type getGenericComponentType()
/* 418:    */     {
/* 419:506 */       return this.componentType;
/* 420:    */     }
/* 421:    */     
/* 422:    */     public boolean equals(Object o)
/* 423:    */     {
/* 424:510 */       return ((o instanceof GenericArrayType)) && (.Gson.Types.equals(this, (GenericArrayType)o));
/* 425:    */     }
/* 426:    */     
/* 427:    */     public int hashCode()
/* 428:    */     {
/* 429:515 */       return this.componentType.hashCode();
/* 430:    */     }
/* 431:    */     
/* 432:    */     public String toString()
/* 433:    */     {
/* 434:519 */       return .Gson.Types.typeToString(this.componentType) + "[]";
/* 435:    */     }
/* 436:    */   }
/* 437:    */   
/* 438:    */   private static final class WildcardTypeImpl
/* 439:    */     implements WildcardType, Serializable
/* 440:    */   {
/* 441:    */     private final Type upperBound;
/* 442:    */     private final Type lowerBound;
/* 443:    */     private static final long serialVersionUID = 0L;
/* 444:    */     
/* 445:    */     public WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds)
/* 446:    */     {
/* 447:535 */       .Gson.Preconditions.checkArgument(lowerBounds.length <= 1);
/* 448:536 */       .Gson.Preconditions.checkArgument(upperBounds.length == 1);
/* 449:538 */       if (lowerBounds.length == 1)
/* 450:    */       {
/* 451:539 */         .Gson.Preconditions.checkNotNull(lowerBounds[0]);
/* 452:540 */         .Gson.Types.checkNotPrimitive(lowerBounds[0]);
/* 453:541 */         .Gson.Preconditions.checkArgument(upperBounds[0] == Object.class);
/* 454:542 */         this.lowerBound = .Gson.Types.canonicalize(lowerBounds[0]);
/* 455:543 */         this.upperBound = Object.class;
/* 456:    */       }
/* 457:    */       else
/* 458:    */       {
/* 459:546 */         .Gson.Preconditions.checkNotNull(upperBounds[0]);
/* 460:547 */         .Gson.Types.checkNotPrimitive(upperBounds[0]);
/* 461:548 */         this.lowerBound = null;
/* 462:549 */         this.upperBound = .Gson.Types.canonicalize(upperBounds[0]);
/* 463:    */       }
/* 464:    */     }
/* 465:    */     
/* 466:    */     public Type[] getUpperBounds()
/* 467:    */     {
/* 468:554 */       return new Type[] { this.upperBound };
/* 469:    */     }
/* 470:    */     
/* 471:    */     public Type[] getLowerBounds()
/* 472:    */     {
/* 473:558 */       return this.lowerBound != null ? new Type[] { this.lowerBound } : .Gson.Types.EMPTY_TYPE_ARRAY;
/* 474:    */     }
/* 475:    */     
/* 476:    */     public boolean equals(Object other)
/* 477:    */     {
/* 478:562 */       return ((other instanceof WildcardType)) && (.Gson.Types.equals(this, (WildcardType)other));
/* 479:    */     }
/* 480:    */     
/* 481:    */     public int hashCode()
/* 482:    */     {
/* 483:568 */       return (this.lowerBound != null ? 31 + this.lowerBound.hashCode() : 1) ^ 31 + this.upperBound.hashCode();
/* 484:    */     }
/* 485:    */     
/* 486:    */     public String toString()
/* 487:    */     {
/* 488:573 */       if (this.lowerBound != null) {
/* 489:574 */         return "? super " + .Gson.Types.typeToString(this.lowerBound);
/* 490:    */       }
/* 491:575 */       if (this.upperBound == Object.class) {
/* 492:576 */         return "?";
/* 493:    */       }
/* 494:578 */       return "? extends " + .Gson.Types.typeToString(this.upperBound);
/* 495:    */     }
/* 496:    */   }
/* 497:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.internal..Gson.Types
 * JD-Core Version:    0.7.0.1
 */