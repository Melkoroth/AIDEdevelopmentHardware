
package phat.agents.automaton.tasks.seq;

import phat.agents.automaton.*;
import phat.agents.automaton.conditions.*;
import phat.agents.automaton.uses.*;
import phat.agents.events.*;
import phat.agents.Agent;

public class short_eat_chair_2Task extends SeqTaskAutomaton {

	public short_eat_chair_2Task(Agent agent, String name) {
            super(agent, name);
            setMetadata("SOCIAALML_ENTITY_ID", "short_eat_chair_2");
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
                 .setMetadata("SOCIAALML_ENTITY_ID", "BGoToTask11")
                 .setMetadata("SOCIAALML_ENTITY_TYPE", "BGoToTask");
                
                
                
                
                
                
                
                
                
                
		
                    addTransition(automaton, false);
                } 
                
		{
                Automaton automaton = new SitDownAutomaton(
                        agent, 
                        "SitDownAutomaton"
                        , "Chair2"
                        
                ).setCanBeInterrupted(true)
                 .setMetadata("SOCIAALML_DESCRIPTION", "")
                 .setMetadata("SOCIAALML_ENTITY_ID", "SitDown4")
                 .setMetadata("SOCIAALML_ENTITY_TYPE", "SitDown");
                
                
                
                
                if(getParent() != null && getParent().getMetadata("") != null) {
                    automaton.setFinishCondition(new TimerFinishedCondition(0, 0, Integer.parseInt(getParent().getMetadata(""))));
                } else if(!"1".equals("-1")) {
                    automaton.setFinishCondition(new TimerFinishedCondition(0, 0, 1));
                }
                
                
                
                
                
                
		
                    addTransition(automaton, false);
                } 
                
		{
                Automaton automaton = new DrinkAutomaton(
                        agent, 
                        "DrinkAutomaton"
                        , "null"
                        
                ).setCanBeInterrupted(true)
                 .setMetadata("SOCIAALML_DESCRIPTION", "")
                 .setMetadata("SOCIAALML_ENTITY_ID", "Drink5")
                 .setMetadata("SOCIAALML_ENTITY_TYPE", "Drink");
                
                
                
                
                if(getParent() != null && getParent().getMetadata("") != null) {
                    automaton.setFinishCondition(new TimerFinishedCondition(0, 0, Integer.parseInt(getParent().getMetadata(""))));
                } else if(!"120".equals("-1")) {
                    automaton.setFinishCondition(new TimerFinishedCondition(0, 0, 120));
                }
                
                
                
                
                
                
		
                    addTransition(automaton, false);
                } 
                
		{
                Automaton automaton = new EatAutomaton(
                        agent, 
                        "EatAutomaton"
                        , "null"
                        
                ).setCanBeInterrupted(true)
                 .setMetadata("SOCIAALML_DESCRIPTION", "")
                 .setMetadata("SOCIAALML_ENTITY_ID", "Eat3")
                 .setMetadata("SOCIAALML_ENTITY_TYPE", "Eat");
                
                
                
                
                if(getParent() != null && getParent().getMetadata("") != null) {
                    automaton.setFinishCondition(new TimerFinishedCondition(0, 0, Integer.parseInt(getParent().getMetadata(""))));
                } else if(!"120".equals("-1")) {
                    automaton.setFinishCondition(new TimerFinishedCondition(0, 0, 120));
                }
                
                
                
                
                
                
		
                    addTransition(automaton, false);
                } 
                
		{
                Automaton automaton = new DrinkAutomaton(
                        agent, 
                        "DrinkAutomaton"
                        , "null"
                        
                ).setCanBeInterrupted(true)
                 .setMetadata("SOCIAALML_DESCRIPTION", "")
                 .setMetadata("SOCIAALML_ENTITY_ID", "Drink4")
                 .setMetadata("SOCIAALML_ENTITY_TYPE", "Drink");
                
                
                
                
                if(getParent() != null && getParent().getMetadata("") != null) {
                    automaton.setFinishCondition(new TimerFinishedCondition(0, 0, Integer.parseInt(getParent().getMetadata(""))));
                } else if(!"60".equals("-1")) {
                    automaton.setFinishCondition(new TimerFinishedCondition(0, 0, 60));
                }
                
                
                
                
                
                
		
                    addTransition(automaton, false);
                } 
                
	}
}
