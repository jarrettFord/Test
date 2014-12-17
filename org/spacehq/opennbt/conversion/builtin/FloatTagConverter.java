/*  1:   */ package org.spacehq.opennbt.conversion.builtin;
/*  2:   */ 
/*  3:   */ import org.spacehq.opennbt.conversion.TagConverter;
/*  4:   */ import org.spacehq.opennbt.tag.builtin.FloatTag;
/*  5:   */ 
/*  6:   */ public class FloatTagConverter
/*  7:   */   implements TagConverter<FloatTag, Float>
/*  8:   */ {
/*  9:   */   public Float convert(FloatTag tag)
/* 10:   */   {
/* 11:13 */     return tag.getValue();
/* 12:   */   }
/* 13:   */   
/* 14:   */   public FloatTag convert(String name, Float value)
/* 15:   */   {
/* 16:18 */     return new FloatTag(name, value.floatValue());
/* 17:   */   }
/* 18:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.conversion.builtin.FloatTagConverter
 * JD-Core Version:    0.7.0.1
 */