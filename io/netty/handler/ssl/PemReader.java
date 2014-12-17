/*   1:    */ package io.netty.handler.ssl;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.Unpooled;
/*   5:    */ import io.netty.handler.codec.base64.Base64;
/*   6:    */ import io.netty.util.CharsetUtil;
/*   7:    */ import io.netty.util.internal.logging.InternalLogger;
/*   8:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   9:    */ import java.io.ByteArrayOutputStream;
/*  10:    */ import java.io.File;
/*  11:    */ import java.io.FileInputStream;
/*  12:    */ import java.io.IOException;
/*  13:    */ import java.io.InputStream;
/*  14:    */ import java.io.OutputStream;
/*  15:    */ import java.nio.charset.Charset;
/*  16:    */ import java.security.KeyException;
/*  17:    */ import java.security.cert.CertificateException;
/*  18:    */ import java.util.ArrayList;
/*  19:    */ import java.util.List;
/*  20:    */ import java.util.regex.Matcher;
/*  21:    */ import java.util.regex.Pattern;
/*  22:    */ 
/*  23:    */ final class PemReader
/*  24:    */ {
/*  25: 45 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(PemReader.class);
/*  26: 47 */   private static final Pattern CERT_PATTERN = Pattern.compile("-+BEGIN\\s+.*CERTIFICATE[^-]*-+(?:\\s|\\r|\\n)+([a-z0-9+/=\\r\\n]+)-+END\\s+.*CERTIFICATE[^-]*-+", 2);
/*  27: 52 */   private static final Pattern KEY_PATTERN = Pattern.compile("-+BEGIN\\s+.*PRIVATE\\s+KEY[^-]*-+(?:\\s|\\r|\\n)+([a-z0-9+/=\\r\\n]+)-+END\\s+.*PRIVATE\\s+KEY[^-]*-+", 2);
/*  28:    */   
/*  29:    */   static ByteBuf[] readCertificates(File file)
/*  30:    */     throws CertificateException
/*  31:    */   {
/*  32:    */     String content;
/*  33:    */     try
/*  34:    */     {
/*  35: 61 */       content = readContent(file);
/*  36:    */     }
/*  37:    */     catch (IOException e)
/*  38:    */     {
/*  39: 63 */       throw new CertificateException("failed to read a file: " + file, e);
/*  40:    */     }
/*  41: 66 */     List<ByteBuf> certs = new ArrayList();
/*  42: 67 */     Matcher m = CERT_PATTERN.matcher(content);
/*  43: 68 */     int start = 0;
/*  44: 70 */     while (m.find(start))
/*  45:    */     {
/*  46: 74 */       ByteBuf base64 = Unpooled.copiedBuffer(m.group(1), CharsetUtil.US_ASCII);
/*  47: 75 */       ByteBuf der = Base64.decode(base64);
/*  48: 76 */       base64.release();
/*  49: 77 */       certs.add(der);
/*  50:    */       
/*  51: 79 */       start = m.end();
/*  52:    */     }
/*  53: 82 */     if (certs.isEmpty()) {
/*  54: 83 */       throw new CertificateException("found no certificates: " + file);
/*  55:    */     }
/*  56: 86 */     return (ByteBuf[])certs.toArray(new ByteBuf[certs.size()]);
/*  57:    */   }
/*  58:    */   
/*  59:    */   static ByteBuf readPrivateKey(File file)
/*  60:    */     throws KeyException
/*  61:    */   {
/*  62:    */     String content;
/*  63:    */     try
/*  64:    */     {
/*  65: 92 */       content = readContent(file);
/*  66:    */     }
/*  67:    */     catch (IOException e)
/*  68:    */     {
/*  69: 94 */       throw new KeyException("failed to read a file: " + file, e);
/*  70:    */     }
/*  71: 97 */     Matcher m = KEY_PATTERN.matcher(content);
/*  72: 98 */     if (!m.find()) {
/*  73: 99 */       throw new KeyException("found no private key: " + file);
/*  74:    */     }
/*  75:102 */     ByteBuf base64 = Unpooled.copiedBuffer(m.group(1), CharsetUtil.US_ASCII);
/*  76:103 */     ByteBuf der = Base64.decode(base64);
/*  77:104 */     base64.release();
/*  78:105 */     return der;
/*  79:    */   }
/*  80:    */   
/*  81:    */   private static String readContent(File file)
/*  82:    */     throws IOException
/*  83:    */   {
/*  84:109 */     InputStream in = new FileInputStream(file);
/*  85:110 */     ByteArrayOutputStream out = new ByteArrayOutputStream();
/*  86:    */     try
/*  87:    */     {
/*  88:112 */       byte[] buf = new byte[8192];
/*  89:    */       int ret;
/*  90:    */       for (;;)
/*  91:    */       {
/*  92:114 */         ret = in.read(buf);
/*  93:115 */         if (ret < 0) {
/*  94:    */           break;
/*  95:    */         }
/*  96:118 */         out.write(buf, 0, ret);
/*  97:    */       }
/*  98:120 */       return out.toString(CharsetUtil.US_ASCII.name());
/*  99:    */     }
/* 100:    */     finally
/* 101:    */     {
/* 102:122 */       safeClose(in);
/* 103:123 */       safeClose(out);
/* 104:    */     }
/* 105:    */   }
/* 106:    */   
/* 107:    */   private static void safeClose(InputStream in)
/* 108:    */   {
/* 109:    */     try
/* 110:    */     {
/* 111:129 */       in.close();
/* 112:    */     }
/* 113:    */     catch (IOException e)
/* 114:    */     {
/* 115:131 */       logger.warn("Failed to close a stream.", e);
/* 116:    */     }
/* 117:    */   }
/* 118:    */   
/* 119:    */   private static void safeClose(OutputStream out)
/* 120:    */   {
/* 121:    */     try
/* 122:    */     {
/* 123:137 */       out.close();
/* 124:    */     }
/* 125:    */     catch (IOException e)
/* 126:    */     {
/* 127:139 */       logger.warn("Failed to close a stream.", e);
/* 128:    */     }
/* 129:    */   }
/* 130:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.ssl.PemReader
 * JD-Core Version:    0.7.0.1
 */