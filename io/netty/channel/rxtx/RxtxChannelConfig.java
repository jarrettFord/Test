/*   1:    */ package io.netty.channel.rxtx;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.channel.ChannelConfig;
/*   5:    */ import io.netty.channel.MessageSizeEstimator;
/*   6:    */ import io.netty.channel.RecvByteBufAllocator;
/*   7:    */ 
/*   8:    */ public abstract interface RxtxChannelConfig
/*   9:    */   extends ChannelConfig
/*  10:    */ {
/*  11:    */   public abstract RxtxChannelConfig setBaudrate(int paramInt);
/*  12:    */   
/*  13:    */   public abstract RxtxChannelConfig setStopbits(Stopbits paramStopbits);
/*  14:    */   
/*  15:    */   public abstract RxtxChannelConfig setDatabits(Databits paramDatabits);
/*  16:    */   
/*  17:    */   public abstract RxtxChannelConfig setParitybit(Paritybit paramParitybit);
/*  18:    */   
/*  19:    */   public abstract int getBaudrate();
/*  20:    */   
/*  21:    */   public abstract Stopbits getStopbits();
/*  22:    */   
/*  23:    */   public abstract Databits getDatabits();
/*  24:    */   
/*  25:    */   public abstract Paritybit getParitybit();
/*  26:    */   
/*  27:    */   public abstract boolean isDtr();
/*  28:    */   
/*  29:    */   public abstract RxtxChannelConfig setDtr(boolean paramBoolean);
/*  30:    */   
/*  31:    */   public abstract boolean isRts();
/*  32:    */   
/*  33:    */   public abstract RxtxChannelConfig setRts(boolean paramBoolean);
/*  34:    */   
/*  35:    */   public abstract int getWaitTimeMillis();
/*  36:    */   
/*  37:    */   public abstract RxtxChannelConfig setWaitTimeMillis(int paramInt);
/*  38:    */   
/*  39:    */   public abstract RxtxChannelConfig setReadTimeout(int paramInt);
/*  40:    */   
/*  41:    */   public abstract int getReadTimeout();
/*  42:    */   
/*  43:    */   public abstract RxtxChannelConfig setConnectTimeoutMillis(int paramInt);
/*  44:    */   
/*  45:    */   public abstract RxtxChannelConfig setMaxMessagesPerRead(int paramInt);
/*  46:    */   
/*  47:    */   public abstract RxtxChannelConfig setWriteSpinCount(int paramInt);
/*  48:    */   
/*  49:    */   public abstract RxtxChannelConfig setAllocator(ByteBufAllocator paramByteBufAllocator);
/*  50:    */   
/*  51:    */   public abstract RxtxChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator paramRecvByteBufAllocator);
/*  52:    */   
/*  53:    */   public abstract RxtxChannelConfig setAutoRead(boolean paramBoolean);
/*  54:    */   
/*  55:    */   public abstract RxtxChannelConfig setAutoClose(boolean paramBoolean);
/*  56:    */   
/*  57:    */   public abstract RxtxChannelConfig setWriteBufferHighWaterMark(int paramInt);
/*  58:    */   
/*  59:    */   public abstract RxtxChannelConfig setWriteBufferLowWaterMark(int paramInt);
/*  60:    */   
/*  61:    */   public abstract RxtxChannelConfig setMessageSizeEstimator(MessageSizeEstimator paramMessageSizeEstimator);
/*  62:    */   
/*  63:    */   public static enum Stopbits
/*  64:    */   {
/*  65: 57 */     STOPBITS_1(1),  STOPBITS_2(2),  STOPBITS_1_5(3);
/*  66:    */     
/*  67:    */     private final int value;
/*  68:    */     
/*  69:    */     private Stopbits(int value)
/*  70:    */     {
/*  71: 70 */       this.value = value;
/*  72:    */     }
/*  73:    */     
/*  74:    */     public int value()
/*  75:    */     {
/*  76: 74 */       return this.value;
/*  77:    */     }
/*  78:    */     
/*  79:    */     public static Stopbits valueOf(int value)
/*  80:    */     {
/*  81: 78 */       for (Stopbits stopbit : ) {
/*  82: 79 */         if (stopbit.value == value) {
/*  83: 80 */           return stopbit;
/*  84:    */         }
/*  85:    */       }
/*  86: 83 */       throw new IllegalArgumentException("unknown " + Stopbits.class.getSimpleName() + " value: " + value);
/*  87:    */     }
/*  88:    */   }
/*  89:    */   
/*  90:    */   public static enum Databits
/*  91:    */   {
/*  92: 91 */     DATABITS_5(5),  DATABITS_6(6),  DATABITS_7(7),  DATABITS_8(8);
/*  93:    */     
/*  94:    */     private final int value;
/*  95:    */     
/*  96:    */     private Databits(int value)
/*  97:    */     {
/*  98:108 */       this.value = value;
/*  99:    */     }
/* 100:    */     
/* 101:    */     public int value()
/* 102:    */     {
/* 103:112 */       return this.value;
/* 104:    */     }
/* 105:    */     
/* 106:    */     public static Databits valueOf(int value)
/* 107:    */     {
/* 108:116 */       for (Databits databit : ) {
/* 109:117 */         if (databit.value == value) {
/* 110:118 */           return databit;
/* 111:    */         }
/* 112:    */       }
/* 113:121 */       throw new IllegalArgumentException("unknown " + Databits.class.getSimpleName() + " value: " + value);
/* 114:    */     }
/* 115:    */   }
/* 116:    */   
/* 117:    */   public static enum Paritybit
/* 118:    */   {
/* 119:129 */     NONE(0),  ODD(1),  EVEN(2),  MARK(3),  SPACE(4);
/* 120:    */     
/* 121:    */     private final int value;
/* 122:    */     
/* 123:    */     private Paritybit(int value)
/* 124:    */     {
/* 125:152 */       this.value = value;
/* 126:    */     }
/* 127:    */     
/* 128:    */     public int value()
/* 129:    */     {
/* 130:156 */       return this.value;
/* 131:    */     }
/* 132:    */     
/* 133:    */     public static Paritybit valueOf(int value)
/* 134:    */     {
/* 135:160 */       for (Paritybit paritybit : ) {
/* 136:161 */         if (paritybit.value == value) {
/* 137:162 */           return paritybit;
/* 138:    */         }
/* 139:    */       }
/* 140:165 */       throw new IllegalArgumentException("unknown " + Paritybit.class.getSimpleName() + " value: " + value);
/* 141:    */     }
/* 142:    */   }
/* 143:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.rxtx.RxtxChannelConfig
 * JD-Core Version:    0.7.0.1
 */