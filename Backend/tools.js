/*global console*/
/*global sendFinalEmail*/
'use strict';

const crypto = require('crypto');
const BufferList = require('bl');
class Tools {
	constructor() {
		this._maxKeySize = 32;
		this._maxIVSize = 16;
		this._algorithm = 'AES-256-CBC';
		this._charset = 'utf8';
		this._encoding = 'base64';
		this._hashAlgo = 'sha256';
		this._digestEncoding = 'hex';
		this._characterMatrixForRandomIVStringGeneration = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-'.split('');
	}
	_encryptDecrypt(text, key, initVector, isEncrypt) {
		if (!text || !key) {
			throw '_encryptDecrypt: -> key and plain or encrypted text ' + 'required';
		}
		let ivBl = new BufferList(),
			keyBl = new BufferList(),
			keyCharArray = key.split(''),
			ivCharArray = [],
			encryptor,
			decryptor,
			clearText;
		if (initVector && initVector.length > 0) {
			ivCharArray = initVector.split('');
		}
		for (let i = 0; i < this._maxIVSize; i++) {
			ivBl.append(ivCharArray.shift() || [ null ]);
		}
		for (let i = 0; i < this._maxKeySize; i++) {
			keyBl.append(keyCharArray.shift() || [ null ]);
		}
		if (isEncrypt) {
			encryptor = crypto.createCipheriv(this._algorithm, keyBl.toString(), ivBl.toString());
			encryptor.setEncoding(this._encoding);
			encryptor.write(text);
			encryptor.end();
			return encryptor.read();
		}
		decryptor = crypto.createDecipheriv(this._algorithm, keyBl.toString(), ivBl.toString());
		let dec = decryptor.update(text, this._encoding, this._charset);
		dec += decryptor.final(this._charset);
		return dec;
	}
	_isCorrectLength(length) {
		return length && /^\d+$/.test(length) && parseInt(length, 10) !== 0;
	}
	generateRandomIV(length) {
		if (!this._isCorrectLength(length)) {
			throw 'generateRandomIV() -> needs length or in wrong format';
		}

		let randomBytes = crypto.randomBytes(length),
			_iv = [];

		for (let i = 0; i < length; i++) {
			let ptr = randomBytes[i] % this._characterMatrixForRandomIVStringGeneration.length;
			_iv[i] = this._characterMatrixForRandomIVStringGeneration[ptr];
		}
		return _iv.join('');
	}
	generateRandomIV16() {
		let randomBytes = crypto.randomBytes(16),
			_iv = [];

		for (let i = 0; i < 16; i++) {
			let ptr = randomBytes[i] % this._characterMatrixForRandomIVStringGeneration.length;
			_iv[i] = this._characterMatrixForRandomIVStringGeneration[ptr];
		}
		return _iv.join('');
	}
	getHashSha256(key, length) {
		if (!key) {
			throw 'getHashSha256() -> needs key';
		}

		if (!this._isCorrectLength(length)) {
			throw 'getHashSha256() -> needs length or in wrong format';
		}

		return crypto.createHash(this._hashAlgo).update(key).digest(this._digestEncoding).substring(0, length);
	}
	encrypt(plainText, key, initVector) {
		return this._encryptDecrypt(plainText, key, initVector, true);
	}
	decrypt(encryptedText, key, initVector) {
		return this._encryptDecrypt(encryptedText, key, initVector, false);
	}
	encryptPlainTextWithRandomIV(plainText, key) {
		return this._encryptDecrypt(
			this.generateRandomIV16() + plainText,
			this.getHashSha256(key, 32),
			this.generateRandomIV16(),
			true
		);
	}
	decryptCipherTextWithRandomIV(cipherText, key) {
		let out = this._encryptDecrypt(cipherText, this.getHashSha256(key, 32), this.generateRandomIV16(), false);
		return out.substring(16, out.length);
	}

	sendVerificationMail(ses, request, email, token, res, user, success) {
		request(
			{
				uri: 'https://api.rebrandly.com/v1/links',
				method: 'POST',
				body: JSON.stringify({
					destination: token,
					domain: { fullName: 'rebrand.ly' },
					title: 'Verify Your Account'
				}),
				headers: {
					'Content-Type': 'application/json',
					apikey: process.env.REBRAND
				}
			},
			function(e, response, result) {
				if (e) {
					console.log(">  Couldn't Generate Short URL\n   >  " + e.message);
					console.log('>  Proceeding With Original URL');
				} else {
					token = JSON.parse(result).shortUrl;
					console.log('>  Short URL Generated : ' + token);
				}
				const message =
					'<!DOCTYPE html><html ><head> <meta charset="UTF-8"> <title>Welcome to Sanrakshak</title></head><body><!DOCTYPE html><html><head> <meta content="text/html; charset=utf-8" http-equiv="Content-Type"> <meta content="width=device-width, initial-scale=1.0" name="viewport"> <title>Welcome to Sanrakshak</title> <style>/* Client reset */ #outlook a{padding:0;}.ReadMsgBody{width:100%;}.ExternalClass{width:100%;}.ExternalClass, .ExternalClass p, .ExternalClass span, .ExternalClass font, .ExternalClass td, .ExternalClass div{line-height: 100%!important;}body, table, td, p, a, li, blockquote{-webkit-text-size-adjust:100%; -ms-text-size-adjust:100%;}table, td{mso-table-lspace:0pt; mso-table-rspace:0pt;}img{-ms-interpolation-mode:bicubic;}/* bring inline */ img{display: block; outline: none; text-decoration: none; -ms-interpolation-mode: bicubic;}a img{border: none;}a{text-decoration: none; color: #0DDDC2;}/* text link */ /* Responsive */ @media only screen and (max-device-width: 450px){#content-wrapper, #body-wrapper{width: 100%!important;}.c-responsive-container{display: inline-block; width: 100% padding: 0 16px;}.c-table-responsive{width: 100%;}.button{width: 100%!important; max-width: 300px;}}</style></head><body class=" layouts-application_mailer" data-rc="layouts/application_mailer" style="font-family:\'Helvetica Neue\', \'Arial\', sans-serif;width:100%;-webkit-text-size-adjust:100%;-ms-text-size-adjust:100%;margin:0;padding:0;font-size:16px;line-height:24px"> <table cellspacing="0" cellpadding="0" border="0" width="100%" class="table" id="body-wrapper" style="border-collapse:collapse;table-layout:fixed;margin:0 auto;min-width:100% !important;width:100% !important"> <tr> <td style="border-collapse:collapse"> <div class=" mailers-global-header" data-rc="mailers/global/header"> <table cellspacing="0" cellpadding="0" border="0" width="100%" class="mailers-global-header__header-container " style="border-bottom:1px solid #eeeeee;margin-bottom:48px;border-collapse:collapse;min-width:100% !important;width:100% !important"> <tr> <td align="center" style="border-collapse:collapse;text-align:center"> <h1 class="s-margin1" style="margin:0;margin-top:16px;margin-bottom:16px;margin-left:16px;margin-right:16px"><a href="https://republic.co/?utm_campaign=user_mailer-signup_welcome&utm_medium=email&utm_source=user_mailer-signup_welcome&utm_term=image" style="text-decoration:none;color:#03A9F4"><img width="240" height="60" alt="Sanrakshak" title="Sanrakshak" src="https://raw.githubusercontent.com/ItzzRitik/SanrakshakHome/master/assets/emaillogo.png" style="height:auto;line-height:100%;outline:none;text-decoration:none;display:inline-block;border:0 none"></a></h1> </td></tr></table> </div><table cellspacing="0" cellpadding="16px" border="0" width="100%" class="c-responsive-container" style="border-collapse:collapse;min-width:100% !important;width:100% !important"> <tr> <td style="border-collapse:collapse"> <table cellspacing="0" cellpadding="0" border="0" width="450" class="table" id="content-wrapper" align="center" style="border-collapse:collapse;table-layout:fixed;margin:0 auto"> <tr> <td style="border-collapse:collapse"> <div class=" mailers-user_mailer-signup_welcome" data-rc="mailers/user_mailer/signup_welcome"> <p class="c-emailText" style="margin:0;font-size:16px;line-height:24px;color:#777777;text-align:center;font-weight:300;margin-bottom:16px"> <img width="75" height="68" src="https://republic.co/assets/mailers/signup_welcome/welcome-b2e17100521c87167e0d46ef9cc99a4949153529d71e60e8af7e8e7a98e9598b.png" alt="Welcome" style="border:0 none;height:auto;line-height:100%;outline:none;text-decoration:none;display:inline-block"> </p><h1 class="c-emailTitle" style="margin:0;font-size:30px;line-height:48px;color:#333333;font-weight:200;text-align:center;margin-bottom:16px">Welcome to Sanrakshak</h1> <p class="c-emailText" style="margin:0;font-size:16px;line-height:24px;color:#777777;text-align:center;font-weight:300;margin-bottom:16px">Glad to See You Here, In Order to Start Using Sanrakshak, You Need to Verify Your Email Account.</p><p class="c-emailText s-paddingTop1" style="margin:0;padding-top:16px;font-size:16px;line-height:24px;color:#777777;text-align:center;font-weight:300;margin-bottom:16px"> <a target="_blank" class="button" style="color:#03A9F4;border-radius:3px;display:inline-block;font-size:16px;line-height:50px;height:50px;text-align:center;text-decoration:none;-webkit-text-size-adjust:none;mso-hide:all;color:#fefefe;background-color:#222222;border:1px solid #222222;width:250px;background-color: #03A9F4; border: 1px solid #03A9F4; color: #ffffff; width: 240px; height: 48px; line-height: 48px" href="' +
					token +
					'">VERIFY THIS ACCOUNT</a> </p><p class="s-paddingTop1 s-fontSize14 u-colorGray7 u-text-center u-fontWeight300" style="margin:0;font-size:14px;line-height:24px;padding-top:16px;text-align:center;font-weight:300;color:#777777">Can\'t click the button above? Copy and paste this link into your browser:</p><p class="s-fontSize14 u-colorGray5 u-text-center u-fontWeight300 s-paddingTop0_5" style="margin:0;font-size:14px;line-height:24px;padding-top:8px;text-align:center;font-weight:300;color:#555555">' +
					token +
					'</p><hr class="s-marginTop3 s-marginBottom2" style="margin-top:15px;margin-bottom:16px;border:0;border-top:1px solid #eeeeee;margin-bottom:32px;margin-top:48px"> <table cellspacing="0" cellpadding="0" border="0" width="400" class="table" align="center" style="border-collapse:collapse"> <tr> <td valign="middle" style="border-collapse:collapse;vertical-align:middle"> <img width="60px" height="60px" class="s-marginTop0_5" src="https://republic.co/assets/mailers/user_mailer/signup/discover-95fd5a6c059c1bd9c44c7e9a191f15a6d823e6c9ada10a29c7ea170a621ef7ea.png" alt="Discover" style="margin-top:8px;border:0 none;height:auto;line-height:100%;outline:none;text-decoration:none;display:inline-block"> </td><td class="s-paddingLeft2 s-paddingVert1" style="padding-top:16px;padding-bottom:16px;padding-left:32px;border-collapse:collapse"> <div class="s-fontSize22 u-colorGray2 u-fontWeight300" style="font-size:22px;line-height:32px;font-weight:300;color:#222222">Discover</div><div class="u-colorGray8 s-marginTop0_5 u-fontWeight300 s-fontSize17" style="font-size:17px;line-height:32px;margin-top:8px;font-weight:300;color:#888888">Browse, Discover &amp; Share Details of Cracks in Real Time</div></td></tr><tr> <td valign="middle" style="border-collapse:collapse;vertical-align:middle"> <img width="60px" height="44px" class="s-marginTop0_5" src="https://republic.co/assets/mailers/user_mailer/signup/learn-a8592391ec05a25a453341bbbd5eb54fe12dbb896596b040bb67e35c97c80ee8.png" alt="Learn" style="margin-top:8px;border:0 none;height:auto;line-height:100%;outline:none;text-decoration:none;display:inline-block"> </td><td class="s-paddingLeft2 s-paddingVert1" style="padding-top:16px;padding-bottom:16px;padding-left:32px;border-collapse:collapse"> <div class="s-fontSize22 u-colorGray2 u-fontWeight300" style="font-size:22px;line-height:32px;font-weight:300;color:#222222">Stay Updated</div><div class="u-colorGray8 s-marginTop0_5 u-fontWeight300 s-fontSize17" style="font-size:17px;line-height:32px;margin-top:8px;font-weight:300;color:#888888">Always stay Awared of The Latest Budding Cracks And it\'s Exact Location</div></td></tr><tr> <td valign="middle" style="border-collapse:collapse;vertical-align:middle"> <img width="60px" height="49px" class="s-marginTop0_5" src="https://republic.co/assets/mailers/user_mailer/signup/invest-356e3dee480ada4fdd4ec4d077c6ef958b88020d0dbc853baf2986c3c0e70a66.png" alt="Invest" style="margin-top:8px;border:0 none;height:auto;line-height:100%;outline:none;text-decoration:none;display:inline-block"> </td><td class="s-paddingLeft2 s-paddingVert1" style="padding-top:16px;padding-bottom:16px;padding-left:32px;border-collapse:collapse"> <div class="s-fontSize22 u-colorGray2 u-fontWeight300" style="font-size:22px;line-height:32px;font-weight:300;color:#222222">Free to Use</div><div class="u-colorGray8 s-marginTop0_5 u-fontWeight300 s-fontSize17" style="font-size:17px;line-height:32px;margin-top:8px;font-weight:300;color:#888888">Sanrakshak is Free to Use Service For All Users</div></td></tr></table> </p></div></td></tr></table> </td></tr></table> <div class=" mailers-global-footer" data-rc="mailers/global/footer"> <table cellspacing="0" cellpadding="0" border="0" width="100%" class="mailers-global-footer__footer-container " style="border-top:1px solid #eeeeee;padding:32px;margin-top:48px;text-align:center;font-size:14px;line-height:24px;color:#aaaaaa;border-collapse:collapse;min-width:100% !important;width:100% !important"> <tr> <td align="center" style="border-collapse:collapse;text-align:center"> <div class="s-margin1" style="margin-top:16px;margin-bottom:16px;margin-left:16px;margin-right:16px"> <a alt="Republic" title="Republic" href="https://republic.co/?utm_campaign=user_mailer-signup_welcome&utm_medium=email&utm_source=user_mailer-signup_welcome&utm_term=image" style="text-decoration:none;color:#03A9F4;color:#aaaaaa"> <img width="40" height="40" alt="Republic" title="Republic" src="https://raw.githubusercontent.com/ItzzRitik/SanrakshakHome/master/assets/favicon.png" style="height:auto;line-height:100%;outline:none;text-decoration:none;display:inline-block;border:0 none"> </a> </td></tr></table> </div></td></tr></table> </body></html> </body></html>';
				const params = {
					Destination: { ToAddresses: [ email ] },
					ConfigurationSetName: 'sanrakshak',
					Message: {
						Body: { Html: { Data: message } },
						Subject: { Data: 'Verify Your Email' }
					},
					Source: 'Sanrakshak <verify@mail.sanrakshak.in>'
				};
				const sendEmail = ses.sendEmail(params).promise();
				sendEmail
					.then((data) => {
						res.send(success);
						console.log('>  Verification Email Sent');
					})
					.catch((e) => {
						res.send('0');
						console.log(">  Couldn't Send Verification Email\n   >  " + e.message);
						user.remove({ email: email }, function(e, obj) {
							if (e) throw e;
						});
					});
			}
		);
	}

	sendPasswordMail(ses, request, email, pass, res, user, success) {
		const message = 'Your System generated password is :\n' + pass;
		const params = {
			Destination: { ToAddresses: [ email ] },
			ConfigurationSetName: 'sanrakshak',
			Message: {
				Body: { Html: { Data: message } },
				Subject: { Data: 'Password for Sanrakshak' }
			},
			Source: 'Sanrakshak <verify@mail.sanrakshak.in>'
		};
		const sendEmail = ses.sendEmail(params).promise();
		sendEmail
			.then((data) => {
				res.send(success);
				console.log('>  Password Email Sent Sucessfully');
			})
			.catch((e) => {
				res.send('0');
				console.log(">  Couldn't Send Password Email\n   >  " + e.message);
				user.remove({ email: email }, function(e, obj) {
					if (e) throw e;
				});
			});
	}
}

module.exports = new Tools();
