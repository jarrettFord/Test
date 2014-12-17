/*   1:    */ package io.netty.util.internal;
/*   2:    */ 
/*   3:    */ import java.util.Arrays;
/*   4:    */ 
/*   5:    */ public final class AppendableCharSequence
/*   6:    */   implements CharSequence, Appendable
/*   7:    */ {
/*   8:    */   private char[] chars;
/*   9:    */   private int pos;
/*  10:    */   
/*  11:    */   public AppendableCharSequence(int length)
/*  12:    */   {
/*  13: 26 */     if (length < 1) {
/*  14: 27 */       throw new IllegalArgumentException("length: " + length + " (length: >= 1)");
/*  15:    */     }
/*  16: 29 */     this.chars = new char[length];
/*  17:    */   }
/*  18:    */   
/*  19:    */   private AppendableCharSequence(char[] chars)
/*  20:    */   {
/*  21: 33 */     this.chars = chars;
/*  22: 34 */     this.pos = chars.length;
/*  23:    */   }
/*  24:    */   
/*  25:    */   public int length()
/*  26:    */   {
/*  27: 39 */     return this.pos;
/*  28:    */   }
/*  29:    */   
/*  30:    */   public char charAt(int index)
/*  31:    */   {
/*  32: 44 */     if (index > this.pos) {
/*  33: 45 */       throw new IndexOutOfBoundsException();
/*  34:    */     }
/*  35: 47 */     return this.chars[index];
/*  36:    */   }
/*  37:    */   
/*  38:    */   public AppendableCharSequence subSequence(int start, int end)
/*  39:    */   {
/*  40: 52 */     return new AppendableCharSequence(Arrays.copyOfRange(this.chars, start, end));
/*  41:    */   }
/*  42:    */   
/*  43:    */   public AppendableCharSequence append(char c)
/*  44:    */   {
/*  45: 57 */     if (this.pos == this.chars.length)
/*  46:    */     {
/*  47: 58 */       char[] old = this.chars;
/*  48:    */       
/*  49: 60 */       int len = old.length << 1;
/*  50: 61 */       if (len < 0) {
/*  51: 62 */         throw new IllegalStateException();
/*  52:    */       }
/*  53: 64 */       this.chars = new char[len];
/*  54: 65 */       System.arraycopy(old, 0, this.chars, 0, old.length);
/*  55:    */     }
/*  56: 67 */     this.chars[(this.pos++)] = c;
/*  57: 68 */     return this;
/*  58:    */   }
/*  59:    */   
/*  60:    */   public AppendableCharSequence append(CharSequence csq)
/*  61:    */   {
/*  62: 73 */     return append(csq, 0, csq.length());
/*  63:    */   }
/*  64:    */   
/*  65:    */   public AppendableCharSequence append(CharSequence csq, int start, int end)
/*  66:    */   {
/*  67: 78 */     if (csq.length() < end) {
/*  68: 79 */       throw new IndexOutOfBoundsException();
/*  69:    */     }
/*  70: 81 */     int length = end - start;
/*  71: 82 */     if (length > this.chars.length - this.pos) {
/*  72: 83 */       this.chars = expand(this.chars, this.pos + length, this.pos);
/*  73:    */     }
/*  74: 85 */     if ((csq instanceof AppendableCharSequence))
/*  75:    */     {
/*  76: 87 */       AppendableCharSequence seq = (AppendableCharSequence)csq;
/*  77: 88 */       char[] src = seq.chars;
/*  78: 89 */       System.arraycopy(src, start, this.chars, this.pos, length);
/*  79: 90 */       this.pos += length;
/*  80: 91 */       return this;
/*  81:    */     }
/*  82: 93 */     for (int i = start; i < end; i++) {
/*  83: 94 */       this.chars[(this.pos++)] = csq.charAt(i);
/*  84:    */     }
/*  85: 97 */     return this;
/*  86:    */   }
/*  87:    */   
/*  88:    */   public void reset()
/*  89:    */   {
/*  90:105 */     this.pos = 0;
/*  91:    */   }
/*  92:    */   
/*  93:    */   public String toString()
/*  94:    */   {
/*  95:110 */     return new String(this.chars, 0, this.pos);
/*  96:    */   }
/*  97:    */   
/*  98:    */   public String substring(int start, int end)
/*  99:    */   {
/* 100:117 */     int length = end - start;
/* 101:118 */     if ((start > this.pos) || (length > this.pos)) {
/* 102:119 */       throw new IndexOutOfBoundsException();
/* 103:    */     }
/* 104:121 */     return new String(this.chars, start, length);
/* 105:    */   }
/* 106:    */   
/* 107:    */   private static char[] expand(char[] array, int neededSpace, int size)
/* 108:    */   {
/* 109:125 */     int newCapacity = array.length;
/* 110:    */     do
/* 111:    */     {
/* 112:128 */       newCapacity <<= 1;
/* 113:130 */       if (newCapacity < 0) {
/* 114:131 */         throw new IllegalStateException();
/* 115:    */       }
/* 116:134 */     } while (neededSpace > newCapacity);
/* 117:136 */     char[] newArray = new char[newCapacity];
/* 118:137 */     System.arraycopy(array, 0, newArray, 0, size);
/* 119:    */     
/* 120:139 */     return newArray;
/* 121:    */   }
/* 122:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.internal.AppendableCharSequence
 * JD-Core Version:    0.7.0.1
 */