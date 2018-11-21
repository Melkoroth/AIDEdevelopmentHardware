
package phat.agents.automaton.activities;

import phat.agents.automaton.*;
import phat.agents.automaton.tasks.seq.*;
import phat.agents.Agent;

public class BActivity3Activity extends ActivityAutomaton {

	public BActivity3Activity(Agent agent, String name) {
        super(agent, name);
        setMetadata("SOCIAALML_ENTITY_ID", "BActivity3");
        setMetadata("SOCIAALML_ENTITY_TYPE", "BActivity");
        setMetadata("SOCIAALML_DESCRIPTION", "Going to help patient");
    }
	
	@Override
	public void initTasks() {
            addTransition(new go_help_patientTask(agent, "go_help_patient"), false);	
	}
}
