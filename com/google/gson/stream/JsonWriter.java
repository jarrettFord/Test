/*   1:    */ package com.google.gson.stream;
/*   2:    */ 
/*   3:    */ import java.io.Closeable;
/*   4:    */ import java.io.Flushable;
/*   5:    */ import java.io.IOException;
/*   6:    */ import java.io.Writer;
/*   7:    */ 
/*   8:    */ public class JsonWriter
/*   9:    */   implements Closeable, Flushable
/*  10:    */ {
/*  11:145 */   private static final String[] REPLACEMENT_CHARS = new String[''];
/*  12:    */   private static final String[] HTML_SAFE_REPLACEMENT_CHARS;
/*  13:    */   private final Writer out;
/*  14:    */   
/*  15:    */   static
/*  16:    */   {
/*  17:146 */     for (int i = 0; i <= 31; i++) {
/*  18:147 */       REPLACEMENT_CHARS[i] = String.format("\\u%04x", new Object[] { Integer.valueOf(i) });
/*  19:    */     }
/*  20:149 */     REPLACEMENT_CHARS[34] = "\\\"";
/*  21:150 */     REPLACEMENT_CHARS[92] = "\\\\";
/*  22:151 */     REPLACEMENT_CHARS[9] = "\\t";
/*  23:152 */     REPLACEMENT_CHARS[8] = "\\b";
/*  24:153 */     REPLACEMENT_CHARS[10] = "\\n";
/*  25:154 */     REPLACEMENT_CHARS[13] = "\\r";
/*  26:155 */     REPLACEMENT_CHARS[12] = "\\f";
/*  27:156 */     HTML_SAFE_REPLACEMENT_CHARS = (String[])REPLACEMENT_CHARS.clone();
/*  28:157 */     HTML_SAFE_REPLACEMENT_CHARS[60] = "\\u003c";
/*  29:158 */     HTML_SAFE_REPLACEMENT_CHARS[62] = "\\u003e";
/*  30:159 */     HTML_SAFE_REPLACEMENT_CHARS[38] = "\\u0026";
/*  31:160 */     HTML_SAFE_REPLACEMENT_CHARS[61] = "\\u003d";
/*  32:161 */     HTML_SAFE_REPLACEMENT_CHARS[39] = "\\u0027";
/*  33:    */   }
/*  34:    */   
/*  35:167 */   private int[] stack = new int[32];
/*  36:168 */   private int stackSize = 0;
/*  37:    */   private String indent;
/*  38:    */   private String separator;
/*  39:    */   private boolean lenient;
/*  40:    */   private boolean htmlSafe;
/*  41:    */   private String deferredName;
/*  42:    */   private boolean serializeNulls;
/*  43:    */   
/*  44:    */   public JsonWriter(Writer out)
/*  45:    */   {
/*  46:170 */     push(6);
/*  47:    */     
/*  48:    */ 
/*  49:    */ 
/*  50:    */ 
/*  51:    */ 
/*  52:    */ 
/*  53:    */ 
/*  54:    */ 
/*  55:    */ 
/*  56:    */ 
/*  57:    */ 
/*  58:182 */     this.separator = ":";
/*  59:    */     
/*  60:    */ 
/*  61:    */ 
/*  62:    */ 
/*  63:    */ 
/*  64:    */ 
/*  65:    */ 
/*  66:190 */     this.serializeNulls = true;
/*  67:198 */     if (out == null) {
/*  68:199 */       throw new NullPointerException("out == null");
/*  69:    */     }
/*  70:201 */     this.out = out;
/*  71:    */   }
/*  72:    */   
/*  73:    */   public final void setIndent(String indent)
/*  74:    */   {
/*  75:213 */     if (indent.length() == 0)
/*  76:    */     {
/*  77:214 */       this.indent = null;
/*  78:215 */       this.separator = ":";
/*  79:    */     }
/*  80:    */     else
/*  81:    */     {
/*  82:217 */       this.indent = indent;
/*  83:218 */       this.separator = ": ";
/*  84:    */     }
/*  85:    */   }
/*  86:    */   
/*  87:    */   public final void setLenient(boolean lenient)
/*  88:    */   {
/*  89:235 */     this.lenient = lenient;
/*  90:    */   }
/*  91:    */   
/*  92:    */   public boolean isLenient()
/*  93:    */   {
/*  94:242 */     return this.lenient;
/*  95:    */   }
/*  96:    */   
/*  97:    */   public final void setHtmlSafe(boolean htmlSafe)
/*  98:    */   {
/*  99:253 */     this.htmlSafe = htmlSafe;
/* 100:    */   }
/* 101:    */   
/* 102:    */   public final boolean isHtmlSafe()
/* 103:    */   {
/* 104:261 */     return this.htmlSafe;
/* 105:    */   }
/* 106:    */   
/* 107:    */   public final void setSerializeNulls(boolean serializeNulls)
/* 108:    */   {
/* 109:269 */     this.serializeNulls = serializeNulls;
/* 110:    */   }
/* 111:    */   
/* 112:    */   public final boolean getSerializeNulls()
/* 113:    */   {
/* 114:277 */     return this.serializeNulls;
/* 115:    */   }
/* 116:    */   
/* 117:    */   public JsonWriter beginArray()
/* 118:    */     throws IOException
/* 119:    */   {
/* 120:287 */     writeDeferredName();
/* 121:288 */     return open(1, "[");
/* 122:    */   }
/* 123:    */   
/* 124:    */   public JsonWriter endArray()
/* 125:    */     throws IOException
/* 126:    */   {
/* 127:297 */     return close(1, 2, "]");
/* 128:    */   }
/* 129:    */   
/* 130:    */   public JsonWriter beginObject()
/* 131:    */     throws IOException
/* 132:    */   {
/* 133:307 */     writeDeferredName();
/* 134:308 */     return open(3, "{");
/* 135:    */   }
/* 136:    */   
/* 137:    */   public JsonWriter endObject()
/* 138:    */     throws IOException
/* 139:    */   {
/* 140:317 */     return close(3, 5, "}");
/* 141:    */   }
/* 142:    */   
/* 143:    */   private JsonWriter open(int empty, String openBracket)
/* 144:    */     throws IOException
/* 145:    */   {
/* 146:325 */     beforeValue(true);
/* 147:326 */     push(empty);
/* 148:327 */     this.out.write(openBracket);
/* 149:328 */     return this;
/* 150:    */   }
/* 151:    */   
/* 152:    */   private JsonWriter close(int empty, int nonempty, String closeBracket)
/* 153:    */     throws IOException
/* 154:    */   {
/* 155:337 */     int context = peek();
/* 156:338 */     if ((context != nonempty) && (context != empty)) {
/* 157:339 */       throw new IllegalStateException("Nesting problem.");
/* 158:    */     }
/* 159:341 */     if (this.deferredName != null) {
/* 160:342 */       throw new IllegalStateException("Dangling name: " + this.deferredName);
/* 161:    */     }
/* 162:345 */     this.stackSize -= 1;
/* 163:346 */     if (context == nonempty) {
/* 164:347 */       newline();
/* 165:    */     }
/* 166:349 */     this.out.write(closeBracket);
/* 167:350 */     return this;
/* 168:    */   }
/* 169:    */   
/* 170:    */   private void push(int newTop)
/* 171:    */   {
/* 172:354 */     if (this.stackSize == this.stack.length)
/* 173:    */     {
/* 174:355 */       int[] newStack = new int[this.stackSize * 2];
/* 175:356 */       System.arraycopy(this.stack, 0, newStack, 0, this.stackSize);
/* 176:357 */       this.stack = newStack;
/* 177:    */     }
/* 178:359 */     this.stack[(this.stackSize++)] = newTop;
/* 179:    */   }
/* 180:    */   
/* 181:    */   private int peek()
/* 182:    */   {
/* 183:366 */     if (this.stackSize == 0) {
/* 184:367 */       throw new IllegalStateException("JsonWriter is closed.");
/* 185:    */     }
/* 186:369 */     return this.stack[(this.stackSize - 1)];
/* 187:    */   }
/* 188:    */   
/* 189:    */   private void replaceTop(int topOfStack)
/* 190:    */   {
/* 191:376 */     this.stack[(this.stackSize - 1)] = topOfStack;
/* 192:    */   }
/* 193:    */   
/* 194:    */   public JsonWriter name(String name)
/* 195:    */     throws IOException
/* 196:    */   {
/* 197:386 */     if (name == null) {
/* 198:387 */       throw new NullPointerException("name == null");
/* 199:    */     }
/* 200:389 */     if (this.deferredName != null) {
/* 201:390 */       throw new IllegalStateException();
/* 202:    */     }
/* 203:392 */     if (this.stackSize == 0) {
/* 204:393 */       throw new IllegalStateException("JsonWriter is closed.");
/* 205:    */     }
/* 206:395 */     this.deferredName = name;
/* 207:396 */     return this;
/* 208:    */   }
/* 209:    */   
/* 210:    */   private void writeDeferredName()
/* 211:    */     throws IOException
/* 212:    */   {
/* 213:400 */     if (this.deferredName != null)
/* 214:    */     {
/* 215:401 */       beforeName();
/* 216:402 */       string(this.deferredName);
/* 217:403 */       this.deferredName = null;
/* 218:    */     }
/* 219:    */   }
/* 220:    */   
/* 221:    */   public JsonWriter value(String value)
/* 222:    */     throws IOException
/* 223:    */   {
/* 224:414 */     if (value == null) {
/* 225:415 */       return nullValue();
/* 226:    */     }
/* 227:417 */     writeDeferredName();
/* 228:418 */     beforeValue(false);
/* 229:419 */     string(value);
/* 230:420 */     return this;
/* 231:    */   }
/* 232:    */   
/* 233:    */   public JsonWriter nullValue()
/* 234:    */     throws IOException
/* 235:    */   {
/* 236:429 */     if (this.deferredName != null) {
/* 237:430 */       if (this.serializeNulls)
/* 238:    */       {
/* 239:431 */         writeDeferredName();
/* 240:    */       }
/* 241:    */       else
/* 242:    */       {
/* 243:433 */         this.deferredName = null;
/* 244:434 */         return this;
/* 245:    */       }
/* 246:    */     }
/* 247:437 */     beforeValue(false);
/* 248:438 */     this.out.write("null");
/* 249:439 */     return this;
/* 250:    */   }
/* 251:    */   
/* 252:    */   public JsonWriter value(boolean value)
/* 253:    */     throws IOException
/* 254:    */   {
/* 255:448 */     writeDeferredName();
/* 256:449 */     beforeValue(false);
/* 257:450 */     this.out.write(value ? "true" : "false");
/* 258:451 */     return this;
/* 259:    */   }
/* 260:    */   
/* 261:    */   public JsonWriter value(double value)
/* 262:    */     throws IOException
/* 263:    */   {
/* 264:462 */     if ((Double.isNaN(value)) || (Double.isInfinite(value))) {
/* 265:463 */       throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
/* 266:    */     }
/* 267:465 */     writeDeferredName();
/* 268:466 */     beforeValue(false);
/* 269:467 */     this.out.append(Double.toString(value));
/* 270:468 */     return this;
/* 271:    */   }
/* 272:    */   
/* 273:    */   public JsonWriter value(long value)
/* 274:    */     throws IOException
/* 275:    */   {
/* 276:477 */     writeDeferredName();
/* 277:478 */     beforeValue(false);
/* 278:479 */     this.out.write(Long.toString(value));
/* 279:480 */     return this;
/* 280:    */   }
/* 281:    */   
/* 282:    */   public JsonWriter value(Number value)
/* 283:    */     throws IOException
/* 284:    */   {
/* 285:491 */     if (value == null) {
/* 286:492 */       return nullValue();
/* 287:    */     }
/* 288:495 */     writeDeferredName();
/* 289:496 */     String string = value.toString();
/* 290:497 */     if ((!this.lenient) && ((string.equals("-Infinity")) || (string.equals("Infinity")) || (string.equals("NaN")))) {
/* 291:499 */       throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
/* 292:    */     }
/* 293:501 */     beforeValue(false);
/* 294:502 */     this.out.append(string);
/* 295:503 */     return this;
/* 296:    */   }
/* 297:    */   
/* 298:    */   public void flush()
/* 299:    */     throws IOException
/* 300:    */   {
/* 301:511 */     if (this.stackSize == 0) {
/* 302:512 */       throw new IllegalStateException("JsonWriter is closed.");
/* 303:    */     }
/* 304:514 */     this.out.flush();
/* 305:    */   }
/* 306:    */   
/* 307:    */   public void close()
/* 308:    */     throws IOException
/* 309:    */   {
/* 310:523 */     this.out.close();
/* 311:    */     
/* 312:525 */     int size = this.stackSize;
/* 313:526 */     if ((size > 1) || ((size == 1) && (this.stack[(size - 1)] != 7))) {
/* 314:527 */       throw new IOException("Incomplete document");
/* 315:    */     }
/* 316:529 */     this.stackSize = 0;
/* 317:    */   }
/* 318:    */   
/* 319:    */   private void string(String value)
/* 320:    */     throws IOException
/* 321:    */   {
/* 322:533 */     String[] replacements = this.htmlSafe ? HTML_SAFE_REPLACEMENT_CHARS : REPLACEMENT_CHARS;
/* 323:534 */     this.out.write("\"");
/* 324:535 */     int last = 0;
/* 325:536 */     int length = value.length();
/* 326:537 */     for (int i = 0; i < length; i++)
/* 327:    */     {
/* 328:538 */       char c = value.charAt(i);
/* 329:    */       String replacement;
/* 330:540 */       if (c < '')
/* 331:    */       {
/* 332:541 */         String replacement = replacements[c];
/* 333:542 */         if (replacement == null) {
/* 334:    */           continue;
/* 335:    */         }
/* 336:    */       }
/* 337:    */       else
/* 338:    */       {
/* 339:    */         String replacement;
/* 340:545 */         if (c == ' ')
/* 341:    */         {
/* 342:546 */           replacement = "\\u2028";
/* 343:    */         }
/* 344:    */         else
/* 345:    */         {
/* 346:547 */           if (c != ' ') {
/* 347:    */             continue;
/* 348:    */           }
/* 349:548 */           replacement = "\\u2029";
/* 350:    */         }
/* 351:    */       }
/* 352:552 */       if (last < i) {
/* 353:553 */         this.out.write(value, last, i - last);
/* 354:    */       }
/* 355:555 */       this.out.write(replacement);
/* 356:556 */       last = i + 1;
/* 357:    */     }
/* 358:558 */     if (last < length) {
/* 359:559 */       this.out.write(value, last, length - last);
/* 360:    */     }
/* 361:561 */     this.out.write("\"");
/* 362:    */   }
/* 363:    */   
/* 364:    */   private void newline()
/* 365:    */     throws IOException
/* 366:    */   {
/* 367:565 */     if (this.indent == null) {
/* 368:566 */       return;
/* 369:    */     }
/* 370:569 */     this.out.write("\n");
/* 371:570 */     int i = 1;
/* 372:570 */     for (int size = this.stackSize; i < size; i++) {
/* 373:571 */       this.out.write(this.indent);
/* 374:    */     }
/* 375:    */   }
/* 376:    */   
/* 377:    */   private void beforeName()
/* 378:    */     throws IOException
/* 379:    */   {
/* 380:580 */     int context = peek();
/* 381:581 */     if (context == 5) {
/* 382:582 */       this.out.write(44);
/* 383:583 */     } else if (context != 3) {
/* 384:584 */       throw new IllegalStateException("Nesting problem.");
/* 385:    */     }
/* 386:586 */     newline();
/* 387:587 */     replaceTop(4);
/* 388:    */   }
/* 389:    */   
/* 390:    */   private void beforeValue(boolean root)
/* 391:    */     throws IOException
/* 392:    */   {
/* 393:600 */     switch (peek())
/* 394:    */     {
/* 395:    */     case 7: 
/* 396:602 */       if (!this.lenient) {
/* 397:603 */         throw new IllegalStateException("JSON must have only one top-level value.");
/* 398:    */       }
/* 399:    */     case 6: 
/* 400:608 */       if ((!this.lenient) && (!root)) {
/* 401:609 */         throw new IllegalStateException("JSON must start with an array or an object.");
/* 402:    */       }
/* 403:612 */       replaceTop(7);
/* 404:613 */       break;
/* 405:    */     case 1: 
/* 406:616 */       replaceTop(2);
/* 407:617 */       newline();
/* 408:618 */       break;
/* 409:    */     case 2: 
/* 410:621 */       this.out.append(',');
/* 411:622 */       newline();
/* 412:623 */       break;
/* 413:    */     case 4: 
/* 414:626 */       this.out.append(this.separator);
/* 415:627 */       replaceTop(5);
/* 416:628 */       break;
/* 417:    */     }
/* 418:631 */     throw new IllegalStateException("Nesting problem.");
/* 419:    */   }
/* 420:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.stream.JsonWriter
 * JD-Core Version:    0.7.0.1
 */