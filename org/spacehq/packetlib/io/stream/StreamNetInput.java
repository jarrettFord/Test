/*   1:    */ package org.spacehq.packetlib.io.stream;
/*   2:    */ 
/*   3:    */ import java.io.EOFException;
/*   4:    */ import java.io.IOException;
/*   5:    */ import java.io.InputStream;
/*   6:    */ import org.spacehq.packetlib.io.NetInput;
/*   7:    */ 
/*   8:    */ public class StreamNetInput
/*   9:    */   implements NetInput
/*  10:    */ {
/*  11:    */   private InputStream in;
/*  12:    */   
/*  13:    */   public StreamNetInput(InputStream in)
/*  14:    */   {
/*  15: 17 */     this.in = in;
/*  16:    */   }
/*  17:    */   
/*  18:    */   public boolean readBoolean()
/*  19:    */     throws IOException
/*  20:    */   {
/*  21: 22 */     return readByte() == 1;
/*  22:    */   }
/*  23:    */   
/*  24:    */   public byte readByte()
/*  25:    */     throws IOException
/*  26:    */   {
/*  27: 27 */     return (byte)readUnsignedByte();
/*  28:    */   }
/*  29:    */   
/*  30:    */   public int readUnsignedByte()
/*  31:    */     throws IOException
/*  32:    */   {
/*  33: 32 */     int b = this.in.read();
/*  34: 33 */     if (b < 0) {
/*  35: 34 */       throw new EOFException();
/*  36:    */     }
/*  37: 37 */     return b;
/*  38:    */   }
/*  39:    */   
/*  40:    */   public short readShort()
/*  41:    */     throws IOException
/*  42:    */   {
/*  43: 42 */     return (short)readUnsignedShort();
/*  44:    */   }
/*  45:    */   
/*  46:    */   public int readUnsignedShort()
/*  47:    */     throws IOException
/*  48:    */   {
/*  49: 47 */     int ch1 = readUnsignedByte();
/*  50: 48 */     int ch2 = readUnsignedByte();
/*  51: 49 */     return (ch1 << 8) + (ch2 << 0);
/*  52:    */   }
/*  53:    */   
/*  54:    */   public char readChar()
/*  55:    */     throws IOException
/*  56:    */   {
/*  57: 54 */     int ch1 = readUnsignedByte();
/*  58: 55 */     int ch2 = readUnsignedByte();
/*  59: 56 */     return (char)((ch1 << 8) + (ch2 << 0));
/*  60:    */   }
/*  61:    */   
/*  62:    */   public int readInt()
/*  63:    */     throws IOException
/*  64:    */   {
/*  65: 61 */     int ch1 = readUnsignedByte();
/*  66: 62 */     int ch2 = readUnsignedByte();
/*  67: 63 */     int ch3 = readUnsignedByte();
/*  68: 64 */     int ch4 = readUnsignedByte();
/*  69: 65 */     return (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0);
/*  70:    */   }
/*  71:    */   
/*  72:    */   public int readVarInt()
/*  73:    */     throws IOException
/*  74:    */   {
/*  75: 70 */     int ret = 0;
/*  76: 71 */     int size = 0;
/*  77:    */     byte b;
/*  78: 73 */     while (((b = readByte()) & 0x80) == 128)
/*  79:    */     {
/*  80:    */       byte b;
/*  81: 74 */       ret |= (b & 0x7F) << size++ * 7;
/*  82: 75 */       if (size > 5) {
/*  83: 76 */         throw new IOException("Varint too big");
/*  84:    */       }
/*  85:    */     }
/*  86: 80 */     return ret;
/*  87:    */   }
/*  88:    */   
/*  89:    */   public long readLong()
/*  90:    */     throws IOException
/*  91:    */   {
/*  92: 85 */     byte[] read = readBytes(8);
/*  93: 86 */     return (read[0] << 56) + ((read[1] & 0xFF) << 48) + ((read[2] & 0xFF) << 40) + ((read[3] & 0xFF) << 32) + ((read[4] & 0xFF) << 24) + ((read[5] & 0xFF) << 16) + ((read[6] & 0xFF) << 8) + ((read[7] & 0xFF) << 0);
/*  94:    */   }
/*  95:    */   
/*  96:    */   public float readFloat()
/*  97:    */     throws IOException
/*  98:    */   {
/*  99: 91 */     return Float.intBitsToFloat(readInt());
/* 100:    */   }
/* 101:    */   
/* 102:    */   public double readDouble()
/* 103:    */     throws IOException
/* 104:    */   {
/* 105: 96 */     return Double.longBitsToDouble(readLong());
/* 106:    */   }
/* 107:    */   
/* 108:    */   public byte[] readPrefixedBytes()
/* 109:    */     throws IOException
/* 110:    */   {
/* 111:101 */     short length = readShort();
/* 112:102 */     return readBytes(length);
/* 113:    */   }
/* 114:    */   
/* 115:    */   public byte[] readBytes(int length)
/* 116:    */     throws IOException
/* 117:    */   {
/* 118:107 */     if (length < 0) {
/* 119:108 */       throw new IllegalArgumentException("Array cannot have length less than 0.");
/* 120:    */     }
/* 121:111 */     byte[] b = new byte[length];
/* 122:112 */     int n = 0;
/* 123:113 */     while (n < length)
/* 124:    */     {
/* 125:114 */       int count = this.in.read(b, n, length - n);
/* 126:115 */       if (count < 0) {
/* 127:116 */         throw new EOFException();
/* 128:    */       }
/* 129:119 */       n += count;
/* 130:    */     }
/* 131:122 */     return b;
/* 132:    */   }
/* 133:    */   
/* 134:    */   public int readBytes(byte[] b)
/* 135:    */     throws IOException
/* 136:    */   {
/* 137:127 */     return this.in.read(b);
/* 138:    */   }
/* 139:    */   
/* 140:    */   public int readBytes(byte[] b, int offset, int length)
/* 141:    */     throws IOException
/* 142:    */   {
/* 143:132 */     return this.in.read(b, offset, length);
/* 144:    */   }
/* 145:    */   
/* 146:    */   public String readString()
/* 147:    */     throws IOException
/* 148:    */   {
/* 149:137 */     int length = readVarInt();
/* 150:138 */     byte[] bytes = readBytes(length);
/* 151:139 */     return new String(bytes, "UTF-8");
/* 152:    */   }
/* 153:    */   
/* 154:    */   public int available()
/* 155:    */     throws IOException
/* 156:    */   {
/* 157:144 */     return this.in.available();
/* 158:    */   }
/* 159:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.packetlib.io.stream.StreamNetInput
 * JD-Core Version:    0.7.0.1
 */