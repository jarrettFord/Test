/*   1:    */ package io.netty.handler.codec.http.multipart;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.ChannelException;
/*   5:    */ import java.io.IOException;
/*   6:    */ import java.nio.charset.Charset;
/*   7:    */ 
/*   8:    */ public class MemoryFileUpload
/*   9:    */   extends AbstractMemoryHttpData
/*  10:    */   implements FileUpload
/*  11:    */ {
/*  12:    */   private String filename;
/*  13:    */   private String contentType;
/*  14:    */   private String contentTransferEncoding;
/*  15:    */   
/*  16:    */   public MemoryFileUpload(String name, String filename, String contentType, String contentTransferEncoding, Charset charset, long size)
/*  17:    */   {
/*  18: 40 */     super(name, charset, size);
/*  19: 41 */     setFilename(filename);
/*  20: 42 */     setContentType(contentType);
/*  21: 43 */     setContentTransferEncoding(contentTransferEncoding);
/*  22:    */   }
/*  23:    */   
/*  24:    */   public InterfaceHttpData.HttpDataType getHttpDataType()
/*  25:    */   {
/*  26: 48 */     return InterfaceHttpData.HttpDataType.FileUpload;
/*  27:    */   }
/*  28:    */   
/*  29:    */   public String getFilename()
/*  30:    */   {
/*  31: 53 */     return this.filename;
/*  32:    */   }
/*  33:    */   
/*  34:    */   public void setFilename(String filename)
/*  35:    */   {
/*  36: 58 */     if (filename == null) {
/*  37: 59 */       throw new NullPointerException("filename");
/*  38:    */     }
/*  39: 61 */     this.filename = filename;
/*  40:    */   }
/*  41:    */   
/*  42:    */   public int hashCode()
/*  43:    */   {
/*  44: 66 */     return getName().hashCode();
/*  45:    */   }
/*  46:    */   
/*  47:    */   public boolean equals(Object o)
/*  48:    */   {
/*  49: 71 */     if (!(o instanceof Attribute)) {
/*  50: 72 */       return false;
/*  51:    */     }
/*  52: 74 */     Attribute attribute = (Attribute)o;
/*  53: 75 */     return getName().equalsIgnoreCase(attribute.getName());
/*  54:    */   }
/*  55:    */   
/*  56:    */   public int compareTo(InterfaceHttpData o)
/*  57:    */   {
/*  58: 80 */     if (!(o instanceof FileUpload)) {
/*  59: 81 */       throw new ClassCastException("Cannot compare " + getHttpDataType() + " with " + o.getHttpDataType());
/*  60:    */     }
/*  61: 84 */     return compareTo((FileUpload)o);
/*  62:    */   }
/*  63:    */   
/*  64:    */   public int compareTo(FileUpload o)
/*  65:    */   {
/*  66: 89 */     int v = getName().compareToIgnoreCase(o.getName());
/*  67: 90 */     if (v != 0) {
/*  68: 91 */       return v;
/*  69:    */     }
/*  70: 94 */     return v;
/*  71:    */   }
/*  72:    */   
/*  73:    */   public void setContentType(String contentType)
/*  74:    */   {
/*  75: 99 */     if (contentType == null) {
/*  76:100 */       throw new NullPointerException("contentType");
/*  77:    */     }
/*  78:102 */     this.contentType = contentType;
/*  79:    */   }
/*  80:    */   
/*  81:    */   public String getContentType()
/*  82:    */   {
/*  83:107 */     return this.contentType;
/*  84:    */   }
/*  85:    */   
/*  86:    */   public String getContentTransferEncoding()
/*  87:    */   {
/*  88:112 */     return this.contentTransferEncoding;
/*  89:    */   }
/*  90:    */   
/*  91:    */   public void setContentTransferEncoding(String contentTransferEncoding)
/*  92:    */   {
/*  93:117 */     this.contentTransferEncoding = contentTransferEncoding;
/*  94:    */   }
/*  95:    */   
/*  96:    */   public String toString()
/*  97:    */   {
/*  98:122 */     return "Content-Disposition: form-data; name=\"" + getName() + "\"; " + "filename" + "=\"" + this.filename + "\"\r\n" + "Content-Type" + ": " + this.contentType + (this.charset != null ? "; charset=" + this.charset + "\r\n" : "\r\n") + "Content-Length" + ": " + length() + "\r\n" + "Completed: " + isCompleted() + "\r\nIsInMemory: " + isInMemory();
/*  99:    */   }
/* 100:    */   
/* 101:    */   public FileUpload copy()
/* 102:    */   {
/* 103:134 */     MemoryFileUpload upload = new MemoryFileUpload(getName(), getFilename(), getContentType(), getContentTransferEncoding(), getCharset(), this.size);
/* 104:    */     
/* 105:136 */     ByteBuf buf = content();
/* 106:137 */     if (buf != null) {
/* 107:    */       try
/* 108:    */       {
/* 109:139 */         upload.setContent(buf.copy());
/* 110:140 */         return upload;
/* 111:    */       }
/* 112:    */       catch (IOException e)
/* 113:    */       {
/* 114:142 */         throw new ChannelException(e);
/* 115:    */       }
/* 116:    */     }
/* 117:145 */     return upload;
/* 118:    */   }
/* 119:    */   
/* 120:    */   public FileUpload duplicate()
/* 121:    */   {
/* 122:150 */     MemoryFileUpload upload = new MemoryFileUpload(getName(), getFilename(), getContentType(), getContentTransferEncoding(), getCharset(), this.size);
/* 123:    */     
/* 124:152 */     ByteBuf buf = content();
/* 125:153 */     if (buf != null) {
/* 126:    */       try
/* 127:    */       {
/* 128:155 */         upload.setContent(buf.duplicate());
/* 129:156 */         return upload;
/* 130:    */       }
/* 131:    */       catch (IOException e)
/* 132:    */       {
/* 133:158 */         throw new ChannelException(e);
/* 134:    */       }
/* 135:    */     }
/* 136:161 */     return upload;
/* 137:    */   }
/* 138:    */   
/* 139:    */   public FileUpload retain()
/* 140:    */   {
/* 141:165 */     super.retain();
/* 142:166 */     return this;
/* 143:    */   }
/* 144:    */   
/* 145:    */   public FileUpload retain(int increment)
/* 146:    */   {
/* 147:171 */     super.retain(increment);
/* 148:172 */     return this;
/* 149:    */   }
/* 150:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.multipart.MemoryFileUpload
 * JD-Core Version:    0.7.0.1
 */