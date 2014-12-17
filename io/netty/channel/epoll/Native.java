/*   1:    */ package io.netty.channel.epoll;
/*   2:    */ 
/*   3:    */ import io.netty.channel.ChannelException;
/*   4:    */ import io.netty.channel.DefaultFileRegion;
/*   5:    */ import io.netty.util.internal.NativeLibraryLoader;
/*   6:    */ import io.netty.util.internal.PlatformDependent;
/*   7:    */ import io.netty.util.internal.SystemPropertyUtil;
/*   8:    */ import java.io.IOException;
/*   9:    */ import java.net.Inet6Address;
/*  10:    */ import java.net.InetAddress;
/*  11:    */ import java.net.InetSocketAddress;
/*  12:    */ import java.nio.ByteBuffer;
/*  13:    */ import java.util.Locale;
/*  14:    */ 
/*  15:    */ final class Native
/*  16:    */ {
/*  17: 38 */   private static final byte[] IPV4_MAPPED_IPV6_PREFIX = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1 };
/*  18:    */   public static final int EPOLLIN = 1;
/*  19:    */   public static final int EPOLLOUT = 2;
/*  20:    */   public static final int EPOLLACCEPT = 4;
/*  21:    */   public static final int EPOLLRDHUP = 8;
/*  22:    */   
/*  23:    */   static
/*  24:    */   {
/*  25: 42 */     String name = SystemPropertyUtil.get("os.name").toLowerCase(Locale.UK).trim();
/*  26: 43 */     if (!name.startsWith("linux")) {
/*  27: 44 */       throw new IllegalStateException("Only supported on Linux");
/*  28:    */     }
/*  29: 46 */     NativeLibraryLoader.load("netty-transport-native-epoll", PlatformDependent.getClassLoader(Native.class));
/*  30:    */   }
/*  31:    */   
/*  32:    */   public static int sendTo(int fd, ByteBuffer buf, int pos, int limit, InetAddress addr, int port)
/*  33:    */     throws IOException
/*  34:    */   {
/*  35:    */     int scopeId;
/*  36:    */     int scopeId;
/*  37:    */     byte[] address;
/*  38: 82 */     if ((addr instanceof Inet6Address))
/*  39:    */     {
/*  40: 83 */       byte[] address = addr.getAddress();
/*  41: 84 */       scopeId = ((Inet6Address)addr).getScopeId();
/*  42:    */     }
/*  43:    */     else
/*  44:    */     {
/*  45: 87 */       scopeId = 0;
/*  46: 88 */       address = ipv4MappedIpv6Address(addr.getAddress());
/*  47:    */     }
/*  48: 90 */     return sendTo(fd, buf, pos, limit, address, scopeId, port);
/*  49:    */   }
/*  50:    */   
/*  51:    */   public static int sendToAddress(int fd, long memoryAddress, int pos, int limit, InetAddress addr, int port)
/*  52:    */     throws IOException
/*  53:    */   {
/*  54:    */     int scopeId;
/*  55:    */     int scopeId;
/*  56:    */     byte[] address;
/*  57:102 */     if ((addr instanceof Inet6Address))
/*  58:    */     {
/*  59:103 */       byte[] address = addr.getAddress();
/*  60:104 */       scopeId = ((Inet6Address)addr).getScopeId();
/*  61:    */     }
/*  62:    */     else
/*  63:    */     {
/*  64:107 */       scopeId = 0;
/*  65:108 */       address = ipv4MappedIpv6Address(addr.getAddress());
/*  66:    */     }
/*  67:110 */     return sendToAddress(fd, memoryAddress, pos, limit, address, scopeId, port);
/*  68:    */   }
/*  69:    */   
/*  70:    */   public static int socketStreamFd()
/*  71:    */   {
/*  72:    */     try
/*  73:    */     {
/*  74:125 */       return socketStream();
/*  75:    */     }
/*  76:    */     catch (IOException e)
/*  77:    */     {
/*  78:127 */       throw new ChannelException(e);
/*  79:    */     }
/*  80:    */   }
/*  81:    */   
/*  82:    */   public static int socketDgramFd()
/*  83:    */   {
/*  84:    */     try
/*  85:    */     {
/*  86:133 */       return socketDgram();
/*  87:    */     }
/*  88:    */     catch (IOException e)
/*  89:    */     {
/*  90:135 */       throw new ChannelException(e);
/*  91:    */     }
/*  92:    */   }
/*  93:    */   
/*  94:    */   public static void bind(int fd, InetAddress addr, int port)
/*  95:    */     throws IOException
/*  96:    */   {
/*  97:142 */     NativeInetAddress address = toNativeInetAddress(addr);
/*  98:143 */     bind(fd, address.address, address.scopeId, port);
/*  99:    */   }
/* 100:    */   
/* 101:    */   private static byte[] ipv4MappedIpv6Address(byte[] ipv4)
/* 102:    */   {
/* 103:147 */     byte[] address = new byte[16];
/* 104:148 */     System.arraycopy(IPV4_MAPPED_IPV6_PREFIX, 0, address, 0, IPV4_MAPPED_IPV6_PREFIX.length);
/* 105:149 */     System.arraycopy(ipv4, 0, address, 12, ipv4.length);
/* 106:150 */     return address;
/* 107:    */   }
/* 108:    */   
/* 109:    */   public static boolean connect(int fd, InetAddress addr, int port)
/* 110:    */     throws IOException
/* 111:    */   {
/* 112:156 */     NativeInetAddress address = toNativeInetAddress(addr);
/* 113:157 */     return connect(fd, address.address, address.scopeId, port);
/* 114:    */   }
/* 115:    */   
/* 116:    */   private static NativeInetAddress toNativeInetAddress(InetAddress addr)
/* 117:    */   {
/* 118:197 */     byte[] bytes = addr.getAddress();
/* 119:198 */     if ((addr instanceof Inet6Address)) {
/* 120:199 */       return new NativeInetAddress(bytes, ((Inet6Address)addr).getScopeId());
/* 121:    */     }
/* 122:202 */     return new NativeInetAddress(ipv4MappedIpv6Address(bytes));
/* 123:    */   }
/* 124:    */   
/* 125:    */   public static native int eventFd();
/* 126:    */   
/* 127:    */   public static native void eventFdWrite(int paramInt, long paramLong);
/* 128:    */   
/* 129:    */   public static native void eventFdRead(int paramInt);
/* 130:    */   
/* 131:    */   public static native int epollCreate();
/* 132:    */   
/* 133:    */   public static native int epollWait(int paramInt1, long[] paramArrayOfLong, int paramInt2);
/* 134:    */   
/* 135:    */   public static native void epollCtlAdd(int paramInt1, int paramInt2, int paramInt3, int paramInt4);
/* 136:    */   
/* 137:    */   public static native void epollCtlMod(int paramInt1, int paramInt2, int paramInt3, int paramInt4);
/* 138:    */   
/* 139:    */   public static native void epollCtlDel(int paramInt1, int paramInt2);
/* 140:    */   
/* 141:    */   public static native void close(int paramInt)
/* 142:    */     throws IOException;
/* 143:    */   
/* 144:    */   public static native int write(int paramInt1, ByteBuffer paramByteBuffer, int paramInt2, int paramInt3)
/* 145:    */     throws IOException;
/* 146:    */   
/* 147:    */   public static native int writeAddress(int paramInt1, long paramLong, int paramInt2, int paramInt3)
/* 148:    */     throws IOException;
/* 149:    */   
/* 150:    */   public static native long writev(int paramInt1, ByteBuffer[] paramArrayOfByteBuffer, int paramInt2, int paramInt3)
/* 151:    */     throws IOException;
/* 152:    */   
/* 153:    */   public static native int read(int paramInt1, ByteBuffer paramByteBuffer, int paramInt2, int paramInt3)
/* 154:    */     throws IOException;
/* 155:    */   
/* 156:    */   public static native int readAddress(int paramInt1, long paramLong, int paramInt2, int paramInt3)
/* 157:    */     throws IOException;
/* 158:    */   
/* 159:    */   public static native long sendfile(int paramInt, DefaultFileRegion paramDefaultFileRegion, long paramLong1, long paramLong2)
/* 160:    */     throws IOException;
/* 161:    */   
/* 162:    */   private static native int sendTo(int paramInt1, ByteBuffer paramByteBuffer, int paramInt2, int paramInt3, byte[] paramArrayOfByte, int paramInt4, int paramInt5)
/* 163:    */     throws IOException;
/* 164:    */   
/* 165:    */   private static native int sendToAddress(int paramInt1, long paramLong, int paramInt2, int paramInt3, byte[] paramArrayOfByte, int paramInt4, int paramInt5)
/* 166:    */     throws IOException;
/* 167:    */   
/* 168:    */   public static native EpollDatagramChannel.DatagramSocketAddress recvFrom(int paramInt1, ByteBuffer paramByteBuffer, int paramInt2, int paramInt3)
/* 169:    */     throws IOException;
/* 170:    */   
/* 171:    */   public static native EpollDatagramChannel.DatagramSocketAddress recvFromAddress(int paramInt1, long paramLong, int paramInt2, int paramInt3)
/* 172:    */     throws IOException;
/* 173:    */   
/* 174:    */   private static native int socketStream()
/* 175:    */     throws IOException;
/* 176:    */   
/* 177:    */   private static native int socketDgram()
/* 178:    */     throws IOException;
/* 179:    */   
/* 180:    */   public static native void bind(int paramInt1, byte[] paramArrayOfByte, int paramInt2, int paramInt3)
/* 181:    */     throws IOException;
/* 182:    */   
/* 183:    */   public static native void listen(int paramInt1, int paramInt2)
/* 184:    */     throws IOException;
/* 185:    */   
/* 186:    */   public static native boolean connect(int paramInt1, byte[] paramArrayOfByte, int paramInt2, int paramInt3)
/* 187:    */     throws IOException;
/* 188:    */   
/* 189:    */   public static native boolean finishConnect(int paramInt)
/* 190:    */     throws IOException;
/* 191:    */   
/* 192:    */   public static native InetSocketAddress remoteAddress(int paramInt);
/* 193:    */   
/* 194:    */   public static native InetSocketAddress localAddress(int paramInt);
/* 195:    */   
/* 196:    */   public static native int accept(int paramInt)
/* 197:    */     throws IOException;
/* 198:    */   
/* 199:    */   public static native void shutdown(int paramInt, boolean paramBoolean1, boolean paramBoolean2)
/* 200:    */     throws IOException;
/* 201:    */   
/* 202:    */   public static native int getReceiveBufferSize(int paramInt);
/* 203:    */   
/* 204:    */   public static native int getSendBufferSize(int paramInt);
/* 205:    */   
/* 206:    */   public static native int isKeepAlive(int paramInt);
/* 207:    */   
/* 208:    */   public static native int isReuseAddress(int paramInt);
/* 209:    */   
/* 210:    */   public static native int isReusePort(int paramInt);
/* 211:    */   
/* 212:    */   public static native int isTcpNoDelay(int paramInt);
/* 213:    */   
/* 214:    */   public static native int isTcpCork(int paramInt);
/* 215:    */   
/* 216:    */   public static native int getSoLinger(int paramInt);
/* 217:    */   
/* 218:    */   public static native int getTrafficClass(int paramInt);
/* 219:    */   
/* 220:    */   public static native int isBroadcast(int paramInt);
/* 221:    */   
/* 222:    */   public static native int getTcpKeepIdle(int paramInt);
/* 223:    */   
/* 224:    */   public static native int getTcpKeepIntvl(int paramInt);
/* 225:    */   
/* 226:    */   public static native int getTcpKeepCnt(int paramInt);
/* 227:    */   
/* 228:    */   public static native void setKeepAlive(int paramInt1, int paramInt2);
/* 229:    */   
/* 230:    */   public static native void setReceiveBufferSize(int paramInt1, int paramInt2);
/* 231:    */   
/* 232:    */   public static native void setReuseAddress(int paramInt1, int paramInt2);
/* 233:    */   
/* 234:    */   public static native void setReusePort(int paramInt1, int paramInt2);
/* 235:    */   
/* 236:    */   public static native void setSendBufferSize(int paramInt1, int paramInt2);
/* 237:    */   
/* 238:    */   public static native void setTcpNoDelay(int paramInt1, int paramInt2);
/* 239:    */   
/* 240:    */   public static native void setTcpCork(int paramInt1, int paramInt2);
/* 241:    */   
/* 242:    */   public static native void setSoLinger(int paramInt1, int paramInt2);
/* 243:    */   
/* 244:    */   public static native void setTrafficClass(int paramInt1, int paramInt2);
/* 245:    */   
/* 246:    */   public static native void setBroadcast(int paramInt1, int paramInt2);
/* 247:    */   
/* 248:    */   public static native void setTcpKeepIdle(int paramInt1, int paramInt2);
/* 249:    */   
/* 250:    */   public static native void setTcpKeepIntvl(int paramInt1, int paramInt2);
/* 251:    */   
/* 252:    */   public static native void setTcpKeepCnt(int paramInt1, int paramInt2);
/* 253:    */   
/* 254:    */   public static native String kernelVersion();
/* 255:    */   
/* 256:    */   private static class NativeInetAddress
/* 257:    */   {
/* 258:    */     final byte[] address;
/* 259:    */     final int scopeId;
/* 260:    */     
/* 261:    */     NativeInetAddress(byte[] address, int scopeId)
/* 262:    */     {
/* 263:211 */       this.address = address;
/* 264:212 */       this.scopeId = scopeId;
/* 265:    */     }
/* 266:    */     
/* 267:    */     NativeInetAddress(byte[] address)
/* 268:    */     {
/* 269:216 */       this(address, 0);
/* 270:    */     }
/* 271:    */   }
/* 272:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.epoll.Native
 * JD-Core Version:    0.7.0.1
 */