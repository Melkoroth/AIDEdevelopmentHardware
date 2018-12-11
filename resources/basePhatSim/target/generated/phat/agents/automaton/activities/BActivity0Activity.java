
package phat.agents.automaton.activities;

import phat.agents.automaton.*;
import phat.agents.automaton.tasks.seq.*;
import phat.agents.Agent;

public class BActivity0Activity extends ActivityAutomaton {

	public BActivity0Activity(Agent agent, String name) {
        super(agent, name);
        setMetadata("SOCIAALML_ENTITY_ID", "BActivity0");
        setMetadata("SOCIAALML_ENTITY_TYPE", "BActivity");
        setMetadata("SOCIAALML_DESCRIPTION", "");
    }
	
	@Override
	public void initTasks() {
            addTransition(new waitTask(agent, "wait"), false);	
	}
}
