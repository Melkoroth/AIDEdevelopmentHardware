#Config BB pins
#config-pin P8.8 gpio
#config-pin P8.10 gpio

import paho.mqtt.client as mqtt
import Adafruit_BBIO.GPIO as GPIO

#config-pin P8_8 gpio
#config-pin P8_10 gpio

ledPin = "P8_8"
pirPin = "P8_10"

# The callback for when the client receives a CONNACK response from the server.
def on_connect(client, userdata, flags, rc):
	print("Connected with result code" + str(rc))

	# Subscribing in on_connect() means that if we lose the connection and
	# reconnect then subscriptions will be renewed.
	client.subscribe("$SYS/#")

# The callback for when a PUBLISH message is received from the server.
def on_message(client, userdata, msg):
   	print(msg.topic+" "+str(msg.payload))

# Conenct to Broker, set up callbacks
client = mqtt.Client()
client.on_connect = on_connect
#client.on_message = on_message
client.connect("192.168.1.34", 1986)

#Set up pins
GPIO.setup(ledPin, GPIO.OUT)
GPIO.setup(pirPin, GPIO.IN)

GPIO.output(ledPin, GPIO.HIGH)

# Blocking call that processes network traffic, dispatches callbacks and
# handles reconnecting.
# Other loop*() functions are available that give a threaded interface and a
# manual interface.
#client.loop_forever(timeout=1.0)

while True:
	#GPIO.output(ledPin, GPIO.HIGH)
	client.publish("presence", "alarm", 2)
	client.loop()
	#time.sleep(1)
	#GPIO.output(ledPin, GPIO.LOw)
	#time.sleep(1)
