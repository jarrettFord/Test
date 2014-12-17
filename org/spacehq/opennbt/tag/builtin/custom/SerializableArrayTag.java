/*   1:    */ package org.spacehq.opennbt.tag.builtin.custom;
/*   2:    */ 
/*   3:    */ import java.io.DataInputStream;
/*   4:    */ import java.io.DataOutputStream;
/*   5:    */ import java.io.IOException;
/*   6:    */ import java.io.ObjectInputStream;
/*   7:    */ import java.io.ObjectOutputStream;
/*   8:    */ import java.io.Serializable;
/*   9:    */ import org.spacehq.opennbt.tag.builtin.Tag;
/*  10:    */ 
/*  11:    */ public class SerializableArrayTag
/*  12:    */   extends Tag
/*  13:    */ {
/*  14:    */   private Serializable[] value;
/*  15:    */   
/*  16:    */   public SerializableArrayTag(String name)
/*  17:    */   {
/*  18: 20 */     this(name, new Serializable[0]);
/*  19:    */   }
/*  20:    */   
/*  21:    */   public SerializableArrayTag(String name, Serializable[] value)
/*  22:    */   {
/*  23: 30 */     super(name);
/*  24: 31 */     this.value = value;
/*  25:    */   }
/*  26:    */   
/*  27:    */   public Serializable[] getValue()
/*  28:    */   {
/*  29: 36 */     return (Serializable[])this.value.clone();
/*  30:    */   }
/*  31:    */   
/*  32:    */   public void setValue(Serializable[] value)
/*  33:    */   {
/*  34: 45 */     if (value == null) {
/*  35: 46 */       return;
/*  36:    */     }
/*  37: 49 */     this.value = ((Serializable[])value.clone());
/*  38:    */   }
/*  39:    */   
/*  40:    */   public Serializable getValue(int index)
/*  41:    */   {
/*  42: 59 */     return this.value[index];
/*  43:    */   }
/*  44:    */   
/*  45:    */   public void setValue(int index, Serializable value)
/*  46:    */   {
/*  47: 69 */     this.value[index] = value;
/*  48:    */   }
/*  49:    */   
/*  50:    */   public int length()
/*  51:    */   {
/*  52: 78 */     return this.value.length;
/*  53:    */   }
/*  54:    */   
/*  55:    */   public void read(DataInputStream in)
/*  56:    */     throws IOException
/*  57:    */   {
/*  58: 83 */     this.value = new Serializable[in.readInt()];
/*  59: 84 */     ObjectInputStream str = new ObjectInputStream(in);
/*  60: 85 */     for (int index = 0; index < this.value.length; index++) {
/*  61:    */       try
/*  62:    */       {
/*  63: 87 */         this.value[index] = ((Serializable)str.readObject());
/*  64:    */       }
/*  65:    */       catch (ClassNotFoundException e)
/*  66:    */       {
/*  67: 89 */         throw new IOException("Class not found while reading SerializableArrayTag!", e);
/*  68:    */       }
/*  69:    */     }
/*  70:    */   }
/*  71:    */   
/*  72:    */   public void write(DataOutputStream out)
/*  73:    */     throws IOException
/*  74:    */   {
/*  75: 96 */     out.writeInt(this.value.length);
/*  76: 97 */     ObjectOutputStream str = new ObjectOutputStream(out);
/*  77: 98 */     for (int index = 0; index < this.value.length; index++) {
/*  78: 99 */       str.writeObject(this.value[index]);
/*  79:    */     }
/*  80:    */   }
/*  81:    */   
/*  82:    */   public SerializableArrayTag clone()
/*  83:    */   {
/*  84:105 */     return new SerializableArrayTag(getName(), getValue());
/*  85:    */   }
/*  86:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.tag.builtin.custom.SerializableArrayTag
 * JD-Core Version:    0.7.0.1
 */