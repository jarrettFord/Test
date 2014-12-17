/*  1:   */ package org.spacehq.opennbt.tag.builtin;
/*  2:   */ 
/*  3:   */ import java.io.DataInputStream;
/*  4:   */ import java.io.DataOutputStream;
/*  5:   */ import java.io.IOException;
/*  6:   */ import org.spacehq.opennbt.NBTIO;
/*  7:   */ 
/*  8:   */ public class StringTag
/*  9:   */   extends Tag
/* 10:   */ {
/* 11:   */   private String value;
/* 12:   */   
/* 13:   */   public StringTag(String name)
/* 14:   */   {
/* 15:22 */     this(name, "");
/* 16:   */   }
/* 17:   */   
/* 18:   */   public StringTag(String name, String value)
/* 19:   */   {
/* 20:32 */     super(name);
/* 21:33 */     this.value = value;
/* 22:   */   }
/* 23:   */   
/* 24:   */   public String getValue()
/* 25:   */   {
/* 26:38 */     return this.value;
/* 27:   */   }
/* 28:   */   
/* 29:   */   public void setValue(String value)
/* 30:   */   {
/* 31:47 */     this.value = value;
/* 32:   */   }
/* 33:   */   
/* 34:   */   public void read(DataInputStream in)
/* 35:   */     throws IOException
/* 36:   */   {
/* 37:52 */     byte[] bytes = new byte[in.readShort()];
/* 38:53 */     in.readFully(bytes);
/* 39:54 */     this.value = new String(bytes, NBTIO.CHARSET);
/* 40:   */   }
/* 41:   */   
/* 42:   */   public void write(DataOutputStream out)
/* 43:   */     throws IOException
/* 44:   */   {
/* 45:59 */     byte[] bytes = this.value.getBytes(NBTIO.CHARSET);
/* 46:60 */     out.writeShort(bytes.length);
/* 47:61 */     out.write(bytes);
/* 48:   */   }
/* 49:   */   
/* 50:   */   public StringTag clone()
/* 51:   */   {
/* 52:66 */     return new StringTag(getName(), getValue());
/* 53:   */   }
/* 54:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.tag.builtin.StringTag
 * JD-Core Version:    0.7.0.1
 */