package drcl.inet.sensorsim.LA_Agg;
/**
* Created by IntelliJ IDEA.
* User: admin
* Date: Oct 24, 2009
* Time: 4:14:35 PM
* To change this template use File | Settings | File Templates.
*/

import drcl.inet.sensorsim.AC_Agg.Neighbour;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Random;
import java.util.Calendar;

/**
an LA should be able for deciding reward or penalty
in this way, it will be an LEARNING machin otherwise
it has no mean for learning
*/
public class LA extends LearningAutomata {

    //private static float Phrmn_CO;
       private static  double Enrgy_CO ;//=(double)0.3
       private static  double Hop_CO ;//=(double) 0.2
       private static  double DOA_CO ;//=(double) 0.4

//if DAR is higher than this rate, this route will recieve reward
//else it will recievve penalty
public static final double ACCEPT_RATE_DATA_AGG = 0.85 ;

  public LA(int actionNum,  Vector Actions, double rwPrm, double pnltPrm,double Enrgy,double Hop,double DOA){
      super(actionNum,Actions,rwPrm,pnltPrm);
        Enrgy_CO = Enrgy;
        Hop_CO=Hop;
        DOA_CO = DOA;
}
//maximum probobility
public int SelectAction(){
    RulletwillUpdate();
    return SelectActionRltwl();
}
//Ruletwill method
private int SelectActionRltwl(){

    if(m_iActionNum == 0){
        Calendar cla= Calendar.getInstance();
        long t = cla.getTimeInMillis();
        System.out.println("No Actions in LA in "+t/1000+" second");
    }

    int RsltIndx=-1;
    Random rnd=new Random();
    double rndNum = rnd.nextDouble() ;
    for(int i=0;i<m_iActionNum;i++){
        if(
             ((Double) m_dRulletwillVec.elementAt(i)).doubleValue()  < rndNum
                                            &&
             ((Double) m_dRulletwillVec.elementAt(i+1)).doubleValue() > rndNum )
            RsltIndx = i;
        if(RsltIndx != -1 )
            break;
    }
    if(RsltIndx == -1)
        System.out.println("ERR");
    return RsltIndx;
}

//In this function we decide reward or penaltize decide in last step by LA
 public void rewardOrPenalize(int index,Object obj){

    Double DAR;
     if(obj instanceof Double){
        DAR = (Double) obj;
        if(DAR.doubleValue() >ACCEPT_RATE_DATA_AGG )
                 reward(index,DAR.doubleValue());
        else
                 penalize(index,DAR.doubleValue() );
         return;
     }
     else if (obj instanceof LAinfo)
         DAR = ((LAinfo)obj).getDAR();
     else
        return;

    if(DAR.doubleValue() > 0 ){
             for(int i=0;  i < m_iActionNum ; i++){
                  Neighbour n = (Neighbour)m_iActions.elementAt(i);
                  if(      n.getDAR()!= 0 && n.getH2S() < ((LAinfo)obj).getH2S() )
                  {
                      penalize(index, (LAinfo)obj);
                      return;
                  }
              /*   else if( n.getDAR()>DAR.doubleValue()  && n.getH2S()== ((LAinfo)obj).getH2S() && n.getEP()>((LAinfo)obj).getEP().doubleValue())
                  {
                      penalize(index, (LAinfo)obj);
                      return;
                  }*/
              }
              reward(index,(LAinfo)obj);
          }
         else
               penalize(index, (LAinfo)obj);

    }
    
 public void forcePenalty(int index,LAinfo obj){
     penalize(index,obj);
 }
private  void  reward(int index, double DAR){
   for(int i=0 ; i< m_iActionNum ; i++){
       if (i==index){
            double temp =((Double)(m_fProbOfAction.elementAt(i))).doubleValue();
            temp = temp + DAR * Alpha *(1-temp);

            m_fProbOfAction.setElementAt(new Double(temp),i);
           ((Neighbour)(m_iActions.elementAt(i))).SetProSlctn(temp);
       }
       else  {
           double temp =((Double)(m_fProbOfAction.elementAt(i))).doubleValue();
            temp = temp - DAR * Alpha *temp;
            m_fProbOfAction.setElementAt(new Double(temp),i);
            ((Neighbour)(m_iActions.elementAt(i))).SetProSlctn(temp);
       }
   }
}
    private  void  penalize(int index, double DAR){
          for(int i=0 ; i< m_iActionNum ; i++){
           if (i==index) {
               double temp =((Double)(m_fProbOfAction.elementAt(i))).doubleValue();
               temp = (1- Betta*(1-DAR))* temp;
               m_fProbOfAction.setElementAt(new Double(temp),i);
               ((Neighbour)(m_iActions.elementAt(i))).SetProSlctn(temp);
           }
           else{
               double temp =((Double)(m_fProbOfAction.elementAt(i))).doubleValue();
               temp = ( Betta*(1-DAR))/(m_iActionNum-1) +  (1- Betta*(1-DAR))* temp ;
               m_fProbOfAction.setElementAt(new Double(temp),i);
               ((Neighbour)(m_iActions.elementAt(i))).SetProSlctn(temp);
          }
        }
    }

    private  void  reward(int index,LAinfo obj ){

    double DAR = (obj.getDAR()).doubleValue();
    double EP =(obj.getEP()).doubleValue();
    double H2S = obj.getH2S();
  double myElement =(Enrgy_CO * EP + Hop_CO * 1/(H2S+1)  + DOA_CO * DAR )/(Enrgy_CO + Hop_CO + DOA_CO);
 //  double myElement =0.5;

   for(int i=0 ; i< m_iActionNum ; i++){
       if (i==index){
        double temp =((Double)(m_fProbOfAction.elementAt(i))).doubleValue();
           double x=1-temp;
           x = myElement*x;
           temp = temp + x;
        m_fProbOfAction.setElementAt(new Double(temp),i);
       ((Neighbour)(m_iActions.elementAt(i))).SetProSlctn(temp);
       }
       else
       {
       double temp =((Double)(m_fProbOfAction.elementAt(i))).doubleValue();
        double x = myElement * temp ;
           temp = temp-x;
           //temp = temp - myElement * Alpha *temp;
        m_fProbOfAction.setElementAt(new Double(temp),i);
       ((Neighbour)(m_iActions.elementAt(i))).SetProSlctn(temp);
       }
   }
}
    private  void  penalize(int index,LAinfo obj ){
        double DAR = (obj.getDAR()).doubleValue();
        double EP =(obj.getEP()).doubleValue();
        double H2S = obj.getH2S();
        double myElement =(Enrgy_CO * EP + Hop_CO * 1/(H2S+1)  + DOA_CO * DAR )/(Enrgy_CO + Hop_CO + DOA_CO);
    //  double myElement =0.5;

        for(int i=0 ; i< m_iActionNum ; i++){
            if (i==index) {
                double temp =((Double)(m_fProbOfAction.elementAt(i))).doubleValue();
                temp= myElement*temp;
              //  temp = (1- Betta*(1-myElement))* temp;
                m_fProbOfAction.setElementAt(new Double(temp),i);
                ((Neighbour)(m_iActions.elementAt(i))).SetProSlctn(temp);
            }
            else{
                double temp =((Double)(m_fProbOfAction.elementAt(i))).doubleValue();
                double x=0,y=0;
                x= myElement*temp;

                y= 1-myElement;
                y =y/(m_iActionNum-1);
                temp = y + x;
               // temp = ( Betta*(1-myElement))/(m_iActionNum-1) +  (1- Betta*(1-myElement))* temp ;
               //  temp = (1-myElement)/(m_iActionNum-1) +  myElement*temp ;
                m_fProbOfAction.setElementAt(new Double(temp),i);
                ((Neighbour)(m_iActions.elementAt(i))).SetProSlctn(temp);
            }
        }
    }


}

