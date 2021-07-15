

package drcl.inet.sensorsim.DirectLink;
import drcl.inet.sensorsim.SensorApp;
import drcl.inet.sensorsim.SensorPacket;
import drcl.comp.ActiveComponent;
import drcl.comp.Port;
import drcl.data.DoubleObj;
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
public class DL_SinkApp extends SensorApp implements ActiveComponent {

private int totalINpackets = 0;
public static final String PACKETS_REC_EVENT     = "Total Packets Received by Sink";
public static final String PLOTTER_PORT_ID  = ".PacketsReceivedPlot";
public Port packetsPlotPort = addEventPort(PLOTTER_PORT_ID); //for total packets received.
private int totalVirtualPackets = 0;
public static final String VIRTUAL_PACKETS_REC_EVENT     = "Total Theoretical Packets Received";
public static final String PLOTTER_PORT_ID_2  = ".theo_PacketsReceivedPlot";
public Port packetsPlotPort2 = addEventPort(PLOTTER_PORT_ID_2);
    public static final int    SOURCE_DATA          = 1 ;

//===============================================================================
    public DL_SinkApp(){
        totalINpackets=0;
        totalVirtualPackets=0;
    }
//===============================================================================
     protected void _start (){    }
//===============================================================================
     protected void _stop()
       {
        if (rTimer != null)
               cancelTimeout(rTimer);
           this.setCPUMode(3);     //turn off CPU when sim stops
         }
//===============================================================================
     protected synchronized void timeout(Object data_){    }
//===============================================================================
    public synchronized void recvSensorPacket(Object data_)
    {

        if ( data_ instanceof SensorPacket) {
            SensorPacket spkt = (SensorPacket)data_ ;
            if(spkt.getPktType() == SOURCE_DATA )
            {
    this.totalINpackets = this.totalINpackets + 1;
    if (packetsPlotPort.anyOutConnection()) {
        packetsPlotPort.exportEvent(PACKETS_REC_EVENT, new DoubleObj(this.totalINpackets), null);
    }
    this.totalVirtualPackets = this.totalVirtualPackets +1 ;//added by one because of packet itself: agg+1=all packets
    if (packetsPlotPort2.anyOutConnection()) {
        packetsPlotPort2.exportEvent(VIRTUAL_PACKETS_REC_EVENT, new DoubleObj(totalVirtualPackets), null);
    }

    try{
            File file;
            FileWriter fw;
            BufferedWriter bw;
            file = new File("./out/DL/total.txt");
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
            }
        super.recvSensorPacket(data_) ;
        }
 }
//===============================================================================
}