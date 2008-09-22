/*
  GSV 1.0, by Michal Migurski <mike-gsv@teczno.com>
  $Id: gsv.js,v 1.6 2005/06/28 03:30:49 migurski Exp $

  Description:
    Generates a draggable and zoomable viewer for images that would be
    otherwise too-large for a browser window, e.g. maps or hi-res
    document scans. Images must be pre-cut into tiles by PowersOfTwo
    Python library.
   	
  Usage:
    For an HTML construct such as this:

        <div class="imageViewer">
            <div class="well"> </div>
            <div class="surface"> </div>
            <p class="status"> </p>
        </div>

    ...pass the DOM node for the top-level DIV, a directory name where
    tile images can be found, and an integer describing the height of
    each image tile to prepareViewer():

        prepareViewer(element, 'tiles', 256);
        
    It is expected that the visual behavior of these nodes is determined
    by a set of CSS rules.
    
    The "well" node is where generated IMG elements are appended. It
    should have the CSS rule "overflow: hidden", to occlude image tiles
    that have scrolled out of view.
    
    The "surface" node is the transparent mouse-responsive layer of the
    image viewer, and should match the well in size.
    
    The "status" node is generally set to "display: none", but can be
    shown when diagnostic information is desired. It's controlled by the
    displayStatus() function here.

  License:
    Copyright (c) 2005 Michal Migurski <mike-gsv@teczno.com>
    
    Redistribution and use in source form, with or without modification,
    are permitted provided that the following conditions are met:
    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
    2. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.
    
    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/   

function getEvent(event)
{
    if(event == undefined) {
        return window.event;
    }
    
    return event;
}

function prepareViewer(imageViewer, tileDir, tileSize)
{
    for(var child = imageViewer.firstChild; child; child = child.nextSibling) {
        if(child.className == 'surface') {
            imageViewer.activeSurface = child;
            child.imageViewer = imageViewer;
        
        } else if(child.className == 'well') {
            imageViewer.tileWell = child;
            child.imageViewer = imageViewer;
        
        } else if(child.className == 'status') {
            imageViewer.status = child;
            child.imageViewer = imageViewer;
        
        }
    }
    
    var width = imageViewer.offsetWidth;
    var height = imageViewer.offsetHeight;
    var zoomLevel = -1; // guaranteed at least one increment below, so start at less-than-zero
    var fullSize = tileSize * Math.pow(2, zoomLevel); // full pixel size of the image at this zoom level
    do {
        zoomLevel += 1;
        fullSize *= 2;
    } while(fullSize < Math.max(width, height));

    var center = {'x': ((fullSize - width) / -2), 'y': ((fullSize - height) / -2)}; // top-left pixel of viewer, if it were to be centered in the view window

    imageViewer.style.width = width+'px';
    imageViewer.style.height = height+'px';
    
    var top = 0;
    var left = 0;
    for(var node = imageViewer; node; node = node.offsetParent) {
        top += node.offsetTop;
        left += node.offsetLeft;
    }

    imageViewer.dimensions = {

         // width and height of the viewer in pixels
         'width': width, 'height': height,

         // position of the viewer in the document, from the upper-left corner
         'top': top, 'left': left,

         // location and height of each tile; they're always square
         'tileDir': tileDir, 'tileSize': tileSize,
         'mapType': 'tbmap', 'mapName': '',
         // zero or higher; big number == big image, lots of tiles
         'zoomLevel': zoomLevel,

         // initial viewer position
         // defined as window-relative x,y coordinate of upper-left hand corner of complete image
         // usually negative. constant until zoomLevel changes
         'x': center.x, 'y': center.y

         };

    imageViewer.start = {'x': 0, 'y': 0}; // this is reset each time that the mouse is pressed anew
    imageViewer.pressed = false;

    if(document.body.imageViewers == undefined) {
        document.body.imageViewers = [imageViewer];
        document.body.onmouseup = releaseViewer;

    } else {
        document.body.imageViewers.push(imageViewer);
    
    }

    prepareTiles(imageViewer);
}


function prepareTiles(imageViewer)
{
    var activeSurface = imageViewer.activeSurface;
    var tileWell = imageViewer.tileWell;
    var dim = imageViewer.dimensions;

    imageViewer.tiles = [];
    
    var rows = Math.ceil(dim.height / dim.tileSize) + 1;
    var cols = Math.ceil(dim.width / dim.tileSize) + 1;
    
    displayStatus(imageViewer, 'rows: '+rows+', cols: '+cols);
    
    for(var c = 0; c < cols; c += 1) {
        var tileCol = [];
    
        for(var r = 0; r < rows; r += 1) {

            var tile = {'c': c, 'r': r, 'img': document.createElement('img'), 'imageViewer': imageViewer};

            tile.img.className = 'tile';
            tile.img.style.width = dim.tileSize+'px';
            tile.img.style.height = dim.tileSize+'px';
            setTileImage(tile, true);
            
            tileWell.appendChild(tile.img);
            tileCol.push(tile);
        }
        
        imageViewer.tiles.push(tileCol);
    }
    
    activeSurface.onmousedown = pressViewer;
    positionTiles(imageViewer, {'x': 0, 'y': 0}); // x, y should match imageViewer.start x, y
}

function positionTiles(imageViewer, mouse)
{
    var tiles = imageViewer.tiles;
    var dim = imageViewer.dimensions;
    var start = imageViewer.start;
    
    var statusTextLines = [];
    statusTextLines.push('imageViewer.dimensions x,y: '+dim.x+','+dim.y);
    
    for(var c = 0; c < tiles.length; c += 1) {
        for(var r = 0; r < tiles[c].length; r += 1) {

            var tile = tiles[c][r];
            
            // wrappedAround will become true if any tile has to be wrapped around
            var wrappedAround = false;
            
            tile.x = (tile.c * dim.tileSize) + dim.x + (mouse.x - start.x);
            tile.y = (tile.r * dim.tileSize) + dim.y + (mouse.y - start.y);
            
            if(tile.x > dim.width) {
                // tile is too far to the right
                // shift it to the far-left until it's within the viewer window
                do {
                    tile.c -= tiles.length;
                    tile.x = (tile.c * dim.tileSize) + dim.x + (mouse.x - start.x);
                    wrappedAround = true;

                } while(tile.x > dim.width);

            } else {
                // tile may be too far to the right
                // if it is, shift it to the far-right until it's within the viewer window
                while(tile.x < (-1 * dim.tileSize)) {
                    tile.c += tiles.length;
                    tile.x = (tile.c * dim.tileSize) + dim.x + (mouse.x - start.x);
                    wrappedAround = true;

                }
            }
            
            if(tile.y > dim.height) {
                // tile is too far down
                // shift it to the very top until it's within the viewer window
                do {
                    tile.r -= tiles[c].length;
                    tile.y = (tile.r * dim.tileSize) + dim.y + (mouse.y - start.y);
                    wrappedAround = true;

                } while(tile.y > dim.height);

            } else {
                // tile may be too far up
                // if it is, shift it to the very bottom until it's within the viewer window
                while(tile.y < (-1 * dim.tileSize)) {
                    tile.r += tiles[c].length;
                    tile.y = (tile.r * dim.tileSize) + dim.y + (mouse.y - start.y);
                    wrappedAround = true;

                }
            }

            statusTextLines.push('tile '+r+','+c+' at '+tile.c+','+tile.r);
            
            // set the tile image once to *maybe* null, then again to
            // definitely the correct tile. this removes the wraparound
            // artifacts seen over slower connections.
            setTileImage(tile, wrappedAround);
            setTileImage(tile, false);

            tile.img.style.top = tile.y+'px';
            tile.img.style.left = tile.x+'px';
        }
    }
    
    displayStatus(imageViewer, statusTextLines.join('<br>'));
}

function setTileImage(tile, nullOverride)
{
    var dim = tile.imageViewer.dimensions;

    // request a particular image slice
    var src = dim.tileDir;
    var n = dim.mapName;
    if (dim.mapType == 'tbmap') {
      var z = 6-dim.zoomLevel;
      n = n+ z;
      var nn = n+'000001';
      src += '/'+n+'/'+nn+'/set/'+nn+'_'+tile.c*256+'_'+tile.r*256+'.png';
    } else {
      // for gmaps not finished yet
      src = '/v=2.83'+'&x='+tile.c+'&y='+tile.r+'&z='+dim.zoomLevel+'&s=';
    }
    // has the image been scrolled too far in any particular direction?
    var left = tile.c < 0;
    var high = tile.r < 0;
    var right = tile.c >= Math.pow(2, tile.imageViewer.dimensions.zoomLevel);
    var low = tile.r >= Math.pow(2, tile.imageViewer.dimensions.zoomLevel);
    //var outside = high || left || low || right;
    var outside = high || left

         if(nullOverride)     { src = 'null/none.png';          }

    // note this "outside" clause overrides all those below
    else if(outside)          { src = 'null/none.png';          }

    else if(high && left)     { src = 'null/top-left.png';      }
    else if(low  && left)     { src = 'null/bottom-left.png';   }
    else if(high && right)    { src = 'null/top-right.png';     }
    //else if(low  && right)    { src = 'null/bottom-right.png';  }
    else if(high)             { src = 'null/top.png';           }
    //else if(right)            { src = 'null/right.png';         }
    //else if(low)              { src = 'null/bottom.png';        }
    else if(left)             { src = 'null/left.png';          }

    tile.img.src = src;
}

function moveViewer(event)
{
    var imageViewer = this.imageViewer;
    var ev = getEvent(event);
    var mouse = localizeCoordinates(imageViewer, {'x': ev.clientX, 'y': ev.clientY});

    displayStatus(imageViewer, 'mouse at: '+mouse.x+', '+mouse.y+', '+(imageViewer.tiles.length * imageViewer.tiles[0].length)+' tiles to process');
    positionTiles(imageViewer, {'x': mouse.x, 'y': mouse.y});
}

function localizeCoordinates(imageViewer, client)
{
    var local = {'x': client.x, 'y': client.y};

    for(var node = imageViewer; node; node = node.offsetParent) {
        local.x -= node.offsetLeft;
        local.y -= node.offsetTop;
    }
    
    return local;
}

function pressViewer(event)
{
    var imageViewer = this.imageViewer;
    var dim = imageViewer.dimensions;
    var ev = getEvent(event);
    var mouse = localizeCoordinates(imageViewer, {'x': ev.clientX, 'y': ev.clientY});

    imageViewer.pressed = true;
    imageViewer.tileWell.style.cursor = imageViewer.activeSurface.style.cursor = 'move';
    
    imageViewer.start = {'x': mouse.x, 'y': mouse.y};
    this.onmousemove = moveViewer;

    displayStatus(imageViewer, 'mouse pressed at '+mouse.x+','+mouse.y);
}

function releaseViewer(event)
{
    var ev = getEvent(event);
    
    for(var i = 0; i < document.body.imageViewers.length; i += 1) {
        var imageViewer = document.body.imageViewers[i];
        var mouse = localizeCoordinates(imageViewer, {'x': ev.clientX, 'y': ev.clientY});
        var dim = imageViewer.dimensions;

        if(imageViewer.pressed) {
            imageViewer.activeSurface.onmousemove = null;
            imageViewer.tileWell.style.cursor = imageViewer.activeSurface.style.cursor = 'default';
            imageViewer.pressed = false;

            dim.x += (mouse.x - imageViewer.start.x);
            dim.y += (mouse.y - imageViewer.start.y);
        }

        displayStatus(imageViewer, 'mouse dragged from '+imageViewer.start.x+', '+imageViewer.start.y+' to '+mouse.x+','+mouse.y+'. image: '+dim.x+','+dim.y);
    }
}

function displayStatus(imageViewer, message)
{
    imageViewer.status.innerHTML = message;
}

function dumpInfo(imageViewer)
{
    var dim = imageViewer.dimensions;
    var tiles = imageViewer.tiles;

    var statusTextLines = ['imageViewer '+(i + 1), 'current window position: '+dim.x+','+dim.y+'.', '----'];

    for(var c = 0; c < tiles.length; c += 1) {
        for(var r = 0; r < tiles[c].length; r += 1) {
            statusTextLines.push('image ('+c+','+r+') has tile ('+dim.zoomLevel+','+tiles[c][r].c+','+tiles[c][r].r+')');
        }
    }
    
    alert(statusTextLines.join("\n"));
}

function dumpAllInfo()
{
    for(var i = 0; i < document.body.imageViewers.length; i += 1) {
        dumpInfo(document.body.imageViewers[i]);
    }
}

function zoomImage(imageViewer, mouse, direction)
{
    var dim = imageViewer.dimensions;
    
    if(mouse == undefined) {
        var mouse = {'x': dim.width / 2, 'y': dim.height / 2};
    }

    var pos = {'before': {'x': 0, 'y': 0}};

    // pixel position within the image is a function of the
    // upper-left-hand corner of the viewe in the page (pos.before),
    // the click position (event), and the image position within
    // the viewer (dim).
    pos.before.x = (mouse.x - pos.before.x) - dim.x;
    pos.before.y = (mouse.y - pos.before.y) - dim.y;
    pos.before.width = pos.before.height = Math.pow(2, dim.zoomLevel) * dim.tileSize;
    
    var statusMessage = ['at current zoom level, image is '+pos.before.width+' pixels wide',
                         '...mouse position is now '+pos.before.x+','+pos.before.y+' in the full image at zoom '+dim.zoomLevel,
                         '...with the corner at '+dim.x+','+dim.y];

    if(dim.zoomLevel + direction >= 0) {
        pos.after = {'width': (pos.before.width * Math.pow(2, direction)), 'height': (pos.before.height * Math.pow(2, direction))};
        statusMessage.push('at zoom level '+(dim.zoomLevel + direction)+', image is '+pos.after.width+' pixels wide');

        pos.after.x = pos.before.x * Math.pow(2, direction);
        pos.after.y = pos.before.y * Math.pow(2, direction);
        statusMessage.push('...so the current mouse position would be '+pos.after.x+','+pos.after.y);

        pos.after.left = mouse.x - pos.after.x;
        pos.after.top = mouse.y - pos.after.y;
        statusMessage.push('...with the corner at '+pos.after.left+','+pos.after.top);
        
        dim.x = pos.after.left;
        dim.y = pos.after.top;
        dim.zoomLevel += direction;
        
        imageViewer.start = mouse;
        positionTiles(imageViewer, mouse);
    }

    displayStatus(imageViewer, statusMessage.join('<br>'));
}

function zoomImageUp(imageViewer, mouse)
{
    zoomImage(imageViewer, mouse, 1);
}

function zoomImageDown(imageViewer, mouse)
{
    zoomImage(imageViewer, mouse, -1);
}


/*
   Behaviour v1.0 by Ben Nolan, June 2005. Based largely on the work
   of Simon Willison (see comments by Simon below).

   Description:
   	
   	Uses css selectors to apply javascript behaviours to enable
   	unobtrusive javascript in html documents.
   	
   Usage:   
   
	var myrules = {
		'b.someclass' : function(element){
			element.onclick = function(){
				alert(this.innerHTML);
			}
		},
		'#someid u' : function(element){
			element.onmouseover = function(){
				this.innerHTML = "BLAH!";
			}
		}
	);
	
	Behaviour.register(myrules);
	
	// Call Behaviour.apply() to re-apply the rules (if you
	// update the dom, etc).

   License:
   
   	My stuff is BSD licensed. Not sure about Simon's.
   	
   More information:
   	
   	http://ripcord.co.nz/behaviour/
   
*/   

var Behaviour = {
	list : new Array,
	
	register : function(sheet){
		Behaviour.list.push(sheet);
	},
	
	start : function(){
		Behaviour.addLoadEvent(function(){
			Behaviour.apply();
		});
	},
	
	apply : function(){
		for (h=0;sheet=Behaviour.list[h];h++){
			for (selector in sheet){
				list = document.getElementsBySelector(selector);
				
				if (!list){
					continue;
				}

				for (i=0;element=list[i];i++){
					sheet[selector](element);
				}
			}
		}
	},
	
	addLoadEvent : function(func){
		var oldonload = window.onload;
		
		if (typeof window.onload != 'function') {
			window.onload = func;
		} else {
			window.onload = function() {
				oldonload();
				func();
			}
		}
	}
}

Behaviour.start();

/*
   The following code is Copyright (C) Simon Willison 2004.

   document.getElementsBySelector(selector)
   - returns an array of element objects from the current document
     matching the CSS selector. Selectors can contain element names, 
     class names and ids and can be nested. For example:
     
       elements = document.getElementsBySelect('div#main p a.external')
     
     Will return an array of all 'a' elements with 'external' in their 
     class attribute that are contained inside 'p' elements that are 
     contained inside the 'div' element which has id="main"

   New in version 0.4: Support for CSS2 and CSS3 attribute selectors:
   See http://www.w3.org/TR/css3-selectors/#attribute-selectors

   Version 0.4 - Simon Willison, March 25th 2003
   -- Works in Phoenix 0.5, Mozilla 1.3, Opera 7, Internet Explorer 6, Internet Explorer 5 on Windows
   -- Opera 7 fails 
*/

function getAllChildren(e) {
  // Returns all children of element. Workaround required for IE5/Windows. Ugh.
  return e.all ? e.all : e.getElementsByTagName('*');
}

document.getElementsBySelector = function(selector) {
  // Attempt to fail gracefully in lesser browsers
  if (!document.getElementsByTagName) {
    return new Array();
  }
  // Split selector in to tokens
  var tokens = selector.split(' ');
  var currentContext = new Array(document);
  for (var i = 0; i < tokens.length; i++) {
    token = tokens[i].replace(/^\s+/,'').replace(/\s+$/,'');;
    if (token.indexOf('#') > -1) {
      // Token is an ID selector
      var bits = token.split('#');
      var tagName = bits[0];
      var id = bits[1];
      var element = document.getElementById(id);
      if (tagName && element.nodeName.toLowerCase() != tagName) {
        // tag with that ID not found, return false
        return new Array();
      }
      // Set currentContext to contain just this element
      currentContext = new Array(element);
      continue; // Skip to next token
    }
    if (token.indexOf('.') > -1) {
      // Token contains a class selector
      var bits = token.split('.');
      var tagName = bits[0];
      var className = bits[1];
      if (!tagName) {
        tagName = '*';
      }
      // Get elements matching tag, filter them for class selector
      var found = new Array;
      var foundCount = 0;
      for (var h = 0; h < currentContext.length; h++) {
        var elements;
        if (tagName == '*') {
            elements = getAllChildren(currentContext[h]);
        } else {
            elements = currentContext[h].getElementsByTagName(tagName);
        }
        for (var j = 0; j < elements.length; j++) {
          found[foundCount++] = elements[j];
        }
      }
      currentContext = new Array;
      var currentContextIndex = 0;
      for (var k = 0; k < found.length; k++) {
        if (found[k].className && found[k].className.match(new RegExp('\\b'+className+'\\b'))) {
          currentContext[currentContextIndex++] = found[k];
        }
      }
      continue; // Skip to next token
    }
    // Code to deal with attribute selectors
    if (token.match(/^(\w*)\[(\w+)([=~\|\^\$\*]?)=?"?([^\]"]*)"?\]$/)) {
      var tagName = RegExp.$1;
      var attrName = RegExp.$2;
      var attrOperator = RegExp.$3;
      var attrValue = RegExp.$4;
      if (!tagName) {
        tagName = '*';
      }
      // Grab all of the tagName elements within current context
      var found = new Array;
      var foundCount = 0;
      for (var h = 0; h < currentContext.length; h++) {
        var elements;
        if (tagName == '*') {
            elements = getAllChildren(currentContext[h]);
        } else {
            elements = currentContext[h].getElementsByTagName(tagName);
        }
        for (var j = 0; j < elements.length; j++) {
          found[foundCount++] = elements[j];
        }
      }
      currentContext = new Array;
      var currentContextIndex = 0;
      var checkFunction; // This function will be used to filter the elements
      switch (attrOperator) {
        case '=': // Equality
          checkFunction = function(e) { return (e.getAttribute(attrName) == attrValue); };
          break;
        case '~': // Match one of space seperated words 
          checkFunction = function(e) { return (e.getAttribute(attrName).match(new RegExp('\\b'+attrValue+'\\b'))); };
          break;
        case '|': // Match start with value followed by optional hyphen
          checkFunction = function(e) { return (e.getAttribute(attrName).match(new RegExp('^'+attrValue+'-?'))); };
          break;
        case '^': // Match starts with value
          checkFunction = function(e) { return (e.getAttribute(attrName).indexOf(attrValue) == 0); };
          break;
        case '$': // Match ends with value - fails with "Warning" in Opera 7
          checkFunction = function(e) { return (e.getAttribute(attrName).lastIndexOf(attrValue) == e.getAttribute(attrName).length - attrValue.length); };
          break;
        case '*': // Match ends with value
          checkFunction = function(e) { return (e.getAttribute(attrName).indexOf(attrValue) > -1); };
          break;
        default :
          // Just test for existence of attribute
          checkFunction = function(e) { return e.getAttribute(attrName); };
      }
      currentContext = new Array;
      var currentContextIndex = 0;
      for (var k = 0; k < found.length; k++) {
        if (checkFunction(found[k])) {
          currentContext[currentContextIndex++] = found[k];
        }
      }
      // alert('Attribute Selector: '+tagName+' '+attrName+' '+attrOperator+' '+attrValue);
      continue; // Skip to next token
    }
    
    if (!currentContext[0]){
    	return;
    }
    
    // If we get here, token is JUST an element (not a class or ID selector)
    tagName = token;
    var found = new Array;
    var foundCount = 0;
    for (var h = 0; h < currentContext.length; h++) {
      var elements = currentContext[h].getElementsByTagName(tagName);
      for (var j = 0; j < elements.length; j++) {
        found[foundCount++] = elements[j];
      }
    }
    currentContext = found;
  }
  return currentContext;
}

/* That revolting regular expression explained 
/^(\w+)\[(\w+)([=~\|\^\$\*]?)=?"?([^\]"]*)"?\]$/
  \---/  \---/\-------------/    \-------/
    |      |         |               |
    |      |         |           The value
    |      |    ~,|,^,$,* or =
    |   Attribute 
   Tag
*/

/*
        Behaviour.register({
            '.imageViewer' : function(el) {
                prepareViewer(el, 'test-gmap-gen/atlases/20080716_225929/boulder3/boulder3000001/set', 256);
            },
            '.imageViewer .zoom .up' : function(el) {
                el.onclick = function() {
                    zoomImageUp(el.parentNode.parentNode, undefined);
                    return false;
                }
            },
            '.imageViewer .zoom .down' : function(el) {
                el.onclick = function() {
                    zoomImageDown(el.parentNode.parentNode, undefined);
                    return false;
                }
            },
            '.imageViewer .zoom .dump' : function(el) {
                el.onclick = function() {
                    dumpInfo(el.parentNode.parentNode);
                    return false;
                }
            }
        });

*/

