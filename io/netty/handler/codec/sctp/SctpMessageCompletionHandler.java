/*  1:   */ package io.netty.handler.codec.sctp;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.buffer.Unpooled;
/*  5:   */ import io.netty.channel.ChannelHandlerContext;
/*  6:   */ import io.netty.channel.sctp.SctpMessage;
/*  7:   */ import io.netty.handler.codec.MessageToMessageDecoder;
/*  8:   */ import java.util.HashMap;
/*  9:   */ import java.util.List;
/* 10:   */ import java.util.Map;
/* 11:   */ 
/* 12:   */ public class SctpMessageCompletionHandler
/* 13:   */   extends MessageToMessageDecoder<SctpMessage>
/* 14:   */ {
/* 15:36 */   private final Map<Integer, ByteBuf> fragments = new HashMap();
/* 16:   */   
/* 17:   */   protected void decode(ChannelHandlerContext ctx, SctpMessage msg, List<Object> out)
/* 18:   */     throws Exception
/* 19:   */   {
/* 20:40 */     ByteBuf byteBuf = msg.content();
/* 21:41 */     int protocolIdentifier = msg.protocolIdentifier();
/* 22:42 */     int streamIdentifier = msg.streamIdentifier();
/* 23:43 */     boolean isComplete = msg.isComplete();
/* 24:   */     ByteBuf frag;
/* 25:   */     ByteBuf frag;
/* 26:46 */     if (this.fragments.containsKey(Integer.valueOf(streamIdentifier))) {
/* 27:47 */       frag = (ByteBuf)this.fragments.remove(Integer.valueOf(streamIdentifier));
/* 28:   */     } else {
/* 29:49 */       frag = Unpooled.EMPTY_BUFFER;
/* 30:   */     }
/* 31:52 */     if ((isComplete) && (!frag.isReadable()))
/* 32:   */     {
/* 33:54 */       out.add(msg);
/* 34:   */     }
/* 35:55 */     else if ((!isComplete) && (frag.isReadable()))
/* 36:   */     {
/* 37:57 */       this.fragments.put(Integer.valueOf(streamIdentifier), Unpooled.wrappedBuffer(new ByteBuf[] { frag, byteBuf }));
/* 38:   */     }
/* 39:58 */     else if ((isComplete) && (frag.isReadable()))
/* 40:   */     {
/* 41:60 */       this.fragments.remove(Integer.valueOf(streamIdentifier));
/* 42:61 */       SctpMessage assembledMsg = new SctpMessage(protocolIdentifier, streamIdentifier, Unpooled.wrappedBuffer(new ByteBuf[] { frag, byteBuf }));
/* 43:   */       
/* 44:   */ 
/* 45:   */ 
/* 46:65 */       out.add(assembledMsg);
/* 47:   */     }
/* 48:   */     else
/* 49:   */     {
/* 50:68 */       this.fragments.put(Integer.valueOf(streamIdentifier), byteBuf);
/* 51:   */     }
/* 52:70 */     byteBuf.retain();
/* 53:   */   }
/* 54:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.sctp.SctpMessageCompletionHandler
 * JD-Core Version:    0.7.0.1
 */