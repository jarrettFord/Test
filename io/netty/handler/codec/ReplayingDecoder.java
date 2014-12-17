/*   1:    */ package io.netty.handler.codec;
/*   2:    */ 
/*   3:    */ import io.netty.buffer.ByteBuf;
/*   4:    */ import io.netty.channel.ChannelHandlerContext;
/*   5:    */ import io.netty.util.Signal;
/*   6:    */ import io.netty.util.internal.RecyclableArrayList;
/*   7:    */ import io.netty.util.internal.StringUtil;
/*   8:    */ import java.util.List;
/*   9:    */ 
/*  10:    */ public abstract class ReplayingDecoder<S>
/*  11:    */   extends ByteToMessageDecoder
/*  12:    */ {
/*  13:270 */   static final Signal REPLAY = Signal.valueOf(ReplayingDecoder.class.getName() + ".REPLAY");
/*  14:272 */   private final ReplayingDecoderBuffer replayable = new ReplayingDecoderBuffer();
/*  15:    */   private S state;
/*  16:274 */   private int checkpoint = -1;
/*  17:    */   
/*  18:    */   protected ReplayingDecoder()
/*  19:    */   {
/*  20:280 */     this(null);
/*  21:    */   }
/*  22:    */   
/*  23:    */   protected ReplayingDecoder(S initialState)
/*  24:    */   {
/*  25:287 */     this.state = initialState;
/*  26:    */   }
/*  27:    */   
/*  28:    */   protected void checkpoint()
/*  29:    */   {
/*  30:294 */     this.checkpoint = internalBuffer().readerIndex();
/*  31:    */   }
/*  32:    */   
/*  33:    */   protected void checkpoint(S state)
/*  34:    */   {
/*  35:302 */     checkpoint();
/*  36:303 */     state(state);
/*  37:    */   }
/*  38:    */   
/*  39:    */   protected S state()
/*  40:    */   {
/*  41:311 */     return this.state;
/*  42:    */   }
/*  43:    */   
/*  44:    */   protected S state(S newState)
/*  45:    */   {
/*  46:319 */     S oldState = this.state;
/*  47:320 */     this.state = newState;
/*  48:321 */     return oldState;
/*  49:    */   }
/*  50:    */   
/*  51:    */   public void channelInactive(ChannelHandlerContext ctx)
/*  52:    */     throws Exception
/*  53:    */   {
/*  54:326 */     RecyclableArrayList out = RecyclableArrayList.newInstance();
/*  55:    */     try
/*  56:    */     {
/*  57:328 */       this.replayable.terminate();
/*  58:329 */       callDecode(ctx, internalBuffer(), out);
/*  59:330 */       decodeLast(ctx, this.replayable, out);
/*  60:    */     }
/*  61:    */     catch (Signal replay)
/*  62:    */     {
/*  63:    */       int size;
/*  64:    */       int i;
/*  65:333 */       replay.expect(REPLAY);
/*  66:    */     }
/*  67:    */     catch (DecoderException e)
/*  68:    */     {
/*  69:    */       int size;
/*  70:    */       int i;
/*  71:335 */       throw e;
/*  72:    */     }
/*  73:    */     catch (Exception e)
/*  74:    */     {
/*  75:337 */       throw new DecoderException(e);
/*  76:    */     }
/*  77:    */     finally
/*  78:    */     {
/*  79:339 */       if (this.cumulation != null)
/*  80:    */       {
/*  81:340 */         this.cumulation.release();
/*  82:341 */         this.cumulation = null;
/*  83:    */       }
/*  84:344 */       int size = out.size();
/*  85:345 */       for (int i = 0; i < size; i++) {
/*  86:346 */         ctx.fireChannelRead(out.get(i));
/*  87:    */       }
/*  88:348 */       ctx.fireChannelInactive();
/*  89:349 */       out.recycle();
/*  90:    */     }
/*  91:    */   }
/*  92:    */   
/*  93:    */   protected void callDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
/*  94:    */   {
/*  95:355 */     this.replayable.setCumulation(in);
/*  96:    */     try
/*  97:    */     {
/*  98:357 */       while (in.isReadable())
/*  99:    */       {
/* 100:358 */         int oldReaderIndex = this.checkpoint = in.readerIndex();
/* 101:359 */         int outSize = out.size();
/* 102:360 */         S oldState = this.state;
/* 103:361 */         int oldInputLength = in.readableBytes();
/* 104:    */         try
/* 105:    */         {
/* 106:363 */           decode(ctx, this.replayable, out);
/* 107:369 */           if (ctx.isRemoved()) {
/* 108:    */             break;
/* 109:    */           }
/* 110:373 */           if (outSize == out.size())
/* 111:    */           {
/* 112:374 */             if ((oldInputLength == in.readableBytes()) && (oldState == this.state)) {
/* 113:375 */               throw new DecoderException(StringUtil.simpleClassName(getClass()) + ".decode() must consume the inbound " + "data or change its state if it did not decode anything.");
/* 114:    */             }
/* 115:381 */             continue;
/* 116:    */           }
/* 117:    */         }
/* 118:    */         catch (Signal replay)
/* 119:    */         {
/* 120:385 */           replay.expect(REPLAY);
/* 121:391 */           if (!ctx.isRemoved()) {
/* 122:    */             break label163;
/* 123:    */           }
/* 124:    */         }
/* 125:392 */         break;
/* 126:    */         label163:
/* 127:396 */         int checkpoint = this.checkpoint;
/* 128:397 */         if (checkpoint >= 0) {
/* 129:398 */           in.readerIndex(checkpoint);
/* 130:    */         }
/* 131:403 */         break;
/* 132:406 */         if ((oldReaderIndex == in.readerIndex()) && (oldState == this.state)) {
/* 133:407 */           throw new DecoderException(StringUtil.simpleClassName(getClass()) + ".decode() method must consume the inbound data " + "or change its state if it decoded something.");
/* 134:    */         }
/* 135:411 */         if (isSingleDecode()) {
/* 136:    */           break;
/* 137:    */         }
/* 138:    */       }
/* 139:    */     }
/* 140:    */     catch (DecoderException e)
/* 141:    */     {
/* 142:416 */       throw e;
/* 143:    */     }
/* 144:    */     catch (Throwable cause)
/* 145:    */     {
/* 146:418 */       throw new DecoderException(cause);
/* 147:    */     }
/* 148:    */   }
/* 149:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.ReplayingDecoder
 * JD-Core Version:    0.7.0.1
 */