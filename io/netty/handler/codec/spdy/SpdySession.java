/*   1:    */ package io.netty.handler.codec.spdy;
/*   2:    */ 
/*   3:    */ import io.netty.channel.ChannelPromise;
/*   4:    */ import io.netty.util.internal.PlatformDependent;
/*   5:    */ import java.util.Comparator;
/*   6:    */ import java.util.Map;
/*   7:    */ import java.util.Queue;
/*   8:    */ import java.util.Set;
/*   9:    */ import java.util.TreeSet;
/*  10:    */ import java.util.concurrent.ConcurrentLinkedQueue;
/*  11:    */ import java.util.concurrent.atomic.AtomicInteger;
/*  12:    */ 
/*  13:    */ final class SpdySession
/*  14:    */ {
/*  15: 33 */   private final AtomicInteger activeLocalStreams = new AtomicInteger();
/*  16: 34 */   private final AtomicInteger activeRemoteStreams = new AtomicInteger();
/*  17: 35 */   private final Map<Integer, StreamState> activeStreams = PlatformDependent.newConcurrentHashMap();
/*  18:    */   private final AtomicInteger sendWindowSize;
/*  19:    */   private final AtomicInteger receiveWindowSize;
/*  20:    */   
/*  21:    */   SpdySession(int sendWindowSize, int receiveWindowSize)
/*  22:    */   {
/*  23: 41 */     this.sendWindowSize = new AtomicInteger(sendWindowSize);
/*  24: 42 */     this.receiveWindowSize = new AtomicInteger(receiveWindowSize);
/*  25:    */   }
/*  26:    */   
/*  27:    */   int numActiveStreams(boolean remote)
/*  28:    */   {
/*  29: 46 */     if (remote) {
/*  30: 47 */       return this.activeRemoteStreams.get();
/*  31:    */     }
/*  32: 49 */     return this.activeLocalStreams.get();
/*  33:    */   }
/*  34:    */   
/*  35:    */   boolean noActiveStreams()
/*  36:    */   {
/*  37: 54 */     return this.activeStreams.isEmpty();
/*  38:    */   }
/*  39:    */   
/*  40:    */   boolean isActiveStream(int streamId)
/*  41:    */   {
/*  42: 58 */     return this.activeStreams.containsKey(Integer.valueOf(streamId));
/*  43:    */   }
/*  44:    */   
/*  45:    */   Set<Integer> getActiveStreams()
/*  46:    */   {
/*  47: 63 */     TreeSet<Integer> streamIds = new TreeSet(new PriorityComparator(null));
/*  48: 64 */     streamIds.addAll(this.activeStreams.keySet());
/*  49: 65 */     return streamIds;
/*  50:    */   }
/*  51:    */   
/*  52:    */   void acceptStream(int streamId, byte priority, boolean remoteSideClosed, boolean localSideClosed, int sendWindowSize, int receiveWindowSize, boolean remote)
/*  53:    */   {
/*  54: 71 */     if ((!remoteSideClosed) || (!localSideClosed))
/*  55:    */     {
/*  56: 72 */       StreamState state = (StreamState)this.activeStreams.put(Integer.valueOf(streamId), new StreamState(priority, remoteSideClosed, localSideClosed, sendWindowSize, receiveWindowSize));
/*  57: 74 */       if (state == null) {
/*  58: 75 */         if (remote) {
/*  59: 76 */           this.activeRemoteStreams.incrementAndGet();
/*  60:    */         } else {
/*  61: 78 */           this.activeLocalStreams.incrementAndGet();
/*  62:    */         }
/*  63:    */       }
/*  64:    */     }
/*  65:    */   }
/*  66:    */   
/*  67:    */   private StreamState removeActiveStream(int streamId, boolean remote)
/*  68:    */   {
/*  69: 85 */     StreamState state = (StreamState)this.activeStreams.remove(Integer.valueOf(streamId));
/*  70: 86 */     if (state != null) {
/*  71: 87 */       if (remote) {
/*  72: 88 */         this.activeRemoteStreams.decrementAndGet();
/*  73:    */       } else {
/*  74: 90 */         this.activeLocalStreams.decrementAndGet();
/*  75:    */       }
/*  76:    */     }
/*  77: 93 */     return state;
/*  78:    */   }
/*  79:    */   
/*  80:    */   void removeStream(int streamId, Throwable cause, boolean remote)
/*  81:    */   {
/*  82: 97 */     StreamState state = removeActiveStream(streamId, remote);
/*  83: 98 */     if (state != null) {
/*  84: 99 */       state.clearPendingWrites(cause);
/*  85:    */     }
/*  86:    */   }
/*  87:    */   
/*  88:    */   boolean isRemoteSideClosed(int streamId)
/*  89:    */   {
/*  90:104 */     StreamState state = (StreamState)this.activeStreams.get(Integer.valueOf(streamId));
/*  91:105 */     return (state == null) || (state.isRemoteSideClosed());
/*  92:    */   }
/*  93:    */   
/*  94:    */   void closeRemoteSide(int streamId, boolean remote)
/*  95:    */   {
/*  96:109 */     StreamState state = (StreamState)this.activeStreams.get(Integer.valueOf(streamId));
/*  97:110 */     if (state != null)
/*  98:    */     {
/*  99:111 */       state.closeRemoteSide();
/* 100:112 */       if (state.isLocalSideClosed()) {
/* 101:113 */         removeActiveStream(streamId, remote);
/* 102:    */       }
/* 103:    */     }
/* 104:    */   }
/* 105:    */   
/* 106:    */   boolean isLocalSideClosed(int streamId)
/* 107:    */   {
/* 108:119 */     StreamState state = (StreamState)this.activeStreams.get(Integer.valueOf(streamId));
/* 109:120 */     return (state == null) || (state.isLocalSideClosed());
/* 110:    */   }
/* 111:    */   
/* 112:    */   void closeLocalSide(int streamId, boolean remote)
/* 113:    */   {
/* 114:124 */     StreamState state = (StreamState)this.activeStreams.get(Integer.valueOf(streamId));
/* 115:125 */     if (state != null)
/* 116:    */     {
/* 117:126 */       state.closeLocalSide();
/* 118:127 */       if (state.isRemoteSideClosed()) {
/* 119:128 */         removeActiveStream(streamId, remote);
/* 120:    */       }
/* 121:    */     }
/* 122:    */   }
/* 123:    */   
/* 124:    */   boolean hasReceivedReply(int streamId)
/* 125:    */   {
/* 126:138 */     StreamState state = (StreamState)this.activeStreams.get(Integer.valueOf(streamId));
/* 127:139 */     return (state != null) && (state.hasReceivedReply());
/* 128:    */   }
/* 129:    */   
/* 130:    */   void receivedReply(int streamId)
/* 131:    */   {
/* 132:143 */     StreamState state = (StreamState)this.activeStreams.get(Integer.valueOf(streamId));
/* 133:144 */     if (state != null) {
/* 134:145 */       state.receivedReply();
/* 135:    */     }
/* 136:    */   }
/* 137:    */   
/* 138:    */   int getSendWindowSize(int streamId)
/* 139:    */   {
/* 140:150 */     if (streamId == 0) {
/* 141:151 */       return this.sendWindowSize.get();
/* 142:    */     }
/* 143:154 */     StreamState state = (StreamState)this.activeStreams.get(Integer.valueOf(streamId));
/* 144:155 */     return state != null ? state.getSendWindowSize() : -1;
/* 145:    */   }
/* 146:    */   
/* 147:    */   int updateSendWindowSize(int streamId, int deltaWindowSize)
/* 148:    */   {
/* 149:159 */     if (streamId == 0) {
/* 150:160 */       return this.sendWindowSize.addAndGet(deltaWindowSize);
/* 151:    */     }
/* 152:163 */     StreamState state = (StreamState)this.activeStreams.get(Integer.valueOf(streamId));
/* 153:164 */     return state != null ? state.updateSendWindowSize(deltaWindowSize) : -1;
/* 154:    */   }
/* 155:    */   
/* 156:    */   int updateReceiveWindowSize(int streamId, int deltaWindowSize)
/* 157:    */   {
/* 158:168 */     if (streamId == 0) {
/* 159:169 */       return this.receiveWindowSize.addAndGet(deltaWindowSize);
/* 160:    */     }
/* 161:172 */     StreamState state = (StreamState)this.activeStreams.get(Integer.valueOf(streamId));
/* 162:173 */     if (deltaWindowSize > 0) {
/* 163:174 */       state.setReceiveWindowSizeLowerBound(0);
/* 164:    */     }
/* 165:176 */     return state != null ? state.updateReceiveWindowSize(deltaWindowSize) : -1;
/* 166:    */   }
/* 167:    */   
/* 168:    */   int getReceiveWindowSizeLowerBound(int streamId)
/* 169:    */   {
/* 170:180 */     if (streamId == 0) {
/* 171:181 */       return 0;
/* 172:    */     }
/* 173:184 */     StreamState state = (StreamState)this.activeStreams.get(Integer.valueOf(streamId));
/* 174:185 */     return state != null ? state.getReceiveWindowSizeLowerBound() : 0;
/* 175:    */   }
/* 176:    */   
/* 177:    */   void updateAllSendWindowSizes(int deltaWindowSize)
/* 178:    */   {
/* 179:189 */     for (StreamState state : this.activeStreams.values()) {
/* 180:190 */       state.updateSendWindowSize(deltaWindowSize);
/* 181:    */     }
/* 182:    */   }
/* 183:    */   
/* 184:    */   void updateAllReceiveWindowSizes(int deltaWindowSize)
/* 185:    */   {
/* 186:195 */     for (StreamState state : this.activeStreams.values())
/* 187:    */     {
/* 188:196 */       state.updateReceiveWindowSize(deltaWindowSize);
/* 189:197 */       if (deltaWindowSize < 0) {
/* 190:198 */         state.setReceiveWindowSizeLowerBound(deltaWindowSize);
/* 191:    */       }
/* 192:    */     }
/* 193:    */   }
/* 194:    */   
/* 195:    */   boolean putPendingWrite(int streamId, PendingWrite pendingWrite)
/* 196:    */   {
/* 197:204 */     StreamState state = (StreamState)this.activeStreams.get(Integer.valueOf(streamId));
/* 198:205 */     return (state != null) && (state.putPendingWrite(pendingWrite));
/* 199:    */   }
/* 200:    */   
/* 201:    */   PendingWrite getPendingWrite(int streamId)
/* 202:    */   {
/* 203:209 */     if (streamId == 0)
/* 204:    */     {
/* 205:210 */       for (Integer id : getActiveStreams())
/* 206:    */       {
/* 207:211 */         StreamState state = (StreamState)this.activeStreams.get(id);
/* 208:212 */         if (state.getSendWindowSize() > 0)
/* 209:    */         {
/* 210:213 */           PendingWrite pendingWrite = state.getPendingWrite();
/* 211:214 */           if (pendingWrite != null) {
/* 212:215 */             return pendingWrite;
/* 213:    */           }
/* 214:    */         }
/* 215:    */       }
/* 216:219 */       return null;
/* 217:    */     }
/* 218:222 */     StreamState state = (StreamState)this.activeStreams.get(Integer.valueOf(streamId));
/* 219:223 */     return state != null ? state.getPendingWrite() : null;
/* 220:    */   }
/* 221:    */   
/* 222:    */   PendingWrite removePendingWrite(int streamId)
/* 223:    */   {
/* 224:227 */     StreamState state = (StreamState)this.activeStreams.get(Integer.valueOf(streamId));
/* 225:228 */     return state != null ? state.removePendingWrite() : null;
/* 226:    */   }
/* 227:    */   
/* 228:    */   private static final class StreamState
/* 229:    */   {
/* 230:    */     private final byte priority;
/* 231:    */     private boolean remoteSideClosed;
/* 232:    */     private boolean localSideClosed;
/* 233:    */     private boolean receivedReply;
/* 234:    */     private final AtomicInteger sendWindowSize;
/* 235:    */     private final AtomicInteger receiveWindowSize;
/* 236:    */     private int receiveWindowSizeLowerBound;
/* 237:240 */     private final Queue<SpdySession.PendingWrite> pendingWriteQueue = new ConcurrentLinkedQueue();
/* 238:    */     
/* 239:    */     StreamState(byte priority, boolean remoteSideClosed, boolean localSideClosed, int sendWindowSize, int receiveWindowSize)
/* 240:    */     {
/* 241:245 */       this.priority = priority;
/* 242:246 */       this.remoteSideClosed = remoteSideClosed;
/* 243:247 */       this.localSideClosed = localSideClosed;
/* 244:248 */       this.sendWindowSize = new AtomicInteger(sendWindowSize);
/* 245:249 */       this.receiveWindowSize = new AtomicInteger(receiveWindowSize);
/* 246:    */     }
/* 247:    */     
/* 248:    */     byte getPriority()
/* 249:    */     {
/* 250:253 */       return this.priority;
/* 251:    */     }
/* 252:    */     
/* 253:    */     boolean isRemoteSideClosed()
/* 254:    */     {
/* 255:257 */       return this.remoteSideClosed;
/* 256:    */     }
/* 257:    */     
/* 258:    */     void closeRemoteSide()
/* 259:    */     {
/* 260:261 */       this.remoteSideClosed = true;
/* 261:    */     }
/* 262:    */     
/* 263:    */     boolean isLocalSideClosed()
/* 264:    */     {
/* 265:265 */       return this.localSideClosed;
/* 266:    */     }
/* 267:    */     
/* 268:    */     void closeLocalSide()
/* 269:    */     {
/* 270:269 */       this.localSideClosed = true;
/* 271:    */     }
/* 272:    */     
/* 273:    */     boolean hasReceivedReply()
/* 274:    */     {
/* 275:273 */       return this.receivedReply;
/* 276:    */     }
/* 277:    */     
/* 278:    */     void receivedReply()
/* 279:    */     {
/* 280:277 */       this.receivedReply = true;
/* 281:    */     }
/* 282:    */     
/* 283:    */     int getSendWindowSize()
/* 284:    */     {
/* 285:281 */       return this.sendWindowSize.get();
/* 286:    */     }
/* 287:    */     
/* 288:    */     int updateSendWindowSize(int deltaWindowSize)
/* 289:    */     {
/* 290:285 */       return this.sendWindowSize.addAndGet(deltaWindowSize);
/* 291:    */     }
/* 292:    */     
/* 293:    */     int updateReceiveWindowSize(int deltaWindowSize)
/* 294:    */     {
/* 295:289 */       return this.receiveWindowSize.addAndGet(deltaWindowSize);
/* 296:    */     }
/* 297:    */     
/* 298:    */     int getReceiveWindowSizeLowerBound()
/* 299:    */     {
/* 300:293 */       return this.receiveWindowSizeLowerBound;
/* 301:    */     }
/* 302:    */     
/* 303:    */     void setReceiveWindowSizeLowerBound(int receiveWindowSizeLowerBound)
/* 304:    */     {
/* 305:297 */       this.receiveWindowSizeLowerBound = receiveWindowSizeLowerBound;
/* 306:    */     }
/* 307:    */     
/* 308:    */     boolean putPendingWrite(SpdySession.PendingWrite msg)
/* 309:    */     {
/* 310:301 */       return this.pendingWriteQueue.offer(msg);
/* 311:    */     }
/* 312:    */     
/* 313:    */     SpdySession.PendingWrite getPendingWrite()
/* 314:    */     {
/* 315:305 */       return (SpdySession.PendingWrite)this.pendingWriteQueue.peek();
/* 316:    */     }
/* 317:    */     
/* 318:    */     SpdySession.PendingWrite removePendingWrite()
/* 319:    */     {
/* 320:309 */       return (SpdySession.PendingWrite)this.pendingWriteQueue.poll();
/* 321:    */     }
/* 322:    */     
/* 323:    */     void clearPendingWrites(Throwable cause)
/* 324:    */     {
/* 325:    */       for (;;)
/* 326:    */       {
/* 327:314 */         SpdySession.PendingWrite pendingWrite = (SpdySession.PendingWrite)this.pendingWriteQueue.poll();
/* 328:315 */         if (pendingWrite == null) {
/* 329:    */           break;
/* 330:    */         }
/* 331:318 */         pendingWrite.fail(cause);
/* 332:    */       }
/* 333:    */     }
/* 334:    */   }
/* 335:    */   
/* 336:    */   private final class PriorityComparator
/* 337:    */     implements Comparator<Integer>
/* 338:    */   {
/* 339:    */     private PriorityComparator() {}
/* 340:    */     
/* 341:    */     public int compare(Integer id1, Integer id2)
/* 342:    */     {
/* 343:326 */       SpdySession.StreamState state1 = (SpdySession.StreamState)SpdySession.this.activeStreams.get(id1);
/* 344:327 */       SpdySession.StreamState state2 = (SpdySession.StreamState)SpdySession.this.activeStreams.get(id2);
/* 345:328 */       return state1.getPriority() - state2.getPriority();
/* 346:    */     }
/* 347:    */   }
/* 348:    */   
/* 349:    */   public static final class PendingWrite
/* 350:    */   {
/* 351:    */     final SpdyDataFrame spdyDataFrame;
/* 352:    */     final ChannelPromise promise;
/* 353:    */     
/* 354:    */     PendingWrite(SpdyDataFrame spdyDataFrame, ChannelPromise promise)
/* 355:    */     {
/* 356:337 */       this.spdyDataFrame = spdyDataFrame;
/* 357:338 */       this.promise = promise;
/* 358:    */     }
/* 359:    */     
/* 360:    */     void fail(Throwable cause)
/* 361:    */     {
/* 362:342 */       this.spdyDataFrame.release();
/* 363:343 */       this.promise.setFailure(cause);
/* 364:    */     }
/* 365:    */   }
/* 366:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.spdy.SpdySession
 * JD-Core Version:    0.7.0.1
 */