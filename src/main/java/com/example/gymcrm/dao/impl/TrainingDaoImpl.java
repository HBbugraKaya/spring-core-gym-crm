package com.example.gymcrm.dao.impl;

import com.example.gymcrm.dao.TrainingDao;
import com.example.gymcrm.config.StorageBeanNames;
import com.example.gymcrm.domain.Training;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TrainingDaoImpl extends AbstractMapDao<Training> implements TrainingDao {
    @Autowired
    public void setStorage(@Qualifier(StorageBeanNames.TRAINING_STORAGE) Map<Long, Training> storage) {
        initializeStorage(storage);
    }

    @Override
    public Training create(Training training) {
        return createEntity(training);
    }

    @Override
    public Optional<Training> findById(Long id) {
        return findEntity(id);
    }

    @Override
    public List<Training> findAll() {
        return findAllEntities();
    }

    @Override
    protected String entityType() {
        return "Training";
    }

    @Override
    protected Long getId(Training entity) {
        return entity.getId();
    }

    @Override
    protected void setId(Training entity, Long id) {
        entity.setId(id);
    }
}
