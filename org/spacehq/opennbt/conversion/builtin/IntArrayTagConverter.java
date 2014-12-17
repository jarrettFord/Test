/*  1:   */ package org.spacehq.opennbt.conversion.builtin;
/*  2:   */ 
/*  3:   */ import org.spacehq.opennbt.conversion.TagConverter;
/*  4:   */ import org.spacehq.opennbt.tag.builtin.IntArrayTag;
/*  5:   */ 
/*  6:   */ public class IntArrayTagConverter
/*  7:   */   implements TagConverter<IntArrayTag, int[]>
/*  8:   */ {
/*  9:   */   public int[] convert(IntArrayTag tag)
/* 10:   */   {
/* 11:13 */     return tag.getValue();
/* 12:   */   }
/* 13:   */   
/* 14:   */   public IntArrayTag convert(String name, int[] value)
/* 15:   */   {
/* 16:18 */     return new IntArrayTag(name, value);
/* 17:   */   }
/* 18:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.conversion.builtin.IntArrayTagConverter
 * JD-Core Version:    0.7.0.1
 */