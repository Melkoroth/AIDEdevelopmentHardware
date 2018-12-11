
package phat.agents.filters;

import phat.agents.Agent;
import phat.agents.automaton.*;
import phat.agents.automaton.tasks.seq.*;
import phat.agents.automaton.conditions.*;
import phat.agents.filters.*;
import phat.agents.filters.types.*;

public class symptoms_specs_Patient extends DiseaseManager {

    public symptoms_specs_Patient(Agent agent, String simulation) {
        super(agent);
        initSymptoms(simulation);
    }
    
    private void initSymptoms(String simulation) {
        setStage("");
        
        agent.getAutomaton().setAutomatonModificator(this);
    }
}
