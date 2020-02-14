import pexpect
import socket
import time

TCP_IP = '127.0.0.1'
TCP_PORT = 5010
BUFFER_SIZE = 1024

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect((TCP_IP, TCP_PORT))

p = pexpect.spawn('/home/pi/linux_libnfc-nci/nfcDemoApp poll', timeout=None)
for line in p:
	if "Text :" in line:
		s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		s.connect((TCP_IP, TCP_PORT))
		print "scan success"
		test = line.strip()
		test = test.replace('Text :','')
		test = test.replace('	','')
		test = test.replace(' ','')
		test = test.replace("'",'')
		print test
		s.send(test)
		data = s.recv(BUFFER_SIZE)
		print "received data:", data
	#if  "Lost" in line:
		#print "lost"
		#s.send("lost")
p.close()