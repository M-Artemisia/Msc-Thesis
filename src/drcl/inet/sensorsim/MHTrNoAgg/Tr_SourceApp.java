package drcl.inet.sensorsim.MHTrNoAgg;

import drcl.inet.sensorsim.SensorApp;
import drcl.inet.sensorsim.SensorPacket;
import drcl.inet.sensorsim.SensorAppWirelessAgentContract;
import drcl.inet.sensorsim.AC_Agg.environment;
import drcl.inet.sensorsim.AC_Agg.Neighbour;
import drcl.inet.sensorsim.AC_Agg.Path2Sink;
import drcl.inet.data.RTKey;
import drcl.inet.data.RTEntry;
import drcl.inet.contract.RTConfig;
import drcl.inet.mac.EnergyContract;
import drcl.comp.ActiveComponent;
import drcl.comp.ACATimer;
import drcl.comp.Port;
import drcl.data.DoubleObj;

import java.util.Vector;
import java.util.Random;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

/**
* Created by IntelliJ IDEA.
* User: mali
* Date: Sep 29, 2009
* Time: 11:49:53 AM
* To change this template use File | Settings | File Templates.
*/
public class Tr_SourceApp extends SensorApp implements ActiveComponent {


      //Parameters that should be set before running,or in tcl file
    public  double PERIODIC_TIME_FOR_SENSING        = 10 ;
    public  int    DIM1                             = 0;//meter
    public  int    DIM2                             = 0;//meter
    public  int    DIM3                             = 0;//meter
    public  int    AREA_NUMBER                      = 1;


/*To collect and display energy levels in graph form. Created a
    port that will output to a plotter*/
public static final String ENERGY_EVENT     = "Remaining Energy";
public static final String REM_ENERGY_PORT_ID  = ".plotter";
public Port plotterPort = addEventPort(REM_ENERGY_PORT_ID);

    
    boolean SendIt = true;
    int nn_;
    Long id;
    long iDstNid;
    long ackNghbrId;
    //id of first src packet that send pckt to the sink
    long BaceSource;
    Vector m_Neighbours;
    /**the path from this node to the dst.
    if we aggregate our data with another pck, path of that packet
     will be our path too!*/
    Vector Path;
    Double SensedData;

    double[] SenseLoc;
    //Data Aggregatio Ratio For Every Neighbour
    Vector DAR;
    int AggCount;


    //packet RouteMinHopPckt, will send from sink has this type
    //this packet compute min hop to sink from every node
    public static final int    SINK_DATA            = 0 ;

    //data that sense in a source and aggregated will send to sink with this type
    public static final int    SOURCE_DATA          = 1 ;

    //ack from node that recieve data packet has this type
    public static final int    ACK_DATA             = 2 ;

    /**broadcast from a sensor among its neighbours
    *when a node recieve this packet,response with an ack-Neighbr packet
    *contains its ID */
    public static final int    FIND_NEIGHBOUR       = 3 ;

    //when a node recievve a FIND_NEIGHBOUR packet response with this packet
    //contains its ID
    public static final int    ACK_NEIGHBOUR        = 4 ;

    //if diffrence among data of two nodes is lower than epsilon , agg will done
    public static final int    EPSILON              = 5 ;
    
    //aggregate packet & packets that we can't aggregate them
    Vector listSendingPacket;
    //a list for sending again and again packets to neighbours
   //that we have,nt recieve any response from them!
    Vector Templist;
    // number of sending packet to node that we don't recieve ack from it
    int SendingCount;
    // a timer for recieving ack pkt from a neighbour that we send data to it
    public ACATimer Timer ;
    //number of minimum hops from this node to sink
    // that this number determine in first phase of algorithm
    double Hop2Sink;

    final double BaseEnrgy=0.6;//juoles-29 dei
/**Ants variables And Parameters */
  //  BlueAnts ant;

    // a random number that generate a random number in every node
    // that show data sense frrom environment
    Random rnd;
    environment env=null;

    boolean AckSend=false;
    
//===============================================================================
public Tr_SourceApp(){
    listSendingPacket = new Vector();
    m_Neighbours = new Vector();
    BaceSource =-1;
    iDstNid = -1;
    Hop2Sink = -1;
    SensedData = new Double(-1.0);
    rnd=new Random();
    Timer =null;
   Path = new Vector();
}
//===============================================================================    
protected void _start (){
    id = new Long(nid);
    iDstNid=0;
    env = new environment(DIM1,DIM2,DIM3,AREA_NUMBER,getTime());
    rTimer = setTimeout("print neighbours",250);
//    rTimer = setTimeout("SenseDataFromEnv",150 + 4);
    getRemainingEng();  //start the periodic querying of the remaining energy
    }
//===============================================================================
protected void _stop(){

   if (rTimer != null)
       cancelTimeout(rTimer);
   this.setCPUMode(3);     //turn off CPU when sim stops
}
//===============================================================================
    public synchronized void recvSensorPacket(Object data_){
    this.checkAlive();
    if ( data_ instanceof SensorPacket)
    {
      SensorPacket spkt = (SensorPacket)data_ ;
        if(spkt.getPktType() == SINK_DATA)
            RcvSinkPacket(spkt);
        else if(spkt.getPktType() == SOURCE_DATA)
            RcvSourcePacket(spkt);
    }
    super.recvSensorPacket(data_) ;
  
}
//===============================================================================
    protected void RcvSinkPacket(SensorPacket spkt){

       Tr_SinkPacket RouteMinHopPckt = (Tr_SinkPacket) spkt.getBody();
       double H2S=RouteMinHopPckt.getHop2Sink();
       Neighbour n;
    boolean flood=false;
        if( Hop2Sink > H2S || Hop2Sink == -1){
            Hop2Sink = H2S;
            for(int i=0; i<m_Neighbours.size();i++)
            {
               if(((Neighbour)(m_Neighbours.elementAt(i))).Level.compareTo("PARENT")== 1)
                    m_Neighbours.remove(i);
            }
                //assign parent
            n=new Neighbour("PARENT",RouteMinHopPckt.id,RouteMinHopPckt.getLoc());
            if(Hop2Sink -1 == 0 )
            n.SetH2S(0.1);
            else
            n.SetH2S(Hop2Sink -1);
            flood = true;
        }
        else if(Hop2Sink == H2S){
            //save this path too
            n=new Neighbour("PARENT",RouteMinHopPckt.id,RouteMinHopPckt.getLoc());
            n.SetH2S(Hop2Sink -1);
        }
        else   //   Hop2Sink < H2S
            return;       //ignore packet


        for(int m=0; m<m_Neighbours.size() ;m++)
            if(((Neighbour)m_Neighbours.elementAt(m)).id == n.id)//repeated neighbour
                return;

        m_Neighbours.addElement(n);
    if(flood == true)
        rTimer = setTimeout("SendSinkPacket",nid);
   }
//===============================================================================
    protected void RcvSourcePacket(SensorPacket spkt){

    Tr_SourcePacket pckt= (Tr_SourcePacket) spkt.getBody();
    double[] DstLoc = pckt.getLoc() ;
    Object data = pckt.getData();
    if(data instanceof Double ){
        Double pktdata=(Double) data;
        String str;
        double [] loc = new double[3];
        loc[0] = getX() ;         loc[1] = getY();         loc[2] = getZ();
        Tr_SourcePacket spckt=new Tr_SourcePacket(this.nid,pckt.id,loc,"Source Packet"+ id.toString(),207,pckt.getData(),pckt.BaseSrc,pckt.Path);
        listSendingPacket.add(spckt );}
 }
//===============================================================================
//===============================================================================
    private void PrintNeighbours(){
    if(m_Neighbours.size() == 0)
        System.out.println("Alone node!");
}
//===============================================================================
protected synchronized void timeout(Object data_){

    if (!sensorDEAD && data_.equals("SendData"))
        this.SendData();
    else if (!sensorDEAD && data_.equals("SendSinkPacket"))
        this.SendSinkPckt();
    else if (!sensorDEAD && data_.equals("print neighbours"))
        this.PrintNeighbours();

    else if(!sensorDEAD && data_.equals("SenseDataFromEnv"))
        this.SenseDataFromEnv();
    else if(!sensorDEAD && data_.equals("checkAlive"))
        this.checkAlive();
    else if ( data_.equals("getEnergy")) {
         Double enr =new Double( RemainEnergy());
        if (plotterPort.anyOutConnection()) {
            plotterPort.exportEvent(ENERGY_EVENT, new DoubleObj(enr.doubleValue()), null);
        }
         try{
            File EnergyFile=new File("./out/Tree/e"+id.toString()+".txt");
            FileWriter fw=new FileWriter(EnergyFile,true);
            BufferedWriter bw=new BufferedWriter(fw);
            bw.write(enr.doubleValue()+",");
            bw.close();
            fw.close();
        }
        catch(IOException ioe){
            System.out.println("an IOE is happend:"+ioe);
        }
        rTimer = setTimeout("getEnergy", 1);
        return;
    }
   }
//===============================================================================
protected void addRoute(long src_nid_, long dst_nid_, int timeout_){
    int type = 0;
    RTKey key = new RTKey(src_nid_, dst_nid_, timeout_);
    RTEntry entry = new RTEntry(new drcl.data.BitSet(new int[]{0}));

    /**connect to the port and send the message based on the RTConfig
        contract settings which are:
            RTConfig.Message (int type_, RTKey key_, RTEntry entry_, double timeout_)*/
    setRoutePort.sendReceive(new RTConfig.Message(type, key, entry, timeout_));
    }
//===============================================================================
protected void SendSinkPckt(){

    this.checkAlive();
    double [] loc = new double[3];
    loc[0] = getX();        loc[1] = getY();        loc[2] = getZ(); 
    Tr_SinkPacket spkt= new Tr_SinkPacket(nid,"MinimRouteConstruction",8,loc);
    spkt.setHop2Sink(Hop2Sink+1);
    downPort.doSending(new SensorAppWirelessAgentContract.Message(SensorAppWirelessAgentContract.BROADCAST_SENSOR_PACKET,
                     SINK_DATA,this.nid,spkt.getSize(),spkt));
    }
 //===============================================================================
 protected synchronized void recvSensorEvent(Object data_)
    {
        if(getTime() < 250)
            return;
        double [] loc = new double[3];   loc[0] = getX() ;         loc[1] = getY();         loc[2] = getZ();
        SensedData = new Double(env.getWarmth(loc,getTime()));
        Random rnd=new Random();
        setTimeout("SendData",rnd.nextDouble()*5+nid+PERIODIC_TIME_FOR_SENSING);
        return;
    }
 //===============================================================================
protected void SendData(){

    double [] loc = new double[3];    loc[0] = getX() ;      loc[1] = getY();       loc[2] = getZ();
    double[] iDstLoc;
    Tr_SourcePacket pckt;
    int indx=-1;
    if(Path.size() != 0){
        indx=Path.size()-1;
        while(Path.elementAt(indx) instanceof String){
            indx--;//omit last string of aggregated path packet
            while(Path.elementAt(indx) instanceof Path2Sink)
                indx--;//omit aggregated path packet
            indx--;//omit first string of aggregated path packet
            if(indx <= 0)
              break;
        }
    }
    if( indx <= 0) {
        Path.add(new Path2Sink(nid,"","Base Src"));
            pckt=new Tr_SourcePacket(this.nid,-1,loc ,"Source Packet"+ id.toString(),255,SensedData,BaceSource,Path);
    }
    else
        pckt=new Tr_SourcePacket(this.nid,((Path2Sink)(Path.elementAt(indx))).PathNodeId,loc,"Source Packet"+ id.toString(),255,SensedData,BaceSource,Path);

    listSendingPacket.add(pckt);
    if(m_Neighbours.size() == 0 ){
            listSendingPacket.clear();      AggCount = 0;   Path.clear();     BaceSource = this.nid;
  //          SenseDataFromEnv();
            return;
    }
    this.checkAlive();
    for(int i=0;i<listSendingPacket.size();i++){
        Tr_SourcePacket thisPkt = (Tr_SourcePacket)(listSendingPacket.elementAt(i));
        int index=-1;
        double d=rnd.nextDouble();
        double n=1.0/(double)m_Neighbours.size();
        for(int j=1;j<m_Neighbours.size()+1;j++  ){
            if( n*(j-1) <d  &&  d < n*j)
                index = j-1;
        }
        iDstNid = ((Neighbour)m_Neighbours.elementAt(index)).id ;
        thisPkt.addPathNode(new Path2Sink(iDstNid,((Neighbour)m_Neighbours.elementAt(index)).Level,""));
        iDstLoc = ((Neighbour)(m_Neighbours.elementAt(index))).Locaion;
        addRoute(this.nid,iDstNid,-1);
        downPort.doSending(new SensorAppWirelessAgentContract.Message(SensorAppWirelessAgentContract.UNICAST_SENSOR_PACKET,
                             iDstNid,this.nid,iDstLoc,255+78,SOURCE_DATA,eID,this.nid,thisPkt));
        }
    listSendingPacket.clear();  Path.clear(); BaceSource = this.nid;
 //   SenseDataFromEnv();
  }
//===============================================================================
protected void SenseDataFromEnv(){
   double [] loc = new double[3];   loc[0] = getX() ;         loc[1] = getY();         loc[2] = getZ();
    SensedData = new Double(env.getWarmth(loc,getTime()));
    BaceSource = this.nid;
    this.checkAlive();
    //we send data.because of sending data every 5sin send data, we have 5s for agg along path to the sink.
    double r = nid + PERIODIC_TIME_FOR_SENSING;
    setTimeout("SendData",r);

   }
//===============================================================================
public void checkAlive(){
// Contract type: ENERGY_QUERY =0
double energy = ((EnergyContract.Message)wirelessPhyPort.sendReceive(new EnergyContract.Message(0, -1.0,-1))).getEnergyLevel();
//double dst = ((EnergyContract.Message)wirelessPhyPort.sendReceive(new EnergyContract.Message(0, -1.0,-1))).getEnergyLevel();
  //wirelessPhyPort.
//check energy model to see if we are dead
if((energy <= 0.0) && (!this.sensorDEAD)) {
    this.sensorDEADAT = getTime();
    this.sensorDEAD = true;
    System.out.println("Sensor"+this.nid+" is dead at time: " + getTime());
    nn_ = nn_ - 1;  //1 less total node in system
    this._stop();
}
else if (this.sensorDEAD) {
    return;
}
else {
    //continuously check if Sensor is alive.
//    rTimer = setTimeout("checkAlive", 0.1);
    this.sensorDEAD = false;
}
}
//===============================================================================
  public void setNodeNumber(int nn_){
        this.nn_ = nn_;
    }
    //======================================================================================================
    public void setPriodicSenseing(double t){
        PERIODIC_TIME_FOR_SENSING   = t;
}
//======================================================================================================
    public void setDim(int d1,int d2, int d3){
        DIM1   = d1; DIM2 = d2; DIM3 = d3;
}
//======================================================================================================
    public void setAreaNum(int n){
        AREA_NUMBER =  n;
}
//======================================================================================================
//===============================================================================
public double   RemainEnergy(){
    double energy = ((EnergyContract.Message)wirelessPhyPort.sendReceive(new EnergyContract.Message(0, -1.0,-1))).getEnergyLevel();
    return energy;
}

    public double    UsedEnergy(){
    double enr = BaseEnrgy - RemainEnergy();
    return enr ;
}
//================================================================================
 protected synchronized void  getRemainingEng()
{
    rTimer = setTimeout("getEnergy", 1);
}
//===============================================================================

   }
