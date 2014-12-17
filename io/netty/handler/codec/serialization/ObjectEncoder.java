/*  1:   */ package io.netty.handler.codec.serialization;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.buffer.ByteBufOutputStream;
/*  5:   */ import io.netty.channel.ChannelHandler.Sharable;
/*  6:   */ import io.netty.channel.ChannelHandlerContext;
/*  7:   */ import io.netty.handler.codec.MessageToByteEncoder;
/*  8:   */ import java.io.ObjectOutputStream;
/*  9:   */ import java.io.Serializable;
/* 10:   */ 
/* 11:   */ @ChannelHandler.Sharable
/* 12:   */ public class ObjectEncoder
/* 13:   */   extends MessageToByteEncoder<Serializable>
/* 14:   */ {
/* 15:38 */   private static final byte[] LENGTH_PLACEHOLDER = new byte[4];
/* 16:   */   
/* 17:   */   protected void encode(ChannelHandlerContext ctx, Serializable msg, ByteBuf out)
/* 18:   */     throws Exception
/* 19:   */   {
/* 20:42 */     int startIdx = out.writerIndex();
/* 21:   */     
/* 22:44 */     ByteBufOutputStream bout = new ByteBufOutputStream(out);
/* 23:45 */     bout.write(LENGTH_PLACEHOLDER);
/* 24:46 */     ObjectOutputStream oout = new CompactObjectOutputStream(bout);
/* 25:47 */     oout.writeObject(msg);
/* 26:48 */     oout.flush();
/* 27:49 */     oout.close();
/* 28:   */     
/* 29:51 */     int endIdx = out.writerIndex();
/* 30:   */     
/* 31:53 */     out.setInt(startIdx, endIdx - startIdx - 4);
/* 32:   */   }
/* 33:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.serialization.ObjectEncoder
 * JD-Core Version:    0.7.0.1
 */