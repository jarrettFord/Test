/*   1:    */ package io.netty.util.internal;
/*   2:    */ 
/*   3:    */ import io.netty.util.Recycler;
/*   4:    */ import io.netty.util.Recycler.Handle;
/*   5:    */ import java.util.ArrayList;
/*   6:    */ import java.util.Collection;
/*   7:    */ import java.util.List;
/*   8:    */ import java.util.RandomAccess;
/*   9:    */ 
/*  10:    */ public final class RecyclableArrayList
/*  11:    */   extends ArrayList<Object>
/*  12:    */ {
/*  13:    */   private static final long serialVersionUID = -8605125654176467947L;
/*  14:    */   private static final int DEFAULT_INITIAL_CAPACITY = 8;
/*  15: 36 */   private static final Recycler<RecyclableArrayList> RECYCLER = new Recycler()
/*  16:    */   {
/*  17:    */     protected RecyclableArrayList newObject(Recycler.Handle handle)
/*  18:    */     {
/*  19: 39 */       return new RecyclableArrayList(handle, null);
/*  20:    */     }
/*  21:    */   };
/*  22:    */   private final Recycler.Handle handle;
/*  23:    */   
/*  24:    */   public static RecyclableArrayList newInstance()
/*  25:    */   {
/*  26: 47 */     return newInstance(8);
/*  27:    */   }
/*  28:    */   
/*  29:    */   public static RecyclableArrayList newInstance(int minCapacity)
/*  30:    */   {
/*  31: 54 */     RecyclableArrayList ret = (RecyclableArrayList)RECYCLER.get();
/*  32: 55 */     ret.ensureCapacity(minCapacity);
/*  33: 56 */     return ret;
/*  34:    */   }
/*  35:    */   
/*  36:    */   private RecyclableArrayList(Recycler.Handle handle)
/*  37:    */   {
/*  38: 62 */     this(handle, 8);
/*  39:    */   }
/*  40:    */   
/*  41:    */   private RecyclableArrayList(Recycler.Handle handle, int initialCapacity)
/*  42:    */   {
/*  43: 66 */     super(initialCapacity);
/*  44: 67 */     this.handle = handle;
/*  45:    */   }
/*  46:    */   
/*  47:    */   public boolean addAll(Collection<?> c)
/*  48:    */   {
/*  49: 72 */     checkNullElements(c);
/*  50: 73 */     return super.addAll(c);
/*  51:    */   }
/*  52:    */   
/*  53:    */   public boolean addAll(int index, Collection<?> c)
/*  54:    */   {
/*  55: 78 */     checkNullElements(c);
/*  56: 79 */     return super.addAll(index, c);
/*  57:    */   }
/*  58:    */   
/*  59:    */   private static void checkNullElements(Collection<?> c)
/*  60:    */   {
/*  61: 83 */     if (((c instanceof RandomAccess)) && ((c instanceof List)))
/*  62:    */     {
/*  63: 85 */       List<?> list = (List)c;
/*  64: 86 */       int size = list.size();
/*  65: 87 */       for (int i = 0; i < size; i++) {
/*  66: 88 */         if (list.get(i) == null) {
/*  67: 89 */           throw new IllegalArgumentException("c contains null values");
/*  68:    */         }
/*  69:    */       }
/*  70:    */     }
/*  71:    */     else
/*  72:    */     {
/*  73: 93 */       for (Object element : c) {
/*  74: 94 */         if (element == null) {
/*  75: 95 */           throw new IllegalArgumentException("c contains null values");
/*  76:    */         }
/*  77:    */       }
/*  78:    */     }
/*  79:    */   }
/*  80:    */   
/*  81:    */   public boolean add(Object element)
/*  82:    */   {
/*  83:103 */     if (element == null) {
/*  84:104 */       throw new NullPointerException("element");
/*  85:    */     }
/*  86:106 */     return super.add(element);
/*  87:    */   }
/*  88:    */   
/*  89:    */   public void add(int index, Object element)
/*  90:    */   {
/*  91:111 */     if (element == null) {
/*  92:112 */       throw new NullPointerException("element");
/*  93:    */     }
/*  94:114 */     super.add(index, element);
/*  95:    */   }
/*  96:    */   
/*  97:    */   public Object set(int index, Object element)
/*  98:    */   {
/*  99:119 */     if (element == null) {
/* 100:120 */       throw new NullPointerException("element");
/* 101:    */     }
/* 102:122 */     return super.set(index, element);
/* 103:    */   }
/* 104:    */   
/* 105:    */   public boolean recycle()
/* 106:    */   {
/* 107:129 */     clear();
/* 108:130 */     return RECYCLER.recycle(this, this.handle);
/* 109:    */   }
/* 110:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.RecyclableArrayList
 * JD-Core Version:    0.7.0.1
 */