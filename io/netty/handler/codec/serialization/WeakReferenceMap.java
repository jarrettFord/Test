/*  1:   */ package io.netty.handler.codec.serialization;
/*  2:   */ 
/*  3:   */ import java.lang.ref.Reference;
/*  4:   */ import java.lang.ref.WeakReference;
/*  5:   */ import java.util.Map;
/*  6:   */ 
/*  7:   */ final class WeakReferenceMap<K, V>
/*  8:   */   extends ReferenceMap<K, V>
/*  9:   */ {
/* 10:   */   public WeakReferenceMap(Map<K, Reference<V>> delegate)
/* 11:   */   {
/* 12:25 */     super(delegate);
/* 13:   */   }
/* 14:   */   
/* 15:   */   Reference<V> fold(V value)
/* 16:   */   {
/* 17:30 */     return new WeakReference(value);
/* 18:   */   }
/* 19:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.serialization.WeakReferenceMap
 * JD-Core Version:    0.7.0.1
 */