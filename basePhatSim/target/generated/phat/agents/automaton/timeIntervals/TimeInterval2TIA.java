
package phat.agents.automaton.timeIntervals;

import phat.agents.automaton.FSM;
import phat.agents.automaton.*;
import phat.agents.automaton.activities.*;
import phat.agents.automaton.conditions.*;
import phat.agents.automaton.tasks.seq.*;
import phat.agents.automaton.TimeIntervalAutomaton;
import phat.agents.automaton.Transition;
import phat.agents.Agent;
import phat.PHATInterface;

public class TimeInterval2TIA extends TimeIntervalAutomaton {

    public TimeInterval2TIA(Agent agent, String name) {
        super(agent, name);
        setMetadata("SOCIAALML_ENTITY_ID", "TimeInterval2");
        setMetadata("SOCIAALML_ENTITY_TYPE", "TimeInterval");
        //setMetadata("SOCIAALML_ENTITY_TYPE", "");
    }
	
	@Override
	public void initSubAutomaton() {
		FSM fsm = new FSM(agent, 1,"FSM-TimeInterval2TIA");
		ActivityAutomaton BActivity1Activity = new BActivity1Activity(agent, "BActivity1");
		
                
                
    	fsm.registerStartState(BActivity1Activity);
    	
        
    	
        
        fsm.registerFinalState(BActivity1Activity);
    	// DoNothing due to no activity defined
    	
    	
    	addTransition(fsm, true);
	}
        
        public Automaton getDefaultState(PHATInterface phatInterface) {
            return new sleepTask(agent, "sleep");
        }
}
