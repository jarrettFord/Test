/*  1:   */ package io.netty.handler.codec.compression;
/*  2:   */ 
/*  3:   */ import com.jcraft.jzlib.Deflater;
/*  4:   */ import com.jcraft.jzlib.Inflater;
/*  5:   */ import com.jcraft.jzlib.JZlib;
/*  6:   */ import com.jcraft.jzlib.JZlib.WrapperType;
/*  7:   */ 
/*  8:   */ final class ZlibUtil
/*  9:   */ {
/* 10:   */   static void fail(Inflater z, String message, int resultCode)
/* 11:   */   {
/* 12:28 */     throw inflaterException(z, message, resultCode);
/* 13:   */   }
/* 14:   */   
/* 15:   */   static void fail(Deflater z, String message, int resultCode)
/* 16:   */   {
/* 17:32 */     throw deflaterException(z, message, resultCode);
/* 18:   */   }
/* 19:   */   
/* 20:   */   static CompressionException inflaterException(Inflater z, String message, int resultCode)
/* 21:   */   {
/* 22:36 */     return new CompressionException(message + " (" + resultCode + ')' + (z.msg != null ? ": " + z.msg : ""));
/* 23:   */   }
/* 24:   */   
/* 25:   */   static CompressionException deflaterException(Deflater z, String message, int resultCode)
/* 26:   */   {
/* 27:40 */     return new CompressionException(message + " (" + resultCode + ')' + (z.msg != null ? ": " + z.msg : ""));
/* 28:   */   }
/* 29:   */   
/* 30:   */   static JZlib.WrapperType convertWrapperType(ZlibWrapper wrapper)
/* 31:   */   {
/* 32:   */     JZlib.WrapperType convertedWrapperType;
/* 33:45 */     switch (1.$SwitchMap$io$netty$handler$codec$compression$ZlibWrapper[wrapper.ordinal()])
/* 34:   */     {
/* 35:   */     case 1: 
/* 36:47 */       convertedWrapperType = JZlib.W_NONE;
/* 37:48 */       break;
/* 38:   */     case 2: 
/* 39:50 */       convertedWrapperType = JZlib.W_ZLIB;
/* 40:51 */       break;
/* 41:   */     case 3: 
/* 42:53 */       convertedWrapperType = JZlib.W_GZIP;
/* 43:54 */       break;
/* 44:   */     case 4: 
/* 45:56 */       convertedWrapperType = JZlib.W_ANY;
/* 46:57 */       break;
/* 47:   */     default: 
/* 48:59 */       throw new Error();
/* 49:   */     }
/* 50:61 */     return convertedWrapperType;
/* 51:   */   }
/* 52:   */   
/* 53:   */   static int wrapperOverhead(ZlibWrapper wrapper)
/* 54:   */   {
/* 55:   */     int overhead;
/* 56:66 */     switch (1.$SwitchMap$io$netty$handler$codec$compression$ZlibWrapper[wrapper.ordinal()])
/* 57:   */     {
/* 58:   */     case 1: 
/* 59:68 */       overhead = 0;
/* 60:69 */       break;
/* 61:   */     case 2: 
/* 62:   */     case 4: 
/* 63:72 */       overhead = 2;
/* 64:73 */       break;
/* 65:   */     case 3: 
/* 66:75 */       overhead = 10;
/* 67:76 */       break;
/* 68:   */     default: 
/* 69:78 */       throw new Error();
/* 70:   */     }
/* 71:80 */     return overhead;
/* 72:   */   }
/* 73:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.compression.ZlibUtil
 * JD-Core Version:    0.7.0.1
 */