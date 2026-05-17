package gg.hatchery.storage;

import gg.hatchery.daycare.Daycare;

import java.util.List;
import java.util.UUID;

public interface Storage {
    void init() throws Exception;
    void close();

    void saveDaycare(Daycare d);
    void deleteDaycare(UUID id);
    List<Daycare> loadAllDaycares();
}
