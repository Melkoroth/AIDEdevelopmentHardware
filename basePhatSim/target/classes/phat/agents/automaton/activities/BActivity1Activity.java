
package phat.agents.automaton.activities;

import phat.agents.automaton.*;
import phat.agents.automaton.tasks.seq.*;
import phat.agents.Agent;

public class BActivity1Activity extends ActivityAutomaton {

	public BActivity1Activity(Agent agent, String name) {
        super(agent, name);
        setMetadata("SOCIAALML_ENTITY_ID", "BActivity1");
        setMetadata("SOCIAALML_ENTITY_TYPE", "BActivity");
        setMetadata("SOCIAALML_DESCRIPTION", "Sleeping");
    }
	
	@Override
	public void initTasks() {
            addTransition(new sleep_leftTask(agent, "sleep_left"), false);	
	}
}
