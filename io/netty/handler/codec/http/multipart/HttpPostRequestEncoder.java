/*    1:     */ package io.netty.handler.codec.http.multipart;
/*    2:     */ 
/*    3:     */ import io.netty.buffer.ByteBuf;
/*    4:     */ import io.netty.buffer.Unpooled;
/*    5:     */ import io.netty.channel.ChannelHandlerContext;
/*    6:     */ import io.netty.handler.codec.DecoderResult;
/*    7:     */ import io.netty.handler.codec.http.DefaultFullHttpRequest;
/*    8:     */ import io.netty.handler.codec.http.DefaultHttpContent;
/*    9:     */ import io.netty.handler.codec.http.FullHttpRequest;
/*   10:     */ import io.netty.handler.codec.http.HttpConstants;
/*   11:     */ import io.netty.handler.codec.http.HttpContent;
/*   12:     */ import io.netty.handler.codec.http.HttpHeaders;
/*   13:     */ import io.netty.handler.codec.http.HttpMethod;
/*   14:     */ import io.netty.handler.codec.http.HttpRequest;
/*   15:     */ import io.netty.handler.codec.http.HttpVersion;
/*   16:     */ import io.netty.handler.codec.http.LastHttpContent;
/*   17:     */ import io.netty.handler.stream.ChunkedInput;
/*   18:     */ import io.netty.util.internal.ThreadLocalRandom;
/*   19:     */ import java.io.File;
/*   20:     */ import java.io.IOException;
/*   21:     */ import java.io.UnsupportedEncodingException;
/*   22:     */ import java.net.URLEncoder;
/*   23:     */ import java.nio.charset.Charset;
/*   24:     */ import java.util.ArrayList;
/*   25:     */ import java.util.HashMap;
/*   26:     */ import java.util.List;
/*   27:     */ import java.util.ListIterator;
/*   28:     */ import java.util.Map;
/*   29:     */ import java.util.Map.Entry;
/*   30:     */ import java.util.regex.Matcher;
/*   31:     */ import java.util.regex.Pattern;
/*   32:     */ 
/*   33:     */ public class HttpPostRequestEncoder
/*   34:     */   implements ChunkedInput<HttpContent>
/*   35:     */ {
/*   36:     */   public static enum EncoderMode
/*   37:     */   {
/*   38:  61 */     RFC1738,  RFC3986;
/*   39:     */     
/*   40:     */     private EncoderMode() {}
/*   41:     */   }
/*   42:     */   
/*   43:  69 */   private static final Map<Pattern, String> percentEncodings = new HashMap();
/*   44:     */   private final HttpDataFactory factory;
/*   45:     */   private final HttpRequest request;
/*   46:     */   private final Charset charset;
/*   47:     */   private boolean isChunked;
/*   48:     */   private final List<InterfaceHttpData> bodyListDatas;
/*   49:     */   final List<InterfaceHttpData> multipartHttpDatas;
/*   50:     */   private final boolean isMultipart;
/*   51:     */   String multipartDataBoundary;
/*   52:     */   String multipartMixedBoundary;
/*   53:     */   private boolean headerFinalized;
/*   54:     */   private final EncoderMode encoderMode;
/*   55:     */   private boolean isLastChunk;
/*   56:     */   private boolean isLastChunkSent;
/*   57:     */   private FileUpload currentFileUpload;
/*   58:     */   private boolean duringMixedMode;
/*   59:     */   private long globalBodySize;
/*   60:     */   private ListIterator<InterfaceHttpData> iterator;
/*   61:     */   private ByteBuf currentBuffer;
/*   62:     */   private InterfaceHttpData currentData;
/*   63:     */   
/*   64:     */   static
/*   65:     */   {
/*   66:  72 */     percentEncodings.put(Pattern.compile("\\*"), "%2A");
/*   67:  73 */     percentEncodings.put(Pattern.compile("\\+"), "%20");
/*   68:  74 */     percentEncodings.put(Pattern.compile("%7E"), "~");
/*   69:     */   }
/*   70:     */   
/*   71:     */   public HttpPostRequestEncoder(HttpRequest request, boolean multipart)
/*   72:     */     throws HttpPostRequestEncoder.ErrorDataEncoderException
/*   73:     */   {
/*   74: 139 */     this(new DefaultHttpDataFactory(16384L), request, multipart, HttpConstants.DEFAULT_CHARSET, EncoderMode.RFC1738);
/*   75:     */   }
/*   76:     */   
/*   77:     */   public HttpPostRequestEncoder(HttpDataFactory factory, HttpRequest request, boolean multipart)
/*   78:     */     throws HttpPostRequestEncoder.ErrorDataEncoderException
/*   79:     */   {
/*   80: 158 */     this(factory, request, multipart, HttpConstants.DEFAULT_CHARSET, EncoderMode.RFC1738);
/*   81:     */   }
/*   82:     */   
/*   83:     */   public HttpPostRequestEncoder(HttpDataFactory factory, HttpRequest request, boolean multipart, Charset charset, EncoderMode encoderMode)
/*   84:     */     throws HttpPostRequestEncoder.ErrorDataEncoderException
/*   85:     */   {
/*   86: 182 */     if (factory == null) {
/*   87: 183 */       throw new NullPointerException("factory");
/*   88:     */     }
/*   89: 185 */     if (request == null) {
/*   90: 186 */       throw new NullPointerException("request");
/*   91:     */     }
/*   92: 188 */     if (charset == null) {
/*   93: 189 */       throw new NullPointerException("charset");
/*   94:     */     }
/*   95: 191 */     if (request.getMethod() != HttpMethod.POST) {
/*   96: 192 */       throw new ErrorDataEncoderException("Cannot create a Encoder if not a POST");
/*   97:     */     }
/*   98: 194 */     this.request = request;
/*   99: 195 */     this.charset = charset;
/*  100: 196 */     this.factory = factory;
/*  101:     */     
/*  102: 198 */     this.bodyListDatas = new ArrayList();
/*  103:     */     
/*  104: 200 */     this.isLastChunk = false;
/*  105: 201 */     this.isLastChunkSent = false;
/*  106: 202 */     this.isMultipart = multipart;
/*  107: 203 */     this.multipartHttpDatas = new ArrayList();
/*  108: 204 */     this.encoderMode = encoderMode;
/*  109: 205 */     if (this.isMultipart) {
/*  110: 206 */       initDataMultipart();
/*  111:     */     }
/*  112:     */   }
/*  113:     */   
/*  114:     */   public void cleanFiles()
/*  115:     */   {
/*  116: 214 */     this.factory.cleanRequestHttpDatas(this.request);
/*  117:     */   }
/*  118:     */   
/*  119:     */   public boolean isMultipart()
/*  120:     */   {
/*  121: 245 */     return this.isMultipart;
/*  122:     */   }
/*  123:     */   
/*  124:     */   private void initDataMultipart()
/*  125:     */   {
/*  126: 252 */     this.multipartDataBoundary = getNewMultipartDelimiter();
/*  127:     */   }
/*  128:     */   
/*  129:     */   private void initMixedMultipart()
/*  130:     */   {
/*  131: 259 */     this.multipartMixedBoundary = getNewMultipartDelimiter();
/*  132:     */   }
/*  133:     */   
/*  134:     */   private static String getNewMultipartDelimiter()
/*  135:     */   {
/*  136: 268 */     return Long.toHexString(ThreadLocalRandom.current().nextLong()).toLowerCase();
/*  137:     */   }
/*  138:     */   
/*  139:     */   public List<InterfaceHttpData> getBodyListAttributes()
/*  140:     */   {
/*  141: 277 */     return this.bodyListDatas;
/*  142:     */   }
/*  143:     */   
/*  144:     */   public void setBodyHttpDatas(List<InterfaceHttpData> datas)
/*  145:     */     throws HttpPostRequestEncoder.ErrorDataEncoderException
/*  146:     */   {
/*  147: 289 */     if (datas == null) {
/*  148: 290 */       throw new NullPointerException("datas");
/*  149:     */     }
/*  150: 292 */     this.globalBodySize = 0L;
/*  151: 293 */     this.bodyListDatas.clear();
/*  152: 294 */     this.currentFileUpload = null;
/*  153: 295 */     this.duringMixedMode = false;
/*  154: 296 */     this.multipartHttpDatas.clear();
/*  155: 297 */     for (InterfaceHttpData data : datas) {
/*  156: 298 */       addBodyHttpData(data);
/*  157:     */     }
/*  158:     */   }
/*  159:     */   
/*  160:     */   public void addBodyAttribute(String name, String value)
/*  161:     */     throws HttpPostRequestEncoder.ErrorDataEncoderException
/*  162:     */   {
/*  163: 315 */     if (name == null) {
/*  164: 316 */       throw new NullPointerException("name");
/*  165:     */     }
/*  166: 318 */     String svalue = value;
/*  167: 319 */     if (value == null) {
/*  168: 320 */       svalue = "";
/*  169:     */     }
/*  170: 322 */     Attribute data = this.factory.createAttribute(this.request, name, svalue);
/*  171: 323 */     addBodyHttpData(data);
/*  172:     */   }
/*  173:     */   
/*  174:     */   public void addBodyFileUpload(String name, File file, String contentType, boolean isText)
/*  175:     */     throws HttpPostRequestEncoder.ErrorDataEncoderException
/*  176:     */   {
/*  177: 344 */     if (name == null) {
/*  178: 345 */       throw new NullPointerException("name");
/*  179:     */     }
/*  180: 347 */     if (file == null) {
/*  181: 348 */       throw new NullPointerException("file");
/*  182:     */     }
/*  183: 350 */     String scontentType = contentType;
/*  184: 351 */     String contentTransferEncoding = null;
/*  185: 352 */     if (contentType == null) {
/*  186: 353 */       if (isText) {
/*  187: 354 */         scontentType = "text/plain";
/*  188:     */       } else {
/*  189: 356 */         scontentType = "application/octet-stream";
/*  190:     */       }
/*  191:     */     }
/*  192: 359 */     if (!isText) {
/*  193: 360 */       contentTransferEncoding = HttpPostBodyUtil.TransferEncodingMechanism.BINARY.value();
/*  194:     */     }
/*  195: 362 */     FileUpload fileUpload = this.factory.createFileUpload(this.request, name, file.getName(), scontentType, contentTransferEncoding, null, file.length());
/*  196:     */     try
/*  197:     */     {
/*  198: 365 */       fileUpload.setContent(file);
/*  199:     */     }
/*  200:     */     catch (IOException e)
/*  201:     */     {
/*  202: 367 */       throw new ErrorDataEncoderException(e);
/*  203:     */     }
/*  204: 369 */     addBodyHttpData(fileUpload);
/*  205:     */   }
/*  206:     */   
/*  207:     */   public void addBodyFileUploads(String name, File[] file, String[] contentType, boolean[] isText)
/*  208:     */     throws HttpPostRequestEncoder.ErrorDataEncoderException
/*  209:     */   {
/*  210: 390 */     if ((file.length != contentType.length) && (file.length != isText.length)) {
/*  211: 391 */       throw new NullPointerException("Different array length");
/*  212:     */     }
/*  213: 393 */     for (int i = 0; i < file.length; i++) {
/*  214: 394 */       addBodyFileUpload(name, file[i], contentType[i], isText[i]);
/*  215:     */     }
/*  216:     */   }
/*  217:     */   
/*  218:     */   public void addBodyHttpData(InterfaceHttpData data)
/*  219:     */     throws HttpPostRequestEncoder.ErrorDataEncoderException
/*  220:     */   {
/*  221: 407 */     if (this.headerFinalized) {
/*  222: 408 */       throw new ErrorDataEncoderException("Cannot add value once finalized");
/*  223:     */     }
/*  224: 410 */     if (data == null) {
/*  225: 411 */       throw new NullPointerException("data");
/*  226:     */     }
/*  227: 413 */     this.bodyListDatas.add(data);
/*  228: 414 */     if (!this.isMultipart)
/*  229:     */     {
/*  230: 415 */       if ((data instanceof Attribute))
/*  231:     */       {
/*  232: 416 */         Attribute attribute = (Attribute)data;
/*  233:     */         try
/*  234:     */         {
/*  235: 419 */           String key = encodeAttribute(attribute.getName(), this.charset);
/*  236: 420 */           String value = encodeAttribute(attribute.getValue(), this.charset);
/*  237: 421 */           Attribute newattribute = this.factory.createAttribute(this.request, key, value);
/*  238: 422 */           this.multipartHttpDatas.add(newattribute);
/*  239: 423 */           this.globalBodySize += newattribute.getName().length() + 1 + newattribute.length() + 1L;
/*  240:     */         }
/*  241:     */         catch (IOException e)
/*  242:     */         {
/*  243: 425 */           throw new ErrorDataEncoderException(e);
/*  244:     */         }
/*  245:     */       }
/*  246: 427 */       else if ((data instanceof FileUpload))
/*  247:     */       {
/*  248: 429 */         FileUpload fileUpload = (FileUpload)data;
/*  249:     */         
/*  250: 431 */         String key = encodeAttribute(fileUpload.getName(), this.charset);
/*  251: 432 */         String value = encodeAttribute(fileUpload.getFilename(), this.charset);
/*  252: 433 */         Attribute newattribute = this.factory.createAttribute(this.request, key, value);
/*  253: 434 */         this.multipartHttpDatas.add(newattribute);
/*  254: 435 */         this.globalBodySize += newattribute.getName().length() + 1 + newattribute.length() + 1L;
/*  255:     */       }
/*  256: 437 */       return;
/*  257:     */     }
/*  258: 471 */     if ((data instanceof Attribute))
/*  259:     */     {
/*  260: 472 */       if (this.duringMixedMode)
/*  261:     */       {
/*  262: 473 */         InternalAttribute internal = new InternalAttribute(this.charset);
/*  263: 474 */         internal.addValue("\r\n--" + this.multipartMixedBoundary + "--");
/*  264: 475 */         this.multipartHttpDatas.add(internal);
/*  265: 476 */         this.multipartMixedBoundary = null;
/*  266: 477 */         this.currentFileUpload = null;
/*  267: 478 */         this.duringMixedMode = false;
/*  268:     */       }
/*  269: 480 */       InternalAttribute internal = new InternalAttribute(this.charset);
/*  270: 481 */       if (!this.multipartHttpDatas.isEmpty()) {
/*  271: 483 */         internal.addValue("\r\n");
/*  272:     */       }
/*  273: 485 */       internal.addValue("--" + this.multipartDataBoundary + "\r\n");
/*  274:     */       
/*  275: 487 */       Attribute attribute = (Attribute)data;
/*  276: 488 */       internal.addValue("Content-Disposition: form-data; name=\"" + attribute.getName() + "\"\r\n");
/*  277:     */       
/*  278: 490 */       Charset localcharset = attribute.getCharset();
/*  279: 491 */       if (localcharset != null) {
/*  280: 493 */         internal.addValue("Content-Type: text/plain; charset=" + localcharset + "\r\n");
/*  281:     */       }
/*  282: 499 */       internal.addValue("\r\n");
/*  283: 500 */       this.multipartHttpDatas.add(internal);
/*  284: 501 */       this.multipartHttpDatas.add(data);
/*  285: 502 */       this.globalBodySize += attribute.length() + internal.size();
/*  286:     */     }
/*  287: 503 */     else if ((data instanceof FileUpload))
/*  288:     */     {
/*  289: 504 */       FileUpload fileUpload = (FileUpload)data;
/*  290: 505 */       InternalAttribute internal = new InternalAttribute(this.charset);
/*  291: 506 */       if (!this.multipartHttpDatas.isEmpty()) {
/*  292: 508 */         internal.addValue("\r\n");
/*  293:     */       }
/*  294:     */       boolean localMixed;
/*  295: 511 */       if (this.duringMixedMode)
/*  296:     */       {
/*  297:     */         boolean localMixed;
/*  298: 512 */         if ((this.currentFileUpload != null) && (this.currentFileUpload.getName().equals(fileUpload.getName())))
/*  299:     */         {
/*  300: 515 */           localMixed = true;
/*  301:     */         }
/*  302:     */         else
/*  303:     */         {
/*  304: 522 */           internal.addValue("--" + this.multipartMixedBoundary + "--");
/*  305: 523 */           this.multipartHttpDatas.add(internal);
/*  306: 524 */           this.multipartMixedBoundary = null;
/*  307:     */           
/*  308:     */ 
/*  309: 527 */           internal = new InternalAttribute(this.charset);
/*  310: 528 */           internal.addValue("\r\n");
/*  311: 529 */           boolean localMixed = false;
/*  312:     */           
/*  313: 531 */           this.currentFileUpload = fileUpload;
/*  314: 532 */           this.duringMixedMode = false;
/*  315:     */         }
/*  316:     */       }
/*  317: 535 */       else if ((this.currentFileUpload != null) && (this.currentFileUpload.getName().equals(fileUpload.getName())))
/*  318:     */       {
/*  319: 556 */         initMixedMultipart();
/*  320: 557 */         InternalAttribute pastAttribute = (InternalAttribute)this.multipartHttpDatas.get(this.multipartHttpDatas.size() - 2);
/*  321:     */         
/*  322:     */ 
/*  323: 560 */         this.globalBodySize -= pastAttribute.size();
/*  324: 561 */         StringBuilder replacement = new StringBuilder(139 + this.multipartDataBoundary.length() + this.multipartMixedBoundary.length() * 2 + fileUpload.getFilename().length() + fileUpload.getName().length());
/*  325:     */         
/*  326:     */ 
/*  327:     */ 
/*  328: 565 */         replacement.append("--");
/*  329: 566 */         replacement.append(this.multipartDataBoundary);
/*  330: 567 */         replacement.append("\r\n");
/*  331:     */         
/*  332: 569 */         replacement.append("Content-Disposition");
/*  333: 570 */         replacement.append(": ");
/*  334: 571 */         replacement.append("form-data");
/*  335: 572 */         replacement.append("; ");
/*  336: 573 */         replacement.append("name");
/*  337: 574 */         replacement.append("=\"");
/*  338: 575 */         replacement.append(fileUpload.getName());
/*  339: 576 */         replacement.append("\"\r\n");
/*  340:     */         
/*  341: 578 */         replacement.append("Content-Type");
/*  342: 579 */         replacement.append(": ");
/*  343: 580 */         replacement.append("multipart/mixed");
/*  344: 581 */         replacement.append("; ");
/*  345: 582 */         replacement.append("boundary");
/*  346: 583 */         replacement.append('=');
/*  347: 584 */         replacement.append(this.multipartMixedBoundary);
/*  348: 585 */         replacement.append("\r\n\r\n");
/*  349:     */         
/*  350: 587 */         replacement.append("--");
/*  351: 588 */         replacement.append(this.multipartMixedBoundary);
/*  352: 589 */         replacement.append("\r\n");
/*  353:     */         
/*  354: 591 */         replacement.append("Content-Disposition");
/*  355: 592 */         replacement.append(": ");
/*  356: 593 */         replacement.append("attachment");
/*  357: 594 */         replacement.append("; ");
/*  358: 595 */         replacement.append("filename");
/*  359: 596 */         replacement.append("=\"");
/*  360: 597 */         replacement.append(fileUpload.getFilename());
/*  361: 598 */         replacement.append("\"\r\n");
/*  362:     */         
/*  363: 600 */         pastAttribute.setValue(replacement.toString(), 1);
/*  364: 601 */         pastAttribute.setValue("", 2);
/*  365:     */         
/*  366:     */ 
/*  367: 604 */         this.globalBodySize += pastAttribute.size();
/*  368:     */         
/*  369:     */ 
/*  370:     */ 
/*  371:     */ 
/*  372:     */ 
/*  373: 610 */         boolean localMixed = true;
/*  374: 611 */         this.duringMixedMode = true;
/*  375:     */       }
/*  376:     */       else
/*  377:     */       {
/*  378: 616 */         localMixed = false;
/*  379: 617 */         this.currentFileUpload = fileUpload;
/*  380: 618 */         this.duringMixedMode = false;
/*  381:     */       }
/*  382: 622 */       if (localMixed)
/*  383:     */       {
/*  384: 625 */         internal.addValue("--" + this.multipartMixedBoundary + "\r\n");
/*  385:     */         
/*  386: 627 */         internal.addValue("Content-Disposition: attachment; filename=\"" + fileUpload.getFilename() + "\"\r\n");
/*  387:     */       }
/*  388:     */       else
/*  389:     */       {
/*  390: 630 */         internal.addValue("--" + this.multipartDataBoundary + "\r\n");
/*  391:     */         
/*  392:     */ 
/*  393: 633 */         internal.addValue("Content-Disposition: form-data; name=\"" + fileUpload.getName() + "\"; " + "filename" + "=\"" + fileUpload.getFilename() + "\"\r\n");
/*  394:     */       }
/*  395: 640 */       internal.addValue("Content-Type: " + fileUpload.getContentType());
/*  396: 641 */       String contentTransferEncoding = fileUpload.getContentTransferEncoding();
/*  397: 642 */       if ((contentTransferEncoding != null) && (contentTransferEncoding.equals(HttpPostBodyUtil.TransferEncodingMechanism.BINARY.value()))) {
/*  398: 644 */         internal.addValue("\r\nContent-Transfer-Encoding: " + HttpPostBodyUtil.TransferEncodingMechanism.BINARY.value() + "\r\n\r\n");
/*  399: 646 */       } else if (fileUpload.getCharset() != null) {
/*  400: 647 */         internal.addValue("; charset=" + fileUpload.getCharset() + "\r\n\r\n");
/*  401:     */       } else {
/*  402: 649 */         internal.addValue("\r\n\r\n");
/*  403:     */       }
/*  404: 651 */       this.multipartHttpDatas.add(internal);
/*  405: 652 */       this.multipartHttpDatas.add(data);
/*  406: 653 */       this.globalBodySize += fileUpload.length() + internal.size();
/*  407:     */     }
/*  408:     */   }
/*  409:     */   
/*  410:     */   public HttpRequest finalizeRequest()
/*  411:     */     throws HttpPostRequestEncoder.ErrorDataEncoderException
/*  412:     */   {
/*  413: 674 */     if (!this.headerFinalized)
/*  414:     */     {
/*  415: 675 */       if (this.isMultipart)
/*  416:     */       {
/*  417: 676 */         InternalAttribute internal = new InternalAttribute(this.charset);
/*  418: 677 */         if (this.duringMixedMode) {
/*  419: 678 */           internal.addValue("\r\n--" + this.multipartMixedBoundary + "--");
/*  420:     */         }
/*  421: 680 */         internal.addValue("\r\n--" + this.multipartDataBoundary + "--\r\n");
/*  422: 681 */         this.multipartHttpDatas.add(internal);
/*  423: 682 */         this.multipartMixedBoundary = null;
/*  424: 683 */         this.currentFileUpload = null;
/*  425: 684 */         this.duringMixedMode = false;
/*  426: 685 */         this.globalBodySize += internal.size();
/*  427:     */       }
/*  428: 687 */       this.headerFinalized = true;
/*  429:     */     }
/*  430:     */     else
/*  431:     */     {
/*  432: 689 */       throw new ErrorDataEncoderException("Header already encoded");
/*  433:     */     }
/*  434: 692 */     HttpHeaders headers = this.request.headers();
/*  435: 693 */     List<String> contentTypes = headers.getAll("Content-Type");
/*  436: 694 */     List<String> transferEncoding = headers.getAll("Transfer-Encoding");
/*  437: 695 */     if (contentTypes != null)
/*  438:     */     {
/*  439: 696 */       headers.remove("Content-Type");
/*  440: 697 */       for (String contentType : contentTypes)
/*  441:     */       {
/*  442: 699 */         String lowercased = contentType.toLowerCase();
/*  443: 700 */         if ((!lowercased.startsWith("multipart/form-data")) && (!lowercased.startsWith("application/x-www-form-urlencoded"))) {
/*  444: 704 */           headers.add("Content-Type", contentType);
/*  445:     */         }
/*  446:     */       }
/*  447:     */     }
/*  448: 708 */     if (this.isMultipart)
/*  449:     */     {
/*  450: 709 */       String value = "multipart/form-data; boundary=" + this.multipartDataBoundary;
/*  451:     */       
/*  452: 711 */       headers.add("Content-Type", value);
/*  453:     */     }
/*  454:     */     else
/*  455:     */     {
/*  456: 714 */       headers.add("Content-Type", "application/x-www-form-urlencoded");
/*  457:     */     }
/*  458: 717 */     long realSize = this.globalBodySize;
/*  459: 718 */     if (this.isMultipart)
/*  460:     */     {
/*  461: 719 */       this.iterator = this.multipartHttpDatas.listIterator();
/*  462:     */     }
/*  463:     */     else
/*  464:     */     {
/*  465: 721 */       realSize -= 1L;
/*  466: 722 */       this.iterator = this.multipartHttpDatas.listIterator();
/*  467:     */     }
/*  468: 724 */     headers.set("Content-Length", String.valueOf(realSize));
/*  469: 725 */     if ((realSize > 8096L) || (this.isMultipart))
/*  470:     */     {
/*  471: 726 */       this.isChunked = true;
/*  472: 727 */       if (transferEncoding != null)
/*  473:     */       {
/*  474: 728 */         headers.remove("Transfer-Encoding");
/*  475: 729 */         for (String v : transferEncoding) {
/*  476: 730 */           if (!v.equalsIgnoreCase("chunked")) {
/*  477: 733 */             headers.add("Transfer-Encoding", v);
/*  478:     */           }
/*  479:     */         }
/*  480:     */       }
/*  481: 737 */       HttpHeaders.setTransferEncodingChunked(this.request);
/*  482:     */       
/*  483:     */ 
/*  484: 740 */       return new WrappedHttpRequest(this.request);
/*  485:     */     }
/*  486: 743 */     HttpContent chunk = nextChunk();
/*  487: 744 */     if ((this.request instanceof FullHttpRequest))
/*  488:     */     {
/*  489: 745 */       FullHttpRequest fullRequest = (FullHttpRequest)this.request;
/*  490: 746 */       ByteBuf chunkContent = chunk.content();
/*  491: 747 */       if (fullRequest.content() != chunkContent)
/*  492:     */       {
/*  493: 748 */         fullRequest.content().clear().writeBytes(chunkContent);
/*  494: 749 */         chunkContent.release();
/*  495:     */       }
/*  496: 751 */       return fullRequest;
/*  497:     */     }
/*  498: 753 */     return new WrappedFullHttpRequest(this.request, chunk, null);
/*  499:     */   }
/*  500:     */   
/*  501:     */   public boolean isChunked()
/*  502:     */   {
/*  503: 762 */     return this.isChunked;
/*  504:     */   }
/*  505:     */   
/*  506:     */   private String encodeAttribute(String s, Charset charset)
/*  507:     */     throws HttpPostRequestEncoder.ErrorDataEncoderException
/*  508:     */   {
/*  509: 773 */     if (s == null) {
/*  510: 774 */       return "";
/*  511:     */     }
/*  512:     */     try
/*  513:     */     {
/*  514: 777 */       String encoded = URLEncoder.encode(s, charset.name());
/*  515: 778 */       if (this.encoderMode == EncoderMode.RFC3986) {
/*  516: 779 */         for (Map.Entry<Pattern, String> entry : percentEncodings.entrySet())
/*  517:     */         {
/*  518: 780 */           String replacement = (String)entry.getValue();
/*  519: 781 */           encoded = ((Pattern)entry.getKey()).matcher(encoded).replaceAll(replacement);
/*  520:     */         }
/*  521:     */       }
/*  522: 784 */       return encoded;
/*  523:     */     }
/*  524:     */     catch (UnsupportedEncodingException e)
/*  525:     */     {
/*  526: 786 */       throw new ErrorDataEncoderException(charset.name(), e);
/*  527:     */     }
/*  528:     */   }
/*  529:     */   
/*  530: 801 */   private boolean isKey = true;
/*  531:     */   
/*  532:     */   private ByteBuf fillByteBuf()
/*  533:     */   {
/*  534: 808 */     int length = this.currentBuffer.readableBytes();
/*  535: 809 */     if (length > 8096)
/*  536:     */     {
/*  537: 810 */       ByteBuf slice = this.currentBuffer.slice(this.currentBuffer.readerIndex(), 8096);
/*  538: 811 */       this.currentBuffer.skipBytes(8096);
/*  539: 812 */       return slice;
/*  540:     */     }
/*  541: 815 */     ByteBuf slice = this.currentBuffer;
/*  542: 816 */     this.currentBuffer = null;
/*  543: 817 */     return slice;
/*  544:     */   }
/*  545:     */   
/*  546:     */   private HttpContent encodeNextChunkMultipart(int sizeleft)
/*  547:     */     throws HttpPostRequestEncoder.ErrorDataEncoderException
/*  548:     */   {
/*  549: 832 */     if (this.currentData == null) {
/*  550: 833 */       return null;
/*  551:     */     }
/*  552: 836 */     if ((this.currentData instanceof InternalAttribute))
/*  553:     */     {
/*  554: 837 */       ByteBuf buffer = ((InternalAttribute)this.currentData).toByteBuf();
/*  555: 838 */       this.currentData = null;
/*  556:     */     }
/*  557:     */     else
/*  558:     */     {
/*  559: 840 */       if ((this.currentData instanceof Attribute)) {
/*  560:     */         try
/*  561:     */         {
/*  562: 842 */           buffer = ((Attribute)this.currentData).getChunk(sizeleft);
/*  563:     */         }
/*  564:     */         catch (IOException e)
/*  565:     */         {
/*  566: 844 */           throw new ErrorDataEncoderException(e);
/*  567:     */         }
/*  568:     */       } else {
/*  569:     */         try
/*  570:     */         {
/*  571: 848 */           buffer = ((HttpData)this.currentData).getChunk(sizeleft);
/*  572:     */         }
/*  573:     */         catch (IOException e)
/*  574:     */         {
/*  575: 850 */           throw new ErrorDataEncoderException(e);
/*  576:     */         }
/*  577:     */       }
/*  578: 853 */       if (buffer.capacity() == 0)
/*  579:     */       {
/*  580: 855 */         this.currentData = null;
/*  581: 856 */         return null;
/*  582:     */       }
/*  583:     */     }
/*  584: 859 */     if (this.currentBuffer == null) {
/*  585: 860 */       this.currentBuffer = buffer;
/*  586:     */     } else {
/*  587: 862 */       this.currentBuffer = Unpooled.wrappedBuffer(new ByteBuf[] { this.currentBuffer, buffer });
/*  588:     */     }
/*  589: 864 */     if (this.currentBuffer.readableBytes() < 8096)
/*  590:     */     {
/*  591: 865 */       this.currentData = null;
/*  592: 866 */       return null;
/*  593:     */     }
/*  594: 868 */     ByteBuf buffer = fillByteBuf();
/*  595: 869 */     return new DefaultHttpContent(buffer);
/*  596:     */   }
/*  597:     */   
/*  598:     */   private HttpContent encodeNextChunkUrlEncoded(int sizeleft)
/*  599:     */     throws HttpPostRequestEncoder.ErrorDataEncoderException
/*  600:     */   {
/*  601: 883 */     if (this.currentData == null) {
/*  602: 884 */       return null;
/*  603:     */     }
/*  604: 886 */     int size = sizeleft;
/*  605: 890 */     if (this.isKey)
/*  606:     */     {
/*  607: 891 */       String key = this.currentData.getName();
/*  608: 892 */       ByteBuf buffer = Unpooled.wrappedBuffer(key.getBytes());
/*  609: 893 */       this.isKey = false;
/*  610: 894 */       if (this.currentBuffer == null)
/*  611:     */       {
/*  612: 895 */         this.currentBuffer = Unpooled.wrappedBuffer(new ByteBuf[] { buffer, Unpooled.wrappedBuffer("=".getBytes()) });
/*  613:     */         
/*  614: 897 */         size -= buffer.readableBytes() + 1;
/*  615:     */       }
/*  616:     */       else
/*  617:     */       {
/*  618: 899 */         this.currentBuffer = Unpooled.wrappedBuffer(new ByteBuf[] { this.currentBuffer, buffer, Unpooled.wrappedBuffer("=".getBytes()) });
/*  619:     */         
/*  620: 901 */         size -= buffer.readableBytes() + 1;
/*  621:     */       }
/*  622: 903 */       if (this.currentBuffer.readableBytes() >= 8096)
/*  623:     */       {
/*  624: 904 */         buffer = fillByteBuf();
/*  625: 905 */         return new DefaultHttpContent(buffer);
/*  626:     */       }
/*  627:     */     }
/*  628:     */     try
/*  629:     */     {
/*  630: 911 */       buffer = ((HttpData)this.currentData).getChunk(size);
/*  631:     */     }
/*  632:     */     catch (IOException e)
/*  633:     */     {
/*  634: 913 */       throw new ErrorDataEncoderException(e);
/*  635:     */     }
/*  636: 917 */     ByteBuf delimiter = null;
/*  637: 918 */     if (buffer.readableBytes() < size)
/*  638:     */     {
/*  639: 919 */       this.isKey = true;
/*  640: 920 */       delimiter = this.iterator.hasNext() ? Unpooled.wrappedBuffer("&".getBytes()) : null;
/*  641:     */     }
/*  642: 924 */     if (buffer.capacity() == 0)
/*  643:     */     {
/*  644: 925 */       this.currentData = null;
/*  645: 926 */       if (this.currentBuffer == null) {
/*  646: 927 */         this.currentBuffer = delimiter;
/*  647: 929 */       } else if (delimiter != null) {
/*  648: 930 */         this.currentBuffer = Unpooled.wrappedBuffer(new ByteBuf[] { this.currentBuffer, delimiter });
/*  649:     */       }
/*  650: 933 */       if (this.currentBuffer.readableBytes() >= 8096)
/*  651:     */       {
/*  652: 934 */         buffer = fillByteBuf();
/*  653: 935 */         return new DefaultHttpContent(buffer);
/*  654:     */       }
/*  655: 937 */       return null;
/*  656:     */     }
/*  657: 941 */     if (this.currentBuffer == null)
/*  658:     */     {
/*  659: 942 */       if (delimiter != null) {
/*  660: 943 */         this.currentBuffer = Unpooled.wrappedBuffer(new ByteBuf[] { buffer, delimiter });
/*  661:     */       } else {
/*  662: 945 */         this.currentBuffer = buffer;
/*  663:     */       }
/*  664:     */     }
/*  665: 948 */     else if (delimiter != null) {
/*  666: 949 */       this.currentBuffer = Unpooled.wrappedBuffer(new ByteBuf[] { this.currentBuffer, buffer, delimiter });
/*  667:     */     } else {
/*  668: 951 */       this.currentBuffer = Unpooled.wrappedBuffer(new ByteBuf[] { this.currentBuffer, buffer });
/*  669:     */     }
/*  670: 956 */     if (this.currentBuffer.readableBytes() < 8096)
/*  671:     */     {
/*  672: 957 */       this.currentData = null;
/*  673: 958 */       this.isKey = true;
/*  674: 959 */       return null;
/*  675:     */     }
/*  676: 962 */     ByteBuf buffer = fillByteBuf();
/*  677: 963 */     return new DefaultHttpContent(buffer);
/*  678:     */   }
/*  679:     */   
/*  680:     */   public HttpContent readChunk(ChannelHandlerContext ctx)
/*  681:     */     throws Exception
/*  682:     */   {
/*  683: 982 */     if (this.isLastChunkSent) {
/*  684: 983 */       return null;
/*  685:     */     }
/*  686: 985 */     return nextChunk();
/*  687:     */   }
/*  688:     */   
/*  689:     */   private HttpContent nextChunk()
/*  690:     */     throws HttpPostRequestEncoder.ErrorDataEncoderException
/*  691:     */   {
/*  692: 998 */     if (this.isLastChunk)
/*  693:     */     {
/*  694: 999 */       this.isLastChunkSent = true;
/*  695:1000 */       return LastHttpContent.EMPTY_LAST_CONTENT;
/*  696:     */     }
/*  697:1003 */     int size = 8096;
/*  698:1005 */     if (this.currentBuffer != null) {
/*  699:1006 */       size -= this.currentBuffer.readableBytes();
/*  700:     */     }
/*  701:1008 */     if (size <= 0)
/*  702:     */     {
/*  703:1010 */       ByteBuf buffer = fillByteBuf();
/*  704:1011 */       return new DefaultHttpContent(buffer);
/*  705:     */     }
/*  706:1014 */     if (this.currentData != null)
/*  707:     */     {
/*  708:1016 */       if (this.isMultipart)
/*  709:     */       {
/*  710:1017 */         HttpContent chunk = encodeNextChunkMultipart(size);
/*  711:1018 */         if (chunk != null) {
/*  712:1019 */           return chunk;
/*  713:     */         }
/*  714:     */       }
/*  715:     */       else
/*  716:     */       {
/*  717:1022 */         HttpContent chunk = encodeNextChunkUrlEncoded(size);
/*  718:1023 */         if (chunk != null) {
/*  719:1025 */           return chunk;
/*  720:     */         }
/*  721:     */       }
/*  722:1028 */       size = 8096 - this.currentBuffer.readableBytes();
/*  723:     */     }
/*  724:1030 */     if (!this.iterator.hasNext())
/*  725:     */     {
/*  726:1031 */       this.isLastChunk = true;
/*  727:     */       
/*  728:1033 */       ByteBuf buffer = this.currentBuffer;
/*  729:1034 */       this.currentBuffer = null;
/*  730:1035 */       return new DefaultHttpContent(buffer);
/*  731:     */     }
/*  732:1037 */     while ((size > 0) && (this.iterator.hasNext()))
/*  733:     */     {
/*  734:1038 */       this.currentData = ((InterfaceHttpData)this.iterator.next());
/*  735:     */       HttpContent chunk;
/*  736:     */       HttpContent chunk;
/*  737:1040 */       if (this.isMultipart) {
/*  738:1041 */         chunk = encodeNextChunkMultipart(size);
/*  739:     */       } else {
/*  740:1043 */         chunk = encodeNextChunkUrlEncoded(size);
/*  741:     */       }
/*  742:1045 */       if (chunk == null) {
/*  743:1047 */         size = 8096 - this.currentBuffer.readableBytes();
/*  744:     */       } else {
/*  745:1051 */         return chunk;
/*  746:     */       }
/*  747:     */     }
/*  748:1054 */     this.isLastChunk = true;
/*  749:1055 */     if (this.currentBuffer == null)
/*  750:     */     {
/*  751:1056 */       this.isLastChunkSent = true;
/*  752:     */       
/*  753:1058 */       return LastHttpContent.EMPTY_LAST_CONTENT;
/*  754:     */     }
/*  755:1061 */     ByteBuf buffer = this.currentBuffer;
/*  756:1062 */     this.currentBuffer = null;
/*  757:1063 */     return new DefaultHttpContent(buffer);
/*  758:     */   }
/*  759:     */   
/*  760:     */   public boolean isEndOfInput()
/*  761:     */     throws Exception
/*  762:     */   {
/*  763:1068 */     return this.isLastChunkSent;
/*  764:     */   }
/*  765:     */   
/*  766:     */   public void close()
/*  767:     */     throws Exception
/*  768:     */   {}
/*  769:     */   
/*  770:     */   public static class ErrorDataEncoderException
/*  771:     */     extends Exception
/*  772:     */   {
/*  773:     */     private static final long serialVersionUID = 5020247425493164465L;
/*  774:     */     
/*  775:     */     public ErrorDataEncoderException() {}
/*  776:     */     
/*  777:     */     public ErrorDataEncoderException(String msg)
/*  778:     */     {
/*  779:1081 */       super();
/*  780:     */     }
/*  781:     */     
/*  782:     */     public ErrorDataEncoderException(Throwable cause)
/*  783:     */     {
/*  784:1085 */       super();
/*  785:     */     }
/*  786:     */     
/*  787:     */     public ErrorDataEncoderException(String msg, Throwable cause)
/*  788:     */     {
/*  789:1089 */       super(cause);
/*  790:     */     }
/*  791:     */   }
/*  792:     */   
/*  793:     */   private static class WrappedHttpRequest
/*  794:     */     implements HttpRequest
/*  795:     */   {
/*  796:     */     private final HttpRequest request;
/*  797:     */     
/*  798:     */     WrappedHttpRequest(HttpRequest request)
/*  799:     */     {
/*  800:1096 */       this.request = request;
/*  801:     */     }
/*  802:     */     
/*  803:     */     public HttpRequest setProtocolVersion(HttpVersion version)
/*  804:     */     {
/*  805:1101 */       this.request.setProtocolVersion(version);
/*  806:1102 */       return this;
/*  807:     */     }
/*  808:     */     
/*  809:     */     public HttpRequest setMethod(HttpMethod method)
/*  810:     */     {
/*  811:1107 */       this.request.setMethod(method);
/*  812:1108 */       return this;
/*  813:     */     }
/*  814:     */     
/*  815:     */     public HttpRequest setUri(String uri)
/*  816:     */     {
/*  817:1113 */       this.request.setUri(uri);
/*  818:1114 */       return this;
/*  819:     */     }
/*  820:     */     
/*  821:     */     public HttpMethod getMethod()
/*  822:     */     {
/*  823:1119 */       return this.request.getMethod();
/*  824:     */     }
/*  825:     */     
/*  826:     */     public String getUri()
/*  827:     */     {
/*  828:1124 */       return this.request.getUri();
/*  829:     */     }
/*  830:     */     
/*  831:     */     public HttpVersion getProtocolVersion()
/*  832:     */     {
/*  833:1129 */       return this.request.getProtocolVersion();
/*  834:     */     }
/*  835:     */     
/*  836:     */     public HttpHeaders headers()
/*  837:     */     {
/*  838:1134 */       return this.request.headers();
/*  839:     */     }
/*  840:     */     
/*  841:     */     public DecoderResult getDecoderResult()
/*  842:     */     {
/*  843:1139 */       return this.request.getDecoderResult();
/*  844:     */     }
/*  845:     */     
/*  846:     */     public void setDecoderResult(DecoderResult result)
/*  847:     */     {
/*  848:1144 */       this.request.setDecoderResult(result);
/*  849:     */     }
/*  850:     */   }
/*  851:     */   
/*  852:     */   private static final class WrappedFullHttpRequest
/*  853:     */     extends HttpPostRequestEncoder.WrappedHttpRequest
/*  854:     */     implements FullHttpRequest
/*  855:     */   {
/*  856:     */     private final HttpContent content;
/*  857:     */     
/*  858:     */     private WrappedFullHttpRequest(HttpRequest request, HttpContent content)
/*  859:     */     {
/*  860:1151 */       super();
/*  861:1152 */       this.content = content;
/*  862:     */     }
/*  863:     */     
/*  864:     */     public FullHttpRequest setProtocolVersion(HttpVersion version)
/*  865:     */     {
/*  866:1157 */       super.setProtocolVersion(version);
/*  867:1158 */       return this;
/*  868:     */     }
/*  869:     */     
/*  870:     */     public FullHttpRequest setMethod(HttpMethod method)
/*  871:     */     {
/*  872:1163 */       super.setMethod(method);
/*  873:1164 */       return this;
/*  874:     */     }
/*  875:     */     
/*  876:     */     public FullHttpRequest setUri(String uri)
/*  877:     */     {
/*  878:1169 */       super.setUri(uri);
/*  879:1170 */       return this;
/*  880:     */     }
/*  881:     */     
/*  882:     */     public FullHttpRequest copy()
/*  883:     */     {
/*  884:1175 */       DefaultFullHttpRequest copy = new DefaultFullHttpRequest(getProtocolVersion(), getMethod(), getUri(), content().copy());
/*  885:     */       
/*  886:1177 */       copy.headers().set(headers());
/*  887:1178 */       copy.trailingHeaders().set(trailingHeaders());
/*  888:1179 */       return copy;
/*  889:     */     }
/*  890:     */     
/*  891:     */     public FullHttpRequest duplicate()
/*  892:     */     {
/*  893:1184 */       DefaultFullHttpRequest duplicate = new DefaultFullHttpRequest(getProtocolVersion(), getMethod(), getUri(), content().duplicate());
/*  894:     */       
/*  895:1186 */       duplicate.headers().set(headers());
/*  896:1187 */       duplicate.trailingHeaders().set(trailingHeaders());
/*  897:1188 */       return duplicate;
/*  898:     */     }
/*  899:     */     
/*  900:     */     public FullHttpRequest retain(int increment)
/*  901:     */     {
/*  902:1193 */       this.content.retain(increment);
/*  903:1194 */       return this;
/*  904:     */     }
/*  905:     */     
/*  906:     */     public FullHttpRequest retain()
/*  907:     */     {
/*  908:1199 */       this.content.retain();
/*  909:1200 */       return this;
/*  910:     */     }
/*  911:     */     
/*  912:     */     public ByteBuf content()
/*  913:     */     {
/*  914:1205 */       return this.content.content();
/*  915:     */     }
/*  916:     */     
/*  917:     */     public HttpHeaders trailingHeaders()
/*  918:     */     {
/*  919:1210 */       if ((this.content instanceof LastHttpContent)) {
/*  920:1211 */         return ((LastHttpContent)this.content).trailingHeaders();
/*  921:     */       }
/*  922:1213 */       return HttpHeaders.EMPTY_HEADERS;
/*  923:     */     }
/*  924:     */     
/*  925:     */     public int refCnt()
/*  926:     */     {
/*  927:1219 */       return this.content.refCnt();
/*  928:     */     }
/*  929:     */     
/*  930:     */     public boolean release()
/*  931:     */     {
/*  932:1224 */       return this.content.release();
/*  933:     */     }
/*  934:     */     
/*  935:     */     public boolean release(int decrement)
/*  936:     */     {
/*  937:1229 */       return this.content.release(decrement);
/*  938:     */     }
/*  939:     */   }
/*  940:     */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.multipart.HttpPostRequestEncoder
 * JD-Core Version:    0.7.0.1
 */