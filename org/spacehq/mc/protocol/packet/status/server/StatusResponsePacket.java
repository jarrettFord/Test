/*   1:    */ package org.spacehq.mc.protocol.packet.status.server;
/*   2:    */ 
/*   3:    */ import com.google.gson.Gson;
/*   4:    */ import com.google.gson.JsonArray;
/*   5:    */ import com.google.gson.JsonElement;
/*   6:    */ import com.google.gson.JsonObject;
/*   7:    */ import java.awt.image.BufferedImage;
/*   8:    */ import java.io.ByteArrayInputStream;
/*   9:    */ import java.io.ByteArrayOutputStream;
/*  10:    */ import java.io.IOException;
/*  11:    */ import javax.imageio.ImageIO;
/*  12:    */ import org.spacehq.mc.auth.GameProfile;
/*  13:    */ import org.spacehq.mc.auth.util.Base64;
/*  14:    */ import org.spacehq.mc.protocol.data.message.Message;
/*  15:    */ import org.spacehq.mc.protocol.data.status.PlayerInfo;
/*  16:    */ import org.spacehq.mc.protocol.data.status.ServerStatusInfo;
/*  17:    */ import org.spacehq.mc.protocol.data.status.VersionInfo;
/*  18:    */ import org.spacehq.packetlib.io.NetInput;
/*  19:    */ import org.spacehq.packetlib.io.NetOutput;
/*  20:    */ import org.spacehq.packetlib.packet.Packet;
/*  21:    */ 
/*  22:    */ public class StatusResponsePacket
/*  23:    */   implements Packet
/*  24:    */ {
/*  25:    */   private ServerStatusInfo info;
/*  26:    */   
/*  27:    */   private StatusResponsePacket() {}
/*  28:    */   
/*  29:    */   public StatusResponsePacket(ServerStatusInfo info)
/*  30:    */   {
/*  31: 32 */     this.info = info;
/*  32:    */   }
/*  33:    */   
/*  34:    */   public ServerStatusInfo getInfo()
/*  35:    */   {
/*  36: 36 */     return this.info;
/*  37:    */   }
/*  38:    */   
/*  39:    */   public void read(NetInput in)
/*  40:    */     throws IOException
/*  41:    */   {
/*  42: 41 */     JsonObject obj = (JsonObject)new Gson().fromJson(in.readString(), JsonObject.class);
/*  43: 42 */     JsonObject ver = obj.get("version").getAsJsonObject();
/*  44: 43 */     VersionInfo version = new VersionInfo(ver.get("name").getAsString(), ver.get("protocol").getAsInt());
/*  45: 44 */     JsonObject plrs = obj.get("players").getAsJsonObject();
/*  46: 45 */     GameProfile[] profiles = new GameProfile[0];
/*  47: 46 */     if (plrs.has("sample"))
/*  48:    */     {
/*  49: 47 */       JsonArray prof = plrs.get("sample").getAsJsonArray();
/*  50: 48 */       if (prof.size() > 0)
/*  51:    */       {
/*  52: 49 */         profiles = new GameProfile[prof.size()];
/*  53: 50 */         for (int index = 0; index < prof.size(); index++)
/*  54:    */         {
/*  55: 51 */           JsonObject o = prof.get(index).getAsJsonObject();
/*  56: 52 */           profiles[index] = new GameProfile(o.get("id").getAsString(), o.get("name").getAsString());
/*  57:    */         }
/*  58:    */       }
/*  59:    */     }
/*  60: 57 */     PlayerInfo players = new PlayerInfo(plrs.get("max").getAsInt(), plrs.get("online").getAsInt(), profiles);
/*  61: 58 */     JsonElement desc = obj.get("description");
/*  62: 59 */     Message description = Message.fromJson(desc);
/*  63: 60 */     BufferedImage icon = null;
/*  64: 61 */     if (obj.has("favicon")) {
/*  65: 62 */       icon = stringToIcon(obj.get("favicon").getAsString());
/*  66:    */     }
/*  67: 65 */     this.info = new ServerStatusInfo(version, players, description, icon);
/*  68:    */   }
/*  69:    */   
/*  70:    */   public void write(NetOutput out)
/*  71:    */     throws IOException
/*  72:    */   {
/*  73: 70 */     JsonObject obj = new JsonObject();
/*  74: 71 */     JsonObject ver = new JsonObject();
/*  75: 72 */     ver.addProperty("name", this.info.getVersionInfo().getVersionName());
/*  76: 73 */     ver.addProperty("protocol", Integer.valueOf(this.info.getVersionInfo().getProtocolVersion()));
/*  77: 74 */     JsonObject plrs = new JsonObject();
/*  78: 75 */     plrs.addProperty("max", Integer.valueOf(this.info.getPlayerInfo().getMaxPlayers()));
/*  79: 76 */     plrs.addProperty("online", Integer.valueOf(this.info.getPlayerInfo().getOnlinePlayers()));
/*  80: 77 */     if (this.info.getPlayerInfo().getPlayers().length > 0)
/*  81:    */     {
/*  82: 78 */       JsonArray array = new JsonArray();
/*  83: 79 */       for (GameProfile profile : this.info.getPlayerInfo().getPlayers())
/*  84:    */       {
/*  85: 80 */         JsonObject o = new JsonObject();
/*  86: 81 */         o.addProperty("name", profile.getName());
/*  87: 82 */         o.addProperty("id", profile.getIdAsString());
/*  88: 83 */         array.add(o);
/*  89:    */       }
/*  90: 86 */       plrs.add("sample", array);
/*  91:    */     }
/*  92: 89 */     obj.add("version", ver);
/*  93: 90 */     obj.add("players", plrs);
/*  94: 91 */     obj.add("description", this.info.getDescription().toJson());
/*  95: 92 */     if (this.info.getIcon() != null) {
/*  96: 93 */       obj.addProperty("favicon", iconToString(this.info.getIcon()));
/*  97:    */     }
/*  98: 96 */     out.writeString(obj.toString());
/*  99:    */   }
/* 100:    */   
/* 101:    */   private BufferedImage stringToIcon(String str)
/* 102:    */     throws IOException
/* 103:    */   {
/* 104:100 */     if (str.startsWith("data:image/png;base64,")) {
/* 105:101 */       str = str.substring("data:image/png;base64,".length());
/* 106:    */     }
/* 107:104 */     byte[] bytes = Base64.decode(str.getBytes("UTF-8"));
/* 108:105 */     ByteArrayInputStream in = new ByteArrayInputStream(bytes);
/* 109:106 */     BufferedImage icon = ImageIO.read(in);
/* 110:107 */     in.close();
/* 111:108 */     if ((icon != null) && ((icon.getWidth() != 64) || (icon.getHeight() != 64))) {
/* 112:109 */       throw new IOException("Icon must be 64x64.");
/* 113:    */     }
/* 114:112 */     return icon;
/* 115:    */   }
/* 116:    */   
/* 117:    */   private String iconToString(BufferedImage icon)
/* 118:    */     throws IOException
/* 119:    */   {
/* 120:116 */     if ((icon.getWidth() != 64) || (icon.getHeight() != 64)) {
/* 121:117 */       throw new IOException("Icon must be 64x64.");
/* 122:    */     }
/* 123:120 */     ByteArrayOutputStream out = new ByteArrayOutputStream();
/* 124:121 */     ImageIO.write(icon, "PNG", out);
/* 125:122 */     out.close();
/* 126:123 */     byte[] encoded = Base64.encode(out.toByteArray());
/* 127:124 */     return "data:image/png;base64," + new String(encoded, "UTF-8");
/* 128:    */   }
/* 129:    */   
/* 130:    */   public boolean isPriority()
/* 131:    */   {
/* 132:129 */     return false;
/* 133:    */   }
/* 134:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.status.server.StatusResponsePacket
 * JD-Core Version:    0.7.0.1
 */