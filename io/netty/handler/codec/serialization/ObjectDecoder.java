/*  1:   */ package io.netty.handler.codec.serialization;
/*  2:   */ 
/*  3:   */ import io.netty.buffer.ByteBuf;
/*  4:   */ import io.netty.buffer.ByteBufInputStream;
/*  5:   */ import io.netty.channel.ChannelHandlerContext;
/*  6:   */ import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
/*  7:   */ 
/*  8:   */ public class ObjectDecoder
/*  9:   */   extends LengthFieldBasedFrameDecoder
/* 10:   */ {
/* 11:   */   private final ClassResolver classResolver;
/* 12:   */   
/* 13:   */   public ObjectDecoder(ClassResolver classResolver)
/* 14:   */   {
/* 15:48 */     this(1048576, classResolver);
/* 16:   */   }
/* 17:   */   
/* 18:   */   public ObjectDecoder(int maxObjectSize, ClassResolver classResolver)
/* 19:   */   {
/* 20:62 */     super(maxObjectSize, 0, 4, 0, 4);
/* 21:63 */     this.classResolver = classResolver;
/* 22:   */   }
/* 23:   */   
/* 24:   */   protected Object decode(ChannelHandlerContext ctx, ByteBuf in)
/* 25:   */     throws Exception
/* 26:   */   {
/* 27:68 */     ByteBuf frame = (ByteBuf)super.decode(ctx, in);
/* 28:69 */     if (frame == null) {
/* 29:70 */       return null;
/* 30:   */     }
/* 31:73 */     return new CompactObjectInputStream(new ByteBufInputStream(frame), this.classResolver).readObject();
/* 32:   */   }
/* 33:   */   
/* 34:   */   protected ByteBuf extractFrame(ChannelHandlerContext ctx, ByteBuf buffer, int index, int length)
/* 35:   */   {
/* 36:79 */     return buffer.slice(index, length);
/* 37:   */   }
/* 38:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.serialization.ObjectDecoder
 * JD-Core Version:    0.7.0.1
 */