/*  1:   */ package org.spacehq.opennbt.conversion.builtin.custom;
/*  2:   */ 
/*  3:   */ import org.spacehq.opennbt.conversion.TagConverter;
/*  4:   */ import org.spacehq.opennbt.tag.builtin.custom.LongArrayTag;
/*  5:   */ 
/*  6:   */ public class LongArrayTagConverter
/*  7:   */   implements TagConverter<LongArrayTag, long[]>
/*  8:   */ {
/*  9:   */   public long[] convert(LongArrayTag tag)
/* 10:   */   {
/* 11:13 */     return tag.getValue();
/* 12:   */   }
/* 13:   */   
/* 14:   */   public LongArrayTag convert(String name, long[] value)
/* 15:   */   {
/* 16:18 */     return new LongArrayTag(name, value);
/* 17:   */   }
/* 18:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.conversion.builtin.custom.LongArrayTagConverter
 * JD-Core Version:    0.7.0.1
 */