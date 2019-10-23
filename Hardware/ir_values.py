import serial
import time
ser = serial.Serial()
ser.baudrate = 9600
ser.port = "/dev/ttyACM0"
ser.timeout=10

class sharpIr():
	def __init__(self):
		try:
			ser.open()
		except serial.serialutil.SerialException:
			print "port opened"
		self.val = 0
		self.lastVal=0
	def takeValue(self):
		ser.close()
		ser.open()
		ser.flush()
		self.val = ser.readline()
		ser.close()
		try:
			self.lastVal=int(self.val)
			return int(self.val)
		except ValueError:
			return self.lastVal

a = sharpIr()
print a.takeValue()
