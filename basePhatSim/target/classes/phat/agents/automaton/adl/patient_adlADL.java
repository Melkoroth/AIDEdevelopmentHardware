
package phat.agents.automaton.adl;

import phat.agents.automaton.*;
import phat.agents.automaton.conditions.*;
import phat.agents.automaton.timeIntervals.*;
import phat.agents.automaton.tasks.seq.*;
import phat.agents.Agent;

public class patient_adlADL extends TimeIntervalManager {

    public patient_adlADL(Agent agent, String name) {
        super(agent, name);
        setMetadata("SOCIAALML_ENTITY_ID", "patient_adl");
        setMetadata("SOCIAALML_ENTITY_TYPE", "ADLSpecDiagram");
        setMetadata("SOCIAALML_DESCRIPTION", "ADLSpecDiagram");
    }
	
	@Override
	public void initTIs() {
            Transition TimeInterval1Transition = new Transition(new TimeInterval1TIA(agent, "TimeInterval1"));
                TimeInterval1Transition.setCondition(new PastTimeCondition(1, 0,0)); 
            Transition TimeInterval0Transition = new Transition(new TimeInterval0TIA(agent, "TimeInterval0"));
                TimeInterval0Transition.setCondition(new PastTimeCondition(6, 0,0)); 
            
	    registerStartState(TimeInterval0Transition.getTarget());
	    
	    registerTransition(TimeInterval1Transition.getTarget(), TimeInterval0Transition);
	    registerTransition(TimeInterval0Transition.getTarget(), TimeInterval1Transition);
	    
	}
}
