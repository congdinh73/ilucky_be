package burundi.ilucky.controller;

import burundi.ilucky.payload.Response;
import burundi.ilucky.model.Gift;
import burundi.ilucky.model.LuckyHistory;
import burundi.ilucky.model.User;
import burundi.ilucky.model.dto.LuckyHistoryDTO;
import burundi.ilucky.payload.LuckyResponse;
import burundi.ilucky.repository.LuckyHistoryRepository;
import burundi.ilucky.service.GiftService;
import burundi.ilucky.service.LuckyService;
import burundi.ilucky.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/lucky")
@Log4j2
public class LuckyController {
    @Autowired
    private UserService userService;

    @Autowired
    private LuckyService luckyService;

    @Autowired
    private LuckyHistoryRepository luckyHistoryRepository;

    @Autowired
    private UserService userRepository;

    @PostMapping("/play")
    public ResponseEntity<?> play(@AuthenticationPrincipal UserDetails userDetails) {
        try {

            if(userDetails != null) {
                User user = userService.findByUserName(userDetails.getUsername());

                if(user.getTotalPlay() <= 0) {
                    return ResponseEntity.ok(new Response("FAILED", "Bạn đã hết lượt chơi. Mua thêm để chơi tiếp"));
                } else {
                    Gift gift = luckyService.lucky(user);
                    return ResponseEntity.ok(new LuckyResponse("OK", gift, user.getTotalPlay()));
                }
            } else {
                Gift gift = luckyService.lucky();
                return ResponseEntity.ok(new LuckyResponse("OK", gift));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/history")
    public ResponseEntity<?> history(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            if(userDetails != null) {
                User user = userService.findByUserName(userDetails.getUsername());
                List<LuckyHistory> luckyHistories = luckyService.getHistoriesByUserId(user.getId());
                List<LuckyHistoryDTO> luckyGiftHistoriesDTO = luckyService.convertLuckyHistoriesToDTO(luckyHistories);
                return ResponseEntity.ok(luckyGiftHistoriesDTO);
            } else {
                List<LuckyHistory> luckyHistories = luckyService.getHistoriesByUserId(null);
                List<LuckyHistoryDTO> luckyGiftHistoriesDTO = luckyService.convertLuckyHistoriesToDTO(luckyHistories);
                return ResponseEntity.ok(luckyGiftHistoriesDTO);
            }

        } catch (Exception e) {
            log.warn(e);
            return ResponseEntity.internalServerError().build();
        }
    }


    @PostMapping("/get_all_gift")
    public ResponseEntity<?> getAllGift() {
        try {
            return ResponseEntity.ok(GiftService.gifts);
        } catch (Exception e) {
            log.warn(e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/buy_turns")
    public ResponseEntity<?> buyPlayTurns(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam int turns,
            @RequestParam int costPerTurn,
            @RequestParam String currencyType) {
        try {
            if (userDetails == null) {
                return ResponseEntity.badRequest().body(new Response("FAILED", "User is not authenticated"));
            }

            User user = userService.findByUserName(userDetails.getUsername());
            if (user == null) {
                return ResponseEntity.badRequest().body(new Response("FAILED", "User not found"));
            }

            Optional<User> updatedUser = luckyService.buyPlayTurns(user, turns, costPerTurn, currencyType);
            return updatedUser.map(value -> ResponseEntity.ok(new Response("OK", "Purchase successful! Remaining Play Turns: " + value.getTotalPlay())))
                    .orElseGet(() -> ResponseEntity.badRequest().body(new Response("FAILED", "Insufficient balance or invalid request")));

        } catch (Exception e) {
            log.error("Error in /buy_turns", e);
            return ResponseEntity.internalServerError().body(new Response("FAILED", "Internal Server Error"));
        }
    }

//    Test daily 5 time play
//    @GetMapping("/triggerDailyPlays")
//    public ResponseEntity<String> triggerDailyPlays() {
//        luckyService.giveDailyFreePlays();
//        return new ResponseEntity<>("Daily Free Plays Triggered", HttpStatus.OK);
//    }

    @GetMapping("/top-users")
    public List<Object[]> getTopStartUsers() {
        return luckyService.getTopUsersByStars();
    }
}
