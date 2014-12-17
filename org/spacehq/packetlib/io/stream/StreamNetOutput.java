/*   1:    */ package org.spacehq.packetlib.io.stream;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import java.io.OutputStream;
/*   5:    */ import org.spacehq.packetlib.io.NetOutput;
/*   6:    */ 
/*   7:    */ public class StreamNetOutput
/*   8:    */   implements NetOutput
/*   9:    */ {
/*  10:    */   private OutputStream out;
/*  11:    */   
/*  12:    */   public StreamNetOutput(OutputStream out)
/*  13:    */   {
/*  14: 16 */     this.out = out;
/*  15:    */   }
/*  16:    */   
/*  17:    */   public void writeBoolean(boolean b)
/*  18:    */     throws IOException
/*  19:    */   {
/*  20: 21 */     writeByte(b ? 1 : 0);
/*  21:    */   }
/*  22:    */   
/*  23:    */   public void writeByte(int b)
/*  24:    */     throws IOException
/*  25:    */   {
/*  26: 26 */     this.out.write(b);
/*  27:    */   }
/*  28:    */   
/*  29:    */   public void writeShort(int s)
/*  30:    */     throws IOException
/*  31:    */   {
/*  32: 31 */     writeByte((byte)(s >>> 8 & 0xFF));
/*  33: 32 */     writeByte((byte)(s >>> 0 & 0xFF));
/*  34:    */   }
/*  35:    */   
/*  36:    */   public void writeChar(int c)
/*  37:    */     throws IOException
/*  38:    */   {
/*  39: 37 */     writeByte((byte)(c >>> 8 & 0xFF));
/*  40: 38 */     writeByte((byte)(c >>> 0 & 0xFF));
/*  41:    */   }
/*  42:    */   
/*  43:    */   public void writeInt(int i)
/*  44:    */     throws IOException
/*  45:    */   {
/*  46: 43 */     writeByte((byte)(i >>> 24 & 0xFF));
/*  47: 44 */     writeByte((byte)(i >>> 16 & 0xFF));
/*  48: 45 */     writeByte((byte)(i >>> 8 & 0xFF));
/*  49: 46 */     writeByte((byte)(i >>> 0 & 0xFF));
/*  50:    */   }
/*  51:    */   
/*  52:    */   public void writeVarInt(int i)
/*  53:    */     throws IOException
/*  54:    */   {
/*  55: 51 */     while ((i & 0xFFFFFF80) != 0)
/*  56:    */     {
/*  57: 52 */       writeByte(i & 0x7F | 0x80);
/*  58: 53 */       i >>>= 7;
/*  59:    */     }
/*  60: 56 */     writeByte(i);
/*  61:    */   }
/*  62:    */   
/*  63:    */   public void writeLong(long l)
/*  64:    */     throws IOException
/*  65:    */   {
/*  66: 61 */     writeByte((byte)(int)(l >>> 56));
/*  67: 62 */     writeByte((byte)(int)(l >>> 48));
/*  68: 63 */     writeByte((byte)(int)(l >>> 40));
/*  69: 64 */     writeByte((byte)(int)(l >>> 32));
/*  70: 65 */     writeByte((byte)(int)(l >>> 24));
/*  71: 66 */     writeByte((byte)(int)(l >>> 16));
/*  72: 67 */     writeByte((byte)(int)(l >>> 8));
/*  73: 68 */     writeByte((byte)(int)(l >>> 0));
/*  74:    */   }
/*  75:    */   
/*  76:    */   public void writeFloat(float f)
/*  77:    */     throws IOException
/*  78:    */   {
/*  79: 73 */     writeInt(Float.floatToIntBits(f));
/*  80:    */   }
/*  81:    */   
/*  82:    */   public void writeDouble(double d)
/*  83:    */     throws IOException
/*  84:    */   {
/*  85: 78 */     writeLong(Double.doubleToLongBits(d));
/*  86:    */   }
/*  87:    */   
/*  88:    */   public void writePrefixedBytes(byte[] b)
/*  89:    */     throws IOException
/*  90:    */   {
/*  91: 83 */     writeShort(b.length);
/*  92: 84 */     writeBytes(b);
/*  93:    */   }
/*  94:    */   
/*  95:    */   public void writeBytes(byte[] b)
/*  96:    */     throws IOException
/*  97:    */   {
/*  98: 89 */     writeBytes(b, b.length);
/*  99:    */   }
/* 100:    */   
/* 101:    */   public void writeBytes(byte[] b, int length)
/* 102:    */     throws IOException
/* 103:    */   {
/* 104: 94 */     this.out.write(b, 0, length);
/* 105:    */   }
/* 106:    */   
/* 107:    */   public void writeString(String s)
/* 108:    */     throws IOException
/* 109:    */   {
/* 110: 99 */     if (s == null) {
/* 111:100 */       throw new IllegalArgumentException("String cannot be null!");
/* 112:    */     }
/* 113:103 */     byte[] bytes = s.getBytes("UTF-8");
/* 114:104 */     if (bytes.length > 32767) {
/* 115:105 */       throw new IOException("String too big (was " + s.length() + " bytes encoded, max " + 32767 + ")");
/* 116:    */     }
/* 117:107 */     writeVarInt(bytes.length);
/* 118:108 */     writeBytes(bytes);
/* 119:    */   }
/* 120:    */   
/* 121:    */   public void flush()
/* 122:    */     throws IOException
/* 123:    */   {
/* 124:114 */     this.out.flush();
/* 125:    */   }
/* 126:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.packetlib.io.stream.StreamNetOutput
 * JD-Core Version:    0.7.0.1
 */