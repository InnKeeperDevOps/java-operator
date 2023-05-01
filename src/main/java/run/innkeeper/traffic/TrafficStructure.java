package run.innkeeper.traffic;

import run.innkeeper.v1.guest.crd.objects.TrafficSettings;

public abstract interface TrafficStructure {

    boolean create(TrafficSettings trafficSettings);
    boolean update(TrafficSettings trafficSettings);
    boolean delete(TrafficSettings trafficSettings);
}
