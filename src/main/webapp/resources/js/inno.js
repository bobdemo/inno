var bucket;

// estrazione centro mappa
if ( bbox && bbox.length > 28 )
    bbox = bbox.substring(28);
if ( bbox && bbox.length > 2 )
    bbox = bbox.substring(0,bbox.length-2);

var points = eval(bbox);

if ( points )  {
     var cx = (points[0][0]+points[2][0])/2;
     var cy = (points[0][1]+points[1][1])/2;
}

  
var center= [ cy, cx ];


var centero = [39.22, 9.11];

$(document).ready(function() {

// estrazione centro mappa
var point1 = bbox.match( /.*?[[((.*?)]]/ );

colorsById = { };
function draw_new( myCtx, geom, myId ) {
    var type = 3; //poly
    var coords_str = geom.match( /.*?\(\((.*?)\)\)/ );
    if ( geom.substring(0,1) === '1' )
         type = 1;
    else if ( geom.substring(0,1) === '2' )
         type = 2; 
    else if ( geom.substring(0,1) === '3' )
         type = 3;

    if ( ! coords_str )
         coords_str = geom.match( /.*?\((.*?)\)/ );

    var all_coords = coords_str[1].match(/.{1,2}/g);
        // Conversione hex -> dec
    var coords = [];
    for (i=0; i<(all_coords.length); i+=2) {
        var x = parseInt( all_coords[i], 16 );
        var y = parseInt( all_coords[i+1], 16 );
        if (x == 255) x = 256;
        else if (x == 0) {}
        else x += 0.5;

        if (y == 255) y = 256;
        else if (y == 0) {}
        else y += 0.5
        coords.push( x + ' ' + y );
    }

    var prevPoint = getPoint( coords[0] );
    if ( type === 3 || type == 2 ) {
            myCtx.beginPath();
            myCtx.moveTo( prevPoint.x, prevPoint.y );
            for (i=1; i<coords.length; i++) {
                    var thisPoint = getPoint( coords[i] );
      // +0.5 : shift delle geometrie di mezzo pixel, per coincidere
      // con il baricentro del pixel.
      //myCtx.lineTo( thisPoint.x + 0.5, thisPoint.y + 0.5 );
                    myCtx.lineTo( thisPoint.x , thisPoint.y );
            }	

            if ( type === 3 ) {
                    myCtx.closePath();
                    myCtx.fillStyle = getRandomColorById( myId );
                    myCtx.fill();
            } else if ( type === 2 ) {

                    myCtx.strokeStyle="#FF0000";
                    myCtx.lineWidth = 3;
                    myCtx.stroke();
            } 
    } 
    else if ( type === 1 ) {
        var grd = myCtx.createRadialGradient(prevPoint.x,prevPoint.y, 0, prevPoint.x, prevPoint.y, 4);
        grd.addColorStop(0, 'rgba(255,0,0,1)');
        grd.addColorStop(1, 'rgba(0,100,0,1)');
        myCtx.beginPath();
        myCtx.arc(prevPoint.x,prevPoint.y, 6, 0, 4 * Math.PI, false);
        myCtx.closePath();
        myCtx.fillStyle = grd;
        myCtx.fill();
    }  
}

function getPoint( str ) {
        var l = str.split(" ");
        var p = new Object;
        p['x'] = l[0] * 1;
        p['y'] = l[1] * 1;
        return p;
      }

function getRandomColor() {
        var randColor = (Math.random() * 0xFFFFFF << 0).toString(16);
        while (randColor.length < 6) {
          randColor = '0' + randColor;
        }
        return '#' + randColor;
      }

function getRandomColorById( myId ) {
	var c = colorsById[ myId ];
	if (!c) {
  		colorsById[ myId ] = getRandomColor();
  		c = colorsById[ myId ];
  		console.log( "colors data size: ", Object.keys(colorsById).length );
	}
	return c;
}
var map;

var OpenStreetMap_BlackAndWhite = L.tileLayer('http://{s}.tiles.wmflabs.org/bw-mapnik/{z}/{x}/{y}.png', {
	maxZoom: 17,
	attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
});


var myCanvasTiles = L.tileLayer.canvas();
myCanvasTiles.drawTile = function(canvas, tilePoint, zoom) {
	var cx = canvas.getContext('2d');
	cx.globalAlpha = 0.5;
	var myurl = 'http://inno.crs4.it/inno/json/tile/'+layername+'/' + tilePoint.x + '/' + tilePoint.y + '/' + zoom;
	var objData;
	$.ajax({
  		type: 'GET',
  		async: 'true',
  		dataType: 'html',    // jsoooooooooooooooooooooooooooooooooon
  		url: myurl,
  		success: function( data ) {
  			objData = jQuery.parseJSON( data );  // noooooooooooooooooooooooooooooooooooooooo
  			console.log( objData );

			if ( objData.id ){
        			var ss = objData.id.split(":");   // "comuni:539:391:10" - era: "539_385_10"
				var ok = false;
          			var obj_x = ss[1].split("+");
          			var obj_y = ss[2].split("+"); 
          			if ( obj_x.length > 1 ) {
             				console.log( obj_x[0]  + "," + obj_x[1] + "," + tilePoint.x  )
             				console.log( obj_y[0]  + "," + obj_y[1] + "," + tilePoint.y  )
             				if ( obj_x[0] <= tilePoint.x && obj_x[0] + obj_x[1] >= tilePoint.x &&
                		  	     obj_y[0] <= tilePoint.y + 1 && obj_y[0] + obj_y[1] >= tilePoint.y )
             				{
             					ok = true;  
             				} 
          			}
          			else if ( (tilePoint.x == ss[1]) && (tilePoint.y == ss[2])  ) 
          			{
                			ok = true;
         			} 
          			if (ok)
          			{
             				$.each(objData.objs, function (index, obj) {
                		 		draw_new( cx, obj.g, obj.id );
             				});
             				cx.font = '20pt Calibri';
             				cx.fillStyle = 'black';
             				cx.fillText( 'TILE: ' + tilePoint.x + "," + tilePoint.y, 30, 50 );
          			}
       			}
  		},
 	 	error: function(request, textStatus, errorThrown) {} 
	});
}



map = L.map('map',{maxZoom: 17}).setView(center, 15).addLayer( OpenStreetMap_BlackAndWhite ).addLayer( myCanvasTiles ).setZoom( 10 );
 
});
