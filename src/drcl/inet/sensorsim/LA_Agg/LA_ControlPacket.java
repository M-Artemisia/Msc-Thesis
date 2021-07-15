package drcl.inet.sensorsim.LA_Agg;

import drcl.net.Packet;

/**
 * Created by IntelliJ IDEA.
 * User: mali
 * Date: Nov 2, 2009
 * Time: 11:51:38 PM
 * To change this template use File | Settings | File Templates.
 */

public class LA_ControlPacket extends Packet {

   public  Long ID;
 
    //7 esfand
   public  double [] loc;
 //END 7 esfand

    private String NAME;
    //30 dei
   // //In Src Node NO Base Node.So it Change In every transmition
   public double             H2sInRcvr ;
   //In Src Node NO Base Node.So it Change In every transmition
   public double          EnrgInRcvr ;
   //END 30 dei

    private   double  RemainEnergy;
       private double  DAR;



        public LA_ControlPacket(long nid ,String name ,int size ,Object Body
         /*30 dei*/,double h2sInRcvr , double enrgInRcvr,double[] Myloc/*7 esfand*/){

            ID = new Long(nid);
            NAME = name;
            //DAR
            this.body = Body;
            this.size = size;
          //30 dei
            H2sInRcvr = h2sInRcvr ;
            EnrgInRcvr = enrgInRcvr;
           //END 30 dei

            loc = new double[3];
        loc=Myloc;
        }
    public LA_ControlPacket(long nid ,String name ,int size,double[] Myloc,double RemEnergy,double dar){

        loc = new double[3];
        loc=Myloc;

        id=nid;
        ID = new Long(nid);
        NAME = name;
        this.size = size;

        RemainEnergy = RemEnergy;
        DAR = dar;

         //7 esfand
            H2sInRcvr = 0;
            EnrgInRcvr = 0;
           //END 7 esfand
        }                    

    //END 7 esfand

    public String getName(){
            return NAME;
        }
    public double getData(){
        return ((Double)body).doubleValue();
    }

    //30 dei
    public double getH2SInSrcNode(){
        return H2sInRcvr;
    }
    public double getEnrgprcntInSrcNode(){
        return EnrgInRcvr;
    }
//END 30 dei
       //7 esfand
    public double[] getLoc(){
    return loc;
    }
 //END 7 esfand

    public double getDAR(){return DAR;  }
    public double getEP(){return RemainEnergy;  }
        public Object clone(){
            return  new LA_ControlPacket(ID.longValue(), getName(), size,this.body,H2sInRcvr,EnrgInRcvr,loc) ;
        }
    }

