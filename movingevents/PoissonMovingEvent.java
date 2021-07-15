package drcl.inet.sensorsim.dynamicpointcoverage.movingevents;

import drcl.inet.sensorsim.dynamicpointcoverage.DynamicPointCoverageConstants;
import drcl.inet.sensorsim.dynamicpointcoverage.EventInfo;

//import java.util.*;

import DESimulator.random.ExponentialGenerator;

import java.util.Random;
import java.util.Vector;

/**
 * Author: Esnaashari
 * Date: Aug 29, 2008
 * Time: 4:15:13 PM
 */
public class PoissonMovingEvent extends MovingEventBase {
    private final int OFF = 0;
    private final int ON = 1;

    private static PoissonMovingEvent instance;

    private Vector eventLocations;
    private int locationNum;
    ExponentialGenerator[] exponentialGenerators;
    TimeEvent[] timeEvents;

    private final Integer mutex = new Integer(1);

    static {
        instance = new PoissonMovingEvent();
    }

    /**
     * _start()
     * This method is called when attempting to 'run' the component
     * in TCL.
     */
    protected void _start() {
        Random rnd = new Random();
        locationNum = rnd.nextInt(DynamicPointCoverageConstants.MAX_EVENT_NUMBER);
        exponentialGenerators = new ExponentialGenerator[locationNum];
        timeEvents = new TimeEvent[locationNum];
        eventLocations = new Vector();
        for (int i = 0; i < locationNum; i++) {
            ((EventInfo) eventLocations.get(i)).setX(rnd.nextDouble() * DynamicPointCoverageConstants.AREA_X);
            ((EventInfo) eventLocations.get(i)).setY(rnd.nextDouble() * DynamicPointCoverageConstants.AREA_Y);
            ((EventInfo) eventLocations.get(i)).setState(OFF);
            exponentialGenerators[i] = new ExponentialGenerator(DynamicPointCoverageConstants.MEAN_EVENT_NUM_PER_HOUR, i);
            timeEvents[i] = new TimeEvent();
            timeEvents[i].setEventIndex(i);
            timeEvents[i].setUpdateTime((long)(exponentialGenerators[i].generate()));
        }
        sortUpdateTimes();
        taskTimer = setTimeout("updateEventInfo", timeEvents[0].getUpdateTime());
//        TimerTask updateInfoTask = new UpdateMovingEventTimerTask(this);
//        Timer updateInfoTimer = new Timer();
//        updateInfoTimer.schedule(updateInfoTask, timeEvents[0].getUpdateTime());
        new Thread(new StateChanger());
    }

    private PoissonMovingEvent() {
    }

    protected void timeout(Object data_) {
        if (data_.equals("updateEventInfo")) {
            updateEventInfo();
            taskTimer = setTimeout("updateEventInfo", timeEvents[0].getUpdateTime());
        }
    }

    private void sortUpdateTimes() {
        for (int i = 0; i < locationNum - 1; i++) {
            for (int j = i + 1; j < locationNum; j++) {
                if (timeEvents[i].getUpdateTime() > timeEvents[j].getUpdateTime()) {
                    TimeEvent tmp = timeEvents[i];
                    timeEvents[i] = timeEvents[j];
                    timeEvents[j] = tmp;
                }
            }
        }
        for (int i = 1;i < locationNum;i++) {
            long updatetedUpdateTime = timeEvents[i].getUpdateTime() - timeEvents[0].getUpdateTime();
            timeEvents[i].setUpdateTime(updatetedUpdateTime);
        }
    }

    public static PoissonMovingEvent getInstance() {
        return instance;
    }

    public void updateEventInfo() {
        synchronized (mutex) {
            ((EventInfo) eventLocations.get(timeEvents[0].getEventIndex())).setState(ON);
            ((EventInfo) eventLocations.get(timeEvents[0].getEventIndex())).setStartSecond(System.currentTimeMillis());
            timeEvents[0].setUpdateTime((long) exponentialGenerators[timeEvents[0].getEventIndex()].generate());
            sortUpdateTimes();
//            TimerTask updateInfoTask = new UpdateMovingEventTimerTask(this);
//            Timer updateInfoTimer = new Timer();
//            updateInfoTimer.schedule(updateInfoTask, timeEvents[0].getUpdateTime());
        }
    }

    public Vector getCurrentEventInfo(int secOfDay) {
        Vector eventInfo = new Vector();
        synchronized (mutex) {
            for (int i = 0; i < locationNum; i++) {
                if (((EventInfo) eventLocations.get(i)).getState() == ON) {
                    eventInfo.add(eventLocations.get(i));
                }
            }
        }
        return eventInfo;
    }

    public class TimeEvent {
        long updateTime;
        int eventIndex;

        public long getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(long updateTime) {
            this.updateTime = updateTime;
        }

        public int getEventIndex() {
            return eventIndex;
        }

        public void setEventIndex(int eventIndex) {
            this.eventIndex = eventIndex;
        }
    }

    public class StateChanger implements Runnable {

        public void run() {
            while (true) {
                long curTime = System.currentTimeMillis();
                synchronized (mutex) {
                    for (int i = 0; i < locationNum; i++) {
                        if (((EventInfo) eventLocations.get(i)).getState() == ON &&
                            ((EventInfo) eventLocations.get(i)).getStartSecond() +
                                        DynamicPointCoverageConstants.POISSON_EVENT_DURATION < curTime) {
                            ((EventInfo) eventLocations.get(i)).setState(OFF);
                        }
                    }
                }
                try {
                    Thread.sleep(5 * 60 * 1000l);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
