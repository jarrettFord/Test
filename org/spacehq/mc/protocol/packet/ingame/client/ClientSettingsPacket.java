/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.client;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ClientSettingsPacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private String locale;
/* 12:   */   private int renderDistance;
/* 13:   */   private ChatVisibility chatVisibility;
/* 14:   */   private boolean chatColors;
/* 15:   */   private Difficulty difficulty;
/* 16:   */   private boolean capes;
/* 17:   */   
/* 18:   */   private ClientSettingsPacket() {}
/* 19:   */   
/* 20:   */   public ClientSettingsPacket(String locale, int renderDistance, ChatVisibility chatVisibility, boolean chatColors, Difficulty difficulty, boolean capes)
/* 21:   */   {
/* 22:23 */     this.locale = locale;
/* 23:24 */     this.renderDistance = renderDistance;
/* 24:25 */     this.chatVisibility = chatVisibility;
/* 25:26 */     this.chatColors = chatColors;
/* 26:27 */     this.difficulty = difficulty;
/* 27:28 */     this.capes = capes;
/* 28:   */   }
/* 29:   */   
/* 30:   */   public String getLocale()
/* 31:   */   {
/* 32:32 */     return this.locale;
/* 33:   */   }
/* 34:   */   
/* 35:   */   public int getRenderDistance()
/* 36:   */   {
/* 37:36 */     return this.renderDistance;
/* 38:   */   }
/* 39:   */   
/* 40:   */   public ChatVisibility getChatVisibility()
/* 41:   */   {
/* 42:40 */     return this.chatVisibility;
/* 43:   */   }
/* 44:   */   
/* 45:   */   public boolean getUseChatColors()
/* 46:   */   {
/* 47:44 */     return this.chatColors;
/* 48:   */   }
/* 49:   */   
/* 50:   */   public Difficulty getDifficulty()
/* 51:   */   {
/* 52:48 */     return this.difficulty;
/* 53:   */   }
/* 54:   */   
/* 55:   */   public boolean getShowCapes()
/* 56:   */   {
/* 57:52 */     return this.capes;
/* 58:   */   }
/* 59:   */   
/* 60:   */   public void read(NetInput in)
/* 61:   */     throws IOException
/* 62:   */   {
/* 63:57 */     this.locale = in.readString();
/* 64:58 */     this.renderDistance = in.readByte();
/* 65:59 */     this.chatVisibility = ChatVisibility.values()[in.readByte()];
/* 66:60 */     this.chatColors = in.readBoolean();
/* 67:61 */     this.difficulty = Difficulty.values()[in.readByte()];
/* 68:62 */     this.capes = in.readBoolean();
/* 69:   */   }
/* 70:   */   
/* 71:   */   public void write(NetOutput out)
/* 72:   */     throws IOException
/* 73:   */   {
/* 74:67 */     out.writeString(this.locale);
/* 75:68 */     out.writeByte(this.renderDistance);
/* 76:69 */     out.writeByte(this.chatVisibility.ordinal());
/* 77:70 */     out.writeBoolean(this.chatColors);
/* 78:71 */     out.writeByte(this.difficulty.ordinal());
/* 79:72 */     out.writeBoolean(this.capes);
/* 80:   */   }
/* 81:   */   
/* 82:   */   public boolean isPriority()
/* 83:   */   {
/* 84:77 */     return false;
/* 85:   */   }
/* 86:   */   
/* 87:   */   public static enum ChatVisibility
/* 88:   */   {
/* 89:81 */     FULL,  SYSTEM,  HIDDEN;
/* 90:   */   }
/* 91:   */   
/* 92:   */   public static enum Difficulty
/* 93:   */   {
/* 94:87 */     PEACEFUL,  EASY,  NORMAL,  HARD;
/* 95:   */   }
/* 96:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.client.ClientSettingsPacket
 * JD-Core Version:    0.7.0.1
 */