/*  1:   */ package org.spacehq.opennbt.conversion.builtin.custom;
/*  2:   */ 
/*  3:   */ import org.spacehq.opennbt.conversion.TagConverter;
/*  4:   */ import org.spacehq.opennbt.tag.builtin.custom.FloatArrayTag;
/*  5:   */ 
/*  6:   */ public class FloatArrayTagConverter
/*  7:   */   implements TagConverter<FloatArrayTag, float[]>
/*  8:   */ {
/*  9:   */   public float[] convert(FloatArrayTag tag)
/* 10:   */   {
/* 11:13 */     return tag.getValue();
/* 12:   */   }
/* 13:   */   
/* 14:   */   public FloatArrayTag convert(String name, float[] value)
/* 15:   */   {
/* 16:18 */     return new FloatArrayTag(name, value);
/* 17:   */   }
/* 18:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.conversion.builtin.custom.FloatArrayTagConverter
 * JD-Core Version:    0.7.0.1
 */