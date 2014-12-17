/*  1:   */ package org.spacehq.opennbt.conversion.builtin.custom;
/*  2:   */ 
/*  3:   */ import org.spacehq.opennbt.conversion.TagConverter;
/*  4:   */ import org.spacehq.opennbt.tag.builtin.custom.DoubleArrayTag;
/*  5:   */ 
/*  6:   */ public class DoubleArrayTagConverter
/*  7:   */   implements TagConverter<DoubleArrayTag, double[]>
/*  8:   */ {
/*  9:   */   public double[] convert(DoubleArrayTag tag)
/* 10:   */   {
/* 11:13 */     return tag.getValue();
/* 12:   */   }
/* 13:   */   
/* 14:   */   public DoubleArrayTag convert(String name, double[] value)
/* 15:   */   {
/* 16:18 */     return new DoubleArrayTag(name, value);
/* 17:   */   }
/* 18:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.conversion.builtin.custom.DoubleArrayTagConverter
 * JD-Core Version:    0.7.0.1
 */