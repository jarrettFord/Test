/*   1:    */ package io.netty.channel.group;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufHolder;
/*   5:    */ import io.netty.channel.Channel;
/*   6:    */ import io.netty.channel.ChannelFuture;
/*   7:    */ import io.netty.channel.ChannelFutureListener;
/*   8:    */ import io.netty.channel.ServerChannel;
/*   9:    */ import io.netty.util.ReferenceCountUtil;
/*  10:    */ import io.netty.util.concurrent.EventExecutor;
/*  11:    */ import io.netty.util.internal.ConcurrentSet;
/*  12:    */ import io.netty.util.internal.StringUtil;
/*  13:    */ import java.util.AbstractSet;
/*  14:    */ import java.util.ArrayList;
/*  15:    */ import java.util.Collection;
/*  16:    */ import java.util.Iterator;
/*  17:    */ import java.util.LinkedHashMap;
/*  18:    */ import java.util.Map;
/*  19:    */ import java.util.concurrent.atomic.AtomicInteger;
/*  20:    */ 
/*  21:    */ public class DefaultChannelGroup
/*  22:    */   extends AbstractSet<Channel>
/*  23:    */   implements ChannelGroup
/*  24:    */ {
/*  25: 42 */   private static final AtomicInteger nextId = new AtomicInteger();
/*  26:    */   private final String name;
/*  27:    */   private final EventExecutor executor;
/*  28: 45 */   private final ConcurrentSet<Channel> serverChannels = new ConcurrentSet();
/*  29: 46 */   private final ConcurrentSet<Channel> nonServerChannels = new ConcurrentSet();
/*  30: 47 */   private final ChannelFutureListener remover = new ChannelFutureListener()
/*  31:    */   {
/*  32:    */     public void operationComplete(ChannelFuture future)
/*  33:    */       throws Exception
/*  34:    */     {
/*  35: 50 */       DefaultChannelGroup.this.remove(future.channel());
/*  36:    */     }
/*  37:    */   };
/*  38:    */   
/*  39:    */   public DefaultChannelGroup(EventExecutor executor)
/*  40:    */   {
/*  41: 59 */     this("group-0x" + Integer.toHexString(nextId.incrementAndGet()), executor);
/*  42:    */   }
/*  43:    */   
/*  44:    */   public DefaultChannelGroup(String name, EventExecutor executor)
/*  45:    */   {
/*  46: 68 */     if (name == null) {
/*  47: 69 */       throw new NullPointerException("name");
/*  48:    */     }
/*  49: 71 */     this.name = name;
/*  50: 72 */     this.executor = executor;
/*  51:    */   }
/*  52:    */   
/*  53:    */   public String name()
/*  54:    */   {
/*  55: 77 */     return this.name;
/*  56:    */   }
/*  57:    */   
/*  58:    */   public boolean isEmpty()
/*  59:    */   {
/*  60: 82 */     return (this.nonServerChannels.isEmpty()) && (this.serverChannels.isEmpty());
/*  61:    */   }
/*  62:    */   
/*  63:    */   public int size()
/*  64:    */   {
/*  65: 87 */     return this.nonServerChannels.size() + this.serverChannels.size();
/*  66:    */   }
/*  67:    */   
/*  68:    */   public boolean contains(Object o)
/*  69:    */   {
/*  70: 92 */     if ((o instanceof Channel))
/*  71:    */     {
/*  72: 93 */       Channel c = (Channel)o;
/*  73: 94 */       if ((o instanceof ServerChannel)) {
/*  74: 95 */         return this.serverChannels.contains(c);
/*  75:    */       }
/*  76: 97 */       return this.nonServerChannels.contains(c);
/*  77:    */     }
/*  78:100 */     return false;
/*  79:    */   }
/*  80:    */   
/*  81:    */   public boolean add(Channel channel)
/*  82:    */   {
/*  83:106 */     ConcurrentSet<Channel> set = (channel instanceof ServerChannel) ? this.serverChannels : this.nonServerChannels;
/*  84:    */     
/*  85:    */ 
/*  86:109 */     boolean added = set.add(channel);
/*  87:110 */     if (added) {
/*  88:111 */       channel.closeFuture().addListener(this.remover);
/*  89:    */     }
/*  90:113 */     return added;
/*  91:    */   }
/*  92:    */   
/*  93:    */   public boolean remove(Object o)
/*  94:    */   {
/*  95:118 */     if (!(o instanceof Channel)) {
/*  96:119 */       return false;
/*  97:    */     }
/*  98:122 */     Channel c = (Channel)o;
/*  99:    */     boolean removed;
/* 100:    */     boolean removed;
/* 101:123 */     if ((c instanceof ServerChannel)) {
/* 102:124 */       removed = this.serverChannels.remove(c);
/* 103:    */     } else {
/* 104:126 */       removed = this.nonServerChannels.remove(c);
/* 105:    */     }
/* 106:128 */     if (!removed) {
/* 107:129 */       return false;
/* 108:    */     }
/* 109:132 */     c.closeFuture().removeListener(this.remover);
/* 110:133 */     return true;
/* 111:    */   }
/* 112:    */   
/* 113:    */   public void clear()
/* 114:    */   {
/* 115:138 */     this.nonServerChannels.clear();
/* 116:139 */     this.serverChannels.clear();
/* 117:    */   }
/* 118:    */   
/* 119:    */   public Iterator<Channel> iterator()
/* 120:    */   {
/* 121:144 */     return new CombinedIterator(this.serverChannels.iterator(), this.nonServerChannels.iterator());
/* 122:    */   }
/* 123:    */   
/* 124:    */   public Object[] toArray()
/* 125:    */   {
/* 126:151 */     Collection<Channel> channels = new ArrayList(size());
/* 127:152 */     channels.addAll(this.serverChannels);
/* 128:153 */     channels.addAll(this.nonServerChannels);
/* 129:154 */     return channels.toArray();
/* 130:    */   }
/* 131:    */   
/* 132:    */   public <T> T[] toArray(T[] a)
/* 133:    */   {
/* 134:159 */     Collection<Channel> channels = new ArrayList(size());
/* 135:160 */     channels.addAll(this.serverChannels);
/* 136:161 */     channels.addAll(this.nonServerChannels);
/* 137:162 */     return channels.toArray(a);
/* 138:    */   }
/* 139:    */   
/* 140:    */   public ChannelGroupFuture close()
/* 141:    */   {
/* 142:167 */     return close(ChannelMatchers.all());
/* 143:    */   }
/* 144:    */   
/* 145:    */   public ChannelGroupFuture disconnect()
/* 146:    */   {
/* 147:172 */     return disconnect(ChannelMatchers.all());
/* 148:    */   }
/* 149:    */   
/* 150:    */   public ChannelGroupFuture deregister()
/* 151:    */   {
/* 152:177 */     return deregister(ChannelMatchers.all());
/* 153:    */   }
/* 154:    */   
/* 155:    */   public ChannelGroupFuture write(Object message)
/* 156:    */   {
/* 157:182 */     return write(message, ChannelMatchers.all());
/* 158:    */   }
/* 159:    */   
/* 160:    */   private static Object safeDuplicate(Object message)
/* 161:    */   {
/* 162:188 */     if ((message instanceof ByteBuf)) {
/* 163:189 */       return ((ByteBuf)message).duplicate().retain();
/* 164:    */     }
/* 165:190 */     if ((message instanceof ByteBufHolder)) {
/* 166:191 */       return ((ByteBufHolder)message).duplicate().retain();
/* 167:    */     }
/* 168:193 */     return ReferenceCountUtil.retain(message);
/* 169:    */   }
/* 170:    */   
/* 171:    */   public ChannelGroupFuture write(Object message, ChannelMatcher matcher)
/* 172:    */   {
/* 173:199 */     if (message == null) {
/* 174:200 */       throw new NullPointerException("message");
/* 175:    */     }
/* 176:202 */     if (matcher == null) {
/* 177:203 */       throw new NullPointerException("matcher");
/* 178:    */     }
/* 179:206 */     Map<Channel, ChannelFuture> futures = new LinkedHashMap(size());
/* 180:207 */     for (Channel c : this.nonServerChannels) {
/* 181:208 */       if (matcher.matches(c)) {
/* 182:209 */         futures.put(c, c.write(safeDuplicate(message)));
/* 183:    */       }
/* 184:    */     }
/* 185:213 */     ReferenceCountUtil.release(message);
/* 186:214 */     return new DefaultChannelGroupFuture(this, futures, this.executor);
/* 187:    */   }
/* 188:    */   
/* 189:    */   public ChannelGroup flush()
/* 190:    */   {
/* 191:219 */     return flush(ChannelMatchers.all());
/* 192:    */   }
/* 193:    */   
/* 194:    */   public ChannelGroupFuture flushAndWrite(Object message)
/* 195:    */   {
/* 196:224 */     return writeAndFlush(message);
/* 197:    */   }
/* 198:    */   
/* 199:    */   public ChannelGroupFuture writeAndFlush(Object message)
/* 200:    */   {
/* 201:229 */     return writeAndFlush(message, ChannelMatchers.all());
/* 202:    */   }
/* 203:    */   
/* 204:    */   public ChannelGroupFuture disconnect(ChannelMatcher matcher)
/* 205:    */   {
/* 206:234 */     if (matcher == null) {
/* 207:235 */       throw new NullPointerException("matcher");
/* 208:    */     }
/* 209:238 */     Map<Channel, ChannelFuture> futures = new LinkedHashMap(size());
/* 210:241 */     for (Channel c : this.serverChannels) {
/* 211:242 */       if (matcher.matches(c)) {
/* 212:243 */         futures.put(c, c.disconnect());
/* 213:    */       }
/* 214:    */     }
/* 215:246 */     for (Channel c : this.nonServerChannels) {
/* 216:247 */       if (matcher.matches(c)) {
/* 217:248 */         futures.put(c, c.disconnect());
/* 218:    */       }
/* 219:    */     }
/* 220:252 */     return new DefaultChannelGroupFuture(this, futures, this.executor);
/* 221:    */   }
/* 222:    */   
/* 223:    */   public ChannelGroupFuture close(ChannelMatcher matcher)
/* 224:    */   {
/* 225:257 */     if (matcher == null) {
/* 226:258 */       throw new NullPointerException("matcher");
/* 227:    */     }
/* 228:261 */     Map<Channel, ChannelFuture> futures = new LinkedHashMap(size());
/* 229:264 */     for (Channel c : this.serverChannels) {
/* 230:265 */       if (matcher.matches(c)) {
/* 231:266 */         futures.put(c, c.close());
/* 232:    */       }
/* 233:    */     }
/* 234:269 */     for (Channel c : this.nonServerChannels) {
/* 235:270 */       if (matcher.matches(c)) {
/* 236:271 */         futures.put(c, c.close());
/* 237:    */       }
/* 238:    */     }
/* 239:275 */     return new DefaultChannelGroupFuture(this, futures, this.executor);
/* 240:    */   }
/* 241:    */   
/* 242:    */   public ChannelGroupFuture deregister(ChannelMatcher matcher)
/* 243:    */   {
/* 244:280 */     if (matcher == null) {
/* 245:281 */       throw new NullPointerException("matcher");
/* 246:    */     }
/* 247:284 */     Map<Channel, ChannelFuture> futures = new LinkedHashMap(size());
/* 248:287 */     for (Channel c : this.serverChannels) {
/* 249:288 */       if (matcher.matches(c)) {
/* 250:289 */         futures.put(c, c.deregister());
/* 251:    */       }
/* 252:    */     }
/* 253:292 */     for (Channel c : this.nonServerChannels) {
/* 254:293 */       if (matcher.matches(c)) {
/* 255:294 */         futures.put(c, c.deregister());
/* 256:    */       }
/* 257:    */     }
/* 258:298 */     return new DefaultChannelGroupFuture(this, futures, this.executor);
/* 259:    */   }
/* 260:    */   
/* 261:    */   public ChannelGroup flush(ChannelMatcher matcher)
/* 262:    */   {
/* 263:303 */     for (Channel c : this.nonServerChannels) {
/* 264:304 */       if (matcher.matches(c)) {
/* 265:305 */         c.flush();
/* 266:    */       }
/* 267:    */     }
/* 268:308 */     return this;
/* 269:    */   }
/* 270:    */   
/* 271:    */   public ChannelGroupFuture flushAndWrite(Object message, ChannelMatcher matcher)
/* 272:    */   {
/* 273:313 */     return writeAndFlush(message, matcher);
/* 274:    */   }
/* 275:    */   
/* 276:    */   public ChannelGroupFuture writeAndFlush(Object message, ChannelMatcher matcher)
/* 277:    */   {
/* 278:318 */     if (message == null) {
/* 279:319 */       throw new NullPointerException("message");
/* 280:    */     }
/* 281:322 */     Map<Channel, ChannelFuture> futures = new LinkedHashMap(size());
/* 282:324 */     for (Channel c : this.nonServerChannels) {
/* 283:325 */       if (matcher.matches(c)) {
/* 284:326 */         futures.put(c, c.writeAndFlush(safeDuplicate(message)));
/* 285:    */       }
/* 286:    */     }
/* 287:330 */     ReferenceCountUtil.release(message);
/* 288:    */     
/* 289:332 */     return new DefaultChannelGroupFuture(this, futures, this.executor);
/* 290:    */   }
/* 291:    */   
/* 292:    */   public int hashCode()
/* 293:    */   {
/* 294:337 */     return System.identityHashCode(this);
/* 295:    */   }
/* 296:    */   
/* 297:    */   public boolean equals(Object o)
/* 298:    */   {
/* 299:342 */     return this == o;
/* 300:    */   }
/* 301:    */   
/* 302:    */   public int compareTo(ChannelGroup o)
/* 303:    */   {
/* 304:347 */     int v = name().compareTo(o.name());
/* 305:348 */     if (v != 0) {
/* 306:349 */       return v;
/* 307:    */     }
/* 308:352 */     return System.identityHashCode(this) - System.identityHashCode(o);
/* 309:    */   }
/* 310:    */   
/* 311:    */   public String toString()
/* 312:    */   {
/* 313:357 */     return StringUtil.simpleClassName(this) + "(name: " + name() + ", size: " + size() + ')';
/* 314:    */   }
/* 315:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.group.DefaultChannelGroup
 * JD-Core Version:    0.7.0.1
 */