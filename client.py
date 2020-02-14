import pexpect
import socket
import time

TCP_IP = '127.0.0.1'
TCP_PORT = 5010
BUFFER_SIZE = 1024

p = pexpect.spawn('/home/pi/linux_libnfc-nci/nfcDemoApp poll', timeout=None)

for line in p:
	if "Text :" in line:
		print "Scan success"
		print "Connecting to TCP server"
		s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		s.connect((TCP_IP, TCP_PORT))
		print "Connected to server"
		NfcContent = line.strip()
		NfcContent = NfcContent.replace('Text :','')
		NfcContent = NfcContent.replace('	','')
		NfcContent = NfcContent.replace(' ','')
		NfcContent = NfcContent.replace("'",'')
		print "Sending \"" + NfcContent + "\" to server"
		s.send(NfcContent)
		serverData = s.recv(BUFFER_SIZE)
		print "Server echo:", serverData
		s.close()
		print "Connection to server closed"
	#if  "Lost" in line:
		#print "lost"
		#s.send("lost")
p.close()