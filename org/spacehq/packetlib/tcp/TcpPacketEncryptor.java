/*  1:   */ package org.spacehq.packetlib.tcp;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.buffer.ByteBufAllocator;
/*  5:   */ import io.netty.channel.ChannelHandlerContext;
/*  6:   */ import io.netty.handler.codec.ByteToMessageCodec;
/*  7:   */ import java.util.List;
/*  8:   */ import org.spacehq.packetlib.Session;
/*  9:   */ import org.spacehq.packetlib.crypt.PacketEncryption;
/* 10:   */ import org.spacehq.packetlib.packet.PacketProtocol;
/* 11:   */ 
/* 12:   */ public class TcpPacketEncryptor
/* 13:   */   extends ByteToMessageCodec<ByteBuf>
/* 14:   */ {
/* 15:   */   private Session session;
/* 16:13 */   private byte[] decryptedArray = new byte[0];
/* 17:14 */   private byte[] encryptedArray = new byte[0];
/* 18:   */   
/* 19:   */   public TcpPacketEncryptor(Session session)
/* 20:   */   {
/* 21:17 */     this.session = session;
/* 22:   */   }
/* 23:   */   
/* 24:   */   public void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out)
/* 25:   */     throws Exception
/* 26:   */   {
/* 27:22 */     if (this.session.getPacketProtocol().getEncryption() != null)
/* 28:   */     {
/* 29:23 */       int length = in.readableBytes();
/* 30:24 */       byte[] bytes = getBytes(in);
/* 31:25 */       int outLength = this.session.getPacketProtocol().getEncryption().getEncryptOutputSize(length);
/* 32:26 */       if (this.encryptedArray.length < outLength) {
/* 33:27 */         this.encryptedArray = new byte[outLength];
/* 34:   */       }
/* 35:30 */       out.writeBytes(this.encryptedArray, 0, this.session.getPacketProtocol().getEncryption().encrypt(bytes, 0, length, this.encryptedArray, 0));
/* 36:   */     }
/* 37:   */     else
/* 38:   */     {
/* 39:32 */       out.writeBytes(in);
/* 40:   */     }
/* 41:   */   }
/* 42:   */   
/* 43:   */   protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out)
/* 44:   */     throws Exception
/* 45:   */   {
/* 46:38 */     if (this.session.getPacketProtocol().getEncryption() != null)
/* 47:   */     {
/* 48:39 */       int length = buf.readableBytes();
/* 49:40 */       byte[] bytes = getBytes(buf);
/* 50:41 */       ByteBuf result = ctx.alloc().heapBuffer(this.session.getPacketProtocol().getEncryption().getDecryptOutputSize(length));
/* 51:42 */       result.writerIndex(this.session.getPacketProtocol().getEncryption().decrypt(bytes, 0, length, result.array(), result.arrayOffset()));
/* 52:43 */       out.add(result);
/* 53:   */     }
/* 54:   */     else
/* 55:   */     {
/* 56:45 */       out.add(buf.readBytes(buf.readableBytes()));
/* 57:   */     }
/* 58:   */   }
/* 59:   */   
/* 60:   */   private byte[] getBytes(ByteBuf buf)
/* 61:   */   {
/* 62:50 */     int length = buf.readableBytes();
/* 63:51 */     if (this.decryptedArray.length < length) {
/* 64:52 */       this.decryptedArray = new byte[length];
/* 65:   */     }
/* 66:55 */     buf.readBytes(this.decryptedArray, 0, length);
/* 67:56 */     return this.decryptedArray;
/* 68:   */   }
/* 69:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.packetlib.tcp.TcpPacketEncryptor
 * JD-Core Version:    0.7.0.1
 */