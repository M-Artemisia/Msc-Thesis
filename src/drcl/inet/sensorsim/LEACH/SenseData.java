package drcl.inet.sensorsim.LEACH;

/**
 * Created by IntelliJ IDEA.
 * User: mali
 * Date: Apr 26, 2010
 * Time: 1:18:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class SenseData {
     double data;
        int AggCount;

        public  SenseData(double d,int aggCnt){
            data = d;
            AggCount = aggCnt;
        }
}
