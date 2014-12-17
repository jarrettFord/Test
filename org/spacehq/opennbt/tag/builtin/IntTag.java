/*  1:   */ package org.spacehq.opennbt.tag.builtin;
/*  2:   */ 
/*  3:   */ import java.io.DataInputStream;
/*  4:   */ import java.io.DataOutputStream;
/*  5:   */ import java.io.IOException;
/*  6:   */ 
/*  7:   */ public class IntTag
/*  8:   */   extends Tag
/*  9:   */ {
/* 10:   */   private int value;
/* 11:   */   
/* 12:   */   public IntTag(String name)
/* 13:   */   {
/* 14:20 */     this(name, 0);
/* 15:   */   }
/* 16:   */   
/* 17:   */   public IntTag(String name, int value)
/* 18:   */   {
/* 19:30 */     super(name);
/* 20:31 */     this.value = value;
/* 21:   */   }
/* 22:   */   
/* 23:   */   public Integer getValue()
/* 24:   */   {
/* 25:36 */     return Integer.valueOf(this.value);
/* 26:   */   }
/* 27:   */   
/* 28:   */   public void setValue(int value)
/* 29:   */   {
/* 30:45 */     this.value = value;
/* 31:   */   }
/* 32:   */   
/* 33:   */   public void read(DataInputStream in)
/* 34:   */     throws IOException
/* 35:   */   {
/* 36:50 */     this.value = in.readInt();
/* 37:   */   }
/* 38:   */   
/* 39:   */   public void write(DataOutputStream out)
/* 40:   */     throws IOException
/* 41:   */   {
/* 42:55 */     out.writeInt(this.value);
/* 43:   */   }
/* 44:   */   
/* 45:   */   public IntTag clone()
/* 46:   */   {
/* 47:60 */     return new IntTag(getName(), getValue().intValue());
/* 48:   */   }
/* 49:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.tag.builtin.IntTag
 * JD-Core Version:    0.7.0.1
 */