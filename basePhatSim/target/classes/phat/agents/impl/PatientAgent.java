
package phat.agents.impl;

import phat.agents.HumanAgent;
import phat.agents.automaton.*;
import phat.agents.automaton.activities.*;
import phat.agents.automaton.conditions.*;
import phat.agents.events.*;
import phat.agents.filters.*;

public class PatientAgent extends HumanAgent {
    private String simulation="";
    
    public PatientAgent(String bodyId, String simName) {
        super(bodyId);
        this.simulation=simName;
    }
	

    @Override
    protected void initAutomaton() {
        
            setDiseaseManager(new symptoms_specs_Patient(this, simulation));
	
        
    }
}
