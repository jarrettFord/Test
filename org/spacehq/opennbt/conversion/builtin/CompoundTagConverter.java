/*  1:   */ package org.spacehq.opennbt.conversion.builtin;
/*  2:   */ 
/*  3:   */ import java.util.HashMap;
/*  4:   */ import java.util.Map;
/*  5:   */ import org.spacehq.opennbt.conversion.ConverterRegistry;
/*  6:   */ import org.spacehq.opennbt.conversion.TagConverter;
/*  7:   */ import org.spacehq.opennbt.tag.builtin.CompoundTag;
/*  8:   */ import org.spacehq.opennbt.tag.builtin.Tag;
/*  9:   */ 
/* 10:   */ public class CompoundTagConverter
/* 11:   */   implements TagConverter<CompoundTag, Map>
/* 12:   */ {
/* 13:   */   public Map convert(CompoundTag tag)
/* 14:   */   {
/* 15:18 */     Map<String, Object> ret = new HashMap();
/* 16:19 */     Map<String, Tag> tags = tag.getValue();
/* 17:20 */     for (String name : tags.keySet())
/* 18:   */     {
/* 19:21 */       Tag t = (Tag)tags.get(name);
/* 20:22 */       ret.put(t.getName(), ConverterRegistry.convertToValue(t));
/* 21:   */     }
/* 22:25 */     return ret;
/* 23:   */   }
/* 24:   */   
/* 25:   */   public CompoundTag convert(String name, Map value)
/* 26:   */   {
/* 27:30 */     Map<String, Tag> tags = new HashMap();
/* 28:31 */     for (Object na : value.keySet())
/* 29:   */     {
/* 30:32 */       String n = (String)na;
/* 31:33 */       tags.put(n, ConverterRegistry.convertToTag(n, value.get(n)));
/* 32:   */     }
/* 33:36 */     return new CompoundTag(name, tags);
/* 34:   */   }
/* 35:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.conversion.builtin.CompoundTagConverter
 * JD-Core Version:    0.7.0.1
 */