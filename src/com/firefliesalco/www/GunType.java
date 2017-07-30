package com.firefliesalco.www;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

public class GunType {

	String name;
	int bullets;
	double offset;
	double sneakOffset;
	double knockback;
	double kickback;
	double upwardForce;
	float explosionSize;
	String trail;
	double damage;
	Material m;
	int clipSize;
	long reloadTime;
	int range;
	boolean fullDamage;
	String rarity;
	String type;
	String firing;
	int fireRate;
	int criticalChance;
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFiring() {
		return firing;
	}

	public void setFiring(String firing) {
		this.firing = firing;
	}

	HashMap<String, String> extra = new HashMap<String, String>();
	
	
	public GunType(String name){
		this.name = name;
	}
	
	public void addExtra(String flag, String val){
		extra.put(flag, val);
	}
	
	public String getRarity(){
		return rarity;
	}
	
	public void setRarity(String rarity){
		this.rarity = rarity;
	}
	
	public int getBullets() {
		return bullets;
	}

	public void setBullets(int bullets) {
		this.bullets = bullets;
	}

	public double getOffset() {
		return offset;
	}

	public void setOffset(double offset) {
		this.offset = offset;
	}

	public double getSneakOffset() {
		return sneakOffset;
	}

	public void setSneakOffset(double sneakOffset) {
		this.sneakOffset = sneakOffset;
	}

	public double getKnockback() {
		return knockback;
	}

	public void setKnockback(double knockback) {
		this.knockback = knockback;
	}

	public double getKickback() {
		return kickback;
	}

	public void setKickback(double kickback) {
		this.kickback = kickback;
	}

	public double getUpwardForce() {
		return upwardForce;
	}

	public void setUpwardForce(double upwardForce) {
		this.upwardForce = upwardForce;
	}

	public float getExplosionSize() {
		return explosionSize;
	}

	public void setExplosionSize(float explosionSize) {
		this.explosionSize = explosionSize;
	}

	public String getTrail() {
		return trail;
	}

	public void setTrail(String trail) {
		this.trail = trail;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public Material getM() {
		return m;
	}

	public void setM(Material m) {
		this.m = m;
	}

	public int getClipSize() {
		return clipSize;
	}

	public void setClipSize(int clipSize) {
		this.clipSize = clipSize;
	}

	public long getReloadTime() {
		return reloadTime;
	}

	public void setReloadTime(long reloadTime) {
		this.reloadTime = reloadTime;
	}

	public int getRange() {
		return range;
	}

	public void setRange(int range) {
		this.range = range;
	}

	public boolean isFullDamage() {
		return fullDamage;
	}

	public void setFullDamage(boolean fullDamage) {
		this.fullDamage = fullDamage;
	}

	public static boolean isGun(ItemStack is){
		if(dataLoc(is, "Type:") != -1 && dataLoc(is, "Damage:") != -1 && getBullets(is) != -1)
			return true;
		return false;
				
	}
	
	public String getName(String is){
		return null;
	}
	
	public  static int dataLoc(ItemStack is, String data){
		ItemMeta im = is.getItemMeta();
		if(im == null)
			return -1;
		List<String> lore = im.getLore();
		if(lore == null)
			return -1;
		for(int i = 0; i < lore.size(); i++){
			if(lore.get(i).contains(data))
				return i;
		}
		return -1;
					
	}
	
	public static double getDouble(ItemStack is, String data){
		if(dataLoc(is, data) != -1){
			String dataLine = is.getItemMeta().getLore().get(dataLoc(is, data));
			return Double.parseDouble(dataLine.split(" ")[dataLine.split(" ").length-1]);
		}
		return -1;
	}
	
	public static int getInt(ItemStack is, String data){
		if(dataLoc(is, data) != -1){
			String dataLine = is.getItemMeta().getLore().get(dataLoc(is, data));
			return Integer.parseInt(dataLine.split(" ")[dataLine.split(" ").length-1]);
		}
		return -1;
	}
	
	public static void setData(ItemStack is, String data, String value){
		ItemMeta im = is.getItemMeta();
		List<String> lore = im.getLore();
		int line = dataLoc(is, data);
		String[] split = lore.get(line).split(" ");
		String end = "";
		for(int i = 0; i < split.length-1; i++) end += split[i] + " ";
		end += value;
		lore.set(line, end);
		im.setLore(lore);
		is.setItemMeta(im);
	}
	
	public static void setInt(ItemStack is, String data, int value){
		setData(is, data, Integer.toString(value));
	}
	
	public static void setDouble(ItemStack is, String data, double value){
		setData(is, data, Double.toString(Math.floor(value * 1000.0)/1000.0));
	}
	
	public static String getString(ItemStack is, String data){
		if(dataLoc(is, data) != -1){
			String dataLine = is.getItemMeta().getLore().get(dataLoc(is, data));
			return dataLine.split(" ")[dataLine.split(" ").length-1];
		}
		return null;
	}
	
	public static void setBullets(ItemStack is, int bullets){
		ItemMeta im = is.getItemMeta();
		String orig = im.getDisplayName();
		String first = orig.substring(0, orig.indexOf('<'));
		String last = orig.substring(orig.indexOf('>')+1, orig.length());
		im.setDisplayName(first + '<'+bullets+'>'+last);
		is.setItemMeta(im);
	}
	
	public int getFireRate() {
		return fireRate;
	}

	public void setFireRate(int fireRate) {
		this.fireRate = fireRate;
	}

	public int getCriticalChance() {
		return criticalChance;
	}

	public void setCriticalChance(int criticalChance) {
		this.criticalChance = criticalChance;
	}

	public static int getBullets(ItemStack is){
		String name = is.getItemMeta().getDisplayName();
		int from = name.indexOf('<');
		int to = name.indexOf('>');
		if(from == -1 || to == -1)
			return -1;
		return Integer.parseInt(name.substring(from+1, to));
	}
	
	private static HashMap<String, String> color(){
		HashMap<String, String> values = new HashMap<String, String>();
		values.put("Legendary", ChatColor.GOLD + "");
		values.put("Exotic", ChatColor.RED + "");
		values.put("Epic", ChatColor.AQUA + "");
		values.put("Rare", ChatColor.LIGHT_PURPLE + "");
		values.put("Uncommon", ChatColor.BLUE + "");
		values.put("Common", ChatColor.WHITE + "");
		return values;
	}
	
	public static String color(String rarity){
		return color().get(rarity);
	}
	
	public static int getExp(ItemStack is){
		int line = dataLoc(is, "Exp:");
		String t = is.getItemMeta().getLore().get(line);
		String i = t.split(" ")[2];
		return Integer.parseInt(i.split("/")[0]);
	}
	
	public static void setExp(ItemStack is, int value){
		int line = dataLoc(is, "Exp:");
		String lineText = is.getItemMeta().getLore().get(line);
		String[] lineSplit = lineText.split(" ");
		int expLoc = lineSplit[0].length() + lineSplit[1].length() + 2;
		int finalLoc = lineSplit[2].split("/")[0].length() + expLoc;
		ItemMeta im = is.getItemMeta();
		List<String> list = im.getLore();
		list.set(line, lineText.substring(0, expLoc) + value + lineText.substring(finalLoc));
		im.setLore(list);
		is.setItemMeta(im);
		updateBar(is);
	}
	
	public static int getMaxExp(ItemStack is){
		int line = dataLoc(is, "Exp:");
		return Integer.parseInt(is.getItemMeta().getLore().get(line).split(" ")[2].split("/")[1]);
	}
	
	public static void setMaxExp(ItemStack is, int value){
		ItemMeta im = is.getItemMeta();
		List<String> lore = im.getLore();
		int line = dataLoc(is, "Exp:");
		
		lore.set(line, lore.get(line).substring(0, lore.get(line).indexOf('/'))+"/"+value);
		
		
		im.setLore(lore);
		is.setItemMeta(im);
		updateBar(is);
	}
	
	public static void levelUp(ItemStack is){
		double prevMax = getMaxExp(is);
		int prevLevel = getInt(is, "Level");
		int newMax = (int) (prevMax*(1.0 / Math.pow((double)prevLevel, 1.0/1.25) + 1.0));
		setExp(is, (int) (getExp(is)-prevMax));
		setMaxExp(is, newMax);
		setInt(is, "Level", prevLevel+1);
		
		double damage = getDouble(is, "Damage");
		setDouble(is, "Damage", damage * 1.002);
		setInt(is, "Clip Size", (int) Math.ceil(getInt(is, "Clip Size")*1.002));
		setDouble(is, "Offset", getDouble(is, "Offset")*.9992);
		
	}
	
	public static void updateBar(ItemStack is){
		ItemMeta im = is.getItemMeta();
		List<String> lore = im.getLore();
		int line = dataLoc(is, "Exp:");
		int ticks = getExp(is) * 15 / getMaxExp(is);
		String finish = color(getString(is, "Tier")) + "" + ChatColor.BOLD;
		for(int i = 0; i < ticks; i++){
			finish += "\u25AE";
		}
		finish += ChatColor.GRAY + "" + ChatColor.BOLD;
		for(int i = 0; i < 15-ticks; i++){
			finish += "\u25AE";
		}
		String[] split = lore.get(line).split(" ");
		lore.set(line, split[0] + " " + finish + color(getString(is, "Tier")) + " " + split[2]);
		im.setLore(lore);
		is.setItemMeta(im);
		
	}
	
	public ItemStack getGun(){
		String color = color(rarity);
		String lines = color + ChatColor.STRIKETHROUGH + "--+------------+--";
		ItemStack gun = new ItemStack(m);
		ItemMeta im = gun.getItemMeta();
		im.setDisplayName(color + name.replace('_', ' ') + " <" + clipSize + ">");
		List<String> lore = new ArrayList<String>();
		//Add Lore
		lore.add(lines);
		lore.add(ChatColor.GRAY + "Type:" + color + " " + type);
		lore.add(ChatColor.GRAY + "Firing:" + color + " " + firing);
		lore.add(ChatColor.GRAY + "Tier:" + color + " " + rarity);
		lore.add(lines);
		lore.add(ChatColor.GRAY + "Damage:" + color + " " + damage);
		lore.add(ChatColor.GRAY + "Fire Rate:" + color + " " + fireRate);
		lore.add(ChatColor.GRAY + "Bullets:" + color + " " + bullets);
		lore.add(ChatColor.GRAY + "Critical Chance:" + color + " " + criticalChance);
		lore.add(lines);
		lore.add(ChatColor.GRAY + "Range:" + color + " " + range);
		lore.add(ChatColor.GRAY + "Offset:" + color + " " + offset);
		lore.add(ChatColor.GRAY + "Particle:" + color + " " + trail);
		lore.add(lines);
		lore.add(ChatColor.GRAY + "Clip Size:" + color + " " + clipSize);
		lore.add(ChatColor.GRAY + "Reload Time:" + color + " " + reloadTime);
		if(extra.size() > 0)
			lore.add(lines);
		for(String str : extra.keySet()){
			String output = ChatColor.GRAY + str;
			if(extra.get(str) != null){
				output += ":" + color + " " + extra.get(str); 
			}
			lore.add(output);
		}
		lore.add(lines);
		lore.add(ChatColor.GRAY + "Level:" + color + " 1");
		String special = "\u25AE";
		String bar  = "";
		for(int i = 0; i < 15; i++) bar += special;
		lore.add(ChatColor.GRAY + "Exp: " + ChatColor.GRAY + "" + ChatColor.BOLD + bar + color + " " + "0/100");
		
		//End Lore
		im.setLore(lore);
		gun.setItemMeta(im);
		return gun;
	}
	
	
	
}
