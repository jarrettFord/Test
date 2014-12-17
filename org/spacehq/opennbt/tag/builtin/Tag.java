/*   1:    */ package org.spacehq.opennbt.tag.builtin;
/*   2:    */ 
/*   3:    */ import java.io.DataInputStream;
/*   4:    */ import java.io.DataOutputStream;
/*   5:    */ import java.io.IOException;
/*   6:    */ import java.lang.reflect.Array;
/*   7:    */ 
/*   8:    */ public abstract class Tag
/*   9:    */   implements Cloneable
/*  10:    */ {
/*  11:    */   private String name;
/*  12:    */   
/*  13:    */   public Tag(String name)
/*  14:    */   {
/*  15: 24 */     this.name = name;
/*  16:    */   }
/*  17:    */   
/*  18:    */   public final String getName()
/*  19:    */   {
/*  20: 33 */     return this.name;
/*  21:    */   }
/*  22:    */   
/*  23:    */   public abstract Object getValue();
/*  24:    */   
/*  25:    */   public abstract void read(DataInputStream paramDataInputStream)
/*  26:    */     throws IOException;
/*  27:    */   
/*  28:    */   public abstract void write(DataOutputStream paramDataOutputStream)
/*  29:    */     throws IOException;
/*  30:    */   
/*  31:    */   public abstract Tag clone();
/*  32:    */   
/*  33:    */   public boolean equals(Object obj)
/*  34:    */   {
/*  35: 64 */     if (!(obj instanceof Tag)) {
/*  36: 65 */       return false;
/*  37:    */     }
/*  38: 68 */     Tag tag = (Tag)obj;
/*  39: 69 */     if (!getName().equals(tag.getName())) {
/*  40: 70 */       return false;
/*  41:    */     }
/*  42: 73 */     if (getValue() == null) {
/*  43: 74 */       return tag.getValue() == null;
/*  44:    */     }
/*  45: 75 */     if (tag.getValue() == null) {
/*  46: 76 */       return false;
/*  47:    */     }
/*  48: 79 */     if ((getValue().getClass().isArray()) && (tag.getValue().getClass().isArray()))
/*  49:    */     {
/*  50: 80 */       int length = Array.getLength(getValue());
/*  51: 81 */       if (Array.getLength(tag.getValue()) != length) {
/*  52: 82 */         return false;
/*  53:    */       }
/*  54: 85 */       for (int index = 0; index < length; index++)
/*  55:    */       {
/*  56: 86 */         Object o = Array.get(getValue(), index);
/*  57: 87 */         Object other = Array.get(tag.getValue(), index);
/*  58: 88 */         if (((o == null) && (other != null)) || ((o != null) && (!o.equals(other)))) {
/*  59: 89 */           return false;
/*  60:    */         }
/*  61:    */       }
/*  62: 93 */       return true;
/*  63:    */     }
/*  64: 96 */     return getValue().equals(tag.getValue());
/*  65:    */   }
/*  66:    */   
/*  67:    */   public String toString()
/*  68:    */   {
/*  69:101 */     String name = (getName() != null) && (!getName().equals("")) ? "(" + getName() + ")" : "";
/*  70:102 */     String value = "";
/*  71:103 */     if (getValue() != null)
/*  72:    */     {
/*  73:104 */       value = getValue().toString();
/*  74:105 */       if (getValue().getClass().isArray())
/*  75:    */       {
/*  76:106 */         StringBuilder build = new StringBuilder();
/*  77:107 */         build.append("[");
/*  78:108 */         for (int index = 0; index < Array.getLength(getValue()); index++)
/*  79:    */         {
/*  80:109 */           if (index > 0) {
/*  81:110 */             build.append(", ");
/*  82:    */           }
/*  83:113 */           build.append(Array.get(getValue(), index));
/*  84:    */         }
/*  85:116 */         build.append("]");
/*  86:117 */         value = build.toString();
/*  87:    */       }
/*  88:    */     }
/*  89:121 */     return getClass().getSimpleName() + name + " { " + value + " }";
/*  90:    */   }
/*  91:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.tag.builtin.Tag
 * JD-Core Version:    0.7.0.1
 */