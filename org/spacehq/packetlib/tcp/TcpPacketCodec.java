/*  1:   */ package org.spacehq.packetlib.tcp;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.channel.ChannelHandlerContext;
/*  5:   */ import io.netty.handler.codec.ByteToMessageCodec;
/*  6:   */ import java.util.List;
/*  7:   */ import org.spacehq.packetlib.Session;
/*  8:   */ import org.spacehq.packetlib.event.session.PacketReceivedEvent;
/*  9:   */ import org.spacehq.packetlib.io.NetInput;
/* 10:   */ import org.spacehq.packetlib.io.NetOutput;
/* 11:   */ import org.spacehq.packetlib.packet.Packet;
/* 12:   */ import org.spacehq.packetlib.packet.PacketHeader;
/* 13:   */ import org.spacehq.packetlib.packet.PacketProtocol;
/* 14:   */ import org.spacehq.packetlib.tcp.io.ByteBufNetInput;
/* 15:   */ import org.spacehq.packetlib.tcp.io.ByteBufNetOutput;
/* 16:   */ 
/* 17:   */ public class TcpPacketCodec
/* 18:   */   extends ByteToMessageCodec<Packet>
/* 19:   */ {
/* 20:   */   private Session session;
/* 21:   */   
/* 22:   */   public TcpPacketCodec(Session session)
/* 23:   */   {
/* 24:21 */     this.session = session;
/* 25:   */   }
/* 26:   */   
/* 27:   */   public void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf buf)
/* 28:   */     throws Exception
/* 29:   */   {
/* 30:26 */     NetOutput out = new ByteBufNetOutput(buf);
/* 31:27 */     this.session.getPacketProtocol().getPacketHeader().writePacketId(out, this.session.getPacketProtocol().getOutgoingId(packet.getClass()));
/* 32:28 */     packet.write(out);
/* 33:   */   }
/* 34:   */   
/* 35:   */   protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out)
/* 36:   */     throws Exception
/* 37:   */   {
/* 38:33 */     int initial = buf.readerIndex();
/* 39:34 */     NetInput in = new ByteBufNetInput(buf);
/* 40:35 */     int id = this.session.getPacketProtocol().getPacketHeader().readPacketId(in);
/* 41:36 */     if (id == -1)
/* 42:   */     {
/* 43:37 */       buf.readerIndex(initial);
/* 44:38 */       return;
/* 45:   */     }
/* 46:41 */     Packet packet = this.session.getPacketProtocol().createIncomingPacket(id);
/* 47:42 */     packet.read(in);
/* 48:43 */     if (packet.isPriority()) {
/* 49:44 */       this.session.callEvent(new PacketReceivedEvent(this.session, packet));
/* 50:   */     }
/* 51:47 */     out.add(packet);
/* 52:   */   }
/* 53:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.packetlib.tcp.TcpPacketCodec
 * JD-Core Version:    0.7.0.1
 */