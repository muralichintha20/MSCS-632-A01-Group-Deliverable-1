package todo;

import java.util.List;
import java.util.Scanner;

/**
 * Entry point for the Collaborative To-Do List (Java).
 * Demonstrates:
 *   - OOP (User, Task, TaskManager classes)
 *   - Java concurrency (threads, ReadWriteLock, ExecutorService)
 *   - Java Streams for filtering / statistics
 *   - Enum types for Status and Category
 */
public class Main {

    private static final TaskManager taskManager = new TaskManager();
    private static User currentUser = null;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║   Collaborative To-Do List (Java)    ║");
        System.out.println("╚══════════════════════════════════════╝\n");

        // Run concurrency demo first
        ConcurrencyDemo.runDemo(taskManager);

        // Interactive CLI
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your username to continue (alice/bob/carol): ");
        String uname = scanner.nextLine().trim().toLowerCase();

        switch (uname) {
            case "alice" -> currentUser = new User("alice", "admin");
            case "bob"   -> currentUser = new User("bob",   "member");
            case "carol" -> currentUser = new User("carol", "member");
            default      -> currentUser = new User(uname,   "member");
        }

        System.out.println("Logged in as: " + currentUser);
        runMenu(scanner);
        scanner.close();
    }

    private static void runMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> listAllTasks();
                case "2" -> listMyTasks();
                case "3" -> addTask(scanner);
                case "4" -> updateTaskStatus(scanner);
                case "5" -> deleteTask(scanner);
                case "6" -> filterByCategory(scanner);
                case "7" -> filterByStatus(scanner);
                case "8" -> taskManager.printSummary();
                case "9" -> running = false;
                default  -> System.out.println("Invalid option, try again.");
            }
        }
        System.out.println("Goodbye, " + currentUser.getUsername() + "!");
    }

    private static void printMenu() {
        System.out.println("\n--- MENU (" + currentUser + ") ---");
        System.out.println(" 1. List all tasks");
        System.out.println(" 2. List my tasks");
        System.out.println(" 3. Add a task");
        System.out.println(" 4. Update task status");
        System.out.println(" 5. Delete a task");
        System.out.println(" 6. Filter by category");
        System.out.println(" 7. Filter by status");
        System.out.println(" 8. Summary / stats");
        System.out.println(" 9. Exit");
    }

    private static void listAllTasks() {
        List<Task> all = taskManager.getAllTasks();
        if (all.isEmpty()) { System.out.println("No tasks found."); return; }
        System.out.println("\n--- All Tasks ---");
        all.stream()
           .sorted((a, b) -> Integer.compare(a.getId(), b.getId()))
           .forEach(System.out::println);
    }

    private static void listMyTasks() {
        List<Task> mine = taskManager.getTasksByUser(currentUser.getUsername());
        if (mine.isEmpty()) { System.out.println("You have no tasks."); return; }
        System.out.println("\n--- My Tasks (" + currentUser.getUsername() + ") ---");
        mine.forEach(System.out::println);
    }

    private static void addTask(Scanner scanner) {
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();

        System.out.print("Description: ");
        String desc = scanner.nextLine().trim();

        System.out.println("Category (WORK/PERSONAL/SHOPPING/HEALTH/OTHER): ");
        Task.Category cat;
        try {
            cat = Task.Category.valueOf(scanner.nextLine().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            cat = Task.Category.OTHER;
        }

        System.out.print("Assign to user (leave blank for yourself): ");
        String assignTo = scanner.nextLine().trim();
        if (assignTo.isEmpty()) assignTo = currentUser.getUsername();

        Task created = taskManager.addTask(title, desc, cat, assignTo);
        System.out.println("Task created: " + created);
    }

    private static void updateTaskStatus(Scanner scanner) {
        System.out.print("Task ID: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            System.out.println("New status (PENDING/IN_PROGRESS/COMPLETED): ");
            Task.Status status = Task.Status.valueOf(scanner.nextLine().trim().toUpperCase());
            boolean ok = taskManager.updateStatus(id, status, currentUser);
            System.out.println(ok ? "Status updated." : "Task not found.");
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid input.");
        }
    }

    private static void deleteTask(Scanner scanner) {
        System.out.print("Task ID to delete: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            boolean ok = taskManager.removeTask(id, currentUser);
            System.out.println(ok ? "Task deleted." : "Task not found or permission denied.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
        }
    }

    private static void filterByCategory(Scanner scanner) {
        System.out.println("Category (WORK/PERSONAL/SHOPPING/HEALTH/OTHER): ");
        try {
            Task.Category cat = Task.Category.valueOf(scanner.nextLine().trim().toUpperCase());
            List<Task> result = taskManager.getTasksByCategory(cat);
            if (result.isEmpty()) { System.out.println("No tasks in that category."); return; }
            result.forEach(System.out::println);
        } catch (IllegalArgumentException e) {
            System.out.println("Unknown category.");
        }
    }

    private static void filterByStatus(Scanner scanner) {
        System.out.println("Status (PENDING/IN_PROGRESS/COMPLETED): ");
        try {
            Task.Status status = Task.Status.valueOf(scanner.nextLine().trim().toUpperCase());
            List<Task> result = taskManager.getTasksByStatus(status);
            if (result.isEmpty()) { System.out.println("No tasks with that status."); return; }
            result.forEach(System.out::println);
        } catch (IllegalArgumentException e) {
            System.out.println("Unknown status.");
        }
    }
}
