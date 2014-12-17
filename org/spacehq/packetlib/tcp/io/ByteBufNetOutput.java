/*  1:   */ package org.spacehq.packetlib.tcp.io;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import java.io.IOException;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ 
/*  7:   */ public class ByteBufNetOutput
/*  8:   */   implements NetOutput
/*  9:   */ {
/* 10:   */   private ByteBuf buf;
/* 11:   */   
/* 12:   */   public ByteBufNetOutput(ByteBuf buf)
/* 13:   */   {
/* 14:16 */     this.buf = buf;
/* 15:   */   }
/* 16:   */   
/* 17:   */   public void writeBoolean(boolean b)
/* 18:   */     throws IOException
/* 19:   */   {
/* 20:21 */     this.buf.writeBoolean(b);
/* 21:   */   }
/* 22:   */   
/* 23:   */   public void writeByte(int b)
/* 24:   */     throws IOException
/* 25:   */   {
/* 26:26 */     this.buf.writeByte(b);
/* 27:   */   }
/* 28:   */   
/* 29:   */   public void writeShort(int s)
/* 30:   */     throws IOException
/* 31:   */   {
/* 32:31 */     this.buf.writeShort(s);
/* 33:   */   }
/* 34:   */   
/* 35:   */   public void writeChar(int c)
/* 36:   */     throws IOException
/* 37:   */   {
/* 38:36 */     this.buf.writeChar(c);
/* 39:   */   }
/* 40:   */   
/* 41:   */   public void writeInt(int i)
/* 42:   */     throws IOException
/* 43:   */   {
/* 44:41 */     this.buf.writeInt(i);
/* 45:   */   }
/* 46:   */   
/* 47:   */   public void writeVarInt(int i)
/* 48:   */     throws IOException
/* 49:   */   {
/* 50:46 */     while ((i & 0xFFFFFF80) != 0L)
/* 51:   */     {
/* 52:47 */       writeByte(i & 0x7F | 0x80);
/* 53:48 */       i >>>= 7;
/* 54:   */     }
/* 55:51 */     writeByte(i & 0x7F);
/* 56:   */   }
/* 57:   */   
/* 58:   */   public void writeLong(long l)
/* 59:   */     throws IOException
/* 60:   */   {
/* 61:56 */     this.buf.writeLong(l);
/* 62:   */   }
/* 63:   */   
/* 64:   */   public void writeFloat(float f)
/* 65:   */     throws IOException
/* 66:   */   {
/* 67:61 */     this.buf.writeFloat(f);
/* 68:   */   }
/* 69:   */   
/* 70:   */   public void writeDouble(double d)
/* 71:   */     throws IOException
/* 72:   */   {
/* 73:66 */     this.buf.writeDouble(d);
/* 74:   */   }
/* 75:   */   
/* 76:   */   public void writePrefixedBytes(byte[] b)
/* 77:   */     throws IOException
/* 78:   */   {
/* 79:71 */     this.buf.writeShort(b.length);
/* 80:72 */     this.buf.writeBytes(b);
/* 81:   */   }
/* 82:   */   
/* 83:   */   public void writeBytes(byte[] b)
/* 84:   */     throws IOException
/* 85:   */   {
/* 86:77 */     this.buf.writeBytes(b);
/* 87:   */   }
/* 88:   */   
/* 89:   */   public void writeBytes(byte[] b, int length)
/* 90:   */     throws IOException
/* 91:   */   {
/* 92:82 */     this.buf.writeBytes(b, 0, length);
/* 93:   */   }
/* 94:   */   
/* 95:   */   public void writeString(String s)
/* 96:   */     throws IOException
/* 97:   */   {
/* 98:87 */     if (s == null) {
/* 99:88 */       throw new IllegalArgumentException("String cannot be null!");
/* :0:   */     }
/* :1:91 */     byte[] bytes = s.getBytes("UTF-8");
/* :2:92 */     if (bytes.length > 32767) {
/* :3:93 */       throw new IOException("String too big (was " + s.length() + " bytes encoded, max " + 32767 + ")");
/* :4:   */     }
/* :5:95 */     writeVarInt(bytes.length);
/* :6:96 */     writeBytes(bytes);
/* :7:   */   }
/* :8:   */   
/* :9:   */   public void flush()
/* ;0:   */     throws IOException
/* ;1:   */   {}
/* ;2:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.packetlib.tcp.io.ByteBufNetOutput
 * JD-Core Version:    0.7.0.1
 */