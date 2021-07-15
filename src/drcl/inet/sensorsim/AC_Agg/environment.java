package drcl.inet.sensorsim.AC_Agg;

import drcl.util.random.GaussianDistribution;

import java.util.Random;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: mali
 * Date: Apr 17, 2010
 * Time: 10:50:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class environment {
    public static double ArNum;
    public static final long SEED_RNG = 10 ;
    public static final double AvrgTmpOfAllArea = 10;
    public static final double MEAN		= 0.01 ; 		// gaussian mean
    public static final double VAR		= 0.001 ; 		// gaussian standard deviation

    double Xlength=0.0;
    double Ylength=0.0;
    double Zlength=0.0;
    Vector AreaVec;


    Random rnd;
    GaussianDistribution gen;
    double gaussianVar=VAR;
    double Time;

    public environment(double X,double Y, double Z,double areanum,double variance,double time){
        Xlength = X;
        Ylength = Y;
        Zlength = Z;
        ArNum = areanum;
        rnd=new Random();
        gen = null;
        AreaVec = null;
        Time = time;
        CreateClimates(variance,time);
    }
    public environment(double X,double Y, double Z,double areanum,double time ){
          Xlength = X;
          Ylength = Y;
          Zlength = Z;
          ArNum = areanum;
          rnd=new Random();
          gen = null;
          AreaVec = new Vector();
          Time = time;
          CreateClimates(time);
      }

    private void CreateClimates(double variance,double t){
       gaussianVar = variance;
       CreateClimates(t);
    }
    private void CreateClimates(double t){
       long  s = SEED_RNG ;
       // Uncomment the following line to get different results in every run
       // s = (long)(Math.abs(java.lang.System.currentTimeMillis()*Math.random()*100)) ;
         gen = new GaussianDistribution(MEAN, gaussianVar, s);
         double x=0,y=0,z=0,d=(int)(Xlength/(ArNum+3));

         for (int i=0;i<ArNum;i++){
             y = i*(Ylength/ArNum);
             Climates clmt=new Climates(gen.nextDouble(),rnd.nextDouble()* 5 + AvrgTmpOfAllArea*(i+1),t);
             clmt.setPos(x,y,z,d,Xlength,Ylength);
             AreaVec.add(clmt);
         }
        return ;
    }

    public double getWarmth(double[] pos,double t){
        double Tmpr = AvrgTmpOfAllArea;
        double num=1;

        for(int i=0;i<ArNum ; i++){
            Climates currClmt = (Climates)AreaVec.elementAt(i);
            currClmt.updatePos(t);
            if(     currClmt.getPos()[0] <= pos[0]  &&  pos[0] <= currClmt.getPos()[0]+ currClmt.getDiameter()
                &&  currClmt.getPos()[1] <= pos[1]  &&  pos[1] <= currClmt.getPos()[1]+ currClmt.getDiameter())
            {
                Tmpr+= ((Climates)AreaVec.elementAt(i)).getTmprature();
                num ++;
            }
        }
        return Tmpr/num;
    }

}
