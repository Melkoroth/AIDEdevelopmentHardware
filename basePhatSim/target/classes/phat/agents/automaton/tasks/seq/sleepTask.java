
package phat.agents.automaton.tasks.seq;

import phat.agents.automaton.*;
import phat.agents.automaton.conditions.*;
import phat.agents.automaton.uses.*;
import phat.agents.events.*;
import phat.agents.Agent;

public class sleepTask extends SeqTaskAutomaton {

	public sleepTask(Agent agent, String name) {
            super(agent, name);
            setMetadata("SOCIAALML_ENTITY_ID", "sleep");
            setMetadata("SOCIAALML_ENTITY_TYPE", "SequentialTaskDiagram");
            setMetadata("SOCIAALML_DESCRIPTION", "");
        }
	
	@Override
	public void initTasks() {
		
		{
                Automaton automaton = new SleepAutomaton(
                        agent, 
                        "SleepAutomaton"
                        
                ).setCanBeInterrupted(true)
                 .setMetadata("SOCIAALML_DESCRIPTION", "")
                 .setMetadata("SOCIAALML_ENTITY_ID", "FallSleep1")
                 .setMetadata("SOCIAALML_ENTITY_TYPE", "FallSleep");
                
                
                
                
                if(getParent() != null && getParent().getMetadata("") != null) {
                    automaton.setFinishCondition(new TimerFinishedCondition(0, 0, Integer.parseInt(getParent().getMetadata(""))));
                } else if(!"1".equals("-1")) {
                    automaton.setFinishCondition(new TimerFinishedCondition(0, 0, 1));
                }
                
                
                
                
                
                
		
                    addTransition(automaton, false);
                } 
                
	}
}
