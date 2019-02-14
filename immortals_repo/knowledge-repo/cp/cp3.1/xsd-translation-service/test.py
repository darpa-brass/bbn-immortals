import time as time___
from flask import Flask, redirect, url_for, request, Response

app = Flask(__name__)



@app.route('/xsdsts/ping')
def ping():
	return str(int(round(time___.time() * 1000)))

@app.route('/xsdsts/translate', methods = ['POST'])
def translate():
	print(request.json)
	return Response("no translation needed", status=200, mimetype='application/xml')

if __name__ == '__main__':
	app.run(debug = True)
