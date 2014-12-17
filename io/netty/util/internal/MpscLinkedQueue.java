/*   1:    */ package io.netty.util.internal;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import java.io.ObjectInputStream;
/*   5:    */ import java.io.ObjectOutputStream;
/*   6:    */ import java.lang.reflect.Array;
/*   7:    */ import java.util.Arrays;
/*   8:    */ import java.util.Collection;
/*   9:    */ import java.util.Iterator;
/*  10:    */ import java.util.NoSuchElementException;
/*  11:    */ import java.util.Queue;
/*  12:    */ 
/*  13:    */ final class MpscLinkedQueue<E>
/*  14:    */   extends MpscLinkedQueueTailRef<E>
/*  15:    */   implements Queue<E>
/*  16:    */ {
/*  17:    */   private static final long serialVersionUID = -1878402552271506449L;
/*  18:    */   long p00;
/*  19:    */   long p01;
/*  20:    */   long p02;
/*  21:    */   long p03;
/*  22:    */   long p04;
/*  23:    */   long p05;
/*  24:    */   long p06;
/*  25:    */   long p07;
/*  26:    */   long p30;
/*  27:    */   long p31;
/*  28:    */   long p32;
/*  29:    */   long p33;
/*  30:    */   long p34;
/*  31:    */   long p35;
/*  32:    */   long p36;
/*  33:    */   long p37;
/*  34:    */   
/*  35:    */   MpscLinkedQueue()
/*  36:    */   {
/*  37: 91 */     MpscLinkedQueueNode<E> tombstone = new DefaultNode(null);
/*  38: 92 */     setHeadRef(tombstone);
/*  39: 93 */     setTailRef(tombstone);
/*  40:    */   }
/*  41:    */   
/*  42:    */   private MpscLinkedQueueNode<E> peekNode()
/*  43:    */   {
/*  44:    */     for (;;)
/*  45:    */     {
/*  46:101 */       MpscLinkedQueueNode<E> head = headRef();
/*  47:102 */       MpscLinkedQueueNode<E> next = head.next();
/*  48:103 */       if (next != null) {
/*  49:104 */         return next;
/*  50:    */       }
/*  51:106 */       if (head == tailRef()) {
/*  52:107 */         return null;
/*  53:    */       }
/*  54:    */     }
/*  55:    */   }
/*  56:    */   
/*  57:    */   public boolean offer(E value)
/*  58:    */   {
/*  59:120 */     if (value == null) {
/*  60:121 */       throw new NullPointerException("value");
/*  61:    */     }
/*  62:    */     MpscLinkedQueueNode<E> newTail;
/*  63:125 */     if ((value instanceof MpscLinkedQueueNode))
/*  64:    */     {
/*  65:126 */       MpscLinkedQueueNode<E> newTail = (MpscLinkedQueueNode)value;
/*  66:127 */       newTail.setNext(null);
/*  67:    */     }
/*  68:    */     else
/*  69:    */     {
/*  70:129 */       newTail = new DefaultNode(value);
/*  71:    */     }
/*  72:132 */     MpscLinkedQueueNode<E> oldTail = getAndSetTailRef(newTail);
/*  73:133 */     oldTail.setNext(newTail);
/*  74:134 */     return true;
/*  75:    */   }
/*  76:    */   
/*  77:    */   public E poll()
/*  78:    */   {
/*  79:139 */     MpscLinkedQueueNode<E> next = peekNode();
/*  80:140 */     if (next == null) {
/*  81:141 */       return null;
/*  82:    */     }
/*  83:145 */     MpscLinkedQueueNode<E> oldHead = headRef();
/*  84:    */     
/*  85:    */ 
/*  86:    */ 
/*  87:149 */     lazySetHeadRef(next);
/*  88:    */     
/*  89:    */ 
/*  90:152 */     oldHead.unlink();
/*  91:    */     
/*  92:154 */     return next.clearMaybe();
/*  93:    */   }
/*  94:    */   
/*  95:    */   public E peek()
/*  96:    */   {
/*  97:159 */     MpscLinkedQueueNode<E> next = peekNode();
/*  98:160 */     if (next == null) {
/*  99:161 */       return null;
/* 100:    */     }
/* 101:163 */     return next.value();
/* 102:    */   }
/* 103:    */   
/* 104:    */   public int size()
/* 105:    */   {
/* 106:168 */     int count = 0;
/* 107:169 */     MpscLinkedQueueNode<E> n = peekNode();
/* 108:171 */     while (n != null)
/* 109:    */     {
/* 110:174 */       count++;
/* 111:175 */       n = n.next();
/* 112:    */     }
/* 113:177 */     return count;
/* 114:    */   }
/* 115:    */   
/* 116:    */   public boolean isEmpty()
/* 117:    */   {
/* 118:182 */     return peekNode() == null;
/* 119:    */   }
/* 120:    */   
/* 121:    */   public boolean contains(Object o)
/* 122:    */   {
/* 123:187 */     MpscLinkedQueueNode<E> n = peekNode();
/* 124:189 */     while (n != null)
/* 125:    */     {
/* 126:192 */       if (n.value() == o) {
/* 127:193 */         return true;
/* 128:    */       }
/* 129:195 */       n = n.next();
/* 130:    */     }
/* 131:197 */     return false;
/* 132:    */   }
/* 133:    */   
/* 134:    */   public Iterator<E> iterator()
/* 135:    */   {
/* 136:202 */     new Iterator()
/* 137:    */     {
/* 138:203 */       private MpscLinkedQueueNode<E> node = MpscLinkedQueue.this.peekNode();
/* 139:    */       
/* 140:    */       public boolean hasNext()
/* 141:    */       {
/* 142:207 */         return this.node != null;
/* 143:    */       }
/* 144:    */       
/* 145:    */       public E next()
/* 146:    */       {
/* 147:212 */         MpscLinkedQueueNode<E> node = this.node;
/* 148:213 */         if (node == null) {
/* 149:214 */           throw new NoSuchElementException();
/* 150:    */         }
/* 151:216 */         E value = node.value();
/* 152:217 */         this.node = node.next();
/* 153:218 */         return value;
/* 154:    */       }
/* 155:    */       
/* 156:    */       public void remove()
/* 157:    */       {
/* 158:223 */         throw new UnsupportedOperationException();
/* 159:    */       }
/* 160:    */     };
/* 161:    */   }
/* 162:    */   
/* 163:    */   public boolean add(E e)
/* 164:    */   {
/* 165:230 */     if (offer(e)) {
/* 166:231 */       return true;
/* 167:    */     }
/* 168:233 */     throw new IllegalStateException("queue full");
/* 169:    */   }
/* 170:    */   
/* 171:    */   public E remove()
/* 172:    */   {
/* 173:238 */     E e = poll();
/* 174:239 */     if (e != null) {
/* 175:240 */       return e;
/* 176:    */     }
/* 177:242 */     throw new NoSuchElementException();
/* 178:    */   }
/* 179:    */   
/* 180:    */   public E element()
/* 181:    */   {
/* 182:247 */     E e = peek();
/* 183:248 */     if (e != null) {
/* 184:249 */       return e;
/* 185:    */     }
/* 186:251 */     throw new NoSuchElementException();
/* 187:    */   }
/* 188:    */   
/* 189:    */   public Object[] toArray()
/* 190:    */   {
/* 191:256 */     Object[] array = new Object[size()];
/* 192:257 */     Iterator<E> it = iterator();
/* 193:258 */     for (int i = 0; i < array.length; i++) {
/* 194:259 */       if (it.hasNext()) {
/* 195:260 */         array[i] = it.next();
/* 196:    */       } else {
/* 197:262 */         return Arrays.copyOf(array, i);
/* 198:    */       }
/* 199:    */     }
/* 200:265 */     return array;
/* 201:    */   }
/* 202:    */   
/* 203:    */   public <T> T[] toArray(T[] a)
/* 204:    */   {
/* 205:271 */     int size = size();
/* 206:    */     T[] array;
/* 207:    */     T[] array;
/* 208:273 */     if (a.length >= size) {
/* 209:274 */       array = a;
/* 210:    */     } else {
/* 211:276 */       array = (Object[])Array.newInstance(a.getClass().getComponentType(), size);
/* 212:    */     }
/* 213:279 */     Iterator<E> it = iterator();
/* 214:280 */     for (int i = 0; i < array.length; i++) {
/* 215:281 */       if (it.hasNext())
/* 216:    */       {
/* 217:282 */         array[i] = it.next();
/* 218:    */       }
/* 219:    */       else
/* 220:    */       {
/* 221:284 */         if (a == array)
/* 222:    */         {
/* 223:285 */           array[i] = null;
/* 224:286 */           return array;
/* 225:    */         }
/* 226:289 */         if (a.length < i) {
/* 227:290 */           return Arrays.copyOf(array, i);
/* 228:    */         }
/* 229:293 */         System.arraycopy(array, 0, a, 0, i);
/* 230:294 */         if (a.length > i) {
/* 231:295 */           a[i] = null;
/* 232:    */         }
/* 233:297 */         return a;
/* 234:    */       }
/* 235:    */     }
/* 236:300 */     return array;
/* 237:    */   }
/* 238:    */   
/* 239:    */   public boolean remove(Object o)
/* 240:    */   {
/* 241:305 */     throw new UnsupportedOperationException();
/* 242:    */   }
/* 243:    */   
/* 244:    */   public boolean containsAll(Collection<?> c)
/* 245:    */   {
/* 246:310 */     for (Object e : c) {
/* 247:311 */       if (!contains(e)) {
/* 248:312 */         return false;
/* 249:    */       }
/* 250:    */     }
/* 251:315 */     return true;
/* 252:    */   }
/* 253:    */   
/* 254:    */   public boolean addAll(Collection<? extends E> c)
/* 255:    */   {
/* 256:320 */     if (c == null) {
/* 257:321 */       throw new NullPointerException("c");
/* 258:    */     }
/* 259:323 */     if (c == this) {
/* 260:324 */       throw new IllegalArgumentException("c == this");
/* 261:    */     }
/* 262:327 */     boolean modified = false;
/* 263:328 */     for (E e : c)
/* 264:    */     {
/* 265:329 */       add(e);
/* 266:330 */       modified = true;
/* 267:    */     }
/* 268:332 */     return modified;
/* 269:    */   }
/* 270:    */   
/* 271:    */   public boolean removeAll(Collection<?> c)
/* 272:    */   {
/* 273:337 */     throw new UnsupportedOperationException();
/* 274:    */   }
/* 275:    */   
/* 276:    */   public boolean retainAll(Collection<?> c)
/* 277:    */   {
/* 278:342 */     throw new UnsupportedOperationException();
/* 279:    */   }
/* 280:    */   
/* 281:    */   public void clear()
/* 282:    */   {
/* 283:347 */     while (poll() != null) {}
/* 284:    */   }
/* 285:    */   
/* 286:    */   private void writeObject(ObjectOutputStream out)
/* 287:    */     throws IOException
/* 288:    */   {
/* 289:353 */     out.defaultWriteObject();
/* 290:354 */     for (E e : this) {
/* 291:355 */       out.writeObject(e);
/* 292:    */     }
/* 293:357 */     out.writeObject(null);
/* 294:    */   }
/* 295:    */   
/* 296:    */   private void readObject(ObjectInputStream in)
/* 297:    */     throws IOException, ClassNotFoundException
/* 298:    */   {
/* 299:361 */     in.defaultReadObject();
/* 300:    */     
/* 301:363 */     MpscLinkedQueueNode<E> tombstone = new DefaultNode(null);
/* 302:364 */     setHeadRef(tombstone);
/* 303:365 */     setTailRef(tombstone);
/* 304:    */     for (;;)
/* 305:    */     {
/* 306:369 */       E e = in.readObject();
/* 307:370 */       if (e == null) {
/* 308:    */         break;
/* 309:    */       }
/* 310:373 */       add(e);
/* 311:    */     }
/* 312:    */   }
/* 313:    */   
/* 314:    */   private static final class DefaultNode<T>
/* 315:    */     extends MpscLinkedQueueNode<T>
/* 316:    */   {
/* 317:    */     private T value;
/* 318:    */     
/* 319:    */     DefaultNode(T value)
/* 320:    */     {
/* 321:382 */       this.value = value;
/* 322:    */     }
/* 323:    */     
/* 324:    */     public T value()
/* 325:    */     {
/* 326:387 */       return this.value;
/* 327:    */     }
/* 328:    */     
/* 329:    */     protected T clearMaybe()
/* 330:    */     {
/* 331:392 */       T value = this.value;
/* 332:393 */       this.value = null;
/* 333:394 */       return value;
/* 334:    */     }
/* 335:    */   }
/* 336:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.MpscLinkedQueue
 * JD-Core Version:    0.7.0.1
 */