package com.example.gymcrm.dao.impl;

import com.example.gymcrm.dao.TraineeDao;
import com.example.gymcrm.config.StorageBeanNames;
import com.example.gymcrm.domain.Trainee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TraineeDaoImpl extends AbstractMapDao<Trainee> implements TraineeDao {
    @Autowired
    public void setStorage(@Qualifier(StorageBeanNames.TRAINEE_STORAGE) Map<Long, Trainee> storage) {
        initializeStorage(storage);
    }

    @Override
    public Trainee create(Trainee trainee) {
        return createEntity(trainee);
    }

    @Override
    public Trainee update(Trainee trainee) {
        return updateEntity(trainee);
    }

    @Override
    public void deleteById(Long id) {
        deleteEntity(id);
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        return findEntity(id);
    }

    @Override
    public List<Trainee> findAll() {
        return findAllEntities();
    }

    @Override
    protected String entityType() {
        return "Trainee";
    }

    @Override
    protected Long getId(Trainee entity) {
        return entity.getId();
    }

    @Override
    protected void setId(Trainee entity, Long id) {
        entity.setId(id);
    }
}
