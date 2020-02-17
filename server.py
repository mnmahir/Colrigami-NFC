#!/usr/bin/env python

import socket
import sys
import subprocess

TCP_IP = '127.0.0.1'
TCP_PORT = 5010
BUFFER_SIZE = 20  # Normally 1024, but we want fast response

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.bind((TCP_IP, TCP_PORT))
s.listen(1)

#Global Variable
data = ""
videoSW = 0

#Main
while True:
	conn, addr = s.accept()
	print '[SERVER] Connection address:', addr
	while True:
		data = conn.recv(BUFFER_SIZE)
		if not data: break
		print "[SERVER] Server received data:", data
		conn.send(data)  # echo
		if (videoSW == 0 and int(data) >=0 and int(data) <=2):
			print "[SERVER] Now playing video " +data
			vidpath = "/home/pi/Downloads/"+str(data)+".mp4"
			process = subprocess.Popen(['omxplayer', '-b', vidpath], stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, close_fds=True)
			videoSW = 1
			#stdout, stderr = process.communicate()
			#print stdout
	
		elif (data == "10" and videoSW == 1):
			print "[SERVER] Pause/Play video"
			process.stdin.write('p')
		elif (data == "11" and videoSW == 1):
			print "[SERVER] Decrease speed video"
			process.stdin.write('1')
		elif (data == "12" and videoSW == 1):
			print "[SERVER] Increase speed video"
			process.stdin.write('>')
		elif (data == "13" and videoSW == 1):
			print "[SERVER] Rewind video"
			process.stdin.write('\033[D')
		elif (data == "14" and videoSW == 1):
			print "[SERVER] Fast forward video"
			process.stdin.write('\033[C')
		elif (data == "15" and videoSW == 1):
			print "[SERVER] Stop playing video"
			process.stdin.write('q')
			videoSW = 0
		else:
			print "[SERVER] Sorry, invalid input"

	
conn.close()
