/*  1:   */ package org.spacehq.opennbt.conversion.builtin;
/*  2:   */ 
/*  3:   */ import java.util.ArrayList;
/*  4:   */ import java.util.List;
/*  5:   */ import org.spacehq.opennbt.conversion.ConverterRegistry;
/*  6:   */ import org.spacehq.opennbt.conversion.TagConverter;
/*  7:   */ import org.spacehq.opennbt.tag.builtin.ListTag;
/*  8:   */ import org.spacehq.opennbt.tag.builtin.Tag;
/*  9:   */ 
/* 10:   */ public class ListTagConverter
/* 11:   */   implements TagConverter<ListTag, List>
/* 12:   */ {
/* 13:   */   public List convert(ListTag tag)
/* 14:   */   {
/* 15:18 */     List<Object> ret = new ArrayList();
/* 16:19 */     List<? extends Tag> tags = tag.getValue();
/* 17:20 */     for (Tag t : tags) {
/* 18:21 */       ret.add(ConverterRegistry.convertToValue(t));
/* 19:   */     }
/* 20:24 */     return ret;
/* 21:   */   }
/* 22:   */   
/* 23:   */   public ListTag convert(String name, List value)
/* 24:   */   {
/* 25:29 */     if (value.isEmpty()) {
/* 26:30 */       throw new IllegalArgumentException("Cannot convert ListTag with size of 0.");
/* 27:   */     }
/* 28:33 */     List<Tag> tags = new ArrayList();
/* 29:34 */     for (Object o : value) {
/* 30:35 */       tags.add(ConverterRegistry.convertToTag("", o));
/* 31:   */     }
/* 32:38 */     return new ListTag(name, tags);
/* 33:   */   }
/* 34:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.conversion.builtin.ListTagConverter
 * JD-Core Version:    0.7.0.1
 */