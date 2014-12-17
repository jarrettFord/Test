/*  1:   */ package org.spacehq.packetlib.tcp;
/*  2:   */ 
/*  3:   */ import io.netty.bootstrap.Bootstrap;
/*  4:   */ import io.netty.bootstrap.ServerBootstrap;
/*  5:   */ import io.netty.channel.Channel;
/*  6:   */ import io.netty.channel.ChannelConfig;
/*  7:   */ import io.netty.channel.ChannelFuture;
/*  8:   */ import io.netty.channel.ChannelInitializer;
/*  9:   */ import io.netty.channel.ChannelOption;
/* 10:   */ import io.netty.channel.ChannelPipeline;
/* 11:   */ import io.netty.channel.EventLoopGroup;
/* 12:   */ import io.netty.channel.nio.NioEventLoopGroup;
/* 13:   */ import io.netty.channel.oio.OioEventLoopGroup;
/* 14:   */ import io.netty.channel.socket.nio.NioServerSocketChannel;
/* 15:   */ import io.netty.channel.socket.nio.NioSocketChannel;
/* 16:   */ import io.netty.handler.timeout.ReadTimeoutHandler;
/* 17:   */ import java.net.InetSocketAddress;
/* 18:   */ import java.net.Proxy;
/* 19:   */ import org.spacehq.packetlib.Client;
/* 20:   */ import org.spacehq.packetlib.ConnectionListener;
/* 21:   */ import org.spacehq.packetlib.Server;
/* 22:   */ import org.spacehq.packetlib.Session;
/* 23:   */ import org.spacehq.packetlib.SessionFactory;
/* 24:   */ import org.spacehq.packetlib.packet.PacketProtocol;
/* 25:   */ 
/* 26:   */ public class TcpSessionFactory
/* 27:   */   implements SessionFactory
/* 28:   */ {
/* 29:   */   private Proxy clientProxy;
/* 30:   */   
/* 31:   */   public TcpSessionFactory() {}
/* 32:   */   
/* 33:   */   public TcpSessionFactory(Proxy clientProxy)
/* 34:   */   {
/* 35:25 */     this.clientProxy = clientProxy;
/* 36:   */   }
/* 37:   */   
/* 38:   */   public Session createClientSession(final Client client)
/* 39:   */   {
/* 40:30 */     Bootstrap bootstrap = new Bootstrap();
/* 41:31 */     EventLoopGroup group = null;
/* 42:32 */     if (this.clientProxy != null)
/* 43:   */     {
/* 44:33 */       group = new OioEventLoopGroup();
/* 45:34 */       bootstrap.channelFactory(new ProxyOioChannelFactory(this.clientProxy));
/* 46:   */     }
/* 47:   */     else
/* 48:   */     {
/* 49:36 */       group = new NioEventLoopGroup();
/* 50:37 */       bootstrap.channel(NioSocketChannel.class);
/* 51:   */     }
/* 52:40 */     final TcpSession session = new TcpSession(client.getHost(), client.getPort(), client.getPacketProtocol(), group, bootstrap);
/* 53:   */     
/* 54:   */ 
/* 55:   */ 
/* 56:   */ 
/* 57:   */ 
/* 58:   */ 
/* 59:   */ 
/* 60:   */ 
/* 61:   */ 
/* 62:   */ 
/* 63:   */ 
/* 64:   */ 
/* 65:   */ 
/* 66:   */ 
/* 67:   */ 
/* 68:   */ 
/* 69:   */ 
/* 70:   */ 
/* 71:   */ 
/* 72:60 */     ((Bootstrap)((Bootstrap)bootstrap.handler(new ChannelInitializer()
/* 73:   */     {
/* 74:   */       public void initChannel(Channel ch)
/* 75:   */         throws Exception
/* 76:   */       {
/* 77:44 */         session.getPacketProtocol().newClientSession(client, session);
/* 78:45 */         ch.config().setOption(ChannelOption.IP_TOS, Integer.valueOf(24));
/* 79:46 */         ch.config().setOption(ChannelOption.TCP_NODELAY, Boolean.valueOf(false));
/* 80:47 */         ChannelPipeline pipeline = ch.pipeline();
/* 81:48 */         pipeline.addLast("timer", new ReadTimeoutHandler(30));
/* 82:49 */         if (session.getPacketProtocol().needsPacketEncryptor()) {
/* 83:50 */           pipeline.addLast("encryption", new TcpPacketEncryptor(session));
/* 84:   */         }
/* 85:53 */         if (session.getPacketProtocol().needsPacketSizer()) {
/* 86:54 */           pipeline.addLast("sizer", new TcpPacketSizer(session));
/* 87:   */         }
/* 88:57 */         pipeline.addLast("codec", new TcpPacketCodec(session));
/* 89:58 */         pipeline.addLast("manager", session);
/* 90:   */       }
/* 91:60 */     })).group(group)).remoteAddress(client.getHost(), client.getPort());
/* 92:61 */     return session;
/* 93:   */   }
/* 94:   */   
/* 95:   */   public ConnectionListener createServerListener(final Server server)
/* 96:   */   {
/* 97:66 */     EventLoopGroup group = new NioEventLoopGroup();
/* 98:67 */     new TcpConnectionListener(server.getHost(), server.getPort(), group, 
/* 99:   */     
/* :0:   */ 
/* :1:   */ 
/* :2:   */ 
/* :3:   */ 
/* :4:   */ 
/* :5:   */ 
/* :6:   */ 
/* :7:   */ 
/* :8:   */ 
/* :9:   */ 
/* ;0:   */ 
/* ;1:   */ 
/* ;2:   */ 
/* ;3:   */ 
/* ;4:   */ 
/* ;5:   */ 
/* ;6:   */ 
/* ;7:   */ 
/* ;8:   */ 
/* ;9:   */ 
/* <0:   */ 
/* <1:90 */       ((ServerBootstrap)((ServerBootstrap)new ServerBootstrap().channel(NioServerSocketChannel.class)).childHandler(new ChannelInitializer()
/* <2:   */       {
/* <3:   */         public void initChannel(Channel ch)
/* <4:   */           throws Exception
/* <5:   */         {
/* <6:70 */           InetSocketAddress address = (InetSocketAddress)ch.remoteAddress();
/* <7:71 */           PacketProtocol protocol = server.createPacketProtocol();
/* <8:72 */           TcpSession session = new TcpServerSession(address.getHostName(), address.getPort(), protocol, null, null, server);
/* <9:73 */           session.getPacketProtocol().newServerSession(server, session);
/* =0:74 */           ch.config().setOption(ChannelOption.IP_TOS, Integer.valueOf(24));
/* =1:75 */           ch.config().setOption(ChannelOption.TCP_NODELAY, Boolean.valueOf(false));
/* =2:76 */           ChannelPipeline pipeline = ch.pipeline();
/* =3:77 */           pipeline.addLast("timer", new ReadTimeoutHandler(30));
/* =4:78 */           if (session.getPacketProtocol().needsPacketEncryptor()) {
/* =5:79 */             pipeline.addLast("encryption", new TcpPacketEncryptor(session));
/* =6:   */           }
/* =7:82 */           if (session.getPacketProtocol().needsPacketSizer()) {
/* =8:83 */             pipeline.addLast("sizer", new TcpPacketSizer(session));
/* =9:   */           }
/* >0:86 */           pipeline.addLast("codec", new TcpPacketCodec(session));
/* >1:87 */           pipeline.addLast("manager", session);
/* >2:88 */           server.addSession(session);
/* >3:   */         }
/* >4:90 */       }).group(group).localAddress(server.getHost(), server.getPort())).bind().syncUninterruptibly().channel());
/* >5:   */   }
/* >6:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.packetlib.tcp.TcpSessionFactory
 * JD-Core Version:    0.7.0.1
 */