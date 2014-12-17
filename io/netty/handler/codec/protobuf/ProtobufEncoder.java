/*  1:   */ package io.netty.handler.codec.protobuf;
/*  2:   */ 
/*  3:   */ import com.google.protobuf.MessageLite;
/*  4:   */ import com.google.protobuf.MessageLite.Builder;
/*  5:   */ import com.google.protobuf.MessageLiteOrBuilder;
/*  6:   */ import io.netty.buffer.Unpooled;
/*  7:   */ import io.netty.channel.ChannelHandler.Sharable;
/*  8:   */ import io.netty.channel.ChannelHandlerContext;
/*  9:   */ import io.netty.handler.codec.MessageToMessageEncoder;
/* 10:   */ import java.util.List;
/* 11:   */ 
/* 12:   */ @ChannelHandler.Sharable
/* 13:   */ public class ProtobufEncoder
/* 14:   */   extends MessageToMessageEncoder<MessageLiteOrBuilder>
/* 15:   */ {
/* 16:   */   protected void encode(ChannelHandlerContext ctx, MessageLiteOrBuilder msg, List<Object> out)
/* 17:   */     throws Exception
/* 18:   */   {
/* 19:65 */     if ((msg instanceof MessageLite))
/* 20:   */     {
/* 21:66 */       out.add(Unpooled.wrappedBuffer(((MessageLite)msg).toByteArray()));
/* 22:67 */       return;
/* 23:   */     }
/* 24:69 */     if ((msg instanceof MessageLite.Builder)) {
/* 25:70 */       out.add(Unpooled.wrappedBuffer(((MessageLite.Builder)msg).build().toByteArray()));
/* 26:   */     }
/* 27:   */   }
/* 28:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.protobuf.ProtobufEncoder
 * JD-Core Version:    0.7.0.1
 */