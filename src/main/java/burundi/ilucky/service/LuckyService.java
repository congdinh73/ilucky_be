package burundi.ilucky.service;

import burundi.ilucky.model.Gift;
import burundi.ilucky.model.LuckyHistory;
import burundi.ilucky.model.User;
import burundi.ilucky.model.dto.LuckyHistoryDTO;
import burundi.ilucky.repository.LuckyHistoryRepository;
import burundi.ilucky.repository.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Log4j2
public class LuckyService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LuckyHistoryRepository luckyHistoryRepository;

    public Page<LuckyHistory> getHistoriesByUserId(Long userId, Pageable pageable) {
        return luckyHistoryRepository.findByUserIdOrderByAddTimeDesc(userId, pageable);
    }


    @Async("customTaskExecutor")
    public CompletableFuture<Gift> lucky(User user) {
        return CompletableFuture.supplyAsync(() -> {
            Gift gift = GiftService.getRandomGift();

            LuckyHistory luckyHistory = new LuckyHistory();
            if (gift.getType().equals("VND")) {
                user.setTotalVnd(user.getTotalVnd() + gift.getNoItem());
            } else if (gift.getType().equals("STARS")) {
                user.setTotalStar(user.getTotalStar() + gift.getNoItem());
            }

            luckyHistory.setGiftType(gift.getType());
            luckyHistory.setAddTime(new Date());
            luckyHistory.setGiftId(gift.getId());
            luckyHistory.setNoItem(gift.getNoItem());
            luckyHistory.setUser(user);

            luckyHistoryRepository.save(luckyHistory);

            user.setTotalPlay(user.getTotalPlay() - 1);
            userRepository.save(user);

            return gift;
        });
    }

    @Async("customTaskExecutor")
    public CompletableFuture<Gift> lucky() {
        return CompletableFuture.supplyAsync(() -> {
            Gift gift = GiftService.getRandomGift();

            LuckyHistory luckyHistory = new LuckyHistory();
            luckyHistory.setGiftType(gift.getType());
            luckyHistory.setAddTime(new Date());
            luckyHistory.setGiftId(gift.getId());
            luckyHistory.setNoItem(gift.getNoItem());
            luckyHistoryRepository.save(luckyHistory);

            return gift;
        });
    }

    public List<LuckyHistoryDTO> convertLuckyHistoriesToDTO(Page<LuckyHistory> luckyGiftHistories) {
    	List<LuckyHistoryDTO> luckyGiftHistoriesDTO = new ArrayList<>();
    	
    	for(LuckyHistory item: luckyGiftHistories) {
    		Gift gift = GiftService.gifts.get(item.getGiftId());
    		LuckyHistoryDTO luckyHistoryDTO = new LuckyHistoryDTO(item.getAddTime(), gift);
    		luckyGiftHistoriesDTO.add(luckyHistoryDTO);
    	}
    	
    	return luckyGiftHistoriesDTO;
    }


    public Optional<User> buyPlayTurns(User user, int turns, int costPerTurn, String currencyType) {
        if (user == null || turns <= 0 || costPerTurn <= 0) {
            log.warn("Invalid purchase request.");
            return Optional.empty();
        }

        int totalCost = turns * costPerTurn;

        // Checking currency
        if (currencyType.equalsIgnoreCase("VND")) {
            if (user.getTotalVnd() < totalCost) {
                log.warn("User {} does not have enough VND.", user.getId());
                return Optional.empty();
            }
            user.setTotalVnd(user.getTotalVnd() - totalCost);
        } else if (currencyType.equalsIgnoreCase("STARS")) {
            if (user.getTotalStar() < totalCost) {
                log.warn("User {} does not have enough STARS.", user.getId());
                return Optional.empty();
            }
            user.setTotalStar(user.getTotalStar() - totalCost);
        } else {
            log.warn("Invalid currency type: {}", currencyType);
            return Optional.empty();
        }

        // Update play turn
        user.setTotalPlay(user.getTotalPlay() + turns);
        userRepository.save(user);
        log.info("User {} purchased {} play turns using {}.", user.getId(), turns, currencyType);

        return Optional.of(user);
    }

    @Scheduled(cron = "0 0 0 * * *") // Run at 00:00
    public void giveDailyFreePlays() {
        log.info("Giving daily free plays to all users.");
        List<User> users = userRepository.findAll();
        for (User user : users) {
            user.setTotalPlay(user.getTotalPlay() + 5);
            try {
                userRepository.save(user);
                log.info("Gave 5 free plays to user {}.", user.getId());
            } catch (Exception e) {
                log.error("Error giving free plays to user {}: {}", user.getId(), e.getMessage(), e);
            }
        }
        log.info("Finished giving daily free plays to all users.");
    }

    public List<Object[]> getTopUsersByStars() {
        return luckyHistoryRepository.findTopUsersByStars();
    }
}
