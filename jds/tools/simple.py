from org.apache.lucene.index import * 
from org.apache.lucene.search import *
from org.apache.lucene.analysis.standard import *
from org.apache.lucene.document import *
from org.apache.lucene.queryParser import *
idxDir = '../JDesktopSearch.index'
reader = IndexReader.open(idxDir);
searcher = IndexSearcher(reader)
writer = IndexWriter(idxDir, StandardAnalyzer(), 255)
def search(key, value):
    analyzer = StandardAnalyzer()
    parser = QueryParser(key,analyzer)
    query = parser.parse(value)

    collector = TopDocCollector(10)
    searcher.search(query, collector)
    hits = collector.topDocs().scoreDocs
    r = []
    for i in hits:
        doc = searcher.doc(i.doc)
        r += [doc]
        #print doc.get('contents')
    return r
   
import string   
from java.lang import String

def listAll(key='*'): 
    for i in xrange(searcher.maxDoc()):
        d = searcher.doc(i)
        path = d.get('path')
        #c = d.get('contents')
        if key != '*' and -1 == path.find(key): continue
        #print c.encode('utf8')
        tt = string.maketrans(" ;,[]/+_-:()@.\\\t","                ")    
        #print 'keys:',d.getFields('keywords')
        print path.encode('utf8').translate(tt).split()
        print 'keys',
        for f in d.getFields('keywords'):
          print f.stringValue(),
        print

def getBase(p):
    return p.split('/')[2]

#listAll('exe') 
#import os
#print os.listdir(r'\\192.168.2.105')
print reader.maxDoc()


for d in search('keywords','aaa'):
    path = d.get('path')
    print [path]
    #writer.deleteDocuments(Term('path',path))
writer.close()
reader.close()
reader = IndexReader.open(idxDir);   
searcher = IndexSearcher(reader)
listAll()
