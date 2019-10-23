from flask import *
from random import randint
from takevalues import *
from gpsvalues import *
import os
import requests
app = Flask(__name__)

lq=0

@app.route("/")
def hello():
	return "enter / sensors"


@app.route("/<string:c>/<string:b>")
def portt(c,b):
	print c+"/"+b
	global opened_port
	opened_port = sensor("/"+c+"/"+b)
	global gpsData
	gpsData = gps()
	return render_template('web.html', **locals())

@app.route("/sensors")
def sensors():
	score = randint(0,10)
	return render_template('web.html', **locals())

@app.route("/private")
def private():
	text = request.args.get('jsdata')
	if text=="1":
		q = opened_port.takeValue()
		print "1   "+str(q)
		return render_template('ultrasonic.html', val=q)	
	if text=="2":
		q = opened_port.takeUltrasonicValue2()
		print "2   "+str(q)
		return render_template('ultrasonic2.html', val=q)
	if text[0]=="3":
		print("Forward")
		q = opened_port.sendMotor("3")
		return render_template('m.html', val=q)
	if text[0]=="4":
		print("Stop")
		q = opened_port.sendMotor("4")
		return render_template('m.html', val=q)
	if text[0]=="5":
		print("Back")
		q = opened_port.sendMotor("5")
		return render_template('m.html', val=q)
	if text[0]=="6":
		print("GPSLat")
		q = gpsData.gpsvaluesLat()
		return render_template('m.html', val=q)
	if text[0]=="7":
		print("GPSLon")
		q = gpsData.gpsvaluesLon()
		return render_template('m.html', val=q)
	 
if __name__ == "__main__":
	app.debug=True
	app.run(host='0.0.0.0')