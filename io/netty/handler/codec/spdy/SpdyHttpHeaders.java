/*   1:    */ package io.netty.handler.codec.spdy;
/*   2:    */ 
/*   3:    */ import io.netty.handler.codec.http.HttpHeaders;
/*   4:    */ import io.netty.handler.codec.http.HttpMessage;
/*   5:    */ 
/*   6:    */ public final class SpdyHttpHeaders
/*   7:    */ {
/*   8:    */   public static void removeStreamId(HttpMessage message)
/*   9:    */   {
/*  10: 62 */     message.headers().remove("X-SPDY-Stream-ID");
/*  11:    */   }
/*  12:    */   
/*  13:    */   public static int getStreamId(HttpMessage message)
/*  14:    */   {
/*  15: 69 */     return HttpHeaders.getIntHeader(message, "X-SPDY-Stream-ID");
/*  16:    */   }
/*  17:    */   
/*  18:    */   public static void setStreamId(HttpMessage message, int streamId)
/*  19:    */   {
/*  20: 76 */     HttpHeaders.setIntHeader(message, "X-SPDY-Stream-ID", streamId);
/*  21:    */   }
/*  22:    */   
/*  23:    */   public static void removeAssociatedToStreamId(HttpMessage message)
/*  24:    */   {
/*  25: 83 */     message.headers().remove("X-SPDY-Associated-To-Stream-ID");
/*  26:    */   }
/*  27:    */   
/*  28:    */   public static int getAssociatedToStreamId(HttpMessage message)
/*  29:    */   {
/*  30: 93 */     return HttpHeaders.getIntHeader(message, "X-SPDY-Associated-To-Stream-ID", 0);
/*  31:    */   }
/*  32:    */   
/*  33:    */   public static void setAssociatedToStreamId(HttpMessage message, int associatedToStreamId)
/*  34:    */   {
/*  35:100 */     HttpHeaders.setIntHeader(message, "X-SPDY-Associated-To-Stream-ID", associatedToStreamId);
/*  36:    */   }
/*  37:    */   
/*  38:    */   public static void removePriority(HttpMessage message)
/*  39:    */   {
/*  40:107 */     message.headers().remove("X-SPDY-Priority");
/*  41:    */   }
/*  42:    */   
/*  43:    */   public static byte getPriority(HttpMessage message)
/*  44:    */   {
/*  45:117 */     return (byte)HttpHeaders.getIntHeader(message, "X-SPDY-Priority", 0);
/*  46:    */   }
/*  47:    */   
/*  48:    */   public static void setPriority(HttpMessage message, byte priority)
/*  49:    */   {
/*  50:124 */     HttpHeaders.setIntHeader(message, "X-SPDY-Priority", priority);
/*  51:    */   }
/*  52:    */   
/*  53:    */   public static void removeUrl(HttpMessage message)
/*  54:    */   {
/*  55:131 */     message.headers().remove("X-SPDY-URL");
/*  56:    */   }
/*  57:    */   
/*  58:    */   public static String getUrl(HttpMessage message)
/*  59:    */   {
/*  60:138 */     return message.headers().get("X-SPDY-URL");
/*  61:    */   }
/*  62:    */   
/*  63:    */   public static void setUrl(HttpMessage message, String url)
/*  64:    */   {
/*  65:145 */     message.headers().set("X-SPDY-URL", url);
/*  66:    */   }
/*  67:    */   
/*  68:    */   public static void removeScheme(HttpMessage message)
/*  69:    */   {
/*  70:152 */     message.headers().remove("X-SPDY-Scheme");
/*  71:    */   }
/*  72:    */   
/*  73:    */   public static String getScheme(HttpMessage message)
/*  74:    */   {
/*  75:159 */     return message.headers().get("X-SPDY-Scheme");
/*  76:    */   }
/*  77:    */   
/*  78:    */   public static void setScheme(HttpMessage message, String scheme)
/*  79:    */   {
/*  80:166 */     message.headers().set("X-SPDY-Scheme", scheme);
/*  81:    */   }
/*  82:    */   
/*  83:    */   public static final class Names
/*  84:    */   {
/*  85:    */     public static final String STREAM_ID = "X-SPDY-Stream-ID";
/*  86:    */     public static final String ASSOCIATED_TO_STREAM_ID = "X-SPDY-Associated-To-Stream-ID";
/*  87:    */     public static final String PRIORITY = "X-SPDY-Priority";
/*  88:    */     public static final String URL = "X-SPDY-URL";
/*  89:    */     public static final String SCHEME = "X-SPDY-Scheme";
/*  90:    */   }
/*  91:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.SpdyHttpHeaders
 * JD-Core Version:    0.7.0.1
 */