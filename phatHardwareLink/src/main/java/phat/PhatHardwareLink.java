package phat;

import java.util.logging.Logger;
import java.util.logging.Level;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.jme3.app.state.AppStateManager;

import com.jme3.scene.Node;
import phat.agents.Agent;
import phat.agents.HumanAgent;
import phat.agents.automaton.Automaton;
import phat.agents.automaton.AutomatonIcon;
import phat.agents.automaton.DoNothing;
import phat.agents.automaton.DrinkAutomaton;
import phat.agents.automaton.FSM;
import phat.agents.automaton.FallAutomaton;
import phat.agents.automaton.GoIntoBedAutomaton;
import phat.agents.automaton.MoveToSpace;
import phat.agents.automaton.SayAutomaton;
import phat.agents.automaton.SitDownAutomaton;
import phat.agents.automaton.StandUpAutomaton;
import phat.agents.automaton.UseObjectAutomaton;
import phat.agents.automaton.conditions.TimerFinishedCondition;
import phat.agents.automaton.uses.UseDoorbellAutomaton;
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
public class PhatHardwareLink implements PHATInitializer, PHATCommandListener {
    static GUIPHATInterface phat;

    String bodyId = "Patient";
    String houseId = "House1";
    JFrame sensorMonitor;

    String mqttTopic = "presence";
    int mqttQos = 2;
    String mqttBroker = "tcp://localhost:1986";
    String mqttClientId = "PhatHardwareLink";
    MemoryPersistence mqttPersistence = new MemoryPersistence();

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
        AppStateManager stateManager = phat.app.getStateManager();
        stateManager.attach(phat.deviceConfig.getDevicesAppState());
        //Create presence sensor
        CreatePresenceSensorCommand cpsc = new CreatePresenceSensorCommand("PreSen-Bedroom1-1", this);
        cpsc.setEnableDebug(true);
        cpsc.sethAngle(90f);
        cpsc.setvAngle(30f);
        cpsc.setAngleStep(10f);
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
        //Use WC
        int secondsWC = 10;
        UseObjectAutomaton useWC1 = new UseObjectAutomaton(agent, "WC1");
        useWC1.setFinishCondition(new TimerFinishedCondition(0, 0, secondsWC));
        UseObjectAutomaton useWC2 = new UseObjectAutomaton(agent, "WC1");
        useWC2.setFinishCondition(new TimerFinishedCondition(0, 0, secondsWC));
        UseObjectAutomaton useWC3 = new UseObjectAutomaton(agent, "WC1");
        useWC3.setFinishCondition(new TimerFinishedCondition(0, 0, secondsWC));
        UseObjectAutomaton useWC4 = new UseObjectAutomaton(agent, "WC1");
        useWC4.setFinishCondition(new TimerFinishedCondition(0, 0, secondsWC));
        UseObjectAutomaton useWC5 = new UseObjectAutomaton(agent, "WC1");
        useWC5.setFinishCondition(new TimerFinishedCondition(0, 0, secondsWC));
        //Get into bed        
        GoIntoBedAutomaton goIntoBed = new GoIntoBedAutomaton(agent, "Bed1");
        //Sleep
        int secondsSleep = 10;
        DoNothing sleep1 = new DoNothing(agent, "Sleep");
        sleep1.setFinishCondition(new TimerFinishedCondition(0, 0, secondsSleep));
        DoNothing sleep2 = new DoNothing(agent, "Sleep");
        sleep2.setFinishCondition(new TimerFinishedCondition(0, 0, secondsSleep));
        DoNothing sleep3 = new DoNothing(agent, "Sleep");
        sleep3.setFinishCondition(new TimerFinishedCondition(0, 0, secondsSleep));
        DoNothing sleep4 = new DoNothing(agent, "Sleep");
        sleep4.setFinishCondition(new TimerFinishedCondition(0, 0, secondsSleep));
        DoNothing sleep5 = new DoNothing(agent, "Sleep");
        sleep5.setFinishCondition(new TimerFinishedCondition(0, 0, secondsSleep));
        //Get out of bed
        StandUpAutomaton standUp = new StandUpAutomaton(agent, "StandUpFromBed");
        //Wait
        int secondsWait = 5;
        Automaton wait1 = new DoNothing(agent, "Wait1").setFinishCondition(new TimerFinishedCondition(0, 0, secondsWait));
        Automaton wait2 = new DoNothing(agent, "Wait1").setFinishCondition(new TimerFinishedCondition(0, 0, secondsWait));
        Automaton wait3 = new DoNothing(agent, "Wait1").setFinishCondition(new TimerFinishedCondition(0, 0, secondsWait));
        Automaton wait4 = new DoNothing(agent, "Wait1").setFinishCondition(new TimerFinishedCondition(0, 0, secondsWait));
        Automaton wait5 = new DoNothing(agent, "Wait1").setFinishCondition(new TimerFinishedCondition(0, 0, secondsWait));

        //Create and populate Finite State Machine
        //Timer conditions are used once and then expire. For this reason code is duplicated
        FSM fsm = new FSM(agent);
        fsm.registerStartState(wait1);
        //We make scene happen various times
        fsm.registerTransition(wait1, goIntoBed);
        fsm.registerTransition(goIntoBed, sleep1);
        fsm.registerTransition(sleep1, standUp);
        fsm.registerTransition(standUp, moveToBathroom1);
        fsm.registerTransition(moveToBathroom1, useWC1);
        fsm.registerTransition(useWC1, moveToBedroom1);
        fsm.registerTransition(moveToBedroom1, wait2);
        //Second iteration
        fsm.registerTransition(wait2, goIntoBed);
        fsm.registerTransition(goIntoBed, sleep2);
        fsm.registerTransition(sleep2, standUp);
        fsm.registerTransition(standUp, moveToBathroom1);
        fsm.registerTransition(moveToBathroom1, useWC1);
        fsm.registerTransition(useWC2, moveToBedroom1);
        fsm.registerTransition(moveToBedroom1, wait3);
        //Third iteration
        fsm.registerTransition(wait3, goIntoBed);
        fsm.registerTransition(goIntoBed, sleep3);
        fsm.registerTransition(sleep3, standUp);
        fsm.registerTransition(standUp, moveToBathroom1);
        fsm.registerTransition(moveToBathroom1, useWC3);
        fsm.registerTransition(useWC3, moveToBedroom1);
        fsm.registerTransition(moveToBedroom1, wait4);
        //Fourth iteration
        fsm.registerTransition(wait4, goIntoBed);
        fsm.registerTransition(goIntoBed, sleep4);
        fsm.registerTransition(sleep4, standUp);
        fsm.registerTransition(standUp, moveToBathroom1);
        fsm.registerTransition(moveToBathroom1, useWC4);
        fsm.registerTransition(useWC4, moveToBedroom1);
        fsm.registerTransition(moveToBedroom1, wait5);
        //Fifth iteration
        fsm.registerTransition(wait5, goIntoBed);
        fsm.registerTransition(goIntoBed, sleep5);
        fsm.registerTransition(sleep5, standUp);
        fsm.registerTransition(standUp, moveToBathroom1);
        fsm.registerTransition(moveToBathroom1, useWC5);
        fsm.registerTransition(useWC5, moveToBedroom1);
        fsm.registerTransition(moveToBedroom1, wait5);
        //fsm.registerFinalState(wait5s);

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
        if (command instanceof CreatePresenceSensorCommand) {
            CreatePresenceSensorCommand cpsc = (CreatePresenceSensorCommand) command;
            Node psNode = phat.deviceConfig.getDevicesAppState().getDevice(cpsc.getPresenceSensorId());
            if (psNode != null) {
                PHATPresenceSensor psControl = psNode.getControl(PHATPresenceSensor.class);
                if (psControl != null) {
                    PresenceStatePanel psp1 = new PresenceStatePanel();
                    psControl.add(psp1);
                    sensorMonitor.getContentPane().add(psp1);
                    sensorMonitor.pack();
                    sendMQTTmessage(psNode.toString());
                }
            }
        }
    }

    //TODO: Separar init de mensaje en si
    private void sendMQTTmessage(String msg) {
        try {
            MqttClient sampleClient = new MqttClient(mqttBroker, mqttClientId, mqttPersistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: "+mqttBroker);
            sampleClient.connect(connOpts);
            System.out.println("Connected");
            System.out.println("Publishing message: "+msg);
            MqttMessage message = new MqttMessage(msg.getBytes(StandardCharsets.UTF_8));
            message.setQos(mqttQos);
            sampleClient.publish(mqttTopic, message);
            System.out.println("Message published");
            sampleClient.disconnect();
            System.out.println("Disconnected");
        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
    }
}