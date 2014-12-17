/*  1:   */ package org.spacehq.packetlib.tcp;
/*  2:   */ 
/*  3:   */ import io.netty.bootstrap.Bootstrap;
/*  4:   */ import io.netty.channel.ChannelHandlerContext;
/*  5:   */ import io.netty.channel.EventLoopGroup;
/*  6:   */ import java.util.Map;
/*  7:   */ import org.spacehq.packetlib.Server;
/*  8:   */ import org.spacehq.packetlib.packet.PacketProtocol;
/*  9:   */ 
/* 10:   */ public class TcpServerSession
/* 11:   */   extends TcpSession
/* 12:   */ {
/* 13:   */   private Server server;
/* 14:   */   
/* 15:   */   public TcpServerSession(String host, int port, PacketProtocol protocol, EventLoopGroup group, Bootstrap bootstrap, Server server)
/* 16:   */   {
/* 17:16 */     super(host, port, protocol, group, bootstrap);
/* 18:17 */     this.server = server;
/* 19:   */   }
/* 20:   */   
/* 21:   */   public void connect() {}
/* 22:   */   
/* 23:   */   public Map<String, Object> getFlags()
/* 24:   */   {
/* 25:26 */     Map<String, Object> ret = super.getFlags();
/* 26:27 */     ret.putAll(this.server.getGlobalFlags());
/* 27:28 */     return ret;
/* 28:   */   }
/* 29:   */   
/* 30:   */   public void channelInactive(ChannelHandlerContext ctx)
/* 31:   */     throws Exception
/* 32:   */   {
/* 33:33 */     super.channelInactive(ctx);
/* 34:34 */     this.server.removeSession(this);
/* 35:   */   }
/* 36:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.packetlib.tcp.TcpServerSession
 * JD-Core Version:    0.7.0.1
 */