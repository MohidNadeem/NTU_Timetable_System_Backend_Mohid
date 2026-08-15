package com.ntu.timetabling.repository;

import com.ntu.timetabling.model.Request;
import com.ntu.timetabling.model.RequestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByRequesterIdAndType(Long requesterId, RequestType type);
    List<Request> findByType(RequestType type);
    // used by the lecturer dashboard's status-count summary - every request they've ever submitted, any type
    List<Request> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);

    // reusable filtered lookup for both constraint and change-request list views
    @Query("SELECT r FROM Request r WHERE " +
            "r.type = :type " +
            "AND (:requesterId IS NULL OR r.requester.id = :requesterId) " +
            "AND (:constraintKind IS NULL OR r.constraintKind = :constraintKind) " +
            "AND (:status IS NULL OR r.status = :status) " +
            "AND (:departmentId IS NULL OR r.department.id = :departmentId) " +
            "AND (:changeCategory IS NULL OR r.changeCategory = :changeCategory) " +
            "ORDER BY r.createdAt DESC")
    List<Request> findFiltered(@Param("type") RequestType type,
                                @Param("requesterId") Long requesterId,
                                @Param("constraintKind") com.ntu.timetabling.model.ConstraintKind constraintKind,
                                @Param("status") com.ntu.timetabling.model.RequestStatus status,
                                @Param("departmentId") Long departmentId,
                                @Param("changeCategory") com.ntu.timetabling.model.ChangeCategory changeCategory);
}
