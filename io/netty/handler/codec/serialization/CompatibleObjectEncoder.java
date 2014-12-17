/*   1:    */ package io.netty.handler.codec.serialization;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufOutputStream;
/*   5:    */ import io.netty.channel.ChannelHandlerContext;
/*   6:    */ import io.netty.handler.codec.MessageToByteEncoder;
/*   7:    */ import io.netty.util.Attribute;
/*   8:    */ import io.netty.util.AttributeKey;
/*   9:    */ import java.io.ObjectOutputStream;
/*  10:    */ import java.io.OutputStream;
/*  11:    */ import java.io.Serializable;
/*  12:    */ 
/*  13:    */ public class CompatibleObjectEncoder
/*  14:    */   extends MessageToByteEncoder<Serializable>
/*  15:    */ {
/*  16: 39 */   private static final AttributeKey<ObjectOutputStream> OOS = AttributeKey.valueOf(CompatibleObjectEncoder.class.getName() + ".OOS");
/*  17:    */   private final int resetInterval;
/*  18:    */   private int writtenObjects;
/*  19:    */   
/*  20:    */   public CompatibleObjectEncoder()
/*  21:    */   {
/*  22: 49 */     this(16);
/*  23:    */   }
/*  24:    */   
/*  25:    */   public CompatibleObjectEncoder(int resetInterval)
/*  26:    */   {
/*  27: 62 */     if (resetInterval < 0) {
/*  28: 63 */       throw new IllegalArgumentException("resetInterval: " + resetInterval);
/*  29:    */     }
/*  30: 66 */     this.resetInterval = resetInterval;
/*  31:    */   }
/*  32:    */   
/*  33:    */   protected ObjectOutputStream newObjectOutputStream(OutputStream out)
/*  34:    */     throws Exception
/*  35:    */   {
/*  36: 75 */     return new ObjectOutputStream(out);
/*  37:    */   }
/*  38:    */   
/*  39:    */   protected void encode(ChannelHandlerContext ctx, Serializable msg, ByteBuf out)
/*  40:    */     throws Exception
/*  41:    */   {
/*  42: 80 */     Attribute<ObjectOutputStream> oosAttr = ctx.attr(OOS);
/*  43: 81 */     ObjectOutputStream oos = (ObjectOutputStream)oosAttr.get();
/*  44: 82 */     if (oos == null)
/*  45:    */     {
/*  46: 83 */       oos = newObjectOutputStream(new ByteBufOutputStream(out));
/*  47: 84 */       ObjectOutputStream newOos = (ObjectOutputStream)oosAttr.setIfAbsent(oos);
/*  48: 85 */       if (newOos != null) {
/*  49: 86 */         oos = newOos;
/*  50:    */       }
/*  51:    */     }
/*  52: 90 */     synchronized (oos)
/*  53:    */     {
/*  54: 91 */       if (this.resetInterval != 0)
/*  55:    */       {
/*  56: 93 */         this.writtenObjects += 1;
/*  57: 94 */         if (this.writtenObjects % this.resetInterval == 0) {
/*  58: 95 */           oos.reset();
/*  59:    */         }
/*  60:    */       }
/*  61: 99 */       oos.writeObject(msg);
/*  62:100 */       oos.flush();
/*  63:    */     }
/*  64:    */   }
/*  65:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.serialization.CompatibleObjectEncoder
 * JD-Core Version:    0.7.0.1
 */