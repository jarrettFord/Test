/*  1:   */ package org.spacehq.mc.protocol.packet.login.server;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.mc.auth.GameProfile;
/*  5:   */ import org.spacehq.packetlib.io.NetInput;
/*  6:   */ import org.spacehq.packetlib.io.NetOutput;
/*  7:   */ import org.spacehq.packetlib.packet.Packet;
/*  8:   */ 
/*  9:   */ public class LoginSuccessPacket
/* 10:   */   implements Packet
/* 11:   */ {
/* 12:   */   private GameProfile profile;
/* 13:   */   
/* 14:   */   private LoginSuccessPacket() {}
/* 15:   */   
/* 16:   */   public LoginSuccessPacket(GameProfile profile)
/* 17:   */   {
/* 18:19 */     this.profile = profile;
/* 19:   */   }
/* 20:   */   
/* 21:   */   public GameProfile getProfile()
/* 22:   */   {
/* 23:23 */     return this.profile;
/* 24:   */   }
/* 25:   */   
/* 26:   */   public void read(NetInput in)
/* 27:   */     throws IOException
/* 28:   */   {
/* 29:28 */     this.profile = new GameProfile(in.readString(), in.readString());
/* 30:   */   }
/* 31:   */   
/* 32:   */   public void write(NetOutput out)
/* 33:   */     throws IOException
/* 34:   */   {
/* 35:33 */     out.writeString(this.profile.getIdAsString());
/* 36:34 */     out.writeString(this.profile.getName());
/* 37:   */   }
/* 38:   */   
/* 39:   */   public boolean isPriority()
/* 40:   */   {
/* 41:39 */     return true;
/* 42:   */   }
/* 43:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.login.server.LoginSuccessPacket
 * JD-Core Version:    0.7.0.1
 */