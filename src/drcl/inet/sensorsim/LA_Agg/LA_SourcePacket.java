package drcl.inet.sensorsim.LA_Agg;

/**
 * Created by IntelliJ IDEA.
 * User: mali
 * Date: Sep 29, 2009
 * Time: 11:05:01 AM
 * To change this template use File | Settings | File Templates.
 */
import drcl.net.Packet;

import java.util.Vector;

public class LA_SourcePacket extends Packet {

    public Long ID;
    //7 esfand
    public double [] loc;
    //END 7 esfand
    private String NAME;
    private Object AggData;
    private int NumOfAggPckt;
    public long BaseSrc;
//30 dei- delete 29 dei in this class
    //path from drc to the dst
    public Vector PathNode;

    /*
    Only 4 TESTING
     */
    public Vector PathInfo;
    public boolean AckSend=false;
    public long LastSRCid=-1;
    
    public LA_SourcePacket(long nid,long lastSrcId,double[] MyLoc/*7 esfand*/ ,String str, int size , int NumAggPckt , Object Body,
                        long baseSrc, Vector path, Vector pathInfo,boolean ack){
        NAME = str;
        id = nid;
        ID = new Long(nid);
        this.size = size;
        AggData = Body;
        NumOfAggPckt = NumAggPckt;
        BaseSrc = baseSrc;
        
        PathNode = new Vector();
        PathInfo = new Vector();
        SetPathNode(path,pathInfo);
        //7 esfand
        loc = new double[3]; 
        loc = MyLoc;
        AckSend = ack;

        LastSRCid = lastSrcId;
        //END 7 esfand

    }


    public String getName(){
        return NAME;
    }
    public Object getData(){
        return AggData;
    }
    public int getNumOfAggPckt(){
        return NumOfAggPckt;
    }
    //7 esfand
    public double [] getLoc(){
        return loc;
    }
    //END 7 esfand

    public void addPathNode(Long id){
        PathNode.add(id);
    }

    public void addPathInfo(String str){
        PathInfo.add(str);
    }

    private void SetPathNode(Vector path,Vector pathInfo){
        int size = path.size() ;
        for(int i=0; i<size ;i++)
            addPathNode((Long)(path.elementAt(i)));
        size = pathInfo.size() ;
        for(int i=0; i<size ;i++)
          addPathInfo((String)(pathInfo.elementAt(i)));
    }

    public Object clone(){
        return  new LA_SourcePacket(ID.longValue(),LastSRCid , loc,getName(),size,NumOfAggPckt,AggData,BaseSrc
                ,PathNode,PathInfo,AckSend) ;
    }
}

