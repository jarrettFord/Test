/*  1:   */ package org.spacehq.opennbt.conversion.builtin.custom;
/*  2:   */ 
/*  3:   */ import org.spacehq.opennbt.conversion.TagConverter;
/*  4:   */ import org.spacehq.opennbt.tag.builtin.custom.StringArrayTag;
/*  5:   */ 
/*  6:   */ public class StringArrayTagConverter
/*  7:   */   implements TagConverter<StringArrayTag, String[]>
/*  8:   */ {
/*  9:   */   public String[] convert(StringArrayTag tag)
/* 10:   */   {
/* 11:13 */     return tag.getValue();
/* 12:   */   }
/* 13:   */   
/* 14:   */   public StringArrayTag convert(String name, String[] value)
/* 15:   */   {
/* 16:18 */     return new StringArrayTag(name, value);
/* 17:   */   }
/* 18:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.conversion.builtin.custom.StringArrayTagConverter
 * JD-Core Version:    0.7.0.1
 */