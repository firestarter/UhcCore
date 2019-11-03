package com.gmail.val59000mc.schematics;

import com.gmail.val59000mc.game.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DeathmatchArena{
	private Location loc;
	private File arenaSchematic;
	private boolean enable;
	private List<Location> teleportSpots;
	private boolean built;
	protected static int width, length, height;
	
	public DeathmatchArena(Location loc){
		this.loc = loc;
		built = false;
		teleportSpots = new ArrayList<>();
		enable = true;
		teleportSpots.add(loc);
		checkIfSchematicCanBePasted(); 
	}
	
	private void checkIfSchematicCanBePasted() {
		if(GameManager.getGameManager().getConfiguration().getWorldEditLoaded()){
			arenaSchematic = SchematicHandler.getSchematicFile("arena");
        	if(!arenaSchematic.exists()){
				enable = false;
				Bukkit.getLogger().info("[UhcCore] Arena schematic not found in 'plugins/UhcCore/arena.schematic'. There will be a deathmatch at 0 0.");
        	}
		}else{
			Bukkit.getLogger().info("[UhcCore] No WorldEdit installed so ending with deathmatch at 0 0");
			enable = false;
		}
	}

	public void build(){
		if(enable){
			if(!built){
				
				ArrayList<Integer> dimensions;
				try {
					dimensions = SchematicHandler.pasteSchematic(loc, arenaSchematic, 3);
					DeathmatchArena.height = dimensions.get(0);
					DeathmatchArena.length = dimensions.get(1);
					DeathmatchArena.width = dimensions.get(2);
					built = true;
				} catch (Exception e) {
					Bukkit.getLogger().severe("[UhcCore] An error ocurred while pasting the arena");
					e.printStackTrace();
					built = false;
				}
			}  
				
			if(built){
				calculateTeleportSpots();
			}else{
				Bukkit.getLogger().severe("[UhcCore] Deathmatch will be at 0 0 as the arena could not be pasted.");
				enable = false;
			}
		}
	}

	public Location getLoc() {
		return loc;
	}

	public boolean isUsed() {
		return enable;
	}

	public int getMaxSize() {
		return Math.max(DeathmatchArena.length, DeathmatchArena.width);
	}
	
	public void calculateTeleportSpots(){
		List<Location> spots = new ArrayList<>();
		int x = loc.getBlockX(),
			y = loc.getBlockY(),
			z = loc.getBlockZ();
		
		Material spotMaterial = GameManager.getGameManager().getConfiguration().getArenaTeleportSpotBLock();
		
		for(int i = x - width ; i < x + width ; i++ ){
			for(int j = y - height ; j < y + height ; j++ ){
				for(int k = z - length ; k < z + length ; k++ ){
					Block block = loc.getWorld().getBlockAt(i, j, k);
					if(block.getType().equals(spotMaterial)){
						spots.add(block.getLocation().clone().add(0.5, 1, 0.5));
						Bukkit.getLogger().info("[UhcCore] Arena teleport spot found at "+i+" "+(j+1)+" "+k);
					}
				}
			}
		}
		
		if(spots.isEmpty()){
			Bukkit.getLogger().info("[UhcCore] No Arena teleport spot found, defaulting to schematic origin");
		}else{
			teleportSpots = spots;
		}
	}
	
	public List<Location> getTeleportSpots(){
		return teleportSpots;
	}

	public void loadChunks(){
		if(enable){
			World world = getLoc().getWorld();
			for(Location loc : teleportSpots){
				world.loadChunk(loc.getChunk());
			}
		}
	}
}
