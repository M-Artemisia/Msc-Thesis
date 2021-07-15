package drcl.inet.sensorsim.AC_Agg;

/**
 * Created by IntelliJ IDEA.
 * User: admin
 * Date: Mar 8, 2010
 * Time: 2:11:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class Neighbour {
    public String   Level ;//level of neighbour
    public long     id;//Neighbour's ID
    public double[] Locaion;//NEighbour's Loacation

    final double BaseEnrgy=1.0;//juoles-29 dei
    private static  double InitialPheromen =0.1 ;

    private  double   pheromen =  InitialPheromen;
    public   double   SelectionProp;
    private  double DAR=1;
    private  double    H2S =1 ;
    private  double EP = BaseEnrgy;

   public  int NumNotAck=0;
    
    public Neighbour(String level ,long id, double[] locaion){
        Level   = level;
        this.id = id ;
        Locaion = locaion;
    }
    
     public void SetPhromn(double phromn){ pheromen = phromn;}
     public double getPhromn(){ return pheromen ;}
    public void SetProSlctn(double prob){ SelectionProp = prob ;}

    public void   SetEP(double ep){ EP = ep ;}
    public double getEP(){ return EP;}

    public void SetDAR(double dar){ DAR = dar;}
    public double getDAR(){ return DAR;}


    public void SetH2S(double h2s){ H2S = h2s;}
    public double  getH2S(){ return H2S;}

}
