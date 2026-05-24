// index.js - Interactive CLI for the JS To-Do app

import readlineSync from 'readline-sync';
import {
  addTask, removeTask, updateStatus, reassignTask,
  getAllTasks, getTasksByUser, getTasksByCategory,
  getTasksByStatus, getSummary
} from './taskStore.js';

// ------------------------------------------------------------------ helpers
function formatTask(t) {
  return `[#${t.id}] ${t.title.padEnd(30)} | ${t.status.padEnd(11)} | ${t.category.padEnd(8)} | assigned: ${t.assignedTo}`;
}

function printList(tasks) {
  if (!tasks.length) { console.log('  (no tasks found)'); return; }
  tasks.sort((a, b) => a.id - b.id).forEach(t => console.log(formatTask(t)));
}

// ------------------------------------------------------------------ login
console.log('╔══════════════════════════════════════════╗');
console.log('║  Collaborative To-Do List (JavaScript)   ║');
console.log('╚══════════════════════════════════════════╝\n');

const uname = readlineSync.question('Enter your username (alice/bob/carol): ').trim().toLowerCase();
const currentUser = {
  username: uname,
  isAdmin: uname === 'alice',
};
console.log(`\nLogged in as: ${currentUser.username} (${currentUser.isAdmin ? 'admin' : 'member'})\n`);

// ------------------------------------------------------------------ menu
async function menu() {
  let running = true;
  while (running) {
    console.log('\n--- MENU ---');
    console.log(' 1. List all tasks');
    console.log(' 2. List my tasks');
    console.log(' 3. Add a task');
    console.log(' 4. Update task status');
    console.log(' 5. Delete a task');
    console.log(' 6. Filter by category');
    console.log(' 7. Filter by status');
    console.log(' 8. Summary / stats');
    console.log(' 9. Exit');

    const choice = readlineSync.question('Choice: ').trim();

    switch (choice) {
      case '1':
        printList(getAllTasks());
        break;

      case '2':
        printList(getTasksByUser(currentUser.username));
        break;

      case '3': {
        const title       = readlineSync.question('Title: ').trim();
        const description = readlineSync.question('Description: ').trim();
        const category    = readlineSync.question('Category (WORK/PERSONAL/SHOPPING/HEALTH/OTHER): ').trim();
        let assignTo      = readlineSync.question('Assign to (blank = yourself): ').trim();
        if (!assignTo) assignTo = currentUser.username;
        const created = await addTask(title, description, category, assignTo);
        console.log('Created:', formatTask(created));
        break;
      }

      case '4': {
        const id        = parseInt(readlineSync.question('Task ID: ').trim(), 10);
        const newStatus = readlineSync.question('New status (PENDING/IN_PROGRESS/COMPLETED): ').trim();
        try {
          const updated = await updateStatus(id, newStatus, currentUser);
          console.log('Updated:', formatTask(updated));
        } catch (err) {
          console.log('Error:', err.message);
        }
        break;
      }

      case '5': {
        const id = parseInt(readlineSync.question('Task ID to delete: ').trim(), 10);
        try {
          await removeTask(id, currentUser);
          console.log(`Task #${id} deleted.`);
        } catch (err) {
          console.log('Error:', err.message);
        }
        break;
      }

      case '6': {
        const cat = readlineSync.question('Category: ').trim().toUpperCase();
        printList(getTasksByCategory(cat));
        break;
      }

      case '7': {
        const status = readlineSync.question('Status: ').trim().toUpperCase();
        printList(getTasksByStatus(status));
        break;
      }

      case '8': {
        const s = getSummary();
        console.log('\n===== SUMMARY =====');
        console.log('Total tasks:', s.total);
        console.log('By Status:',   JSON.stringify(s.byStatus,   null, 2));
        console.log('By Category:', JSON.stringify(s.byCategory, null, 2));
        console.log('By User:',     JSON.stringify(s.byUser,     null, 2));
        console.log('===================');
        break;
      }

      case '9':
        running = false;
        break;

      default:
        console.log('Invalid option.');
    }
  }
  console.log(`Goodbye, ${currentUser.username}!`);
}

menu().catch(console.error);
