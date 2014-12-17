/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import io.netty.util.ReferenceCountUtil;
/*   4:    */ import io.netty.util.ReferenceCounted;
/*   5:    */ import io.netty.util.internal.StringUtil;
/*   6:    */ import java.net.SocketAddress;
/*   7:    */ 
/*   8:    */ public class DefaultAddressedEnvelope<M, A extends SocketAddress>
/*   9:    */   implements AddressedEnvelope<M, A>
/*  10:    */ {
/*  11:    */   private final M message;
/*  12:    */   private final A sender;
/*  13:    */   private final A recipient;
/*  14:    */   
/*  15:    */   public DefaultAddressedEnvelope(M message, A recipient, A sender)
/*  16:    */   {
/*  17: 42 */     if (message == null) {
/*  18: 43 */       throw new NullPointerException("message");
/*  19:    */     }
/*  20: 46 */     this.message = message;
/*  21: 47 */     this.sender = sender;
/*  22: 48 */     this.recipient = recipient;
/*  23:    */   }
/*  24:    */   
/*  25:    */   public DefaultAddressedEnvelope(M message, A recipient)
/*  26:    */   {
/*  27: 56 */     this(message, recipient, null);
/*  28:    */   }
/*  29:    */   
/*  30:    */   public M content()
/*  31:    */   {
/*  32: 61 */     return this.message;
/*  33:    */   }
/*  34:    */   
/*  35:    */   public A sender()
/*  36:    */   {
/*  37: 66 */     return this.sender;
/*  38:    */   }
/*  39:    */   
/*  40:    */   public A recipient()
/*  41:    */   {
/*  42: 71 */     return this.recipient;
/*  43:    */   }
/*  44:    */   
/*  45:    */   public int refCnt()
/*  46:    */   {
/*  47: 76 */     if ((this.message instanceof ReferenceCounted)) {
/*  48: 77 */       return ((ReferenceCounted)this.message).refCnt();
/*  49:    */     }
/*  50: 79 */     return 1;
/*  51:    */   }
/*  52:    */   
/*  53:    */   public AddressedEnvelope<M, A> retain()
/*  54:    */   {
/*  55: 85 */     ReferenceCountUtil.retain(this.message);
/*  56: 86 */     return this;
/*  57:    */   }
/*  58:    */   
/*  59:    */   public AddressedEnvelope<M, A> retain(int increment)
/*  60:    */   {
/*  61: 91 */     ReferenceCountUtil.retain(this.message, increment);
/*  62: 92 */     return this;
/*  63:    */   }
/*  64:    */   
/*  65:    */   public boolean release()
/*  66:    */   {
/*  67: 97 */     return ReferenceCountUtil.release(this.message);
/*  68:    */   }
/*  69:    */   
/*  70:    */   public boolean release(int decrement)
/*  71:    */   {
/*  72:102 */     return ReferenceCountUtil.release(this.message, decrement);
/*  73:    */   }
/*  74:    */   
/*  75:    */   public String toString()
/*  76:    */   {
/*  77:107 */     if (this.sender != null) {
/*  78:108 */       return StringUtil.simpleClassName(this) + '(' + this.sender + " => " + this.recipient + ", " + this.message + ')';
/*  79:    */     }
/*  80:111 */     return StringUtil.simpleClassName(this) + "(=> " + this.recipient + ", " + this.message + ')';
/*  81:    */   }
/*  82:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.DefaultAddressedEnvelope
 * JD-Core Version:    0.7.0.1
 */