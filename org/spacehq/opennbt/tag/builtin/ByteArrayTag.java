/*  1:   */ package org.spacehq.opennbt.tag.builtin;
/*  2:   */ 
/*  3:   */ import java.io.DataInputStream;
/*  4:   */ import java.io.DataOutputStream;
/*  5:   */ import java.io.IOException;
/*  6:   */ 
/*  7:   */ public class ByteArrayTag
/*  8:   */   extends Tag
/*  9:   */ {
/* 10:   */   private byte[] value;
/* 11:   */   
/* 12:   */   public ByteArrayTag(String name)
/* 13:   */   {
/* 14:20 */     this(name, new byte[0]);
/* 15:   */   }
/* 16:   */   
/* 17:   */   public ByteArrayTag(String name, byte[] value)
/* 18:   */   {
/* 19:30 */     super(name);
/* 20:31 */     this.value = value;
/* 21:   */   }
/* 22:   */   
/* 23:   */   public byte[] getValue()
/* 24:   */   {
/* 25:36 */     return (byte[])this.value.clone();
/* 26:   */   }
/* 27:   */   
/* 28:   */   public void setValue(byte[] value)
/* 29:   */   {
/* 30:45 */     if (value == null) {
/* 31:46 */       return;
/* 32:   */     }
/* 33:49 */     this.value = ((byte[])value.clone());
/* 34:   */   }
/* 35:   */   
/* 36:   */   public byte getValue(int index)
/* 37:   */   {
/* 38:59 */     return this.value[index];
/* 39:   */   }
/* 40:   */   
/* 41:   */   public void setValue(int index, byte value)
/* 42:   */   {
/* 43:69 */     this.value[index] = value;
/* 44:   */   }
/* 45:   */   
/* 46:   */   public int length()
/* 47:   */   {
/* 48:78 */     return this.value.length;
/* 49:   */   }
/* 50:   */   
/* 51:   */   public void read(DataInputStream in)
/* 52:   */     throws IOException
/* 53:   */   {
/* 54:83 */     this.value = new byte[in.readInt()];
/* 55:84 */     in.readFully(this.value);
/* 56:   */   }
/* 57:   */   
/* 58:   */   public void write(DataOutputStream out)
/* 59:   */     throws IOException
/* 60:   */   {
/* 61:89 */     out.writeInt(this.value.length);
/* 62:90 */     out.write(this.value);
/* 63:   */   }
/* 64:   */   
/* 65:   */   public ByteArrayTag clone()
/* 66:   */   {
/* 67:95 */     return new ByteArrayTag(getName(), getValue());
/* 68:   */   }
/* 69:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.tag.builtin.ByteArrayTag
 * JD-Core Version:    0.7.0.1
 */