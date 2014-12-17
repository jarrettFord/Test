/*   1:    */ package io.netty.handler.codec.compression;
/*   2:    */ 
/*   3:    */ import com.jcraft.jzlib.Inflater;
/*   4:    */ import com.jcraft.jzlib.JZlib;
/*   5:    */ import io.netty.buffer.ByteBuf;
/*   6:    */ import io.netty.buffer.ByteBufAllocator;
/*   7:    */ import io.netty.channel.ChannelHandlerContext;
/*   8:    */ import java.util.List;
/*   9:    */ 
/*  10:    */ public class JZlibDecoder
/*  11:    */   extends ZlibDecoder
/*  12:    */ {
/*  13: 27 */   private final Inflater z = new Inflater();
/*  14:    */   private byte[] dictionary;
/*  15:    */   private volatile boolean finished;
/*  16:    */   
/*  17:    */   public JZlibDecoder()
/*  18:    */   {
/*  19: 37 */     this(ZlibWrapper.ZLIB);
/*  20:    */   }
/*  21:    */   
/*  22:    */   public JZlibDecoder(ZlibWrapper wrapper)
/*  23:    */   {
/*  24: 46 */     if (wrapper == null) {
/*  25: 47 */       throw new NullPointerException("wrapper");
/*  26:    */     }
/*  27: 50 */     int resultCode = this.z.init(ZlibUtil.convertWrapperType(wrapper));
/*  28: 51 */     if (resultCode != 0) {
/*  29: 52 */       ZlibUtil.fail(this.z, "initialization failure", resultCode);
/*  30:    */     }
/*  31:    */   }
/*  32:    */   
/*  33:    */   public JZlibDecoder(byte[] dictionary)
/*  34:    */   {
/*  35: 64 */     if (dictionary == null) {
/*  36: 65 */       throw new NullPointerException("dictionary");
/*  37:    */     }
/*  38: 67 */     this.dictionary = dictionary;
/*  39:    */     
/*  40:    */ 
/*  41: 70 */     int resultCode = this.z.inflateInit(JZlib.W_ZLIB);
/*  42: 71 */     if (resultCode != 0) {
/*  43: 72 */       ZlibUtil.fail(this.z, "initialization failure", resultCode);
/*  44:    */     }
/*  45:    */   }
/*  46:    */   
/*  47:    */   public boolean isClosed()
/*  48:    */   {
/*  49: 82 */     return this.finished;
/*  50:    */   }
/*  51:    */   
/*  52:    */   protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/*  53:    */     throws Exception
/*  54:    */   {
/*  55: 87 */     if (this.finished)
/*  56:    */     {
/*  57: 89 */       in.skipBytes(in.readableBytes());
/*  58: 90 */       return;
/*  59:    */     }
/*  60: 93 */     if (!in.isReadable()) {
/*  61: 94 */       return;
/*  62:    */     }
/*  63:    */     try
/*  64:    */     {
/*  65: 99 */       int inputLength = in.readableBytes();
/*  66:100 */       this.z.avail_in = inputLength;
/*  67:101 */       if (in.hasArray())
/*  68:    */       {
/*  69:102 */         this.z.next_in = in.array();
/*  70:103 */         this.z.next_in_index = (in.arrayOffset() + in.readerIndex());
/*  71:    */       }
/*  72:    */       else
/*  73:    */       {
/*  74:105 */         byte[] array = new byte[inputLength];
/*  75:106 */         in.getBytes(in.readerIndex(), array);
/*  76:107 */         this.z.next_in = array;
/*  77:108 */         this.z.next_in_index = 0;
/*  78:    */       }
/*  79:110 */       int oldNextInIndex = this.z.next_in_index;
/*  80:    */       
/*  81:    */ 
/*  82:113 */       int maxOutputLength = inputLength << 1;
/*  83:114 */       ByteBuf decompressed = ctx.alloc().heapBuffer(maxOutputLength);
/*  84:    */       try
/*  85:    */       {
/*  86:    */         for (;;)
/*  87:    */         {
/*  88:118 */           this.z.avail_out = maxOutputLength;
/*  89:119 */           decompressed.ensureWritable(maxOutputLength);
/*  90:120 */           this.z.next_out = decompressed.array();
/*  91:121 */           this.z.next_out_index = (decompressed.arrayOffset() + decompressed.writerIndex());
/*  92:122 */           int oldNextOutIndex = this.z.next_out_index;
/*  93:    */           
/*  94:    */ 
/*  95:125 */           int resultCode = this.z.inflate(2);
/*  96:126 */           int outputLength = this.z.next_out_index - oldNextOutIndex;
/*  97:127 */           if (outputLength > 0) {
/*  98:128 */             decompressed.writerIndex(decompressed.writerIndex() + outputLength);
/*  99:    */           }
/* 100:131 */           switch (resultCode)
/* 101:    */           {
/* 102:    */           case 2: 
/* 103:133 */             if (this.dictionary == null)
/* 104:    */             {
/* 105:134 */               ZlibUtil.fail(this.z, "decompression failure", resultCode);
/* 106:    */             }
/* 107:    */             else
/* 108:    */             {
/* 109:136 */               resultCode = this.z.inflateSetDictionary(this.dictionary, this.dictionary.length);
/* 110:137 */               if (resultCode != 0) {
/* 111:138 */                 ZlibUtil.fail(this.z, "failed to set the dictionary", resultCode);
/* 112:    */               }
/* 113:    */             }
/* 114:    */             break;
/* 115:    */           case 1: 
/* 116:143 */             this.finished = true;
/* 117:144 */             this.z.inflateEnd();
/* 118:145 */             break;
/* 119:    */           case 0: 
/* 120:    */             break;
/* 121:    */           case -5: 
/* 122:149 */             if (this.z.avail_in > 0) {
/* 123:    */               break;
/* 124:    */             }
/* 125:150 */             break;
/* 126:    */           case -4: 
/* 127:    */           case -3: 
/* 128:    */           case -2: 
/* 129:    */           case -1: 
/* 130:    */           default: 
/* 131:154 */             ZlibUtil.fail(this.z, "decompression failure", resultCode);
/* 132:    */           }
/* 133:    */         }
/* 134:    */       }
/* 135:    */       finally
/* 136:    */       {
/* 137:158 */         in.skipBytes(this.z.next_in_index - oldNextInIndex);
/* 138:159 */         if (decompressed.isReadable()) {
/* 139:160 */           out.add(decompressed);
/* 140:    */         } else {
/* 141:162 */           decompressed.release();
/* 142:    */         }
/* 143:    */       }
/* 144:    */     }
/* 145:    */     finally
/* 146:    */     {
/* 147:170 */       this.z.next_in = null;
/* 148:171 */       this.z.next_out = null;
/* 149:    */     }
/* 150:    */   }
/* 151:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.compression.JZlibDecoder
 * JD-Core Version:    0.7.0.1
 */