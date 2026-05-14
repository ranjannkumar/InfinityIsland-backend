package com.infinityisland.service;

import com.infinityisland.dao.Catalog;
import com.infinityisland.dao.Catalog.Fact;
import com.infinityisland.model.Belt;
import com.infinityisland.model.Operation;
import com.infinityisland.repositories.CatalogRepository;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Order(0)
public class DivisionCatalogSeeder {

    private static final Logger log = LoggerFactory.getLogger(DivisionCatalogSeeder.class);

    @Autowired
    private CatalogRepository catalogRepo;

    private static final int[][][] DIV_FACTS = {
            // Level 1: white, yellow, green, blue, red, brown
            { {1,1}, {2,1}, {2,2}, {3,1}, {3,3}, {4,1} },
            // Level 2
            { {4,2}, {4,4}, {5,1}, {5,5}, {6,1}, {6,2} },
            // Level 3
            { {6,3}, {6,6}, {7,1}, {7,7}, {8,1}, {8,2} },
            // Level 4 (Brown intentionally duplicates Blue per curriculum sheet)
            { {8,4}, {8,8}, {9,1}, {9,3}, {9,9}, {9,3} },
    };

    @PostConstruct
    public void seedIfMissing() {
        String op = Operation.DIV.value();
        List<String> belts = Belt.COLORED_ORDER;
        int inserted = 0;

        for (int level = 1; level <= DIV_FACTS.length; level++) {
            for (int b = 0; b < belts.size(); b++) {
                String belt = belts.get(b);
                if (catalogRepo.findByOperationAndLevelAndBelt(op, String.valueOf(level), belt).isPresent()) {
                    continue;
                }
                int[] pair = DIV_FACTS[level - 1][b];
                validateDivisionFact(pair[0], pair[1]);

                Fact fact = new Fact();
                fact.setA(pair[0]);
                fact.setB(pair[1]);
                fact.setIdentical(pair[0] == pair[1]);

                Catalog cat = new Catalog();
                cat.setOperation(op);
                cat.setLevel(String.valueOf(level));
                cat.setBelt(belt);
                List<Fact> facts = new ArrayList<>();
                facts.add(fact);
                cat.setFacts(facts);

                catalogRepo.save(cat);
                inserted++;
            }
        }

        if (inserted > 0) {
            log.info("[INIT] Seeded {} division catalog documents", inserted);
        }
    }

    static void validateDivisionFact(int a, int b) {
        if (b == 0) {
            throw new IllegalStateException("Division fact rejected: divisor cannot be 0 (a=" + a + ")");
        }
    }
}
