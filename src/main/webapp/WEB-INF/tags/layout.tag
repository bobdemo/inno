<%@tag description="Page Layout" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <title>Couchbase Inno </title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="The  Inno Layer Web App for ${bucket}">
    
    
    <link href="/${bucket}/resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="/${bucket}/resources/css/inno.css" rel="stylesheet">
    <link href="/${bucket}/resources/css/bootstrap-responsive.min.css" rel="stylesheet">
    <link rel="stylesheet" href="http://cdn.leafletjs.com/leaflet-0.7.3/leaflet.css" />
     <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
    <!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
      <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->
  </head>
  <body>
    <div class="container-narrow">
      <div class="masthead">
        <ul class="nav nav-pills pull-right">
          <li><a href="/${bucket}/welcome">Home</a></li>
        </ul>
        <h2 class="muted"><img src="/${bucket}/resources/img/inno.png" alt "Inno" /> Web App </h2>
      </div>
      <hr />
      <div class="row-fluid">
        <div class="span12">
            <jsp:doBody/>
        </div>
      </div>
      
      <div class="footer">
<h5>Alcuni collegamenti utili:</h5>
        <p>....</p>
        <p>....</p>
        <hr />        
        <p>Autore: xxxxxxxx <br /> Email: xxxxxxxxxxxx <br /> &copy; xxxx, 2015</p>
      </div>
    </div>
    <script src="/${bucket}/resources/js/bootstrap.min.js"></script>
    
   
    
  </body>
</html>