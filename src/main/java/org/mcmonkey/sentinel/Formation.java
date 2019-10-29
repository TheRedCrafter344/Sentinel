package org.mcmonkey.sentinel;

import org.bukkit.util.Vector;

public interface Formation {
	
	public Vector[] form(int depth, int frontage, double spacing, SentinelRegiment regiment); //commander position is 0,0, ;depth is base stat, only use frontage if depth = 0
}
