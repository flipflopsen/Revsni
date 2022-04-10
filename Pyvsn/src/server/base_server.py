import socket


class Server:
    port = 8866
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
