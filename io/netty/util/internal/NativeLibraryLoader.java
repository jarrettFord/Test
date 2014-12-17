/*   1:    */ package io.netty.util.internal;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.logging.InternalLogger;
/*   4:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   5:    */ import java.io.File;
/*   6:    */ import java.io.FileOutputStream;
/*   7:    */ import java.io.IOException;
/*   8:    */ import java.io.InputStream;
/*   9:    */ import java.io.OutputStream;
/*  10:    */ import java.net.URL;
/*  11:    */ import java.util.Locale;
/*  12:    */ 
/*  13:    */ public final class NativeLibraryLoader
/*  14:    */ {
/*  15: 35 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(NativeLibraryLoader.class);
/*  16:    */   private static final String NATIVE_RESOURCE_HOME = "META-INF/native/";
/*  17: 42 */   private static final String OSNAME = SystemPropertyUtil.get("os.name", "").toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", "");
/*  18:    */   private static final File WORKDIR;
/*  19:    */   
/*  20:    */   static
/*  21:    */   {
/*  22: 44 */     String workdir = SystemPropertyUtil.get("io.netty.native.workdir");
/*  23: 45 */     if (workdir != null)
/*  24:    */     {
/*  25: 46 */       File f = new File(workdir);
/*  26: 47 */       if (!f.exists()) {
/*  27: 50 */         f.mkdirs();
/*  28:    */       }
/*  29:    */       try
/*  30:    */       {
/*  31: 54 */         f = f.getAbsoluteFile();
/*  32:    */       }
/*  33:    */       catch (Exception ignored) {}
/*  34: 59 */       WORKDIR = f;
/*  35: 60 */       logger.debug("-Dio.netty.netty.workdir: " + WORKDIR);
/*  36:    */     }
/*  37:    */     else
/*  38:    */     {
/*  39: 62 */       WORKDIR = tmpdir();
/*  40: 63 */       logger.debug("-Dio.netty.netty.workdir: " + WORKDIR + " (io.netty.tmpdir)");
/*  41:    */     }
/*  42:    */   }
/*  43:    */   
/*  44:    */   private static File tmpdir()
/*  45:    */   {
/*  46:    */     File f;
/*  47:    */     try
/*  48:    */     {
/*  49: 70 */       f = toDirectory(SystemPropertyUtil.get("io.netty.tmpdir"));
/*  50: 71 */       if (f != null)
/*  51:    */       {
/*  52: 72 */         logger.debug("-Dio.netty.tmpdir: " + f);
/*  53: 73 */         return f;
/*  54:    */       }
/*  55: 76 */       f = toDirectory(SystemPropertyUtil.get("java.io.tmpdir"));
/*  56: 77 */       if (f != null)
/*  57:    */       {
/*  58: 78 */         logger.debug("-Dio.netty.tmpdir: " + f + " (java.io.tmpdir)");
/*  59: 79 */         return f;
/*  60:    */       }
/*  61: 83 */       if (isWindows())
/*  62:    */       {
/*  63: 84 */         f = toDirectory(System.getenv("TEMP"));
/*  64: 85 */         if (f != null)
/*  65:    */         {
/*  66: 86 */           logger.debug("-Dio.netty.tmpdir: " + f + " (%TEMP%)");
/*  67: 87 */           return f;
/*  68:    */         }
/*  69: 90 */         String userprofile = System.getenv("USERPROFILE");
/*  70: 91 */         if (userprofile != null)
/*  71:    */         {
/*  72: 92 */           f = toDirectory(userprofile + "\\AppData\\Local\\Temp");
/*  73: 93 */           if (f != null)
/*  74:    */           {
/*  75: 94 */             logger.debug("-Dio.netty.tmpdir: " + f + " (%USERPROFILE%\\AppData\\Local\\Temp)");
/*  76: 95 */             return f;
/*  77:    */           }
/*  78: 98 */           f = toDirectory(userprofile + "\\Local Settings\\Temp");
/*  79: 99 */           if (f != null)
/*  80:    */           {
/*  81:100 */             logger.debug("-Dio.netty.tmpdir: " + f + " (%USERPROFILE%\\Local Settings\\Temp)");
/*  82:101 */             return f;
/*  83:    */           }
/*  84:    */         }
/*  85:    */       }
/*  86:    */       else
/*  87:    */       {
/*  88:105 */         f = toDirectory(System.getenv("TMPDIR"));
/*  89:106 */         if (f != null)
/*  90:    */         {
/*  91:107 */           logger.debug("-Dio.netty.tmpdir: " + f + " ($TMPDIR)");
/*  92:108 */           return f;
/*  93:    */         }
/*  94:    */       }
/*  95:    */     }
/*  96:    */     catch (Exception ignored) {}
/*  97:    */     File f;
/*  98:116 */     if (isWindows()) {
/*  99:117 */       f = new File("C:\\Windows\\Temp");
/* 100:    */     } else {
/* 101:119 */       f = new File("/tmp");
/* 102:    */     }
/* 103:122 */     logger.warn("Failed to get the temporary directory; falling back to: " + f);
/* 104:123 */     return f;
/* 105:    */   }
/* 106:    */   
/* 107:    */   private static File toDirectory(String path)
/* 108:    */   {
/* 109:128 */     if (path == null) {
/* 110:129 */       return null;
/* 111:    */     }
/* 112:132 */     File f = new File(path);
/* 113:133 */     if (!f.exists()) {
/* 114:134 */       f.mkdirs();
/* 115:    */     }
/* 116:137 */     if (!f.isDirectory()) {
/* 117:138 */       return null;
/* 118:    */     }
/* 119:    */     try
/* 120:    */     {
/* 121:142 */       return f.getAbsoluteFile();
/* 122:    */     }
/* 123:    */     catch (Exception ignored) {}
/* 124:144 */     return f;
/* 125:    */   }
/* 126:    */   
/* 127:    */   private static boolean isWindows()
/* 128:    */   {
/* 129:149 */     return OSNAME.startsWith("windows");
/* 130:    */   }
/* 131:    */   
/* 132:    */   private static boolean isOSX()
/* 133:    */   {
/* 134:153 */     return (OSNAME.startsWith("macosx")) || (OSNAME.startsWith("osx"));
/* 135:    */   }
/* 136:    */   
/* 137:    */   public static void load(String name, ClassLoader loader)
/* 138:    */   {
/* 139:160 */     String libname = System.mapLibraryName(name);
/* 140:161 */     String path = "META-INF/native/" + libname;
/* 141:    */     
/* 142:163 */     URL url = loader.getResource(path);
/* 143:164 */     if ((url == null) && (isOSX())) {
/* 144:165 */       if (path.endsWith(".jnilib")) {
/* 145:166 */         url = loader.getResource("META-INF/native/lib" + name + ".dynlib");
/* 146:    */       } else {
/* 147:168 */         url = loader.getResource("META-INF/native/lib" + name + ".jnilib");
/* 148:    */       }
/* 149:    */     }
/* 150:172 */     if (url == null)
/* 151:    */     {
/* 152:174 */       System.loadLibrary(name);
/* 153:175 */       return;
/* 154:    */     }
/* 155:178 */     int index = libname.lastIndexOf('.');
/* 156:179 */     String prefix = libname.substring(0, index);
/* 157:180 */     String suffix = libname.substring(index, libname.length());
/* 158:181 */     InputStream in = null;
/* 159:182 */     OutputStream out = null;
/* 160:183 */     File tmpFile = null;
/* 161:184 */     boolean loaded = false;
/* 162:    */     try
/* 163:    */     {
/* 164:186 */       tmpFile = File.createTempFile(prefix, suffix, WORKDIR);
/* 165:187 */       in = url.openStream();
/* 166:188 */       out = new FileOutputStream(tmpFile);
/* 167:    */       
/* 168:190 */       byte[] buffer = new byte[8192];
/* 169:    */       int length;
/* 170:192 */       while ((length = in.read(buffer)) > 0) {
/* 171:193 */         out.write(buffer, 0, length);
/* 172:    */       }
/* 173:195 */       out.flush();
/* 174:196 */       out.close();
/* 175:197 */       out = null;
/* 176:    */       
/* 177:199 */       System.load(tmpFile.getPath());
/* 178:200 */       loaded = true;
/* 179:    */     }
/* 180:    */     catch (Exception e)
/* 181:    */     {
/* 182:202 */       throw ((UnsatisfiedLinkError)new UnsatisfiedLinkError("could not load a native library: " + name).initCause(e));
/* 183:    */     }
/* 184:    */     finally
/* 185:    */     {
/* 186:205 */       if (in != null) {
/* 187:    */         try
/* 188:    */         {
/* 189:207 */           in.close();
/* 190:    */         }
/* 191:    */         catch (IOException ignore) {}
/* 192:    */       }
/* 193:212 */       if (out != null) {
/* 194:    */         try
/* 195:    */         {
/* 196:214 */           out.close();
/* 197:    */         }
/* 198:    */         catch (IOException ignore) {}
/* 199:    */       }
/* 200:219 */       if (tmpFile != null) {
/* 201:220 */         if (loaded) {
/* 202:221 */           tmpFile.deleteOnExit();
/* 203:223 */         } else if (!tmpFile.delete()) {
/* 204:224 */           tmpFile.deleteOnExit();
/* 205:    */         }
/* 206:    */       }
/* 207:    */     }
/* 208:    */   }
/* 209:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.NativeLibraryLoader
 * JD-Core Version:    0.7.0.1
 */