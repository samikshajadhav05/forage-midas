package com.jpmc.midascore;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserPopulator {

    @Autowired
    private FileLoader fileLoader;

    @Autowired
    private UserRepository userRepository;

    public void populate() {
        System.out.println("--- Starting User Population ---");
        // This is the correct file for user data for Task Three
        String[] userLines = fileLoader.loadStrings("test_data/lkjhgfdsa.hjkl");

        if (userLines == null || userLines.length == 0) {
            System.err.println("!!! ERROR: User data file could not be loaded or is empty.");
            return;
        }

        System.out.println("... Found " + userLines.length + " lines in user data file.");
        int usersSaved = 0;
        for (String userLine : userLines) {
            if (userLine.trim().isEmpty()) {
                continue; // Skip any blank lines
            }

            String[] userData = userLine.split(", ");
            if (userData.length != 3) {
                System.err.println("!!! SKIPPING line (wrong column count): [" + userLine + "]");
                continue;
            }

            try {
                // Correctly parse data in the format: name, balance, id
                String name = userData[0].trim();
                float balance = Float.parseFloat(userData[1].trim());
                long id = Long.parseLong(userData[2].trim());

                UserRecord user = new UserRecord(id, name, balance);
                userRepository.save(user);
                usersSaved++;
            } catch (NumberFormatException e) {
                System.err.println("!!! SKIPPING line (bad number format): [" + userLine + "]");
            }
        }
        System.out.println("--- Finished User Population. Total users saved: " + usersSaved + " ---");
    }
}

