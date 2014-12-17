/*   1:    */ package io.netty.handler.codec.http.multipart;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import java.io.File;
/*   5:    */ import java.io.IOException;
/*   6:    */ import java.io.InputStream;
/*   7:    */ import java.nio.charset.Charset;
/*   8:    */ 
/*   9:    */ public class MixedFileUpload
/*  10:    */   implements FileUpload
/*  11:    */ {
/*  12:    */   private FileUpload fileUpload;
/*  13:    */   private final long limitSize;
/*  14:    */   private final long definedSize;
/*  15:    */   
/*  16:    */   public MixedFileUpload(String name, String filename, String contentType, String contentTransferEncoding, Charset charset, long size, long limitSize)
/*  17:    */   {
/*  18: 39 */     this.limitSize = limitSize;
/*  19: 40 */     if (size > this.limitSize) {
/*  20: 41 */       this.fileUpload = new DiskFileUpload(name, filename, contentType, contentTransferEncoding, charset, size);
/*  21:    */     } else {
/*  22: 44 */       this.fileUpload = new MemoryFileUpload(name, filename, contentType, contentTransferEncoding, charset, size);
/*  23:    */     }
/*  24: 47 */     this.definedSize = size;
/*  25:    */   }
/*  26:    */   
/*  27:    */   public void addContent(ByteBuf buffer, boolean last)
/*  28:    */     throws IOException
/*  29:    */   {
/*  30: 53 */     if (((this.fileUpload instanceof MemoryFileUpload)) && 
/*  31: 54 */       (this.fileUpload.length() + buffer.readableBytes() > this.limitSize))
/*  32:    */     {
/*  33: 55 */       DiskFileUpload diskFileUpload = new DiskFileUpload(this.fileUpload.getName(), this.fileUpload.getFilename(), this.fileUpload.getContentType(), this.fileUpload.getContentTransferEncoding(), this.fileUpload.getCharset(), this.definedSize);
/*  34:    */       
/*  35:    */ 
/*  36:    */ 
/*  37:    */ 
/*  38:    */ 
/*  39: 61 */       ByteBuf data = this.fileUpload.getByteBuf();
/*  40: 62 */       if ((data != null) && (data.isReadable())) {
/*  41: 63 */         diskFileUpload.addContent(data.retain(), false);
/*  42:    */       }
/*  43: 66 */       this.fileUpload.release();
/*  44:    */       
/*  45: 68 */       this.fileUpload = diskFileUpload;
/*  46:    */     }
/*  47: 71 */     this.fileUpload.addContent(buffer, last);
/*  48:    */   }
/*  49:    */   
/*  50:    */   public void delete()
/*  51:    */   {
/*  52: 76 */     this.fileUpload.delete();
/*  53:    */   }
/*  54:    */   
/*  55:    */   public byte[] get()
/*  56:    */     throws IOException
/*  57:    */   {
/*  58: 81 */     return this.fileUpload.get();
/*  59:    */   }
/*  60:    */   
/*  61:    */   public ByteBuf getByteBuf()
/*  62:    */     throws IOException
/*  63:    */   {
/*  64: 86 */     return this.fileUpload.getByteBuf();
/*  65:    */   }
/*  66:    */   
/*  67:    */   public Charset getCharset()
/*  68:    */   {
/*  69: 91 */     return this.fileUpload.getCharset();
/*  70:    */   }
/*  71:    */   
/*  72:    */   public String getContentType()
/*  73:    */   {
/*  74: 96 */     return this.fileUpload.getContentType();
/*  75:    */   }
/*  76:    */   
/*  77:    */   public String getContentTransferEncoding()
/*  78:    */   {
/*  79:101 */     return this.fileUpload.getContentTransferEncoding();
/*  80:    */   }
/*  81:    */   
/*  82:    */   public String getFilename()
/*  83:    */   {
/*  84:106 */     return this.fileUpload.getFilename();
/*  85:    */   }
/*  86:    */   
/*  87:    */   public String getString()
/*  88:    */     throws IOException
/*  89:    */   {
/*  90:111 */     return this.fileUpload.getString();
/*  91:    */   }
/*  92:    */   
/*  93:    */   public String getString(Charset encoding)
/*  94:    */     throws IOException
/*  95:    */   {
/*  96:116 */     return this.fileUpload.getString(encoding);
/*  97:    */   }
/*  98:    */   
/*  99:    */   public boolean isCompleted()
/* 100:    */   {
/* 101:121 */     return this.fileUpload.isCompleted();
/* 102:    */   }
/* 103:    */   
/* 104:    */   public boolean isInMemory()
/* 105:    */   {
/* 106:126 */     return this.fileUpload.isInMemory();
/* 107:    */   }
/* 108:    */   
/* 109:    */   public long length()
/* 110:    */   {
/* 111:131 */     return this.fileUpload.length();
/* 112:    */   }
/* 113:    */   
/* 114:    */   public boolean renameTo(File dest)
/* 115:    */     throws IOException
/* 116:    */   {
/* 117:136 */     return this.fileUpload.renameTo(dest);
/* 118:    */   }
/* 119:    */   
/* 120:    */   public void setCharset(Charset charset)
/* 121:    */   {
/* 122:141 */     this.fileUpload.setCharset(charset);
/* 123:    */   }
/* 124:    */   
/* 125:    */   public void setContent(ByteBuf buffer)
/* 126:    */     throws IOException
/* 127:    */   {
/* 128:146 */     if ((buffer.readableBytes() > this.limitSize) && 
/* 129:147 */       ((this.fileUpload instanceof MemoryFileUpload)))
/* 130:    */     {
/* 131:148 */       FileUpload memoryUpload = this.fileUpload;
/* 132:    */       
/* 133:150 */       this.fileUpload = new DiskFileUpload(memoryUpload.getName(), memoryUpload.getFilename(), memoryUpload.getContentType(), memoryUpload.getContentTransferEncoding(), memoryUpload.getCharset(), this.definedSize);
/* 134:    */       
/* 135:    */ 
/* 136:    */ 
/* 137:    */ 
/* 138:    */ 
/* 139:    */ 
/* 140:157 */       memoryUpload.release();
/* 141:    */     }
/* 142:160 */     this.fileUpload.setContent(buffer);
/* 143:    */   }
/* 144:    */   
/* 145:    */   public void setContent(File file)
/* 146:    */     throws IOException
/* 147:    */   {
/* 148:165 */     if ((file.length() > this.limitSize) && 
/* 149:166 */       ((this.fileUpload instanceof MemoryFileUpload)))
/* 150:    */     {
/* 151:167 */       FileUpload memoryUpload = this.fileUpload;
/* 152:    */       
/* 153:    */ 
/* 154:170 */       this.fileUpload = new DiskFileUpload(memoryUpload.getName(), memoryUpload.getFilename(), memoryUpload.getContentType(), memoryUpload.getContentTransferEncoding(), memoryUpload.getCharset(), this.definedSize);
/* 155:    */       
/* 156:    */ 
/* 157:    */ 
/* 158:    */ 
/* 159:    */ 
/* 160:    */ 
/* 161:177 */       memoryUpload.release();
/* 162:    */     }
/* 163:180 */     this.fileUpload.setContent(file);
/* 164:    */   }
/* 165:    */   
/* 166:    */   public void setContent(InputStream inputStream)
/* 167:    */     throws IOException
/* 168:    */   {
/* 169:185 */     if ((this.fileUpload instanceof MemoryFileUpload))
/* 170:    */     {
/* 171:186 */       FileUpload memoryUpload = this.fileUpload;
/* 172:    */       
/* 173:    */ 
/* 174:189 */       this.fileUpload = new DiskFileUpload(this.fileUpload.getName(), this.fileUpload.getFilename(), this.fileUpload.getContentType(), this.fileUpload.getContentTransferEncoding(), this.fileUpload.getCharset(), this.definedSize);
/* 175:    */       
/* 176:    */ 
/* 177:    */ 
/* 178:    */ 
/* 179:    */ 
/* 180:    */ 
/* 181:196 */       memoryUpload.release();
/* 182:    */     }
/* 183:198 */     this.fileUpload.setContent(inputStream);
/* 184:    */   }
/* 185:    */   
/* 186:    */   public void setContentType(String contentType)
/* 187:    */   {
/* 188:203 */     this.fileUpload.setContentType(contentType);
/* 189:    */   }
/* 190:    */   
/* 191:    */   public void setContentTransferEncoding(String contentTransferEncoding)
/* 192:    */   {
/* 193:208 */     this.fileUpload.setContentTransferEncoding(contentTransferEncoding);
/* 194:    */   }
/* 195:    */   
/* 196:    */   public void setFilename(String filename)
/* 197:    */   {
/* 198:213 */     this.fileUpload.setFilename(filename);
/* 199:    */   }
/* 200:    */   
/* 201:    */   public InterfaceHttpData.HttpDataType getHttpDataType()
/* 202:    */   {
/* 203:218 */     return this.fileUpload.getHttpDataType();
/* 204:    */   }
/* 205:    */   
/* 206:    */   public String getName()
/* 207:    */   {
/* 208:223 */     return this.fileUpload.getName();
/* 209:    */   }
/* 210:    */   
/* 211:    */   public int compareTo(InterfaceHttpData o)
/* 212:    */   {
/* 213:228 */     return this.fileUpload.compareTo(o);
/* 214:    */   }
/* 215:    */   
/* 216:    */   public String toString()
/* 217:    */   {
/* 218:233 */     return "Mixed: " + this.fileUpload.toString();
/* 219:    */   }
/* 220:    */   
/* 221:    */   public ByteBuf getChunk(int length)
/* 222:    */     throws IOException
/* 223:    */   {
/* 224:238 */     return this.fileUpload.getChunk(length);
/* 225:    */   }
/* 226:    */   
/* 227:    */   public File getFile()
/* 228:    */     throws IOException
/* 229:    */   {
/* 230:243 */     return this.fileUpload.getFile();
/* 231:    */   }
/* 232:    */   
/* 233:    */   public FileUpload copy()
/* 234:    */   {
/* 235:248 */     return this.fileUpload.copy();
/* 236:    */   }
/* 237:    */   
/* 238:    */   public FileUpload duplicate()
/* 239:    */   {
/* 240:253 */     return this.fileUpload.duplicate();
/* 241:    */   }
/* 242:    */   
/* 243:    */   public ByteBuf content()
/* 244:    */   {
/* 245:258 */     return this.fileUpload.content();
/* 246:    */   }
/* 247:    */   
/* 248:    */   public int refCnt()
/* 249:    */   {
/* 250:263 */     return this.fileUpload.refCnt();
/* 251:    */   }
/* 252:    */   
/* 253:    */   public FileUpload retain()
/* 254:    */   {
/* 255:268 */     this.fileUpload.retain();
/* 256:269 */     return this;
/* 257:    */   }
/* 258:    */   
/* 259:    */   public FileUpload retain(int increment)
/* 260:    */   {
/* 261:274 */     this.fileUpload.retain(increment);
/* 262:275 */     return this;
/* 263:    */   }
/* 264:    */   
/* 265:    */   public boolean release()
/* 266:    */   {
/* 267:280 */     return this.fileUpload.release();
/* 268:    */   }
/* 269:    */   
/* 270:    */   public boolean release(int decrement)
/* 271:    */   {
/* 272:285 */     return this.fileUpload.release(decrement);
/* 273:    */   }
/* 274:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.multipart.MixedFileUpload
 * JD-Core Version:    0.7.0.1
 */