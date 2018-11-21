
package phat.agents.automaton.tasks.seq;

import phat.agents.automaton.*;
import phat.agents.automaton.conditions.*;
import phat.agents.automaton.uses.*;
import phat.agents.events.*;
import phat.agents.Agent;

public class clean_houseTask extends SeqTaskAutomaton {

	public clean_houseTask(Agent agent, String name) {
            super(agent, name);
            setMetadata("SOCIAALML_ENTITY_ID", "clean_house");
            setMetadata("SOCIAALML_ENTITY_TYPE", "SequentialTaskDiagram");
            setMetadata("SOCIAALML_DESCRIPTION", "");
        }
	
	@Override
	public void initTasks() {
		
		{
                Automaton automaton = new DoNothing(
                        agent, 
                        "DoNothing"
                        
                ).setCanBeInterrupted(true)
                 .setMetadata("SOCIAALML_DESCRIPTION", "")
                 .setMetadata("SOCIAALML_ENTITY_ID", "WaitTask4")
                 .setMetadata("SOCIAALML_ENTITY_TYPE", "WaitTask");
                
                
                
                
                if(getParent() != null && getParent().getMetadata("") != null) {
                    automaton.setFinishCondition(new TimerFinishedCondition(0, 0, Integer.parseInt(getParent().getMetadata(""))));
                } else if(!"120".equals("-1")) {
                    automaton.setFinishCondition(new TimerFinishedCondition(0, 0, 120));
                }
                
                
                
                
                
                
		
                    addTransition(automaton, false);
                } 
                
		{
                Automaton automaton = new MoveToSpace(
                        agent, 
                        "MoveToSpace"
                        , "Hall"
                        
                ).setCanBeInterrupted(true)
                 .setMetadata("SOCIAALML_DESCRIPTION", "")
                 .setMetadata("SOCIAALML_ENTITY_ID", "BGoToTask20")
                 .setMetadata("SOCIAALML_ENTITY_TYPE", "BGoToTask");
                
                
                
                
                
                
                
                
                
                
		
                    addTransition(automaton, false);
                } 
                
		{
                Automaton automaton = new DoNothing(
                        agent, 
                        "DoNothing"
                        
                ).setCanBeInterrupted(true)
                 .setMetadata("SOCIAALML_DESCRIPTION", "")
                 .setMetadata("SOCIAALML_ENTITY_ID", "WaitTask6")
                 .setMetadata("SOCIAALML_ENTITY_TYPE", "WaitTask");
                
                
                
                
                if(getParent() != null && getParent().getMetadata("") != null) {
                    automaton.setFinishCondition(new TimerFinishedCondition(0, 0, Integer.parseInt(getParent().getMetadata(""))));
                } else if(!"120".equals("-1")) {
                    automaton.setFinishCondition(new TimerFinishedCondition(0, 0, 120));
                }
                
                
                
                
                
                
		
                    addTransition(automaton, false);
                } 
                
		{
                Automaton automaton = new MoveToSpace(
                        agent, 
                        "MoveToSpace"
                        , "BathRoom1"
                        
                ).setCanBeInterrupted(true)
                 .setMetadata("SOCIAALML_DESCRIPTION", "")
                 .setMetadata("SOCIAALML_ENTITY_ID", "BGoToTask18")
                 .setMetadata("SOCIAALML_ENTITY_TYPE", "BGoToTask");
                
                
                
                
                
                
                
                
                
                
		
                    addTransition(automaton, false);
                } 
                
	}
}
