/*  1:   */ package io.netty.util.internal;
/*  2:   */ 
/*  3:   */ import java.lang.reflect.Field;
/*  4:   */ import java.lang.reflect.Modifier;
/*  5:   */ import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
/*  6:   */ import sun.misc.Unsafe;
/*  7:   */ 
/*  8:   */ final class UnsafeAtomicReferenceFieldUpdater<U, M>
/*  9:   */   extends AtomicReferenceFieldUpdater<U, M>
/* 10:   */ {
/* 11:   */   private final long offset;
/* 12:   */   private final Unsafe unsafe;
/* 13:   */   
/* 14:   */   UnsafeAtomicReferenceFieldUpdater(Unsafe unsafe, Class<U> tClass, String fieldName)
/* 15:   */     throws NoSuchFieldException
/* 16:   */   {
/* 17:29 */     Field field = tClass.getDeclaredField(fieldName);
/* 18:30 */     if (!Modifier.isVolatile(field.getModifiers())) {
/* 19:31 */       throw new IllegalArgumentException("Must be volatile");
/* 20:   */     }
/* 21:33 */     this.unsafe = unsafe;
/* 22:34 */     this.offset = unsafe.objectFieldOffset(field);
/* 23:   */   }
/* 24:   */   
/* 25:   */   public boolean compareAndSet(U obj, M expect, M update)
/* 26:   */   {
/* 27:39 */     return this.unsafe.compareAndSwapObject(obj, this.offset, expect, update);
/* 28:   */   }
/* 29:   */   
/* 30:   */   public boolean weakCompareAndSet(U obj, M expect, M update)
/* 31:   */   {
/* 32:44 */     return this.unsafe.compareAndSwapObject(obj, this.offset, expect, update);
/* 33:   */   }
/* 34:   */   
/* 35:   */   public void set(U obj, M newValue)
/* 36:   */   {
/* 37:49 */     this.unsafe.putObjectVolatile(obj, this.offset, newValue);
/* 38:   */   }
/* 39:   */   
/* 40:   */   public void lazySet(U obj, M newValue)
/* 41:   */   {
/* 42:54 */     this.unsafe.putOrderedObject(obj, this.offset, newValue);
/* 43:   */   }
/* 44:   */   
/* 45:   */   public M get(U obj)
/* 46:   */   {
/* 47:60 */     return this.unsafe.getObjectVolatile(obj, this.offset);
/* 48:   */   }
/* 49:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.UnsafeAtomicReferenceFieldUpdater
 * JD-Core Version:    0.7.0.1
 */