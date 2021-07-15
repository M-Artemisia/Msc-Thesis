

package drcl.inet.sensorsim.MHTrNoAgg;
import drcl.inet.sensorsim.*;
import drcl.inet.data.RTKey;
import drcl.inet.data.RTEntry;
import drcl.inet.contract.RTConfig;
import drcl.comp.ActiveComponent;
import drcl.comp.Port;
import drcl.data.DoubleObj;
import java.util.Vector;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;


/**
  * Created by IntelliJ IDEA.
 * User: mali
 * Date: Sep 29, 2009
 * Time: 11:49:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class Tr_SinkApp extends SensorApp implements ActiveComponent {

//to keep track of the total number of received packets
private int totalINpackets = 0;
/*To collect and display Total packets received by sink node in graph form. Created a
    port that will output to a plotter*/
public static final String PACKETS_REC_EVENT     = "Total Packets Received by Sink";
public static final String PLOTTER_PORT_ID  = ".PacketsReceivedPlot";
public Port packetsPlotPort = addEventPort(PLOTTER_PORT_ID); //for total packets received.


//this counter will be used to keep track of how many packets that the BS would have actually
//received if it wasn't using Clusters. In other words every CH sends one packet which combines
//the data of all its nodes in the their respective cluster. So just to see by how much traffic
//near the sink is reduced we also graph this plot as well.
private int totalVirtualPackets = 0;

public static final String VIRTUAL_PACKETS_REC_EVENT     = "Total Theoretical Packets Received";
public static final String PLOTTER_PORT_ID_2  = ".theo_PacketsReceivedPlot";
public Port packetsPlotPort2 = addEventPort(PLOTTER_PORT_ID_2); //for total theoretical packets received.
    

    long iDstNid;
    Vector DataFromPkts;
    Vector CountOfAggPckt;

   //packet RouteMinHopPckt, will send from sink has this type
    //this packet compute min hop to sink from every node
    public static final int    SINK_DATA            = 0 ;
    //data that sense in a source and aggregated will send to sink with this type
      public static final int    SOURCE_DATA          = 1 ;

    Vector Neighbours;//13 esfand logfile
    //Number of nodes that are in frequnce range in this node
    int NeighbourNum;//13 esfand logfile
    
//===============================================================================
    public Tr_SinkApp(){

        iDstNid = -1;
        DataFromPkts = new Vector();
        Neighbours = new Vector();
        totalINpackets=0;
        totalVirtualPackets=0;
        
    }
//===============================================================================
     protected void _start ()
    {
        //after 1 second send route Find packet
        rTimer = setTimeout("SendData", 1);
    }
//===============================================================================
     protected void _stop()
       {
        if (rTimer != null)
               cancelTimeout(rTimer);
           this.setCPUMode(3);     //turn off CPU when sim stops
           //WriteRslts();
       }
//===============================================================================
     protected synchronized void timeout(Object data_)
    {
        if (!sensorDEAD && data_.equals("SendData"))
            this.SendData();
    }
//===============================================================================
    public synchronized void recvSensorPacket(Object data_)
    {
        if ( data_ instanceof SensorPacket) {
            SensorPacket spkt = (SensorPacket)data_ ;
            if(spkt.getPktType() == SOURCE_DATA )
            {
                Tr_SourcePacket pckt = (Tr_SourcePacket) spkt.getBody();
                RcvSourcePacket(spkt);
            }
        super.recvSensorPacket(data_) ;
        }
 }
//===============================================================================
    //MinimRouteConstruction
    void SendData(){
    double [] loc= new double[3];
    loc[0]=getX();    loc[1]=getY();       loc[2] = getZ();
        Tr_SinkPacket pckt= new Tr_SinkPacket(this.nid,"MinimRouteConstruction",8,loc);
        downPort.doSending(new SensorAppWirelessAgentContract.Message(
                SensorAppWirelessAgentContract.BROADCAST_SENSOR_PACKET,SINK_DATA,this.nid,pckt.getSize(),pckt));
        return;
        }
//===============================================================================
    protected void RcvSourcePacket(SensorPacket spkt){

    Tr_SourcePacket pckt = (Tr_SourcePacket) spkt.getBody();
    DataFromPkts.add(pckt.getData());
    System.out.println("Base Source of this pkt is"+ pckt.BaseSrc);
      for(int j=0; j<pckt.Path.size() ; j++)
            System.out.print(pckt.Path.elementAt(j)+",");
    System.out.println();

//***********************
// Step 1. Update the number of total packets that have been received. This means
// only the packets received and processed from the CHs throughout the simulation.
//***********************
    this.totalINpackets = this.totalINpackets + 1;
    if (packetsPlotPort.anyOutConnection()) {
        packetsPlotPort.exportEvent(PACKETS_REC_EVENT, new DoubleObj(this.totalINpackets), null);
    }
//**********************
// Step 2. Theoretical packets received-Since tree combined packets from all sensors in
// its path into one... here we actually display the theoretical number of packets that
// should have been received by the base station.
//**********************
    this.totalVirtualPackets = this.totalVirtualPackets +1 ;//added by one because of packet itself: agg+1=all packets
    if (packetsPlotPort2.anyOutConnection()) {
        packetsPlotPort2.exportEvent(VIRTUAL_PACKETS_REC_EVENT, new DoubleObj(totalVirtualPackets), null);
    }
    try{
            File file;
            FileWriter fw;
            BufferedWriter bw;
            file = new File("./out/Tree/total.txt");
            fw = new FileWriter(file,true);
            bw = new BufferedWriter(fw);
            bw.write(new Integer(totalINpackets).toString() );
            bw.newLine();
            bw.close();
            fw.close();
        }
        catch(
            IOException ioe){
            System.out.println("an IOE is happend:"+ioe);
        }
    try{
            File file;
            FileWriter fw;
            BufferedWriter bw;
            file = new File("./out/Tree/virtual.txt");
            fw = new FileWriter(file,true);
            bw = new BufferedWriter(fw);
            bw.write(new Integer(totalVirtualPackets).toString());
            bw.newLine();
            bw.close();
            fw.close();
        }
        catch(
            IOException ioe){
            System.out.println("an IOE is happend:"+ioe);
        }
}
//===============================================================================
    protected void addRoute(long src_nid_, long dst_nid_, int timeout_){
    int type = 0;
    RTKey key = new RTKey(src_nid_, dst_nid_, timeout_);
    RTEntry entry = new RTEntry(new drcl.data.BitSet(new int[]{0}));
   setRoutePort.sendReceive(new RTConfig.Message(type, key, entry, timeout_));
    }

      public double getTotalPkts(){
        return (double)totalINpackets;
    }
    public double  getVirtualPkts(){
        return (double)totalVirtualPackets;
    }
}