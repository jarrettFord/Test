/*  1:   */ package org.spacehq.opennbt.conversion.builtin;
/*  2:   */ 
/*  3:   */ import org.spacehq.opennbt.conversion.TagConverter;
/*  4:   */ import org.spacehq.opennbt.tag.builtin.LongTag;
/*  5:   */ 
/*  6:   */ public class LongTagConverter
/*  7:   */   implements TagConverter<LongTag, Long>
/*  8:   */ {
/*  9:   */   public Long convert(LongTag tag)
/* 10:   */   {
/* 11:13 */     return tag.getValue();
/* 12:   */   }
/* 13:   */   
/* 14:   */   public LongTag convert(String name, Long value)
/* 15:   */   {
/* 16:18 */     return new LongTag(name, value.longValue());
/* 17:   */   }
/* 18:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.conversion.builtin.LongTagConverter
 * JD-Core Version:    0.7.0.1
 */