/*   1:    */ package io.netty.handler.codec.marshalling;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.ChannelHandlerContext;
/*   5:    */ import io.netty.handler.codec.ReplayingDecoder;
/*   6:    */ import io.netty.handler.codec.TooLongFrameException;
/*   7:    */ import java.util.List;
/*   8:    */ import org.jboss.marshalling.ByteInput;
/*   9:    */ import org.jboss.marshalling.Unmarshaller;
/*  10:    */ 
/*  11:    */ public class CompatibleMarshallingDecoder
/*  12:    */   extends ReplayingDecoder<Void>
/*  13:    */ {
/*  14:    */   protected final UnmarshallerProvider provider;
/*  15:    */   protected final int maxObjectSize;
/*  16:    */   private boolean discardingTooLongFrame;
/*  17:    */   
/*  18:    */   public CompatibleMarshallingDecoder(UnmarshallerProvider provider, int maxObjectSize)
/*  19:    */   {
/*  20: 54 */     this.provider = provider;
/*  21: 55 */     this.maxObjectSize = maxObjectSize;
/*  22:    */   }
/*  23:    */   
/*  24:    */   protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out)
/*  25:    */     throws Exception
/*  26:    */   {
/*  27: 60 */     if (this.discardingTooLongFrame)
/*  28:    */     {
/*  29: 61 */       buffer.skipBytes(actualReadableBytes());
/*  30: 62 */       checkpoint();
/*  31: 63 */       return;
/*  32:    */     }
/*  33: 66 */     Unmarshaller unmarshaller = this.provider.getUnmarshaller(ctx);
/*  34: 67 */     ByteInput input = new ChannelBufferByteInput(buffer);
/*  35: 68 */     if (this.maxObjectSize != 2147483647) {
/*  36: 69 */       input = new LimitingByteInput(input, this.maxObjectSize);
/*  37:    */     }
/*  38:    */     try
/*  39:    */     {
/*  40: 72 */       unmarshaller.start(input);
/*  41: 73 */       Object obj = unmarshaller.readObject();
/*  42: 74 */       unmarshaller.finish();
/*  43: 75 */       out.add(obj);
/*  44:    */     }
/*  45:    */     catch (LimitingByteInput.TooBigObjectException e)
/*  46:    */     {
/*  47: 77 */       this.discardingTooLongFrame = true;
/*  48: 78 */       throw new TooLongFrameException();
/*  49:    */     }
/*  50:    */     finally
/*  51:    */     {
/*  52: 82 */       unmarshaller.close();
/*  53:    */     }
/*  54:    */   }
/*  55:    */   
/*  56:    */   protected void decodeLast(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out)
/*  57:    */     throws Exception
/*  58:    */   {
/*  59: 88 */     switch (buffer.readableBytes())
/*  60:    */     {
/*  61:    */     case 0: 
/*  62: 90 */       return;
/*  63:    */     case 1: 
/*  64: 93 */       if (buffer.getByte(buffer.readerIndex()) == 121)
/*  65:    */       {
/*  66: 94 */         buffer.skipBytes(1); return;
/*  67:    */       }
/*  68:    */       break;
/*  69:    */     }
/*  70: 99 */     decode(ctx, buffer, out);
/*  71:    */   }
/*  72:    */   
/*  73:    */   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
/*  74:    */     throws Exception
/*  75:    */   {
/*  76:104 */     if ((cause instanceof TooLongFrameException)) {
/*  77:105 */       ctx.close();
/*  78:    */     } else {
/*  79:107 */       super.exceptionCaught(ctx, cause);
/*  80:    */     }
/*  81:    */   }
/*  82:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.marshalling.CompatibleMarshallingDecoder
 * JD-Core Version:    0.7.0.1
 */