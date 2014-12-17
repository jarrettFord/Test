/*   1:    */ package io.netty.handler.codec.socks;
/*   2:    */ 
/*   3:    */ import io.netty.util.internal.StringUtil;
/*   4:    */ 
/*   5:    */ final class SocksCommonUtils
/*   6:    */ {
/*   7: 21 */   public static final SocksRequest UNKNOWN_SOCKS_REQUEST = new UnknownSocksRequest();
/*   8: 22 */   public static final SocksResponse UNKNOWN_SOCKS_RESPONSE = new UnknownSocksResponse();
/*   9:    */   private static final int SECOND_ADDRESS_OCTET_SHIFT = 16;
/*  10:    */   private static final int FIRST_ADDRESS_OCTET_SHIFT = 24;
/*  11:    */   private static final int THIRD_ADDRESS_OCTET_SHIFT = 8;
/*  12:    */   private static final int XOR_DEFAULT_VALUE = 255;
/*  13:    */   
/*  14:    */   public static String intToIp(int i)
/*  15:    */   {
/*  16: 37 */     return String.valueOf(i >> 24 & 0xFF) + '.' + (i >> 16 & 0xFF) + '.' + (i >> 8 & 0xFF) + '.' + (i & 0xFF);
/*  17:    */   }
/*  18:    */   
/*  19: 43 */   private static final char[] ipv6conseqZeroFiller = { ':', ':' };
/*  20:    */   private static final char ipv6hextetSeparator = ':';
/*  21:    */   
/*  22:    */   public static String ipv6toCompressedForm(byte[] src)
/*  23:    */   {
/*  24: 51 */     assert (src.length == 16);
/*  25:    */     
/*  26:    */ 
/*  27: 54 */     int cmprHextet = -1;
/*  28:    */     
/*  29: 56 */     int cmprSize = 0;
/*  30: 57 */     for (int hextet = 0; hextet < 8;)
/*  31:    */     {
/*  32: 58 */       int curByte = hextet * 2;
/*  33: 59 */       int size = 0;
/*  34: 61 */       while ((curByte < src.length) && (src[curByte] == 0) && (src[(curByte + 1)] == 0))
/*  35:    */       {
/*  36: 62 */         curByte += 2;
/*  37: 63 */         size++;
/*  38:    */       }
/*  39: 65 */       if (size > cmprSize)
/*  40:    */       {
/*  41: 66 */         cmprHextet = hextet;
/*  42: 67 */         cmprSize = size;
/*  43:    */       }
/*  44: 69 */       hextet = curByte / 2 + 1;
/*  45:    */     }
/*  46: 71 */     if ((cmprHextet == -1) || (cmprSize < 2)) {
/*  47: 73 */       return ipv6toStr(src);
/*  48:    */     }
/*  49: 75 */     StringBuilder sb = new StringBuilder(39);
/*  50: 76 */     ipv6toStr(sb, src, 0, cmprHextet);
/*  51: 77 */     sb.append(ipv6conseqZeroFiller);
/*  52: 78 */     ipv6toStr(sb, src, cmprHextet + cmprSize, 8);
/*  53: 79 */     return sb.toString();
/*  54:    */   }
/*  55:    */   
/*  56:    */   public static String ipv6toStr(byte[] src)
/*  57:    */   {
/*  58: 86 */     assert (src.length == 16);
/*  59: 87 */     StringBuilder sb = new StringBuilder(39);
/*  60: 88 */     ipv6toStr(sb, src, 0, 8);
/*  61: 89 */     return sb.toString();
/*  62:    */   }
/*  63:    */   
/*  64:    */   private static void ipv6toStr(StringBuilder sb, byte[] src, int fromHextet, int toHextet)
/*  65:    */   {
/*  66:    */     
/*  67: 95 */     for (int i = fromHextet; i < toHextet; i++)
/*  68:    */     {
/*  69: 96 */       appendHextet(sb, src, i);
/*  70: 97 */       sb.append(':');
/*  71:    */     }
/*  72:100 */     appendHextet(sb, src, i);
/*  73:    */   }
/*  74:    */   
/*  75:    */   private static void appendHextet(StringBuilder sb, byte[] src, int i)
/*  76:    */   {
/*  77:104 */     StringUtil.toHexString(sb, src, i << 1, 2);
/*  78:    */   }
/*  79:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.socks.SocksCommonUtils
 * JD-Core Version:    0.7.0.1
 */