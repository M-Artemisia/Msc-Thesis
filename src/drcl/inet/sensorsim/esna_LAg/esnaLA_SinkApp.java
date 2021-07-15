

package drcl.inet.sensorsim.esna_LAg;
import drcl.inet.sensorsim.*;
import drcl.inet.sensorsim.LA_Agg.*;
import drcl.inet.data.RTKey;
import drcl.inet.data.RTEntry;
import drcl.inet.contract.RTConfig;
import drcl.comp.ActiveComponent;
import drcl.comp.Port;
import drcl.data.DoubleObj;
import java.util.Vector;
import java.io.*;


/**
  * Created by IntelliJ IDEA.
 * User: mali
 * Date: Sep 29, 2009
 * Time: 11:49:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class esnaLA_SinkApp extends SensorApp implements ActiveComponent {

    public int stopIt =  0;
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

    //ack from node that recieve data packet has this type
    public static final int    ACK_DATA             = 2 ;

    File file;
    Vector Neighbours;//13 esfand logfile
    int NeighbourNum;//13 esfand logfile
//===============================================================================
    public esnaLA_SinkApp(){

        iDstNid = -1;
        DataFromPkts = new Vector();
        CountOfAggPckt = new Vector();
        Neighbours = new Vector();
        totalINpackets=0;
        totalVirtualPackets=0;

    }
//===============================================================================
     protected void _start ()
    {
        file = new File("./out/ELA/n"+ new Long(nid).toString()+".txt");   //13 esfand log file

        rTimer = setTimeout("SendData", 1);
        setTimeout("PktsFile", 150);

//        rTimer = setTimeout("check", 1000 + 61 * 20);
    }

//===============================================================================
 /*   public void check(){
        if(this.NeighbourNum == 0)
            stopIt = 1;
}
    public int shouldStop(){
        return stopIt;
    }
*///===============================================================================
     protected void _stop()
       {
            //13 esfand log file
    try{
        FileWriter fw=new FileWriter(file,true);
        BufferedWriter bw=new BufferedWriter(fw);
        bw.write("stop at time:"+ new Double(getTime()).toString());
        bw.newLine();
        bw.close();
        fw.close();
    }
     catch(IOException ioe){
            System.out.println("an IOE is happend:"+ioe);
        }
    //END 13 esfand log file
           if (rTimer != null)
               cancelTimeout(rTimer);
           this.setCPUMode(3);     //turn off CPU when sim stops
           //WriteRslts();
       }
//===============================================================================
     protected synchronized void timeout(Object data_)
    {
         //13 esfand log file
    try{
        FileWriter fw=new FileWriter(file,true);
        BufferedWriter bw=new BufferedWriter(fw);
        bw.write("timeout at time:"+ new Double(getTime()).toString());
        bw.newLine();
        bw.close();
        fw.close();
    }
     catch(IOException ioe){
            System.out.println("an IOE is happend:"+ioe);
        }
    //END 13 esfand log file
        if (!sensorDEAD && data_.equals("SendData"))
            this.SendData();
        else if ( data_.equals("PktsFile")) {
              try{
                  File file2 = new File("./out/ELA/total.txt");
                  FileWriter fw2= new FileWriter(file2,true);
                  BufferedWriter bw2 = new BufferedWriter(fw2);
                  bw2.write(new Integer(totalINpackets).toString() + ",");
                  bw2.close(); fw2.close();

                  File file3= new File("./out/ELA/Virtual.txt");
                  FileWriter fw3= new FileWriter(file3,true);
                  BufferedWriter bw3 = new BufferedWriter(fw3);
                  bw3.write(new Integer(totalVirtualPackets).toString()+ ",");
                  bw3.close(); fw3.close();
            }
            catch(IOException ioe){
                System.out.println("an IOE is happend:"+ioe);
            }
            setTimeout("PktsFile", 1);
            return;
        }
    }
//===============================================================================
    public synchronized void recvSensorPacket(Object data_)
    {
            //13 esfand log file
    try{
        FileWriter fw=new FileWriter(file,true);
        BufferedWriter bw=new BufferedWriter(fw);
        if ( data_ instanceof SensorPacket) {
            SensorPacket spkt = (SensorPacket)data_ ;
            if(spkt.getPktType() == SOURCE_DATA )
            {
                bw.write("recvSensorPacket-SOURCE_DATA at time:"+ new Double(getTime()).toString()+"from "+new Long(spkt.id) );
                RcvSourcePacket(spkt);
            }

        }
        else
           bw.write("recvSensorPacket at time:"+ new Double(getTime()).toString());

        super.recvSensorPacket(data_) ;
        bw.newLine();
        bw.close();
        fw.close();
    }
     catch(IOException ioe){
            System.out.println("an IOE is happend:"+ioe);
     }
    //END 13 esfand log file
 }
//===============================================================================
    //MinimRouteConstruction
    void SendData(){
         //13 esfand log file
    try{
        FileWriter fw=new FileWriter(file,true);
        BufferedWriter bw=new BufferedWriter(fw);
        bw.write("SendData at time:"+ new Double(getTime()).toString());
        bw.newLine();
        bw.close();
        fw.close();
    }
     catch(IOException ioe){
            System.out.println("an IOE is happend:"+ioe);
        }
    //END 13 esfand log file
    double [] loc= new double[3];
       loc[0]=getX();    loc[1]=getY();       loc[2] = getZ();

        LA_SinkPacket pckt= new LA_SinkPacket(this.nid,"MinimRouteConstruction",8,loc);
        downPort.doSending(new SensorAppWirelessAgentContract.Message(
                SensorAppWirelessAgentContract.BROADCAST_SENSOR_PACKET,SINK_DATA,this.nid,8,pckt));
        return;
        }
//===============================================================================
    //13 esfand
    protected void RcvSourcePacket(SensorPacket spkt){
       //13 esfand log file
    try{
        FileWriter fw=new FileWriter(file,true);
        BufferedWriter bw=new BufferedWriter(fw);
        bw.write("SendAckPacket at:"+ new Double(getTime()).toString());
        bw.newLine();
        bw.close();
        fw.close();
    }
     catch(IOException ioe){
            System.out.println("an IOE is happend:"+ioe);
    }

    double time=getTime();
    LA_SourcePacket msg = (LA_SourcePacket) spkt.getBody();
 /*   int index = Neighbours.indexOf(msg.ID);
    if(index==-1){
        System.out.println("error in index RcvSourcePacket  in node "+nid);
        addNeighbour(msg.ID);
    }
   */ DataFromPkts.add(msg.getData());
    CountOfAggPckt.add(new Integer(msg.getNumOfAggPckt()));

    System.out.println("packet rcvd in Sink"+ msg.getData());
    System.out.println("Num of agg pkt in it is"+ msg.getNumOfAggPckt());
    System.out.println("Base Source of this pkt is"+ msg.BaseSrc);
      for(int j=0; j<msg.PathNode.size() ; j++)
            System.out.print(msg.PathNode.elementAt(j)+",");
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
    this.totalVirtualPackets = this.totalVirtualPackets +msg.getNumOfAggPckt()+1 ;//added by one because of packet itself: agg+1=all packets
    if (packetsPlotPort2.anyOutConnection()) {
        packetsPlotPort2.exportEvent(VIRTUAL_PACKETS_REC_EVENT, new DoubleObj(totalVirtualPackets), null);
    }

    WriteRslts(msg.BaseSrc,msg.PathNode,msg.PathInfo,time);
    SendAckPacket(msg.id,msg.getLoc());
}
//===============================================================================
    //13 esfand
protected void SendAckPacket(long Dst_id,double [] DstLoc){
      //13 esfand log file
    try{
        FileWriter fw=new FileWriter(file,true);
        BufferedWriter bw=new BufferedWriter(fw);
        bw.write("SendAckPacket at:"+ new Double(getTime()).toString());
        bw.newLine();
        bw.close();
        fw.close();
    }
     catch(IOException ioe){
            System.out.println("an IOE is happend:"+ioe);
    }
          double [] loc = new double[3];loc[0] = loc[1] = loc[2] = 0.0;
    LA_ControlPacket pckt=new LA_ControlPacket(nid,"Ack Data Packet",8,new Double(1),1,0.1,loc);
    downPort.doSending(new SensorAppWirelessAgentContract.Message(SensorAppWirelessAgentContract.UNICAST_SENSOR_PACKET,
           Dst_id,this.nid,/*7 esfand*/DstLoc,8,ACK_DATA,eID,this.nid,pckt));
    }
//===============================================================================
    //13 esfand
    private void addNeighbour(Long NghbrID){
        //13 esfand log file
    try{
        FileWriter fw=new FileWriter(file,true);
        BufferedWriter bw=new BufferedWriter(fw);
        bw.write("addNeighbour at:"+ new Double(getTime()).toString());
        bw.write("NeighbourNum = "+new Integer(NeighbourNum+1).toString()+"      Neighbour: "
                +NghbrID.toString());
        bw.newLine();
        bw.close();
        fw.close();
    }
     catch(IOException ioe){
            System.out.println("an IOE is happend:"+ioe);
    }
    //END 13 esfand log file
        NeighbourNum++;
        Neighbours.add(NghbrID);
        //show Neighbours if we add a Neighbour
        System.out.print("Neighbours of "+nid+": ");
        for (int j=0;j<Neighbours.size() ; j++)
            System.out.print(Neighbours.elementAt(j)+",");
    }
//===============================================================================
    protected void addRoute(long src_nid_, long dst_nid_, int timeout_){
    int type = 0;
    RTKey key = new RTKey(src_nid_, dst_nid_, timeout_);
    RTEntry entry = new RTEntry(new drcl.data.BitSet(new int[]{0}));
   setRoutePort.sendReceive(new RTConfig.Message(type, key, entry, timeout_));
    }
//===============================================================================
    void WriteRslts(long BaseSrc,Vector PathNode,Vector PathInfo,double time){
          //13 esfand log file
    try{
        FileWriter fw=new FileWriter(file,true);
        BufferedWriter bw=new BufferedWriter(fw);
        bw.write("WriteRslts at time:"+ new Double(getTime()).toString());
        bw.newLine();
        bw.close();                   fw.close();
    }
     catch(IOException ioe){
            System.out.println("an IOE is happend:"+ioe);
        }
    //END 13 esfand log file
        try{
            //IO
            File file= new File("./out/ELA/rslt.txt");
            FileWriter fw= new FileWriter(file,true);
            BufferedWriter bw= new BufferedWriter(fw);

            int i = DataFromPkts.size()-1;
             //write rslts in a file
                bw.write("time of rcving this pkt is:"+ new Double(time).toString());                           bw.newLine();
                bw.write("data rcvd in Sink is:           "+DataFromPkts.elementAt(i).toString());              bw.newLine();
                bw.write("Num of agg pkt in it is:        "+ CountOfAggPckt.elementAt(i).toString());           bw.newLine();
                bw.write("Base Source of this pkt is:      "+ BaseSrc);                                         bw.newLine();
                bw.write("Path of this pkt is:             ");
                for(int j=0; j<PathNode.size() ; j++){
                    bw.write(PathNode.elementAt(j).toString()+", ");
                }
            bw.write("0.");            bw.newLine();
            bw.write("info of this Path is:                ");
            for(int k=0; k<PathInfo.size() ; k++){
                bw.write(PathInfo.elementAt(k).toString()+", ");
            }
            bw.newLine();    bw.newLine();    bw.newLine();
            bw.close();              fw.close();
//27 bahman
            File file1= new File("./out/ELA/pckts.txt");
            FileWriter fw1= new FileWriter(file1,true);
            BufferedWriter bw1 = new BufferedWriter(fw1);

            bw1.write("with agg: "+ new Integer(totalINpackets).toString() + "        ");
            bw1.write("without agg: "+ new Integer(totalVirtualPackets).toString()+ "        ");
            bw1.write("at time:"+ new Double(time).toString());
            bw1.newLine();             bw1.newLine();
            bw1.close(); fw1.close();
//end 27 bahman
    //31 farv
        /*    File file2 = new File("./out/ELA/total.txt");
            FileWriter fw2= new FileWriter(file2,true);
            BufferedWriter bw2 = new BufferedWriter(fw2);
            bw2.write(new Integer(totalINpackets).toString() + ",");
            bw2.close(); fw2.close();

            File file3= new File("./out/ELA/Virtual.txt");
            FileWriter fw3= new FileWriter(file3,true);
            BufferedWriter bw3 = new BufferedWriter(fw3);
            bw3.write(new Integer(totalVirtualPackets).toString()+ ",");
            bw3.close(); fw3.close();*/
//end 31 farv
        }
        catch(IOException ioe){
            System.out.println("an IOE is happend:"+ioe);
        }
        return;
    }
    //===============================================================================
  public void setNodeNumber(int nn_){
        this.nn_ = nn_;
    }
//===============================================================================
    public double getTotalPkts(){
        return (double)totalINpackets;
    }
    public double  getVirtualPkts(){
        return (double)totalVirtualPackets;
    }
}