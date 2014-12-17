/*  1:   */ package org.spacehq.opennbt.conversion.builtin;
/*  2:   */ 
/*  3:   */ import org.spacehq.opennbt.conversion.TagConverter;
/*  4:   */ import org.spacehq.opennbt.tag.builtin.DoubleTag;
/*  5:   */ 
/*  6:   */ public class DoubleTagConverter
/*  7:   */   implements TagConverter<DoubleTag, Double>
/*  8:   */ {
/*  9:   */   public Double convert(DoubleTag tag)
/* 10:   */   {
/* 11:13 */     return tag.getValue();
/* 12:   */   }
/* 13:   */   
/* 14:   */   public DoubleTag convert(String name, Double value)
/* 15:   */   {
/* 16:18 */     return new DoubleTag(name, value.doubleValue());
/* 17:   */   }
/* 18:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.conversion.builtin.DoubleTagConverter
 * JD-Core Version:    0.7.0.1
 */