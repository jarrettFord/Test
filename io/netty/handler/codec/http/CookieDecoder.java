/*   1:    */ package io.netty.handler.codec.http;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.StringUtil;
/*   4:    */ import java.text.ParseException;
/*   5:    */ import java.util.ArrayList;
/*   6:    */ import java.util.Collections;
/*   7:    */ import java.util.Date;
/*   8:    */ import java.util.List;
/*   9:    */ import java.util.Set;
/*  10:    */ import java.util.TreeSet;
/*  11:    */ 
/*  12:    */ public final class CookieDecoder
/*  13:    */ {
/*  14:    */   private static final char COMMA = ',';
/*  15:    */   
/*  16:    */   public static Set<Cookie> decode(String header)
/*  17:    */   {
/*  18: 50 */     List<String> names = new ArrayList(8);
/*  19: 51 */     List<String> values = new ArrayList(8);
/*  20: 52 */     extractKeyValuePairs(header, names, values);
/*  21: 54 */     if (names.isEmpty()) {
/*  22: 55 */       return Collections.emptySet();
/*  23:    */     }
/*  24: 59 */     int version = 0;
/*  25:    */     int i;
/*  26:    */     int i;
/*  27: 63 */     if (((String)names.get(0)).equalsIgnoreCase("Version"))
/*  28:    */     {
/*  29:    */       try
/*  30:    */       {
/*  31: 65 */         version = Integer.parseInt((String)values.get(0));
/*  32:    */       }
/*  33:    */       catch (NumberFormatException e) {}
/*  34: 69 */       i = 1;
/*  35:    */     }
/*  36:    */     else
/*  37:    */     {
/*  38: 71 */       i = 0;
/*  39:    */     }
/*  40: 74 */     if (names.size() <= i) {
/*  41: 76 */       return Collections.emptySet();
/*  42:    */     }
/*  43: 79 */     Set<Cookie> cookies = new TreeSet();
/*  44: 80 */     for (; i < names.size(); i++)
/*  45:    */     {
/*  46: 81 */       String name = (String)names.get(i);
/*  47: 82 */       String value = (String)values.get(i);
/*  48: 83 */       if (value == null) {
/*  49: 84 */         value = "";
/*  50:    */       }
/*  51: 87 */       Cookie c = new DefaultCookie(name, value);
/*  52:    */       
/*  53: 89 */       boolean discard = false;
/*  54: 90 */       boolean secure = false;
/*  55: 91 */       boolean httpOnly = false;
/*  56: 92 */       String comment = null;
/*  57: 93 */       String commentURL = null;
/*  58: 94 */       String domain = null;
/*  59: 95 */       String path = null;
/*  60: 96 */       long maxAge = -9223372036854775808L;
/*  61: 97 */       List<Integer> ports = new ArrayList(2);
/*  62: 99 */       for (int j = i + 1; j < names.size(); i++)
/*  63:    */       {
/*  64:100 */         name = (String)names.get(j);
/*  65:101 */         value = (String)values.get(j);
/*  66:103 */         if ("Discard".equalsIgnoreCase(name))
/*  67:    */         {
/*  68:104 */           discard = true;
/*  69:    */         }
/*  70:105 */         else if ("Secure".equalsIgnoreCase(name))
/*  71:    */         {
/*  72:106 */           secure = true;
/*  73:    */         }
/*  74:107 */         else if ("HTTPOnly".equalsIgnoreCase(name))
/*  75:    */         {
/*  76:108 */           httpOnly = true;
/*  77:    */         }
/*  78:109 */         else if ("Comment".equalsIgnoreCase(name))
/*  79:    */         {
/*  80:110 */           comment = value;
/*  81:    */         }
/*  82:111 */         else if ("CommentURL".equalsIgnoreCase(name))
/*  83:    */         {
/*  84:112 */           commentURL = value;
/*  85:    */         }
/*  86:113 */         else if ("Domain".equalsIgnoreCase(name))
/*  87:    */         {
/*  88:114 */           domain = value;
/*  89:    */         }
/*  90:115 */         else if ("Path".equalsIgnoreCase(name))
/*  91:    */         {
/*  92:116 */           path = value;
/*  93:    */         }
/*  94:117 */         else if ("Expires".equalsIgnoreCase(name))
/*  95:    */         {
/*  96:    */           try
/*  97:    */           {
/*  98:119 */             long maxAgeMillis = HttpHeaderDateFormat.get().parse(value).getTime() - System.currentTimeMillis();
/*  99:    */             
/* 100:    */ 
/* 101:    */ 
/* 102:123 */             maxAge = maxAgeMillis / 1000L + (maxAgeMillis % 1000L != 0L ? 1 : 0);
/* 103:    */           }
/* 104:    */           catch (ParseException e) {}
/* 105:    */         }
/* 106:127 */         else if ("Max-Age".equalsIgnoreCase(name))
/* 107:    */         {
/* 108:128 */           maxAge = Integer.parseInt(value);
/* 109:    */         }
/* 110:129 */         else if ("Version".equalsIgnoreCase(name))
/* 111:    */         {
/* 112:130 */           version = Integer.parseInt(value);
/* 113:    */         }
/* 114:    */         else
/* 115:    */         {
/* 116:131 */           if (!"Port".equalsIgnoreCase(name)) {
/* 117:    */             break;
/* 118:    */           }
/* 119:132 */           String[] portList = StringUtil.split(value, ',');
/* 120:133 */           for (String s1 : portList) {
/* 121:    */             try
/* 122:    */             {
/* 123:135 */               ports.add(Integer.valueOf(s1));
/* 124:    */             }
/* 125:    */             catch (NumberFormatException e) {}
/* 126:    */           }
/* 127:    */         }
/* 128: 99 */         j++;
/* 129:    */       }
/* 130:145 */       c.setVersion(version);
/* 131:146 */       c.setMaxAge(maxAge);
/* 132:147 */       c.setPath(path);
/* 133:148 */       c.setDomain(domain);
/* 134:149 */       c.setSecure(secure);
/* 135:150 */       c.setHttpOnly(httpOnly);
/* 136:151 */       if (version > 0) {
/* 137:152 */         c.setComment(comment);
/* 138:    */       }
/* 139:154 */       if (version > 1)
/* 140:    */       {
/* 141:155 */         c.setCommentUrl(commentURL);
/* 142:156 */         c.setPorts(ports);
/* 143:157 */         c.setDiscard(discard);
/* 144:    */       }
/* 145:160 */       cookies.add(c);
/* 146:    */     }
/* 147:163 */     return cookies;
/* 148:    */   }
/* 149:    */   
/* 150:    */   private static void extractKeyValuePairs(String header, List<String> names, List<String> values)
/* 151:    */   {
/* 152:169 */     int headerLen = header.length();
/* 153:170 */     int i = 0;
/* 154:174 */     while (i != headerLen) {
/* 155:177 */       switch (header.charAt(i))
/* 156:    */       {
/* 157:    */       case '\t': 
/* 158:    */       case '\n': 
/* 159:    */       case '\013': 
/* 160:    */       case '\f': 
/* 161:    */       case '\r': 
/* 162:    */       case ' ': 
/* 163:    */       case ',': 
/* 164:    */       case ';': 
/* 165:180 */         i++;
/* 166:181 */         break;
/* 167:    */       default: 
/* 168:    */         for (;;)
/* 169:    */         {
/* 170:188 */           if (i == headerLen) {
/* 171:    */             return;
/* 172:    */           }
/* 173:191 */           if (header.charAt(i) != '$') {
/* 174:    */             break;
/* 175:    */           }
/* 176:192 */           i++;
/* 177:    */         }
/* 178:    */         String value;
/* 179:    */         String name;
/* 180:    */         String value;
/* 181:201 */         if (i == headerLen)
/* 182:    */         {
/* 183:202 */           String name = null;
/* 184:203 */           value = null;
/* 185:    */         }
/* 186:    */         else
/* 187:    */         {
/* 188:205 */           int newNameStart = i;
/* 189:    */           do
/* 190:    */           {
/* 191:    */             String value;
/* 192:207 */             switch (header.charAt(i))
/* 193:    */             {
/* 194:    */             case ';': 
/* 195:210 */               name = header.substring(newNameStart, i);
/* 196:211 */               value = null;
/* 197:212 */               break;
/* 198:    */             case '=': 
/* 199:215 */               name = header.substring(newNameStart, i);
/* 200:216 */               i++;
/* 201:217 */               if (i == headerLen)
/* 202:    */               {
/* 203:219 */                 value = "";
/* 204:    */               }
/* 205:    */               else
/* 206:    */               {
/* 207:223 */                 int newValueStart = i;
/* 208:224 */                 char c = header.charAt(i);
/* 209:225 */                 if ((c == '"') || (c == '\''))
/* 210:    */                 {
/* 211:227 */                   StringBuilder newValueBuf = new StringBuilder(header.length() - i);
/* 212:228 */                   char q = c;
/* 213:229 */                   boolean hadBackslash = false;
/* 214:230 */                   i++;
/* 215:    */                   for (;;)
/* 216:    */                   {
/* 217:232 */                     if (i == headerLen)
/* 218:    */                     {
/* 219:233 */                       String value = newValueBuf.toString();
/* 220:234 */                       break;
/* 221:    */                     }
/* 222:236 */                     if (hadBackslash)
/* 223:    */                     {
/* 224:237 */                       hadBackslash = false;
/* 225:238 */                       c = header.charAt(i++);
/* 226:239 */                       switch (c)
/* 227:    */                       {
/* 228:    */                       case '"': 
/* 229:    */                       case '\'': 
/* 230:    */                       case '\\': 
/* 231:242 */                         newValueBuf.setCharAt(newValueBuf.length() - 1, c);
/* 232:243 */                         break;
/* 233:    */                       default: 
/* 234:246 */                         newValueBuf.append(c); break;
/* 235:    */                       }
/* 236:    */                     }
/* 237:    */                     else
/* 238:    */                     {
/* 239:249 */                       c = header.charAt(i++);
/* 240:250 */                       if (c == q)
/* 241:    */                       {
/* 242:251 */                         String value = newValueBuf.toString();
/* 243:252 */                         break;
/* 244:    */                       }
/* 245:254 */                       newValueBuf.append(c);
/* 246:255 */                       if (c == '\\') {
/* 247:256 */                         hadBackslash = true;
/* 248:    */                       }
/* 249:    */                     }
/* 250:    */                   }
/* 251:    */                 }
/* 252:262 */                 int semiPos = header.indexOf(';', i);
/* 253:263 */                 if (semiPos > 0)
/* 254:    */                 {
/* 255:264 */                   String value = header.substring(newValueStart, semiPos);
/* 256:265 */                   i = semiPos;
/* 257:    */                 }
/* 258:    */                 else
/* 259:    */                 {
/* 260:267 */                   value = header.substring(newValueStart);
/* 261:268 */                   i = headerLen;
/* 262:    */                 }
/* 263:    */               }
/* 264:271 */               break;
/* 265:    */             default: 
/* 266:273 */               i++;
/* 267:    */             }
/* 268:276 */           } while (i != headerLen);
/* 269:278 */           name = header.substring(newNameStart);
/* 270:279 */           value = null;
/* 271:    */         }
/* 272:285 */         names.add(name);
/* 273:286 */         values.add(value);
/* 274:    */       }
/* 275:    */     }
/* 276:    */   }
/* 277:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.CookieDecoder
 * JD-Core Version:    0.7.0.1
 */