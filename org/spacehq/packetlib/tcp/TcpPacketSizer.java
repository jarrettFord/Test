/*  1:   */ package org.spacehq.packetlib.tcp;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.buffer.Unpooled;
/*  5:   */ import io.netty.channel.ChannelHandlerContext;
/*  6:   */ import io.netty.handler.codec.ByteToMessageCodec;
/*  7:   */ import io.netty.handler.codec.CorruptedFrameException;
/*  8:   */ import java.util.List;
/*  9:   */ import org.spacehq.packetlib.Session;
/* 10:   */ import org.spacehq.packetlib.packet.PacketHeader;
/* 11:   */ import org.spacehq.packetlib.packet.PacketProtocol;
/* 12:   */ import org.spacehq.packetlib.tcp.io.ByteBufNetInput;
/* 13:   */ import org.spacehq.packetlib.tcp.io.ByteBufNetOutput;
/* 14:   */ 
/* 15:   */ public class TcpPacketSizer
/* 16:   */   extends ByteToMessageCodec<ByteBuf>
/* 17:   */ {
/* 18:   */   private Session session;
/* 19:   */   
/* 20:   */   public TcpPacketSizer(Session session)
/* 21:   */   {
/* 22:19 */     this.session = session;
/* 23:   */   }
/* 24:   */   
/* 25:   */   public void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out)
/* 26:   */     throws Exception
/* 27:   */   {
/* 28:24 */     int length = in.readableBytes();
/* 29:25 */     out.ensureWritable(this.session.getPacketProtocol().getPacketHeader().getLengthSize(length) + length);
/* 30:26 */     this.session.getPacketProtocol().getPacketHeader().writeLength(new ByteBufNetOutput(out), length);
/* 31:27 */     out.writeBytes(in);
/* 32:   */   }
/* 33:   */   
/* 34:   */   protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out)
/* 35:   */     throws Exception
/* 36:   */   {
/* 37:32 */     int size = this.session.getPacketProtocol().getPacketHeader().getLengthSize();
/* 38:33 */     if (size > 0)
/* 39:   */     {
/* 40:34 */       buf.markReaderIndex();
/* 41:35 */       byte[] lengthBytes = new byte[size];
/* 42:36 */       for (int index = 0; index < lengthBytes.length; index++)
/* 43:   */       {
/* 44:37 */         if (!buf.isReadable())
/* 45:   */         {
/* 46:38 */           buf.resetReaderIndex();
/* 47:39 */           return;
/* 48:   */         }
/* 49:42 */         lengthBytes[index] = buf.readByte();
/* 50:43 */         if (((this.session.getPacketProtocol().getPacketHeader().isLengthVariable()) && (lengthBytes[index] >= 0)) || (index == size - 1))
/* 51:   */         {
/* 52:44 */           int length = this.session.getPacketProtocol().getPacketHeader().readLength(new ByteBufNetInput(Unpooled.wrappedBuffer(lengthBytes)), buf.readableBytes());
/* 53:45 */           if (buf.readableBytes() < length)
/* 54:   */           {
/* 55:46 */             buf.resetReaderIndex();
/* 56:47 */             return;
/* 57:   */           }
/* 58:50 */           out.add(buf.readBytes(length));
/* 59:51 */           return;
/* 60:   */         }
/* 61:   */       }
/* 62:55 */       throw new CorruptedFrameException("Length is too long.");
/* 63:   */     }
/* 64:57 */     out.add(buf.readBytes(buf.readableBytes()));
/* 65:   */   }
/* 66:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.packetlib.tcp.TcpPacketSizer
 * JD-Core Version:    0.7.0.1
 */