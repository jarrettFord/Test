/*   1:    */ package org.spacehq.opennbt.tag.builtin;
/*   2:    */ 
/*   3:    */ import java.io.DataInputStream;
/*   4:    */ import java.io.DataOutputStream;
/*   5:    */ import java.io.EOFException;
/*   6:    */ import java.io.IOException;
/*   7:    */ import java.util.ArrayList;
/*   8:    */ import java.util.Collection;
/*   9:    */ import java.util.Iterator;
/*  10:    */ import java.util.LinkedHashMap;
/*  11:    */ import java.util.List;
/*  12:    */ import java.util.Map;
/*  13:    */ import java.util.Map.Entry;
/*  14:    */ import java.util.Set;
/*  15:    */ import org.spacehq.opennbt.NBTIO;
/*  16:    */ 
/*  17:    */ public class CompoundTag
/*  18:    */   extends Tag
/*  19:    */   implements Iterable<Tag>
/*  20:    */ {
/*  21:    */   private Map<String, Tag> value;
/*  22:    */   
/*  23:    */   public CompoundTag(String name)
/*  24:    */   {
/*  25: 25 */     this(name, new LinkedHashMap());
/*  26:    */   }
/*  27:    */   
/*  28:    */   public CompoundTag(String name, Map<String, Tag> value)
/*  29:    */   {
/*  30: 35 */     super(name);
/*  31: 36 */     this.value = new LinkedHashMap(value);
/*  32:    */   }
/*  33:    */   
/*  34:    */   public Map<String, Tag> getValue()
/*  35:    */   {
/*  36: 41 */     return new LinkedHashMap(this.value);
/*  37:    */   }
/*  38:    */   
/*  39:    */   public void setValue(Map<String, Tag> value)
/*  40:    */   {
/*  41: 50 */     this.value = new LinkedHashMap(value);
/*  42:    */   }
/*  43:    */   
/*  44:    */   public boolean isEmpty()
/*  45:    */   {
/*  46: 59 */     return this.value.isEmpty();
/*  47:    */   }
/*  48:    */   
/*  49:    */   public boolean contains(String tagName)
/*  50:    */   {
/*  51: 69 */     return this.value.containsKey(tagName);
/*  52:    */   }
/*  53:    */   
/*  54:    */   public <T extends Tag> T get(String tagName)
/*  55:    */   {
/*  56: 79 */     return (Tag)this.value.get(tagName);
/*  57:    */   }
/*  58:    */   
/*  59:    */   public <T extends Tag> T put(T tag)
/*  60:    */   {
/*  61: 89 */     return (Tag)this.value.put(tag.getName(), tag);
/*  62:    */   }
/*  63:    */   
/*  64:    */   public <T extends Tag> T remove(String tagName)
/*  65:    */   {
/*  66: 99 */     return (Tag)this.value.remove(tagName);
/*  67:    */   }
/*  68:    */   
/*  69:    */   public Set<String> keySet()
/*  70:    */   {
/*  71:108 */     return this.value.keySet();
/*  72:    */   }
/*  73:    */   
/*  74:    */   public Collection<Tag> values()
/*  75:    */   {
/*  76:117 */     return this.value.values();
/*  77:    */   }
/*  78:    */   
/*  79:    */   public int size()
/*  80:    */   {
/*  81:126 */     return this.value.size();
/*  82:    */   }
/*  83:    */   
/*  84:    */   public void clear()
/*  85:    */   {
/*  86:133 */     this.value.clear();
/*  87:    */   }
/*  88:    */   
/*  89:    */   public Iterator<Tag> iterator()
/*  90:    */   {
/*  91:138 */     return values().iterator();
/*  92:    */   }
/*  93:    */   
/*  94:    */   public void read(DataInputStream in)
/*  95:    */     throws IOException
/*  96:    */   {
/*  97:143 */     List<Tag> tags = new ArrayList();
/*  98:    */     try
/*  99:    */     {
/* 100:    */       Tag tag;
/* 101:146 */       while ((tag = NBTIO.readTag(in)) != null)
/* 102:    */       {
/* 103:    */         Tag tag;
/* 104:147 */         tags.add(tag);
/* 105:    */       }
/* 106:    */     }
/* 107:    */     catch (EOFException e)
/* 108:    */     {
/* 109:150 */       throw new IOException("Closing EndTag was not found!");
/* 110:    */     }
/* 111:153 */     for (Tag tag : tags) {
/* 112:154 */       put(tag);
/* 113:    */     }
/* 114:    */   }
/* 115:    */   
/* 116:    */   public void write(DataOutputStream out)
/* 117:    */     throws IOException
/* 118:    */   {
/* 119:160 */     for (Tag tag : this.value.values()) {
/* 120:161 */       NBTIO.writeTag(out, tag);
/* 121:    */     }
/* 122:164 */     out.writeByte(0);
/* 123:    */   }
/* 124:    */   
/* 125:    */   public CompoundTag clone()
/* 126:    */   {
/* 127:169 */     Map<String, Tag> newMap = new LinkedHashMap();
/* 128:170 */     for (Map.Entry<String, Tag> entry : this.value.entrySet()) {
/* 129:171 */       newMap.put((String)entry.getKey(), ((Tag)entry.getValue()).clone());
/* 130:    */     }
/* 131:174 */     return new CompoundTag(getName(), newMap);
/* 132:    */   }
/* 133:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.tag.builtin.CompoundTag
 * JD-Core Version:    0.7.0.1
 */