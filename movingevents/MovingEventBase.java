package drcl.inet.sensorsim.dynamicpointcoverage.movingevents;

import drcl.inet.sensorsim.dynamicpointcoverage.EventInfo;
import drcl.net.Module;
import drcl.comp.ACATimer;

/**
 * Author: Esnaashari
 * Date: Aug 29, 2008
 * Time: 4:12:01 PM
 */
public abstract class MovingEventBase extends Module implements IMovingEvent {
    public ACATimer taskTimer;
}
