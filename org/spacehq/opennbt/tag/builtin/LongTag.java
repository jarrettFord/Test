/*  1:   */ package org.spacehq.opennbt.tag.builtin;
/*  2:   */ 
/*  3:   */ import java.io.DataInputStream;
/*  4:   */ import java.io.DataOutputStream;
/*  5:   */ import java.io.IOException;
/*  6:   */ 
/*  7:   */ public class LongTag
/*  8:   */   extends Tag
/*  9:   */ {
/* 10:   */   private long value;
/* 11:   */   
/* 12:   */   public LongTag(String name)
/* 13:   */   {
/* 14:20 */     this(name, 0L);
/* 15:   */   }
/* 16:   */   
/* 17:   */   public LongTag(String name, long value)
/* 18:   */   {
/* 19:30 */     super(name);
/* 20:31 */     this.value = value;
/* 21:   */   }
/* 22:   */   
/* 23:   */   public Long getValue()
/* 24:   */   {
/* 25:36 */     return Long.valueOf(this.value);
/* 26:   */   }
/* 27:   */   
/* 28:   */   public void setValue(long value)
/* 29:   */   {
/* 30:45 */     this.value = value;
/* 31:   */   }
/* 32:   */   
/* 33:   */   public void read(DataInputStream in)
/* 34:   */     throws IOException
/* 35:   */   {
/* 36:50 */     this.value = in.readLong();
/* 37:   */   }
/* 38:   */   
/* 39:   */   public void write(DataOutputStream out)
/* 40:   */     throws IOException
/* 41:   */   {
/* 42:55 */     out.writeLong(this.value);
/* 43:   */   }
/* 44:   */   
/* 45:   */   public LongTag clone()
/* 46:   */   {
/* 47:60 */     return new LongTag(getName(), getValue().longValue());
/* 48:   */   }
/* 49:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.tag.builtin.LongTag
 * JD-Core Version:    0.7.0.1
 */