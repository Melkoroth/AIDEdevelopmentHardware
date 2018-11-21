
package phat.agents.automaton.tasks.seq;

import phat.agents.automaton.*;
import phat.agents.automaton.conditions.*;
import phat.agents.automaton.uses.*;
import phat.agents.events.*;
import phat.agents.Agent;

public class go_help_patientTask extends SeqTaskAutomaton {

	public go_help_patientTask(Agent agent, String name) {
            super(agent, name);
            setMetadata("SOCIAALML_ENTITY_ID", "go_help_patient");
            setMetadata("SOCIAALML_ENTITY_TYPE", "SequentialTaskDiagram");
            setMetadata("SOCIAALML_DESCRIPTION", "");
        }
	
	@Override
	public void initTasks() {
		
		{
                Automaton automaton = new MoveToBodyLocAutomaton(
                        agent, 
                        "MoveToBodyLocAutomaton"
                        , "Patient"
                        
                ).setCanBeInterrupted(true)
                 .setMetadata("SOCIAALML_DESCRIPTION", "")
                 .setMetadata("SOCIAALML_ENTITY_ID", "GoToBodyLoc0")
                 .setMetadata("SOCIAALML_ENTITY_TYPE", "GoToBodyLoc");
                
                
                
                
                
                
                
                
                
                
		
                    addTransition(automaton, false);
                } 
                
	}
}
