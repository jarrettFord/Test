/*  1:   */ package com.google.gson.internal;
/*  2:   */ 
/*  3:   */ import java.io.ObjectInputStream;
/*  4:   */ import java.io.ObjectStreamClass;
/*  5:   */ import java.lang.reflect.Field;
/*  6:   */ import java.lang.reflect.Method;
/*  7:   */ 
/*  8:   */ public abstract class UnsafeAllocator
/*  9:   */ {
/* 10:   */   public abstract <T> T newInstance(Class<T> paramClass)
/* 11:   */     throws Exception;
/* 12:   */   
/* 13:   */   public static UnsafeAllocator create()
/* 14:   */   {
/* 15:   */     try
/* 16:   */     {
/* 17:39 */       Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
/* 18:40 */       Field f = unsafeClass.getDeclaredField("theUnsafe");
/* 19:41 */       f.setAccessible(true);
/* 20:42 */       final Object unsafe = f.get(null);
/* 21:43 */       Method allocateInstance = unsafeClass.getMethod("allocateInstance", new Class[] { Class.class });
/* 22:44 */       new UnsafeAllocator()
/* 23:   */       {
/* 24:   */         public <T> T newInstance(Class<T> c)
/* 25:   */           throws Exception
/* 26:   */         {
/* 27:48 */           return this.val$allocateInstance.invoke(unsafe, new Object[] { c });
/* 28:   */         }
/* 29:   */       };
/* 30:   */     }
/* 31:   */     catch (Exception ignored)
/* 32:   */     {
/* 33:   */       try
/* 34:   */       {
/* 35:60 */         Method newInstance = ObjectInputStream.class.getDeclaredMethod("newInstance", new Class[] { Class.class, Class.class });
/* 36:   */         
/* 37:62 */         newInstance.setAccessible(true);
/* 38:63 */         new UnsafeAllocator()
/* 39:   */         {
/* 40:   */           public <T> T newInstance(Class<T> c)
/* 41:   */             throws Exception
/* 42:   */           {
/* 43:67 */             return this.val$newInstance.invoke(null, new Object[] { c, Object.class });
/* 44:   */           }
/* 45:   */         };
/* 46:   */       }
/* 47:   */       catch (Exception ignored)
/* 48:   */       {
/* 49:   */         try
/* 50:   */         {
/* 51:79 */           Method getConstructorId = ObjectStreamClass.class.getDeclaredMethod("getConstructorId", new Class[] { Class.class });
/* 52:   */           
/* 53:81 */           getConstructorId.setAccessible(true);
/* 54:82 */           final int constructorId = ((Integer)getConstructorId.invoke(null, new Object[] { Object.class })).intValue();
/* 55:83 */           Method newInstance = ObjectStreamClass.class.getDeclaredMethod("newInstance", new Class[] { Class.class, Integer.TYPE });
/* 56:   */           
/* 57:85 */           newInstance.setAccessible(true);
/* 58:86 */           new UnsafeAllocator()
/* 59:   */           {
/* 60:   */             public <T> T newInstance(Class<T> c)
/* 61:   */               throws Exception
/* 62:   */             {
/* 63:90 */               return this.val$newInstance.invoke(null, new Object[] { c, Integer.valueOf(constructorId) });
/* 64:   */             }
/* 65:   */           };
/* 66:   */         }
/* 67:   */         catch (Exception ignored) {}
/* 68:   */       }
/* 69:   */     }
/* 70:97 */     new UnsafeAllocator()
/* 71:   */     {
/* 72:   */       public <T> T newInstance(Class<T> c)
/* 73:   */       {
/* 74::0 */         throw new UnsupportedOperationException("Cannot allocate " + c);
/* 75:   */       }
/* 76:   */     };
/* 77:   */   }
/* 78:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.internal.UnsafeAllocator
 * JD-Core Version:    0.7.0.1
 */