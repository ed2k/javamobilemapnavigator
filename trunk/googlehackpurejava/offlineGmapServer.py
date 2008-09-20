# python web server to convert images from gmaps caches to serve GSV client
#

import os
import glob

try:
    from cStringIO import StringIO
except ImportError:
    from StringIO import StringIO


import subprocess


photo_path = '/tiles/'
www_root = './'


def get_file(path):
    mode = 'rb'
    f = glob.glob(path+'*')
    if len(f) == 0: return ''
    f = f[0]
    print f
    return open(f,mode).read()

from BaseHTTPServer import BaseHTTPRequestHandler, HTTPServer
import urllib


def br(): return '<br/>'
def img(path):
    return '<img src='+path+' width= height=/></a> '+' '.join(u)

guess_type = {'.png':'image/png','.jpg':'image/jpeg'}



class MyHandler(BaseHTTPRequestHandler):
    ' web ui for phone and photo tool '
    def do_GET(self):
        self.send_response(200)
        if self.path[:7] == '/tiles/':
            self.send_header("Conten-type","image/png")            
            self.end_headers()
            scale,x,y = [int(t) for t in (self.path[7+5:-4].split('-'))]
            print scale,x,y
            root = 'mt/'
            s = 2+scale
            
            f = './%sv=w2.83&hl=en&x=%d&y=%d&z=%d&s=' % (root,x,y,s)
            if s == 15:
               f = './%sv=w2.83&hl=en&x=%d&s=&y=%d&z=%d&s=' % (root,x,y,s)
            print f
	    self.wfile.write(get_file(f))
	    return
        else:
            self.send_header("Content-type","text/html")
        self.end_headers()
        
        r = get_file(www_root + 'gsv.js.htm')
        self.wfile.write(r)


def myserver():
 try:
     server = HTTPServer(('', 80), MyHandler)
     print 'started httpserver...'
     server.serve_forever()
 except:
     print '^C received, shutting down server'
     server.socket.close()

if __name__ == '__main__':
    myserver()    
