package phat;

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
import phat.sensors.presence.PresenceStatePanel;
import phat.server.ServerAppState;
import phat.server.commands.CreateAllPresenceSensorServersCommand;
import phat.structures.houses.HouseFactory;
import phat.world.WorldAppState;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.nio.charset.StandardCharsets;

/**
 * Phat Hardware Link
 * @author melkoroth
 */
public class PhatHardwareLink implements PHATInitializer, PHATCommandListener, SensorListener {
    static GUIPHATInterface phat;

    PHATPresenceSensor presence;

    String bodyId = "Patient";
    String houseId = "House1";
    JFrame sensorMonitor;

    String mqttTopic = "presence";
    int mqttQos = 2;
    static String mqttBroker = "tcp://localhost:1986";
    static String mqttClientId = "PhatHardwareLink";
    static MemoryPersistence mqttPersistence = new MemoryPersistence();
    static MqttClient mqttClient;
    static MqttConnectOptions connOpts;

    public static void main(String[] args) {
        //String[] a = {"-record"};
        PhatHardwareLink sim = new PhatHardwareLink();
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

        startMQTTclient();
    }

    public static void startMQTTclient() {
        try {
            mqttClient = new MqttClient(mqttBroker, mqttClientId, mqttPersistence);
            connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

        } catch(MqttException me) {
            handleMQTTexception(me);
        }
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
        return "This is a proof of concept simulation with a presence sensor attached which sends MQTT messages out";
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

    private void sendMQTTmessage(String msg) {
        try {
            System.out.println("Connecting to broker: "+mqttBroker);
            mqttClient.connect(connOpts);
            System.out.println("Connected");
            System.out.println("Publishing message: "+msg);
            MqttMessage message = new MqttMessage(msg.getBytes(StandardCharsets.UTF_8));
            message.setQos(mqttQos);
            mqttClient.publish(mqttTopic, message);
            System.out.println("Message published");
            mqttClient.disconnect();
            System.out.println("Disconnected");
        } catch(MqttException me) {
            handleMQTTexception(me);
        }
    }

    private static void handleMQTTexception(MqttException me) {
        System.out.println("reason "+me.getReasonCode());
        System.out.println("msg "+me.getMessage());
        System.out.println("loc "+me.getLocalizedMessage());
        System.out.println("cause "+me.getCause());
        System.out.println("excep "+me);
        me.printStackTrace();
    }

    @Override
    public void update(Sensor sensor, SensorData sensorData) {
        System.out.println("Sensor updated");
        sendMQTTmessage("Sensor updated");
        System.out.println(phat.deviceConfig.getDevicesAppState().getDevice("PreSen-Bedroom1-1").getControl(PHATPresenceSensor.class).getPresenceData());
    }

    @Override
    public void cleanUp() {

    }
}