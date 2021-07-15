package drcl.inet.sensorsim.LA_Agg;

import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: mali
 * Date: Nov 4, 2009
 * Time: 12:10:45 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class LearningAutomata {

       Vector m_iActions;
       int m_iActionNum;
       Vector m_fProbOfAction;
       Vector m_dRulletwillVec;

       // Reward Parameter
       double Alpha;
       //    Penalty Parameter
       double Betta;

    public  LearningAutomata(int actionNum,Vector Actions, double rwPrm, double pnltPrm){
        m_fProbOfAction = new Vector();   
        m_iActionNum = actionNum;
        m_iActions = new Vector();
        initProb();
           //###############################################
           //why I need this function otherwords Wyh I need Actions?
           //###############################################
        initActions(Actions);
        Alpha = rwPrm;
        Betta = pnltPrm;
        m_dRulletwillVec=new Vector();
       }

    //In this function we decide reward or penaltize decide in last step by LA
    abstract    void  rewardOrPenalize(int index,Object obj);
    protected  void  reward(int index){
       for(int i=0 ; i< m_iActionNum ; i++){
         if (i==index){
            double temp =((Double)(m_fProbOfAction.elementAt(i))).doubleValue();
            temp = temp + Alpha *(1 - temp) ;
            m_fProbOfAction.setElementAt(new Double(temp),i);
         }
         else{
            double temp =((Double)(m_fProbOfAction.elementAt(i))).doubleValue();
            temp = temp - Alpha *(temp) ;
            m_fProbOfAction.setElementAt(new Double(temp),i);
         }
       }
   }
    protected  void  penalize(int index){
      for(int i=0 ; i< m_iActionNum ; i++){
       if (i==index){
           double temp =((Double)(m_fProbOfAction.elementAt(i))).doubleValue();
           temp = temp + (1- Betta)*temp;
           m_fProbOfAction.setElementAt(new Double(temp),i);
       }
       else{
          double temp =((Double)(m_fProbOfAction.elementAt(i))).doubleValue();
          temp = ( Betta/(m_iActionNum-1) +  (1- Betta)* temp );
          m_fProbOfAction.setElementAt(new Double(temp),i);
      }
   }
}
    protected void RulletwillUpdate(){
        m_dRulletwillVec.clear();
        m_dRulletwillVec.add(new Double(0.0));
        for(int i=1;i<m_iActionNum;i++){
                double lastInBaze=((Double)(m_dRulletwillVec.elementAt(i-1))).doubleValue();
                double nextInBaze=((Double)(m_fProbOfAction.elementAt(i-1))).doubleValue();
                m_dRulletwillVec.add(new Double(lastInBaze + nextInBaze));
        }
        m_dRulletwillVec.add(new Double(1.0));
}

    private void EditProb(int actinIndx){
//because every action has a prob with same index in m_fProbOfAction, so
        //we should delete prob with that index too!

        double delAmount = ((Double)(m_fProbOfAction.elementAt(actinIndx))).doubleValue() ;
        double addAmount=delAmount/(double)m_iActionNum;
        m_fProbOfAction.remove(actinIndx);
        for(int i=0;i< m_iActionNum; i++){
           double tmp=((Double)(m_fProbOfAction.elementAt(i))).doubleValue();
           tmp += addAmount;
           m_fProbOfAction.setElementAt(new Double(tmp),i);
        }
   }
    private void initProb(){
        //init probablity of actions
           for(int i=0;i< m_iActionNum; i++)
               m_fProbOfAction.add(new Double(1.0/m_iActionNum));
       }
    private void initActions( Vector Actions){
           for(int i=0;i< m_iActionNum ; i++){
               m_iActions.add(i,Actions.elementAt(i));
           }
       }

    public void setActions(int acts,Vector Actions){
        m_iActions.clear();
        m_iActionNum = acts;
        initActions(Actions);
        return;
    }
    public void DelAct_EditProb(int ActIndex){
         /*
    acion with index actIndx should be deleted.so we delet index
    related action from m_iActions, decrease actions number
    and then delete its prob from m_fProbOfAction and
    destrect its probobility among the others equally!
     */

        m_iActions.remove(ActIndex);
        m_iActionNum=m_iActions.size();
        if(m_iActionNum ==0){
            System.out.println("0 actions. ERR...!");
            return;
        }
        EditProb(ActIndex);
        return;
    }
    public void AddAct_EditProb(Object Action){
        //set probobility same as, again
        m_iActions.add(Action);
        m_iActionNum=m_iActions.size();
        // delete its prob and destrect its prob among the others equally
        m_fProbOfAction.clear();
        initProb();
        return;
    }
}
