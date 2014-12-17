/*   1:    */ package org.spacehq.packetlib.tcp.io;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import java.io.IOException;
/*   5:    */ import org.spacehq.packetlib.io.NetInput;
/*   6:    */ 
/*   7:    */ public class ByteBufNetInput
/*   8:    */   implements NetInput
/*   9:    */ {
/*  10:    */   private ByteBuf buf;
/*  11:    */   
/*  12:    */   public ByteBufNetInput(ByteBuf buf)
/*  13:    */   {
/*  14: 16 */     this.buf = buf;
/*  15:    */   }
/*  16:    */   
/*  17:    */   public boolean readBoolean()
/*  18:    */     throws IOException
/*  19:    */   {
/*  20: 21 */     return this.buf.readBoolean();
/*  21:    */   }
/*  22:    */   
/*  23:    */   public byte readByte()
/*  24:    */     throws IOException
/*  25:    */   {
/*  26: 26 */     return this.buf.readByte();
/*  27:    */   }
/*  28:    */   
/*  29:    */   public int readUnsignedByte()
/*  30:    */     throws IOException
/*  31:    */   {
/*  32: 31 */     return this.buf.readUnsignedByte();
/*  33:    */   }
/*  34:    */   
/*  35:    */   public short readShort()
/*  36:    */     throws IOException
/*  37:    */   {
/*  38: 36 */     return this.buf.readShort();
/*  39:    */   }
/*  40:    */   
/*  41:    */   public int readUnsignedShort()
/*  42:    */     throws IOException
/*  43:    */   {
/*  44: 41 */     return this.buf.readUnsignedShort();
/*  45:    */   }
/*  46:    */   
/*  47:    */   public char readChar()
/*  48:    */     throws IOException
/*  49:    */   {
/*  50: 46 */     return this.buf.readChar();
/*  51:    */   }
/*  52:    */   
/*  53:    */   public int readInt()
/*  54:    */     throws IOException
/*  55:    */   {
/*  56: 51 */     return this.buf.readInt();
/*  57:    */   }
/*  58:    */   
/*  59:    */   public int readVarInt()
/*  60:    */     throws IOException
/*  61:    */   {
/*  62: 56 */     int value = 0;
/*  63: 57 */     int size = 0;
/*  64:    */     int b;
/*  65: 59 */     while (((b = readByte()) & 0x80) != 0)
/*  66:    */     {
/*  67:    */       int b;
/*  68: 60 */       value |= (b & 0x7F) << size;
/*  69: 61 */       size += 7;
/*  70: 62 */       if (size > 35) {
/*  71: 63 */         throw new IOException("Variable length quantity is too long (must be <= 35)");
/*  72:    */       }
/*  73:    */     }
/*  74: 67 */     return value | b << size;
/*  75:    */   }
/*  76:    */   
/*  77:    */   public long readLong()
/*  78:    */     throws IOException
/*  79:    */   {
/*  80: 72 */     return this.buf.readLong();
/*  81:    */   }
/*  82:    */   
/*  83:    */   public float readFloat()
/*  84:    */     throws IOException
/*  85:    */   {
/*  86: 77 */     return this.buf.readFloat();
/*  87:    */   }
/*  88:    */   
/*  89:    */   public double readDouble()
/*  90:    */     throws IOException
/*  91:    */   {
/*  92: 82 */     return this.buf.readDouble();
/*  93:    */   }
/*  94:    */   
/*  95:    */   public byte[] readPrefixedBytes()
/*  96:    */     throws IOException
/*  97:    */   {
/*  98: 87 */     short length = this.buf.readShort();
/*  99: 88 */     return readBytes(length);
/* 100:    */   }
/* 101:    */   
/* 102:    */   public byte[] readBytes(int length)
/* 103:    */     throws IOException
/* 104:    */   {
/* 105: 93 */     if (length < 0) {
/* 106: 94 */       throw new IllegalArgumentException("Array cannot have length less than 0.");
/* 107:    */     }
/* 108: 97 */     byte[] b = new byte[length];
/* 109: 98 */     this.buf.readBytes(b);
/* 110: 99 */     return b;
/* 111:    */   }
/* 112:    */   
/* 113:    */   public int readBytes(byte[] b)
/* 114:    */     throws IOException
/* 115:    */   {
/* 116:104 */     return readBytes(b, 0, b.length);
/* 117:    */   }
/* 118:    */   
/* 119:    */   public int readBytes(byte[] b, int offset, int length)
/* 120:    */     throws IOException
/* 121:    */   {
/* 122:109 */     int readable = this.buf.readableBytes();
/* 123:110 */     if (readable <= 0) {
/* 124:111 */       return -1;
/* 125:    */     }
/* 126:114 */     if (readable < length) {
/* 127:115 */       length = readable;
/* 128:    */     }
/* 129:118 */     this.buf.readBytes(b, offset, length);
/* 130:119 */     return length;
/* 131:    */   }
/* 132:    */   
/* 133:    */   public String readString()
/* 134:    */     throws IOException
/* 135:    */   {
/* 136:124 */     int length = readVarInt();
/* 137:125 */     byte[] bytes = readBytes(length);
/* 138:126 */     return new String(bytes, "UTF-8");
/* 139:    */   }
/* 140:    */   
/* 141:    */   public int available()
/* 142:    */     throws IOException
/* 143:    */   {
/* 144:131 */     return this.buf.readableBytes();
/* 145:    */   }
/* 146:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.packetlib.tcp.io.ByteBufNetInput
 * JD-Core Version:    0.7.0.1
 */