package todo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Thread-safe task store.
 * Uses a ReadWriteLock so multiple users can read concurrently
 * but writes are exclusive.
 */
public class TaskManager {

    private final Map<Integer, Task> tasks = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    // ------------------------------------------------------------------ add
    public Task addTask(String title, String description,
                        Task.Category category, String assignedTo) {
        lock.writeLock().lock();
        try {
            Task task = new Task(title, description, category, assignedTo);
            tasks.put(task.getId(), task);
            return task;
        } finally {
            lock.writeLock().unlock();
        }
    }

    // --------------------------------------------------------------- remove
    public boolean removeTask(int id, User requestingUser) {
        lock.writeLock().lock();
        try {
            Task task = tasks.get(id);
            if (task == null) return false;
            // Only admin or the assigned user may delete
            if (!requestingUser.isAdmin()
                    && !task.getAssignedTo().equals(requestingUser.getUsername())) {
                System.out.println("  [Permission denied] Only admins or the assigned user can delete this task.");
                return false;
            }
            tasks.remove(id);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    // --------------------------------------------------------- update status
    public boolean updateStatus(int id, Task.Status newStatus, User requestingUser) {
        lock.writeLock().lock();
        try {
            Task task = tasks.get(id);
            if (task == null) return false;
            if (!requestingUser.isAdmin()
                    && !task.getAssignedTo().equals(requestingUser.getUsername())) {
                System.out.println("  [Permission denied] Only admins or the assigned user can update this task.");
                return false;
            }
            task.setStatus(newStatus);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    // -------------------------------------------------------- reassign task
    public boolean reassignTask(int id, String newUser, User requestingUser) {
        lock.writeLock().lock();
        try {
            if (!requestingUser.isAdmin()) {
                System.out.println("  [Permission denied] Only admins can reassign tasks.");
                return false;
            }
            Task task = tasks.get(id);
            if (task == null) return false;
            task.setAssignedTo(newUser);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ------------------------------------------------------- list / filters
    public List<Task> getAllTasks() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(tasks.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Task> getTasksByUser(String username) {
        lock.readLock().lock();
        try {
            return tasks.values().stream()
                    .filter(t -> t.getAssignedTo().equalsIgnoreCase(username))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Task> getTasksByCategory(Task.Category category) {
        lock.readLock().lock();
        try {
            return tasks.values().stream()
                    .filter(t -> t.getCategory() == category)
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Task> getTasksByStatus(Task.Status status) {
        lock.readLock().lock();
        try {
            return tasks.values().stream()
                    .filter(t -> t.getStatus() == status)
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    public Task getTaskById(int id) {
        lock.readLock().lock();
        try {
            return tasks.get(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    // ------------------------------------------------------ summary / stats
    public void printSummary() {
        lock.readLock().lock();
        try {
            System.out.println("\n===== TASK SUMMARY =====");
            System.out.println("Total tasks : " + tasks.size());

            Map<Task.Status, Long> byStatus = tasks.values().stream()
                    .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));
            byStatus.forEach((s, c) -> System.out.printf("  %-12s : %d%n", s, c));

            System.out.println("\nBy Category:");
            Map<Task.Category, Long> byCat = tasks.values().stream()
                    .collect(Collectors.groupingBy(Task::getCategory, Collectors.counting()));
            byCat.forEach((cat, c) -> System.out.printf("  %-10s : %d%n", cat, c));

            System.out.println("\nBy Assigned User:");
            Map<String, Long> byUser = tasks.values().stream()
                    .collect(Collectors.groupingBy(Task::getAssignedTo, Collectors.counting()));
            byUser.forEach((u, c) -> System.out.printf("  %-10s : %d%n", u, c));
            System.out.println("========================\n");
        } finally {
            lock.readLock().unlock();
        }
    }
}
