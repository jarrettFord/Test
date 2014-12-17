/*  1:   */ package org.spacehq.opennbt.tag.builtin;
/*  2:   */ 
/*  3:   */ import java.io.DataInputStream;
/*  4:   */ import java.io.DataOutputStream;
/*  5:   */ import java.io.IOException;
/*  6:   */ 
/*  7:   */ public class IntArrayTag
/*  8:   */   extends Tag
/*  9:   */ {
/* 10:   */   private int[] value;
/* 11:   */   
/* 12:   */   public IntArrayTag(String name)
/* 13:   */   {
/* 14:20 */     this(name, new int[0]);
/* 15:   */   }
/* 16:   */   
/* 17:   */   public IntArrayTag(String name, int[] value)
/* 18:   */   {
/* 19:30 */     super(name);
/* 20:31 */     this.value = value;
/* 21:   */   }
/* 22:   */   
/* 23:   */   public int[] getValue()
/* 24:   */   {
/* 25:36 */     return (int[])this.value.clone();
/* 26:   */   }
/* 27:   */   
/* 28:   */   public void setValue(int[] value)
/* 29:   */   {
/* 30:45 */     if (value == null) {
/* 31:46 */       return;
/* 32:   */     }
/* 33:49 */     this.value = ((int[])value.clone());
/* 34:   */   }
/* 35:   */   
/* 36:   */   public int getValue(int index)
/* 37:   */   {
/* 38:59 */     return this.value[index];
/* 39:   */   }
/* 40:   */   
/* 41:   */   public void setValue(int index, int value)
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
/* 54:83 */     this.value = new int[in.readInt()];
/* 55:84 */     for (int index = 0; index < this.value.length; index++) {
/* 56:85 */       this.value[index] = in.readInt();
/* 57:   */     }
/* 58:   */   }
/* 59:   */   
/* 60:   */   public void write(DataOutputStream out)
/* 61:   */     throws IOException
/* 62:   */   {
/* 63:91 */     out.writeInt(this.value.length);
/* 64:92 */     for (int index = 0; index < this.value.length; index++) {
/* 65:93 */       out.writeInt(this.value[index]);
/* 66:   */     }
/* 67:   */   }
/* 68:   */   
/* 69:   */   public IntArrayTag clone()
/* 70:   */   {
/* 71:99 */     return new IntArrayTag(getName(), getValue());
/* 72:   */   }
/* 73:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.tag.builtin.IntArrayTag
 * JD-Core Version:    0.7.0.1
 */