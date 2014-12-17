/*  1:   */ package io.netty.handler.codec.spdy;
/*  2:   */ 
/*  3:   */ import io.netty.channel.ChannelHandlerContext;
/*  4:   */ import io.netty.handler.codec.MessageToMessageCodec;
/*  5:   */ import io.netty.handler.codec.http.HttpHeaders;
/*  6:   */ import io.netty.handler.codec.http.HttpMessage;
/*  7:   */ import io.netty.util.ReferenceCountUtil;
/*  8:   */ import java.util.LinkedList;
/*  9:   */ import java.util.List;
/* 10:   */ import java.util.Queue;
/* 11:   */ 
/* 12:   */ public class SpdyHttpResponseStreamIdHandler
/* 13:   */   extends MessageToMessageCodec<Object, HttpMessage>
/* 14:   */ {
/* 15:34 */   private static final Integer NO_ID = Integer.valueOf(-1);
/* 16:35 */   private final Queue<Integer> ids = new LinkedList();
/* 17:   */   
/* 18:   */   public boolean acceptInboundMessage(Object msg)
/* 19:   */     throws Exception
/* 20:   */   {
/* 21:39 */     return ((msg instanceof HttpMessage)) || ((msg instanceof SpdyRstStreamFrame));
/* 22:   */   }
/* 23:   */   
/* 24:   */   protected void encode(ChannelHandlerContext ctx, HttpMessage msg, List<Object> out)
/* 25:   */     throws Exception
/* 26:   */   {
/* 27:44 */     Integer id = (Integer)this.ids.poll();
/* 28:45 */     if ((id != null) && (id.intValue() != NO_ID.intValue()) && (!msg.headers().contains("X-SPDY-Stream-ID"))) {
/* 29:46 */       SpdyHttpHeaders.setStreamId(msg, id.intValue());
/* 30:   */     }
/* 31:49 */     out.add(ReferenceCountUtil.retain(msg));
/* 32:   */   }
/* 33:   */   
/* 34:   */   protected void decode(ChannelHandlerContext ctx, Object msg, List<Object> out)
/* 35:   */     throws Exception
/* 36:   */   {
/* 37:54 */     if ((msg instanceof HttpMessage))
/* 38:   */     {
/* 39:55 */       boolean contains = ((HttpMessage)msg).headers().contains("X-SPDY-Stream-ID");
/* 40:56 */       if (!contains) {
/* 41:57 */         this.ids.add(NO_ID);
/* 42:   */       } else {
/* 43:59 */         this.ids.add(Integer.valueOf(SpdyHttpHeaders.getStreamId((HttpMessage)msg)));
/* 44:   */       }
/* 45:   */     }
/* 46:61 */     else if ((msg instanceof SpdyRstStreamFrame))
/* 47:   */     {
/* 48:62 */       this.ids.remove(Integer.valueOf(((SpdyRstStreamFrame)msg).streamId()));
/* 49:   */     }
/* 50:65 */     out.add(ReferenceCountUtil.retain(msg));
/* 51:   */   }
/* 52:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.SpdyHttpResponseStreamIdHandler
 * JD-Core Version:    0.7.0.1
 */