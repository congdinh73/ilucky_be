package burundi.ilucky.service;

import burundi.ilucky.model.Gift;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Map.entry;

@Service
public class GiftService {

    public static Map<String, Gift> gifts;

    static {
        gifts = new HashMap<>();
        gifts.put("10000VND", new Gift("10000VND", "10.000 VND", 10000, "VND"));
        gifts.put("1000VND", new Gift("1000VND", "1.000 VND", 1000, "VND"));
        gifts.put("500VND", new Gift("500VND", "500 VND", 500, "VND"));
        gifts.put("200VND", new Gift("200VND", "200 VND", 200, "VND"));

        //Mete: Mảnh ghép
        gifts.put("SAMSUNG1", new Gift("SAMSUNG1", "Mảnh Samsung 1", 1, "SAMSUNG"));
        gifts.put("SAMSUNG2", new Gift("SAMSUNG2", "Mảnh Samsung 2", 1, "SAMSUNG"));
        gifts.put("SAMSUNG3", new Gift("SAMSUNG3", "Mảnh Samsung 3", 1, "SAMSUNG"));
        gifts.put("SAMSUNG4", new Gift("SAMSUNG4", "Mảnh Samsung 4", 1, "SAMSUNG"));

        //Let: Chữ cái
        gifts.put("L", new Gift("L", "1 Chữ cái \"L\"", 1, "PIECE"));
        gifts.put("I", new Gift("I", "1 Chữ cái \"I\"", 1, "PIECE"));
        gifts.put("T", new Gift("T", "1 Chữ cái \"T\"", 1, "PIECE"));
        gifts.put("E", new Gift("E", "1 Chữ cái \"E\"", 1, "PIECE"));

        gifts.put("SHARE", new Gift("SHARE", "Chia sẻ cho bạn bè để nhận được 1 lượt chơi", 1, "SHARE"));

        gifts.put("5555STARS", new Gift("5555STARS", "5555 Sao", 4, "STARS"));
        gifts.put("555STARS", new Gift("555STARS", "555 Sao", 3, "STARS"));
        gifts.put("55STARS", new Gift("55STARS", "55 Sao", 2, "STARS"));
        gifts.put("5STARS", new Gift("5STARS", "5 Sao", 1, "STARS"));

        gifts.put("UNLUCKY", new Gift("UNLUCKY", "Chúc bạn may mắn lần sau", 1, "UNLUCKY"));
    }

    private static final Map<String, Double> GIFT_PROBABILITIES = Map.ofEntries(
            entry("10000VND", 0.01),
            entry("1000VND", 0.02),
            entry("500VND", 0.03),
            entry("200VND", 0.05),
            entry("SAMSUNG1", 0.07),
            entry("SAMSUNG2", 0.07),
            entry("SAMSUNG3", 0.05),
            entry("SAMSUNG4", 0.07),
            entry("L", 0.05),
            entry("I", 0.02),
            entry("T", 0.05),
            entry("E", 0.05),
            entry("SHARE", 0.08),
            entry("5STARS", 0.10),
            entry("55STARS", 0.08),
            entry("555STARS", 0.06),
            entry("5555STARS", 0.05),
            entry("UNLUCKY", 0.09)
    );

    public static Gift getRandomGift() {
        double randomValue = ThreadLocalRandom.current().nextDouble();
        double cumulativeProbability = 0.0;

        for (Map.Entry<String, Double> entry : GIFT_PROBABILITIES.entrySet()) {
            cumulativeProbability += entry.getValue();
            if (randomValue < cumulativeProbability) {
                return gifts.get(entry.getKey());
            }
        }


        return gifts.get("UNLUCKY");
    }
}
