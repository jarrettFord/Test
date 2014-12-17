/*   1:    */ package org.spacehq.opennbt.tag.builtin.custom;
/*   2:    */ 
/*   3:    */ import java.io.DataInputStream;
/*   4:    */ import java.io.DataOutputStream;
/*   5:    */ import java.io.IOException;
/*   6:    */ import org.spacehq.opennbt.NBTIO;
/*   7:    */ import org.spacehq.opennbt.tag.builtin.Tag;
/*   8:    */ 
/*   9:    */ public class StringArrayTag
/*  10:    */   extends Tag
/*  11:    */ {
/*  12:    */   private String[] value;
/*  13:    */   
/*  14:    */   public StringArrayTag(String name)
/*  15:    */   {
/*  16: 23 */     this(name, new String[0]);
/*  17:    */   }
/*  18:    */   
/*  19:    */   public StringArrayTag(String name, String[] value)
/*  20:    */   {
/*  21: 33 */     super(name);
/*  22: 34 */     this.value = value;
/*  23:    */   }
/*  24:    */   
/*  25:    */   public String[] getValue()
/*  26:    */   {
/*  27: 39 */     return (String[])this.value.clone();
/*  28:    */   }
/*  29:    */   
/*  30:    */   public void setValue(String[] value)
/*  31:    */   {
/*  32: 48 */     if (value == null) {
/*  33: 49 */       return;
/*  34:    */     }
/*  35: 52 */     this.value = ((String[])value.clone());
/*  36:    */   }
/*  37:    */   
/*  38:    */   public String getValue(int index)
/*  39:    */   {
/*  40: 62 */     return this.value[index];
/*  41:    */   }
/*  42:    */   
/*  43:    */   public void setValue(int index, String value)
/*  44:    */   {
/*  45: 72 */     this.value[index] = value;
/*  46:    */   }
/*  47:    */   
/*  48:    */   public int length()
/*  49:    */   {
/*  50: 81 */     return this.value.length;
/*  51:    */   }
/*  52:    */   
/*  53:    */   public void read(DataInputStream in)
/*  54:    */     throws IOException
/*  55:    */   {
/*  56: 86 */     this.value = new String[in.readInt()];
/*  57: 87 */     for (int index = 0; index < this.value.length; index++)
/*  58:    */     {
/*  59: 88 */       byte[] bytes = new byte[in.readShort()];
/*  60: 89 */       in.readFully(bytes);
/*  61: 90 */       this.value[index] = new String(bytes, NBTIO.CHARSET);
/*  62:    */     }
/*  63:    */   }
/*  64:    */   
/*  65:    */   public void write(DataOutputStream out)
/*  66:    */     throws IOException
/*  67:    */   {
/*  68: 96 */     out.writeInt(this.value.length);
/*  69: 97 */     for (int index = 0; index < this.value.length; index++)
/*  70:    */     {
/*  71: 98 */       byte[] bytes = this.value[index].getBytes(NBTIO.CHARSET);
/*  72: 99 */       out.writeShort(bytes.length);
/*  73:100 */       out.write(bytes);
/*  74:    */     }
/*  75:    */   }
/*  76:    */   
/*  77:    */   public StringArrayTag clone()
/*  78:    */   {
/*  79:106 */     return new StringArrayTag(getName(), getValue());
/*  80:    */   }
/*  81:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.tag.builtin.custom.StringArrayTag
 * JD-Core Version:    0.7.0.1
 */