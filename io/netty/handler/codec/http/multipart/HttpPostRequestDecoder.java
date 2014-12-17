/*    1:     */ package io.netty.handler.codec.http.multipart;
/*    2:     */ 
/*    3:     */ import io.netty.buffer.ByteBuf;
/*    4:     */ import io.netty.buffer.Unpooled;
/*    5:     */ import io.netty.handler.codec.DecoderException;
/*    6:     */ import io.netty.handler.codec.http.HttpConstants;
/*    7:     */ import io.netty.handler.codec.http.HttpContent;
/*    8:     */ import io.netty.handler.codec.http.HttpHeaders;
/*    9:     */ import io.netty.handler.codec.http.HttpMethod;
/*   10:     */ import io.netty.handler.codec.http.HttpRequest;
/*   11:     */ import io.netty.handler.codec.http.LastHttpContent;
/*   12:     */ import io.netty.handler.codec.http.QueryStringDecoder;
/*   13:     */ import io.netty.util.internal.StringUtil;
/*   14:     */ import java.io.IOException;
/*   15:     */ import java.nio.charset.Charset;
/*   16:     */ import java.util.ArrayList;
/*   17:     */ import java.util.List;
/*   18:     */ import java.util.Map;
/*   19:     */ import java.util.TreeMap;
/*   20:     */ 
/*   21:     */ public class HttpPostRequestDecoder
/*   22:     */ {
/*   23:     */   private static final int DEFAULT_DISCARD_THRESHOLD = 10485760;
/*   24:     */   private final HttpDataFactory factory;
/*   25:     */   private final HttpRequest request;
/*   26:     */   private final Charset charset;
/*   27:     */   private boolean bodyToDecode;
/*   28:     */   private boolean isLastChunk;
/*   29:  78 */   private final List<InterfaceHttpData> bodyListHttpData = new ArrayList();
/*   30:  83 */   private final Map<String, List<InterfaceHttpData>> bodyMapHttpData = new TreeMap(CaseIgnoringComparator.INSTANCE);
/*   31:     */   private ByteBuf undecodedChunk;
/*   32:     */   private boolean isMultipart;
/*   33:     */   private int bodyListHttpDataRank;
/*   34:     */   private String multipartDataBoundary;
/*   35:     */   private String multipartMixedBoundary;
/*   36: 115 */   private MultiPartStatus currentStatus = MultiPartStatus.NOTSTARTED;
/*   37:     */   private Map<String, Attribute> currentFieldAttributes;
/*   38:     */   private FileUpload currentFileUpload;
/*   39:     */   private Attribute currentAttribute;
/*   40:     */   private boolean destroyed;
/*   41: 134 */   private int discardThreshold = 10485760;
/*   42:     */   
/*   43:     */   public HttpPostRequestDecoder(HttpRequest request)
/*   44:     */     throws HttpPostRequestDecoder.ErrorDataDecoderException, HttpPostRequestDecoder.IncompatibleDataDecoderException
/*   45:     */   {
/*   46: 150 */     this(new DefaultHttpDataFactory(16384L), request, HttpConstants.DEFAULT_CHARSET);
/*   47:     */   }
/*   48:     */   
/*   49:     */   public HttpPostRequestDecoder(HttpDataFactory factory, HttpRequest request)
/*   50:     */     throws HttpPostRequestDecoder.ErrorDataDecoderException, HttpPostRequestDecoder.IncompatibleDataDecoderException
/*   51:     */   {
/*   52: 169 */     this(factory, request, HttpConstants.DEFAULT_CHARSET);
/*   53:     */   }
/*   54:     */   
/*   55:     */   public HttpPostRequestDecoder(HttpDataFactory factory, HttpRequest request, Charset charset)
/*   56:     */     throws HttpPostRequestDecoder.ErrorDataDecoderException, HttpPostRequestDecoder.IncompatibleDataDecoderException
/*   57:     */   {
/*   58: 190 */     if (factory == null) {
/*   59: 191 */       throw new NullPointerException("factory");
/*   60:     */     }
/*   61: 193 */     if (request == null) {
/*   62: 194 */       throw new NullPointerException("request");
/*   63:     */     }
/*   64: 196 */     if (charset == null) {
/*   65: 197 */       throw new NullPointerException("charset");
/*   66:     */     }
/*   67: 199 */     this.request = request;
/*   68: 200 */     HttpMethod method = request.getMethod();
/*   69: 201 */     if ((method.equals(HttpMethod.POST)) || (method.equals(HttpMethod.PUT)) || (method.equals(HttpMethod.PATCH))) {
/*   70: 202 */       this.bodyToDecode = true;
/*   71:     */     }
/*   72: 204 */     this.charset = charset;
/*   73: 205 */     this.factory = factory;
/*   74:     */     
/*   75:     */ 
/*   76: 208 */     String contentType = this.request.headers().get("Content-Type");
/*   77: 209 */     if (contentType != null) {
/*   78: 210 */       checkMultipart(contentType);
/*   79:     */     } else {
/*   80: 212 */       this.isMultipart = false;
/*   81:     */     }
/*   82: 214 */     if (!this.bodyToDecode) {
/*   83: 215 */       throw new IncompatibleDataDecoderException("No Body to decode");
/*   84:     */     }
/*   85: 217 */     if ((request instanceof HttpContent))
/*   86:     */     {
/*   87: 220 */       offer((HttpContent)request);
/*   88:     */     }
/*   89:     */     else
/*   90:     */     {
/*   91: 222 */       this.undecodedChunk = Unpooled.buffer();
/*   92: 223 */       parseBody();
/*   93:     */     }
/*   94:     */   }
/*   95:     */   
/*   96:     */   private static enum MultiPartStatus
/*   97:     */   {
/*   98: 258 */     NOTSTARTED,  PREAMBLE,  HEADERDELIMITER,  DISPOSITION,  FIELD,  FILEUPLOAD,  MIXEDPREAMBLE,  MIXEDDELIMITER,  MIXEDDISPOSITION,  MIXEDFILEUPLOAD,  MIXEDCLOSEDELIMITER,  CLOSEDELIMITER,  PREEPILOGUE,  EPILOGUE;
/*   99:     */     
/*  100:     */     private MultiPartStatus() {}
/*  101:     */   }
/*  102:     */   
/*  103:     */   private void checkMultipart(String contentType)
/*  104:     */     throws HttpPostRequestDecoder.ErrorDataDecoderException
/*  105:     */   {
/*  106: 268 */     String[] headerContentType = splitHeaderContentType(contentType);
/*  107: 269 */     if ((headerContentType[0].toLowerCase().startsWith("multipart/form-data")) && (headerContentType[1].toLowerCase().startsWith("boundary")))
/*  108:     */     {
/*  109: 271 */       String[] boundary = StringUtil.split(headerContentType[1], '=');
/*  110: 272 */       if (boundary.length != 2) {
/*  111: 273 */         throw new ErrorDataDecoderException("Needs a boundary value");
/*  112:     */       }
/*  113: 275 */       if (boundary[1].charAt(0) == '"')
/*  114:     */       {
/*  115: 276 */         String bound = boundary[1].trim();
/*  116: 277 */         int index = bound.length() - 1;
/*  117: 278 */         if (bound.charAt(index) == '"') {
/*  118: 279 */           boundary[1] = bound.substring(1, index);
/*  119:     */         }
/*  120:     */       }
/*  121: 282 */       this.multipartDataBoundary = ("--" + boundary[1]);
/*  122: 283 */       this.isMultipart = true;
/*  123: 284 */       this.currentStatus = MultiPartStatus.HEADERDELIMITER;
/*  124:     */     }
/*  125:     */     else
/*  126:     */     {
/*  127: 286 */       this.isMultipart = false;
/*  128:     */     }
/*  129:     */   }
/*  130:     */   
/*  131:     */   private void checkDestroyed()
/*  132:     */   {
/*  133: 291 */     if (this.destroyed) {
/*  134: 292 */       throw new IllegalStateException(HttpPostRequestDecoder.class.getSimpleName() + " was destroyed already");
/*  135:     */     }
/*  136:     */   }
/*  137:     */   
/*  138:     */   public boolean isMultipart()
/*  139:     */   {
/*  140: 302 */     checkDestroyed();
/*  141: 303 */     return this.isMultipart;
/*  142:     */   }
/*  143:     */   
/*  144:     */   public void setDiscardThreshold(int discardThreshold)
/*  145:     */   {
/*  146: 312 */     if (discardThreshold < 0) {
/*  147: 313 */       throw new IllegalArgumentException("discardThreshold must be >= 0");
/*  148:     */     }
/*  149: 315 */     this.discardThreshold = discardThreshold;
/*  150:     */   }
/*  151:     */   
/*  152:     */   public int getDiscardThreshold()
/*  153:     */   {
/*  154: 322 */     return this.discardThreshold;
/*  155:     */   }
/*  156:     */   
/*  157:     */   public List<InterfaceHttpData> getBodyHttpDatas()
/*  158:     */     throws HttpPostRequestDecoder.NotEnoughDataDecoderException
/*  159:     */   {
/*  160: 336 */     checkDestroyed();
/*  161: 338 */     if (!this.isLastChunk) {
/*  162: 339 */       throw new NotEnoughDataDecoderException();
/*  163:     */     }
/*  164: 341 */     return this.bodyListHttpData;
/*  165:     */   }
/*  166:     */   
/*  167:     */   public List<InterfaceHttpData> getBodyHttpDatas(String name)
/*  168:     */     throws HttpPostRequestDecoder.NotEnoughDataDecoderException
/*  169:     */   {
/*  170: 356 */     checkDestroyed();
/*  171: 358 */     if (!this.isLastChunk) {
/*  172: 359 */       throw new NotEnoughDataDecoderException();
/*  173:     */     }
/*  174: 361 */     return (List)this.bodyMapHttpData.get(name);
/*  175:     */   }
/*  176:     */   
/*  177:     */   public InterfaceHttpData getBodyHttpData(String name)
/*  178:     */     throws HttpPostRequestDecoder.NotEnoughDataDecoderException
/*  179:     */   {
/*  180: 377 */     checkDestroyed();
/*  181: 379 */     if (!this.isLastChunk) {
/*  182: 380 */       throw new NotEnoughDataDecoderException();
/*  183:     */     }
/*  184: 382 */     List<InterfaceHttpData> list = (List)this.bodyMapHttpData.get(name);
/*  185: 383 */     if (list != null) {
/*  186: 384 */       return (InterfaceHttpData)list.get(0);
/*  187:     */     }
/*  188: 386 */     return null;
/*  189:     */   }
/*  190:     */   
/*  191:     */   public HttpPostRequestDecoder offer(HttpContent content)
/*  192:     */     throws HttpPostRequestDecoder.ErrorDataDecoderException
/*  193:     */   {
/*  194: 399 */     checkDestroyed();
/*  195:     */     
/*  196:     */ 
/*  197:     */ 
/*  198:     */ 
/*  199: 404 */     ByteBuf buf = content.content();
/*  200: 405 */     if (this.undecodedChunk == null) {
/*  201: 406 */       this.undecodedChunk = buf.copy();
/*  202:     */     } else {
/*  203: 408 */       this.undecodedChunk.writeBytes(buf);
/*  204:     */     }
/*  205: 410 */     if ((content instanceof LastHttpContent)) {
/*  206: 411 */       this.isLastChunk = true;
/*  207:     */     }
/*  208: 413 */     parseBody();
/*  209: 414 */     if ((this.undecodedChunk != null) && (this.undecodedChunk.writerIndex() > this.discardThreshold)) {
/*  210: 415 */       this.undecodedChunk.discardReadBytes();
/*  211:     */     }
/*  212: 417 */     return this;
/*  213:     */   }
/*  214:     */   
/*  215:     */   public boolean hasNext()
/*  216:     */     throws HttpPostRequestDecoder.EndOfDataDecoderException
/*  217:     */   {
/*  218: 431 */     checkDestroyed();
/*  219: 433 */     if (this.currentStatus == MultiPartStatus.EPILOGUE) {
/*  220: 435 */       if (this.bodyListHttpDataRank >= this.bodyListHttpData.size()) {
/*  221: 436 */         throw new EndOfDataDecoderException();
/*  222:     */       }
/*  223:     */     }
/*  224: 439 */     return (!this.bodyListHttpData.isEmpty()) && (this.bodyListHttpDataRank < this.bodyListHttpData.size());
/*  225:     */   }
/*  226:     */   
/*  227:     */   public InterfaceHttpData next()
/*  228:     */     throws HttpPostRequestDecoder.EndOfDataDecoderException
/*  229:     */   {
/*  230: 455 */     checkDestroyed();
/*  231: 457 */     if (hasNext()) {
/*  232: 458 */       return (InterfaceHttpData)this.bodyListHttpData.get(this.bodyListHttpDataRank++);
/*  233:     */     }
/*  234: 460 */     return null;
/*  235:     */   }
/*  236:     */   
/*  237:     */   private void parseBody()
/*  238:     */     throws HttpPostRequestDecoder.ErrorDataDecoderException
/*  239:     */   {
/*  240: 471 */     if ((this.currentStatus == MultiPartStatus.PREEPILOGUE) || (this.currentStatus == MultiPartStatus.EPILOGUE))
/*  241:     */     {
/*  242: 472 */       if (this.isLastChunk) {
/*  243: 473 */         this.currentStatus = MultiPartStatus.EPILOGUE;
/*  244:     */       }
/*  245: 475 */       return;
/*  246:     */     }
/*  247: 477 */     if (this.isMultipart) {
/*  248: 478 */       parseBodyMultipart();
/*  249:     */     } else {
/*  250: 480 */       parseBodyAttributes();
/*  251:     */     }
/*  252:     */   }
/*  253:     */   
/*  254:     */   protected void addHttpData(InterfaceHttpData data)
/*  255:     */   {
/*  256: 488 */     if (data == null) {
/*  257: 489 */       return;
/*  258:     */     }
/*  259: 491 */     List<InterfaceHttpData> datas = (List)this.bodyMapHttpData.get(data.getName());
/*  260: 492 */     if (datas == null)
/*  261:     */     {
/*  262: 493 */       datas = new ArrayList(1);
/*  263: 494 */       this.bodyMapHttpData.put(data.getName(), datas);
/*  264:     */     }
/*  265: 496 */     datas.add(data);
/*  266: 497 */     this.bodyListHttpData.add(data);
/*  267:     */   }
/*  268:     */   
/*  269:     */   private void parseBodyAttributesStandard()
/*  270:     */     throws HttpPostRequestDecoder.ErrorDataDecoderException
/*  271:     */   {
/*  272: 509 */     int firstpos = this.undecodedChunk.readerIndex();
/*  273: 510 */     int currentpos = firstpos;
/*  274: 513 */     if (this.currentStatus == MultiPartStatus.NOTSTARTED) {
/*  275: 514 */       this.currentStatus = MultiPartStatus.DISPOSITION;
/*  276:     */     }
/*  277: 516 */     boolean contRead = true;
/*  278:     */     try
/*  279:     */     {
/*  280:     */       int ampersandpos;
/*  281: 518 */       while ((this.undecodedChunk.isReadable()) && (contRead))
/*  282:     */       {
/*  283: 519 */         char read = (char)this.undecodedChunk.readUnsignedByte();
/*  284: 520 */         currentpos++;
/*  285:     */         int ampersandpos;
/*  286: 521 */         switch (1.$SwitchMap$io$netty$handler$codec$http$multipart$HttpPostRequestDecoder$MultiPartStatus[this.currentStatus.ordinal()])
/*  287:     */         {
/*  288:     */         case 1: 
/*  289: 523 */           if (read == '=')
/*  290:     */           {
/*  291: 524 */             this.currentStatus = MultiPartStatus.FIELD;
/*  292: 525 */             int equalpos = currentpos - 1;
/*  293: 526 */             String key = decodeAttribute(this.undecodedChunk.toString(firstpos, equalpos - firstpos, this.charset), this.charset);
/*  294:     */             
/*  295: 528 */             this.currentAttribute = this.factory.createAttribute(this.request, key);
/*  296: 529 */             firstpos = currentpos;
/*  297:     */           }
/*  298: 530 */           else if (read == '&')
/*  299:     */           {
/*  300: 531 */             this.currentStatus = MultiPartStatus.DISPOSITION;
/*  301: 532 */             ampersandpos = currentpos - 1;
/*  302: 533 */             String key = decodeAttribute(this.undecodedChunk.toString(firstpos, ampersandpos - firstpos, this.charset), this.charset);
/*  303:     */             
/*  304: 535 */             this.currentAttribute = this.factory.createAttribute(this.request, key);
/*  305: 536 */             this.currentAttribute.setValue("");
/*  306: 537 */             addHttpData(this.currentAttribute);
/*  307: 538 */             this.currentAttribute = null;
/*  308: 539 */             firstpos = currentpos;
/*  309: 540 */             contRead = true;
/*  310:     */           }
/*  311: 541 */           break;
/*  312:     */         case 2: 
/*  313: 544 */           if (read == '&')
/*  314:     */           {
/*  315: 545 */             this.currentStatus = MultiPartStatus.DISPOSITION;
/*  316: 546 */             ampersandpos = currentpos - 1;
/*  317: 547 */             setFinalBuffer(this.undecodedChunk.copy(firstpos, ampersandpos - firstpos));
/*  318: 548 */             firstpos = currentpos;
/*  319: 549 */             contRead = true;
/*  320:     */           }
/*  321: 550 */           else if (read == '\r')
/*  322:     */           {
/*  323: 551 */             if (this.undecodedChunk.isReadable())
/*  324:     */             {
/*  325: 552 */               read = (char)this.undecodedChunk.readUnsignedByte();
/*  326: 553 */               currentpos++;
/*  327: 554 */               if (read == '\n')
/*  328:     */               {
/*  329: 555 */                 this.currentStatus = MultiPartStatus.PREEPILOGUE;
/*  330: 556 */                 int ampersandpos = currentpos - 2;
/*  331: 557 */                 setFinalBuffer(this.undecodedChunk.copy(firstpos, ampersandpos - firstpos));
/*  332: 558 */                 firstpos = currentpos;
/*  333: 559 */                 contRead = false;
/*  334:     */               }
/*  335:     */               else
/*  336:     */               {
/*  337: 562 */                 throw new ErrorDataDecoderException("Bad end of line");
/*  338:     */               }
/*  339:     */             }
/*  340:     */             else
/*  341:     */             {
/*  342: 565 */               currentpos--;
/*  343:     */             }
/*  344:     */           }
/*  345: 567 */           else if (read == '\n')
/*  346:     */           {
/*  347: 568 */             this.currentStatus = MultiPartStatus.PREEPILOGUE;
/*  348: 569 */             ampersandpos = currentpos - 1;
/*  349: 570 */             setFinalBuffer(this.undecodedChunk.copy(firstpos, ampersandpos - firstpos));
/*  350: 571 */             firstpos = currentpos;
/*  351: 572 */             contRead = false;
/*  352:     */           }
/*  353:     */           break;
/*  354:     */         default: 
/*  355: 577 */           contRead = false;
/*  356:     */         }
/*  357:     */       }
/*  358: 580 */       if ((this.isLastChunk) && (this.currentAttribute != null))
/*  359:     */       {
/*  360: 582 */         ampersandpos = currentpos;
/*  361: 583 */         if (ampersandpos > firstpos) {
/*  362: 584 */           setFinalBuffer(this.undecodedChunk.copy(firstpos, ampersandpos - firstpos));
/*  363: 585 */         } else if (!this.currentAttribute.isCompleted()) {
/*  364: 586 */           setFinalBuffer(Unpooled.EMPTY_BUFFER);
/*  365:     */         }
/*  366: 588 */         firstpos = currentpos;
/*  367: 589 */         this.currentStatus = MultiPartStatus.EPILOGUE;
/*  368: 590 */         this.undecodedChunk.readerIndex(firstpos);
/*  369: 591 */         return;
/*  370:     */       }
/*  371: 593 */       if ((contRead) && (this.currentAttribute != null))
/*  372:     */       {
/*  373: 595 */         if (this.currentStatus == MultiPartStatus.FIELD)
/*  374:     */         {
/*  375: 596 */           this.currentAttribute.addContent(this.undecodedChunk.copy(firstpos, currentpos - firstpos), false);
/*  376:     */           
/*  377: 598 */           firstpos = currentpos;
/*  378:     */         }
/*  379: 600 */         this.undecodedChunk.readerIndex(firstpos);
/*  380:     */       }
/*  381:     */       else
/*  382:     */       {
/*  383: 603 */         this.undecodedChunk.readerIndex(firstpos);
/*  384:     */       }
/*  385:     */     }
/*  386:     */     catch (ErrorDataDecoderException e)
/*  387:     */     {
/*  388: 607 */       this.undecodedChunk.readerIndex(firstpos);
/*  389: 608 */       throw e;
/*  390:     */     }
/*  391:     */     catch (IOException e)
/*  392:     */     {
/*  393: 611 */       this.undecodedChunk.readerIndex(firstpos);
/*  394: 612 */       throw new ErrorDataDecoderException(e);
/*  395:     */     }
/*  396:     */   }
/*  397:     */   
/*  398:     */   private void parseBodyAttributes()
/*  399:     */     throws HttpPostRequestDecoder.ErrorDataDecoderException
/*  400:     */   {
/*  401:     */     HttpPostBodyUtil.SeekAheadOptimize sao;
/*  402:     */     try
/*  403:     */     {
/*  404: 627 */       sao = new HttpPostBodyUtil.SeekAheadOptimize(this.undecodedChunk);
/*  405:     */     }
/*  406:     */     catch (HttpPostBodyUtil.SeekAheadNoBackArrayException e1)
/*  407:     */     {
/*  408: 629 */       parseBodyAttributesStandard();
/*  409: 630 */       return;
/*  410:     */     }
/*  411: 632 */     int firstpos = this.undecodedChunk.readerIndex();
/*  412: 633 */     int currentpos = firstpos;
/*  413: 636 */     if (this.currentStatus == MultiPartStatus.NOTSTARTED) {
/*  414: 637 */       this.currentStatus = MultiPartStatus.DISPOSITION;
/*  415:     */     }
/*  416: 639 */     boolean contRead = true;
/*  417:     */     try
/*  418:     */     {
/*  419:     */       int ampersandpos;
/*  420: 641 */       while (sao.pos < sao.limit)
/*  421:     */       {
/*  422: 642 */         char read = (char)(sao.bytes[(sao.pos++)] & 0xFF);
/*  423: 643 */         currentpos++;
/*  424:     */         int ampersandpos;
/*  425: 644 */         switch (1.$SwitchMap$io$netty$handler$codec$http$multipart$HttpPostRequestDecoder$MultiPartStatus[this.currentStatus.ordinal()])
/*  426:     */         {
/*  427:     */         case 1: 
/*  428: 646 */           if (read == '=')
/*  429:     */           {
/*  430: 647 */             this.currentStatus = MultiPartStatus.FIELD;
/*  431: 648 */             int equalpos = currentpos - 1;
/*  432: 649 */             String key = decodeAttribute(this.undecodedChunk.toString(firstpos, equalpos - firstpos, this.charset), this.charset);
/*  433:     */             
/*  434: 651 */             this.currentAttribute = this.factory.createAttribute(this.request, key);
/*  435: 652 */             firstpos = currentpos;
/*  436:     */           }
/*  437: 653 */           else if (read == '&')
/*  438:     */           {
/*  439: 654 */             this.currentStatus = MultiPartStatus.DISPOSITION;
/*  440: 655 */             ampersandpos = currentpos - 1;
/*  441: 656 */             String key = decodeAttribute(this.undecodedChunk.toString(firstpos, ampersandpos - firstpos, this.charset), this.charset);
/*  442:     */             
/*  443: 658 */             this.currentAttribute = this.factory.createAttribute(this.request, key);
/*  444: 659 */             this.currentAttribute.setValue("");
/*  445: 660 */             addHttpData(this.currentAttribute);
/*  446: 661 */             this.currentAttribute = null;
/*  447: 662 */             firstpos = currentpos;
/*  448: 663 */             contRead = true;
/*  449:     */           }
/*  450: 664 */           break;
/*  451:     */         case 2: 
/*  452: 667 */           if (read == '&')
/*  453:     */           {
/*  454: 668 */             this.currentStatus = MultiPartStatus.DISPOSITION;
/*  455: 669 */             ampersandpos = currentpos - 1;
/*  456: 670 */             setFinalBuffer(this.undecodedChunk.copy(firstpos, ampersandpos - firstpos));
/*  457: 671 */             firstpos = currentpos;
/*  458: 672 */             contRead = true;
/*  459:     */           }
/*  460: 673 */           else if (read == '\r')
/*  461:     */           {
/*  462: 674 */             if (sao.pos < sao.limit)
/*  463:     */             {
/*  464: 675 */               read = (char)(sao.bytes[(sao.pos++)] & 0xFF);
/*  465: 676 */               currentpos++;
/*  466: 677 */               if (read == '\n')
/*  467:     */               {
/*  468: 678 */                 this.currentStatus = MultiPartStatus.PREEPILOGUE;
/*  469: 679 */                 int ampersandpos = currentpos - 2;
/*  470: 680 */                 sao.setReadPosition(0);
/*  471: 681 */                 setFinalBuffer(this.undecodedChunk.copy(firstpos, ampersandpos - firstpos));
/*  472: 682 */                 firstpos = currentpos;
/*  473: 683 */                 contRead = false;
/*  474:     */                 break label512;
/*  475:     */               }
/*  476: 687 */               sao.setReadPosition(0);
/*  477: 688 */               throw new ErrorDataDecoderException("Bad end of line");
/*  478:     */             }
/*  479: 691 */             if (sao.limit > 0) {
/*  480: 692 */               currentpos--;
/*  481:     */             }
/*  482:     */           }
/*  483: 695 */           else if (read == '\n')
/*  484:     */           {
/*  485: 696 */             this.currentStatus = MultiPartStatus.PREEPILOGUE;
/*  486: 697 */             ampersandpos = currentpos - 1;
/*  487: 698 */             sao.setReadPosition(0);
/*  488: 699 */             setFinalBuffer(this.undecodedChunk.copy(firstpos, ampersandpos - firstpos));
/*  489: 700 */             firstpos = currentpos;
/*  490: 701 */             contRead = false;
/*  491:     */           }
/*  492: 702 */           break;
/*  493:     */         default: 
/*  494: 707 */           sao.setReadPosition(0);
/*  495: 708 */           contRead = false;
/*  496:     */           break label512;
/*  497:     */         }
/*  498:     */       }
/*  499:     */       label512:
/*  500: 712 */       if ((this.isLastChunk) && (this.currentAttribute != null))
/*  501:     */       {
/*  502: 714 */         ampersandpos = currentpos;
/*  503: 715 */         if (ampersandpos > firstpos) {
/*  504: 716 */           setFinalBuffer(this.undecodedChunk.copy(firstpos, ampersandpos - firstpos));
/*  505: 717 */         } else if (!this.currentAttribute.isCompleted()) {
/*  506: 718 */           setFinalBuffer(Unpooled.EMPTY_BUFFER);
/*  507:     */         }
/*  508: 720 */         firstpos = currentpos;
/*  509: 721 */         this.currentStatus = MultiPartStatus.EPILOGUE;
/*  510: 722 */         this.undecodedChunk.readerIndex(firstpos);
/*  511: 723 */         return;
/*  512:     */       }
/*  513: 725 */       if ((contRead) && (this.currentAttribute != null))
/*  514:     */       {
/*  515: 727 */         if (this.currentStatus == MultiPartStatus.FIELD)
/*  516:     */         {
/*  517: 728 */           this.currentAttribute.addContent(this.undecodedChunk.copy(firstpos, currentpos - firstpos), false);
/*  518:     */           
/*  519: 730 */           firstpos = currentpos;
/*  520:     */         }
/*  521: 732 */         this.undecodedChunk.readerIndex(firstpos);
/*  522:     */       }
/*  523:     */       else
/*  524:     */       {
/*  525: 735 */         this.undecodedChunk.readerIndex(firstpos);
/*  526:     */       }
/*  527:     */     }
/*  528:     */     catch (ErrorDataDecoderException e)
/*  529:     */     {
/*  530: 739 */       this.undecodedChunk.readerIndex(firstpos);
/*  531: 740 */       throw e;
/*  532:     */     }
/*  533:     */     catch (IOException e)
/*  534:     */     {
/*  535: 743 */       this.undecodedChunk.readerIndex(firstpos);
/*  536: 744 */       throw new ErrorDataDecoderException(e);
/*  537:     */     }
/*  538:     */   }
/*  539:     */   
/*  540:     */   private void setFinalBuffer(ByteBuf buffer)
/*  541:     */     throws HttpPostRequestDecoder.ErrorDataDecoderException, IOException
/*  542:     */   {
/*  543: 749 */     this.currentAttribute.addContent(buffer, true);
/*  544: 750 */     String value = decodeAttribute(this.currentAttribute.getByteBuf().toString(this.charset), this.charset);
/*  545: 751 */     this.currentAttribute.setValue(value);
/*  546: 752 */     addHttpData(this.currentAttribute);
/*  547: 753 */     this.currentAttribute = null;
/*  548:     */   }
/*  549:     */   
/*  550:     */   private static String decodeAttribute(String s, Charset charset)
/*  551:     */     throws HttpPostRequestDecoder.ErrorDataDecoderException
/*  552:     */   {
/*  553:     */     try
/*  554:     */     {
/*  555: 763 */       return QueryStringDecoder.decodeComponent(s, charset);
/*  556:     */     }
/*  557:     */     catch (IllegalArgumentException e)
/*  558:     */     {
/*  559: 765 */       throw new ErrorDataDecoderException("Bad string: '" + s + '\'', e);
/*  560:     */     }
/*  561:     */   }
/*  562:     */   
/*  563:     */   private void parseBodyMultipart()
/*  564:     */     throws HttpPostRequestDecoder.ErrorDataDecoderException
/*  565:     */   {
/*  566: 777 */     if ((this.undecodedChunk == null) || (this.undecodedChunk.readableBytes() == 0)) {
/*  567: 779 */       return;
/*  568:     */     }
/*  569: 781 */     InterfaceHttpData data = decodeMultipart(this.currentStatus);
/*  570: 782 */     while (data != null)
/*  571:     */     {
/*  572: 783 */       addHttpData(data);
/*  573: 784 */       if ((this.currentStatus == MultiPartStatus.PREEPILOGUE) || (this.currentStatus == MultiPartStatus.EPILOGUE)) {
/*  574:     */         break;
/*  575:     */       }
/*  576: 787 */       data = decodeMultipart(this.currentStatus);
/*  577:     */     }
/*  578:     */   }
/*  579:     */   
/*  580:     */   private InterfaceHttpData decodeMultipart(MultiPartStatus state)
/*  581:     */     throws HttpPostRequestDecoder.ErrorDataDecoderException
/*  582:     */   {
/*  583: 808 */     switch (1.$SwitchMap$io$netty$handler$codec$http$multipart$HttpPostRequestDecoder$MultiPartStatus[state.ordinal()])
/*  584:     */     {
/*  585:     */     case 3: 
/*  586: 810 */       throw new ErrorDataDecoderException("Should not be called with the current getStatus");
/*  587:     */     case 4: 
/*  588: 813 */       throw new ErrorDataDecoderException("Should not be called with the current getStatus");
/*  589:     */     case 5: 
/*  590: 816 */       return findMultipartDelimiter(this.multipartDataBoundary, MultiPartStatus.DISPOSITION, MultiPartStatus.PREEPILOGUE);
/*  591:     */     case 1: 
/*  592: 829 */       return findMultipartDisposition();
/*  593:     */     case 2: 
/*  594: 833 */       Charset localCharset = null;
/*  595: 834 */       Attribute charsetAttribute = (Attribute)this.currentFieldAttributes.get("charset");
/*  596: 835 */       if (charsetAttribute != null) {
/*  597:     */         try
/*  598:     */         {
/*  599: 837 */           localCharset = Charset.forName(charsetAttribute.getValue());
/*  600:     */         }
/*  601:     */         catch (IOException e)
/*  602:     */         {
/*  603: 839 */           throw new ErrorDataDecoderException(e);
/*  604:     */         }
/*  605:     */       }
/*  606: 842 */       Attribute nameAttribute = (Attribute)this.currentFieldAttributes.get("name");
/*  607: 843 */       if (this.currentAttribute == null)
/*  608:     */       {
/*  609:     */         try
/*  610:     */         {
/*  611: 845 */           this.currentAttribute = this.factory.createAttribute(this.request, cleanString(nameAttribute.getValue()));
/*  612:     */         }
/*  613:     */         catch (NullPointerException e)
/*  614:     */         {
/*  615: 848 */           throw new ErrorDataDecoderException(e);
/*  616:     */         }
/*  617:     */         catch (IllegalArgumentException e)
/*  618:     */         {
/*  619: 850 */           throw new ErrorDataDecoderException(e);
/*  620:     */         }
/*  621:     */         catch (IOException e)
/*  622:     */         {
/*  623: 852 */           throw new ErrorDataDecoderException(e);
/*  624:     */         }
/*  625: 854 */         if (localCharset != null) {
/*  626: 855 */           this.currentAttribute.setCharset(localCharset);
/*  627:     */         }
/*  628:     */       }
/*  629:     */       try
/*  630:     */       {
/*  631: 860 */         loadFieldMultipart(this.multipartDataBoundary);
/*  632:     */       }
/*  633:     */       catch (NotEnoughDataDecoderException e)
/*  634:     */       {
/*  635: 862 */         return null;
/*  636:     */       }
/*  637: 864 */       Attribute finalAttribute = this.currentAttribute;
/*  638: 865 */       this.currentAttribute = null;
/*  639: 866 */       this.currentFieldAttributes = null;
/*  640:     */       
/*  641: 868 */       this.currentStatus = MultiPartStatus.HEADERDELIMITER;
/*  642: 869 */       return finalAttribute;
/*  643:     */     case 6: 
/*  644: 873 */       return getFileUpload(this.multipartDataBoundary);
/*  645:     */     case 7: 
/*  646: 878 */       return findMultipartDelimiter(this.multipartMixedBoundary, MultiPartStatus.MIXEDDISPOSITION, MultiPartStatus.HEADERDELIMITER);
/*  647:     */     case 8: 
/*  648: 882 */       return findMultipartDisposition();
/*  649:     */     case 9: 
/*  650: 886 */       return getFileUpload(this.multipartMixedBoundary);
/*  651:     */     case 10: 
/*  652: 889 */       return null;
/*  653:     */     case 11: 
/*  654: 891 */       return null;
/*  655:     */     }
/*  656: 893 */     throw new ErrorDataDecoderException("Shouldn't reach here.");
/*  657:     */   }
/*  658:     */   
/*  659:     */   void skipControlCharacters()
/*  660:     */     throws HttpPostRequestDecoder.NotEnoughDataDecoderException
/*  661:     */   {
/*  662:     */     HttpPostBodyUtil.SeekAheadOptimize sao;
/*  663:     */     try
/*  664:     */     {
/*  665: 905 */       sao = new HttpPostBodyUtil.SeekAheadOptimize(this.undecodedChunk);
/*  666:     */     }
/*  667:     */     catch (HttpPostBodyUtil.SeekAheadNoBackArrayException e)
/*  668:     */     {
/*  669:     */       try
/*  670:     */       {
/*  671: 908 */         skipControlCharactersStandard();
/*  672:     */       }
/*  673:     */       catch (IndexOutOfBoundsException e1)
/*  674:     */       {
/*  675: 910 */         throw new NotEnoughDataDecoderException(e1);
/*  676:     */       }
/*  677: 912 */       return;
/*  678:     */     }
/*  679: 915 */     while (sao.pos < sao.limit)
/*  680:     */     {
/*  681: 916 */       char c = (char)(sao.bytes[(sao.pos++)] & 0xFF);
/*  682: 917 */       if ((!Character.isISOControl(c)) && (!Character.isWhitespace(c)))
/*  683:     */       {
/*  684: 918 */         sao.setReadPosition(1);
/*  685: 919 */         return;
/*  686:     */       }
/*  687:     */     }
/*  688: 922 */     throw new NotEnoughDataDecoderException("Access out of bounds");
/*  689:     */   }
/*  690:     */   
/*  691:     */   void skipControlCharactersStandard()
/*  692:     */   {
/*  693:     */     for (;;)
/*  694:     */     {
/*  695: 927 */       char c = (char)this.undecodedChunk.readUnsignedByte();
/*  696: 928 */       if ((!Character.isISOControl(c)) && (!Character.isWhitespace(c)))
/*  697:     */       {
/*  698: 929 */         this.undecodedChunk.readerIndex(this.undecodedChunk.readerIndex() - 1);
/*  699: 930 */         break;
/*  700:     */       }
/*  701:     */     }
/*  702:     */   }
/*  703:     */   
/*  704:     */   private InterfaceHttpData findMultipartDelimiter(String delimiter, MultiPartStatus dispositionStatus, MultiPartStatus closeDelimiterStatus)
/*  705:     */     throws HttpPostRequestDecoder.ErrorDataDecoderException
/*  706:     */   {
/*  707: 950 */     int readerIndex = this.undecodedChunk.readerIndex();
/*  708:     */     try
/*  709:     */     {
/*  710: 952 */       skipControlCharacters();
/*  711:     */     }
/*  712:     */     catch (NotEnoughDataDecoderException e1)
/*  713:     */     {
/*  714: 954 */       this.undecodedChunk.readerIndex(readerIndex);
/*  715: 955 */       return null;
/*  716:     */     }
/*  717: 957 */     skipOneLine();
/*  718:     */     String newline;
/*  719:     */     try
/*  720:     */     {
/*  721: 960 */       newline = readDelimiter(delimiter);
/*  722:     */     }
/*  723:     */     catch (NotEnoughDataDecoderException e)
/*  724:     */     {
/*  725: 962 */       this.undecodedChunk.readerIndex(readerIndex);
/*  726: 963 */       return null;
/*  727:     */     }
/*  728: 965 */     if (newline.equals(delimiter))
/*  729:     */     {
/*  730: 966 */       this.currentStatus = dispositionStatus;
/*  731: 967 */       return decodeMultipart(dispositionStatus);
/*  732:     */     }
/*  733: 969 */     if (newline.equals(delimiter + "--"))
/*  734:     */     {
/*  735: 971 */       this.currentStatus = closeDelimiterStatus;
/*  736: 972 */       if (this.currentStatus == MultiPartStatus.HEADERDELIMITER)
/*  737:     */       {
/*  738: 975 */         this.currentFieldAttributes = null;
/*  739: 976 */         return decodeMultipart(MultiPartStatus.HEADERDELIMITER);
/*  740:     */       }
/*  741: 978 */       return null;
/*  742:     */     }
/*  743: 980 */     this.undecodedChunk.readerIndex(readerIndex);
/*  744: 981 */     throw new ErrorDataDecoderException("No Multipart delimiter found");
/*  745:     */   }
/*  746:     */   
/*  747:     */   private InterfaceHttpData findMultipartDisposition()
/*  748:     */     throws HttpPostRequestDecoder.ErrorDataDecoderException
/*  749:     */   {
/*  750: 991 */     int readerIndex = this.undecodedChunk.readerIndex();
/*  751: 992 */     if (this.currentStatus == MultiPartStatus.DISPOSITION) {
/*  752: 993 */       this.currentFieldAttributes = new TreeMap(CaseIgnoringComparator.INSTANCE);
/*  753:     */     }
/*  754: 996 */     while (!skipOneLine())
/*  755:     */     {
/*  756:     */       String newline;
/*  757:     */       try
/*  758:     */       {
/*  759: 999 */         skipControlCharacters();
/*  760:1000 */         newline = readLine();
/*  761:     */       }
/*  762:     */       catch (NotEnoughDataDecoderException e)
/*  763:     */       {
/*  764:1002 */         this.undecodedChunk.readerIndex(readerIndex);
/*  765:1003 */         return null;
/*  766:     */       }
/*  767:1005 */       String[] contents = splitMultipartHeader(newline);
/*  768:1006 */       if (contents[0].equalsIgnoreCase("Content-Disposition"))
/*  769:     */       {
/*  770:     */         boolean checkSecondArg;
/*  771:     */         boolean checkSecondArg;
/*  772:1008 */         if (this.currentStatus == MultiPartStatus.DISPOSITION) {
/*  773:1009 */           checkSecondArg = contents[1].equalsIgnoreCase("form-data");
/*  774:     */         } else {
/*  775:1011 */           checkSecondArg = (contents[1].equalsIgnoreCase("attachment")) || (contents[1].equalsIgnoreCase("file"));
/*  776:     */         }
/*  777:1014 */         if (checkSecondArg) {
/*  778:1016 */           for (int i = 2; i < contents.length; i++)
/*  779:     */           {
/*  780:1017 */             String[] values = StringUtil.split(contents[i], '=');
/*  781:     */             Attribute attribute;
/*  782:     */             try
/*  783:     */             {
/*  784:1020 */               String name = cleanString(values[0]);
/*  785:1021 */               String value = values[1];
/*  786:1024 */               if ("filename".equals(name)) {
/*  787:1026 */                 value = value.substring(1, value.length() - 1);
/*  788:     */               } else {
/*  789:1029 */                 value = cleanString(value);
/*  790:     */               }
/*  791:1031 */               attribute = this.factory.createAttribute(this.request, name, value);
/*  792:     */             }
/*  793:     */             catch (NullPointerException e)
/*  794:     */             {
/*  795:1033 */               throw new ErrorDataDecoderException(e);
/*  796:     */             }
/*  797:     */             catch (IllegalArgumentException e)
/*  798:     */             {
/*  799:1035 */               throw new ErrorDataDecoderException(e);
/*  800:     */             }
/*  801:1037 */             this.currentFieldAttributes.put(attribute.getName(), attribute);
/*  802:     */           }
/*  803:     */         }
/*  804:     */       }
/*  805:1040 */       else if (contents[0].equalsIgnoreCase("Content-Transfer-Encoding"))
/*  806:     */       {
/*  807:     */         Attribute attribute;
/*  808:     */         try
/*  809:     */         {
/*  810:1043 */           attribute = this.factory.createAttribute(this.request, "Content-Transfer-Encoding", cleanString(contents[1]));
/*  811:     */         }
/*  812:     */         catch (NullPointerException e)
/*  813:     */         {
/*  814:1046 */           throw new ErrorDataDecoderException(e);
/*  815:     */         }
/*  816:     */         catch (IllegalArgumentException e)
/*  817:     */         {
/*  818:1048 */           throw new ErrorDataDecoderException(e);
/*  819:     */         }
/*  820:1050 */         this.currentFieldAttributes.put("Content-Transfer-Encoding", attribute);
/*  821:     */       }
/*  822:1051 */       else if (contents[0].equalsIgnoreCase("Content-Length"))
/*  823:     */       {
/*  824:     */         Attribute attribute;
/*  825:     */         try
/*  826:     */         {
/*  827:1054 */           attribute = this.factory.createAttribute(this.request, "Content-Length", cleanString(contents[1]));
/*  828:     */         }
/*  829:     */         catch (NullPointerException e)
/*  830:     */         {
/*  831:1057 */           throw new ErrorDataDecoderException(e);
/*  832:     */         }
/*  833:     */         catch (IllegalArgumentException e)
/*  834:     */         {
/*  835:1059 */           throw new ErrorDataDecoderException(e);
/*  836:     */         }
/*  837:1061 */         this.currentFieldAttributes.put("Content-Length", attribute);
/*  838:     */       }
/*  839:1062 */       else if (contents[0].equalsIgnoreCase("Content-Type"))
/*  840:     */       {
/*  841:1064 */         if (contents[1].equalsIgnoreCase("multipart/mixed"))
/*  842:     */         {
/*  843:1065 */           if (this.currentStatus == MultiPartStatus.DISPOSITION)
/*  844:     */           {
/*  845:1066 */             String[] values = StringUtil.split(contents[2], '=');
/*  846:1067 */             this.multipartMixedBoundary = ("--" + values[1]);
/*  847:1068 */             this.currentStatus = MultiPartStatus.MIXEDDELIMITER;
/*  848:1069 */             return decodeMultipart(MultiPartStatus.MIXEDDELIMITER);
/*  849:     */           }
/*  850:1071 */           throw new ErrorDataDecoderException("Mixed Multipart found in a previous Mixed Multipart");
/*  851:     */         }
/*  852:1074 */         for (int i = 1; i < contents.length; i++) {
/*  853:1075 */           if (contents[i].toLowerCase().startsWith("charset"))
/*  854:     */           {
/*  855:1076 */             String[] values = StringUtil.split(contents[i], '=');
/*  856:     */             Attribute attribute;
/*  857:     */             try
/*  858:     */             {
/*  859:1079 */               attribute = this.factory.createAttribute(this.request, "charset", cleanString(values[1]));
/*  860:     */             }
/*  861:     */             catch (NullPointerException e)
/*  862:     */             {
/*  863:1082 */               throw new ErrorDataDecoderException(e);
/*  864:     */             }
/*  865:     */             catch (IllegalArgumentException e)
/*  866:     */             {
/*  867:1084 */               throw new ErrorDataDecoderException(e);
/*  868:     */             }
/*  869:1086 */             this.currentFieldAttributes.put("charset", attribute);
/*  870:     */           }
/*  871:     */           else
/*  872:     */           {
/*  873:     */             Attribute attribute;
/*  874:     */             try
/*  875:     */             {
/*  876:1090 */               attribute = this.factory.createAttribute(this.request, cleanString(contents[0]), contents[i]);
/*  877:     */             }
/*  878:     */             catch (NullPointerException e)
/*  879:     */             {
/*  880:1093 */               throw new ErrorDataDecoderException(e);
/*  881:     */             }
/*  882:     */             catch (IllegalArgumentException e)
/*  883:     */             {
/*  884:1095 */               throw new ErrorDataDecoderException(e);
/*  885:     */             }
/*  886:1097 */             this.currentFieldAttributes.put(attribute.getName(), attribute);
/*  887:     */           }
/*  888:     */         }
/*  889:     */       }
/*  890:     */       else
/*  891:     */       {
/*  892:1102 */         throw new ErrorDataDecoderException("Unknown Params: " + newline);
/*  893:     */       }
/*  894:     */     }
/*  895:1106 */     Attribute filenameAttribute = (Attribute)this.currentFieldAttributes.get("filename");
/*  896:1107 */     if (this.currentStatus == MultiPartStatus.DISPOSITION)
/*  897:     */     {
/*  898:1108 */       if (filenameAttribute != null)
/*  899:     */       {
/*  900:1110 */         this.currentStatus = MultiPartStatus.FILEUPLOAD;
/*  901:     */         
/*  902:1112 */         return decodeMultipart(MultiPartStatus.FILEUPLOAD);
/*  903:     */       }
/*  904:1115 */       this.currentStatus = MultiPartStatus.FIELD;
/*  905:     */       
/*  906:1117 */       return decodeMultipart(MultiPartStatus.FIELD);
/*  907:     */     }
/*  908:1120 */     if (filenameAttribute != null)
/*  909:     */     {
/*  910:1122 */       this.currentStatus = MultiPartStatus.MIXEDFILEUPLOAD;
/*  911:     */       
/*  912:1124 */       return decodeMultipart(MultiPartStatus.MIXEDFILEUPLOAD);
/*  913:     */     }
/*  914:1127 */     throw new ErrorDataDecoderException("Filename not found");
/*  915:     */   }
/*  916:     */   
/*  917:     */   protected InterfaceHttpData getFileUpload(String delimiter)
/*  918:     */     throws HttpPostRequestDecoder.ErrorDataDecoderException
/*  919:     */   {
/*  920:1143 */     Attribute encoding = (Attribute)this.currentFieldAttributes.get("Content-Transfer-Encoding");
/*  921:1144 */     Charset localCharset = this.charset;
/*  922:     */     
/*  923:1146 */     HttpPostBodyUtil.TransferEncodingMechanism mechanism = HttpPostBodyUtil.TransferEncodingMechanism.BIT7;
/*  924:1147 */     if (encoding != null)
/*  925:     */     {
/*  926:     */       String code;
/*  927:     */       try
/*  928:     */       {
/*  929:1150 */         code = encoding.getValue().toLowerCase();
/*  930:     */       }
/*  931:     */       catch (IOException e)
/*  932:     */       {
/*  933:1152 */         throw new ErrorDataDecoderException(e);
/*  934:     */       }
/*  935:1154 */       if (code.equals(HttpPostBodyUtil.TransferEncodingMechanism.BIT7.value()))
/*  936:     */       {
/*  937:1155 */         localCharset = HttpPostBodyUtil.US_ASCII;
/*  938:     */       }
/*  939:1156 */       else if (code.equals(HttpPostBodyUtil.TransferEncodingMechanism.BIT8.value()))
/*  940:     */       {
/*  941:1157 */         localCharset = HttpPostBodyUtil.ISO_8859_1;
/*  942:1158 */         mechanism = HttpPostBodyUtil.TransferEncodingMechanism.BIT8;
/*  943:     */       }
/*  944:1159 */       else if (code.equals(HttpPostBodyUtil.TransferEncodingMechanism.BINARY.value()))
/*  945:     */       {
/*  946:1161 */         mechanism = HttpPostBodyUtil.TransferEncodingMechanism.BINARY;
/*  947:     */       }
/*  948:     */       else
/*  949:     */       {
/*  950:1163 */         throw new ErrorDataDecoderException("TransferEncoding Unknown: " + code);
/*  951:     */       }
/*  952:     */     }
/*  953:1166 */     Attribute charsetAttribute = (Attribute)this.currentFieldAttributes.get("charset");
/*  954:1167 */     if (charsetAttribute != null) {
/*  955:     */       try
/*  956:     */       {
/*  957:1169 */         localCharset = Charset.forName(charsetAttribute.getValue());
/*  958:     */       }
/*  959:     */       catch (IOException e)
/*  960:     */       {
/*  961:1171 */         throw new ErrorDataDecoderException(e);
/*  962:     */       }
/*  963:     */     }
/*  964:1174 */     if (this.currentFileUpload == null)
/*  965:     */     {
/*  966:1175 */       Attribute filenameAttribute = (Attribute)this.currentFieldAttributes.get("filename");
/*  967:1176 */       Attribute nameAttribute = (Attribute)this.currentFieldAttributes.get("name");
/*  968:1177 */       Attribute contentTypeAttribute = (Attribute)this.currentFieldAttributes.get("Content-Type");
/*  969:1178 */       if (contentTypeAttribute == null) {
/*  970:1179 */         throw new ErrorDataDecoderException("Content-Type is absent but required");
/*  971:     */       }
/*  972:1181 */       Attribute lengthAttribute = (Attribute)this.currentFieldAttributes.get("Content-Length");
/*  973:     */       long size;
/*  974:     */       try
/*  975:     */       {
/*  976:1184 */         size = lengthAttribute != null ? Long.parseLong(lengthAttribute.getValue()) : 0L;
/*  977:     */       }
/*  978:     */       catch (IOException e)
/*  979:     */       {
/*  980:1186 */         throw new ErrorDataDecoderException(e);
/*  981:     */       }
/*  982:     */       catch (NumberFormatException e)
/*  983:     */       {
/*  984:1188 */         size = 0L;
/*  985:     */       }
/*  986:     */       try
/*  987:     */       {
/*  988:1191 */         this.currentFileUpload = this.factory.createFileUpload(this.request, cleanString(nameAttribute.getValue()), cleanString(filenameAttribute.getValue()), contentTypeAttribute.getValue(), mechanism.value(), localCharset, size);
/*  989:     */       }
/*  990:     */       catch (NullPointerException e)
/*  991:     */       {
/*  992:1196 */         throw new ErrorDataDecoderException(e);
/*  993:     */       }
/*  994:     */       catch (IllegalArgumentException e)
/*  995:     */       {
/*  996:1198 */         throw new ErrorDataDecoderException(e);
/*  997:     */       }
/*  998:     */       catch (IOException e)
/*  999:     */       {
/* 1000:1200 */         throw new ErrorDataDecoderException(e);
/* 1001:     */       }
/* 1002:     */     }
/* 1003:     */     try
/* 1004:     */     {
/* 1005:1205 */       readFileUploadByteMultipart(delimiter);
/* 1006:     */     }
/* 1007:     */     catch (NotEnoughDataDecoderException e)
/* 1008:     */     {
/* 1009:1210 */       return null;
/* 1010:     */     }
/* 1011:1212 */     if (this.currentFileUpload.isCompleted())
/* 1012:     */     {
/* 1013:1214 */       if (this.currentStatus == MultiPartStatus.FILEUPLOAD)
/* 1014:     */       {
/* 1015:1215 */         this.currentStatus = MultiPartStatus.HEADERDELIMITER;
/* 1016:1216 */         this.currentFieldAttributes = null;
/* 1017:     */       }
/* 1018:     */       else
/* 1019:     */       {
/* 1020:1218 */         this.currentStatus = MultiPartStatus.MIXEDDELIMITER;
/* 1021:1219 */         cleanMixedAttributes();
/* 1022:     */       }
/* 1023:1221 */       FileUpload fileUpload = this.currentFileUpload;
/* 1024:1222 */       this.currentFileUpload = null;
/* 1025:1223 */       return fileUpload;
/* 1026:     */     }
/* 1027:1228 */     return null;
/* 1028:     */   }
/* 1029:     */   
/* 1030:     */   public void destroy()
/* 1031:     */   {
/* 1032:1236 */     checkDestroyed();
/* 1033:1237 */     cleanFiles();
/* 1034:1238 */     this.destroyed = true;
/* 1035:1240 */     if ((this.undecodedChunk != null) && (this.undecodedChunk.refCnt() > 0))
/* 1036:     */     {
/* 1037:1241 */       this.undecodedChunk.release();
/* 1038:1242 */       this.undecodedChunk = null;
/* 1039:     */     }
/* 1040:1246 */     for (int i = this.bodyListHttpDataRank; i < this.bodyListHttpData.size(); i++) {
/* 1041:1247 */       ((InterfaceHttpData)this.bodyListHttpData.get(i)).release();
/* 1042:     */     }
/* 1043:     */   }
/* 1044:     */   
/* 1045:     */   public void cleanFiles()
/* 1046:     */   {
/* 1047:1255 */     checkDestroyed();
/* 1048:     */     
/* 1049:1257 */     this.factory.cleanRequestHttpDatas(this.request);
/* 1050:     */   }
/* 1051:     */   
/* 1052:     */   public void removeHttpDataFromClean(InterfaceHttpData data)
/* 1053:     */   {
/* 1054:1264 */     checkDestroyed();
/* 1055:     */     
/* 1056:1266 */     this.factory.removeHttpDataFromClean(this.request, data);
/* 1057:     */   }
/* 1058:     */   
/* 1059:     */   private void cleanMixedAttributes()
/* 1060:     */   {
/* 1061:1274 */     this.currentFieldAttributes.remove("charset");
/* 1062:1275 */     this.currentFieldAttributes.remove("Content-Length");
/* 1063:1276 */     this.currentFieldAttributes.remove("Content-Transfer-Encoding");
/* 1064:1277 */     this.currentFieldAttributes.remove("Content-Type");
/* 1065:1278 */     this.currentFieldAttributes.remove("filename");
/* 1066:     */   }
/* 1067:     */   
/* 1068:     */   private String readLineStandard()
/* 1069:     */     throws HttpPostRequestDecoder.NotEnoughDataDecoderException
/* 1070:     */   {
/* 1071:1290 */     int readerIndex = this.undecodedChunk.readerIndex();
/* 1072:     */     try
/* 1073:     */     {
/* 1074:1292 */       ByteBuf line = Unpooled.buffer(64);
/* 1075:1294 */       while (this.undecodedChunk.isReadable())
/* 1076:     */       {
/* 1077:1295 */         byte nextByte = this.undecodedChunk.readByte();
/* 1078:1296 */         if (nextByte == 13)
/* 1079:     */         {
/* 1080:1298 */           nextByte = this.undecodedChunk.getByte(this.undecodedChunk.readerIndex());
/* 1081:1299 */           if (nextByte == 10)
/* 1082:     */           {
/* 1083:1301 */             this.undecodedChunk.skipBytes(1);
/* 1084:1302 */             return line.toString(this.charset);
/* 1085:     */           }
/* 1086:1305 */           line.writeByte(13);
/* 1087:     */         }
/* 1088:     */         else
/* 1089:     */         {
/* 1090:1307 */           if (nextByte == 10) {
/* 1091:1308 */             return line.toString(this.charset);
/* 1092:     */           }
/* 1093:1310 */           line.writeByte(nextByte);
/* 1094:     */         }
/* 1095:     */       }
/* 1096:     */     }
/* 1097:     */     catch (IndexOutOfBoundsException e)
/* 1098:     */     {
/* 1099:1314 */       this.undecodedChunk.readerIndex(readerIndex);
/* 1100:1315 */       throw new NotEnoughDataDecoderException(e);
/* 1101:     */     }
/* 1102:1317 */     this.undecodedChunk.readerIndex(readerIndex);
/* 1103:1318 */     throw new NotEnoughDataDecoderException();
/* 1104:     */   }
/* 1105:     */   
/* 1106:     */   private String readLine()
/* 1107:     */     throws HttpPostRequestDecoder.NotEnoughDataDecoderException
/* 1108:     */   {
/* 1109:     */     HttpPostBodyUtil.SeekAheadOptimize sao;
/* 1110:     */     try
/* 1111:     */     {
/* 1112:1332 */       sao = new HttpPostBodyUtil.SeekAheadOptimize(this.undecodedChunk);
/* 1113:     */     }
/* 1114:     */     catch (HttpPostBodyUtil.SeekAheadNoBackArrayException e1)
/* 1115:     */     {
/* 1116:1334 */       return readLineStandard();
/* 1117:     */     }
/* 1118:1336 */     int readerIndex = this.undecodedChunk.readerIndex();
/* 1119:     */     try
/* 1120:     */     {
/* 1121:1338 */       ByteBuf line = Unpooled.buffer(64);
/* 1122:1340 */       while (sao.pos < sao.limit)
/* 1123:     */       {
/* 1124:1341 */         byte nextByte = sao.bytes[(sao.pos++)];
/* 1125:1342 */         if (nextByte == 13)
/* 1126:     */         {
/* 1127:1343 */           if (sao.pos < sao.limit)
/* 1128:     */           {
/* 1129:1344 */             nextByte = sao.bytes[(sao.pos++)];
/* 1130:1345 */             if (nextByte == 10)
/* 1131:     */             {
/* 1132:1346 */               sao.setReadPosition(0);
/* 1133:1347 */               return line.toString(this.charset);
/* 1134:     */             }
/* 1135:1350 */             sao.pos -= 1;
/* 1136:1351 */             line.writeByte(13);
/* 1137:     */           }
/* 1138:     */           else
/* 1139:     */           {
/* 1140:1354 */             line.writeByte(nextByte);
/* 1141:     */           }
/* 1142:     */         }
/* 1143:     */         else
/* 1144:     */         {
/* 1145:1356 */           if (nextByte == 10)
/* 1146:     */           {
/* 1147:1357 */             sao.setReadPosition(0);
/* 1148:1358 */             return line.toString(this.charset);
/* 1149:     */           }
/* 1150:1360 */           line.writeByte(nextByte);
/* 1151:     */         }
/* 1152:     */       }
/* 1153:     */     }
/* 1154:     */     catch (IndexOutOfBoundsException e)
/* 1155:     */     {
/* 1156:1364 */       this.undecodedChunk.readerIndex(readerIndex);
/* 1157:1365 */       throw new NotEnoughDataDecoderException(e);
/* 1158:     */     }
/* 1159:1367 */     this.undecodedChunk.readerIndex(readerIndex);
/* 1160:1368 */     throw new NotEnoughDataDecoderException();
/* 1161:     */   }
/* 1162:     */   
/* 1163:     */   private String readDelimiterStandard(String delimiter)
/* 1164:     */     throws HttpPostRequestDecoder.NotEnoughDataDecoderException
/* 1165:     */   {
/* 1166:1387 */     int readerIndex = this.undecodedChunk.readerIndex();
/* 1167:     */     try
/* 1168:     */     {
/* 1169:1389 */       StringBuilder sb = new StringBuilder(64);
/* 1170:1390 */       int delimiterPos = 0;
/* 1171:1391 */       int len = delimiter.length();
/* 1172:1392 */       while ((this.undecodedChunk.isReadable()) && (delimiterPos < len))
/* 1173:     */       {
/* 1174:1393 */         byte nextByte = this.undecodedChunk.readByte();
/* 1175:1394 */         if (nextByte == delimiter.charAt(delimiterPos))
/* 1176:     */         {
/* 1177:1395 */           delimiterPos++;
/* 1178:1396 */           sb.append((char)nextByte);
/* 1179:     */         }
/* 1180:     */         else
/* 1181:     */         {
/* 1182:1399 */           this.undecodedChunk.readerIndex(readerIndex);
/* 1183:1400 */           throw new NotEnoughDataDecoderException();
/* 1184:     */         }
/* 1185:     */       }
/* 1186:1404 */       if (this.undecodedChunk.isReadable())
/* 1187:     */       {
/* 1188:1405 */         byte nextByte = this.undecodedChunk.readByte();
/* 1189:1407 */         if (nextByte == 13)
/* 1190:     */         {
/* 1191:1408 */           nextByte = this.undecodedChunk.readByte();
/* 1192:1409 */           if (nextByte == 10) {
/* 1193:1410 */             return sb.toString();
/* 1194:     */           }
/* 1195:1414 */           this.undecodedChunk.readerIndex(readerIndex);
/* 1196:1415 */           throw new NotEnoughDataDecoderException();
/* 1197:     */         }
/* 1198:1417 */         if (nextByte == 10) {
/* 1199:1418 */           return sb.toString();
/* 1200:     */         }
/* 1201:1419 */         if (nextByte == 45)
/* 1202:     */         {
/* 1203:1420 */           sb.append('-');
/* 1204:     */           
/* 1205:1422 */           nextByte = this.undecodedChunk.readByte();
/* 1206:1423 */           if (nextByte == 45)
/* 1207:     */           {
/* 1208:1424 */             sb.append('-');
/* 1209:1426 */             if (this.undecodedChunk.isReadable())
/* 1210:     */             {
/* 1211:1427 */               nextByte = this.undecodedChunk.readByte();
/* 1212:1428 */               if (nextByte == 13)
/* 1213:     */               {
/* 1214:1429 */                 nextByte = this.undecodedChunk.readByte();
/* 1215:1430 */                 if (nextByte == 10) {
/* 1216:1431 */                   return sb.toString();
/* 1217:     */                 }
/* 1218:1435 */                 this.undecodedChunk.readerIndex(readerIndex);
/* 1219:1436 */                 throw new NotEnoughDataDecoderException();
/* 1220:     */               }
/* 1221:1438 */               if (nextByte == 10) {
/* 1222:1439 */                 return sb.toString();
/* 1223:     */               }
/* 1224:1444 */               this.undecodedChunk.readerIndex(this.undecodedChunk.readerIndex() - 1);
/* 1225:1445 */               return sb.toString();
/* 1226:     */             }
/* 1227:1452 */             return sb.toString();
/* 1228:     */           }
/* 1229:     */         }
/* 1230:     */       }
/* 1231:     */     }
/* 1232:     */     catch (IndexOutOfBoundsException e)
/* 1233:     */     {
/* 1234:1459 */       this.undecodedChunk.readerIndex(readerIndex);
/* 1235:1460 */       throw new NotEnoughDataDecoderException(e);
/* 1236:     */     }
/* 1237:1462 */     this.undecodedChunk.readerIndex(readerIndex);
/* 1238:1463 */     throw new NotEnoughDataDecoderException();
/* 1239:     */   }
/* 1240:     */   
/* 1241:     */   private String readDelimiter(String delimiter)
/* 1242:     */     throws HttpPostRequestDecoder.NotEnoughDataDecoderException
/* 1243:     */   {
/* 1244:     */     HttpPostBodyUtil.SeekAheadOptimize sao;
/* 1245:     */     try
/* 1246:     */     {
/* 1247:1483 */       sao = new HttpPostBodyUtil.SeekAheadOptimize(this.undecodedChunk);
/* 1248:     */     }
/* 1249:     */     catch (HttpPostBodyUtil.SeekAheadNoBackArrayException e1)
/* 1250:     */     {
/* 1251:1485 */       return readDelimiterStandard(delimiter);
/* 1252:     */     }
/* 1253:1487 */     int readerIndex = this.undecodedChunk.readerIndex();
/* 1254:1488 */     int delimiterPos = 0;
/* 1255:1489 */     int len = delimiter.length();
/* 1256:     */     try
/* 1257:     */     {
/* 1258:1491 */       StringBuilder sb = new StringBuilder(64);
/* 1259:1493 */       while ((sao.pos < sao.limit) && (delimiterPos < len))
/* 1260:     */       {
/* 1261:1494 */         byte nextByte = sao.bytes[(sao.pos++)];
/* 1262:1495 */         if (nextByte == delimiter.charAt(delimiterPos))
/* 1263:     */         {
/* 1264:1496 */           delimiterPos++;
/* 1265:1497 */           sb.append((char)nextByte);
/* 1266:     */         }
/* 1267:     */         else
/* 1268:     */         {
/* 1269:1500 */           this.undecodedChunk.readerIndex(readerIndex);
/* 1270:1501 */           throw new NotEnoughDataDecoderException();
/* 1271:     */         }
/* 1272:     */       }
/* 1273:1505 */       if (sao.pos < sao.limit)
/* 1274:     */       {
/* 1275:1506 */         byte nextByte = sao.bytes[(sao.pos++)];
/* 1276:1507 */         if (nextByte == 13)
/* 1277:     */         {
/* 1278:1509 */           if (sao.pos < sao.limit)
/* 1279:     */           {
/* 1280:1510 */             nextByte = sao.bytes[(sao.pos++)];
/* 1281:1511 */             if (nextByte == 10)
/* 1282:     */             {
/* 1283:1512 */               sao.setReadPosition(0);
/* 1284:1513 */               return sb.toString();
/* 1285:     */             }
/* 1286:1517 */             this.undecodedChunk.readerIndex(readerIndex);
/* 1287:1518 */             throw new NotEnoughDataDecoderException();
/* 1288:     */           }
/* 1289:1523 */           this.undecodedChunk.readerIndex(readerIndex);
/* 1290:1524 */           throw new NotEnoughDataDecoderException();
/* 1291:     */         }
/* 1292:1526 */         if (nextByte == 10)
/* 1293:     */         {
/* 1294:1529 */           sao.setReadPosition(0);
/* 1295:1530 */           return sb.toString();
/* 1296:     */         }
/* 1297:1531 */         if (nextByte == 45)
/* 1298:     */         {
/* 1299:1532 */           sb.append('-');
/* 1300:1534 */           if (sao.pos < sao.limit)
/* 1301:     */           {
/* 1302:1535 */             nextByte = sao.bytes[(sao.pos++)];
/* 1303:1536 */             if (nextByte == 45)
/* 1304:     */             {
/* 1305:1537 */               sb.append('-');
/* 1306:1539 */               if (sao.pos < sao.limit)
/* 1307:     */               {
/* 1308:1540 */                 nextByte = sao.bytes[(sao.pos++)];
/* 1309:1541 */                 if (nextByte == 13)
/* 1310:     */                 {
/* 1311:1542 */                   if (sao.pos < sao.limit)
/* 1312:     */                   {
/* 1313:1543 */                     nextByte = sao.bytes[(sao.pos++)];
/* 1314:1544 */                     if (nextByte == 10)
/* 1315:     */                     {
/* 1316:1545 */                       sao.setReadPosition(0);
/* 1317:1546 */                       return sb.toString();
/* 1318:     */                     }
/* 1319:1550 */                     this.undecodedChunk.readerIndex(readerIndex);
/* 1320:1551 */                     throw new NotEnoughDataDecoderException();
/* 1321:     */                   }
/* 1322:1556 */                   this.undecodedChunk.readerIndex(readerIndex);
/* 1323:1557 */                   throw new NotEnoughDataDecoderException();
/* 1324:     */                 }
/* 1325:1559 */                 if (nextByte == 10)
/* 1326:     */                 {
/* 1327:1560 */                   sao.setReadPosition(0);
/* 1328:1561 */                   return sb.toString();
/* 1329:     */                 }
/* 1330:1567 */                 sao.setReadPosition(1);
/* 1331:1568 */                 return sb.toString();
/* 1332:     */               }
/* 1333:1575 */               sao.setReadPosition(0);
/* 1334:1576 */               return sb.toString();
/* 1335:     */             }
/* 1336:     */           }
/* 1337:     */         }
/* 1338:     */       }
/* 1339:     */     }
/* 1340:     */     catch (IndexOutOfBoundsException e)
/* 1341:     */     {
/* 1342:1585 */       this.undecodedChunk.readerIndex(readerIndex);
/* 1343:1586 */       throw new NotEnoughDataDecoderException(e);
/* 1344:     */     }
/* 1345:1588 */     this.undecodedChunk.readerIndex(readerIndex);
/* 1346:1589 */     throw new NotEnoughDataDecoderException();
/* 1347:     */   }
/* 1348:     */   
/* 1349:     */   private void readFileUploadByteMultipartStandard(String delimiter)
/* 1350:     */     throws HttpPostRequestDecoder.NotEnoughDataDecoderException, HttpPostRequestDecoder.ErrorDataDecoderException
/* 1351:     */   {
/* 1352:1604 */     int readerIndex = this.undecodedChunk.readerIndex();
/* 1353:     */     
/* 1354:1606 */     boolean newLine = true;
/* 1355:1607 */     int index = 0;
/* 1356:1608 */     int lastPosition = this.undecodedChunk.readerIndex();
/* 1357:1609 */     boolean found = false;
/* 1358:1610 */     while (this.undecodedChunk.isReadable())
/* 1359:     */     {
/* 1360:1611 */       byte nextByte = this.undecodedChunk.readByte();
/* 1361:1612 */       if (newLine)
/* 1362:     */       {
/* 1363:1614 */         if (nextByte == delimiter.codePointAt(index))
/* 1364:     */         {
/* 1365:1615 */           index++;
/* 1366:1616 */           if (delimiter.length() == index)
/* 1367:     */           {
/* 1368:1617 */             found = true;
/* 1369:1618 */             break;
/* 1370:     */           }
/* 1371:     */         }
/* 1372:     */         else
/* 1373:     */         {
/* 1374:1622 */           newLine = false;
/* 1375:1623 */           index = 0;
/* 1376:1625 */           if (nextByte == 13)
/* 1377:     */           {
/* 1378:1626 */             if (this.undecodedChunk.isReadable())
/* 1379:     */             {
/* 1380:1627 */               nextByte = this.undecodedChunk.readByte();
/* 1381:1628 */               if (nextByte == 10)
/* 1382:     */               {
/* 1383:1629 */                 newLine = true;
/* 1384:1630 */                 index = 0;
/* 1385:1631 */                 lastPosition = this.undecodedChunk.readerIndex() - 2;
/* 1386:     */               }
/* 1387:     */               else
/* 1388:     */               {
/* 1389:1634 */                 lastPosition = this.undecodedChunk.readerIndex() - 1;
/* 1390:     */                 
/* 1391:     */ 
/* 1392:1637 */                 this.undecodedChunk.readerIndex(lastPosition);
/* 1393:     */               }
/* 1394:     */             }
/* 1395:     */           }
/* 1396:1640 */           else if (nextByte == 10)
/* 1397:     */           {
/* 1398:1641 */             newLine = true;
/* 1399:1642 */             index = 0;
/* 1400:1643 */             lastPosition = this.undecodedChunk.readerIndex() - 1;
/* 1401:     */           }
/* 1402:     */           else
/* 1403:     */           {
/* 1404:1646 */             lastPosition = this.undecodedChunk.readerIndex();
/* 1405:     */           }
/* 1406:     */         }
/* 1407:     */       }
/* 1408:1651 */       else if (nextByte == 13)
/* 1409:     */       {
/* 1410:1652 */         if (this.undecodedChunk.isReadable())
/* 1411:     */         {
/* 1412:1653 */           nextByte = this.undecodedChunk.readByte();
/* 1413:1654 */           if (nextByte == 10)
/* 1414:     */           {
/* 1415:1655 */             newLine = true;
/* 1416:1656 */             index = 0;
/* 1417:1657 */             lastPosition = this.undecodedChunk.readerIndex() - 2;
/* 1418:     */           }
/* 1419:     */           else
/* 1420:     */           {
/* 1421:1660 */             lastPosition = this.undecodedChunk.readerIndex() - 1;
/* 1422:     */             
/* 1423:     */ 
/* 1424:1663 */             this.undecodedChunk.readerIndex(lastPosition);
/* 1425:     */           }
/* 1426:     */         }
/* 1427:     */       }
/* 1428:1666 */       else if (nextByte == 10)
/* 1429:     */       {
/* 1430:1667 */         newLine = true;
/* 1431:1668 */         index = 0;
/* 1432:1669 */         lastPosition = this.undecodedChunk.readerIndex() - 1;
/* 1433:     */       }
/* 1434:     */       else
/* 1435:     */       {
/* 1436:1672 */         lastPosition = this.undecodedChunk.readerIndex();
/* 1437:     */       }
/* 1438:     */     }
/* 1439:1676 */     ByteBuf buffer = this.undecodedChunk.copy(readerIndex, lastPosition - readerIndex);
/* 1440:1677 */     if (found) {
/* 1441:     */       try
/* 1442:     */       {
/* 1443:1680 */         this.currentFileUpload.addContent(buffer, true);
/* 1444:     */         
/* 1445:1682 */         this.undecodedChunk.readerIndex(lastPosition);
/* 1446:     */       }
/* 1447:     */       catch (IOException e)
/* 1448:     */       {
/* 1449:1684 */         throw new ErrorDataDecoderException(e);
/* 1450:     */       }
/* 1451:     */     } else {
/* 1452:     */       try
/* 1453:     */       {
/* 1454:1690 */         this.currentFileUpload.addContent(buffer, false);
/* 1455:     */         
/* 1456:1692 */         this.undecodedChunk.readerIndex(lastPosition);
/* 1457:1693 */         throw new NotEnoughDataDecoderException();
/* 1458:     */       }
/* 1459:     */       catch (IOException e)
/* 1460:     */       {
/* 1461:1695 */         throw new ErrorDataDecoderException(e);
/* 1462:     */       }
/* 1463:     */     }
/* 1464:     */   }
/* 1465:     */   
/* 1466:     */   private void readFileUploadByteMultipart(String delimiter)
/* 1467:     */     throws HttpPostRequestDecoder.NotEnoughDataDecoderException, HttpPostRequestDecoder.ErrorDataDecoderException
/* 1468:     */   {
/* 1469:     */     HttpPostBodyUtil.SeekAheadOptimize sao;
/* 1470:     */     try
/* 1471:     */     {
/* 1472:1714 */       sao = new HttpPostBodyUtil.SeekAheadOptimize(this.undecodedChunk);
/* 1473:     */     }
/* 1474:     */     catch (HttpPostBodyUtil.SeekAheadNoBackArrayException e1)
/* 1475:     */     {
/* 1476:1716 */       readFileUploadByteMultipartStandard(delimiter);
/* 1477:1717 */       return;
/* 1478:     */     }
/* 1479:1719 */     int readerIndex = this.undecodedChunk.readerIndex();
/* 1480:     */     
/* 1481:1721 */     boolean newLine = true;
/* 1482:1722 */     int index = 0;
/* 1483:1723 */     int lastrealpos = sao.pos;
/* 1484:     */     
/* 1485:1725 */     boolean found = false;
/* 1486:1727 */     while (sao.pos < sao.limit)
/* 1487:     */     {
/* 1488:1728 */       byte nextByte = sao.bytes[(sao.pos++)];
/* 1489:1729 */       if (newLine)
/* 1490:     */       {
/* 1491:1731 */         if (nextByte == delimiter.codePointAt(index))
/* 1492:     */         {
/* 1493:1732 */           index++;
/* 1494:1733 */           if (delimiter.length() == index)
/* 1495:     */           {
/* 1496:1734 */             found = true;
/* 1497:1735 */             break;
/* 1498:     */           }
/* 1499:     */         }
/* 1500:     */         else
/* 1501:     */         {
/* 1502:1739 */           newLine = false;
/* 1503:1740 */           index = 0;
/* 1504:1742 */           if (nextByte == 13)
/* 1505:     */           {
/* 1506:1743 */             if (sao.pos < sao.limit)
/* 1507:     */             {
/* 1508:1744 */               nextByte = sao.bytes[(sao.pos++)];
/* 1509:1745 */               if (nextByte == 10)
/* 1510:     */               {
/* 1511:1746 */                 newLine = true;
/* 1512:1747 */                 index = 0;
/* 1513:1748 */                 lastrealpos = sao.pos - 2;
/* 1514:     */               }
/* 1515:     */               else
/* 1516:     */               {
/* 1517:1751 */                 sao.pos -= 1;
/* 1518:     */                 
/* 1519:     */ 
/* 1520:1754 */                 lastrealpos = sao.pos;
/* 1521:     */               }
/* 1522:     */             }
/* 1523:     */           }
/* 1524:1757 */           else if (nextByte == 10)
/* 1525:     */           {
/* 1526:1758 */             newLine = true;
/* 1527:1759 */             index = 0;
/* 1528:1760 */             lastrealpos = sao.pos - 1;
/* 1529:     */           }
/* 1530:     */           else
/* 1531:     */           {
/* 1532:1763 */             lastrealpos = sao.pos;
/* 1533:     */           }
/* 1534:     */         }
/* 1535:     */       }
/* 1536:1768 */       else if (nextByte == 13)
/* 1537:     */       {
/* 1538:1769 */         if (sao.pos < sao.limit)
/* 1539:     */         {
/* 1540:1770 */           nextByte = sao.bytes[(sao.pos++)];
/* 1541:1771 */           if (nextByte == 10)
/* 1542:     */           {
/* 1543:1772 */             newLine = true;
/* 1544:1773 */             index = 0;
/* 1545:1774 */             lastrealpos = sao.pos - 2;
/* 1546:     */           }
/* 1547:     */           else
/* 1548:     */           {
/* 1549:1777 */             sao.pos -= 1;
/* 1550:     */             
/* 1551:     */ 
/* 1552:1780 */             lastrealpos = sao.pos;
/* 1553:     */           }
/* 1554:     */         }
/* 1555:     */       }
/* 1556:1783 */       else if (nextByte == 10)
/* 1557:     */       {
/* 1558:1784 */         newLine = true;
/* 1559:1785 */         index = 0;
/* 1560:1786 */         lastrealpos = sao.pos - 1;
/* 1561:     */       }
/* 1562:     */       else
/* 1563:     */       {
/* 1564:1789 */         lastrealpos = sao.pos;
/* 1565:     */       }
/* 1566:     */     }
/* 1567:1793 */     int lastPosition = sao.getReadPosition(lastrealpos);
/* 1568:1794 */     ByteBuf buffer = this.undecodedChunk.copy(readerIndex, lastPosition - readerIndex);
/* 1569:1795 */     if (found) {
/* 1570:     */       try
/* 1571:     */       {
/* 1572:1798 */         this.currentFileUpload.addContent(buffer, true);
/* 1573:     */         
/* 1574:1800 */         this.undecodedChunk.readerIndex(lastPosition);
/* 1575:     */       }
/* 1576:     */       catch (IOException e)
/* 1577:     */       {
/* 1578:1802 */         throw new ErrorDataDecoderException(e);
/* 1579:     */       }
/* 1580:     */     } else {
/* 1581:     */       try
/* 1582:     */       {
/* 1583:1808 */         this.currentFileUpload.addContent(buffer, false);
/* 1584:     */         
/* 1585:1810 */         this.undecodedChunk.readerIndex(lastPosition);
/* 1586:1811 */         throw new NotEnoughDataDecoderException();
/* 1587:     */       }
/* 1588:     */       catch (IOException e)
/* 1589:     */       {
/* 1590:1813 */         throw new ErrorDataDecoderException(e);
/* 1591:     */       }
/* 1592:     */     }
/* 1593:     */   }
/* 1594:     */   
/* 1595:     */   private void loadFieldMultipartStandard(String delimiter)
/* 1596:     */     throws HttpPostRequestDecoder.NotEnoughDataDecoderException, HttpPostRequestDecoder.ErrorDataDecoderException
/* 1597:     */   {
/* 1598:1827 */     int readerIndex = this.undecodedChunk.readerIndex();
/* 1599:     */     try
/* 1600:     */     {
/* 1601:1830 */       boolean newLine = true;
/* 1602:1831 */       int index = 0;
/* 1603:1832 */       int lastPosition = this.undecodedChunk.readerIndex();
/* 1604:1833 */       boolean found = false;
/* 1605:1834 */       while (this.undecodedChunk.isReadable())
/* 1606:     */       {
/* 1607:1835 */         byte nextByte = this.undecodedChunk.readByte();
/* 1608:1836 */         if (newLine)
/* 1609:     */         {
/* 1610:1838 */           if (nextByte == delimiter.codePointAt(index))
/* 1611:     */           {
/* 1612:1839 */             index++;
/* 1613:1840 */             if (delimiter.length() == index)
/* 1614:     */             {
/* 1615:1841 */               found = true;
/* 1616:1842 */               break;
/* 1617:     */             }
/* 1618:     */           }
/* 1619:     */           else
/* 1620:     */           {
/* 1621:1846 */             newLine = false;
/* 1622:1847 */             index = 0;
/* 1623:1849 */             if (nextByte == 13)
/* 1624:     */             {
/* 1625:1850 */               if (this.undecodedChunk.isReadable())
/* 1626:     */               {
/* 1627:1851 */                 nextByte = this.undecodedChunk.readByte();
/* 1628:1852 */                 if (nextByte == 10)
/* 1629:     */                 {
/* 1630:1853 */                   newLine = true;
/* 1631:1854 */                   index = 0;
/* 1632:1855 */                   lastPosition = this.undecodedChunk.readerIndex() - 2;
/* 1633:     */                 }
/* 1634:     */                 else
/* 1635:     */                 {
/* 1636:1858 */                   lastPosition = this.undecodedChunk.readerIndex() - 1;
/* 1637:1859 */                   this.undecodedChunk.readerIndex(lastPosition);
/* 1638:     */                 }
/* 1639:     */               }
/* 1640:     */               else
/* 1641:     */               {
/* 1642:1862 */                 lastPosition = this.undecodedChunk.readerIndex() - 1;
/* 1643:     */               }
/* 1644:     */             }
/* 1645:1864 */             else if (nextByte == 10)
/* 1646:     */             {
/* 1647:1865 */               newLine = true;
/* 1648:1866 */               index = 0;
/* 1649:1867 */               lastPosition = this.undecodedChunk.readerIndex() - 1;
/* 1650:     */             }
/* 1651:     */             else
/* 1652:     */             {
/* 1653:1869 */               lastPosition = this.undecodedChunk.readerIndex();
/* 1654:     */             }
/* 1655:     */           }
/* 1656:     */         }
/* 1657:1874 */         else if (nextByte == 13)
/* 1658:     */         {
/* 1659:1875 */           if (this.undecodedChunk.isReadable())
/* 1660:     */           {
/* 1661:1876 */             nextByte = this.undecodedChunk.readByte();
/* 1662:1877 */             if (nextByte == 10)
/* 1663:     */             {
/* 1664:1878 */               newLine = true;
/* 1665:1879 */               index = 0;
/* 1666:1880 */               lastPosition = this.undecodedChunk.readerIndex() - 2;
/* 1667:     */             }
/* 1668:     */             else
/* 1669:     */             {
/* 1670:1883 */               lastPosition = this.undecodedChunk.readerIndex() - 1;
/* 1671:1884 */               this.undecodedChunk.readerIndex(lastPosition);
/* 1672:     */             }
/* 1673:     */           }
/* 1674:     */           else
/* 1675:     */           {
/* 1676:1887 */             lastPosition = this.undecodedChunk.readerIndex() - 1;
/* 1677:     */           }
/* 1678:     */         }
/* 1679:1889 */         else if (nextByte == 10)
/* 1680:     */         {
/* 1681:1890 */           newLine = true;
/* 1682:1891 */           index = 0;
/* 1683:1892 */           lastPosition = this.undecodedChunk.readerIndex() - 1;
/* 1684:     */         }
/* 1685:     */         else
/* 1686:     */         {
/* 1687:1894 */           lastPosition = this.undecodedChunk.readerIndex();
/* 1688:     */         }
/* 1689:     */       }
/* 1690:1898 */       if (found)
/* 1691:     */       {
/* 1692:     */         try
/* 1693:     */         {
/* 1694:1904 */           this.currentAttribute.addContent(this.undecodedChunk.copy(readerIndex, lastPosition - readerIndex), true);
/* 1695:     */         }
/* 1696:     */         catch (IOException e)
/* 1697:     */         {
/* 1698:1907 */           throw new ErrorDataDecoderException(e);
/* 1699:     */         }
/* 1700:1909 */         this.undecodedChunk.readerIndex(lastPosition);
/* 1701:     */       }
/* 1702:     */       else
/* 1703:     */       {
/* 1704:     */         try
/* 1705:     */         {
/* 1706:1912 */           this.currentAttribute.addContent(this.undecodedChunk.copy(readerIndex, lastPosition - readerIndex), false);
/* 1707:     */         }
/* 1708:     */         catch (IOException e)
/* 1709:     */         {
/* 1710:1915 */           throw new ErrorDataDecoderException(e);
/* 1711:     */         }
/* 1712:1917 */         this.undecodedChunk.readerIndex(lastPosition);
/* 1713:1918 */         throw new NotEnoughDataDecoderException();
/* 1714:     */       }
/* 1715:     */     }
/* 1716:     */     catch (IndexOutOfBoundsException e)
/* 1717:     */     {
/* 1718:1921 */       this.undecodedChunk.readerIndex(readerIndex);
/* 1719:1922 */       throw new NotEnoughDataDecoderException(e);
/* 1720:     */     }
/* 1721:     */   }
/* 1722:     */   
/* 1723:     */   private void loadFieldMultipart(String delimiter)
/* 1724:     */     throws HttpPostRequestDecoder.NotEnoughDataDecoderException, HttpPostRequestDecoder.ErrorDataDecoderException
/* 1725:     */   {
/* 1726:     */     HttpPostBodyUtil.SeekAheadOptimize sao;
/* 1727:     */     try
/* 1728:     */     {
/* 1729:1936 */       sao = new HttpPostBodyUtil.SeekAheadOptimize(this.undecodedChunk);
/* 1730:     */     }
/* 1731:     */     catch (HttpPostBodyUtil.SeekAheadNoBackArrayException e1)
/* 1732:     */     {
/* 1733:1938 */       loadFieldMultipartStandard(delimiter);
/* 1734:1939 */       return;
/* 1735:     */     }
/* 1736:1941 */     int readerIndex = this.undecodedChunk.readerIndex();
/* 1737:     */     try
/* 1738:     */     {
/* 1739:1944 */       boolean newLine = true;
/* 1740:1945 */       int index = 0;
/* 1741:     */       
/* 1742:1947 */       int lastrealpos = sao.pos;
/* 1743:1948 */       boolean found = false;
/* 1744:1950 */       while (sao.pos < sao.limit)
/* 1745:     */       {
/* 1746:1951 */         byte nextByte = sao.bytes[(sao.pos++)];
/* 1747:1952 */         if (newLine)
/* 1748:     */         {
/* 1749:1954 */           if (nextByte == delimiter.codePointAt(index))
/* 1750:     */           {
/* 1751:1955 */             index++;
/* 1752:1956 */             if (delimiter.length() == index)
/* 1753:     */             {
/* 1754:1957 */               found = true;
/* 1755:1958 */               break;
/* 1756:     */             }
/* 1757:     */           }
/* 1758:     */           else
/* 1759:     */           {
/* 1760:1962 */             newLine = false;
/* 1761:1963 */             index = 0;
/* 1762:1965 */             if (nextByte == 13)
/* 1763:     */             {
/* 1764:1966 */               if (sao.pos < sao.limit)
/* 1765:     */               {
/* 1766:1967 */                 nextByte = sao.bytes[(sao.pos++)];
/* 1767:1968 */                 if (nextByte == 10)
/* 1768:     */                 {
/* 1769:1969 */                   newLine = true;
/* 1770:1970 */                   index = 0;
/* 1771:1971 */                   lastrealpos = sao.pos - 2;
/* 1772:     */                 }
/* 1773:     */                 else
/* 1774:     */                 {
/* 1775:1974 */                   sao.pos -= 1;
/* 1776:1975 */                   lastrealpos = sao.pos;
/* 1777:     */                 }
/* 1778:     */               }
/* 1779:     */             }
/* 1780:1978 */             else if (nextByte == 10)
/* 1781:     */             {
/* 1782:1979 */               newLine = true;
/* 1783:1980 */               index = 0;
/* 1784:1981 */               lastrealpos = sao.pos - 1;
/* 1785:     */             }
/* 1786:     */             else
/* 1787:     */             {
/* 1788:1983 */               lastrealpos = sao.pos;
/* 1789:     */             }
/* 1790:     */           }
/* 1791:     */         }
/* 1792:1988 */         else if (nextByte == 13)
/* 1793:     */         {
/* 1794:1989 */           if (sao.pos < sao.limit)
/* 1795:     */           {
/* 1796:1990 */             nextByte = sao.bytes[(sao.pos++)];
/* 1797:1991 */             if (nextByte == 10)
/* 1798:     */             {
/* 1799:1992 */               newLine = true;
/* 1800:1993 */               index = 0;
/* 1801:1994 */               lastrealpos = sao.pos - 2;
/* 1802:     */             }
/* 1803:     */             else
/* 1804:     */             {
/* 1805:1997 */               sao.pos -= 1;
/* 1806:1998 */               lastrealpos = sao.pos;
/* 1807:     */             }
/* 1808:     */           }
/* 1809:     */         }
/* 1810:2001 */         else if (nextByte == 10)
/* 1811:     */         {
/* 1812:2002 */           newLine = true;
/* 1813:2003 */           index = 0;
/* 1814:2004 */           lastrealpos = sao.pos - 1;
/* 1815:     */         }
/* 1816:     */         else
/* 1817:     */         {
/* 1818:2006 */           lastrealpos = sao.pos;
/* 1819:     */         }
/* 1820:     */       }
/* 1821:2010 */       int lastPosition = sao.getReadPosition(lastrealpos);
/* 1822:2011 */       if (found)
/* 1823:     */       {
/* 1824:     */         try
/* 1825:     */         {
/* 1826:2017 */           this.currentAttribute.addContent(this.undecodedChunk.copy(readerIndex, lastPosition - readerIndex), true);
/* 1827:     */         }
/* 1828:     */         catch (IOException e)
/* 1829:     */         {
/* 1830:2020 */           throw new ErrorDataDecoderException(e);
/* 1831:     */         }
/* 1832:2022 */         this.undecodedChunk.readerIndex(lastPosition);
/* 1833:     */       }
/* 1834:     */       else
/* 1835:     */       {
/* 1836:     */         try
/* 1837:     */         {
/* 1838:2025 */           this.currentAttribute.addContent(this.undecodedChunk.copy(readerIndex, lastPosition - readerIndex), false);
/* 1839:     */         }
/* 1840:     */         catch (IOException e)
/* 1841:     */         {
/* 1842:2028 */           throw new ErrorDataDecoderException(e);
/* 1843:     */         }
/* 1844:2030 */         this.undecodedChunk.readerIndex(lastPosition);
/* 1845:2031 */         throw new NotEnoughDataDecoderException();
/* 1846:     */       }
/* 1847:     */     }
/* 1848:     */     catch (IndexOutOfBoundsException e)
/* 1849:     */     {
/* 1850:2034 */       this.undecodedChunk.readerIndex(readerIndex);
/* 1851:2035 */       throw new NotEnoughDataDecoderException(e);
/* 1852:     */     }
/* 1853:     */   }
/* 1854:     */   
/* 1855:     */   private static String cleanString(String field)
/* 1856:     */   {
/* 1857:2045 */     StringBuilder sb = new StringBuilder(field.length());
/* 1858:2046 */     for (int i = 0; i < field.length(); i++)
/* 1859:     */     {
/* 1860:2047 */       char nextChar = field.charAt(i);
/* 1861:2048 */       if (nextChar == ':') {
/* 1862:2049 */         sb.append(32);
/* 1863:2050 */       } else if (nextChar == ',') {
/* 1864:2051 */         sb.append(32);
/* 1865:2052 */       } else if (nextChar == '=') {
/* 1866:2053 */         sb.append(32);
/* 1867:2054 */       } else if (nextChar == ';') {
/* 1868:2055 */         sb.append(32);
/* 1869:2056 */       } else if (nextChar == '\t') {
/* 1870:2057 */         sb.append(32);
/* 1871:2058 */       } else if (nextChar != '"') {
/* 1872:2061 */         sb.append(nextChar);
/* 1873:     */       }
/* 1874:     */     }
/* 1875:2064 */     return sb.toString().trim();
/* 1876:     */   }
/* 1877:     */   
/* 1878:     */   private boolean skipOneLine()
/* 1879:     */   {
/* 1880:2073 */     if (!this.undecodedChunk.isReadable()) {
/* 1881:2074 */       return false;
/* 1882:     */     }
/* 1883:2076 */     byte nextByte = this.undecodedChunk.readByte();
/* 1884:2077 */     if (nextByte == 13)
/* 1885:     */     {
/* 1886:2078 */       if (!this.undecodedChunk.isReadable())
/* 1887:     */       {
/* 1888:2079 */         this.undecodedChunk.readerIndex(this.undecodedChunk.readerIndex() - 1);
/* 1889:2080 */         return false;
/* 1890:     */       }
/* 1891:2082 */       nextByte = this.undecodedChunk.readByte();
/* 1892:2083 */       if (nextByte == 10) {
/* 1893:2084 */         return true;
/* 1894:     */       }
/* 1895:2086 */       this.undecodedChunk.readerIndex(this.undecodedChunk.readerIndex() - 2);
/* 1896:2087 */       return false;
/* 1897:     */     }
/* 1898:2089 */     if (nextByte == 10) {
/* 1899:2090 */       return true;
/* 1900:     */     }
/* 1901:2092 */     this.undecodedChunk.readerIndex(this.undecodedChunk.readerIndex() - 1);
/* 1902:2093 */     return false;
/* 1903:     */   }
/* 1904:     */   
/* 1905:     */   private static String[] splitHeaderContentType(String sb)
/* 1906:     */   {
/* 1907:2106 */     int aStart = HttpPostBodyUtil.findNonWhitespace(sb, 0);
/* 1908:2107 */     int aEnd = sb.indexOf(';');
/* 1909:2108 */     if (aEnd == -1) {
/* 1910:2109 */       return new String[] { sb, "" };
/* 1911:     */     }
/* 1912:2111 */     if (sb.charAt(aEnd - 1) == ' ') {
/* 1913:2112 */       aEnd--;
/* 1914:     */     }
/* 1915:2114 */     int bStart = HttpPostBodyUtil.findNonWhitespace(sb, aEnd + 1);
/* 1916:2115 */     int bEnd = HttpPostBodyUtil.findEndOfString(sb);
/* 1917:2116 */     return new String[] { sb.substring(aStart, aEnd), sb.substring(bStart, bEnd) };
/* 1918:     */   }
/* 1919:     */   
/* 1920:     */   private static String[] splitMultipartHeader(String sb)
/* 1921:     */   {
/* 1922:2126 */     ArrayList<String> headers = new ArrayList(1);
/* 1923:     */     
/* 1924:     */ 
/* 1925:     */ 
/* 1926:     */ 
/* 1927:     */ 
/* 1928:2132 */     int nameStart = HttpPostBodyUtil.findNonWhitespace(sb, 0);
/* 1929:2133 */     for (int nameEnd = nameStart; nameEnd < sb.length(); nameEnd++)
/* 1930:     */     {
/* 1931:2134 */       char ch = sb.charAt(nameEnd);
/* 1932:2135 */       if ((ch == ':') || (Character.isWhitespace(ch))) {
/* 1933:     */         break;
/* 1934:     */       }
/* 1935:     */     }
/* 1936:2139 */     for (int colonEnd = nameEnd; colonEnd < sb.length(); colonEnd++) {
/* 1937:2140 */       if (sb.charAt(colonEnd) == ':')
/* 1938:     */       {
/* 1939:2141 */         colonEnd++;
/* 1940:2142 */         break;
/* 1941:     */       }
/* 1942:     */     }
/* 1943:2145 */     int valueStart = HttpPostBodyUtil.findNonWhitespace(sb, colonEnd);
/* 1944:2146 */     int valueEnd = HttpPostBodyUtil.findEndOfString(sb);
/* 1945:2147 */     headers.add(sb.substring(nameStart, nameEnd));
/* 1946:2148 */     String svalue = sb.substring(valueStart, valueEnd);
/* 1947:     */     String[] values;
/* 1948:     */     String[] values;
/* 1949:2150 */     if (svalue.indexOf(';') >= 0) {
/* 1950:2151 */       values = StringUtil.split(svalue, ';');
/* 1951:     */     } else {
/* 1952:2153 */       values = StringUtil.split(svalue, ',');
/* 1953:     */     }
/* 1954:2155 */     for (String value : values) {
/* 1955:2156 */       headers.add(value.trim());
/* 1956:     */     }
/* 1957:2158 */     String[] array = new String[headers.size()];
/* 1958:2159 */     for (int i = 0; i < headers.size(); i++) {
/* 1959:2160 */       array[i] = ((String)headers.get(i));
/* 1960:     */     }
/* 1961:2162 */     return array;
/* 1962:     */   }
/* 1963:     */   
/* 1964:     */   public static class NotEnoughDataDecoderException
/* 1965:     */     extends DecoderException
/* 1966:     */   {
/* 1967:     */     private static final long serialVersionUID = -7846841864603865638L;
/* 1968:     */     
/* 1969:     */     public NotEnoughDataDecoderException() {}
/* 1970:     */     
/* 1971:     */     public NotEnoughDataDecoderException(String msg)
/* 1972:     */     {
/* 1973:2176 */       super();
/* 1974:     */     }
/* 1975:     */     
/* 1976:     */     public NotEnoughDataDecoderException(Throwable cause)
/* 1977:     */     {
/* 1978:2180 */       super();
/* 1979:     */     }
/* 1980:     */     
/* 1981:     */     public NotEnoughDataDecoderException(String msg, Throwable cause)
/* 1982:     */     {
/* 1983:2184 */       super(cause);
/* 1984:     */     }
/* 1985:     */   }
/* 1986:     */   
/* 1987:     */   public static class EndOfDataDecoderException
/* 1988:     */     extends DecoderException
/* 1989:     */   {
/* 1990:     */     private static final long serialVersionUID = 1336267941020800769L;
/* 1991:     */   }
/* 1992:     */   
/* 1993:     */   public static class ErrorDataDecoderException
/* 1994:     */     extends DecoderException
/* 1995:     */   {
/* 1996:     */     private static final long serialVersionUID = 5020247425493164465L;
/* 1997:     */     
/* 1998:     */     public ErrorDataDecoderException() {}
/* 1999:     */     
/* 2000:     */     public ErrorDataDecoderException(String msg)
/* 2001:     */     {
/* 2002:2205 */       super();
/* 2003:     */     }
/* 2004:     */     
/* 2005:     */     public ErrorDataDecoderException(Throwable cause)
/* 2006:     */     {
/* 2007:2209 */       super();
/* 2008:     */     }
/* 2009:     */     
/* 2010:     */     public ErrorDataDecoderException(String msg, Throwable cause)
/* 2011:     */     {
/* 2012:2213 */       super(cause);
/* 2013:     */     }
/* 2014:     */   }
/* 2015:     */   
/* 2016:     */   public static class IncompatibleDataDecoderException
/* 2017:     */     extends DecoderException
/* 2018:     */   {
/* 2019:     */     private static final long serialVersionUID = -953268047926250267L;
/* 2020:     */     
/* 2021:     */     public IncompatibleDataDecoderException() {}
/* 2022:     */     
/* 2023:     */     public IncompatibleDataDecoderException(String msg)
/* 2024:     */     {
/* 2025:2227 */       super();
/* 2026:     */     }
/* 2027:     */     
/* 2028:     */     public IncompatibleDataDecoderException(Throwable cause)
/* 2029:     */     {
/* 2030:2231 */       super();
/* 2031:     */     }
/* 2032:     */     
/* 2033:     */     public IncompatibleDataDecoderException(String msg, Throwable cause)
/* 2034:     */     {
/* 2035:2235 */       super(cause);
/* 2036:     */     }
/* 2037:     */   }
/* 2038:     */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.multipart.HttpPostRequestDecoder
 * JD-Core Version:    0.7.0.1
 */