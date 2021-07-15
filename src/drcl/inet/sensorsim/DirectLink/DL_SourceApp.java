package drcl.inet.sensorsim.DirectLink;

import drcl.inet.sensorsim.SensorApp;
import drcl.inet.sensorsim.SensorAppWirelessAgentContract;
import drcl.inet.sensorsim.AC_Agg.environment;
import drcl.inet.data.RTKey;
import drcl.inet.data.RTEntry;
import drcl.inet.contract.RTConfig;
import drcl.inet.mac.EnergyContract;
import drcl.comp.ActiveComponent;
import drcl.comp.Port;
import drcl.data.DoubleObj;

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
public class DL_SourceApp extends SensorApp implements ActiveComponent {


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
    Double SensedData;
    public static final int    SOURCE_DATA          = 1 ;
    final double BaseEnrgy=1.0;//juoles-29 dei
    environment env=null;


//===============================================================================
public DL_SourceApp(){
    BaceSource =-1;
    SensedData = new Double(-1.0);
}
//===============================================================================    
protected void _start (){
    id = new Long(nid);
    env = new environment(DIM1,DIM2,DIM3,AREA_NUMBER,getTime());
    rTimer = setTimeout("SenseDataFromEnv",150 + 4);
    
    getRemainingEng();  //start the periodic querying of the remaining energy
    }
//===============================================================================
protected void _stop(){

   if (rTimer != null)
       cancelTimeout(rTimer);
   this.setCPUMode(3);     //turn off CPU when sim stops
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
protected void SendData(){

    double [] loc = new double[3];    loc[0] = getX() ;      loc[1] = getY();       loc[2] = getZ();
    DL_SourcePacket pckt;
    pckt=new DL_SourcePacket(this.nid,loc ,"Source Packet"+ id.toString(),207,SensedData,BaceSource);
    this.checkAlive();
    addRoute(this.nid,sink_nid,-1);
    downPort.doSending(new SensorAppWirelessAgentContract.Message(SensorAppWirelessAgentContract.UNICAST_SENSOR_PACKET,
                         sink_nid,this.nid,sinkPos,pckt.getSize(),SOURCE_DATA,eID,this.nid,pckt));
    BaceSource = this.nid;
    SenseDataFromEnv();
  }
//===============================================================================
    protected synchronized void timeout(Object data_){

    if (!sensorDEAD && data_.equals("SendData"))
        this.SendData();
    if ( data_.equals("getEnergy")) {
        Double enr =new Double( RemainEnergy());
        if (plotterPort.anyOutConnection()) {
            plotterPort.exportEvent(ENERGY_EVENT, new DoubleObj(enr.doubleValue()), null);
        }
         try{
            File EnergyFile=new File("C:/simRslt/DL/e"+id.toString()+".txt");
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
    if(data_.equals("SenseDataFromEnv"))
        SenseDataFromEnv();
    }
//===============================================================================
protected void SenseDataFromEnv(){
    double [] loc = new double[3];   loc[0] = getX() ;         loc[1] = getY();         loc[2] = getZ();
    SensedData = new Double(env.getWarmth(loc,getTime()));
    BaceSource = this.nid;
    this.checkAlive();
    //we send data.because of sending data every 5sin send data, we have 5s for agg along path to the sink.
    Random rnd=new Random() ;
    double r=rnd.nextDouble()*5+PERIODIC_TIME_FOR_SENSING;
    setTimeout("SendData",r);

   }
//===============================================================================
public void checkAlive(){
double energy = ((EnergyContract.Message)wirelessPhyPort.sendReceive(new EnergyContract.Message(0, -1.0,-1))).getEnergyLevel();
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
//===============================================================================
public double   RemainEnergy(){
    double energy = ((EnergyContract.Message)wirelessPhyPort.sendReceive(new EnergyContract.Message(0, -1.0,-1))).getEnergyLevel();
    return energy;
}
//================================================================================
 protected synchronized void  getRemainingEng()
{
    rTimer = setTimeout("getEnergy", 1);
}
//===============================================================================

   }
