/*  1:   */ package org.spacehq.mc.auth.util;
/*  2:   */ 
/*  3:   */ import java.io.ByteArrayOutputStream;
/*  4:   */ import java.io.Closeable;
/*  5:   */ import java.io.IOException;
/*  6:   */ import java.io.InputStream;
/*  7:   */ import java.io.InputStreamReader;
/*  8:   */ import java.io.StringWriter;
/*  9:   */ 
/* 10:   */ public class IOUtils
/* 11:   */ {
/* 12:   */   private static final int DEFAULT_BUFFER_SIZE = 4096;
/* 13:   */   
/* 14:   */   public static void closeQuietly(Closeable close)
/* 15:   */   {
/* 16:   */     try
/* 17:   */     {
/* 18:16 */       if (close != null) {
/* 19:17 */         close.close();
/* 20:   */       }
/* 21:   */     }
/* 22:   */     catch (IOException localIOException) {}
/* 23:   */   }
/* 24:   */   
/* 25:   */   public static String toString(InputStream input, String encoding)
/* 26:   */     throws IOException
/* 27:   */   {
/* 28:24 */     StringWriter writer = new StringWriter();
/* 29:25 */     InputStreamReader in = encoding != null ? new InputStreamReader(input, encoding) : new InputStreamReader(input);
/* 30:26 */     char[] buffer = new char[4096];
/* 31:27 */     int n = 0;
/* 32:28 */     while (-1 != (n = in.read(buffer))) {
/* 33:29 */       writer.write(buffer, 0, n);
/* 34:   */     }
/* 35:32 */     in.close();
/* 36:33 */     return writer.toString();
/* 37:   */   }
/* 38:   */   
/* 39:   */   public static byte[] toByteArray(InputStream in)
/* 40:   */     throws IOException
/* 41:   */   {
/* 42:37 */     ByteArrayOutputStream out = new ByteArrayOutputStream();
/* 43:38 */     byte[] buffer = new byte[4096];
/* 44:39 */     int n = 0;
/* 45:40 */     while (-1 != (n = in.read(buffer))) {
/* 46:41 */       out.write(buffer, 0, n);
/* 47:   */     }
/* 48:44 */     in.close();
/* 49:45 */     out.close();
/* 50:46 */     return out.toByteArray();
/* 51:   */   }
/* 52:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.auth.util.IOUtils
 * JD-Core Version:    0.7.0.1
 */