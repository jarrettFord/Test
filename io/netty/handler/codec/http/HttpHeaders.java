/*    1:     */ package io.netty.handler.codec.http;
/*    2:     */ 
/*    3:     */ import io.netty.buffer.ByteBuf;
/*    4:     */ import java.text.ParseException;
/*    5:     */ import java.util.Collections;
/*    6:     */ import java.util.Date;
/*    7:     */ import java.util.Iterator;
/*    8:     */ import java.util.List;
/*    9:     */ import java.util.Map.Entry;
/*   10:     */ import java.util.Set;
/*   11:     */ 
/*   12:     */ public abstract class HttpHeaders
/*   13:     */   implements Iterable<Map.Entry<String, String>>
/*   14:     */ {
/*   15:  38 */   private static final byte[] HEADER_SEPERATOR = { 58, 32 };
/*   16:  39 */   private static final byte[] CRLF = { 13, 10 };
/*   17:  40 */   private static final CharSequence CONTENT_LENGTH_ENTITY = newEntity("Content-Length");
/*   18:  41 */   private static final CharSequence CONNECTION_ENTITY = newEntity("Connection");
/*   19:  42 */   private static final CharSequence CLOSE_ENTITY = newEntity("close");
/*   20:  43 */   private static final CharSequence KEEP_ALIVE_ENTITY = newEntity("keep-alive");
/*   21:  44 */   private static final CharSequence HOST_ENTITY = newEntity("Host");
/*   22:  45 */   private static final CharSequence DATE_ENTITY = newEntity("Date");
/*   23:  46 */   private static final CharSequence EXPECT_ENTITY = newEntity("Expect");
/*   24:  47 */   private static final CharSequence CONTINUE_ENTITY = newEntity("100-continue");
/*   25:  48 */   private static final CharSequence TRANSFER_ENCODING_ENTITY = newEntity("Transfer-Encoding");
/*   26:  49 */   private static final CharSequence CHUNKED_ENTITY = newEntity("chunked");
/*   27:  50 */   private static final CharSequence SEC_WEBSOCKET_KEY1_ENTITY = newEntity("Sec-WebSocket-Key1");
/*   28:  51 */   private static final CharSequence SEC_WEBSOCKET_KEY2_ENTITY = newEntity("Sec-WebSocket-Key2");
/*   29:  52 */   private static final CharSequence SEC_WEBSOCKET_ORIGIN_ENTITY = newEntity("Sec-WebSocket-Origin");
/*   30:  53 */   private static final CharSequence SEC_WEBSOCKET_LOCATION_ENTITY = newEntity("Sec-WebSocket-Location");
/*   31:  55 */   public static final HttpHeaders EMPTY_HEADERS = new HttpHeaders()
/*   32:     */   {
/*   33:     */     public String get(String name)
/*   34:     */     {
/*   35:  58 */       return null;
/*   36:     */     }
/*   37:     */     
/*   38:     */     public List<String> getAll(String name)
/*   39:     */     {
/*   40:  63 */       return Collections.emptyList();
/*   41:     */     }
/*   42:     */     
/*   43:     */     public List<Map.Entry<String, String>> entries()
/*   44:     */     {
/*   45:  68 */       return Collections.emptyList();
/*   46:     */     }
/*   47:     */     
/*   48:     */     public boolean contains(String name)
/*   49:     */     {
/*   50:  73 */       return false;
/*   51:     */     }
/*   52:     */     
/*   53:     */     public boolean isEmpty()
/*   54:     */     {
/*   55:  78 */       return true;
/*   56:     */     }
/*   57:     */     
/*   58:     */     public Set<String> names()
/*   59:     */     {
/*   60:  83 */       return Collections.emptySet();
/*   61:     */     }
/*   62:     */     
/*   63:     */     public HttpHeaders add(String name, Object value)
/*   64:     */     {
/*   65:  88 */       throw new UnsupportedOperationException("read only");
/*   66:     */     }
/*   67:     */     
/*   68:     */     public HttpHeaders add(String name, Iterable<?> values)
/*   69:     */     {
/*   70:  93 */       throw new UnsupportedOperationException("read only");
/*   71:     */     }
/*   72:     */     
/*   73:     */     public HttpHeaders set(String name, Object value)
/*   74:     */     {
/*   75:  98 */       throw new UnsupportedOperationException("read only");
/*   76:     */     }
/*   77:     */     
/*   78:     */     public HttpHeaders set(String name, Iterable<?> values)
/*   79:     */     {
/*   80: 103 */       throw new UnsupportedOperationException("read only");
/*   81:     */     }
/*   82:     */     
/*   83:     */     public HttpHeaders remove(String name)
/*   84:     */     {
/*   85: 108 */       throw new UnsupportedOperationException("read only");
/*   86:     */     }
/*   87:     */     
/*   88:     */     public HttpHeaders clear()
/*   89:     */     {
/*   90: 113 */       throw new UnsupportedOperationException("read only");
/*   91:     */     }
/*   92:     */     
/*   93:     */     public Iterator<Map.Entry<String, String>> iterator()
/*   94:     */     {
/*   95: 118 */       return entries().iterator();
/*   96:     */     }
/*   97:     */   };
/*   98:     */   
/*   99:     */   public static boolean isKeepAlive(HttpMessage message)
/*  100:     */   {
/*  101: 568 */     String connection = message.headers().get(CONNECTION_ENTITY);
/*  102: 569 */     if ((connection != null) && (equalsIgnoreCase(CLOSE_ENTITY, connection))) {
/*  103: 570 */       return false;
/*  104:     */     }
/*  105: 573 */     if (message.getProtocolVersion().isKeepAliveDefault()) {
/*  106: 574 */       return !equalsIgnoreCase(CLOSE_ENTITY, connection);
/*  107:     */     }
/*  108: 576 */     return equalsIgnoreCase(KEEP_ALIVE_ENTITY, connection);
/*  109:     */   }
/*  110:     */   
/*  111:     */   public static void setKeepAlive(HttpMessage message, boolean keepAlive)
/*  112:     */   {
/*  113: 600 */     HttpHeaders h = message.headers();
/*  114: 601 */     if (message.getProtocolVersion().isKeepAliveDefault())
/*  115:     */     {
/*  116: 602 */       if (keepAlive) {
/*  117: 603 */         h.remove(CONNECTION_ENTITY);
/*  118:     */       } else {
/*  119: 605 */         h.set(CONNECTION_ENTITY, CLOSE_ENTITY);
/*  120:     */       }
/*  121:     */     }
/*  122: 608 */     else if (keepAlive) {
/*  123: 609 */       h.set(CONNECTION_ENTITY, KEEP_ALIVE_ENTITY);
/*  124:     */     } else {
/*  125: 611 */       h.remove(CONNECTION_ENTITY);
/*  126:     */     }
/*  127:     */   }
/*  128:     */   
/*  129:     */   public static String getHeader(HttpMessage message, String name)
/*  130:     */   {
/*  131: 620 */     return message.headers().get(name);
/*  132:     */   }
/*  133:     */   
/*  134:     */   public static String getHeader(HttpMessage message, CharSequence name)
/*  135:     */   {
/*  136: 631 */     return message.headers().get(name);
/*  137:     */   }
/*  138:     */   
/*  139:     */   public static String getHeader(HttpMessage message, String name, String defaultValue)
/*  140:     */   {
/*  141: 638 */     return getHeader(message, name, defaultValue);
/*  142:     */   }
/*  143:     */   
/*  144:     */   public static String getHeader(HttpMessage message, CharSequence name, String defaultValue)
/*  145:     */   {
/*  146: 650 */     String value = message.headers().get(name);
/*  147: 651 */     if (value == null) {
/*  148: 652 */       return defaultValue;
/*  149:     */     }
/*  150: 654 */     return value;
/*  151:     */   }
/*  152:     */   
/*  153:     */   public static void setHeader(HttpMessage message, String name, Object value)
/*  154:     */   {
/*  155: 661 */     message.headers().set(name, value);
/*  156:     */   }
/*  157:     */   
/*  158:     */   public static void setHeader(HttpMessage message, CharSequence name, Object value)
/*  159:     */   {
/*  160: 673 */     message.headers().set(name, value);
/*  161:     */   }
/*  162:     */   
/*  163:     */   public static void setHeader(HttpMessage message, String name, Iterable<?> values)
/*  164:     */   {
/*  165: 681 */     message.headers().set(name, values);
/*  166:     */   }
/*  167:     */   
/*  168:     */   public static void setHeader(HttpMessage message, CharSequence name, Iterable<?> values)
/*  169:     */   {
/*  170: 699 */     message.headers().set(name, values);
/*  171:     */   }
/*  172:     */   
/*  173:     */   public static void addHeader(HttpMessage message, String name, Object value)
/*  174:     */   {
/*  175: 706 */     message.headers().add(name, value);
/*  176:     */   }
/*  177:     */   
/*  178:     */   public static void addHeader(HttpMessage message, CharSequence name, Object value)
/*  179:     */   {
/*  180: 717 */     message.headers().add(name, value);
/*  181:     */   }
/*  182:     */   
/*  183:     */   public static void removeHeader(HttpMessage message, String name)
/*  184:     */   {
/*  185: 724 */     message.headers().remove(name);
/*  186:     */   }
/*  187:     */   
/*  188:     */   public static void removeHeader(HttpMessage message, CharSequence name)
/*  189:     */   {
/*  190: 731 */     message.headers().remove(name);
/*  191:     */   }
/*  192:     */   
/*  193:     */   public static void clearHeaders(HttpMessage message)
/*  194:     */   {
/*  195: 738 */     message.headers().clear();
/*  196:     */   }
/*  197:     */   
/*  198:     */   public static int getIntHeader(HttpMessage message, String name)
/*  199:     */   {
/*  200: 745 */     return getIntHeader(message, name);
/*  201:     */   }
/*  202:     */   
/*  203:     */   public static int getIntHeader(HttpMessage message, CharSequence name)
/*  204:     */   {
/*  205: 758 */     String value = getHeader(message, name);
/*  206: 759 */     if (value == null) {
/*  207: 760 */       throw new NumberFormatException("header not found: " + name);
/*  208:     */     }
/*  209: 762 */     return Integer.parseInt(value);
/*  210:     */   }
/*  211:     */   
/*  212:     */   public static int getIntHeader(HttpMessage message, String name, int defaultValue)
/*  213:     */   {
/*  214: 769 */     return getIntHeader(message, name, defaultValue);
/*  215:     */   }
/*  216:     */   
/*  217:     */   public static int getIntHeader(HttpMessage message, CharSequence name, int defaultValue)
/*  218:     */   {
/*  219: 781 */     String value = getHeader(message, name);
/*  220: 782 */     if (value == null) {
/*  221: 783 */       return defaultValue;
/*  222:     */     }
/*  223:     */     try
/*  224:     */     {
/*  225: 787 */       return Integer.parseInt(value);
/*  226:     */     }
/*  227:     */     catch (NumberFormatException e) {}
/*  228: 789 */     return defaultValue;
/*  229:     */   }
/*  230:     */   
/*  231:     */   public static void setIntHeader(HttpMessage message, String name, int value)
/*  232:     */   {
/*  233: 797 */     message.headers().set(name, Integer.valueOf(value));
/*  234:     */   }
/*  235:     */   
/*  236:     */   public static void setIntHeader(HttpMessage message, CharSequence name, int value)
/*  237:     */   {
/*  238: 805 */     message.headers().set(name, Integer.valueOf(value));
/*  239:     */   }
/*  240:     */   
/*  241:     */   public static void setIntHeader(HttpMessage message, String name, Iterable<Integer> values)
/*  242:     */   {
/*  243: 812 */     message.headers().set(name, values);
/*  244:     */   }
/*  245:     */   
/*  246:     */   public static void setIntHeader(HttpMessage message, CharSequence name, Iterable<Integer> values)
/*  247:     */   {
/*  248: 820 */     message.headers().set(name, values);
/*  249:     */   }
/*  250:     */   
/*  251:     */   public static void addIntHeader(HttpMessage message, String name, int value)
/*  252:     */   {
/*  253: 828 */     message.headers().add(name, Integer.valueOf(value));
/*  254:     */   }
/*  255:     */   
/*  256:     */   public static void addIntHeader(HttpMessage message, CharSequence name, int value)
/*  257:     */   {
/*  258: 835 */     message.headers().add(name, Integer.valueOf(value));
/*  259:     */   }
/*  260:     */   
/*  261:     */   public static Date getDateHeader(HttpMessage message, String name)
/*  262:     */     throws ParseException
/*  263:     */   {
/*  264: 842 */     return getDateHeader(message, name);
/*  265:     */   }
/*  266:     */   
/*  267:     */   public static Date getDateHeader(HttpMessage message, CharSequence name)
/*  268:     */     throws ParseException
/*  269:     */   {
/*  270: 855 */     String value = getHeader(message, name);
/*  271: 856 */     if (value == null) {
/*  272: 857 */       throw new ParseException("header not found: " + name, 0);
/*  273:     */     }
/*  274: 859 */     return HttpHeaderDateFormat.get().parse(value);
/*  275:     */   }
/*  276:     */   
/*  277:     */   public static Date getDateHeader(HttpMessage message, String name, Date defaultValue)
/*  278:     */   {
/*  279: 866 */     return getDateHeader(message, name, defaultValue);
/*  280:     */   }
/*  281:     */   
/*  282:     */   public static Date getDateHeader(HttpMessage message, CharSequence name, Date defaultValue)
/*  283:     */   {
/*  284: 878 */     String value = getHeader(message, name);
/*  285: 879 */     if (value == null) {
/*  286: 880 */       return defaultValue;
/*  287:     */     }
/*  288:     */     try
/*  289:     */     {
/*  290: 884 */       return HttpHeaderDateFormat.get().parse(value);
/*  291:     */     }
/*  292:     */     catch (ParseException e) {}
/*  293: 886 */     return defaultValue;
/*  294:     */   }
/*  295:     */   
/*  296:     */   public static void setDateHeader(HttpMessage message, String name, Date value)
/*  297:     */   {
/*  298: 894 */     setDateHeader(message, name, value);
/*  299:     */   }
/*  300:     */   
/*  301:     */   public static void setDateHeader(HttpMessage message, CharSequence name, Date value)
/*  302:     */   {
/*  303: 904 */     if (value != null) {
/*  304: 905 */       message.headers().set(name, HttpHeaderDateFormat.get().format(value));
/*  305:     */     } else {
/*  306: 907 */       message.headers().set(name, null);
/*  307:     */     }
/*  308:     */   }
/*  309:     */   
/*  310:     */   public static void setDateHeader(HttpMessage message, String name, Iterable<Date> values)
/*  311:     */   {
/*  312: 915 */     message.headers().set(name, values);
/*  313:     */   }
/*  314:     */   
/*  315:     */   public static void setDateHeader(HttpMessage message, CharSequence name, Iterable<Date> values)
/*  316:     */   {
/*  317: 925 */     message.headers().set(name, values);
/*  318:     */   }
/*  319:     */   
/*  320:     */   public static void addDateHeader(HttpMessage message, String name, Date value)
/*  321:     */   {
/*  322: 932 */     message.headers().add(name, value);
/*  323:     */   }
/*  324:     */   
/*  325:     */   public static void addDateHeader(HttpMessage message, CharSequence name, Date value)
/*  326:     */   {
/*  327: 941 */     message.headers().add(name, value);
/*  328:     */   }
/*  329:     */   
/*  330:     */   public static long getContentLength(HttpMessage message)
/*  331:     */   {
/*  332: 957 */     String value = getHeader(message, CONTENT_LENGTH_ENTITY);
/*  333: 958 */     if (value != null) {
/*  334: 959 */       return Long.parseLong(value);
/*  335:     */     }
/*  336: 964 */     long webSocketContentLength = getWebSocketContentLength(message);
/*  337: 965 */     if (webSocketContentLength >= 0L) {
/*  338: 966 */       return webSocketContentLength;
/*  339:     */     }
/*  340: 970 */     throw new NumberFormatException("header not found: Content-Length");
/*  341:     */   }
/*  342:     */   
/*  343:     */   public static long getContentLength(HttpMessage message, long defaultValue)
/*  344:     */   {
/*  345: 984 */     String contentLength = message.headers().get(CONTENT_LENGTH_ENTITY);
/*  346: 985 */     if (contentLength != null) {
/*  347:     */       try
/*  348:     */       {
/*  349: 987 */         return Long.parseLong(contentLength);
/*  350:     */       }
/*  351:     */       catch (NumberFormatException e)
/*  352:     */       {
/*  353: 989 */         return defaultValue;
/*  354:     */       }
/*  355:     */     }
/*  356: 995 */     long webSocketContentLength = getWebSocketContentLength(message);
/*  357: 996 */     if (webSocketContentLength >= 0L) {
/*  358: 997 */       return webSocketContentLength;
/*  359:     */     }
/*  360:1001 */     return defaultValue;
/*  361:     */   }
/*  362:     */   
/*  363:     */   private static int getWebSocketContentLength(HttpMessage message)
/*  364:     */   {
/*  365:1010 */     HttpHeaders h = message.headers();
/*  366:1011 */     if ((message instanceof HttpRequest))
/*  367:     */     {
/*  368:1012 */       HttpRequest req = (HttpRequest)message;
/*  369:1013 */       if ((HttpMethod.GET.equals(req.getMethod())) && (h.contains(SEC_WEBSOCKET_KEY1_ENTITY)) && (h.contains(SEC_WEBSOCKET_KEY2_ENTITY))) {
/*  370:1016 */         return 8;
/*  371:     */       }
/*  372:     */     }
/*  373:1018 */     else if ((message instanceof HttpResponse))
/*  374:     */     {
/*  375:1019 */       HttpResponse res = (HttpResponse)message;
/*  376:1020 */       if ((res.getStatus().code() == 101) && (h.contains(SEC_WEBSOCKET_ORIGIN_ENTITY)) && (h.contains(SEC_WEBSOCKET_LOCATION_ENTITY))) {
/*  377:1023 */         return 16;
/*  378:     */       }
/*  379:     */     }
/*  380:1028 */     return -1;
/*  381:     */   }
/*  382:     */   
/*  383:     */   public static void setContentLength(HttpMessage message, long length)
/*  384:     */   {
/*  385:1035 */     message.headers().set(CONTENT_LENGTH_ENTITY, Long.valueOf(length));
/*  386:     */   }
/*  387:     */   
/*  388:     */   public static String getHost(HttpMessage message)
/*  389:     */   {
/*  390:1042 */     return message.headers().get(HOST_ENTITY);
/*  391:     */   }
/*  392:     */   
/*  393:     */   public static String getHost(HttpMessage message, String defaultValue)
/*  394:     */   {
/*  395:1050 */     return getHeader(message, HOST_ENTITY, defaultValue);
/*  396:     */   }
/*  397:     */   
/*  398:     */   public static void setHost(HttpMessage message, String value)
/*  399:     */   {
/*  400:1057 */     message.headers().set(HOST_ENTITY, value);
/*  401:     */   }
/*  402:     */   
/*  403:     */   public static void setHost(HttpMessage message, CharSequence value)
/*  404:     */   {
/*  405:1064 */     message.headers().set(HOST_ENTITY, value);
/*  406:     */   }
/*  407:     */   
/*  408:     */   public static Date getDate(HttpMessage message)
/*  409:     */     throws ParseException
/*  410:     */   {
/*  411:1074 */     return getDateHeader(message, DATE_ENTITY);
/*  412:     */   }
/*  413:     */   
/*  414:     */   public static Date getDate(HttpMessage message, Date defaultValue)
/*  415:     */   {
/*  416:1083 */     return getDateHeader(message, DATE_ENTITY, defaultValue);
/*  417:     */   }
/*  418:     */   
/*  419:     */   public static void setDate(HttpMessage message, Date value)
/*  420:     */   {
/*  421:1090 */     if (value != null) {
/*  422:1091 */       message.headers().set(DATE_ENTITY, HttpHeaderDateFormat.get().format(value));
/*  423:     */     } else {
/*  424:1093 */       message.headers().set(DATE_ENTITY, null);
/*  425:     */     }
/*  426:     */   }
/*  427:     */   
/*  428:     */   public static boolean is100ContinueExpected(HttpMessage message)
/*  429:     */   {
/*  430:1103 */     if (!(message instanceof HttpRequest)) {
/*  431:1104 */       return false;
/*  432:     */     }
/*  433:1108 */     if (message.getProtocolVersion().compareTo(HttpVersion.HTTP_1_1) < 0) {
/*  434:1109 */       return false;
/*  435:     */     }
/*  436:1113 */     String value = message.headers().get(EXPECT_ENTITY);
/*  437:1114 */     if (value == null) {
/*  438:1115 */       return false;
/*  439:     */     }
/*  440:1117 */     if (equalsIgnoreCase(CONTINUE_ENTITY, value)) {
/*  441:1118 */       return true;
/*  442:     */     }
/*  443:1122 */     return message.headers().contains(EXPECT_ENTITY, CONTINUE_ENTITY, true);
/*  444:     */   }
/*  445:     */   
/*  446:     */   public static void set100ContinueExpected(HttpMessage message)
/*  447:     */   {
/*  448:1131 */     set100ContinueExpected(message, true);
/*  449:     */   }
/*  450:     */   
/*  451:     */   public static void set100ContinueExpected(HttpMessage message, boolean set)
/*  452:     */   {
/*  453:1142 */     if (set) {
/*  454:1143 */       message.headers().set(EXPECT_ENTITY, CONTINUE_ENTITY);
/*  455:     */     } else {
/*  456:1145 */       message.headers().remove(EXPECT_ENTITY);
/*  457:     */     }
/*  458:     */   }
/*  459:     */   
/*  460:     */   static void validateHeaderName(CharSequence headerName)
/*  461:     */   {
/*  462:1156 */     if (headerName == null) {
/*  463:1157 */       throw new NullPointerException("Header names cannot be null");
/*  464:     */     }
/*  465:1160 */     for (int index = 0; index < headerName.length(); index++)
/*  466:     */     {
/*  467:1162 */       char character = headerName.charAt(index);
/*  468:1165 */       if (character > '') {
/*  469:1166 */         throw new IllegalArgumentException("Header name cannot contain non-ASCII characters: " + headerName);
/*  470:     */       }
/*  471:1171 */       switch (character)
/*  472:     */       {
/*  473:     */       case '\t': 
/*  474:     */       case '\n': 
/*  475:     */       case '\013': 
/*  476:     */       case '\f': 
/*  477:     */       case '\r': 
/*  478:     */       case ' ': 
/*  479:     */       case ',': 
/*  480:     */       case ':': 
/*  481:     */       case ';': 
/*  482:     */       case '=': 
/*  483:1174 */         throw new IllegalArgumentException("Header name cannot contain the following prohibited characters: =,;: \\t\\r\\n\\v\\f: " + headerName);
/*  484:     */       }
/*  485:     */     }
/*  486:     */   }
/*  487:     */   
/*  488:     */   static void validateHeaderValue(CharSequence headerValue)
/*  489:     */   {
/*  490:1188 */     if (headerValue == null) {
/*  491:1189 */       throw new NullPointerException("Header values cannot be null");
/*  492:     */     }
/*  493:1201 */     int state = 0;
/*  494:1205 */     for (int index = 0; index < headerValue.length(); index++)
/*  495:     */     {
/*  496:1206 */       char character = headerValue.charAt(index);
/*  497:1209 */       switch (character)
/*  498:     */       {
/*  499:     */       case '\013': 
/*  500:1211 */         throw new IllegalArgumentException("Header value contains a prohibited character '\\v': " + headerValue);
/*  501:     */       case '\f': 
/*  502:1214 */         throw new IllegalArgumentException("Header value contains a prohibited character '\\f': " + headerValue);
/*  503:     */       }
/*  504:1219 */       switch (state)
/*  505:     */       {
/*  506:     */       case 0: 
/*  507:1221 */         switch (character)
/*  508:     */         {
/*  509:     */         case '\r': 
/*  510:1223 */           state = 1;
/*  511:1224 */           break;
/*  512:     */         case '\n': 
/*  513:1226 */           state = 2;
/*  514:     */         }
/*  515:1229 */         break;
/*  516:     */       case 1: 
/*  517:1231 */         switch (character)
/*  518:     */         {
/*  519:     */         case '\n': 
/*  520:1233 */           state = 2;
/*  521:1234 */           break;
/*  522:     */         default: 
/*  523:1236 */           throw new IllegalArgumentException("Only '\\n' is allowed after '\\r': " + headerValue);
/*  524:     */         }
/*  525:     */         break;
/*  526:     */       case 2: 
/*  527:1241 */         switch (character)
/*  528:     */         {
/*  529:     */         case '\t': 
/*  530:     */         case ' ': 
/*  531:1243 */           state = 0;
/*  532:1244 */           break;
/*  533:     */         default: 
/*  534:1246 */           throw new IllegalArgumentException("Only ' ' and '\\t' are allowed after '\\n': " + headerValue);
/*  535:     */         }
/*  536:     */         break;
/*  537:     */       }
/*  538:     */     }
/*  539:1252 */     if (state != 0) {
/*  540:1253 */       throw new IllegalArgumentException("Header value must not end with '\\r' or '\\n':" + headerValue);
/*  541:     */     }
/*  542:     */   }
/*  543:     */   
/*  544:     */   public static boolean isTransferEncodingChunked(HttpMessage message)
/*  545:     */   {
/*  546:1265 */     return message.headers().contains(TRANSFER_ENCODING_ENTITY, CHUNKED_ENTITY, true);
/*  547:     */   }
/*  548:     */   
/*  549:     */   public static void removeTransferEncodingChunked(HttpMessage m)
/*  550:     */   {
/*  551:1269 */     List<String> values = m.headers().getAll(TRANSFER_ENCODING_ENTITY);
/*  552:1270 */     if (values.isEmpty()) {
/*  553:1271 */       return;
/*  554:     */     }
/*  555:1273 */     Iterator<String> valuesIt = values.iterator();
/*  556:1274 */     while (valuesIt.hasNext())
/*  557:     */     {
/*  558:1275 */       String value = (String)valuesIt.next();
/*  559:1276 */       if (equalsIgnoreCase(value, CHUNKED_ENTITY)) {
/*  560:1277 */         valuesIt.remove();
/*  561:     */       }
/*  562:     */     }
/*  563:1280 */     if (values.isEmpty()) {
/*  564:1281 */       m.headers().remove(TRANSFER_ENCODING_ENTITY);
/*  565:     */     } else {
/*  566:1283 */       m.headers().set(TRANSFER_ENCODING_ENTITY, values);
/*  567:     */     }
/*  568:     */   }
/*  569:     */   
/*  570:     */   public static void setTransferEncodingChunked(HttpMessage m)
/*  571:     */   {
/*  572:1288 */     addHeader(m, TRANSFER_ENCODING_ENTITY, CHUNKED_ENTITY);
/*  573:1289 */     removeHeader(m, CONTENT_LENGTH_ENTITY);
/*  574:     */   }
/*  575:     */   
/*  576:     */   public static boolean isContentLengthSet(HttpMessage m)
/*  577:     */   {
/*  578:1293 */     return m.headers().contains(CONTENT_LENGTH_ENTITY);
/*  579:     */   }
/*  580:     */   
/*  581:     */   public static boolean equalsIgnoreCase(CharSequence name1, CharSequence name2)
/*  582:     */   {
/*  583:1301 */     if (name1 == name2) {
/*  584:1302 */       return true;
/*  585:     */     }
/*  586:1305 */     if ((name1 == null) || (name2 == null)) {
/*  587:1306 */       return false;
/*  588:     */     }
/*  589:1309 */     int nameLen = name1.length();
/*  590:1310 */     if (nameLen != name2.length()) {
/*  591:1311 */       return false;
/*  592:     */     }
/*  593:1314 */     for (int i = nameLen - 1; i >= 0; i--)
/*  594:     */     {
/*  595:1315 */       char c1 = name1.charAt(i);
/*  596:1316 */       char c2 = name2.charAt(i);
/*  597:1317 */       if (c1 != c2)
/*  598:     */       {
/*  599:1318 */         if ((c1 >= 'A') && (c1 <= 'Z')) {
/*  600:1319 */           c1 = (char)(c1 + ' ');
/*  601:     */         }
/*  602:1321 */         if ((c2 >= 'A') && (c2 <= 'Z')) {
/*  603:1322 */           c2 = (char)(c2 + ' ');
/*  604:     */         }
/*  605:1324 */         if (c1 != c2) {
/*  606:1325 */           return false;
/*  607:     */         }
/*  608:     */       }
/*  609:     */     }
/*  610:1329 */     return true;
/*  611:     */   }
/*  612:     */   
/*  613:     */   static int hash(CharSequence name)
/*  614:     */   {
/*  615:1333 */     if ((name instanceof HttpHeaderEntity)) {
/*  616:1334 */       return ((HttpHeaderEntity)name).hash();
/*  617:     */     }
/*  618:1336 */     int h = 0;
/*  619:1337 */     for (int i = name.length() - 1; i >= 0; i--)
/*  620:     */     {
/*  621:1338 */       char c = name.charAt(i);
/*  622:1339 */       if ((c >= 'A') && (c <= 'Z')) {
/*  623:1340 */         c = (char)(c + ' ');
/*  624:     */       }
/*  625:1342 */       h = 31 * h + c;
/*  626:     */     }
/*  627:1345 */     if (h > 0) {
/*  628:1346 */       return h;
/*  629:     */     }
/*  630:1347 */     if (h == -2147483648) {
/*  631:1348 */       return 2147483647;
/*  632:     */     }
/*  633:1350 */     return -h;
/*  634:     */   }
/*  635:     */   
/*  636:     */   static void encode(HttpHeaders headers, ByteBuf buf)
/*  637:     */   {
/*  638:1355 */     if ((headers instanceof DefaultHttpHeaders)) {
/*  639:1356 */       ((DefaultHttpHeaders)headers).encode(buf);
/*  640:     */     } else {
/*  641:1358 */       for (Map.Entry<String, String> header : headers) {
/*  642:1359 */         encode((CharSequence)header.getKey(), (CharSequence)header.getValue(), buf);
/*  643:     */       }
/*  644:     */     }
/*  645:     */   }
/*  646:     */   
/*  647:     */   static void encode(CharSequence key, CharSequence value, ByteBuf buf)
/*  648:     */   {
/*  649:1365 */     encodeAscii(key, buf);
/*  650:1366 */     buf.writeBytes(HEADER_SEPERATOR);
/*  651:1367 */     encodeAscii(value, buf);
/*  652:1368 */     buf.writeBytes(CRLF);
/*  653:     */   }
/*  654:     */   
/*  655:     */   public static void encodeAscii(CharSequence seq, ByteBuf buf)
/*  656:     */   {
/*  657:1372 */     if ((seq instanceof HttpHeaderEntity)) {
/*  658:1373 */       ((HttpHeaderEntity)seq).encode(buf);
/*  659:     */     } else {
/*  660:1375 */       encodeAscii0(seq, buf);
/*  661:     */     }
/*  662:     */   }
/*  663:     */   
/*  664:     */   static void encodeAscii0(CharSequence seq, ByteBuf buf)
/*  665:     */   {
/*  666:1380 */     int length = seq.length();
/*  667:1381 */     for (int i = 0; i < length; i++) {
/*  668:1382 */       buf.writeByte((byte)seq.charAt(i));
/*  669:     */     }
/*  670:     */   }
/*  671:     */   
/*  672:     */   public static CharSequence newEntity(String name)
/*  673:     */   {
/*  674:1391 */     if (name == null) {
/*  675:1392 */       throw new NullPointerException("name");
/*  676:     */     }
/*  677:1394 */     return new HttpHeaderEntity(name);
/*  678:     */   }
/*  679:     */   
/*  680:     */   public abstract String get(String paramString);
/*  681:     */   
/*  682:     */   public String get(CharSequence name)
/*  683:     */   {
/*  684:1412 */     return get(name.toString());
/*  685:     */   }
/*  686:     */   
/*  687:     */   public abstract List<String> getAll(String paramString);
/*  688:     */   
/*  689:     */   public List<String> getAll(CharSequence name)
/*  690:     */   {
/*  691:1428 */     return getAll(name.toString());
/*  692:     */   }
/*  693:     */   
/*  694:     */   public abstract List<Map.Entry<String, String>> entries();
/*  695:     */   
/*  696:     */   public abstract boolean contains(String paramString);
/*  697:     */   
/*  698:     */   public boolean contains(CharSequence name)
/*  699:     */   {
/*  700:1450 */     return contains(name.toString());
/*  701:     */   }
/*  702:     */   
/*  703:     */   public abstract boolean isEmpty();
/*  704:     */   
/*  705:     */   public abstract Set<String> names();
/*  706:     */   
/*  707:     */   public abstract HttpHeaders add(String paramString, Object paramObject);
/*  708:     */   
/*  709:     */   public HttpHeaders add(CharSequence name, Object value)
/*  710:     */   {
/*  711:1484 */     return add(name.toString(), value);
/*  712:     */   }
/*  713:     */   
/*  714:     */   public abstract HttpHeaders add(String paramString, Iterable<?> paramIterable);
/*  715:     */   
/*  716:     */   public HttpHeaders add(CharSequence name, Iterable<?> values)
/*  717:     */   {
/*  718:1510 */     return add(name.toString(), values);
/*  719:     */   }
/*  720:     */   
/*  721:     */   public HttpHeaders add(HttpHeaders headers)
/*  722:     */   {
/*  723:1519 */     if (headers == null) {
/*  724:1520 */       throw new NullPointerException("headers");
/*  725:     */     }
/*  726:1522 */     for (Map.Entry<String, String> e : headers) {
/*  727:1523 */       add((String)e.getKey(), e.getValue());
/*  728:     */     }
/*  729:1525 */     return this;
/*  730:     */   }
/*  731:     */   
/*  732:     */   public abstract HttpHeaders set(String paramString, Object paramObject);
/*  733:     */   
/*  734:     */   public HttpHeaders set(CharSequence name, Object value)
/*  735:     */   {
/*  736:1547 */     return set(name.toString(), value);
/*  737:     */   }
/*  738:     */   
/*  739:     */   public abstract HttpHeaders set(String paramString, Iterable<?> paramIterable);
/*  740:     */   
/*  741:     */   public HttpHeaders set(CharSequence name, Iterable<?> values)
/*  742:     */   {
/*  743:1575 */     return set(name.toString(), values);
/*  744:     */   }
/*  745:     */   
/*  746:     */   public HttpHeaders set(HttpHeaders headers)
/*  747:     */   {
/*  748:1584 */     if (headers == null) {
/*  749:1585 */       throw new NullPointerException("headers");
/*  750:     */     }
/*  751:1587 */     clear();
/*  752:1588 */     for (Map.Entry<String, String> e : headers) {
/*  753:1589 */       add((String)e.getKey(), e.getValue());
/*  754:     */     }
/*  755:1591 */     return this;
/*  756:     */   }
/*  757:     */   
/*  758:     */   public abstract HttpHeaders remove(String paramString);
/*  759:     */   
/*  760:     */   public HttpHeaders remove(CharSequence name)
/*  761:     */   {
/*  762:1606 */     return remove(name.toString());
/*  763:     */   }
/*  764:     */   
/*  765:     */   public abstract HttpHeaders clear();
/*  766:     */   
/*  767:     */   public boolean contains(String name, String value, boolean ignoreCaseValue)
/*  768:     */   {
/*  769:1620 */     List<String> values = getAll(name);
/*  770:1621 */     if (values.isEmpty()) {
/*  771:1622 */       return false;
/*  772:     */     }
/*  773:1625 */     for (String v : values) {
/*  774:1626 */       if (ignoreCaseValue)
/*  775:     */       {
/*  776:1627 */         if (equalsIgnoreCase(v, value)) {
/*  777:1628 */           return true;
/*  778:     */         }
/*  779:     */       }
/*  780:1631 */       else if (v.equals(value)) {
/*  781:1632 */         return true;
/*  782:     */       }
/*  783:     */     }
/*  784:1636 */     return false;
/*  785:     */   }
/*  786:     */   
/*  787:     */   public boolean contains(CharSequence name, CharSequence value, boolean ignoreCaseValue)
/*  788:     */   {
/*  789:1648 */     return contains(name.toString(), value.toString(), ignoreCaseValue);
/*  790:     */   }
/*  791:     */   
/*  792:     */   public static final class Values
/*  793:     */   {
/*  794:     */     public static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
/*  795:     */     public static final String BASE64 = "base64";
/*  796:     */     public static final String BINARY = "binary";
/*  797:     */     public static final String BOUNDARY = "boundary";
/*  798:     */     public static final String BYTES = "bytes";
/*  799:     */     public static final String CHARSET = "charset";
/*  800:     */     public static final String CHUNKED = "chunked";
/*  801:     */     public static final String CLOSE = "close";
/*  802:     */     public static final String COMPRESS = "compress";
/*  803:     */     public static final String CONTINUE = "100-continue";
/*  804:     */     public static final String DEFLATE = "deflate";
/*  805:     */     public static final String GZIP = "gzip";
/*  806:     */     public static final String IDENTITY = "identity";
/*  807:     */     public static final String KEEP_ALIVE = "keep-alive";
/*  808:     */     public static final String MAX_AGE = "max-age";
/*  809:     */     public static final String MAX_STALE = "max-stale";
/*  810:     */     public static final String MIN_FRESH = "min-fresh";
/*  811:     */     public static final String MULTIPART_FORM_DATA = "multipart/form-data";
/*  812:     */     public static final String MUST_REVALIDATE = "must-revalidate";
/*  813:     */     public static final String NO_CACHE = "no-cache";
/*  814:     */     public static final String NO_STORE = "no-store";
/*  815:     */     public static final String NO_TRANSFORM = "no-transform";
/*  816:     */     public static final String NONE = "none";
/*  817:     */     public static final String ONLY_IF_CACHED = "only-if-cached";
/*  818:     */     public static final String PRIVATE = "private";
/*  819:     */     public static final String PROXY_REVALIDATE = "proxy-revalidate";
/*  820:     */     public static final String PUBLIC = "public";
/*  821:     */     public static final String QUOTED_PRINTABLE = "quoted-printable";
/*  822:     */     public static final String S_MAXAGE = "s-maxage";
/*  823:     */     public static final String TRAILERS = "trailers";
/*  824:     */     public static final String UPGRADE = "Upgrade";
/*  825:     */     public static final String WEBSOCKET = "WebSocket";
/*  826:     */   }
/*  827:     */   
/*  828:     */   public static final class Names
/*  829:     */   {
/*  830:     */     public static final String ACCEPT = "Accept";
/*  831:     */     public static final String ACCEPT_CHARSET = "Accept-Charset";
/*  832:     */     public static final String ACCEPT_ENCODING = "Accept-Encoding";
/*  833:     */     public static final String ACCEPT_LANGUAGE = "Accept-Language";
/*  834:     */     public static final String ACCEPT_RANGES = "Accept-Ranges";
/*  835:     */     public static final String ACCEPT_PATCH = "Accept-Patch";
/*  836:     */     public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
/*  837:     */     public static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
/*  838:     */     public static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
/*  839:     */     public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
/*  840:     */     public static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
/*  841:     */     public static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";
/*  842:     */     public static final String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";
/*  843:     */     public static final String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
/*  844:     */     public static final String AGE = "Age";
/*  845:     */     public static final String ALLOW = "Allow";
/*  846:     */     public static final String AUTHORIZATION = "Authorization";
/*  847:     */     public static final String CACHE_CONTROL = "Cache-Control";
/*  848:     */     public static final String CONNECTION = "Connection";
/*  849:     */     public static final String CONTENT_BASE = "Content-Base";
/*  850:     */     public static final String CONTENT_ENCODING = "Content-Encoding";
/*  851:     */     public static final String CONTENT_LANGUAGE = "Content-Language";
/*  852:     */     public static final String CONTENT_LENGTH = "Content-Length";
/*  853:     */     public static final String CONTENT_LOCATION = "Content-Location";
/*  854:     */     public static final String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
/*  855:     */     public static final String CONTENT_MD5 = "Content-MD5";
/*  856:     */     public static final String CONTENT_RANGE = "Content-Range";
/*  857:     */     public static final String CONTENT_TYPE = "Content-Type";
/*  858:     */     public static final String COOKIE = "Cookie";
/*  859:     */     public static final String DATE = "Date";
/*  860:     */     public static final String ETAG = "ETag";
/*  861:     */     public static final String EXPECT = "Expect";
/*  862:     */     public static final String EXPIRES = "Expires";
/*  863:     */     public static final String FROM = "From";
/*  864:     */     public static final String HOST = "Host";
/*  865:     */     public static final String IF_MATCH = "If-Match";
/*  866:     */     public static final String IF_MODIFIED_SINCE = "If-Modified-Since";
/*  867:     */     public static final String IF_NONE_MATCH = "If-None-Match";
/*  868:     */     public static final String IF_RANGE = "If-Range";
/*  869:     */     public static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
/*  870:     */     public static final String LAST_MODIFIED = "Last-Modified";
/*  871:     */     public static final String LOCATION = "Location";
/*  872:     */     public static final String MAX_FORWARDS = "Max-Forwards";
/*  873:     */     public static final String ORIGIN = "Origin";
/*  874:     */     public static final String PRAGMA = "Pragma";
/*  875:     */     public static final String PROXY_AUTHENTICATE = "Proxy-Authenticate";
/*  876:     */     public static final String PROXY_AUTHORIZATION = "Proxy-Authorization";
/*  877:     */     public static final String RANGE = "Range";
/*  878:     */     public static final String REFERER = "Referer";
/*  879:     */     public static final String RETRY_AFTER = "Retry-After";
/*  880:     */     public static final String SEC_WEBSOCKET_KEY1 = "Sec-WebSocket-Key1";
/*  881:     */     public static final String SEC_WEBSOCKET_KEY2 = "Sec-WebSocket-Key2";
/*  882:     */     public static final String SEC_WEBSOCKET_LOCATION = "Sec-WebSocket-Location";
/*  883:     */     public static final String SEC_WEBSOCKET_ORIGIN = "Sec-WebSocket-Origin";
/*  884:     */     public static final String SEC_WEBSOCKET_PROTOCOL = "Sec-WebSocket-Protocol";
/*  885:     */     public static final String SEC_WEBSOCKET_VERSION = "Sec-WebSocket-Version";
/*  886:     */     public static final String SEC_WEBSOCKET_KEY = "Sec-WebSocket-Key";
/*  887:     */     public static final String SEC_WEBSOCKET_ACCEPT = "Sec-WebSocket-Accept";
/*  888:     */     public static final String SERVER = "Server";
/*  889:     */     public static final String SET_COOKIE = "Set-Cookie";
/*  890:     */     public static final String SET_COOKIE2 = "Set-Cookie2";
/*  891:     */     public static final String TE = "TE";
/*  892:     */     public static final String TRAILER = "Trailer";
/*  893:     */     public static final String TRANSFER_ENCODING = "Transfer-Encoding";
/*  894:     */     public static final String UPGRADE = "Upgrade";
/*  895:     */     public static final String USER_AGENT = "User-Agent";
/*  896:     */     public static final String VARY = "Vary";
/*  897:     */     public static final String VIA = "Via";
/*  898:     */     public static final String WARNING = "Warning";
/*  899:     */     public static final String WEBSOCKET_LOCATION = "WebSocket-Location";
/*  900:     */     public static final String WEBSOCKET_ORIGIN = "WebSocket-Origin";
/*  901:     */     public static final String WEBSOCKET_PROTOCOL = "WebSocket-Protocol";
/*  902:     */     public static final String WWW_AUTHENTICATE = "WWW-Authenticate";
/*  903:     */   }
/*  904:     */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.HttpHeaders
 * JD-Core Version:    0.7.0.1
 */