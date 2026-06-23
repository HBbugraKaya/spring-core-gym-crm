package com.example.gymcrm.dao.impl;

import com.example.gymcrm.dao.TrainerDao;
import com.example.gymcrm.config.StorageBeanNames;
import com.example.gymcrm.domain.Trainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TrainerDaoImpl extends AbstractMapDao<Trainer> implements TrainerDao {
    @Autowired
    public void setStorage(@Qualifier(StorageBeanNames.TRAINER_STORAGE) Map<Long, Trainer> storage) {
        initializeStorage(storage);
    }

    @Override
    public Trainer create(Trainer trainer) {
        return createEntity(trainer);
    }

    @Override
    public Trainer update(Trainer trainer) {
        return updateEntity(trainer);
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        return findEntity(id);
    }

    @Override
    public List<Trainer> findAll() {
        return findAllEntities();
    }

    @Override
    protected String entityType() {
        return "Trainer";
    }

    @Override
    protected Long getId(Trainer entity) {
        return entity.getId();
    }

    @Override
    protected void setId(Trainer entity, Long id) {
        entity.setId(id);
    }
}
