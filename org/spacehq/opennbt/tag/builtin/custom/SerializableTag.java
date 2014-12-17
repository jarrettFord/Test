/*  1:   */ package org.spacehq.opennbt.tag.builtin.custom;
/*  2:   */ 
/*  3:   */ import java.io.DataInputStream;
/*  4:   */ import java.io.DataOutputStream;
/*  5:   */ import java.io.IOException;
/*  6:   */ import java.io.ObjectInputStream;
/*  7:   */ import java.io.ObjectOutputStream;
/*  8:   */ import java.io.Serializable;
/*  9:   */ import org.spacehq.opennbt.tag.builtin.Tag;
/* 10:   */ 
/* 11:   */ public class SerializableTag
/* 12:   */   extends Tag
/* 13:   */ {
/* 14:   */   private Serializable value;
/* 15:   */   
/* 16:   */   public SerializableTag(String name)
/* 17:   */   {
/* 18:20 */     this(name, Integer.valueOf(0));
/* 19:   */   }
/* 20:   */   
/* 21:   */   public SerializableTag(String name, Serializable value)
/* 22:   */   {
/* 23:30 */     super(name);
/* 24:31 */     this.value = value;
/* 25:   */   }
/* 26:   */   
/* 27:   */   public Serializable getValue()
/* 28:   */   {
/* 29:36 */     return this.value;
/* 30:   */   }
/* 31:   */   
/* 32:   */   public void setValue(Serializable value)
/* 33:   */   {
/* 34:45 */     this.value = value;
/* 35:   */   }
/* 36:   */   
/* 37:   */   public void read(DataInputStream in)
/* 38:   */     throws IOException
/* 39:   */   {
/* 40:50 */     ObjectInputStream str = new ObjectInputStream(in);
/* 41:   */     try
/* 42:   */     {
/* 43:52 */       this.value = ((Serializable)str.readObject());
/* 44:   */     }
/* 45:   */     catch (ClassNotFoundException e)
/* 46:   */     {
/* 47:54 */       throw new IOException("Class not found while reading SerializableTag!", e);
/* 48:   */     }
/* 49:   */   }
/* 50:   */   
/* 51:   */   public void write(DataOutputStream out)
/* 52:   */     throws IOException
/* 53:   */   {
/* 54:60 */     ObjectOutputStream str = new ObjectOutputStream(out);
/* 55:61 */     str.writeObject(this.value);
/* 56:   */   }
/* 57:   */   
/* 58:   */   public SerializableTag clone()
/* 59:   */   {
/* 60:66 */     return new SerializableTag(getName(), getValue());
/* 61:   */   }
/* 62:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.tag.builtin.custom.SerializableTag
 * JD-Core Version:    0.7.0.1
 */