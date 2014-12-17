/*   1:    */ package io.netty.handler.codec;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.ChannelHandlerContext;
/*   5:    */ import java.util.List;
/*   6:    */ 
/*   7:    */ public class LineBasedFrameDecoder
/*   8:    */   extends ByteToMessageDecoder
/*   9:    */ {
/*  10:    */   private final int maxLength;
/*  11:    */   private final boolean failFast;
/*  12:    */   private final boolean stripDelimiter;
/*  13:    */   private boolean discarding;
/*  14:    */   private int discardedBytes;
/*  15:    */   
/*  16:    */   public LineBasedFrameDecoder(int maxLength)
/*  17:    */   {
/*  18: 48 */     this(maxLength, true, false);
/*  19:    */   }
/*  20:    */   
/*  21:    */   public LineBasedFrameDecoder(int maxLength, boolean stripDelimiter, boolean failFast)
/*  22:    */   {
/*  23: 67 */     this.maxLength = maxLength;
/*  24: 68 */     this.failFast = failFast;
/*  25: 69 */     this.stripDelimiter = stripDelimiter;
/*  26:    */   }
/*  27:    */   
/*  28:    */   protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/*  29:    */     throws Exception
/*  30:    */   {
/*  31: 74 */     Object decoded = decode(ctx, in);
/*  32: 75 */     if (decoded != null) {
/*  33: 76 */       out.add(decoded);
/*  34:    */     }
/*  35:    */   }
/*  36:    */   
/*  37:    */   protected Object decode(ChannelHandlerContext ctx, ByteBuf buffer)
/*  38:    */     throws Exception
/*  39:    */   {
/*  40: 89 */     int eol = findEndOfLine(buffer);
/*  41: 90 */     if (!this.discarding)
/*  42:    */     {
/*  43: 91 */       if (eol >= 0)
/*  44:    */       {
/*  45: 93 */         int length = eol - buffer.readerIndex();
/*  46: 94 */         int delimLength = buffer.getByte(eol) == 13 ? 2 : 1;
/*  47: 96 */         if (length > this.maxLength)
/*  48:    */         {
/*  49: 97 */           buffer.readerIndex(eol + delimLength);
/*  50: 98 */           fail(ctx, length);
/*  51: 99 */           return null;
/*  52:    */         }
/*  53:    */         ByteBuf frame;
/*  54:102 */         if (this.stripDelimiter)
/*  55:    */         {
/*  56:103 */           ByteBuf frame = buffer.readSlice(length);
/*  57:104 */           buffer.skipBytes(delimLength);
/*  58:    */         }
/*  59:    */         else
/*  60:    */         {
/*  61:106 */           frame = buffer.readSlice(length + delimLength);
/*  62:    */         }
/*  63:109 */         return frame.retain();
/*  64:    */       }
/*  65:111 */       int length = buffer.readableBytes();
/*  66:112 */       if (length > this.maxLength)
/*  67:    */       {
/*  68:113 */         this.discardedBytes = length;
/*  69:114 */         buffer.readerIndex(buffer.writerIndex());
/*  70:115 */         this.discarding = true;
/*  71:116 */         if (this.failFast) {
/*  72:117 */           fail(ctx, "over " + this.discardedBytes);
/*  73:    */         }
/*  74:    */       }
/*  75:120 */       return null;
/*  76:    */     }
/*  77:123 */     if (eol >= 0)
/*  78:    */     {
/*  79:124 */       int length = this.discardedBytes + eol - buffer.readerIndex();
/*  80:125 */       int delimLength = buffer.getByte(eol) == 13 ? 2 : 1;
/*  81:126 */       buffer.readerIndex(eol + delimLength);
/*  82:127 */       this.discardedBytes = 0;
/*  83:128 */       this.discarding = false;
/*  84:129 */       if (!this.failFast) {
/*  85:130 */         fail(ctx, length);
/*  86:    */       }
/*  87:    */     }
/*  88:    */     else
/*  89:    */     {
/*  90:133 */       this.discardedBytes = buffer.readableBytes();
/*  91:134 */       buffer.readerIndex(buffer.writerIndex());
/*  92:    */     }
/*  93:136 */     return null;
/*  94:    */   }
/*  95:    */   
/*  96:    */   private void fail(ChannelHandlerContext ctx, int length)
/*  97:    */   {
/*  98:141 */     fail(ctx, String.valueOf(length));
/*  99:    */   }
/* 100:    */   
/* 101:    */   private void fail(ChannelHandlerContext ctx, String length)
/* 102:    */   {
/* 103:145 */     ctx.fireExceptionCaught(new TooLongFrameException("frame length (" + length + ") exceeds the allowed maximum (" + this.maxLength + ')'));
/* 104:    */   }
/* 105:    */   
/* 106:    */   private static int findEndOfLine(ByteBuf buffer)
/* 107:    */   {
/* 108:155 */     int n = buffer.writerIndex();
/* 109:156 */     for (int i = buffer.readerIndex(); i < n; i++)
/* 110:    */     {
/* 111:157 */       byte b = buffer.getByte(i);
/* 112:158 */       if (b == 10) {
/* 113:159 */         return i;
/* 114:    */       }
/* 115:160 */       if ((b == 13) && (i < n - 1) && (buffer.getByte(i + 1) == 10)) {
/* 116:161 */         return i;
/* 117:    */       }
/* 118:    */     }
/* 119:164 */     return -1;
/* 120:    */   }
/* 121:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.LineBasedFrameDecoder
 * JD-Core Version:    0.7.0.1
 */