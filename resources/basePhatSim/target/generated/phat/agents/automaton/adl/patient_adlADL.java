
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
            Transition TimeInterval0Transition = new Transition(new TimeInterval0TIA(agent, "TimeInterval0"));
                TimeInterval0Transition.setCondition(new PastTimeCondition(1, 0,0)); 
            Transition TimeInterval2Transition = new Transition(new TimeInterval2TIA(agent, "TimeInterval2"));
                TimeInterval2Transition.setCondition(new PastTimeCondition(1, 0,10)); 
            Transition TimeInterval4Transition = new Transition(new TimeInterval4TIA(agent, "TimeInterval4"));
                TimeInterval4Transition.setCondition(new PastTimeCondition(1, 2,30)); 
            Transition TimeInterval1Transition = new Transition(new TimeInterval1TIA(agent, "TimeInterval1"));
                TimeInterval1Transition.setCondition(new PastTimeCondition(6, 0,0)); 
            Transition TimeInterval3Transition = new Transition(new TimeInterval3TIA(agent, "TimeInterval3"));
                TimeInterval3Transition.setCondition(new PastTimeCondition(1, 1,0)); 
            
	    registerStartState(TimeInterval0Transition.getTarget());
	    
	    registerTransition(TimeInterval0Transition.getTarget(), TimeInterval2Transition);
	    registerTransition(TimeInterval2Transition.getTarget(), TimeInterval3Transition);
	    registerTransition(TimeInterval4Transition.getTarget(), TimeInterval1Transition);
	    registerTransition(TimeInterval1Transition.getTarget(), TimeInterval0Transition);
	    registerTransition(TimeInterval3Transition.getTarget(), TimeInterval4Transition);
	    
	}
}
