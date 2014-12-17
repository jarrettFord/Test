/*   1:    */ package io.netty.util;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.PlatformDependent;
/*   4:    */ import io.netty.util.internal.logging.InternalLogger;
/*   5:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   6:    */ import java.io.BufferedReader;
/*   7:    */ import java.io.FileReader;
/*   8:    */ import java.net.Inet4Address;
/*   9:    */ import java.net.Inet6Address;
/*  10:    */ import java.net.InetAddress;
/*  11:    */ import java.net.NetworkInterface;
/*  12:    */ import java.net.SocketException;
/*  13:    */ import java.util.ArrayList;
/*  14:    */ import java.util.Enumeration;
/*  15:    */ import java.util.Iterator;
/*  16:    */ import java.util.List;
/*  17:    */ import java.util.StringTokenizer;
/*  18:    */ 
/*  19:    */ public final class NetUtil
/*  20:    */ {
/*  21:    */   public static final Inet4Address LOCALHOST4;
/*  22:    */   public static final Inet6Address LOCALHOST6;
/*  23:    */   public static final InetAddress LOCALHOST;
/*  24:    */   public static final NetworkInterface LOOPBACK_IF;
/*  25:    */   public static final int SOMAXCONN;
/*  26: 73 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(NetUtil.class);
/*  27:    */   
/*  28:    */   static
/*  29:    */   {
/*  30: 76 */     byte[] LOCALHOST4_BYTES = { 127, 0, 0, 1 };
/*  31: 77 */     byte[] LOCALHOST6_BYTES = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 };
/*  32:    */     
/*  33:    */ 
/*  34: 80 */     Inet4Address localhost4 = null;
/*  35:    */     try
/*  36:    */     {
/*  37: 82 */       localhost4 = (Inet4Address)InetAddress.getByAddress(LOCALHOST4_BYTES);
/*  38:    */     }
/*  39:    */     catch (Exception e)
/*  40:    */     {
/*  41: 85 */       PlatformDependent.throwException(e);
/*  42:    */     }
/*  43: 87 */     LOCALHOST4 = localhost4;
/*  44:    */     
/*  45:    */ 
/*  46: 90 */     Inet6Address localhost6 = null;
/*  47:    */     try
/*  48:    */     {
/*  49: 92 */       localhost6 = (Inet6Address)InetAddress.getByAddress(LOCALHOST6_BYTES);
/*  50:    */     }
/*  51:    */     catch (Exception e)
/*  52:    */     {
/*  53: 95 */       PlatformDependent.throwException(e);
/*  54:    */     }
/*  55: 97 */     LOCALHOST6 = localhost6;
/*  56:    */     
/*  57:    */ 
/*  58:100 */     List<NetworkInterface> ifaces = new ArrayList();
/*  59:    */     try
/*  60:    */     {
/*  61:102 */       for (i = NetworkInterface.getNetworkInterfaces(); i.hasMoreElements();)
/*  62:    */       {
/*  63:103 */         NetworkInterface iface = (NetworkInterface)i.nextElement();
/*  64:105 */         if (iface.getInetAddresses().hasMoreElements()) {
/*  65:106 */           ifaces.add(iface);
/*  66:    */         }
/*  67:    */       }
/*  68:    */     }
/*  69:    */     catch (SocketException e)
/*  70:    */     {
/*  71:    */       Enumeration<NetworkInterface> i;
/*  72:110 */       logger.warn("Failed to retrieve the list of available network interfaces", e);
/*  73:    */     }
/*  74:116 */     NetworkInterface loopbackIface = null;
/*  75:117 */     InetAddress loopbackAddr = null;
/*  76:118 */     for (Iterator i$ = ifaces.iterator(); i$.hasNext();)
/*  77:    */     {
/*  78:118 */       iface = (NetworkInterface)i$.next();
/*  79:119 */       for (i = iface.getInetAddresses(); i.hasMoreElements();)
/*  80:    */       {
/*  81:120 */         InetAddress addr = (InetAddress)i.nextElement();
/*  82:121 */         if (addr.isLoopbackAddress())
/*  83:    */         {
/*  84:123 */           loopbackIface = iface;
/*  85:124 */           loopbackAddr = addr;
/*  86:    */           break label325;
/*  87:    */         }
/*  88:    */       }
/*  89:    */     }
/*  90:    */     NetworkInterface iface;
/*  91:    */     Enumeration<InetAddress> i;
/*  92:    */     label325:
/*  93:131 */     if (loopbackIface == null) {
/*  94:    */       try
/*  95:    */       {
/*  96:133 */         for (NetworkInterface iface : ifaces) {
/*  97:134 */           if (iface.isLoopback())
/*  98:    */           {
/*  99:135 */             Enumeration<InetAddress> i = iface.getInetAddresses();
/* 100:136 */             if (i.hasMoreElements())
/* 101:    */             {
/* 102:138 */               loopbackIface = iface;
/* 103:139 */               loopbackAddr = (InetAddress)i.nextElement();
/* 104:140 */               break;
/* 105:    */             }
/* 106:    */           }
/* 107:    */         }
/* 108:145 */         if (loopbackIface == null) {
/* 109:146 */           logger.warn("Failed to find the loopback interface");
/* 110:    */         }
/* 111:    */       }
/* 112:    */       catch (SocketException e)
/* 113:    */       {
/* 114:149 */         logger.warn("Failed to find the loopback interface", e);
/* 115:    */       }
/* 116:    */     }
/* 117:153 */     if (loopbackIface != null) {
/* 118:155 */       logger.debug("Loopback interface: {} ({}, {})", new Object[] { loopbackIface.getName(), loopbackIface.getDisplayName(), loopbackAddr.getHostAddress() });
/* 119:161 */     } else if (loopbackAddr == null) {
/* 120:    */       try
/* 121:    */       {
/* 122:163 */         if (NetworkInterface.getByInetAddress(LOCALHOST6) != null)
/* 123:    */         {
/* 124:164 */           logger.debug("Using hard-coded IPv6 localhost address: {}", localhost6);
/* 125:165 */           loopbackAddr = localhost6;
/* 126:    */         }
/* 127:    */       }
/* 128:    */       catch (Exception e) {}finally
/* 129:    */       {
/* 130:170 */         if (loopbackAddr == null)
/* 131:    */         {
/* 132:171 */           logger.debug("Using hard-coded IPv4 localhost address: {}", localhost4);
/* 133:172 */           loopbackAddr = localhost4;
/* 134:    */         }
/* 135:    */       }
/* 136:    */     }
/* 137:178 */     LOOPBACK_IF = loopbackIface;
/* 138:179 */     LOCALHOST = loopbackAddr;
/* 139:    */     
/* 140:    */ 
/* 141:182 */     int somaxconn = 3072;
/* 142:183 */     BufferedReader in = null;
/* 143:    */     try
/* 144:    */     {
/* 145:185 */       in = new BufferedReader(new FileReader("/proc/sys/net/core/somaxconn"));
/* 146:186 */       somaxconn = Integer.parseInt(in.readLine());
/* 147:187 */       logger.debug("/proc/sys/net/core/somaxconn: {}", Integer.valueOf(somaxconn));
/* 148:191 */       if (in != null) {
/* 149:    */         try
/* 150:    */         {
/* 151:193 */           in.close();
/* 152:    */         }
/* 153:    */         catch (Exception e) {}
/* 154:    */       }
/* 155:200 */       SOMAXCONN = somaxconn;
/* 156:    */     }
/* 157:    */     catch (Exception e) {}finally
/* 158:    */     {
/* 159:191 */       if (in != null) {
/* 160:    */         try
/* 161:    */         {
/* 162:193 */           in.close();
/* 163:    */         }
/* 164:    */         catch (Exception e) {}
/* 165:    */       }
/* 166:    */     }
/* 167:    */   }
/* 168:    */   
/* 169:    */   public static byte[] createByteArrayFromIpAddressString(String ipAddressString)
/* 170:    */   {
/* 171:209 */     if (isValidIpV4Address(ipAddressString))
/* 172:    */     {
/* 173:210 */       StringTokenizer tokenizer = new StringTokenizer(ipAddressString, ".");
/* 174:    */       
/* 175:    */ 
/* 176:213 */       byte[] byteAddress = new byte[4];
/* 177:214 */       for (int i = 0; i < 4; i++)
/* 178:    */       {
/* 179:215 */         String token = tokenizer.nextToken();
/* 180:216 */         int tempInt = Integer.parseInt(token);
/* 181:217 */         byteAddress[i] = ((byte)tempInt);
/* 182:    */       }
/* 183:220 */       return byteAddress;
/* 184:    */     }
/* 185:223 */     if (isValidIpV6Address(ipAddressString))
/* 186:    */     {
/* 187:224 */       if (ipAddressString.charAt(0) == '[') {
/* 188:225 */         ipAddressString = ipAddressString.substring(1, ipAddressString.length() - 1);
/* 189:    */       }
/* 190:228 */       StringTokenizer tokenizer = new StringTokenizer(ipAddressString, ":.", true);
/* 191:229 */       ArrayList<String> hexStrings = new ArrayList();
/* 192:230 */       ArrayList<String> decStrings = new ArrayList();
/* 193:231 */       String token = "";
/* 194:232 */       String prevToken = "";
/* 195:233 */       int doubleColonIndex = -1;
/* 196:241 */       while (tokenizer.hasMoreTokens())
/* 197:    */       {
/* 198:242 */         prevToken = token;
/* 199:243 */         token = tokenizer.nextToken();
/* 200:245 */         if (":".equals(token))
/* 201:    */         {
/* 202:246 */           if (":".equals(prevToken)) {
/* 203:247 */             doubleColonIndex = hexStrings.size();
/* 204:248 */           } else if (!prevToken.isEmpty()) {
/* 205:249 */             hexStrings.add(prevToken);
/* 206:    */           }
/* 207:    */         }
/* 208:251 */         else if (".".equals(token)) {
/* 209:252 */           decStrings.add(prevToken);
/* 210:    */         }
/* 211:    */       }
/* 212:256 */       if (":".equals(prevToken))
/* 213:    */       {
/* 214:257 */         if (":".equals(token)) {
/* 215:258 */           doubleColonIndex = hexStrings.size();
/* 216:    */         } else {
/* 217:260 */           hexStrings.add(token);
/* 218:    */         }
/* 219:    */       }
/* 220:262 */       else if (".".equals(prevToken)) {
/* 221:263 */         decStrings.add(token);
/* 222:    */       }
/* 223:268 */       int hexStringsLength = 8;
/* 224:272 */       if (!decStrings.isEmpty()) {
/* 225:273 */         hexStringsLength -= 2;
/* 226:    */       }
/* 227:277 */       if (doubleColonIndex != -1)
/* 228:    */       {
/* 229:278 */         int numberToInsert = hexStringsLength - hexStrings.size();
/* 230:279 */         for (int i = 0; i < numberToInsert; i++) {
/* 231:280 */           hexStrings.add(doubleColonIndex, "0");
/* 232:    */         }
/* 233:    */       }
/* 234:284 */       byte[] ipByteArray = new byte[16];
/* 235:287 */       for (int i = 0; i < hexStrings.size(); i++) {
/* 236:288 */         convertToBytes((String)hexStrings.get(i), ipByteArray, i * 2);
/* 237:    */       }
/* 238:292 */       for (int i = 0; i < decStrings.size(); i++) {
/* 239:293 */         ipByteArray[(i + 12)] = ((byte)(Integer.parseInt((String)decStrings.get(i)) & 0xFF));
/* 240:    */       }
/* 241:295 */       return ipByteArray;
/* 242:    */     }
/* 243:297 */     return null;
/* 244:    */   }
/* 245:    */   
/* 246:    */   private static void convertToBytes(String hexWord, byte[] ipByteArray, int byteIndex)
/* 247:    */   {
/* 248:305 */     int hexWordLength = hexWord.length();
/* 249:306 */     int hexWordIndex = 0;
/* 250:307 */     ipByteArray[byteIndex] = 0;
/* 251:308 */     ipByteArray[(byteIndex + 1)] = 0;
/* 252:312 */     if (hexWordLength > 3)
/* 253:    */     {
/* 254:313 */       int charValue = getIntValue(hexWord.charAt(hexWordIndex++)); int 
/* 255:314 */         tmp39_38 = byteIndex; byte[] tmp39_37 = ipByteArray;tmp39_37[tmp39_38] = ((byte)(tmp39_37[tmp39_38] | charValue << 4));
/* 256:    */     }
/* 257:318 */     if (hexWordLength > 2)
/* 258:    */     {
/* 259:319 */       int charValue = getIntValue(hexWord.charAt(hexWordIndex++)); int 
/* 260:320 */         tmp69_68 = byteIndex; byte[] tmp69_67 = ipByteArray;tmp69_67[tmp69_68] = ((byte)(tmp69_67[tmp69_68] | charValue));
/* 261:    */     }
/* 262:324 */     if (hexWordLength > 1)
/* 263:    */     {
/* 264:325 */       int charValue = getIntValue(hexWord.charAt(hexWordIndex++)); int 
/* 265:326 */         tmp99_98 = (byteIndex + 1); byte[] tmp99_95 = ipByteArray;tmp99_95[tmp99_98] = ((byte)(tmp99_95[tmp99_98] | charValue << 4));
/* 266:    */     }
/* 267:330 */     int charValue = getIntValue(hexWord.charAt(hexWordIndex)); int 
/* 268:331 */       tmp123_122 = (byteIndex + 1); byte[] tmp123_119 = ipByteArray;tmp123_119[tmp123_122] = ((byte)(tmp123_119[tmp123_122] | charValue & 0xF));
/* 269:    */   }
/* 270:    */   
/* 271:    */   static int getIntValue(char c)
/* 272:    */   {
/* 273:336 */     switch (c)
/* 274:    */     {
/* 275:    */     case '0': 
/* 276:338 */       return 0;
/* 277:    */     case '1': 
/* 278:340 */       return 1;
/* 279:    */     case '2': 
/* 280:342 */       return 2;
/* 281:    */     case '3': 
/* 282:344 */       return 3;
/* 283:    */     case '4': 
/* 284:346 */       return 4;
/* 285:    */     case '5': 
/* 286:348 */       return 5;
/* 287:    */     case '6': 
/* 288:350 */       return 6;
/* 289:    */     case '7': 
/* 290:352 */       return 7;
/* 291:    */     case '8': 
/* 292:354 */       return 8;
/* 293:    */     case '9': 
/* 294:356 */       return 9;
/* 295:    */     }
/* 296:359 */     c = Character.toLowerCase(c);
/* 297:360 */     switch (c)
/* 298:    */     {
/* 299:    */     case 'a': 
/* 300:362 */       return 10;
/* 301:    */     case 'b': 
/* 302:364 */       return 11;
/* 303:    */     case 'c': 
/* 304:366 */       return 12;
/* 305:    */     case 'd': 
/* 306:368 */       return 13;
/* 307:    */     case 'e': 
/* 308:370 */       return 14;
/* 309:    */     case 'f': 
/* 310:372 */       return 15;
/* 311:    */     }
/* 312:374 */     return 0;
/* 313:    */   }
/* 314:    */   
/* 315:    */   public static boolean isValidIpV6Address(String ipAddress)
/* 316:    */   {
/* 317:378 */     int length = ipAddress.length();
/* 318:379 */     boolean doubleColon = false;
/* 319:380 */     int numberOfColons = 0;
/* 320:381 */     int numberOfPeriods = 0;
/* 321:382 */     int numberOfPercent = 0;
/* 322:383 */     StringBuilder word = new StringBuilder();
/* 323:384 */     char c = '\000';
/* 324:    */     
/* 325:386 */     int offset = 0;
/* 326:388 */     if (length < 2) {
/* 327:389 */       return false;
/* 328:    */     }
/* 329:392 */     for (int i = 0; i < length; i++)
/* 330:    */     {
/* 331:393 */       char prevChar = c;
/* 332:394 */       c = ipAddress.charAt(i);
/* 333:395 */       switch (c)
/* 334:    */       {
/* 335:    */       case '[': 
/* 336:399 */         if (i != 0) {
/* 337:400 */           return false;
/* 338:    */         }
/* 339:402 */         if (ipAddress.charAt(length - 1) != ']') {
/* 340:403 */           return false;
/* 341:    */         }
/* 342:405 */         offset = 1;
/* 343:406 */         if (length < 4) {
/* 344:407 */           return false;
/* 345:    */         }
/* 346:    */         break;
/* 347:    */       case ']': 
/* 348:413 */         if (i != length - 1) {
/* 349:414 */           return false;
/* 350:    */         }
/* 351:416 */         if (ipAddress.charAt(0) != '[') {
/* 352:417 */           return false;
/* 353:    */         }
/* 354:    */         break;
/* 355:    */       case '.': 
/* 356:423 */         numberOfPeriods++;
/* 357:424 */         if (numberOfPeriods > 3) {
/* 358:425 */           return false;
/* 359:    */         }
/* 360:427 */         if (!isValidIp4Word(word.toString())) {
/* 361:428 */           return false;
/* 362:    */         }
/* 363:430 */         if ((numberOfColons != 6) && (!doubleColon)) {
/* 364:431 */           return false;
/* 365:    */         }
/* 366:435 */         if ((numberOfColons == 7) && (ipAddress.charAt(offset) != ':') && (ipAddress.charAt(1 + offset) != ':')) {
/* 367:437 */           return false;
/* 368:    */         }
/* 369:439 */         word.delete(0, word.length());
/* 370:440 */         break;
/* 371:    */       case ':': 
/* 372:446 */         if ((i == offset) && ((ipAddress.length() <= i) || (ipAddress.charAt(i + 1) != ':'))) {
/* 373:447 */           return false;
/* 374:    */         }
/* 375:450 */         numberOfColons++;
/* 376:451 */         if (numberOfColons > 7) {
/* 377:452 */           return false;
/* 378:    */         }
/* 379:454 */         if (numberOfPeriods > 0) {
/* 380:455 */           return false;
/* 381:    */         }
/* 382:457 */         if (prevChar == ':')
/* 383:    */         {
/* 384:458 */           if (doubleColon) {
/* 385:459 */             return false;
/* 386:    */           }
/* 387:461 */           doubleColon = true;
/* 388:    */         }
/* 389:463 */         word.delete(0, word.length());
/* 390:464 */         break;
/* 391:    */       case '%': 
/* 392:466 */         if (numberOfColons == 0) {
/* 393:467 */           return false;
/* 394:    */         }
/* 395:469 */         numberOfPercent++;
/* 396:472 */         if (i + 1 >= length) {
/* 397:475 */           return false;
/* 398:    */         }
/* 399:    */         try
/* 400:    */         {
/* 401:478 */           if (Integer.parseInt(ipAddress.substring(i + 1)) < 0) {
/* 402:479 */             return false;
/* 403:    */           }
/* 404:    */         }
/* 405:    */         catch (NumberFormatException e)
/* 406:    */         {
/* 407:485 */           return false;
/* 408:    */         }
/* 409:    */       default: 
/* 410:490 */         if (numberOfPercent == 0)
/* 411:    */         {
/* 412:491 */           if ((word != null) && (word.length() > 3)) {
/* 413:492 */             return false;
/* 414:    */           }
/* 415:494 */           if (!isValidHexChar(c)) {
/* 416:495 */             return false;
/* 417:    */           }
/* 418:    */         }
/* 419:498 */         word.append(c);
/* 420:    */       }
/* 421:    */     }
/* 422:503 */     if (numberOfPeriods > 0)
/* 423:    */     {
/* 424:505 */       if ((numberOfPeriods != 3) || (!isValidIp4Word(word.toString())) || (numberOfColons >= 7)) {
/* 425:506 */         return false;
/* 426:    */       }
/* 427:    */     }
/* 428:    */     else
/* 429:    */     {
/* 430:511 */       if ((numberOfColons != 7) && (!doubleColon)) {
/* 431:512 */         return false;
/* 432:    */       }
/* 433:518 */       if ((numberOfPercent == 0) && 
/* 434:519 */         (word.length() == 0) && (ipAddress.charAt(length - 1 - offset) == ':') && (ipAddress.charAt(length - 2 - offset) != ':')) {
/* 435:521 */         return false;
/* 436:    */       }
/* 437:    */     }
/* 438:526 */     return true;
/* 439:    */   }
/* 440:    */   
/* 441:    */   public static boolean isValidIp4Word(String word)
/* 442:    */   {
/* 443:531 */     if ((word.length() < 1) || (word.length() > 3)) {
/* 444:532 */       return false;
/* 445:    */     }
/* 446:534 */     for (int i = 0; i < word.length(); i++)
/* 447:    */     {
/* 448:535 */       char c = word.charAt(i);
/* 449:536 */       if ((c < '0') || (c > '9')) {
/* 450:537 */         return false;
/* 451:    */       }
/* 452:    */     }
/* 453:540 */     return Integer.parseInt(word) <= 255;
/* 454:    */   }
/* 455:    */   
/* 456:    */   static boolean isValidHexChar(char c)
/* 457:    */   {
/* 458:544 */     return ((c >= '0') && (c <= '9')) || ((c >= 'A') && (c <= 'F')) || ((c >= 'a') && (c <= 'f'));
/* 459:    */   }
/* 460:    */   
/* 461:    */   public static boolean isValidIpV4Address(String value)
/* 462:    */   {
/* 463:555 */     int periods = 0;
/* 464:    */     
/* 465:557 */     int length = value.length();
/* 466:559 */     if (length > 15) {
/* 467:560 */       return false;
/* 468:    */     }
/* 469:563 */     StringBuilder word = new StringBuilder();
/* 470:564 */     for (int i = 0; i < length; i++)
/* 471:    */     {
/* 472:565 */       char c = value.charAt(i);
/* 473:566 */       if (c == '.')
/* 474:    */       {
/* 475:567 */         periods++;
/* 476:568 */         if (periods > 3) {
/* 477:569 */           return false;
/* 478:    */         }
/* 479:571 */         if (word.length() == 0) {
/* 480:572 */           return false;
/* 481:    */         }
/* 482:574 */         if (Integer.parseInt(word.toString()) > 255) {
/* 483:575 */           return false;
/* 484:    */         }
/* 485:577 */         word.delete(0, word.length());
/* 486:    */       }
/* 487:    */       else
/* 488:    */       {
/* 489:578 */         if (!Character.isDigit(c)) {
/* 490:579 */           return false;
/* 491:    */         }
/* 492:581 */         if (word.length() > 2) {
/* 493:582 */           return false;
/* 494:    */         }
/* 495:584 */         word.append(c);
/* 496:    */       }
/* 497:    */     }
/* 498:588 */     if ((word.length() == 0) || (Integer.parseInt(word.toString()) > 255)) {
/* 499:589 */       return false;
/* 500:    */     }
/* 501:592 */     return periods == 3;
/* 502:    */   }
/* 503:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.NetUtil
 * JD-Core Version:    0.7.0.1
 */