/*   1:    */ package io.netty.handler.codec.http.multipart;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import java.io.File;
/*   5:    */ import java.io.IOException;
/*   6:    */ import java.io.InputStream;
/*   7:    */ import java.nio.charset.Charset;
/*   8:    */ 
/*   9:    */ public class MixedAttribute
/*  10:    */   implements Attribute
/*  11:    */ {
/*  12:    */   private Attribute attribute;
/*  13:    */   private final long limitSize;
/*  14:    */   
/*  15:    */   public MixedAttribute(String name, long limitSize)
/*  16:    */   {
/*  17: 34 */     this.limitSize = limitSize;
/*  18: 35 */     this.attribute = new MemoryAttribute(name);
/*  19:    */   }
/*  20:    */   
/*  21:    */   public MixedAttribute(String name, String value, long limitSize)
/*  22:    */   {
/*  23: 39 */     this.limitSize = limitSize;
/*  24: 40 */     if (value.length() > this.limitSize) {
/*  25:    */       try
/*  26:    */       {
/*  27: 42 */         this.attribute = new DiskAttribute(name, value);
/*  28:    */       }
/*  29:    */       catch (IOException e)
/*  30:    */       {
/*  31:    */         try
/*  32:    */         {
/*  33: 46 */           this.attribute = new MemoryAttribute(name, value);
/*  34:    */         }
/*  35:    */         catch (IOException e1)
/*  36:    */         {
/*  37: 48 */           throw new IllegalArgumentException(e);
/*  38:    */         }
/*  39:    */       }
/*  40:    */     } else {
/*  41:    */       try
/*  42:    */       {
/*  43: 53 */         this.attribute = new MemoryAttribute(name, value);
/*  44:    */       }
/*  45:    */       catch (IOException e)
/*  46:    */       {
/*  47: 55 */         throw new IllegalArgumentException(e);
/*  48:    */       }
/*  49:    */     }
/*  50:    */   }
/*  51:    */   
/*  52:    */   public void addContent(ByteBuf buffer, boolean last)
/*  53:    */     throws IOException
/*  54:    */   {
/*  55: 62 */     if (((this.attribute instanceof MemoryAttribute)) && 
/*  56: 63 */       (this.attribute.length() + buffer.readableBytes() > this.limitSize))
/*  57:    */     {
/*  58: 64 */       DiskAttribute diskAttribute = new DiskAttribute(this.attribute.getName());
/*  59: 66 */       if (((MemoryAttribute)this.attribute).getByteBuf() != null) {
/*  60: 67 */         diskAttribute.addContent(((MemoryAttribute)this.attribute).getByteBuf(), false);
/*  61:    */       }
/*  62: 70 */       this.attribute = diskAttribute;
/*  63:    */     }
/*  64: 73 */     this.attribute.addContent(buffer, last);
/*  65:    */   }
/*  66:    */   
/*  67:    */   public void delete()
/*  68:    */   {
/*  69: 78 */     this.attribute.delete();
/*  70:    */   }
/*  71:    */   
/*  72:    */   public byte[] get()
/*  73:    */     throws IOException
/*  74:    */   {
/*  75: 83 */     return this.attribute.get();
/*  76:    */   }
/*  77:    */   
/*  78:    */   public ByteBuf getByteBuf()
/*  79:    */     throws IOException
/*  80:    */   {
/*  81: 88 */     return this.attribute.getByteBuf();
/*  82:    */   }
/*  83:    */   
/*  84:    */   public Charset getCharset()
/*  85:    */   {
/*  86: 93 */     return this.attribute.getCharset();
/*  87:    */   }
/*  88:    */   
/*  89:    */   public String getString()
/*  90:    */     throws IOException
/*  91:    */   {
/*  92: 98 */     return this.attribute.getString();
/*  93:    */   }
/*  94:    */   
/*  95:    */   public String getString(Charset encoding)
/*  96:    */     throws IOException
/*  97:    */   {
/*  98:103 */     return this.attribute.getString(encoding);
/*  99:    */   }
/* 100:    */   
/* 101:    */   public boolean isCompleted()
/* 102:    */   {
/* 103:108 */     return this.attribute.isCompleted();
/* 104:    */   }
/* 105:    */   
/* 106:    */   public boolean isInMemory()
/* 107:    */   {
/* 108:113 */     return this.attribute.isInMemory();
/* 109:    */   }
/* 110:    */   
/* 111:    */   public long length()
/* 112:    */   {
/* 113:118 */     return this.attribute.length();
/* 114:    */   }
/* 115:    */   
/* 116:    */   public boolean renameTo(File dest)
/* 117:    */     throws IOException
/* 118:    */   {
/* 119:123 */     return this.attribute.renameTo(dest);
/* 120:    */   }
/* 121:    */   
/* 122:    */   public void setCharset(Charset charset)
/* 123:    */   {
/* 124:128 */     this.attribute.setCharset(charset);
/* 125:    */   }
/* 126:    */   
/* 127:    */   public void setContent(ByteBuf buffer)
/* 128:    */     throws IOException
/* 129:    */   {
/* 130:133 */     if ((buffer.readableBytes() > this.limitSize) && 
/* 131:134 */       ((this.attribute instanceof MemoryAttribute))) {
/* 132:136 */       this.attribute = new DiskAttribute(this.attribute.getName());
/* 133:    */     }
/* 134:139 */     this.attribute.setContent(buffer);
/* 135:    */   }
/* 136:    */   
/* 137:    */   public void setContent(File file)
/* 138:    */     throws IOException
/* 139:    */   {
/* 140:144 */     if ((file.length() > this.limitSize) && 
/* 141:145 */       ((this.attribute instanceof MemoryAttribute))) {
/* 142:147 */       this.attribute = new DiskAttribute(this.attribute.getName());
/* 143:    */     }
/* 144:150 */     this.attribute.setContent(file);
/* 145:    */   }
/* 146:    */   
/* 147:    */   public void setContent(InputStream inputStream)
/* 148:    */     throws IOException
/* 149:    */   {
/* 150:155 */     if ((this.attribute instanceof MemoryAttribute)) {
/* 151:157 */       this.attribute = new DiskAttribute(this.attribute.getName());
/* 152:    */     }
/* 153:159 */     this.attribute.setContent(inputStream);
/* 154:    */   }
/* 155:    */   
/* 156:    */   public InterfaceHttpData.HttpDataType getHttpDataType()
/* 157:    */   {
/* 158:164 */     return this.attribute.getHttpDataType();
/* 159:    */   }
/* 160:    */   
/* 161:    */   public String getName()
/* 162:    */   {
/* 163:169 */     return this.attribute.getName();
/* 164:    */   }
/* 165:    */   
/* 166:    */   public int compareTo(InterfaceHttpData o)
/* 167:    */   {
/* 168:174 */     return this.attribute.compareTo(o);
/* 169:    */   }
/* 170:    */   
/* 171:    */   public String toString()
/* 172:    */   {
/* 173:179 */     return "Mixed: " + this.attribute.toString();
/* 174:    */   }
/* 175:    */   
/* 176:    */   public String getValue()
/* 177:    */     throws IOException
/* 178:    */   {
/* 179:184 */     return this.attribute.getValue();
/* 180:    */   }
/* 181:    */   
/* 182:    */   public void setValue(String value)
/* 183:    */     throws IOException
/* 184:    */   {
/* 185:189 */     this.attribute.setValue(value);
/* 186:    */   }
/* 187:    */   
/* 188:    */   public ByteBuf getChunk(int length)
/* 189:    */     throws IOException
/* 190:    */   {
/* 191:194 */     return this.attribute.getChunk(length);
/* 192:    */   }
/* 193:    */   
/* 194:    */   public File getFile()
/* 195:    */     throws IOException
/* 196:    */   {
/* 197:199 */     return this.attribute.getFile();
/* 198:    */   }
/* 199:    */   
/* 200:    */   public Attribute copy()
/* 201:    */   {
/* 202:204 */     return this.attribute.copy();
/* 203:    */   }
/* 204:    */   
/* 205:    */   public Attribute duplicate()
/* 206:    */   {
/* 207:209 */     return this.attribute.duplicate();
/* 208:    */   }
/* 209:    */   
/* 210:    */   public ByteBuf content()
/* 211:    */   {
/* 212:214 */     return this.attribute.content();
/* 213:    */   }
/* 214:    */   
/* 215:    */   public int refCnt()
/* 216:    */   {
/* 217:219 */     return this.attribute.refCnt();
/* 218:    */   }
/* 219:    */   
/* 220:    */   public Attribute retain()
/* 221:    */   {
/* 222:224 */     this.attribute.retain();
/* 223:225 */     return this;
/* 224:    */   }
/* 225:    */   
/* 226:    */   public Attribute retain(int increment)
/* 227:    */   {
/* 228:230 */     this.attribute.retain(increment);
/* 229:231 */     return this;
/* 230:    */   }
/* 231:    */   
/* 232:    */   public boolean release()
/* 233:    */   {
/* 234:236 */     return this.attribute.release();
/* 235:    */   }
/* 236:    */   
/* 237:    */   public boolean release(int decrement)
/* 238:    */   {
/* 239:241 */     return this.attribute.release(decrement);
/* 240:    */   }
/* 241:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.multipart.MixedAttribute
 * JD-Core Version:    0.7.0.1
 */