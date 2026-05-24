package todo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class Task {

    public enum Status {
        PENDING, IN_PROGRESS, COMPLETED
    }

    public enum Category {
        WORK, PERSONAL, SHOPPING, HEALTH, OTHER
    }

    private static final AtomicInteger ID_COUNTER = new AtomicInteger(1);
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final int id;
    private String title;
    private String description;
    private Status status;
    private Category category;
    private String assignedTo;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Task(String title, String description, Category category, String assignedTo) {
        this.id          = ID_COUNTER.getAndIncrement();
        this.title       = title;
        this.description = description;
        this.category    = category;
        this.assignedTo  = assignedTo;
        this.status      = Status.PENDING;
        this.createdAt   = LocalDateTime.now();
        this.updatedAt   = LocalDateTime.now();
    }

    // Getters
    public int getId()            { return id; }
    public String getTitle()      { return title; }
    public String getDescription(){ return description; }
    public Status getStatus()     { return status; }
    public Category getCategory() { return category; }
    public String getAssignedTo() { return assignedTo; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setTitle(String title)           { this.title = title; touch(); }
    public void setDescription(String desc)      { this.description = desc; touch(); }
    public void setStatus(Status status)         { this.status = status; touch(); }
    public void setCategory(Category category)   { this.category = category; touch(); }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; touch(); }

    private void touch() { this.updatedAt = LocalDateTime.now(); }

    @Override
    public String toString() {
        return String.format(
            "[#%d] %-30s | %-11s | %-8s | Assigned: %-10s | %s",
            id, title, status, category, assignedTo,
            updatedAt.format(FORMATTER)
        );
    }
}
