/*   1:    */ package org.spacehq.mc.protocol.packet.ingame.server;
/*   2:    */ 
/*   3:    */ import java.io.IOException;
/*   4:    */ import java.util.HashMap;
/*   5:    */ import java.util.Map;
/*   6:    */ import org.spacehq.packetlib.io.NetInput;
/*   7:    */ import org.spacehq.packetlib.io.NetOutput;
/*   8:    */ import org.spacehq.packetlib.packet.Packet;
/*   9:    */ 
/*  10:    */ public class ServerStatisticsPacket
/*  11:    */   implements Packet
/*  12:    */ {
/*  13: 13 */   private Map<String, Integer> statistics = new HashMap();
/*  14:    */   
/*  15:    */   private ServerStatisticsPacket() {}
/*  16:    */   
/*  17:    */   public ServerStatisticsPacket(Map<String, Integer> statistics)
/*  18:    */   {
/*  19: 20 */     this.statistics = statistics;
/*  20:    */   }
/*  21:    */   
/*  22:    */   public Map<String, Integer> getStatistics()
/*  23:    */   {
/*  24: 24 */     return this.statistics;
/*  25:    */   }
/*  26:    */   
/*  27:    */   public void read(NetInput in)
/*  28:    */     throws IOException
/*  29:    */   {
/*  30: 29 */     int length = in.readVarInt();
/*  31: 30 */     for (int index = 0; index < length; index++) {
/*  32: 31 */       this.statistics.put(in.readString(), Integer.valueOf(in.readVarInt()));
/*  33:    */     }
/*  34:    */   }
/*  35:    */   
/*  36:    */   public void write(NetOutput out)
/*  37:    */     throws IOException
/*  38:    */   {
/*  39: 37 */     out.writeVarInt(this.statistics.size());
/*  40: 38 */     for (String statistic : this.statistics.keySet())
/*  41:    */     {
/*  42: 39 */       out.writeString(statistic);
/*  43: 40 */       out.writeVarInt(((Integer)this.statistics.get(statistic)).intValue());
/*  44:    */     }
/*  45:    */   }
/*  46:    */   
/*  47:    */   public boolean isPriority()
/*  48:    */   {
/*  49: 46 */     return false;
/*  50:    */   }
/*  51:    */   
/*  52:    */   public static class Achievement
/*  53:    */   {
/*  54:    */     public static final String OPEN_INVENTORY = "achievement.openInventory";
/*  55:    */     public static final String GET_WOOD = "achievement.mineWood";
/*  56:    */     public static final String MAKE_WORKBENCH = "achievement.buildWorkBench";
/*  57:    */     public static final String MAKE_PICKAXE = "achievement.buildPickaxe";
/*  58:    */     public static final String MAKE_FURNACE = "achievement.buildFurnace";
/*  59:    */     public static final String GET_IRON = "achievement.acquireIron";
/*  60:    */     public static final String MAKE_HOE = "achievement.buildHoe";
/*  61:    */     public static final String MAKE_BREAD = "achievement.makeBread";
/*  62:    */     public static final String MAKE_CAKE = "achievement.bakeCake";
/*  63:    */     public static final String MAKE_IRON_PICKAXE = "achievement.buildBetterPickaxe";
/*  64:    */     public static final String COOK_FISH = "achievement.cookFish";
/*  65:    */     public static final String RIDE_MINECART_1000_BLOCKS = "achievement.onARail";
/*  66:    */     public static final String MAKE_SWORD = "achievement.buildSword";
/*  67:    */     public static final String KILL_ENEMY = "achievement.killEnemy";
/*  68:    */     public static final String KILL_COW = "achievement.killCow";
/*  69:    */     public static final String FLY_PIG = "achievement.flyPig";
/*  70:    */     public static final String SNIPE_SKELETON = "achievement.snipeSkeleton";
/*  71:    */     public static final String GET_DIAMONDS = "achievement.diamonds";
/*  72:    */     public static final String GIVE_DIAMONDS = "achievement.diamondsToYou";
/*  73:    */     public static final String ENTER_PORTAL = "achievement.portal";
/*  74:    */     public static final String ATTACKED_BY_GHAST = "achievement.ghast";
/*  75:    */     public static final String GET_BLAZE_ROD = "achievement.blazeRod";
/*  76:    */     public static final String MAKE_POTION = "achievement.potion";
/*  77:    */     public static final String GO_TO_THE_END = "achievement.theEnd";
/*  78:    */     public static final String DEFEAT_ENDER_DRAGON = "achievement.theEnd2";
/*  79:    */     public static final String DEAL_18_OR_MORE_DAMAGE = "achievement.overkill";
/*  80:    */     public static final String MAKE_BOOKCASE = "achievement.bookcase";
/*  81:    */     public static final String BREED_COW = "achievement.breedCow";
/*  82:    */     public static final String SPAWN_WITHER = "achievement.spawnWither";
/*  83:    */     public static final String KILL_WITHER = "achievement.killWither";
/*  84:    */     public static final String MAKE_FULL_BEACON = "achievement.fullBeacon";
/*  85:    */     public static final String EXPLORE_ALL_BIOMES = "achievement.exploreAllBiomes";
/*  86:    */   }
/*  87:    */   
/*  88:    */   public static class Statistic
/*  89:    */   {
/*  90:    */     public static final String TIMES_LEFT_GAME = "stat.leaveGame";
/*  91:    */     public static final String MINUTES_PLAYED = "stat.playOneMinute";
/*  92:    */     public static final String BLOCKS_WALKED = "stat.walkOneCm";
/*  93:    */     public static final String BLOCKS_SWAM = "stat.swimOneCm";
/*  94:    */     public static final String BLOCKS_FALLEN = "stat.fallOneCm";
/*  95:    */     public static final String BLOCKS_CLIMBED = "stat.climbOneCm";
/*  96:    */     public static final String BLOCKS_FLOWN = "stat.flyOneCm";
/*  97:    */     public static final String BLOCKS_DOVE = "stat.diveOneCm";
/*  98:    */     public static final String BLOCKS_TRAVELLED_IN_MINECART = "stat.minecartOneCm";
/*  99:    */     public static final String BLOCKS_TRAVELLED_IN_BOAT = "stat.boatOneCm";
/* 100:    */     public static final String BLOCKS_RODE_ON_PIG = "stat.pigOneCm";
/* 101:    */     public static final String BLOCKS_RODE_ON_HORSE = "stat.horseOneCm";
/* 102:    */     public static final String TIMES_JUMPED = "stat.jump";
/* 103:    */     public static final String TIMES_DROPPED_ITEMS = "stat.drop";
/* 104:    */     public static final String TIMES_DEALT_DAMAGE = "stat.damageDealt";
/* 105:    */     public static final String DAMAGE_TAKEN = "stat.damageTaken";
/* 106:    */     public static final String DEATHS = "stat.deaths";
/* 107:    */     public static final String MOB_KILLS = "stat.mobKills";
/* 108:    */     public static final String ANIMALS_BRED = "stat.animalsBred";
/* 109:    */     public static final String PLAYERS_KILLED = "stat.playerKills";
/* 110:    */     public static final String FISH_CAUGHT = "stat.fishCaught";
/* 111:    */     public static final String JUNK_FISHED = "stat.junkFished";
/* 112:    */     public static final String TREASURE_FISHED = "stat.treasureFished";
/* 113:    */     private static final String CRAFT_ITEM_BASE = "stat.craftItem.";
/* 114:    */     private static final String BREAK_BLOCK_BASE = "stat.mineBlock.";
/* 115:    */     private static final String USE_ITEM_BASE = "stat.useItem.";
/* 116:    */     private static final String BREAK_ITEM_BASE = "stat.breakItem.";
/* 117:    */     
/* 118:    */     public static final String CRAFT_ITEM(int id)
/* 119:    */     {
/* 120:115 */       return "stat.craftItem." + id;
/* 121:    */     }
/* 122:    */     
/* 123:    */     public static final String BREAK_BLOCK(int id)
/* 124:    */     {
/* 125:119 */       return "stat.mineBlock." + id;
/* 126:    */     }
/* 127:    */     
/* 128:    */     public static final String USE_ITEM(int id)
/* 129:    */     {
/* 130:123 */       return "stat.useItem." + id;
/* 131:    */     }
/* 132:    */     
/* 133:    */     public static final String BREAK_ITEM(int id)
/* 134:    */     {
/* 135:127 */       return "stat.breakItem." + id;
/* 136:    */     }
/* 137:    */   }
/* 138:    */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.ServerStatisticsPacket
 * JD-Core Version:    0.7.0.1
 */