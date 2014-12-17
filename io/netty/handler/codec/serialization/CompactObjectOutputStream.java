/*  1:   */ package io.netty.handler.codec.serialization;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import java.io.ObjectOutputStream;
/*  5:   */ import java.io.ObjectStreamClass;
/*  6:   */ import java.io.OutputStream;
/*  7:   */ 
/*  8:   */ class CompactObjectOutputStream
/*  9:   */   extends ObjectOutputStream
/* 10:   */ {
/* 11:   */   static final int TYPE_FAT_DESCRIPTOR = 0;
/* 12:   */   static final int TYPE_THIN_DESCRIPTOR = 1;
/* 13:   */   
/* 14:   */   CompactObjectOutputStream(OutputStream out)
/* 15:   */     throws IOException
/* 16:   */   {
/* 17:29 */     super(out);
/* 18:   */   }
/* 19:   */   
/* 20:   */   protected void writeStreamHeader()
/* 21:   */     throws IOException
/* 22:   */   {
/* 23:34 */     writeByte(5);
/* 24:   */   }
/* 25:   */   
/* 26:   */   protected void writeClassDescriptor(ObjectStreamClass desc)
/* 27:   */     throws IOException
/* 28:   */   {
/* 29:39 */     Class<?> clazz = desc.forClass();
/* 30:40 */     if ((clazz.isPrimitive()) || (clazz.isArray()) || (clazz.isInterface()) || (desc.getSerialVersionUID() == 0L))
/* 31:   */     {
/* 32:42 */       write(0);
/* 33:43 */       super.writeClassDescriptor(desc);
/* 34:   */     }
/* 35:   */     else
/* 36:   */     {
/* 37:45 */       write(1);
/* 38:46 */       writeUTF(desc.getName());
/* 39:   */     }
/* 40:   */   }
/* 41:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.serialization.CompactObjectOutputStream
 * JD-Core Version:    0.7.0.1
 */