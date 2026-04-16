package logic;

import storage.Storage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages the business logic for Interview records.
 * Enforces referential integrity with applications, supports
 * creation, retrieval (sorted by round), and note updates.
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
     * Creates and persists a new interview linked to an existing application.
     * Validates that the parent application exists before saving.
     *
     * @param applicationId The unique ID of the parent application.
     * @param round         The interview round number (e.g., 1 for first round).
     * @param date          The scheduled date and time.
     * @return The newly created Interview object.
     * @throws IllegalArgumentException if no application exists with the given ID.
     */
    public Interview addInterview(String applicationId, int round, LocalDateTime date) {
        boolean isAppExists = storage.loadAllApplications().stream()
                .anyMatch(a -> a.getId().equals(applicationId));
        if (!isAppExists) {
            throw new IllegalArgumentException(
                    "Cannot add interview: Application ID " + applicationId + " not found.");
        }

        Interview interview = new Interview(applicationId, round, date);
        storage.saveInterview(interview);
        return interview;
    }

    /**
     * Returns all interviews across all applications.
     *
     * @return List of all stored interviews.
     */
    public List<Interview> getAllInterviews() {
        return storage.loadAllInterviews();
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
     * Updates the notes of an existing interview record.
     *
     * @param interviewId The unique ID of the interview to update.
     * @param notes       The new notes text.
     * @return The updated Interview object.
     * @throws IllegalArgumentException if no interview exists with the given ID.
     */
    public Interview updateNotes(String interviewId, String notes) {
        Interview interview = storage.loadAllInterviews().stream()
                .filter(i -> i.getId().equals(interviewId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Interview not found: " + interviewId));
        interview.setNotes(notes);
        storage.updateInterview(interview);
        return interview;
    }
}
