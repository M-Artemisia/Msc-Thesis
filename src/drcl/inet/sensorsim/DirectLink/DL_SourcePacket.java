package drcl.inet.sensorsim.DirectLink;

/**
 * Created by IntelliJ IDEA.
 * User: mali
 * Date: Sep 29, 2009
 * Time: 11:05:01 AM
 * To change this template use File | Settings | File Templates.
 */
import drcl.net.Packet;

public class DL_SourcePacket extends Packet {

    Long ID;
    double [] loc;
    private String NAME;
    public long BaseSrc;
    double [] SenseLoc;
     private Object Data;
    
    public DL_SourcePacket(long nid ,double[] MyLoc,String str, int size ,Object Body,long baseSrc){
        NAME         = str;
        id           = nid;
        ID           = new Long(nid);
        this.size    = size;
        BaseSrc      = baseSrc;
        loc          = new double[3];
        loc          = MyLoc;
        SenseLoc          = new double[3];
        Data = Body;
    }

    public String getName(){return NAME;    }
    Object getData(){   return Data;    }
    public double [] getLoc(){return loc;    }
    public Object clone(){
        return  new DL_SourcePacket(ID.longValue(), loc,getName(),size,Data,BaseSrc) ;
    }
}

