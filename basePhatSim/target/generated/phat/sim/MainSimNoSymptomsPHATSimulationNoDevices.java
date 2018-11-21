
package phat.sim;


import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import phat.agents.Agent;
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
import phat.agents.automaton.adl.*;

import phat.agents.automaton.activities.*;
import phat.util.PHATUtils;
import phat.GUIArgumentProcessor;
import phat.agents.commands.*;

/**
 *
 * @author pablo
 */
public class MainSimNoSymptomsPHATSimulationNoDevices extends MainSimNoSymptomsPHATSimulation {
	static MonitorEventQueueImp meq=new MonitorEventQueueImp();
    public static void main(String[] args) throws RemoteException, AlreadyBoundException, NotBoundException {
    	meq.startServer(MonitorEventQueue.DefaultName);
        MainSimNoSymptomsPHATSimulationNoDevices sim = new MainSimNoSymptomsPHATSimulationNoDevices();
        GUIPHATInterface phat = new GUIPHATInterface(sim, new GUIArgumentProcessor(args));
        phat.setSeed(0L);
        phat.start();
        phat.setPrettyLogView(true);
    }
    
    @Override
    public void initDevices(DeviceConfigurator deviceConfig) {
    }
    
    @Override
    public void initServer(ServerConfigurator serverConfig) {
    }
    
    @Override
    public String getTittle() {
        return "SimNoSymptoms";
    }

	@Override
	public String getDescription() {
		return "";
	}
}


