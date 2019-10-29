const app = require('express')();
const bodyparser = require('body-parser');
const mongoose = require('mongoose');
const git = require('simple-git/promise')();
const aws = require('aws-sdk');
const tools = require('./tools');
const request = require('request');
const passgen = require('generate-password');
require('dotenv').config();

const { createLogger, format, transports } = require('winston');
require('winston-syslog');
const logger = createLogger({
	format: format.combine(
		format.colorize(),
		format.timestamp(),
		format.align(),
		format.printf((info) => info.message.replace('\t', ''))
	),
	transports: [
		new transports.Console(),
		new transports.Syslog({
			host: process.env.papertrailHOST,
			port: process.env.papertrailPORT,
			app_name: 'Sanrakshak',
			localhost: require('os').hostname(),
			protocol: 'udp4'
		})
	],
	defaultMeta: { service: 'user-service' }
});

var call = 0;
app.set('view engine', 'ejs');
aws.config.update({
	accessKeyId: process.env.accessKeyId,
	secretAccessKey: process.env.secretAccessKey,
	region: process.env.region
});
const ses = new aws.SES({ apiVersion: '2010-12-01' });

const dbOptions = { useNewUrlParser: true, reconnectTries: Number.MAX_VALUE, poolSize: 10 };
mongoose.connect(process.env.MONGO_KEY, dbOptions).then(
	() => {
		logger.info('  >  Connection Established');
	},
	(e) => {
		logger.error('  >  Connection Failed \n>  ' + e);
	}
);

app.use(bodyparser.urlencoded({ extended: true }));
app.use(bodyparser.json());
app.use(function(req, res, next) {
	res.header('Access-Control-Allow-Origin', '*');
	res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept');
	next();
});

var User = mongoose.model(
	'users',
	new mongoose.Schema({
		email: String,
		pass: String,
		fname: String,
		lname: String,
		gender: String,
		dob: String,
		aadhaar: String,
		profile: String,
		cover: String,
		verified: String
	})
);
var Crack = mongoose.model(
	'cracks',
	new mongoose.Schema({
		x: String,
		y: String,
		intensity: String,
		date: String
	})
);

//Routes

const demoDevices = (process.env.demoDevices).split(',');
const rejectDevices = (process.env.rejectDevices).split(',');
app.post('/connect', function(req, res) {
	// Return 0 - App won't start
	// Return 1 - App will start normally
	// Return 2 - App will start as demo

	var device = req.body.device;
	var versionCode = req.body.versionCode;
	var versionName = req.body.versionName;
	try {
		device = tools.decryptCipherTextWithRandomIV(device, 'sanrakshak');
		versionCode = tools.decryptCipherTextWithRandomIV(versionCode, 'sanrakshak');
		versionName = tools.decryptCipherTextWithRandomIV(versionName, 'sanrakshak');
	} catch (e) {
		logger.error('>  Error occured while decrypting device data :\n>  ' + e);
		res.send('0');
		//clearInterval(con);
	}
	logger.info(' ');
	logger.info(++call + ') New Device Connected');
	logger.info('  >  Device Model - ' + device);
	logger.info('  >  Version Code - ' + versionCode);
	logger.info('  >  Version Name - ' + versionName);

	const indexDemo = demoDevices.indexOf((versionName.split('-'))[1]);
	const indexReject = rejectDevices.indexOf((versionName.split('-'))[1]);
	if (indexReject != -1) {
		res.send('0');
	}
	else if (indexDemo != -1) {
		logger.info('>  Application approved as demo.');
		let msg = ((process.env.deviceMessages).split(',')[indexDemo]).split('<->');
		if(msg.length==1) {
			msg += process.env.demoCredentials;
			msg = msg.split('<->');
		}
		res.send('2<->'+msg[0]+'<->'+msg[1]+'<->'+msg[2]);
	} else {
		logger.info('>  Application Approved.');
		res.send('1');
	}

	// if (mongoose.connection.readyState == 2) {
	//     console.log(">  Connection Request Recieved");
	// }
	// con = setInterval(function sendEmail() {
	//     if (mongoose.connection.readyState == 1) {
	// var device = req.body.device;
	// try {
	//     device = tools.decryptCipherTextWithRandomIV(device, "sanrakshak");
	// }
	// catch (e) {
	//     console.log(">  Error occured while decrypting device name :\n>  " + e);
	//     res.send("1");
	//     clearInterval(con);
	// }
	// console.log("\n" + ++call + ") Device Connected");
	// console.log(">  " + device);
	// res.send("1");
	//         clearInterval(con);
	//     }
	//     else if (mongoose.connection.readyState == 0) {
	//         res.send("0");
	//         clearInterval(con);
	//     }
	// }, 1000);
});

app.post('/check', function(req, res) {
	var email = req.body.email;
	logger.info(' ');
	logger.info(++call + ') Searching For Account');
	try {
		email = tools.decryptCipherTextWithRandomIV(email, 'sanrakshak');
		logger.info('Email : ' + email);
	} catch (e) {
		logger.error('>  Error occured while decrypting data :\n>  ' + e);
		res.send('0');
		return;
	}
	User.find({ email: email }, function(e, user) {
		if (e) {
			logger.error('>  Error occured while checking for email :\n>  ' + e);
		} else {
			if (user.length) {
				res.send('1');
				logger.info('"' + email + '" exists in database');
				logger.info('>  Login Initiated');
			} else {
				res.send('0');
				logger.info('"' + email + '" doesn\'t exists in database');
				logger.info('>  Account creation Initiated');
			}
		}
	});
});

app.post('/login', function(req, res) {
	var email = req.body.email;
	var token = email;
	var pass = req.body.pass;
	logger.info(' ');
	logger.info(++call + ') Authentication Started');
	try {
		email = tools.decryptCipherTextWithRandomIV(email, 'sanrakshak');
		logger.info('Email : ' + email + '\nEncrypted Password : ' + pass.replace(/\r?\n|\r/g, ''));
		pass = tools.decryptCipherTextWithRandomIV(pass, 'sanrakshak');
	} catch (e) {
		logger.error('>  Error occured while decrypting data :\n>  ' + e);
		res.send('0');
		return;
	}
	User.find({ email: email }, function(e, user) {
		if (e) {
			logger.error('>  Error occured while logging in :\n>  ' + e);
			res.send('0');
		} else if (user.length > 0) {
			if (user[0].pass == pass) {
				if (user[0].verified == '1') {
					if ( user[0].fname == '' || user[0].lname == '' || user[0].gender == '' || user[0].dob == '' || user[0].aadhaar == '' ) {
						res.send('3');
						logger.info('>  Authentication Pending : Launching Profile Creation');
					} else {
						res.send('1');
						logger.info('>  Authentication Successfull');
					}
				} else {
					var message = req.protocol + '://' + req.get('host') + '/verify?landing=yes&token=' + encodeURIComponent(token);
					tools.sendVerificationMail(ses, request, email, message, res, user, '2');
					logger.info('>  Authentication Pending : Launching Email Verification');
				}
			} else {
				res.send('0');
				logger.info('>  Authentication Terminated : Invalid Password');
			}
		} else if (user.length <= 0) {
			res.send('0');
			logger.info(">  Authentication Terminated : User doesn't exist");
		}
	});
});

app.post('/signup', function(req, res) {
	var email = req.body.email;
	var token = email;
	var pass = req.body.pass;
	var verified = req.body.verified;
	logger.info(' ');
	logger.info(++call + ') Account Creation Started');
	try {
		email = tools.decryptCipherTextWithRandomIV(email, 'sanrakshak');
		logger.info('Email : ' + email + '\nEncrypted Password : ' + pass.replace(/\r?\n|\r/g, ''));
		pass = tools.decryptCipherTextWithRandomIV(pass, 'sanrakshak');
	} catch (e) {
		logger.error('>  Error occured while decrypting data :\n>  ' + e);
		res.send('0');
		return;
	}
	User.create(
		{
			email: email,
			pass: pass,
			fname: '',
			lname: '',
			gender: '',
			dob: '',
			aadhaar: '',
			profile: '',
			cover: '',
			verified: verified
		},
		function(e, user) {
			if (e) {
				logger.error('>  Error While Creating Account\n>  ' + e);
				res.send('0');
			} else {
				if (verified == 0) {
					logger.info('Token Generated: ' + token.replace(/\r?\n|\r/g, ''));
					var message = req.protocol + '://' + req.get('host') + '/verify?landing=yes&token=' + encodeURIComponent(token);
					tools.sendVerificationMail(ses, request, email, message, res, user, '1');
				} else {
					logger.info('>  Account Created Successfuly\n>  Account Verification Not Required');
					res.send('1');
				}
			}
		}
	);
});
app.post('/profile', function(req, res) {
	var email = req.body.email,
		fname = req.body.fname,
		lname = req.body.lname,
		gender = req.body.gender,
		dob = req.body.dob,
		aadhaar = req.body.aadhaar,
		profile = req.body.profile,
		cover = req.body.cover;
	logger.info(' ');
	logger.info(++call + ') Profile Creation Started');
	try {
		email == null ? '' : (email = tools.decryptCipherTextWithRandomIV(email, 'sanrakshak'));
		fname == null ? '' : (fname = tools.decryptCipherTextWithRandomIV(fname, 'sanrakshak'));
		lname == null ? '' : (lname = tools.decryptCipherTextWithRandomIV(lname, 'sanrakshak'));
		gender == null ? '' : (gender = tools.decryptCipherTextWithRandomIV(gender, 'sanrakshak'));
		dob == null ? '' : (dob = tools.decryptCipherTextWithRandomIV(dob, 'sanrakshak'));
		aadhaar == null ? '' : (aadhaar = tools.decryptCipherTextWithRandomIV(aadhaar, 'sanrakshak'));
		profile == null ? '' : (profile = tools.decryptCipherTextWithRandomIV(profile, 'sanrakshak'));
		cover == null ? '' : (cover = tools.decryptCipherTextWithRandomIV(cover, 'sanrakshak'));
	} catch (e) {
		logger.error('>  Error occured while decrypting data :\n>  ' + e);
		res.send('0');
		return;
	}
	User.find({ email: email }, function(e, user) {
		if (e) {
			res.send('0');
		} else if (user.length > 0) {
			User.updateMany(
				{ email: email },
				{
					$set: {
						fname: fname,
						lname: lname,
						gender: gender,
						dob: dob,
						aadhaar: aadhaar,
						profile: profile,
						cover: cover
					}
				},
				function(err, user) {
					if (err) {
						logger.error('>  Profile Creation Failed');
						res.send('0');
					} else {
						logger.info('>  Profile Created Successfuly'+ '\n'+
						'  >  Email : ' + email)+ '\n'
						'  >  First Name : ' + fname+ '\n'
						'  >  Last Name : ' + lname+ '\n'
						'  >  Gender : ' + gender+ '\n'
						'  >  Date of Birth : ' + dob+ '\n'
						'  >  Aadhaar Number : ' + aadhaar+ '\n'
						'  >  Profile URL : ' + profile;
						res.send('1');
					}
				}
			);
		} else {
			res.send('0');
		}
	});
});

app.post('/social', function(req, res) {
	var email = req.body.email,
		fname = req.body.fname,
		lname = req.body.lname,
		gender = req.body.gender,
		dob = req.body.dob,
		profile = req.body.profile,
		cover = req.body.cover,
		pass = '';
	logger.info(' ');
	logger.info(++call + ') Profile Creation Started');
	try {
		email == null ? '' : (email = tools.decryptCipherTextWithRandomIV(email, 'sanrakshak'));
		fname == null ? '' : (fname = tools.decryptCipherTextWithRandomIV(fname, 'sanrakshak'));
		lname == null ? '' : (lname = tools.decryptCipherTextWithRandomIV(lname, 'sanrakshak'));
		gender == null ? '' : (gender = tools.decryptCipherTextWithRandomIV(gender, 'sanrakshak'));
		dob == null ? '' : (dob = tools.decryptCipherTextWithRandomIV(dob, 'sanrakshak'));
		profile == null ? '' : (profile = tools.decryptCipherTextWithRandomIV(profile, 'sanrakshak'));
		cover == null ? '' : (cover = tools.decryptCipherTextWithRandomIV(cover, 'sanrakshak'));

		pass = passgen.generate({
			length: 10,
			numbers: true,
			uppercase: true,
			excludeSimilarCharacters: true,
			strict: true
		});
	} catch (e) {
		logger.error('>  Error occured while decrypting data :\n>  ' + e);
		res.send('0');
		return;
	}
	User.create(
		{
			email: email,
			pass: pass,
			fname: fname,
			lname: lname,
			gender: gender,
			dob: dob,
			profile: profile,
			cover: cover,
			verified: '1'
		},
		function(e, user) {
			if (e) {
				res.send('0');
				logger.error('>  Error While Creating Account\n>  ' + e);
			} else {
				logger.info('>  Profile Created Successfuly'+ '\n'+
				'  >  Email : ' + email)+ '\n'
				'  >  First Name : ' + fname+ '\n'
				'  >  Last Name : ' + lname+ '\n'
				'  >  Gender : ' + gender+ '\n'
				'  >  Date of Birth : ' + dob+ '\n'
				'  >  Profile URL : ' + profile+ '\n'
				'  >  Cover URL : ' + cover;
				tools.sendPasswordMail(ses, request, email, pass, res, user, '1');
			}
		}
	);
});

app.get('/verify', function(req, res) {
	var landing = req.query.landing;
	var token = req.query.token;
	var email,
		verified = '0';
	try {
		email = tools.decryptCipherTextWithRandomIV(token, 'sanrakshak');
	} catch (e) {
		logger.info(' ');
		logger.info(++call + ') Verification Initiated');
		logger.error('>  Error occured while decrypting data :\n>  ' + e);
		res.send('0');
		return;
	}
	User.find({ email: email }, function(e, user) {
		if (e) {} 
		else if (user.length > 0) {verified = user[0].verified;}
		if (landing == 'yes') {
			res.render('verify', {
				protocol: req.protocol,
				host: req.get('host'),
				token: encodeURIComponent(token),
				verified: verified
			});
		} else if (landing == 'no' && verified == '0') {
			logger.info(' ');
			logger.info(++call + ') Verification Initiated');
			logger.info('Token Received : ' + token.replace(/\r?\n|\r/g, ''));
			logger.info('Email Linked : ' + email);
			if (verified == '0') {
				User.find({ email: email }, function(e, user) {
					if (e) { res.send('0');} 
					else if (user.length > 0) {
						User.updateMany({ email: email }, { $set: { verified: '1' } }, function(err, user) {
							if (err) {
								logger.info('>  Verification Failed');
								res.send('0');
							} else {
								logger.info('>  Account Has Been Verified');
								res.send('1');
							}
						});
					} else {
						res.send('0');
					}
				});
			}
		}
	});
});

app.post('/checkverification', function(req, res) {
	var email = req.body.email;
	logger.info(' ');
	logger.info(++call + ') Checking Email Verification');
	try {
		email = tools.decryptCipherTextWithRandomIV(email, 'sanrakshak');
		logger.info('> Email : ' + email);
	} catch (e) {
		logger.error('>  Error occured while decrypting data :\n>  ' + e);
		res.send('0');
		return;
	}
	User.find({ email: email }, function(e, user) {
		if (e) {
			res.send('0');
		} else if (user.length) {
			if (user[0].verified == '1') {
				logger.info('Verified');
				res.send('1');
			} else {
				logger.info('Not Verified');
				res.send('0');
			}
		} else {
			logger.info("User Doesn't Exist");
			res.send('0');
		}
	});
});

app.post('/getprofile', function(req, res) {
	var email = req.body.email;
	logger.info(' ');
	logger.info(++call + ') Profile Details Requested');
	try {
		email = tools.decryptCipherTextWithRandomIV(email, 'sanrakshak');
		logger.info('> Email : ' + email);
	} catch (e) {
		logger.error('>  Error occured while decrypting data :\n>  ' + e);
		res.send('0');
		return;
	}
	User.find({ email: email }, function(e, user) {
		if (e) {
			logger.info('>  "' + email + '" Doesn\'t Exist in Database');
			res.send('0');
		} else {
			res.json(user);
			logger.info('>  Profile Details Sent Sucessfully.');
		}
	});
});

app.post('/addcrack', function(req, res) {
	var data = req.body.dataFrame;
	data = Buffer.from('' + data, 'base64').toString('ascii');
	data = data.split('-');
	var intensity = data[3];
	var date = data[4];
	logger.info(' ');
	logger.info(++call + ') Adding a New Crack');
	Crack.create(
		{
			x: data[1],
			y: data[2],
			intensity: intensity != null ? intensity : Math.floor(Math.random() * 10 + 1),
			date: date != null ? date : new Date().toLocaleString('en-IN')
		},
		function(e, crack) {
			if (e) {
				res.send('0');
				logger.error('>  Failed');
			} else {
				res.send('1');
				logger.info('>  Success\n' + crack);
			}
		}
	);
});

app.get('/addcrack', function(req, res) {
	res.render('crack', {});
});

app.post('/addcrackweb', function(req, res) {
	var x = req.body.x;
	var y = req.body.y;
	var intensity = req.body.i;
	var date = req.body.date;
	logger.info(' ');
	logger.info(++call + ') Adding a New Crack');
	if (x == 0 || y == 0 || x == null || y == null) {
		logger.info('Empty or Zero(0) Value Received');
		logger.info(">  Can't add these values");
		res.send('0');
	} else
		Crack.find({ x: x, y: y }, function(e, crack) {
			if (e) {
				logger.error('>  Error occured while checking for crack :\n>  ' + e);
			} else {
				if (crack.length) {
					res.send('0');
					logger.info('Requested data already exists in database');
					logger.info(">  Can't add duplicate cracks");
					return;
				} else {
					logger.info('New Crack Detected');
					Crack.create(
						{
							x: x,
							y: y,
							intensity: (intensity != null && intensity != '') ? intensity : Math.floor(Math.random() * 100 + 1),
							date: date != null ? date : new Date().toLocaleString('en-IN')
						},
						function(e, crack) {
							if (e) {
								logger.error('>  Failed');
								res.send('0');
							} else {
								logger.info('>  Crack added sucessfully :\n' + crack);
								res.send('1');
							}
						}
					);
				}
			}
		});
});

app.post('/getcrack', function(req, res) {
	var email = req.body.email;
	logger.info(' ');
	logger.info(++call + ') Cracks Requested');
	try {
		email = tools.decryptCipherTextWithRandomIV(email, 'sanrakshak');
		logger.info('> Email : ' + email);
	} catch (e) {
		logger.error('>  Error occured while decrypting data :\n>  ' + e);
		res.send('0');
		return;
	}
	Crack.find({}, function(e, cracks) {
		if (e) {
			logger.error('>  Collection "cracks" Doesn\'t exist');
			res.send('0');
		} else {
			cracks.reverse();
			res.json(cracks);
			logger.info('>  Cracks List Sent Sucessfully.');
		}
	});
});

app.get('/encrypt', function(req, res) {
	var text = req.query.text;
	logger.info(' ');
	logger.info(++call + ') Encrypting Requested Data"');
	logger.error('  >  '+text);
	try {
		text = tools.encryptPlainTextWithRandomIV(text, 'sanrakshak');
		text = tools.encryptPlainTextWithRandomIV(text, 'sanrakshak');
	} catch (e) {
		logger.error('>  Error occured while decrypting data :\n>  ' + e);
		res.send('0');
		return;
	}
	logger.error('>  Encrypted  : '+text);
	res.send(text);
});

app.get('/dropusers', function(req, res) {
	logger.info(' ');
	logger.info(++call + ') Deleting Collection "Users"');
	mongoose.connection.dropCollection('cracks', function(err, result) {
		if (err) {
			res.send('0');
			logger.error('>  Failed');
		} else {
			res.send('1');
			logger.info('>  Success');
		}
	});
});

app.get('/git', function(req, res) {
	var m = req.query.m;
	logger.info(' ');
	logger.info(++call + ') Pushing to Github');
	git.add('.').then(
		(addSuccess) => {
			logger.info('>  Changes Successfully Added to Stack');
		},
		(failedAdd) => {
			logger.error('>  Changes Adding Failed\n>  ' + failedAdd);
		}
	);
	git.commit(m).then(
		(successCommit) => {
			logger.info('>  Changes Successfully Commited\n   >  Message : "' + m + '"');
		},
		(failed) => {
			logger.error('>  Changes Commit Failed\n>  ' + failed);
		}
	);
	git.push('origin', 'master').then(
		(success) => {
			logger.info('>  Changes Successfully Pushed to Origin Master');
		},
		(failed) => {
			logger.error('>  Changes Push Failed\n>  ' + failed);
		}
	);
	res.send('1');
});

app.get('*', function(req, res) {
	res.send('Working!!!');
});


app.listen(process.env.PORT || 8080, function() {
	logger.info(' ');
	logger.info(++call + ') Starting Server');
	logger.info('  >  Server is Listening');
	logger.info(' ');
	logger.info(++call + ') Connection to MongoDB Atlas Server');
});
