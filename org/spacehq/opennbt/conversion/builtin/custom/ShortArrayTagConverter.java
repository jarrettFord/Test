/*  1:   */ package org.spacehq.opennbt.conversion.builtin.custom;
/*  2:   */ 
/*  3:   */ import org.spacehq.opennbt.conversion.TagConverter;
/*  4:   */ import org.spacehq.opennbt.tag.builtin.custom.ShortArrayTag;
/*  5:   */ 
/*  6:   */ public class ShortArrayTagConverter
/*  7:   */   implements TagConverter<ShortArrayTag, short[]>
/*  8:   */ {
/*  9:   */   public short[] convert(ShortArrayTag tag)
/* 10:   */   {
/* 11:13 */     return tag.getValue();
/* 12:   */   }
/* 13:   */   
/* 14:   */   public ShortArrayTag convert(String name, short[] value)
/* 15:   */   {
/* 16:18 */     return new ShortArrayTag(name, value);
/* 17:   */   }
/* 18:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.conversion.builtin.custom.ShortArrayTagConverter
 * JD-Core Version:    0.7.0.1
 */