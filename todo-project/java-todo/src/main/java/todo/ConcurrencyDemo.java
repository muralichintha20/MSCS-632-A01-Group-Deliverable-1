package todo;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Simulates multiple users accessing the TaskManager concurrently.
 * Demonstrates Java's thread/concurrency model with an ExecutorService.
 */
public class ConcurrencyDemo {

    public static void runDemo(TaskManager taskManager) throws InterruptedException {
        System.out.println("\n====================================================");
        System.out.println("  CONCURRENCY DEMO: 3 users acting simultaneously");
        System.out.println("====================================================");

        User alice = new User("alice", "admin");
        User bob   = new User("bob",   "member");
        User carol = new User("carol", "member");

        // Pre-populate some tasks
        taskManager.addTask("Design database schema",    "ERD + DDL",            Task.Category.WORK,     "alice");
        taskManager.addTask("Write unit tests",          "JUnit 5 coverage",     Task.Category.WORK,     "bob");
        taskManager.addTask("Buy groceries",             "Milk, eggs, bread",    Task.Category.SHOPPING, "carol");
        taskManager.addTask("Code review PR #42",        "Backend changes",      Task.Category.WORK,     "alice");
        taskManager.addTask("Morning run",               "5km target",           Task.Category.HEALTH,   "bob");

        // Latch ensures all threads start at the same instant
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch  = new CountDownLatch(3);

        ExecutorService executor = Executors.newFixedThreadPool(3);

        // Alice (admin): marks tasks complete and adds a new one
        executor.submit(() -> {
            try {
                startLatch.await();
                System.out.println("[alice] Marking task #1 as COMPLETED...");
                taskManager.updateStatus(1, Task.Status.COMPLETED, alice);

                System.out.println("[alice] Adding new task...");
                taskManager.addTask("Sprint planning", "Q3 sprint kick-off", Task.Category.WORK, "alice");

                System.out.println("[alice] Reassigning task #2 to carol...");
                taskManager.reassignTask(2, "carol", alice);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                doneLatch.countDown();
            }
        });

        // Bob (member): updates his own task, tries to delete alice's (denied)
        executor.submit(() -> {
            try {
                startLatch.await();
                System.out.println("[bob] Setting task #5 to IN_PROGRESS...");
                taskManager.updateStatus(5, Task.Status.IN_PROGRESS, bob);

                System.out.println("[bob] Attempting to delete task #1 (alice's task)...");
                boolean deleted = taskManager.removeTask(1, bob);
                System.out.println("[bob] Delete result: " + deleted);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                doneLatch.countDown();
            }
        });

        // Carol (member): views her tasks, marks shopping done
        executor.submit(() -> {
            try {
                startLatch.await();
                System.out.println("[carol] Listing my tasks...");
                taskManager.getTasksByUser("carol")
                           .forEach(t -> System.out.println("  carol sees: " + t));

                System.out.println("[carol] Marking task #3 as COMPLETED...");
                taskManager.updateStatus(3, Task.Status.COMPLETED, carol);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                doneLatch.countDown();
            }
        });

        // Fire all threads at once
        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        System.out.println("\n[Concurrency demo complete - final state below]");
        System.out.println("====================================================\n");
    }
}
