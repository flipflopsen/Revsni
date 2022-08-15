from server.base_server import BaseServer
import selectors
import types


class MultiServer(BaseServer):

    selector = selectors.DefaultSelector()

    def __int__(self):
        BaseServer.__init__(self)

    def start_server(self):
        self.server_activate()
        print("--Server started...")
        self.selector.register(self.socket, selectors.EVENT_READ, data=None)
        self.handler()

    def handler(self):
        try:
            while True:
                events = self.selector.select(timeout=None)
                for key, mask in events:
                    if key.data is None:
                        self.accept_wrapper(key.fileobj)
                    else:
                        self.service_connection(key, mask)

        except KeyboardInterrupt:
            print("Keyboard interrupt")
        finally:
            self.selector.close()

    def accept_wrapper(self, socket):
        conn, addr = socket.accept()
        print(f"Accepted connection from {addr}")
        self.socket.setblocking(0)
        data = types.SimpleNamespace(addr=addr, inb=b"", outb=b"")
        events = selectors.EVENT_READ | selectors.EVENT_WRITE
        self.selector.register(conn, events, data=data)

    def service_connection(self, key, mask):
        socket = key.fileobj
        data = key.data
        if mask & selectors.EVENT_READ:
            recv_data = socket.recv(1024)  # Should be ready to read
            if recv_data:
                data.outb += recv_data
            else:
                print(f"Closing connection to {data.addr}")
                self.selector.unregister(socket)
                socket.close()
        if mask & selectors.EVENT_WRITE:
            if data.outb:
                print(f"Echoing {data.outb!r} to {data.addr}")
                sent = socket.send(data.outb)  # Should be ready to write
                data.outb = data.outb[sent:]
