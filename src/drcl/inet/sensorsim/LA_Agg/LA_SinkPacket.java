package drcl.inet.sensorsim.LA_Agg;
import drcl.net.Packet;

/**
 * Created by IntelliJ IDEA.
 * User: mali
 * Date: Sep 29, 2009
 * Time: 11:27:01 AM
 * To change this template use File | Settings | File Templates.
 */

public class LA_SinkPacket extends Packet {
    public String Name;
    public Long ID;
    public double HopsToSink;
    public double [] Location;

    public LA_SinkPacket(long nid,String name, int size,double [] loc ){

        HopsToSink =1 ;
        id = nid;
        ID = new Long(nid);
        Name = name;
        Name.concat( ID.toString() );
        this.size = size;
         Location = new double[3];
        Location = loc;
    }

    public  void setHop2Sink(double hop){
         HopsToSink = hop;
        return ;
    }

   public double getHop2Sink(){
        return HopsToSink;
    }
    public double[] getLoc(){ return Location; }
    public String getName(){
        return Name;        
    }
    
    public Object clone(){
        return new LA_SinkPacket(ID.longValue(),getName(),size,Location) ;
    }


}
