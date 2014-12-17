/*   1:    */ package io.netty.buffer;
/*   2:    */ 
/*   3:    */ import java.io.DataOutput;
/*   4:    */ import java.io.DataOutputStream;
/*   5:    */ import java.io.IOException;
/*   6:    */ import java.io.OutputStream;
/*   7:    */ 
/*   8:    */ public class ByteBufOutputStream
/*   9:    */   extends OutputStream
/*  10:    */   implements DataOutput
/*  11:    */ {
/*  12:    */   private final ByteBuf buffer;
/*  13:    */   private final int startIndex;
/*  14: 40 */   private final DataOutputStream utf8out = new DataOutputStream(this);
/*  15:    */   
/*  16:    */   public ByteBufOutputStream(ByteBuf buffer)
/*  17:    */   {
/*  18: 46 */     if (buffer == null) {
/*  19: 47 */       throw new NullPointerException("buffer");
/*  20:    */     }
/*  21: 49 */     this.buffer = buffer;
/*  22: 50 */     this.startIndex = buffer.writerIndex();
/*  23:    */   }
/*  24:    */   
/*  25:    */   public int writtenBytes()
/*  26:    */   {
/*  27: 57 */     return this.buffer.writerIndex() - this.startIndex;
/*  28:    */   }
/*  29:    */   
/*  30:    */   public void write(byte[] b, int off, int len)
/*  31:    */     throws IOException
/*  32:    */   {
/*  33: 62 */     if (len == 0) {
/*  34: 63 */       return;
/*  35:    */     }
/*  36: 66 */     this.buffer.writeBytes(b, off, len);
/*  37:    */   }
/*  38:    */   
/*  39:    */   public void write(byte[] b)
/*  40:    */     throws IOException
/*  41:    */   {
/*  42: 71 */     this.buffer.writeBytes(b);
/*  43:    */   }
/*  44:    */   
/*  45:    */   public void write(int b)
/*  46:    */     throws IOException
/*  47:    */   {
/*  48: 76 */     this.buffer.writeByte((byte)b);
/*  49:    */   }
/*  50:    */   
/*  51:    */   public void writeBoolean(boolean v)
/*  52:    */     throws IOException
/*  53:    */   {
/*  54: 81 */     write(v ? 1 : 0);
/*  55:    */   }
/*  56:    */   
/*  57:    */   public void writeByte(int v)
/*  58:    */     throws IOException
/*  59:    */   {
/*  60: 86 */     write(v);
/*  61:    */   }
/*  62:    */   
/*  63:    */   public void writeBytes(String s)
/*  64:    */     throws IOException
/*  65:    */   {
/*  66: 91 */     int len = s.length();
/*  67: 92 */     for (int i = 0; i < len; i++) {
/*  68: 93 */       write((byte)s.charAt(i));
/*  69:    */     }
/*  70:    */   }
/*  71:    */   
/*  72:    */   public void writeChar(int v)
/*  73:    */     throws IOException
/*  74:    */   {
/*  75: 99 */     writeShort((short)v);
/*  76:    */   }
/*  77:    */   
/*  78:    */   public void writeChars(String s)
/*  79:    */     throws IOException
/*  80:    */   {
/*  81:104 */     int len = s.length();
/*  82:105 */     for (int i = 0; i < len; i++) {
/*  83:106 */       writeChar(s.charAt(i));
/*  84:    */     }
/*  85:    */   }
/*  86:    */   
/*  87:    */   public void writeDouble(double v)
/*  88:    */     throws IOException
/*  89:    */   {
/*  90:112 */     writeLong(Double.doubleToLongBits(v));
/*  91:    */   }
/*  92:    */   
/*  93:    */   public void writeFloat(float v)
/*  94:    */     throws IOException
/*  95:    */   {
/*  96:117 */     writeInt(Float.floatToIntBits(v));
/*  97:    */   }
/*  98:    */   
/*  99:    */   public void writeInt(int v)
/* 100:    */     throws IOException
/* 101:    */   {
/* 102:122 */     this.buffer.writeInt(v);
/* 103:    */   }
/* 104:    */   
/* 105:    */   public void writeLong(long v)
/* 106:    */     throws IOException
/* 107:    */   {
/* 108:127 */     this.buffer.writeLong(v);
/* 109:    */   }
/* 110:    */   
/* 111:    */   public void writeShort(int v)
/* 112:    */     throws IOException
/* 113:    */   {
/* 114:132 */     this.buffer.writeShort((short)v);
/* 115:    */   }
/* 116:    */   
/* 117:    */   public void writeUTF(String s)
/* 118:    */     throws IOException
/* 119:    */   {
/* 120:137 */     this.utf8out.writeUTF(s);
/* 121:    */   }
/* 122:    */   
/* 123:    */   public ByteBuf buffer()
/* 124:    */   {
/* 125:144 */     return this.buffer;
/* 126:    */   }
/* 127:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.buffer.ByteBufOutputStream
 * JD-Core Version:    0.7.0.1
 */