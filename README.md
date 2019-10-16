# Sanrakshak
This project is a demonstration of real time graph of two different sensors mounted on a train prototype. One can view graph, switch on or switch off the sensor, view live camera view with 20FPS and 100ms ping. 
## Hardware Used
### Raspberry Pi(Server for Flask and arduino)
Raspberry is used to take value from different sensors via arduino and upload the same on server in real time.
### Arduino(Gateway for sensor)
Using serial monitor to take sensor data and publish onto pi COM Port.
### Ultrasonic Sensor
This sensor returns the distance of the object from the sensor using ultrasonic waves.
### IR
It tells whether there is any obstacle infront of sensors or not. 
### Camera
Used for live streaming.
# Project Snap
![](snapshots/snap.gif)
