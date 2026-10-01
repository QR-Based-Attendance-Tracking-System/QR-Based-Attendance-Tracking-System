package lk.ruhunaefac.qrattendance.user.repository;

import java.util.UUID;
import lk.ruhunaefac.qrattendance.user.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, UUID> {
}
