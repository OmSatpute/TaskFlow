const $ = (id) => document.getElementById(id);
const form = $("taskForm"), list = $("taskList");
const labels = { PENDING: "Pending", IN_PROGRESS: "In progress", COMPLETED: "Completed" };
const priorities = { LOW: "Low", MEDIUM: "Medium", HIGH: "High" };
let tasks = [];
let categories = [];
let currentUser = null;
let currentProfile = "PERSONAL";
const dashboardProfiles = {
  PERSONAL: { label: "PERSONAL WORKSPACE", intro: "A little clarity goes a long way. What would you like to move forward today?" },
  STUDENT: { label: "STUDENT WORKSPACE", intro: "Keep assignments, study plans, and important deadlines in view." },
  OFFICE_WORKER: { label: "OFFICE WORKSPACE", intro: "Keep projects, meetings, and follow-ups moving in one place." },
  FREELANCER: { label: "FREELANCE WORKSPACE", intro: "Balance client work, deadlines, and your next steps." },
  BUSINESS_OWNER: { label: "BUSINESS WORKSPACE", intro: "Keep operations, priorities, and follow-ups organized." },
  TEACHER: { label: "TEACHING WORKSPACE", intro: "Keep lesson planning, grading, and class tasks on track." }
};

function applyProfile(profile) {
  currentProfile = dashboardProfiles[profile] ? profile : "PERSONAL";
  $("workspaceLabel").textContent = dashboardProfiles[currentProfile].label;
  $("workspaceIntro").textContent = dashboardProfiles[currentProfile].intro;
  $("profileLabel").textContent = currentProfile.replaceAll("_", " ").toLowerCase().replace(/\b\w/g, c => c.toUpperCase());
  $("accountName").textContent = currentUser.name;
}
$("today").textContent = new Intl.DateTimeFormat(undefined, { weekday: "long", month: "long", day: "numeric" }).format(new Date());
async function csrfToken() {
  const response = await fetch("/api/auth/csrf",{cache:"no-store",credentials:"same-origin"});
  if (!response.ok) throw new Error("Could not start a secure session. Refresh and try again.");
  return (await response.json()).token;
}
async function api(path, options = {}) {
  const method=(options.method || "GET").toUpperCase(), headers={"Content-Type":"application/json",...(options.headers || {})};
  if(!["GET","HEAD","OPTIONS"].includes(method)) headers["X-CSRF-TOKEN"]=await csrfToken();
  const response = await fetch(path, { ...options, method, headers, credentials:"same-origin" });
  if (!response.ok) { let message = "Something went wrong. Please try again."; try { const data = await response.json(); message = data.detail || data.message || data.error || message; } catch {} throw new Error(message); }
  return response.status === 204 ? null : response.json();
}
function dateLabel(date) {
  if (!date) return { text: "No deadline", late: false };
  const d = new Date(date), today = new Date();
  const day = d.toDateString() === today.toDateString() ? "Today" : d.toDateString() === new Date(today.getTime()+86400000).toDateString() ? "Tomorrow" : new Intl.DateTimeFormat(undefined,{month:"short",day:"numeric"}).format(d);
  return { text: `${day}, ${new Intl.DateTimeFormat(undefined,{hour:"numeric",minute:"2-digit"}).format(d)}`, late: d < today };
}
function updateClock() {
  $("today").textContent = new Intl.DateTimeFormat(undefined,{weekday:"long",month:"long",day:"numeric",hour:"numeric",minute:"2-digit",second:"2-digit"}).format(new Date());
}
function updateCountdowns() {
  document.querySelectorAll(".countdown[data-deadline]").forEach(el => {
    let seconds = Math.floor((new Date(el.dataset.deadline).getTime() - Date.now()) / 1000);
    const late = seconds < 0, urgent = late || seconds <= 3600; seconds = Math.abs(seconds);
    const days=Math.floor(seconds/86400), hours=Math.floor(seconds%86400/3600), minutes=Math.floor(seconds%3600/60), secs=seconds%60;
    const time = seconds === 0 ? "now" : days ? `${days}d ${hours}h ${minutes}m ${secs}s` : hours ? `${hours}h ${minutes}m ${secs}s` : minutes ? `${minutes}m ${secs}s` : `${secs}s`;
    el.textContent = late ? `Overdue by ${time}` : urgent ? `Due in ${time}` : `In ${time}`;
    el.classList.toggle("late",late); el.closest(".task-card")?.classList.toggle("urgent",urgent);
  });
}
function render() {
  const search = $("search").value.trim().toLowerCase();
  const visible = tasks.filter(t => (!search || `${t.title} ${t.description || ""}`.toLowerCase().includes(search)) && (!$("statusFilter").value || t.status === $("statusFilter").value) && (!$("priorityFilter").value || t.priority === $("priorityFilter").value));
  list.replaceChildren();
  if (!visible.length) {
    const empty = document.createElement("div"); empty.className = "empty";
    const mark = document.createElement("span"); mark.className = "empty-mark"; mark.textContent = tasks.length ? "⌕" : "✳";
    const title = document.createElement("strong"); title.textContent = tasks.length ? "No matching tasks" : "Nothing on your list yet";
    const body = document.createElement("span"); body.textContent = tasks.length ? "Try a different search or filter." : "Add a task to give your day a little direction.";
    empty.append(mark,title,body); list.append(empty); return;
  }
  visible.forEach(task => {
    const card = document.createElement("article"); card.className = "task-card";
    const top = document.createElement("div"); top.className = "task-top";
    const check = document.createElement("input"); check.type = "checkbox"; check.className = "check"; check.checked = task.status === "COMPLETED"; check.setAttribute("aria-label", `Mark ${task.title} complete`);
    check.addEventListener("change", () => saveStatus(task, check.checked ? "COMPLETED" : "PENDING"));
    const copy = document.createElement("div"); copy.className = "task-copy";
    const title = document.createElement("div"); title.className = `task-title${check.checked ? " is-complete" : ""}`; title.textContent = task.title; copy.append(title);
    if (task.description) { const desc = document.createElement("p"); desc.className = "task-description"; desc.textContent = task.description; copy.append(desc); }
    const actions = document.createElement("div"); actions.className = "task-actions";
    const edit = document.createElement("button"); edit.type = "button"; edit.className = "icon-button"; edit.textContent = "Edit"; edit.setAttribute("aria-label", `Edit ${task.title}`); edit.addEventListener("click", () => editTask(task));
    const del = document.createElement("button"); del.type = "button"; del.className = "icon-button"; del.textContent = "Delete"; del.setAttribute("aria-label", `Delete ${task.title}`); del.addEventListener("click", () => deleteTask(task));
    actions.append(edit,del); top.append(check,copy,actions); card.append(top);
    const tags = document.createElement("div"); tags.className = "task-tags";
    const priority = document.createElement("span"); priority.className = `tag priority-${task.priority.toLowerCase()}`; priority.textContent = `${priorities[task.priority]} priority`;
    const status = document.createElement("span"); status.className = "tag status-tag"; status.textContent = labels[task.status];
    const due = document.createElement("span"), dueInfo = dateLabel(task.deadline); due.className = `due-tag${dueInfo.late && task.status !== "COMPLETED" ? " late" : ""}`; due.textContent = dueInfo.text;
    tags.append(priority,status,due);
    if (task.category) { const category = document.createElement("span"); category.className = "tag category-tag"; category.textContent = `${task.category.domain} · ${task.category.name}`; tags.append(category); }
    if (task.deadline && task.status !== "COMPLETED") { const timer=document.createElement("span"); timer.className="countdown"; timer.dataset.deadline=task.deadline; tags.append(timer); }
    card.append(tags); list.append(card);
  });
  updateCountdowns();
}
async function refresh() {
  try {
    const params = new URLSearchParams(); if ($("statusFilter").value) params.set("status", $("statusFilter").value); if ($("priorityFilter").value) params.set("priority", $("priorityFilter").value);
    tasks = await api(`/api/tasks${params.size ? `?${params}` : ""}`); render();
    const d = await api("/api/dashboard"); $("total").textContent=d.total; $("inProgress").textContent=d.inProgress; $("completed").textContent=d.completed; $("attention").textContent=d.overdue+d.dueToday; $("attentionNote").textContent=`${d.overdue} overdue · ${d.dueToday} due today`;
    const tips = await api("/api/tips"); const tipList = $("tipList"); tipList.replaceChildren();
    tips.forEach(tip => { const item=document.createElement("li"); item.textContent=tip.message; tipList.append(item); });
  } catch (error) { list.textContent = error.message; list.className = "task-list error"; }
}
async function saveStatus(task, status) { try { await api(`/api/tasks/${task.id}`, {method:"PUT",body:JSON.stringify({...task,categoryId:task.category?.id || null,profile:currentProfile,status})}); await refresh(); } catch (e) { alert(e.message); } }
function resetForm() { form.reset(); $("taskId").value=""; $("categoryId").value=""; $("status").value="PENDING"; $("formTitle").textContent="Create a task"; $("formEyebrow").textContent="A FRESH START"; $("saveButton").firstChild.textContent="Add task "; $("cancelEdit").classList.add("hidden"); $("formError").textContent=""; }
function editTask(task) { $("taskId").value=task.id; $("title").value=task.title; $("description").value=task.description || ""; $("priority").value=task.priority; $("status").value=task.status; $("categoryId").value=task.category?.id || ""; $("deadline").value=task.deadline ? task.deadline.slice(0,16) : ""; $("formTitle").textContent="Edit task"; $("formEyebrow").textContent="MAKE IT YOURS"; $("saveButton").firstChild.textContent="Save changes "; $("cancelEdit").classList.remove("hidden"); $("formPanel").scrollIntoView({behavior:"smooth",block:"start"}); $("title").focus(); }
async function deleteTask(task) { if (!confirm(`Delete “${task.title}”?`)) return; try { await api(`/api/tasks/${task.id}`,{method:"DELETE"}); if ($("taskId").value == task.id) resetForm(); await refresh(); } catch(e) { alert(e.message); } }
form.addEventListener("submit", async event => { event.preventDefault(); $("formError").textContent=""; const id=$("taskId").value, deadline=$("deadline").value, categoryId=$("categoryId").value, profile=currentProfile; const payload={title:$("title").value.trim(),description:$("description").value.trim(),priority:$("priority").value,status:$("status").value,deadline:deadline ? `${deadline}:00` : null,categoryId:categoryId ? Number(categoryId) : null,profile}; try { await api(id ? `/api/tasks/${id}` : "/api/tasks",{method:id?"PUT":"POST",body:JSON.stringify(payload)}); resetForm(); await refresh(); } catch(e) { $("formError").textContent=e.message; } });
$("cancelEdit").addEventListener("click",resetForm); $("search").addEventListener("input",render); $("statusFilter").addEventListener("change",refresh); $("priorityFilter").addEventListener("change",refresh);

async function loadCategories() {
  categories = await api("/api/categories");
  const select = $("categoryId"); select.replaceChildren(new Option("No category", ""));
  const domains = new Map();
  categories.forEach(category => {
    if (!domains.has(category.domain)) { const group=document.createElement("optgroup"); group.label=category.domain; domains.set(category.domain,group); select.append(group); }
    domains.get(category.domain).append(new Option(category.name, category.id));
  });
}
$("categoryForm").addEventListener("submit", async event => {
  event.preventDefault(); $("categoryError").textContent="";
  try {
    await api("/api/categories",{method:"POST",body:JSON.stringify({domain:$("categoryDomain").value.trim(),name:$("categoryName").value.trim()})});
    await loadCategories();
    $("categoryForm").reset(); $("categoryId").focus();
  } catch(error) { $("categoryError").textContent=error.message; }
});
$("downloadReport").addEventListener("click", () => {
  const from=$("reportFrom").value, to=$("reportTo").value; $("reportError").textContent="";
  if(from && to && from>to) { $("reportError").textContent="Start date must be before or equal to end date."; return; }
  const params=new URLSearchParams(); if(from) params.set("from",from); if(to) params.set("to",to);
  window.location.href=`/api/reports.csv${params.size ? `?${params}` : ""}`;
});
$("logoutButton").addEventListener("click", async () => { try { await api("/api/auth/logout",{method:"POST"}); location.replace("/"); } catch(error) { alert(error.message); } });
async function startWorkspace() {
  try {
    currentUser=await api("/api/auth/me");
    if(!currentUser) { location.replace("/"); return; }
    applyProfile(currentUser.profile);
    await loadCategories(); await refresh();
  } catch(error) { location.replace("/login.html"); }
}
updateClock(); setInterval(updateClock,1000); setInterval(updateCountdowns,1000); startWorkspace();
