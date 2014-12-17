/*   1:    */ package io.netty.util.internal.logging;
/*   2:    */ 
/*   3:    */ import org.apache.commons.logging.Log;
/*   4:    */ 
/*   5:    */ class CommonsLogger
/*   6:    */   extends AbstractInternalLogger
/*   7:    */ {
/*   8:    */   private static final long serialVersionUID = 8647838678388394885L;
/*   9:    */   private final transient Log logger;
/*  10:    */   
/*  11:    */   CommonsLogger(Log logger, String name)
/*  12:    */   {
/*  13: 55 */     super(name);
/*  14: 56 */     if (logger == null) {
/*  15: 57 */       throw new NullPointerException("logger");
/*  16:    */     }
/*  17: 59 */     this.logger = logger;
/*  18:    */   }
/*  19:    */   
/*  20:    */   public boolean isTraceEnabled()
/*  21:    */   {
/*  22: 68 */     return this.logger.isTraceEnabled();
/*  23:    */   }
/*  24:    */   
/*  25:    */   public void trace(String msg)
/*  26:    */   {
/*  27: 79 */     this.logger.trace(msg);
/*  28:    */   }
/*  29:    */   
/*  30:    */   public void trace(String format, Object arg)
/*  31:    */   {
/*  32: 98 */     if (this.logger.isTraceEnabled())
/*  33:    */     {
/*  34: 99 */       FormattingTuple ft = MessageFormatter.format(format, arg);
/*  35:100 */       this.logger.trace(ft.getMessage(), ft.getThrowable());
/*  36:    */     }
/*  37:    */   }
/*  38:    */   
/*  39:    */   public void trace(String format, Object argA, Object argB)
/*  40:    */   {
/*  41:122 */     if (this.logger.isTraceEnabled())
/*  42:    */     {
/*  43:123 */       FormattingTuple ft = MessageFormatter.format(format, argA, argB);
/*  44:124 */       this.logger.trace(ft.getMessage(), ft.getThrowable());
/*  45:    */     }
/*  46:    */   }
/*  47:    */   
/*  48:    */   public void trace(String format, Object... arguments)
/*  49:    */   {
/*  50:142 */     if (this.logger.isTraceEnabled())
/*  51:    */     {
/*  52:143 */       FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
/*  53:144 */       this.logger.trace(ft.getMessage(), ft.getThrowable());
/*  54:    */     }
/*  55:    */   }
/*  56:    */   
/*  57:    */   public void trace(String msg, Throwable t)
/*  58:    */   {
/*  59:159 */     this.logger.trace(msg, t);
/*  60:    */   }
/*  61:    */   
/*  62:    */   public boolean isDebugEnabled()
/*  63:    */   {
/*  64:168 */     return this.logger.isDebugEnabled();
/*  65:    */   }
/*  66:    */   
/*  67:    */   public void debug(String msg)
/*  68:    */   {
/*  69:181 */     this.logger.debug(msg);
/*  70:    */   }
/*  71:    */   
/*  72:    */   public void debug(String format, Object arg)
/*  73:    */   {
/*  74:200 */     if (this.logger.isDebugEnabled())
/*  75:    */     {
/*  76:201 */       FormattingTuple ft = MessageFormatter.format(format, arg);
/*  77:202 */       this.logger.debug(ft.getMessage(), ft.getThrowable());
/*  78:    */     }
/*  79:    */   }
/*  80:    */   
/*  81:    */   public void debug(String format, Object argA, Object argB)
/*  82:    */   {
/*  83:224 */     if (this.logger.isDebugEnabled())
/*  84:    */     {
/*  85:225 */       FormattingTuple ft = MessageFormatter.format(format, argA, argB);
/*  86:226 */       this.logger.debug(ft.getMessage(), ft.getThrowable());
/*  87:    */     }
/*  88:    */   }
/*  89:    */   
/*  90:    */   public void debug(String format, Object... arguments)
/*  91:    */   {
/*  92:244 */     if (this.logger.isDebugEnabled())
/*  93:    */     {
/*  94:245 */       FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
/*  95:246 */       this.logger.debug(ft.getMessage(), ft.getThrowable());
/*  96:    */     }
/*  97:    */   }
/*  98:    */   
/*  99:    */   public void debug(String msg, Throwable t)
/* 100:    */   {
/* 101:261 */     this.logger.debug(msg, t);
/* 102:    */   }
/* 103:    */   
/* 104:    */   public boolean isInfoEnabled()
/* 105:    */   {
/* 106:270 */     return this.logger.isInfoEnabled();
/* 107:    */   }
/* 108:    */   
/* 109:    */   public void info(String msg)
/* 110:    */   {
/* 111:281 */     this.logger.info(msg);
/* 112:    */   }
/* 113:    */   
/* 114:    */   public void info(String format, Object arg)
/* 115:    */   {
/* 116:301 */     if (this.logger.isInfoEnabled())
/* 117:    */     {
/* 118:302 */       FormattingTuple ft = MessageFormatter.format(format, arg);
/* 119:303 */       this.logger.info(ft.getMessage(), ft.getThrowable());
/* 120:    */     }
/* 121:    */   }
/* 122:    */   
/* 123:    */   public void info(String format, Object argA, Object argB)
/* 124:    */   {
/* 125:324 */     if (this.logger.isInfoEnabled())
/* 126:    */     {
/* 127:325 */       FormattingTuple ft = MessageFormatter.format(format, argA, argB);
/* 128:326 */       this.logger.info(ft.getMessage(), ft.getThrowable());
/* 129:    */     }
/* 130:    */   }
/* 131:    */   
/* 132:    */   public void info(String format, Object... arguments)
/* 133:    */   {
/* 134:344 */     if (this.logger.isInfoEnabled())
/* 135:    */     {
/* 136:345 */       FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
/* 137:346 */       this.logger.info(ft.getMessage(), ft.getThrowable());
/* 138:    */     }
/* 139:    */   }
/* 140:    */   
/* 141:    */   public void info(String msg, Throwable t)
/* 142:    */   {
/* 143:361 */     this.logger.info(msg, t);
/* 144:    */   }
/* 145:    */   
/* 146:    */   public boolean isWarnEnabled()
/* 147:    */   {
/* 148:370 */     return this.logger.isWarnEnabled();
/* 149:    */   }
/* 150:    */   
/* 151:    */   public void warn(String msg)
/* 152:    */   {
/* 153:381 */     this.logger.warn(msg);
/* 154:    */   }
/* 155:    */   
/* 156:    */   public void warn(String format, Object arg)
/* 157:    */   {
/* 158:400 */     if (this.logger.isWarnEnabled())
/* 159:    */     {
/* 160:401 */       FormattingTuple ft = MessageFormatter.format(format, arg);
/* 161:402 */       this.logger.warn(ft.getMessage(), ft.getThrowable());
/* 162:    */     }
/* 163:    */   }
/* 164:    */   
/* 165:    */   public void warn(String format, Object argA, Object argB)
/* 166:    */   {
/* 167:424 */     if (this.logger.isWarnEnabled())
/* 168:    */     {
/* 169:425 */       FormattingTuple ft = MessageFormatter.format(format, argA, argB);
/* 170:426 */       this.logger.warn(ft.getMessage(), ft.getThrowable());
/* 171:    */     }
/* 172:    */   }
/* 173:    */   
/* 174:    */   public void warn(String format, Object... arguments)
/* 175:    */   {
/* 176:444 */     if (this.logger.isWarnEnabled())
/* 177:    */     {
/* 178:445 */       FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
/* 179:446 */       this.logger.warn(ft.getMessage(), ft.getThrowable());
/* 180:    */     }
/* 181:    */   }
/* 182:    */   
/* 183:    */   public void warn(String msg, Throwable t)
/* 184:    */   {
/* 185:462 */     this.logger.warn(msg, t);
/* 186:    */   }
/* 187:    */   
/* 188:    */   public boolean isErrorEnabled()
/* 189:    */   {
/* 190:471 */     return this.logger.isErrorEnabled();
/* 191:    */   }
/* 192:    */   
/* 193:    */   public void error(String msg)
/* 194:    */   {
/* 195:482 */     this.logger.error(msg);
/* 196:    */   }
/* 197:    */   
/* 198:    */   public void error(String format, Object arg)
/* 199:    */   {
/* 200:501 */     if (this.logger.isErrorEnabled())
/* 201:    */     {
/* 202:502 */       FormattingTuple ft = MessageFormatter.format(format, arg);
/* 203:503 */       this.logger.error(ft.getMessage(), ft.getThrowable());
/* 204:    */     }
/* 205:    */   }
/* 206:    */   
/* 207:    */   public void error(String format, Object argA, Object argB)
/* 208:    */   {
/* 209:525 */     if (this.logger.isErrorEnabled())
/* 210:    */     {
/* 211:526 */       FormattingTuple ft = MessageFormatter.format(format, argA, argB);
/* 212:527 */       this.logger.error(ft.getMessage(), ft.getThrowable());
/* 213:    */     }
/* 214:    */   }
/* 215:    */   
/* 216:    */   public void error(String format, Object... arguments)
/* 217:    */   {
/* 218:545 */     if (this.logger.isErrorEnabled())
/* 219:    */     {
/* 220:546 */       FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
/* 221:547 */       this.logger.error(ft.getMessage(), ft.getThrowable());
/* 222:    */     }
/* 223:    */   }
/* 224:    */   
/* 225:    */   public void error(String msg, Throwable t)
/* 226:    */   {
/* 227:562 */     this.logger.error(msg, t);
/* 228:    */   }
/* 229:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.logging.CommonsLogger
 * JD-Core Version:    0.7.0.1
 */