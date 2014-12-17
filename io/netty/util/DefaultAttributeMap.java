/*   1:    */ package io.netty.util;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.PlatformDependent;
/*   4:    */ import java.util.concurrent.atomic.AtomicReference;
/*   5:    */ import java.util.concurrent.atomic.AtomicReferenceArray;
/*   6:    */ import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
/*   7:    */ 
/*   8:    */ public class DefaultAttributeMap
/*   9:    */   implements AttributeMap
/*  10:    */ {
/*  11:    */   private static final AtomicReferenceFieldUpdater<DefaultAttributeMap, AtomicReferenceArray> updater;
/*  12:    */   private static final int BUCKET_SIZE = 4;
/*  13:    */   private static final int MASK = 3;
/*  14:    */   private volatile AtomicReferenceArray<DefaultAttribute<?>> attributes;
/*  15:    */   
/*  16:    */   static
/*  17:    */   {
/*  18: 35 */     AtomicReferenceFieldUpdater<DefaultAttributeMap, AtomicReferenceArray> referenceFieldUpdater = PlatformDependent.newAtomicReferenceFieldUpdater(DefaultAttributeMap.class, "attributes");
/*  19: 37 */     if (referenceFieldUpdater == null) {
/*  20: 38 */       referenceFieldUpdater = AtomicReferenceFieldUpdater.newUpdater(DefaultAttributeMap.class, AtomicReferenceArray.class, "attributes");
/*  21:    */     }
/*  22: 41 */     updater = referenceFieldUpdater;
/*  23:    */   }
/*  24:    */   
/*  25:    */   public <T> Attribute<T> attr(AttributeKey<T> key)
/*  26:    */   {
/*  27: 54 */     if (key == null) {
/*  28: 55 */       throw new NullPointerException("key");
/*  29:    */     }
/*  30: 57 */     AtomicReferenceArray<DefaultAttribute<?>> attributes = this.attributes;
/*  31: 58 */     if (attributes == null)
/*  32:    */     {
/*  33: 60 */       attributes = new AtomicReferenceArray(4);
/*  34: 62 */       if (!updater.compareAndSet(this, null, attributes)) {
/*  35: 63 */         attributes = this.attributes;
/*  36:    */       }
/*  37:    */     }
/*  38: 67 */     int i = index(key);
/*  39: 68 */     DefaultAttribute<?> head = (DefaultAttribute)attributes.get(i);
/*  40: 69 */     if (head == null)
/*  41:    */     {
/*  42: 72 */       head = new DefaultAttribute(key);
/*  43: 73 */       if (attributes.compareAndSet(i, null, head)) {
/*  44: 75 */         return head;
/*  45:    */       }
/*  46: 77 */       head = (DefaultAttribute)attributes.get(i);
/*  47:    */     }
/*  48: 81 */     synchronized (head)
/*  49:    */     {
/*  50: 82 */       DefaultAttribute<?> curr = head;
/*  51: 84 */       if ((!curr.removed) && (curr.key == key)) {
/*  52: 85 */         return curr;
/*  53:    */       }
/*  54: 88 */       DefaultAttribute<?> next = curr.next;
/*  55: 89 */       if (next == null)
/*  56:    */       {
/*  57: 90 */         DefaultAttribute<T> attr = new DefaultAttribute(head, key);
/*  58: 91 */         curr.next = attr;
/*  59: 92 */         attr.prev = curr;
/*  60: 93 */         return attr;
/*  61:    */       }
/*  62: 95 */       curr = next;
/*  63:    */     }
/*  64:    */   }
/*  65:    */   
/*  66:    */   private static int index(AttributeKey<?> key)
/*  67:    */   {
/*  68:102 */     return key.id() & 0x3;
/*  69:    */   }
/*  70:    */   
/*  71:    */   private static final class DefaultAttribute<T>
/*  72:    */     extends AtomicReference<T>
/*  73:    */     implements Attribute<T>
/*  74:    */   {
/*  75:    */     private static final long serialVersionUID = -2661411462200283011L;
/*  76:    */     private final DefaultAttribute<?> head;
/*  77:    */     private final AttributeKey<T> key;
/*  78:    */     private DefaultAttribute<?> prev;
/*  79:    */     private DefaultAttribute<?> next;
/*  80:    */     private volatile boolean removed;
/*  81:    */     
/*  82:    */     DefaultAttribute(DefaultAttribute<?> head, AttributeKey<T> key)
/*  83:    */     {
/*  84:122 */       this.head = head;
/*  85:123 */       this.key = key;
/*  86:    */     }
/*  87:    */     
/*  88:    */     DefaultAttribute(AttributeKey<T> key)
/*  89:    */     {
/*  90:127 */       this.head = this;
/*  91:128 */       this.key = key;
/*  92:    */     }
/*  93:    */     
/*  94:    */     public AttributeKey<T> key()
/*  95:    */     {
/*  96:133 */       return this.key;
/*  97:    */     }
/*  98:    */     
/*  99:    */     public T setIfAbsent(T value)
/* 100:    */     {
/* 101:138 */       while (!compareAndSet(null, value))
/* 102:    */       {
/* 103:139 */         T old = get();
/* 104:140 */         if (old != null) {
/* 105:141 */           return old;
/* 106:    */         }
/* 107:    */       }
/* 108:144 */       return null;
/* 109:    */     }
/* 110:    */     
/* 111:    */     public T getAndRemove()
/* 112:    */     {
/* 113:149 */       this.removed = true;
/* 114:150 */       T oldValue = getAndSet(null);
/* 115:151 */       remove0();
/* 116:152 */       return oldValue;
/* 117:    */     }
/* 118:    */     
/* 119:    */     public void remove()
/* 120:    */     {
/* 121:157 */       this.removed = true;
/* 122:158 */       set(null);
/* 123:159 */       remove0();
/* 124:    */     }
/* 125:    */     
/* 126:    */     private void remove0()
/* 127:    */     {
/* 128:163 */       synchronized (this.head)
/* 129:    */       {
/* 130:169 */         if (this.prev != null)
/* 131:    */         {
/* 132:170 */           this.prev.next = this.next;
/* 133:172 */           if (this.next != null) {
/* 134:173 */             this.next.prev = this.prev;
/* 135:    */           }
/* 136:    */         }
/* 137:    */       }
/* 138:    */     }
/* 139:    */   }
/* 140:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.DefaultAttributeMap
 * JD-Core Version:    0.7.0.1
 */