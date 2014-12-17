/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import io.netty.util.concurrent.AbstractFuture;
/*   4:    */ import io.netty.util.concurrent.Future;
/*   5:    */ import io.netty.util.concurrent.GenericFutureListener;
/*   6:    */ import java.util.concurrent.TimeUnit;
/*   7:    */ 
/*   8:    */ final class VoidChannelPromise
/*   9:    */   extends AbstractFuture<Void>
/*  10:    */   implements ChannelPromise
/*  11:    */ {
/*  12:    */   private final Channel channel;
/*  13:    */   private final boolean fireException;
/*  14:    */   
/*  15:    */   public VoidChannelPromise(Channel channel, boolean fireException)
/*  16:    */   {
/*  17: 35 */     if (channel == null) {
/*  18: 36 */       throw new NullPointerException("channel");
/*  19:    */     }
/*  20: 38 */     this.channel = channel;
/*  21: 39 */     this.fireException = fireException;
/*  22:    */   }
/*  23:    */   
/*  24:    */   public VoidChannelPromise addListener(GenericFutureListener<? extends Future<? super Void>> listener)
/*  25:    */   {
/*  26: 44 */     fail();
/*  27: 45 */     return this;
/*  28:    */   }
/*  29:    */   
/*  30:    */   public VoidChannelPromise addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners)
/*  31:    */   {
/*  32: 50 */     fail();
/*  33: 51 */     return this;
/*  34:    */   }
/*  35:    */   
/*  36:    */   public VoidChannelPromise removeListener(GenericFutureListener<? extends Future<? super Void>> listener)
/*  37:    */   {
/*  38: 57 */     return this;
/*  39:    */   }
/*  40:    */   
/*  41:    */   public VoidChannelPromise removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners)
/*  42:    */   {
/*  43: 63 */     return this;
/*  44:    */   }
/*  45:    */   
/*  46:    */   public VoidChannelPromise await()
/*  47:    */     throws InterruptedException
/*  48:    */   {
/*  49: 68 */     if (Thread.interrupted()) {
/*  50: 69 */       throw new InterruptedException();
/*  51:    */     }
/*  52: 71 */     return this;
/*  53:    */   }
/*  54:    */   
/*  55:    */   public boolean await(long timeout, TimeUnit unit)
/*  56:    */   {
/*  57: 76 */     fail();
/*  58: 77 */     return false;
/*  59:    */   }
/*  60:    */   
/*  61:    */   public boolean await(long timeoutMillis)
/*  62:    */   {
/*  63: 82 */     fail();
/*  64: 83 */     return false;
/*  65:    */   }
/*  66:    */   
/*  67:    */   public VoidChannelPromise awaitUninterruptibly()
/*  68:    */   {
/*  69: 88 */     fail();
/*  70: 89 */     return this;
/*  71:    */   }
/*  72:    */   
/*  73:    */   public boolean awaitUninterruptibly(long timeout, TimeUnit unit)
/*  74:    */   {
/*  75: 94 */     fail();
/*  76: 95 */     return false;
/*  77:    */   }
/*  78:    */   
/*  79:    */   public boolean awaitUninterruptibly(long timeoutMillis)
/*  80:    */   {
/*  81:100 */     fail();
/*  82:101 */     return false;
/*  83:    */   }
/*  84:    */   
/*  85:    */   public Channel channel()
/*  86:    */   {
/*  87:106 */     return this.channel;
/*  88:    */   }
/*  89:    */   
/*  90:    */   public boolean isDone()
/*  91:    */   {
/*  92:111 */     return false;
/*  93:    */   }
/*  94:    */   
/*  95:    */   public boolean isSuccess()
/*  96:    */   {
/*  97:116 */     return false;
/*  98:    */   }
/*  99:    */   
/* 100:    */   public boolean setUncancellable()
/* 101:    */   {
/* 102:121 */     return true;
/* 103:    */   }
/* 104:    */   
/* 105:    */   public boolean isCancellable()
/* 106:    */   {
/* 107:126 */     return false;
/* 108:    */   }
/* 109:    */   
/* 110:    */   public boolean isCancelled()
/* 111:    */   {
/* 112:131 */     return false;
/* 113:    */   }
/* 114:    */   
/* 115:    */   public Throwable cause()
/* 116:    */   {
/* 117:136 */     return null;
/* 118:    */   }
/* 119:    */   
/* 120:    */   public VoidChannelPromise sync()
/* 121:    */   {
/* 122:141 */     fail();
/* 123:142 */     return this;
/* 124:    */   }
/* 125:    */   
/* 126:    */   public VoidChannelPromise syncUninterruptibly()
/* 127:    */   {
/* 128:147 */     fail();
/* 129:148 */     return this;
/* 130:    */   }
/* 131:    */   
/* 132:    */   public VoidChannelPromise setFailure(Throwable cause)
/* 133:    */   {
/* 134:152 */     fireException(cause);
/* 135:153 */     return this;
/* 136:    */   }
/* 137:    */   
/* 138:    */   public VoidChannelPromise setSuccess()
/* 139:    */   {
/* 140:158 */     return this;
/* 141:    */   }
/* 142:    */   
/* 143:    */   public boolean tryFailure(Throwable cause)
/* 144:    */   {
/* 145:163 */     fireException(cause);
/* 146:164 */     return false;
/* 147:    */   }
/* 148:    */   
/* 149:    */   public boolean cancel(boolean mayInterruptIfRunning)
/* 150:    */   {
/* 151:169 */     return false;
/* 152:    */   }
/* 153:    */   
/* 154:    */   public boolean trySuccess()
/* 155:    */   {
/* 156:174 */     return false;
/* 157:    */   }
/* 158:    */   
/* 159:    */   private static void fail()
/* 160:    */   {
/* 161:178 */     throw new IllegalStateException("void future");
/* 162:    */   }
/* 163:    */   
/* 164:    */   public VoidChannelPromise setSuccess(Void result)
/* 165:    */   {
/* 166:183 */     return this;
/* 167:    */   }
/* 168:    */   
/* 169:    */   public boolean trySuccess(Void result)
/* 170:    */   {
/* 171:188 */     return false;
/* 172:    */   }
/* 173:    */   
/* 174:    */   public Void getNow()
/* 175:    */   {
/* 176:193 */     return null;
/* 177:    */   }
/* 178:    */   
/* 179:    */   private void fireException(Throwable cause)
/* 180:    */   {
/* 181:201 */     if ((this.fireException) && (this.channel.isRegistered())) {
/* 182:202 */       this.channel.pipeline().fireExceptionCaught(cause);
/* 183:    */     }
/* 184:    */   }
/* 185:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.VoidChannelPromise
 * JD-Core Version:    0.7.0.1
 */