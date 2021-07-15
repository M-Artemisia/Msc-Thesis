package drcl.inet.sensorsim.MHTrAgg;

import drcl.inet.sensorsim.SensorApp;
import drcl.inet.sensorsim.SensorPacket;
import drcl.inet.sensorsim.SensorAppWirelessAgentContract;
import drcl.inet.sensorsim.MHTrNoAgg.Tr_SourcePacket;
import drcl.inet.sensorsim.AC_Agg.environment;
import drcl.inet.sensorsim.AC_Agg.Neighbour;
import drcl.inet.sensorsim.LA_Agg.*;
import drcl.inet.data.RTKey;
import drcl.inet.data.RTEntry;
import drcl.inet.contract.RTConfig;
import drcl.inet.mac.EnergyContract;
import drcl.comp.ActiveComponent;
import drcl.comp.Port;
import drcl.data.DoubleObj;

import java.util.Vector;
import java.lang.Math;
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
public class AgTr_SourceApp extends SensorApp implements ActiveComponent {

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

    int nn_;
    Long id;
    long BaceSource;
    Vector Path=new Vector();
    Vector pInfo=new Vector();
    Double SensedData;
    int AggCount;
    public static final int    SINK_DATA            = 0 ;
    public static final int    SOURCE_DATA          = 1 ;
    public static final int    EPSILON              = 5 ;
    Vector listSendingPacket=new Vector();
    double Hop2Sink;
    final double BaseEnrgy=0.6;//juoles
    Random rnd;
    environment env=null;
    Vector m_Neighbours=new Vector();

//===============================================================================
public AgTr_SourceApp(){
    BaceSource =-1;
    Hop2Sink = -1;
    SensedData = new Double(-1.0);
    AggCount =0;
    rnd=new Random();
}

//===============================================================================    
protected void _start (){
    id = new Long(nid);
    env = new environment(DIM1,DIM2,DIM3,AREA_NUMBER,getTime());
    rTimer = setTimeout("print neighbours",250);
    getRemainingEng();
}
//===============================================================================
protected void _stop(){
   if (rTimer != null)
       cancelTimeout(rTimer);
      File DieFile;
   DieFile = new File("./out/AggTree/die.txt");
         try{
                FileWriter fw=new FileWriter(DieFile,true);
                BufferedWriter bw=new BufferedWriter(fw);
                bw.write(new Double(getTime()).toString());
                bw.close();
                fw.close();
            }
            catch(IOException ioe){
                System.out.println("an IOE is happend:"+ioe);
            }
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
//===============================================================================
    protected void RcvSinkPacket(SensorPacket spkt){
    LA_SinkPacket RouteMinHopPckt = (LA_SinkPacket) spkt.getBody();
    double H2S=RouteMinHopPckt.getHop2Sink();
    Neighbour n;
    boolean flood=false;
    if( Hop2Sink > H2S || Hop2Sink == -1){
        Hop2Sink = H2S;
        m_Neighbours.clear();
        n=new Neighbour("PARENT",RouteMinHopPckt.id,RouteMinHopPckt.getLoc());
        if(Hop2Sink -1 == 0 )
            n.SetH2S(0.1);
        else
        n.SetH2S(Hop2Sink -1);
        flood = true;
    }
    else if(Hop2Sink == H2S){
        n=new Neighbour("PARENT",RouteMinHopPckt.id,RouteMinHopPckt.getLoc());
        n.SetH2S(Hop2Sink -1);
     //   flood = true;
    } 
    else
        return;
    for(int m=0; m<m_Neighbours.size() ;m++)
        if(((Neighbour)m_Neighbours.elementAt(m)).id == n.id)//repeated neighbour
                 //  m_Neighbours.remove(m);//ignore repeated neighbours
        return;

    m_Neighbours.addElement(n);
    if(flood)
        rTimer = setTimeout("SendSinkPacket",nid);
   }
//===============================================================================
    protected void RcvSourcePacket(SensorPacket spkt){
    LA_SourcePacket pckt= (LA_SourcePacket) spkt.getBody();
     double[] DstLoc = pckt.getLoc() ;
    Object data = pckt.getData();
    if(data instanceof Double ){
        Double dar=new Double(0.0);
        Double pktdata=(Double) data;
        if(SensedData.doubleValue() == -1.0)
            return;
     if(Math.abs(pktdata.doubleValue()-SensedData.doubleValue()) < EPSILON ){
          for(int i=0;i<pckt.PathNode.size() ; i++)
                     Path.add(pckt.PathNode.elementAt(i));
          aggregationFunction(pktdata,pckt.getNumOfAggPckt());
          dar = CalculateDAR(pktdata.doubleValue());
          BaceSource = pckt.BaseSrc;
      }
      else{
     //  Double pktdata=(Double) data;
        double [] loc = new double[3];  loc[0] = getX();    loc[1] = getY();         loc[2] = getZ();
          pckt.addPathNode(new Long(this.nid));
        LA_SourcePacket spckt=new LA_SourcePacket(this.nid,-1,loc,"Source Packet"+ id.toString(),255,pckt.getNumOfAggPckt()
                  ,pckt.getData(),pckt.BaseSrc,pckt.PathNode,pckt.PathInfo,true);
          listSendingPacket.add(spckt );
     }
   }
 }
 //===============================================================================
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
    
  else if ( data_.equals("getEnergy")) {
         Double enr =new Double( RemainEnergy());
        if (plotterPort.anyOutConnection()) {
            plotterPort.exportEvent(ENERGY_EVENT, new DoubleObj(enr.doubleValue()), null);
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
    setRoutePort.sendReceive(new RTConfig.Message(type, key, entry, timeout_));
    }
//===============================================================================
protected void SendSinkPckt(){
    this.checkAlive();    
    double [] loc= new double[3];   loc[0]=getX();    loc[1]=getY();       loc[2] = getZ();
    LA_SinkPacket spkt= new LA_SinkPacket(nid,"MinimRouteConstruction",8,loc);
    spkt.setHop2Sink(Hop2Sink+1);
    downPort.doSending(new SensorAppWirelessAgentContract.Message(SensorAppWirelessAgentContract.BROADCAST_SENSOR_PACKET,
                     SINK_DATA,this.nid,8,spkt));
    }
//===============================================================================
protected void SendData(){
    this.checkAlive();    this.WakeUp();  Path.add(this.id);
    double [] loc = new double[3]; loc[0] = getX() ;loc[1] = getY();loc[2] = getZ();
    double[] iDstLoc;long iDstNid=-1;
    LA_SourcePacket pckt=new LA_SourcePacket(this.nid,-1,loc ,"Source Packet"+ id.toString(),207,AggCount,SensedData,BaceSource,Path,pInfo,true);
    listSendingPacket.add(pckt);
    int s = listSendingPacket.size();
     if(m_Neighbours.size() == 0 ){
            listSendingPacket.clear();      AggCount = 0;       BaceSource = this.nid; return;
    }
    this.checkAlive();

    for(int i=0;i<listSendingPacket.size();i++){
        LA_SourcePacket thisPkt = (LA_SourcePacket)(listSendingPacket.elementAt(i));
        int index=-1;
        double d=rnd.nextDouble();
        double n=1.0/(double)m_Neighbours.size();
        for(int j=1;j<m_Neighbours.size()+1;j++  ){
            if( n*(j-1) <d  &&  d < n*j)
                index = j-1;
        }
        iDstNid = ((Neighbour) m_Neighbours.elementAt(index)).id ;
        iDstLoc = ((Neighbour) m_Neighbours.elementAt(index)).Locaion;
        addRoute(this.nid,iDstNid,-1);
        downPort.doSending(new SensorAppWirelessAgentContract.Message(SensorAppWirelessAgentContract.UNICAST_SENSOR_PACKET,
                         iDstNid,this.nid,iDstLoc/*7 esfand*/,255+78,SOURCE_DATA,eID,-1,listSendingPacket.elementAt(i)));
    }
    listSendingPacket.clear();    AggCount = 0;    Path.clear();    pInfo.clear();    BaceSource = this.nid;
  }
//===============================================================================
//===============================================================================
protected void aggregationFunction(Double Data,int aggCount ){
    /*    Number Of pkts that are aggregated, is equal to AggCount+1     */
    SensedData =new Double(
            (SensedData.doubleValue() * (AggCount+1) + Data.doubleValue()*(aggCount+1))/
                    (AggCount+aggCount+2));
    /*      1 is for aggregatio betwen this too pkt. our node and recieving node!     */
    AggCount = AggCount + aggCount + 1;
    }
//===============================================================================
protected Double CalculateDAR(double pktdata){
    return (new Double(1-((Math.abs(pktdata-SensedData.doubleValue()))/EPSILON)));
}
//===============================================================================
//===============================================================================
   protected double EuclideanDist(double X, double Y, double Z,double X2, double Y2, double Z2)
   {
       double dx = X2 - X;
       double dy = Y2 - Y;
       double dz = Z2 - Z;
       return(Math.sqrt((dx*dx) + (dy*dy) + (dz*dz)));
   }
//===============================================================================
public void checkAlive(){

    double energy = ((EnergyContract.Message)wirelessPhyPort.sendReceive(new EnergyContract.Message(0, -1.0,-1))).getEnergyLevel();
    if((energy <= 0.0) && (!this.sensorDEAD)) {
        this.sensorDEADAT = getTime();
        this.sensorDEAD = true;
         nn_ = nn_ - 1;  //1 less total node in system
         this._stop();
    }
    else if (this.sensorDEAD) {
        return;
    }
else {
    this.sensorDEAD = false;
}
}
//===============================================================================
public void WakeUp() {
    if (this.cpuMode != 2) {
        this.setCPUMode(2);    //CPU_IDLE=0, CPU_SLEEP=1, CPU_ACTIVE=2, CPU_OFF=3
    }
    EnergyContract.Message temp = (EnergyContract.Message)wirelessPhyPort.sendReceive(new EnergyContract.Message(1, -1.0, 0));
    if (temp.getRadioMode() != 0) {
        System.out.println("Unable to turn radio back on to Idle mode. Its mode is: " + temp.getRadioMode());
    }
}
//===============================================================================
  public double getRandomNumber(double llim, double ulim,long seed){
        if (llim >= ulim) {
            System.out.println("RandomNumber Generator Error: Lower Bound is greater then Higher Bound");
            return -1.0;
        }
        Random generator = new Random(seed);
        return((generator.nextDouble()*llim)+ (ulim-llim));
    }
//===============================================================================
  public void setNodeNumber(int nn_){
        this.nn_ = nn_;
    }
//===============================================================================
  public void setTimeBegin(String s){
//      System.out.println(s);
  }
//===============================================================================
   public  double RemainEnergy(){
    double energy = ((EnergyContract.Message)wirelessPhyPort.sendReceive(new EnergyContract.Message(0, -1.0,-1))).getEnergyLevel();
    return energy;
}
//================================================================================
     protected synchronized void  getRemainingEng()
    {
        rTimer = setTimeout("getEnergy", 1);
    }

//======================================================================================================
    public void setPriodicSenseing(double t){
        PERIODIC_TIME_FOR_SENSING   = t;
}
//======================================================================================================

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
public double    UsedEnergy(){
    double enr = BaseEnrgy - RemainEnergy();
    return enr ;
}
//===============================================================================
    
}
