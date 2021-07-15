package drcl.inet.sensorsim.dynamicpointcoverage.movingevents;

import drcl.comp.ACARuntime;

/**
 * Author: Esnaashari
 * Date: Aug 29, 2008
 * Time: 4:58:39 PM
 */
public class MovingEventFactory {
    private static MovingEventFactory instance = null;
    ACARuntime runTime;

    private MovingEventFactory(ACARuntime runTime) {
        this.runTime = runTime;
        constantMovingEvent = ConstantMovingEvent.getInstance();
        noisyConstantMovingEvent = NoisyConstantMovingEvent.getInstance(this.runTime);
        poissonMovingEvent = PoissonMovingEvent.getInstance();
        hamiltonPathMovingEvent = HamiltonPathMovingEvent.getInstance();
        hourlyUniformDistributedMovingEvent = HourlyUniformDistributedMovingEvent.getInstance();
        hourlyNormalDistributedMovingEvent = HourlyNormalDistributedMovingEvent.getInstance();
    }

    public static MovingEventFactory getInstance(ACARuntime runTime) {
        if (instance == null) {
            instance = new MovingEventFactory(runTime);
        }
        return instance;
    }

    ConstantMovingEvent constantMovingEvent;
    NoisyConstantMovingEvent noisyConstantMovingEvent;
    PoissonMovingEvent poissonMovingEvent;
    HamiltonPathMovingEvent hamiltonPathMovingEvent;
    HourlyUniformDistributedMovingEvent hourlyUniformDistributedMovingEvent;
    HourlyNormalDistributedMovingEvent hourlyNormalDistributedMovingEvent;

    public IMovingEvent getConstantMovingEvent() {
        return constantMovingEvent;
    }

    public IMovingEvent getNoisyConstantMovingEvent() {
        return noisyConstantMovingEvent;
    }

    public IMovingEvent getPoissonMovingEvent() {
        return poissonMovingEvent;
    }

    public IMovingEvent getHamiltonPathMovingEvent() {
        return hamiltonPathMovingEvent;
    }

    public IMovingEvent getHourlyUniformDistributedMovingEvent() {
        return hourlyUniformDistributedMovingEvent;
    }

    public IMovingEvent getHourlyNormalDistributedMovingEvent() {
        return hourlyNormalDistributedMovingEvent;
    }
}
