/*   1:    */ package io.netty.bootstrap;
/*   2:    */ 
/*   3:    */ import io.netty.channel.Channel;
/*   4:    */ import io.netty.channel.Channel.Unsafe;
/*   5:    */ import io.netty.channel.ChannelConfig;
/*   6:    */ import io.netty.channel.ChannelFuture;
/*   7:    */ import io.netty.channel.ChannelFutureListener;
/*   8:    */ import io.netty.channel.ChannelHandler;
/*   9:    */ import io.netty.channel.ChannelHandlerContext;
/*  10:    */ import io.netty.channel.ChannelInboundHandlerAdapter;
/*  11:    */ import io.netty.channel.ChannelInitializer;
/*  12:    */ import io.netty.channel.ChannelOption;
/*  13:    */ import io.netty.channel.ChannelPipeline;
/*  14:    */ import io.netty.channel.EventLoop;
/*  15:    */ import io.netty.channel.EventLoopGroup;
/*  16:    */ import io.netty.channel.ServerChannel;
/*  17:    */ import io.netty.util.Attribute;
/*  18:    */ import io.netty.util.AttributeKey;
/*  19:    */ import io.netty.util.internal.StringUtil;
/*  20:    */ import io.netty.util.internal.logging.InternalLogger;
/*  21:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  22:    */ import java.util.LinkedHashMap;
/*  23:    */ import java.util.Map;
/*  24:    */ import java.util.Map.Entry;
/*  25:    */ import java.util.Set;
/*  26:    */ import java.util.concurrent.TimeUnit;
/*  27:    */ 
/*  28:    */ public final class ServerBootstrap
/*  29:    */   extends AbstractBootstrap<ServerBootstrap, ServerChannel>
/*  30:    */ {
/*  31: 47 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(ServerBootstrap.class);
/*  32: 49 */   private final Map<ChannelOption<?>, Object> childOptions = new LinkedHashMap();
/*  33: 50 */   private final Map<AttributeKey<?>, Object> childAttrs = new LinkedHashMap();
/*  34:    */   private volatile EventLoopGroup childGroup;
/*  35:    */   private volatile ChannelHandler childHandler;
/*  36:    */   
/*  37:    */   public ServerBootstrap() {}
/*  38:    */   
/*  39:    */   private ServerBootstrap(ServerBootstrap bootstrap)
/*  40:    */   {
/*  41: 57 */     super(bootstrap);
/*  42: 58 */     this.childGroup = bootstrap.childGroup;
/*  43: 59 */     this.childHandler = bootstrap.childHandler;
/*  44: 60 */     synchronized (bootstrap.childOptions)
/*  45:    */     {
/*  46: 61 */       this.childOptions.putAll(bootstrap.childOptions);
/*  47:    */     }
/*  48: 63 */     synchronized (bootstrap.childAttrs)
/*  49:    */     {
/*  50: 64 */       this.childAttrs.putAll(bootstrap.childAttrs);
/*  51:    */     }
/*  52:    */   }
/*  53:    */   
/*  54:    */   public ServerBootstrap group(EventLoopGroup group)
/*  55:    */   {
/*  56: 73 */     return group(group, group);
/*  57:    */   }
/*  58:    */   
/*  59:    */   public ServerBootstrap group(EventLoopGroup parentGroup, EventLoopGroup childGroup)
/*  60:    */   {
/*  61: 82 */     super.group(parentGroup);
/*  62: 83 */     if (childGroup == null) {
/*  63: 84 */       throw new NullPointerException("childGroup");
/*  64:    */     }
/*  65: 86 */     if (this.childGroup != null) {
/*  66: 87 */       throw new IllegalStateException("childGroup set already");
/*  67:    */     }
/*  68: 89 */     this.childGroup = childGroup;
/*  69: 90 */     return this;
/*  70:    */   }
/*  71:    */   
/*  72:    */   public <T> ServerBootstrap childOption(ChannelOption<T> childOption, T value)
/*  73:    */   {
/*  74: 99 */     if (childOption == null) {
/*  75:100 */       throw new NullPointerException("childOption");
/*  76:    */     }
/*  77:102 */     if (value == null) {
/*  78:103 */       synchronized (this.childOptions)
/*  79:    */       {
/*  80:104 */         this.childOptions.remove(childOption);
/*  81:    */       }
/*  82:    */     } else {
/*  83:107 */       synchronized (this.childOptions)
/*  84:    */       {
/*  85:108 */         this.childOptions.put(childOption, value);
/*  86:    */       }
/*  87:    */     }
/*  88:111 */     return this;
/*  89:    */   }
/*  90:    */   
/*  91:    */   public <T> ServerBootstrap childAttr(AttributeKey<T> childKey, T value)
/*  92:    */   {
/*  93:119 */     if (childKey == null) {
/*  94:120 */       throw new NullPointerException("childKey");
/*  95:    */     }
/*  96:122 */     if (value == null) {
/*  97:123 */       this.childAttrs.remove(childKey);
/*  98:    */     } else {
/*  99:125 */       this.childAttrs.put(childKey, value);
/* 100:    */     }
/* 101:127 */     return this;
/* 102:    */   }
/* 103:    */   
/* 104:    */   public ServerBootstrap childHandler(ChannelHandler childHandler)
/* 105:    */   {
/* 106:134 */     if (childHandler == null) {
/* 107:135 */       throw new NullPointerException("childHandler");
/* 108:    */     }
/* 109:137 */     this.childHandler = childHandler;
/* 110:138 */     return this;
/* 111:    */   }
/* 112:    */   
/* 113:    */   public EventLoopGroup childGroup()
/* 114:    */   {
/* 115:146 */     return this.childGroup;
/* 116:    */   }
/* 117:    */   
/* 118:    */   void init(Channel channel)
/* 119:    */     throws Exception
/* 120:    */   {
/* 121:151 */     Map<ChannelOption<?>, Object> options = options();
/* 122:152 */     synchronized (options)
/* 123:    */     {
/* 124:153 */       channel.config().setOptions(options);
/* 125:    */     }
/* 126:156 */     Map<AttributeKey<?>, Object> attrs = attrs();
/* 127:157 */     synchronized (attrs)
/* 128:    */     {
/* 129:158 */       for (Map.Entry<AttributeKey<?>, Object> e : attrs.entrySet())
/* 130:    */       {
/* 131:160 */         AttributeKey<Object> key = (AttributeKey)e.getKey();
/* 132:161 */         channel.attr(key).set(e.getValue());
/* 133:    */       }
/* 134:    */     }
/* 135:165 */     ChannelPipeline p = channel.pipeline();
/* 136:166 */     if (handler() != null) {
/* 137:167 */       p.addLast(new ChannelHandler[] { handler() });
/* 138:    */     }
/* 139:170 */     final EventLoopGroup currentChildGroup = this.childGroup;
/* 140:171 */     final ChannelHandler currentChildHandler = this.childHandler;
/* 141:    */     final Map.Entry<ChannelOption<?>, Object>[] currentChildOptions;
/* 142:174 */     synchronized (this.childOptions)
/* 143:    */     {
/* 144:175 */       currentChildOptions = (Map.Entry[])this.childOptions.entrySet().toArray(newOptionArray(this.childOptions.size()));
/* 145:    */     }
/* 146:    */     Object currentChildAttrs;
/* 147:177 */     synchronized (this.childAttrs)
/* 148:    */     {
/* 149:178 */       currentChildAttrs = (Map.Entry[])this.childAttrs.entrySet().toArray(newAttrArray(this.childAttrs.size()));
/* 150:    */     }
/* 151:181 */     p.addLast(new ChannelHandler[] { new ChannelInitializer()
/* 152:    */     {
/* 153:    */       public void initChannel(Channel ch)
/* 154:    */         throws Exception
/* 155:    */       {
/* 156:184 */         ch.pipeline().addLast(new ChannelHandler[] { new ServerBootstrap.ServerBootstrapAcceptor(currentChildGroup, currentChildHandler, currentChildOptions, this.val$currentChildAttrs) });
/* 157:    */       }
/* 158:    */     } });
/* 159:    */   }
/* 160:    */   
/* 161:    */   public ServerBootstrap validate()
/* 162:    */   {
/* 163:192 */     super.validate();
/* 164:193 */     if (this.childHandler == null) {
/* 165:194 */       throw new IllegalStateException("childHandler not set");
/* 166:    */     }
/* 167:196 */     if (this.childGroup == null)
/* 168:    */     {
/* 169:197 */       logger.warn("childGroup is not set. Using parentGroup instead.");
/* 170:198 */       this.childGroup = group();
/* 171:    */     }
/* 172:200 */     return this;
/* 173:    */   }
/* 174:    */   
/* 175:    */   private static Map.Entry<ChannelOption<?>, Object>[] newOptionArray(int size)
/* 176:    */   {
/* 177:205 */     return new Map.Entry[size];
/* 178:    */   }
/* 179:    */   
/* 180:    */   private static Map.Entry<AttributeKey<?>, Object>[] newAttrArray(int size)
/* 181:    */   {
/* 182:210 */     return new Map.Entry[size];
/* 183:    */   }
/* 184:    */   
/* 185:    */   private static class ServerBootstrapAcceptor
/* 186:    */     extends ChannelInboundHandlerAdapter
/* 187:    */   {
/* 188:    */     private final EventLoopGroup childGroup;
/* 189:    */     private final ChannelHandler childHandler;
/* 190:    */     private final Map.Entry<ChannelOption<?>, Object>[] childOptions;
/* 191:    */     private final Map.Entry<AttributeKey<?>, Object>[] childAttrs;
/* 192:    */     
/* 193:    */     ServerBootstrapAcceptor(EventLoopGroup childGroup, ChannelHandler childHandler, Map.Entry<ChannelOption<?>, Object>[] childOptions, Map.Entry<AttributeKey<?>, Object>[] childAttrs)
/* 194:    */     {
/* 195:224 */       this.childGroup = childGroup;
/* 196:225 */       this.childHandler = childHandler;
/* 197:226 */       this.childOptions = childOptions;
/* 198:227 */       this.childAttrs = childAttrs;
/* 199:    */     }
/* 200:    */     
/* 201:    */     public void channelRead(ChannelHandlerContext ctx, Object msg)
/* 202:    */     {
/* 203:233 */       final Channel child = (Channel)msg;
/* 204:    */       
/* 205:235 */       child.pipeline().addLast(new ChannelHandler[] { this.childHandler });
/* 206:237 */       for (Map.Entry<ChannelOption<?>, Object> e : this.childOptions) {
/* 207:    */         try
/* 208:    */         {
/* 209:239 */           if (!child.config().setOption((ChannelOption)e.getKey(), e.getValue())) {
/* 210:240 */             ServerBootstrap.logger.warn("Unknown channel option: " + e);
/* 211:    */           }
/* 212:    */         }
/* 213:    */         catch (Throwable t)
/* 214:    */         {
/* 215:243 */           ServerBootstrap.logger.warn("Failed to set a channel option: " + child, t);
/* 216:    */         }
/* 217:    */       }
/* 218:247 */       for (Map.Entry<AttributeKey<?>, Object> e : this.childAttrs) {
/* 219:248 */         child.attr((AttributeKey)e.getKey()).set(e.getValue());
/* 220:    */       }
/* 221:    */       try
/* 222:    */       {
/* 223:252 */         this.childGroup.register(child).addListener(new ChannelFutureListener()
/* 224:    */         {
/* 225:    */           public void operationComplete(ChannelFuture future)
/* 226:    */             throws Exception
/* 227:    */           {
/* 228:255 */             if (!future.isSuccess()) {
/* 229:256 */               ServerBootstrap.ServerBootstrapAcceptor.forceClose(child, future.cause());
/* 230:    */             }
/* 231:    */           }
/* 232:    */         });
/* 233:    */       }
/* 234:    */       catch (Throwable t)
/* 235:    */       {
/* 236:261 */         forceClose(child, t);
/* 237:    */       }
/* 238:    */     }
/* 239:    */     
/* 240:    */     private static void forceClose(Channel child, Throwable t)
/* 241:    */     {
/* 242:266 */       child.unsafe().closeForcibly();
/* 243:267 */       ServerBootstrap.logger.warn("Failed to register an accepted channel: " + child, t);
/* 244:    */     }
/* 245:    */     
/* 246:    */     public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
/* 247:    */       throws Exception
/* 248:    */     {
/* 249:272 */       final ChannelConfig config = ctx.channel().config();
/* 250:273 */       if (config.isAutoRead())
/* 251:    */       {
/* 252:276 */         config.setAutoRead(false);
/* 253:277 */         ctx.channel().eventLoop().schedule(new Runnable()
/* 254:    */         {
/* 255:    */           public void run()
/* 256:    */           {
/* 257:280 */             config.setAutoRead(true);
/* 258:    */           }
/* 259:280 */         }, 1L, TimeUnit.SECONDS);
/* 260:    */       }
/* 261:286 */       ctx.fireExceptionCaught(cause);
/* 262:    */     }
/* 263:    */   }
/* 264:    */   
/* 265:    */   public ServerBootstrap clone()
/* 266:    */   {
/* 267:293 */     return new ServerBootstrap(this);
/* 268:    */   }
/* 269:    */   
/* 270:    */   public String toString()
/* 271:    */   {
/* 272:298 */     StringBuilder buf = new StringBuilder(super.toString());
/* 273:299 */     buf.setLength(buf.length() - 1);
/* 274:300 */     buf.append(", ");
/* 275:301 */     if (this.childGroup != null)
/* 276:    */     {
/* 277:302 */       buf.append("childGroup: ");
/* 278:303 */       buf.append(StringUtil.simpleClassName(this.childGroup));
/* 279:304 */       buf.append(", ");
/* 280:    */     }
/* 281:306 */     synchronized (this.childOptions)
/* 282:    */     {
/* 283:307 */       if (!this.childOptions.isEmpty())
/* 284:    */       {
/* 285:308 */         buf.append("childOptions: ");
/* 286:309 */         buf.append(this.childOptions);
/* 287:310 */         buf.append(", ");
/* 288:    */       }
/* 289:    */     }
/* 290:313 */     synchronized (this.childAttrs)
/* 291:    */     {
/* 292:314 */       if (!this.childAttrs.isEmpty())
/* 293:    */       {
/* 294:315 */         buf.append("childAttrs: ");
/* 295:316 */         buf.append(this.childAttrs);
/* 296:317 */         buf.append(", ");
/* 297:    */       }
/* 298:    */     }
/* 299:320 */     if (this.childHandler != null)
/* 300:    */     {
/* 301:321 */       buf.append("childHandler: ");
/* 302:322 */       buf.append(this.childHandler);
/* 303:323 */       buf.append(", ");
/* 304:    */     }
/* 305:325 */     if (buf.charAt(buf.length() - 1) == '(')
/* 306:    */     {
/* 307:326 */       buf.append(')');
/* 308:    */     }
/* 309:    */     else
/* 310:    */     {
/* 311:328 */       buf.setCharAt(buf.length() - 2, ')');
/* 312:329 */       buf.setLength(buf.length() - 1);
/* 313:    */     }
/* 314:332 */     return buf.toString();
/* 315:    */   }
/* 316:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.bootstrap.ServerBootstrap
 * JD-Core Version:    0.7.0.1
 */