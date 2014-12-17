/*  1:   */ package org.spacehq.packetlib.tcp;
/*  2:   */ 
/*  3:   */ import io.netty.channel.Channel;
/*  4:   */ import io.netty.channel.ChannelFuture;
/*  5:   */ import io.netty.channel.EventLoopGroup;
/*  6:   */ import io.netty.util.concurrent.Future;
/*  7:   */ import org.spacehq.packetlib.ConnectionListener;
/*  8:   */ 
/*  9:   */ public class TcpConnectionListener
/* 10:   */   implements ConnectionListener
/* 11:   */ {
/* 12:   */   private String host;
/* 13:   */   private int port;
/* 14:   */   private EventLoopGroup group;
/* 15:   */   private Channel channel;
/* 16:   */   
/* 17:   */   public TcpConnectionListener(String host, int port, EventLoopGroup group, Channel channel)
/* 18:   */   {
/* 19:15 */     this.host = host;
/* 20:16 */     this.port = port;
/* 21:17 */     this.group = group;
/* 22:18 */     this.channel = channel;
/* 23:   */   }
/* 24:   */   
/* 25:   */   public String getHost()
/* 26:   */   {
/* 27:23 */     return this.host;
/* 28:   */   }
/* 29:   */   
/* 30:   */   public int getPort()
/* 31:   */   {
/* 32:28 */     return this.port;
/* 33:   */   }
/* 34:   */   
/* 35:   */   public boolean isListening()
/* 36:   */   {
/* 37:33 */     return this.channel.isOpen();
/* 38:   */   }
/* 39:   */   
/* 40:   */   public void close()
/* 41:   */   {
/* 42:38 */     if (this.channel.isOpen()) {
/* 43:39 */       this.channel.close().syncUninterruptibly();
/* 44:   */     }
/* 45:   */     try
/* 46:   */     {
/* 47:43 */       this.group.shutdownGracefully().sync();
/* 48:   */     }
/* 49:   */     catch (InterruptedException e)
/* 50:   */     {
/* 51:45 */       e.printStackTrace();
/* 52:   */     }
/* 53:   */   }
/* 54:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.packetlib.tcp.TcpConnectionListener
 * JD-Core Version:    0.7.0.1
 */