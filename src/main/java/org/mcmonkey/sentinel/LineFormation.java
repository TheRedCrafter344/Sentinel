package org.mcmonkey.sentinel;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

//a typical solid formation. strong anti-knockback, solid. have fun flanking individual troops unless you flank the whole formation/ or break it with a strong charge
//can volleyfire (will be able to)
public class LineFormation implements Formation {
	
	//x is horizontal, z is vertical
	@Override
	public Vector[] form(int depth, int frontage, double spacing, SentinelRegiment regiment) {
		Player command = regiment.getCommander();
		int N = regiment.getSoldiers().size();
		Vector[] ret = new Vector[N];
		if(command != null) N++;
		double f = frontage == 0 ? Math.ceil((double)N/(double)depth) : frontage;
		int q = 0; //becomes 1 when officer is found by the loop, so no NPC fills the officers spot.
		for(int n = 0; n < N; n++) {
			if(q == 0 && n > (double)f/2) {
				q = 1;
				continue;
			}
			double x = Math.ceil((double)(2 * ((n + q) % f) - f) / 2) * spacing;
			double z = (n + q) / f * spacing;
			ret[n] = new Vector(x, 0, z);
		}
		return ret;
	}
}
