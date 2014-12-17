/*    1:     */ package com.google.gson.stream;
/*    2:     */ 
/*    3:     */ import com.google.gson.internal.JsonReaderInternalAccess;
/*    4:     */ import com.google.gson.internal.bind.JsonTreeReader;
/*    5:     */ import java.io.Closeable;
/*    6:     */ import java.io.EOFException;
/*    7:     */ import java.io.IOException;
/*    8:     */ import java.io.Reader;
/*    9:     */ 
/*   10:     */ public class JsonReader
/*   11:     */   implements Closeable
/*   12:     */ {
/*   13: 192 */   private static final char[] NON_EXECUTE_PREFIX = ")]}'\n".toCharArray();
/*   14:     */   private static final long MIN_INCOMPLETE_INTEGER = -922337203685477580L;
/*   15:     */   private static final int PEEKED_NONE = 0;
/*   16:     */   private static final int PEEKED_BEGIN_OBJECT = 1;
/*   17:     */   private static final int PEEKED_END_OBJECT = 2;
/*   18:     */   private static final int PEEKED_BEGIN_ARRAY = 3;
/*   19:     */   private static final int PEEKED_END_ARRAY = 4;
/*   20:     */   private static final int PEEKED_TRUE = 5;
/*   21:     */   private static final int PEEKED_FALSE = 6;
/*   22:     */   private static final int PEEKED_NULL = 7;
/*   23:     */   private static final int PEEKED_SINGLE_QUOTED = 8;
/*   24:     */   private static final int PEEKED_DOUBLE_QUOTED = 9;
/*   25:     */   private static final int PEEKED_UNQUOTED = 10;
/*   26:     */   private static final int PEEKED_BUFFERED = 11;
/*   27:     */   private static final int PEEKED_SINGLE_QUOTED_NAME = 12;
/*   28:     */   private static final int PEEKED_DOUBLE_QUOTED_NAME = 13;
/*   29:     */   private static final int PEEKED_UNQUOTED_NAME = 14;
/*   30:     */   private static final int PEEKED_LONG = 15;
/*   31:     */   private static final int PEEKED_NUMBER = 16;
/*   32:     */   private static final int PEEKED_EOF = 17;
/*   33:     */   private static final int NUMBER_CHAR_NONE = 0;
/*   34:     */   private static final int NUMBER_CHAR_SIGN = 1;
/*   35:     */   private static final int NUMBER_CHAR_DIGIT = 2;
/*   36:     */   private static final int NUMBER_CHAR_DECIMAL = 3;
/*   37:     */   private static final int NUMBER_CHAR_FRACTION_DIGIT = 4;
/*   38:     */   private static final int NUMBER_CHAR_EXP_E = 5;
/*   39:     */   private static final int NUMBER_CHAR_EXP_SIGN = 6;
/*   40:     */   private static final int NUMBER_CHAR_EXP_DIGIT = 7;
/*   41:     */   private final Reader in;
/*   42: 230 */   private boolean lenient = false;
/*   43: 238 */   private final char[] buffer = new char[1024];
/*   44: 239 */   private int pos = 0;
/*   45: 240 */   private int limit = 0;
/*   46: 242 */   private int lineNumber = 0;
/*   47: 243 */   private int lineStart = 0;
/*   48: 245 */   private int peeked = 0;
/*   49:     */   private long peekedLong;
/*   50:     */   private int peekedNumberLength;
/*   51:     */   private String peekedString;
/*   52: 269 */   private int[] stack = new int[32];
/*   53: 270 */   private int stackSize = 0;
/*   54:     */   
/*   55:     */   public JsonReader(Reader in)
/*   56:     */   {
/*   57: 272 */     this.stack[(this.stackSize++)] = 6;
/*   58: 279 */     if (in == null) {
/*   59: 280 */       throw new NullPointerException("in == null");
/*   60:     */     }
/*   61: 282 */     this.in = in;
/*   62:     */   }
/*   63:     */   
/*   64:     */   public final void setLenient(boolean lenient)
/*   65:     */   {
/*   66: 315 */     this.lenient = lenient;
/*   67:     */   }
/*   68:     */   
/*   69:     */   public final boolean isLenient()
/*   70:     */   {
/*   71: 322 */     return this.lenient;
/*   72:     */   }
/*   73:     */   
/*   74:     */   public void beginArray()
/*   75:     */     throws IOException
/*   76:     */   {
/*   77: 330 */     int p = this.peeked;
/*   78: 331 */     if (p == 0) {
/*   79: 332 */       p = doPeek();
/*   80:     */     }
/*   81: 334 */     if (p == 3)
/*   82:     */     {
/*   83: 335 */       push(1);
/*   84: 336 */       this.peeked = 0;
/*   85:     */     }
/*   86:     */     else
/*   87:     */     {
/*   88: 338 */       throw new IllegalStateException("Expected BEGIN_ARRAY but was " + peek() + " at line " + getLineNumber() + " column " + getColumnNumber());
/*   89:     */     }
/*   90:     */   }
/*   91:     */   
/*   92:     */   public void endArray()
/*   93:     */     throws IOException
/*   94:     */   {
/*   95: 348 */     int p = this.peeked;
/*   96: 349 */     if (p == 0) {
/*   97: 350 */       p = doPeek();
/*   98:     */     }
/*   99: 352 */     if (p == 4)
/*  100:     */     {
/*  101: 353 */       this.stackSize -= 1;
/*  102: 354 */       this.peeked = 0;
/*  103:     */     }
/*  104:     */     else
/*  105:     */     {
/*  106: 356 */       throw new IllegalStateException("Expected END_ARRAY but was " + peek() + " at line " + getLineNumber() + " column " + getColumnNumber());
/*  107:     */     }
/*  108:     */   }
/*  109:     */   
/*  110:     */   public void beginObject()
/*  111:     */     throws IOException
/*  112:     */   {
/*  113: 366 */     int p = this.peeked;
/*  114: 367 */     if (p == 0) {
/*  115: 368 */       p = doPeek();
/*  116:     */     }
/*  117: 370 */     if (p == 1)
/*  118:     */     {
/*  119: 371 */       push(3);
/*  120: 372 */       this.peeked = 0;
/*  121:     */     }
/*  122:     */     else
/*  123:     */     {
/*  124: 374 */       throw new IllegalStateException("Expected BEGIN_OBJECT but was " + peek() + " at line " + getLineNumber() + " column " + getColumnNumber());
/*  125:     */     }
/*  126:     */   }
/*  127:     */   
/*  128:     */   public void endObject()
/*  129:     */     throws IOException
/*  130:     */   {
/*  131: 384 */     int p = this.peeked;
/*  132: 385 */     if (p == 0) {
/*  133: 386 */       p = doPeek();
/*  134:     */     }
/*  135: 388 */     if (p == 2)
/*  136:     */     {
/*  137: 389 */       this.stackSize -= 1;
/*  138: 390 */       this.peeked = 0;
/*  139:     */     }
/*  140:     */     else
/*  141:     */     {
/*  142: 392 */       throw new IllegalStateException("Expected END_OBJECT but was " + peek() + " at line " + getLineNumber() + " column " + getColumnNumber());
/*  143:     */     }
/*  144:     */   }
/*  145:     */   
/*  146:     */   public boolean hasNext()
/*  147:     */     throws IOException
/*  148:     */   {
/*  149: 401 */     int p = this.peeked;
/*  150: 402 */     if (p == 0) {
/*  151: 403 */       p = doPeek();
/*  152:     */     }
/*  153: 405 */     return (p != 2) && (p != 4);
/*  154:     */   }
/*  155:     */   
/*  156:     */   public JsonToken peek()
/*  157:     */     throws IOException
/*  158:     */   {
/*  159: 412 */     int p = this.peeked;
/*  160: 413 */     if (p == 0) {
/*  161: 414 */       p = doPeek();
/*  162:     */     }
/*  163: 417 */     switch (p)
/*  164:     */     {
/*  165:     */     case 1: 
/*  166: 419 */       return JsonToken.BEGIN_OBJECT;
/*  167:     */     case 2: 
/*  168: 421 */       return JsonToken.END_OBJECT;
/*  169:     */     case 3: 
/*  170: 423 */       return JsonToken.BEGIN_ARRAY;
/*  171:     */     case 4: 
/*  172: 425 */       return JsonToken.END_ARRAY;
/*  173:     */     case 12: 
/*  174:     */     case 13: 
/*  175:     */     case 14: 
/*  176: 429 */       return JsonToken.NAME;
/*  177:     */     case 5: 
/*  178:     */     case 6: 
/*  179: 432 */       return JsonToken.BOOLEAN;
/*  180:     */     case 7: 
/*  181: 434 */       return JsonToken.NULL;
/*  182:     */     case 8: 
/*  183:     */     case 9: 
/*  184:     */     case 10: 
/*  185:     */     case 11: 
/*  186: 439 */       return JsonToken.STRING;
/*  187:     */     case 15: 
/*  188:     */     case 16: 
/*  189: 442 */       return JsonToken.NUMBER;
/*  190:     */     case 17: 
/*  191: 444 */       return JsonToken.END_DOCUMENT;
/*  192:     */     }
/*  193: 446 */     throw new AssertionError();
/*  194:     */   }
/*  195:     */   
/*  196:     */   private int doPeek()
/*  197:     */     throws IOException
/*  198:     */   {
/*  199: 451 */     int peekStack = this.stack[(this.stackSize - 1)];
/*  200: 452 */     if (peekStack == 1)
/*  201:     */     {
/*  202: 453 */       this.stack[(this.stackSize - 1)] = 2;
/*  203:     */     }
/*  204: 454 */     else if (peekStack == 2)
/*  205:     */     {
/*  206: 456 */       int c = nextNonWhitespace(true);
/*  207: 457 */       switch (c)
/*  208:     */       {
/*  209:     */       case 93: 
/*  210: 459 */         return this.peeked = 4;
/*  211:     */       case 59: 
/*  212: 461 */         checkLenient();
/*  213:     */       case 44: 
/*  214:     */         break;
/*  215:     */       default: 
/*  216: 465 */         throw syntaxError("Unterminated array");
/*  217:     */       }
/*  218:     */     }
/*  219:     */     else
/*  220:     */     {
/*  221: 467 */       if ((peekStack == 3) || (peekStack == 5))
/*  222:     */       {
/*  223: 468 */         this.stack[(this.stackSize - 1)] = 4;
/*  224: 470 */         if (peekStack == 5)
/*  225:     */         {
/*  226: 471 */           int c = nextNonWhitespace(true);
/*  227: 472 */           switch (c)
/*  228:     */           {
/*  229:     */           case 125: 
/*  230: 474 */             return this.peeked = 2;
/*  231:     */           case 59: 
/*  232: 476 */             checkLenient();
/*  233:     */           case 44: 
/*  234:     */             break;
/*  235:     */           default: 
/*  236: 480 */             throw syntaxError("Unterminated object");
/*  237:     */           }
/*  238:     */         }
/*  239: 483 */         int c = nextNonWhitespace(true);
/*  240: 484 */         switch (c)
/*  241:     */         {
/*  242:     */         case 34: 
/*  243: 486 */           return this.peeked = 13;
/*  244:     */         case 39: 
/*  245: 488 */           checkLenient();
/*  246: 489 */           return this.peeked = 12;
/*  247:     */         case 125: 
/*  248: 491 */           if (peekStack != 5) {
/*  249: 492 */             return this.peeked = 2;
/*  250:     */           }
/*  251: 494 */           throw syntaxError("Expected name");
/*  252:     */         }
/*  253: 497 */         checkLenient();
/*  254: 498 */         this.pos -= 1;
/*  255: 499 */         if (isLiteral((char)c)) {
/*  256: 500 */           return this.peeked = 14;
/*  257:     */         }
/*  258: 502 */         throw syntaxError("Expected name");
/*  259:     */       }
/*  260: 505 */       if (peekStack == 4)
/*  261:     */       {
/*  262: 506 */         this.stack[(this.stackSize - 1)] = 5;
/*  263:     */         
/*  264: 508 */         int c = nextNonWhitespace(true);
/*  265: 509 */         switch (c)
/*  266:     */         {
/*  267:     */         case 58: 
/*  268:     */           break;
/*  269:     */         case 61: 
/*  270: 513 */           checkLenient();
/*  271: 514 */           if (((this.pos < this.limit) || (fillBuffer(1))) && (this.buffer[this.pos] == '>')) {
/*  272: 515 */             this.pos += 1;
/*  273:     */           }
/*  274:     */           break;
/*  275:     */         default: 
/*  276: 519 */           throw syntaxError("Expected ':'");
/*  277:     */         }
/*  278:     */       }
/*  279: 521 */       else if (peekStack == 6)
/*  280:     */       {
/*  281: 522 */         if (this.lenient) {
/*  282: 523 */           consumeNonExecutePrefix();
/*  283:     */         }
/*  284: 525 */         this.stack[(this.stackSize - 1)] = 7;
/*  285:     */       }
/*  286: 526 */       else if (peekStack == 7)
/*  287:     */       {
/*  288: 527 */         int c = nextNonWhitespace(false);
/*  289: 528 */         if (c == -1) {
/*  290: 529 */           return this.peeked = 17;
/*  291:     */         }
/*  292: 531 */         checkLenient();
/*  293: 532 */         this.pos -= 1;
/*  294:     */       }
/*  295: 534 */       else if (peekStack == 8)
/*  296:     */       {
/*  297: 535 */         throw new IllegalStateException("JsonReader is closed");
/*  298:     */       }
/*  299:     */     }
/*  300: 538 */     int c = nextNonWhitespace(true);
/*  301: 539 */     switch (c)
/*  302:     */     {
/*  303:     */     case 93: 
/*  304: 541 */       if (peekStack == 1) {
/*  305: 542 */         return this.peeked = 4;
/*  306:     */       }
/*  307:     */     case 44: 
/*  308:     */     case 59: 
/*  309: 548 */       if ((peekStack == 1) || (peekStack == 2))
/*  310:     */       {
/*  311: 549 */         checkLenient();
/*  312: 550 */         this.pos -= 1;
/*  313: 551 */         return this.peeked = 7;
/*  314:     */       }
/*  315: 553 */       throw syntaxError("Unexpected value");
/*  316:     */     case 39: 
/*  317: 556 */       checkLenient();
/*  318: 557 */       return this.peeked = 8;
/*  319:     */     case 34: 
/*  320: 559 */       if (this.stackSize == 1) {
/*  321: 560 */         checkLenient();
/*  322:     */       }
/*  323: 562 */       return this.peeked = 9;
/*  324:     */     case 91: 
/*  325: 564 */       return this.peeked = 3;
/*  326:     */     case 123: 
/*  327: 566 */       return this.peeked = 1;
/*  328:     */     }
/*  329: 568 */     this.pos -= 1;
/*  330: 571 */     if (this.stackSize == 1) {
/*  331: 572 */       checkLenient();
/*  332:     */     }
/*  333: 575 */     int result = peekKeyword();
/*  334: 576 */     if (result != 0) {
/*  335: 577 */       return result;
/*  336:     */     }
/*  337: 580 */     result = peekNumber();
/*  338: 581 */     if (result != 0) {
/*  339: 582 */       return result;
/*  340:     */     }
/*  341: 585 */     if (!isLiteral(this.buffer[this.pos])) {
/*  342: 586 */       throw syntaxError("Expected value");
/*  343:     */     }
/*  344: 589 */     checkLenient();
/*  345: 590 */     return this.peeked = 10;
/*  346:     */   }
/*  347:     */   
/*  348:     */   private int peekKeyword()
/*  349:     */     throws IOException
/*  350:     */   {
/*  351: 595 */     char c = this.buffer[this.pos];
/*  352:     */     int peeking;
/*  353: 599 */     if ((c == 't') || (c == 'T'))
/*  354:     */     {
/*  355: 600 */       String keyword = "true";
/*  356: 601 */       String keywordUpper = "TRUE";
/*  357: 602 */       peeking = 5;
/*  358:     */     }
/*  359:     */     else
/*  360:     */     {
/*  361:     */       int peeking;
/*  362: 603 */       if ((c == 'f') || (c == 'F'))
/*  363:     */       {
/*  364: 604 */         String keyword = "false";
/*  365: 605 */         String keywordUpper = "FALSE";
/*  366: 606 */         peeking = 6;
/*  367:     */       }
/*  368:     */       else
/*  369:     */       {
/*  370:     */         int peeking;
/*  371: 607 */         if ((c == 'n') || (c == 'N'))
/*  372:     */         {
/*  373: 608 */           String keyword = "null";
/*  374: 609 */           String keywordUpper = "NULL";
/*  375: 610 */           peeking = 7;
/*  376:     */         }
/*  377:     */         else
/*  378:     */         {
/*  379: 612 */           return 0;
/*  380:     */         }
/*  381:     */       }
/*  382:     */     }
/*  383:     */     int peeking;
/*  384:     */     String keywordUpper;
/*  385:     */     String keyword;
/*  386: 616 */     int length = keyword.length();
/*  387: 617 */     for (int i = 1; i < length; i++)
/*  388:     */     {
/*  389: 618 */       if ((this.pos + i >= this.limit) && (!fillBuffer(i + 1))) {
/*  390: 619 */         return 0;
/*  391:     */       }
/*  392: 621 */       c = this.buffer[(this.pos + i)];
/*  393: 622 */       if ((c != keyword.charAt(i)) && (c != keywordUpper.charAt(i))) {
/*  394: 623 */         return 0;
/*  395:     */       }
/*  396:     */     }
/*  397: 627 */     if (((this.pos + length < this.limit) || (fillBuffer(length + 1))) && (isLiteral(this.buffer[(this.pos + length)]))) {
/*  398: 629 */       return 0;
/*  399:     */     }
/*  400: 633 */     this.pos += length;
/*  401: 634 */     return this.peeked = peeking;
/*  402:     */   }
/*  403:     */   
/*  404:     */   private int peekNumber()
/*  405:     */     throws IOException
/*  406:     */   {
/*  407: 639 */     char[] buffer = this.buffer;
/*  408: 640 */     int p = this.pos;
/*  409: 641 */     int l = this.limit;
/*  410:     */     
/*  411: 643 */     long value = 0L;
/*  412: 644 */     boolean negative = false;
/*  413: 645 */     boolean fitsInLong = true;
/*  414: 646 */     int last = 0;
/*  415:     */     
/*  416: 648 */     int i = 0;
/*  417: 651 */     for (;; i++)
/*  418:     */     {
/*  419: 652 */       if (p + i == l)
/*  420:     */       {
/*  421: 653 */         if (i == buffer.length) {
/*  422: 656 */           return 0;
/*  423:     */         }
/*  424: 658 */         if (!fillBuffer(i + 1)) {
/*  425:     */           break;
/*  426:     */         }
/*  427: 661 */         p = this.pos;
/*  428: 662 */         l = this.limit;
/*  429:     */       }
/*  430: 665 */       char c = buffer[(p + i)];
/*  431: 666 */       switch (c)
/*  432:     */       {
/*  433:     */       case '-': 
/*  434: 668 */         if (last == 0)
/*  435:     */         {
/*  436: 669 */           negative = true;
/*  437: 670 */           last = 1;
/*  438:     */         }
/*  439: 672 */         else if (last == 5)
/*  440:     */         {
/*  441: 673 */           last = 6;
/*  442:     */         }
/*  443:     */         else
/*  444:     */         {
/*  445: 676 */           return 0;
/*  446:     */         }
/*  447:     */         break;
/*  448:     */       case '+': 
/*  449: 679 */         if (last == 5) {
/*  450: 680 */           last = 6;
/*  451:     */         } else {
/*  452: 683 */           return 0;
/*  453:     */         }
/*  454:     */         break;
/*  455:     */       case 'E': 
/*  456:     */       case 'e': 
/*  457: 687 */         if ((last == 2) || (last == 4)) {
/*  458: 688 */           last = 5;
/*  459:     */         } else {
/*  460: 691 */           return 0;
/*  461:     */         }
/*  462:     */         break;
/*  463:     */       case '.': 
/*  464: 694 */         if (last == 2) {
/*  465: 695 */           last = 3;
/*  466:     */         } else {
/*  467: 698 */           return 0;
/*  468:     */         }
/*  469:     */         break;
/*  470:     */       default: 
/*  471: 701 */         if ((c < '0') || (c > '9'))
/*  472:     */         {
/*  473: 702 */           if (!isLiteral(c)) {
/*  474:     */             break label372;
/*  475:     */           }
/*  476: 705 */           return 0;
/*  477:     */         }
/*  478: 707 */         if ((last == 1) || (last == 0))
/*  479:     */         {
/*  480: 708 */           value = -(c - '0');
/*  481: 709 */           last = 2;
/*  482:     */         }
/*  483: 710 */         else if (last == 2)
/*  484:     */         {
/*  485: 711 */           if (value == 0L) {
/*  486: 712 */             return 0;
/*  487:     */           }
/*  488: 714 */           long newValue = value * 10L - (c - '0');
/*  489: 715 */           fitsInLong &= ((value > -922337203685477580L) || ((value == -922337203685477580L) && (newValue < value)));
/*  490:     */           
/*  491: 717 */           value = newValue;
/*  492:     */         }
/*  493: 718 */         else if (last == 3)
/*  494:     */         {
/*  495: 719 */           last = 4;
/*  496:     */         }
/*  497: 720 */         else if ((last == 5) || (last == 6))
/*  498:     */         {
/*  499: 721 */           last = 7;
/*  500:     */         }
/*  501:     */         break;
/*  502:     */       }
/*  503:     */     }
/*  504:     */     label372:
/*  505: 727 */     if ((last == 2) && (fitsInLong) && ((value != -9223372036854775808L) || (negative)))
/*  506:     */     {
/*  507: 728 */       this.peekedLong = (negative ? value : -value);
/*  508: 729 */       this.pos += i;
/*  509: 730 */       return this.peeked = 15;
/*  510:     */     }
/*  511: 731 */     if ((last == 2) || (last == 4) || (last == 7))
/*  512:     */     {
/*  513: 733 */       this.peekedNumberLength = i;
/*  514: 734 */       return this.peeked = 16;
/*  515:     */     }
/*  516: 736 */     return 0;
/*  517:     */   }
/*  518:     */   
/*  519:     */   private boolean isLiteral(char c)
/*  520:     */     throws IOException
/*  521:     */   {
/*  522: 741 */     switch (c)
/*  523:     */     {
/*  524:     */     case '#': 
/*  525:     */     case '/': 
/*  526:     */     case ';': 
/*  527:     */     case '=': 
/*  528:     */     case '\\': 
/*  529: 747 */       checkLenient();
/*  530:     */     case '\t': 
/*  531:     */     case '\n': 
/*  532:     */     case '\f': 
/*  533:     */     case '\r': 
/*  534:     */     case ' ': 
/*  535:     */     case ',': 
/*  536:     */     case ':': 
/*  537:     */     case '[': 
/*  538:     */     case ']': 
/*  539:     */     case '{': 
/*  540:     */     case '}': 
/*  541: 759 */       return false;
/*  542:     */     }
/*  543: 761 */     return true;
/*  544:     */   }
/*  545:     */   
/*  546:     */   public String nextName()
/*  547:     */     throws IOException
/*  548:     */   {
/*  549: 773 */     int p = this.peeked;
/*  550: 774 */     if (p == 0) {
/*  551: 775 */       p = doPeek();
/*  552:     */     }
/*  553:     */     String result;
/*  554: 778 */     if (p == 14)
/*  555:     */     {
/*  556: 779 */       result = nextUnquotedValue();
/*  557:     */     }
/*  558:     */     else
/*  559:     */     {
/*  560:     */       String result;
/*  561: 780 */       if (p == 12)
/*  562:     */       {
/*  563: 781 */         result = nextQuotedValue('\'');
/*  564:     */       }
/*  565:     */       else
/*  566:     */       {
/*  567:     */         String result;
/*  568: 782 */         if (p == 13) {
/*  569: 783 */           result = nextQuotedValue('"');
/*  570:     */         } else {
/*  571: 785 */           throw new IllegalStateException("Expected a name but was " + peek() + " at line " + getLineNumber() + " column " + getColumnNumber());
/*  572:     */         }
/*  573:     */       }
/*  574:     */     }
/*  575:     */     String result;
/*  576: 788 */     this.peeked = 0;
/*  577: 789 */     return result;
/*  578:     */   }
/*  579:     */   
/*  580:     */   public String nextString()
/*  581:     */     throws IOException
/*  582:     */   {
/*  583: 801 */     int p = this.peeked;
/*  584: 802 */     if (p == 0) {
/*  585: 803 */       p = doPeek();
/*  586:     */     }
/*  587:     */     String result;
/*  588: 806 */     if (p == 10)
/*  589:     */     {
/*  590: 807 */       result = nextUnquotedValue();
/*  591:     */     }
/*  592:     */     else
/*  593:     */     {
/*  594:     */       String result;
/*  595: 808 */       if (p == 8)
/*  596:     */       {
/*  597: 809 */         result = nextQuotedValue('\'');
/*  598:     */       }
/*  599:     */       else
/*  600:     */       {
/*  601:     */         String result;
/*  602: 810 */         if (p == 9)
/*  603:     */         {
/*  604: 811 */           result = nextQuotedValue('"');
/*  605:     */         }
/*  606: 812 */         else if (p == 11)
/*  607:     */         {
/*  608: 813 */           String result = this.peekedString;
/*  609: 814 */           this.peekedString = null;
/*  610:     */         }
/*  611:     */         else
/*  612:     */         {
/*  613:     */           String result;
/*  614: 815 */           if (p == 15)
/*  615:     */           {
/*  616: 816 */             result = Long.toString(this.peekedLong);
/*  617:     */           }
/*  618: 817 */           else if (p == 16)
/*  619:     */           {
/*  620: 818 */             String result = new String(this.buffer, this.pos, this.peekedNumberLength);
/*  621: 819 */             this.pos += this.peekedNumberLength;
/*  622:     */           }
/*  623:     */           else
/*  624:     */           {
/*  625: 821 */             throw new IllegalStateException("Expected a string but was " + peek() + " at line " + getLineNumber() + " column " + getColumnNumber());
/*  626:     */           }
/*  627:     */         }
/*  628:     */       }
/*  629:     */     }
/*  630:     */     String result;
/*  631: 824 */     this.peeked = 0;
/*  632: 825 */     return result;
/*  633:     */   }
/*  634:     */   
/*  635:     */   public boolean nextBoolean()
/*  636:     */     throws IOException
/*  637:     */   {
/*  638: 836 */     int p = this.peeked;
/*  639: 837 */     if (p == 0) {
/*  640: 838 */       p = doPeek();
/*  641:     */     }
/*  642: 840 */     if (p == 5)
/*  643:     */     {
/*  644: 841 */       this.peeked = 0;
/*  645: 842 */       return true;
/*  646:     */     }
/*  647: 843 */     if (p == 6)
/*  648:     */     {
/*  649: 844 */       this.peeked = 0;
/*  650: 845 */       return false;
/*  651:     */     }
/*  652: 847 */     throw new IllegalStateException("Expected a boolean but was " + peek() + " at line " + getLineNumber() + " column " + getColumnNumber());
/*  653:     */   }
/*  654:     */   
/*  655:     */   public void nextNull()
/*  656:     */     throws IOException
/*  657:     */   {
/*  658: 859 */     int p = this.peeked;
/*  659: 860 */     if (p == 0) {
/*  660: 861 */       p = doPeek();
/*  661:     */     }
/*  662: 863 */     if (p == 7) {
/*  663: 864 */       this.peeked = 0;
/*  664:     */     } else {
/*  665: 866 */       throw new IllegalStateException("Expected null but was " + peek() + " at line " + getLineNumber() + " column " + getColumnNumber());
/*  666:     */     }
/*  667:     */   }
/*  668:     */   
/*  669:     */   public double nextDouble()
/*  670:     */     throws IOException
/*  671:     */   {
/*  672: 881 */     int p = this.peeked;
/*  673: 882 */     if (p == 0) {
/*  674: 883 */       p = doPeek();
/*  675:     */     }
/*  676: 886 */     if (p == 15)
/*  677:     */     {
/*  678: 887 */       this.peeked = 0;
/*  679: 888 */       return this.peekedLong;
/*  680:     */     }
/*  681: 891 */     if (p == 16)
/*  682:     */     {
/*  683: 892 */       this.peekedString = new String(this.buffer, this.pos, this.peekedNumberLength);
/*  684: 893 */       this.pos += this.peekedNumberLength;
/*  685:     */     }
/*  686: 894 */     else if ((p == 8) || (p == 9))
/*  687:     */     {
/*  688: 895 */       this.peekedString = nextQuotedValue(p == 8 ? '\'' : '"');
/*  689:     */     }
/*  690: 896 */     else if (p == 10)
/*  691:     */     {
/*  692: 897 */       this.peekedString = nextUnquotedValue();
/*  693:     */     }
/*  694: 898 */     else if (p != 11)
/*  695:     */     {
/*  696: 899 */       throw new IllegalStateException("Expected a double but was " + peek() + " at line " + getLineNumber() + " column " + getColumnNumber());
/*  697:     */     }
/*  698: 903 */     this.peeked = 11;
/*  699: 904 */     double result = Double.parseDouble(this.peekedString);
/*  700: 905 */     if ((!this.lenient) && ((Double.isNaN(result)) || (Double.isInfinite(result)))) {
/*  701: 906 */       throw new MalformedJsonException("JSON forbids NaN and infinities: " + result + " at line " + getLineNumber() + " column " + getColumnNumber());
/*  702:     */     }
/*  703: 909 */     this.peekedString = null;
/*  704: 910 */     this.peeked = 0;
/*  705: 911 */     return result;
/*  706:     */   }
/*  707:     */   
/*  708:     */   public long nextLong()
/*  709:     */     throws IOException
/*  710:     */   {
/*  711: 925 */     int p = this.peeked;
/*  712: 926 */     if (p == 0) {
/*  713: 927 */       p = doPeek();
/*  714:     */     }
/*  715: 930 */     if (p == 15)
/*  716:     */     {
/*  717: 931 */       this.peeked = 0;
/*  718: 932 */       return this.peekedLong;
/*  719:     */     }
/*  720: 935 */     if (p == 16)
/*  721:     */     {
/*  722: 936 */       this.peekedString = new String(this.buffer, this.pos, this.peekedNumberLength);
/*  723: 937 */       this.pos += this.peekedNumberLength;
/*  724:     */     }
/*  725: 938 */     else if ((p == 8) || (p == 9))
/*  726:     */     {
/*  727: 939 */       this.peekedString = nextQuotedValue(p == 8 ? '\'' : '"');
/*  728:     */       try
/*  729:     */       {
/*  730: 941 */         long result = Long.parseLong(this.peekedString);
/*  731: 942 */         this.peeked = 0;
/*  732: 943 */         return result;
/*  733:     */       }
/*  734:     */       catch (NumberFormatException ignored) {}
/*  735:     */     }
/*  736:     */     else
/*  737:     */     {
/*  738: 948 */       throw new IllegalStateException("Expected a long but was " + peek() + " at line " + getLineNumber() + " column " + getColumnNumber());
/*  739:     */     }
/*  740: 952 */     this.peeked = 11;
/*  741: 953 */     double asDouble = Double.parseDouble(this.peekedString);
/*  742: 954 */     long result = asDouble;
/*  743: 955 */     if (result != asDouble) {
/*  744: 956 */       throw new NumberFormatException("Expected a long but was " + this.peekedString + " at line " + getLineNumber() + " column " + getColumnNumber());
/*  745:     */     }
/*  746: 959 */     this.peekedString = null;
/*  747: 960 */     this.peeked = 0;
/*  748: 961 */     return result;
/*  749:     */   }
/*  750:     */   
/*  751:     */   private String nextQuotedValue(char quote)
/*  752:     */     throws IOException
/*  753:     */   {
/*  754: 976 */     char[] buffer = this.buffer;
/*  755: 977 */     StringBuilder builder = new StringBuilder();
/*  756:     */     for (;;)
/*  757:     */     {
/*  758: 979 */       int p = this.pos;
/*  759: 980 */       int l = this.limit;
/*  760:     */       
/*  761: 982 */       int start = p;
/*  762: 983 */       while (p < l)
/*  763:     */       {
/*  764: 984 */         int c = buffer[(p++)];
/*  765: 986 */         if (c == quote)
/*  766:     */         {
/*  767: 987 */           this.pos = p;
/*  768: 988 */           builder.append(buffer, start, p - start - 1);
/*  769: 989 */           return builder.toString();
/*  770:     */         }
/*  771: 990 */         if (c == 92)
/*  772:     */         {
/*  773: 991 */           this.pos = p;
/*  774: 992 */           builder.append(buffer, start, p - start - 1);
/*  775: 993 */           builder.append(readEscapeCharacter());
/*  776: 994 */           p = this.pos;
/*  777: 995 */           l = this.limit;
/*  778: 996 */           start = p;
/*  779:     */         }
/*  780: 997 */         else if (c == 10)
/*  781:     */         {
/*  782: 998 */           this.lineNumber += 1;
/*  783: 999 */           this.lineStart = p;
/*  784:     */         }
/*  785:     */       }
/*  786:1003 */       builder.append(buffer, start, p - start);
/*  787:1004 */       this.pos = p;
/*  788:1005 */       if (!fillBuffer(1)) {
/*  789:1006 */         throw syntaxError("Unterminated string");
/*  790:     */       }
/*  791:     */     }
/*  792:     */   }
/*  793:     */   
/*  794:     */   private String nextUnquotedValue()
/*  795:     */     throws IOException
/*  796:     */   {
/*  797:1016 */     StringBuilder builder = null;
/*  798:1017 */     int i = 0;
/*  799:     */     for (;;)
/*  800:     */     {
/*  801:1021 */       if (this.pos + i < this.limit)
/*  802:     */       {
/*  803:1022 */         switch (this.buffer[(this.pos + i)])
/*  804:     */         {
/*  805:     */         case '#': 
/*  806:     */         case '/': 
/*  807:     */         case ';': 
/*  808:     */         case '=': 
/*  809:     */         case '\\': 
/*  810:1028 */           checkLenient();
/*  811:     */         case '\t': 
/*  812:     */         case '\n': 
/*  813:     */         case '\f': 
/*  814:     */         case '\r': 
/*  815:     */         case ' ': 
/*  816:     */         case ',': 
/*  817:     */         case ':': 
/*  818:     */         case '[': 
/*  819:     */         case ']': 
/*  820:     */         case '{': 
/*  821:     */         case '}': 
/*  822:     */           break;
/*  823:     */         default: 
/*  824:1021 */           i++; break;
/*  825:     */         }
/*  826:     */       }
/*  827:1045 */       else if (i < this.buffer.length)
/*  828:     */       {
/*  829:1046 */         if (!fillBuffer(i + 1)) {
/*  830:     */           break;
/*  831:     */         }
/*  832:     */       }
/*  833:     */       else
/*  834:     */       {
/*  835:1054 */         if (builder == null) {
/*  836:1055 */           builder = new StringBuilder();
/*  837:     */         }
/*  838:1057 */         builder.append(this.buffer, this.pos, i);
/*  839:1058 */         this.pos += i;
/*  840:1059 */         i = 0;
/*  841:1060 */         if (!fillBuffer(1)) {
/*  842:     */           break;
/*  843:     */         }
/*  844:     */       }
/*  845:     */     }
/*  846:     */     String result;
/*  847:     */     String result;
/*  848:1066 */     if (builder == null)
/*  849:     */     {
/*  850:1067 */       result = new String(this.buffer, this.pos, i);
/*  851:     */     }
/*  852:     */     else
/*  853:     */     {
/*  854:1069 */       builder.append(this.buffer, this.pos, i);
/*  855:1070 */       result = builder.toString();
/*  856:     */     }
/*  857:1072 */     this.pos += i;
/*  858:1073 */     return result;
/*  859:     */   }
/*  860:     */   
/*  861:     */   private void skipQuotedValue(char quote)
/*  862:     */     throws IOException
/*  863:     */   {
/*  864:1078 */     char[] buffer = this.buffer;
/*  865:     */     do
/*  866:     */     {
/*  867:1080 */       int p = this.pos;
/*  868:1081 */       int l = this.limit;
/*  869:1083 */       while (p < l)
/*  870:     */       {
/*  871:1084 */         int c = buffer[(p++)];
/*  872:1085 */         if (c == quote)
/*  873:     */         {
/*  874:1086 */           this.pos = p;
/*  875:1087 */           return;
/*  876:     */         }
/*  877:1088 */         if (c == 92)
/*  878:     */         {
/*  879:1089 */           this.pos = p;
/*  880:1090 */           readEscapeCharacter();
/*  881:1091 */           p = this.pos;
/*  882:1092 */           l = this.limit;
/*  883:     */         }
/*  884:1093 */         else if (c == 10)
/*  885:     */         {
/*  886:1094 */           this.lineNumber += 1;
/*  887:1095 */           this.lineStart = p;
/*  888:     */         }
/*  889:     */       }
/*  890:1098 */       this.pos = p;
/*  891:1099 */     } while (fillBuffer(1));
/*  892:1100 */     throw syntaxError("Unterminated string");
/*  893:     */   }
/*  894:     */   
/*  895:     */   private void skipUnquotedValue()
/*  896:     */     throws IOException
/*  897:     */   {
/*  898:     */     do
/*  899:     */     {
/*  900:1105 */       for (int i = 0; this.pos + i < this.limit; i++) {
/*  901:1107 */         switch (this.buffer[(this.pos + i)])
/*  902:     */         {
/*  903:     */         case '#': 
/*  904:     */         case '/': 
/*  905:     */         case ';': 
/*  906:     */         case '=': 
/*  907:     */         case '\\': 
/*  908:1113 */           checkLenient();
/*  909:     */         case '\t': 
/*  910:     */         case '\n': 
/*  911:     */         case '\f': 
/*  912:     */         case '\r': 
/*  913:     */         case ' ': 
/*  914:     */         case ',': 
/*  915:     */         case ':': 
/*  916:     */         case '[': 
/*  917:     */         case ']': 
/*  918:     */         case '{': 
/*  919:     */         case '}': 
/*  920:1125 */           this.pos += i;
/*  921:1126 */           return;
/*  922:     */         }
/*  923:     */       }
/*  924:1129 */       this.pos += i;
/*  925:1130 */     } while (fillBuffer(1));
/*  926:     */   }
/*  927:     */   
/*  928:     */   public int nextInt()
/*  929:     */     throws IOException
/*  930:     */   {
/*  931:1144 */     int p = this.peeked;
/*  932:1145 */     if (p == 0) {
/*  933:1146 */       p = doPeek();
/*  934:     */     }
/*  935:1150 */     if (p == 15)
/*  936:     */     {
/*  937:1151 */       int result = (int)this.peekedLong;
/*  938:1152 */       if (this.peekedLong != result) {
/*  939:1153 */         throw new NumberFormatException("Expected an int but was " + this.peekedLong + " at line " + getLineNumber() + " column " + getColumnNumber());
/*  940:     */       }
/*  941:1156 */       this.peeked = 0;
/*  942:1157 */       return result;
/*  943:     */     }
/*  944:1160 */     if (p == 16)
/*  945:     */     {
/*  946:1161 */       this.peekedString = new String(this.buffer, this.pos, this.peekedNumberLength);
/*  947:1162 */       this.pos += this.peekedNumberLength;
/*  948:     */     }
/*  949:1163 */     else if ((p == 8) || (p == 9))
/*  950:     */     {
/*  951:1164 */       this.peekedString = nextQuotedValue(p == 8 ? '\'' : '"');
/*  952:     */       try
/*  953:     */       {
/*  954:1166 */         result = Integer.parseInt(this.peekedString);
/*  955:1167 */         this.peeked = 0;
/*  956:1168 */         return result;
/*  957:     */       }
/*  958:     */       catch (NumberFormatException ignored) {}
/*  959:     */     }
/*  960:     */     else
/*  961:     */     {
/*  962:1173 */       throw new IllegalStateException("Expected an int but was " + peek() + " at line " + getLineNumber() + " column " + getColumnNumber());
/*  963:     */     }
/*  964:1177 */     this.peeked = 11;
/*  965:1178 */     double asDouble = Double.parseDouble(this.peekedString);
/*  966:1179 */     int result = (int)asDouble;
/*  967:1180 */     if (result != asDouble) {
/*  968:1181 */       throw new NumberFormatException("Expected an int but was " + this.peekedString + " at line " + getLineNumber() + " column " + getColumnNumber());
/*  969:     */     }
/*  970:1184 */     this.peekedString = null;
/*  971:1185 */     this.peeked = 0;
/*  972:1186 */     return result;
/*  973:     */   }
/*  974:     */   
/*  975:     */   public void close()
/*  976:     */     throws IOException
/*  977:     */   {
/*  978:1193 */     this.peeked = 0;
/*  979:1194 */     this.stack[0] = 8;
/*  980:1195 */     this.stackSize = 1;
/*  981:1196 */     this.in.close();
/*  982:     */   }
/*  983:     */   
/*  984:     */   public void skipValue()
/*  985:     */     throws IOException
/*  986:     */   {
/*  987:1205 */     int count = 0;
/*  988:     */     do
/*  989:     */     {
/*  990:1207 */       int p = this.peeked;
/*  991:1208 */       if (p == 0) {
/*  992:1209 */         p = doPeek();
/*  993:     */       }
/*  994:1212 */       if (p == 3)
/*  995:     */       {
/*  996:1213 */         push(1);
/*  997:1214 */         count++;
/*  998:     */       }
/*  999:1215 */       else if (p == 1)
/* 1000:     */       {
/* 1001:1216 */         push(3);
/* 1002:1217 */         count++;
/* 1003:     */       }
/* 1004:1218 */       else if (p == 4)
/* 1005:     */       {
/* 1006:1219 */         this.stackSize -= 1;
/* 1007:1220 */         count--;
/* 1008:     */       }
/* 1009:1221 */       else if (p == 2)
/* 1010:     */       {
/* 1011:1222 */         this.stackSize -= 1;
/* 1012:1223 */         count--;
/* 1013:     */       }
/* 1014:1224 */       else if ((p == 14) || (p == 10))
/* 1015:     */       {
/* 1016:1225 */         skipUnquotedValue();
/* 1017:     */       }
/* 1018:1226 */       else if ((p == 8) || (p == 12))
/* 1019:     */       {
/* 1020:1227 */         skipQuotedValue('\'');
/* 1021:     */       }
/* 1022:1228 */       else if ((p == 9) || (p == 13))
/* 1023:     */       {
/* 1024:1229 */         skipQuotedValue('"');
/* 1025:     */       }
/* 1026:1230 */       else if (p == 16)
/* 1027:     */       {
/* 1028:1231 */         this.pos += this.peekedNumberLength;
/* 1029:     */       }
/* 1030:1233 */       this.peeked = 0;
/* 1031:1234 */     } while (count != 0);
/* 1032:     */   }
/* 1033:     */   
/* 1034:     */   private void push(int newTop)
/* 1035:     */   {
/* 1036:1238 */     if (this.stackSize == this.stack.length)
/* 1037:     */     {
/* 1038:1239 */       int[] newStack = new int[this.stackSize * 2];
/* 1039:1240 */       System.arraycopy(this.stack, 0, newStack, 0, this.stackSize);
/* 1040:1241 */       this.stack = newStack;
/* 1041:     */     }
/* 1042:1243 */     this.stack[(this.stackSize++)] = newTop;
/* 1043:     */   }
/* 1044:     */   
/* 1045:     */   private boolean fillBuffer(int minimum)
/* 1046:     */     throws IOException
/* 1047:     */   {
/* 1048:1252 */     char[] buffer = this.buffer;
/* 1049:1253 */     this.lineStart -= this.pos;
/* 1050:1254 */     if (this.limit != this.pos)
/* 1051:     */     {
/* 1052:1255 */       this.limit -= this.pos;
/* 1053:1256 */       System.arraycopy(buffer, this.pos, buffer, 0, this.limit);
/* 1054:     */     }
/* 1055:     */     else
/* 1056:     */     {
/* 1057:1258 */       this.limit = 0;
/* 1058:     */     }
/* 1059:1261 */     this.pos = 0;
/* 1060:     */     int total;
/* 1061:1263 */     while ((total = this.in.read(buffer, this.limit, buffer.length - this.limit)) != -1)
/* 1062:     */     {
/* 1063:1264 */       this.limit += total;
/* 1064:1267 */       if ((this.lineNumber == 0) && (this.lineStart == 0) && (this.limit > 0) && (buffer[0] == 65279))
/* 1065:     */       {
/* 1066:1268 */         this.pos += 1;
/* 1067:1269 */         this.lineStart += 1;
/* 1068:1270 */         minimum++;
/* 1069:     */       }
/* 1070:1273 */       if (this.limit >= minimum) {
/* 1071:1274 */         return true;
/* 1072:     */       }
/* 1073:     */     }
/* 1074:1277 */     return false;
/* 1075:     */   }
/* 1076:     */   
/* 1077:     */   private int getLineNumber()
/* 1078:     */   {
/* 1079:1281 */     return this.lineNumber + 1;
/* 1080:     */   }
/* 1081:     */   
/* 1082:     */   private int getColumnNumber()
/* 1083:     */   {
/* 1084:1285 */     return this.pos - this.lineStart + 1;
/* 1085:     */   }
/* 1086:     */   
/* 1087:     */   private int nextNonWhitespace(boolean throwOnEof)
/* 1088:     */     throws IOException
/* 1089:     */   {
/* 1090:1303 */     char[] buffer = this.buffer;
/* 1091:1304 */     int p = this.pos;
/* 1092:1305 */     int l = this.limit;
/* 1093:     */     for (;;)
/* 1094:     */     {
/* 1095:1307 */       if (p == l)
/* 1096:     */       {
/* 1097:1308 */         this.pos = p;
/* 1098:1309 */         if (!fillBuffer(1)) {
/* 1099:     */           break;
/* 1100:     */         }
/* 1101:1312 */         p = this.pos;
/* 1102:1313 */         l = this.limit;
/* 1103:     */       }
/* 1104:1316 */       int c = buffer[(p++)];
/* 1105:1317 */       if (c == 10)
/* 1106:     */       {
/* 1107:1318 */         this.lineNumber += 1;
/* 1108:1319 */         this.lineStart = p;
/* 1109:     */       }
/* 1110:1321 */       else if ((c != 32) && (c != 13) && (c != 9))
/* 1111:     */       {
/* 1112:1325 */         if (c == 47)
/* 1113:     */         {
/* 1114:1326 */           this.pos = p;
/* 1115:1327 */           if (p == l)
/* 1116:     */           {
/* 1117:1328 */             this.pos -= 1;
/* 1118:1329 */             boolean charsLoaded = fillBuffer(2);
/* 1119:1330 */             this.pos += 1;
/* 1120:1331 */             if (!charsLoaded) {
/* 1121:1332 */               return c;
/* 1122:     */             }
/* 1123:     */           }
/* 1124:1336 */           checkLenient();
/* 1125:1337 */           char peek = buffer[this.pos];
/* 1126:1338 */           switch (peek)
/* 1127:     */           {
/* 1128:     */           case '*': 
/* 1129:1341 */             this.pos += 1;
/* 1130:1342 */             if (!skipTo("*/")) {
/* 1131:1343 */               throw syntaxError("Unterminated comment");
/* 1132:     */             }
/* 1133:1345 */             p = this.pos + 2;
/* 1134:1346 */             l = this.limit;
/* 1135:1347 */             break;
/* 1136:     */           case '/': 
/* 1137:1351 */             this.pos += 1;
/* 1138:1352 */             skipToEndOfLine();
/* 1139:1353 */             p = this.pos;
/* 1140:1354 */             l = this.limit;
/* 1141:1355 */             break;
/* 1142:     */           default: 
/* 1143:1358 */             return c;
/* 1144:     */           }
/* 1145:     */         }
/* 1146:1360 */         else if (c == 35)
/* 1147:     */         {
/* 1148:1361 */           this.pos = p;
/* 1149:     */           
/* 1150:     */ 
/* 1151:     */ 
/* 1152:     */ 
/* 1153:     */ 
/* 1154:1367 */           checkLenient();
/* 1155:1368 */           skipToEndOfLine();
/* 1156:1369 */           p = this.pos;
/* 1157:1370 */           l = this.limit;
/* 1158:     */         }
/* 1159:     */         else
/* 1160:     */         {
/* 1161:1372 */           this.pos = p;
/* 1162:1373 */           return c;
/* 1163:     */         }
/* 1164:     */       }
/* 1165:     */     }
/* 1166:1376 */     if (throwOnEof) {
/* 1167:1377 */       throw new EOFException("End of input at line " + getLineNumber() + " column " + getColumnNumber());
/* 1168:     */     }
/* 1169:1380 */     return -1;
/* 1170:     */   }
/* 1171:     */   
/* 1172:     */   private void checkLenient()
/* 1173:     */     throws IOException
/* 1174:     */   {
/* 1175:1385 */     if (!this.lenient) {
/* 1176:1386 */       throw syntaxError("Use JsonReader.setLenient(true) to accept malformed JSON");
/* 1177:     */     }
/* 1178:     */   }
/* 1179:     */   
/* 1180:     */   private void skipToEndOfLine()
/* 1181:     */     throws IOException
/* 1182:     */   {
/* 1183:1396 */     while ((this.pos < this.limit) || (fillBuffer(1)))
/* 1184:     */     {
/* 1185:1397 */       char c = this.buffer[(this.pos++)];
/* 1186:1398 */       if (c == '\n')
/* 1187:     */       {
/* 1188:1399 */         this.lineNumber += 1;
/* 1189:1400 */         this.lineStart = this.pos;
/* 1190:     */       }
/* 1191:     */       else
/* 1192:     */       {
/* 1193:1402 */         if (c == '\r') {
/* 1194:     */           break;
/* 1195:     */         }
/* 1196:     */       }
/* 1197:     */     }
/* 1198:     */   }
/* 1199:     */   
/* 1200:     */   private boolean skipTo(String toFind)
/* 1201:     */     throws IOException
/* 1202:     */   {
/* 1203:     */     label104:
/* 1204:1413 */     for (; (this.pos + toFind.length() <= this.limit) || (fillBuffer(toFind.length())); this.pos += 1) {
/* 1205:1414 */       if (this.buffer[this.pos] == '\n')
/* 1206:     */       {
/* 1207:1415 */         this.lineNumber += 1;
/* 1208:1416 */         this.lineStart = (this.pos + 1);
/* 1209:     */       }
/* 1210:     */       else
/* 1211:     */       {
/* 1212:1419 */         for (int c = 0; c < toFind.length(); c++) {
/* 1213:1420 */           if (this.buffer[(this.pos + c)] != toFind.charAt(c)) {
/* 1214:     */             break label104;
/* 1215:     */           }
/* 1216:     */         }
/* 1217:1424 */         return true;
/* 1218:     */       }
/* 1219:     */     }
/* 1220:1426 */     return false;
/* 1221:     */   }
/* 1222:     */   
/* 1223:     */   public String toString()
/* 1224:     */   {
/* 1225:1430 */     return getClass().getSimpleName() + " at line " + getLineNumber() + " column " + getColumnNumber();
/* 1226:     */   }
/* 1227:     */   
/* 1228:     */   private char readEscapeCharacter()
/* 1229:     */     throws IOException
/* 1230:     */   {
/* 1231:1444 */     if ((this.pos == this.limit) && (!fillBuffer(1))) {
/* 1232:1445 */       throw syntaxError("Unterminated escape sequence");
/* 1233:     */     }
/* 1234:1448 */     char escaped = this.buffer[(this.pos++)];
/* 1235:1449 */     switch (escaped)
/* 1236:     */     {
/* 1237:     */     case 'u': 
/* 1238:1451 */       if ((this.pos + 4 > this.limit) && (!fillBuffer(4))) {
/* 1239:1452 */         throw syntaxError("Unterminated escape sequence");
/* 1240:     */       }
/* 1241:1455 */       char result = '\000';
/* 1242:1456 */       int i = this.pos;
/* 1243:1456 */       for (int end = i + 4; i < end; i++)
/* 1244:     */       {
/* 1245:1457 */         char c = this.buffer[i];
/* 1246:1458 */         result = (char)(result << '\004');
/* 1247:1459 */         if ((c >= '0') && (c <= '9')) {
/* 1248:1460 */           result = (char)(result + (c - '0'));
/* 1249:1461 */         } else if ((c >= 'a') && (c <= 'f')) {
/* 1250:1462 */           result = (char)(result + (c - 'a' + 10));
/* 1251:1463 */         } else if ((c >= 'A') && (c <= 'F')) {
/* 1252:1464 */           result = (char)(result + (c - 'A' + 10));
/* 1253:     */         } else {
/* 1254:1466 */           throw new NumberFormatException("\\u" + new String(this.buffer, this.pos, 4));
/* 1255:     */         }
/* 1256:     */       }
/* 1257:1469 */       this.pos += 4;
/* 1258:1470 */       return result;
/* 1259:     */     case 't': 
/* 1260:1473 */       return '\t';
/* 1261:     */     case 'b': 
/* 1262:1476 */       return '\b';
/* 1263:     */     case 'n': 
/* 1264:1479 */       return '\n';
/* 1265:     */     case 'r': 
/* 1266:1482 */       return '\r';
/* 1267:     */     case 'f': 
/* 1268:1485 */       return '\f';
/* 1269:     */     case '\n': 
/* 1270:1488 */       this.lineNumber += 1;
/* 1271:1489 */       this.lineStart = this.pos;
/* 1272:     */     }
/* 1273:1496 */     return escaped;
/* 1274:     */   }
/* 1275:     */   
/* 1276:     */   private IOException syntaxError(String message)
/* 1277:     */     throws IOException
/* 1278:     */   {
/* 1279:1505 */     throw new MalformedJsonException(message + " at line " + getLineNumber() + " column " + getColumnNumber());
/* 1280:     */   }
/* 1281:     */   
/* 1282:     */   private void consumeNonExecutePrefix()
/* 1283:     */     throws IOException
/* 1284:     */   {
/* 1285:1514 */     nextNonWhitespace(true);
/* 1286:1515 */     this.pos -= 1;
/* 1287:1517 */     if ((this.pos + NON_EXECUTE_PREFIX.length > this.limit) && (!fillBuffer(NON_EXECUTE_PREFIX.length))) {
/* 1288:1518 */       return;
/* 1289:     */     }
/* 1290:1521 */     for (int i = 0; i < NON_EXECUTE_PREFIX.length; i++) {
/* 1291:1522 */       if (this.buffer[(this.pos + i)] != NON_EXECUTE_PREFIX[i]) {
/* 1292:1523 */         return;
/* 1293:     */       }
/* 1294:     */     }
/* 1295:1528 */     this.pos += NON_EXECUTE_PREFIX.length;
/* 1296:     */   }
/* 1297:     */   
/* 1298:     */   static
/* 1299:     */   {
/* 1300:1532 */     JsonReaderInternalAccess.INSTANCE = new JsonReaderInternalAccess()
/* 1301:     */     {
/* 1302:     */       public void promoteNameToValue(JsonReader reader)
/* 1303:     */         throws IOException
/* 1304:     */       {
/* 1305:1534 */         if ((reader instanceof JsonTreeReader))
/* 1306:     */         {
/* 1307:1535 */           ((JsonTreeReader)reader).promoteNameToValue();
/* 1308:1536 */           return;
/* 1309:     */         }
/* 1310:1538 */         int p = reader.peeked;
/* 1311:1539 */         if (p == 0) {
/* 1312:1540 */           p = reader.doPeek();
/* 1313:     */         }
/* 1314:1542 */         if (p == 13) {
/* 1315:1543 */           reader.peeked = 9;
/* 1316:1544 */         } else if (p == 12) {
/* 1317:1545 */           reader.peeked = 8;
/* 1318:1546 */         } else if (p == 14) {
/* 1319:1547 */           reader.peeked = 10;
/* 1320:     */         } else {
/* 1321:1549 */           throw new IllegalStateException("Expected a name but was " + reader.peek() + " " + " at line " + reader.getLineNumber() + " column " + reader.getColumnNumber());
/* 1322:     */         }
/* 1323:     */       }
/* 1324:     */     };
/* 1325:     */   }
/* 1326:     */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.stream.JsonReader
 * JD-Core Version:    0.7.0.1
 */