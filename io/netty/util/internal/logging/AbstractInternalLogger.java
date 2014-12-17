/*   1:    */ package io.netty.util.internal.logging;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.StringUtil;
/*   4:    */ import java.io.ObjectStreamException;
/*   5:    */ import java.io.Serializable;
/*   6:    */ 
/*   7:    */ public abstract class AbstractInternalLogger
/*   8:    */   implements InternalLogger, Serializable
/*   9:    */ {
/*  10:    */   private static final long serialVersionUID = -6382972526573193470L;
/*  11:    */   private final String name;
/*  12:    */   
/*  13:    */   protected AbstractInternalLogger(String name)
/*  14:    */   {
/*  15: 38 */     if (name == null) {
/*  16: 39 */       throw new NullPointerException("name");
/*  17:    */     }
/*  18: 41 */     this.name = name;
/*  19:    */   }
/*  20:    */   
/*  21:    */   public String name()
/*  22:    */   {
/*  23: 46 */     return this.name;
/*  24:    */   }
/*  25:    */   
/*  26:    */   public boolean isEnabled(InternalLogLevel level)
/*  27:    */   {
/*  28: 51 */     switch (1.$SwitchMap$io$netty$util$internal$logging$InternalLogLevel[level.ordinal()])
/*  29:    */     {
/*  30:    */     case 1: 
/*  31: 53 */       return isTraceEnabled();
/*  32:    */     case 2: 
/*  33: 55 */       return isDebugEnabled();
/*  34:    */     case 3: 
/*  35: 57 */       return isInfoEnabled();
/*  36:    */     case 4: 
/*  37: 59 */       return isWarnEnabled();
/*  38:    */     case 5: 
/*  39: 61 */       return isErrorEnabled();
/*  40:    */     }
/*  41: 63 */     throw new Error();
/*  42:    */   }
/*  43:    */   
/*  44:    */   public void log(InternalLogLevel level, String msg, Throwable cause)
/*  45:    */   {
/*  46: 69 */     switch (1.$SwitchMap$io$netty$util$internal$logging$InternalLogLevel[level.ordinal()])
/*  47:    */     {
/*  48:    */     case 1: 
/*  49: 71 */       trace(msg, cause);
/*  50: 72 */       break;
/*  51:    */     case 2: 
/*  52: 74 */       debug(msg, cause);
/*  53: 75 */       break;
/*  54:    */     case 3: 
/*  55: 77 */       info(msg, cause);
/*  56: 78 */       break;
/*  57:    */     case 4: 
/*  58: 80 */       warn(msg, cause);
/*  59: 81 */       break;
/*  60:    */     case 5: 
/*  61: 83 */       error(msg, cause);
/*  62: 84 */       break;
/*  63:    */     default: 
/*  64: 86 */       throw new Error();
/*  65:    */     }
/*  66:    */   }
/*  67:    */   
/*  68:    */   public void log(InternalLogLevel level, String msg)
/*  69:    */   {
/*  70: 92 */     switch (1.$SwitchMap$io$netty$util$internal$logging$InternalLogLevel[level.ordinal()])
/*  71:    */     {
/*  72:    */     case 1: 
/*  73: 94 */       trace(msg);
/*  74: 95 */       break;
/*  75:    */     case 2: 
/*  76: 97 */       debug(msg);
/*  77: 98 */       break;
/*  78:    */     case 3: 
/*  79:100 */       info(msg);
/*  80:101 */       break;
/*  81:    */     case 4: 
/*  82:103 */       warn(msg);
/*  83:104 */       break;
/*  84:    */     case 5: 
/*  85:106 */       error(msg);
/*  86:107 */       break;
/*  87:    */     default: 
/*  88:109 */       throw new Error();
/*  89:    */     }
/*  90:    */   }
/*  91:    */   
/*  92:    */   public void log(InternalLogLevel level, String format, Object arg)
/*  93:    */   {
/*  94:115 */     switch (1.$SwitchMap$io$netty$util$internal$logging$InternalLogLevel[level.ordinal()])
/*  95:    */     {
/*  96:    */     case 1: 
/*  97:117 */       trace(format, arg);
/*  98:118 */       break;
/*  99:    */     case 2: 
/* 100:120 */       debug(format, arg);
/* 101:121 */       break;
/* 102:    */     case 3: 
/* 103:123 */       info(format, arg);
/* 104:124 */       break;
/* 105:    */     case 4: 
/* 106:126 */       warn(format, arg);
/* 107:127 */       break;
/* 108:    */     case 5: 
/* 109:129 */       error(format, arg);
/* 110:130 */       break;
/* 111:    */     default: 
/* 112:132 */       throw new Error();
/* 113:    */     }
/* 114:    */   }
/* 115:    */   
/* 116:    */   public void log(InternalLogLevel level, String format, Object argA, Object argB)
/* 117:    */   {
/* 118:138 */     switch (1.$SwitchMap$io$netty$util$internal$logging$InternalLogLevel[level.ordinal()])
/* 119:    */     {
/* 120:    */     case 1: 
/* 121:140 */       trace(format, argA, argB);
/* 122:141 */       break;
/* 123:    */     case 2: 
/* 124:143 */       debug(format, argA, argB);
/* 125:144 */       break;
/* 126:    */     case 3: 
/* 127:146 */       info(format, argA, argB);
/* 128:147 */       break;
/* 129:    */     case 4: 
/* 130:149 */       warn(format, argA, argB);
/* 131:150 */       break;
/* 132:    */     case 5: 
/* 133:152 */       error(format, argA, argB);
/* 134:153 */       break;
/* 135:    */     default: 
/* 136:155 */       throw new Error();
/* 137:    */     }
/* 138:    */   }
/* 139:    */   
/* 140:    */   public void log(InternalLogLevel level, String format, Object... arguments)
/* 141:    */   {
/* 142:161 */     switch (1.$SwitchMap$io$netty$util$internal$logging$InternalLogLevel[level.ordinal()])
/* 143:    */     {
/* 144:    */     case 1: 
/* 145:163 */       trace(format, arguments);
/* 146:164 */       break;
/* 147:    */     case 2: 
/* 148:166 */       debug(format, arguments);
/* 149:167 */       break;
/* 150:    */     case 3: 
/* 151:169 */       info(format, arguments);
/* 152:170 */       break;
/* 153:    */     case 4: 
/* 154:172 */       warn(format, arguments);
/* 155:173 */       break;
/* 156:    */     case 5: 
/* 157:175 */       error(format, arguments);
/* 158:176 */       break;
/* 159:    */     default: 
/* 160:178 */       throw new Error();
/* 161:    */     }
/* 162:    */   }
/* 163:    */   
/* 164:    */   protected Object readResolve()
/* 165:    */     throws ObjectStreamException
/* 166:    */   {
/* 167:183 */     return InternalLoggerFactory.getInstance(name());
/* 168:    */   }
/* 169:    */   
/* 170:    */   public String toString()
/* 171:    */   {
/* 172:188 */     return StringUtil.simpleClassName(this) + '(' + name() + ')';
/* 173:    */   }
/* 174:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.logging.AbstractInternalLogger
 * JD-Core Version:    0.7.0.1
 */