/*   1:    */ package io.netty.channel.sctp.oio;
/*   2:    */ 
/*   3:    */ import com.sun.nio.sctp.SctpChannel;
/*   4:    */ import io.netty.channel.ChannelException;
/*   5:    */ import io.netty.channel.ChannelFuture;
/*   6:    */ import io.netty.channel.ChannelMetadata;
/*   7:    */ import io.netty.channel.ChannelOutboundBuffer;
/*   8:    */ import io.netty.channel.ChannelPromise;
/*   9:    */ import io.netty.channel.EventLoop;
/*  10:    */ import io.netty.channel.oio.AbstractOioMessageChannel;
/*  11:    */ import io.netty.channel.sctp.DefaultSctpServerChannelConfig;
/*  12:    */ import io.netty.channel.sctp.SctpServerChannelConfig;
/*  13:    */ import io.netty.util.internal.logging.InternalLogger;
/*  14:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*  15:    */ import java.io.IOException;
/*  16:    */ import java.net.InetAddress;
/*  17:    */ import java.net.InetSocketAddress;
/*  18:    */ import java.net.SocketAddress;
/*  19:    */ import java.nio.channels.SelectionKey;
/*  20:    */ import java.nio.channels.Selector;
/*  21:    */ import java.util.Collections;
/*  22:    */ import java.util.Iterator;
/*  23:    */ import java.util.LinkedHashSet;
/*  24:    */ import java.util.List;
/*  25:    */ import java.util.Set;
/*  26:    */ 
/*  27:    */ public class OioSctpServerChannel
/*  28:    */   extends AbstractOioMessageChannel
/*  29:    */   implements io.netty.channel.sctp.SctpServerChannel
/*  30:    */ {
/*  31: 53 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(OioSctpServerChannel.class);
/*  32: 56 */   private static final ChannelMetadata METADATA = new ChannelMetadata(false);
/*  33:    */   private final com.sun.nio.sctp.SctpServerChannel sch;
/*  34:    */   private final SctpServerChannelConfig config;
/*  35:    */   private final Selector selector;
/*  36:    */   
/*  37:    */   private static com.sun.nio.sctp.SctpServerChannel newServerSocket()
/*  38:    */   {
/*  39:    */     try
/*  40:    */     {
/*  41: 60 */       return com.sun.nio.sctp.SctpServerChannel.open();
/*  42:    */     }
/*  43:    */     catch (IOException e)
/*  44:    */     {
/*  45: 62 */       throw new ChannelException("failed to create a sctp server channel", e);
/*  46:    */     }
/*  47:    */   }
/*  48:    */   
/*  49:    */   public OioSctpServerChannel()
/*  50:    */   {
/*  51: 74 */     this(newServerSocket());
/*  52:    */   }
/*  53:    */   
/*  54:    */   public OioSctpServerChannel(com.sun.nio.sctp.SctpServerChannel sch)
/*  55:    */   {
/*  56: 83 */     super(null);
/*  57: 84 */     if (sch == null) {
/*  58: 85 */       throw new NullPointerException("sctp server channel");
/*  59:    */     }
/*  60: 88 */     this.sch = sch;
/*  61: 89 */     boolean success = false;
/*  62:    */     try
/*  63:    */     {
/*  64: 91 */       sch.configureBlocking(false);
/*  65: 92 */       this.selector = Selector.open();
/*  66: 93 */       sch.register(this.selector, 16);
/*  67: 94 */       this.config = new OioSctpServerChannelConfig(this, sch, null);
/*  68: 95 */       success = true; return;
/*  69:    */     }
/*  70:    */     catch (Exception e)
/*  71:    */     {
/*  72: 97 */       throw new ChannelException("failed to initialize a sctp server channel", e);
/*  73:    */     }
/*  74:    */     finally
/*  75:    */     {
/*  76: 99 */       if (!success) {
/*  77:    */         try
/*  78:    */         {
/*  79:101 */           sch.close();
/*  80:    */         }
/*  81:    */         catch (IOException e)
/*  82:    */         {
/*  83:103 */           logger.warn("Failed to close a sctp server channel.", e);
/*  84:    */         }
/*  85:    */       }
/*  86:    */     }
/*  87:    */   }
/*  88:    */   
/*  89:    */   public ChannelMetadata metadata()
/*  90:    */   {
/*  91:111 */     return METADATA;
/*  92:    */   }
/*  93:    */   
/*  94:    */   public SctpServerChannelConfig config()
/*  95:    */   {
/*  96:116 */     return this.config;
/*  97:    */   }
/*  98:    */   
/*  99:    */   public InetSocketAddress remoteAddress()
/* 100:    */   {
/* 101:121 */     return null;
/* 102:    */   }
/* 103:    */   
/* 104:    */   public InetSocketAddress localAddress()
/* 105:    */   {
/* 106:126 */     return (InetSocketAddress)super.localAddress();
/* 107:    */   }
/* 108:    */   
/* 109:    */   public boolean isOpen()
/* 110:    */   {
/* 111:131 */     return this.sch.isOpen();
/* 112:    */   }
/* 113:    */   
/* 114:    */   protected SocketAddress localAddress0()
/* 115:    */   {
/* 116:    */     try
/* 117:    */     {
/* 118:137 */       Iterator<SocketAddress> i = this.sch.getAllLocalAddresses().iterator();
/* 119:138 */       if (i.hasNext()) {
/* 120:139 */         return (SocketAddress)i.next();
/* 121:    */       }
/* 122:    */     }
/* 123:    */     catch (IOException e) {}
/* 124:144 */     return null;
/* 125:    */   }
/* 126:    */   
/* 127:    */   public Set<InetSocketAddress> allLocalAddresses()
/* 128:    */   {
/* 129:    */     try
/* 130:    */     {
/* 131:150 */       Set<SocketAddress> allLocalAddresses = this.sch.getAllLocalAddresses();
/* 132:151 */       Set<InetSocketAddress> addresses = new LinkedHashSet(allLocalAddresses.size());
/* 133:152 */       for (SocketAddress socketAddress : allLocalAddresses) {
/* 134:153 */         addresses.add((InetSocketAddress)socketAddress);
/* 135:    */       }
/* 136:155 */       return addresses;
/* 137:    */     }
/* 138:    */     catch (Throwable t) {}
/* 139:157 */     return Collections.emptySet();
/* 140:    */   }
/* 141:    */   
/* 142:    */   public boolean isActive()
/* 143:    */   {
/* 144:163 */     return (isOpen()) && (localAddress0() != null);
/* 145:    */   }
/* 146:    */   
/* 147:    */   protected void doBind(SocketAddress localAddress)
/* 148:    */     throws Exception
/* 149:    */   {
/* 150:168 */     this.sch.bind(localAddress, this.config.getBacklog());
/* 151:    */   }
/* 152:    */   
/* 153:    */   protected void doClose()
/* 154:    */     throws Exception
/* 155:    */   {
/* 156:    */     try
/* 157:    */     {
/* 158:174 */       this.selector.close();
/* 159:    */     }
/* 160:    */     catch (IOException e)
/* 161:    */     {
/* 162:176 */       logger.warn("Failed to close a selector.", e);
/* 163:    */     }
/* 164:178 */     this.sch.close();
/* 165:    */   }
/* 166:    */   
/* 167:    */   protected int doReadMessages(List<Object> buf)
/* 168:    */     throws Exception
/* 169:    */   {
/* 170:183 */     if (!isActive()) {
/* 171:184 */       return -1;
/* 172:    */     }
/* 173:187 */     SctpChannel s = null;
/* 174:188 */     int acceptedChannels = 0;
/* 175:    */     try
/* 176:    */     {
/* 177:190 */       int selectedKeys = this.selector.select(1000L);
/* 178:191 */       if (selectedKeys > 0)
/* 179:    */       {
/* 180:192 */         Iterator<SelectionKey> selectionKeys = this.selector.selectedKeys().iterator();
/* 181:    */         for (;;)
/* 182:    */         {
/* 183:194 */           SelectionKey key = (SelectionKey)selectionKeys.next();
/* 184:195 */           selectionKeys.remove();
/* 185:196 */           if (key.isAcceptable())
/* 186:    */           {
/* 187:197 */             s = this.sch.accept();
/* 188:198 */             if (s != null)
/* 189:    */             {
/* 190:199 */               buf.add(new OioSctpChannel(this, s));
/* 191:200 */               acceptedChannels++;
/* 192:    */             }
/* 193:    */           }
/* 194:203 */           if (!selectionKeys.hasNext()) {
/* 195:204 */             return acceptedChannels;
/* 196:    */           }
/* 197:    */         }
/* 198:    */       }
/* 199:    */     }
/* 200:    */     catch (Throwable t)
/* 201:    */     {
/* 202:209 */       logger.warn("Failed to create a new channel from an accepted sctp channel.", t);
/* 203:210 */       if (s != null) {
/* 204:    */         try
/* 205:    */         {
/* 206:212 */           s.close();
/* 207:    */         }
/* 208:    */         catch (Throwable t2)
/* 209:    */         {
/* 210:214 */           logger.warn("Failed to close a sctp channel.", t2);
/* 211:    */         }
/* 212:    */       }
/* 213:    */     }
/* 214:219 */     return acceptedChannels;
/* 215:    */   }
/* 216:    */   
/* 217:    */   public ChannelFuture bindAddress(InetAddress localAddress)
/* 218:    */   {
/* 219:224 */     return bindAddress(localAddress, newPromise());
/* 220:    */   }
/* 221:    */   
/* 222:    */   public ChannelFuture bindAddress(final InetAddress localAddress, final ChannelPromise promise)
/* 223:    */   {
/* 224:229 */     if (eventLoop().inEventLoop()) {
/* 225:    */       try
/* 226:    */       {
/* 227:231 */         this.sch.bindAddress(localAddress);
/* 228:232 */         promise.setSuccess();
/* 229:    */       }
/* 230:    */       catch (Throwable t)
/* 231:    */       {
/* 232:234 */         promise.setFailure(t);
/* 233:    */       }
/* 234:    */     } else {
/* 235:237 */       eventLoop().execute(new Runnable()
/* 236:    */       {
/* 237:    */         public void run()
/* 238:    */         {
/* 239:240 */           OioSctpServerChannel.this.bindAddress(localAddress, promise);
/* 240:    */         }
/* 241:    */       });
/* 242:    */     }
/* 243:244 */     return promise;
/* 244:    */   }
/* 245:    */   
/* 246:    */   public ChannelFuture unbindAddress(InetAddress localAddress)
/* 247:    */   {
/* 248:249 */     return unbindAddress(localAddress, newPromise());
/* 249:    */   }
/* 250:    */   
/* 251:    */   public ChannelFuture unbindAddress(final InetAddress localAddress, final ChannelPromise promise)
/* 252:    */   {
/* 253:254 */     if (eventLoop().inEventLoop()) {
/* 254:    */       try
/* 255:    */       {
/* 256:256 */         this.sch.unbindAddress(localAddress);
/* 257:257 */         promise.setSuccess();
/* 258:    */       }
/* 259:    */       catch (Throwable t)
/* 260:    */       {
/* 261:259 */         promise.setFailure(t);
/* 262:    */       }
/* 263:    */     } else {
/* 264:262 */       eventLoop().execute(new Runnable()
/* 265:    */       {
/* 266:    */         public void run()
/* 267:    */         {
/* 268:265 */           OioSctpServerChannel.this.unbindAddress(localAddress, promise);
/* 269:    */         }
/* 270:    */       });
/* 271:    */     }
/* 272:269 */     return promise;
/* 273:    */   }
/* 274:    */   
/* 275:    */   protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress)
/* 276:    */     throws Exception
/* 277:    */   {
/* 278:275 */     throw new UnsupportedOperationException();
/* 279:    */   }
/* 280:    */   
/* 281:    */   protected SocketAddress remoteAddress0()
/* 282:    */   {
/* 283:280 */     return null;
/* 284:    */   }
/* 285:    */   
/* 286:    */   protected void doDisconnect()
/* 287:    */     throws Exception
/* 288:    */   {
/* 289:285 */     throw new UnsupportedOperationException();
/* 290:    */   }
/* 291:    */   
/* 292:    */   protected void doWrite(ChannelOutboundBuffer in)
/* 293:    */     throws Exception
/* 294:    */   {
/* 295:290 */     throw new UnsupportedOperationException();
/* 296:    */   }
/* 297:    */   
/* 298:    */   private final class OioSctpServerChannelConfig
/* 299:    */     extends DefaultSctpServerChannelConfig
/* 300:    */   {
/* 301:    */     private OioSctpServerChannelConfig(OioSctpServerChannel channel, com.sun.nio.sctp.SctpServerChannel javaChannel)
/* 302:    */     {
/* 303:295 */       super(javaChannel);
/* 304:    */     }
/* 305:    */     
/* 306:    */     protected void autoReadCleared()
/* 307:    */     {
/* 308:300 */       OioSctpServerChannel.this.setReadPending(false);
/* 309:    */     }
/* 310:    */   }
/* 311:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.sctp.oio.OioSctpServerChannel
 * JD-Core Version:    0.7.0.1
 */