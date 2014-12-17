/*   1:    */ package com.google.gson.internal.bind;
/*   2:    */ 
/*   3:    */ import com.google.gson.Gson;
/*   4:    */ import com.google.gson.JsonArray;
/*   5:    */ import com.google.gson.JsonElement;
/*   6:    */ import com.google.gson.JsonIOException;
/*   7:    */ import com.google.gson.JsonNull;
/*   8:    */ import com.google.gson.JsonObject;
/*   9:    */ import com.google.gson.JsonPrimitive;
/*  10:    */ import com.google.gson.JsonSyntaxException;
/*  11:    */ import com.google.gson.TypeAdapter;
/*  12:    */ import com.google.gson.TypeAdapterFactory;
/*  13:    */ import com.google.gson.annotations.SerializedName;
/*  14:    */ import com.google.gson.internal.LazilyParsedNumber;
/*  15:    */ import com.google.gson.reflect.TypeToken;
/*  16:    */ import com.google.gson.stream.JsonReader;
/*  17:    */ import com.google.gson.stream.JsonToken;
/*  18:    */ import com.google.gson.stream.JsonWriter;
/*  19:    */ import java.io.IOException;
/*  20:    */ import java.lang.reflect.Field;
/*  21:    */ import java.math.BigDecimal;
/*  22:    */ import java.math.BigInteger;
/*  23:    */ import java.net.InetAddress;
/*  24:    */ import java.net.URI;
/*  25:    */ import java.net.URISyntaxException;
/*  26:    */ import java.net.URL;
/*  27:    */ import java.sql.Timestamp;
/*  28:    */ import java.util.BitSet;
/*  29:    */ import java.util.Calendar;
/*  30:    */ import java.util.Date;
/*  31:    */ import java.util.GregorianCalendar;
/*  32:    */ import java.util.HashMap;
/*  33:    */ import java.util.Locale;
/*  34:    */ import java.util.Map;
/*  35:    */ import java.util.Map.Entry;
/*  36:    */ import java.util.StringTokenizer;
/*  37:    */ import java.util.UUID;
/*  38:    */ 
/*  39:    */ public final class TypeAdapters
/*  40:    */ {
/*  41: 61 */   public static final TypeAdapter<Class> CLASS = new TypeAdapter()
/*  42:    */   {
/*  43:    */     public void write(JsonWriter out, Class value)
/*  44:    */       throws IOException
/*  45:    */     {
/*  46: 64 */       if (value == null) {
/*  47: 65 */         out.nullValue();
/*  48:    */       } else {
/*  49: 67 */         throw new UnsupportedOperationException("Attempted to serialize java.lang.Class: " + value.getName() + ". Forgot to register a type adapter?");
/*  50:    */       }
/*  51:    */     }
/*  52:    */     
/*  53:    */     public Class read(JsonReader in)
/*  54:    */       throws IOException
/*  55:    */     {
/*  56: 73 */       if (in.peek() == JsonToken.NULL)
/*  57:    */       {
/*  58: 74 */         in.nextNull();
/*  59: 75 */         return null;
/*  60:    */       }
/*  61: 77 */       throw new UnsupportedOperationException("Attempted to deserialize a java.lang.Class. Forgot to register a type adapter?");
/*  62:    */     }
/*  63:    */   };
/*  64: 82 */   public static final TypeAdapterFactory CLASS_FACTORY = newFactory(Class.class, CLASS);
/*  65: 84 */   public static final TypeAdapter<BitSet> BIT_SET = new TypeAdapter()
/*  66:    */   {
/*  67:    */     public BitSet read(JsonReader in)
/*  68:    */       throws IOException
/*  69:    */     {
/*  70: 86 */       if (in.peek() == JsonToken.NULL)
/*  71:    */       {
/*  72: 87 */         in.nextNull();
/*  73: 88 */         return null;
/*  74:    */       }
/*  75: 91 */       BitSet bitset = new BitSet();
/*  76: 92 */       in.beginArray();
/*  77: 93 */       int i = 0;
/*  78: 94 */       JsonToken tokenType = in.peek();
/*  79: 95 */       while (tokenType != JsonToken.END_ARRAY)
/*  80:    */       {
/*  81:    */         boolean set;
/*  82: 97 */         switch (TypeAdapters.32.$SwitchMap$com$google$gson$stream$JsonToken[tokenType.ordinal()])
/*  83:    */         {
/*  84:    */         case 1: 
/*  85: 99 */           set = in.nextInt() != 0;
/*  86:100 */           break;
/*  87:    */         case 2: 
/*  88:102 */           set = in.nextBoolean();
/*  89:103 */           break;
/*  90:    */         case 3: 
/*  91:105 */           String stringValue = in.nextString();
/*  92:    */           try
/*  93:    */           {
/*  94:107 */             set = Integer.parseInt(stringValue) != 0;
/*  95:    */           }
/*  96:    */           catch (NumberFormatException e)
/*  97:    */           {
/*  98:109 */             throw new JsonSyntaxException("Error: Expecting: bitset number value (1, 0), Found: " + stringValue);
/*  99:    */           }
/* 100:    */         default: 
/* 101:114 */           throw new JsonSyntaxException("Invalid bitset value type: " + tokenType);
/* 102:    */         }
/* 103:116 */         if (set) {
/* 104:117 */           bitset.set(i);
/* 105:    */         }
/* 106:119 */         i++;
/* 107:120 */         tokenType = in.peek();
/* 108:    */       }
/* 109:122 */       in.endArray();
/* 110:123 */       return bitset;
/* 111:    */     }
/* 112:    */     
/* 113:    */     public void write(JsonWriter out, BitSet src)
/* 114:    */       throws IOException
/* 115:    */     {
/* 116:127 */       if (src == null)
/* 117:    */       {
/* 118:128 */         out.nullValue();
/* 119:129 */         return;
/* 120:    */       }
/* 121:132 */       out.beginArray();
/* 122:133 */       for (int i = 0; i < src.length(); i++)
/* 123:    */       {
/* 124:134 */         int value = src.get(i) ? 1 : 0;
/* 125:135 */         out.value(value);
/* 126:    */       }
/* 127:137 */       out.endArray();
/* 128:    */     }
/* 129:    */   };
/* 130:141 */   public static final TypeAdapterFactory BIT_SET_FACTORY = newFactory(BitSet.class, BIT_SET);
/* 131:143 */   public static final TypeAdapter<Boolean> BOOLEAN = new TypeAdapter()
/* 132:    */   {
/* 133:    */     public Boolean read(JsonReader in)
/* 134:    */       throws IOException
/* 135:    */     {
/* 136:146 */       if (in.peek() == JsonToken.NULL)
/* 137:    */       {
/* 138:147 */         in.nextNull();
/* 139:148 */         return null;
/* 140:    */       }
/* 141:149 */       if (in.peek() == JsonToken.STRING) {
/* 142:151 */         return Boolean.valueOf(Boolean.parseBoolean(in.nextString()));
/* 143:    */       }
/* 144:153 */       return Boolean.valueOf(in.nextBoolean());
/* 145:    */     }
/* 146:    */     
/* 147:    */     public void write(JsonWriter out, Boolean value)
/* 148:    */       throws IOException
/* 149:    */     {
/* 150:157 */       if (value == null)
/* 151:    */       {
/* 152:158 */         out.nullValue();
/* 153:159 */         return;
/* 154:    */       }
/* 155:161 */       out.value(value.booleanValue());
/* 156:    */     }
/* 157:    */   };
/* 158:169 */   public static final TypeAdapter<Boolean> BOOLEAN_AS_STRING = new TypeAdapter()
/* 159:    */   {
/* 160:    */     public Boolean read(JsonReader in)
/* 161:    */       throws IOException
/* 162:    */     {
/* 163:171 */       if (in.peek() == JsonToken.NULL)
/* 164:    */       {
/* 165:172 */         in.nextNull();
/* 166:173 */         return null;
/* 167:    */       }
/* 168:175 */       return Boolean.valueOf(in.nextString());
/* 169:    */     }
/* 170:    */     
/* 171:    */     public void write(JsonWriter out, Boolean value)
/* 172:    */       throws IOException
/* 173:    */     {
/* 174:179 */       out.value(value == null ? "null" : value.toString());
/* 175:    */     }
/* 176:    */   };
/* 177:183 */   public static final TypeAdapterFactory BOOLEAN_FACTORY = newFactory(Boolean.TYPE, Boolean.class, BOOLEAN);
/* 178:186 */   public static final TypeAdapter<Number> BYTE = new TypeAdapter()
/* 179:    */   {
/* 180:    */     public Number read(JsonReader in)
/* 181:    */       throws IOException
/* 182:    */     {
/* 183:189 */       if (in.peek() == JsonToken.NULL)
/* 184:    */       {
/* 185:190 */         in.nextNull();
/* 186:191 */         return null;
/* 187:    */       }
/* 188:    */       try
/* 189:    */       {
/* 190:194 */         int intValue = in.nextInt();
/* 191:195 */         return Byte.valueOf((byte)intValue);
/* 192:    */       }
/* 193:    */       catch (NumberFormatException e)
/* 194:    */       {
/* 195:197 */         throw new JsonSyntaxException(e);
/* 196:    */       }
/* 197:    */     }
/* 198:    */     
/* 199:    */     public void write(JsonWriter out, Number value)
/* 200:    */       throws IOException
/* 201:    */     {
/* 202:202 */       out.value(value);
/* 203:    */     }
/* 204:    */   };
/* 205:206 */   public static final TypeAdapterFactory BYTE_FACTORY = newFactory(Byte.TYPE, Byte.class, BYTE);
/* 206:209 */   public static final TypeAdapter<Number> SHORT = new TypeAdapter()
/* 207:    */   {
/* 208:    */     public Number read(JsonReader in)
/* 209:    */       throws IOException
/* 210:    */     {
/* 211:212 */       if (in.peek() == JsonToken.NULL)
/* 212:    */       {
/* 213:213 */         in.nextNull();
/* 214:214 */         return null;
/* 215:    */       }
/* 216:    */       try
/* 217:    */       {
/* 218:217 */         return Short.valueOf((short)in.nextInt());
/* 219:    */       }
/* 220:    */       catch (NumberFormatException e)
/* 221:    */       {
/* 222:219 */         throw new JsonSyntaxException(e);
/* 223:    */       }
/* 224:    */     }
/* 225:    */     
/* 226:    */     public void write(JsonWriter out, Number value)
/* 227:    */       throws IOException
/* 228:    */     {
/* 229:224 */       out.value(value);
/* 230:    */     }
/* 231:    */   };
/* 232:228 */   public static final TypeAdapterFactory SHORT_FACTORY = newFactory(Short.TYPE, Short.class, SHORT);
/* 233:231 */   public static final TypeAdapter<Number> INTEGER = new TypeAdapter()
/* 234:    */   {
/* 235:    */     public Number read(JsonReader in)
/* 236:    */       throws IOException
/* 237:    */     {
/* 238:234 */       if (in.peek() == JsonToken.NULL)
/* 239:    */       {
/* 240:235 */         in.nextNull();
/* 241:236 */         return null;
/* 242:    */       }
/* 243:    */       try
/* 244:    */       {
/* 245:239 */         return Integer.valueOf(in.nextInt());
/* 246:    */       }
/* 247:    */       catch (NumberFormatException e)
/* 248:    */       {
/* 249:241 */         throw new JsonSyntaxException(e);
/* 250:    */       }
/* 251:    */     }
/* 252:    */     
/* 253:    */     public void write(JsonWriter out, Number value)
/* 254:    */       throws IOException
/* 255:    */     {
/* 256:246 */       out.value(value);
/* 257:    */     }
/* 258:    */   };
/* 259:250 */   public static final TypeAdapterFactory INTEGER_FACTORY = newFactory(Integer.TYPE, Integer.class, INTEGER);
/* 260:253 */   public static final TypeAdapter<Number> LONG = new TypeAdapter()
/* 261:    */   {
/* 262:    */     public Number read(JsonReader in)
/* 263:    */       throws IOException
/* 264:    */     {
/* 265:256 */       if (in.peek() == JsonToken.NULL)
/* 266:    */       {
/* 267:257 */         in.nextNull();
/* 268:258 */         return null;
/* 269:    */       }
/* 270:    */       try
/* 271:    */       {
/* 272:261 */         return Long.valueOf(in.nextLong());
/* 273:    */       }
/* 274:    */       catch (NumberFormatException e)
/* 275:    */       {
/* 276:263 */         throw new JsonSyntaxException(e);
/* 277:    */       }
/* 278:    */     }
/* 279:    */     
/* 280:    */     public void write(JsonWriter out, Number value)
/* 281:    */       throws IOException
/* 282:    */     {
/* 283:268 */       out.value(value);
/* 284:    */     }
/* 285:    */   };
/* 286:272 */   public static final TypeAdapter<Number> FLOAT = new TypeAdapter()
/* 287:    */   {
/* 288:    */     public Number read(JsonReader in)
/* 289:    */       throws IOException
/* 290:    */     {
/* 291:275 */       if (in.peek() == JsonToken.NULL)
/* 292:    */       {
/* 293:276 */         in.nextNull();
/* 294:277 */         return null;
/* 295:    */       }
/* 296:279 */       return Float.valueOf((float)in.nextDouble());
/* 297:    */     }
/* 298:    */     
/* 299:    */     public void write(JsonWriter out, Number value)
/* 300:    */       throws IOException
/* 301:    */     {
/* 302:283 */       out.value(value);
/* 303:    */     }
/* 304:    */   };
/* 305:287 */   public static final TypeAdapter<Number> DOUBLE = new TypeAdapter()
/* 306:    */   {
/* 307:    */     public Number read(JsonReader in)
/* 308:    */       throws IOException
/* 309:    */     {
/* 310:290 */       if (in.peek() == JsonToken.NULL)
/* 311:    */       {
/* 312:291 */         in.nextNull();
/* 313:292 */         return null;
/* 314:    */       }
/* 315:294 */       return Double.valueOf(in.nextDouble());
/* 316:    */     }
/* 317:    */     
/* 318:    */     public void write(JsonWriter out, Number value)
/* 319:    */       throws IOException
/* 320:    */     {
/* 321:298 */       out.value(value);
/* 322:    */     }
/* 323:    */   };
/* 324:302 */   public static final TypeAdapter<Number> NUMBER = new TypeAdapter()
/* 325:    */   {
/* 326:    */     public Number read(JsonReader in)
/* 327:    */       throws IOException
/* 328:    */     {
/* 329:305 */       JsonToken jsonToken = in.peek();
/* 330:306 */       switch (TypeAdapters.32.$SwitchMap$com$google$gson$stream$JsonToken[jsonToken.ordinal()])
/* 331:    */       {
/* 332:    */       case 4: 
/* 333:308 */         in.nextNull();
/* 334:309 */         return null;
/* 335:    */       case 1: 
/* 336:311 */         return new LazilyParsedNumber(in.nextString());
/* 337:    */       }
/* 338:313 */       throw new JsonSyntaxException("Expecting number, got: " + jsonToken);
/* 339:    */     }
/* 340:    */     
/* 341:    */     public void write(JsonWriter out, Number value)
/* 342:    */       throws IOException
/* 343:    */     {
/* 344:318 */       out.value(value);
/* 345:    */     }
/* 346:    */   };
/* 347:322 */   public static final TypeAdapterFactory NUMBER_FACTORY = newFactory(Number.class, NUMBER);
/* 348:324 */   public static final TypeAdapter<Character> CHARACTER = new TypeAdapter()
/* 349:    */   {
/* 350:    */     public Character read(JsonReader in)
/* 351:    */       throws IOException
/* 352:    */     {
/* 353:327 */       if (in.peek() == JsonToken.NULL)
/* 354:    */       {
/* 355:328 */         in.nextNull();
/* 356:329 */         return null;
/* 357:    */       }
/* 358:331 */       String str = in.nextString();
/* 359:332 */       if (str.length() != 1) {
/* 360:333 */         throw new JsonSyntaxException("Expecting character, got: " + str);
/* 361:    */       }
/* 362:335 */       return Character.valueOf(str.charAt(0));
/* 363:    */     }
/* 364:    */     
/* 365:    */     public void write(JsonWriter out, Character value)
/* 366:    */       throws IOException
/* 367:    */     {
/* 368:339 */       out.value(value == null ? null : String.valueOf(value));
/* 369:    */     }
/* 370:    */   };
/* 371:343 */   public static final TypeAdapterFactory CHARACTER_FACTORY = newFactory(Character.TYPE, Character.class, CHARACTER);
/* 372:346 */   public static final TypeAdapter<String> STRING = new TypeAdapter()
/* 373:    */   {
/* 374:    */     public String read(JsonReader in)
/* 375:    */       throws IOException
/* 376:    */     {
/* 377:349 */       JsonToken peek = in.peek();
/* 378:350 */       if (peek == JsonToken.NULL)
/* 379:    */       {
/* 380:351 */         in.nextNull();
/* 381:352 */         return null;
/* 382:    */       }
/* 383:355 */       if (peek == JsonToken.BOOLEAN) {
/* 384:356 */         return Boolean.toString(in.nextBoolean());
/* 385:    */       }
/* 386:358 */       return in.nextString();
/* 387:    */     }
/* 388:    */     
/* 389:    */     public void write(JsonWriter out, String value)
/* 390:    */       throws IOException
/* 391:    */     {
/* 392:362 */       out.value(value);
/* 393:    */     }
/* 394:    */   };
/* 395:366 */   public static final TypeAdapter<BigDecimal> BIG_DECIMAL = new TypeAdapter()
/* 396:    */   {
/* 397:    */     public BigDecimal read(JsonReader in)
/* 398:    */       throws IOException
/* 399:    */     {
/* 400:368 */       if (in.peek() == JsonToken.NULL)
/* 401:    */       {
/* 402:369 */         in.nextNull();
/* 403:370 */         return null;
/* 404:    */       }
/* 405:    */       try
/* 406:    */       {
/* 407:373 */         return new BigDecimal(in.nextString());
/* 408:    */       }
/* 409:    */       catch (NumberFormatException e)
/* 410:    */       {
/* 411:375 */         throw new JsonSyntaxException(e);
/* 412:    */       }
/* 413:    */     }
/* 414:    */     
/* 415:    */     public void write(JsonWriter out, BigDecimal value)
/* 416:    */       throws IOException
/* 417:    */     {
/* 418:380 */       out.value(value);
/* 419:    */     }
/* 420:    */   };
/* 421:384 */   public static final TypeAdapter<BigInteger> BIG_INTEGER = new TypeAdapter()
/* 422:    */   {
/* 423:    */     public BigInteger read(JsonReader in)
/* 424:    */       throws IOException
/* 425:    */     {
/* 426:386 */       if (in.peek() == JsonToken.NULL)
/* 427:    */       {
/* 428:387 */         in.nextNull();
/* 429:388 */         return null;
/* 430:    */       }
/* 431:    */       try
/* 432:    */       {
/* 433:391 */         return new BigInteger(in.nextString());
/* 434:    */       }
/* 435:    */       catch (NumberFormatException e)
/* 436:    */       {
/* 437:393 */         throw new JsonSyntaxException(e);
/* 438:    */       }
/* 439:    */     }
/* 440:    */     
/* 441:    */     public void write(JsonWriter out, BigInteger value)
/* 442:    */       throws IOException
/* 443:    */     {
/* 444:398 */       out.value(value);
/* 445:    */     }
/* 446:    */   };
/* 447:402 */   public static final TypeAdapterFactory STRING_FACTORY = newFactory(String.class, STRING);
/* 448:404 */   public static final TypeAdapter<StringBuilder> STRING_BUILDER = new TypeAdapter()
/* 449:    */   {
/* 450:    */     public StringBuilder read(JsonReader in)
/* 451:    */       throws IOException
/* 452:    */     {
/* 453:407 */       if (in.peek() == JsonToken.NULL)
/* 454:    */       {
/* 455:408 */         in.nextNull();
/* 456:409 */         return null;
/* 457:    */       }
/* 458:411 */       return new StringBuilder(in.nextString());
/* 459:    */     }
/* 460:    */     
/* 461:    */     public void write(JsonWriter out, StringBuilder value)
/* 462:    */       throws IOException
/* 463:    */     {
/* 464:415 */       out.value(value == null ? null : value.toString());
/* 465:    */     }
/* 466:    */   };
/* 467:419 */   public static final TypeAdapterFactory STRING_BUILDER_FACTORY = newFactory(StringBuilder.class, STRING_BUILDER);
/* 468:422 */   public static final TypeAdapter<StringBuffer> STRING_BUFFER = new TypeAdapter()
/* 469:    */   {
/* 470:    */     public StringBuffer read(JsonReader in)
/* 471:    */       throws IOException
/* 472:    */     {
/* 473:425 */       if (in.peek() == JsonToken.NULL)
/* 474:    */       {
/* 475:426 */         in.nextNull();
/* 476:427 */         return null;
/* 477:    */       }
/* 478:429 */       return new StringBuffer(in.nextString());
/* 479:    */     }
/* 480:    */     
/* 481:    */     public void write(JsonWriter out, StringBuffer value)
/* 482:    */       throws IOException
/* 483:    */     {
/* 484:433 */       out.value(value == null ? null : value.toString());
/* 485:    */     }
/* 486:    */   };
/* 487:437 */   public static final TypeAdapterFactory STRING_BUFFER_FACTORY = newFactory(StringBuffer.class, STRING_BUFFER);
/* 488:440 */   public static final TypeAdapter<URL> URL = new TypeAdapter()
/* 489:    */   {
/* 490:    */     public URL read(JsonReader in)
/* 491:    */       throws IOException
/* 492:    */     {
/* 493:443 */       if (in.peek() == JsonToken.NULL)
/* 494:    */       {
/* 495:444 */         in.nextNull();
/* 496:445 */         return null;
/* 497:    */       }
/* 498:447 */       String nextString = in.nextString();
/* 499:448 */       return "null".equals(nextString) ? null : new URL(nextString);
/* 500:    */     }
/* 501:    */     
/* 502:    */     public void write(JsonWriter out, URL value)
/* 503:    */       throws IOException
/* 504:    */     {
/* 505:452 */       out.value(value == null ? null : value.toExternalForm());
/* 506:    */     }
/* 507:    */   };
/* 508:456 */   public static final TypeAdapterFactory URL_FACTORY = newFactory(URL.class, URL);
/* 509:458 */   public static final TypeAdapter<URI> URI = new TypeAdapter()
/* 510:    */   {
/* 511:    */     public URI read(JsonReader in)
/* 512:    */       throws IOException
/* 513:    */     {
/* 514:461 */       if (in.peek() == JsonToken.NULL)
/* 515:    */       {
/* 516:462 */         in.nextNull();
/* 517:463 */         return null;
/* 518:    */       }
/* 519:    */       try
/* 520:    */       {
/* 521:466 */         String nextString = in.nextString();
/* 522:467 */         return "null".equals(nextString) ? null : new URI(nextString);
/* 523:    */       }
/* 524:    */       catch (URISyntaxException e)
/* 525:    */       {
/* 526:469 */         throw new JsonIOException(e);
/* 527:    */       }
/* 528:    */     }
/* 529:    */     
/* 530:    */     public void write(JsonWriter out, URI value)
/* 531:    */       throws IOException
/* 532:    */     {
/* 533:474 */       out.value(value == null ? null : value.toASCIIString());
/* 534:    */     }
/* 535:    */   };
/* 536:478 */   public static final TypeAdapterFactory URI_FACTORY = newFactory(URI.class, URI);
/* 537:480 */   public static final TypeAdapter<InetAddress> INET_ADDRESS = new TypeAdapter()
/* 538:    */   {
/* 539:    */     public InetAddress read(JsonReader in)
/* 540:    */       throws IOException
/* 541:    */     {
/* 542:483 */       if (in.peek() == JsonToken.NULL)
/* 543:    */       {
/* 544:484 */         in.nextNull();
/* 545:485 */         return null;
/* 546:    */       }
/* 547:488 */       return InetAddress.getByName(in.nextString());
/* 548:    */     }
/* 549:    */     
/* 550:    */     public void write(JsonWriter out, InetAddress value)
/* 551:    */       throws IOException
/* 552:    */     {
/* 553:492 */       out.value(value == null ? null : value.getHostAddress());
/* 554:    */     }
/* 555:    */   };
/* 556:496 */   public static final TypeAdapterFactory INET_ADDRESS_FACTORY = newTypeHierarchyFactory(InetAddress.class, INET_ADDRESS);
/* 557:499 */   public static final TypeAdapter<UUID> UUID = new TypeAdapter()
/* 558:    */   {
/* 559:    */     public UUID read(JsonReader in)
/* 560:    */       throws IOException
/* 561:    */     {
/* 562:502 */       if (in.peek() == JsonToken.NULL)
/* 563:    */       {
/* 564:503 */         in.nextNull();
/* 565:504 */         return null;
/* 566:    */       }
/* 567:506 */       return UUID.fromString(in.nextString());
/* 568:    */     }
/* 569:    */     
/* 570:    */     public void write(JsonWriter out, UUID value)
/* 571:    */       throws IOException
/* 572:    */     {
/* 573:510 */       out.value(value == null ? null : value.toString());
/* 574:    */     }
/* 575:    */   };
/* 576:514 */   public static final TypeAdapterFactory UUID_FACTORY = newFactory(UUID.class, UUID);
/* 577:516 */   public static final TypeAdapterFactory TIMESTAMP_FACTORY = new TypeAdapterFactory()
/* 578:    */   {
/* 579:    */     public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken)
/* 580:    */     {
/* 581:519 */       if (typeToken.getRawType() != Timestamp.class) {
/* 582:520 */         return null;
/* 583:    */       }
/* 584:523 */       final TypeAdapter<Date> dateTypeAdapter = gson.getAdapter(Date.class);
/* 585:524 */       new TypeAdapter()
/* 586:    */       {
/* 587:    */         public Timestamp read(JsonReader in)
/* 588:    */           throws IOException
/* 589:    */         {
/* 590:526 */           Date date = (Date)dateTypeAdapter.read(in);
/* 591:527 */           return date != null ? new Timestamp(date.getTime()) : null;
/* 592:    */         }
/* 593:    */         
/* 594:    */         public void write(JsonWriter out, Timestamp value)
/* 595:    */           throws IOException
/* 596:    */         {
/* 597:531 */           dateTypeAdapter.write(out, value);
/* 598:    */         }
/* 599:    */       };
/* 600:    */     }
/* 601:    */   };
/* 602:537 */   public static final TypeAdapter<Calendar> CALENDAR = new TypeAdapter()
/* 603:    */   {
/* 604:    */     private static final String YEAR = "year";
/* 605:    */     private static final String MONTH = "month";
/* 606:    */     private static final String DAY_OF_MONTH = "dayOfMonth";
/* 607:    */     private static final String HOUR_OF_DAY = "hourOfDay";
/* 608:    */     private static final String MINUTE = "minute";
/* 609:    */     private static final String SECOND = "second";
/* 610:    */     
/* 611:    */     public Calendar read(JsonReader in)
/* 612:    */       throws IOException
/* 613:    */     {
/* 614:547 */       if (in.peek() == JsonToken.NULL)
/* 615:    */       {
/* 616:548 */         in.nextNull();
/* 617:549 */         return null;
/* 618:    */       }
/* 619:551 */       in.beginObject();
/* 620:552 */       int year = 0;
/* 621:553 */       int month = 0;
/* 622:554 */       int dayOfMonth = 0;
/* 623:555 */       int hourOfDay = 0;
/* 624:556 */       int minute = 0;
/* 625:557 */       int second = 0;
/* 626:558 */       while (in.peek() != JsonToken.END_OBJECT)
/* 627:    */       {
/* 628:559 */         String name = in.nextName();
/* 629:560 */         int value = in.nextInt();
/* 630:561 */         if ("year".equals(name)) {
/* 631:562 */           year = value;
/* 632:563 */         } else if ("month".equals(name)) {
/* 633:564 */           month = value;
/* 634:565 */         } else if ("dayOfMonth".equals(name)) {
/* 635:566 */           dayOfMonth = value;
/* 636:567 */         } else if ("hourOfDay".equals(name)) {
/* 637:568 */           hourOfDay = value;
/* 638:569 */         } else if ("minute".equals(name)) {
/* 639:570 */           minute = value;
/* 640:571 */         } else if ("second".equals(name)) {
/* 641:572 */           second = value;
/* 642:    */         }
/* 643:    */       }
/* 644:575 */       in.endObject();
/* 645:576 */       return new GregorianCalendar(year, month, dayOfMonth, hourOfDay, minute, second);
/* 646:    */     }
/* 647:    */     
/* 648:    */     public void write(JsonWriter out, Calendar value)
/* 649:    */       throws IOException
/* 650:    */     {
/* 651:581 */       if (value == null)
/* 652:    */       {
/* 653:582 */         out.nullValue();
/* 654:583 */         return;
/* 655:    */       }
/* 656:585 */       out.beginObject();
/* 657:586 */       out.name("year");
/* 658:587 */       out.value(value.get(1));
/* 659:588 */       out.name("month");
/* 660:589 */       out.value(value.get(2));
/* 661:590 */       out.name("dayOfMonth");
/* 662:591 */       out.value(value.get(5));
/* 663:592 */       out.name("hourOfDay");
/* 664:593 */       out.value(value.get(11));
/* 665:594 */       out.name("minute");
/* 666:595 */       out.value(value.get(12));
/* 667:596 */       out.name("second");
/* 668:597 */       out.value(value.get(13));
/* 669:598 */       out.endObject();
/* 670:    */     }
/* 671:    */   };
/* 672:602 */   public static final TypeAdapterFactory CALENDAR_FACTORY = newFactoryForMultipleTypes(Calendar.class, GregorianCalendar.class, CALENDAR);
/* 673:605 */   public static final TypeAdapter<Locale> LOCALE = new TypeAdapter()
/* 674:    */   {
/* 675:    */     public Locale read(JsonReader in)
/* 676:    */       throws IOException
/* 677:    */     {
/* 678:608 */       if (in.peek() == JsonToken.NULL)
/* 679:    */       {
/* 680:609 */         in.nextNull();
/* 681:610 */         return null;
/* 682:    */       }
/* 683:612 */       String locale = in.nextString();
/* 684:613 */       StringTokenizer tokenizer = new StringTokenizer(locale, "_");
/* 685:614 */       String language = null;
/* 686:615 */       String country = null;
/* 687:616 */       String variant = null;
/* 688:617 */       if (tokenizer.hasMoreElements()) {
/* 689:618 */         language = tokenizer.nextToken();
/* 690:    */       }
/* 691:620 */       if (tokenizer.hasMoreElements()) {
/* 692:621 */         country = tokenizer.nextToken();
/* 693:    */       }
/* 694:623 */       if (tokenizer.hasMoreElements()) {
/* 695:624 */         variant = tokenizer.nextToken();
/* 696:    */       }
/* 697:626 */       if ((country == null) && (variant == null)) {
/* 698:627 */         return new Locale(language);
/* 699:    */       }
/* 700:628 */       if (variant == null) {
/* 701:629 */         return new Locale(language, country);
/* 702:    */       }
/* 703:631 */       return new Locale(language, country, variant);
/* 704:    */     }
/* 705:    */     
/* 706:    */     public void write(JsonWriter out, Locale value)
/* 707:    */       throws IOException
/* 708:    */     {
/* 709:636 */       out.value(value == null ? null : value.toString());
/* 710:    */     }
/* 711:    */   };
/* 712:640 */   public static final TypeAdapterFactory LOCALE_FACTORY = newFactory(Locale.class, LOCALE);
/* 713:642 */   public static final TypeAdapter<JsonElement> JSON_ELEMENT = new TypeAdapter()
/* 714:    */   {
/* 715:    */     public JsonElement read(JsonReader in)
/* 716:    */       throws IOException
/* 717:    */     {
/* 718:644 */       switch (TypeAdapters.32.$SwitchMap$com$google$gson$stream$JsonToken[in.peek().ordinal()])
/* 719:    */       {
/* 720:    */       case 3: 
/* 721:646 */         return new JsonPrimitive(in.nextString());
/* 722:    */       case 1: 
/* 723:648 */         String number = in.nextString();
/* 724:649 */         return new JsonPrimitive(new LazilyParsedNumber(number));
/* 725:    */       case 2: 
/* 726:651 */         return new JsonPrimitive(Boolean.valueOf(in.nextBoolean()));
/* 727:    */       case 4: 
/* 728:653 */         in.nextNull();
/* 729:654 */         return JsonNull.INSTANCE;
/* 730:    */       case 5: 
/* 731:656 */         JsonArray array = new JsonArray();
/* 732:657 */         in.beginArray();
/* 733:658 */         while (in.hasNext()) {
/* 734:659 */           array.add(read(in));
/* 735:    */         }
/* 736:661 */         in.endArray();
/* 737:662 */         return array;
/* 738:    */       case 6: 
/* 739:664 */         JsonObject object = new JsonObject();
/* 740:665 */         in.beginObject();
/* 741:666 */         while (in.hasNext()) {
/* 742:667 */           object.add(in.nextName(), read(in));
/* 743:    */         }
/* 744:669 */         in.endObject();
/* 745:670 */         return object;
/* 746:    */       }
/* 747:676 */       throw new IllegalArgumentException();
/* 748:    */     }
/* 749:    */     
/* 750:    */     public void write(JsonWriter out, JsonElement value)
/* 751:    */       throws IOException
/* 752:    */     {
/* 753:681 */       if ((value == null) || (value.isJsonNull()))
/* 754:    */       {
/* 755:682 */         out.nullValue();
/* 756:    */       }
/* 757:683 */       else if (value.isJsonPrimitive())
/* 758:    */       {
/* 759:684 */         JsonPrimitive primitive = value.getAsJsonPrimitive();
/* 760:685 */         if (primitive.isNumber()) {
/* 761:686 */           out.value(primitive.getAsNumber());
/* 762:687 */         } else if (primitive.isBoolean()) {
/* 763:688 */           out.value(primitive.getAsBoolean());
/* 764:    */         } else {
/* 765:690 */           out.value(primitive.getAsString());
/* 766:    */         }
/* 767:    */       }
/* 768:693 */       else if (value.isJsonArray())
/* 769:    */       {
/* 770:694 */         out.beginArray();
/* 771:695 */         for (JsonElement e : value.getAsJsonArray()) {
/* 772:696 */           write(out, e);
/* 773:    */         }
/* 774:698 */         out.endArray();
/* 775:    */       }
/* 776:700 */       else if (value.isJsonObject())
/* 777:    */       {
/* 778:701 */         out.beginObject();
/* 779:702 */         for (Map.Entry<String, JsonElement> e : value.getAsJsonObject().entrySet())
/* 780:    */         {
/* 781:703 */           out.name((String)e.getKey());
/* 782:704 */           write(out, (JsonElement)e.getValue());
/* 783:    */         }
/* 784:706 */         out.endObject();
/* 785:    */       }
/* 786:    */       else
/* 787:    */       {
/* 788:709 */         throw new IllegalArgumentException("Couldn't write " + value.getClass());
/* 789:    */       }
/* 790:    */     }
/* 791:    */   };
/* 792:714 */   public static final TypeAdapterFactory JSON_ELEMENT_FACTORY = newTypeHierarchyFactory(JsonElement.class, JSON_ELEMENT);
/* 793:    */   
/* 794:    */   private static final class EnumTypeAdapter<T extends Enum<T>>
/* 795:    */     extends TypeAdapter<T>
/* 796:    */   {
/* 797:718 */     private final Map<String, T> nameToConstant = new HashMap();
/* 798:719 */     private final Map<T, String> constantToName = new HashMap();
/* 799:    */     
/* 800:    */     public EnumTypeAdapter(Class<T> classOfT)
/* 801:    */     {
/* 802:    */       try
/* 803:    */       {
/* 804:723 */         for (T constant : (Enum[])classOfT.getEnumConstants())
/* 805:    */         {
/* 806:724 */           String name = constant.name();
/* 807:725 */           SerializedName annotation = (SerializedName)classOfT.getField(name).getAnnotation(SerializedName.class);
/* 808:726 */           if (annotation != null) {
/* 809:727 */             name = annotation.value();
/* 810:    */           }
/* 811:729 */           this.nameToConstant.put(name, constant);
/* 812:730 */           this.constantToName.put(constant, name);
/* 813:    */         }
/* 814:    */       }
/* 815:    */       catch (NoSuchFieldException e)
/* 816:    */       {
/* 817:733 */         throw new AssertionError();
/* 818:    */       }
/* 819:    */     }
/* 820:    */     
/* 821:    */     public T read(JsonReader in)
/* 822:    */       throws IOException
/* 823:    */     {
/* 824:737 */       if (in.peek() == JsonToken.NULL)
/* 825:    */       {
/* 826:738 */         in.nextNull();
/* 827:739 */         return null;
/* 828:    */       }
/* 829:741 */       return (Enum)this.nameToConstant.get(in.nextString());
/* 830:    */     }
/* 831:    */     
/* 832:    */     public void write(JsonWriter out, T value)
/* 833:    */       throws IOException
/* 834:    */     {
/* 835:745 */       out.value(value == null ? null : (String)this.constantToName.get(value));
/* 836:    */     }
/* 837:    */   }
/* 838:    */   
/* 839:749 */   public static final TypeAdapterFactory ENUM_FACTORY = newEnumTypeHierarchyFactory();
/* 840:    */   
/* 841:    */   public static TypeAdapterFactory newEnumTypeHierarchyFactory()
/* 842:    */   {
/* 843:752 */     new TypeAdapterFactory()
/* 844:    */     {
/* 845:    */       public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken)
/* 846:    */       {
/* 847:755 */         Class<? super T> rawType = typeToken.getRawType();
/* 848:756 */         if ((!Enum.class.isAssignableFrom(rawType)) || (rawType == Enum.class)) {
/* 849:757 */           return null;
/* 850:    */         }
/* 851:759 */         if (!rawType.isEnum()) {
/* 852:760 */           rawType = rawType.getSuperclass();
/* 853:    */         }
/* 854:762 */         return new TypeAdapters.EnumTypeAdapter(rawType);
/* 855:    */       }
/* 856:    */     };
/* 857:    */   }
/* 858:    */   
/* 859:    */   public static <TT> TypeAdapterFactory newFactory(TypeToken<TT> type, final TypeAdapter<TT> typeAdapter)
/* 860:    */   {
/* 861:769 */     new TypeAdapterFactory()
/* 862:    */     {
/* 863:    */       public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken)
/* 864:    */       {
/* 865:772 */         return typeToken.equals(this.val$type) ? typeAdapter : null;
/* 866:    */       }
/* 867:    */     };
/* 868:    */   }
/* 869:    */   
/* 870:    */   public static <TT> TypeAdapterFactory newFactory(Class<TT> type, final TypeAdapter<TT> typeAdapter)
/* 871:    */   {
/* 872:779 */     new TypeAdapterFactory()
/* 873:    */     {
/* 874:    */       public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken)
/* 875:    */       {
/* 876:782 */         return typeToken.getRawType() == this.val$type ? typeAdapter : null;
/* 877:    */       }
/* 878:    */       
/* 879:    */       public String toString()
/* 880:    */       {
/* 881:785 */         return "Factory[type=" + this.val$type.getName() + ",adapter=" + typeAdapter + "]";
/* 882:    */       }
/* 883:    */     };
/* 884:    */   }
/* 885:    */   
/* 886:    */   public static <TT> TypeAdapterFactory newFactory(Class<TT> unboxed, final Class<TT> boxed, final TypeAdapter<? super TT> typeAdapter)
/* 887:    */   {
/* 888:792 */     new TypeAdapterFactory()
/* 889:    */     {
/* 890:    */       public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken)
/* 891:    */       {
/* 892:795 */         Class<? super T> rawType = typeToken.getRawType();
/* 893:796 */         return (rawType == this.val$unboxed) || (rawType == boxed) ? typeAdapter : null;
/* 894:    */       }
/* 895:    */       
/* 896:    */       public String toString()
/* 897:    */       {
/* 898:799 */         return "Factory[type=" + boxed.getName() + "+" + this.val$unboxed.getName() + ",adapter=" + typeAdapter + "]";
/* 899:    */       }
/* 900:    */     };
/* 901:    */   }
/* 902:    */   
/* 903:    */   public static <TT> TypeAdapterFactory newFactoryForMultipleTypes(Class<TT> base, final Class<? extends TT> sub, final TypeAdapter<? super TT> typeAdapter)
/* 904:    */   {
/* 905:807 */     new TypeAdapterFactory()
/* 906:    */     {
/* 907:    */       public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken)
/* 908:    */       {
/* 909:810 */         Class<? super T> rawType = typeToken.getRawType();
/* 910:811 */         return (rawType == this.val$base) || (rawType == sub) ? typeAdapter : null;
/* 911:    */       }
/* 912:    */       
/* 913:    */       public String toString()
/* 914:    */       {
/* 915:814 */         return "Factory[type=" + this.val$base.getName() + "+" + sub.getName() + ",adapter=" + typeAdapter + "]";
/* 916:    */       }
/* 917:    */     };
/* 918:    */   }
/* 919:    */   
/* 920:    */   public static <TT> TypeAdapterFactory newTypeHierarchyFactory(Class<TT> clazz, final TypeAdapter<TT> typeAdapter)
/* 921:    */   {
/* 922:822 */     new TypeAdapterFactory()
/* 923:    */     {
/* 924:    */       public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken)
/* 925:    */       {
/* 926:825 */         return this.val$clazz.isAssignableFrom(typeToken.getRawType()) ? typeAdapter : null;
/* 927:    */       }
/* 928:    */       
/* 929:    */       public String toString()
/* 930:    */       {
/* 931:828 */         return "Factory[typeHierarchy=" + this.val$clazz.getName() + ",adapter=" + typeAdapter + "]";
/* 932:    */       }
/* 933:    */     };
/* 934:    */   }
/* 935:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.internal.bind.TypeAdapters
 * JD-Core Version:    0.7.0.1
 */