package it.crs4.inno.layer;
/**
 * Copyright (C) 2014 CRS4
 *
 * @author Roberto Demontis
 */

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;
import com.couchbase.client.protocol.views.View;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The LayerServlet handles inno layers info-related HTTP Queries.
 * 
 * The LayerServlet is used to handle info HTTP queries to a inno
 * layer couchbase bucket with json response
 */
public class HTMLServlet extends HttpServlet {

  /**
   * Obtains the current CouchbaseClient connection.
   */
  final CouchbaseClient client = ConnectionManager.getInstance();
  
  /**
   * Obtains the cbucket name.
   */
  final String name = ConnectionManager.getBucketName();
  
  /**
   * Google GSON is used for JSON encoding/decoding.
   */
  final Gson gson = new Gson();

  
  
  /**
   * Dispatch all incoming GET HTTP requests.
   *
   * @param request the HTTP request object.
   * @param response the HTTP response object.
   * @throws ServletException
   * @throws IOException
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    try {
         Logger.getLogger(HTMLServlet.class.getName()).log(Level.INFO,  
                        request.getPathInfo() ) ;
         if ( request.getPathInfo() != null && request.getPathInfo().startsWith("/map/") )
             handleMap( request, response );
         else handleNone(request,response);
    } catch (ServletException ex) {
        Logger.getLogger(HTMLServlet.class.getName()).log(
        Level.SEVERE, null, ex);
        handleError(request,response,1);
    }  
  }

  /**
   *
   * @param request
   * @param response
   * @throws ServletException
   * @throws IOException
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
      doGet(request,response);
  }
  
  
  
  /**
   * Handle the /json/tile/<layer_name>/<x>/<y>/<zoom> action
   * 
   * This method loads up and returns a JSON document based on the given tile 
   * coords when the layer isn't a polygon layer 
   * 
   * @param request the HTTP request object.
   * @param response the HTTP response object.
   * @throws IOException
   * @throws ServletException
   */
  private void handleMap(HttpServletRequest request,
    HttpServletResponse response) throws IOException, ServletException {
      // global request: /json/html/map/<layername>  
      // getPathInfo()( /map/<layername>  )
      if ( request.getPathInfo().length() > 5 ) {
            HashMap<String, String> parsedDoc;
            String layername = null;
            String document;
            String[] values = request.getPathInfo().substring(5).split("/");
            try {    
                if ( values.length == 1 ) 
                    layername = values[0];
                
            } catch ( Exception exc ) {
                Logger.getLogger(HTMLServlet.class.getName()).log(Level.INFO,  
                        String.format("ERROR BAD PARAMETERS IN :(%s)",
                        request.getPathInfo()) ) ;
            }
            if ( layername != null ) {
                HashMap<String, String> info = new HashMap<String, String>();
                document = (String) client.get(layername);
                if ( document != null ) {
                    parsedDoc = gson.fromJson( document, HashMap.class );
                    info.put("id", layername);
                    info.put("description", parsedDoc.get("description"));
                    info.put("_bbox_", parsedDoc.get("_bbox_"));
                    info.put("vertices", parsedDoc.get("vertices"));
                    info.put("count", parsedDoc.get("count"));
                    info.put("type", parsedDoc.get("type"));
                }
                request.setAttribute("layername", layername);
                request.setAttribute("info", info);
                request.setAttribute("bucket", name);
                request.getRequestDispatcher("/WEB-INF/welcome/map.jsp")
                       .forward(request, response);
                return;
            }
            else handleError(request,response,1);
       }
       handleError(request,response,1);
       
  }
  
  /**
   * Handle the not correct /json/* action  
   *
   * This method get the index page for show action
   *
   * @param request the HTTP request object.
   * @param response the HTTP response object.
   * @throws IOException
   * @throws ServletException
   */
  private void handleNone(HttpServletRequest request, HttpServletResponse response) 
     throws IOException, ServletException {
          View view = client.getView("innospatial", "byname");
      Query query = new Query();
      query.setIncludeDocs(true).setLimit(20);
      ViewResponse result = client.query(view, query);

      HashMap<String, String> info = new HashMap<String, String>(); 
      for( ViewRow row : result ) {
           HashMap<String, String> parsedDoc = gson.fromJson(
            (String)row.getDocument(), HashMap.class);
            info.put(parsedDoc.get("_innoname_"), parsedDoc.get("type") );
      }
      request.setAttribute("info", info);
      request.setAttribute("bucket", name);
      request.getRequestDispatcher("/WEB-INF/welcome/index.jsp").forward(request, response);
  
  }
  
  /**
   * Handle a not correct /json/* action  
   *
   * This method get the index page for show action
   *
   * @param request the HTTP request object.
   * @param response the HTTP response object.
   * @throws IOException
   * @throws ServletException
   */
  private void handleError(HttpServletRequest request, HttpServletResponse response, Integer type) 
     throws IOException, ServletException {
          request.getRequestDispatcher("/WEB-INF/welcome/error.jsp")
               .forward(request, response);
  }
   
                 
}
