/*   1:    */ package io.netty.handler.codec.http.multipart;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.Unpooled;
/*   5:    */ import io.netty.channel.ChannelException;
/*   6:    */ import io.netty.handler.codec.http.HttpConstants;
/*   7:    */ import java.io.IOException;
/*   8:    */ import java.nio.charset.Charset;
/*   9:    */ 
/*  10:    */ public class MemoryAttribute
/*  11:    */   extends AbstractMemoryHttpData
/*  12:    */   implements Attribute
/*  13:    */ {
/*  14:    */   public MemoryAttribute(String name)
/*  15:    */   {
/*  16: 32 */     super(name, HttpConstants.DEFAULT_CHARSET, 0L);
/*  17:    */   }
/*  18:    */   
/*  19:    */   public MemoryAttribute(String name, String value)
/*  20:    */     throws IOException
/*  21:    */   {
/*  22: 36 */     super(name, HttpConstants.DEFAULT_CHARSET, 0L);
/*  23: 37 */     setValue(value);
/*  24:    */   }
/*  25:    */   
/*  26:    */   public InterfaceHttpData.HttpDataType getHttpDataType()
/*  27:    */   {
/*  28: 42 */     return InterfaceHttpData.HttpDataType.Attribute;
/*  29:    */   }
/*  30:    */   
/*  31:    */   public String getValue()
/*  32:    */   {
/*  33: 47 */     return getByteBuf().toString(this.charset);
/*  34:    */   }
/*  35:    */   
/*  36:    */   public void setValue(String value)
/*  37:    */     throws IOException
/*  38:    */   {
/*  39: 52 */     if (value == null) {
/*  40: 53 */       throw new NullPointerException("value");
/*  41:    */     }
/*  42: 55 */     byte[] bytes = value.getBytes(this.charset.name());
/*  43: 56 */     ByteBuf buffer = Unpooled.wrappedBuffer(bytes);
/*  44: 57 */     if (this.definedSize > 0L) {
/*  45: 58 */       this.definedSize = buffer.readableBytes();
/*  46:    */     }
/*  47: 60 */     setContent(buffer);
/*  48:    */   }
/*  49:    */   
/*  50:    */   public void addContent(ByteBuf buffer, boolean last)
/*  51:    */     throws IOException
/*  52:    */   {
/*  53: 65 */     int localsize = buffer.readableBytes();
/*  54: 66 */     if ((this.definedSize > 0L) && (this.definedSize < this.size + localsize)) {
/*  55: 67 */       this.definedSize = (this.size + localsize);
/*  56:    */     }
/*  57: 69 */     super.addContent(buffer, last);
/*  58:    */   }
/*  59:    */   
/*  60:    */   public int hashCode()
/*  61:    */   {
/*  62: 74 */     return getName().hashCode();
/*  63:    */   }
/*  64:    */   
/*  65:    */   public boolean equals(Object o)
/*  66:    */   {
/*  67: 79 */     if (!(o instanceof Attribute)) {
/*  68: 80 */       return false;
/*  69:    */     }
/*  70: 82 */     Attribute attribute = (Attribute)o;
/*  71: 83 */     return getName().equalsIgnoreCase(attribute.getName());
/*  72:    */   }
/*  73:    */   
/*  74:    */   public int compareTo(InterfaceHttpData other)
/*  75:    */   {
/*  76: 88 */     if (!(other instanceof Attribute)) {
/*  77: 89 */       throw new ClassCastException("Cannot compare " + getHttpDataType() + " with " + other.getHttpDataType());
/*  78:    */     }
/*  79: 92 */     return compareTo((Attribute)other);
/*  80:    */   }
/*  81:    */   
/*  82:    */   public int compareTo(Attribute o)
/*  83:    */   {
/*  84: 96 */     return getName().compareToIgnoreCase(o.getName());
/*  85:    */   }
/*  86:    */   
/*  87:    */   public String toString()
/*  88:    */   {
/*  89:101 */     return getName() + '=' + getValue();
/*  90:    */   }
/*  91:    */   
/*  92:    */   public Attribute copy()
/*  93:    */   {
/*  94:106 */     MemoryAttribute attr = new MemoryAttribute(getName());
/*  95:107 */     attr.setCharset(getCharset());
/*  96:108 */     ByteBuf content = content();
/*  97:109 */     if (content != null) {
/*  98:    */       try
/*  99:    */       {
/* 100:111 */         attr.setContent(content.copy());
/* 101:    */       }
/* 102:    */       catch (IOException e)
/* 103:    */       {
/* 104:113 */         throw new ChannelException(e);
/* 105:    */       }
/* 106:    */     }
/* 107:116 */     return attr;
/* 108:    */   }
/* 109:    */   
/* 110:    */   public Attribute duplicate()
/* 111:    */   {
/* 112:121 */     MemoryAttribute attr = new MemoryAttribute(getName());
/* 113:122 */     attr.setCharset(getCharset());
/* 114:123 */     ByteBuf content = content();
/* 115:124 */     if (content != null) {
/* 116:    */       try
/* 117:    */       {
/* 118:126 */         attr.setContent(content.duplicate());
/* 119:    */       }
/* 120:    */       catch (IOException e)
/* 121:    */       {
/* 122:128 */         throw new ChannelException(e);
/* 123:    */       }
/* 124:    */     }
/* 125:131 */     return attr;
/* 126:    */   }
/* 127:    */   
/* 128:    */   public Attribute retain()
/* 129:    */   {
/* 130:136 */     super.retain();
/* 131:137 */     return this;
/* 132:    */   }
/* 133:    */   
/* 134:    */   public Attribute retain(int increment)
/* 135:    */   {
/* 136:142 */     super.retain(increment);
/* 137:143 */     return this;
/* 138:    */   }
/* 139:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.multipart.MemoryAttribute
 * JD-Core Version:    0.7.0.1
 */