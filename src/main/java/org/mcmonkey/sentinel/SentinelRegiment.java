package org.mcmonkey.sentinel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Owner;

public class SentinelRegiment {

	private Town town;
	
	private Set<NPC> bois;
	
	private static Set<SentinelRegiment> units = new HashSet<SentinelRegiment>();
	private static Map<Player, Set<SentinelRegiment>> commanders = new HashMap<Player, Set<SentinelRegiment>>();
	private static Map<Player, InterUnitFormation> interUFormations = new HashMap<Player, InterUnitFormation>(); //if a player controls many units, how should they be arranged?
	
	private Player command;
	private Formation formation; //how to arrange the NPCs inside the unit itself.
	
	public static Set<SentinelRegiment> getUnitSet() {
		return new HashSet<SentinelRegiment>(units);
	}
	
	public static Set<SentinelRegiment> getByCommander(Player cmd) {
		return commanders.get(cmd);
	}
	
	public static InterUnitFormation getInterUFormation(Player cmd) {
		return interUFormations.get(cmd);
	}
	
	public Set<NPC> getSoldiers() {
		return new HashSet<NPC>(bois);
	}
	
	public Player getCommander() {
		return command;
	}
	
	public Formation getFormation() {
		return formation;
	}
	
	public Town getTown() {
		return town;
	}
	
	public boolean addSoldier(NPC npc) {
		if(!npc.hasTrait(SentinelTrait.class)) return false;
		UUID ownerId = npc.getTrait(Owner.class).getOwnerId();
		if(ownerId == null) return false;
		OfflinePlayer npcOwner = Bukkit.getOfflinePlayer(ownerId);
		if(npcOwner == null) return false;
		Town npcTown = null;
		try {
			npcTown = TownyUniverse.getDataSource().getResident(npcOwner.getName()).getTown();
		} catch (NotRegisteredException e) {return false;}
		if(npcTown != town) return false;
		return bois.add(npc);
	}
	
	public static SentinelRegiment getRegiment(NPC npc) {
		for(SentinelRegiment reg : getUnitSet()) {
			for(NPC n : reg.getSoldiers()) {
				if(n == npc) return reg;
			}
		}
		return null;
	}
	
	public static Set<SentinelRegiment> getRegiments(Town town) {
		Set<SentinelRegiment> ret = new HashSet<SentinelRegiment>();
		for(SentinelRegiment reg : getUnitSet()) {
			if(reg.getTown() == town) ret.add(reg);
		}
		return ret;
	}
	
	public static Set<SentinelRegiment> getRegiments(Nation nat) {
		Set<SentinelRegiment> ret = new HashSet<SentinelRegiment>();
		for(SentinelRegiment reg : getUnitSet()) {
			try {
				if(reg.getTown().getNation() == nat) ret.add(reg);
			} catch (NotRegisteredException e) {}
		}
		return ret;
	}
	
	//returns an array where element 0 is x, element 1 is z
	private static double[] rotate(double x, double z, double angle) {
		double[] ret = new double[2];
		//convert to radians
		double rAngle = angle * Math.PI / 180;
		//rotate
		ret[0] = x * Math.cos(rAngle) + z * Math.sin(rAngle);
		ret[1] = z * Math.cos(rAngle) - x * Math.sin(rAngle);
		return ret;
	}
	
	private static Vector rotate(Vector loc, double angle) {
		double[] rotated = rotate(loc.getX(), loc.getZ(), angle);
		loc.setX(rotated[0]);
		loc.setZ(rotated[1]);
		return loc;
	}
	
	private static Vector rotateAndFix(Vector loc, double angle) {
		double[] rotated = rotate(loc.getX(), loc.getZ(), angle);
		loc.setX(rotated[0]);
		loc.setZ(rotated[1]);
		//help me
		
		//TODO rotate around Y axis and then fix the Y coordinate (or the other ones too) so the NPC doesnt end up in a wall or sth
		return loc;
	}
	
	public static Location addVecLoc(Vector a, Location b) {
		World w = b.getWorld();
		double x = a.getX() + b.getX();
		double y = a.getY() + b.getY();
		double z = a.getZ() + b.getZ();
		return new Location(w, x, y, z);
	}
	
	public void moveTo(Location target, double angle, int depth, int frontage, double spacing) {
		Vector[] locations = formation.form(depth, frontage, spacing, this);
		NPC[] soldiers = bois.toArray(new NPC[]{});
		for(int i = 0; i < bois.size(); i++) {
			soldiers[i].getNavigator().setTarget(addVecLoc(rotateAndFix(locations[i], angle), target));
		}
	}
	
	public void teleportTo(Location target, double angle, int depth, int frontage, double spacing) {
		Vector[] locations = formation.form(depth, frontage, spacing, this);
		NPC[] soldiers = bois.toArray(new NPC[]{});
		for(int i = 0; i < bois.size(); i++) {
			soldiers[i].teleport(addVecLoc(rotateAndFix(locations[i], angle), target), TeleportCause.PLUGIN);
		}
	}
