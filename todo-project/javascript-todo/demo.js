// demo.js
// Demonstrates async/await concurrency: multiple "users" act simultaneously
// using Promise.all, showing JS's event-loop-based concurrency model.

import {
  addTask, updateStatus, removeTask, reassignTask,
  getAllTasks, getTasksByUser, getSummary
} from './taskStore.js';

const alice = { username: 'alice', isAdmin: true };
const bob   = { username: 'bob',   isAdmin: false };
const carol = { username: 'carol', isAdmin: false };

function formatTask(t) {
  return `[#${t.id}] ${t.title.padEnd(28)} | ${t.status.padEnd(11)} | ${t.category.padEnd(8)} | assigned: ${t.assignedTo}`;
}

async function seedData() {
  console.log('Seeding initial tasks...');
  await Promise.all([
    addTask('Design database schema',  'ERD + DDL scripts',    'WORK',     'alice'),
    addTask('Write unit tests',        'JUnit/Jest coverage',  'WORK',     'bob'),
    addTask('Buy groceries',           'Milk, eggs, bread',    'SHOPPING', 'carol'),
    addTask('Code review PR #42',      'Backend API changes',  'WORK',     'alice'),
    addTask('Morning run',             '5km target',           'HEALTH',   'bob'),
  ]);
  console.log('Seeding done.\n');
}

async function aliceActions() {
  console.log('[alice] Marking task #1 COMPLETED...');
  await updateStatus(1, 'COMPLETED', alice);

  console.log('[alice] Adding new task (Sprint planning)...');
  await addTask('Sprint planning', 'Q3 kick-off meeting', 'WORK', 'alice');

  console.log('[alice] Reassigning task #2 to carol...');
  await reassignTask(2, 'carol', alice);
}

async function bobActions() {
  console.log('[bob]   Setting task #5 to IN_PROGRESS...');
  await updateStatus(5, 'IN_PROGRESS', bob);

  console.log('[bob]   Attempting to delete alice\'s task #4 (should fail)...');
  await removeTask(4, bob).catch(err => {
    console.log(`[bob]   Caught expected error: ${err.message}`);
  });
}

async function carolActions() {
  console.log('[carol] Viewing my tasks...');
  const myTasks = getTasksByUser('carol');
  myTasks.forEach(t => console.log('  carol sees: ' + formatTask(t)));

  console.log('[carol] Marking task #3 COMPLETED...');
  await updateStatus(3, 'COMPLETED', carol);
}

async function main() {
  console.log('╔══════════════════════════════════════════╗');
  console.log('║  Collaborative To-Do List (JavaScript)   ║');
  console.log('╚══════════════════════════════════════════╝\n');

  await seedData();

  console.log('===================================================');
  console.log('  CONCURRENCY DEMO: 3 users acting with Promise.all');
  console.log('===================================================\n');

  // All three user action-sets run concurrently via Promise.all
  await Promise.all([
    aliceActions(),
    bobActions(),
    carolActions(),
  ]);

  console.log('\n--- Final task list ---');
  getAllTasks()
    .sort((a, b) => a.id - b.id)
    .forEach(t => console.log(formatTask(t)));

  const summary = getSummary();
  console.log('\n===== SUMMARY =====');
  console.log('Total tasks:', summary.total);
  console.log('By Status:',   summary.byStatus);
  console.log('By Category:', summary.byCategory);
  console.log('By User:',     summary.byUser);
  console.log('===================\n');
}

main().catch(console.error);
