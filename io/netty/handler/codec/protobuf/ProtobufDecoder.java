/*   1:    */ package io.netty.handler.codec.protobuf;
/*   2:    */ 
/*   3:    */ import com.google.protobuf.ExtensionRegistry;
/*   4:    */ import com.google.protobuf.MessageLite;
/*   5:    */ import com.google.protobuf.MessageLite.Builder;
/*   6:    */ import com.google.protobuf.Parser;
/*   7:    */ import io.netty.buffer.ByteBuf;
/*   8:    */ import io.netty.channel.ChannelHandler.Sharable;
/*   9:    */ import io.netty.channel.ChannelHandlerContext;
/*  10:    */ import io.netty.handler.codec.MessageToMessageDecoder;
/*  11:    */ import java.util.List;
/*  12:    */ 
/*  13:    */ @ChannelHandler.Sharable
/*  14:    */ public class ProtobufDecoder
/*  15:    */   extends MessageToMessageDecoder<ByteBuf>
/*  16:    */ {
/*  17:    */   private static final boolean HAS_PARSER;
/*  18:    */   private final MessageLite prototype;
/*  19:    */   private final ExtensionRegistry extensionRegistry;
/*  20:    */   
/*  21:    */   static
/*  22:    */   {
/*  23: 68 */     boolean hasParser = false;
/*  24:    */     try
/*  25:    */     {
/*  26: 71 */       MessageLite.class.getDeclaredMethod("getParserForType", new Class[0]);
/*  27: 72 */       hasParser = true;
/*  28:    */     }
/*  29:    */     catch (Throwable t) {}
/*  30: 77 */     HAS_PARSER = hasParser;
/*  31:    */   }
/*  32:    */   
/*  33:    */   public ProtobufDecoder(MessageLite prototype)
/*  34:    */   {
/*  35: 87 */     this(prototype, null);
/*  36:    */   }
/*  37:    */   
/*  38:    */   public ProtobufDecoder(MessageLite prototype, ExtensionRegistry extensionRegistry)
/*  39:    */   {
/*  40: 91 */     if (prototype == null) {
/*  41: 92 */       throw new NullPointerException("prototype");
/*  42:    */     }
/*  43: 94 */     this.prototype = prototype.getDefaultInstanceForType();
/*  44: 95 */     this.extensionRegistry = extensionRegistry;
/*  45:    */   }
/*  46:    */   
/*  47:    */   protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out)
/*  48:    */     throws Exception
/*  49:    */   {
/*  50:102 */     int length = msg.readableBytes();
/*  51:    */     int offset;
/*  52:    */     byte[] array;
/*  53:    */     int offset;
/*  54:103 */     if (msg.hasArray())
/*  55:    */     {
/*  56:104 */       byte[] array = msg.array();
/*  57:105 */       offset = msg.arrayOffset() + msg.readerIndex();
/*  58:    */     }
/*  59:    */     else
/*  60:    */     {
/*  61:107 */       array = new byte[length];
/*  62:108 */       msg.getBytes(msg.readerIndex(), array, 0, length);
/*  63:109 */       offset = 0;
/*  64:    */     }
/*  65:112 */     if (this.extensionRegistry == null)
/*  66:    */     {
/*  67:113 */       if (HAS_PARSER) {
/*  68:114 */         out.add(this.prototype.getParserForType().parseFrom(array, offset, length));
/*  69:    */       } else {
/*  70:116 */         out.add(this.prototype.newBuilderForType().mergeFrom(array, offset, length).build());
/*  71:    */       }
/*  72:    */     }
/*  73:119 */     else if (HAS_PARSER) {
/*  74:120 */       out.add(this.prototype.getParserForType().parseFrom(array, offset, length, this.extensionRegistry));
/*  75:    */     } else {
/*  76:122 */       out.add(this.prototype.newBuilderForType().mergeFrom(array, offset, length, this.extensionRegistry).build());
/*  77:    */     }
/*  78:    */   }
/*  79:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.protobuf.ProtobufDecoder
 * JD-Core Version:    0.7.0.1
 */