package drcl.inet.sensorsim.dynamicpointcoverage.movingevents;

import drcl.inet.sensorsim.dynamicpointcoverage.EventInfo;

import java.util.Vector;

/**
 * Author: Esnaashari
 * Date: Aug 29, 2008
 * Time: 3:52:50 PM
 */
public interface IMovingEvent {
    Vector getCurrentEventInfo(int secOfDay);

    public abstract void updateEventInfo();
}
