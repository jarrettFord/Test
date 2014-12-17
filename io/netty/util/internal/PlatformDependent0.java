/*   1:    */ package io.netty.util.internal;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.logging.InternalLogger;
/*   4:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   5:    */ import java.lang.reflect.Field;
/*   6:    */ import java.lang.reflect.Method;
/*   7:    */ import java.nio.Buffer;
/*   8:    */ import java.nio.ByteBuffer;
/*   9:    */ import java.nio.ByteOrder;
/*  10:    */ import java.security.AccessController;
/*  11:    */ import java.security.PrivilegedAction;
/*  12:    */ import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
/*  13:    */ import java.util.concurrent.atomic.AtomicLongFieldUpdater;
/*  14:    */ import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
/*  15:    */ import sun.misc.Unsafe;
/*  16:    */ 
/*  17:    */ final class PlatformDependent0
/*  18:    */ {
/*  19: 38 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(PlatformDependent0.class);
/*  20:    */   private static final Unsafe UNSAFE;
/*  21: 40 */   private static final boolean BIG_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
/*  22:    */   private static final long ADDRESS_FIELD_OFFSET;
/*  23:    */   private static final long UNSAFE_COPY_THRESHOLD = 1048576L;
/*  24:    */   private static final boolean UNALIGNED;
/*  25:    */   
/*  26:    */   static
/*  27:    */   {
/*  28: 57 */     ByteBuffer direct = ByteBuffer.allocateDirect(1);
/*  29:    */     Field addressField;
/*  30:    */     try
/*  31:    */     {
/*  32: 60 */       addressField = Buffer.class.getDeclaredField("address");
/*  33: 61 */       addressField.setAccessible(true);
/*  34: 62 */       if (addressField.getLong(ByteBuffer.allocate(1)) != 0L) {
/*  35: 64 */         addressField = null;
/*  36: 66 */       } else if (addressField.getLong(direct) == 0L) {
/*  37: 68 */         addressField = null;
/*  38:    */       }
/*  39:    */     }
/*  40:    */     catch (Throwable t)
/*  41:    */     {
/*  42: 73 */       addressField = null;
/*  43:    */     }
/*  44: 75 */     logger.debug("java.nio.Buffer.address: {}", addressField != null ? "available" : "unavailable");
/*  45:    */     Unsafe unsafe;
/*  46: 78 */     if (addressField != null) {
/*  47:    */       try
/*  48:    */       {
/*  49: 80 */         Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
/*  50: 81 */         unsafeField.setAccessible(true);
/*  51: 82 */         unsafe = (Unsafe)unsafeField.get(null);
/*  52: 83 */         logger.debug("sun.misc.Unsafe.theUnsafe: {}", unsafe != null ? "available" : "unavailable");
/*  53:    */         try
/*  54:    */         {
/*  55: 89 */           unsafe.getClass().getDeclaredMethod("copyMemory", new Class[] { Object.class, Long.TYPE, Object.class, Long.TYPE, Long.TYPE });
/*  56:    */           
/*  57:    */ 
/*  58:    */ 
/*  59: 93 */           logger.debug("sun.misc.Unsafe.copyMemory: available");
/*  60:    */         }
/*  61:    */         catch (NoSuchMethodError t)
/*  62:    */         {
/*  63: 95 */           logger.debug("sun.misc.Unsafe.copyMemory: unavailable");
/*  64: 96 */           throw t;
/*  65:    */         }
/*  66:    */         catch (NoSuchMethodException e)
/*  67:    */         {
/*  68: 98 */           logger.debug("sun.misc.Unsafe.copyMemory: unavailable");
/*  69: 99 */           throw e;
/*  70:    */         }
/*  71:    */       }
/*  72:    */       catch (Throwable cause)
/*  73:    */       {
/*  74:103 */         Unsafe unsafe = null;
/*  75:    */       }
/*  76:    */     } else {
/*  77:108 */       unsafe = null;
/*  78:    */     }
/*  79:111 */     UNSAFE = unsafe;
/*  80:113 */     if (unsafe == null)
/*  81:    */     {
/*  82:114 */       ADDRESS_FIELD_OFFSET = -1L;
/*  83:115 */       UNALIGNED = false;
/*  84:    */     }
/*  85:    */     else
/*  86:    */     {
/*  87:117 */       ADDRESS_FIELD_OFFSET = objectFieldOffset(addressField);
/*  88:    */       boolean unaligned;
/*  89:    */       try
/*  90:    */       {
/*  91:120 */         Class<?> bitsClass = Class.forName("java.nio.Bits", false, ClassLoader.getSystemClassLoader());
/*  92:121 */         Method unalignedMethod = bitsClass.getDeclaredMethod("unaligned", new Class[0]);
/*  93:122 */         unalignedMethod.setAccessible(true);
/*  94:123 */         unaligned = Boolean.TRUE.equals(unalignedMethod.invoke(null, new Object[0]));
/*  95:    */       }
/*  96:    */       catch (Throwable t)
/*  97:    */       {
/*  98:126 */         String arch = SystemPropertyUtil.get("os.arch", "");
/*  99:    */         
/* 100:128 */         unaligned = arch.matches("^(i[3-6]86|x86(_64)?|x64|amd64)$");
/* 101:    */       }
/* 102:131 */       UNALIGNED = unaligned;
/* 103:132 */       logger.debug("java.nio.Bits.unaligned: {}", Boolean.valueOf(UNALIGNED));
/* 104:    */     }
/* 105:    */   }
/* 106:    */   
/* 107:    */   static boolean hasUnsafe()
/* 108:    */   {
/* 109:137 */     return UNSAFE != null;
/* 110:    */   }
/* 111:    */   
/* 112:    */   static void throwException(Throwable t)
/* 113:    */   {
/* 114:141 */     UNSAFE.throwException(t);
/* 115:    */   }
/* 116:    */   
/* 117:    */   static void freeDirectBuffer(ByteBuffer buffer)
/* 118:    */   {
/* 119:147 */     Cleaner0.freeDirectBuffer(buffer);
/* 120:    */   }
/* 121:    */   
/* 122:    */   static long directBufferAddress(ByteBuffer buffer)
/* 123:    */   {
/* 124:151 */     return getLong(buffer, ADDRESS_FIELD_OFFSET);
/* 125:    */   }
/* 126:    */   
/* 127:    */   static long arrayBaseOffset()
/* 128:    */   {
/* 129:155 */     return UNSAFE.arrayBaseOffset([B.class);
/* 130:    */   }
/* 131:    */   
/* 132:    */   static Object getObject(Object object, long fieldOffset)
/* 133:    */   {
/* 134:159 */     return UNSAFE.getObject(object, fieldOffset);
/* 135:    */   }
/* 136:    */   
/* 137:    */   static Object getObjectVolatile(Object object, long fieldOffset)
/* 138:    */   {
/* 139:163 */     return UNSAFE.getObjectVolatile(object, fieldOffset);
/* 140:    */   }
/* 141:    */   
/* 142:    */   static int getInt(Object object, long fieldOffset)
/* 143:    */   {
/* 144:167 */     return UNSAFE.getInt(object, fieldOffset);
/* 145:    */   }
/* 146:    */   
/* 147:    */   private static long getLong(Object object, long fieldOffset)
/* 148:    */   {
/* 149:171 */     return UNSAFE.getLong(object, fieldOffset);
/* 150:    */   }
/* 151:    */   
/* 152:    */   static long objectFieldOffset(Field field)
/* 153:    */   {
/* 154:175 */     return UNSAFE.objectFieldOffset(field);
/* 155:    */   }
/* 156:    */   
/* 157:    */   static byte getByte(long address)
/* 158:    */   {
/* 159:179 */     return UNSAFE.getByte(address);
/* 160:    */   }
/* 161:    */   
/* 162:    */   static short getShort(long address)
/* 163:    */   {
/* 164:183 */     if (UNALIGNED) {
/* 165:184 */       return UNSAFE.getShort(address);
/* 166:    */     }
/* 167:185 */     if (BIG_ENDIAN) {
/* 168:186 */       return (short)(getByte(address) << 8 | getByte(address + 1L) & 0xFF);
/* 169:    */     }
/* 170:188 */     return (short)(getByte(address + 1L) << 8 | getByte(address) & 0xFF);
/* 171:    */   }
/* 172:    */   
/* 173:    */   static int getInt(long address)
/* 174:    */   {
/* 175:193 */     if (UNALIGNED) {
/* 176:194 */       return UNSAFE.getInt(address);
/* 177:    */     }
/* 178:195 */     if (BIG_ENDIAN) {
/* 179:196 */       return getByte(address) << 24 | (getByte(address + 1L) & 0xFF) << 16 | (getByte(address + 2L) & 0xFF) << 8 | getByte(address + 3L) & 0xFF;
/* 180:    */     }
/* 181:201 */     return getByte(address + 3L) << 24 | (getByte(address + 2L) & 0xFF) << 16 | (getByte(address + 1L) & 0xFF) << 8 | getByte(address) & 0xFF;
/* 182:    */   }
/* 183:    */   
/* 184:    */   static long getLong(long address)
/* 185:    */   {
/* 186:209 */     if (UNALIGNED) {
/* 187:210 */       return UNSAFE.getLong(address);
/* 188:    */     }
/* 189:211 */     if (BIG_ENDIAN) {
/* 190:212 */       return getByte(address) << 56 | (getByte(address + 1L) & 0xFF) << 48 | (getByte(address + 2L) & 0xFF) << 40 | (getByte(address + 3L) & 0xFF) << 32 | (getByte(address + 4L) & 0xFF) << 24 | (getByte(address + 5L) & 0xFF) << 16 | (getByte(address + 6L) & 0xFF) << 8 | getByte(address + 7L) & 0xFF;
/* 191:    */     }
/* 192:221 */     return getByte(address + 7L) << 56 | (getByte(address + 6L) & 0xFF) << 48 | (getByte(address + 5L) & 0xFF) << 40 | (getByte(address + 4L) & 0xFF) << 32 | (getByte(address + 3L) & 0xFF) << 24 | (getByte(address + 2L) & 0xFF) << 16 | (getByte(address + 1L) & 0xFF) << 8 | getByte(address) & 0xFF;
/* 193:    */   }
/* 194:    */   
/* 195:    */   static void putOrderedObject(Object object, long address, Object value)
/* 196:    */   {
/* 197:233 */     UNSAFE.putOrderedObject(object, address, value);
/* 198:    */   }
/* 199:    */   
/* 200:    */   static void putByte(long address, byte value)
/* 201:    */   {
/* 202:237 */     UNSAFE.putByte(address, value);
/* 203:    */   }
/* 204:    */   
/* 205:    */   static void putShort(long address, short value)
/* 206:    */   {
/* 207:241 */     if (UNALIGNED)
/* 208:    */     {
/* 209:242 */       UNSAFE.putShort(address, value);
/* 210:    */     }
/* 211:243 */     else if (BIG_ENDIAN)
/* 212:    */     {
/* 213:244 */       putByte(address, (byte)(value >>> 8));
/* 214:245 */       putByte(address + 1L, (byte)value);
/* 215:    */     }
/* 216:    */     else
/* 217:    */     {
/* 218:247 */       putByte(address + 1L, (byte)(value >>> 8));
/* 219:248 */       putByte(address, (byte)value);
/* 220:    */     }
/* 221:    */   }
/* 222:    */   
/* 223:    */   static void putInt(long address, int value)
/* 224:    */   {
/* 225:253 */     if (UNALIGNED)
/* 226:    */     {
/* 227:254 */       UNSAFE.putInt(address, value);
/* 228:    */     }
/* 229:255 */     else if (BIG_ENDIAN)
/* 230:    */     {
/* 231:256 */       putByte(address, (byte)(value >>> 24));
/* 232:257 */       putByte(address + 1L, (byte)(value >>> 16));
/* 233:258 */       putByte(address + 2L, (byte)(value >>> 8));
/* 234:259 */       putByte(address + 3L, (byte)value);
/* 235:    */     }
/* 236:    */     else
/* 237:    */     {
/* 238:261 */       putByte(address + 3L, (byte)(value >>> 24));
/* 239:262 */       putByte(address + 2L, (byte)(value >>> 16));
/* 240:263 */       putByte(address + 1L, (byte)(value >>> 8));
/* 241:264 */       putByte(address, (byte)value);
/* 242:    */     }
/* 243:    */   }
/* 244:    */   
/* 245:    */   static void putLong(long address, long value)
/* 246:    */   {
/* 247:269 */     if (UNALIGNED)
/* 248:    */     {
/* 249:270 */       UNSAFE.putLong(address, value);
/* 250:    */     }
/* 251:271 */     else if (BIG_ENDIAN)
/* 252:    */     {
/* 253:272 */       putByte(address, (byte)(int)(value >>> 56));
/* 254:273 */       putByte(address + 1L, (byte)(int)(value >>> 48));
/* 255:274 */       putByte(address + 2L, (byte)(int)(value >>> 40));
/* 256:275 */       putByte(address + 3L, (byte)(int)(value >>> 32));
/* 257:276 */       putByte(address + 4L, (byte)(int)(value >>> 24));
/* 258:277 */       putByte(address + 5L, (byte)(int)(value >>> 16));
/* 259:278 */       putByte(address + 6L, (byte)(int)(value >>> 8));
/* 260:279 */       putByte(address + 7L, (byte)(int)value);
/* 261:    */     }
/* 262:    */     else
/* 263:    */     {
/* 264:281 */       putByte(address + 7L, (byte)(int)(value >>> 56));
/* 265:282 */       putByte(address + 6L, (byte)(int)(value >>> 48));
/* 266:283 */       putByte(address + 5L, (byte)(int)(value >>> 40));
/* 267:284 */       putByte(address + 4L, (byte)(int)(value >>> 32));
/* 268:285 */       putByte(address + 3L, (byte)(int)(value >>> 24));
/* 269:286 */       putByte(address + 2L, (byte)(int)(value >>> 16));
/* 270:287 */       putByte(address + 1L, (byte)(int)(value >>> 8));
/* 271:288 */       putByte(address, (byte)(int)value);
/* 272:    */     }
/* 273:    */   }
/* 274:    */   
/* 275:    */   static void copyMemory(long srcAddr, long dstAddr, long length)
/* 276:    */   {
/* 277:294 */     while (length > 0L)
/* 278:    */     {
/* 279:295 */       long size = Math.min(length, 1048576L);
/* 280:296 */       UNSAFE.copyMemory(srcAddr, dstAddr, size);
/* 281:297 */       length -= size;
/* 282:298 */       srcAddr += size;
/* 283:299 */       dstAddr += size;
/* 284:    */     }
/* 285:    */   }
/* 286:    */   
/* 287:    */   static void copyMemory(Object src, long srcOffset, Object dst, long dstOffset, long length)
/* 288:    */   {
/* 289:305 */     while (length > 0L)
/* 290:    */     {
/* 291:306 */       long size = Math.min(length, 1048576L);
/* 292:307 */       UNSAFE.copyMemory(src, srcOffset, dst, dstOffset, size);
/* 293:308 */       length -= size;
/* 294:309 */       srcOffset += size;
/* 295:310 */       dstOffset += size;
/* 296:    */     }
/* 297:    */   }
/* 298:    */   
/* 299:    */   static <U, W> AtomicReferenceFieldUpdater<U, W> newAtomicReferenceFieldUpdater(Class<U> tclass, String fieldName)
/* 300:    */     throws Exception
/* 301:    */   {
/* 302:316 */     return new UnsafeAtomicReferenceFieldUpdater(UNSAFE, tclass, fieldName);
/* 303:    */   }
/* 304:    */   
/* 305:    */   static <T> AtomicIntegerFieldUpdater<T> newAtomicIntegerFieldUpdater(Class<?> tclass, String fieldName)
/* 306:    */     throws Exception
/* 307:    */   {
/* 308:321 */     return new UnsafeAtomicIntegerFieldUpdater(UNSAFE, tclass, fieldName);
/* 309:    */   }
/* 310:    */   
/* 311:    */   static <T> AtomicLongFieldUpdater<T> newAtomicLongFieldUpdater(Class<?> tclass, String fieldName)
/* 312:    */     throws Exception
/* 313:    */   {
/* 314:326 */     return new UnsafeAtomicLongFieldUpdater(UNSAFE, tclass, fieldName);
/* 315:    */   }
/* 316:    */   
/* 317:    */   static ClassLoader getClassLoader(Class<?> clazz)
/* 318:    */   {
/* 319:330 */     if (System.getSecurityManager() == null) {
/* 320:331 */       return clazz.getClassLoader();
/* 321:    */     }
/* 322:333 */     (ClassLoader)AccessController.doPrivileged(new PrivilegedAction()
/* 323:    */     {
/* 324:    */       public ClassLoader run()
/* 325:    */       {
/* 326:336 */         return this.val$clazz.getClassLoader();
/* 327:    */       }
/* 328:    */     });
/* 329:    */   }
/* 330:    */   
/* 331:    */   static ClassLoader getContextClassLoader()
/* 332:    */   {
/* 333:343 */     if (System.getSecurityManager() == null) {
/* 334:344 */       return Thread.currentThread().getContextClassLoader();
/* 335:    */     }
/* 336:346 */     (ClassLoader)AccessController.doPrivileged(new PrivilegedAction()
/* 337:    */     {
/* 338:    */       public ClassLoader run()
/* 339:    */       {
/* 340:349 */         return Thread.currentThread().getContextClassLoader();
/* 341:    */       }
/* 342:    */     });
/* 343:    */   }
/* 344:    */   
/* 345:    */   static ClassLoader getSystemClassLoader()
/* 346:    */   {
/* 347:356 */     if (System.getSecurityManager() == null) {
/* 348:357 */       return ClassLoader.getSystemClassLoader();
/* 349:    */     }
/* 350:359 */     (ClassLoader)AccessController.doPrivileged(new PrivilegedAction()
/* 351:    */     {
/* 352:    */       public ClassLoader run()
/* 353:    */       {
/* 354:362 */         return ClassLoader.getSystemClassLoader();
/* 355:    */       }
/* 356:    */     });
/* 357:    */   }
/* 358:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.PlatformDependent0
 * JD-Core Version:    0.7.0.1
 */