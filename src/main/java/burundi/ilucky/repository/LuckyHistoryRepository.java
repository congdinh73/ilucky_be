package burundi.ilucky.repository;

import burundi.ilucky.model.LuckyHistory;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface LuckyHistoryRepository extends JpaRepository<LuckyHistory, Long> {
    List<LuckyHistory> findByUserIdOrderByAddTimeDesc(Long userId);

    @Query("SELECT u.id, u.username, u.totalPlay, u.totalStar FROM User u  ORDER BY u.totalStar DESC LIMIT 10")
    List<Object[]> findTopUsersByStars();
}

