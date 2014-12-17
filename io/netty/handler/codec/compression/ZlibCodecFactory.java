/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.PlatformDependent;
/*   4:    */ import io.netty.util.internal.SystemPropertyUtil;
/*   5:    */ import io.netty.util.internal.logging.InternalLogger;
/*   6:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   7:    */ 
/*   8:    */ public final class ZlibCodecFactory
/*   9:    */ {
/*  10: 27 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(ZlibCodecFactory.class);
/*  11: 32 */   private static final boolean noJdkZlibDecoder = SystemPropertyUtil.getBoolean("io.netty.noJdkZlibDecoder", true);
/*  12:    */   
/*  13:    */   static
/*  14:    */   {
/*  15: 33 */     logger.debug("-Dio.netty.noJdkZlibDecoder: {}", Boolean.valueOf(noJdkZlibDecoder));
/*  16:    */   }
/*  17:    */   
/*  18:    */   public static ZlibEncoder newZlibEncoder(int compressionLevel)
/*  19:    */   {
/*  20: 37 */     if (PlatformDependent.javaVersion() < 7) {
/*  21: 38 */       return new JZlibEncoder(compressionLevel);
/*  22:    */     }
/*  23: 40 */     return new JdkZlibEncoder(compressionLevel);
/*  24:    */   }
/*  25:    */   
/*  26:    */   public static ZlibEncoder newZlibEncoder(ZlibWrapper wrapper)
/*  27:    */   {
/*  28: 45 */     if (PlatformDependent.javaVersion() < 7) {
/*  29: 46 */       return new JZlibEncoder(wrapper);
/*  30:    */     }
/*  31: 48 */     return new JdkZlibEncoder(wrapper);
/*  32:    */   }
/*  33:    */   
/*  34:    */   public static ZlibEncoder newZlibEncoder(ZlibWrapper wrapper, int compressionLevel)
/*  35:    */   {
/*  36: 53 */     if (PlatformDependent.javaVersion() < 7) {
/*  37: 54 */       return new JZlibEncoder(wrapper, compressionLevel);
/*  38:    */     }
/*  39: 56 */     return new JdkZlibEncoder(wrapper, compressionLevel);
/*  40:    */   }
/*  41:    */   
/*  42:    */   public static ZlibEncoder newZlibEncoder(ZlibWrapper wrapper, int compressionLevel, int windowBits, int memLevel)
/*  43:    */   {
/*  44: 61 */     if (PlatformDependent.javaVersion() < 7) {
/*  45: 62 */       return new JZlibEncoder(wrapper, compressionLevel, windowBits, memLevel);
/*  46:    */     }
/*  47: 64 */     return new JdkZlibEncoder(wrapper, compressionLevel);
/*  48:    */   }
/*  49:    */   
/*  50:    */   public static ZlibEncoder newZlibEncoder(byte[] dictionary)
/*  51:    */   {
/*  52: 69 */     if (PlatformDependent.javaVersion() < 7) {
/*  53: 70 */       return new JZlibEncoder(dictionary);
/*  54:    */     }
/*  55: 72 */     return new JdkZlibEncoder(dictionary);
/*  56:    */   }
/*  57:    */   
/*  58:    */   public static ZlibEncoder newZlibEncoder(int compressionLevel, byte[] dictionary)
/*  59:    */   {
/*  60: 77 */     if (PlatformDependent.javaVersion() < 7) {
/*  61: 78 */       return new JZlibEncoder(compressionLevel, dictionary);
/*  62:    */     }
/*  63: 80 */     return new JdkZlibEncoder(compressionLevel, dictionary);
/*  64:    */   }
/*  65:    */   
/*  66:    */   public static ZlibEncoder newZlibEncoder(int compressionLevel, int windowBits, int memLevel, byte[] dictionary)
/*  67:    */   {
/*  68: 85 */     if (PlatformDependent.javaVersion() < 7) {
/*  69: 86 */       return new JZlibEncoder(compressionLevel, windowBits, memLevel, dictionary);
/*  70:    */     }
/*  71: 88 */     return new JdkZlibEncoder(compressionLevel, dictionary);
/*  72:    */   }
/*  73:    */   
/*  74:    */   public static ZlibDecoder newZlibDecoder()
/*  75:    */   {
/*  76: 93 */     if ((PlatformDependent.javaVersion() < 7) || (noJdkZlibDecoder)) {
/*  77: 94 */       return new JZlibDecoder();
/*  78:    */     }
/*  79: 96 */     return new JdkZlibDecoder();
/*  80:    */   }
/*  81:    */   
/*  82:    */   public static ZlibDecoder newZlibDecoder(ZlibWrapper wrapper)
/*  83:    */   {
/*  84:101 */     if ((PlatformDependent.javaVersion() < 7) || (noJdkZlibDecoder)) {
/*  85:102 */       return new JZlibDecoder(wrapper);
/*  86:    */     }
/*  87:104 */     return new JdkZlibDecoder(wrapper);
/*  88:    */   }
/*  89:    */   
/*  90:    */   public static ZlibDecoder newZlibDecoder(byte[] dictionary)
/*  91:    */   {
/*  92:109 */     if ((PlatformDependent.javaVersion() < 7) || (noJdkZlibDecoder)) {
/*  93:110 */       return new JZlibDecoder(dictionary);
/*  94:    */     }
/*  95:112 */     return new JdkZlibDecoder(dictionary);
/*  96:    */   }
/*  97:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.compression.ZlibCodecFactory
 * JD-Core Version:    0.7.0.1
 */