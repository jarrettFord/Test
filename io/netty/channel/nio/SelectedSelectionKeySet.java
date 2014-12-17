/*   1:    */ package io.netty.channel.nio;
/*   2:    */ 
/*   3:    */ import java.nio.channels.SelectionKey;
/*   4:    */ import java.util.AbstractSet;
/*   5:    */ import java.util.Iterator;
/*   6:    */ 
/*   7:    */ final class SelectedSelectionKeySet
/*   8:    */   extends AbstractSet<SelectionKey>
/*   9:    */ {
/*  10:    */   private SelectionKey[] keysA;
/*  11:    */   private int keysASize;
/*  12:    */   private SelectionKey[] keysB;
/*  13:    */   private int keysBSize;
/*  14: 29 */   private boolean isA = true;
/*  15:    */   
/*  16:    */   SelectedSelectionKeySet()
/*  17:    */   {
/*  18: 32 */     this.keysA = new SelectionKey[1024];
/*  19: 33 */     this.keysB = ((SelectionKey[])this.keysA.clone());
/*  20:    */   }
/*  21:    */   
/*  22:    */   public boolean add(SelectionKey o)
/*  23:    */   {
/*  24: 38 */     if (o == null) {
/*  25: 39 */       return false;
/*  26:    */     }
/*  27: 42 */     if (this.isA)
/*  28:    */     {
/*  29: 43 */       int size = this.keysASize;
/*  30: 44 */       this.keysA[(size++)] = o;
/*  31: 45 */       this.keysASize = size;
/*  32: 46 */       if (size == this.keysA.length) {
/*  33: 47 */         doubleCapacityA();
/*  34:    */       }
/*  35:    */     }
/*  36:    */     else
/*  37:    */     {
/*  38: 50 */       int size = this.keysBSize;
/*  39: 51 */       this.keysB[(size++)] = o;
/*  40: 52 */       this.keysBSize = size;
/*  41: 53 */       if (size == this.keysB.length) {
/*  42: 54 */         doubleCapacityB();
/*  43:    */       }
/*  44:    */     }
/*  45: 58 */     return true;
/*  46:    */   }
/*  47:    */   
/*  48:    */   private void doubleCapacityA()
/*  49:    */   {
/*  50: 62 */     SelectionKey[] newKeysA = new SelectionKey[this.keysA.length << 1];
/*  51: 63 */     System.arraycopy(this.keysA, 0, newKeysA, 0, this.keysASize);
/*  52: 64 */     this.keysA = newKeysA;
/*  53:    */   }
/*  54:    */   
/*  55:    */   private void doubleCapacityB()
/*  56:    */   {
/*  57: 68 */     SelectionKey[] newKeysB = new SelectionKey[this.keysB.length << 1];
/*  58: 69 */     System.arraycopy(this.keysB, 0, newKeysB, 0, this.keysBSize);
/*  59: 70 */     this.keysB = newKeysB;
/*  60:    */   }
/*  61:    */   
/*  62:    */   SelectionKey[] flip()
/*  63:    */   {
/*  64: 74 */     if (this.isA)
/*  65:    */     {
/*  66: 75 */       this.isA = false;
/*  67: 76 */       this.keysA[this.keysASize] = null;
/*  68: 77 */       this.keysBSize = 0;
/*  69: 78 */       return this.keysA;
/*  70:    */     }
/*  71: 80 */     this.isA = true;
/*  72: 81 */     this.keysB[this.keysBSize] = null;
/*  73: 82 */     this.keysASize = 0;
/*  74: 83 */     return this.keysB;
/*  75:    */   }
/*  76:    */   
/*  77:    */   public int size()
/*  78:    */   {
/*  79: 89 */     if (this.isA) {
/*  80: 90 */       return this.keysASize;
/*  81:    */     }
/*  82: 92 */     return this.keysBSize;
/*  83:    */   }
/*  84:    */   
/*  85:    */   public boolean remove(Object o)
/*  86:    */   {
/*  87: 98 */     return false;
/*  88:    */   }
/*  89:    */   
/*  90:    */   public boolean contains(Object o)
/*  91:    */   {
/*  92:103 */     return false;
/*  93:    */   }
/*  94:    */   
/*  95:    */   public Iterator<SelectionKey> iterator()
/*  96:    */   {
/*  97:108 */     throw new UnsupportedOperationException();
/*  98:    */   }
/*  99:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.nio.SelectedSelectionKeySet
 * JD-Core Version:    0.7.0.1
 */