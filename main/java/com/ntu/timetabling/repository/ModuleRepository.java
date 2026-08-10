package com.ntu.timetabling.repository;

import com.ntu.timetabling.model.ModuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleRepository extends JpaRepository<ModuleEntity, Long> {
}
