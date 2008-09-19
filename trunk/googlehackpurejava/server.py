# python web server to convert images from trekbuddy to server GSV client
#

# copy from SimpleHTTPServer.py
import os
import posixpath
import BaseHTTPServer
import urllib
import urlparse
import cgi
import shutil
import mimetypes
try:
    from cStringIO import StringIO
except ImportError:
    from StringIO import StringIO


import subprocess


photo_path = '/tiles/'
www_root = './'

def tag(name,attr=None,value=""):
    pass

def get_file(path):
    mode = 'r'
    if path[-3:] in ['jpg','png']: mode = 'rb'
    if not os.path.exists(path): return ''
    return open(path,mode).read()

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
            root = 'test-gmap-gen/atlases/20080717_141107/'
            loc = 'montreal'
            s = str(8-scale)
            n = loc+s+'000001'
            f = root+loc+s+'/'+n+'/set/'+n+'_'+str(x*256)+'_'+str(y*256)+'.png'
            print f
	    self.wfile.write(get_file(f))
	    return
        else:
            self.send_header("Content-type","text/html")
        self.end_headers()
        
        r = get_file(www_root + 'gsv.js.htm')
        self.wfile.write(r)

def simple_one_line_web_server_test():
 #from BaseHTTPServer import HTTPServer
 #from SimpleHTTPServer import SimpleHTTPRequestHandler
 BaseHTTPServer.HTTPServer(('',80),SimpleHTTPServer.SimpleHTTPRequestHandler).serve_forever()


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
