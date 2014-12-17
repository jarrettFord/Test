/*   1:    */ package io.netty.handler.codec.serialization;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufOutputStream;
/*   5:    */ import io.netty.buffer.Unpooled;
/*   6:    */ import java.io.DataOutputStream;
/*   7:    */ import java.io.IOException;
/*   8:    */ import java.io.ObjectOutput;
/*   9:    */ import java.io.ObjectOutputStream;
/*  10:    */ import java.io.OutputStream;
/*  11:    */ 
/*  12:    */ public class ObjectEncoderOutputStream
/*  13:    */   extends OutputStream
/*  14:    */   implements ObjectOutput
/*  15:    */ {
/*  16:    */   private final DataOutputStream out;
/*  17:    */   private final int estimatedLength;
/*  18:    */   
/*  19:    */   public ObjectEncoderOutputStream(OutputStream out)
/*  20:    */   {
/*  21: 47 */     this(out, 512);
/*  22:    */   }
/*  23:    */   
/*  24:    */   public ObjectEncoderOutputStream(OutputStream out, int estimatedLength)
/*  25:    */   {
/*  26: 66 */     if (out == null) {
/*  27: 67 */       throw new NullPointerException("out");
/*  28:    */     }
/*  29: 69 */     if (estimatedLength < 0) {
/*  30: 70 */       throw new IllegalArgumentException("estimatedLength: " + estimatedLength);
/*  31:    */     }
/*  32: 73 */     if ((out instanceof DataOutputStream)) {
/*  33: 74 */       this.out = ((DataOutputStream)out);
/*  34:    */     } else {
/*  35: 76 */       this.out = new DataOutputStream(out);
/*  36:    */     }
/*  37: 78 */     this.estimatedLength = estimatedLength;
/*  38:    */   }
/*  39:    */   
/*  40:    */   public void writeObject(Object obj)
/*  41:    */     throws IOException
/*  42:    */   {
/*  43: 83 */     ByteBufOutputStream bout = new ByteBufOutputStream(Unpooled.buffer(this.estimatedLength));
/*  44: 84 */     ObjectOutputStream oout = new CompactObjectOutputStream(bout);
/*  45: 85 */     oout.writeObject(obj);
/*  46: 86 */     oout.flush();
/*  47: 87 */     oout.close();
/*  48:    */     
/*  49: 89 */     ByteBuf buffer = bout.buffer();
/*  50: 90 */     int objectSize = buffer.readableBytes();
/*  51: 91 */     writeInt(objectSize);
/*  52: 92 */     buffer.getBytes(0, this, objectSize);
/*  53:    */   }
/*  54:    */   
/*  55:    */   public void write(int b)
/*  56:    */     throws IOException
/*  57:    */   {
/*  58: 97 */     this.out.write(b);
/*  59:    */   }
/*  60:    */   
/*  61:    */   public void close()
/*  62:    */     throws IOException
/*  63:    */   {
/*  64:102 */     this.out.close();
/*  65:    */   }
/*  66:    */   
/*  67:    */   public void flush()
/*  68:    */     throws IOException
/*  69:    */   {
/*  70:107 */     this.out.flush();
/*  71:    */   }
/*  72:    */   
/*  73:    */   public final int size()
/*  74:    */   {
/*  75:111 */     return this.out.size();
/*  76:    */   }
/*  77:    */   
/*  78:    */   public void write(byte[] b, int off, int len)
/*  79:    */     throws IOException
/*  80:    */   {
/*  81:116 */     this.out.write(b, off, len);
/*  82:    */   }
/*  83:    */   
/*  84:    */   public void write(byte[] b)
/*  85:    */     throws IOException
/*  86:    */   {
/*  87:121 */     this.out.write(b);
/*  88:    */   }
/*  89:    */   
/*  90:    */   public final void writeBoolean(boolean v)
/*  91:    */     throws IOException
/*  92:    */   {
/*  93:126 */     this.out.writeBoolean(v);
/*  94:    */   }
/*  95:    */   
/*  96:    */   public final void writeByte(int v)
/*  97:    */     throws IOException
/*  98:    */   {
/*  99:131 */     this.out.writeByte(v);
/* 100:    */   }
/* 101:    */   
/* 102:    */   public final void writeBytes(String s)
/* 103:    */     throws IOException
/* 104:    */   {
/* 105:136 */     this.out.writeBytes(s);
/* 106:    */   }
/* 107:    */   
/* 108:    */   public final void writeChar(int v)
/* 109:    */     throws IOException
/* 110:    */   {
/* 111:141 */     this.out.writeChar(v);
/* 112:    */   }
/* 113:    */   
/* 114:    */   public final void writeChars(String s)
/* 115:    */     throws IOException
/* 116:    */   {
/* 117:146 */     this.out.writeChars(s);
/* 118:    */   }
/* 119:    */   
/* 120:    */   public final void writeDouble(double v)
/* 121:    */     throws IOException
/* 122:    */   {
/* 123:151 */     this.out.writeDouble(v);
/* 124:    */   }
/* 125:    */   
/* 126:    */   public final void writeFloat(float v)
/* 127:    */     throws IOException
/* 128:    */   {
/* 129:156 */     this.out.writeFloat(v);
/* 130:    */   }
/* 131:    */   
/* 132:    */   public final void writeInt(int v)
/* 133:    */     throws IOException
/* 134:    */   {
/* 135:161 */     this.out.writeInt(v);
/* 136:    */   }
/* 137:    */   
/* 138:    */   public final void writeLong(long v)
/* 139:    */     throws IOException
/* 140:    */   {
/* 141:166 */     this.out.writeLong(v);
/* 142:    */   }
/* 143:    */   
/* 144:    */   public final void writeShort(int v)
/* 145:    */     throws IOException
/* 146:    */   {
/* 147:171 */     this.out.writeShort(v);
/* 148:    */   }
/* 149:    */   
/* 150:    */   public final void writeUTF(String str)
/* 151:    */     throws IOException
/* 152:    */   {
/* 153:176 */     this.out.writeUTF(str);
/* 154:    */   }
/* 155:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.serialization.ObjectEncoderOutputStream
 * JD-Core Version:    0.7.0.1
 */