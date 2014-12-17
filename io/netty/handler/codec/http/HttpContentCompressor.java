/*   1:    */ package io.netty.handler.codec.http;
/*   2:    */ 
/*   3:    */ import io.netty.channel.ChannelHandler;
/*   4:    */ import io.netty.channel.embedded.EmbeddedChannel;
/*   5:    */ import io.netty.handler.codec.compression.ZlibCodecFactory;
/*   6:    */ import io.netty.handler.codec.compression.ZlibWrapper;
/*   7:    */ import io.netty.util.internal.StringUtil;
/*   8:    */ 
/*   9:    */ public class HttpContentCompressor
/*  10:    */   extends HttpContentEncoder
/*  11:    */ {
/*  12:    */   private final int compressionLevel;
/*  13:    */   private final int windowBits;
/*  14:    */   private final int memLevel;
/*  15:    */   
/*  16:    */   public HttpContentCompressor()
/*  17:    */   {
/*  18: 41 */     this(6);
/*  19:    */   }
/*  20:    */   
/*  21:    */   public HttpContentCompressor(int compressionLevel)
/*  22:    */   {
/*  23: 54 */     this(compressionLevel, 15, 8);
/*  24:    */   }
/*  25:    */   
/*  26:    */   public HttpContentCompressor(int compressionLevel, int windowBits, int memLevel)
/*  27:    */   {
/*  28: 77 */     if ((compressionLevel < 0) || (compressionLevel > 9)) {
/*  29: 78 */       throw new IllegalArgumentException("compressionLevel: " + compressionLevel + " (expected: 0-9)");
/*  30:    */     }
/*  31: 82 */     if ((windowBits < 9) || (windowBits > 15)) {
/*  32: 83 */       throw new IllegalArgumentException("windowBits: " + windowBits + " (expected: 9-15)");
/*  33:    */     }
/*  34: 86 */     if ((memLevel < 1) || (memLevel > 9)) {
/*  35: 87 */       throw new IllegalArgumentException("memLevel: " + memLevel + " (expected: 1-9)");
/*  36:    */     }
/*  37: 90 */     this.compressionLevel = compressionLevel;
/*  38: 91 */     this.windowBits = windowBits;
/*  39: 92 */     this.memLevel = memLevel;
/*  40:    */   }
/*  41:    */   
/*  42:    */   protected HttpContentEncoder.Result beginEncode(HttpResponse headers, String acceptEncoding)
/*  43:    */     throws Exception
/*  44:    */   {
/*  45: 97 */     String contentEncoding = headers.headers().get("Content-Encoding");
/*  46: 98 */     if ((contentEncoding != null) && (!"identity".equalsIgnoreCase(contentEncoding))) {
/*  47:100 */       return null;
/*  48:    */     }
/*  49:103 */     ZlibWrapper wrapper = determineWrapper(acceptEncoding);
/*  50:104 */     if (wrapper == null) {
/*  51:105 */       return null;
/*  52:    */     }
/*  53:    */     String targetContentEncoding;
/*  54:109 */     switch (1.$SwitchMap$io$netty$handler$codec$compression$ZlibWrapper[wrapper.ordinal()])
/*  55:    */     {
/*  56:    */     case 1: 
/*  57:111 */       targetContentEncoding = "gzip";
/*  58:112 */       break;
/*  59:    */     case 2: 
/*  60:114 */       targetContentEncoding = "deflate";
/*  61:115 */       break;
/*  62:    */     default: 
/*  63:117 */       throw new Error();
/*  64:    */     }
/*  65:120 */     return new HttpContentEncoder.Result(targetContentEncoding, new EmbeddedChannel(new ChannelHandler[] { ZlibCodecFactory.newZlibEncoder(wrapper, this.compressionLevel, this.windowBits, this.memLevel) }));
/*  66:    */   }
/*  67:    */   
/*  68:    */   protected ZlibWrapper determineWrapper(String acceptEncoding)
/*  69:    */   {
/*  70:127 */     float starQ = -1.0F;
/*  71:128 */     float gzipQ = -1.0F;
/*  72:129 */     float deflateQ = -1.0F;
/*  73:130 */     for (String encoding : StringUtil.split(acceptEncoding, ','))
/*  74:    */     {
/*  75:131 */       float q = 1.0F;
/*  76:132 */       int equalsPos = encoding.indexOf('=');
/*  77:133 */       if (equalsPos != -1) {
/*  78:    */         try
/*  79:    */         {
/*  80:135 */           q = Float.valueOf(encoding.substring(equalsPos + 1)).floatValue();
/*  81:    */         }
/*  82:    */         catch (NumberFormatException e)
/*  83:    */         {
/*  84:138 */           q = 0.0F;
/*  85:    */         }
/*  86:    */       }
/*  87:141 */       if (encoding.contains("*")) {
/*  88:142 */         starQ = q;
/*  89:143 */       } else if ((encoding.contains("gzip")) && (q > gzipQ)) {
/*  90:144 */         gzipQ = q;
/*  91:145 */       } else if ((encoding.contains("deflate")) && (q > deflateQ)) {
/*  92:146 */         deflateQ = q;
/*  93:    */       }
/*  94:    */     }
/*  95:149 */     if ((gzipQ > 0.0F) || (deflateQ > 0.0F))
/*  96:    */     {
/*  97:150 */       if (gzipQ >= deflateQ) {
/*  98:151 */         return ZlibWrapper.GZIP;
/*  99:    */       }
/* 100:153 */       return ZlibWrapper.ZLIB;
/* 101:    */     }
/* 102:156 */     if (starQ > 0.0F)
/* 103:    */     {
/* 104:157 */       if (gzipQ == -1.0F) {
/* 105:158 */         return ZlibWrapper.GZIP;
/* 106:    */       }
/* 107:160 */       if (deflateQ == -1.0F) {
/* 108:161 */         return ZlibWrapper.ZLIB;
/* 109:    */       }
/* 110:    */     }
/* 111:164 */     return null;
/* 112:    */   }
/* 113:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.HttpContentCompressor
 * JD-Core Version:    0.7.0.1
 */