/*   1:    */ package io.netty.channel.socket.nio;
/*   2:    */ 
/*   3:    */ import io.netty.channel.ChannelException;
/*   4:    */ import io.netty.channel.socket.DatagramChannelConfig;
/*   5:    */ import io.netty.channel.socket.DefaultDatagramChannelConfig;
/*   6:    */ import io.netty.util.internal.PlatformDependent;
/*   7:    */ import java.lang.reflect.Field;
/*   8:    */ import java.lang.reflect.Method;
/*   9:    */ import java.net.InetAddress;
/*  10:    */ import java.net.NetworkInterface;
/*  11:    */ import java.net.SocketException;
/*  12:    */ import java.nio.channels.DatagramChannel;
/*  13:    */ import java.nio.channels.NetworkChannel;
/*  14:    */ import java.util.Enumeration;
/*  15:    */ 
/*  16:    */ class NioDatagramChannelConfig
/*  17:    */   extends DefaultDatagramChannelConfig
/*  18:    */ {
/*  19:    */   private static final Object IP_MULTICAST_TTL;
/*  20:    */   private static final Object IP_MULTICAST_IF;
/*  21:    */   private static final Object IP_MULTICAST_LOOP;
/*  22:    */   private static final Method GET_OPTION;
/*  23:    */   private static final Method SET_OPTION;
/*  24:    */   private final DatagramChannel javaChannel;
/*  25:    */   
/*  26:    */   static
/*  27:    */   {
/*  28: 43 */     ClassLoader classLoader = PlatformDependent.getClassLoader(DatagramChannel.class);
/*  29: 44 */     Class<?> socketOptionType = null;
/*  30:    */     try
/*  31:    */     {
/*  32: 46 */       socketOptionType = Class.forName("java.net.SocketOption", true, classLoader);
/*  33:    */     }
/*  34:    */     catch (Exception e) {}
/*  35: 50 */     Class<?> stdSocketOptionType = null;
/*  36:    */     try
/*  37:    */     {
/*  38: 52 */       stdSocketOptionType = Class.forName("java.net.StandardSocketOptions", true, classLoader);
/*  39:    */     }
/*  40:    */     catch (Exception e) {}
/*  41: 57 */     Object ipMulticastTtl = null;
/*  42: 58 */     Object ipMulticastIf = null;
/*  43: 59 */     Object ipMulticastLoop = null;
/*  44: 60 */     Method getOption = null;
/*  45: 61 */     Method setOption = null;
/*  46: 62 */     if (socketOptionType != null)
/*  47:    */     {
/*  48:    */       try
/*  49:    */       {
/*  50: 64 */         ipMulticastTtl = stdSocketOptionType.getDeclaredField("IP_MULTICAST_TTL").get(null);
/*  51:    */       }
/*  52:    */       catch (Exception e)
/*  53:    */       {
/*  54: 66 */         throw new Error("cannot locate the IP_MULTICAST_TTL field", e);
/*  55:    */       }
/*  56:    */       try
/*  57:    */       {
/*  58: 70 */         ipMulticastIf = stdSocketOptionType.getDeclaredField("IP_MULTICAST_IF").get(null);
/*  59:    */       }
/*  60:    */       catch (Exception e)
/*  61:    */       {
/*  62: 72 */         throw new Error("cannot locate the IP_MULTICAST_IF field", e);
/*  63:    */       }
/*  64:    */       try
/*  65:    */       {
/*  66: 76 */         ipMulticastLoop = stdSocketOptionType.getDeclaredField("IP_MULTICAST_LOOP").get(null);
/*  67:    */       }
/*  68:    */       catch (Exception e)
/*  69:    */       {
/*  70: 78 */         throw new Error("cannot locate the IP_MULTICAST_LOOP field", e);
/*  71:    */       }
/*  72:    */       try
/*  73:    */       {
/*  74: 82 */         getOption = NetworkChannel.class.getDeclaredMethod("getOption", new Class[] { socketOptionType });
/*  75:    */       }
/*  76:    */       catch (Exception e)
/*  77:    */       {
/*  78: 84 */         throw new Error("cannot locate the getOption() method", e);
/*  79:    */       }
/*  80:    */       try
/*  81:    */       {
/*  82: 88 */         setOption = NetworkChannel.class.getDeclaredMethod("setOption", new Class[] { socketOptionType, Object.class });
/*  83:    */       }
/*  84:    */       catch (Exception e)
/*  85:    */       {
/*  86: 90 */         throw new Error("cannot locate the setOption() method", e);
/*  87:    */       }
/*  88:    */     }
/*  89: 93 */     IP_MULTICAST_TTL = ipMulticastTtl;
/*  90: 94 */     IP_MULTICAST_IF = ipMulticastIf;
/*  91: 95 */     IP_MULTICAST_LOOP = ipMulticastLoop;
/*  92: 96 */     GET_OPTION = getOption;
/*  93: 97 */     SET_OPTION = setOption;
/*  94:    */   }
/*  95:    */   
/*  96:    */   NioDatagramChannelConfig(NioDatagramChannel channel, DatagramChannel javaChannel)
/*  97:    */   {
/*  98:103 */     super(channel, javaChannel.socket());
/*  99:104 */     this.javaChannel = javaChannel;
/* 100:    */   }
/* 101:    */   
/* 102:    */   public int getTimeToLive()
/* 103:    */   {
/* 104:109 */     return ((Integer)getOption0(IP_MULTICAST_TTL)).intValue();
/* 105:    */   }
/* 106:    */   
/* 107:    */   public DatagramChannelConfig setTimeToLive(int ttl)
/* 108:    */   {
/* 109:114 */     setOption0(IP_MULTICAST_TTL, Integer.valueOf(ttl));
/* 110:115 */     return this;
/* 111:    */   }
/* 112:    */   
/* 113:    */   public InetAddress getInterface()
/* 114:    */   {
/* 115:120 */     NetworkInterface inf = getNetworkInterface();
/* 116:121 */     if (inf == null) {
/* 117:122 */       return null;
/* 118:    */     }
/* 119:124 */     Enumeration<InetAddress> addresses = inf.getInetAddresses();
/* 120:125 */     if (addresses.hasMoreElements()) {
/* 121:126 */       return (InetAddress)addresses.nextElement();
/* 122:    */     }
/* 123:128 */     return null;
/* 124:    */   }
/* 125:    */   
/* 126:    */   public DatagramChannelConfig setInterface(InetAddress interfaceAddress)
/* 127:    */   {
/* 128:    */     try
/* 129:    */     {
/* 130:135 */       setNetworkInterface(NetworkInterface.getByInetAddress(interfaceAddress));
/* 131:    */     }
/* 132:    */     catch (SocketException e)
/* 133:    */     {
/* 134:137 */       throw new ChannelException(e);
/* 135:    */     }
/* 136:139 */     return this;
/* 137:    */   }
/* 138:    */   
/* 139:    */   public NetworkInterface getNetworkInterface()
/* 140:    */   {
/* 141:144 */     return (NetworkInterface)getOption0(IP_MULTICAST_IF);
/* 142:    */   }
/* 143:    */   
/* 144:    */   public DatagramChannelConfig setNetworkInterface(NetworkInterface networkInterface)
/* 145:    */   {
/* 146:149 */     setOption0(IP_MULTICAST_IF, networkInterface);
/* 147:150 */     return this;
/* 148:    */   }
/* 149:    */   
/* 150:    */   public boolean isLoopbackModeDisabled()
/* 151:    */   {
/* 152:155 */     return ((Boolean)getOption0(IP_MULTICAST_LOOP)).booleanValue();
/* 153:    */   }
/* 154:    */   
/* 155:    */   public DatagramChannelConfig setLoopbackModeDisabled(boolean loopbackModeDisabled)
/* 156:    */   {
/* 157:160 */     setOption0(IP_MULTICAST_LOOP, Boolean.valueOf(loopbackModeDisabled));
/* 158:161 */     return this;
/* 159:    */   }
/* 160:    */   
/* 161:    */   public DatagramChannelConfig setAutoRead(boolean autoRead)
/* 162:    */   {
/* 163:166 */     super.setAutoRead(autoRead);
/* 164:167 */     return this;
/* 165:    */   }
/* 166:    */   
/* 167:    */   protected void autoReadCleared()
/* 168:    */   {
/* 169:172 */     ((NioDatagramChannel)this.channel).setReadPending(false);
/* 170:    */   }
/* 171:    */   
/* 172:    */   private Object getOption0(Object option)
/* 173:    */   {
/* 174:176 */     if (PlatformDependent.javaVersion() < 7) {
/* 175:177 */       throw new UnsupportedOperationException();
/* 176:    */     }
/* 177:    */     try
/* 178:    */     {
/* 179:180 */       return GET_OPTION.invoke(this.javaChannel, new Object[] { option });
/* 180:    */     }
/* 181:    */     catch (Exception e)
/* 182:    */     {
/* 183:182 */       throw new ChannelException(e);
/* 184:    */     }
/* 185:    */   }
/* 186:    */   
/* 187:    */   private void setOption0(Object option, Object value)
/* 188:    */   {
/* 189:188 */     if (PlatformDependent.javaVersion() < 7) {
/* 190:189 */       throw new UnsupportedOperationException();
/* 191:    */     }
/* 192:    */     try
/* 193:    */     {
/* 194:192 */       SET_OPTION.invoke(this.javaChannel, new Object[] { option, value });
/* 195:    */     }
/* 196:    */     catch (Exception e)
/* 197:    */     {
/* 198:194 */       throw new ChannelException(e);
/* 199:    */     }
/* 200:    */   }
/* 201:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.socket.nio.NioDatagramChannelConfig
 * JD-Core Version:    0.7.0.1
 */