
package phat.agents.impl;

import phat.agents.HumanAgent;
import phat.agents.automaton.*;
import phat.agents.automaton.activities.*;
import phat.agents.automaton.conditions.*;
import phat.agents.events.*;
import phat.agents.filters.*;

public class CaregiverAgent extends HumanAgent {
    private String simulation="";
    
    public CaregiverAgent(String bodyId, String simName) {
        super(bodyId);
        this.simulation=simName;
    }
	

    @Override
    protected void initAutomaton() {
        
	
        
    }
}
