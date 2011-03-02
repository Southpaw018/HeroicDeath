package com.herocraftonline.squallseed31.heroicdeath;

import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player; 
import org.bukkit.entity.Entity;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageByProjectileEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener; 
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class HeroicDeathListener extends EntityListener { 
 private final HeroicDeath plugin; 
 protected Map<String, DeathCertificate> deathRecords = new HashMap<String, DeathCertificate>();

 private Random RN = new Random();
 
 public HeroicDeathListener(HeroicDeath instance) { 
	 plugin = instance; 
 }

 public String getMessage(ArrayList<String> ArrayString, DeathCertificate dc)
 {
	 String attackerName = dc.getAttacker();
	 String murderWeapon = null;
	 if (dc.getMurderWeapon() != null)
		 murderWeapon = HeroicDeath.Items.getItem(dc.getMurderWeapon());
	 if (ArrayString.size() == 0)
	     return dc.getDefender() + " died";
   if (ArrayString.size() > 1)
   {
     int num = this.RN.nextInt(ArrayString.size());
     String temp = (String)ArrayString.get(num);
     
     String temp2 = temp.replaceAll("%d", HeroicDeath.nameColor + dc.getDefender() + HeroicDeath.messageColor);
     String temp3 = temp2.replaceAll("%a", HeroicDeath.nameColor + attackerName + HeroicDeath.messageColor);
     temp = temp3.replaceAll("%i", HeroicDeath.itemColor + murderWeapon + HeroicDeath.messageColor);

     return temp;
   }

   String temp = (String)ArrayString.get(0);

   String temp2 = temp.replaceAll("%d", HeroicDeath.nameColor + dc.getDefender() + HeroicDeath.messageColor);
   String temp3 = temp2.replaceAll("%a", HeroicDeath.nameColor + attackerName + HeroicDeath.messageColor);
   temp = temp3.replaceAll("%i", HeroicDeath.itemColor + murderWeapon + HeroicDeath.messageColor);
   return temp;
 }
 
 public MaterialData getMurderWeapon(Player Attacker) {
	 ItemStack item = Attacker.getItemInHand();
	 int typeID = item.getTypeId();
	 Short mData = item.getDurability();
	 Byte matData = 0;
	 if (mData < 256)
		 matData = mData.byteValue();
	 MaterialData mItem = new MaterialData(typeID, matData);
	 return mItem;
 }
 
 public String getAttackerName(Entity damager) {
	 String attackerName = "Unknown";
	 if (damager instanceof Player) {
		 Player attacker = (Player)damager;
		 attackerName = attacker.getDisplayName();
	 } else if (damager instanceof PigZombie) {
		 attackerName = "Pig Zombie";
	 } else if (damager instanceof Giant) {
		 attackerName = "Giant";
	 } else if (damager instanceof Zombie) {
		 attackerName = "Zombie";
	 } else if (damager instanceof Skeleton) {
		 attackerName = "Skeleton";
	 } else if (damager instanceof Spider) {
		 attackerName = "Spider";
	 } else if (damager instanceof Creeper) {
		 attackerName = "Creeper";
	 } else if (damager instanceof Ghast) {
		 attackerName = "Ghast";
	 } else if (damager instanceof Slime) {
		 attackerName = "Slime";
	 } else {
		 attackerName = "Monster";
	 }
	 return attackerName;
 }
 
 public void onEntityDeath(EntityDeathEvent event) {
	 Player player;
	 if (!(event.getEntity() instanceof Player)) {
		 return;
	 } else {
		 try {
			 player = (Player)event.getEntity();
		 } catch (ClassCastException e)
		 {
			 HeroicDeath.log.severe("Cannot cast entity as player: " + e);
			 return;
		 }
	 }
	 String name = player.getName();
	 DeathCertificate dc = deathRecords.remove(name);
	 if (dc == null)
		 dc = new DeathCertificate(player);
	 String killString = dc.getMessage();
	 if (killString == null) {
		 killString = getMessage(HeroicDeath.DeathMessages.OtherMessages, dc);
	 	 dc.setMessage(killString);
	 }
	 if(!plugin.getEventsOnly()){
		 plugin.getServer().broadcastMessage(HeroicDeath.messageColor + killString + " ");
	 }
	 HeroicDeath.log.info(killString.replaceAll("(?i)\u00A7[0-F]", ""));
	 plugin.recordDeath(dc);
 }
 
 public void onEntityDamage(EntityDamageEvent event)
 {
	 if (event.isCancelled()) {
		 return;
	 }
	 Player player;
	 if (!(event.getEntity() instanceof Player)) {
		 return;
	 } else {
		 try {
			 player = (Player)event.getEntity();
		 } catch (ClassCastException e)
		 {
			 HeroicDeath.log.severe("Cannot cast entity as player: " + e);
			 return;
		 }
	 }
	 String name = player.getName();
	 int damage = event.getDamage();
	 int oldHealth = player.getHealth();
	 int newHealth = oldHealth - damage;
	 HeroicDeath.debug("Player damaged: " + name + " [" + oldHealth + "-" + damage + "=" + newHealth + "] Cause: " + event.getCause().toString());	 
	 if (newHealth <= 0) {
		 String killString = name + " died.";
		 DeathCertificate dc = new DeathCertificate(player, event.getCause());
		 Entity damager = null;
		 Block damageBlock = null;
		 String blockName = null;
		 if (event instanceof EntityDamageByProjectileEvent) {
			 EntityDamageByProjectileEvent subEvent = (EntityDamageByProjectileEvent)event;
			 damager = subEvent.getDamager();
		 } else if (event instanceof EntityDamageByEntityEvent) {
			 EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent)event;
			 damager = subEvent.getDamager();
		 } else if (event instanceof EntityDamageByBlockEvent) {
			 EntityDamageByBlockEvent subEvent = (EntityDamageByBlockEvent)event;
			 damageBlock = subEvent.getDamager();
			 try {
				 blockName = damageBlock.getType().toString();
			 } catch (NullPointerException e) {
				 blockName = "Unknown";
			 }
		 }
		 switch (event.getCause()) {
		 	case ENTITY_EXPLOSION:
		 		killString = getMessage(HeroicDeath.DeathMessages.CreeperExplosionMessages, dc);
		 		break;
		 	case ENTITY_ATTACK:
		 		 if (damager == null) {
					 dc.setAttacker("Dispenser");
					 killString = getMessage(HeroicDeath.DeathMessages.DispenserMessages, dc);
				 } else if (damager instanceof Player) {
					 Player attacker = (Player)damager;
					 dc.setAttacker(attacker.getDisplayName());
					 dc.setMurderWeapon(getMurderWeapon(attacker));
					 killString = getMessage(HeroicDeath.DeathMessages.PVPMessages, dc);
				 } else {
					 dc.setAttacker(getAttackerName(damager));
					 if (dc.getAttacker().equalsIgnoreCase("creeper"))
						 killString = getMessage(HeroicDeath.DeathMessages.CreeperExplosionMessages, dc);
					 else
						 killString = getMessage(HeroicDeath.DeathMessages.MonsterMessages, dc);
				 }
		 		break;
		 	case BLOCK_EXPLOSION:
		 		killString = getMessage(HeroicDeath.DeathMessages.ExplosionMessages, dc);
		 		break;
		 	case CONTACT:
				 dc.setAttacker(blockName);
				 if (blockName != "CACTUS")
					 HeroicDeath.log.info(name + "was damaged by non-cactus block: " + blockName);
				 killString = getMessage(HeroicDeath.DeathMessages.CactusMessages, dc);
				 break;
			case FALL:
				killString = getMessage(HeroicDeath.DeathMessages.FallMessages, dc);
				break;
			case FIRE_TICK:
			case FIRE:
			  killString = getMessage(HeroicDeath.DeathMessages.FireMessages, dc);
			  break;
			case DROWNING:
				killString = getMessage(HeroicDeath.DeathMessages.DrownMessages, dc);
				break;
			case LAVA:
				killString = getMessage(HeroicDeath.DeathMessages.LavaMessages, dc);
				break;
			case SUFFOCATION:
				killString = getMessage(HeroicDeath.DeathMessages.SuffocationMessages, dc);
				break;
			default:
			{
				killString = getMessage(HeroicDeath.DeathMessages.OtherMessages, dc);
			}
		 }

		 dc.setMessage(killString);
		 deathRecords.put(name, dc);
	 }
	 
   return;
 }
}