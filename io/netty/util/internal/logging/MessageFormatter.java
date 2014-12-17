/*   1:    */ package io.netty.util.internal.logging;
/*   2:    */ 
/*   3:    */ import java.io.PrintStream;
/*   4:    */ import java.util.HashMap;
/*   5:    */ import java.util.Map;
/*   6:    */ 
/*   7:    */ final class MessageFormatter
/*   8:    */ {
/*   9:    */   static final char DELIM_START = '{';
/*  10:    */   static final char DELIM_STOP = '}';
/*  11:    */   static final String DELIM_STR = "{}";
/*  12:    */   private static final char ESCAPE_CHAR = '\\';
/*  13:    */   
/*  14:    */   static FormattingTuple format(String messagePattern, Object arg)
/*  15:    */   {
/*  16:135 */     return arrayFormat(messagePattern, new Object[] { arg });
/*  17:    */   }
/*  18:    */   
/*  19:    */   static FormattingTuple format(String messagePattern, Object argA, Object argB)
/*  20:    */   {
/*  21:159 */     return arrayFormat(messagePattern, new Object[] { argA, argB });
/*  22:    */   }
/*  23:    */   
/*  24:    */   static Throwable getThrowableCandidate(Object[] argArray)
/*  25:    */   {
/*  26:163 */     if ((argArray == null) || (argArray.length == 0)) {
/*  27:164 */       return null;
/*  28:    */     }
/*  29:167 */     Object lastEntry = argArray[(argArray.length - 1)];
/*  30:168 */     if ((lastEntry instanceof Throwable)) {
/*  31:169 */       return (Throwable)lastEntry;
/*  32:    */     }
/*  33:171 */     return null;
/*  34:    */   }
/*  35:    */   
/*  36:    */   static FormattingTuple arrayFormat(String messagePattern, Object[] argArray)
/*  37:    */   {
/*  38:187 */     Throwable throwableCandidate = getThrowableCandidate(argArray);
/*  39:189 */     if (messagePattern == null) {
/*  40:190 */       return new FormattingTuple(null, argArray, throwableCandidate);
/*  41:    */     }
/*  42:193 */     if (argArray == null) {
/*  43:194 */       return new FormattingTuple(messagePattern);
/*  44:    */     }
/*  45:197 */     int i = 0;
/*  46:    */     
/*  47:199 */     StringBuffer sbuf = new StringBuffer(messagePattern.length() + 50);
/*  48:202 */     for (int L = 0; L < argArray.length; L++)
/*  49:    */     {
/*  50:204 */       int j = messagePattern.indexOf("{}", i);
/*  51:206 */       if (j == -1)
/*  52:    */       {
/*  53:208 */         if (i == 0) {
/*  54:209 */           return new FormattingTuple(messagePattern, argArray, throwableCandidate);
/*  55:    */         }
/*  56:213 */         sbuf.append(messagePattern.substring(i, messagePattern.length()));
/*  57:214 */         return new FormattingTuple(sbuf.toString(), argArray, throwableCandidate);
/*  58:    */       }
/*  59:218 */       if (isEscapedDelimeter(messagePattern, j))
/*  60:    */       {
/*  61:219 */         if (!isDoubleEscaped(messagePattern, j))
/*  62:    */         {
/*  63:220 */           L--;
/*  64:221 */           sbuf.append(messagePattern.substring(i, j - 1));
/*  65:222 */           sbuf.append('{');
/*  66:223 */           i = j + 1;
/*  67:    */         }
/*  68:    */         else
/*  69:    */         {
/*  70:228 */           sbuf.append(messagePattern.substring(i, j - 1));
/*  71:229 */           deeplyAppendParameter(sbuf, argArray[L], new HashMap());
/*  72:230 */           i = j + 2;
/*  73:    */         }
/*  74:    */       }
/*  75:    */       else
/*  76:    */       {
/*  77:234 */         sbuf.append(messagePattern.substring(i, j));
/*  78:235 */         deeplyAppendParameter(sbuf, argArray[L], new HashMap());
/*  79:236 */         i = j + 2;
/*  80:    */       }
/*  81:    */     }
/*  82:241 */     sbuf.append(messagePattern.substring(i, messagePattern.length()));
/*  83:242 */     if (L < argArray.length - 1) {
/*  84:243 */       return new FormattingTuple(sbuf.toString(), argArray, throwableCandidate);
/*  85:    */     }
/*  86:245 */     return new FormattingTuple(sbuf.toString(), argArray, null);
/*  87:    */   }
/*  88:    */   
/*  89:    */   static boolean isEscapedDelimeter(String messagePattern, int delimeterStartIndex)
/*  90:    */   {
/*  91:252 */     if (delimeterStartIndex == 0) {
/*  92:253 */       return false;
/*  93:    */     }
/*  94:255 */     return messagePattern.charAt(delimeterStartIndex - 1) == '\\';
/*  95:    */   }
/*  96:    */   
/*  97:    */   static boolean isDoubleEscaped(String messagePattern, int delimeterStartIndex)
/*  98:    */   {
/*  99:260 */     return (delimeterStartIndex >= 2) && (messagePattern.charAt(delimeterStartIndex - 2) == '\\');
/* 100:    */   }
/* 101:    */   
/* 102:    */   private static void deeplyAppendParameter(StringBuffer sbuf, Object o, Map<Object[], Void> seenMap)
/* 103:    */   {
/* 104:266 */     if (o == null)
/* 105:    */     {
/* 106:267 */       sbuf.append("null");
/* 107:268 */       return;
/* 108:    */     }
/* 109:270 */     if (!o.getClass().isArray()) {
/* 110:271 */       safeObjectAppend(sbuf, o);
/* 111:275 */     } else if ((o instanceof boolean[])) {
/* 112:276 */       booleanArrayAppend(sbuf, (boolean[])o);
/* 113:277 */     } else if ((o instanceof byte[])) {
/* 114:278 */       byteArrayAppend(sbuf, (byte[])o);
/* 115:279 */     } else if ((o instanceof char[])) {
/* 116:280 */       charArrayAppend(sbuf, (char[])o);
/* 117:281 */     } else if ((o instanceof short[])) {
/* 118:282 */       shortArrayAppend(sbuf, (short[])o);
/* 119:283 */     } else if ((o instanceof int[])) {
/* 120:284 */       intArrayAppend(sbuf, (int[])o);
/* 121:285 */     } else if ((o instanceof long[])) {
/* 122:286 */       longArrayAppend(sbuf, (long[])o);
/* 123:287 */     } else if ((o instanceof float[])) {
/* 124:288 */       floatArrayAppend(sbuf, (float[])o);
/* 125:289 */     } else if ((o instanceof double[])) {
/* 126:290 */       doubleArrayAppend(sbuf, (double[])o);
/* 127:    */     } else {
/* 128:292 */       objectArrayAppend(sbuf, (Object[])o, seenMap);
/* 129:    */     }
/* 130:    */   }
/* 131:    */   
/* 132:    */   private static void safeObjectAppend(StringBuffer sbuf, Object o)
/* 133:    */   {
/* 134:    */     try
/* 135:    */     {
/* 136:299 */       String oAsString = o.toString();
/* 137:300 */       sbuf.append(oAsString);
/* 138:    */     }
/* 139:    */     catch (Throwable t)
/* 140:    */     {
/* 141:302 */       System.err.println("SLF4J: Failed toString() invocation on an object of type [" + o.getClass().getName() + ']');
/* 142:    */       
/* 143:    */ 
/* 144:305 */       t.printStackTrace();
/* 145:306 */       sbuf.append("[FAILED toString()]");
/* 146:    */     }
/* 147:    */   }
/* 148:    */   
/* 149:    */   private static void objectArrayAppend(StringBuffer sbuf, Object[] a, Map<Object[], Void> seenMap)
/* 150:    */   {
/* 151:312 */     sbuf.append('[');
/* 152:313 */     if (!seenMap.containsKey(a))
/* 153:    */     {
/* 154:314 */       seenMap.put(a, null);
/* 155:315 */       int len = a.length;
/* 156:316 */       for (int i = 0; i < len; i++)
/* 157:    */       {
/* 158:317 */         deeplyAppendParameter(sbuf, a[i], seenMap);
/* 159:318 */         if (i != len - 1) {
/* 160:319 */           sbuf.append(", ");
/* 161:    */         }
/* 162:    */       }
/* 163:323 */       seenMap.remove(a);
/* 164:    */     }
/* 165:    */     else
/* 166:    */     {
/* 167:325 */       sbuf.append("...");
/* 168:    */     }
/* 169:327 */     sbuf.append(']');
/* 170:    */   }
/* 171:    */   
/* 172:    */   private static void booleanArrayAppend(StringBuffer sbuf, boolean[] a)
/* 173:    */   {
/* 174:331 */     sbuf.append('[');
/* 175:332 */     int len = a.length;
/* 176:333 */     for (int i = 0; i < len; i++)
/* 177:    */     {
/* 178:334 */       sbuf.append(a[i]);
/* 179:335 */       if (i != len - 1) {
/* 180:336 */         sbuf.append(", ");
/* 181:    */       }
/* 182:    */     }
/* 183:339 */     sbuf.append(']');
/* 184:    */   }
/* 185:    */   
/* 186:    */   private static void byteArrayAppend(StringBuffer sbuf, byte[] a)
/* 187:    */   {
/* 188:343 */     sbuf.append('[');
/* 189:344 */     int len = a.length;
/* 190:345 */     for (int i = 0; i < len; i++)
/* 191:    */     {
/* 192:346 */       sbuf.append(a[i]);
/* 193:347 */       if (i != len - 1) {
/* 194:348 */         sbuf.append(", ");
/* 195:    */       }
/* 196:    */     }
/* 197:351 */     sbuf.append(']');
/* 198:    */   }
/* 199:    */   
/* 200:    */   private static void charArrayAppend(StringBuffer sbuf, char[] a)
/* 201:    */   {
/* 202:355 */     sbuf.append('[');
/* 203:356 */     int len = a.length;
/* 204:357 */     for (int i = 0; i < len; i++)
/* 205:    */     {
/* 206:358 */       sbuf.append(a[i]);
/* 207:359 */       if (i != len - 1) {
/* 208:360 */         sbuf.append(", ");
/* 209:    */       }
/* 210:    */     }
/* 211:363 */     sbuf.append(']');
/* 212:    */   }
/* 213:    */   
/* 214:    */   private static void shortArrayAppend(StringBuffer sbuf, short[] a)
/* 215:    */   {
/* 216:367 */     sbuf.append('[');
/* 217:368 */     int len = a.length;
/* 218:369 */     for (int i = 0; i < len; i++)
/* 219:    */     {
/* 220:370 */       sbuf.append(a[i]);
/* 221:371 */       if (i != len - 1) {
/* 222:372 */         sbuf.append(", ");
/* 223:    */       }
/* 224:    */     }
/* 225:375 */     sbuf.append(']');
/* 226:    */   }
/* 227:    */   
/* 228:    */   private static void intArrayAppend(StringBuffer sbuf, int[] a)
/* 229:    */   {
/* 230:379 */     sbuf.append('[');
/* 231:380 */     int len = a.length;
/* 232:381 */     for (int i = 0; i < len; i++)
/* 233:    */     {
/* 234:382 */       sbuf.append(a[i]);
/* 235:383 */       if (i != len - 1) {
/* 236:384 */         sbuf.append(", ");
/* 237:    */       }
/* 238:    */     }
/* 239:387 */     sbuf.append(']');
/* 240:    */   }
/* 241:    */   
/* 242:    */   private static void longArrayAppend(StringBuffer sbuf, long[] a)
/* 243:    */   {
/* 244:391 */     sbuf.append('[');
/* 245:392 */     int len = a.length;
/* 246:393 */     for (int i = 0; i < len; i++)
/* 247:    */     {
/* 248:394 */       sbuf.append(a[i]);
/* 249:395 */       if (i != len - 1) {
/* 250:396 */         sbuf.append(", ");
/* 251:    */       }
/* 252:    */     }
/* 253:399 */     sbuf.append(']');
/* 254:    */   }
/* 255:    */   
/* 256:    */   private static void floatArrayAppend(StringBuffer sbuf, float[] a)
/* 257:    */   {
/* 258:403 */     sbuf.append('[');
/* 259:404 */     int len = a.length;
/* 260:405 */     for (int i = 0; i < len; i++)
/* 261:    */     {
/* 262:406 */       sbuf.append(a[i]);
/* 263:407 */       if (i != len - 1) {
/* 264:408 */         sbuf.append(", ");
/* 265:    */       }
/* 266:    */     }
/* 267:411 */     sbuf.append(']');
/* 268:    */   }
/* 269:    */   
/* 270:    */   private static void doubleArrayAppend(StringBuffer sbuf, double[] a)
/* 271:    */   {
/* 272:415 */     sbuf.append('[');
/* 273:416 */     int len = a.length;
/* 274:417 */     for (int i = 0; i < len; i++)
/* 275:    */     {
/* 276:418 */       sbuf.append(a[i]);
/* 277:419 */       if (i != len - 1) {
/* 278:420 */         sbuf.append(", ");
/* 279:    */       }
/* 280:    */     }
/* 281:423 */     sbuf.append(']');
/* 282:    */   }
/* 283:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.logging.MessageFormatter
 * JD-Core Version:    0.7.0.1
 */