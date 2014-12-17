/*  1:   */ package org.spacehq.opennbt.tag.builtin;
/*  2:   */ 
/*  3:   */ import java.io.DataInputStream;
/*  4:   */ import java.io.DataOutputStream;
/*  5:   */ import java.io.IOException;
/*  6:   */ 
/*  7:   */ public class FloatTag
/*  8:   */   extends Tag
/*  9:   */ {
/* 10:   */   private float value;
/* 11:   */   
/* 12:   */   public FloatTag(String name)
/* 13:   */   {
/* 14:20 */     this(name, 0.0F);
/* 15:   */   }
/* 16:   */   
/* 17:   */   public FloatTag(String name, float value)
/* 18:   */   {
/* 19:30 */     super(name);
/* 20:31 */     this.value = value;
/* 21:   */   }
/* 22:   */   
/* 23:   */   public Float getValue()
/* 24:   */   {
/* 25:36 */     return Float.valueOf(this.value);
/* 26:   */   }
/* 27:   */   
/* 28:   */   public void setValue(float value)
/* 29:   */   {
/* 30:45 */     this.value = value;
/* 31:   */   }
/* 32:   */   
/* 33:   */   public void read(DataInputStream in)
/* 34:   */     throws IOException
/* 35:   */   {
/* 36:50 */     this.value = in.readFloat();
/* 37:   */   }
/* 38:   */   
/* 39:   */   public void write(DataOutputStream out)
/* 40:   */     throws IOException
/* 41:   */   {
/* 42:55 */     out.writeFloat(this.value);
/* 43:   */   }
/* 44:   */   
/* 45:   */   public FloatTag clone()
/* 46:   */   {
/* 47:60 */     return new FloatTag(getName(), getValue().floatValue());
/* 48:   */   }
/* 49:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.tag.builtin.FloatTag
 * JD-Core Version:    0.7.0.1
 */