/*  1:   */ package io.netty.handler.codec.sctp;
/*  2:   */ 
/*  3:   */ import io.netty.channel.sctp.SctpMessage;
/*  4:   */ import io.netty.handler.codec.CodecException;
/*  5:   */ import io.netty.handler.codec.MessageToMessageDecoder;
/*  6:   */ 
/*  7:   */ public abstract class SctpMessageToMessageDecoder
/*  8:   */   extends MessageToMessageDecoder<SctpMessage>
/*  9:   */ {
/* 10:   */   public boolean acceptInboundMessage(Object msg)
/* 11:   */     throws Exception
/* 12:   */   {
/* 13:27 */     if ((msg instanceof SctpMessage))
/* 14:   */     {
/* 15:28 */       SctpMessage sctpMsg = (SctpMessage)msg;
/* 16:29 */       if (sctpMsg.isComplete()) {
/* 17:30 */         return true;
/* 18:   */       }
/* 19:33 */       throw new CodecException(String.format("Received SctpMessage is not complete, please add %s in the pipeline before this handler", new Object[] { SctpMessageCompletionHandler.class.getSimpleName() }));
/* 20:   */     }
/* 21:36 */     return false;
/* 22:   */   }
/* 23:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.sctp.SctpMessageToMessageDecoder
 * JD-Core Version:    0.7.0.1
 */