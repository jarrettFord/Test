/*   1:    */ package io.netty.handler.codec;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.buffer.ByteBufAllocator;
/*   5:    */ import io.netty.buffer.Unpooled;
/*   6:    */ import io.netty.channel.Channel;
/*   7:    */ import io.netty.channel.ChannelConfig;
/*   8:    */ import io.netty.channel.ChannelHandlerContext;
/*   9:    */ import io.netty.channel.ChannelInboundHandlerAdapter;
/*  10:    */ import io.netty.util.internal.RecyclableArrayList;
/*  11:    */ import io.netty.util.internal.StringUtil;
/*  12:    */ import java.util.List;
/*  13:    */ 
/*  14:    */ public abstract class ByteToMessageDecoder
/*  15:    */   extends ChannelInboundHandlerAdapter
/*  16:    */ {
/*  17:    */   ByteBuf cumulation;
/*  18:    */   private boolean singleDecode;
/*  19:    */   private boolean decodeWasNull;
/*  20:    */   private boolean first;
/*  21:    */   
/*  22:    */   protected ByteToMessageDecoder()
/*  23:    */   {
/*  24: 55 */     if (isSharable()) {
/*  25: 56 */       throw new IllegalStateException("@Sharable annotation is not allowed");
/*  26:    */     }
/*  27:    */   }
/*  28:    */   
/*  29:    */   public void setSingleDecode(boolean singleDecode)
/*  30:    */   {
/*  31: 67 */     this.singleDecode = singleDecode;
/*  32:    */   }
/*  33:    */   
/*  34:    */   public boolean isSingleDecode()
/*  35:    */   {
/*  36: 77 */     return this.singleDecode;
/*  37:    */   }
/*  38:    */   
/*  39:    */   protected int actualReadableBytes()
/*  40:    */   {
/*  41: 87 */     return internalBuffer().readableBytes();
/*  42:    */   }
/*  43:    */   
/*  44:    */   protected ByteBuf internalBuffer()
/*  45:    */   {
/*  46: 96 */     if (this.cumulation != null) {
/*  47: 97 */       return this.cumulation;
/*  48:    */     }
/*  49: 99 */     return Unpooled.EMPTY_BUFFER;
/*  50:    */   }
/*  51:    */   
/*  52:    */   public final void handlerRemoved(ChannelHandlerContext ctx)
/*  53:    */     throws Exception
/*  54:    */   {
/*  55:105 */     ByteBuf buf = internalBuffer();
/*  56:106 */     int readable = buf.readableBytes();
/*  57:107 */     if (buf.isReadable())
/*  58:    */     {
/*  59:108 */       ByteBuf bytes = buf.readBytes(readable);
/*  60:109 */       buf.release();
/*  61:110 */       ctx.fireChannelRead(bytes);
/*  62:    */     }
/*  63:    */     else
/*  64:    */     {
/*  65:112 */       buf.release();
/*  66:    */     }
/*  67:114 */     this.cumulation = null;
/*  68:115 */     ctx.fireChannelReadComplete();
/*  69:116 */     handlerRemoved0(ctx);
/*  70:    */   }
/*  71:    */   
/*  72:    */   protected void handlerRemoved0(ChannelHandlerContext ctx)
/*  73:    */     throws Exception
/*  74:    */   {}
/*  75:    */   
/*  76:    */   public void channelRead(ChannelHandlerContext ctx, Object msg)
/*  77:    */     throws Exception
/*  78:    */   {
/*  79:127 */     if ((msg instanceof ByteBuf))
/*  80:    */     {
/*  81:128 */       RecyclableArrayList out = RecyclableArrayList.newInstance();
/*  82:    */       try
/*  83:    */       {
/*  84:130 */         ByteBuf data = (ByteBuf)msg;
/*  85:131 */         this.first = (this.cumulation == null);
/*  86:132 */         if (this.first)
/*  87:    */         {
/*  88:133 */           this.cumulation = data;
/*  89:    */         }
/*  90:    */         else
/*  91:    */         {
/*  92:135 */           if ((this.cumulation.writerIndex() > this.cumulation.maxCapacity() - data.readableBytes()) || (this.cumulation.refCnt() > 1)) {
/*  93:144 */             expandCumulation(ctx, data.readableBytes());
/*  94:    */           }
/*  95:146 */           this.cumulation.writeBytes(data);
/*  96:147 */           data.release();
/*  97:    */         }
/*  98:149 */         callDecode(ctx, this.cumulation, out);
/*  99:    */       }
/* 100:    */       catch (DecoderException e)
/* 101:    */       {
/* 102:    */         int size;
/* 103:    */         int i;
/* 104:151 */         throw e;
/* 105:    */       }
/* 106:    */       catch (Throwable t)
/* 107:    */       {
/* 108:153 */         throw new DecoderException(t);
/* 109:    */       }
/* 110:    */       finally
/* 111:    */       {
/* 112:155 */         if ((this.cumulation != null) && (!this.cumulation.isReadable()))
/* 113:    */         {
/* 114:156 */           this.cumulation.release();
/* 115:157 */           this.cumulation = null;
/* 116:    */         }
/* 117:159 */         int size = out.size();
/* 118:160 */         this.decodeWasNull = (size == 0);
/* 119:162 */         for (int i = 0; i < size; i++) {
/* 120:163 */           ctx.fireChannelRead(out.get(i));
/* 121:    */         }
/* 122:165 */         out.recycle();
/* 123:    */       }
/* 124:    */     }
/* 125:    */     else
/* 126:    */     {
/* 127:168 */       ctx.fireChannelRead(msg);
/* 128:    */     }
/* 129:    */   }
/* 130:    */   
/* 131:    */   private void expandCumulation(ChannelHandlerContext ctx, int readable)
/* 132:    */   {
/* 133:173 */     ByteBuf oldCumulation = this.cumulation;
/* 134:174 */     this.cumulation = ctx.alloc().buffer(oldCumulation.readableBytes() + readable);
/* 135:175 */     this.cumulation.writeBytes(oldCumulation);
/* 136:176 */     oldCumulation.release();
/* 137:    */   }
/* 138:    */   
/* 139:    */   public void channelReadComplete(ChannelHandlerContext ctx)
/* 140:    */     throws Exception
/* 141:    */   {
/* 142:181 */     if ((this.cumulation != null) && (!this.first) && (this.cumulation.refCnt() == 1)) {
/* 143:189 */       this.cumulation.discardSomeReadBytes();
/* 144:    */     }
/* 145:191 */     if (this.decodeWasNull)
/* 146:    */     {
/* 147:192 */       this.decodeWasNull = false;
/* 148:193 */       if (!ctx.channel().config().isAutoRead()) {
/* 149:194 */         ctx.read();
/* 150:    */       }
/* 151:    */     }
/* 152:197 */     ctx.fireChannelReadComplete();
/* 153:    */   }
/* 154:    */   
/* 155:    */   public void channelInactive(ChannelHandlerContext ctx)
/* 156:    */     throws Exception
/* 157:    */   {
/* 158:202 */     RecyclableArrayList out = RecyclableArrayList.newInstance();
/* 159:    */     try
/* 160:    */     {
/* 161:204 */       if (this.cumulation != null)
/* 162:    */       {
/* 163:205 */         callDecode(ctx, this.cumulation, out);
/* 164:206 */         decodeLast(ctx, this.cumulation, out);
/* 165:    */       }
/* 166:    */       else
/* 167:    */       {
/* 168:208 */         decodeLast(ctx, Unpooled.EMPTY_BUFFER, out);
/* 169:    */       }
/* 170:    */     }
/* 171:    */     catch (DecoderException e)
/* 172:    */     {
/* 173:    */       int size;
/* 174:    */       int i;
/* 175:211 */       throw e;
/* 176:    */     }
/* 177:    */     catch (Exception e)
/* 178:    */     {
/* 179:213 */       throw new DecoderException(e);
/* 180:    */     }
/* 181:    */     finally
/* 182:    */     {
/* 183:215 */       if (this.cumulation != null)
/* 184:    */       {
/* 185:216 */         this.cumulation.release();
/* 186:217 */         this.cumulation = null;
/* 187:    */       }
/* 188:219 */       int size = out.size();
/* 189:220 */       for (int i = 0; i < size; i++) {
/* 190:221 */         ctx.fireChannelRead(out.get(i));
/* 191:    */       }
/* 192:223 */       ctx.fireChannelInactive();
/* 193:224 */       out.recycle();
/* 194:    */     }
/* 195:    */   }
/* 196:    */   
/* 197:    */   protected void callDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/* 198:    */   {
/* 199:    */     try
/* 200:    */     {
/* 201:238 */       while (in.isReadable())
/* 202:    */       {
/* 203:239 */         int outSize = out.size();
/* 204:240 */         int oldInputLength = in.readableBytes();
/* 205:241 */         decode(ctx, in, out);
/* 206:247 */         if (ctx.isRemoved()) {
/* 207:    */           break;
/* 208:    */         }
/* 209:251 */         if (outSize == out.size())
/* 210:    */         {
/* 211:252 */           if (oldInputLength == in.readableBytes()) {
/* 212:    */             break;
/* 213:    */           }
/* 214:    */         }
/* 215:    */         else
/* 216:    */         {
/* 217:259 */           if (oldInputLength == in.readableBytes()) {
/* 218:260 */             throw new DecoderException(StringUtil.simpleClassName(getClass()) + ".decode() did not read anything but decoded a message.");
/* 219:    */           }
/* 220:265 */           if (isSingleDecode()) {
/* 221:    */             break;
/* 222:    */           }
/* 223:    */         }
/* 224:    */       }
/* 225:    */     }
/* 226:    */     catch (DecoderException e)
/* 227:    */     {
/* 228:270 */       throw e;
/* 229:    */     }
/* 230:    */     catch (Throwable cause)
/* 231:    */     {
/* 232:272 */       throw new DecoderException(cause);
/* 233:    */     }
/* 234:    */   }
/* 235:    */   
/* 236:    */   protected abstract void decode(ChannelHandlerContext paramChannelHandlerContext, ByteBuf paramByteBuf, List<Object> paramList)
/* 237:    */     throws Exception;
/* 238:    */   
/* 239:    */   protected void decodeLast(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/* 240:    */     throws Exception
/* 241:    */   {
/* 242:297 */     decode(ctx, in, out);
/* 243:    */   }
/* 244:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.ByteToMessageDecoder
 * JD-Core Version:    0.7.0.1
 */