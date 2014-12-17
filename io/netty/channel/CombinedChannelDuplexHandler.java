/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import java.net.SocketAddress;
/*   4:    */ 
/*   5:    */ public class CombinedChannelDuplexHandler<I extends ChannelInboundHandler, O extends ChannelOutboundHandler>
/*   6:    */   extends ChannelDuplexHandler
/*   7:    */ {
/*   8:    */   private I inboundHandler;
/*   9:    */   private O outboundHandler;
/*  10:    */   
/*  11:    */   protected CombinedChannelDuplexHandler() {}
/*  12:    */   
/*  13:    */   public CombinedChannelDuplexHandler(I inboundHandler, O outboundHandler)
/*  14:    */   {
/*  15: 41 */     init(inboundHandler, outboundHandler);
/*  16:    */   }
/*  17:    */   
/*  18:    */   protected final void init(I inboundHandler, O outboundHandler)
/*  19:    */   {
/*  20: 53 */     validate(inboundHandler, outboundHandler);
/*  21: 54 */     this.inboundHandler = inboundHandler;
/*  22: 55 */     this.outboundHandler = outboundHandler;
/*  23:    */   }
/*  24:    */   
/*  25:    */   private void validate(I inboundHandler, O outboundHandler)
/*  26:    */   {
/*  27: 60 */     if (this.inboundHandler != null) {
/*  28: 61 */       throw new IllegalStateException("init() can not be invoked if " + CombinedChannelDuplexHandler.class.getSimpleName() + " was constructed with non-default constructor.");
/*  29:    */     }
/*  30: 66 */     if (inboundHandler == null) {
/*  31: 67 */       throw new NullPointerException("inboundHandler");
/*  32:    */     }
/*  33: 69 */     if (outboundHandler == null) {
/*  34: 70 */       throw new NullPointerException("outboundHandler");
/*  35:    */     }
/*  36: 72 */     if ((inboundHandler instanceof ChannelOutboundHandler)) {
/*  37: 73 */       throw new IllegalArgumentException("inboundHandler must not implement " + ChannelOutboundHandler.class.getSimpleName() + " to get combined.");
/*  38:    */     }
/*  39: 77 */     if ((outboundHandler instanceof ChannelInboundHandler)) {
/*  40: 78 */       throw new IllegalArgumentException("outboundHandler must not implement " + ChannelInboundHandler.class.getSimpleName() + " to get combined.");
/*  41:    */     }
/*  42:    */   }
/*  43:    */   
/*  44:    */   protected final I inboundHandler()
/*  45:    */   {
/*  46: 85 */     return this.inboundHandler;
/*  47:    */   }
/*  48:    */   
/*  49:    */   protected final O outboundHandler()
/*  50:    */   {
/*  51: 89 */     return this.outboundHandler;
/*  52:    */   }
/*  53:    */   
/*  54:    */   public void handlerAdded(ChannelHandlerContext ctx)
/*  55:    */     throws Exception
/*  56:    */   {
/*  57: 94 */     if (this.inboundHandler == null) {
/*  58: 95 */       throw new IllegalStateException("init() must be invoked before being added to a " + ChannelPipeline.class.getSimpleName() + " if " + CombinedChannelDuplexHandler.class.getSimpleName() + " was constructed with the default constructor.");
/*  59:    */     }
/*  60:    */     try
/*  61:    */     {
/*  62:101 */       this.inboundHandler.handlerAdded(ctx);
/*  63:    */     }
/*  64:    */     finally
/*  65:    */     {
/*  66:103 */       this.outboundHandler.handlerAdded(ctx);
/*  67:    */     }
/*  68:    */   }
/*  69:    */   
/*  70:    */   public void handlerRemoved(ChannelHandlerContext ctx)
/*  71:    */     throws Exception
/*  72:    */   {
/*  73:    */     try
/*  74:    */     {
/*  75:110 */       this.inboundHandler.handlerRemoved(ctx);
/*  76:    */     }
/*  77:    */     finally
/*  78:    */     {
/*  79:112 */       this.outboundHandler.handlerRemoved(ctx);
/*  80:    */     }
/*  81:    */   }
/*  82:    */   
/*  83:    */   public void channelRegistered(ChannelHandlerContext ctx)
/*  84:    */     throws Exception
/*  85:    */   {
/*  86:118 */     this.inboundHandler.channelRegistered(ctx);
/*  87:    */   }
/*  88:    */   
/*  89:    */   public void channelUnregistered(ChannelHandlerContext ctx)
/*  90:    */     throws Exception
/*  91:    */   {
/*  92:123 */     this.inboundHandler.channelUnregistered(ctx);
/*  93:    */   }
/*  94:    */   
/*  95:    */   public void channelActive(ChannelHandlerContext ctx)
/*  96:    */     throws Exception
/*  97:    */   {
/*  98:128 */     this.inboundHandler.channelActive(ctx);
/*  99:    */   }
/* 100:    */   
/* 101:    */   public void channelInactive(ChannelHandlerContext ctx)
/* 102:    */     throws Exception
/* 103:    */   {
/* 104:133 */     this.inboundHandler.channelInactive(ctx);
/* 105:    */   }
/* 106:    */   
/* 107:    */   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
/* 108:    */     throws Exception
/* 109:    */   {
/* 110:138 */     this.inboundHandler.exceptionCaught(ctx, cause);
/* 111:    */   }
/* 112:    */   
/* 113:    */   public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
/* 114:    */     throws Exception
/* 115:    */   {
/* 116:143 */     this.inboundHandler.userEventTriggered(ctx, evt);
/* 117:    */   }
/* 118:    */   
/* 119:    */   public void channelRead(ChannelHandlerContext ctx, Object msg)
/* 120:    */     throws Exception
/* 121:    */   {
/* 122:148 */     this.inboundHandler.channelRead(ctx, msg);
/* 123:    */   }
/* 124:    */   
/* 125:    */   public void channelReadComplete(ChannelHandlerContext ctx)
/* 126:    */     throws Exception
/* 127:    */   {
/* 128:153 */     this.inboundHandler.channelReadComplete(ctx);
/* 129:    */   }
/* 130:    */   
/* 131:    */   public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise)
/* 132:    */     throws Exception
/* 133:    */   {
/* 134:160 */     this.outboundHandler.bind(ctx, localAddress, promise);
/* 135:    */   }
/* 136:    */   
/* 137:    */   public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
/* 138:    */     throws Exception
/* 139:    */   {
/* 140:168 */     this.outboundHandler.connect(ctx, remoteAddress, localAddress, promise);
/* 141:    */   }
/* 142:    */   
/* 143:    */   public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise)
/* 144:    */     throws Exception
/* 145:    */   {
/* 146:173 */     this.outboundHandler.disconnect(ctx, promise);
/* 147:    */   }
/* 148:    */   
/* 149:    */   public void close(ChannelHandlerContext ctx, ChannelPromise promise)
/* 150:    */     throws Exception
/* 151:    */   {
/* 152:178 */     this.outboundHandler.close(ctx, promise);
/* 153:    */   }
/* 154:    */   
/* 155:    */   public void deregister(ChannelHandlerContext ctx, ChannelPromise promise)
/* 156:    */     throws Exception
/* 157:    */   {
/* 158:183 */     this.outboundHandler.deregister(ctx, promise);
/* 159:    */   }
/* 160:    */   
/* 161:    */   public void read(ChannelHandlerContext ctx)
/* 162:    */     throws Exception
/* 163:    */   {
/* 164:188 */     this.outboundHandler.read(ctx);
/* 165:    */   }
/* 166:    */   
/* 167:    */   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
/* 168:    */     throws Exception
/* 169:    */   {
/* 170:193 */     this.outboundHandler.write(ctx, msg, promise);
/* 171:    */   }
/* 172:    */   
/* 173:    */   public void flush(ChannelHandlerContext ctx)
/* 174:    */     throws Exception
/* 175:    */   {
/* 176:198 */     this.outboundHandler.flush(ctx);
/* 177:    */   }
/* 178:    */   
/* 179:    */   public void channelWritabilityChanged(ChannelHandlerContext ctx)
/* 180:    */     throws Exception
/* 181:    */   {
/* 182:203 */     this.inboundHandler.channelWritabilityChanged(ctx);
/* 183:    */   }
/* 184:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.CombinedChannelDuplexHandler
 * JD-Core Version:    0.7.0.1
 */