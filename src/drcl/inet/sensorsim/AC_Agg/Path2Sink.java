package drcl.inet.sensorsim.AC_Agg;

import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: admin
 * Date: Mar 15, 2010
 * Time: 12:58:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class Path2Sink {
    public long PathNodeId; /*Only 4 TESTING*/
    public String PathLevel;
    public String PathInfo;

    public Path2Sink(long pathNodeId,String pathLevel, String pathInfo){
        PathNodeId  = pathNodeId;
        PathLevel   = pathLevel;
        PathInfo    = pathInfo;
}
}
