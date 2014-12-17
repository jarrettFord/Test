/*   1:    */ package io.netty.channel.oio;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.channel.Channel;
/*   6:    */ import io.netty.channel.Channel.Unsafe;
/*   7:    */ import io.netty.channel.ChannelConfig;
/*   8:    */ import io.netty.channel.ChannelMetadata;
/*   9:    */ import io.netty.channel.ChannelOption;
/*  10:    */ import io.netty.channel.ChannelOutboundBuffer;
/*  11:    */ import io.netty.channel.ChannelPipeline;
/*  12:    */ import io.netty.channel.FileRegion;
/*  13:    */ import io.netty.channel.RecvByteBufAllocator;
/*  14:    */ import io.netty.channel.RecvByteBufAllocator.Handle;
/*  15:    */ import io.netty.channel.socket.ChannelInputShutdownEvent;
/*  16:    */ import io.netty.util.internal.StringUtil;
/*  17:    */ import java.io.IOException;
/*  18:    */ 
/*  19:    */ public abstract class AbstractOioByteChannel
/*  20:    */   extends AbstractOioChannel
/*  21:    */ {
/*  22:    */   private RecvByteBufAllocator.Handle allocHandle;
/*  23:    */   private volatile boolean inputShutdown;
/*  24: 39 */   private static final ChannelMetadata METADATA = new ChannelMetadata(false);
/*  25:    */   
/*  26:    */   protected AbstractOioByteChannel(Channel parent)
/*  27:    */   {
/*  28: 45 */     super(parent);
/*  29:    */   }
/*  30:    */   
/*  31:    */   protected boolean isInputShutdown()
/*  32:    */   {
/*  33: 49 */     return this.inputShutdown;
/*  34:    */   }
/*  35:    */   
/*  36:    */   public ChannelMetadata metadata()
/*  37:    */   {
/*  38: 54 */     return METADATA;
/*  39:    */   }
/*  40:    */   
/*  41:    */   protected boolean checkInputShutdown()
/*  42:    */   {
/*  43: 62 */     if (this.inputShutdown)
/*  44:    */     {
/*  45:    */       try
/*  46:    */       {
/*  47: 64 */         Thread.sleep(1000L);
/*  48:    */       }
/*  49:    */       catch (InterruptedException e) {}
/*  50: 68 */       return true;
/*  51:    */     }
/*  52: 70 */     return false;
/*  53:    */   }
/*  54:    */   
/*  55:    */   protected void doRead()
/*  56:    */   {
/*  57: 75 */     if (checkInputShutdown()) {
/*  58: 76 */       return;
/*  59:    */     }
/*  60: 78 */     ChannelConfig config = config();
/*  61: 79 */     ChannelPipeline pipeline = pipeline();
/*  62:    */     
/*  63: 81 */     RecvByteBufAllocator.Handle allocHandle = this.allocHandle;
/*  64: 82 */     if (allocHandle == null) {
/*  65: 83 */       this.allocHandle = (allocHandle = config.getRecvByteBufAllocator().newHandle());
/*  66:    */     }
/*  67: 86 */     ByteBuf byteBuf = allocHandle.allocate(alloc());
/*  68:    */     
/*  69: 88 */     boolean closed = false;
/*  70: 89 */     boolean read = false;
/*  71: 90 */     Throwable exception = null;
/*  72: 91 */     int localReadAmount = 0;
/*  73:    */     try
/*  74:    */     {
/*  75: 93 */       int totalReadAmount = 0;
/*  76:    */       for (;;)
/*  77:    */       {
/*  78: 96 */         localReadAmount = doReadBytes(byteBuf);
/*  79: 97 */         if (localReadAmount > 0) {
/*  80: 98 */           read = true;
/*  81: 99 */         } else if (localReadAmount < 0) {
/*  82:100 */           closed = true;
/*  83:    */         }
/*  84:103 */         int available = available();
/*  85:104 */         if (available <= 0) {
/*  86:    */           break;
/*  87:    */         }
/*  88:108 */         if (!byteBuf.isWritable())
/*  89:    */         {
/*  90:109 */           int capacity = byteBuf.capacity();
/*  91:110 */           int maxCapacity = byteBuf.maxCapacity();
/*  92:111 */           if (capacity == maxCapacity)
/*  93:    */           {
/*  94:112 */             if (read)
/*  95:    */             {
/*  96:113 */               read = false;
/*  97:114 */               pipeline.fireChannelRead(byteBuf);
/*  98:115 */               byteBuf = alloc().buffer();
/*  99:    */             }
/* 100:    */           }
/* 101:    */           else
/* 102:    */           {
/* 103:118 */             int writerIndex = byteBuf.writerIndex();
/* 104:119 */             if (writerIndex + available > maxCapacity) {
/* 105:120 */               byteBuf.capacity(maxCapacity);
/* 106:    */             } else {
/* 107:122 */               byteBuf.ensureWritable(available);
/* 108:    */             }
/* 109:    */           }
/* 110:    */         }
/* 111:127 */         if (totalReadAmount >= 2147483647 - localReadAmount)
/* 112:    */         {
/* 113:129 */           totalReadAmount = 2147483647;
/* 114:    */         }
/* 115:    */         else
/* 116:    */         {
/* 117:133 */           totalReadAmount += localReadAmount;
/* 118:135 */           if (!config.isAutoRead()) {
/* 119:    */             break;
/* 120:    */           }
/* 121:    */         }
/* 122:    */       }
/* 123:141 */       allocHandle.record(totalReadAmount);
/* 124:    */     }
/* 125:    */     catch (Throwable t)
/* 126:    */     {
/* 127:144 */       exception = t;
/* 128:    */     }
/* 129:    */     finally
/* 130:    */     {
/* 131:146 */       if (read) {
/* 132:147 */         pipeline.fireChannelRead(byteBuf);
/* 133:    */       } else {
/* 134:150 */         byteBuf.release();
/* 135:    */       }
/* 136:153 */       pipeline.fireChannelReadComplete();
/* 137:154 */       if (exception != null) {
/* 138:155 */         if ((exception instanceof IOException))
/* 139:    */         {
/* 140:156 */           closed = true;
/* 141:157 */           pipeline().fireExceptionCaught(exception);
/* 142:    */         }
/* 143:    */         else
/* 144:    */         {
/* 145:159 */           pipeline.fireExceptionCaught(exception);
/* 146:160 */           unsafe().close(voidPromise());
/* 147:    */         }
/* 148:    */       }
/* 149:164 */       if (closed)
/* 150:    */       {
/* 151:165 */         this.inputShutdown = true;
/* 152:166 */         if (isOpen()) {
/* 153:167 */           if (Boolean.TRUE.equals(config().getOption(ChannelOption.ALLOW_HALF_CLOSURE))) {
/* 154:168 */             pipeline.fireUserEventTriggered(ChannelInputShutdownEvent.INSTANCE);
/* 155:    */           } else {
/* 156:170 */             unsafe().close(unsafe().voidPromise());
/* 157:    */           }
/* 158:    */         }
/* 159:    */       }
/* 160:174 */       if ((localReadAmount == 0) && (isActive())) {
/* 161:181 */         read();
/* 162:    */       }
/* 163:    */     }
/* 164:    */   }
/* 165:    */   
/* 166:    */   protected void doWrite(ChannelOutboundBuffer in)
/* 167:    */     throws Exception
/* 168:    */   {
/* 169:    */     for (;;)
/* 170:    */     {
/* 171:189 */       Object msg = in.current();
/* 172:190 */       if (msg == null) {
/* 173:    */         break;
/* 174:    */       }
/* 175:194 */       if ((msg instanceof ByteBuf))
/* 176:    */       {
/* 177:195 */         ByteBuf buf = (ByteBuf)msg;
/* 178:196 */         while (buf.isReadable()) {
/* 179:197 */           doWriteBytes(buf);
/* 180:    */         }
/* 181:199 */         in.remove();
/* 182:    */       }
/* 183:200 */       else if ((msg instanceof FileRegion))
/* 184:    */       {
/* 185:201 */         doWriteFileRegion((FileRegion)msg);
/* 186:202 */         in.remove();
/* 187:    */       }
/* 188:    */       else
/* 189:    */       {
/* 190:204 */         in.remove(new UnsupportedOperationException("unsupported message type: " + StringUtil.simpleClassName(msg)));
/* 191:    */       }
/* 192:    */     }
/* 193:    */   }
/* 194:    */   
/* 195:    */   protected abstract int available();
/* 196:    */   
/* 197:    */   protected abstract int doReadBytes(ByteBuf paramByteBuf)
/* 198:    */     throws Exception;
/* 199:    */   
/* 200:    */   protected abstract void doWriteBytes(ByteBuf paramByteBuf)
/* 201:    */     throws Exception;
/* 202:    */   
/* 203:    */   protected abstract void doWriteFileRegion(FileRegion paramFileRegion)
/* 204:    */     throws Exception;
/* 205:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.oio.AbstractOioByteChannel
 * JD-Core Version:    0.7.0.1
 */