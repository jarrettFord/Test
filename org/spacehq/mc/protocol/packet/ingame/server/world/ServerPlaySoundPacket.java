/*  1:   */ package org.spacehq.mc.protocol.packet.ingame.server.world;
/*  2:   */ 
/*  3:   */ import java.io.IOException;
/*  4:   */ import org.spacehq.packetlib.io.NetInput;
/*  5:   */ import org.spacehq.packetlib.io.NetOutput;
/*  6:   */ import org.spacehq.packetlib.packet.Packet;
/*  7:   */ 
/*  8:   */ public class ServerPlaySoundPacket
/*  9:   */   implements Packet
/* 10:   */ {
/* 11:   */   private String sound;
/* 12:   */   private double x;
/* 13:   */   private double y;
/* 14:   */   private double z;
/* 15:   */   private float volume;
/* 16:   */   private float pitch;
/* 17:   */   
/* 18:   */   private ServerPlaySoundPacket() {}
/* 19:   */   
/* 20:   */   public ServerPlaySoundPacket(String sound, double x, double y, double z, float volume, float pitch)
/* 21:   */   {
/* 22:23 */     this.sound = sound;
/* 23:24 */     this.x = x;
/* 24:25 */     this.y = y;
/* 25:26 */     this.z = z;
/* 26:27 */     this.volume = volume;
/* 27:28 */     this.pitch = pitch;
/* 28:   */   }
/* 29:   */   
/* 30:   */   public String getSound()
/* 31:   */   {
/* 32:32 */     return this.sound;
/* 33:   */   }
/* 34:   */   
/* 35:   */   public double getX()
/* 36:   */   {
/* 37:36 */     return this.x;
/* 38:   */   }
/* 39:   */   
/* 40:   */   public double getY()
/* 41:   */   {
/* 42:40 */     return this.y;
/* 43:   */   }
/* 44:   */   
/* 45:   */   public double getZ()
/* 46:   */   {
/* 47:44 */     return this.z;
/* 48:   */   }
/* 49:   */   
/* 50:   */   public float getVolume()
/* 51:   */   {
/* 52:48 */     return this.volume;
/* 53:   */   }
/* 54:   */   
/* 55:   */   public float getPitch()
/* 56:   */   {
/* 57:52 */     return this.pitch;
/* 58:   */   }
/* 59:   */   
/* 60:   */   public void read(NetInput in)
/* 61:   */     throws IOException
/* 62:   */   {
/* 63:57 */     this.sound = in.readString();
/* 64:58 */     this.x = (in.readInt() / 8.0D);
/* 65:59 */     this.y = (in.readInt() / 8.0D);
/* 66:60 */     this.z = (in.readInt() / 8.0D);
/* 67:61 */     this.volume = in.readFloat();
/* 68:62 */     this.pitch = (in.readUnsignedByte() / 63.0F);
/* 69:   */   }
/* 70:   */   
/* 71:   */   public void write(NetOutput out)
/* 72:   */     throws IOException
/* 73:   */   {
/* 74:67 */     out.writeString(this.sound);
/* 75:68 */     out.writeInt((int)(this.x * 8.0D));
/* 76:69 */     out.writeInt((int)(this.y * 8.0D));
/* 77:70 */     out.writeInt((int)(this.z * 8.0D));
/* 78:71 */     out.writeFloat(this.volume);
/* 79:72 */     int pitch = (int)(this.pitch * 63.0F);
/* 80:73 */     if (pitch > 255) {
/* 81:74 */       pitch = 255;
/* 82:   */     }
/* 83:77 */     if (pitch < 0) {
/* 84:78 */       pitch = 0;
/* 85:   */     }
/* 86:81 */     out.writeByte(pitch);
/* 87:   */   }
/* 88:   */   
/* 89:   */   public boolean isPriority()
/* 90:   */   {
/* 91:86 */     return false;
/* 92:   */   }
/* 93:   */   
/* 94:   */   public static class Sound
/* 95:   */   {
/* 96:   */     public static final String CLICK = "random.click";
/* 97:   */     public static final String FIZZ = "random.fizz";
/* 98:   */     public static final String FIRE_AMBIENT = "fire.fire";
/* 99:   */     public static final String IGNITE_FIRE = "fire.ignite";
/* :0:   */     public static final String WATER_AMBIENT = "liquid.water";
/* :1:   */     public static final String LAVA_AMBIENT = "liquid.lava";
/* :2:   */     public static final String LAVA_POP = "liquid.lavapop";
/* :3:   */     public static final String HARP = "note.harp";
/* :4:   */     public static final String BASS_DRUM = "note.bd";
/* :5:   */     public static final String SNARE_DRUM = "note.snare";
/* :6:   */     public static final String HI_HAT = "note.hat";
/* :7:   */     public static final String DOUBLE_BASS = "note.bassattack";
/* :8:   */     public static final String PISTON_EXTEND = "tile.piston.out";
/* :9:   */     public static final String PISTON_RETRACT = "tile.piston.in";
/* ;0:   */     public static final String PORTAL_AMBIENT = "portal.portal";
/* ;1:   */     public static final String TNT_PRIMED = "game.tnt.primed";
/* ;2:   */     public static final String BOW_HIT = "random.bowhit";
/* ;3:   */     public static final String COLLECT_ITEM = "random.pop";
/* ;4:   */     public static final String COLLECT_EXP = "random.orb";
/* ;5:   */     public static final String SUCCESSFUL_HIT = "random.successful_hit";
/* ;6:   */     public static final String FIREWORK_BLAST = "fireworks.blast";
/* ;7:   */     public static final String FIREWORK_LARGE_BLAST = "fireworks.largeBlast";
/* ;8:   */     public static final String FIREWORK_FAR_BLAST = "fireworks.blast_far";
/* ;9:   */     public static final String FIREWORK_FAR_LARGE_BLAST = "fireworks.largeBlast_far";
/* <0:   */     public static final String FIREWORK_TWINKLE = "fireworks.twinkle";
/* <1:   */     public static final String FIREWORK_FAR_TWINKLE = "fireworks.twinkle_far";
/* <2:   */     public static final String RAIN_AMBIENT = "ambient.weather.rain";
/* <3:   */     public static final String WITHER_SPAWN = "mob.wither.spawn";
/* <4:   */     public static final String ENDER_DRAGON_DEATH = "mob.enderdragon.end";
/* <5:   */     public static final String FIRE_PROJECTILE = "random.bow";
/* <6:   */     public static final String DOOR_OPEN = "random.door_open";
/* <7:   */     public static final String DOOR_CLOSE = "random.door_close";
/* <8:   */     public static final String GHAST_CHARGE = "mob.ghast.charge";
/* <9:   */     public static final String GHAST_FIRE = "mob.ghast.fireball";
/* =0:   */     public static final String POUND_WOODEN_DOOR = "mob.zombie.wood";
/* =1:   */     public static final String POUND_METAL_DOOR = "mob.zombie.metal";
/* =2:   */     public static final String BREAK_WOODEN_DOOR = "mob.zombie.woodbreak";
/* =3:   */     public static final String WITHER_SHOOT = "mob.wither.shoot";
/* =4:   */     public static final String BAT_TAKE_OFF = "mob.bat.takeoff";
/* =5:   */     public static final String INFECT_VILLAGER = "mob.zombie.infect";
/* =6:   */     public static final String DISINFECT_VILLAGER = "mob.zombie.unfect";
/* =7:   */     public static final String ANVIL_BREAK = "random.anvil_break";
/* =8:   */     public static final String ANVIL_USE = "random.anvil_use";
/* =9:   */     public static final String ANVIL_LAND = "random.anvil_land";
/* >0:   */     public static final String BREAK_SPLASH_POTION = "game.potion.smash";
/* >1:   */     public static final String THORNS_DAMAGE = "damage.thorns";
/* >2:   */     public static final String EXPLOSION = "random.explode";
/* >3:   */     public static final String CAVE_AMBIENT = "ambient.cave.cave";
/* >4:   */     public static final String OPEN_CHEST = "random.chestopen";
/* >5:   */     public static final String CLOSE_CHEST = "random.chestclosed";
/* >6:   */     public static final String DIG_STONE = "dig.stone";
/* >7:   */     public static final String DIG_WOOD = "dig.wood";
/* >8:   */     public static final String DIG_GRAVEL = "dig.gravel";
/* >9:   */     public static final String DIG_GRASS = "dig.grass";
/* ?0:   */     public static final String DIG_CLOTH = "dig.cloth";
/* ?1:   */     public static final String DIG_SAND = "dig.sand";
/* ?2:   */     public static final String DIG_SNOW = "dig.snow";
/* ?3:   */     public static final String DIG_GLASS = "dig.glass";
/* ?4:   */     public static final String ANVIL_STEP = "step.anvil";
/* ?5:   */     public static final String LADDER_STEP = "step.ladder";
/* ?6:   */     public static final String STONE_STEP = "step.stone";
/* ?7:   */     public static final String WOOD_STEP = "step.wood";
/* ?8:   */     public static final String GRAVEL_STEP = "step.gravel";
/* ?9:   */     public static final String GRASS_STEP = "step.grass";
/* @0:   */     public static final String CLOTH_STEP = "step.cloth";
/* @1:   */     public static final String SAND_STEP = "step.sand";
/* @2:   */     public static final String SNOW_STEP = "step.snow";
/* @3:   */     public static final String BURP = "random.burp";
/* @4:   */     public static final String SADDLE_HORSE = "mob.horse.leather";
/* @5:   */     public static final String ENDER_DRAGON_FLAP_WINGS = "mob.enderdragon.wings";
/* @6:   */     public static final String THUNDER_AMBIENT = "ambient.weather.thunder";
/* @7:   */     public static final String LAUNCH_FIREWORKS = "fireworks.launch";
/* @8:   */     public static final String CREEPER_PRIMED = "creeper.primed";
/* @9:   */     public static final String ENDERMAN_STARE = "mob.endermen.stare";
/* A0:   */     public static final String ENDERMAN_TELEPORT = "mob.endermen.portal";
/* A1:   */     public static final String IRON_GOLEM_THROW = "mob.irongolem.throw";
/* A2:   */     public static final String IRON_GOLEM_WALK = "mob.irongolem.walk";
/* A3:   */     public static final String ZOMBIE_PIGMAN_ANGRY = "mob.zombiepig.zpigangry";
/* A4:   */     public static final String SILVERFISH_STEP = "mob.silverfish.step";
/* A5:   */     public static final String SKELETON_STEP = "mob.skeleton.step";
/* A6:   */     public static final String SPIDER_STEP = "mob.spider.step";
/* A7:   */     public static final String ZOMBIE_STEP = "mob.zombie.step";
/* A8:   */     public static final String ZOMBIE_CURE = "mob.zombie.remedy";
/* A9:   */     public static final String CHICKEN_LAY_EGG = "mob.chicken.plop";
/* B0:   */     public static final String CHICKEN_STEP = "mob.chicken.step";
/* B1:   */     public static final String COW_STEP = "mob.cow.step";
/* B2:   */     public static final String HORSE_EATING = "eating";
/* B3:   */     public static final String HORSE_LAND = "mob.horse.land";
/* B4:   */     public static final String HORSE_WEAR_ARMOR = "mob.horse.armor";
/* B5:   */     public static final String HORSE_GALLOP = "mob.horse.gallop";
/* B6:   */     public static final String HORSE_BREATHE = "mob.horse.breathe";
/* B7:   */     public static final String HORSE_WOOD_STEP = "mob.horse.wood";
/* B8:   */     public static final String HORSE_SOFT_STEP = "mob.horse.soft";
/* B9:   */     public static final String HORSE_JUMP = "mob.horse.jump";
/* C0:   */     public static final String SHEAR_SHEEP = "mob.sheep.shear";
/* C1:   */     public static final String PIG_STEP = "mob.pig.step";
/* C2:   */     public static final String SHEEP_STEP = "mob.sheep.step";
/* C3:   */     public static final String VILLAGER_YES = "mob.villager.yes";
/* C4:   */     public static final String VILLAGER_NO = "mob.villager.no";
/* C5:   */     public static final String WOLF_STEP = "mob.wolf.step";
/* C6:   */     public static final String WOLF_SHAKE = "mob.wolf.shake";
/* C7:   */     public static final String DRINK = "random.drink";
/* C8:   */     public static final String EAT = "random.eat";
/* C9:   */     public static final String LEVEL_UP = "random.levelup";
/* D0:   */     public static final String FISH_HOOK_SPLASH = "random.splash";
/* D1:   */     public static final String ITEM_BREAK = "random.break";
/* D2:   */     public static final String SWIM = "game.neutral.swim";
/* D3:   */     public static final String SPLASH = "game.neutral.swim.splash";
/* D4:   */     public static final String HURT = "game.neutral.hurt";
/* D5:   */     public static final String DEATH = "game.neutral.die";
/* D6:   */     public static final String BIG_FALL = "game.neutral.hurt.fall.big";
/* D7:   */     public static final String SMALL_FALL = "game.neutral.hurt.fall.small";
/* D8:   */     public static final String MOB_SWIM = "game.hostile.swim";
/* D9:   */     public static final String MOB_SPLASH = "game.hostile.swim.splash";
/* E0:   */     public static final String PLAYER_SWIM = "game.player.swim";
/* E1:   */     public static final String PLAYER_SPLASH = "game.player.swim.splash";
/* E2:   */     public static final String ENDER_DRAGON_GROWL = "mob.enderdragon.growl";
/* E3:   */     public static final String WITHER_IDLE = "mob.wither.idle";
/* E4:   */     public static final String BLAZE_BREATHE = "mob.blaze.breathe";
/* E5:   */     public static final String ENDERMAN_SCREAM = "mob.endermen.scream";
/* E6:   */     public static final String ENDERMAN_IDLE = "mob.endermen.idle";
/* E7:   */     public static final String GHAST_MOAN = "mob.ghast.moan";
/* E8:   */     public static final String ZOMBIE_PIGMAN_IDLE = "mob.zombiepig.zpig";
/* E9:   */     public static final String SILVERFISH_IDLE = "mob.silverfish.say";
/* F0:   */     public static final String SKELETON_IDLE = "mob.skeleton.say";
/* F1:   */     public static final String SPIDER_IDLE = "mob.spider.say";
/* F2:   */     public static final String WITCH_IDLE = "mob.witch.idle";
/* F3:   */     public static final String ZOMBIE_IDLE = "mob.zombie.say";
/* F4:   */     public static final String BAT_IDLE = "mob.bat.idle";
/* F5:   */     public static final String CHICKEN_IDLE = "mob.chicken.say";
/* F6:   */     public static final String COW_IDLE = "mob.cow.say";
/* F7:   */     public static final String HORSE_IDLE = "mob.horse.idle";
/* F8:   */     public static final String DONKEY_IDLE = "mob.horse.donkey.idle";
/* F9:   */     public static final String ZOMBIE_HORSE_IDLE = "mob.horse.zombie.idle";
/* G0:   */     public static final String SKELETON_HORSE_IDLE = "mob.horse.skeleton.idle";
/* G1:   */     public static final String OCELOT_PURR = "mob.cat.purr";
/* G2:   */     public static final String OCELOT_PURR_MEOW = "mob.cat.purreow";
/* G3:   */     public static final String OCELOT_MEOW = "mob.cat.meow";
/* G4:   */     public static final String PIG_IDLE = "mob.pig.say";
/* G5:   */     public static final String SHEEP_IDLE = "mob.sheep.say";
/* G6:   */     public static final String VILLAGER_HAGGLE = "mob.villager.haggle";
/* G7:   */     public static final String VILLAGER_IDLE = "mob.villager.idle";
/* G8:   */     public static final String WOLF_GROWL = "mob.wolf.growl";
/* G9:   */     public static final String WOLF_PANT = "mob.wolf.panting";
/* H0:   */     public static final String WOLF_WHINE = "mob.wolf.whine";
/* H1:   */     public static final String WOLF_BARK = "mob.wolf.bark";
/* H2:   */     public static final String MOB_BIG_FALL = "game.hostile.hurt.fall.big";
/* H3:   */     public static final String MOB_SMALL_FALL = "game.hostile.hurt.fall.small";
/* H4:   */     public static final String PLAYER_BIG_FALL = "game.player.hurt.fall.big";
/* H5:   */     public static final String PLAYER_SMALL_FALL = "game.player.hurt.fall.small";
/* H6:   */     public static final String ENDER_DRAGON_HURT = "mob.enderdragon.hit";
/* H7:   */     public static final String WITHER_HURT = "mob.wither.hurt";
/* H8:   */     public static final String WITHER_DEATH = "mob.wither.death";
/* H9:   */     public static final String BLAZE_HURT = "mob.blaze.hit";
/* I0:   */     public static final String BLAZE_DEATH = "mob.blaze.death";
/* I1:   */     public static final String CREEPER_HURT = "mob.creeper.say";
/* I2:   */     public static final String CREEPER_DEATH = "mob.creeper.death";
/* I3:   */     public static final String ENDERMAN_HURT = "mob.endermen.hit";
/* I4:   */     public static final String ENDERMAN_DEATH = "mob.endermen.death";
/* I5:   */     public static final String GHAST_HURT = "mob.ghast.scream";
/* I6:   */     public static final String GHAST_DEATH = "mob.ghast.death";
/* I7:   */     public static final String IRON_GOLEM_HURT = "mob.irongolem.hit";
/* I8:   */     public static final String IRON_GOLEM_DEATH = "mob.irongolem.death";
/* I9:   */     public static final String MOB_HURT = "game.hostile.hurt";
/* J0:   */     public static final String MOB_DEATH = "game.hostile.die";
/* J1:   */     public static final String ZOMBIE_PIGMAN_HURT = "mob.zombiepig.zpighurt";
/* J2:   */     public static final String ZOMBIE_PIGMAN_DEATH = "mob.zombiepig.zpigdeath";
/* J3:   */     public static final String SILVERFISH_HURT = "mob.silverfish.hit";
/* J4:   */     public static final String SILVERFISH_DEATH = "mob.silverfish.kill";
/* J5:   */     public static final String SKELETON_HURT = "mob.skeleton.hurt";
/* J6:   */     public static final String SKELETON_DEATH = "mob.skeleton.death";
/* J7:   */     public static final String SLIME = "mob.slime.small";
/* J8:   */     public static final String BIG_SLIME = "mob.slime.big";
/* J9:   */     public static final String SPIDER_DEATH = "mob.spider.death";
/* K0:   */     public static final String WITCH_HURT = "mob.witch.hurt";
/* K1:   */     public static final String WITCH_DEATH = "mob.witch.death";
/* K2:   */     public static final String ZOMBIE_HURT = "mob.zombie.hurt";
/* K3:   */     public static final String ZOMBIE_DEATH = "mob.zombie.death";
/* K4:   */     public static final String PLAYER_HURT = "game.player.hurt";
/* K5:   */     public static final String PLAYER_DEATH = "game.player.die";
/* K6:   */     public static final String WOLF_HURT = "mob.wolf.hurt";
/* K7:   */     public static final String WOLF_DEATH = "mob.wolf.death";
/* K8:   */     public static final String VILLAGER_HURT = "mob.villager.hit";
/* K9:   */     public static final String VILLAGER_DEATH = "mob.villager.death";
/* L0:   */     public static final String PIG_DEATH = "mob.pig.death";
/* L1:   */     public static final String OCELOT_HURT = "mob.cat.hitt";
/* L2:   */     public static final String HORSE_HURT = "mob.horse.hit";
/* L3:   */     public static final String DONKEY_HURT = "mob.horse.donkey.hit";
/* L4:   */     public static final String ZOMBIE_HORSE_HURT = "mob.horse.zombie.hit";
/* L5:   */     public static final String SKELETON_HORSE_HURT = "mob.horse.skeleton.hit";
/* L6:   */     public static final String HORSE_DEATH = "mob.horse.death";
/* L7:   */     public static final String DONKEY_DEATH = "mob.horse.donkey.death";
/* L8:   */     public static final String ZOMBIE_HORSE_DEATH = "mob.horse.zombie.death";
/* L9:   */     public static final String SKELETON_HORSE_DEATH = "mob.horse.skeleton.death";
/* M0:   */     public static final String COW_HURT = "mob.cow.hurt";
/* M1:   */     public static final String CHICKEN_HURT = "mob.chicken.hurt";
/* M2:   */     public static final String BAT_HURT = "mob.bat.hurt";
/* M3:   */     public static final String BAT_DEATH = "mob.bat.death";
/* M4:   */     public static final String MOB_ATTACK = "mob.attack";
/* M5:   */   }
/* M6:   */ }


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.packet.ingame.server.world.ServerPlaySoundPacket
 * JD-Core Version:    0.7.0.1
 */