package phat.sim;


import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import java.util.logging.Level;
import java.util.logging.Logger;
import phat.agents.*;

import phat.body.BodiesAppState;
import phat.body.commands.*;
import phat.devices.commands.*;
import phat.server.commands.*;
import phat.config.*;
import phat.world.MonitorEventQueue;
import phat.world.MonitorEventQueueImp;
import phat.world.WorldAppState;
import phat.structures.houses.HouseFactory;
import phat.structures.houses.commands.*;
import phat.PHATInitializer;
import phat.GUIPHATInterface;
import phat.agents.impl.*;
import phat.agents.automaton.*;
import phat.agents.automaton.adl.*;

import phat.agents.automaton.activities.*;
import phat.util.PHATUtils;
import phat.GUIArgumentProcessor;
import phat.agents.commands.*;
import phat.body.sensing.hearing.GrammarFacilitator;

/**
 *
 * @author pablo
 */
public class MainSimPresencePHATSimulation implements PHATInitializer {
	static MonitorEventQueueImp meq=new MonitorEventQueueImp();
    public static void main(String[] args) throws RemoteException, AlreadyBoundException, NotBoundException {
    	meq.startServer(MonitorEventQueue.DefaultName);
        MainSimPresencePHATSimulation sim = new MainSimPresencePHATSimulation();
        GUIPHATInterface phat = new GUIPHATInterface(sim, new GUIArgumentProcessor(args));
        phat.setSeed(0L);
        
        phat.start();
        phat.setPrettyLogView(true);
    }

    @Override
    public void initWorld(WorldConfigurator worldConfig) {
        worldConfig.setTime(2018, 1, 1, 0, 59, 50);
        worldConfig.setTimeVisible(true);
        worldConfig.setLandType(WorldAppState.LandType.Grass);
    }

    @Override
    public void initHouse(HouseConfigurator houseConfig) {
        houseConfig.addHouseType("House1", HouseFactory.HouseType.House3room2bath);
    }

    @Override
    public void initBodies(BodyConfigurator bodyConfig) {
       
       bodyConfig.createBody(BodiesAppState.BodyType.Young, "Patient");
       bodyConfig.runCommand(new BodyLabelCommand("Patient", true));
       
       // Initial locations
       bodyConfig.setInSpace("Patient", "House1", "BedRoom1LeftSide");
        
       bodyConfig.runCommand(new SetBodyHeightCommand("Patient", 1.7f));
       
       
    }
    
    @Override
    public void initDevices(DeviceConfigurator deviceConfig) {
    	
        
    }
    
    @Override
    public void initServer(ServerConfigurator serverConfig) {
        
        
        
    }
    
    @Override
    public void initAgents(AgentConfigurator agentsConfig) {
        //System.setProperty("java.util.logging.config.class", "");
        //Logger.getLogger("").setLevel(Level.OFF);
       
       {PatientAgent agent = new PatientAgent("Patient","SimPresence" );
       
            FSM adl = new patient_adlADL(agent, "ADL-patient_adl");
            MainAutomaton mainAutomaton = new MainAutomaton(agent);
	    mainAutomaton.addTransition(adl, false);
            mainAutomaton.addListener(new AutomatonIcon());
            agent.setAutomaton(mainAutomaton);
        
        
        
       agent.registerListener(meq); 
       agentsConfig.add(agent);
        
        }
        
        
        
       agentsConfig.runCommand(new ActivateActuatorEventsLauncherCommand(null));
       
    }
    
    @Override
    public String getTittle() {
        return "SimPresence";
    }

	@Override
	public String getDescription() {
		return "";
	}
}


