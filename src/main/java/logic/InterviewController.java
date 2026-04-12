package logic;

import storage.Storage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controls the logic for managing interviews.
 * Ensures interviews are correctly linked to existing applications.
 */
public class InterviewController {
    private final Storage storage;

    /**
     * Constructs an InterviewController with a specified storage dependency.
     *
     * @param storage The storage implementation to use for data persistence.
     */
    public InterviewController(Storage storage) {
        this.storage = storage;
    }

    /**
     * Adds a new interview to an application.
     * Verifies that the parent application exists first to maintain referential integrity.
     *
     * @param applicationId The unique ID of the application this interview is for.
     * @param round         The interview round number (e.g., 1, 2).
     * @param date          The scheduled date and time for the interview.
     * @return The newly created Interview object.
     * @throws IllegalArgumentException if the specified application ID does not exist in storage.
     */
    public Interview addInterview(String applicationId, int round, LocalDateTime date) {
        // Referential Integrity Check
        boolean appExists = storage.loadAllApplications().stream()
                .anyMatch(a -> a.getId().equals(applicationId));

        if (!appExists) {
            throw new IllegalArgumentException("Cannot add interview: Application ID " + applicationId + " not found.");
        }

        Interview interview = new Interview(applicationId, round, date);
        storage.saveInterview(interview);
        return interview;
    }

    /**
     * Retrieves all interviews associated with a specific application, sorted by round number.
     *
     * @param applicationId The unique ID of the application.
     * @return A list of interviews for the application, sorted in ascending order by round.
     */
    public List<Interview> getInterviewsByApplication(String applicationId) {
        return storage.loadAllInterviews().stream()
                .filter(i -> i.getApplicationId().equals(applicationId))
                .sorted((a, b) -> Integer.compare(a.getRound(), b.getRound()))
                .collect(Collectors.toList());
    }

    /**
     * Updates the custom notes for a specific interview.
     *
     * @param interviewId The unique ID of the interview to update.
     * @param notes       The new notes text to save.
     * @return The updated Interview object.
     * @throws IllegalArgumentException if the specified interview ID does not exist.
     */
    public Interview updateNotes(String interviewId, String notes) {
        Interview interview = storage.loadAllInterviews().stream()
                .filter(i -> i.getId().equals(interviewId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Interview not found: " + interviewId));

        interview.setNotes(notes);
        storage.updateInterview(interview);
        return interview;
    }
}