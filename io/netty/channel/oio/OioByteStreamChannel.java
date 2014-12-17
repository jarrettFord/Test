/*   1:    */ package io.netty.channel.oio;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.Channel;
/*   5:    */ import io.netty.channel.FileRegion;
/*   6:    */ import java.io.IOException;
/*   7:    */ import java.io.InputStream;
/*   8:    */ import java.io.OutputStream;
/*   9:    */ import java.nio.channels.Channels;
/*  10:    */ import java.nio.channels.ClosedChannelException;
/*  11:    */ import java.nio.channels.NotYetConnectedException;
/*  12:    */ import java.nio.channels.WritableByteChannel;
/*  13:    */ 
/*  14:    */ public abstract class OioByteStreamChannel
/*  15:    */   extends AbstractOioByteChannel
/*  16:    */ {
/*  17: 35 */   private static final InputStream CLOSED_IN = new InputStream()
/*  18:    */   {
/*  19:    */     public int read()
/*  20:    */     {
/*  21: 38 */       return -1;
/*  22:    */     }
/*  23:    */   };
/*  24: 42 */   private static final OutputStream CLOSED_OUT = new OutputStream()
/*  25:    */   {
/*  26:    */     public void write(int b)
/*  27:    */       throws IOException
/*  28:    */     {
/*  29: 45 */       throw new ClosedChannelException();
/*  30:    */     }
/*  31:    */   };
/*  32:    */   private InputStream is;
/*  33:    */   private OutputStream os;
/*  34:    */   private WritableByteChannel outChannel;
/*  35:    */   
/*  36:    */   protected OioByteStreamChannel(Channel parent)
/*  37:    */   {
/*  38: 60 */     super(parent);
/*  39:    */   }
/*  40:    */   
/*  41:    */   protected final void activate(InputStream is, OutputStream os)
/*  42:    */   {
/*  43: 67 */     if (this.is != null) {
/*  44: 68 */       throw new IllegalStateException("input was set already");
/*  45:    */     }
/*  46: 70 */     if (this.os != null) {
/*  47: 71 */       throw new IllegalStateException("output was set already");
/*  48:    */     }
/*  49: 73 */     if (is == null) {
/*  50: 74 */       throw new NullPointerException("is");
/*  51:    */     }
/*  52: 76 */     if (os == null) {
/*  53: 77 */       throw new NullPointerException("os");
/*  54:    */     }
/*  55: 79 */     this.is = is;
/*  56: 80 */     this.os = os;
/*  57:    */   }
/*  58:    */   
/*  59:    */   public boolean isActive()
/*  60:    */   {
/*  61: 85 */     InputStream is = this.is;
/*  62: 86 */     if ((is == null) || (is == CLOSED_IN)) {
/*  63: 87 */       return false;
/*  64:    */     }
/*  65: 90 */     OutputStream os = this.os;
/*  66: 91 */     if ((os == null) || (os == CLOSED_OUT)) {
/*  67: 92 */       return false;
/*  68:    */     }
/*  69: 95 */     return true;
/*  70:    */   }
/*  71:    */   
/*  72:    */   protected int available()
/*  73:    */   {
/*  74:    */     try
/*  75:    */     {
/*  76:101 */       return this.is.available();
/*  77:    */     }
/*  78:    */     catch (IOException e) {}
/*  79:103 */     return 0;
/*  80:    */   }
/*  81:    */   
/*  82:    */   protected int doReadBytes(ByteBuf buf)
/*  83:    */     throws Exception
/*  84:    */   {
/*  85:109 */     int length = Math.max(1, Math.min(available(), buf.maxWritableBytes()));
/*  86:110 */     return buf.writeBytes(this.is, length);
/*  87:    */   }
/*  88:    */   
/*  89:    */   protected void doWriteBytes(ByteBuf buf)
/*  90:    */     throws Exception
/*  91:    */   {
/*  92:115 */     OutputStream os = this.os;
/*  93:116 */     if (os == null) {
/*  94:117 */       throw new NotYetConnectedException();
/*  95:    */     }
/*  96:119 */     buf.readBytes(os, buf.readableBytes());
/*  97:    */   }
/*  98:    */   
/*  99:    */   protected void doWriteFileRegion(FileRegion region)
/* 100:    */     throws Exception
/* 101:    */   {
/* 102:124 */     OutputStream os = this.os;
/* 103:125 */     if (os == null) {
/* 104:126 */       throw new NotYetConnectedException();
/* 105:    */     }
/* 106:128 */     if (this.outChannel == null) {
/* 107:129 */       this.outChannel = Channels.newChannel(os);
/* 108:    */     }
/* 109:132 */     long written = 0L;
/* 110:    */     for (;;)
/* 111:    */     {
/* 112:134 */       long localWritten = region.transferTo(this.outChannel, written);
/* 113:135 */       if (localWritten == -1L)
/* 114:    */       {
/* 115:136 */         checkEOF(region);
/* 116:137 */         return;
/* 117:    */       }
/* 118:139 */       written += localWritten;
/* 119:141 */       if (written >= region.count()) {
/* 120:142 */         return;
/* 121:    */       }
/* 122:    */     }
/* 123:    */   }
/* 124:    */   
/* 125:    */   protected void doClose()
/* 126:    */     throws Exception
/* 127:    */   {
/* 128:149 */     InputStream is = this.is;
/* 129:150 */     OutputStream os = this.os;
/* 130:151 */     this.is = CLOSED_IN;
/* 131:152 */     this.os = CLOSED_OUT;
/* 132:    */     try
/* 133:    */     {
/* 134:155 */       if (is != null) {
/* 135:156 */         is.close();
/* 136:    */       }
/* 137:    */     }
/* 138:    */     finally
/* 139:    */     {
/* 140:159 */       if (os != null) {
/* 141:160 */         os.close();
/* 142:    */       }
/* 143:    */     }
/* 144:    */   }
/* 145:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.oio.OioByteStreamChannel
 * JD-Core Version:    0.7.0.1
 */