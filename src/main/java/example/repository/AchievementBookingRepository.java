package example.repository;

import example.entity.AchievementBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AchievementBookingRepository extends JpaRepository<AchievementBooking, Long> {
}
