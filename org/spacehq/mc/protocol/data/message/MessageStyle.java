/*   1:    */ package org.spacehq.mc.protocol.data.message;
/*   2:    */ 
/*   3:    */ import java.util.ArrayList;
/*   4:    */ import java.util.List;
/*   5:    */ 
/*   6:    */ public class MessageStyle
/*   7:    */   implements Cloneable
/*   8:    */ {
/*   9:  8 */   private ChatColor color = ChatColor.WHITE;
/*  10:  9 */   private List<ChatFormat> formats = new ArrayList();
/*  11:    */   private ClickEvent click;
/*  12:    */   private HoverEvent hover;
/*  13:    */   private String insertion;
/*  14:    */   private MessageStyle parent;
/*  15:    */   
/*  16:    */   public MessageStyle() {}
/*  17:    */   
/*  18:    */   public MessageStyle(MessageStyle parent)
/*  19:    */   {
/*  20: 19 */     this.parent = parent;
/*  21:    */   }
/*  22:    */   
/*  23:    */   public boolean isDefault()
/*  24:    */   {
/*  25: 23 */     return (this.color == ChatColor.WHITE) && (this.formats.isEmpty()) && (this.click == null) && (this.hover == null) && (this.insertion == null);
/*  26:    */   }
/*  27:    */   
/*  28:    */   public ChatColor getColor()
/*  29:    */   {
/*  30: 27 */     return this.color;
/*  31:    */   }
/*  32:    */   
/*  33:    */   public List<ChatFormat> getFormats()
/*  34:    */   {
/*  35: 31 */     return new ArrayList(this.formats);
/*  36:    */   }
/*  37:    */   
/*  38:    */   public ClickEvent getClickEvent()
/*  39:    */   {
/*  40: 35 */     return this.click;
/*  41:    */   }
/*  42:    */   
/*  43:    */   public HoverEvent getHoverEvent()
/*  44:    */   {
/*  45: 39 */     return this.hover;
/*  46:    */   }
/*  47:    */   
/*  48:    */   public String getInsertion()
/*  49:    */   {
/*  50: 43 */     return this.insertion;
/*  51:    */   }
/*  52:    */   
/*  53:    */   public MessageStyle getParent()
/*  54:    */   {
/*  55: 47 */     return this.parent != null ? this.parent : new MessageStyle();
/*  56:    */   }
/*  57:    */   
/*  58:    */   public MessageStyle setColor(ChatColor color)
/*  59:    */   {
/*  60: 51 */     this.color = color;
/*  61: 52 */     return this;
/*  62:    */   }
/*  63:    */   
/*  64:    */   public MessageStyle setFormats(List<ChatFormat> formats)
/*  65:    */   {
/*  66: 56 */     this.formats = new ArrayList(formats);
/*  67: 57 */     return this;
/*  68:    */   }
/*  69:    */   
/*  70:    */   public MessageStyle addFormat(ChatFormat format)
/*  71:    */   {
/*  72: 61 */     this.formats.add(format);
/*  73: 62 */     return this;
/*  74:    */   }
/*  75:    */   
/*  76:    */   public MessageStyle removeFormat(ChatFormat format)
/*  77:    */   {
/*  78: 66 */     this.formats.remove(format);
/*  79: 67 */     return this;
/*  80:    */   }
/*  81:    */   
/*  82:    */   public MessageStyle clearFormats()
/*  83:    */   {
/*  84: 71 */     this.formats.clear();
/*  85: 72 */     return this;
/*  86:    */   }
/*  87:    */   
/*  88:    */   public MessageStyle setClickEvent(ClickEvent event)
/*  89:    */   {
/*  90: 76 */     this.click = event;
/*  91: 77 */     return this;
/*  92:    */   }
/*  93:    */   
/*  94:    */   public MessageStyle setHoverEvent(HoverEvent event)
/*  95:    */   {
/*  96: 81 */     this.hover = event;
/*  97: 82 */     return this;
/*  98:    */   }
/*  99:    */   
/* 100:    */   public MessageStyle setInsertion(String insertion)
/* 101:    */   {
/* 102: 86 */     this.insertion = insertion;
/* 103: 87 */     return this;
/* 104:    */   }
/* 105:    */   
/* 106:    */   protected MessageStyle setParent(MessageStyle parent)
/* 107:    */   {
/* 108: 91 */     this.parent = parent;
/* 109: 92 */     return this;
/* 110:    */   }
/* 111:    */   
/* 112:    */   public String toString()
/* 113:    */   {
/* 114: 97 */     return "MessageStyle{color=" + this.color + ",formats=" + this.formats + ",clickEvent=" + this.click + ",hoverEvent=" + this.hover + ",insertion=" + this.insertion + "}";
/* 115:    */   }
/* 116:    */   
/* 117:    */   public MessageStyle clone()
/* 118:    */   {
/* 119:102 */     return (this.parent != null ? new MessageStyle(this.parent) : new MessageStyle()).setColor(this.color).setFormats(this.formats).setClickEvent(this.click != null ? this.click.clone() : null).setHoverEvent(this.hover != null ? this.hover.clone() : null).setInsertion(this.insertion);
/* 120:    */   }
/* 121:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.data.message.MessageStyle
 * JD-Core Version:    0.7.0.1
 */