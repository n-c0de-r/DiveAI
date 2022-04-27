package s0577683;

import java.awt.Color;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DivingAction;
import lenz.htw.ai4g.ai.Info;
import lenz.htw.ai4g.ai.PlayerAction;

public class FirstAI3 extends AI {
	
	public FirstAI3 (Info info) {
		super(info);
		
		enlistForTournament(577683, 577423);
	}

	@Override
	public String getName() {
		return "DrownedOne";
	}

	@Override
	public Color getPrimaryColor() {
		return Color.BLUE;
	}

	@Override
	public Color getSecondaryColor() {
		return Color.GREEN;
	}

	@Override
	public PlayerAction update() {
		return new DivingAction(info.getMaxAcceleration(), 1.4f);
	}

}
