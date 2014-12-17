/*   1:    */ package io.netty.channel.epoll;
/*   2:    */ 
/*   3:    */ import io.netty.channel.AbstractChannel;
/*   4:    */ import io.netty.channel.AbstractChannel.AbstractUnsafe;
/*   5:    */ import io.netty.channel.Channel;
/*   6:    */ import io.netty.channel.ChannelConfig;
/*   7:    */ import io.netty.channel.ChannelMetadata;
/*   8:    */ import io.netty.channel.EventLoop;
/*   9:    */ import io.netty.util.internal.OneTimeTask;
/*  10:    */ import java.net.InetSocketAddress;
/*  11:    */ import java.nio.channels.UnresolvedAddressException;
/*  12:    */ 
/*  13:    */ abstract class AbstractEpollChannel
/*  14:    */   extends AbstractChannel
/*  15:    */ {
/*  16: 28 */   private static final ChannelMetadata DATA = new ChannelMetadata(false);
/*  17:    */   private final int readFlag;
/*  18:    */   protected int flags;
/*  19:    */   protected volatile boolean active;
/*  20:    */   volatile int fd;
/*  21:    */   int id;
/*  22:    */   
/*  23:    */   AbstractEpollChannel(int fd, int flag)
/*  24:    */   {
/*  25: 36 */     this(null, fd, flag, false);
/*  26:    */   }
/*  27:    */   
/*  28:    */   AbstractEpollChannel(Channel parent, int fd, int flag, boolean active)
/*  29:    */   {
/*  30: 40 */     super(parent);
/*  31: 41 */     this.fd = fd;
/*  32: 42 */     this.readFlag = flag;
/*  33: 43 */     this.flags |= flag;
/*  34: 44 */     this.active = active;
/*  35:    */   }
/*  36:    */   
/*  37:    */   public boolean isActive()
/*  38:    */   {
/*  39: 49 */     return this.active;
/*  40:    */   }
/*  41:    */   
/*  42:    */   public ChannelMetadata metadata()
/*  43:    */   {
/*  44: 54 */     return DATA;
/*  45:    */   }
/*  46:    */   
/*  47:    */   protected void doClose()
/*  48:    */     throws Exception
/*  49:    */   {
/*  50: 59 */     this.active = false;
/*  51:    */     
/*  52:    */ 
/*  53: 62 */     doDeregister();
/*  54:    */     
/*  55: 64 */     int fd = this.fd;
/*  56: 65 */     this.fd = -1;
/*  57: 66 */     Native.close(fd);
/*  58:    */   }
/*  59:    */   
/*  60:    */   public InetSocketAddress remoteAddress()
/*  61:    */   {
/*  62: 71 */     return (InetSocketAddress)super.remoteAddress();
/*  63:    */   }
/*  64:    */   
/*  65:    */   public InetSocketAddress localAddress()
/*  66:    */   {
/*  67: 76 */     return (InetSocketAddress)super.localAddress();
/*  68:    */   }
/*  69:    */   
/*  70:    */   protected void doDisconnect()
/*  71:    */     throws Exception
/*  72:    */   {
/*  73: 81 */     doClose();
/*  74:    */   }
/*  75:    */   
/*  76:    */   protected boolean isCompatible(EventLoop loop)
/*  77:    */   {
/*  78: 86 */     return loop instanceof EpollEventLoop;
/*  79:    */   }
/*  80:    */   
/*  81:    */   public boolean isOpen()
/*  82:    */   {
/*  83: 91 */     return this.fd != -1;
/*  84:    */   }
/*  85:    */   
/*  86:    */   protected void doDeregister()
/*  87:    */     throws Exception
/*  88:    */   {
/*  89: 96 */     ((EpollEventLoop)eventLoop()).remove(this);
/*  90:    */   }
/*  91:    */   
/*  92:    */   protected void doBeginRead()
/*  93:    */     throws Exception
/*  94:    */   {
/*  95:101 */     if ((this.flags & this.readFlag) == 0)
/*  96:    */     {
/*  97:102 */       this.flags |= this.readFlag;
/*  98:103 */       modifyEvents();
/*  99:    */     }
/* 100:    */   }
/* 101:    */   
/* 102:    */   final void clearEpollIn()
/* 103:    */   {
/* 104:109 */     if (isRegistered())
/* 105:    */     {
/* 106:110 */       EventLoop loop = eventLoop();
/* 107:111 */       final AbstractEpollUnsafe unsafe = (AbstractEpollUnsafe)unsafe();
/* 108:112 */       if (loop.inEventLoop()) {
/* 109:113 */         unsafe.clearEpollIn0();
/* 110:    */       } else {
/* 111:116 */         loop.execute(new OneTimeTask()
/* 112:    */         {
/* 113:    */           public void run()
/* 114:    */           {
/* 115:119 */             if ((!AbstractEpollChannel.this.config().isAutoRead()) && (!unsafe.readPending)) {
/* 116:121 */               unsafe.clearEpollIn0();
/* 117:    */             }
/* 118:    */           }
/* 119:    */         });
/* 120:    */       }
/* 121:    */     }
/* 122:    */     else
/* 123:    */     {
/* 124:129 */       this.flags &= (this.readFlag ^ 0xFFFFFFFF);
/* 125:    */     }
/* 126:    */   }
/* 127:    */   
/* 128:    */   protected final void setEpollOut()
/* 129:    */   {
/* 130:134 */     if ((this.flags & 0x2) == 0)
/* 131:    */     {
/* 132:135 */       this.flags |= 0x2;
/* 133:136 */       modifyEvents();
/* 134:    */     }
/* 135:    */   }
/* 136:    */   
/* 137:    */   protected final void clearEpollOut()
/* 138:    */   {
/* 139:141 */     if ((this.flags & 0x2) != 0)
/* 140:    */     {
/* 141:142 */       this.flags &= 0xFFFFFFFD;
/* 142:143 */       modifyEvents();
/* 143:    */     }
/* 144:    */   }
/* 145:    */   
/* 146:    */   private void modifyEvents()
/* 147:    */   {
/* 148:148 */     if (isOpen()) {
/* 149:149 */       ((EpollEventLoop)eventLoop()).modify(this);
/* 150:    */     }
/* 151:    */   }
/* 152:    */   
/* 153:    */   protected void doRegister()
/* 154:    */     throws Exception
/* 155:    */   {
/* 156:155 */     EpollEventLoop loop = (EpollEventLoop)eventLoop();
/* 157:156 */     loop.add(this);
/* 158:    */   }
/* 159:    */   
/* 160:    */   protected abstract AbstractEpollUnsafe newUnsafe();
/* 161:    */   
/* 162:    */   protected static void checkResolvable(InetSocketAddress addr)
/* 163:    */   {
/* 164:163 */     if (addr.isUnresolved()) {
/* 165:164 */       throw new UnresolvedAddressException();
/* 166:    */     }
/* 167:    */   }
/* 168:    */   
/* 169:    */   protected abstract class AbstractEpollUnsafe
/* 170:    */     extends AbstractChannel.AbstractUnsafe
/* 171:    */   {
/* 172:    */     protected boolean readPending;
/* 173:    */     
/* 174:    */     protected AbstractEpollUnsafe()
/* 175:    */     {
/* 176:168 */       super();
/* 177:    */     }
/* 178:    */     
/* 179:    */     abstract void epollInReady();
/* 180:    */     
/* 181:    */     void epollRdHupReady() {}
/* 182:    */     
/* 183:    */     public void beginRead()
/* 184:    */     {
/* 185:186 */       this.readPending = true;
/* 186:187 */       super.beginRead();
/* 187:    */     }
/* 188:    */     
/* 189:    */     protected void flush0()
/* 190:    */     {
/* 191:195 */       if (isFlushPending()) {
/* 192:196 */         return;
/* 193:    */       }
/* 194:198 */       super.flush0();
/* 195:    */     }
/* 196:    */     
/* 197:    */     void epollOutReady()
/* 198:    */     {
/* 199:206 */       super.flush0();
/* 200:    */     }
/* 201:    */     
/* 202:    */     private boolean isFlushPending()
/* 203:    */     {
/* 204:210 */       return (AbstractEpollChannel.this.flags & 0x2) != 0;
/* 205:    */     }
/* 206:    */     
/* 207:    */     protected final void clearEpollIn0()
/* 208:    */     {
/* 209:214 */       if ((AbstractEpollChannel.this.flags & AbstractEpollChannel.this.readFlag) != 0)
/* 210:    */       {
/* 211:215 */         AbstractEpollChannel.this.flags &= (AbstractEpollChannel.this.readFlag ^ 0xFFFFFFFF);
/* 212:216 */         AbstractEpollChannel.this.modifyEvents();
/* 213:    */       }
/* 214:    */     }
/* 215:    */   }
/* 216:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.epoll.AbstractEpollChannel
 * JD-Core Version:    0.7.0.1
 */