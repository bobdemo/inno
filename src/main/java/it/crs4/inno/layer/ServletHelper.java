package it.crs4.inno.layer;

/**
 * Copyright (C) 2014 CRS4
 *
 * @author Roberto Demontis
 */

public class ServletHelper {
    
    
    public static String getTileId(final Double lat, final Double lon, final Integer zoom) {
    int xtile = (int)Math.floor( (lon + 180) / 360 * (1<<zoom) ) ;
    int ytile = (int)Math.floor( (1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1<<zoom) ) ;
    if (xtile < 0)  
        xtile=0;
    if (xtile >= (1<<zoom) )
        xtile=((1<<zoom)-1);
    if (ytile < 0)
        ytile=0;
    if (ytile >= (1<<zoom))
        ytile=((1<<zoom)-1);
    return xtile + ":" + ytile + ":" + zoom  ;
 }

 public static Double[] getTileBbx( Integer x,  Integer y,  Integer zoom) {
    Double [] bb = new Double[4];  
    bb[0] = tile2lon(x, zoom);
    bb[1] = tile2lat(y+1, zoom);
    bb[2] = tile2lon(x+1, zoom);
    bb[3] = tile2lat(y, zoom);
    if ( bb[0] == null || bb[1] == null || bb[2] == null || bb[3] == null )
         return null;
    return bb;
  }
 
  public static Double[] getTileSearchBbx( Integer x,  Integer y,  Integer zoom) {
    Double [] bb = getTileBbx(x,y,zoom);  
    double dx = (bb[2]-bb[0])/4;
    double dy = (bb[3]-bb[1])/4;
    bb[0] = bb[0] + dx;
    bb[1] = bb[1] + dy;
    bb[2] = bb[2] - dx;
    bb[3] = bb[3] - dy;
    return bb;
  }
 
  public static Double tile2lon(Integer x, Integer z) {
      Double lon =  x / Math.pow(2.0, z) * 360.0 - 180;
      if ( lon >= -180 && lon <= 180)
           return lon;
      return null;        
  }
 
  public static Double tile2lat(Integer y, Integer z) {
    double lat = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
    lat = Math.toDegrees(Math.atan(Math.sinh(lat)));
    if ( lat >= -90 && lat <= 90)
           return lat;
    return null;        
  }
}
