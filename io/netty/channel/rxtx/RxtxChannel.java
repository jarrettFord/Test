/*   1:    */ package io.netty.channel.rxtx;
/*   2:    */ 
/*   3:    */ import gnu.io.CommPort;
/*   4:    */ import gnu.io.CommPortIdentifier;
/*   5:    */ import gnu.io.SerialPort;
/*   6:    */ import io.netty.channel.AbstractChannel;
/*   7:    */ import io.netty.channel.AbstractChannel.AbstractUnsafe;
/*   8:    */ import io.netty.channel.ChannelPipeline;
/*   9:    */ import io.netty.channel.ChannelPromise;
/*  10:    */ import io.netty.channel.EventLoop;
/*  11:    */ import io.netty.channel.oio.OioByteStreamChannel;
/*  12:    */ import java.net.SocketAddress;
/*  13:    */ import java.util.concurrent.TimeUnit;
/*  14:    */ 
/*  15:    */ public class RxtxChannel
/*  16:    */   extends OioByteStreamChannel
/*  17:    */ {
/*  18: 34 */   private static final RxtxDeviceAddress LOCAL_ADDRESS = new RxtxDeviceAddress("localhost");
/*  19:    */   private final RxtxChannelConfig config;
/*  20: 38 */   private boolean open = true;
/*  21:    */   private RxtxDeviceAddress deviceAddress;
/*  22:    */   private SerialPort serialPort;
/*  23:    */   
/*  24:    */   public RxtxChannel()
/*  25:    */   {
/*  26: 43 */     super(null);
/*  27:    */     
/*  28: 45 */     this.config = new DefaultRxtxChannelConfig(this);
/*  29:    */   }
/*  30:    */   
/*  31:    */   public RxtxChannelConfig config()
/*  32:    */   {
/*  33: 50 */     return this.config;
/*  34:    */   }
/*  35:    */   
/*  36:    */   public boolean isOpen()
/*  37:    */   {
/*  38: 55 */     return this.open;
/*  39:    */   }
/*  40:    */   
/*  41:    */   protected AbstractChannel.AbstractUnsafe newUnsafe()
/*  42:    */   {
/*  43: 60 */     return new RxtxUnsafe(null);
/*  44:    */   }
/*  45:    */   
/*  46:    */   protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress)
/*  47:    */     throws Exception
/*  48:    */   {
/*  49: 65 */     RxtxDeviceAddress remote = (RxtxDeviceAddress)remoteAddress;
/*  50: 66 */     CommPortIdentifier cpi = CommPortIdentifier.getPortIdentifier(remote.value());
/*  51: 67 */     CommPort commPort = cpi.open(getClass().getName(), 1000);
/*  52: 68 */     commPort.enableReceiveTimeout(((Integer)config().getOption(RxtxChannelOption.READ_TIMEOUT)).intValue());
/*  53: 69 */     this.deviceAddress = remote;
/*  54:    */     
/*  55: 71 */     this.serialPort = ((SerialPort)commPort);
/*  56:    */   }
/*  57:    */   
/*  58:    */   protected void doInit()
/*  59:    */     throws Exception
/*  60:    */   {
/*  61: 75 */     this.serialPort.setSerialPortParams(((Integer)config().getOption(RxtxChannelOption.BAUD_RATE)).intValue(), ((RxtxChannelConfig.Databits)config().getOption(RxtxChannelOption.DATA_BITS)).value(), ((RxtxChannelConfig.Stopbits)config().getOption(RxtxChannelOption.STOP_BITS)).value(), ((RxtxChannelConfig.Paritybit)config().getOption(RxtxChannelOption.PARITY_BIT)).value());
/*  62:    */     
/*  63:    */ 
/*  64:    */ 
/*  65:    */ 
/*  66:    */ 
/*  67: 81 */     this.serialPort.setDTR(((Boolean)config().getOption(RxtxChannelOption.DTR)).booleanValue());
/*  68: 82 */     this.serialPort.setRTS(((Boolean)config().getOption(RxtxChannelOption.RTS)).booleanValue());
/*  69:    */     
/*  70: 84 */     activate(this.serialPort.getInputStream(), this.serialPort.getOutputStream());
/*  71:    */   }
/*  72:    */   
/*  73:    */   public RxtxDeviceAddress localAddress()
/*  74:    */   {
/*  75: 89 */     return (RxtxDeviceAddress)super.localAddress();
/*  76:    */   }
/*  77:    */   
/*  78:    */   public RxtxDeviceAddress remoteAddress()
/*  79:    */   {
/*  80: 94 */     return (RxtxDeviceAddress)super.remoteAddress();
/*  81:    */   }
/*  82:    */   
/*  83:    */   protected RxtxDeviceAddress localAddress0()
/*  84:    */   {
/*  85: 99 */     return LOCAL_ADDRESS;
/*  86:    */   }
/*  87:    */   
/*  88:    */   protected RxtxDeviceAddress remoteAddress0()
/*  89:    */   {
/*  90:104 */     return this.deviceAddress;
/*  91:    */   }
/*  92:    */   
/*  93:    */   protected void doBind(SocketAddress localAddress)
/*  94:    */     throws Exception
/*  95:    */   {
/*  96:109 */     throw new UnsupportedOperationException();
/*  97:    */   }
/*  98:    */   
/*  99:    */   protected void doDisconnect()
/* 100:    */     throws Exception
/* 101:    */   {
/* 102:114 */     doClose();
/* 103:    */   }
/* 104:    */   
/* 105:    */   protected void doClose()
/* 106:    */     throws Exception
/* 107:    */   {
/* 108:119 */     this.open = false;
/* 109:    */     try
/* 110:    */     {
/* 111:121 */       super.doClose();
/* 112:    */     }
/* 113:    */     finally
/* 114:    */     {
/* 115:123 */       if (this.serialPort != null)
/* 116:    */       {
/* 117:124 */         this.serialPort.removeEventListener();
/* 118:125 */         this.serialPort.close();
/* 119:126 */         this.serialPort = null;
/* 120:    */       }
/* 121:    */     }
/* 122:    */   }
/* 123:    */   
/* 124:    */   private final class RxtxUnsafe
/* 125:    */     extends AbstractChannel.AbstractUnsafe
/* 126:    */   {
/* 127:    */     private RxtxUnsafe()
/* 128:    */     {
/* 129:131 */       super();
/* 130:    */     }
/* 131:    */     
/* 132:    */     public void connect(SocketAddress remoteAddress, SocketAddress localAddress, final ChannelPromise promise)
/* 133:    */     {
/* 134:136 */       if ((!promise.setUncancellable()) || (!ensureOpen(promise))) {
/* 135:137 */         return;
/* 136:    */       }
/* 137:    */       try
/* 138:    */       {
/* 139:141 */         final boolean wasActive = RxtxChannel.this.isActive();
/* 140:142 */         RxtxChannel.this.doConnect(remoteAddress, localAddress);
/* 141:    */         
/* 142:144 */         int waitTime = ((Integer)RxtxChannel.this.config().getOption(RxtxChannelOption.WAIT_TIME)).intValue();
/* 143:145 */         if (waitTime > 0)
/* 144:    */         {
/* 145:146 */           RxtxChannel.this.eventLoop().schedule(new Runnable()
/* 146:    */           {
/* 147:    */             public void run()
/* 148:    */             {
/* 149:    */               try
/* 150:    */               {
/* 151:150 */                 RxtxChannel.this.doInit();
/* 152:151 */                 RxtxChannel.RxtxUnsafe.this.safeSetSuccess(promise);
/* 153:152 */                 if ((!wasActive) && (RxtxChannel.this.isActive())) {
/* 154:153 */                   RxtxChannel.this.pipeline().fireChannelActive();
/* 155:    */                 }
/* 156:    */               }
/* 157:    */               catch (Throwable t)
/* 158:    */               {
/* 159:156 */                 RxtxChannel.RxtxUnsafe.this.safeSetFailure(promise, t);
/* 160:157 */                 RxtxChannel.RxtxUnsafe.this.closeIfClosed();
/* 161:    */               }
/* 162:    */             }
/* 163:157 */           }, waitTime, TimeUnit.MILLISECONDS);
/* 164:    */         }
/* 165:    */         else
/* 166:    */         {
/* 167:162 */           RxtxChannel.this.doInit();
/* 168:163 */           safeSetSuccess(promise);
/* 169:164 */           if ((!wasActive) && (RxtxChannel.this.isActive())) {
/* 170:165 */             RxtxChannel.this.pipeline().fireChannelActive();
/* 171:    */           }
/* 172:    */         }
/* 173:    */       }
/* 174:    */       catch (Throwable t)
/* 175:    */       {
/* 176:169 */         safeSetFailure(promise, t);
/* 177:170 */         closeIfClosed();
/* 178:    */       }
/* 179:    */     }
/* 180:    */   }
/* 181:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.rxtx.RxtxChannel
 * JD-Core Version:    0.7.0.1
 */