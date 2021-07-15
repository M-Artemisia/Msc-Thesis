package drcl.inet.sensorsim.dynamicpointcoverage.movingevents;

import java.util.Vector;

/**
 * Author: Esnaashari
 * Date: Aug 29, 2008
 * Time: 4:19:55 PM
 */
public class HourlyNormalDistributedMovingEvent extends MovingEventBase {
    private static HourlyNormalDistributedMovingEvent instance;
    static {
        instance = new HourlyNormalDistributedMovingEvent();
    }

    private HourlyNormalDistributedMovingEvent() {

    }

    public static HourlyNormalDistributedMovingEvent getInstance() {
        return instance;
    }
    
    public void updateEventInfo() {
        
    }

    public Vector getCurrentEventInfo(int secOfDay) {
        return null;
    }
}
