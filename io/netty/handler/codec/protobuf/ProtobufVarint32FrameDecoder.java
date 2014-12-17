/*  1:   */ package io.netty.handler.codec.protobuf;
/*  2:   */ 
/*  3:   */ import com.google.protobuf.CodedInputStream;
/*  4:   */ import io.netty.buffer.ByteBuf;
/*  5:   */ import io.netty.channel.ChannelHandlerContext;
/*  6:   */ import io.netty.handler.codec.ByteToMessageDecoder;
/*  7:   */ import io.netty.handler.codec.CorruptedFrameException;
/*  8:   */ import java.util.List;
/*  9:   */ 
/* 10:   */ public class ProtobufVarint32FrameDecoder
/* 11:   */   extends ByteToMessageDecoder
/* 12:   */ {
/* 13:   */   protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/* 14:   */     throws Exception
/* 15:   */   {
/* 16:49 */     in.markReaderIndex();
/* 17:50 */     byte[] buf = new byte[5];
/* 18:51 */     for (int i = 0; i < buf.length; i++)
/* 19:   */     {
/* 20:52 */       if (!in.isReadable())
/* 21:   */       {
/* 22:53 */         in.resetReaderIndex();
/* 23:54 */         return;
/* 24:   */       }
/* 25:57 */       buf[i] = in.readByte();
/* 26:58 */       if (buf[i] >= 0)
/* 27:   */       {
/* 28:59 */         int length = CodedInputStream.newInstance(buf, 0, i + 1).readRawVarint32();
/* 29:60 */         if (length < 0) {
/* 30:61 */           throw new CorruptedFrameException("negative length: " + length);
/* 31:   */         }
/* 32:64 */         if (in.readableBytes() < length)
/* 33:   */         {
/* 34:65 */           in.resetReaderIndex();
/* 35:66 */           return;
/* 36:   */         }
/* 37:68 */         out.add(in.readBytes(length));
/* 38:69 */         return;
/* 39:   */       }
/* 40:   */     }
/* 41:75 */     throw new CorruptedFrameException("length wider than 32-bit");
/* 42:   */   }
/* 43:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder
 * JD-Core Version:    0.7.0.1
 */