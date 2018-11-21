
package phat.agents.automaton.activities;

import phat.agents.automaton.*;
import phat.agents.automaton.tasks.seq.*;
import phat.agents.Agent;

public class BActivity2Activity extends ActivityAutomaton {

	public BActivity2Activity(Agent agent, String name) {
        super(agent, name);
        setMetadata("SOCIAALML_ENTITY_ID", "BActivity2");
        setMetadata("SOCIAALML_ENTITY_TYPE", "BActivity");
        setMetadata("SOCIAALML_DESCRIPTION", "Sleeping");
    }
	
	@Override
	public void initTasks() {
            addTransition(new sleep_rightTask(agent, "sleep_right"), false);	
	}
}
