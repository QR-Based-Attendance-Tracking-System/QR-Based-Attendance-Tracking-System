package lk.ruhunaefac.qrattendance.lecturehall.repository;

import java.util.UUID;
import java.util.Optional;
import lk.ruhunaefac.qrattendance.lecturehall.entity.LectureHall;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LectureHallRepository extends JpaRepository<LectureHall, UUID> {
    Optional<LectureHall> findByName(String name);
}
