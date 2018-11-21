
package phat.agents.automaton.tasks.seq;

import phat.agents.automaton.*;
import phat.agents.automaton.conditions.*;
import phat.agents.automaton.uses.*;
import phat.agents.events.*;
import phat.agents.Agent;

public class use_fridgeTask extends SeqTaskAutomaton {

	public use_fridgeTask(Agent agent, String name) {
            super(agent, name);
            setMetadata("SOCIAALML_ENTITY_ID", "use_fridge");
            setMetadata("SOCIAALML_ENTITY_TYPE", "SequentialTaskDiagram");
            setMetadata("SOCIAALML_DESCRIPTION", "");
        }
	
	@Override
	public void initTasks() {
		
		{
                Automaton automaton = new MoveToSpace(
                        agent, 
                        "MoveToSpace"
                        , "Kitchen"
                        
                ).setCanBeInterrupted(true)
                 .setMetadata("SOCIAALML_DESCRIPTION", "")
                 .setMetadata("SOCIAALML_ENTITY_ID", "BGoToTask13")
                 .setMetadata("SOCIAALML_ENTITY_TYPE", "BGoToTask");
                
                
                
                
                
                
                
                
                
                
		
                    addTransition(automaton, false);
                } 
                
		{
                Automaton automaton = new OpenObjectAutomaton(
                        agent, 
                        "OpenObjectAutomaton"
                        , "Fridge1"
                        
                ).setCanBeInterrupted(true)
                 .setMetadata("SOCIAALML_DESCRIPTION", "")
                 .setMetadata("SOCIAALML_ENTITY_ID", "OpenTask0")
                 .setMetadata("SOCIAALML_ENTITY_TYPE", "OpenTask");
                
                
                
                
                
                
                
                
                
                
		
                    addTransition(automaton, false);
                } 
                
		{
                Automaton automaton = new DoNothing(
                        agent, 
                        "DoNothing"
                        
                ).setCanBeInterrupted(true)
                 .setMetadata("SOCIAALML_DESCRIPTION", "")
                 .setMetadata("SOCIAALML_ENTITY_ID", "WaitTask1")
                 .setMetadata("SOCIAALML_ENTITY_TYPE", "WaitTask");
                
                
                
                
                if(getParent() != null && getParent().getMetadata("") != null) {
                    automaton.setFinishCondition(new TimerFinishedCondition(0, 0, Integer.parseInt(getParent().getMetadata(""))));
                } else if(!"30".equals("-1")) {
                    automaton.setFinishCondition(new TimerFinishedCondition(0, 0, 30));
                }
                
                
                
                
                
                
		
                    addTransition(automaton, false);
                } 
                
		{
                Automaton automaton = new CloseObjectAutomaton(
                        agent, 
                        "CloseObjectAutomaton"
                        , "Fridge1"
                        
                ).setCanBeInterrupted(true)
                 .setMetadata("SOCIAALML_DESCRIPTION", "")
                 .setMetadata("SOCIAALML_ENTITY_ID", "CloseTask0")
                 .setMetadata("SOCIAALML_ENTITY_TYPE", "CloseTask");
                
                
                
                
                
                
                
                
                
                
		
                    addTransition(automaton, false);
                } 
                
	}
}
