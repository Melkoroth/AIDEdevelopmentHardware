
package phat.agents.automaton.tasks.seq;

import phat.agents.automaton.*;
import phat.agents.automaton.conditions.*;
import phat.agents.automaton.uses.*;
import phat.agents.events.*;
import phat.agents.Agent;

public class cook_foodTask extends SeqTaskAutomaton {

	public cook_foodTask(Agent agent, String name) {
            super(agent, name);
            setMetadata("SOCIAALML_ENTITY_ID", "cook_food");
            setMetadata("SOCIAALML_ENTITY_TYPE", "SequentialTaskDiagram");
            setMetadata("SOCIAALML_DESCRIPTION", "");
        }
	
	@Override
	public void initTasks() {
		
		{
                Automaton automaton = new use_fridgeTask(
                        agent, 
                        "use_fridgeTask"
                        
                ).setCanBeInterrupted(true)
                 .setMetadata("SOCIAALML_DESCRIPTION", "")
                 .setMetadata("SOCIAALML_ENTITY_ID", "BSequentialTask1")
                 .setMetadata("SOCIAALML_ENTITY_TYPE", "BSequentialTask");
                
                
                
                
                
                
                
                
                
                
		
                    addTransition(automaton, false);
                } 
                
		{
                Automaton automaton = new UseObjectAutomaton(
                        agent, 
                        "UseObjectAutomaton"
                        , "Sink"
                        
                ).setCanBeInterrupted(true)
                 .setMetadata("SOCIAALML_DESCRIPTION", "")
                 .setMetadata("SOCIAALML_ENTITY_ID", "BUseTask4")
                 .setMetadata("SOCIAALML_ENTITY_TYPE", "BUseTask");
                
                
                
                
                if(getParent() != null && getParent().getMetadata("") != null) {
                    automaton.setFinishCondition(new TimerFinishedCondition(0, 0, Integer.parseInt(getParent().getMetadata(""))));
                } else if(!"180".equals("-1")) {
                    automaton.setFinishCondition(new TimerFinishedCondition(0, 0, 180));
                }
                
                
                
                
                
                
		
                    addTransition(automaton, false);
                } 
                
		{
                Automaton automaton = new UseObjectAutomaton(
                        agent, 
                        "UseObjectAutomaton"
                        , "Extractor1"
                        
                ).setCanBeInterrupted(true)
                 .setMetadata("SOCIAALML_DESCRIPTION", "")
                 .setMetadata("SOCIAALML_ENTITY_ID", "BUseTask7")
                 .setMetadata("SOCIAALML_ENTITY_TYPE", "BUseTask");
                
                
                
                
                if(getParent() != null && getParent().getMetadata("") != null) {
                    automaton.setFinishCondition(new TimerFinishedCondition(0, 0, Integer.parseInt(getParent().getMetadata(""))));
                } else if(!"600".equals("-1")) {
                    automaton.setFinishCondition(new TimerFinishedCondition(0, 0, 600));
                }
                
                
                
                
                
                
		
                    addTransition(automaton, false);
                } 
                
		{
                Automaton automaton = new use_fridgeTask(
                        agent, 
                        "use_fridgeTask"
                        
                ).setCanBeInterrupted(true)
                 .setMetadata("SOCIAALML_DESCRIPTION", "")
                 .setMetadata("SOCIAALML_ENTITY_ID", "BSequentialTask2")
                 .setMetadata("SOCIAALML_ENTITY_TYPE", "BSequentialTask");
                
                
                
                
                
                
                
                
                
                
		
                    addTransition(automaton, false);
                } 
                
		{
                Automaton automaton = new UseObjectAutomaton(
                        agent, 
                        "UseObjectAutomaton"
                        , "Extractor1"
                        
                ).setCanBeInterrupted(true)
                 .setMetadata("SOCIAALML_DESCRIPTION", "")
                 .setMetadata("SOCIAALML_ENTITY_ID", "BUseTask5")
                 .setMetadata("SOCIAALML_ENTITY_TYPE", "BUseTask");
                
                
                
                
                if(getParent() != null && getParent().getMetadata("") != null) {
                    automaton.setFinishCondition(new TimerFinishedCondition(0, 0, Integer.parseInt(getParent().getMetadata(""))));
                } else if(!"600".equals("-1")) {
                    automaton.setFinishCondition(new TimerFinishedCondition(0, 0, 600));
                }
                
                
                
                
                
                
		
                    addTransition(automaton, false);
                } 
                
		{
                Automaton automaton = new use_fridgeTask(
                        agent, 
                        "use_fridgeTask"
                        
                ).setCanBeInterrupted(true)
                 .setMetadata("SOCIAALML_DESCRIPTION", "")
                 .setMetadata("SOCIAALML_ENTITY_ID", "BSequentialTask3")
                 .setMetadata("SOCIAALML_ENTITY_TYPE", "BSequentialTask");
                
                
                
                
                
                
                
                
                
                
		
                    addTransition(automaton, false);
                } 
                
		{
                Automaton automaton = new UseObjectAutomaton(
                        agent, 
                        "UseObjectAutomaton"
                        , "Extractor1"
                        
                ).setCanBeInterrupted(true)
                 .setMetadata("SOCIAALML_DESCRIPTION", "")
                 .setMetadata("SOCIAALML_ENTITY_ID", "BUseTask6")
                 .setMetadata("SOCIAALML_ENTITY_TYPE", "BUseTask");
                
                
                
                
                if(getParent() != null && getParent().getMetadata("") != null) {
                    automaton.setFinishCondition(new TimerFinishedCondition(0, 0, Integer.parseInt(getParent().getMetadata(""))));
                } else if(!"1200".equals("-1")) {
                    automaton.setFinishCondition(new TimerFinishedCondition(0, 0, 1200));
                }
                
                
                
                
                
                
		
                    addTransition(automaton, false);
                } 
                
	}
}
