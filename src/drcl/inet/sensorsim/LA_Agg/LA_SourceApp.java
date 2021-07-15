package drcl.inet.sensorsim.LA_Agg;

import drcl.inet.sensorsim.SensorApp;
import drcl.inet.sensorsim.SensorPacket;
import drcl.inet.sensorsim.SensorAppWirelessAgentContract;
import drcl.inet.sensorsim.AC_Agg.environment;
import drcl.inet.sensorsim.AC_Agg.Neighbour;
import drcl.inet.data.RTKey;
import drcl.inet.data.RTEntry;
import drcl.inet.contract.RTConfig;
import drcl.inet.mac.EnergyContract;
import drcl.comp.ActiveComponent;
import drcl.comp.Port;
import drcl.comp.ACATimer;
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
public class LA_SourceApp extends SensorApp implements ActiveComponent {

    //Parameters that should be set before running,or in tcl file
     public  double PERIODIC_TIME_FOR_SENSING        = 10 ;
     public  double RCV_EVENT                        =2*PERIODIC_TIME_FOR_SENSING+5;
     public  double PERIODIC_ACK_COUNT               = 1 ;
     public  int    DIM1                             = 0;//meter
     public  int    DIM2                             = 0;//meter
     public  int    DIM3                             = 0;//meter
     public  int    AREA_NUMBER                      = 1;
     public  double ENRGY_CO                         = 1 ;
     public  double HOP_CO                           = 1 ;
     public  double DAR_CO                           = 1 ;

/*To collect and display energy levels in graph form. Created a
    port that will output to a plotter*/
public static final String ENERGY_EVENT     = "Remaining Energy";
public static final String REM_ENERGY_PORT_ID  = ".plotter";
public Port plotterPort = addEventPort(REM_ENERGY_PORT_ID);

    boolean SendIt = false;
    int nn_;
    Long id;
    long DstNeighbrNid;
    double DstNeighbrLoc[];
    long ackNghbrId;
    long BaceSource;
    Vector Path=new Vector();
    /*
    ONLY 4 TESTING
     */
    Vector pInfo=new Vector();
    Double SensedData;
    //Data Aggregatio Ratio For Every Neighbour
    int AggCount;

    //packet RouteMinHopPckt, will send from sink has this type
    //this packet compute min hop to sink from every node
    public static final int    SINK_DATA            = 0 ;

    //data that sense in a source and aggregated will send to sink with this type
    public static final int    SOURCE_DATA          = 1 ;

    //ack from node that recieve data packet has this type
    public static final int    ACK_DATA             = 2 ;
    public static final int    DIE             = 3;

    //if diffrence among data of two nodes is lower than epsilon , agg will done
    public static final int    EPSILON              = 5 ;
    Vector listSendingPacket=new Vector();
    double Hop2Sink;
    final double BaseEnrgy=0.6;//juoles
    LA la;
    //parameters of LA function
    final double REWARD_PARAM  = 1;
    final double PENALTY_PARAM = 1;
    Random rnd;

    File file;
    File EnergyFile,UseEnergyFile ;
    
    environment env=null;
    boolean ReapetedNode = false;
    Vector m_Neighbours=new Vector();
    boolean AckSend=true;


    Vector lastSources=new Vector();
    double LastTime=0.0;

    Vector DstIDs=new Vector();
    ACATimer ackTimer;
    //end 10 esfand logfile

//===============================================================================
public LA_SourceApp(){
    BaceSource =-1;
    DstNeighbrNid = -1;
    DstNeighbrLoc= new double[3];
    ackNghbrId = -1;
    Hop2Sink = -1;
    SensedData = new Double(-1.0);
    AggCount =0;
    rnd=new Random();
}

//===============================================================================    
protected void _start (){
    id = new Long(nid);
    env = new environment(DIM1,DIM2,DIM3,AREA_NUMBER,getTime());
    setTimeout("create LA", 250);
    getRemainingEng();
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
        else if(spkt.getPktType() == ACK_DATA)
            RcvAckPacket(spkt);
        else if(spkt.getPktType() == DIE)
            RcvDiePkt(spkt);
       }
    super.recvSensorPacket(data_) ;
  
}
//===============================================================================
    public void RcvDiePkt(SensorPacket spkt){
    LA_ControlPacket Pckt = (LA_ControlPacket) spkt.getBody();
    long id=Pckt.id;
 for(int i=0; i< m_Neighbours.size() ;i++){
       Neighbour n = (Neighbour)(m_Neighbours.elementAt(i));
       if(n.id == id){
            la.DelAct_EditProb(i);
            break;
      }
   }
}
//===============================================================================
    protected void RcvSinkPacket(SensorPacket spkt){
    LA_SinkPacket RouteMinHopPckt = (LA_SinkPacket) spkt.getBody();
    double H2S=RouteMinHopPckt.getHop2Sink();
    if(RouteMinHopPckt.id == this.nid)
        return;
    if(m_Neighbours.size() >0)
               if(((Neighbour)m_Neighbours.elementAt(0)).id == 0)
                   return;
            
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
   else if(Hop2Sink == H2S -1){
        n = new Neighbour("SAME",RouteMinHopPckt.id,RouteMinHopPckt.getLoc());
        n.SetH2S(Hop2Sink);
    }
    else// if(  Hop2Sink < H2S  )
    {
        n = new Neighbour("Child",RouteMinHopPckt.id,RouteMinHopPckt.getLoc());
        n.SetH2S(Hop2Sink+1);
        return;
    }
    for(int m=0; m<m_Neighbours.size() ;m++)
        if(((Neighbour)m_Neighbours.elementAt(m)).id == n.id)//repeated neighbour
                   m_Neighbours.remove(m);//ignore repeated neighbours

    m_Neighbours.addElement(n);
    addNeighbour(new Long(RouteMinHopPckt.id));
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
          lastSources.add(pckt.ID);
          for(int i=0;i<pckt.PathNode.size() ; i++)
                     Path.add(pckt.PathNode.elementAt(i));
          aggregationFunction(pktdata,pckt.getNumOfAggPckt());
          dar = CalculateDAR(pktdata.doubleValue());
          BaceSource = pckt.BaseSrc;
      }
      else{
        double [] loc = new double[3];  loc[0] = getX();    loc[1] = getY();         loc[2] = getZ();
          pckt.addPathNode(new Long(this.nid));
        LA_SourcePacket spckt=new LA_SourcePacket(this.nid,pckt.id,loc,"Source Packet"+ id.toString(),255,pckt.getNumOfAggPckt()
                  ,pckt.getData(),pckt.BaseSrc,pckt.PathNode,pckt.PathInfo,AckSend);
          listSendingPacket.add(spckt );
      }
            SendAckPacket(pckt.id,dar,DstLoc);
   }
 }
 //===============================================================================
     public void SetAckSend(){

    AckSend = true;
    setTimeout("SetAckSend",PERIODIC_ACK_COUNT*(RCV_EVENT));
    return;
 }
//===============================================================================
    protected void RcvAckPacket(SensorPacket spkt){

    LA_ControlPacket pckt= (LA_ControlPacket) spkt.getBody();
    int index =-1;
    for(int i=0; i<m_Neighbours.size() ;i++){
        Neighbour n = (Neighbour)(m_Neighbours.elementAt(i));
        if(n.id == pckt.ID.longValue()){
            n.SetDAR(pckt.getDAR());
            n.SetEP(pckt.getEP());
            if(pckt.getEP() < 1.70e-4){
   //             la.DelAct_EditProb(i);
    //            m_Neighbours.remove(i);
                System.out.println("del a nghbr");
                if(la.m_iActionNum == 0 || m_Neighbours.size()==0){
                   System.out.println("Alone node");
                   nn_ = nn_ -1;
                   this.sensorDEADAT = getTime();
                   this.sensorDEAD = true;
                   stop();
                   return;
               }
                return;
            }
            index = i;
            break;
        }
    }
    if(DstIDs.indexOf(pckt.ID)!= -1)
        DstIDs.remove(pckt.ID);
    else
        System.out.println();
    if(DstIDs.size() == 0 && ackTimer!=null)
        cancelTimeout(ackTimer);
    if(index==-1){
        System.out.println("ERR");
        return;
    }
    DecideRewORPen(index);
    }
//===============================================================================
    protected synchronized void recvSensorEvent(Object data_)
       {
           if(getTime() < 250)// || getTime()-LastTime < PERIODIC_TIME_FOR_SENSING)
            return;
           LastTime = getTime();
           double [] loc = new double[3];   loc[0] = getX() ;         loc[1] = getY();         loc[2] = getZ();
           SensedData = new Double(env.getWarmth(loc,getTime()));
           Random rnd=new Random();
           setTimeout("SendData",rnd.nextDouble()*5+nid+PERIODIC_TIME_FOR_SENSING);
           return;
       }
//===============================================================================
    private void addNeighbour(Long NghbrID){
        if(la != null){
           la.AddAct_EditProb(NghbrID);
        }
        else if(getTime() > 250)
            createLA();
    }
//===============================================================================
protected synchronized void timeout(Object data_){
     if (!sensorDEAD && data_.equals("SendData"))
        this.SendData();
    else if (!sensorDEAD && data_.equals("SendSinkPacket"))
        this.SendSinkPckt();
    else if(!sensorDEAD && data_.equals("create LA"))
        this.createLA();
     else if(!sensorDEAD && data_.equals("SetAckSend"))
             this.SetAckSend();

    
  else if ( data_.equals("getEnergy")) {
         Double enr =new Double( RemainEnergy());
        if (plotterPort.anyOutConnection()) {
            plotterPort.exportEvent(ENERGY_EVENT, new DoubleObj(enr.doubleValue()), null);
        }
        rTimer = setTimeout("getEnergy", 1);
        return;
    }
    else if ( data_.equals("EnergyFile")) {
          try{
                FileWriter fw=new FileWriter(EnergyFile,true);
                BufferedWriter bw=new BufferedWriter(fw);
                bw.write(RemainEnergy()+",");
                bw.close();
                fw.close();
            }
            catch(IOException ioe){
                System.out.println("an IOE is happend:"+ioe);
            }

         try{
                FileWriter fw=new FileWriter(UseEnergyFile,true);
                BufferedWriter bw=new BufferedWriter(fw);
                bw.write(BaseEnrgy - RemainEnergy()+",");
                bw.close();
                fw.close();
            }
            catch(IOException ioe){
                System.out.println("an IOE is happend:"+ioe);
            }

            setTimeout("EnergyFile", 1);
            return;
        }
    else if(data_.equals("AckTimer")){
         if(DstIDs.size()==0){
                cancelTimeout(ackTimer);
            return;
         }
         else{
             System.out.println("Not Ack!");
             for(int j=0;j<DstIDs.size() ; j++){
               for(int i=0; i< m_Neighbours.size() ;i++){
                   Neighbour n = (Neighbour)(m_Neighbours.elementAt(i));
                   if(n.id == ((Long)(DstIDs.elementAt(j))).longValue()){
                       ((Neighbour)(m_Neighbours.elementAt(i))).NumNotAck++;
                       if(((Neighbour)(m_Neighbours.elementAt(i))).NumNotAck > 10){
                          la.DelAct_EditProb(i);
                          m_Neighbours.remove(i);
                           if(la.m_iActionNum == 0 || m_Neighbours.size()==0){
                               System.out.println("Alone node");
                               nn_ = nn_ -1;
                               this.sensorDEADAT = getTime();
                               this.sensorDEAD = true;
                               stop();
                               return;
                           }

                       }
                       else{
                        ((Neighbour)(m_Neighbours.elementAt(i))).SetDAR(0.0);
                        ((Neighbour)(m_Neighbours.elementAt(i))).SetEP(0.0);
                        ((Neighbour)(la.m_iActions.elementAt(i))).SetDAR(0.0);
                        ((Neighbour)(la.m_iActions.elementAt(i))).SetEP(0.0);
                     // DecideRewORPen(i);
                       }
                    break;
                  }
               }
                 DstIDs.remove(j);
             }
          }
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
    LA_SourcePacket pckt=new LA_SourcePacket(this.nid,-1,loc ,"Source Packet"+ id.toString(),207,AggCount,SensedData,BaceSource,Path,pInfo,AckSend);
    listSendingPacket.add(pckt);
    int s = listSendingPacket.size();
  //  if(((Neighbour)(m_Neighbours.elementAt(0))).id != 0 )
   //     ackTimer = setTimeout("AckTimer",10);
    
    for(int i=0;i<s;i++){
        if(la == null ){
            if(m_Neighbours.size() ==0){
                return;
             }
           else  createLA();
     }
        boolean isdst=false;
        int index=-1;
        int counter=0;
        
        while(!isdst)
    {
         counter++;
         index = la.SelectAction();
         if(index==-1)
             System.out.println("error in index SendData in node "+nid);
            long tmpid = ((Neighbour)(m_Neighbours.elementAt(index))).id ;
         if(((LA_SourcePacket)(listSendingPacket.elementAt(i))).PathNode.indexOf(new Long(tmpid)) == -1)
          isdst=true;
         else {
          int p=0;
        }
         if(counter >3* m_Neighbours.size()){
           isdst = true;
       //    ReapetedNode = true;
         }


    }

        iDstNid = ((Neighbour) m_Neighbours.elementAt(index)).id ;
        DstIDs.add(new Long(iDstNid));
        iDstLoc = ((Neighbour) m_Neighbours.elementAt(index)).Locaion;
    /*   if(((LA_SourcePacket)(listSendingPacket.elementAt(i))).PathNode.indexOf(new Long(iDstNid)) != -1)
           ReapetedNode = true;
     */  addRoute(this.nid,iDstNid,-1);
       downPort.doSending(new SensorAppWirelessAgentContract.Message(SensorAppWirelessAgentContract.UNICAST_SENSOR_PACKET,
                         iDstNid,this.nid,iDstLoc/*7 esfand*/,255+78,SOURCE_DATA,eID,this.nid,listSendingPacket.elementAt(i)));
    }
    listSendingPacket.clear();   lastSources.clear(); AggCount = 0;    Path.clear();    pInfo.clear();    BaceSource = this.nid;
  }
//===============================================================================
protected void SendAckPacket(long Dst_id,Double dar,double [] DstLoc){
    this.checkAlive();
    LA_ControlPacket pckt=new LA_ControlPacket(nid,"Ack Data Packet",8,this.myPos,RemainEnergy()/BaseEnrgy,dar.doubleValue());
    addRoute(this.nid,Dst_id,-1);    
    downPort.doSending(new SensorAppWirelessAgentContract.Message(SensorAppWirelessAgentContract.UNICAST_SENSOR_PACKET,
           Dst_id,this.nid,DstLoc,8,ACK_DATA,eID,this.nid,pckt));
    }
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
public    void DecideRewORPen(int i){
    Neighbour n = (Neighbour)m_Neighbours.elementAt(i);
    LAinfo info=new  LAinfo();
    info.setH2S(n.getH2S());//29 dei
    info.setEP(new Double(n.getEP()));//29 dei
    info.setDAR(new Double(n.getDAR()));
    if(la == null){
        if(m_Neighbours.size()==0){
            System.out.println("Null LA! Maybe an alone node "+nid);
            return;
        }
        else
            createLA();
     }
    if(ReapetedNode == true)
    {
        ReapetedNode = false;
        la.forcePenalty(i,info);
    }
    else if(ReapetedNode == false )
        la.rewardOrPenalize(i,info);
    }
//===============================================================================
public    void createLA(){
    if(m_Neighbours.size()==0 ){
        System.out.println("no neighbour! maybe an alone node...!");
        this.sensorDEAD = true;
        stop();
        return;
    }
    la= new LA(m_Neighbours.size(),m_Neighbours,REWARD_PARAM,PENALTY_PARAM,ENRGY_CO,HOP_CO,DAR_CO);
    }
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
    public void setPriodicAckCount(double c){
        PERIODIC_ACK_COUNT   = c;
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
    public void setLACoef(double enrgy,double hop,double dar){
        ENRGY_CO = enrgy ;
        HOP_CO   = hop ;
        DAR_CO   = dar ;

}

//===============================================================================
public double    UsedEnergy(){
    double enr = BaseEnrgy - RemainEnergy();
    return enr ;
}
//===============================================================================
    
}
