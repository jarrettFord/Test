/*   1:    */ package io.netty.util.concurrent;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.InternalThreadLocalMap;
/*   4:    */ import io.netty.util.internal.PlatformDependent;
/*   5:    */ import java.util.Collections;
/*   6:    */ import java.util.IdentityHashMap;
/*   7:    */ import java.util.Set;
/*   8:    */ 
/*   9:    */ public class FastThreadLocal<V>
/*  10:    */ {
/*  11: 46 */   private static final int variablesToRemoveIndex = ;
/*  12:    */   private final int index;
/*  13:    */   
/*  14:    */   public static void removeAll()
/*  15:    */   {
/*  16: 54 */     InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.getIfSet();
/*  17: 55 */     if (threadLocalMap == null) {
/*  18: 56 */       return;
/*  19:    */     }
/*  20:    */     try
/*  21:    */     {
/*  22: 60 */       Object v = threadLocalMap.indexedVariable(variablesToRemoveIndex);
/*  23: 61 */       if ((v != null) && (v != InternalThreadLocalMap.UNSET))
/*  24:    */       {
/*  25: 63 */         Set<FastThreadLocal<?>> variablesToRemove = (Set)v;
/*  26: 64 */         FastThreadLocal<?>[] variablesToRemoveArray = (FastThreadLocal[])variablesToRemove.toArray(new FastThreadLocal[variablesToRemove.size()]);
/*  27: 66 */         for (FastThreadLocal<?> tlv : variablesToRemoveArray) {
/*  28: 67 */           tlv.remove(threadLocalMap);
/*  29:    */         }
/*  30:    */       }
/*  31:    */     }
/*  32:    */     finally
/*  33:    */     {
/*  34: 71 */       InternalThreadLocalMap.remove();
/*  35:    */     }
/*  36:    */   }
/*  37:    */   
/*  38:    */   public static int size()
/*  39:    */   {
/*  40: 79 */     InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.getIfSet();
/*  41: 80 */     if (threadLocalMap == null) {
/*  42: 81 */       return 0;
/*  43:    */     }
/*  44: 83 */     return threadLocalMap.size();
/*  45:    */   }
/*  46:    */   
/*  47:    */   public static void destroy() {}
/*  48:    */   
/*  49:    */   private static void addToVariablesToRemove(InternalThreadLocalMap threadLocalMap, FastThreadLocal<?> variable)
/*  50:    */   {
/*  51: 99 */     Object v = threadLocalMap.indexedVariable(variablesToRemoveIndex);
/*  52:    */     Set<FastThreadLocal<?>> variablesToRemove;
/*  53:101 */     if ((v == InternalThreadLocalMap.UNSET) || (v == null))
/*  54:    */     {
/*  55:102 */       Set<FastThreadLocal<?>> variablesToRemove = Collections.newSetFromMap(new IdentityHashMap());
/*  56:103 */       threadLocalMap.setIndexedVariable(variablesToRemoveIndex, variablesToRemove);
/*  57:    */     }
/*  58:    */     else
/*  59:    */     {
/*  60:105 */       variablesToRemove = (Set)v;
/*  61:    */     }
/*  62:108 */     variablesToRemove.add(variable);
/*  63:    */   }
/*  64:    */   
/*  65:    */   private static void removeFromVariablesToRemove(InternalThreadLocalMap threadLocalMap, FastThreadLocal<?> variable)
/*  66:    */   {
/*  67:114 */     Object v = threadLocalMap.indexedVariable(variablesToRemoveIndex);
/*  68:116 */     if ((v == InternalThreadLocalMap.UNSET) || (v == null)) {
/*  69:117 */       return;
/*  70:    */     }
/*  71:121 */     Set<FastThreadLocal<?>> variablesToRemove = (Set)v;
/*  72:122 */     variablesToRemove.remove(variable);
/*  73:    */   }
/*  74:    */   
/*  75:    */   public FastThreadLocal()
/*  76:    */   {
/*  77:128 */     this.index = InternalThreadLocalMap.nextVariableIndex();
/*  78:    */   }
/*  79:    */   
/*  80:    */   public final V get()
/*  81:    */   {
/*  82:135 */     return get(InternalThreadLocalMap.get());
/*  83:    */   }
/*  84:    */   
/*  85:    */   public final V get(InternalThreadLocalMap threadLocalMap)
/*  86:    */   {
/*  87:144 */     Object v = threadLocalMap.indexedVariable(this.index);
/*  88:145 */     if (v != InternalThreadLocalMap.UNSET) {
/*  89:146 */       return v;
/*  90:    */     }
/*  91:149 */     return initialize(threadLocalMap);
/*  92:    */   }
/*  93:    */   
/*  94:    */   private V initialize(InternalThreadLocalMap threadLocalMap)
/*  95:    */   {
/*  96:153 */     V v = null;
/*  97:    */     try
/*  98:    */     {
/*  99:155 */       v = initialValue();
/* 100:    */     }
/* 101:    */     catch (Exception e)
/* 102:    */     {
/* 103:157 */       PlatformDependent.throwException(e);
/* 104:    */     }
/* 105:160 */     threadLocalMap.setIndexedVariable(this.index, v);
/* 106:161 */     addToVariablesToRemove(threadLocalMap, this);
/* 107:162 */     return v;
/* 108:    */   }
/* 109:    */   
/* 110:    */   public final void set(V value)
/* 111:    */   {
/* 112:169 */     if (value != InternalThreadLocalMap.UNSET) {
/* 113:170 */       set(InternalThreadLocalMap.get(), value);
/* 114:    */     } else {
/* 115:172 */       remove();
/* 116:    */     }
/* 117:    */   }
/* 118:    */   
/* 119:    */   public final void set(InternalThreadLocalMap threadLocalMap, V value)
/* 120:    */   {
/* 121:180 */     if (value != InternalThreadLocalMap.UNSET)
/* 122:    */     {
/* 123:181 */       if (threadLocalMap.setIndexedVariable(this.index, value)) {
/* 124:182 */         addToVariablesToRemove(threadLocalMap, this);
/* 125:    */       }
/* 126:    */     }
/* 127:    */     else {
/* 128:185 */       remove(threadLocalMap);
/* 129:    */     }
/* 130:    */   }
/* 131:    */   
/* 132:    */   public final boolean isSet()
/* 133:    */   {
/* 134:193 */     return isSet(InternalThreadLocalMap.getIfSet());
/* 135:    */   }
/* 136:    */   
/* 137:    */   public final boolean isSet(InternalThreadLocalMap threadLocalMap)
/* 138:    */   {
/* 139:201 */     return (threadLocalMap != null) && (threadLocalMap.isIndexedVariableSet(this.index));
/* 140:    */   }
/* 141:    */   
/* 142:    */   public final void remove()
/* 143:    */   {
/* 144:207 */     remove(InternalThreadLocalMap.getIfSet());
/* 145:    */   }
/* 146:    */   
/* 147:    */   public final void remove(InternalThreadLocalMap threadLocalMap)
/* 148:    */   {
/* 149:217 */     if (threadLocalMap == null) {
/* 150:218 */       return;
/* 151:    */     }
/* 152:221 */     Object v = threadLocalMap.removeIndexedVariable(this.index);
/* 153:222 */     removeFromVariablesToRemove(threadLocalMap, this);
/* 154:224 */     if (v != InternalThreadLocalMap.UNSET) {
/* 155:    */       try
/* 156:    */       {
/* 157:226 */         onRemoval(v);
/* 158:    */       }
/* 159:    */       catch (Exception e)
/* 160:    */       {
/* 161:228 */         PlatformDependent.throwException(e);
/* 162:    */       }
/* 163:    */     }
/* 164:    */   }
/* 165:    */   
/* 166:    */   protected V initialValue()
/* 167:    */     throws Exception
/* 168:    */   {
/* 169:237 */     return null;
/* 170:    */   }
/* 171:    */   
/* 172:    */   protected void onRemoval(V value)
/* 173:    */     throws Exception
/* 174:    */   {}
/* 175:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.concurrent.FastThreadLocal
 * JD-Core Version:    0.7.0.1
 */