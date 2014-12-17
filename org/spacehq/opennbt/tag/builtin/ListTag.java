/*   1:    */ package org.spacehq.opennbt.tag.builtin;
/*   2:    */ 
/*   3:    */ import java.io.DataInputStream;
/*   4:    */ import java.io.DataOutputStream;
/*   5:    */ import java.io.IOException;
/*   6:    */ import java.util.ArrayList;
/*   7:    */ import java.util.Iterator;
/*   8:    */ import java.util.List;
/*   9:    */ import org.spacehq.opennbt.tag.TagCreateException;
/*  10:    */ import org.spacehq.opennbt.tag.TagRegistry;
/*  11:    */ 
/*  12:    */ public class ListTag
/*  13:    */   extends Tag
/*  14:    */   implements Iterable<Tag>
/*  15:    */ {
/*  16:    */   private Class<? extends Tag> type;
/*  17:    */   private List<Tag> value;
/*  18:    */   
/*  19:    */   private ListTag(String name)
/*  20:    */   {
/*  21: 27 */     super(name);
/*  22:    */   }
/*  23:    */   
/*  24:    */   public ListTag(String name, Class<? extends Tag> type)
/*  25:    */   {
/*  26: 37 */     super(name);
/*  27: 38 */     this.type = type;
/*  28: 39 */     this.value = new ArrayList();
/*  29:    */   }
/*  30:    */   
/*  31:    */   public ListTag(String name, List<Tag> value)
/*  32:    */     throws IllegalArgumentException
/*  33:    */   {
/*  34: 50 */     super(name);
/*  35: 51 */     Class<? extends Tag> type = null;
/*  36: 52 */     for (Tag tag : value)
/*  37:    */     {
/*  38: 53 */       if (tag == null) {
/*  39: 54 */         throw new IllegalArgumentException("List cannot contain null tags.");
/*  40:    */       }
/*  41: 57 */       if (type == null) {
/*  42: 58 */         type = tag.getClass();
/*  43: 59 */       } else if (tag.getClass() != type) {
/*  44: 60 */         throw new IllegalArgumentException("All tags must be of the same type.");
/*  45:    */       }
/*  46:    */     }
/*  47: 64 */     this.type = type;
/*  48: 65 */     this.value = new ArrayList(value);
/*  49:    */   }
/*  50:    */   
/*  51:    */   public List<Tag> getValue()
/*  52:    */   {
/*  53: 70 */     return new ArrayList(this.value);
/*  54:    */   }
/*  55:    */   
/*  56:    */   public void setValue(List<Tag> value)
/*  57:    */   {
/*  58: 79 */     for (Tag tag : value) {
/*  59: 80 */       if (tag.getClass() != this.type) {
/*  60: 81 */         throw new IllegalArgumentException("Tag type cannot differ from ListTag type.");
/*  61:    */       }
/*  62:    */     }
/*  63: 85 */     this.value = new ArrayList(value);
/*  64:    */   }
/*  65:    */   
/*  66:    */   public Class<? extends Tag> getElementType()
/*  67:    */   {
/*  68: 94 */     return this.type;
/*  69:    */   }
/*  70:    */   
/*  71:    */   public boolean add(Tag tag)
/*  72:    */   {
/*  73:104 */     if (tag.getClass() != this.type) {
/*  74:105 */       throw new IllegalArgumentException("Tag type cannot differ from ListTag type.");
/*  75:    */     }
/*  76:108 */     return this.value.add(tag);
/*  77:    */   }
/*  78:    */   
/*  79:    */   public boolean remove(Tag tag)
/*  80:    */   {
/*  81:118 */     return this.value.remove(tag);
/*  82:    */   }
/*  83:    */   
/*  84:    */   public <T extends Tag> T get(int index)
/*  85:    */   {
/*  86:128 */     return (Tag)this.value.get(index);
/*  87:    */   }
/*  88:    */   
/*  89:    */   public int size()
/*  90:    */   {
/*  91:137 */     return this.value.size();
/*  92:    */   }
/*  93:    */   
/*  94:    */   public Iterator<Tag> iterator()
/*  95:    */   {
/*  96:142 */     return this.value.iterator();
/*  97:    */   }
/*  98:    */   
/*  99:    */   public void read(DataInputStream in)
/* 100:    */     throws IOException
/* 101:    */   {
/* 102:147 */     int id = in.readUnsignedByte();
/* 103:148 */     this.type = TagRegistry.getClassFor(id);
/* 104:149 */     this.value = new ArrayList();
/* 105:150 */     if ((id != 0) && (this.type == null)) {
/* 106:151 */       throw new IOException("Unknown tag ID in ListTag: " + id);
/* 107:    */     }
/* 108:154 */     int count = in.readInt();
/* 109:155 */     for (int index = 0; index < count; index++)
/* 110:    */     {
/* 111:156 */       Tag tag = null;
/* 112:    */       try
/* 113:    */       {
/* 114:158 */         tag = TagRegistry.createInstance(id, "");
/* 115:    */       }
/* 116:    */       catch (TagCreateException e)
/* 117:    */       {
/* 118:160 */         throw new IOException("Failed to create tag.", e);
/* 119:    */       }
/* 120:163 */       tag.read(in);
/* 121:164 */       add(tag);
/* 122:    */     }
/* 123:    */   }
/* 124:    */   
/* 125:    */   public void write(DataOutputStream out)
/* 126:    */     throws IOException
/* 127:    */   {
/* 128:170 */     if (this.value.isEmpty())
/* 129:    */     {
/* 130:171 */       out.writeByte(0);
/* 131:    */     }
/* 132:    */     else
/* 133:    */     {
/* 134:173 */       int id = TagRegistry.getIdFor(this.type);
/* 135:174 */       if (id == -1) {
/* 136:175 */         throw new IOException("ListTag contains unregistered tag class.");
/* 137:    */       }
/* 138:178 */       out.writeByte(id);
/* 139:    */     }
/* 140:181 */     out.writeInt(this.value.size());
/* 141:182 */     for (Tag tag : this.value) {
/* 142:183 */       tag.write(out);
/* 143:    */     }
/* 144:    */   }
/* 145:    */   
/* 146:    */   public ListTag clone()
/* 147:    */   {
/* 148:189 */     List<Tag> newList = new ArrayList();
/* 149:190 */     for (Tag value : this.value) {
/* 150:191 */       newList.add(value.clone());
/* 151:    */     }
/* 152:194 */     return new ListTag(getName(), newList);
/* 153:    */   }
/* 154:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.opennbt.tag.builtin.ListTag
 * JD-Core Version:    0.7.0.1
 */