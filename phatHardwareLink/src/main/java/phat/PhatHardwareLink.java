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
        cpsc.setvAngle(45f);
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
        Agent agent = new HumanAgent(bodyId);

        MoveToSpace moveToBathroom1 = new MoveToSpace(agent, "GoToBathRoom1", "BathRoom1");

        UseObjectAutomaton useShower = new UseObjectAutomaton(agent, "Shower1");
        useShower.setFinishCondition(new TimerFinishedCondition(0, 0, 20));

        UseObjectAutomaton useWC1 = new UseObjectAutomaton(agent, "WC1");
        useWC1.setFinishCondition(new TimerFinishedCondition(0, 0, 10));

        UseObjectAutomaton useBasin1 = new UseObjectAutomaton(agent, "Basin1");
        useBasin1.setFinishCondition(new TimerFinishedCondition(0, 0, 10));

        MoveToSpace moveToBedroom1 = new MoveToSpace(agent, "GoToBedRoom1", "BedRoom1");

        GoIntoBedAutomaton goIntoBed = new GoIntoBedAutomaton(agent, "Bed1");

        StandUpAutomaton standUp1 = new StandUpAutomaton(agent, "StandUpFromBed");
        StandUpAutomaton standUp2 = new StandUpAutomaton(agent, "StandUpFromBed");

        DoNothing sleep = new DoNothing(agent, "Sleep");
        sleep.setFinishCondition(new TimerFinishedCondition(0, 0, 5));

        MoveToSpace moveToGettingDressedArea1 = new MoveToSpace(agent, "GoToGettingDressedArea1", "GettingDressedArea1");

        MoveToSpace moveToHaveBreakfast = new MoveToSpace(agent, "GoToHaveBreakfast", "Kitchen");

        SitDownAutomaton sitDownInKitchen = new SitDownAutomaton(agent, "Chair1");

        Automaton haveBreakfast = new DrinkAutomaton(agent).setFinishCondition(new TimerFinishedCondition(0, 0, 20));

        UseObjectAutomaton useSink = new UseObjectAutomaton(agent, "Sink");
        useSink.setFinishCondition(new TimerFinishedCondition(0, 0, 30));

        Automaton wait1 = new DoNothing(agent, "Wait1").setFinishCondition(new TimerFinishedCondition(0, 0, 5));
        Automaton wait2 = new DoNothing(agent, "Wait2").setFinishCondition(new TimerFinishedCondition(0, 0, 5));
        Automaton wait3 = new DoNothing(agent, "Wait3").setFinishCondition(new TimerFinishedCondition(0, 0, 5));
        Automaton wait4 = new DoNothing(agent, "Wait3").setFinishCondition(new TimerFinishedCondition(0, 0, 5));

        UseDoorbellAutomaton useDoorbell = new UseDoorbellAutomaton(agent, "Doorbell1");

        FallAutomaton fall = new FallAutomaton(agent, "TripOver");
        fall.setFinishCondition(new TimerFinishedCondition(0, 0, 5));

        SayAutomaton say1 = new SayAutomaton(agent, "SayGoodMorning", "--> i need help", 0.5f);
        SayAutomaton say2 = new SayAutomaton(agent, "SayGoodMorning", "--> where are you", 0.5f);
        SayAutomaton say3 = new SayAutomaton(agent, "SayGoodMorning", "--> look at me", 0.5f);

        StandUpAutomaton standUp3 = new StandUpAutomaton(agent, "StandUp");


        FSM fsm = new FSM(agent);
        fsm.registerStartState(wait1);
        fsm.registerTransition(wait1, say1);
        fsm.registerTransition(say1, wait2);
        fsm.registerTransition(wait2, say2);
        fsm.registerTransition(say2, wait3);
        fsm.registerTransition(wait3, say3);
        fsm.registerTransition(say3, wait4);
        fsm.registerTransition(wait4, moveToBathroom1);

        fsm.registerTransition(moveToBathroom1, fall);
        fsm.registerTransition(fall, standUp3);
        fsm.registerTransition(standUp3, useShower);
        fsm.registerTransition(useShower, useWC1);
        fsm.registerTransition(useWC1, useBasin1);
        fsm.registerTransition(useBasin1, moveToHaveBreakfast);
        fsm.registerTransition(moveToHaveBreakfast, sitDownInKitchen);
        fsm.registerTransition(sitDownInKitchen, haveBreakfast);
        fsm.registerTransition(haveBreakfast, standUp2);
        fsm.registerTransition(standUp2, useSink);
        fsm.registerFinalState(useSink);

        fsm.addListener(new AutomatonIcon());

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