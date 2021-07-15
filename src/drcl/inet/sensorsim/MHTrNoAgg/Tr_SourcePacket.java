package drcl.inet.sensorsim.MHTrNoAgg;

/**
 * Created by IntelliJ IDEA.
 * User: mali
 * Date: Sep 29, 2009
 * Time: 11:05:01 AM
 * To change this template use File | Settings | File Templates.
 */
import drcl.net.Packet;
import java.util.Vector;
import drcl.inet.sensorsim.AC_Agg.Path2Sink;

public class Tr_SourcePacket extends Packet {

    Long ID;
    long LastSrcId;
    double [] loc;
    private String NAME;
    public long BaseSrc;
    Vector Path=new Vector();//path from src to the dst
    double [] SenseLoc;
     private Object Data;
    
    public Tr_SourcePacket(long nid ,long lastSrc ,double[] MyLoc,String str, int size ,Object Body,long baseSrc,Vector path){
        NAME         = str;
        id           = nid;
        ID           = new Long(nid);
        this.size    = size;
        BaseSrc      = baseSrc;
        loc          = new double[3];
        loc          = MyLoc;
        SenseLoc          = new double[3];
        LastSrcId    = lastSrc;
        Data = Body;
        SetPathNode(path);
    }

    public String getName(){return NAME;    }
    Object getData(){   return Data;    }
    public double [] getLoc(){return loc;    }
    public void addPathNode(Path2Sink p){   Path.add(p);    }

    private void SetPathNode(Vector p){
        for(int i=0; i < p.size() ;i++)
           // addPathNode((Path2Sink)p.elementAt(i));
            Path.add(p.elementAt(i));
    }

    public Object clone(){
        return  new Tr_SourcePacket(ID.longValue() ,LastSrcId , loc,getName(),size,Data,BaseSrc,Path) ;
    }
}

