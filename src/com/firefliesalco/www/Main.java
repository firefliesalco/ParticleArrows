package com.firefliesalco.www;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin implements Listener {

	public ArrayList<Entity> arrows = new ArrayList<Entity>();


	public HashMap<Player, ItemStack> lastHeld = new HashMap<Player, ItemStack>();
	public HashMap<Player, Boolean> canThrow = new HashMap<Player, Boolean>();
	public HashMap<Player, ArmorStand> defusing = new HashMap<Player, ArmorStand>();
	public HashMap<Player, Integer> fireDelay = new HashMap<Player, Integer>();
	public HashMap<Player, Boolean> rightClicking = new HashMap<Player, Boolean>();
	private HashMap<String, Particle> particleNames = new HashMap<String, Particle>();
	
	private ArrayList<GunType> gunTypes = new ArrayList<GunType>();
	
	Main m;
	

	public void reloadGunTypes(){
		try {
			gunTypes.clear();
			
			File f = new File("plugins/ParticleArrows/gunTypes.txt");
			
			if(!f.exists())
				f.createNewFile();
			BufferedReader read = new BufferedReader(new FileReader(f));
			String s = read.readLine();
			GunType gun = null;
			while(s != null) {
				String[] split = s.split(" ");
				if(split[0].equals("Gun")){
					if(gun != null)
						gunTypes.add(gun);
					gun = new GunType(split[1]);
					gun.setBullets(Integer.parseInt(split[2]));
					gun.setClipSize(Integer.parseInt(split[3]));
					gun.setCriticalChance(Integer.parseInt(split[4]));
					gun.setDamage(Integer.parseInt(split[5]));
					gun.setFireRate(Integer.parseInt(split[6]));
					gun.setFiring("Automatic");
					gun.setM(Material.getMaterial(split[7]));
					gun.setOffset(Double.parseDouble(split[8]));
					gun.setRange(Integer.parseInt(split[9]));
					gun.setRarity(split[10]);
					gun.setReloadTime(Long.parseLong(split[11]));
					gun.setTrail(split[12]);
					gun.setType(split[13]);
				}else{
					gun.addExtra(s.contains("=")?s.split("=")[0]:s, s.contains("=")?s.split("=")[1]:null);
				}
				s = read.readLine();
			}
			if(gun != null)
				gunTypes.add(gun);
			read.close();
		}catch (Exception e){
			
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){

		if(sender instanceof Player){
			Player p = (Player) sender;
			if(label.equalsIgnoreCase("hat"))
				p.getInventory().setHelmet(p.getInventory().getItemInMainHand());
			if(label.equalsIgnoreCase("gun")){
				if(args.length == 1 && p.hasPermission("guns.reload") && args[0].equalsIgnoreCase("reload"))
					reloadGunTypes();
				if(args.length == 1 && p.hasPermission("guns.list") && args[0].equalsIgnoreCase("list")){
					p.sendMessage(ChatColor.BLUE + "-=-=+"+ChatColor.GRAY+"Gun Types"+ChatColor.BLUE + "+=-=-");
					for(GunType gt : gunTypes){
						p.sendMessage(ChatColor.GRAY + gt.name);
					}
				}
				if(args.length == 2 && p.hasPermission("guns.get") && args[0].equalsIgnoreCase("get")){
					GunType gun = findGunType(args[1]);
					if(gun != null)
						p.getInventory().addItem(gun.getGun());
				}
				if(args.length >= 2 && p.hasPermission("guns.rename")&&args[0].equalsIgnoreCase("rename")){
					if(p.getInventory().getItemInMainHand() != null && GunType.isGun(p.getInventory().getItemInMainHand())){
						String name = args[1];
						for(int i = 2; i < args.length; i++) name += " " + args[i];
						name = name.replace("&", "§");
						System.out.println(name);
						if(ChatColor.stripColor(name).length() <= 15){
							ItemStack is = p.getInventory().getItemInMainHand();
							ItemMeta im = is.getItemMeta();
							im.setDisplayName(name + GunType.color(GunType.getString(is, "Tier")) + " <" + GunType.getBullets(is) + ">");
							is.setItemMeta(im);
							p.sendMessage(ChatColor.GREEN + "Gun renamed to: " + name);
						}else{
							p.sendMessage(ChatColor.RED + "The name must be under 10 characters");
						}
						
					}else{
						p.sendMessage(ChatColor.RED + "You must be holding a gun to run this command!");
					}
				}
			}
		}

		return true;
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event){
		if(event.getEntity().getKiller() != null){
			ItemStack is = event.getEntity().getKiller().getInventory().getItemInMainHand();
			if(is != null && GunType.isGun(is))
				event.setDeathMessage(event.getEntity().getName() + " was killed by " + event.getEntity().getKiller().getName() + "'s " + is.getItemMeta().getDisplayName().substring(0, is.getItemMeta().getDisplayName().indexOf('<')-1));
		}
	}
	
	public GunType findGunType(String name){
		for(GunType gun : gunTypes){
			if(gun.name.equals(name))
				return gun;
		}
		return null;
	}
	
	
	@Override
	public void onEnable(){
		int currentExp = 100;
		int total = 0;
		for(int i = 1; i < 100; i++){
			System.out.print(i + 1 + ": " + currentExp);
			total += currentExp;

			currentExp = (int) (currentExp*(1.0 / Math.pow((double)i, 1.0/1.25) + 1.0));
		}
		System.out.print("Total: " + total);
		reloadGunTypes();
		particleNames.put("Flames", Particle.FLAME);
		particleNames.put("Love", Particle.HEART);
		particleNames.put("Dragon", Particle.DRAGON_BREATH);
		particleNames.put("Music", Particle.NOTE);
		particleNames.put("Dust", Particle.BLOCK_DUST);
		particleNames.put("Spell", Particle.SPELL_MOB);
		particleNames.put("Spellz", Particle.SPELL);
		m = this;
		getServer().getPluginManager().registerEvents(this, this);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
			public void run(){

				for(int i = 0; i < arrows.size(); i++){
					Entity arrow = arrows.get(i);
					if(arrow.getMetadata("lifetime").size() > 0 && arrow.getTicksLived() >= ((FixedMetadataValue)arrow.getMetadata("lifetime").get(0)).asInt()){
						arrow.remove();
					}
					if(arrow.isDead() == true || arrow == null){
						arrows.remove(arrow);
						if(arrow.getMetadata("Explosive").get(0).asInt() != -1){
							arrow.getWorld().createExplosion(arrow.getLocation().getX(), arrow.getLocation().getY(), arrow.getLocation().getZ(), arrow.getMetadata("Explosive").get(0).asInt(), false, false);
						}
					}
					if(arrow.getMetadata("particle").size() > 0)
						arrow.getWorld().spawnParticle(particleNames.get(arrow.getMetadata("particle").get(0).asString()), arrow.getLocation(), 1, 1, 1, 1);
					
				}
			}
		}, 0, 1);

		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
			public void run(){

				for(Player p : getServer().getOnlinePlayers()){
					if(!lastHeld.containsKey(p))
						lastHeld.put(p, p.getInventory().getItemInMainHand());
					if(lastHeld.get(p).getType() != p.getInventory().getItemInMainHand().getType() || p.getInventory().getItemInMainHand().getAmount() != lastHeld.get(p).getAmount()){
						p.getInventory().setHelmet(lastHeld.get(p));
						lastHeld.put(p, p.getInventory().getItemInMainHand());
					}

				}

			}
		}, 0, 20);

		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
			public void run(){
				for(Player p : getServer().getOnlinePlayers()){
					if(p.getInventory().getItemInMainHand().getDurability() > 0){
						p.getInventory().getItemInMainHand().setDurability((short) Math.max((short) (p.getInventory().getItemInMainHand().getDurability()-(p.getInventory().getItemInMainHand().getType().getMaxDurability()*5/GunType.getInt(p.getInventory().getItemInMainHand(), "Reload Time"))), 0));
						if(p.getInventory().getItemInMainHand().getDurability() == 0){
							GunType.setBullets(p.getInventory().getItemInMainHand(), GunType.getInt(p.getInventory().getItemInMainHand(), "Clip Size"));
						}
					}
					
				}
			}
		}, 0, 5);

		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
			public void run(){
				for(Player p : getServer().getOnlinePlayers()){
					if(p.isSneaking() && GunType.dataLoc(p.getInventory().getItemInMainHand(), "Zoom") != -1){
						p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 1, GunType.getInt(p.getInventory().getItemInMainHand(), "Zoom")-1));
					}
					if((rightClicking.containsKey(p) && rightClicking.get(p))&&(!fireDelay.containsKey(p) || fireDelay.get(p) == 0)){
						ItemStack is = p.getInventory().getItemInMainHand();
						if(GunType.getString(is, "Firing").equals("Automatic")){
							if(GunType.getBullets(is) > 0 && is.getDurability() == 0){
								Random r = new Random();
								for(int i = 0; i < GunType.getInt(is, "Bullets"); i++){
									fireDelay.put(p, GunType.getInt(is, "Reload Time"));
									double OFFSET = GunType.getDouble(is, "Offset");
									double multi1 = r.nextDouble()*OFFSET;
									double multi2 = r.nextDouble()*OFFSET;
									double multi3 = r.nextDouble()*OFFSET;
									Projectile e = (Projectile)p.getWorld().spawnEntity(p.getEyeLocation(), EntityType.SNOWBALL);
									e.setVelocity(p.getLocation().getDirection().multiply(5.0));
									e.setMetadata("Damage", new FixedMetadataValue(m, GunType.getDouble(is, "Damage")/5));
									e.setMetadata("lifetime", new FixedMetadataValue(m, GunType.getInt(is, "Range")));
									e.setMetadata("particle", new FixedMetadataValue(m, GunType.getString(is, "Particle")));
									e.setMetadata("Critical", new FixedMetadataValue(m, GunType.getInt(is, "Critical Chance")));
									e.setMetadata("Explosive", new FixedMetadataValue(m, GunType.getInt(is, "Explosive")));
									double one = e.getVelocity().getX();
									double two = e.getVelocity().getY();
									double three = e.getVelocity().getZ();
									double velocity = one + two + three;
									if(GunType.dataLoc(is, "Anti Gravity") != -1)
										e.setGravity(false);
									one *= (multi1 + 1) - OFFSET / 2;
									two *= (multi2 + 1) - OFFSET / 2;
									three *= (multi3 + 1) - OFFSET / 2;
									double vel2 = one + two + three;
									
									double change = vel2 / velocity;
									one /= change;
									two /= change;
									three /= change;
									e.setVelocity(new Vector(one, two, three));
									e.setShooter(p);
									
									getServer().getPluginManager().callEvent(new ProjectileLaunchEvent(e));
								}
								GunType.setBullets(is, GunType.getBullets(is)-1);
								fireDelay.put(p, GunType.getInt(is, "Fire Rate"));
							
						
							}else if(is != null && is.getDurability() == 0){
								is.setDurability(is.getType().getMaxDurability());
							}
						}
					}
				}
				for(Player p : fireDelay.keySet()){
					if(fireDelay.get(p) > 0){
						fireDelay.put(p, fireDelay.get(p)-1);
					}
				}
			}
		}, 0, 1);
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
			public void run(){
				for(Player p : getServer().getOnlinePlayers()){
					rightClicking.put(p, false);
				}
			}
		}, 2, 5);
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){

			@Override
			public void run() {
				
				for(Player p : getServer().getOnlinePlayers()){
					BossBar b = Bukkit.createBossBar(ChatColor.GREEN + "Health - " + Math.floor(p.getHealth()) * 5 + "/" + p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()*5, BarColor.GREEN, BarStyle.SOLID);
					b.setProgress(p.getHealth() / p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
					b.addPlayer(p);
					new BukkitRunnable(){
						public void run(){
							b.removePlayer(p);
						}
					}.runTaskLater(m, 20);
				}
				
			}
			
		}, 0, 20);



	}
	
	
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event){
		if(event.getItemDrop().getItemStack().getType() == Material.FLINT){
			checkCarpet(event.getPlayer(), event.getItemDrop());
		}
		if(event.getItemDrop().getItemStack().getType() == Material.COAL_BLOCK){
			event.getItemDrop().remove();
			event.getPlayer().sendMessage(ChatColor.RED + "Explosive Incoming");
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
				public void run(){
					FallingBlock fb = event.getItemDrop().getWorld().spawnFallingBlock(event.getItemDrop().getLocation().add(0, 50, 0), new MaterialData(Material.COAL_BLOCK));
					fb.setMetadata("NUKE", new FixedMetadataValue(m, "NUKE"));
				}
			}, 20);
		}
	}
	
	public void checkCarpet(Player p, Item item){
		new BukkitRunnable(){
			public void run(){
				if(item.isOnGround()){
					
					if(item.getItemStack().getAmount() > 1){
						item.getItemStack().setAmount(item.getItemStack().getAmount()-1);
						p.getInventory().addItem(item.getItemStack());
					}
					
					item.remove();
					delayedExplosion(item.getLocation(), 20f, 300);
				}else{
					checkCarpet(p, item);
				}
			}
		}.runTaskLater(this, 1);
	}

	@EventHandler
	public void inventoryClick(InventoryClickEvent event){
		if(event.getSlotType() == InventoryType.SlotType.ARMOR){
			System.out.println("Armor");
			event.setCancelled(true);
		}
		if(event.getInventory().getName().equals("Defuse Bomb")){
			event.setCancelled(true);
			if(event.getCurrentItem().getType() == Material.REDSTONE_BLOCK){
				if(new Random().nextInt(3)==0){
					event.getWhoClicked().sendMessage(ChatColor.GREEN + "Bomb Defused");
				}else{
					explode(defusing.get(event.getWhoClicked()), 20f);

				}
				event.getWhoClicked().closeInventory();
				defusing.get(event.getWhoClicked()).setHelmet(new ItemStack(Material.AIR));

				defusing.get(event.getWhoClicked()).setMetadata("defused", new FixedMetadataValue(this, true));

			}
				
		}
	}
	
	@EventHandler
	public void onArmorStandClick(PlayerInteractAtEntityEvent event){
		if(event.getRightClicked() instanceof ArmorStand){
			ArmorStand as = (ArmorStand)event.getRightClicked();
			if(as.getHelmet() != null && as.getHelmet().getType() == Material.CARPET){

				if(event.getPlayer().getInventory().getItemInMainHand() != null && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.CLAY_BRICK){
					
					defuseBomb(event.getPlayer(), as);	
				}else{
					event.getPlayer().sendMessage(event.getPlayer().getInventory().getItemInMainHand().getType().name());
				}
				event.setCancelled(true);
			}
		}
		
		
		
	}
	
	public void defuseBomb(Player p, ArmorStand as){
		Inventory inv = Bukkit.createInventory(null, 27, "Defuse Bomb");
		System.out.println("Hey");
		Random r = new Random();
		for(int i = 0; i < 3; i++){
			int position = 0;
			do {
				position = r.nextInt(27);
			}while(inv.getItem(position) != null);
			ItemStack is = new ItemStack(Material.REDSTONE_BLOCK);
			ItemMeta im = is.getItemMeta();
			im.setDisplayName(ChatColor.RED + "Wire");
			is.setItemMeta(im);
			inv.setItem(position, is);	
		}
		defusing.put(p, as);
		p.openInventory(inv);
	}
	

	

	
	public void delayedExplosion(Location l, float power, long time){
		ArmorStand as = (ArmorStand) l.getWorld().spawnEntity(l.add(0, -1, 0), EntityType.ARMOR_STAND);
		
		as.setGravity(false);
		as.setCanPickupItems(false);
		as.setInvulnerable(true);
		as.setHelmet(new ItemStack(Material.CARPET, 1, (short) 8));
		as.setVisible(false);
		as.setBasePlate(false);
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
			@Override
			public void run() {
					if(!as.hasMetadata("defused")){
					l.getWorld().createExplosion(l.getX(), l.getY() + 1, l.getZ(), power, false, false);
					}
					as.remove();
				
			
			}
			
		}, time);
	}
	
	@EventHandler
	public void fallingLanded(EntityChangeBlockEvent event){
		if(event.getEntity() instanceof FallingBlock){
			if(event.getEntity().hasMetadata("NUKE")){
				event.getEntity().remove();
				event.getEntity().getWorld().createExplosion(event.getEntity().getLocation().getX(), event.getEntity().getLocation().getY()+2, event.getEntity().getLocation().getZ(), 50f, false, false);
				event.setCancelled(true);
			}
		}
	}
	
	public void explode(ArmorStand as, float power){
		as.getWorld().createExplosion(as.getLocation().getX(), as.getLocation().getY() + 2, as.getLocation().getZ(), power, false, false);
	
	}

	public void removeMetadata(Block b, String tag){
		b.removeMetadata(tag, this);
	}

	@EventHandler
	public void onProjectileHit(EntityDamageByEntityEvent e) {

		if(e.getDamager() instanceof Snowball){
			e.setCancelled(true);
			LivingEntity target = (LivingEntity)e.getEntity();
			double multiplier = 1;
			double damage = 1;
			Player shooter = (Player) ((Snowball)e.getDamager()).getShooter();
			boolean critical = new Random().nextInt(100)<e.getDamager().getMetadata("Critical").get(0).asInt();
			if(target.getEyeLocation().getY()-0.1 < e.getDamager().getLocation().getY()){
				multiplier = 1.5;
				((Player)((Snowball)e.getDamager()).getShooter()).sendMessage("HEADSHOT");
			}else if(critical){
				multiplier = 1.5;
				((Player)((Snowball)e.getDamager()).getShooter()).sendMessage("Critical");

			}
			System.out.println(e.getDamager().getMetadata("Damage").get(0).asDouble());
			damage = e.getDamager().getMetadata("Damage").get(0).asDouble()*multiplier;
			if(!(target instanceof Player))
				damage *= 5;
			GunType.setExp(shooter.getInventory().getItemInMainHand(), (int) (GunType.getExp(shooter.getInventory().getItemInMainHand())+damage));
			while(GunType.getExp(shooter.getInventory().getItemInMainHand()) >= GunType.getMaxExp(shooter.getInventory().getItemInMainHand())){
				GunType.levelUp(shooter.getInventory().getItemInMainHand());
			}
			target.setHealth(Math.max(target.getHealth()-damage, 0.0));
			
			if(target.getHealth() == 0){
				target.remove();
			}else{
				ArmorStand as = (ArmorStand)e.getEntity().getWorld().spawnEntity(e.getEntity().getLocation(), EntityType.ARMOR_STAND);
				as.setVisible(false);
				as.setBasePlate(false);
				
				String name = "-"+Math.floor(damage);
				if(critical)
					name = "*CRITICAL*" + name;
				
				as.setCustomName(name);
				
				as.setCustomNameVisible(true);
				Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
					public void run(){
						as.remove();
					}
				}, 8);
			}
		}
		arrows.remove(e);


	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event){
		if(event.getBlock().getType() == Material.STONE_PLATE || event.getBlock().getType() == Material.WOOD_PLATE){
			event.getBlock().setMetadata("owner", new FixedMetadataValue(this, event.getPlayer().getUniqueId()));
		}
	}

	@EventHandler
	public void onPlayer(PlayerInteractEvent event){
		if(event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.STONE_PLATE && event.getClickedBlock().hasMetadata("owner") && ((FixedMetadataValue)event.getClickedBlock().getMetadata("owner")).value() != event.getPlayer().getUniqueId()){
			event.setCancelled(true);
			event.getClickedBlock().setType(Material.AIR);
			Player e = event.getPlayer();
			e.getWorld().createExplosion(e.getLocation().getX(), e.getLocation().getY(), e.getLocation().getZ(), 1.5f, false, false);

		}
		if(event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.WOOD_PLATE && event.getClickedBlock().hasMetadata("owner") && ((FixedMetadataValue)event.getClickedBlock().getMetadata("owner")).value() != event.getPlayer().getUniqueId()){
			event.setCancelled(true);
			event.getClickedBlock().setType(Material.AIR);
			Player e = event.getPlayer();
			fireRadius(event.getClickedBlock(), 3, 2);

		}
		Player p = event.getPlayer();
		if(event.getAction() == Action.RIGHT_CLICK_AIR) {
			if(event.getMaterial() == Material.SULPHUR) {
				p.getInventory().getItemInMainHand().setAmount(p.getInventory().getItemInMainHand().getAmount() - 1);

				
				Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
					public void run(){
						final Item grenade = p.getWorld().dropItem(p.getEyeLocation(), new ItemStack(Material.SULPHUR));
						grenade.setPickupDelay(100);
						grenade.setVelocity(p.getLocation().getDirection().multiply(0.8D));
						explode(grenade);
						
					}
				}, 20);
			}
		}
	}
	
	public void explode(Item grenade){
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				grenade.getWorld().createExplosion(grenade.getLocation().getX(), grenade.getLocation().getY(), grenade.getLocation().getZ(), 3, false, false);
			}
		}, 20*5);
	}

	public void fireRadius(Block b, int radius, int height){
		for(int x = -radius; x <= radius; x++){
			for(int y = -height; y <= height; y++){
				for(int z = -radius; z <= radius; z++){
					Block b2 = b.getWorld().getBlockAt(b.getX() + x, b.getY() + y, b.getZ() + z);
					Block b3 = b.getWorld().getBlockAt(b.getX()+x, b.getY()+y-1, b.getZ()+z);
					if(Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2)) <= radius && b2.getType()==Material.AIR&&b3.getType().isSolid()){
						b2.setType(Material.FIRE);
					}
				}
			}
		}
	}

	@EventHandler
	public void onRightClick(PlayerInteractEvent event){
		ItemStack is = event.getPlayer().getInventory().getItemInMainHand();
		if(event.getAction() == Action.RIGHT_CLICK_AIR && is != null && GunType.isGun(is)){
			rightClicking.put(event.getPlayer(), true);
		}
		

	}

	@EventHandler
	public void arrowFired(ProjectileLaunchEvent event){
		arrows.add(event.getEntity());
	}

}
