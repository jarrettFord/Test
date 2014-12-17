/*   1:    */ package io.netty.handler.logging;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufHolder;
/*   5:    */ import io.netty.channel.ChannelDuplexHandler;
/*   6:    */ import io.netty.channel.ChannelHandler.Sharable;
/*   7:    */ import io.netty.channel.ChannelHandlerContext;
/*   8:    */ import io.netty.channel.ChannelPromise;
/*   9:    */ import io.netty.util.internal.StringUtil;
/*  10:    */ import io.netty.util.internal.logging.InternalLogLevel;
/*  11:    */ import io.netty.util.internal.logging.InternalLogger;
/*  12:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  13:    */ import java.net.SocketAddress;
/*  14:    */ 
/*  15:    */ @ChannelHandler.Sharable
/*  16:    */ public class LoggingHandler
/*  17:    */   extends ChannelDuplexHandler
/*  18:    */ {
/*  19: 39 */   private static final LogLevel DEFAULT_LEVEL = LogLevel.DEBUG;
/*  20: 41 */   private static final String NEWLINE = StringUtil.NEWLINE;
/*  21: 43 */   private static final String[] BYTE2HEX = new String[256];
/*  22: 44 */   private static final String[] HEXPADDING = new String[16];
/*  23: 45 */   private static final String[] BYTEPADDING = new String[16];
/*  24: 46 */   private static final char[] BYTE2CHAR = new char[256];
/*  25:    */   protected final InternalLogger logger;
/*  26:    */   protected final InternalLogLevel internalLevel;
/*  27:    */   private final LogLevel level;
/*  28:    */   
/*  29:    */   static
/*  30:    */   {
/*  31: 52 */     for (int i = 0; i < BYTE2HEX.length; i++) {
/*  32: 53 */       BYTE2HEX[i] = (' ' + StringUtil.byteToHexStringPadded(i));
/*  33:    */     }
/*  34: 57 */     for (i = 0; i < HEXPADDING.length; i++)
/*  35:    */     {
/*  36: 58 */       int padding = HEXPADDING.length - i;
/*  37: 59 */       StringBuilder buf = new StringBuilder(padding * 3);
/*  38: 60 */       for (int j = 0; j < padding; j++) {
/*  39: 61 */         buf.append("   ");
/*  40:    */       }
/*  41: 63 */       HEXPADDING[i] = buf.toString();
/*  42:    */     }
/*  43: 67 */     for (i = 0; i < BYTEPADDING.length; i++)
/*  44:    */     {
/*  45: 68 */       int padding = BYTEPADDING.length - i;
/*  46: 69 */       StringBuilder buf = new StringBuilder(padding);
/*  47: 70 */       for (int j = 0; j < padding; j++) {
/*  48: 71 */         buf.append(' ');
/*  49:    */       }
/*  50: 73 */       BYTEPADDING[i] = buf.toString();
/*  51:    */     }
/*  52: 77 */     for (i = 0; i < BYTE2CHAR.length; i++) {
/*  53: 78 */       if ((i <= 31) || (i >= 127)) {
/*  54: 79 */         BYTE2CHAR[i] = '.';
/*  55:    */       } else {
/*  56: 81 */         BYTE2CHAR[i] = ((char)i);
/*  57:    */       }
/*  58:    */     }
/*  59:    */   }
/*  60:    */   
/*  61:    */   public LoggingHandler()
/*  62:    */   {
/*  63: 96 */     this(DEFAULT_LEVEL);
/*  64:    */   }
/*  65:    */   
/*  66:    */   public LoggingHandler(LogLevel level)
/*  67:    */   {
/*  68:106 */     if (level == null) {
/*  69:107 */       throw new NullPointerException("level");
/*  70:    */     }
/*  71:110 */     this.logger = InternalLoggerFactory.getInstance(getClass());
/*  72:111 */     this.level = level;
/*  73:112 */     this.internalLevel = level.toInternalLevel();
/*  74:    */   }
/*  75:    */   
/*  76:    */   public LoggingHandler(Class<?> clazz)
/*  77:    */   {
/*  78:120 */     this(clazz, DEFAULT_LEVEL);
/*  79:    */   }
/*  80:    */   
/*  81:    */   public LoggingHandler(Class<?> clazz, LogLevel level)
/*  82:    */   {
/*  83:129 */     if (clazz == null) {
/*  84:130 */       throw new NullPointerException("clazz");
/*  85:    */     }
/*  86:132 */     if (level == null) {
/*  87:133 */       throw new NullPointerException("level");
/*  88:    */     }
/*  89:135 */     this.logger = InternalLoggerFactory.getInstance(clazz);
/*  90:136 */     this.level = level;
/*  91:137 */     this.internalLevel = level.toInternalLevel();
/*  92:    */   }
/*  93:    */   
/*  94:    */   public LoggingHandler(String name)
/*  95:    */   {
/*  96:144 */     this(name, DEFAULT_LEVEL);
/*  97:    */   }
/*  98:    */   
/*  99:    */   public LoggingHandler(String name, LogLevel level)
/* 100:    */   {
/* 101:153 */     if (name == null) {
/* 102:154 */       throw new NullPointerException("name");
/* 103:    */     }
/* 104:156 */     if (level == null) {
/* 105:157 */       throw new NullPointerException("level");
/* 106:    */     }
/* 107:159 */     this.logger = InternalLoggerFactory.getInstance(name);
/* 108:160 */     this.level = level;
/* 109:161 */     this.internalLevel = level.toInternalLevel();
/* 110:    */   }
/* 111:    */   
/* 112:    */   public LogLevel level()
/* 113:    */   {
/* 114:168 */     return this.level;
/* 115:    */   }
/* 116:    */   
/* 117:    */   protected String format(ChannelHandlerContext ctx, String message)
/* 118:    */   {
/* 119:172 */     String chStr = ctx.channel().toString();
/* 120:173 */     StringBuilder buf = new StringBuilder(chStr.length() + message.length() + 1);
/* 121:174 */     buf.append(chStr);
/* 122:175 */     buf.append(' ');
/* 123:176 */     buf.append(message);
/* 124:177 */     return buf.toString();
/* 125:    */   }
/* 126:    */   
/* 127:    */   public void channelRegistered(ChannelHandlerContext ctx)
/* 128:    */     throws Exception
/* 129:    */   {
/* 130:183 */     if (this.logger.isEnabled(this.internalLevel)) {
/* 131:184 */       this.logger.log(this.internalLevel, format(ctx, "REGISTERED"));
/* 132:    */     }
/* 133:186 */     super.channelRegistered(ctx);
/* 134:    */   }
/* 135:    */   
/* 136:    */   public void channelUnregistered(ChannelHandlerContext ctx)
/* 137:    */     throws Exception
/* 138:    */   {
/* 139:192 */     if (this.logger.isEnabled(this.internalLevel)) {
/* 140:193 */       this.logger.log(this.internalLevel, format(ctx, "UNREGISTERED"));
/* 141:    */     }
/* 142:195 */     super.channelUnregistered(ctx);
/* 143:    */   }
/* 144:    */   
/* 145:    */   public void channelActive(ChannelHandlerContext ctx)
/* 146:    */     throws Exception
/* 147:    */   {
/* 148:201 */     if (this.logger.isEnabled(this.internalLevel)) {
/* 149:202 */       this.logger.log(this.internalLevel, format(ctx, "ACTIVE"));
/* 150:    */     }
/* 151:204 */     super.channelActive(ctx);
/* 152:    */   }
/* 153:    */   
/* 154:    */   public void channelInactive(ChannelHandlerContext ctx)
/* 155:    */     throws Exception
/* 156:    */   {
/* 157:210 */     if (this.logger.isEnabled(this.internalLevel)) {
/* 158:211 */       this.logger.log(this.internalLevel, format(ctx, "INACTIVE"));
/* 159:    */     }
/* 160:213 */     super.channelInactive(ctx);
/* 161:    */   }
/* 162:    */   
/* 163:    */   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
/* 164:    */     throws Exception
/* 165:    */   {
/* 166:219 */     if (this.logger.isEnabled(this.internalLevel)) {
/* 167:220 */       this.logger.log(this.internalLevel, format(ctx, "EXCEPTION: " + cause), cause);
/* 168:    */     }
/* 169:222 */     super.exceptionCaught(ctx, cause);
/* 170:    */   }
/* 171:    */   
/* 172:    */   public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
/* 173:    */     throws Exception
/* 174:    */   {
/* 175:228 */     if (this.logger.isEnabled(this.internalLevel)) {
/* 176:229 */       this.logger.log(this.internalLevel, format(ctx, "USER_EVENT: " + evt));
/* 177:    */     }
/* 178:231 */     super.userEventTriggered(ctx, evt);
/* 179:    */   }
/* 180:    */   
/* 181:    */   public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise)
/* 182:    */     throws Exception
/* 183:    */   {
/* 184:237 */     if (this.logger.isEnabled(this.internalLevel)) {
/* 185:238 */       this.logger.log(this.internalLevel, format(ctx, "BIND(" + localAddress + ')'));
/* 186:    */     }
/* 187:240 */     super.bind(ctx, localAddress, promise);
/* 188:    */   }
/* 189:    */   
/* 190:    */   public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
/* 191:    */     throws Exception
/* 192:    */   {
/* 193:247 */     if (this.logger.isEnabled(this.internalLevel)) {
/* 194:248 */       this.logger.log(this.internalLevel, format(ctx, "CONNECT(" + remoteAddress + ", " + localAddress + ')'));
/* 195:    */     }
/* 196:250 */     super.connect(ctx, remoteAddress, localAddress, promise);
/* 197:    */   }
/* 198:    */   
/* 199:    */   public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise)
/* 200:    */     throws Exception
/* 201:    */   {
/* 202:256 */     if (this.logger.isEnabled(this.internalLevel)) {
/* 203:257 */       this.logger.log(this.internalLevel, format(ctx, "DISCONNECT()"));
/* 204:    */     }
/* 205:259 */     super.disconnect(ctx, promise);
/* 206:    */   }
/* 207:    */   
/* 208:    */   public void close(ChannelHandlerContext ctx, ChannelPromise promise)
/* 209:    */     throws Exception
/* 210:    */   {
/* 211:265 */     if (this.logger.isEnabled(this.internalLevel)) {
/* 212:266 */       this.logger.log(this.internalLevel, format(ctx, "CLOSE()"));
/* 213:    */     }
/* 214:268 */     super.close(ctx, promise);
/* 215:    */   }
/* 216:    */   
/* 217:    */   public void deregister(ChannelHandlerContext ctx, ChannelPromise promise)
/* 218:    */     throws Exception
/* 219:    */   {
/* 220:274 */     if (this.logger.isEnabled(this.internalLevel)) {
/* 221:275 */       this.logger.log(this.internalLevel, format(ctx, "DEREGISTER()"));
/* 222:    */     }
/* 223:277 */     super.deregister(ctx, promise);
/* 224:    */   }
/* 225:    */   
/* 226:    */   public void channelRead(ChannelHandlerContext ctx, Object msg)
/* 227:    */     throws Exception
/* 228:    */   {
/* 229:282 */     logMessage(ctx, "RECEIVED", msg);
/* 230:283 */     ctx.fireChannelRead(msg);
/* 231:    */   }
/* 232:    */   
/* 233:    */   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/* 234:    */     throws Exception
/* 235:    */   {
/* 236:288 */     logMessage(ctx, "WRITE", msg);
/* 237:289 */     ctx.write(msg, promise);
/* 238:    */   }
/* 239:    */   
/* 240:    */   public void flush(ChannelHandlerContext ctx)
/* 241:    */     throws Exception
/* 242:    */   {
/* 243:294 */     if (this.logger.isEnabled(this.internalLevel)) {
/* 244:295 */       this.logger.log(this.internalLevel, format(ctx, "FLUSH"));
/* 245:    */     }
/* 246:297 */     ctx.flush();
/* 247:    */   }
/* 248:    */   
/* 249:    */   private void logMessage(ChannelHandlerContext ctx, String eventName, Object msg)
/* 250:    */   {
/* 251:301 */     if (this.logger.isEnabled(this.internalLevel)) {
/* 252:302 */       this.logger.log(this.internalLevel, format(ctx, formatMessage(eventName, msg)));
/* 253:    */     }
/* 254:    */   }
/* 255:    */   
/* 256:    */   protected String formatMessage(String eventName, Object msg)
/* 257:    */   {
/* 258:307 */     if ((msg instanceof ByteBuf)) {
/* 259:308 */       return formatByteBuf(eventName, (ByteBuf)msg);
/* 260:    */     }
/* 261:309 */     if ((msg instanceof ByteBufHolder)) {
/* 262:310 */       return formatByteBufHolder(eventName, (ByteBufHolder)msg);
/* 263:    */     }
/* 264:312 */     return formatNonByteBuf(eventName, msg);
/* 265:    */   }
/* 266:    */   
/* 267:    */   protected String formatByteBuf(String eventName, ByteBuf buf)
/* 268:    */   {
/* 269:320 */     int length = buf.readableBytes();
/* 270:321 */     int rows = length / 16 + (length % 15 == 0 ? 0 : 1) + 4;
/* 271:322 */     StringBuilder dump = new StringBuilder(rows * 80 + eventName.length() + 16);
/* 272:    */     
/* 273:324 */     dump.append(eventName).append('(').append(length).append('B').append(')');
/* 274:325 */     dump.append(NEWLINE + "         +-------------------------------------------------+" + NEWLINE + "         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |" + NEWLINE + "+--------+-------------------------------------------------+----------------+");
/* 275:    */     
/* 276:    */ 
/* 277:    */ 
/* 278:    */ 
/* 279:330 */     int startIndex = buf.readerIndex();
/* 280:331 */     int endIndex = buf.writerIndex();
/* 281:334 */     for (int i = startIndex; i < endIndex; i++)
/* 282:    */     {
/* 283:335 */       int relIdx = i - startIndex;
/* 284:336 */       int relIdxMod16 = relIdx & 0xF;
/* 285:337 */       if (relIdxMod16 == 0)
/* 286:    */       {
/* 287:338 */         dump.append(NEWLINE);
/* 288:339 */         dump.append(Long.toHexString(relIdx & 0xFFFFFFFF | 0x0));
/* 289:340 */         dump.setCharAt(dump.length() - 9, '|');
/* 290:341 */         dump.append('|');
/* 291:    */       }
/* 292:343 */       dump.append(BYTE2HEX[buf.getUnsignedByte(i)]);
/* 293:344 */       if (relIdxMod16 == 15)
/* 294:    */       {
/* 295:345 */         dump.append(" |");
/* 296:346 */         for (int j = i - 15; j <= i; j++) {
/* 297:347 */           dump.append(BYTE2CHAR[buf.getUnsignedByte(j)]);
/* 298:    */         }
/* 299:349 */         dump.append('|');
/* 300:    */       }
/* 301:    */     }
/* 302:353 */     if ((i - startIndex & 0xF) != 0)
/* 303:    */     {
/* 304:354 */       int remainder = length & 0xF;
/* 305:355 */       dump.append(HEXPADDING[remainder]);
/* 306:356 */       dump.append(" |");
/* 307:357 */       for (int j = i - remainder; j < i; j++) {
/* 308:358 */         dump.append(BYTE2CHAR[buf.getUnsignedByte(j)]);
/* 309:    */       }
/* 310:360 */       dump.append(BYTEPADDING[remainder]);
/* 311:361 */       dump.append('|');
/* 312:    */     }
/* 313:364 */     dump.append(NEWLINE + "+--------+-------------------------------------------------+----------------+");
/* 314:    */     
/* 315:    */ 
/* 316:367 */     return dump.toString();
/* 317:    */   }
/* 318:    */   
/* 319:    */   protected String formatNonByteBuf(String eventName, Object msg)
/* 320:    */   {
/* 321:374 */     return eventName + ": " + msg;
/* 322:    */   }
/* 323:    */   
/* 324:    */   protected String formatByteBufHolder(String eventName, ByteBufHolder msg)
/* 325:    */   {
/* 326:384 */     return formatByteBuf(eventName, msg.content());
/* 327:    */   }
/* 328:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.logging.LoggingHandler
 * JD-Core Version:    0.7.0.1
 */