/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBufAllocator;
/*   4:    */ import io.netty.util.UniqueName;
/*   5:    */ import io.netty.util.internal.PlatformDependent;
/*   6:    */ import java.net.InetAddress;
/*   7:    */ import java.net.NetworkInterface;
/*   8:    */ import java.util.concurrent.ConcurrentMap;
/*   9:    */ 
/*  10:    */ public class ChannelOption<T>
/*  11:    */   extends UniqueName
/*  12:    */ {
/*  13: 37 */   private static final ConcurrentMap<String, Boolean> names = ;
/*  14: 39 */   public static final ChannelOption<ByteBufAllocator> ALLOCATOR = valueOf("ALLOCATOR");
/*  15: 40 */   public static final ChannelOption<RecvByteBufAllocator> RCVBUF_ALLOCATOR = valueOf("RCVBUF_ALLOCATOR");
/*  16: 41 */   public static final ChannelOption<MessageSizeEstimator> MESSAGE_SIZE_ESTIMATOR = valueOf("MESSAGE_SIZE_ESTIMATOR");
/*  17: 43 */   public static final ChannelOption<Integer> CONNECT_TIMEOUT_MILLIS = valueOf("CONNECT_TIMEOUT_MILLIS");
/*  18: 44 */   public static final ChannelOption<Integer> MAX_MESSAGES_PER_READ = valueOf("MAX_MESSAGES_PER_READ");
/*  19: 45 */   public static final ChannelOption<Integer> WRITE_SPIN_COUNT = valueOf("WRITE_SPIN_COUNT");
/*  20: 46 */   public static final ChannelOption<Integer> WRITE_BUFFER_HIGH_WATER_MARK = valueOf("WRITE_BUFFER_HIGH_WATER_MARK");
/*  21: 47 */   public static final ChannelOption<Integer> WRITE_BUFFER_LOW_WATER_MARK = valueOf("WRITE_BUFFER_LOW_WATER_MARK");
/*  22: 49 */   public static final ChannelOption<Boolean> ALLOW_HALF_CLOSURE = valueOf("ALLOW_HALF_CLOSURE");
/*  23: 50 */   public static final ChannelOption<Boolean> AUTO_READ = valueOf("AUTO_READ");
/*  24:    */   @Deprecated
/*  25: 59 */   public static final ChannelOption<Boolean> AUTO_CLOSE = valueOf("AUTO_CLOSE");
/*  26: 61 */   public static final ChannelOption<Boolean> SO_BROADCAST = valueOf("SO_BROADCAST");
/*  27: 62 */   public static final ChannelOption<Boolean> SO_KEEPALIVE = valueOf("SO_KEEPALIVE");
/*  28: 63 */   public static final ChannelOption<Integer> SO_SNDBUF = valueOf("SO_SNDBUF");
/*  29: 64 */   public static final ChannelOption<Integer> SO_RCVBUF = valueOf("SO_RCVBUF");
/*  30: 65 */   public static final ChannelOption<Boolean> SO_REUSEADDR = valueOf("SO_REUSEADDR");
/*  31: 66 */   public static final ChannelOption<Integer> SO_LINGER = valueOf("SO_LINGER");
/*  32: 67 */   public static final ChannelOption<Integer> SO_BACKLOG = valueOf("SO_BACKLOG");
/*  33: 68 */   public static final ChannelOption<Integer> SO_TIMEOUT = valueOf("SO_TIMEOUT");
/*  34: 70 */   public static final ChannelOption<Integer> IP_TOS = valueOf("IP_TOS");
/*  35: 71 */   public static final ChannelOption<InetAddress> IP_MULTICAST_ADDR = valueOf("IP_MULTICAST_ADDR");
/*  36: 72 */   public static final ChannelOption<NetworkInterface> IP_MULTICAST_IF = valueOf("IP_MULTICAST_IF");
/*  37: 73 */   public static final ChannelOption<Integer> IP_MULTICAST_TTL = valueOf("IP_MULTICAST_TTL");
/*  38: 74 */   public static final ChannelOption<Boolean> IP_MULTICAST_LOOP_DISABLED = valueOf("IP_MULTICAST_LOOP_DISABLED");
/*  39: 76 */   public static final ChannelOption<Boolean> TCP_NODELAY = valueOf("TCP_NODELAY");
/*  40:    */   @Deprecated
/*  41: 79 */   public static final ChannelOption<Long> AIO_READ_TIMEOUT = valueOf("AIO_READ_TIMEOUT");
/*  42:    */   @Deprecated
/*  43: 81 */   public static final ChannelOption<Long> AIO_WRITE_TIMEOUT = valueOf("AIO_WRITE_TIMEOUT");
/*  44:    */   @Deprecated
/*  45: 84 */   public static final ChannelOption<Boolean> DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION = valueOf("DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION");
/*  46:    */   
/*  47:    */   public static <T> ChannelOption<T> valueOf(String name)
/*  48:    */   {
/*  49: 91 */     return new ChannelOption(name);
/*  50:    */   }
/*  51:    */   
/*  52:    */   @Deprecated
/*  53:    */   protected ChannelOption(String name)
/*  54:    */   {
/*  55: 99 */     super(names, name, new Object[0]);
/*  56:    */   }
/*  57:    */   
/*  58:    */   public void validate(T value)
/*  59:    */   {
/*  60:107 */     if (value == null) {
/*  61:108 */       throw new NullPointerException("value");
/*  62:    */     }
/*  63:    */   }
/*  64:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.ChannelOption
 * JD-Core Version:    0.7.0.1
 */