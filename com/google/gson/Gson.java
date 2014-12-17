/*   1:    */ package com.google.gson;
/*   2:    */ 
/*   3:    */ import com.google.gson.internal.ConstructorConstructor;
/*   4:    */ import com.google.gson.internal.Excluder;
/*   5:    */ import com.google.gson.internal.Primitives;
/*   6:    */ import com.google.gson.internal.Streams;
/*   7:    */ import com.google.gson.internal.bind.ArrayTypeAdapter;
/*   8:    */ import com.google.gson.internal.bind.CollectionTypeAdapterFactory;
/*   9:    */ import com.google.gson.internal.bind.DateTypeAdapter;
/*  10:    */ import com.google.gson.internal.bind.JsonTreeReader;
/*  11:    */ import com.google.gson.internal.bind.JsonTreeWriter;
/*  12:    */ import com.google.gson.internal.bind.MapTypeAdapterFactory;
/*  13:    */ import com.google.gson.internal.bind.ObjectTypeAdapter;
/*  14:    */ import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;
/*  15:    */ import com.google.gson.internal.bind.SqlDateTypeAdapter;
/*  16:    */ import com.google.gson.internal.bind.TimeTypeAdapter;
/*  17:    */ import com.google.gson.internal.bind.TypeAdapters;
/*  18:    */ import com.google.gson.reflect.TypeToken;
/*  19:    */ import com.google.gson.stream.JsonReader;
/*  20:    */ import com.google.gson.stream.JsonToken;
/*  21:    */ import com.google.gson.stream.JsonWriter;
/*  22:    */ import com.google.gson.stream.MalformedJsonException;
/*  23:    */ import java.io.EOFException;
/*  24:    */ import java.io.IOException;
/*  25:    */ import java.io.Reader;
/*  26:    */ import java.io.StringReader;
/*  27:    */ import java.io.StringWriter;
/*  28:    */ import java.io.Writer;
/*  29:    */ import java.lang.reflect.Type;
/*  30:    */ import java.math.BigDecimal;
/*  31:    */ import java.math.BigInteger;
/*  32:    */ import java.util.ArrayList;
/*  33:    */ import java.util.Collections;
/*  34:    */ import java.util.HashMap;
/*  35:    */ import java.util.List;
/*  36:    */ import java.util.Map;
/*  37:    */ 
/*  38:    */ public final class Gson
/*  39:    */ {
/*  40:    */   static final boolean DEFAULT_JSON_NON_EXECUTABLE = false;
/*  41:    */   private static final String JSON_NON_EXECUTABLE_PREFIX = ")]}'\n";
/*  42:109 */   private final ThreadLocal<Map<TypeToken<?>, FutureTypeAdapter<?>>> calls = new ThreadLocal();
/*  43:112 */   private final Map<TypeToken<?>, TypeAdapter<?>> typeTokenCache = Collections.synchronizedMap(new HashMap());
/*  44:    */   private final List<TypeAdapterFactory> factories;
/*  45:    */   private final ConstructorConstructor constructorConstructor;
/*  46:    */   private final boolean serializeNulls;
/*  47:    */   private final boolean htmlSafe;
/*  48:    */   private final boolean generateNonExecutableJson;
/*  49:    */   private final boolean prettyPrinting;
/*  50:123 */   final JsonDeserializationContext deserializationContext = new JsonDeserializationContext()
/*  51:    */   {
/*  52:    */     public <T> T deserialize(JsonElement json, Type typeOfT)
/*  53:    */       throws JsonParseException
/*  54:    */     {
/*  55:126 */       return Gson.this.fromJson(json, typeOfT);
/*  56:    */     }
/*  57:    */   };
/*  58:130 */   final JsonSerializationContext serializationContext = new JsonSerializationContext()
/*  59:    */   {
/*  60:    */     public JsonElement serialize(Object src)
/*  61:    */     {
/*  62:132 */       return Gson.this.toJsonTree(src);
/*  63:    */     }
/*  64:    */     
/*  65:    */     public JsonElement serialize(Object src, Type typeOfSrc)
/*  66:    */     {
/*  67:135 */       return Gson.this.toJsonTree(src, typeOfSrc);
/*  68:    */     }
/*  69:    */   };
/*  70:    */   
/*  71:    */   public Gson()
/*  72:    */   {
/*  73:174 */     this(Excluder.DEFAULT, FieldNamingPolicy.IDENTITY, Collections.emptyMap(), false, false, false, true, false, false, LongSerializationPolicy.DEFAULT, Collections.emptyList());
/*  74:    */   }
/*  75:    */   
/*  76:    */   Gson(Excluder excluder, FieldNamingStrategy fieldNamingPolicy, Map<Type, InstanceCreator<?>> instanceCreators, boolean serializeNulls, boolean complexMapKeySerialization, boolean generateNonExecutableGson, boolean htmlSafe, boolean prettyPrinting, boolean serializeSpecialFloatingPointValues, LongSerializationPolicy longSerializationPolicy, List<TypeAdapterFactory> typeAdapterFactories)
/*  77:    */   {
/*  78:186 */     this.constructorConstructor = new ConstructorConstructor(instanceCreators);
/*  79:187 */     this.serializeNulls = serializeNulls;
/*  80:188 */     this.generateNonExecutableJson = generateNonExecutableGson;
/*  81:189 */     this.htmlSafe = htmlSafe;
/*  82:190 */     this.prettyPrinting = prettyPrinting;
/*  83:    */     
/*  84:192 */     List<TypeAdapterFactory> factories = new ArrayList();
/*  85:    */     
/*  86:    */ 
/*  87:195 */     factories.add(TypeAdapters.JSON_ELEMENT_FACTORY);
/*  88:196 */     factories.add(ObjectTypeAdapter.FACTORY);
/*  89:    */     
/*  90:    */ 
/*  91:199 */     factories.add(excluder);
/*  92:    */     
/*  93:    */ 
/*  94:202 */     factories.addAll(typeAdapterFactories);
/*  95:    */     
/*  96:    */ 
/*  97:205 */     factories.add(TypeAdapters.STRING_FACTORY);
/*  98:206 */     factories.add(TypeAdapters.INTEGER_FACTORY);
/*  99:207 */     factories.add(TypeAdapters.BOOLEAN_FACTORY);
/* 100:208 */     factories.add(TypeAdapters.BYTE_FACTORY);
/* 101:209 */     factories.add(TypeAdapters.SHORT_FACTORY);
/* 102:210 */     factories.add(TypeAdapters.newFactory(Long.TYPE, Long.class, longAdapter(longSerializationPolicy)));
/* 103:    */     
/* 104:212 */     factories.add(TypeAdapters.newFactory(Double.TYPE, Double.class, doubleAdapter(serializeSpecialFloatingPointValues)));
/* 105:    */     
/* 106:214 */     factories.add(TypeAdapters.newFactory(Float.TYPE, Float.class, floatAdapter(serializeSpecialFloatingPointValues)));
/* 107:    */     
/* 108:216 */     factories.add(TypeAdapters.NUMBER_FACTORY);
/* 109:217 */     factories.add(TypeAdapters.CHARACTER_FACTORY);
/* 110:218 */     factories.add(TypeAdapters.STRING_BUILDER_FACTORY);
/* 111:219 */     factories.add(TypeAdapters.STRING_BUFFER_FACTORY);
/* 112:220 */     factories.add(TypeAdapters.newFactory(BigDecimal.class, TypeAdapters.BIG_DECIMAL));
/* 113:221 */     factories.add(TypeAdapters.newFactory(BigInteger.class, TypeAdapters.BIG_INTEGER));
/* 114:222 */     factories.add(TypeAdapters.URL_FACTORY);
/* 115:223 */     factories.add(TypeAdapters.URI_FACTORY);
/* 116:224 */     factories.add(TypeAdapters.UUID_FACTORY);
/* 117:225 */     factories.add(TypeAdapters.LOCALE_FACTORY);
/* 118:226 */     factories.add(TypeAdapters.INET_ADDRESS_FACTORY);
/* 119:227 */     factories.add(TypeAdapters.BIT_SET_FACTORY);
/* 120:228 */     factories.add(DateTypeAdapter.FACTORY);
/* 121:229 */     factories.add(TypeAdapters.CALENDAR_FACTORY);
/* 122:230 */     factories.add(TimeTypeAdapter.FACTORY);
/* 123:231 */     factories.add(SqlDateTypeAdapter.FACTORY);
/* 124:232 */     factories.add(TypeAdapters.TIMESTAMP_FACTORY);
/* 125:233 */     factories.add(ArrayTypeAdapter.FACTORY);
/* 126:234 */     factories.add(TypeAdapters.ENUM_FACTORY);
/* 127:235 */     factories.add(TypeAdapters.CLASS_FACTORY);
/* 128:    */     
/* 129:    */ 
/* 130:238 */     factories.add(new CollectionTypeAdapterFactory(this.constructorConstructor));
/* 131:239 */     factories.add(new MapTypeAdapterFactory(this.constructorConstructor, complexMapKeySerialization));
/* 132:240 */     factories.add(new ReflectiveTypeAdapterFactory(this.constructorConstructor, fieldNamingPolicy, excluder));
/* 133:    */     
/* 134:    */ 
/* 135:243 */     this.factories = Collections.unmodifiableList(factories);
/* 136:    */   }
/* 137:    */   
/* 138:    */   private TypeAdapter<Number> doubleAdapter(boolean serializeSpecialFloatingPointValues)
/* 139:    */   {
/* 140:247 */     if (serializeSpecialFloatingPointValues) {
/* 141:248 */       return TypeAdapters.DOUBLE;
/* 142:    */     }
/* 143:250 */     new TypeAdapter()
/* 144:    */     {
/* 145:    */       public Double read(JsonReader in)
/* 146:    */         throws IOException
/* 147:    */       {
/* 148:252 */         if (in.peek() == JsonToken.NULL)
/* 149:    */         {
/* 150:253 */           in.nextNull();
/* 151:254 */           return null;
/* 152:    */         }
/* 153:256 */         return Double.valueOf(in.nextDouble());
/* 154:    */       }
/* 155:    */       
/* 156:    */       public void write(JsonWriter out, Number value)
/* 157:    */         throws IOException
/* 158:    */       {
/* 159:259 */         if (value == null)
/* 160:    */         {
/* 161:260 */           out.nullValue();
/* 162:261 */           return;
/* 163:    */         }
/* 164:263 */         double doubleValue = value.doubleValue();
/* 165:264 */         Gson.this.checkValidFloatingPoint(doubleValue);
/* 166:265 */         out.value(value);
/* 167:    */       }
/* 168:    */     };
/* 169:    */   }
/* 170:    */   
/* 171:    */   private TypeAdapter<Number> floatAdapter(boolean serializeSpecialFloatingPointValues)
/* 172:    */   {
/* 173:271 */     if (serializeSpecialFloatingPointValues) {
/* 174:272 */       return TypeAdapters.FLOAT;
/* 175:    */     }
/* 176:274 */     new TypeAdapter()
/* 177:    */     {
/* 178:    */       public Float read(JsonReader in)
/* 179:    */         throws IOException
/* 180:    */       {
/* 181:276 */         if (in.peek() == JsonToken.NULL)
/* 182:    */         {
/* 183:277 */           in.nextNull();
/* 184:278 */           return null;
/* 185:    */         }
/* 186:280 */         return Float.valueOf((float)in.nextDouble());
/* 187:    */       }
/* 188:    */       
/* 189:    */       public void write(JsonWriter out, Number value)
/* 190:    */         throws IOException
/* 191:    */       {
/* 192:283 */         if (value == null)
/* 193:    */         {
/* 194:284 */           out.nullValue();
/* 195:285 */           return;
/* 196:    */         }
/* 197:287 */         float floatValue = value.floatValue();
/* 198:288 */         Gson.this.checkValidFloatingPoint(floatValue);
/* 199:289 */         out.value(value);
/* 200:    */       }
/* 201:    */     };
/* 202:    */   }
/* 203:    */   
/* 204:    */   private void checkValidFloatingPoint(double value)
/* 205:    */   {
/* 206:295 */     if ((Double.isNaN(value)) || (Double.isInfinite(value))) {
/* 207:296 */       throw new IllegalArgumentException(value + " is not a valid double value as per JSON specification. To override this" + " behavior, use GsonBuilder.serializeSpecialFloatingPointValues() method.");
/* 208:    */     }
/* 209:    */   }
/* 210:    */   
/* 211:    */   private TypeAdapter<Number> longAdapter(LongSerializationPolicy longSerializationPolicy)
/* 212:    */   {
/* 213:303 */     if (longSerializationPolicy == LongSerializationPolicy.DEFAULT) {
/* 214:304 */       return TypeAdapters.LONG;
/* 215:    */     }
/* 216:306 */     new TypeAdapter()
/* 217:    */     {
/* 218:    */       public Number read(JsonReader in)
/* 219:    */         throws IOException
/* 220:    */       {
/* 221:308 */         if (in.peek() == JsonToken.NULL)
/* 222:    */         {
/* 223:309 */           in.nextNull();
/* 224:310 */           return null;
/* 225:    */         }
/* 226:312 */         return Long.valueOf(in.nextLong());
/* 227:    */       }
/* 228:    */       
/* 229:    */       public void write(JsonWriter out, Number value)
/* 230:    */         throws IOException
/* 231:    */       {
/* 232:315 */         if (value == null)
/* 233:    */         {
/* 234:316 */           out.nullValue();
/* 235:317 */           return;
/* 236:    */         }
/* 237:319 */         out.value(value.toString());
/* 238:    */       }
/* 239:    */     };
/* 240:    */   }
/* 241:    */   
/* 242:    */   public <T> TypeAdapter<T> getAdapter(TypeToken<T> type)
/* 243:    */   {
/* 244:332 */     TypeAdapter<?> cached = (TypeAdapter)this.typeTokenCache.get(type);
/* 245:333 */     if (cached != null) {
/* 246:334 */       return cached;
/* 247:    */     }
/* 248:337 */     Map<TypeToken<?>, FutureTypeAdapter<?>> threadCalls = (Map)this.calls.get();
/* 249:338 */     boolean requiresThreadLocalCleanup = false;
/* 250:339 */     if (threadCalls == null)
/* 251:    */     {
/* 252:340 */       threadCalls = new HashMap();
/* 253:341 */       this.calls.set(threadCalls);
/* 254:342 */       requiresThreadLocalCleanup = true;
/* 255:    */     }
/* 256:346 */     FutureTypeAdapter<T> ongoingCall = (FutureTypeAdapter)threadCalls.get(type);
/* 257:347 */     if (ongoingCall != null) {
/* 258:348 */       return ongoingCall;
/* 259:    */     }
/* 260:    */     try
/* 261:    */     {
/* 262:352 */       FutureTypeAdapter<T> call = new FutureTypeAdapter();
/* 263:353 */       threadCalls.put(type, call);
/* 264:355 */       for (TypeAdapterFactory factory : this.factories)
/* 265:    */       {
/* 266:356 */         TypeAdapter<T> candidate = factory.create(this, type);
/* 267:357 */         if (candidate != null)
/* 268:    */         {
/* 269:358 */           call.setDelegate(candidate);
/* 270:359 */           this.typeTokenCache.put(type, candidate);
/* 271:360 */           return candidate;
/* 272:    */         }
/* 273:    */       }
/* 274:363 */       throw new IllegalArgumentException("GSON cannot handle " + type);
/* 275:    */     }
/* 276:    */     finally
/* 277:    */     {
/* 278:365 */       threadCalls.remove(type);
/* 279:367 */       if (requiresThreadLocalCleanup) {
/* 280:368 */         this.calls.remove();
/* 281:    */       }
/* 282:    */     }
/* 283:    */   }
/* 284:    */   
/* 285:    */   public <T> TypeAdapter<T> getDelegateAdapter(TypeAdapterFactory skipPast, TypeToken<T> type)
/* 286:    */   {
/* 287:420 */     boolean skipPastFound = false;
/* 288:422 */     for (TypeAdapterFactory factory : this.factories) {
/* 289:423 */       if (!skipPastFound)
/* 290:    */       {
/* 291:424 */         if (factory == skipPast) {
/* 292:425 */           skipPastFound = true;
/* 293:    */         }
/* 294:    */       }
/* 295:    */       else
/* 296:    */       {
/* 297:430 */         TypeAdapter<T> candidate = factory.create(this, type);
/* 298:431 */         if (candidate != null) {
/* 299:432 */           return candidate;
/* 300:    */         }
/* 301:    */       }
/* 302:    */     }
/* 303:435 */     throw new IllegalArgumentException("GSON cannot serialize " + type);
/* 304:    */   }
/* 305:    */   
/* 306:    */   public <T> TypeAdapter<T> getAdapter(Class<T> type)
/* 307:    */   {
/* 308:445 */     return getAdapter(TypeToken.get(type));
/* 309:    */   }
/* 310:    */   
/* 311:    */   public JsonElement toJsonTree(Object src)
/* 312:    */   {
/* 313:462 */     if (src == null) {
/* 314:463 */       return JsonNull.INSTANCE;
/* 315:    */     }
/* 316:465 */     return toJsonTree(src, src.getClass());
/* 317:    */   }
/* 318:    */   
/* 319:    */   public JsonElement toJsonTree(Object src, Type typeOfSrc)
/* 320:    */   {
/* 321:485 */     JsonTreeWriter writer = new JsonTreeWriter();
/* 322:486 */     toJson(src, typeOfSrc, writer);
/* 323:487 */     return writer.get();
/* 324:    */   }
/* 325:    */   
/* 326:    */   public String toJson(Object src)
/* 327:    */   {
/* 328:504 */     if (src == null) {
/* 329:505 */       return toJson(JsonNull.INSTANCE);
/* 330:    */     }
/* 331:507 */     return toJson(src, src.getClass());
/* 332:    */   }
/* 333:    */   
/* 334:    */   public String toJson(Object src, Type typeOfSrc)
/* 335:    */   {
/* 336:526 */     StringWriter writer = new StringWriter();
/* 337:527 */     toJson(src, typeOfSrc, writer);
/* 338:528 */     return writer.toString();
/* 339:    */   }
/* 340:    */   
/* 341:    */   public void toJson(Object src, Appendable writer)
/* 342:    */     throws JsonIOException
/* 343:    */   {
/* 344:546 */     if (src != null) {
/* 345:547 */       toJson(src, src.getClass(), writer);
/* 346:    */     } else {
/* 347:549 */       toJson(JsonNull.INSTANCE, writer);
/* 348:    */     }
/* 349:    */   }
/* 350:    */   
/* 351:    */   public void toJson(Object src, Type typeOfSrc, Appendable writer)
/* 352:    */     throws JsonIOException
/* 353:    */   {
/* 354:    */     try
/* 355:    */     {
/* 356:571 */       JsonWriter jsonWriter = newJsonWriter(Streams.writerForAppendable(writer));
/* 357:572 */       toJson(src, typeOfSrc, jsonWriter);
/* 358:    */     }
/* 359:    */     catch (IOException e)
/* 360:    */     {
/* 361:574 */       throw new JsonIOException(e);
/* 362:    */     }
/* 363:    */   }
/* 364:    */   
/* 365:    */   public void toJson(Object src, Type typeOfSrc, JsonWriter writer)
/* 366:    */     throws JsonIOException
/* 367:    */   {
/* 368:585 */     TypeAdapter<?> adapter = getAdapter(TypeToken.get(typeOfSrc));
/* 369:586 */     boolean oldLenient = writer.isLenient();
/* 370:587 */     writer.setLenient(true);
/* 371:588 */     boolean oldHtmlSafe = writer.isHtmlSafe();
/* 372:589 */     writer.setHtmlSafe(this.htmlSafe);
/* 373:590 */     boolean oldSerializeNulls = writer.getSerializeNulls();
/* 374:591 */     writer.setSerializeNulls(this.serializeNulls);
/* 375:    */     try
/* 376:    */     {
/* 377:593 */       adapter.write(writer, src);
/* 378:    */     }
/* 379:    */     catch (IOException e)
/* 380:    */     {
/* 381:595 */       throw new JsonIOException(e);
/* 382:    */     }
/* 383:    */     finally
/* 384:    */     {
/* 385:597 */       writer.setLenient(oldLenient);
/* 386:598 */       writer.setHtmlSafe(oldHtmlSafe);
/* 387:599 */       writer.setSerializeNulls(oldSerializeNulls);
/* 388:    */     }
/* 389:    */   }
/* 390:    */   
/* 391:    */   public String toJson(JsonElement jsonElement)
/* 392:    */   {
/* 393:611 */     StringWriter writer = new StringWriter();
/* 394:612 */     toJson(jsonElement, writer);
/* 395:613 */     return writer.toString();
/* 396:    */   }
/* 397:    */   
/* 398:    */   public void toJson(JsonElement jsonElement, Appendable writer)
/* 399:    */     throws JsonIOException
/* 400:    */   {
/* 401:    */     try
/* 402:    */     {
/* 403:626 */       JsonWriter jsonWriter = newJsonWriter(Streams.writerForAppendable(writer));
/* 404:627 */       toJson(jsonElement, jsonWriter);
/* 405:    */     }
/* 406:    */     catch (IOException e)
/* 407:    */     {
/* 408:629 */       throw new RuntimeException(e);
/* 409:    */     }
/* 410:    */   }
/* 411:    */   
/* 412:    */   private JsonWriter newJsonWriter(Writer writer)
/* 413:    */     throws IOException
/* 414:    */   {
/* 415:638 */     if (this.generateNonExecutableJson) {
/* 416:639 */       writer.write(")]}'\n");
/* 417:    */     }
/* 418:641 */     JsonWriter jsonWriter = new JsonWriter(writer);
/* 419:642 */     if (this.prettyPrinting) {
/* 420:643 */       jsonWriter.setIndent("  ");
/* 421:    */     }
/* 422:645 */     jsonWriter.setSerializeNulls(this.serializeNulls);
/* 423:646 */     return jsonWriter;
/* 424:    */   }
/* 425:    */   
/* 426:    */   public void toJson(JsonElement jsonElement, JsonWriter writer)
/* 427:    */     throws JsonIOException
/* 428:    */   {
/* 429:654 */     boolean oldLenient = writer.isLenient();
/* 430:655 */     writer.setLenient(true);
/* 431:656 */     boolean oldHtmlSafe = writer.isHtmlSafe();
/* 432:657 */     writer.setHtmlSafe(this.htmlSafe);
/* 433:658 */     boolean oldSerializeNulls = writer.getSerializeNulls();
/* 434:659 */     writer.setSerializeNulls(this.serializeNulls);
/* 435:    */     try
/* 436:    */     {
/* 437:661 */       Streams.write(jsonElement, writer);
/* 438:    */     }
/* 439:    */     catch (IOException e)
/* 440:    */     {
/* 441:663 */       throw new JsonIOException(e);
/* 442:    */     }
/* 443:    */     finally
/* 444:    */     {
/* 445:665 */       writer.setLenient(oldLenient);
/* 446:666 */       writer.setHtmlSafe(oldHtmlSafe);
/* 447:667 */       writer.setSerializeNulls(oldSerializeNulls);
/* 448:    */     }
/* 449:    */   }
/* 450:    */   
/* 451:    */   public <T> T fromJson(String json, Class<T> classOfT)
/* 452:    */     throws JsonSyntaxException
/* 453:    */   {
/* 454:689 */     Object object = fromJson(json, classOfT);
/* 455:690 */     return Primitives.wrap(classOfT).cast(object);
/* 456:    */   }
/* 457:    */   
/* 458:    */   public <T> T fromJson(String json, Type typeOfT)
/* 459:    */     throws JsonSyntaxException
/* 460:    */   {
/* 461:713 */     if (json == null) {
/* 462:714 */       return null;
/* 463:    */     }
/* 464:716 */     StringReader reader = new StringReader(json);
/* 465:717 */     T target = fromJson(reader, typeOfT);
/* 466:718 */     return target;
/* 467:    */   }
/* 468:    */   
/* 469:    */   public <T> T fromJson(Reader json, Class<T> classOfT)
/* 470:    */     throws JsonSyntaxException, JsonIOException
/* 471:    */   {
/* 472:740 */     JsonReader jsonReader = new JsonReader(json);
/* 473:741 */     Object object = fromJson(jsonReader, classOfT);
/* 474:742 */     assertFullConsumption(object, jsonReader);
/* 475:743 */     return Primitives.wrap(classOfT).cast(object);
/* 476:    */   }
/* 477:    */   
/* 478:    */   public <T> T fromJson(Reader json, Type typeOfT)
/* 479:    */     throws JsonIOException, JsonSyntaxException
/* 480:    */   {
/* 481:767 */     JsonReader jsonReader = new JsonReader(json);
/* 482:768 */     T object = fromJson(jsonReader, typeOfT);
/* 483:769 */     assertFullConsumption(object, jsonReader);
/* 484:770 */     return object;
/* 485:    */   }
/* 486:    */   
/* 487:    */   private static void assertFullConsumption(Object obj, JsonReader reader)
/* 488:    */   {
/* 489:    */     try
/* 490:    */     {
/* 491:775 */       if ((obj != null) && (reader.peek() != JsonToken.END_DOCUMENT)) {
/* 492:776 */         throw new JsonIOException("JSON document was not fully consumed.");
/* 493:    */       }
/* 494:    */     }
/* 495:    */     catch (MalformedJsonException e)
/* 496:    */     {
/* 497:779 */       throw new JsonSyntaxException(e);
/* 498:    */     }
/* 499:    */     catch (IOException e)
/* 500:    */     {
/* 501:781 */       throw new JsonIOException(e);
/* 502:    */     }
/* 503:    */   }
/* 504:    */   
/* 505:    */   public <T> T fromJson(JsonReader reader, Type typeOfT)
/* 506:    */     throws JsonIOException, JsonSyntaxException
/* 507:    */   {
/* 508:795 */     boolean isEmpty = true;
/* 509:796 */     boolean oldLenient = reader.isLenient();
/* 510:797 */     reader.setLenient(true);
/* 511:    */     try
/* 512:    */     {
/* 513:799 */       reader.peek();
/* 514:800 */       isEmpty = false;
/* 515:801 */       TypeToken<T> typeToken = TypeToken.get(typeOfT);
/* 516:802 */       typeAdapter = getAdapter(typeToken);
/* 517:803 */       T object = typeAdapter.read(reader);
/* 518:804 */       return object;
/* 519:    */     }
/* 520:    */     catch (EOFException e)
/* 521:    */     {
/* 522:    */       TypeAdapter<T> typeAdapter;
/* 523:810 */       if (isEmpty) {
/* 524:811 */         return null;
/* 525:    */       }
/* 526:813 */       throw new JsonSyntaxException(e);
/* 527:    */     }
/* 528:    */     catch (IllegalStateException e)
/* 529:    */     {
/* 530:815 */       throw new JsonSyntaxException(e);
/* 531:    */     }
/* 532:    */     catch (IOException e)
/* 533:    */     {
/* 534:818 */       throw new JsonSyntaxException(e);
/* 535:    */     }
/* 536:    */     finally
/* 537:    */     {
/* 538:820 */       reader.setLenient(oldLenient);
/* 539:    */     }
/* 540:    */   }
/* 541:    */   
/* 542:    */   public <T> T fromJson(JsonElement json, Class<T> classOfT)
/* 543:    */     throws JsonSyntaxException
/* 544:    */   {
/* 545:841 */     Object object = fromJson(json, classOfT);
/* 546:842 */     return Primitives.wrap(classOfT).cast(object);
/* 547:    */   }
/* 548:    */   
/* 549:    */   public <T> T fromJson(JsonElement json, Type typeOfT)
/* 550:    */     throws JsonSyntaxException
/* 551:    */   {
/* 552:865 */     if (json == null) {
/* 553:866 */       return null;
/* 554:    */     }
/* 555:868 */     return fromJson(new JsonTreeReader(json), typeOfT);
/* 556:    */   }
/* 557:    */   
/* 558:    */   static class FutureTypeAdapter<T>
/* 559:    */     extends TypeAdapter<T>
/* 560:    */   {
/* 561:    */     private TypeAdapter<T> delegate;
/* 562:    */     
/* 563:    */     public void setDelegate(TypeAdapter<T> typeAdapter)
/* 564:    */     {
/* 565:875 */       if (this.delegate != null) {
/* 566:876 */         throw new AssertionError();
/* 567:    */       }
/* 568:878 */       this.delegate = typeAdapter;
/* 569:    */     }
/* 570:    */     
/* 571:    */     public T read(JsonReader in)
/* 572:    */       throws IOException
/* 573:    */     {
/* 574:882 */       if (this.delegate == null) {
/* 575:883 */         throw new IllegalStateException();
/* 576:    */       }
/* 577:885 */       return this.delegate.read(in);
/* 578:    */     }
/* 579:    */     
/* 580:    */     public void write(JsonWriter out, T value)
/* 581:    */       throws IOException
/* 582:    */     {
/* 583:889 */       if (this.delegate == null) {
/* 584:890 */         throw new IllegalStateException();
/* 585:    */       }
/* 586:892 */       this.delegate.write(out, value);
/* 587:    */     }
/* 588:    */   }
/* 589:    */   
/* 590:    */   public String toString()
/* 591:    */   {
/* 592:898 */     return "{serializeNulls:" + this.serializeNulls + "factories:" + this.factories + ",instanceCreators:" + this.constructorConstructor + "}";
/* 593:    */   }
/* 594:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.Gson
 * JD-Core Version:    0.7.0.1
 */