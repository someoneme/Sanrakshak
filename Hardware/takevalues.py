import serial
import time
ser = serial.Serial()
ser.baudrate = 9600

ser.timeout=10

class sensor():
	def __init__(self,portName):
		ser.port = portName
		print "Port Name"+portName
		try:
			ser.open()
		except serial.serialutil.SerialException:
			print "port opened"
		self.val = 0
	def takeValue(self):
		ser.write("1")
		self.val = ser.readline()
		return self.val

	def takeUltrasonicValue2(self):
		ser.write("2")
		self.val = ser.readline()
		return self.val

	def sendMotor(self, v):
		ser.write(v)
		self.val = ser.readline()
		print(self.val)
