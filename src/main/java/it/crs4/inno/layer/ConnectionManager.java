 /**
 * Copyright (C) 2014 CRS4
 *
 * @author Roberto Demontis
 */
package it.crs4.inno.layer;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.CouchbaseConnectionFactoryBuilder;
import com.couchbase.client.protocol.views.DesignDocument;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * The ConnectionManager handles connecting, disconnecting and managing
 * of the Couchbase connection.
 *
 * To get the connection from a Servlet context, use the getInstance()
 * method.
 */
public class ConnectionManager implements ServletContextListener {

  /** The Logger to use. */
  private static final Logger logger = Logger.getLogger(
  ConnectionManager.class.getName());
  
  /**
   * Holds the connected Couchbase instance.
   */
  private static CouchbaseClient client;
  
  /**
   * Holds the name of the bucket and app.
   */
  private static String name = "";

  
  private CouchbaseClient getCouchbaseClient() {
    try {
        return client;
    } catch (Exception e) {
        return null;
    }
  }

  /**
   * Connect to Couchbase when the Server starts.
   *
   * @param sce the ServletContextEvent (not used here).
   */
  @Override
  public void contextInitialized(ServletContextEvent sce) {
    String the_nodes = "", password = "";
    try {
        Context ctx = new InitialContext();
        ctx = (Context) ctx.lookup("java:comp/env");
        the_nodes = (String) ctx.lookup("NODES");
        password = (String) ctx.lookup("PASS");
        name = (String) ctx.lookup("BUCKET"); 
    }
    catch (NamingException e) {
       logger.log(Level.SEVERE, e.getMessage());
    }
    
    logger.log(Level.INFO, String.format("Connecting to Couchbase Nodes [%s]", the_nodes + " db:" + name));
    String[] nodes_path = the_nodes.split(","); 
    ArrayList<URI> nodes = new ArrayList<URI>();
    for ( int i = 0; i< nodes_path.length; i++ )
        nodes.add(URI.create(nodes_path[i]));
    try {
        client = new CouchbaseClient(
                new CouchbaseConnectionFactoryBuilder()
                        .setViewWorkerSize(8) // use 8 worker threads instead of one
                        .setViewConnsPerNode(40) // allow 40 parallel http connections per node in the cluster
                        .buildCouchbaseConnection(nodes, name, password)
        );
        
    } catch (IOException ex) {
        logger.log(Level.SEVERE, ex.getMessage());
    }
    
    logger.log(Level.INFO, "Trying to verify the views");
    
  } 
  
  private DesignDocument getDesignDocument(String name) {
    try {
        return client.getDesignDoc(name);
    } catch (com.couchbase.client.protocol.views.InvalidViewException e) {
        return new DesignDocument(name);
    }
  }

  /**
   * Disconnect from Couchbase when the Server shuts down.
   *
   * @param sce the ServletContextEvent (not used here).
   */
  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    logger.log(Level.INFO, "Disconnecting from Couchbase Cluster");
    client.shutdown();
  }

  /**
   * Returns the current CouchbaseClient object.
   *
   * @return the current Couchbase connection.
   */
  public static CouchbaseClient getInstance() {
    return client;
  }
  
  /**
   * Returns the name of the bucket and app.
   *
   * @return the bucket's name.
   */
  public static String getBucketName() {
    return name;
  }
  
}