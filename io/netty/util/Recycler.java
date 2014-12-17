/*   1:    */ package io.netty.util;
/*   2:    */ 
/*   3:    */ import io.netty.util.concurrent.FastThreadLocal;
/*   4:    */ import io.netty.util.internal.SystemPropertyUtil;
/*   5:    */ import io.netty.util.internal.logging.InternalLogger;
/*   6:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   7:    */ import java.lang.ref.WeakReference;
/*   8:    */ import java.util.Arrays;
/*   9:    */ import java.util.Map;
/*  10:    */ import java.util.WeakHashMap;
/*  11:    */ import java.util.concurrent.atomic.AtomicInteger;
/*  12:    */ 
/*  13:    */ public abstract class Recycler<T>
/*  14:    */ {
/*  15: 37 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(Recycler.class);
/*  16: 39 */   private static final AtomicInteger ID_GENERATOR = new AtomicInteger(-2147483648);
/*  17: 40 */   private static final int OWN_THREAD_ID = ID_GENERATOR.getAndIncrement();
/*  18:    */   private static final int DEFAULT_MAX_CAPACITY;
/*  19:    */   
/*  20:    */   static
/*  21:    */   {
/*  22: 48 */     int maxCapacity = SystemPropertyUtil.getInt("io.netty.recycler.maxCapacity.default", 0);
/*  23: 49 */     if (maxCapacity <= 0) {
/*  24: 51 */       maxCapacity = 262144;
/*  25:    */     }
/*  26: 54 */     DEFAULT_MAX_CAPACITY = maxCapacity;
/*  27: 55 */     if (logger.isDebugEnabled()) {
/*  28: 56 */       logger.debug("-Dio.netty.recycler.maxCapacity.default: {}", Integer.valueOf(DEFAULT_MAX_CAPACITY));
/*  29:    */     }
/*  30:    */   }
/*  31:    */   
/*  32: 59 */   private static final int INITIAL_CAPACITY = Math.min(DEFAULT_MAX_CAPACITY, 256);
/*  33:    */   private final int maxCapacity;
/*  34: 63 */   private final FastThreadLocal<Stack<T>> threadLocal = new FastThreadLocal()
/*  35:    */   {
/*  36:    */     protected Recycler.Stack<T> initialValue()
/*  37:    */     {
/*  38: 66 */       return new Recycler.Stack(Recycler.this, Thread.currentThread(), Recycler.this.maxCapacity);
/*  39:    */     }
/*  40:    */   };
/*  41:    */   
/*  42:    */   protected Recycler()
/*  43:    */   {
/*  44: 71 */     this(DEFAULT_MAX_CAPACITY);
/*  45:    */   }
/*  46:    */   
/*  47:    */   protected Recycler(int maxCapacity)
/*  48:    */   {
/*  49: 75 */     this.maxCapacity = Math.max(0, maxCapacity);
/*  50:    */   }
/*  51:    */   
/*  52:    */   public final T get()
/*  53:    */   {
/*  54: 80 */     Stack<T> stack = (Stack)this.threadLocal.get();
/*  55: 81 */     DefaultHandle handle = stack.pop();
/*  56: 82 */     if (handle == null)
/*  57:    */     {
/*  58: 83 */       handle = stack.newHandle();
/*  59: 84 */       handle.value = newObject(handle);
/*  60:    */     }
/*  61: 86 */     return handle.value;
/*  62:    */   }
/*  63:    */   
/*  64:    */   public final boolean recycle(T o, Handle handle)
/*  65:    */   {
/*  66: 90 */     DefaultHandle h = (DefaultHandle)handle;
/*  67: 91 */     if (h.stack.parent != this) {
/*  68: 92 */       return false;
/*  69:    */     }
/*  70: 94 */     if (o != h.value) {
/*  71: 95 */       throw new IllegalArgumentException("o does not belong to handle");
/*  72:    */     }
/*  73: 97 */     h.recycle();
/*  74: 98 */     return true;
/*  75:    */   }
/*  76:    */   
/*  77:    */   protected abstract T newObject(Handle paramHandle);
/*  78:    */   
/*  79:    */   public static abstract interface Handle {}
/*  80:    */   
/*  81:    */   static final class DefaultHandle
/*  82:    */     implements Recycler.Handle
/*  83:    */   {
/*  84:    */     private int lastRecycledId;
/*  85:    */     private int recycleId;
/*  86:    */     private Recycler.Stack<?> stack;
/*  87:    */     private Object value;
/*  88:    */     
/*  89:    */     DefaultHandle(Recycler.Stack<?> stack)
/*  90:    */     {
/*  91:113 */       this.stack = stack;
/*  92:    */     }
/*  93:    */     
/*  94:    */     public void recycle()
/*  95:    */     {
/*  96:117 */       Thread thread = Thread.currentThread();
/*  97:118 */       if (thread == this.stack.thread)
/*  98:    */       {
/*  99:119 */         this.stack.push(this);
/* 100:120 */         return;
/* 101:    */       }
/* 102:125 */       Map<Recycler.Stack<?>, Recycler.WeakOrderQueue> delayedRecycled = (Map)Recycler.DELAYED_RECYCLED.get();
/* 103:126 */       Recycler.WeakOrderQueue queue = (Recycler.WeakOrderQueue)delayedRecycled.get(this.stack);
/* 104:127 */       if (queue == null) {
/* 105:128 */         delayedRecycled.put(this.stack, queue = new Recycler.WeakOrderQueue(this.stack, thread));
/* 106:    */       }
/* 107:130 */       queue.add(this);
/* 108:    */     }
/* 109:    */   }
/* 110:    */   
/* 111:134 */   private static final FastThreadLocal<Map<Stack<?>, WeakOrderQueue>> DELAYED_RECYCLED = new FastThreadLocal()
/* 112:    */   {
/* 113:    */     protected Map<Recycler.Stack<?>, Recycler.WeakOrderQueue> initialValue()
/* 114:    */     {
/* 115:138 */       return new WeakHashMap();
/* 116:    */     }
/* 117:    */   };
/* 118:    */   
/* 119:    */   private static final class WeakOrderQueue
/* 120:    */   {
/* 121:    */     private static final int LINK_CAPACITY = 16;
/* 122:    */     private Link head;
/* 123:    */     private Link tail;
/* 124:    */     private WeakOrderQueue next;
/* 125:    */     private final WeakReference<Thread> owner;
/* 126:    */     
/* 127:    */     private static final class Link
/* 128:    */       extends AtomicInteger
/* 129:    */     {
/* 130:150 */       private final Recycler.DefaultHandle[] elements = new Recycler.DefaultHandle[16];
/* 131:    */       private int readIndex;
/* 132:    */       private Link next;
/* 133:    */     }
/* 134:    */     
/* 135:161 */     private final int id = Recycler.ID_GENERATOR.getAndIncrement();
/* 136:    */     
/* 137:    */     public WeakOrderQueue(Recycler.Stack<?> stack, Thread thread)
/* 138:    */     {
/* 139:164 */       this.head = (this.tail = new Link(null));
/* 140:165 */       this.owner = new WeakReference(thread);
/* 141:166 */       synchronized (stack)
/* 142:    */       {
/* 143:167 */         this.next = stack.head;
/* 144:168 */         stack.head = this;
/* 145:    */       }
/* 146:    */     }
/* 147:    */     
/* 148:    */     void add(Recycler.DefaultHandle handle)
/* 149:    */     {
/* 150:173 */       Recycler.DefaultHandle.access$702(handle, this.id);
/* 151:    */       
/* 152:175 */       Link tail = this.tail;
/* 153:    */       int writeIndex;
/* 154:177 */       if ((writeIndex = tail.get()) == 16)
/* 155:    */       {
/* 156:178 */         this.tail = (tail = tail.next = new Link(null));
/* 157:179 */         writeIndex = tail.get();
/* 158:    */       }
/* 159:181 */       tail.elements[writeIndex] = handle;
/* 160:182 */       Recycler.DefaultHandle.access$202(handle, null);
/* 161:    */       
/* 162:    */ 
/* 163:185 */       tail.lazySet(writeIndex + 1);
/* 164:    */     }
/* 165:    */     
/* 166:    */     boolean hasFinalData()
/* 167:    */     {
/* 168:189 */       return this.tail.readIndex != this.tail.get();
/* 169:    */     }
/* 170:    */     
/* 171:    */     boolean transfer(Recycler.Stack<?> to)
/* 172:    */     {
/* 173:195 */       Link head = this.head;
/* 174:196 */       if (head == null) {
/* 175:197 */         return false;
/* 176:    */       }
/* 177:200 */       if (head.readIndex == 16)
/* 178:    */       {
/* 179:201 */         if (head.next == null) {
/* 180:202 */           return false;
/* 181:    */         }
/* 182:204 */         this.head = (head = head.next);
/* 183:    */       }
/* 184:207 */       int start = head.readIndex;
/* 185:208 */       int end = head.get();
/* 186:209 */       if (start == end) {
/* 187:210 */         return false;
/* 188:    */       }
/* 189:213 */       int count = end - start;
/* 190:214 */       if (to.size + count > to.elements.length) {
/* 191:215 */         to.elements = ((Recycler.DefaultHandle[])Arrays.copyOf(to.elements, (to.size + count) * 2));
/* 192:    */       }
/* 193:218 */       Recycler.DefaultHandle[] src = head.elements;
/* 194:219 */       Recycler.DefaultHandle[] trg = to.elements;
/* 195:220 */       int size = to.size;
/* 196:221 */       while (start < end)
/* 197:    */       {
/* 198:222 */         Recycler.DefaultHandle element = src[start];
/* 199:223 */         if (Recycler.DefaultHandle.access$1300(element) == 0) {
/* 200:224 */           Recycler.DefaultHandle.access$1302(element, Recycler.DefaultHandle.access$700(element));
/* 201:225 */         } else if (Recycler.DefaultHandle.access$1300(element) != Recycler.DefaultHandle.access$700(element)) {
/* 202:226 */           throw new IllegalStateException("recycled already");
/* 203:    */         }
/* 204:228 */         Recycler.DefaultHandle.access$202(element, to);
/* 205:229 */         trg[(size++)] = element;
/* 206:230 */         src[(start++)] = null;
/* 207:    */       }
/* 208:232 */       to.size = size;
/* 209:234 */       if (((end == 16 ? 1 : 0) & (head.next != null ? 1 : 0)) != 0) {
/* 210:235 */         this.head = head.next;
/* 211:    */       }
/* 212:238 */       head.readIndex = end;
/* 213:239 */       return true;
/* 214:    */     }
/* 215:    */   }
/* 216:    */   
/* 217:    */   static final class Stack<T>
/* 218:    */   {
/* 219:    */     final Recycler<T> parent;
/* 220:    */     final Thread thread;
/* 221:    */     private Recycler.DefaultHandle[] elements;
/* 222:    */     private final int maxCapacity;
/* 223:    */     private int size;
/* 224:    */     private volatile Recycler.WeakOrderQueue head;
/* 225:    */     private Recycler.WeakOrderQueue cursor;
/* 226:    */     private Recycler.WeakOrderQueue prev;
/* 227:    */     
/* 228:    */     Stack(Recycler<T> parent, Thread thread, int maxCapacity)
/* 229:    */     {
/* 230:259 */       this.parent = parent;
/* 231:260 */       this.thread = thread;
/* 232:261 */       this.maxCapacity = maxCapacity;
/* 233:262 */       this.elements = new Recycler.DefaultHandle[Recycler.INITIAL_CAPACITY];
/* 234:    */     }
/* 235:    */     
/* 236:    */     Recycler.DefaultHandle pop()
/* 237:    */     {
/* 238:266 */       int size = this.size;
/* 239:267 */       if (size == 0)
/* 240:    */       {
/* 241:268 */         if (!scavenge()) {
/* 242:269 */           return null;
/* 243:    */         }
/* 244:271 */         size = this.size;
/* 245:    */       }
/* 246:273 */       size--;
/* 247:274 */       Recycler.DefaultHandle ret = this.elements[size];
/* 248:275 */       if (Recycler.DefaultHandle.access$700(ret) != Recycler.DefaultHandle.access$1300(ret)) {
/* 249:276 */         throw new IllegalStateException("recycled multiple times");
/* 250:    */       }
/* 251:278 */       Recycler.DefaultHandle.access$1302(ret, 0);
/* 252:279 */       Recycler.DefaultHandle.access$702(ret, 0);
/* 253:280 */       this.size = size;
/* 254:281 */       return ret;
/* 255:    */     }
/* 256:    */     
/* 257:    */     boolean scavenge()
/* 258:    */     {
/* 259:286 */       if (scavengeSome()) {
/* 260:287 */         return true;
/* 261:    */       }
/* 262:291 */       this.prev = null;
/* 263:292 */       this.cursor = this.head;
/* 264:293 */       return false;
/* 265:    */     }
/* 266:    */     
/* 267:    */     boolean scavengeSome()
/* 268:    */     {
/* 269:297 */       boolean success = false;
/* 270:298 */       Recycler.WeakOrderQueue cursor = this.cursor;Recycler.WeakOrderQueue prev = this.prev;
/* 271:299 */       while (cursor != null)
/* 272:    */       {
/* 273:300 */         if (cursor.transfer(this))
/* 274:    */         {
/* 275:301 */           success = true;
/* 276:302 */           break;
/* 277:    */         }
/* 278:304 */         Recycler.WeakOrderQueue next = Recycler.WeakOrderQueue.access$1500(cursor);
/* 279:305 */         if (Recycler.WeakOrderQueue.access$1600(cursor).get() == null)
/* 280:    */         {
/* 281:309 */           if (cursor.hasFinalData()) {
/* 282:    */             for (;;)
/* 283:    */             {
/* 284:311 */               if (!cursor.transfer(this)) {
/* 285:    */                 break;
/* 286:    */               }
/* 287:    */             }
/* 288:    */           }
/* 289:316 */           if (prev != null) {
/* 290:317 */             Recycler.WeakOrderQueue.access$1502(prev, next);
/* 291:    */           }
/* 292:    */         }
/* 293:    */         else
/* 294:    */         {
/* 295:320 */           prev = cursor;
/* 296:    */         }
/* 297:322 */         cursor = next;
/* 298:    */       }
/* 299:324 */       this.prev = prev;
/* 300:325 */       this.cursor = cursor;
/* 301:326 */       return success;
/* 302:    */     }
/* 303:    */     
/* 304:    */     void push(Recycler.DefaultHandle item)
/* 305:    */     {
/* 306:330 */       if ((Recycler.DefaultHandle.access$1300(item) | Recycler.DefaultHandle.access$700(item)) != 0) {
/* 307:331 */         throw new IllegalStateException("recycled already");
/* 308:    */       }
/* 309:333 */       Recycler.DefaultHandle.access$1302(item, Recycler.DefaultHandle.access$702(item, Recycler.OWN_THREAD_ID));
/* 310:    */       
/* 311:335 */       int size = this.size;
/* 312:336 */       if (size == this.elements.length)
/* 313:    */       {
/* 314:337 */         if (size == this.maxCapacity) {
/* 315:339 */           return;
/* 316:    */         }
/* 317:341 */         this.elements = ((Recycler.DefaultHandle[])Arrays.copyOf(this.elements, size << 1));
/* 318:    */       }
/* 319:344 */       this.elements[size] = item;
/* 320:345 */       this.size = (size + 1);
/* 321:    */     }
/* 322:    */     
/* 323:    */     Recycler.DefaultHandle newHandle()
/* 324:    */     {
/* 325:349 */       return new Recycler.DefaultHandle(this);
/* 326:    */     }
/* 327:    */   }
/* 328:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.Recycler
 * JD-Core Version:    0.7.0.1
 */