/*   1:    */ package io.netty.handler.codec.http;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.util.CharsetUtil;
/*   5:    */ 
/*   6:    */ public class HttpResponseStatus
/*   7:    */   implements Comparable<HttpResponseStatus>
/*   8:    */ {
/*   9: 33 */   public static final HttpResponseStatus CONTINUE = new HttpResponseStatus(100, "Continue", true);
/*  10: 38 */   public static final HttpResponseStatus SWITCHING_PROTOCOLS = new HttpResponseStatus(101, "Switching Protocols", true);
/*  11: 44 */   public static final HttpResponseStatus PROCESSING = new HttpResponseStatus(102, "Processing", true);
/*  12: 49 */   public static final HttpResponseStatus OK = new HttpResponseStatus(200, "OK", true);
/*  13: 54 */   public static final HttpResponseStatus CREATED = new HttpResponseStatus(201, "Created", true);
/*  14: 59 */   public static final HttpResponseStatus ACCEPTED = new HttpResponseStatus(202, "Accepted", true);
/*  15: 64 */   public static final HttpResponseStatus NON_AUTHORITATIVE_INFORMATION = new HttpResponseStatus(203, "Non-Authoritative Information", true);
/*  16: 70 */   public static final HttpResponseStatus NO_CONTENT = new HttpResponseStatus(204, "No Content", true);
/*  17: 75 */   public static final HttpResponseStatus RESET_CONTENT = new HttpResponseStatus(205, "Reset Content", true);
/*  18: 80 */   public static final HttpResponseStatus PARTIAL_CONTENT = new HttpResponseStatus(206, "Partial Content", true);
/*  19: 85 */   public static final HttpResponseStatus MULTI_STATUS = new HttpResponseStatus(207, "Multi-Status", true);
/*  20: 90 */   public static final HttpResponseStatus MULTIPLE_CHOICES = new HttpResponseStatus(300, "Multiple Choices", true);
/*  21: 95 */   public static final HttpResponseStatus MOVED_PERMANENTLY = new HttpResponseStatus(301, "Moved Permanently", true);
/*  22:100 */   public static final HttpResponseStatus FOUND = new HttpResponseStatus(302, "Found", true);
/*  23:105 */   public static final HttpResponseStatus SEE_OTHER = new HttpResponseStatus(303, "See Other", true);
/*  24:110 */   public static final HttpResponseStatus NOT_MODIFIED = new HttpResponseStatus(304, "Not Modified", true);
/*  25:115 */   public static final HttpResponseStatus USE_PROXY = new HttpResponseStatus(305, "Use Proxy", true);
/*  26:120 */   public static final HttpResponseStatus TEMPORARY_REDIRECT = new HttpResponseStatus(307, "Temporary Redirect", true);
/*  27:125 */   public static final HttpResponseStatus BAD_REQUEST = new HttpResponseStatus(400, "Bad Request", true);
/*  28:130 */   public static final HttpResponseStatus UNAUTHORIZED = new HttpResponseStatus(401, "Unauthorized", true);
/*  29:135 */   public static final HttpResponseStatus PAYMENT_REQUIRED = new HttpResponseStatus(402, "Payment Required", true);
/*  30:140 */   public static final HttpResponseStatus FORBIDDEN = new HttpResponseStatus(403, "Forbidden", true);
/*  31:145 */   public static final HttpResponseStatus NOT_FOUND = new HttpResponseStatus(404, "Not Found", true);
/*  32:150 */   public static final HttpResponseStatus METHOD_NOT_ALLOWED = new HttpResponseStatus(405, "Method Not Allowed", true);
/*  33:155 */   public static final HttpResponseStatus NOT_ACCEPTABLE = new HttpResponseStatus(406, "Not Acceptable", true);
/*  34:160 */   public static final HttpResponseStatus PROXY_AUTHENTICATION_REQUIRED = new HttpResponseStatus(407, "Proxy Authentication Required", true);
/*  35:166 */   public static final HttpResponseStatus REQUEST_TIMEOUT = new HttpResponseStatus(408, "Request Timeout", true);
/*  36:171 */   public static final HttpResponseStatus CONFLICT = new HttpResponseStatus(409, "Conflict", true);
/*  37:176 */   public static final HttpResponseStatus GONE = new HttpResponseStatus(410, "Gone", true);
/*  38:181 */   public static final HttpResponseStatus LENGTH_REQUIRED = new HttpResponseStatus(411, "Length Required", true);
/*  39:186 */   public static final HttpResponseStatus PRECONDITION_FAILED = new HttpResponseStatus(412, "Precondition Failed", true);
/*  40:192 */   public static final HttpResponseStatus REQUEST_ENTITY_TOO_LARGE = new HttpResponseStatus(413, "Request Entity Too Large", true);
/*  41:198 */   public static final HttpResponseStatus REQUEST_URI_TOO_LONG = new HttpResponseStatus(414, "Request-URI Too Long", true);
/*  42:204 */   public static final HttpResponseStatus UNSUPPORTED_MEDIA_TYPE = new HttpResponseStatus(415, "Unsupported Media Type", true);
/*  43:210 */   public static final HttpResponseStatus REQUESTED_RANGE_NOT_SATISFIABLE = new HttpResponseStatus(416, "Requested Range Not Satisfiable", true);
/*  44:216 */   public static final HttpResponseStatus EXPECTATION_FAILED = new HttpResponseStatus(417, "Expectation Failed", true);
/*  45:222 */   public static final HttpResponseStatus UNPROCESSABLE_ENTITY = new HttpResponseStatus(422, "Unprocessable Entity", true);
/*  46:228 */   public static final HttpResponseStatus LOCKED = new HttpResponseStatus(423, "Locked", true);
/*  47:234 */   public static final HttpResponseStatus FAILED_DEPENDENCY = new HttpResponseStatus(424, "Failed Dependency", true);
/*  48:239 */   public static final HttpResponseStatus UNORDERED_COLLECTION = new HttpResponseStatus(425, "Unordered Collection", true);
/*  49:245 */   public static final HttpResponseStatus UPGRADE_REQUIRED = new HttpResponseStatus(426, "Upgrade Required", true);
/*  50:250 */   public static final HttpResponseStatus PRECONDITION_REQUIRED = new HttpResponseStatus(428, "Precondition Required", true);
/*  51:256 */   public static final HttpResponseStatus TOO_MANY_REQUESTS = new HttpResponseStatus(429, "Too Many Requests", true);
/*  52:261 */   public static final HttpResponseStatus REQUEST_HEADER_FIELDS_TOO_LARGE = new HttpResponseStatus(431, "Request Header Fields Too Large", true);
/*  53:267 */   public static final HttpResponseStatus INTERNAL_SERVER_ERROR = new HttpResponseStatus(500, "Internal Server Error", true);
/*  54:273 */   public static final HttpResponseStatus NOT_IMPLEMENTED = new HttpResponseStatus(501, "Not Implemented", true);
/*  55:278 */   public static final HttpResponseStatus BAD_GATEWAY = new HttpResponseStatus(502, "Bad Gateway", true);
/*  56:283 */   public static final HttpResponseStatus SERVICE_UNAVAILABLE = new HttpResponseStatus(503, "Service Unavailable", true);
/*  57:289 */   public static final HttpResponseStatus GATEWAY_TIMEOUT = new HttpResponseStatus(504, "Gateway Timeout", true);
/*  58:294 */   public static final HttpResponseStatus HTTP_VERSION_NOT_SUPPORTED = new HttpResponseStatus(505, "HTTP Version Not Supported", true);
/*  59:300 */   public static final HttpResponseStatus VARIANT_ALSO_NEGOTIATES = new HttpResponseStatus(506, "Variant Also Negotiates", true);
/*  60:306 */   public static final HttpResponseStatus INSUFFICIENT_STORAGE = new HttpResponseStatus(507, "Insufficient Storage", true);
/*  61:312 */   public static final HttpResponseStatus NOT_EXTENDED = new HttpResponseStatus(510, "Not Extended", true);
/*  62:317 */   public static final HttpResponseStatus NETWORK_AUTHENTICATION_REQUIRED = new HttpResponseStatus(511, "Network Authentication Required", true);
/*  63:    */   private final int code;
/*  64:    */   private final String reasonPhrase;
/*  65:    */   private final byte[] bytes;
/*  66:    */   
/*  67:    */   public static HttpResponseStatus valueOf(int code)
/*  68:    */   {
/*  69:326 */     switch (code)
/*  70:    */     {
/*  71:    */     case 100: 
/*  72:328 */       return CONTINUE;
/*  73:    */     case 101: 
/*  74:330 */       return SWITCHING_PROTOCOLS;
/*  75:    */     case 102: 
/*  76:332 */       return PROCESSING;
/*  77:    */     case 200: 
/*  78:334 */       return OK;
/*  79:    */     case 201: 
/*  80:336 */       return CREATED;
/*  81:    */     case 202: 
/*  82:338 */       return ACCEPTED;
/*  83:    */     case 203: 
/*  84:340 */       return NON_AUTHORITATIVE_INFORMATION;
/*  85:    */     case 204: 
/*  86:342 */       return NO_CONTENT;
/*  87:    */     case 205: 
/*  88:344 */       return RESET_CONTENT;
/*  89:    */     case 206: 
/*  90:346 */       return PARTIAL_CONTENT;
/*  91:    */     case 207: 
/*  92:348 */       return MULTI_STATUS;
/*  93:    */     case 300: 
/*  94:350 */       return MULTIPLE_CHOICES;
/*  95:    */     case 301: 
/*  96:352 */       return MOVED_PERMANENTLY;
/*  97:    */     case 302: 
/*  98:354 */       return FOUND;
/*  99:    */     case 303: 
/* 100:356 */       return SEE_OTHER;
/* 101:    */     case 304: 
/* 102:358 */       return NOT_MODIFIED;
/* 103:    */     case 305: 
/* 104:360 */       return USE_PROXY;
/* 105:    */     case 307: 
/* 106:362 */       return TEMPORARY_REDIRECT;
/* 107:    */     case 400: 
/* 108:364 */       return BAD_REQUEST;
/* 109:    */     case 401: 
/* 110:366 */       return UNAUTHORIZED;
/* 111:    */     case 402: 
/* 112:368 */       return PAYMENT_REQUIRED;
/* 113:    */     case 403: 
/* 114:370 */       return FORBIDDEN;
/* 115:    */     case 404: 
/* 116:372 */       return NOT_FOUND;
/* 117:    */     case 405: 
/* 118:374 */       return METHOD_NOT_ALLOWED;
/* 119:    */     case 406: 
/* 120:376 */       return NOT_ACCEPTABLE;
/* 121:    */     case 407: 
/* 122:378 */       return PROXY_AUTHENTICATION_REQUIRED;
/* 123:    */     case 408: 
/* 124:380 */       return REQUEST_TIMEOUT;
/* 125:    */     case 409: 
/* 126:382 */       return CONFLICT;
/* 127:    */     case 410: 
/* 128:384 */       return GONE;
/* 129:    */     case 411: 
/* 130:386 */       return LENGTH_REQUIRED;
/* 131:    */     case 412: 
/* 132:388 */       return PRECONDITION_FAILED;
/* 133:    */     case 413: 
/* 134:390 */       return REQUEST_ENTITY_TOO_LARGE;
/* 135:    */     case 414: 
/* 136:392 */       return REQUEST_URI_TOO_LONG;
/* 137:    */     case 415: 
/* 138:394 */       return UNSUPPORTED_MEDIA_TYPE;
/* 139:    */     case 416: 
/* 140:396 */       return REQUESTED_RANGE_NOT_SATISFIABLE;
/* 141:    */     case 417: 
/* 142:398 */       return EXPECTATION_FAILED;
/* 143:    */     case 422: 
/* 144:400 */       return UNPROCESSABLE_ENTITY;
/* 145:    */     case 423: 
/* 146:402 */       return LOCKED;
/* 147:    */     case 424: 
/* 148:404 */       return FAILED_DEPENDENCY;
/* 149:    */     case 425: 
/* 150:406 */       return UNORDERED_COLLECTION;
/* 151:    */     case 426: 
/* 152:408 */       return UPGRADE_REQUIRED;
/* 153:    */     case 428: 
/* 154:410 */       return PRECONDITION_REQUIRED;
/* 155:    */     case 429: 
/* 156:412 */       return TOO_MANY_REQUESTS;
/* 157:    */     case 431: 
/* 158:414 */       return REQUEST_HEADER_FIELDS_TOO_LARGE;
/* 159:    */     case 500: 
/* 160:416 */       return INTERNAL_SERVER_ERROR;
/* 161:    */     case 501: 
/* 162:418 */       return NOT_IMPLEMENTED;
/* 163:    */     case 502: 
/* 164:420 */       return BAD_GATEWAY;
/* 165:    */     case 503: 
/* 166:422 */       return SERVICE_UNAVAILABLE;
/* 167:    */     case 504: 
/* 168:424 */       return GATEWAY_TIMEOUT;
/* 169:    */     case 505: 
/* 170:426 */       return HTTP_VERSION_NOT_SUPPORTED;
/* 171:    */     case 506: 
/* 172:428 */       return VARIANT_ALSO_NEGOTIATES;
/* 173:    */     case 507: 
/* 174:430 */       return INSUFFICIENT_STORAGE;
/* 175:    */     case 510: 
/* 176:432 */       return NOT_EXTENDED;
/* 177:    */     case 511: 
/* 178:434 */       return NETWORK_AUTHENTICATION_REQUIRED;
/* 179:    */     }
/* 180:    */     String reasonPhrase;
/* 181:    */     String reasonPhrase;
/* 182:439 */     if (code < 100)
/* 183:    */     {
/* 184:440 */       reasonPhrase = "Unknown Status";
/* 185:    */     }
/* 186:    */     else
/* 187:    */     {
/* 188:    */       String reasonPhrase;
/* 189:441 */       if (code < 200)
/* 190:    */       {
/* 191:442 */         reasonPhrase = "Informational";
/* 192:    */       }
/* 193:    */       else
/* 194:    */       {
/* 195:    */         String reasonPhrase;
/* 196:443 */         if (code < 300)
/* 197:    */         {
/* 198:444 */           reasonPhrase = "Successful";
/* 199:    */         }
/* 200:    */         else
/* 201:    */         {
/* 202:    */           String reasonPhrase;
/* 203:445 */           if (code < 400)
/* 204:    */           {
/* 205:446 */             reasonPhrase = "Redirection";
/* 206:    */           }
/* 207:    */           else
/* 208:    */           {
/* 209:    */             String reasonPhrase;
/* 210:447 */             if (code < 500)
/* 211:    */             {
/* 212:448 */               reasonPhrase = "Client Error";
/* 213:    */             }
/* 214:    */             else
/* 215:    */             {
/* 216:    */               String reasonPhrase;
/* 217:449 */               if (code < 600) {
/* 218:450 */                 reasonPhrase = "Server Error";
/* 219:    */               } else {
/* 220:452 */                 reasonPhrase = "Unknown Status";
/* 221:    */               }
/* 222:    */             }
/* 223:    */           }
/* 224:    */         }
/* 225:    */       }
/* 226:    */     }
/* 227:455 */     return new HttpResponseStatus(code, reasonPhrase + " (" + code + ')');
/* 228:    */   }
/* 229:    */   
/* 230:    */   public HttpResponseStatus(int code, String reasonPhrase)
/* 231:    */   {
/* 232:468 */     this(code, reasonPhrase, false);
/* 233:    */   }
/* 234:    */   
/* 235:    */   private HttpResponseStatus(int code, String reasonPhrase, boolean bytes)
/* 236:    */   {
/* 237:472 */     if (code < 0) {
/* 238:473 */       throw new IllegalArgumentException("code: " + code + " (expected: 0+)");
/* 239:    */     }
/* 240:477 */     if (reasonPhrase == null) {
/* 241:478 */       throw new NullPointerException("reasonPhrase");
/* 242:    */     }
/* 243:481 */     for (int i = 0; i < reasonPhrase.length(); i++)
/* 244:    */     {
/* 245:482 */       char c = reasonPhrase.charAt(i);
/* 246:484 */       switch (c)
/* 247:    */       {
/* 248:    */       case '\n': 
/* 249:    */       case '\r': 
/* 250:486 */         throw new IllegalArgumentException("reasonPhrase contains one of the following prohibited characters: \\r\\n: " + reasonPhrase);
/* 251:    */       }
/* 252:    */     }
/* 253:492 */     this.code = code;
/* 254:493 */     this.reasonPhrase = reasonPhrase;
/* 255:494 */     if (bytes) {
/* 256:495 */       this.bytes = (code + " " + reasonPhrase).getBytes(CharsetUtil.US_ASCII);
/* 257:    */     } else {
/* 258:497 */       this.bytes = null;
/* 259:    */     }
/* 260:    */   }
/* 261:    */   
/* 262:    */   public int code()
/* 263:    */   {
/* 264:505 */     return this.code;
/* 265:    */   }
/* 266:    */   
/* 267:    */   public String reasonPhrase()
/* 268:    */   {
/* 269:512 */     return this.reasonPhrase;
/* 270:    */   }
/* 271:    */   
/* 272:    */   public int hashCode()
/* 273:    */   {
/* 274:517 */     return code();
/* 275:    */   }
/* 276:    */   
/* 277:    */   public boolean equals(Object o)
/* 278:    */   {
/* 279:526 */     if (!(o instanceof HttpResponseStatus)) {
/* 280:527 */       return false;
/* 281:    */     }
/* 282:530 */     return code() == ((HttpResponseStatus)o).code();
/* 283:    */   }
/* 284:    */   
/* 285:    */   public int compareTo(HttpResponseStatus o)
/* 286:    */   {
/* 287:539 */     return code() - o.code();
/* 288:    */   }
/* 289:    */   
/* 290:    */   public String toString()
/* 291:    */   {
/* 292:544 */     StringBuilder buf = new StringBuilder(this.reasonPhrase.length() + 5);
/* 293:545 */     buf.append(this.code);
/* 294:546 */     buf.append(' ');
/* 295:547 */     buf.append(this.reasonPhrase);
/* 296:548 */     return buf.toString();
/* 297:    */   }
/* 298:    */   
/* 299:    */   void encode(ByteBuf buf)
/* 300:    */   {
/* 301:552 */     if (this.bytes == null)
/* 302:    */     {
/* 303:553 */       HttpHeaders.encodeAscii0(String.valueOf(code()), buf);
/* 304:554 */       buf.writeByte(32);
/* 305:555 */       HttpHeaders.encodeAscii0(String.valueOf(reasonPhrase()), buf);
/* 306:    */     }
/* 307:    */     else
/* 308:    */     {
/* 309:557 */       buf.writeBytes(this.bytes);
/* 310:    */     }
/* 311:    */   }
/* 312:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.HttpResponseStatus
 * JD-Core Version:    0.7.0.1
 */