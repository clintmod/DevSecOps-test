#!/usr/bin/env python3

import os
import sys
from http.server import SimpleHTTPRequestHandler
import socketserver

PORT = 8000

class Server(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory='static', **kwargs)

def main():
    with socketserver.TCPServer(("", PORT), Server) as httpd:
        print("http listening on port", PORT)
        httpd.serve_forever()


if __name__ == '__main__':
    try:
        main()
    except KeyboardInterrupt:
        sys.exit(0)
