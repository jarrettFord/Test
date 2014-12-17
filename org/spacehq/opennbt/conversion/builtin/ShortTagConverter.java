/*  1:   */ package org.spacehq.opennbt.conversion.builtin;
/*  2:   */ 
/*  3:   */ import org.spacehq.opennbt.conversion.TagConverter;
/*  4:   */ import org.spacehq.opennbt.tag.builtin.ShortTag;
/*  5:   */ 
/*  6:   */ public class ShortTagConverter
/*  7:   */   implements TagConverter<ShortTag, Short>
/*  8:   */ {
/*  9:   */   public Short convert(ShortTag tag)
/* 10:   */   {
/* 11:13 */     return tag.getValue();
/* 12:   */   }
/* 13:   */   
/* 14:   */   public ShortTag convert(String name, Short value)
/* 15:   */   {
/* 16:18 */     return new ShortTag(name, value.shortValue());
/* 17:   */   }
/* 18:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.conversion.builtin.ShortTagConverter
 * JD-Core Version:    0.7.0.1
 */