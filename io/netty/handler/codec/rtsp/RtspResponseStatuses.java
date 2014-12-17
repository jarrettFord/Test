/*   1:    */ package io.netty.handler.codec.rtsp;
/*   2:    */ 
/*   3:    */ import io.netty.handler.codec.http.HttpResponseStatus;
/*   4:    */ 
/*   5:    */ public final class RtspResponseStatuses
/*   6:    */ {
/*   7: 28 */   public static final HttpResponseStatus CONTINUE = HttpResponseStatus.CONTINUE;
/*   8: 33 */   public static final HttpResponseStatus OK = HttpResponseStatus.OK;
/*   9: 38 */   public static final HttpResponseStatus CREATED = HttpResponseStatus.CREATED;
/*  10: 43 */   public static final HttpResponseStatus LOW_STORAGE_SPACE = new HttpResponseStatus(250, "Low on Storage Space");
/*  11: 49 */   public static final HttpResponseStatus MULTIPLE_CHOICES = HttpResponseStatus.MULTIPLE_CHOICES;
/*  12: 54 */   public static final HttpResponseStatus MOVED_PERMANENTLY = HttpResponseStatus.MOVED_PERMANENTLY;
/*  13: 59 */   public static final HttpResponseStatus MOVED_TEMPORARILY = new HttpResponseStatus(302, "Moved Temporarily");
/*  14: 64 */   public static final HttpResponseStatus NOT_MODIFIED = HttpResponseStatus.NOT_MODIFIED;
/*  15: 69 */   public static final HttpResponseStatus USE_PROXY = HttpResponseStatus.USE_PROXY;
/*  16: 74 */   public static final HttpResponseStatus BAD_REQUEST = HttpResponseStatus.BAD_REQUEST;
/*  17: 79 */   public static final HttpResponseStatus UNAUTHORIZED = HttpResponseStatus.UNAUTHORIZED;
/*  18: 84 */   public static final HttpResponseStatus PAYMENT_REQUIRED = HttpResponseStatus.PAYMENT_REQUIRED;
/*  19: 89 */   public static final HttpResponseStatus FORBIDDEN = HttpResponseStatus.FORBIDDEN;
/*  20: 94 */   public static final HttpResponseStatus NOT_FOUND = HttpResponseStatus.NOT_FOUND;
/*  21: 99 */   public static final HttpResponseStatus METHOD_NOT_ALLOWED = HttpResponseStatus.METHOD_NOT_ALLOWED;
/*  22:104 */   public static final HttpResponseStatus NOT_ACCEPTABLE = HttpResponseStatus.NOT_ACCEPTABLE;
/*  23:109 */   public static final HttpResponseStatus PROXY_AUTHENTICATION_REQUIRED = HttpResponseStatus.PROXY_AUTHENTICATION_REQUIRED;
/*  24:115 */   public static final HttpResponseStatus REQUEST_TIMEOUT = HttpResponseStatus.REQUEST_TIMEOUT;
/*  25:120 */   public static final HttpResponseStatus GONE = HttpResponseStatus.GONE;
/*  26:125 */   public static final HttpResponseStatus LENGTH_REQUIRED = HttpResponseStatus.LENGTH_REQUIRED;
/*  27:130 */   public static final HttpResponseStatus PRECONDITION_FAILED = HttpResponseStatus.PRECONDITION_FAILED;
/*  28:135 */   public static final HttpResponseStatus REQUEST_ENTITY_TOO_LARGE = HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE;
/*  29:140 */   public static final HttpResponseStatus REQUEST_URI_TOO_LONG = HttpResponseStatus.REQUEST_URI_TOO_LONG;
/*  30:145 */   public static final HttpResponseStatus UNSUPPORTED_MEDIA_TYPE = HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE;
/*  31:150 */   public static final HttpResponseStatus PARAMETER_NOT_UNDERSTOOD = new HttpResponseStatus(451, "Parameter Not Understood");
/*  32:156 */   public static final HttpResponseStatus CONFERENCE_NOT_FOUND = new HttpResponseStatus(452, "Conference Not Found");
/*  33:162 */   public static final HttpResponseStatus NOT_ENOUGH_BANDWIDTH = new HttpResponseStatus(453, "Not Enough Bandwidth");
/*  34:168 */   public static final HttpResponseStatus SESSION_NOT_FOUND = new HttpResponseStatus(454, "Session Not Found");
/*  35:174 */   public static final HttpResponseStatus METHOD_NOT_VALID = new HttpResponseStatus(455, "Method Not Valid in This State");
/*  36:180 */   public static final HttpResponseStatus HEADER_FIELD_NOT_VALID = new HttpResponseStatus(456, "Header Field Not Valid for Resource");
/*  37:186 */   public static final HttpResponseStatus INVALID_RANGE = new HttpResponseStatus(457, "Invalid Range");
/*  38:192 */   public static final HttpResponseStatus PARAMETER_IS_READONLY = new HttpResponseStatus(458, "Parameter Is Read-Only");
/*  39:198 */   public static final HttpResponseStatus AGGREGATE_OPERATION_NOT_ALLOWED = new HttpResponseStatus(459, "Aggregate operation not allowed");
/*  40:204 */   public static final HttpResponseStatus ONLY_AGGREGATE_OPERATION_ALLOWED = new HttpResponseStatus(460, "Only Aggregate operation allowed");
/*  41:210 */   public static final HttpResponseStatus UNSUPPORTED_TRANSPORT = new HttpResponseStatus(461, "Unsupported transport");
/*  42:216 */   public static final HttpResponseStatus DESTINATION_UNREACHABLE = new HttpResponseStatus(462, "Destination unreachable");
/*  43:222 */   public static final HttpResponseStatus KEY_MANAGEMENT_FAILURE = new HttpResponseStatus(463, "Key management failure");
/*  44:228 */   public static final HttpResponseStatus INTERNAL_SERVER_ERROR = HttpResponseStatus.INTERNAL_SERVER_ERROR;
/*  45:233 */   public static final HttpResponseStatus NOT_IMPLEMENTED = HttpResponseStatus.NOT_IMPLEMENTED;
/*  46:238 */   public static final HttpResponseStatus BAD_GATEWAY = HttpResponseStatus.BAD_GATEWAY;
/*  47:243 */   public static final HttpResponseStatus SERVICE_UNAVAILABLE = HttpResponseStatus.SERVICE_UNAVAILABLE;
/*  48:248 */   public static final HttpResponseStatus GATEWAY_TIMEOUT = HttpResponseStatus.GATEWAY_TIMEOUT;
/*  49:253 */   public static final HttpResponseStatus RTSP_VERSION_NOT_SUPPORTED = new HttpResponseStatus(505, "RTSP Version not supported");
/*  50:259 */   public static final HttpResponseStatus OPTION_NOT_SUPPORTED = new HttpResponseStatus(551, "Option not supported");
/*  51:    */   
/*  52:    */   public static HttpResponseStatus valueOf(int code)
/*  53:    */   {
/*  54:268 */     switch (code)
/*  55:    */     {
/*  56:    */     case 250: 
/*  57:269 */       return LOW_STORAGE_SPACE;
/*  58:    */     case 302: 
/*  59:270 */       return MOVED_TEMPORARILY;
/*  60:    */     case 451: 
/*  61:271 */       return PARAMETER_NOT_UNDERSTOOD;
/*  62:    */     case 452: 
/*  63:272 */       return CONFERENCE_NOT_FOUND;
/*  64:    */     case 453: 
/*  65:273 */       return NOT_ENOUGH_BANDWIDTH;
/*  66:    */     case 454: 
/*  67:274 */       return SESSION_NOT_FOUND;
/*  68:    */     case 455: 
/*  69:275 */       return METHOD_NOT_VALID;
/*  70:    */     case 456: 
/*  71:276 */       return HEADER_FIELD_NOT_VALID;
/*  72:    */     case 457: 
/*  73:277 */       return INVALID_RANGE;
/*  74:    */     case 458: 
/*  75:278 */       return PARAMETER_IS_READONLY;
/*  76:    */     case 459: 
/*  77:279 */       return AGGREGATE_OPERATION_NOT_ALLOWED;
/*  78:    */     case 460: 
/*  79:280 */       return ONLY_AGGREGATE_OPERATION_ALLOWED;
/*  80:    */     case 461: 
/*  81:281 */       return UNSUPPORTED_TRANSPORT;
/*  82:    */     case 462: 
/*  83:282 */       return DESTINATION_UNREACHABLE;
/*  84:    */     case 463: 
/*  85:283 */       return KEY_MANAGEMENT_FAILURE;
/*  86:    */     case 505: 
/*  87:284 */       return RTSP_VERSION_NOT_SUPPORTED;
/*  88:    */     case 551: 
/*  89:285 */       return OPTION_NOT_SUPPORTED;
/*  90:    */     }
/*  91:286 */     return HttpResponseStatus.valueOf(code);
/*  92:    */   }
/*  93:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.rtsp.RtspResponseStatuses
 * JD-Core Version:    0.7.0.1
 */