/*   1:    */ package io.netty.handler.codec;
/*   2:    */ 
/*   3:    */ import io.netty.channel.ChannelHandlerContext;
/*   4:    */ import io.netty.channel.ChannelOutboundHandlerAdapter;
/*   5:    */ import io.netty.channel.ChannelPromise;
/*   6:    */ import io.netty.util.ReferenceCountUtil;
/*   7:    */ import io.netty.util.internal.RecyclableArrayList;
/*   8:    */ import io.netty.util.internal.StringUtil;
/*   9:    */ import io.netty.util.internal.TypeParameterMatcher;
/*  10:    */ import java.util.List;
/*  11:    */ 
/*  12:    */ public abstract class MessageToMessageEncoder<I>
/*  13:    */   extends ChannelOutboundHandlerAdapter
/*  14:    */ {
/*  15:    */   private final TypeParameterMatcher matcher;
/*  16:    */   
/*  17:    */   protected MessageToMessageEncoder()
/*  18:    */   {
/*  19: 60 */     this.matcher = TypeParameterMatcher.find(this, MessageToMessageEncoder.class, "I");
/*  20:    */   }
/*  21:    */   
/*  22:    */   protected MessageToMessageEncoder(Class<? extends I> outboundMessageType)
/*  23:    */   {
/*  24: 69 */     this.matcher = TypeParameterMatcher.get(outboundMessageType);
/*  25:    */   }
/*  26:    */   
/*  27:    */   public boolean acceptOutboundMessage(Object msg)
/*  28:    */     throws Exception
/*  29:    */   {
/*  30: 77 */     return this.matcher.match(msg);
/*  31:    */   }
/*  32:    */   
/*  33:    */   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/*  34:    */     throws Exception
/*  35:    */   {
/*  36: 82 */     RecyclableArrayList out = null;
/*  37:    */     try
/*  38:    */     {
/*  39: 84 */       if (acceptOutboundMessage(msg))
/*  40:    */       {
/*  41: 85 */         out = RecyclableArrayList.newInstance();
/*  42:    */         
/*  43: 87 */         I cast = msg;
/*  44:    */         try
/*  45:    */         {
/*  46: 89 */           encode(ctx, cast, out);
/*  47:    */         }
/*  48:    */         finally
/*  49:    */         {
/*  50: 91 */           ReferenceCountUtil.release(cast);
/*  51:    */         }
/*  52: 94 */         if (out.isEmpty())
/*  53:    */         {
/*  54: 95 */           out.recycle();
/*  55: 96 */           out = null;
/*  56:    */           
/*  57: 98 */           throw new EncoderException(StringUtil.simpleClassName(this) + " must produce at least one message.");
/*  58:    */         }
/*  59:    */       }
/*  60:    */       else
/*  61:    */       {
/*  62:102 */         ctx.write(msg, promise);
/*  63:    */       }
/*  64:    */     }
/*  65:    */     catch (EncoderException e)
/*  66:    */     {
/*  67:    */       int sizeMinusOne;
/*  68:    */       ChannelPromise voidPromise;
/*  69:    */       boolean isVoidPromise;
/*  70:    */       int i;
/*  71:    */       ChannelPromise p;
/*  72:    */       ChannelPromise p;
/*  73:105 */       throw e;
/*  74:    */     }
/*  75:    */     catch (Throwable t)
/*  76:    */     {
/*  77:107 */       throw new EncoderException(t);
/*  78:    */     }
/*  79:    */     finally
/*  80:    */     {
/*  81:109 */       if (out != null)
/*  82:    */       {
/*  83:110 */         int sizeMinusOne = out.size() - 1;
/*  84:111 */         if (sizeMinusOne == 0)
/*  85:    */         {
/*  86:112 */           ctx.write(out.get(0), promise);
/*  87:    */         }
/*  88:113 */         else if (sizeMinusOne > 0)
/*  89:    */         {
/*  90:116 */           ChannelPromise voidPromise = ctx.voidPromise();
/*  91:117 */           boolean isVoidPromise = promise == voidPromise;
/*  92:118 */           for (int i = 0; i < sizeMinusOne; i++)
/*  93:    */           {
/*  94:    */             ChannelPromise p;
/*  95:    */             ChannelPromise p;
/*  96:120 */             if (isVoidPromise) {
/*  97:121 */               p = voidPromise;
/*  98:    */             } else {
/*  99:123 */               p = ctx.newPromise();
/* 100:    */             }
/* 101:125 */             ctx.write(out.get(i), p);
/* 102:    */           }
/* 103:127 */           ctx.write(out.get(sizeMinusOne), promise);
/* 104:    */         }
/* 105:129 */         out.recycle();
/* 106:    */       }
/* 107:    */     }
/* 108:    */   }
/* 109:    */   
/* 110:    */   protected abstract void encode(ChannelHandlerContext paramChannelHandlerContext, I paramI, List<Object> paramList)
/* 111:    */     throws Exception;
/* 112:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.MessageToMessageEncoder
 * JD-Core Version:    0.7.0.1
 */