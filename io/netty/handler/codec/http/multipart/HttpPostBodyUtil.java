/*   1:    */ package io.netty.handler.codec.http.multipart;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.util.CharsetUtil;
/*   5:    */ import java.nio.charset.Charset;
/*   6:    */ 
/*   7:    */ final class HttpPostBodyUtil
/*   8:    */ {
/*   9:    */   public static final int chunkSize = 8096;
/*  10:    */   public static final String CONTENT_DISPOSITION = "Content-Disposition";
/*  11:    */   public static final String NAME = "name";
/*  12:    */   public static final String FILENAME = "filename";
/*  13:    */   public static final String FORM_DATA = "form-data";
/*  14:    */   public static final String ATTACHMENT = "attachment";
/*  15:    */   public static final String FILE = "file";
/*  16:    */   public static final String MULTIPART_MIXED = "multipart/mixed";
/*  17: 61 */   public static final Charset ISO_8859_1 = CharsetUtil.ISO_8859_1;
/*  18: 66 */   public static final Charset US_ASCII = CharsetUtil.US_ASCII;
/*  19:    */   public static final String DEFAULT_BINARY_CONTENT_TYPE = "application/octet-stream";
/*  20:    */   public static final String DEFAULT_TEXT_CONTENT_TYPE = "text/plain";
/*  21:    */   
/*  22:    */   public static enum TransferEncodingMechanism
/*  23:    */   {
/*  24: 90 */     BIT7("7bit"),  BIT8("8bit"),  BINARY("binary");
/*  25:    */     
/*  26:    */     private final String value;
/*  27:    */     
/*  28:    */     private TransferEncodingMechanism(String value)
/*  29:    */     {
/*  30:103 */       this.value = value;
/*  31:    */     }
/*  32:    */     
/*  33:    */     private TransferEncodingMechanism()
/*  34:    */     {
/*  35:107 */       this.value = name();
/*  36:    */     }
/*  37:    */     
/*  38:    */     public String value()
/*  39:    */     {
/*  40:111 */       return this.value;
/*  41:    */     }
/*  42:    */     
/*  43:    */     public String toString()
/*  44:    */     {
/*  45:116 */       return this.value;
/*  46:    */     }
/*  47:    */   }
/*  48:    */   
/*  49:    */   static class SeekAheadNoBackArrayException
/*  50:    */     extends Exception
/*  51:    */   {
/*  52:    */     private static final long serialVersionUID = -630418804938699495L;
/*  53:    */   }
/*  54:    */   
/*  55:    */   static class SeekAheadOptimize
/*  56:    */   {
/*  57:    */     byte[] bytes;
/*  58:    */     int readerIndex;
/*  59:    */     int pos;
/*  60:    */     int origPos;
/*  61:    */     int limit;
/*  62:    */     ByteBuf buffer;
/*  63:    */     
/*  64:    */     SeekAheadOptimize(ByteBuf buffer)
/*  65:    */       throws HttpPostBodyUtil.SeekAheadNoBackArrayException
/*  66:    */     {
/*  67:143 */       if (!buffer.hasArray()) {
/*  68:144 */         throw new HttpPostBodyUtil.SeekAheadNoBackArrayException();
/*  69:    */       }
/*  70:146 */       this.buffer = buffer;
/*  71:147 */       this.bytes = buffer.array();
/*  72:148 */       this.readerIndex = buffer.readerIndex();
/*  73:149 */       this.origPos = (this.pos = buffer.arrayOffset() + this.readerIndex);
/*  74:150 */       this.limit = (buffer.arrayOffset() + buffer.writerIndex());
/*  75:    */     }
/*  76:    */     
/*  77:    */     void setReadPosition(int minus)
/*  78:    */     {
/*  79:159 */       this.pos -= minus;
/*  80:160 */       this.readerIndex = getReadPosition(this.pos);
/*  81:161 */       this.buffer.readerIndex(this.readerIndex);
/*  82:    */     }
/*  83:    */     
/*  84:    */     int getReadPosition(int index)
/*  85:    */     {
/*  86:170 */       return index - this.origPos + this.readerIndex;
/*  87:    */     }
/*  88:    */     
/*  89:    */     void clear()
/*  90:    */     {
/*  91:174 */       this.buffer = null;
/*  92:175 */       this.bytes = null;
/*  93:176 */       this.limit = 0;
/*  94:177 */       this.pos = 0;
/*  95:178 */       this.readerIndex = 0;
/*  96:    */     }
/*  97:    */   }
/*  98:    */   
/*  99:    */   static int findNonWhitespace(String sb, int offset)
/* 100:    */   {
/* 101:188 */     for (int result = offset; result < sb.length(); result++) {
/* 102:189 */       if (!Character.isWhitespace(sb.charAt(result))) {
/* 103:    */         break;
/* 104:    */       }
/* 105:    */     }
/* 106:193 */     return result;
/* 107:    */   }
/* 108:    */   
/* 109:    */   static int findWhitespace(String sb, int offset)
/* 110:    */   {
/* 111:202 */     for (int result = offset; result < sb.length(); result++) {
/* 112:203 */       if (Character.isWhitespace(sb.charAt(result))) {
/* 113:    */         break;
/* 114:    */       }
/* 115:    */     }
/* 116:207 */     return result;
/* 117:    */   }
/* 118:    */   
/* 119:    */   static int findEndOfString(String sb)
/* 120:    */   {
/* 121:216 */     for (int result = sb.length(); result > 0; result--) {
/* 122:217 */       if (!Character.isWhitespace(sb.charAt(result - 1))) {
/* 123:    */         break;
/* 124:    */       }
/* 125:    */     }
/* 126:221 */     return result;
/* 127:    */   }
/* 128:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.multipart.HttpPostBodyUtil
 * JD-Core Version:    0.7.0.1
 */