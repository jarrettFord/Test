/*  1:   */ package io.netty.handler.codec.socks;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.channel.ChannelHandler.Sharable;
/*  5:   */ import io.netty.channel.ChannelHandlerContext;
/*  6:   */ import io.netty.handler.codec.MessageToByteEncoder;
/*  7:   */ 
/*  8:   */ @ChannelHandler.Sharable
/*  9:   */ public class SocksMessageEncoder
/* 10:   */   extends MessageToByteEncoder<SocksMessage>
/* 11:   */ {
/* 12:   */   private static final String name = "SOCKS_MESSAGE_ENCODER";
/* 13:   */   
/* 14:   */   @Deprecated
/* 15:   */   public static String getName()
/* 16:   */   {
/* 17:38 */     return "SOCKS_MESSAGE_ENCODER";
/* 18:   */   }
/* 19:   */   
/* 20:   */   protected void encode(ChannelHandlerContext ctx, SocksMessage msg, ByteBuf out)
/* 21:   */     throws Exception
/* 22:   */   {
/* 23:44 */     msg.encodeAsByteBuf(out);
/* 24:   */   }
/* 25:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.socks.SocksMessageEncoder
 * JD-Core Version:    0.7.0.1
 */