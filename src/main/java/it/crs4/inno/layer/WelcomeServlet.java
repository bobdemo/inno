/**
 * Copyright (C) 2014 CRS4
 *
 * @author Roberto Demontis
 */

package it.crs4.inno.layer;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The WelcomeServlet is the initial starting point visiting through root.
 */
public class WelcomeServlet extends HttpServlet {

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
   * First page a user visits when contacting the inno layer app.
   *
   * This action is usually dispatched when a client sends a GET request
   * to the root (/{layerid}/) URL of the application.
   *
   * @param request the servlet request instance.
   * @param response the servlet response instance.
   * @throws ServletException
   * @throws IOException
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
      
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

}
