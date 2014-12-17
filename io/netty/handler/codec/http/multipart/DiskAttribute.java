/*   1:    */ package io.netty.handler.codec.http.multipart;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.Unpooled;
/*   5:    */ import io.netty.channel.ChannelException;
/*   6:    */ import io.netty.handler.codec.http.HttpConstants;
/*   7:    */ import java.io.IOException;
/*   8:    */ import java.nio.charset.Charset;
/*   9:    */ 
/*  10:    */ public class DiskAttribute
/*  11:    */   extends AbstractDiskHttpData
/*  12:    */   implements Attribute
/*  13:    */ {
/*  14:    */   public static String baseDirectory;
/*  15: 32 */   public static boolean deleteOnExitTemporaryFile = true;
/*  16:    */   public static final String prefix = "Attr_";
/*  17:    */   public static final String postfix = ".att";
/*  18:    */   
/*  19:    */   public DiskAttribute(String name)
/*  20:    */   {
/*  21: 42 */     super(name, HttpConstants.DEFAULT_CHARSET, 0L);
/*  22:    */   }
/*  23:    */   
/*  24:    */   public DiskAttribute(String name, String value)
/*  25:    */     throws IOException
/*  26:    */   {
/*  27: 46 */     super(name, HttpConstants.DEFAULT_CHARSET, 0L);
/*  28: 47 */     setValue(value);
/*  29:    */   }
/*  30:    */   
/*  31:    */   public InterfaceHttpData.HttpDataType getHttpDataType()
/*  32:    */   {
/*  33: 52 */     return InterfaceHttpData.HttpDataType.Attribute;
/*  34:    */   }
/*  35:    */   
/*  36:    */   public String getValue()
/*  37:    */     throws IOException
/*  38:    */   {
/*  39: 57 */     byte[] bytes = get();
/*  40: 58 */     return new String(bytes, this.charset.name());
/*  41:    */   }
/*  42:    */   
/*  43:    */   public void setValue(String value)
/*  44:    */     throws IOException
/*  45:    */   {
/*  46: 63 */     if (value == null) {
/*  47: 64 */       throw new NullPointerException("value");
/*  48:    */     }
/*  49: 66 */     byte[] bytes = value.getBytes(this.charset.name());
/*  50: 67 */     ByteBuf buffer = Unpooled.wrappedBuffer(bytes);
/*  51: 68 */     if (this.definedSize > 0L) {
/*  52: 69 */       this.definedSize = buffer.readableBytes();
/*  53:    */     }
/*  54: 71 */     setContent(buffer);
/*  55:    */   }
/*  56:    */   
/*  57:    */   public void addContent(ByteBuf buffer, boolean last)
/*  58:    */     throws IOException
/*  59:    */   {
/*  60: 76 */     int localsize = buffer.readableBytes();
/*  61: 77 */     if ((this.definedSize > 0L) && (this.definedSize < this.size + localsize)) {
/*  62: 78 */       this.definedSize = (this.size + localsize);
/*  63:    */     }
/*  64: 80 */     super.addContent(buffer, last);
/*  65:    */   }
/*  66:    */   
/*  67:    */   public int hashCode()
/*  68:    */   {
/*  69: 84 */     return getName().hashCode();
/*  70:    */   }
/*  71:    */   
/*  72:    */   public boolean equals(Object o)
/*  73:    */   {
/*  74: 89 */     if (!(o instanceof Attribute)) {
/*  75: 90 */       return false;
/*  76:    */     }
/*  77: 92 */     Attribute attribute = (Attribute)o;
/*  78: 93 */     return getName().equalsIgnoreCase(attribute.getName());
/*  79:    */   }
/*  80:    */   
/*  81:    */   public int compareTo(InterfaceHttpData o)
/*  82:    */   {
/*  83: 98 */     if (!(o instanceof Attribute)) {
/*  84: 99 */       throw new ClassCastException("Cannot compare " + getHttpDataType() + " with " + o.getHttpDataType());
/*  85:    */     }
/*  86:102 */     return compareTo((Attribute)o);
/*  87:    */   }
/*  88:    */   
/*  89:    */   public int compareTo(Attribute o)
/*  90:    */   {
/*  91:106 */     return getName().compareToIgnoreCase(o.getName());
/*  92:    */   }
/*  93:    */   
/*  94:    */   public String toString()
/*  95:    */   {
/*  96:    */     try
/*  97:    */     {
/*  98:112 */       return getName() + '=' + getValue();
/*  99:    */     }
/* 100:    */     catch (IOException e) {}
/* 101:114 */     return getName() + "=IoException";
/* 102:    */   }
/* 103:    */   
/* 104:    */   protected boolean deleteOnExit()
/* 105:    */   {
/* 106:120 */     return deleteOnExitTemporaryFile;
/* 107:    */   }
/* 108:    */   
/* 109:    */   protected String getBaseDirectory()
/* 110:    */   {
/* 111:125 */     return baseDirectory;
/* 112:    */   }
/* 113:    */   
/* 114:    */   protected String getDiskFilename()
/* 115:    */   {
/* 116:130 */     return getName() + ".att";
/* 117:    */   }
/* 118:    */   
/* 119:    */   protected String getPostfix()
/* 120:    */   {
/* 121:135 */     return ".att";
/* 122:    */   }
/* 123:    */   
/* 124:    */   protected String getPrefix()
/* 125:    */   {
/* 126:140 */     return "Attr_";
/* 127:    */   }
/* 128:    */   
/* 129:    */   public Attribute copy()
/* 130:    */   {
/* 131:145 */     DiskAttribute attr = new DiskAttribute(getName());
/* 132:146 */     attr.setCharset(getCharset());
/* 133:147 */     ByteBuf content = content();
/* 134:148 */     if (content != null) {
/* 135:    */       try
/* 136:    */       {
/* 137:150 */         attr.setContent(content.copy());
/* 138:    */       }
/* 139:    */       catch (IOException e)
/* 140:    */       {
/* 141:152 */         throw new ChannelException(e);
/* 142:    */       }
/* 143:    */     }
/* 144:155 */     return attr;
/* 145:    */   }
/* 146:    */   
/* 147:    */   public Attribute duplicate()
/* 148:    */   {
/* 149:160 */     DiskAttribute attr = new DiskAttribute(getName());
/* 150:161 */     attr.setCharset(getCharset());
/* 151:162 */     ByteBuf content = content();
/* 152:163 */     if (content != null) {
/* 153:    */       try
/* 154:    */       {
/* 155:165 */         attr.setContent(content.duplicate());
/* 156:    */       }
/* 157:    */       catch (IOException e)
/* 158:    */       {
/* 159:167 */         throw new ChannelException(e);
/* 160:    */       }
/* 161:    */     }
/* 162:170 */     return attr;
/* 163:    */   }
/* 164:    */   
/* 165:    */   public Attribute retain(int increment)
/* 166:    */   {
/* 167:175 */     super.retain(increment);
/* 168:176 */     return this;
/* 169:    */   }
/* 170:    */   
/* 171:    */   public Attribute retain()
/* 172:    */   {
/* 173:181 */     super.retain();
/* 174:182 */     return this;
/* 175:    */   }
/* 176:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.multipart.DiskAttribute
 * JD-Core Version:    0.7.0.1
 */