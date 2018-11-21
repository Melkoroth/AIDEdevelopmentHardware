
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

public class TimeInterval3TIA extends TimeIntervalAutomaton {

    public TimeInterval3TIA(Agent agent, String name) {
        super(agent, name);
        setMetadata("SOCIAALML_ENTITY_ID", "TimeInterval3");
        setMetadata("SOCIAALML_ENTITY_TYPE", "TimeInterval");
        //setMetadata("SOCIAALML_ENTITY_TYPE", "");
    }
	
	@Override
	public void initSubAutomaton() {
		FSM fsm = new FSM(agent, 1,"FSM-TimeInterval3TIA");
		ActivityAutomaton BActivity0Activity = new BActivity0Activity(agent, "BActivity0");
		
                
                
    	fsm.registerStartState(BActivity0Activity);
    	
        
    	
        
        fsm.registerFinalState(BActivity0Activity);
    	// DoNothing due to no activity defined
    	
    	
    	addTransition(fsm, true);
	}
        
        public Automaton getDefaultState(PHATInterface phatInterface) {
            return new waitTask(agent, "wait");
        }
}
