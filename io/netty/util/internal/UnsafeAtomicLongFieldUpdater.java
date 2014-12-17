/*  1:   */ package io.netty.util.internal;
/*  2:   */ 
/*  3:   */ import java.lang.reflect.Field;
/*  4:   */ import java.lang.reflect.Modifier;
/*  5:   */ import java.util.concurrent.atomic.AtomicLongFieldUpdater;
/*  6:   */ import sun.misc.Unsafe;
/*  7:   */ 
/*  8:   */ final class UnsafeAtomicLongFieldUpdater<T>
/*  9:   */   extends AtomicLongFieldUpdater<T>
/* 10:   */ {
/* 11:   */   private final long offset;
/* 12:   */   private final Unsafe unsafe;
/* 13:   */   
/* 14:   */   UnsafeAtomicLongFieldUpdater(Unsafe unsafe, Class<?> tClass, String fieldName)
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
/* 25:   */   public boolean compareAndSet(T obj, long expect, long update)
/* 26:   */   {
/* 27:39 */     return this.unsafe.compareAndSwapLong(obj, this.offset, expect, update);
/* 28:   */   }
/* 29:   */   
/* 30:   */   public boolean weakCompareAndSet(T obj, long expect, long update)
/* 31:   */   {
/* 32:44 */     return this.unsafe.compareAndSwapLong(obj, this.offset, expect, update);
/* 33:   */   }
/* 34:   */   
/* 35:   */   public void set(T obj, long newValue)
/* 36:   */   {
/* 37:49 */     this.unsafe.putLongVolatile(obj, this.offset, newValue);
/* 38:   */   }
/* 39:   */   
/* 40:   */   public void lazySet(T obj, long newValue)
/* 41:   */   {
/* 42:54 */     this.unsafe.putOrderedLong(obj, this.offset, newValue);
/* 43:   */   }
/* 44:   */   
/* 45:   */   public long get(T obj)
/* 46:   */   {
/* 47:59 */     return this.unsafe.getLongVolatile(obj, this.offset);
/* 48:   */   }
/* 49:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.UnsafeAtomicLongFieldUpdater
 * JD-Core Version:    0.7.0.1
 */