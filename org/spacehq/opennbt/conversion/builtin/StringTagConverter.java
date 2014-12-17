/*  1:   */ package org.spacehq.opennbt.conversion.builtin;
/*  2:   */ 
/*  3:   */ import org.spacehq.opennbt.conversion.TagConverter;
/*  4:   */ import org.spacehq.opennbt.tag.builtin.StringTag;
/*  5:   */ 
/*  6:   */ public class StringTagConverter
/*  7:   */   implements TagConverter<StringTag, String>
/*  8:   */ {
/*  9:   */   public String convert(StringTag tag)
/* 10:   */   {
/* 11:13 */     return tag.getValue();
/* 12:   */   }
/* 13:   */   
/* 14:   */   public StringTag convert(String name, String value)
/* 15:   */   {
/* 16:18 */     return new StringTag(name, value);
/* 17:   */   }
/* 18:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.conversion.builtin.StringTagConverter
 * JD-Core Version:    0.7.0.1
 */