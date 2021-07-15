package drcl.inet.sensorsim.AC_Agg;

import drcl.comp.Port;
import drcl.inet.InetPacket;
import drcl.inet.sensorsim.SensorPacket;

/**
 * Created by IntelliJ IDEA.
 * User: mali
 * Date: Apr 24, 2010
 * Time: 12:12:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class WirelessACOAgent extends drcl.inet.sensorsim.WirelessAgent
{
     public static final int    SINK_DATA            = 0 ;
    public static final int    SOURCE_DATA          = 1 ;
    public static final int    ACK_DATA             = 2 ;

	public WirelessACOAgent()
	{
        super();
    }

	public WirelessACOAgent(String id_)
	{
        super(id_);
    }
  /*  protected synchronized void dataArriveAtUpPort(Object data_, Port upPort_)
    {
        int i=0;
        i=i+1;
    }
*/
    protected synchronized void dataArriveAtDownPort(Object data_, Port downPort_)
	{

		InetPacket ipkt_ = (InetPacket)data_;

		if ( ipkt_.getBody() instanceof SensorPacket)
		{
			SensorPacket pkt_ = (SensorPacket)ipkt_.getBody();
			switch ( pkt_.getPktType())
			{
				case SINK_DATA:
                    //this constructor is for broadcast packets
                    //SensorPacket(int pktType_, long src_nid_, int eventID_, int dataSize_, Object body_)
					toSensorAppPort.doSending(new SensorPacket(SINK_DATA, pkt_.getSrc_nid(),pkt_.getEventID(), pkt_.getBody(),pkt_.getPacketSize()));
					break ;
				case ACK_DATA :
                    //this constructor is for unicast packets.
                    //SensorPacket(int pktType_, long dst_nid_, long src_nid_, int dataSize_,int eventID_, targetID,  Object body_)	{
					toSensorAppPort.doSending(new SensorPacket(ACK_DATA, pkt_.getDst_nid(), pkt_.getSrc_nid(),pkt_.getPacketSize(), pkt_.getEventID(), -1, pkt_.getBody()));
					break ;
                case SOURCE_DATA :
                     //this constructor is for unicast packets.
                    //SensorPacket(int pktType_, long dst_nid_, long src_nid_, int dataSize_,int eventID_, Object body_)	{
					toSensorAppPort.doSending(new SensorPacket(SOURCE_DATA, pkt_.getDst_nid(), pkt_.getSrc_nid(),pkt_.getPacketSize(), pkt_.getEventID(), -1, pkt_.getBody()));
					break ;
				default :
                    System.out.println("*******The packet is not made for LEACH system its of type: " + pkt_.getPktType() + " going to sensorAPP instead");
					super.dataArriveAtDownPort(data_, downPort_) ;
			}
            return;
		}
		else
		{
            //erroneous message for the LEACH system
            System.out.println("*****Warning: WirelessLEACHAgent Did not receive a SensorPacket instead it received: " + ipkt_.getBody().getClass());
			super.dataArriveAtDownPort(data_, downPort_) ;
		}
        return;
	}
}
