/*   1:    */ package io.netty.channel;
/*   2:    */ 
/*   3:    */ import io.netty.util.AbstractReferenceCounted;
/*   4:    */ import io.netty.util.internal.logging.InternalLogger;
/*   5:    */ import io.netty.util.internal.logging.InternalLoggerFactory;
/*   6:    */ import java.io.IOException;
/*   7:    */ import java.nio.channels.FileChannel;
/*   8:    */ import java.nio.channels.WritableByteChannel;
/*   9:    */ 
/*  10:    */ public class DefaultFileRegion
/*  11:    */   extends AbstractReferenceCounted
/*  12:    */   implements FileRegion
/*  13:    */ {
/*  14: 34 */   private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultFileRegion.class);
/*  15:    */   private final FileChannel file;
/*  16:    */   private final long position;
/*  17:    */   private final long count;
/*  18:    */   private long transfered;
/*  19:    */   
/*  20:    */   public DefaultFileRegion(FileChannel file, long position, long count)
/*  21:    */   {
/*  22: 49 */     if (file == null) {
/*  23: 50 */       throw new NullPointerException("file");
/*  24:    */     }
/*  25: 52 */     if (position < 0L) {
/*  26: 53 */       throw new IllegalArgumentException("position must be >= 0 but was " + position);
/*  27:    */     }
/*  28: 55 */     if (count < 0L) {
/*  29: 56 */       throw new IllegalArgumentException("count must be >= 0 but was " + count);
/*  30:    */     }
/*  31: 58 */     this.file = file;
/*  32: 59 */     this.position = position;
/*  33: 60 */     this.count = count;
/*  34:    */   }
/*  35:    */   
/*  36:    */   public long position()
/*  37:    */   {
/*  38: 65 */     return this.position;
/*  39:    */   }
/*  40:    */   
/*  41:    */   public long count()
/*  42:    */   {
/*  43: 70 */     return this.count;
/*  44:    */   }
/*  45:    */   
/*  46:    */   public long transfered()
/*  47:    */   {
/*  48: 75 */     return this.transfered;
/*  49:    */   }
/*  50:    */   
/*  51:    */   public long transferTo(WritableByteChannel target, long position)
/*  52:    */     throws IOException
/*  53:    */   {
/*  54: 80 */     long count = this.count - position;
/*  55: 81 */     if ((count < 0L) || (position < 0L)) {
/*  56: 82 */       throw new IllegalArgumentException("position out of range: " + position + " (expected: 0 - " + (this.count - 1L) + ')');
/*  57:    */     }
/*  58: 86 */     if (count == 0L) {
/*  59: 87 */       return 0L;
/*  60:    */     }
/*  61: 90 */     long written = this.file.transferTo(this.position + position, count, target);
/*  62: 91 */     if (written > 0L) {
/*  63: 92 */       this.transfered += written;
/*  64:    */     }
/*  65: 94 */     return written;
/*  66:    */   }
/*  67:    */   
/*  68:    */   protected void deallocate()
/*  69:    */   {
/*  70:    */     try
/*  71:    */     {
/*  72:100 */       this.file.close();
/*  73:    */     }
/*  74:    */     catch (IOException e)
/*  75:    */     {
/*  76:102 */       if (logger.isWarnEnabled()) {
/*  77:103 */         logger.warn("Failed to close a file.", e);
/*  78:    */       }
/*  79:    */     }
/*  80:    */   }
/*  81:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.DefaultFileRegion
 * JD-Core Version:    0.7.0.1
 */