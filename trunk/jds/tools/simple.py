from org.apache.lucene.index import * 
from org.apache.lucene.search import *
from org.apache.lucene.analysis.standard import *
from org.apache.lucene.document import *
from org.apache.lucene.queryParser import *

reader = IndexReader.open('../JDesktopSearch.index');
searcher = IndexSearcher(reader)
analyzer = StandardAnalyzer()
parser = QueryParser('path',analyzer)
query = parser.parse('newqq')

collector = TopDocCollector(10)
searcher.search(query, collector)
hits = collector.topDocs().scoreDocs

for i in hits:
    doc = searcher.doc(i.doc)
    #print doc.get('path').encode('utf8')
    #print doc.get('contents')
    
   
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


#listAll('exe') 
import os
print os.listdir('\\192.168.2.105')