package lk.ruhunaefac.qrattendance.user.service;

import java.util.UUID;
import lk.ruhunaefac.qrattendance.user.entity.User;
import lk.ruhunaefac.qrattendance.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository repository;
    public UserService(UserRepository repository) { this.repository = repository; }
    @Transactional public User create(User user) { return repository.save(user); }
    @Transactional public User update(User user) { return repository.save(user); }
    @Transactional(readOnly = true) public User get(UUID id) { return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found: " + id)); }
    @Transactional public void delete(UUID id) { repository.deleteById(id); }
}
