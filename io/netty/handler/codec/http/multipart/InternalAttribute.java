/*   1:    */ package io.netty.handler.codec.http.multipart;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.CompositeByteBuf;
/*   5:    */ import io.netty.buffer.Unpooled;
/*   6:    */ import io.netty.util.AbstractReferenceCounted;
/*   7:    */ import java.nio.charset.Charset;
/*   8:    */ import java.util.ArrayList;
/*   9:    */ import java.util.List;
/*  10:    */ 
/*  11:    */ final class InternalAttribute
/*  12:    */   extends AbstractReferenceCounted
/*  13:    */   implements InterfaceHttpData
/*  14:    */ {
/*  15: 31 */   private final List<ByteBuf> value = new ArrayList();
/*  16:    */   private final Charset charset;
/*  17:    */   private int size;
/*  18:    */   
/*  19:    */   InternalAttribute(Charset charset)
/*  20:    */   {
/*  21: 36 */     this.charset = charset;
/*  22:    */   }
/*  23:    */   
/*  24:    */   public InterfaceHttpData.HttpDataType getHttpDataType()
/*  25:    */   {
/*  26: 41 */     return InterfaceHttpData.HttpDataType.InternalAttribute;
/*  27:    */   }
/*  28:    */   
/*  29:    */   public void addValue(String value)
/*  30:    */   {
/*  31: 45 */     if (value == null) {
/*  32: 46 */       throw new NullPointerException("value");
/*  33:    */     }
/*  34: 48 */     ByteBuf buf = Unpooled.copiedBuffer(value, this.charset);
/*  35: 49 */     this.value.add(buf);
/*  36: 50 */     this.size += buf.readableBytes();
/*  37:    */   }
/*  38:    */   
/*  39:    */   public void addValue(String value, int rank)
/*  40:    */   {
/*  41: 54 */     if (value == null) {
/*  42: 55 */       throw new NullPointerException("value");
/*  43:    */     }
/*  44: 57 */     ByteBuf buf = Unpooled.copiedBuffer(value, this.charset);
/*  45: 58 */     this.value.add(rank, buf);
/*  46: 59 */     this.size += buf.readableBytes();
/*  47:    */   }
/*  48:    */   
/*  49:    */   public void setValue(String value, int rank)
/*  50:    */   {
/*  51: 63 */     if (value == null) {
/*  52: 64 */       throw new NullPointerException("value");
/*  53:    */     }
/*  54: 66 */     ByteBuf buf = Unpooled.copiedBuffer(value, this.charset);
/*  55: 67 */     ByteBuf old = (ByteBuf)this.value.set(rank, buf);
/*  56: 68 */     if (old != null)
/*  57:    */     {
/*  58: 69 */       this.size -= old.readableBytes();
/*  59: 70 */       old.release();
/*  60:    */     }
/*  61: 72 */     this.size += buf.readableBytes();
/*  62:    */   }
/*  63:    */   
/*  64:    */   public int hashCode()
/*  65:    */   {
/*  66: 77 */     return getName().hashCode();
/*  67:    */   }
/*  68:    */   
/*  69:    */   public boolean equals(Object o)
/*  70:    */   {
/*  71: 82 */     if (!(o instanceof Attribute)) {
/*  72: 83 */       return false;
/*  73:    */     }
/*  74: 85 */     Attribute attribute = (Attribute)o;
/*  75: 86 */     return getName().equalsIgnoreCase(attribute.getName());
/*  76:    */   }
/*  77:    */   
/*  78:    */   public int compareTo(InterfaceHttpData o)
/*  79:    */   {
/*  80: 91 */     if (!(o instanceof InternalAttribute)) {
/*  81: 92 */       throw new ClassCastException("Cannot compare " + getHttpDataType() + " with " + o.getHttpDataType());
/*  82:    */     }
/*  83: 95 */     return compareTo((InternalAttribute)o);
/*  84:    */   }
/*  85:    */   
/*  86:    */   public int compareTo(InternalAttribute o)
/*  87:    */   {
/*  88: 99 */     return getName().compareToIgnoreCase(o.getName());
/*  89:    */   }
/*  90:    */   
/*  91:    */   public String toString()
/*  92:    */   {
/*  93:104 */     StringBuilder result = new StringBuilder();
/*  94:105 */     for (ByteBuf elt : this.value) {
/*  95:106 */       result.append(elt.toString(this.charset));
/*  96:    */     }
/*  97:108 */     return result.toString();
/*  98:    */   }
/*  99:    */   
/* 100:    */   public int size()
/* 101:    */   {
/* 102:112 */     return this.size;
/* 103:    */   }
/* 104:    */   
/* 105:    */   public ByteBuf toByteBuf()
/* 106:    */   {
/* 107:116 */     return Unpooled.compositeBuffer().addComponents(this.value).writerIndex(size()).readerIndex(0);
/* 108:    */   }
/* 109:    */   
/* 110:    */   public String getName()
/* 111:    */   {
/* 112:121 */     return "InternalAttribute";
/* 113:    */   }
/* 114:    */   
/* 115:    */   protected void deallocate() {}
/* 116:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.multipart.InternalAttribute
 * JD-Core Version:    0.7.0.1
 */