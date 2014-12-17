/*   1:    */ package io.netty.handler.codec.http.multipart;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.ChannelException;
/*   5:    */ import java.io.File;
/*   6:    */ import java.io.IOException;
/*   7:    */ import java.nio.charset.Charset;
/*   8:    */ 
/*   9:    */ public class DiskFileUpload
/*  10:    */   extends AbstractDiskHttpData
/*  11:    */   implements FileUpload
/*  12:    */ {
/*  13:    */   public static String baseDirectory;
/*  14: 32 */   public static boolean deleteOnExitTemporaryFile = true;
/*  15:    */   public static final String prefix = "FUp_";
/*  16:    */   public static final String postfix = ".tmp";
/*  17:    */   private String filename;
/*  18:    */   private String contentType;
/*  19:    */   private String contentTransferEncoding;
/*  20:    */   
/*  21:    */   public DiskFileUpload(String name, String filename, String contentType, String contentTransferEncoding, Charset charset, long size)
/*  22:    */   {
/*  23: 46 */     super(name, charset, size);
/*  24: 47 */     setFilename(filename);
/*  25: 48 */     setContentType(contentType);
/*  26: 49 */     setContentTransferEncoding(contentTransferEncoding);
/*  27:    */   }
/*  28:    */   
/*  29:    */   public InterfaceHttpData.HttpDataType getHttpDataType()
/*  30:    */   {
/*  31: 54 */     return InterfaceHttpData.HttpDataType.FileUpload;
/*  32:    */   }
/*  33:    */   
/*  34:    */   public String getFilename()
/*  35:    */   {
/*  36: 59 */     return this.filename;
/*  37:    */   }
/*  38:    */   
/*  39:    */   public void setFilename(String filename)
/*  40:    */   {
/*  41: 64 */     if (filename == null) {
/*  42: 65 */       throw new NullPointerException("filename");
/*  43:    */     }
/*  44: 67 */     this.filename = filename;
/*  45:    */   }
/*  46:    */   
/*  47:    */   public int hashCode()
/*  48:    */   {
/*  49: 72 */     return getName().hashCode();
/*  50:    */   }
/*  51:    */   
/*  52:    */   public boolean equals(Object o)
/*  53:    */   {
/*  54: 77 */     if (!(o instanceof Attribute)) {
/*  55: 78 */       return false;
/*  56:    */     }
/*  57: 80 */     Attribute attribute = (Attribute)o;
/*  58: 81 */     return getName().equalsIgnoreCase(attribute.getName());
/*  59:    */   }
/*  60:    */   
/*  61:    */   public int compareTo(InterfaceHttpData o)
/*  62:    */   {
/*  63: 86 */     if (!(o instanceof FileUpload)) {
/*  64: 87 */       throw new ClassCastException("Cannot compare " + getHttpDataType() + " with " + o.getHttpDataType());
/*  65:    */     }
/*  66: 90 */     return compareTo((FileUpload)o);
/*  67:    */   }
/*  68:    */   
/*  69:    */   public int compareTo(FileUpload o)
/*  70:    */   {
/*  71: 95 */     int v = getName().compareToIgnoreCase(o.getName());
/*  72: 96 */     if (v != 0) {
/*  73: 97 */       return v;
/*  74:    */     }
/*  75:100 */     return v;
/*  76:    */   }
/*  77:    */   
/*  78:    */   public void setContentType(String contentType)
/*  79:    */   {
/*  80:105 */     if (contentType == null) {
/*  81:106 */       throw new NullPointerException("contentType");
/*  82:    */     }
/*  83:108 */     this.contentType = contentType;
/*  84:    */   }
/*  85:    */   
/*  86:    */   public String getContentType()
/*  87:    */   {
/*  88:113 */     return this.contentType;
/*  89:    */   }
/*  90:    */   
/*  91:    */   public String getContentTransferEncoding()
/*  92:    */   {
/*  93:118 */     return this.contentTransferEncoding;
/*  94:    */   }
/*  95:    */   
/*  96:    */   public void setContentTransferEncoding(String contentTransferEncoding)
/*  97:    */   {
/*  98:123 */     this.contentTransferEncoding = contentTransferEncoding;
/*  99:    */   }
/* 100:    */   
/* 101:    */   public String toString()
/* 102:    */   {
/* 103:128 */     return "Content-Disposition: form-data; name=\"" + getName() + "\"; " + "filename" + "=\"" + this.filename + "\"\r\n" + "Content-Type" + ": " + this.contentType + (this.charset != null ? "; charset=" + this.charset + "\r\n" : "\r\n") + "Content-Length" + ": " + length() + "\r\n" + "Completed: " + isCompleted() + "\r\nIsInMemory: " + isInMemory() + "\r\nRealFile: " + this.file.getAbsolutePath() + " DefaultDeleteAfter: " + deleteOnExitTemporaryFile;
/* 104:    */   }
/* 105:    */   
/* 106:    */   protected boolean deleteOnExit()
/* 107:    */   {
/* 108:142 */     return deleteOnExitTemporaryFile;
/* 109:    */   }
/* 110:    */   
/* 111:    */   protected String getBaseDirectory()
/* 112:    */   {
/* 113:147 */     return baseDirectory;
/* 114:    */   }
/* 115:    */   
/* 116:    */   protected String getDiskFilename()
/* 117:    */   {
/* 118:152 */     File file = new File(this.filename);
/* 119:153 */     return file.getName();
/* 120:    */   }
/* 121:    */   
/* 122:    */   protected String getPostfix()
/* 123:    */   {
/* 124:158 */     return ".tmp";
/* 125:    */   }
/* 126:    */   
/* 127:    */   protected String getPrefix()
/* 128:    */   {
/* 129:163 */     return "FUp_";
/* 130:    */   }
/* 131:    */   
/* 132:    */   public FileUpload copy()
/* 133:    */   {
/* 134:168 */     DiskFileUpload upload = new DiskFileUpload(getName(), getFilename(), getContentType(), getContentTransferEncoding(), getCharset(), this.size);
/* 135:    */     
/* 136:170 */     ByteBuf buf = content();
/* 137:171 */     if (buf != null) {
/* 138:    */       try
/* 139:    */       {
/* 140:173 */         upload.setContent(buf.copy());
/* 141:    */       }
/* 142:    */       catch (IOException e)
/* 143:    */       {
/* 144:175 */         throw new ChannelException(e);
/* 145:    */       }
/* 146:    */     }
/* 147:178 */     return upload;
/* 148:    */   }
/* 149:    */   
/* 150:    */   public FileUpload duplicate()
/* 151:    */   {
/* 152:183 */     DiskFileUpload upload = new DiskFileUpload(getName(), getFilename(), getContentType(), getContentTransferEncoding(), getCharset(), this.size);
/* 153:    */     
/* 154:185 */     ByteBuf buf = content();
/* 155:186 */     if (buf != null) {
/* 156:    */       try
/* 157:    */       {
/* 158:188 */         upload.setContent(buf.duplicate());
/* 159:    */       }
/* 160:    */       catch (IOException e)
/* 161:    */       {
/* 162:190 */         throw new ChannelException(e);
/* 163:    */       }
/* 164:    */     }
/* 165:193 */     return upload;
/* 166:    */   }
/* 167:    */   
/* 168:    */   public FileUpload retain(int increment)
/* 169:    */   {
/* 170:198 */     super.retain(increment);
/* 171:199 */     return this;
/* 172:    */   }
/* 173:    */   
/* 174:    */   public FileUpload retain()
/* 175:    */   {
/* 176:204 */     super.retain();
/* 177:205 */     return this;
/* 178:    */   }
/* 179:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.multipart.DiskFileUpload
 * JD-Core Version:    0.7.0.1
 */