/*  1:   */ package io.netty.handler.codec.protobuf;
/*  2:   */ 
/*  3:   */ import com.google.protobuf.CodedOutputStream;
/*  4:   */ import io.netty.buffer.ByteBuf;
/*  5:   */ import io.netty.buffer.ByteBufOutputStream;
/*  6:   */ import io.netty.channel.ChannelHandler.Sharable;
/*  7:   */ import io.netty.channel.ChannelHandlerContext;
/*  8:   */ import io.netty.handler.codec.MessageToByteEncoder;
/*  9:   */ 
/* 10:   */ @ChannelHandler.Sharable
/* 11:   */ public class ProtobufVarint32LengthFieldPrepender
/* 12:   */   extends MessageToByteEncoder<ByteBuf>
/* 13:   */ {
/* 14:   */   protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out)
/* 15:   */     throws Exception
/* 16:   */   {
/* 17:45 */     int bodyLen = msg.readableBytes();
/* 18:46 */     int headerLen = CodedOutputStream.computeRawVarint32Size(bodyLen);
/* 19:47 */     out.ensureWritable(headerLen + bodyLen);
/* 20:   */     
/* 21:49 */     CodedOutputStream headerOut = CodedOutputStream.newInstance(new ByteBufOutputStream(out), headerLen);
/* 22:   */     
/* 23:51 */     headerOut.writeRawVarint32(bodyLen);
/* 24:52 */     headerOut.flush();
/* 25:   */     
/* 26:54 */     out.writeBytes(msg, msg.readerIndex(), bodyLen);
/* 27:   */   }
/* 28:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender
 * JD-Core Version:    0.7.0.1
 */