
package phat.agents.automaton.tasks.seq;

import phat.agents.automaton.*;
import phat.agents.automaton.conditions.*;
import phat.agents.automaton.uses.*;
import phat.agents.events.*;
import phat.agents.Agent;

public class wait_for_caregivers_helpTask extends SeqTaskAutomaton {

	public wait_for_caregivers_helpTask(Agent agent, String name) {
            super(agent, name);
            setMetadata("SOCIAALML_ENTITY_ID", "wait_for_caregivers_help");
            setMetadata("SOCIAALML_ENTITY_TYPE", "SequentialTaskDiagram");
            setMetadata("SOCIAALML_DESCRIPTION", "");
        }
	
	@Override
	public void initTasks() {
		
		{
                Automaton automaton = new WaitForCloseToBodyAutomaton(
                        agent, 
                        "WaitForCloseToBodyAutomaton"
                        , "Caregiver"
                        
                ).setCanBeInterrupted(true)
                 .setMetadata("SOCIAALML_DESCRIPTION", "")
                 .setMetadata("SOCIAALML_ENTITY_ID", "WaitForBodyClose0")
                 .setMetadata("SOCIAALML_ENTITY_TYPE", "WaitForBodyClose");
                
                
                
                
                
                
                
                
                
                
		
                    addTransition(automaton, false);
                } 
                
	}
}
