/*   1:    */ package org.spacehq.opennbt.tag.builtin.custom;
/*   2:    */ 
/*   3:    */ import java.io.DataInputStream;
/*   4:    */ import java.io.DataOutputStream;
/*   5:    */ import java.io.IOException;
/*   6:    */ import org.spacehq.opennbt.tag.builtin.Tag;
/*   7:    */ 
/*   8:    */ public class DoubleArrayTag
/*   9:    */   extends Tag
/*  10:    */ {
/*  11:    */   private double[] value;
/*  12:    */   
/*  13:    */   public DoubleArrayTag(String name)
/*  14:    */   {
/*  15: 22 */     this(name, new double[0]);
/*  16:    */   }
/*  17:    */   
/*  18:    */   public DoubleArrayTag(String name, double[] value)
/*  19:    */   {
/*  20: 32 */     super(name);
/*  21: 33 */     this.value = value;
/*  22:    */   }
/*  23:    */   
/*  24:    */   public double[] getValue()
/*  25:    */   {
/*  26: 38 */     return (double[])this.value.clone();
/*  27:    */   }
/*  28:    */   
/*  29:    */   public void setValue(double[] value)
/*  30:    */   {
/*  31: 47 */     if (value == null) {
/*  32: 48 */       return;
/*  33:    */     }
/*  34: 51 */     this.value = ((double[])value.clone());
/*  35:    */   }
/*  36:    */   
/*  37:    */   public double getValue(int index)
/*  38:    */   {
/*  39: 61 */     return this.value[index];
/*  40:    */   }
/*  41:    */   
/*  42:    */   public void setValue(int index, double value)
/*  43:    */   {
/*  44: 71 */     this.value[index] = value;
/*  45:    */   }
/*  46:    */   
/*  47:    */   public int length()
/*  48:    */   {
/*  49: 80 */     return this.value.length;
/*  50:    */   }
/*  51:    */   
/*  52:    */   public void read(DataInputStream in)
/*  53:    */     throws IOException
/*  54:    */   {
/*  55: 85 */     this.value = new double[in.readInt()];
/*  56: 86 */     for (int index = 0; index < this.value.length; index++) {
/*  57: 87 */       this.value[index] = in.readDouble();
/*  58:    */     }
/*  59:    */   }
/*  60:    */   
/*  61:    */   public void write(DataOutputStream out)
/*  62:    */     throws IOException
/*  63:    */   {
/*  64: 93 */     out.writeInt(this.value.length);
/*  65: 94 */     for (int index = 0; index < this.value.length; index++) {
/*  66: 95 */       out.writeDouble(this.value[index]);
/*  67:    */     }
/*  68:    */   }
/*  69:    */   
/*  70:    */   public DoubleArrayTag clone()
/*  71:    */   {
/*  72:101 */     return new DoubleArrayTag(getName(), getValue());
/*  73:    */   }
/*  74:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.tag.builtin.custom.DoubleArrayTag
 * JD-Core Version:    0.7.0.1
 */