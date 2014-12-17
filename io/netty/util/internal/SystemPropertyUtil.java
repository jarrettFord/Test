/*   1:    */ package io.netty.util.internal;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.logging.InternalLogger;
/*   4:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   5:    */ import java.security.AccessController;
/*   6:    */ import java.security.PrivilegedAction;
/*   7:    */ import java.util.logging.Level;
/*   8:    */ import java.util.logging.Logger;
/*   9:    */ import java.util.regex.Matcher;
/*  10:    */ import java.util.regex.Pattern;
/*  11:    */ 
/*  12:    */ public final class SystemPropertyUtil
/*  13:    */ {
/*  14: 38 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(SystemPropertyUtil.class);
/*  15: 39 */   private static boolean initializedLogger = true;
/*  16:    */   
/*  17:    */   public static boolean contains(String key)
/*  18:    */   {
/*  19: 47 */     return get(key) != null;
/*  20:    */   }
/*  21:    */   
/*  22:    */   public static String get(String key)
/*  23:    */   {
/*  24: 57 */     return get(key, null);
/*  25:    */   }
/*  26:    */   
/*  27:    */   public static String get(String key, String def)
/*  28:    */   {
/*  29: 70 */     if (key == null) {
/*  30: 71 */       throw new NullPointerException("key");
/*  31:    */     }
/*  32: 73 */     if (key.isEmpty()) {
/*  33: 74 */       throw new IllegalArgumentException("key must not be empty.");
/*  34:    */     }
/*  35: 77 */     String value = null;
/*  36:    */     try
/*  37:    */     {
/*  38: 79 */       if (System.getSecurityManager() == null) {
/*  39: 80 */         value = System.getProperty(key);
/*  40:    */       } else {
/*  41: 82 */         value = (String)AccessController.doPrivileged(new PrivilegedAction()
/*  42:    */         {
/*  43:    */           public String run()
/*  44:    */           {
/*  45: 85 */             return System.getProperty(this.val$key);
/*  46:    */           }
/*  47:    */         });
/*  48:    */       }
/*  49:    */     }
/*  50:    */     catch (Exception e)
/*  51:    */     {
/*  52: 90 */       if (!loggedException)
/*  53:    */       {
/*  54: 91 */         log("Unable to retrieve a system property '" + key + "'; default values will be used.", e);
/*  55: 92 */         loggedException = true;
/*  56:    */       }
/*  57:    */     }
/*  58: 96 */     if (value == null) {
/*  59: 97 */       return def;
/*  60:    */     }
/*  61:100 */     return value;
/*  62:    */   }
/*  63:    */   
/*  64:    */   public static boolean getBoolean(String key, boolean def)
/*  65:    */   {
/*  66:113 */     String value = get(key);
/*  67:114 */     if (value == null) {
/*  68:115 */       return def;
/*  69:    */     }
/*  70:118 */     value = value.trim().toLowerCase();
/*  71:119 */     if (value.isEmpty()) {
/*  72:120 */       return true;
/*  73:    */     }
/*  74:123 */     if (("true".equals(value)) || ("yes".equals(value)) || ("1".equals(value))) {
/*  75:124 */       return true;
/*  76:    */     }
/*  77:127 */     if (("false".equals(value)) || ("no".equals(value)) || ("0".equals(value))) {
/*  78:128 */       return false;
/*  79:    */     }
/*  80:131 */     log("Unable to parse the boolean system property '" + key + "':" + value + " - " + "using the default value: " + def);
/*  81:    */     
/*  82:    */ 
/*  83:    */ 
/*  84:135 */     return def;
/*  85:    */   }
/*  86:    */   
/*  87:138 */   private static final Pattern INTEGER_PATTERN = Pattern.compile("-?[0-9]+");
/*  88:    */   private static boolean loggedException;
/*  89:    */   
/*  90:    */   public static int getInt(String key, int def)
/*  91:    */   {
/*  92:150 */     String value = get(key);
/*  93:151 */     if (value == null) {
/*  94:152 */       return def;
/*  95:    */     }
/*  96:155 */     value = value.trim().toLowerCase();
/*  97:156 */     if (INTEGER_PATTERN.matcher(value).matches()) {
/*  98:    */       try
/*  99:    */       {
/* 100:158 */         return Integer.parseInt(value);
/* 101:    */       }
/* 102:    */       catch (Exception e) {}
/* 103:    */     }
/* 104:164 */     log("Unable to parse the integer system property '" + key + "':" + value + " - " + "using the default value: " + def);
/* 105:    */     
/* 106:    */ 
/* 107:    */ 
/* 108:168 */     return def;
/* 109:    */   }
/* 110:    */   
/* 111:    */   public static long getLong(String key, long def)
/* 112:    */   {
/* 113:181 */     String value = get(key);
/* 114:182 */     if (value == null) {
/* 115:183 */       return def;
/* 116:    */     }
/* 117:186 */     value = value.trim().toLowerCase();
/* 118:187 */     if (INTEGER_PATTERN.matcher(value).matches()) {
/* 119:    */       try
/* 120:    */       {
/* 121:189 */         return Long.parseLong(value);
/* 122:    */       }
/* 123:    */       catch (Exception e) {}
/* 124:    */     }
/* 125:195 */     log("Unable to parse the long integer system property '" + key + "':" + value + " - " + "using the default value: " + def);
/* 126:    */     
/* 127:    */ 
/* 128:    */ 
/* 129:199 */     return def;
/* 130:    */   }
/* 131:    */   
/* 132:    */   private static void log(String msg)
/* 133:    */   {
/* 134:203 */     if (initializedLogger) {
/* 135:204 */       logger.warn(msg);
/* 136:    */     } else {
/* 137:207 */       Logger.getLogger(SystemPropertyUtil.class.getName()).log(Level.WARNING, msg);
/* 138:    */     }
/* 139:    */   }
/* 140:    */   
/* 141:    */   private static void log(String msg, Exception e)
/* 142:    */   {
/* 143:212 */     if (initializedLogger) {
/* 144:213 */       logger.warn(msg, e);
/* 145:    */     } else {
/* 146:216 */       Logger.getLogger(SystemPropertyUtil.class.getName()).log(Level.WARNING, msg, e);
/* 147:    */     }
/* 148:    */   }
/* 149:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.SystemPropertyUtil
 * JD-Core Version:    0.7.0.1
 */