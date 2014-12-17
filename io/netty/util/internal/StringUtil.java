/*   1:    */ package io.netty.util.internal;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import java.util.ArrayList;
/*   5:    */ import java.util.Formatter;
/*   6:    */ import java.util.List;
/*   7:    */ 
/*   8:    */ public final class StringUtil
/*   9:    */ {
/*  10:    */   public static final String NEWLINE;
/*  11:    */   private static final String[] BYTE2HEX_PAD;
/*  12:    */   private static final String[] BYTE2HEX_NOPAD;
/*  13:    */   private static final String EMPTY_STRING = "";
/*  14:    */   
/*  15:    */   static
/*  16:    */   {
/*  17: 30 */     BYTE2HEX_PAD = new String[256];
/*  18: 31 */     BYTE2HEX_NOPAD = new String[256];
/*  19:    */     String newLine;
/*  20:    */     try
/*  21:    */     {
/*  22: 39 */       newLine = new Formatter().format("%n", new Object[0]).toString();
/*  23:    */     }
/*  24:    */     catch (Exception e)
/*  25:    */     {
/*  26: 42 */       newLine = "\n";
/*  27:    */     }
/*  28: 45 */     NEWLINE = newLine;
/*  29: 49 */     for (int i = 0; i < 10; i++)
/*  30:    */     {
/*  31: 50 */       StringBuilder buf = new StringBuilder(2);
/*  32: 51 */       buf.append('0');
/*  33: 52 */       buf.append(i);
/*  34: 53 */       BYTE2HEX_PAD[i] = buf.toString();
/*  35: 54 */       BYTE2HEX_NOPAD[i] = String.valueOf(i);
/*  36:    */     }
/*  37: 56 */     for (; i < 16; i++)
/*  38:    */     {
/*  39: 57 */       StringBuilder buf = new StringBuilder(2);
/*  40: 58 */       char c = (char)(97 + i - 10);
/*  41: 59 */       buf.append('0');
/*  42: 60 */       buf.append(c);
/*  43: 61 */       BYTE2HEX_PAD[i] = buf.toString();
/*  44: 62 */       BYTE2HEX_NOPAD[i] = String.valueOf(c);
/*  45:    */     }
/*  46: 64 */     for (; i < BYTE2HEX_PAD.length; i++)
/*  47:    */     {
/*  48: 65 */       StringBuilder buf = new StringBuilder(2);
/*  49: 66 */       buf.append(Integer.toHexString(i));
/*  50: 67 */       String str = buf.toString();
/*  51: 68 */       BYTE2HEX_PAD[i] = str;
/*  52: 69 */       BYTE2HEX_NOPAD[i] = str;
/*  53:    */     }
/*  54:    */   }
/*  55:    */   
/*  56:    */   public static String[] split(String value, char delim)
/*  57:    */   {
/*  58: 78 */     int end = value.length();
/*  59: 79 */     List<String> res = new ArrayList();
/*  60:    */     
/*  61: 81 */     int start = 0;
/*  62: 82 */     for (int i = 0; i < end; i++) {
/*  63: 83 */       if (value.charAt(i) == delim)
/*  64:    */       {
/*  65: 84 */         if (start == i) {
/*  66: 85 */           res.add("");
/*  67:    */         } else {
/*  68: 87 */           res.add(value.substring(start, i));
/*  69:    */         }
/*  70: 89 */         start = i + 1;
/*  71:    */       }
/*  72:    */     }
/*  73: 93 */     if (start == 0) {
/*  74: 94 */       res.add(value);
/*  75: 96 */     } else if (start != end) {
/*  76: 98 */       res.add(value.substring(start, end));
/*  77:    */     } else {
/*  78:101 */       for (int i = res.size() - 1; i >= 0; i--)
/*  79:    */       {
/*  80:102 */         if (!((String)res.get(i)).isEmpty()) {
/*  81:    */           break;
/*  82:    */         }
/*  83:103 */         res.remove(i);
/*  84:    */       }
/*  85:    */     }
/*  86:111 */     return (String[])res.toArray(new String[res.size()]);
/*  87:    */   }
/*  88:    */   
/*  89:    */   public static String byteToHexStringPadded(int value)
/*  90:    */   {
/*  91:118 */     return BYTE2HEX_PAD[(value & 0xFF)];
/*  92:    */   }
/*  93:    */   
/*  94:    */   public static <T extends Appendable> T byteToHexStringPadded(T buf, int value)
/*  95:    */   {
/*  96:    */     try
/*  97:    */     {
/*  98:126 */       buf.append(byteToHexStringPadded(value));
/*  99:    */     }
/* 100:    */     catch (IOException e)
/* 101:    */     {
/* 102:128 */       PlatformDependent.throwException(e);
/* 103:    */     }
/* 104:130 */     return buf;
/* 105:    */   }
/* 106:    */   
/* 107:    */   public static String toHexStringPadded(byte[] src)
/* 108:    */   {
/* 109:137 */     return toHexStringPadded(src, 0, src.length);
/* 110:    */   }
/* 111:    */   
/* 112:    */   public static String toHexStringPadded(byte[] src, int offset, int length)
/* 113:    */   {
/* 114:144 */     return ((StringBuilder)toHexStringPadded(new StringBuilder(length << 1), src, offset, length)).toString();
/* 115:    */   }
/* 116:    */   
/* 117:    */   public static <T extends Appendable> T toHexStringPadded(T dst, byte[] src)
/* 118:    */   {
/* 119:151 */     return toHexStringPadded(dst, src, 0, src.length);
/* 120:    */   }
/* 121:    */   
/* 122:    */   public static <T extends Appendable> T toHexStringPadded(T dst, byte[] src, int offset, int length)
/* 123:    */   {
/* 124:158 */     int end = offset + length;
/* 125:159 */     for (int i = offset; i < end; i++) {
/* 126:160 */       byteToHexStringPadded(dst, src[i]);
/* 127:    */     }
/* 128:162 */     return dst;
/* 129:    */   }
/* 130:    */   
/* 131:    */   public static String byteToHexString(int value)
/* 132:    */   {
/* 133:169 */     return BYTE2HEX_NOPAD[(value & 0xFF)];
/* 134:    */   }
/* 135:    */   
/* 136:    */   public static <T extends Appendable> T byteToHexString(T buf, int value)
/* 137:    */   {
/* 138:    */     try
/* 139:    */     {
/* 140:177 */       buf.append(byteToHexString(value));
/* 141:    */     }
/* 142:    */     catch (IOException e)
/* 143:    */     {
/* 144:179 */       PlatformDependent.throwException(e);
/* 145:    */     }
/* 146:181 */     return buf;
/* 147:    */   }
/* 148:    */   
/* 149:    */   public static String toHexString(byte[] src)
/* 150:    */   {
/* 151:188 */     return toHexString(src, 0, src.length);
/* 152:    */   }
/* 153:    */   
/* 154:    */   public static String toHexString(byte[] src, int offset, int length)
/* 155:    */   {
/* 156:195 */     return ((StringBuilder)toHexString(new StringBuilder(length << 1), src, offset, length)).toString();
/* 157:    */   }
/* 158:    */   
/* 159:    */   public static <T extends Appendable> T toHexString(T dst, byte[] src)
/* 160:    */   {
/* 161:202 */     return toHexString(dst, src, 0, src.length);
/* 162:    */   }
/* 163:    */   
/* 164:    */   public static <T extends Appendable> T toHexString(T dst, byte[] src, int offset, int length)
/* 165:    */   {
/* 166:209 */     assert (length >= 0);
/* 167:210 */     if (length == 0) {
/* 168:211 */       return dst;
/* 169:    */     }
/* 170:214 */     int end = offset + length;
/* 171:215 */     int endMinusOne = end - 1;
/* 172:219 */     for (int i = offset; i < endMinusOne; i++) {
/* 173:220 */       if (src[i] != 0) {
/* 174:    */         break;
/* 175:    */       }
/* 176:    */     }
/* 177:225 */     byteToHexString(dst, src[(i++)]);
/* 178:226 */     int remaining = end - i;
/* 179:227 */     toHexStringPadded(dst, src, i, remaining);
/* 180:    */     
/* 181:229 */     return dst;
/* 182:    */   }
/* 183:    */   
/* 184:    */   public static String simpleClassName(Object o)
/* 185:    */   {
/* 186:236 */     if (o == null) {
/* 187:237 */       return "null_object";
/* 188:    */     }
/* 189:239 */     return simpleClassName(o.getClass());
/* 190:    */   }
/* 191:    */   
/* 192:    */   public static String simpleClassName(Class<?> clazz)
/* 193:    */   {
/* 194:248 */     if (clazz == null) {
/* 195:249 */       return "null_class";
/* 196:    */     }
/* 197:252 */     Package pkg = clazz.getPackage();
/* 198:253 */     if (pkg != null) {
/* 199:254 */       return clazz.getName().substring(pkg.getName().length() + 1);
/* 200:    */     }
/* 201:256 */     return clazz.getName();
/* 202:    */   }
/* 203:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.StringUtil
 * JD-Core Version:    0.7.0.1
 */