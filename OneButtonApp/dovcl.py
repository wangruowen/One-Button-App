#!/usr/bin/python

image = "Maple 15 (WinXP)"

import commands
import xmlrpclib
import sys
from datetime import datetime
from time import mktime
import time
import socket
import os
import random
from sys import exit
from Tkinter import *
import threading

class App(Tk):
	def __init__(self, parent):
		Tk.__init__(self, parent)
	 	try:
			self.tk.call('console', 'hide')
		except TclError, msg:
			#print msg
			pass

class VCLTransport(xmlrpclib.SafeTransport):
	##
	# Send a complete request, and parse the response.
	#
	# @param host Target host.
	# @param handler Target PRC handler.
	# @param request_body XML-RPC request body.
	# @param verbose Debugging flag.
	# @return Parsed response.

	def request(self, host, userid, passwd, handler, request_body, verbose=0):
		# issue XML-RPC request

		h = self.make_connection(host)
		if verbose:
			h.set_debuglevel(1)

		self.send_request(h, handler, request_body)
		h.putheader('X-APIVERSION', '2')
		h.putheader('X-User', userid)
		h.putheader('X-Pass', passwd)
		self.send_host(h, host)
		self.send_user_agent(h)
		self.send_content(h, request_body)

		errcode, errmsg, headers = h.getreply()

		if errcode != 200:
			raise ProtocolError(
				host + handler,
				errcode, errmsg,
				headers
				)

		self.verbose = verbose

		try:
			sock = h._conn.sock
		except AttributeError:
			sock = None

		return self._parse_response(h.getfile(), sock)

class VCLServerProxy(xmlrpclib.ServerProxy):
	__userid = ''
	__passwd = ''
	def __init__(self, uri, userid, passwd, transport=None, encoding=None,
				 verbose=0, allow_none=0, use_datetime=0):
		self.__userid = userid
		self.__passwd = passwd
		# establish a "logical" server connection

		# get the url
		import urllib
		type, uri = urllib.splittype(uri)
		if type not in ("http", "https"):
			raise IOError, "unsupported XML-RPC protocol"
		self.__host, self.__handler = urllib.splithost(uri)
		if not self.__handler:
			self.__handler = "/RPC2"

		if transport is None:
			transport = VCLTransport()
		self.__transport = transport

		self.__encoding = encoding
		self.__verbose = verbose
		self.__allow_none = allow_none

	def __request(self, methodname, params):
		# call a method on the remote server

		request = xmlrpclib.dumps(params, methodname, encoding=self.__encoding,
				  allow_none=self.__allow_none)

		response = self.__transport.request(
			self.__host,
			self.__userid,
			self.__passwd,
			self.__handler,
			request,
			verbose=self.__verbose
			)

		if len(response) == 1:
			response = response[0]

		return response

	def __getattr__(self, name):
		# magic method dispatcher
		return xmlrpclib._Method(self.__request, name)

	# note: to call a remote object with an non-standard name, use
	# result getattr(server, "strange-python-name")(args)

class APIhandler(threading.Thread):
	def run(self):
		# check http://code.activestate.com/recipes/82965/
		self.getCaller()
		if(not stateHandler(newstate='getimageid')):
			stateHandler(newstate='exit')
			return
		self.getImageID()
		if(not stateHandler(newstate='makereservation')):
			stateHandler(newstate='exit')
			return
		self.makereservation()
		if(not stateHandler(newstate='waitforready')):
			self.cancelReservation()
			stateHandler(newstate='exit')
			return
		rc = self.waitForReady()
		if(rc == 0):
			delayedExit()
			return
		if(not stateHandler(newstate='getconnectdata')):
			self.cancelReservation()
			stateHandler(newstate='exit')
			return
		self.getConnectData()
		if(not stateHandler(newstate='createrdpfile')):
			self.cancelReservation()
			stateHandler(newstate='exit')
			return
		self.createRDPFile()
		if(not stateHandler(newstate='runrdc')):
			self.cancelReservation()
			stateHandler(newstate='exit')
			return
		self.runRDC()
		time.sleep(10)
		stateHandler(newstate='deleterdpfile')
		self.deleterdpfile()
		time.sleep(15)
		#self.cancelReservation()
		#stateHandler(newstate='exit')

	def setConfig(self, userid, passwd, image):
		self.__userid = userid
		self.__passwd = passwd
		self.image = image
		self.trys = 240
		self.requestid = 0

	# TODO
	def cancelReservation(self):
		if(self.requestid == 0):
			return
		try:
			rc = self.caller.XMLRPCendRequest(self.requestid)
		except xmlrpclib.Fault, err:
			stateHandler(msg='Error communiting with VCL: %s' % err.faultString, newstate='xmlrpcfault')
			self.exit()

		if(rc['status'] == 'error'):
			status('There was an error cancelling the reservation: ' + rc['errormsg'])
		else:
			status('The reservation has been canceled')

	def getCaller(self):
		status('Initializing')
		try:
			self.caller = VCLServerProxy("https://vcl.ncsu.edu/scheduling/index.php?mode=xmlrpccall", self.__userid + '@NCSU', self.__passwd)
		except xmlrpclib.Fault, err:
			stateHandler(msg='Error communiting with VCL: %s' % err.faultString, newstate='xmlrpcfault')
			self.exit()

	def getImageID(self):
		status('Establishing session with VCL')
		try:
			rc = self.caller.XMLRPCgetImages()
		except xmlrpclib.Fault, err:
			stateHandler(msg='Error communiting with VCL: %s' % err.faultString, newstate='xmlrpcfault')
			self.exit()
		# TODO check for error

		# find the imageid
		self.imageid = 0
		for item in rc:
			if(item['name'] == self.image):
				self.imageid = item['id']
				break
		if(self.imageid == 0):
			status('failed to find imageid')
			delayedExit()

	def makereservation(self):
		status('Creating VCL reservation')
		try:
			#rc = self.caller.XMLRPCaddRequest(imageid, self.tounixtime('2009-06-02 12:00:00'), 60)
			rc = self.caller.XMLRPCaddRequest(self.imageid, 'now', 60)
		except xmlrpclib.Fault, err:
			stateHandler(msg='Error communiting with VCL: %s' % err.faultString, newstate='xmlrpcfault')
			self.exit()

		if(rc['status'] == 'error'):
			status("Failed to add request, received this error: " + rc['errormsg'])
			delayedExit()
		elif(rc['status'] == 'notavailable'):
			status("No VCL reservations for the requested image are available at this time")
			delayedExit()

		self.requestid = rc['requestid']

	def waitForReady(self):
		status('VCL resource is being prepared')
		while(self.trys > 0):
			self.trys -= 1
			try:
				rc = self.caller.XMLRPCgetRequestStatus(self.requestid)
			except xmlrpclib.Fault, err:
				stateHandler(msg='Error communiting with VCL: %s' % err.faultString, newstate='xmlrpcfault')
				self.exit()
			if(rc['status'] == 'error'):
				status("Problem getting the status of the reservation: " + rc['errormsg'])
				return 0
			elif(rc['status'] == 'failed'):
				status("The VCL reservation failed")
				return 0
			elif(rc['status'] == 'timedout'):
				status("The VCL reservation timedout")
				return 0
			elif(rc['status'] == 'loading'):
				status('VCL resource is being prepared; Est. time remaining: %d minute(s)' % (rc['time']))
				#stateHandler(newstate='updatewait')
			elif(rc['status'] == 'future'):
				status("The start time for this reservation has not been reached yet")
				return 0
			elif(rc['status'] == 'ready'):
				status("The reservation is ready")
				return 1
			time.sleep(5)
		return 0

	def getConnectData(self):
		status('Getting connection information')
		myip = socket.getaddrinfo(socket.gethostname(), None)[0][4][0]
		try:
			self.conndata = self.caller.XMLRPCgetRequestConnectData(self.requestid, myip)
		except xmlrpclib.Fault, err:
			stateHandler(msg='Error communiting with VCL: %s' % err.faultString, newstate='xmlrpcfault')
			self.exit()

		if(self.conndata['status'] == 'error'):
			status("There was an error getting the connect data - " + self.conndata['errormsg'])
			delayedExit()
		elif(self.conndata['status'] == 'notready'):
			status("The reservation is not ready yet")
			delayedExit()

	def createRDPFile(self):
		width = 1024
		height = 768
		self.filename = '/tmp/VCL' + str(random.randrange(1000,10000)) + '.rdp'
		fp = open(self.filename, 'w')
		fp.write("screen mode id:i:2\r\n")
		fp.write("desktopwidth:i:" + str(width) + "\r\n")
		fp.write("desktopheight:i:" + str(height) + "\r\n")
		fp.write("session bpp:i:16\r\n")
		fp.write("winposstr:s:0,1,382,71,1182,671\r\n")
		fp.write("full address:s:" + self.conndata['serverIP'] + "\r\n")
		fp.write("compression:i:1\r\n")
		fp.write("keyboardhook:i:2\r\n")
		fp.write("audiomode:i:0\r\n")
		fp.write("redirectdrives:i:1\r\n")
		fp.write("redirectprinters:i:1\r\n")
		fp.write("redirectcomports:i:0\r\n")
		fp.write("redirectsmartcards:i:1\r\n")
		fp.write("displayconnectionbar:i:1\r\n")
		fp.write("autoreconnection enabled:i:1\r\n")
		fp.write("username:s:" + self.conndata['user'] + "\r\n")
		fp.write("clear password:s:" + self.conndata['password'] + "\r\n")
		fp.write("domain:s:\r\n")
		fp.write("alternate shell:s:\r\n")
		fp.write("shell working directory:s:\r\n")
		fp.write("disable wallpaper:i:1\r\n")
		fp.write("disable full window drag:i:1\r\n")
		fp.write("disable menu anims:i:1\r\n")
		fp.write("disable themes:i:0\r\n")
		fp.write("disable cursor setting:i:0\r\n")
		fp.write("bitmapcachepersistenable:i:1\r\n")
		fp.write("maximizeshell:i:1\r\n")
		fp.close()

	def runRDC(self):
		status('Connecting to VCL')
		os.system('rdesktop -g 1024x768 -r disk:R=/ -r sound:local -a16 -u ' + self.conndata['user'] + ' -p ' + self.conndata['password'] + ' ' + self.conndata['serverIP'] + ' &')

		#os.system('/Applications/Remote\ Desktop\ Connection.app/Contents/MacOS/Remote\ Desktop\ Connection ' + self.filename + ' &')
		#status('Enter %s as your password in Remote Desktop Connection' % (self.conndata['password']))

	def deleterdpfile(self):
		os.unlink(self.filename)

	def tounixtime(ts):
		t = datetime.strptime(ts, "%Y-%m-%d %H:%M:%S")
		return mktime(t.timetuple())

class Meter(Frame):
	'''A simple progress bar widget.'''
	def __init__(self, master, fillcolor='orchid1', text='', value=0.0, **kw):
		Frame.__init__(self, master, bg='white', width=350, height=20)
		self.configure(**kw)

		self._c = Canvas(self, bg=self['bg'], width=self['width'], height=self['height'],\
		                 highlightthickness=0, relief='flat', bd=0)
		self._c.pack(fill='x', expand=1)
		self._r = self._c.create_rectangle(0, 0, 0, int(self['height']), fill=fillcolor, width=0)
		self._t = self._c.create_text(int(self['width'])/2, int(self['height'])/2, text='')
		self.value = value

		self.set(value, text)

	def set(self, value=0.0, text=None):
		#make the value failsafe:
		if value < 0.0:
			value = 0.0
		elif value > 1.0:
			value = 1.0
		self.value = value
		if text == None:
			#if no text is specified get the default percentage string:
			text = str(int(round(100 * value))) + ' %'
		self._c.coords(self._r, 0, 0, int(self['width']) * value, int(self['height']))
		self._c.itemconfigure(self._t, text=text)

	def get(self):
		return self.value
	
	def incr(self, step, max, jump=0):
		if(jump and self.value < jump):
			self.set(jump)
		if((self.value + step) > max):
			return
		else:
			newval = self.value + step
			self.set(newval)

def stateHandler(msg='', newstate=None):
	global state
	if(newstate == 'exit'):
		state = newstate
	elif(docancel):
		state = 'cancelreservation'
	elif(newstate != None):
		state = newstate
	
	if(state == 'getcaller'):
		vcl.start()
		root.after(1, updatebar)
#	elif(state == 'getimageid'):
#	elif(state == 'makereservation'):
#	elif(state == 'waitforready'):
#	elif(state == 'updatewait'):
#	elif(state == 'getconnectdata'):
#	elif(state == 'createrdpfile'):
	elif(state == 'runrdc'):
		statusbar.grid_remove()
		cancelbtn2['text'] = 'OK'
		cancelbtn2['command'] = cancel
		return 1
#	elif(state == 'deleterdpfile'):
	elif(state == 'xmlrpcfault'):
		status(msg)
		root.after(5000, root.destroy)
		root.after(5000, cancel)
	elif(state == 'exit'):
		try:
			 root.after(50, root.destroy)
	 	except:
			 pass
		cancel()
	elif(state == 'cancelreservation'):
		return 0
	else:
		return 1

def updatebar():
	delay = 1000
	if(state == 'getcaller'):
		root.after(10, updatebar)
	elif(state == 'getimageid'):
		statusbar.incr(0.01, 0.14)
		root.after(delay, updatebar)
	elif(state == 'makereservation'):
		statusbar.incr(0.01, 0.28, 0.14)
		root.after(delay, updatebar)
	elif(state == 'waitforready'):
		statusbar.incr(0.01, 0.89, 0.28)
		root.after(delay, updatebar)
	elif(state == 'getconnectdata'):
		statusbar.incr(0.01, 0.99, 0.9)
		root.after(delay, updatebar)
	elif(state == 'createrdpfile'):
		root.after(delay, updatebar)
	elif(state == 'runrdc'):
		statusbar.incr(0.01, 1, 0.9)
		root.after(delay, updatebar)
#	elif(state == 'deleterdpfile'):
#		statusbar.incr(0.01, 1)
#		root.after(delay, updatebar)
	elif(state == 'cancelreservation'):
		root.after(5000, stateHandler, '', 'exit')

def cancel():
	exit(0)
	root.destroy()

def setCancelReservation():
	global docancel
	docancel = 1
	stateHandler()
	status('Cancelling reservation...')
	cancelbtn2.grid_remove()

def windowclose():
	if(state == 'initialize'):
		cancel()
	elif(cancelbtn2['text'] == 'Cancel'):
		setCancelReservation()
	else:
		cancel()

def delayedExit():
	global docancel
	docancel = 1
	stateHandler()
	cancelbtn2.grid_remove()

def ok(ignore=''):
	userid = userent.get()
	passwd = passent.get()
	userlbl.grid_remove()
	passlbl.grid_remove()
	userent.grid_remove()
	passent.grid_remove()
	okbtn.grid_remove()
	cancelbtn.grid_remove()
	mainlbl['text'] = "Getting %s via VCL:" % (image)
	#Label(root, text="Getting " + image + " via VCL:").grid()
	statuslbl.grid(row=1)
	statusbar.grid(row=2)
	statusbar.set(0)
	cancelbtn2.grid(row=3)

	vcl.setConfig(userid, passwd, image)
	stateHandler(newstate='getcaller')

def moveToPass(self):
	passent.focus_set()

def status(message):
	statuslbl['text'] = message
	statuslbl.update_idletasks()


vcl = APIhandler()
state = 'initialize'
docancel = 0

if __name__ == '__main__':
	root = App(None)
root.protocol("WM_DELETE_WINDOW", windowclose)
root.title('VCL Desktop Launcher')
sw, sh = root.winfo_vrootwidth(), root.winfo_screenheight()
w = 440
h = 120
root.geometry("%dx%d+%d+%d" % (w, h, (sw/2)-(w/2), (sh/2)-(h/2)))

mainlbl = Label(root, text="Please enter your Unity ID and password")
mainlbl.grid(row=0, columnspan=2)
userlbl = Label(root, text="Unity ID:")
userlbl.grid(row=1, sticky=W)
passlbl = Label(root, text="Password:")
passlbl.grid(row=2, sticky=W)

userent = Entry(root)
userent.grid(row=1, column=1)
userent.focus_set()
userent.bind("<Return>", moveToPass)
passent = Entry(root, show='*')
passent.grid(row=2, column=1)
passent.bind("<Return>", ok)

okbtn = Button(root, text="OK", command=ok)
okbtn.grid(row=3)
cancelbtn = Button(root, text="Cancel", command=cancel)
cancelbtn.grid(row=3, column=1)

statuslbl = Label(root)
statusbar = Meter(root, fillcolor='blue')
cancelbtn2 = Button(text="Cancel", command=setCancelReservation)

if __name__ == '__main__':
	root.mainloop()
