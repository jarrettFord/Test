/*   1:    */ package io.netty.util.internal.logging;
/*   2:    */ 
/*   3:    */ import org.apache.log4j.Level;
/*   4:    */ import org.apache.log4j.Logger;
/*   5:    */ 
/*   6:    */ class Log4JLogger
/*   7:    */   extends AbstractInternalLogger
/*   8:    */ {
/*   9:    */   private static final long serialVersionUID = 2851357342488183058L;
/*  10:    */   final transient Logger logger;
/*  11: 59 */   static final String FQCN = Log4JLogger.class.getName();
/*  12:    */   final boolean traceCapable;
/*  13:    */   
/*  14:    */   Log4JLogger(Logger logger)
/*  15:    */   {
/*  16: 66 */     super(logger.getName());
/*  17: 67 */     this.logger = logger;
/*  18: 68 */     this.traceCapable = isTraceCapable();
/*  19:    */   }
/*  20:    */   
/*  21:    */   private boolean isTraceCapable()
/*  22:    */   {
/*  23:    */     try
/*  24:    */     {
/*  25: 73 */       this.logger.isTraceEnabled();
/*  26: 74 */       return true;
/*  27:    */     }
/*  28:    */     catch (NoSuchMethodError e) {}
/*  29: 76 */     return false;
/*  30:    */   }
/*  31:    */   
/*  32:    */   public boolean isTraceEnabled()
/*  33:    */   {
/*  34: 87 */     if (this.traceCapable) {
/*  35: 88 */       return this.logger.isTraceEnabled();
/*  36:    */     }
/*  37: 90 */     return this.logger.isDebugEnabled();
/*  38:    */   }
/*  39:    */   
/*  40:    */   public void trace(String msg)
/*  41:    */   {
/*  42:102 */     this.logger.log(FQCN, this.traceCapable ? Level.TRACE : Level.DEBUG, msg, null);
/*  43:    */   }
/*  44:    */   
/*  45:    */   public void trace(String format, Object arg)
/*  46:    */   {
/*  47:121 */     if (isTraceEnabled())
/*  48:    */     {
/*  49:122 */       FormattingTuple ft = MessageFormatter.format(format, arg);
/*  50:123 */       this.logger.log(FQCN, this.traceCapable ? Level.TRACE : Level.DEBUG, ft.getMessage(), ft.getThrowable());
/*  51:    */     }
/*  52:    */   }
/*  53:    */   
/*  54:    */   public void trace(String format, Object argA, Object argB)
/*  55:    */   {
/*  56:146 */     if (isTraceEnabled())
/*  57:    */     {
/*  58:147 */       FormattingTuple ft = MessageFormatter.format(format, argA, argB);
/*  59:148 */       this.logger.log(FQCN, this.traceCapable ? Level.TRACE : Level.DEBUG, ft.getMessage(), ft.getThrowable());
/*  60:    */     }
/*  61:    */   }
/*  62:    */   
/*  63:    */   public void trace(String format, Object... arguments)
/*  64:    */   {
/*  65:169 */     if (isTraceEnabled())
/*  66:    */     {
/*  67:170 */       FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
/*  68:171 */       this.logger.log(FQCN, this.traceCapable ? Level.TRACE : Level.DEBUG, ft.getMessage(), ft.getThrowable());
/*  69:    */     }
/*  70:    */   }
/*  71:    */   
/*  72:    */   public void trace(String msg, Throwable t)
/*  73:    */   {
/*  74:186 */     this.logger.log(FQCN, this.traceCapable ? Level.TRACE : Level.DEBUG, msg, t);
/*  75:    */   }
/*  76:    */   
/*  77:    */   public boolean isDebugEnabled()
/*  78:    */   {
/*  79:196 */     return this.logger.isDebugEnabled();
/*  80:    */   }
/*  81:    */   
/*  82:    */   public void debug(String msg)
/*  83:    */   {
/*  84:207 */     this.logger.log(FQCN, Level.DEBUG, msg, null);
/*  85:    */   }
/*  86:    */   
/*  87:    */   public void debug(String format, Object arg)
/*  88:    */   {
/*  89:226 */     if (this.logger.isDebugEnabled())
/*  90:    */     {
/*  91:227 */       FormattingTuple ft = MessageFormatter.format(format, arg);
/*  92:228 */       this.logger.log(FQCN, Level.DEBUG, ft.getMessage(), ft.getThrowable());
/*  93:    */     }
/*  94:    */   }
/*  95:    */   
/*  96:    */   public void debug(String format, Object argA, Object argB)
/*  97:    */   {
/*  98:250 */     if (this.logger.isDebugEnabled())
/*  99:    */     {
/* 100:251 */       FormattingTuple ft = MessageFormatter.format(format, argA, argB);
/* 101:252 */       this.logger.log(FQCN, Level.DEBUG, ft.getMessage(), ft.getThrowable());
/* 102:    */     }
/* 103:    */   }
/* 104:    */   
/* 105:    */   public void debug(String format, Object... arguments)
/* 106:    */   {
/* 107:271 */     if (this.logger.isDebugEnabled())
/* 108:    */     {
/* 109:272 */       FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
/* 110:273 */       this.logger.log(FQCN, Level.DEBUG, ft.getMessage(), ft.getThrowable());
/* 111:    */     }
/* 112:    */   }
/* 113:    */   
/* 114:    */   public void debug(String msg, Throwable t)
/* 115:    */   {
/* 116:287 */     this.logger.log(FQCN, Level.DEBUG, msg, t);
/* 117:    */   }
/* 118:    */   
/* 119:    */   public boolean isInfoEnabled()
/* 120:    */   {
/* 121:297 */     return this.logger.isInfoEnabled();
/* 122:    */   }
/* 123:    */   
/* 124:    */   public void info(String msg)
/* 125:    */   {
/* 126:308 */     this.logger.log(FQCN, Level.INFO, msg, null);
/* 127:    */   }
/* 128:    */   
/* 129:    */   public void info(String format, Object arg)
/* 130:    */   {
/* 131:326 */     if (this.logger.isInfoEnabled())
/* 132:    */     {
/* 133:327 */       FormattingTuple ft = MessageFormatter.format(format, arg);
/* 134:328 */       this.logger.log(FQCN, Level.INFO, ft.getMessage(), ft.getThrowable());
/* 135:    */     }
/* 136:    */   }
/* 137:    */   
/* 138:    */   public void info(String format, Object argA, Object argB)
/* 139:    */   {
/* 140:350 */     if (this.logger.isInfoEnabled())
/* 141:    */     {
/* 142:351 */       FormattingTuple ft = MessageFormatter.format(format, argA, argB);
/* 143:352 */       this.logger.log(FQCN, Level.INFO, ft.getMessage(), ft.getThrowable());
/* 144:    */     }
/* 145:    */   }
/* 146:    */   
/* 147:    */   public void info(String format, Object... argArray)
/* 148:    */   {
/* 149:372 */     if (this.logger.isInfoEnabled())
/* 150:    */     {
/* 151:373 */       FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
/* 152:374 */       this.logger.log(FQCN, Level.INFO, ft.getMessage(), ft.getThrowable());
/* 153:    */     }
/* 154:    */   }
/* 155:    */   
/* 156:    */   public void info(String msg, Throwable t)
/* 157:    */   {
/* 158:389 */     this.logger.log(FQCN, Level.INFO, msg, t);
/* 159:    */   }
/* 160:    */   
/* 161:    */   public boolean isWarnEnabled()
/* 162:    */   {
/* 163:399 */     return this.logger.isEnabledFor(Level.WARN);
/* 164:    */   }
/* 165:    */   
/* 166:    */   public void warn(String msg)
/* 167:    */   {
/* 168:410 */     this.logger.log(FQCN, Level.WARN, msg, null);
/* 169:    */   }
/* 170:    */   
/* 171:    */   public void warn(String format, Object arg)
/* 172:    */   {
/* 173:429 */     if (this.logger.isEnabledFor(Level.WARN))
/* 174:    */     {
/* 175:430 */       FormattingTuple ft = MessageFormatter.format(format, arg);
/* 176:431 */       this.logger.log(FQCN, Level.WARN, ft.getMessage(), ft.getThrowable());
/* 177:    */     }
/* 178:    */   }
/* 179:    */   
/* 180:    */   public void warn(String format, Object argA, Object argB)
/* 181:    */   {
/* 182:453 */     if (this.logger.isEnabledFor(Level.WARN))
/* 183:    */     {
/* 184:454 */       FormattingTuple ft = MessageFormatter.format(format, argA, argB);
/* 185:455 */       this.logger.log(FQCN, Level.WARN, ft.getMessage(), ft.getThrowable());
/* 186:    */     }
/* 187:    */   }
/* 188:    */   
/* 189:    */   public void warn(String format, Object... argArray)
/* 190:    */   {
/* 191:475 */     if (this.logger.isEnabledFor(Level.WARN))
/* 192:    */     {
/* 193:476 */       FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
/* 194:477 */       this.logger.log(FQCN, Level.WARN, ft.getMessage(), ft.getThrowable());
/* 195:    */     }
/* 196:    */   }
/* 197:    */   
/* 198:    */   public void warn(String msg, Throwable t)
/* 199:    */   {
/* 200:492 */     this.logger.log(FQCN, Level.WARN, msg, t);
/* 201:    */   }
/* 202:    */   
/* 203:    */   public boolean isErrorEnabled()
/* 204:    */   {
/* 205:502 */     return this.logger.isEnabledFor(Level.ERROR);
/* 206:    */   }
/* 207:    */   
/* 208:    */   public void error(String msg)
/* 209:    */   {
/* 210:513 */     this.logger.log(FQCN, Level.ERROR, msg, null);
/* 211:    */   }
/* 212:    */   
/* 213:    */   public void error(String format, Object arg)
/* 214:    */   {
/* 215:532 */     if (this.logger.isEnabledFor(Level.ERROR))
/* 216:    */     {
/* 217:533 */       FormattingTuple ft = MessageFormatter.format(format, arg);
/* 218:534 */       this.logger.log(FQCN, Level.ERROR, ft.getMessage(), ft.getThrowable());
/* 219:    */     }
/* 220:    */   }
/* 221:    */   
/* 222:    */   public void error(String format, Object argA, Object argB)
/* 223:    */   {
/* 224:556 */     if (this.logger.isEnabledFor(Level.ERROR))
/* 225:    */     {
/* 226:557 */       FormattingTuple ft = MessageFormatter.format(format, argA, argB);
/* 227:558 */       this.logger.log(FQCN, Level.ERROR, ft.getMessage(), ft.getThrowable());
/* 228:    */     }
/* 229:    */   }
/* 230:    */   
/* 231:    */   public void error(String format, Object... argArray)
/* 232:    */   {
/* 233:578 */     if (this.logger.isEnabledFor(Level.ERROR))
/* 234:    */     {
/* 235:579 */       FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
/* 236:580 */       this.logger.log(FQCN, Level.ERROR, ft.getMessage(), ft.getThrowable());
/* 237:    */     }
/* 238:    */   }
/* 239:    */   
/* 240:    */   public void error(String msg, Throwable t)
/* 241:    */   {
/* 242:595 */     this.logger.log(FQCN, Level.ERROR, msg, t);
/* 243:    */   }
/* 244:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.logging.Log4JLogger
 * JD-Core Version:    0.7.0.1
 */