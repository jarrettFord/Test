/*  1:   */ package org.spacehq.mc.protocol.packet.login.client;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class LoginStartPacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private String username;
/* 12:   */   
/* 13:   */   private LoginStartPacket() {}
/* 14:   */   
/* 15:   */   public LoginStartPacket(String username)
/* 16:   */   {
/* 17:18 */     this.username = username;
/* 18:   */   }
/* 19:   */   
/* 20:   */   public String getUsername()
/* 21:   */   {
/* 22:22 */     return this.username;
/* 23:   */   }
/* 24:   */   
/* 25:   */   public void read(NetInput in)
/* 26:   */     throws IOException
/* 27:   */   {
/* 28:27 */     this.username = in.readString();
/* 29:   */   }
/* 30:   */   
/* 31:   */   public void write(NetOutput out)
/* 32:   */     throws IOException
/* 33:   */   {
/* 34:32 */     out.writeString(this.username);
/* 35:   */   }
/* 36:   */   
/* 37:   */   public boolean isPriority()
/* 38:   */   {
/* 39:37 */     return true;
/* 40:   */   }
/* 41:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.login.client.LoginStartPacket
 * JD-Core Version:    0.7.0.1
 */