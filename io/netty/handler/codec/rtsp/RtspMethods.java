/*   1:    */ package io.netty.handler.codec.rtsp;
/*   2:    */ 
/*   3:    */ import io.netty.handler.codec.http.HttpMethod;
/*   4:    */ import java.util.HashMap;
/*   5:    */ import java.util.Map;
/*   6:    */ 
/*   7:    */ public final class RtspMethods
/*   8:    */ {
/*   9: 35 */   public static final HttpMethod OPTIONS = HttpMethod.OPTIONS;
/*  10: 41 */   public static final HttpMethod DESCRIBE = new HttpMethod("DESCRIBE");
/*  11: 48 */   public static final HttpMethod ANNOUNCE = new HttpMethod("ANNOUNCE");
/*  12: 54 */   public static final HttpMethod SETUP = new HttpMethod("SETUP");
/*  13: 60 */   public static final HttpMethod PLAY = new HttpMethod("PLAY");
/*  14: 66 */   public static final HttpMethod PAUSE = new HttpMethod("PAUSE");
/*  15: 72 */   public static final HttpMethod TEARDOWN = new HttpMethod("TEARDOWN");
/*  16: 78 */   public static final HttpMethod GET_PARAMETER = new HttpMethod("GET_PARAMETER");
/*  17: 84 */   public static final HttpMethod SET_PARAMETER = new HttpMethod("SET_PARAMETER");
/*  18: 90 */   public static final HttpMethod REDIRECT = new HttpMethod("REDIRECT");
/*  19: 96 */   public static final HttpMethod RECORD = new HttpMethod("RECORD");
/*  20: 98 */   private static final Map<String, HttpMethod> methodMap = new HashMap();
/*  21:    */   
/*  22:    */   static
/*  23:    */   {
/*  24:101 */     methodMap.put(DESCRIBE.toString(), DESCRIBE);
/*  25:102 */     methodMap.put(ANNOUNCE.toString(), ANNOUNCE);
/*  26:103 */     methodMap.put(GET_PARAMETER.toString(), GET_PARAMETER);
/*  27:104 */     methodMap.put(OPTIONS.toString(), OPTIONS);
/*  28:105 */     methodMap.put(PAUSE.toString(), PAUSE);
/*  29:106 */     methodMap.put(PLAY.toString(), PLAY);
/*  30:107 */     methodMap.put(RECORD.toString(), RECORD);
/*  31:108 */     methodMap.put(REDIRECT.toString(), REDIRECT);
/*  32:109 */     methodMap.put(SETUP.toString(), SETUP);
/*  33:110 */     methodMap.put(SET_PARAMETER.toString(), SET_PARAMETER);
/*  34:111 */     methodMap.put(TEARDOWN.toString(), TEARDOWN);
/*  35:    */   }
/*  36:    */   
/*  37:    */   public static HttpMethod valueOf(String name)
/*  38:    */   {
/*  39:120 */     if (name == null) {
/*  40:121 */       throw new NullPointerException("name");
/*  41:    */     }
/*  42:124 */     name = name.trim().toUpperCase();
/*  43:125 */     if (name.isEmpty()) {
/*  44:126 */       throw new IllegalArgumentException("empty name");
/*  45:    */     }
/*  46:129 */     HttpMethod result = (HttpMethod)methodMap.get(name);
/*  47:130 */     if (result != null) {
/*  48:131 */       return result;
/*  49:    */     }
/*  50:133 */     return new HttpMethod(name);
/*  51:    */   }
/*  52:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.rtsp.RtspMethods
 * JD-Core Version:    0.7.0.1
 */