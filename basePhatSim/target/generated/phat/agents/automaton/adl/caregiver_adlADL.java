
package phat.agents.automaton.adl;

import phat.agents.automaton.*;
import phat.agents.automaton.conditions.*;
import phat.agents.automaton.timeIntervals.*;
import phat.agents.automaton.tasks.seq.*;
import phat.agents.Agent;

public class caregiver_adlADL extends TimeIntervalManager {

    public caregiver_adlADL(Agent agent, String name) {
        super(agent, name);
        setMetadata("SOCIAALML_ENTITY_ID", "caregiver_adl");
        setMetadata("SOCIAALML_ENTITY_TYPE", "ADLSpecDiagram");
        setMetadata("SOCIAALML_DESCRIPTION", "ADLSpecDiagram");
    }
	
	@Override
	public void initTIs() {
            Transition TimeInterval2Transition = new Transition(new TimeInterval2TIA(agent, "TimeInterval2"));
                TimeInterval2Transition.setCondition(new PastTimeCondition(6, 0,0)); 
            Transition TimeInterval3Transition = new Transition(new TimeInterval3TIA(agent, "TimeInterval3"));
                TimeInterval3Transition.setCondition(new PastTimeCondition(1, 0,0)); 
            
	    registerStartState(TimeInterval2Transition.getTarget());
	    
	    registerTransition(TimeInterval2Transition.getTarget(), TimeInterval3Transition);
	    registerTransition(TimeInterval3Transition.getTarget(), TimeInterval2Transition);
	    
	}
}
