/*   1:    */ package io.netty.util.internal.chmv8;
/*   2:    */ 
/*   3:    */ import java.lang.reflect.Field;
/*   4:    */ import java.security.AccessController;
/*   5:    */ import java.security.PrivilegedActionException;
/*   6:    */ import java.security.PrivilegedExceptionAction;
/*   7:    */ import sun.misc.Unsafe;
/*   8:    */ 
/*   9:    */ public abstract class CountedCompleter<T>
/*  10:    */   extends ForkJoinTask<T>
/*  11:    */ {
/*  12:    */   private static final long serialVersionUID = 5232453752276485070L;
/*  13:    */   final CountedCompleter<?> completer;
/*  14:    */   volatile int pending;
/*  15:    */   private static final Unsafe U;
/*  16:    */   private static final long PENDING;
/*  17:    */   
/*  18:    */   protected CountedCompleter(CountedCompleter<?> completer, int initialPendingCount)
/*  19:    */   {
/*  20:418 */     this.completer = completer;
/*  21:419 */     this.pending = initialPendingCount;
/*  22:    */   }
/*  23:    */   
/*  24:    */   protected CountedCompleter(CountedCompleter<?> completer)
/*  25:    */   {
/*  26:429 */     this.completer = completer;
/*  27:    */   }
/*  28:    */   
/*  29:    */   protected CountedCompleter()
/*  30:    */   {
/*  31:437 */     this.completer = null;
/*  32:    */   }
/*  33:    */   
/*  34:    */   public boolean onExceptionalCompletion(Throwable ex, CountedCompleter<?> caller)
/*  35:    */   {
/*  36:479 */     return true;
/*  37:    */   }
/*  38:    */   
/*  39:    */   public final CountedCompleter<?> getCompleter()
/*  40:    */   {
/*  41:489 */     return this.completer;
/*  42:    */   }
/*  43:    */   
/*  44:    */   public final int getPendingCount()
/*  45:    */   {
/*  46:498 */     return this.pending;
/*  47:    */   }
/*  48:    */   
/*  49:    */   public final void setPendingCount(int count)
/*  50:    */   {
/*  51:507 */     this.pending = count;
/*  52:    */   }
/*  53:    */   
/*  54:    */   public final void addToPendingCount(int delta)
/*  55:    */   {
/*  56:    */     int c;
/*  57:517 */     while (!U.compareAndSwapInt(this, PENDING, c = this.pending, c + delta)) {}
/*  58:    */   }
/*  59:    */   
/*  60:    */   public final boolean compareAndSetPendingCount(int expected, int count)
/*  61:    */   {
/*  62:529 */     return U.compareAndSwapInt(this, PENDING, expected, count);
/*  63:    */   }
/*  64:    */   
/*  65:    */   public final int decrementPendingCountUnlessZero()
/*  66:    */   {
/*  67:    */     int c;
/*  68:540 */     while (((c = this.pending) != 0) && (!U.compareAndSwapInt(this, PENDING, c, c - 1))) {}
/*  69:542 */     return c;
/*  70:    */   }
/*  71:    */   
/*  72:    */   public final CountedCompleter<?> getRoot()
/*  73:    */   {
/*  74:552 */     CountedCompleter<?> a = this;
/*  75:    */     CountedCompleter<?> p;
/*  76:553 */     while ((p = a.completer) != null) {
/*  77:554 */       a = p;
/*  78:    */     }
/*  79:555 */     return a;
/*  80:    */   }
/*  81:    */   
/*  82:    */   public final void tryComplete()
/*  83:    */   {
/*  84:565 */     CountedCompleter<?> a = this;CountedCompleter<?> s = a;
/*  85:    */     int c;
/*  86:    */     do
/*  87:    */     {
/*  88:567 */       while ((c = a.pending) == 0)
/*  89:    */       {
/*  90:568 */         a.onCompletion(s);
/*  91:569 */         if ((a = (s = a).completer) == null)
/*  92:    */         {
/*  93:570 */           s.quietlyComplete();
/*  94:571 */           return;
/*  95:    */         }
/*  96:    */       }
/*  97:574 */     } while (!U.compareAndSwapInt(a, PENDING, c, c - 1));
/*  98:    */   }
/*  99:    */   
/* 100:    */   public final void propagateCompletion()
/* 101:    */   {
/* 102:589 */     CountedCompleter<?> a = this;CountedCompleter<?> s = a;
/* 103:    */     int c;
/* 104:    */     do
/* 105:    */     {
/* 106:591 */       while ((c = a.pending) == 0) {
/* 107:592 */         if ((a = (s = a).completer) == null)
/* 108:    */         {
/* 109:593 */           s.quietlyComplete();
/* 110:594 */           return;
/* 111:    */         }
/* 112:    */       }
/* 113:597 */     } while (!U.compareAndSwapInt(a, PENDING, c, c - 1));
/* 114:    */   }
/* 115:    */   
/* 116:    */   public void complete(T rawResult)
/* 117:    */   {
/* 118:623 */     setRawResult(rawResult);
/* 119:624 */     onCompletion(this);
/* 120:625 */     quietlyComplete();
/* 121:    */     CountedCompleter<?> p;
/* 122:626 */     if ((p = this.completer) != null) {
/* 123:627 */       p.tryComplete();
/* 124:    */     }
/* 125:    */   }
/* 126:    */   
/* 127:    */   public final CountedCompleter<?> firstComplete()
/* 128:    */   {
/* 129:    */     int c;
/* 130:    */     do
/* 131:    */     {
/* 132:641 */       if ((c = this.pending) == 0) {
/* 133:642 */         return this;
/* 134:    */       }
/* 135:643 */     } while (!U.compareAndSwapInt(this, PENDING, c, c - 1));
/* 136:644 */     return null;
/* 137:    */   }
/* 138:    */   
/* 139:    */   public final CountedCompleter<?> nextComplete()
/* 140:    */   {
/* 141:    */     CountedCompleter<?> p;
/* 142:667 */     if ((p = this.completer) != null) {
/* 143:668 */       return p.firstComplete();
/* 144:    */     }
/* 145:670 */     quietlyComplete();
/* 146:671 */     return null;
/* 147:    */   }
/* 148:    */   
/* 149:    */   public final void quietlyCompleteRoot()
/* 150:    */   {
/* 151:679 */     CountedCompleter<?> a = this;
/* 152:    */     for (;;)
/* 153:    */     {
/* 154:    */       CountedCompleter<?> p;
/* 155:680 */       if ((p = a.completer) == null)
/* 156:    */       {
/* 157:681 */         a.quietlyComplete();
/* 158:682 */         return;
/* 159:    */       }
/* 160:684 */       a = p;
/* 161:    */     }
/* 162:    */   }
/* 163:    */   
/* 164:    */   void internalPropagateException(Throwable ex)
/* 165:    */   {
/* 166:692 */     CountedCompleter<?> a = this;CountedCompleter<?> s = a;
/* 167:694 */     while ((a.onExceptionalCompletion(ex, s)) && ((a = (s = a).completer) != null) && (a.status >= 0) && (a.recordExceptionalCompletion(ex) == -2147483648)) {}
/* 168:    */   }
/* 169:    */   
/* 170:    */   protected final boolean exec()
/* 171:    */   {
/* 172:703 */     compute();
/* 173:704 */     return false;
/* 174:    */   }
/* 175:    */   
/* 176:    */   public T getRawResult()
/* 177:    */   {
/* 178:716 */     return null;
/* 179:    */   }
/* 180:    */   
/* 181:    */   static
/* 182:    */   {
/* 183:    */     try
/* 184:    */     {
/* 185:732 */       U = getUnsafe();
/* 186:733 */       PENDING = U.objectFieldOffset(CountedCompleter.class.getDeclaredField("pending"));
/* 187:    */     }
/* 188:    */     catch (Exception e)
/* 189:    */     {
/* 190:736 */       throw new Error(e);
/* 191:    */     }
/* 192:    */   }
/* 193:    */   
/* 194:    */   private static Unsafe getUnsafe()
/* 195:    */   {
/* 196:    */     try
/* 197:    */     {
/* 198:749 */       return Unsafe.getUnsafe();
/* 199:    */     }
/* 200:    */     catch (SecurityException tryReflectionInstead)
/* 201:    */     {
/* 202:    */       try
/* 203:    */       {
/* 204:752 */         (Unsafe)AccessController.doPrivileged(new PrivilegedExceptionAction()
/* 205:    */         {
/* 206:    */           public Unsafe run()
/* 207:    */             throws Exception
/* 208:    */           {
/* 209:755 */             Class<Unsafe> k = Unsafe.class;
/* 210:756 */             for (Field f : k.getDeclaredFields())
/* 211:    */             {
/* 212:757 */               f.setAccessible(true);
/* 213:758 */               Object x = f.get(null);
/* 214:759 */               if (k.isInstance(x)) {
/* 215:760 */                 return (Unsafe)k.cast(x);
/* 216:    */               }
/* 217:    */             }
/* 218:762 */             throw new NoSuchFieldError("the Unsafe");
/* 219:    */           }
/* 220:    */         });
/* 221:    */       }
/* 222:    */       catch (PrivilegedActionException e)
/* 223:    */       {
/* 224:765 */         throw new RuntimeException("Could not initialize intrinsics", e.getCause());
/* 225:    */       }
/* 226:    */     }
/* 227:    */   }
/* 228:    */   
/* 229:    */   public abstract void compute();
/* 230:    */   
/* 231:    */   public void onCompletion(CountedCompleter<?> caller) {}
/* 232:    */   
/* 233:    */   protected void setRawResult(T t) {}
/* 234:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.chmv8.CountedCompleter
 * JD-Core Version:    0.7.0.1
 */