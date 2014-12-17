/*   1:    */ package io.netty.handler.codec;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.ChannelDuplexHandler;
/*   5:    */ import io.netty.channel.ChannelHandlerContext;
/*   6:    */ import io.netty.channel.ChannelPromise;
/*   7:    */ import io.netty.util.internal.TypeParameterMatcher;
/*   8:    */ import java.util.List;
/*   9:    */ 
/*  10:    */ public abstract class ByteToMessageCodec<I>
/*  11:    */   extends ChannelDuplexHandler
/*  12:    */ {
/*  13:    */   private final TypeParameterMatcher outboundMsgMatcher;
/*  14:    */   private final MessageToByteEncoder<I> encoder;
/*  15: 39 */   private final ByteToMessageDecoder decoder = new ByteToMessageDecoder()
/*  16:    */   {
/*  17:    */     public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/*  18:    */       throws Exception
/*  19:    */     {
/*  20: 42 */       ByteToMessageCodec.this.decode(ctx, in, out);
/*  21:    */     }
/*  22:    */     
/*  23:    */     protected void decodeLast(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/*  24:    */       throws Exception
/*  25:    */     {
/*  26: 47 */       ByteToMessageCodec.this.decodeLast(ctx, in, out);
/*  27:    */     }
/*  28:    */   };
/*  29:    */   
/*  30:    */   protected ByteToMessageCodec()
/*  31:    */   {
/*  32: 55 */     this(true);
/*  33:    */   }
/*  34:    */   
/*  35:    */   protected ByteToMessageCodec(Class<? extends I> outboundMessageType)
/*  36:    */   {
/*  37: 62 */     this(outboundMessageType, true);
/*  38:    */   }
/*  39:    */   
/*  40:    */   protected ByteToMessageCodec(boolean preferDirect)
/*  41:    */   {
/*  42: 73 */     this.outboundMsgMatcher = TypeParameterMatcher.find(this, ByteToMessageCodec.class, "I");
/*  43: 74 */     this.encoder = new Encoder(preferDirect);
/*  44:    */   }
/*  45:    */   
/*  46:    */   protected ByteToMessageCodec(Class<? extends I> outboundMessageType, boolean preferDirect)
/*  47:    */   {
/*  48: 86 */     checkForSharableAnnotation();
/*  49: 87 */     this.outboundMsgMatcher = TypeParameterMatcher.get(outboundMessageType);
/*  50: 88 */     this.encoder = new Encoder(preferDirect);
/*  51:    */   }
/*  52:    */   
/*  53:    */   private void checkForSharableAnnotation()
/*  54:    */   {
/*  55: 92 */     if (isSharable()) {
/*  56: 93 */       throw new IllegalStateException("@Sharable annotation is not allowed");
/*  57:    */     }
/*  58:    */   }
/*  59:    */   
/*  60:    */   public boolean acceptOutboundMessage(Object msg)
/*  61:    */     throws Exception
/*  62:    */   {
/*  63:103 */     return this.outboundMsgMatcher.match(msg);
/*  64:    */   }
/*  65:    */   
/*  66:    */   public void channelRead(ChannelHandlerContext ctx, Object msg)
/*  67:    */     throws Exception
/*  68:    */   {
/*  69:108 */     this.decoder.channelRead(ctx, msg);
/*  70:    */   }
/*  71:    */   
/*  72:    */   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/*  73:    */     throws Exception
/*  74:    */   {
/*  75:113 */     this.encoder.write(ctx, msg, promise);
/*  76:    */   }
/*  77:    */   
/*  78:    */   protected abstract void encode(ChannelHandlerContext paramChannelHandlerContext, I paramI, ByteBuf paramByteBuf)
/*  79:    */     throws Exception;
/*  80:    */   
/*  81:    */   protected abstract void decode(ChannelHandlerContext paramChannelHandlerContext, ByteBuf paramByteBuf, List<Object> paramList)
/*  82:    */     throws Exception;
/*  83:    */   
/*  84:    */   protected void decodeLast(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/*  85:    */     throws Exception
/*  86:    */   {
/*  87:130 */     decode(ctx, in, out);
/*  88:    */   }
/*  89:    */   
/*  90:    */   private final class Encoder
/*  91:    */     extends MessageToByteEncoder<I>
/*  92:    */   {
/*  93:    */     Encoder(boolean preferDirect)
/*  94:    */     {
/*  95:135 */       super();
/*  96:    */     }
/*  97:    */     
/*  98:    */     public boolean acceptOutboundMessage(Object msg)
/*  99:    */       throws Exception
/* 100:    */     {
/* 101:140 */       return ByteToMessageCodec.this.acceptOutboundMessage(msg);
/* 102:    */     }
/* 103:    */     
/* 104:    */     protected void encode(ChannelHandlerContext ctx, I msg, ByteBuf out)
/* 105:    */       throws Exception
/* 106:    */     {
/* 107:145 */       ByteToMessageCodec.this.encode(ctx, msg, out);
/* 108:    */     }
/* 109:    */   }
/* 110:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.ByteToMessageCodec
 * JD-Core Version:    0.7.0.1
 */