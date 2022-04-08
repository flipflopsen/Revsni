import socket


class Server:
    port = 8866
    host = "localhost"

    def __init__(self):
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    def server_activate(self):
        self.socket.bind((self.host, self.port))
        self.socket.listen()

    def get_request(self):
        return self.socket.accept()

    def start_server(self):
        self.server_activate()
        print("--Server started...")

        conn, addr = self.get_request()

        with conn:
            print(f"Connected by {addr}")
            while True:
                data = conn.recv(1024)
                if not data:
                    break
                conn.sendall(data)

    def talk(self):
        pass

    def server_close(self):
        self.socket.close()
        print("...Server Stopped--")
