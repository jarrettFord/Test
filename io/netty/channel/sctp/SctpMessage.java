/*   1:    */ package io.netty.channel.sctp;
/*   2:    */ 
/*   3:    */ import com.sun.nio.sctp.MessageInfo;
/*   4:    */ import io.netty.buffer.ByteBuf;
/*   5:    */ import io.netty.buffer.ByteBufUtil;
/*   6:    */ import io.netty.buffer.DefaultByteBufHolder;
/*   7:    */ 
/*   8:    */ public final class SctpMessage
/*   9:    */   extends DefaultByteBufHolder
/*  10:    */ {
/*  11:    */   private final int streamIdentifier;
/*  12:    */   private final int protocolIdentifier;
/*  13:    */   private final MessageInfo msgInfo;
/*  14:    */   
/*  15:    */   public SctpMessage(int protocolIdentifier, int streamIdentifier, ByteBuf payloadBuffer)
/*  16:    */   {
/*  17: 39 */     super(payloadBuffer);
/*  18: 40 */     this.protocolIdentifier = protocolIdentifier;
/*  19: 41 */     this.streamIdentifier = streamIdentifier;
/*  20: 42 */     this.msgInfo = null;
/*  21:    */   }
/*  22:    */   
/*  23:    */   public SctpMessage(MessageInfo msgInfo, ByteBuf payloadBuffer)
/*  24:    */   {
/*  25: 51 */     super(payloadBuffer);
/*  26: 52 */     if (msgInfo == null) {
/*  27: 53 */       throw new NullPointerException("msgInfo");
/*  28:    */     }
/*  29: 55 */     this.msgInfo = msgInfo;
/*  30: 56 */     this.streamIdentifier = msgInfo.streamNumber();
/*  31: 57 */     this.protocolIdentifier = msgInfo.payloadProtocolID();
/*  32:    */   }
/*  33:    */   
/*  34:    */   public int streamIdentifier()
/*  35:    */   {
/*  36: 64 */     return this.streamIdentifier;
/*  37:    */   }
/*  38:    */   
/*  39:    */   public int protocolIdentifier()
/*  40:    */   {
/*  41: 71 */     return this.protocolIdentifier;
/*  42:    */   }
/*  43:    */   
/*  44:    */   public MessageInfo messageInfo()
/*  45:    */   {
/*  46: 79 */     return this.msgInfo;
/*  47:    */   }
/*  48:    */   
/*  49:    */   public boolean isComplete()
/*  50:    */   {
/*  51: 86 */     if (this.msgInfo != null) {
/*  52: 87 */       return this.msgInfo.isComplete();
/*  53:    */     }
/*  54: 90 */     return true;
/*  55:    */   }
/*  56:    */   
/*  57:    */   public boolean equals(Object o)
/*  58:    */   {
/*  59: 96 */     if (this == o) {
/*  60: 97 */       return true;
/*  61:    */     }
/*  62:100 */     if ((o == null) || (getClass() != o.getClass())) {
/*  63:101 */       return false;
/*  64:    */     }
/*  65:104 */     SctpMessage sctpFrame = (SctpMessage)o;
/*  66:106 */     if (this.protocolIdentifier != sctpFrame.protocolIdentifier) {
/*  67:107 */       return false;
/*  68:    */     }
/*  69:110 */     if (this.streamIdentifier != sctpFrame.streamIdentifier) {
/*  70:111 */       return false;
/*  71:    */     }
/*  72:114 */     if (!content().equals(sctpFrame.content())) {
/*  73:115 */       return false;
/*  74:    */     }
/*  75:118 */     return true;
/*  76:    */   }
/*  77:    */   
/*  78:    */   public int hashCode()
/*  79:    */   {
/*  80:123 */     int result = this.streamIdentifier;
/*  81:124 */     result = 31 * result + this.protocolIdentifier;
/*  82:125 */     result = 31 * result + content().hashCode();
/*  83:126 */     return result;
/*  84:    */   }
/*  85:    */   
/*  86:    */   public SctpMessage copy()
/*  87:    */   {
/*  88:131 */     if (this.msgInfo == null) {
/*  89:132 */       return new SctpMessage(this.protocolIdentifier, this.streamIdentifier, content().copy());
/*  90:    */     }
/*  91:134 */     return new SctpMessage(this.msgInfo, content().copy());
/*  92:    */   }
/*  93:    */   
/*  94:    */   public SctpMessage duplicate()
/*  95:    */   {
/*  96:140 */     if (this.msgInfo == null) {
/*  97:141 */       return new SctpMessage(this.protocolIdentifier, this.streamIdentifier, content().duplicate());
/*  98:    */     }
/*  99:143 */     return new SctpMessage(this.msgInfo, content().copy());
/* 100:    */   }
/* 101:    */   
/* 102:    */   public SctpMessage retain()
/* 103:    */   {
/* 104:149 */     super.retain();
/* 105:150 */     return this;
/* 106:    */   }
/* 107:    */   
/* 108:    */   public SctpMessage retain(int increment)
/* 109:    */   {
/* 110:155 */     super.retain(increment);
/* 111:156 */     return this;
/* 112:    */   }
/* 113:    */   
/* 114:    */   public String toString()
/* 115:    */   {
/* 116:161 */     if (refCnt() == 0) {
/* 117:162 */       return "SctpFrame{streamIdentifier=" + this.streamIdentifier + ", protocolIdentifier=" + this.protocolIdentifier + ", data=(FREED)}";
/* 118:    */     }
/* 119:166 */     return "SctpFrame{streamIdentifier=" + this.streamIdentifier + ", protocolIdentifier=" + this.protocolIdentifier + ", data=" + ByteBufUtil.hexDump(content()) + '}';
/* 120:    */   }
/* 121:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.sctp.SctpMessage
 * JD-Core Version:    0.7.0.1
 */