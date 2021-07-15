package drcl.inet.sensorsim.AC_Agg;

/**
 * Created by IntelliJ IDEA.
 * User: mali
 * Date: Aug 8, 2010
 * Time: 2:10:11 PM
 *
 * In this class we implement a square shape climate
 *  with Diameter as its diameter and Velocity as its moving velocity.
 * Pos shows the current position of square O-point.
 * all climates are squaes. and their O-point(point (0,0) ) in themselves pivots, moves over area.
 *if a climate fae to edges of environment, it changes its move direction to the opposite direction !
 * To change this template use File | Settings | File Templates.
 */

public class Climates {

    private double[] Pos=new double[3];
    private double Velocity=0.0;      // AreaUnit/second
    private String Direction = "P";//Moving at the positive Direction of pivots
    private double Temprature = 0.0;
    private double Diameter = 0.0;
    double lastTime = 0.0;

    double Xlimitation =0;
    double Ylimitation =0;

    Climates(double v,double tmpr,double time){
    //    Pos[0]=Pos[1]=Pos[2]=0;
        Velocity = v;
        Temprature = tmpr;
        lastTime = time  ;
        lastTime = 250;
    }

    public void setPos(double x, double y, double z, double d, double Xl,double  Yl){
        Pos = new double[3];
        Pos[0]=x; Pos[1]=y; Pos[2]=z;
        Diameter = d;
        Xlimitation = Xl; Ylimitation = Yl;
    }
    public double[] getPos(){
        return Pos;
    }
    public void updatePos(double time){
        double deltaT = time - lastTime;
        lastTime = time;
        double deltaX = Velocity * deltaT ;

        if (Direction.equals("P")==true){
            if(Pos[0] + deltaX +Diameter <= Xlimitation)
                Pos[0] += deltaX;
            else{
                Direction = "N";
                double extra = (Pos[0] + deltaX +Diameter) - Xlimitation;
                Pos[0] = Xlimitation - extra;
            }
        }
        else{  // direction is negative
            if(Pos[0] -(deltaX + Diameter) >= 0.0)
                Pos[0] -= deltaX;
            else{
                Direction = "P";
                double extra = Pos[0] - (deltaX +Diameter);
                Pos[0] = - extra;
            }
        }
    }

    public void setVelocity(double v){
        Velocity = v;
    }
    public double getVelocity(){
     return Velocity;
    }

    public double getTmprature(){
        return Temprature;
    }
    public void setTmprature(double t){
            Temprature = t;
    }

    public double getDiameter(){
        return Diameter;
    }
    public void setDiameter(double d){
            Diameter = d;
    }
}
