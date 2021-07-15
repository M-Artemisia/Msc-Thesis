package drcl.inet.sensorsim.LA_Agg;

/**
 * Created by IntelliJ IDEA.
 * User: mali
 * Date: Nov 22, 2009
 * Time: 2:31:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class LAinfo {

 private   Double DAR;
 private   double Hop2Sink;
 private   Double  EnergyPrcntg;
 private   Double  ClustringFactor;


    public LAinfo(){
        DAR = new Double(0.0);
        Hop2Sink = 0.0;
        EnergyPrcntg = new Double(0.0);
        ClustringFactor = new Double(0.0);
    }

    public void setDAR(Double dar){
        DAR = dar;
    }
    public void setH2S(double h2s){
        Hop2Sink = h2s;
    }

    public void setEP(Double ep){
        EnergyPrcntg = ep;
    }

    public void setCF(Double cf){
        ClustringFactor = cf;
    }

    public Double getDAR() { return DAR ;   }
    public double getH2S(){ return Hop2Sink;   }
    public Double getEP()  { return EnergyPrcntg ;   }
    public Double getCF()  { return ClustringFactor ;   }
    
}
