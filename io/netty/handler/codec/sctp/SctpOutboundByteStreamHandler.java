/*  1:   */ package io.netty.handler.codec.sctp;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.channel.ChannelHandlerContext;
/*  5:   */ import io.netty.channel.sctp.SctpMessage;
/*  6:   */ import io.netty.handler.codec.MessageToMessageEncoder;
/*  7:   */ import java.util.List;
/*  8:   */ 
/*  9:   */ public class SctpOutboundByteStreamHandler
/* 10:   */   extends MessageToMessageEncoder<ByteBuf>
/* 11:   */ {
/* 12:   */   private final int streamIdentifier;
/* 13:   */   private final int protocolIdentifier;
/* 14:   */   
/* 15:   */   public SctpOutboundByteStreamHandler(int streamIdentifier, int protocolIdentifier)
/* 16:   */   {
/* 17:39 */     this.streamIdentifier = streamIdentifier;
/* 18:40 */     this.protocolIdentifier = protocolIdentifier;
/* 19:   */   }
/* 20:   */   
/* 21:   */   protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out)
/* 22:   */     throws Exception
/* 23:   */   {
/* 24:45 */     out.add(new SctpMessage(this.streamIdentifier, this.protocolIdentifier, msg.retain()));
/* 25:   */   }
/* 26:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.sctp.SctpOutboundByteStreamHandler
 * JD-Core Version:    0.7.0.1
 */