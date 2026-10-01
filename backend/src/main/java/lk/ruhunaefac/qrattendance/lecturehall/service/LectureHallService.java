package lk.ruhunaefac.qrattendance.lecturehall.service;

import java.util.List;
import java.util.UUID;
import lk.ruhunaefac.qrattendance.lecturehall.entity.LectureHall;
import lk.ruhunaefac.qrattendance.lecturehall.repository.LectureHallRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LectureHallService {
    private final LectureHallRepository repository;
    public LectureHallService(LectureHallRepository repository) { this.repository = repository; }
    @Transactional public LectureHall create(LectureHall lectureHall) { return repository.save(lectureHall); }
    @Transactional public LectureHall update(LectureHall lectureHall) { return repository.save(lectureHall); }
    @Transactional public void delete(UUID id) { repository.deleteById(id); }
    @Transactional(readOnly = true) public LectureHall get(UUID id) { return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Lecture hall not found: " + id)); }
    @Transactional(readOnly = true) public List<LectureHall> getAll() { return repository.findAll(); }
}
