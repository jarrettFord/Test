/*   1:    */ package io.netty.bootstrap;
/*   2:    */ 
/*   3:    */ import io.netty.channel.Channel;
/*   4:    */ import io.netty.channel.Channel.Unsafe;
/*   5:    */ import io.netty.channel.ChannelException;
/*   6:    */ import io.netty.channel.ChannelFuture;
/*   7:    */ import io.netty.channel.ChannelFutureListener;
/*   8:    */ import io.netty.channel.ChannelHandler;
/*   9:    */ import io.netty.channel.ChannelOption;
/*  10:    */ import io.netty.channel.ChannelPromise;
/*  11:    */ import io.netty.channel.DefaultChannelPromise;
/*  12:    */ import io.netty.channel.EventLoop;
/*  13:    */ import io.netty.channel.EventLoopGroup;
/*  14:    */ import io.netty.util.AttributeKey;
/*  15:    */ import io.netty.util.concurrent.EventExecutor;
/*  16:    */ import io.netty.util.concurrent.GlobalEventExecutor;
/*  17:    */ import io.netty.util.internal.StringUtil;
/*  18:    */ import java.net.InetAddress;
/*  19:    */ import java.net.InetSocketAddress;
/*  20:    */ import java.net.SocketAddress;
/*  21:    */ import java.util.LinkedHashMap;
/*  22:    */ import java.util.Map;
/*  23:    */ 
/*  24:    */ public abstract class AbstractBootstrap<B extends AbstractBootstrap<B, C>, C extends Channel>
/*  25:    */   implements Cloneable
/*  26:    */ {
/*  27:    */   private volatile EventLoopGroup group;
/*  28:    */   private volatile ChannelFactory<? extends C> channelFactory;
/*  29:    */   private volatile SocketAddress localAddress;
/*  30: 52 */   private final Map<ChannelOption<?>, Object> options = new LinkedHashMap();
/*  31: 53 */   private final Map<AttributeKey<?>, Object> attrs = new LinkedHashMap();
/*  32:    */   private volatile ChannelHandler handler;
/*  33:    */   
/*  34:    */   AbstractBootstrap() {}
/*  35:    */   
/*  36:    */   AbstractBootstrap(AbstractBootstrap<B, C> bootstrap)
/*  37:    */   {
/*  38: 61 */     this.group = bootstrap.group;
/*  39: 62 */     this.channelFactory = bootstrap.channelFactory;
/*  40: 63 */     this.handler = bootstrap.handler;
/*  41: 64 */     this.localAddress = bootstrap.localAddress;
/*  42: 65 */     synchronized (bootstrap.options)
/*  43:    */     {
/*  44: 66 */       this.options.putAll(bootstrap.options);
/*  45:    */     }
/*  46: 68 */     synchronized (bootstrap.attrs)
/*  47:    */     {
/*  48: 69 */       this.attrs.putAll(bootstrap.attrs);
/*  49:    */     }
/*  50:    */   }
/*  51:    */   
/*  52:    */   public B group(EventLoopGroup group)
/*  53:    */   {
/*  54: 79 */     if (group == null) {
/*  55: 80 */       throw new NullPointerException("group");
/*  56:    */     }
/*  57: 82 */     if (this.group != null) {
/*  58: 83 */       throw new IllegalStateException("group set already");
/*  59:    */     }
/*  60: 85 */     this.group = group;
/*  61: 86 */     return this;
/*  62:    */   }
/*  63:    */   
/*  64:    */   public B channel(Class<? extends C> channelClass)
/*  65:    */   {
/*  66: 95 */     if (channelClass == null) {
/*  67: 96 */       throw new NullPointerException("channelClass");
/*  68:    */     }
/*  69: 98 */     return channelFactory(new BootstrapChannelFactory(channelClass));
/*  70:    */   }
/*  71:    */   
/*  72:    */   public B channelFactory(ChannelFactory<? extends C> channelFactory)
/*  73:    */   {
/*  74:110 */     if (channelFactory == null) {
/*  75:111 */       throw new NullPointerException("channelFactory");
/*  76:    */     }
/*  77:113 */     if (this.channelFactory != null) {
/*  78:114 */       throw new IllegalStateException("channelFactory set already");
/*  79:    */     }
/*  80:117 */     this.channelFactory = channelFactory;
/*  81:118 */     return this;
/*  82:    */   }
/*  83:    */   
/*  84:    */   public B localAddress(SocketAddress localAddress)
/*  85:    */   {
/*  86:127 */     this.localAddress = localAddress;
/*  87:128 */     return this;
/*  88:    */   }
/*  89:    */   
/*  90:    */   public B localAddress(int inetPort)
/*  91:    */   {
/*  92:135 */     return localAddress(new InetSocketAddress(inetPort));
/*  93:    */   }
/*  94:    */   
/*  95:    */   public B localAddress(String inetHost, int inetPort)
/*  96:    */   {
/*  97:142 */     return localAddress(new InetSocketAddress(inetHost, inetPort));
/*  98:    */   }
/*  99:    */   
/* 100:    */   public B localAddress(InetAddress inetHost, int inetPort)
/* 101:    */   {
/* 102:149 */     return localAddress(new InetSocketAddress(inetHost, inetPort));
/* 103:    */   }
/* 104:    */   
/* 105:    */   public <T> B option(ChannelOption<T> option, T value)
/* 106:    */   {
/* 107:158 */     if (option == null) {
/* 108:159 */       throw new NullPointerException("option");
/* 109:    */     }
/* 110:161 */     if (value == null) {
/* 111:162 */       synchronized (this.options)
/* 112:    */       {
/* 113:163 */         this.options.remove(option);
/* 114:    */       }
/* 115:    */     } else {
/* 116:166 */       synchronized (this.options)
/* 117:    */       {
/* 118:167 */         this.options.put(option, value);
/* 119:    */       }
/* 120:    */     }
/* 121:170 */     return this;
/* 122:    */   }
/* 123:    */   
/* 124:    */   public <T> B attr(AttributeKey<T> key, T value)
/* 125:    */   {
/* 126:178 */     if (key == null) {
/* 127:179 */       throw new NullPointerException("key");
/* 128:    */     }
/* 129:181 */     if (value == null) {
/* 130:182 */       synchronized (this.attrs)
/* 131:    */       {
/* 132:183 */         this.attrs.remove(key);
/* 133:    */       }
/* 134:    */     } else {
/* 135:186 */       synchronized (this.attrs)
/* 136:    */       {
/* 137:187 */         this.attrs.put(key, value);
/* 138:    */       }
/* 139:    */     }
/* 140:192 */     B b = this;
/* 141:193 */     return b;
/* 142:    */   }
/* 143:    */   
/* 144:    */   public B validate()
/* 145:    */   {
/* 146:202 */     if (this.group == null) {
/* 147:203 */       throw new IllegalStateException("group not set");
/* 148:    */     }
/* 149:205 */     if (this.channelFactory == null) {
/* 150:206 */       throw new IllegalStateException("channel or channelFactory not set");
/* 151:    */     }
/* 152:208 */     return this;
/* 153:    */   }
/* 154:    */   
/* 155:    */   public abstract B clone();
/* 156:    */   
/* 157:    */   public ChannelFuture register()
/* 158:    */   {
/* 159:224 */     validate();
/* 160:225 */     return initAndRegister();
/* 161:    */   }
/* 162:    */   
/* 163:    */   public ChannelFuture bind()
/* 164:    */   {
/* 165:232 */     validate();
/* 166:233 */     SocketAddress localAddress = this.localAddress;
/* 167:234 */     if (localAddress == null) {
/* 168:235 */       throw new IllegalStateException("localAddress not set");
/* 169:    */     }
/* 170:237 */     return doBind(localAddress);
/* 171:    */   }
/* 172:    */   
/* 173:    */   public ChannelFuture bind(int inetPort)
/* 174:    */   {
/* 175:244 */     return bind(new InetSocketAddress(inetPort));
/* 176:    */   }
/* 177:    */   
/* 178:    */   public ChannelFuture bind(String inetHost, int inetPort)
/* 179:    */   {
/* 180:251 */     return bind(new InetSocketAddress(inetHost, inetPort));
/* 181:    */   }
/* 182:    */   
/* 183:    */   public ChannelFuture bind(InetAddress inetHost, int inetPort)
/* 184:    */   {
/* 185:258 */     return bind(new InetSocketAddress(inetHost, inetPort));
/* 186:    */   }
/* 187:    */   
/* 188:    */   public ChannelFuture bind(SocketAddress localAddress)
/* 189:    */   {
/* 190:265 */     validate();
/* 191:266 */     if (localAddress == null) {
/* 192:267 */       throw new NullPointerException("localAddress");
/* 193:    */     }
/* 194:269 */     return doBind(localAddress);
/* 195:    */   }
/* 196:    */   
/* 197:    */   private ChannelFuture doBind(final SocketAddress localAddress)
/* 198:    */   {
/* 199:273 */     final ChannelFuture regFuture = initAndRegister();
/* 200:274 */     final Channel channel = regFuture.channel();
/* 201:275 */     if (regFuture.cause() != null) {
/* 202:276 */       return regFuture;
/* 203:    */     }
/* 204:    */     final ChannelPromise promise;
/* 205:280 */     if (regFuture.isDone())
/* 206:    */     {
/* 207:281 */       ChannelPromise promise = channel.newPromise();
/* 208:282 */       doBind0(regFuture, channel, localAddress, promise);
/* 209:    */     }
/* 210:    */     else
/* 211:    */     {
/* 212:285 */       promise = new PendingRegistrationPromise(channel, null);
/* 213:286 */       regFuture.addListener(new ChannelFutureListener()
/* 214:    */       {
/* 215:    */         public void operationComplete(ChannelFuture future)
/* 216:    */           throws Exception
/* 217:    */         {
/* 218:289 */           AbstractBootstrap.doBind0(regFuture, channel, localAddress, promise);
/* 219:    */         }
/* 220:    */       });
/* 221:    */     }
/* 222:294 */     return promise;
/* 223:    */   }
/* 224:    */   
/* 225:    */   final ChannelFuture initAndRegister()
/* 226:    */   {
/* 227:298 */     Channel channel = channelFactory().newChannel();
/* 228:    */     try
/* 229:    */     {
/* 230:300 */       init(channel);
/* 231:    */     }
/* 232:    */     catch (Throwable t)
/* 233:    */     {
/* 234:302 */       channel.unsafe().closeForcibly();
/* 235:303 */       return channel.newFailedFuture(t);
/* 236:    */     }
/* 237:306 */     ChannelFuture regFuture = group().register(channel);
/* 238:307 */     if (regFuture.cause() != null) {
/* 239:308 */       if (channel.isRegistered()) {
/* 240:309 */         channel.close();
/* 241:    */       } else {
/* 242:311 */         channel.unsafe().closeForcibly();
/* 243:    */       }
/* 244:    */     }
/* 245:324 */     return regFuture;
/* 246:    */   }
/* 247:    */   
/* 248:    */   abstract void init(Channel paramChannel)
/* 249:    */     throws Exception;
/* 250:    */   
/* 251:    */   private static void doBind0(ChannelFuture regFuture, final Channel channel, final SocketAddress localAddress, final ChannelPromise promise)
/* 252:    */   {
/* 253:335 */     channel.eventLoop().execute(new Runnable()
/* 254:    */     {
/* 255:    */       public void run()
/* 256:    */       {
/* 257:338 */         if (this.val$regFuture.isSuccess()) {
/* 258:339 */           channel.bind(localAddress, promise).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
/* 259:    */         } else {
/* 260:341 */           promise.setFailure(this.val$regFuture.cause());
/* 261:    */         }
/* 262:    */       }
/* 263:    */     });
/* 264:    */   }
/* 265:    */   
/* 266:    */   public B handler(ChannelHandler handler)
/* 267:    */   {
/* 268:352 */     if (handler == null) {
/* 269:353 */       throw new NullPointerException("handler");
/* 270:    */     }
/* 271:355 */     this.handler = handler;
/* 272:356 */     return this;
/* 273:    */   }
/* 274:    */   
/* 275:    */   final SocketAddress localAddress()
/* 276:    */   {
/* 277:360 */     return this.localAddress;
/* 278:    */   }
/* 279:    */   
/* 280:    */   final ChannelFactory<? extends C> channelFactory()
/* 281:    */   {
/* 282:364 */     return this.channelFactory;
/* 283:    */   }
/* 284:    */   
/* 285:    */   final ChannelHandler handler()
/* 286:    */   {
/* 287:368 */     return this.handler;
/* 288:    */   }
/* 289:    */   
/* 290:    */   public final EventLoopGroup group()
/* 291:    */   {
/* 292:375 */     return this.group;
/* 293:    */   }
/* 294:    */   
/* 295:    */   final Map<ChannelOption<?>, Object> options()
/* 296:    */   {
/* 297:379 */     return this.options;
/* 298:    */   }
/* 299:    */   
/* 300:    */   final Map<AttributeKey<?>, Object> attrs()
/* 301:    */   {
/* 302:383 */     return this.attrs;
/* 303:    */   }
/* 304:    */   
/* 305:    */   public String toString()
/* 306:    */   {
/* 307:388 */     StringBuilder buf = new StringBuilder();
/* 308:389 */     buf.append(StringUtil.simpleClassName(this));
/* 309:390 */     buf.append('(');
/* 310:391 */     if (this.group != null)
/* 311:    */     {
/* 312:392 */       buf.append("group: ");
/* 313:393 */       buf.append(StringUtil.simpleClassName(this.group));
/* 314:394 */       buf.append(", ");
/* 315:    */     }
/* 316:396 */     if (this.channelFactory != null)
/* 317:    */     {
/* 318:397 */       buf.append("channelFactory: ");
/* 319:398 */       buf.append(this.channelFactory);
/* 320:399 */       buf.append(", ");
/* 321:    */     }
/* 322:401 */     if (this.localAddress != null)
/* 323:    */     {
/* 324:402 */       buf.append("localAddress: ");
/* 325:403 */       buf.append(this.localAddress);
/* 326:404 */       buf.append(", ");
/* 327:    */     }
/* 328:406 */     synchronized (this.options)
/* 329:    */     {
/* 330:407 */       if (!this.options.isEmpty())
/* 331:    */       {
/* 332:408 */         buf.append("options: ");
/* 333:409 */         buf.append(this.options);
/* 334:410 */         buf.append(", ");
/* 335:    */       }
/* 336:    */     }
/* 337:413 */     synchronized (this.attrs)
/* 338:    */     {
/* 339:414 */       if (!this.attrs.isEmpty())
/* 340:    */       {
/* 341:415 */         buf.append("attrs: ");
/* 342:416 */         buf.append(this.attrs);
/* 343:417 */         buf.append(", ");
/* 344:    */       }
/* 345:    */     }
/* 346:420 */     if (this.handler != null)
/* 347:    */     {
/* 348:421 */       buf.append("handler: ");
/* 349:422 */       buf.append(this.handler);
/* 350:423 */       buf.append(", ");
/* 351:    */     }
/* 352:425 */     if (buf.charAt(buf.length() - 1) == '(')
/* 353:    */     {
/* 354:426 */       buf.append(')');
/* 355:    */     }
/* 356:    */     else
/* 357:    */     {
/* 358:428 */       buf.setCharAt(buf.length() - 2, ')');
/* 359:429 */       buf.setLength(buf.length() - 1);
/* 360:    */     }
/* 361:431 */     return buf.toString();
/* 362:    */   }
/* 363:    */   
/* 364:    */   private static final class BootstrapChannelFactory<T extends Channel>
/* 365:    */     implements ChannelFactory<T>
/* 366:    */   {
/* 367:    */     private final Class<? extends T> clazz;
/* 368:    */     
/* 369:    */     BootstrapChannelFactory(Class<? extends T> clazz)
/* 370:    */     {
/* 371:438 */       this.clazz = clazz;
/* 372:    */     }
/* 373:    */     
/* 374:    */     public T newChannel()
/* 375:    */     {
/* 376:    */       try
/* 377:    */       {
/* 378:444 */         return (Channel)this.clazz.newInstance();
/* 379:    */       }
/* 380:    */       catch (Throwable t)
/* 381:    */       {
/* 382:446 */         throw new ChannelException("Unable to create Channel from class " + this.clazz, t);
/* 383:    */       }
/* 384:    */     }
/* 385:    */     
/* 386:    */     public String toString()
/* 387:    */     {
/* 388:452 */       return StringUtil.simpleClassName(this.clazz) + ".class";
/* 389:    */     }
/* 390:    */   }
/* 391:    */   
/* 392:    */   private static final class PendingRegistrationPromise
/* 393:    */     extends DefaultChannelPromise
/* 394:    */   {
/* 395:    */     private PendingRegistrationPromise(Channel channel)
/* 396:    */     {
/* 397:458 */       super();
/* 398:    */     }
/* 399:    */     
/* 400:    */     protected EventExecutor executor()
/* 401:    */     {
/* 402:463 */       if (isSuccess()) {
/* 403:466 */         return super.executor();
/* 404:    */       }
/* 405:469 */       return GlobalEventExecutor.INSTANCE;
/* 406:    */     }
/* 407:    */   }
/* 408:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.bootstrap.AbstractBootstrap
 * JD-Core Version:    0.7.0.1
 */