package phat;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.jme3.app.state.AppStateManager;

import com.jme3.scene.Node;
import phat.agents.Agent;
import phat.agents.HumanAgent;
import phat.agents.automaton.*;
import phat.agents.automaton.conditions.TimerFinishedCondition;
import phat.agents.commands.ActivateActuatorEventsLauncherCommand;
import phat.agents.commands.ActivateCallStateEventsLauncherCommand;
import phat.body.BodiesAppState;
import phat.body.commands.*;
import phat.commands.PHATCommand;
import phat.commands.PHATCommandListener;
import phat.config.AgentConfigurator;
import phat.config.BodyConfigurator;
import phat.config.DeviceConfigurator;
import phat.config.HouseConfigurator;
import phat.config.ServerConfigurator;
import phat.config.WorldConfigurator;
import phat.devices.commands.CreatePresenceSensorCommand;
import phat.sensors.Sensor;
import phat.sensors.SensorData;
import phat.sensors.SensorListener;
import phat.sensors.presence.PHATPresenceSensor;
import phat.sensors.presence.PresenceData;
import phat.sensors.presence.PresenceStatePanel;
import phat.server.ServerAppState;
import phat.server.commands.CreateAllPresenceSensorServersCommand;
import phat.structures.houses.HouseFactory;
import phat.world.WorldAppState;

import phat.MqttBroker;
import phat.HardwareLink;

/**
 * Phat Hardware Link
 * @author melkoroth
 */
public class PhatPresenceSensor implements PHATInitializer, PHATCommandListener, SensorListener {
    static HardwareLink hwLink = new HardwareLink();
    static GUIPHATInterface phat;

    PHATPresenceSensor presence;

    String bodyId = "Patient";
    String houseId = "House1";
    JFrame sensorMonitor;

    //Stores previous sensor state to avoid duplicating messages
    //It's init at true so sim doesn't trigger the warning on start
    boolean lastPresenceState = true;
    //Cooldown for sensor. Same reason as above
    static final long SENSORTIMEOUT = 5000;
    long lastPresenceTimestamp;

    public static void main(String[] args) throws IOException {
        //String[] a = {"-record"};
        PhatPresenceSensor sim = new PhatPresenceSensor();
        phat = new GUIPHATInterface(sim);//, new GUIArgumentProcessor());
        phat.setStatView(true);
        phat.setDisplayFPS(true);
        phat.setSeed(0);
        phat.setDisplayHeight(800);
        phat.setDisplayWidth(480);
        phat.setTittle("PhatHardwareLink");
        phat.start();
        //Hide prettyLogger from GUI
        phat.hidePrettyLogger();

        //Start link to hardware
        hwLink.startHardwareLink();

    }

    @Override
    public void initWorld(WorldConfigurator worldConfig) {
        worldConfig.setTime(2018, 1, 1, 2, 30, 0);
        worldConfig.setTimeVisible(true);
        worldConfig.setLandType(WorldAppState.LandType.Grass);
    }

    @Override
    public void initHouse(HouseConfigurator houseConfig) {
        houseConfig.addHouseType(houseId, HouseFactory.HouseType.House3room2bath);
    }

    @Override
    public void initBodies(BodyConfigurator bodyConfig) {
        bodyConfig.createBody(BodiesAppState.BodyType.Young, bodyId);
        bodyConfig.runCommand(new BodyLabelCommand(bodyId, true));
        bodyConfig.setInSpace(bodyId, houseId, "BedRoom1LeftSide");
        bodyConfig.runCommand(new SetBodyHeightCommand(bodyId, 1.7f));
    }

    @Override
    public void initDevices(DeviceConfigurator deviceConfig) {
        presence = new PHATPresenceSensor("PreSen-Bedroom1-2", phat.getSimTime());
        presence.add(this);
        presence.sethAngle(90f);
        presence.setvAngle(30f);
        presence.setAngleStep(10f);

        AppStateManager stateManager = phat.app.getStateManager();
        stateManager.attach(phat.deviceConfig.getDevicesAppState());
        //Create presence sensor
        CreatePresenceSensorCommand cpsc = new CreatePresenceSensorCommand("PreSen-Bedroom1-1", this);
        cpsc.setEnableDebug(true);
        cpsc.sethAngle(90f);
        cpsc.setvAngle(30f);
        cpsc.setAngleStep(10f);
        cpsc.setListener(this);

        phat.deviceConfig.getDevicesAppState().runCommand(cpsc);
        //Create GUI sensor monitor
        sensorMonitor = new JFrame("Sensor Monitoring");
        JPanel content = new JPanel();
        sensorMonitor.setContentPane(content);
        sensorMonitor.setVisible(true);
        //Attach sensor to scene
        ServerAppState serverAppState = new ServerAppState();
        stateManager.attach(serverAppState);
        serverAppState.runCommand(new CreateAllPresenceSensorServersCommand());
    }

    @Override
    public void initServer(ServerConfigurator deviceConfig) {}

    @Override
    public void initAgents(AgentConfigurator agentsConfig) {
    	//Declare agent
        Agent agent = new HumanAgent(bodyId);
        
        //Move
        MoveToSpace moveToBathroom1 = new MoveToSpace(agent, "GoToBathRoom1", "BathRoom1");
        MoveToSpace moveToBedroom1 = new MoveToSpace(agent, "GoToBedRoom1", "BedRoom1LeftSide");
        //Get into bed
        GoIntoBedAutomaton goIntoBed = new GoIntoBedAutomaton(agent, "Bed1");
        //Get out of bed
        StandUpAutomaton standUp = new StandUpAutomaton(agent, "StandUpFromBed");
        //Use WC
        UseObjectAutomaton useWC1 = new UseObjectAutomaton(agent, "WC1");
        useWC1.setFinishCondition(new TimerFinishedCondition(0, 0, 10));
        //Sleep
        SleepAutomaton sleep1 = new SleepAutomaton(agent, "Sleep");
        sleep1.setFinishCondition(new TimerFinishedCondition(0, 0, 10));

        //Create and populate Finite State Machine
        //Timer conditions are used once and then expire. For this reason code is duplicated
        FSM fsm = new FSM(agent);
        fsm.registerStartState(goIntoBed);
        //We make scene happen various times
        fsm.registerTransition(goIntoBed, sleep1);
        fsm.registerTransition(sleep1, standUp);
        fsm.registerTransition(standUp, moveToBathroom1);
        fsm.registerTransition(moveToBathroom1, useWC1);
        fsm.registerTransition(useWC1, moveToBedroom1);
        fsm.registerTransition(moveToBedroom1, goIntoBed);

        fsm.addListener(new AutomatonIcon());
        //Link FSM with agent
        agent.setAutomaton(fsm);
        agentsConfig.add(agent);

        System.setProperty("java.util.logging.config.class", "");
        Logger.getLogger("").setLevel(Level.ALL);

        agentsConfig.runCommand(new ActivateActuatorEventsLauncherCommand(null));
        agentsConfig.runCommand(new ActivateCallStateEventsLauncherCommand(null));
    }

    @Override
    public String getTittle() {
        return "PHAT Hardware Link MQTT";
    }

    @Override
    public String getDescription() {
        return "This is a proof of concept simulation with a presence sensor attached which warns HW";
    }

    //Updates GUI for presence sensor
    @Override
    public void commandStateChanged(PHATCommand command) {
        System.out.println("commandStateChanged");
        if (command instanceof CreatePresenceSensorCommand) {
            System.out.println("is instance");
            CreatePresenceSensorCommand cpsc = (CreatePresenceSensorCommand) command;
            Node psNode = phat.deviceConfig.getDevicesAppState().getDevice(cpsc.getPresenceSensorId());
            if (psNode != null) {
                System.out.println("psNode");
                PHATPresenceSensor psControl = psNode.getControl(PHATPresenceSensor.class);
                if (psControl != null) {
                    System.out.println("psControl");
                    PresenceStatePanel psp1 = new PresenceStatePanel();
                    psControl.add(psp1);
                    sensorMonitor.getContentPane().add(psp1);
                    sensorMonitor.pack();
                    //Attach listener for sensor
                    phat.deviceConfig.getDevicesAppState().getDevice("PreSen-Bedroom1-1").getControl(PHATPresenceSensor.class).add(this);
                }
            }
        }
    }

    @Override
    public void update(Sensor sensor, SensorData sensorData) {
        PHATPresenceSensor ps = (PHATPresenceSensor)sensor;
        PresenceData pd = ps.getPresenceData();
        //If presence detected, coming from a false state and cooldown time passed
        if (pd.isPresence() && !lastPresenceState && (pd.getTimestamp() - lastPresenceTimestamp > SENSORTIMEOUT)) {
            hwLink.warnHardware("Presence detected!");
            lastPresenceState = true;
            lastPresenceTimestamp = pd.getTimestamp();
        } else if (!pd.isPresence()) {
            lastPresenceState = false;
            lastPresenceTimestamp = pd.getTimestamp();
        }
    }

    @Override
    public void cleanUp() {

    }
}