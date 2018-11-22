
package phat.agents.automaton.tasks.seq;

import phat.agents.automaton.*;
import phat.agents.automaton.conditions.*;
import phat.agents.automaton.uses.*;
import phat.agents.events.*;
import phat.agents.Agent;

public class sleep_leftTask extends SeqTaskAutomaton {

	public sleep_leftTask(Agent agent, String name) {
            super(agent, name);
            setMetadata("SOCIAALML_ENTITY_ID", "sleep_left");
            setMetadata("SOCIAALML_ENTITY_TYPE", "SequentialTaskDiagram");
            setMetadata("SOCIAALML_DESCRIPTION", "");
        }
	
	@Override
	public void initTasks() {
		
		{
                Automaton automaton = new MoveToSpace(
                        agent, 
                        "MoveToSpace"
                        , "BedRoom1LeftSide"
                        
                ).setCanBeInterrupted(true)
                 .setMetadata("SOCIAALML_DESCRIPTION", "")
                 .setMetadata("SOCIAALML_ENTITY_ID", "BGoToTask1")
                 .setMetadata("SOCIAALML_ENTITY_TYPE", "BGoToTask");
                
                
                
                
                
                
                
                
                
                
		
                    addTransition(automaton, false);
                } 
                
		{
                Automaton automaton = new GoIntoBedAutomaton(
                        agent, 
                        "GoIntoBedAutomaton"
                        , "Bed1"
                        
                ).setCanBeInterrupted(true)
                 .setMetadata("SOCIAALML_DESCRIPTION", "")
                 .setMetadata("SOCIAALML_ENTITY_ID", "GoIntoBed0")
                 .setMetadata("SOCIAALML_ENTITY_TYPE", "GoIntoBed");
                
                
                
                
                if(getParent() != null && getParent().getMetadata("") != null) {
                    automaton.setFinishCondition(new TimerFinishedCondition(0, 0, Integer.parseInt(getParent().getMetadata(""))));
                } else if(!"5".equals("-1")) {
                    automaton.setFinishCondition(new TimerFinishedCondition(0, 0, 5));
                }
                
                
                
                
                
                
		
                    addTransition(automaton, false);
                } 
                
		{
                Automaton automaton = new SleepAutomaton(
                        agent, 
                        "SleepAutomaton"
                        
                ).setCanBeInterrupted(true)
                 .setMetadata("SOCIAALML_DESCRIPTION", "")
                 .setMetadata("SOCIAALML_ENTITY_ID", "FallSleep0")
                 .setMetadata("SOCIAALML_ENTITY_TYPE", "FallSleep");
                
                
                
                
                if(getParent() != null && getParent().getMetadata("") != null) {
                    automaton.setFinishCondition(new TimerFinishedCondition(0, 0, Integer.parseInt(getParent().getMetadata(""))));
                } else if(!"30".equals("-1")) {
                    automaton.setFinishCondition(new TimerFinishedCondition(0, 0, 30));
                }
                
                
                
                
                
                
		
                    addTransition(automaton, false);
                } 
                
	}
}
