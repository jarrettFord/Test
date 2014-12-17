/*  1:   */ package org.spacehq.packetlib.tcp;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.buffer.Unpooled;
/*  5:   */ import io.netty.channel.ChannelHandlerContext;
/*  6:   */ import io.netty.handler.codec.ByteToMessageCodec;
/*  7:   */ import io.netty.handler.codec.DecoderException;
/*  8:   */ import java.util.List;
/*  9:   */ import java.util.zip.Deflater;
/* 10:   */ import java.util.zip.Inflater;
/* 11:   */ import org.spacehq.packetlib.Session;
/* 12:   */ import org.spacehq.packetlib.tcp.io.ByteBufNetInput;
/* 13:   */ import org.spacehq.packetlib.tcp.io.ByteBufNetOutput;
/* 14:   */ 
/* 15:   */ public class TcpPacketCompression
/* 16:   */   extends ByteToMessageCodec<ByteBuf>
/* 17:   */ {
/* 18:   */   private static final int MAX_COMPRESSED_SIZE = 2097152;
/* 19:   */   private Session session;
/* 20:20 */   private Deflater deflater = new Deflater();
/* 21:21 */   private Inflater inflater = new Inflater();
/* 22:22 */   private byte[] buf = new byte[8192];
/* 23:   */   
/* 24:   */   public TcpPacketCompression(Session session)
/* 25:   */   {
/* 26:25 */     this.session = session;
/* 27:   */   }
/* 28:   */   
/* 29:   */   public void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out)
/* 30:   */     throws Exception
/* 31:   */   {
/* 32:30 */     int readable = in.readableBytes();
/* 33:31 */     ByteBufNetOutput output = new ByteBufNetOutput(out);
/* 34:32 */     if (readable < this.session.getCompressionThreshold())
/* 35:   */     {
/* 36:33 */       output.writeVarInt(0);
/* 37:34 */       out.writeBytes(in);
/* 38:   */     }
/* 39:   */     else
/* 40:   */     {
/* 41:36 */       byte[] bytes = new byte[readable];
/* 42:37 */       in.readBytes(bytes);
/* 43:38 */       output.writeVarInt(bytes.length);
/* 44:39 */       this.deflater.setInput(bytes, 0, readable);
/* 45:40 */       this.deflater.finish();
/* 46:41 */       while (!this.deflater.finished())
/* 47:   */       {
/* 48:42 */         int length = this.deflater.deflate(this.buf);
/* 49:43 */         output.writeBytes(this.buf, length);
/* 50:   */       }
/* 51:46 */       this.deflater.reset();
/* 52:   */     }
/* 53:   */   }
/* 54:   */   
/* 55:   */   protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out)
/* 56:   */     throws Exception
/* 57:   */   {
/* 58:52 */     if (buf.readableBytes() != 0)
/* 59:   */     {
/* 60:53 */       ByteBufNetInput in = new ByteBufNetInput(buf);
/* 61:54 */       int size = in.readVarInt();
/* 62:55 */       if (size == 0)
/* 63:   */       {
/* 64:56 */         out.add(in.readBytes(buf.readableBytes()));
/* 65:   */       }
/* 66:   */       else
/* 67:   */       {
/* 68:58 */         if (size < this.session.getCompressionThreshold()) {
/* 69:59 */           throw new DecoderException("Badly compressed packet: size of " + size + " is below threshold of " + this.session.getCompressionThreshold() + ".");
/* 70:   */         }
/* 71:62 */         if (size > 2097152) {
/* 72:63 */           throw new DecoderException("Badly compressed packet: size of " + size + " is larger than protocol maximum of " + 2097152 + ".");
/* 73:   */         }
/* 74:66 */         byte[] bytes = new byte[buf.readableBytes()];
/* 75:67 */         in.readBytes(bytes);
/* 76:68 */         this.inflater.setInput(bytes);
/* 77:69 */         byte[] inflated = new byte[size];
/* 78:70 */         this.inflater.inflate(inflated);
/* 79:71 */         out.add(Unpooled.wrappedBuffer(inflated));
/* 80:72 */         this.inflater.reset();
/* 81:   */       }
/* 82:   */     }
/* 83:   */   }
/* 84:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.packetlib.tcp.TcpPacketCompression
 * JD-Core Version:    0.7.0.1
 */