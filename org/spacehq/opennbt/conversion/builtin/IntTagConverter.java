/*  1:   */ package org.spacehq.opennbt.conversion.builtin;
/*  2:   */ 
/*  3:   */ import org.spacehq.opennbt.conversion.TagConverter;
/*  4:   */ import org.spacehq.opennbt.tag.builtin.IntTag;
/*  5:   */ 
/*  6:   */ public class IntTagConverter
/*  7:   */   implements TagConverter<IntTag, Integer>
/*  8:   */ {
/*  9:   */   public Integer convert(IntTag tag)
/* 10:   */   {
/* 11:13 */     return tag.getValue();
/* 12:   */   }
/* 13:   */   
/* 14:   */   public IntTag convert(String name, Integer value)
/* 15:   */   {
/* 16:18 */     return new IntTag(name, value.intValue());
/* 17:   */   }
/* 18:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.conversion.builtin.IntTagConverter
 * JD-Core Version:    0.7.0.1
 */