package drcl.inet.sensorsim.MHTrNoAgg;
import drcl.net.Packet;

/**
 * Created by IntelliJ IDEA.
 * User: mali
 * Date: Sep 29, 2009
 * Time: 11:27:01 AM
 * To change this template use File | Settings | File Templates.
 */

public class Tr_SinkPacket extends Packet {
    String Name;
    Long ID;
    double HopsToSink;
    double [] Location;

    
    public Tr_SinkPacket(long nid,String name, int size,double [] loc){

        HopsToSink =1 ;
        id = nid;
        ID = new Long(nid);
        Name = name;
        Name.concat( ID.toString() );
        this.size = size;

        Location = new double[3];
        Location = loc;
    }
    
    public  void setHop2Sink(double hop){HopsToSink = hop;    }
    public double[] getLoc(){ return Location; }
   public double getHop2Sink(){    return HopsToSink;   }
    public String getName(){ return Name; }
    public Object clone(){return new Tr_SinkPacket(ID.longValue(),getName(),size,Location) ;
    }


}
