package it.crs4.inno.layer;
/**
 * Copyright (C) 2014 CRS4
 *
 * @author Roberto Demontis
 */

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.OnError;
import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.SpatialView;
import com.couchbase.client.protocol.views.Stale;
import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;
import com.couchbase.client.protocol.views.View;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
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
public class JSONServlet extends HttpServlet {

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
    // It verifies the type of the request 
    // Verifica il tipo di richiesta
    try {
         if ( request.getPathInfo() != null ){
            if ( request.getPathInfo().startsWith("/tile/") )
         // Request of a tile with geometries 
         // Richiesta di una tile con le geometrie   
                handleTile( request, response );
            else if (  request.getPathInfo().startsWith("/list") )
         // Request for the list of layers 
         // Richiesta della lista degli strati informativi    
                handleList( request, response );
            else if ( request.getPathInfo().startsWith("/layer/") )
         // Request of metadata informations about a layer 
         // Richiesta dei metadati con le informazioni di uno strato informativo    
                handleLayer( request, response );
            else if ( request.getPathInfo().startsWith("/value/") )
         // Request of values of an attribute of the layer for a tile 
         // Richiesta dei valori di un attributo di uno strato informativo per un tassello   
                handleValues( request, response );
            else if ( request.getPathInfo().startsWith("/infos") )
         // Request of values of all attributes of the layer for a tile 
         // Richiesta dei valori di tutti gli attributi di uno strato informativo per un tassello   
                handleInfos( request, response );
            else if ( request.getPathInfo().startsWith("/feature/") )
         // Request of values of all attributes for feature with specific key
         // Richiesta delle informazioni di un elemento con chiave specificata    
                handleFeature( request, response );
            else handleNone(request,response);
         }
         else handleNone(request,response);
    } catch (ServletException ex) {
        Logger.getLogger(JSONServlet.class.getName()).log(
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
  private void handleTile(HttpServletRequest request,
    HttpServletResponse response) throws IOException, ServletException {
      // global request: /json/tile/<layername>/<x>/<y>/<zoom>/(<page>)  
      // getPathInfo()( /tile/<layername>/<x>/<y>/<zoom>/(<page>) )
      
      // It verifies the number of parameters 
      // Verifica il numero dei parametri
      if ( request.getPathInfo().length() > 6 ) {
            String tile_id = null;
            String layername = null;
            String document = null;
            int page = 0;
            Integer x=null,y=null,zoom=null;
            String[] values = request.getPathInfo().substring(6).split("/");
            try {  
                // It verifies the number of parameters 
                if ( values.length > 3 && values.length < 6 ) {
                    zoom = new Integer(values[3]);
                    x = new Integer(values[1]);
                    y = new Integer(values[2]);
                    layername = values[0];
                    tile_id = layername + ":" + x + ":" + y + ":" + zoom;
                    if ( values.length == 5 ){
                        page = Integer.parseInt(values[4]);
                        tile_id = tile_id + ":" + page;
                    }    
                    
                }
            } catch ( Exception exc ) {
                Logger.getLogger(JSONServlet.class.getName()).log(Level.INFO, String.format("ERROR BAD PARAMETERS IN :(%s)",request.getPathInfo()) ) ;
            }
            // If the key of the tile is valid use it to retrive the JSON document
            // Se l'identificatore del tassello è valido lo usa per recuperare in modalità diretta il documento JSON
            if ( tile_id != null ) {
                document = (String) client.get(tile_id);
                if ( document != null ) {
                    finalize(request,response,document);
                    return;
                }
                else {
                    // If the document isn't present as simple tile. Search it in the index of the macro tile. 
                    // Se il documento non è presente effettua una ricerca nell'indice spaziale delle macro-tile
                    SpatialView view = client.getSpatialView(layername+"views", "bymacrobbox"+zoom);
                    Double[] bbox = null;
                    try { 
                        // It creates the bbox of the tile
                        bbox = ServletHelper.getTileBbx(x, y, zoom);
                    } catch ( Exception exc ) {
                        Logger.getLogger(JSONServlet.class.getName()).log(Level.INFO, 
                                         String.format("ERROR BAD X, Y PARAMETERS :(%s)",exc.getMessage()) ) ;
                    }
                    // It uses the spatial index to retrives the tile 
                    if ( view != null && bbox != null ){
                        Query query = new Query();
                        query.setBbox(bbox[0], bbox[1], bbox[2], bbox[3])
                             .setIncludeDocs(true).setStale(Stale.OK).setOnError(OnError.STOP);
                        ViewResponse result = client.query(view, query);
                        // If the document isn't the false document that manage request with empty result 
                        // return an empty document, otherwise return the correct JSON document
                        // Se il documento non è il documento che gestisce le richieste senza risposta 
                        // ritorna un documento vuoto, altrimenti ritorna il documento JSON trovato
                        for( ViewRow row : result ) {
                            Logger.getLogger(JSONServlet.class.getName()).log(Level.INFO, String.format("FOUND ID :(%s)",row.getId()) ) ;
                            if ( !row.getId().contains(":false") ){
                                document = (String)row.getDocument();
                                if ( document != null ) {
                                     finalize(request, response, document);
                                     return;
                                }     
                            }    
                        }
                        handleNone(request,response);
                        return;
                    }
                }
            }
       }
       handleError(request,response,1);
       
  }
  
  /**
   * Handle the /json/list/ action
   * 
   * This method loads up and returns the JSON document of the layers found by  
   * the given localization   
   * 
   * @param request the HTTP request object.
   * @param response the HTTP response object.
   * @throws IOException
   * @throws ServletException
   */
  private void handleList(HttpServletRequest request,
    HttpServletResponse response) throws IOException, ServletException {
       Logger.getLogger(JSONServlet.class.getName()).log(Level.INFO,  request.getPathInfo() ) ;
       // global request: /json/list/(<page>) 
       // getPathInfo()( /list/(<page>)  )
       Integer page=0;
       String layer = null, document = "";
       // It verifies the number of parameters 
       // Verifica il numero dei parametri
       if ( request.getPathInfo().length() > 6 ) {
            String[] values = request.getPathInfo().substring(6).split("/");
            try {    
                if ( values.length > 0  ) 
                    page = new Integer(values[0]);
            } catch ( Exception exc ) {
                Logger.getLogger(JSONServlet.class.getName()).log(Level.INFO,  String.format("ERROR IN PARAMETERS:(%s)", request.getPathInfo().substring(6)) ) ;
            }
       } 
       // It uses the field "_innoname_" to collect the documents of inno layers 
       // Usa il campo "_innoname_" dei documenti su couchbase per creare la lista degli strati informativi  
       View view = client.getView("innospatial", "byname" );
       Query query = new Query();
       if ( page != 0 ) 
           query.setSkip(page*100);
       query.setIncludeDocs(true).setLimit(100).setOnError(OnError.STOP).setStale(Stale.FALSE);
       ViewResponse result = client.query(view, query);
       document = "";
       for( ViewRow row : result ) 
            layer = (String)row.getDocument();
       if ( layer != null ) 
            document += "," + layer;
       // It creates the JSON document with the list of inno layers 
       // Crea il documento JSON con la lista degli strati informativi 
       document = "{\"layers\":[" + document.substring(1) + "]}";                
       finalize(request,response,document);    
  }
  
  
  /**
   * Handle the /json/value/<layer_name>/<field_name>/<x>/<y>/<zoom>/ action
   * 
   * This method loads up and returns a JSON document of the elements found in
   * the tile identified by the given coords and zoom  
   * The json documen doesn't have size limit!!!! it depends on info size and
   * elements number
   * 
   * @param request the HTTP request object.
   * @param response the HTTP response object.
   * @throws IOException
   * @throws ServletException
   */
  private void handleValues(HttpServletRequest request,
    HttpServletResponse response) throws IOException, ServletException {
        Logger.getLogger(JSONServlet.class.getName()).log(Level.INFO, request.getPathInfo());
        // global request: /json/value/<layer_name>/<field_name>/<x>/<y>/<zoom>(/<page>) 
        // path ( /value/<layer_name>/<field_name>/<x>/<y>/<zoom>(/<page>) )
        String layername = null;
        Integer x = null ,y = null ,zoom = null ,page=0;
        String document;
        String info;
        Collection ids;
        Integer size = 20;
        String fieldname = null;
        String id_tile = null;
        HashMap<String, Object> parsedDoc;
        String value;
        Double[] bbox = null;
        if ( request.getPathInfo().length() > 7 ) {
            try {    
                String[] values = request.getPathInfo().substring(7).split("/");
                if ( values.length ==  5 || values.length ==  6 ) {
                    layername = values[0]; 
                    fieldname = values[1];
                    zoom = new Integer(values[4]);
                    x = new Integer(values[2]);
                    y = new Integer(values[3]);
                    id_tile = layername + ":" + x + ":" + y + ":" + zoom ;
                    if ( values.length ==  6 ) {
                        page = new Integer(values[5]);
                        if ( page > 1 )
                            id_tile += ":" + page;
                    }
                }
                bbox = ServletHelper.getTileBbx(x, y, zoom);
            } catch ( Exception exc ) {
                Logger.getLogger(JSONServlet.class.getName()).log(Level.INFO,  String.format("ERROR IN PARAMETERS:(%s)",request.getPathInfo().substring(6)) ) ;
            }
            // The numbers of elements to return is equal to the number of geometries in the tile  
            // Il numero di elementi restituiti è uguale al numero di elementi geometrici nel tassello
            if ( id_tile != null && bbox != null ) {
                document = (String) client.get ( id_tile );
                if ( document != null )  {
                    parsedDoc = gson.fromJson(document, HashMap.class);
                    ids = (Collection)parsedDoc.get("objs");
                    size = ids.size();
                }      
                // It executes the spatial query over the elements  
                // Esegue la query spaziale sulle feature     
                SpatialView view = client.getSpatialView(layername+"views", "bybbox");
                String id, id_base = layername + ":";
                Query query = new Query();
                query.setBbox(bbox[0], bbox[1], bbox[2], bbox[3])
                     .setIncludeDocs(true).setLimit(size);
                ViewResponse result = client.query(view, query);
                // it builds the JSON document with values
                // Costruisce il documento JSON con i valori
                document = "";
                Logger.getLogger(JSONServlet.class.getName()).log(Level.INFO, "RESULT:" + result.size()) ;
                for( ViewRow row : result ) {
                    if ( ! row.getId().contains(layername+":false") ){
                         info = (String)row.getDocument();
                         if ( info != null ) {
                              parsedDoc= gson.fromJson(info, HashMap.class);
                              value = (String)(parsedDoc.get(fieldname));
                              if ( value != null )
                                   document = document + ",{\"id\":\"" +
                                           row.getId() + 
                                           "\",\"" + fieldname + "\":\"" + value + "\"}";
                         }
                    }     
                }
                if ( !document.equals("") ) {
                    document = "{\"" + fieldname + "\":[" + document.substring(1) + "]}";
                    finalize(request,response,document);
                }
                else handleNone(request,response); 
                return;
            }
        }
        handleError(request,response,1);                    
  }
  
  /**
   * Handle the /json/infos/<layer_name>/<x>/<y>/<zoom>/ action
   * 
   * This method loads up and returns a JSON document of the elements found in
   * the tile identified by the given coords and zoom  
   * 
   * @param request the HTTP request object.
   * @param response the HTTP response object.
   * @throws IOException
   * @throws ServletException
   */
  private void handleInfos(HttpServletRequest request,
    HttpServletResponse response) throws IOException, ServletException {
        Logger.getLogger(JSONServlet.class.getName()).log(Level.INFO, request.getPathInfo());
        // global request: /json/infos/<layer_name>/<x>/<y>/<zoom>(/<page>) 
        // path ( /infos/<layer_name>/<field_name>/<x>/<y>/<zoom>(/<page>) )
        String layername = null;
        Integer x = null, y = null, zoom = null, page=0;
        String document;
        String info;
        Collection ids;
        Integer size = 20;
        String id_tile = null;
        HashMap<String, Object> parsedDoc;
        Double[] bbox = null;
        // It verifies the number of parameters 
        // Verifica il numero dei parametri nella richiesta 
        if ( request.getPathInfo().length() > 7 ) {
            try {    
                String[] values = request.getPathInfo().substring(7).split("/");
                if ( values.length ==  4 || values.length ==  5 ) {
                    layername = values[0]; 
                    zoom = new Integer(values[3]);
                    x = new Integer(values[1]);
                    y = new Integer(values[2]);
                    id_tile = layername + ":" + x + ":" + y + ":" + zoom ;
                    if ( values.length ==  5 ) {
                        page = new Integer(values[4]);
                        if ( page > 1 )
                            id_tile += ":" + page;
                    }
                }
                bbox = ServletHelper.getTileBbx(x, y, zoom);
            } catch ( Exception exc ) {
                Logger.getLogger(JSONServlet.class.getName()).log(Level.INFO,  String.format("ERROR IN PARAMETERS:(%s)",request.getPathInfo().substring(6)) ) ;
            }
            // The numbers of elements to return is equal to the number of geometries in the tile  
            // Il numero di elementi restituiti è uguale al numero di elementi geometrici nel tassello
            if ( id_tile != null && bbox != null ) {
                document = (String) client.get ( id_tile );
                if ( document != null )  {
                    parsedDoc = gson.fromJson(document, HashMap.class);
                    ids = (Collection)parsedDoc.get("objs");
                    size = ids.size();
                }      
                // It executes the spatial query over the elements  
                // Esegue la query spaziale sulle feature     
                SpatialView view = client.getSpatialView(layername+"views", "bybbox");
                String id, id_base = layername + ":";
                Query query = new Query();
                query.setBbox(bbox[0], bbox[1], bbox[2], bbox[3])
                     .setIncludeDocs(true).setLimit(size);
                ViewResponse result = client.query(view, query);
                // It builds the JSON document with infos
                // Costruisce il documento JSON con i valori degli attributi
                document = "";
                for( ViewRow row : result ) {
                    if ( ! row.getId().contains(layername+":false") ){
                         info = (String)row.getDocument();
                         if ( info != null ) {
                              document = document + "," + info;
                         }
                    }     
                }
                if ( !document.equals("") ) {
                    document = "{\"objs\":[" + document.substring(1) + "]}";
                    finalize(request,response,document);
                }
                else handleNone(request,response); 
                return;
            }
        }
        handleError(request,response,1);  
  } 
  
  
  /**
   * Handle the /json/feature/<layer_name>/<feature_id> action
   * 
   * This method loads up and returns a JSON document of the elements found in
   * the tile identified by the given coords and zoom  
   * The json documen doesn't have size limit!!!! it depends on info size and
   * elements number
   * 
   * @param request the HTTP request object.
   * @param response the HTTP response object.
   * @throws IOException
   * @throws ServletException
   */
  private void handleFeature(HttpServletRequest request,
    HttpServletResponse response) throws IOException, ServletException {
        Logger.getLogger(JSONServlet.class.getName()).log(Level.INFO, request.getPathInfo());
        // global request: /json/feature/<layer_name>/<feature_id>
        // getPathInfo()( /feature/<layer_name>/<feature_id> )
        String layername = null;
        String document;
        String featureid = null;
        // It verifies the number of parameters 
        // Verifica il numero dei parametri nella richiesta 
        if ( request.getPathInfo().length() > 9) {
            try {    
                String[] values = request.getPathInfo().substring(9).split("/");
                if ( values.length == 2 ) {
                    layername = values[0]; 
                    featureid = values[1];
                }
            } catch ( Exception exc ) {
                Logger.getLogger(JSONServlet.class.getName()).log(Level.INFO,  String.format("ERROR IN PARAMETERS:(%s)",request.getPathInfo().substring(6)) ) ;
            }
            // It retrives the document of the feature 
            // Carica il documento relativo all'elemento con identificativo indicato 
            if ( layername!= null && featureid != null ) {
                document = (String) client.get ( layername + ":" + featureid );
                if ( document == null )
                     handleNone(request,response);
                else finalize(request,response,document); 
                return;
            } 
        }
        handleError(request,response,1);
        
  }
  
  /**
   * Handle the /json/feature/<layer_name>/<feature_id> action
   * 
   * This method loads up and returns a JSON document of the elements found in
   * the tile identified by the given coords and zoom  
   * The json documen doesn't have size limit!!!! it depends on info size and
   * elements number
   * 
   * @param request the HTTP request object.
   * @param response the HTTP response object.
   * @throws IOException
   * @throws ServletException
   */
  private void handleLayer(HttpServletRequest request,
    HttpServletResponse response) throws IOException, ServletException {
        Logger.getLogger(JSONServlet.class.getName()).log(Level.INFO, request.getPathInfo());
        // global request: /json/layer/<layer_name>/
        // getPathInfo()( /layer/<layer_name>/
        String layername = null;
        String document;
        // It verifies the number of parameters 
        // Verifica il numero dei parametri nella richiesta 
        if ( request.getPathInfo().length() > 7) {
            try {    
                String[] values = request.getPathInfo().substring(7).split("/");
                if ( values.length == 1 ) {
                    layername = values[0]; 
                }
            } catch ( Exception exc ) {
                Logger.getLogger(JSONServlet.class.getName()).log(Level.INFO,  String.format("ERROR IN PARAMETERS:(%s)",request.getPathInfo().substring(6)) ) ; 
            }
            // It retrives the document for the layername 
            // Restituisce il documento per lo strato informativo 
            if ( layername != null ) {
                document = (String) client.get ( layername  );
                if ( document == null )
                     handleNone(request,response);
                else finalize(request,response,document); 
                return;
            } 
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
          Logger.getLogger(JSONServlet.class.getName()).log(Level.INFO, request.getPathInfo() );
          response.setContentType("application/json");
          response.setHeader("Access-Control-Allow-Origin","*");
          PrintWriter out = response.getWriter();
          out.print("{}");
          out.flush(); 
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
          response.setContentType("application/json");
          response.setHeader("Access-Control-Allow-Origin","*");
          PrintWriter out = response.getWriter();
          out.print("{\"Error\":\"" + type + "\"}");
          out.flush(); 
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
  private void finalize(HttpServletRequest request, HttpServletResponse response, String document) 
     throws IOException, ServletException {
          if ( document != null )  {
               response.setContentType("application/json");
               response.setHeader("Access-Control-Allow-Origin","*");
               PrintWriter out = response.getWriter();
               out.print(document);
               out.flush(); 
          }
          else handleError(request,response,2);       
  }                    
}
