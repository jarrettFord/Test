/*  1:   */ package org.spacehq.packetlib.tcp;
/*  2:   */ 
/*  3:   */ import io.netty.bootstrap.ChannelFactory;
/*  4:   */ import io.netty.channel.EventLoop;
/*  5:   */ import io.netty.channel.socket.oio.OioSocketChannel;
/*  6:   */ import java.net.Proxy;
/*  7:   */ import java.net.Socket;
/*  8:   */ 
/*  9:   */ public class ProxyOioChannelFactory
/* 10:   */   implements ChannelFactory<OioSocketChannel>
/* 11:   */ {
/* 12:   */   private Proxy proxy;
/* 13:   */   
/* 14:   */   public ProxyOioChannelFactory(Proxy proxy)
/* 15:   */   {
/* 16:15 */     this.proxy = proxy;
/* 17:   */   }
/* 18:   */   
/* 19:   */   public OioSocketChannel newChannel(EventLoop eventLoop)
/* 20:   */   {
/* 21:19 */     return new OioSocketChannel(new Socket(this.proxy));
/* 22:   */   }
/* 23:   */   
/* 24:   */   public OioSocketChannel newChannel()
/* 25:   */   {
/* 26:25 */     return null;
/* 27:   */   }
/* 28:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.packetlib.tcp.ProxyOioChannelFactory
 * JD-Core Version:    0.7.0.1
 */