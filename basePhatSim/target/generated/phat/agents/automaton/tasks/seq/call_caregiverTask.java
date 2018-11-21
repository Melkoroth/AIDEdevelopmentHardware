
package phat.agents.automaton.tasks.seq;

import phat.agents.automaton.*;
import phat.agents.automaton.conditions.*;
import phat.agents.automaton.uses.*;
import phat.agents.events.*;
import phat.agents.Agent;

public class call_caregiverTask extends SeqTaskAutomaton {

	public call_caregiverTask(Agent agent, String name) {
            super(agent, name);
            setMetadata("SOCIAALML_ENTITY_ID", "call_caregiver");
            setMetadata("SOCIAALML_ENTITY_TYPE", "SequentialTaskDiagram");
            setMetadata("SOCIAALML_DESCRIPTION", "");
        }
	
	@Override
	public void initTasks() {
		
		{
                Automaton automaton = new SayAutomaton(
                        agent, 
                        "SayAutomaton"
                        , "I need help"
                        
                ).setCanBeInterrupted(true)
                 .setMetadata("SOCIAALML_DESCRIPTION", "")
                 .setMetadata("SOCIAALML_ENTITY_ID", "SayTask0")
                 .setMetadata("SOCIAALML_ENTITY_TYPE", "SayTask");
                
                
                
                
                
                
                
                
                
                
		
                PHATAudioEventAutomatonFinishedListener audioEvent = 
    			new PHATAudioEventAutomatonFinishedListener(agent,"BEvent0", new AgentEventSource(agent));
                    automaton.addListener(audioEvent);
                    addTransition(automaton, false);
                } 
                
	}
}
