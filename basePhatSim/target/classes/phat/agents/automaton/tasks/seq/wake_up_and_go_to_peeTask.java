
package phat.agents.automaton.tasks.seq;

import phat.agents.automaton.*;
import phat.agents.automaton.conditions.*;
import phat.agents.automaton.uses.*;
import phat.agents.events.*;
import phat.agents.Agent;

public class wake_up_and_go_to_peeTask extends SeqTaskAutomaton {

	public wake_up_and_go_to_peeTask(Agent agent, String name) {
            super(agent, name);
            setMetadata("SOCIAALML_ENTITY_ID", "wake_up_and_go_to_pee");
            setMetadata("SOCIAALML_ENTITY_TYPE", "SequentialTaskDiagram");
            setMetadata("SOCIAALML_DESCRIPTION", "");
        }
	
	@Override
	public void initTasks() {
		
		{
                Automaton automaton = new StandUpAutomaton(
                        agent, 
                        "StandUpAutomaton"
                        
                ).setCanBeInterrupted(true)
                 .setMetadata("SOCIAALML_DESCRIPTION", "")
                 .setMetadata("SOCIAALML_ENTITY_ID", "BGetUpFromBed1")
                 .setMetadata("SOCIAALML_ENTITY_TYPE", "BGetUpFromBed");
                
                
                
                
                if(getParent() != null && getParent().getMetadata("") != null) {
                    automaton.setFinishCondition(new TimerFinishedCondition(0, 0, Integer.parseInt(getParent().getMetadata(""))));
                } else if(!"5".equals("-1")) {
                    automaton.setFinishCondition(new TimerFinishedCondition(0, 0, 5));
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
                 .setMetadata("SOCIAALML_ENTITY_ID", "BGoToTask0")
                 .setMetadata("SOCIAALML_ENTITY_TYPE", "BGoToTask");
                
                
                
                
                
                
                
                
                
                
		
                    addTransition(automaton, false);
                } 
                
		{
                Automaton automaton = new UseObjectAutomaton(
                        agent, 
                        "UseObjectAutomaton"
                        , "WC1"
                        
                ).setCanBeInterrupted(true)
                 .setMetadata("SOCIAALML_DESCRIPTION", "")
                 .setMetadata("SOCIAALML_ENTITY_ID", "BUseTask0")
                 .setMetadata("SOCIAALML_ENTITY_TYPE", "BUseTask");
                
                
                
                
                if(getParent() != null && getParent().getMetadata("") != null) {
                    automaton.setFinishCondition(new TimerFinishedCondition(0, 0, Integer.parseInt(getParent().getMetadata(""))));
                } else if(!"60".equals("-1")) {
                    automaton.setFinishCondition(new TimerFinishedCondition(0, 0, 60));
                }
                
                
                
                
                
                
		
                    addTransition(automaton, false);
                } 
                
	}
}
