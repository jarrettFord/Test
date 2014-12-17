/*   1:    */ package io.netty.handler.codec.spdy;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.ChannelHandlerContext;
/*   5:    */ import io.netty.channel.ChannelInboundHandler;
/*   6:    */ import io.netty.channel.ChannelPipeline;
/*   7:    */ import io.netty.handler.codec.ByteToMessageDecoder;
/*   8:    */ import io.netty.handler.codec.http.HttpObjectAggregator;
/*   9:    */ import io.netty.handler.codec.http.HttpRequestDecoder;
/*  10:    */ import io.netty.handler.codec.http.HttpResponseEncoder;
/*  11:    */ import io.netty.handler.ssl.SslHandler;
/*  12:    */ import java.util.List;
/*  13:    */ import javax.net.ssl.SSLEngine;
/*  14:    */ 
/*  15:    */ public abstract class SpdyOrHttpChooser
/*  16:    */   extends ByteToMessageDecoder
/*  17:    */ {
/*  18:    */   private final int maxSpdyContentLength;
/*  19:    */   private final int maxHttpContentLength;
/*  20:    */   
/*  21:    */   public static enum SelectedProtocol
/*  22:    */   {
/*  23: 42 */     SPDY_3_1("spdy/3.1"),  HTTP_1_1("http/1.1"),  HTTP_1_0("http/1.0"),  UNKNOWN("Unknown");
/*  24:    */     
/*  25:    */     private final String name;
/*  26:    */     
/*  27:    */     private SelectedProtocol(String defaultName)
/*  28:    */     {
/*  29: 50 */       this.name = defaultName;
/*  30:    */     }
/*  31:    */     
/*  32:    */     public String protocolName()
/*  33:    */     {
/*  34: 54 */       return this.name;
/*  35:    */     }
/*  36:    */     
/*  37:    */     public static SelectedProtocol protocol(String name)
/*  38:    */     {
/*  39: 65 */       for (SelectedProtocol protocol : ) {
/*  40: 66 */         if (protocol.protocolName().equals(name)) {
/*  41: 67 */           return protocol;
/*  42:    */         }
/*  43:    */       }
/*  44: 70 */       return UNKNOWN;
/*  45:    */     }
/*  46:    */   }
/*  47:    */   
/*  48:    */   protected SpdyOrHttpChooser(int maxSpdyContentLength, int maxHttpContentLength)
/*  49:    */   {
/*  50: 78 */     this.maxSpdyContentLength = maxSpdyContentLength;
/*  51: 79 */     this.maxHttpContentLength = maxHttpContentLength;
/*  52:    */   }
/*  53:    */   
/*  54:    */   protected abstract SelectedProtocol getProtocol(SSLEngine paramSSLEngine);
/*  55:    */   
/*  56:    */   protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/*  57:    */     throws Exception
/*  58:    */   {
/*  59: 91 */     if (initPipeline(ctx)) {
/*  60: 96 */       ctx.pipeline().remove(this);
/*  61:    */     }
/*  62:    */   }
/*  63:    */   
/*  64:    */   private boolean initPipeline(ChannelHandlerContext ctx)
/*  65:    */   {
/*  66:103 */     SslHandler handler = (SslHandler)ctx.pipeline().get(SslHandler.class);
/*  67:104 */     if (handler == null) {
/*  68:106 */       throw new IllegalStateException("SslHandler is needed for SPDY");
/*  69:    */     }
/*  70:109 */     SelectedProtocol protocol = getProtocol(handler.engine());
/*  71:110 */     switch (1.$SwitchMap$io$netty$handler$codec$spdy$SpdyOrHttpChooser$SelectedProtocol[protocol.ordinal()])
/*  72:    */     {
/*  73:    */     case 1: 
/*  74:113 */       return false;
/*  75:    */     case 2: 
/*  76:115 */       addSpdyHandlers(ctx, SpdyVersion.SPDY_3_1);
/*  77:116 */       break;
/*  78:    */     case 3: 
/*  79:    */     case 4: 
/*  80:119 */       addHttpHandlers(ctx);
/*  81:120 */       break;
/*  82:    */     default: 
/*  83:122 */       throw new IllegalStateException("Unknown SelectedProtocol");
/*  84:    */     }
/*  85:124 */     return true;
/*  86:    */   }
/*  87:    */   
/*  88:    */   protected void addSpdyHandlers(ChannelHandlerContext ctx, SpdyVersion version)
/*  89:    */   {
/*  90:131 */     ChannelPipeline pipeline = ctx.pipeline();
/*  91:132 */     pipeline.addLast("spdyFrameCodec", new SpdyFrameCodec(version));
/*  92:133 */     pipeline.addLast("spdySessionHandler", new SpdySessionHandler(version, true));
/*  93:134 */     pipeline.addLast("spdyHttpEncoder", new SpdyHttpEncoder(version));
/*  94:135 */     pipeline.addLast("spdyHttpDecoder", new SpdyHttpDecoder(version, this.maxSpdyContentLength));
/*  95:136 */     pipeline.addLast("spdyStreamIdHandler", new SpdyHttpResponseStreamIdHandler());
/*  96:137 */     pipeline.addLast("httpRequestHandler", createHttpRequestHandlerForSpdy());
/*  97:    */   }
/*  98:    */   
/*  99:    */   protected void addHttpHandlers(ChannelHandlerContext ctx)
/* 100:    */   {
/* 101:144 */     ChannelPipeline pipeline = ctx.pipeline();
/* 102:145 */     pipeline.addLast("httpRequestDecoder", new HttpRequestDecoder());
/* 103:146 */     pipeline.addLast("httpResponseEncoder", new HttpResponseEncoder());
/* 104:147 */     pipeline.addLast("httpChunkAggregator", new HttpObjectAggregator(this.maxHttpContentLength));
/* 105:148 */     pipeline.addLast("httpRequestHandler", createHttpRequestHandlerForHttp());
/* 106:    */   }
/* 107:    */   
/* 108:    */   protected abstract ChannelInboundHandler createHttpRequestHandlerForHttp();
/* 109:    */   
/* 110:    */   protected ChannelInboundHandler createHttpRequestHandlerForSpdy()
/* 111:    */   {
/* 112:166 */     return createHttpRequestHandlerForHttp();
/* 113:    */   }
/* 114:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.SpdyOrHttpChooser
 * JD-Core Version:    0.7.0.1
 */