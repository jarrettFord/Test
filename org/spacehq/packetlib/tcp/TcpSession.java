/*   1:    */ package org.spacehq.packetlib.tcp;
/*   2:    */ 
/*   3:    */ import io.netty.bootstrap.Bootstrap;
/*   4:    */ import io.netty.channel.Channel;
/*   5:    */ import io.netty.channel.ChannelFuture;
/*   6:    */ import io.netty.channel.ChannelFutureListener;
/*   7:    */ import io.netty.channel.ChannelHandlerContext;
/*   8:    */ import io.netty.channel.ChannelPipeline;
/*   9:    */ import io.netty.channel.EventLoopGroup;
/*  10:    */ import io.netty.channel.SimpleChannelInboundHandler;
/*  11:    */ import io.netty.handler.timeout.ReadTimeoutException;
/*  12:    */ import java.io.PrintStream;
/*  13:    */ import java.util.ArrayList;
/*  14:    */ import java.util.HashMap;
/*  15:    */ import java.util.List;
/*  16:    */ import java.util.Map;
/*  17:    */ import org.spacehq.packetlib.Session;
/*  18:    */ import org.spacehq.packetlib.event.session.ConnectedEvent;
/*  19:    */ import org.spacehq.packetlib.event.session.DisconnectedEvent;
/*  20:    */ import org.spacehq.packetlib.event.session.DisconnectingEvent;
/*  21:    */ import org.spacehq.packetlib.event.session.PacketReceivedEvent;
/*  22:    */ import org.spacehq.packetlib.event.session.PacketSentEvent;
/*  23:    */ import org.spacehq.packetlib.event.session.SessionEvent;
/*  24:    */ import org.spacehq.packetlib.event.session.SessionListener;
/*  25:    */ import org.spacehq.packetlib.packet.Packet;
/*  26:    */ import org.spacehq.packetlib.packet.PacketProtocol;
/*  27:    */ 
/*  28:    */ public class TcpSession
/*  29:    */   extends SimpleChannelInboundHandler<Packet>
/*  30:    */   implements Session
/*  31:    */ {
/*  32:    */   private String host;
/*  33:    */   private int port;
/*  34:    */   private PacketProtocol protocol;
/*  35:    */   private Bootstrap bootstrap;
/*  36:    */   private EventLoopGroup group;
/*  37:    */   private Channel channel;
/*  38: 25 */   private boolean disconnected = false;
/*  39: 26 */   private boolean writing = false;
/*  40: 27 */   private int compressionThreshold = -1;
/*  41: 28 */   private List<Packet> packets = new ArrayList();
/*  42: 30 */   private Map<String, Object> flags = new HashMap();
/*  43: 31 */   private List<SessionListener> listeners = new ArrayList();
/*  44:    */   
/*  45:    */   public TcpSession(String host, int port, PacketProtocol protocol, EventLoopGroup group, Bootstrap bootstrap)
/*  46:    */   {
/*  47: 34 */     this.host = host;
/*  48: 35 */     this.port = port;
/*  49: 36 */     this.protocol = protocol;
/*  50: 37 */     this.group = group;
/*  51: 38 */     this.bootstrap = bootstrap;
/*  52:    */   }
/*  53:    */   
/*  54:    */   public void connect()
/*  55:    */   {
/*  56: 43 */     connect(true);
/*  57:    */   }
/*  58:    */   
/*  59:    */   public void connect(boolean wait)
/*  60:    */   {
/*  61: 48 */     if (this.bootstrap == null)
/*  62:    */     {
/*  63: 49 */       if (!this.disconnected) {
/*  64: 50 */         return;
/*  65:    */       }
/*  66: 52 */       throw new IllegalStateException("Session disconnected.");
/*  67:    */     }
/*  68: 56 */     ChannelFuture future = this.bootstrap.connect();
/*  69: 57 */     this.bootstrap = null;
/*  70: 58 */     if (wait)
/*  71:    */     {
/*  72: 59 */       future.syncUninterruptibly();
/*  73: 60 */       while ((this.channel == null) && (!this.disconnected)) {
/*  74:    */         try
/*  75:    */         {
/*  76: 62 */           Thread.sleep(5L);
/*  77:    */         }
/*  78:    */         catch (InterruptedException localInterruptedException) {}
/*  79:    */       }
/*  80:    */     }
/*  81:    */   }
/*  82:    */   
/*  83:    */   public String getHost()
/*  84:    */   {
/*  85: 71 */     return this.host;
/*  86:    */   }
/*  87:    */   
/*  88:    */   public int getPort()
/*  89:    */   {
/*  90: 76 */     return this.port;
/*  91:    */   }
/*  92:    */   
/*  93:    */   public PacketProtocol getPacketProtocol()
/*  94:    */   {
/*  95: 81 */     return this.protocol;
/*  96:    */   }
/*  97:    */   
/*  98:    */   public Map<String, Object> getFlags()
/*  99:    */   {
/* 100: 86 */     return new HashMap(this.flags);
/* 101:    */   }
/* 102:    */   
/* 103:    */   public boolean hasFlag(String key)
/* 104:    */   {
/* 105: 91 */     return getFlags().containsKey(key);
/* 106:    */   }
/* 107:    */   
/* 108:    */   public <T> T getFlag(String key)
/* 109:    */   {
/* 110: 97 */     Object value = getFlags().get(key);
/* 111: 98 */     if (value == null) {
/* 112: 99 */       return null;
/* 113:    */     }
/* 114:    */     try
/* 115:    */     {
/* 116:103 */       return value;
/* 117:    */     }
/* 118:    */     catch (ClassCastException e)
/* 119:    */     {
/* 120:105 */       throw new IllegalStateException("Tried to get flag \"" + key + "\" as the wrong type. Actual type: " + value.getClass().getName());
/* 121:    */     }
/* 122:    */   }
/* 123:    */   
/* 124:    */   public void setFlag(String key, Object value)
/* 125:    */   {
/* 126:111 */     this.flags.put(key, value);
/* 127:    */   }
/* 128:    */   
/* 129:    */   public List<SessionListener> getListeners()
/* 130:    */   {
/* 131:116 */     return new ArrayList(this.listeners);
/* 132:    */   }
/* 133:    */   
/* 134:    */   public void addListener(SessionListener listener)
/* 135:    */   {
/* 136:121 */     this.listeners.add(listener);
/* 137:    */   }
/* 138:    */   
/* 139:    */   public void removeListener(SessionListener listener)
/* 140:    */   {
/* 141:126 */     this.listeners.remove(listener);
/* 142:    */   }
/* 143:    */   
/* 144:    */   public void callEvent(SessionEvent event)
/* 145:    */   {
/* 146:131 */     for (SessionListener listener : this.listeners) {
/* 147:132 */       event.call(listener);
/* 148:    */     }
/* 149:    */   }
/* 150:    */   
/* 151:    */   public int getCompressionThreshold()
/* 152:    */   {
/* 153:138 */     return this.compressionThreshold;
/* 154:    */   }
/* 155:    */   
/* 156:    */   public void setCompressionThreshold(int threshold)
/* 157:    */   {
/* 158:143 */     this.compressionThreshold = threshold;
/* 159:144 */     if (this.channel != null) {
/* 160:145 */       if (this.compressionThreshold >= 0)
/* 161:    */       {
/* 162:146 */         if (this.channel.pipeline().get("compression") == null) {
/* 163:147 */           this.channel.pipeline().addBefore("codec", "compression", new TcpPacketCompression(this));
/* 164:    */         }
/* 165:    */       }
/* 166:149 */       else if (this.channel.pipeline().get("compression") != null) {
/* 167:150 */         this.channel.pipeline().remove("compression");
/* 168:    */       }
/* 169:    */     }
/* 170:    */   }
/* 171:    */   
/* 172:    */   public boolean isConnected()
/* 173:    */   {
/* 174:157 */     return (this.channel != null) && (this.channel.isOpen()) && (!this.disconnected);
/* 175:    */   }
/* 176:    */   
/* 177:    */   public void send(final Packet packet)
/* 178:    */   {
/* 179:162 */     this.writing = true;
/* 180:163 */     if (this.channel == null)
/* 181:    */     {
/* 182:164 */       this.writing = false;
/* 183:165 */       return;
/* 184:    */     }
/* 185:168 */     this.channel.writeAndFlush(packet).addListener(new ChannelFutureListener()
/* 186:    */     {
/* 187:    */       public void operationComplete(ChannelFuture future)
/* 188:    */         throws Exception
/* 189:    */       {
/* 190:171 */         TcpSession.this.writing = false;
/* 191:172 */         if (!future.isSuccess()) {
/* 192:173 */           TcpSession.this.exceptionCaught(null, future.cause());
/* 193:    */         } else {
/* 194:175 */           TcpSession.this.callEvent(new PacketSentEvent(TcpSession.this, packet));
/* 195:    */         }
/* 196:    */       }
/* 197:    */     });
/* 198:180 */     if (packet.isPriority()) {
/* 199:181 */       while (this.writing) {
/* 200:    */         try
/* 201:    */         {
/* 202:183 */           Thread.sleep(2L);
/* 203:    */         }
/* 204:    */         catch (InterruptedException localInterruptedException) {}
/* 205:    */       }
/* 206:    */     }
/* 207:    */   }
/* 208:    */   
/* 209:    */   public void disconnect(String reason)
/* 210:    */   {
/* 211:192 */     if (this.disconnected) {
/* 212:193 */       return;
/* 213:    */     }
/* 214:196 */     this.disconnected = true;
/* 215:197 */     if (this.writing)
/* 216:    */     {
/* 217:198 */       while (this.writing) {
/* 218:    */         try
/* 219:    */         {
/* 220:200 */           Thread.sleep(2L);
/* 221:    */         }
/* 222:    */         catch (InterruptedException localInterruptedException) {}
/* 223:    */       }
/* 224:    */       try
/* 225:    */       {
/* 226:206 */         Thread.sleep(250L);
/* 227:    */       }
/* 228:    */       catch (InterruptedException localInterruptedException1) {}
/* 229:    */     }
/* 230:211 */     if (reason == null) {
/* 231:212 */       reason = "Connection closed.";
/* 232:    */     }
/* 233:    */     try
/* 234:    */     {
/* 235:216 */       if (this.channel != null)
/* 236:    */       {
/* 237:217 */         if (this.channel.isOpen()) {
/* 238:218 */           callEvent(new DisconnectingEvent(this, reason));
/* 239:    */         }
/* 240:221 */         this.channel.close().syncUninterruptibly();
/* 241:    */       }
/* 242:224 */       callEvent(new DisconnectedEvent(this, reason));
/* 243:    */     }
/* 244:    */     catch (Throwable t)
/* 245:    */     {
/* 246:226 */       System.err.println("[WARNING] Throwable caught while firing disconnect events.");
/* 247:227 */       t.printStackTrace();
/* 248:    */     }
/* 249:230 */     if (this.group != null) {
/* 250:    */       try
/* 251:    */       {
/* 252:232 */         this.group.shutdownGracefully();
/* 253:    */       }
/* 254:    */       catch (Exception localException) {}
/* 255:    */     }
/* 256:237 */     this.channel = null;
/* 257:    */   }
/* 258:    */   
/* 259:    */   public void channelActive(ChannelHandlerContext ctx)
/* 260:    */     throws Exception
/* 261:    */   {
/* 262:242 */     if (this.disconnected)
/* 263:    */     {
/* 264:243 */       ctx.channel().close().syncUninterruptibly();
/* 265:244 */       return;
/* 266:    */     }
/* 267:247 */     this.channel = ctx.channel();
/* 268:248 */     this.disconnected = false;
/* 269:249 */     callEvent(new ConnectedEvent(this));
/* 270:250 */     new PacketHandleThread(null).start();
/* 271:    */   }
/* 272:    */   
/* 273:    */   public void channelInactive(ChannelHandlerContext ctx)
/* 274:    */     throws Exception
/* 275:    */   {
/* 276:255 */     disconnect("Connection closed.");
/* 277:    */   }
/* 278:    */   
/* 279:    */   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
/* 280:    */     throws Exception
/* 281:    */   {
/* 282:260 */     this.writing = false;
/* 283:261 */     if (!this.disconnected) {
/* 284:262 */       if ((cause instanceof ReadTimeoutException))
/* 285:    */       {
/* 286:263 */         disconnect("Connection timed out.");
/* 287:    */       }
/* 288:    */       else
/* 289:    */       {
/* 290:265 */         disconnect("Internal network exception: " + cause.toString());
/* 291:266 */         cause.printStackTrace();
/* 292:    */       }
/* 293:    */     }
/* 294:270 */     this.disconnected = true;
/* 295:    */   }
/* 296:    */   
/* 297:    */   protected void messageReceived(ChannelHandlerContext ctx, Packet packet)
/* 298:    */     throws Exception
/* 299:    */   {
/* 300:274 */     if (!packet.isPriority()) {
/* 301:275 */       this.packets.add(packet);
/* 302:    */     }
/* 303:    */   }
/* 304:    */   
/* 305:    */   protected void channelRead0(ChannelHandlerContext arg0, Packet arg1)
/* 306:    */     throws Exception
/* 307:    */   {}
/* 308:    */   
/* 309:    */   private class PacketHandleThread
/* 310:    */     extends Thread
/* 311:    */   {
/* 312:    */     private PacketHandleThread() {}
/* 313:    */     
/* 314:    */     public void run()
/* 315:    */     {
/* 316:    */       try
/* 317:    */       {
/* 318:283 */         while (!TcpSession.this.disconnected)
/* 319:    */         {
/* 320:284 */           while (TcpSession.this.packets.size() > 0) {
/* 321:285 */             TcpSession.this.callEvent(new PacketReceivedEvent(TcpSession.this, (Packet)TcpSession.this.packets.remove(0)));
/* 322:    */           }
/* 323:    */           try
/* 324:    */           {
/* 325:289 */             Thread.sleep(5L);
/* 326:    */           }
/* 327:    */           catch (InterruptedException localInterruptedException) {}
/* 328:    */         }
/* 329:    */       }
/* 330:    */       catch (Throwable t)
/* 331:    */       {
/* 332:    */         try
/* 333:    */         {
/* 334:295 */           TcpSession.this.exceptionCaught(null, t);
/* 335:    */         }
/* 336:    */         catch (Exception e)
/* 337:    */         {
/* 338:297 */           System.err.println("Exception while handling exception!");
/* 339:298 */           e.printStackTrace();
/* 340:    */         }
/* 341:    */       }
/* 342:    */     }
/* 343:    */   }
/* 344:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.packetlib.tcp.TcpSession
 * JD-Core Version:    0.7.0.1
 */