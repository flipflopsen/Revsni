import socket


<<<<<<< HEAD:Pyvsn/src/server/base_server.py
class BaseServer:
    port = 8866
=======
class Server:
    port = 8866
>>>>>>> 86ca6a6 (Blowfish implemented):Pyvsn/server/server.py
    host = "localhost"

    def __init__(self):
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    def server_activate(self):
        self.socket.bind((self.host, self.port))
        self.socket.listen()

    def start_server(self):
        self.server_activate()
        print("--Server started...")

    def talk(self):
        conn, addr = self.socket.accept()

        with conn:
            print(f"Connected by {addr}")
            while True:
                data = conn.recv(1024)
                if not data:
                    break
                conn.sendall(data)

    def server_close(self):
        self.socket.close()
        print("...Server Stopped--")
