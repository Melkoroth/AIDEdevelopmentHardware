
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

import phat.agents.automaton.activities.*;
import phat.util.PHATUtils;
import phat.GUIArgumentProcessor;
import phat.agents.commands.*;

/**
 *
 * @author pablo
 */
public class MainSimPresencePHATSimulationNoDevicesRecord extends MainSimPresencePHATSimulationNoDevices {
	static MonitorEventQueueImp meq=new MonitorEventQueueImp();
    public static void main(String[] args) throws RemoteException, AlreadyBoundException, NotBoundException {
    	meq.startServer(MonitorEventQueue.DefaultName);
        MainSimPresencePHATSimulationNoDevicesRecord sim = new MainSimPresencePHATSimulationNoDevicesRecord();
        GUIPHATInterface phat = new GUIPHATInterface(sim, new GUIArgumentProcessor(args));
        phat.setSeed(0L);
        phat.setRecordVideo(true);
        phat.start();
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


