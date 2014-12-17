/*   1:    */ package io.netty.handler.codec;
/*   2:    */ 
/*   3:    */ import io.netty.channel.ChannelHandlerContext;
/*   4:    */ import io.netty.channel.ChannelInboundHandlerAdapter;
/*   5:    */ import io.netty.util.ReferenceCountUtil;
/*   6:    */ import io.netty.util.internal.RecyclableArrayList;
/*   7:    */ import io.netty.util.internal.TypeParameterMatcher;
/*   8:    */ import java.util.List;
/*   9:    */ 
/*  10:    */ public abstract class MessageToMessageDecoder<I>
/*  11:    */   extends ChannelInboundHandlerAdapter
/*  12:    */ {
/*  13:    */   private final TypeParameterMatcher matcher;
/*  14:    */   
/*  15:    */   protected MessageToMessageDecoder()
/*  16:    */   {
/*  17: 61 */     this.matcher = TypeParameterMatcher.find(this, MessageToMessageDecoder.class, "I");
/*  18:    */   }
/*  19:    */   
/*  20:    */   protected MessageToMessageDecoder(Class<? extends I> inboundMessageType)
/*  21:    */   {
/*  22: 70 */     this.matcher = TypeParameterMatcher.get(inboundMessageType);
/*  23:    */   }
/*  24:    */   
/*  25:    */   public boolean acceptInboundMessage(Object msg)
/*  26:    */     throws Exception
/*  27:    */   {
/*  28: 78 */     return this.matcher.match(msg);
/*  29:    */   }
/*  30:    */   
/*  31:    */   public void channelRead(ChannelHandlerContext ctx, Object msg)
/*  32:    */     throws Exception
/*  33:    */   {
/*  34: 83 */     RecyclableArrayList out = RecyclableArrayList.newInstance();
/*  35:    */     try
/*  36:    */     {
/*  37: 85 */       if (acceptInboundMessage(msg))
/*  38:    */       {
/*  39: 87 */         I cast = msg;
/*  40:    */         try
/*  41:    */         {
/*  42: 89 */           decode(ctx, cast, out);
/*  43:    */         }
/*  44:    */         finally
/*  45:    */         {
/*  46: 91 */           ReferenceCountUtil.release(cast);
/*  47:    */         }
/*  48:    */       }
/*  49:    */       else
/*  50:    */       {
/*  51: 94 */         out.add(msg);
/*  52:    */       }
/*  53:    */     }
/*  54:    */     catch (DecoderException e)
/*  55:    */     {
/*  56:    */       int size;
/*  57:    */       int i;
/*  58: 97 */       throw e;
/*  59:    */     }
/*  60:    */     catch (Exception e)
/*  61:    */     {
/*  62: 99 */       throw new DecoderException(e);
/*  63:    */     }
/*  64:    */     finally
/*  65:    */     {
/*  66:101 */       int size = out.size();
/*  67:102 */       for (int i = 0; i < size; i++) {
/*  68:103 */         ctx.fireChannelRead(out.get(i));
/*  69:    */       }
/*  70:105 */       out.recycle();
/*  71:    */     }
/*  72:    */   }
/*  73:    */   
/*  74:    */   protected abstract void decode(ChannelHandlerContext paramChannelHandlerContext, I paramI, List<Object> paramList)
/*  75:    */     throws Exception;
/*  76:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.MessageToMessageDecoder
 * JD-Core Version:    0.7.0.1
 */