/*  1:   */ package io.netty.handler.codec.sctp;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.channel.ChannelHandlerContext;
/*  5:   */ import io.netty.channel.sctp.SctpMessage;
/*  6:   */ import io.netty.handler.codec.CodecException;
/*  7:   */ import io.netty.handler.codec.MessageToMessageDecoder;
/*  8:   */ import java.util.List;
/*  9:   */ 
/* 10:   */ public class SctpInboundByteStreamHandler
/* 11:   */   extends MessageToMessageDecoder<SctpMessage>
/* 12:   */ {
/* 13:   */   private final int protocolIdentifier;
/* 14:   */   private final int streamIdentifier;
/* 15:   */   
/* 16:   */   public SctpInboundByteStreamHandler(int protocolIdentifier, int streamIdentifier)
/* 17:   */   {
/* 18:40 */     this.protocolIdentifier = protocolIdentifier;
/* 19:41 */     this.streamIdentifier = streamIdentifier;
/* 20:   */   }
/* 21:   */   
/* 22:   */   public final boolean acceptInboundMessage(Object msg)
/* 23:   */     throws Exception
/* 24:   */   {
/* 25:46 */     if (super.acceptInboundMessage(msg)) {
/* 26:47 */       return acceptInboundMessage((SctpMessage)msg);
/* 27:   */     }
/* 28:49 */     return false;
/* 29:   */   }
/* 30:   */   
/* 31:   */   protected boolean acceptInboundMessage(SctpMessage msg)
/* 32:   */   {
/* 33:53 */     return (msg.protocolIdentifier() == this.protocolIdentifier) && (msg.streamIdentifier() == this.streamIdentifier);
/* 34:   */   }
/* 35:   */   
/* 36:   */   protected void decode(ChannelHandlerContext ctx, SctpMessage msg, List<Object> out)
/* 37:   */     throws Exception
/* 38:   */   {
/* 39:58 */     if (!msg.isComplete()) {
/* 40:59 */       throw new CodecException(String.format("Received SctpMessage is not complete, please add %s in the pipeline before this handler", new Object[] { SctpMessageCompletionHandler.class.getSimpleName() }));
/* 41:   */     }
/* 42:62 */     out.add(msg.content().retain());
/* 43:   */   }
/* 44:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.sctp.SctpInboundByteStreamHandler
 * JD-Core Version:    0.7.0.1
 */