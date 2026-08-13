package com.ntu.timetabling.repository;

import com.ntu.timetabling.model.TimetableSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TimetableSessionRepository extends JpaRepository<TimetableSession, Long> {
    List<TimetableSession> findByLecturerId(Long lecturerId);

    // grabbing just one session per module is enough since every module in the data sits in exactly one block
    Optional<TimetableSession> findFirstByModuleId(Long moduleId);

    // for the lecturer dashboard's "modules I teach" list
    List<TimetableSession> findByLecturerIdOrderByBlockAsc(Long lecturerId);

    // treating every filter as optional (NULL param = "don't filter on this") so one query covers every combination
    @Query("SELECT DISTINCT ts FROM TimetableSession ts LEFT JOIN ts.courses c " +
            "WHERE (:block IS NULL OR ts.block = :block) " +
            "AND (:courseId IS NULL OR c.id = :courseId) " +
            "AND (:lecturerId IS NULL OR ts.lecturer.id = :lecturerId) " +
            "AND (:roomId IS NULL OR ts.room.id = :roomId) " +
            "ORDER BY ts.startTime")
    List<TimetableSession> findFiltered(@Param("block") Integer block,
                                         @Param("courseId") Long courseId,
                                         @Param("lecturerId") Long lecturerId,
                                         @Param("roomId") Long roomId);
}
