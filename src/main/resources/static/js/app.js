const STORAGE_KEY = "mtm_session";

const API = {
  register: "/api/v1/auth/register",
  login: "/api/v1/auth/login",
  logout: "/api/v1/auth/logout",
  tasks: "/api/v1/tasks",
  applications: "/api/v1/applications"
};

const state = {
  token: "",
  email: "",
  roles: [],
  lastTaskId: "",
  lastApplicationId: ""
};

const ROLE_GROUPS = {
  dashboard: ["BUYER", "ADMIN"],
  tasks: ["SELLER", "ADMIN"],
  create: ["BUYER", "ADMIN"],
  applications: ["BUYER", "ADMIN"],
  apply: ["SELLER"]
};

const NAV_LABELS = {
  dashboard: {
    BUYER: "Buyer Dashboard",
    SELLER: "Buyer Dashboard",
    ADMIN: "Admin Dashboard"
  },
  tasks: {
    BUYER: "Available Tasks",
    SELLER: "Seller Tasks",
    ADMIN: "Task Marketplace"
  },
  create: {
    BUYER: "Create Task",
    SELLER: "Create Task",
    ADMIN: "Create Task"
  },
  apply: {
    BUYER: "Apply",
    SELLER: "Apply",
    ADMIN: "Apply"
  },
  applications: {
    BUYER: "Applications",
    SELLER: "Applications",
    ADMIN: "Application Review"
  }
};

const PAGE_COPY = {
  dashboard: {
    BUYER: {
      title: "Buyer Dashboard",
      subtitle: "Track your posted tasks and monitor progress in real time."
    },
    ADMIN: {
      title: "Admin Dashboard",
      subtitle: "Monitor all marketplace tasks and key activity."
    }
  },
  tasks: {
    SELLER: {
      title: "Seller Task Board",
      subtitle: "Browse live tasks and apply with your proposal."
    },
    ADMIN: {
      title: "Marketplace Task Board",
      subtitle: "Review open tasks from all buyers and sellers."
    }
  },
  create: {
    BUYER: {
      title: "Create a New Task",
      subtitle: "Post clear requirements and budget for sellers."
    },
    ADMIN: {
      title: "Create Marketplace Task",
      subtitle: "Create tasks on behalf of operations."
    }
  },
  apply: {
    SELLER: {
      title: "Apply to a Task",
      subtitle: "Load a task and send a competitive proposal."
    },
    ADMIN: {
      title: "Submit Application",
      subtitle: "Submit or simulate an application workflow."
    }
  },
  applications: {
    BUYER: {
      title: "Application Review",
      subtitle: "Load applications for your task and accept the best fit."
    },
    ADMIN: {
      title: "Admin Application Review",
      subtitle: "Audit and process applications across tasks."
    }
  }
};

const byId = (id) => document.getElementById(id);

function loadSession() {
  const raw = localStorage.getItem(STORAGE_KEY);
  if (!raw) {
    return;
  }

  try {
    const parsed = JSON.parse(raw);
    state.token = parsed.token || "";
    state.email = parsed.email || "";
    state.roles = Array.isArray(parsed.roles) ? parsed.roles : [];
    state.lastTaskId = parsed.lastTaskId || "";
    state.lastApplicationId = parsed.lastApplicationId || "";
  } catch {
    localStorage.removeItem(STORAGE_KEY);
  }
}

function persistSession() {
  localStorage.setItem(
    STORAGE_KEY,
    JSON.stringify({
      token: state.token,
      email: state.email,
      roles: state.roles,
      lastTaskId: state.lastTaskId,
      lastApplicationId: state.lastApplicationId
    })
  );
}

function clearSession() {
  state.token = "";
  state.email = "";
  state.roles = [];
  state.lastTaskId = "";
  state.lastApplicationId = "";
  localStorage.removeItem(STORAGE_KEY);
}

function normalizeError(err) {
  if (!err) {
    return { message: "Unknown error" };
  }

  const payload = err.payload;
  if (payload && typeof payload === "object") {
    return {
      status: err.status || payload.status || null,
      message: payload.message || "Request failed",
      error: payload.error || "Request failed",
      details: payload.details || []
    };
  }

  return {
    status: err.status || null,
    message: payload || err.message || "Request failed"
  };
}

function setStatus(title, data) {
  const detailsEl = byId("action-details");
  const statusEl = byId("action-status");
  if (!statusEl) {
    return;
  }

  const pretty = typeof data === "string" ? data : JSON.stringify(data, null, 2);
  statusEl.textContent = `${title}\n\n${pretty}`;
  if (detailsEl) {
    detailsEl.hidden = false;
  }
}

function hasAnyRole(requiredRoles) {
  if (!Array.isArray(requiredRoles) || requiredRoles.length === 0) {
    return true;
  }
  return requiredRoles.some((role) => state.roles.includes(role));
}

function parseRoleList(rawRoles) {
  if (!rawRoles) {
    return [];
  }
  return rawRoles.split(",").map((role) => role.trim()).filter(Boolean);
}

function getPrimaryRole() {
  if (state.roles.includes("ADMIN")) {
    return "ADMIN";
  }
  if (state.roles.includes("BUYER")) {
    return "BUYER";
  }
  if (state.roles.includes("SELLER")) {
    return "SELLER";
  }
  return "";
}

function updateRoleBadge() {
  const roleBadge = byId("role-badge");
  if (!roleBadge) {
    return;
  }

  const role = getPrimaryRole();
  if (!state.token || !role) {
    roleBadge.classList.add("is-hidden");
    return;
  }

  roleBadge.classList.remove("is-hidden");
  roleBadge.textContent = role;
  roleBadge.classList.remove("open", "inprogress", "completed", "rejected");

  if (role === "BUYER") {
    roleBadge.classList.add("open");
  } else if (role === "SELLER") {
    roleBadge.classList.add("inprogress");
  } else {
    roleBadge.classList.add("completed");
  }
}

function applyRoleAwareCopy() {
  const role = getPrimaryRole();
  if (!role) {
    updateRoleBadge();
    return;
  }

  document.querySelectorAll("[data-nav-key]").forEach((element) => {
    const key = element.getAttribute("data-nav-key");
    const label = NAV_LABELS[key]?.[role];
    if (label) {
      element.textContent = label;
    }
  });

  const pageKey = document.body?.dataset?.pageKey;
  if (pageKey) {
    const copy = PAGE_COPY[pageKey]?.[role];
    if (copy) {
      const titleEl = byId("role-page-title");
      const subtitleEl = byId("role-page-subtitle");
      if (titleEl) {
        titleEl.textContent = copy.title;
      }
      if (subtitleEl) {
        subtitleEl.textContent = copy.subtitle;
      }
    }
  }

  updateRoleBadge();
}

function showPageAlert(type, message) {
  const alertEl = byId("page-alert");
  if (!alertEl) {
    return;
  }

  alertEl.className = `mtm-alert show ${type}`;
  alertEl.textContent = message;
}

function clearPageAlert() {
  const alertEl = byId("page-alert");
  if (!alertEl) {
    return;
  }

  alertEl.className = "mtm-alert";
  alertEl.textContent = "";
}

function updateRoleVisibility() {
  document.querySelectorAll("[data-role-visible]").forEach((element) => {
    const requiredRoles = parseRoleList(element.getAttribute("data-role-visible"));
    const visible = state.token && hasAnyRole(requiredRoles);
    element.classList.toggle("is-hidden", !visible);
  });
}

function enforcePageRole() {
  const rawPageRoles = document.body?.dataset?.pageRole;
  const requiredRoles = parseRoleList(rawPageRoles);

  if (requiredRoles.length === 0) {
    return true;
  }

  if (hasAnyRole(requiredRoles)) {
    return true;
  }

  showPageAlert("error", "You do not have access to this page. Redirecting to your dashboard.");
  setTimeout(() => {
    goByRole(state.roles);
  }, 900);
  return false;
}

function setAuthError(type, message) {
  const box = byId(`${type}-error-box`);
  const text = byId(`${type}-error-text`);
  if (!box || !text) {
    return;
  }

  if (!message) {
    box.classList.remove("show");
    text.textContent = "";
    return;
  }

  text.textContent = message;
  box.classList.add("show");
}

function attachLogout() {
  document.querySelectorAll("[data-logout]").forEach((button) => {
    button.addEventListener("click", async (e) => {
      e.preventDefault();
      try {
        await request(API.logout, { method: "POST" });
      } catch {
        // Ignore logout API errors; clear local session regardless.
      }
      clearSession();
      window.location.href = "/index.html";
    });
  });
}

async function request(url, options = {}) {
  const headers = options.headers || {};
  if (!headers["Content-Type"] && options.body) {
    headers["Content-Type"] = "application/json";
  }

  if (state.token) {
    headers.Authorization = `Bearer ${state.token}`;
  }

  const response = await fetch(url, { ...options, headers });
  if (response.status === 204) {
    return {};
  }

  const contentType = response.headers.get("content-type") || "";
  const payload = contentType.includes("application/json")
    ? await response.json()
    : await response.text();

  if (!response.ok) {
    throw { status: response.status, payload };
  }

  return payload;
}

function requireAuth() {
  if (!state.token) {
    window.location.href = "/index.html";
    return false;
  }
  return true;
}

function goByRole(roles) {
  if (roles.includes("BUYER")) {
    window.location.href = "/buyer-dashboard.html";
    return;
  }
  if (roles.includes("SELLER")) {
    window.location.href = "/available-tasks.html";
    return;
  }
  if (roles.includes("ADMIN")) {
    window.location.href = "/application-management.html";
    return;
  }
  window.location.href = "/buyer-dashboard.html";
}

function bindLogin() {
  const form = byId("login-form");
  if (!form) {
    return;
  }

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    clearPageAlert();
    setAuthError("login", "");

    const body = {
      email: byId("login-email")?.value || "",
      password: byId("login-password")?.value || ""
    };

    try {
      const data = await request(API.login, {
        method: "POST",
        body: JSON.stringify(body)
      });

      state.token = data.token || "";
      state.email = data.email || body.email;
      state.roles = Array.isArray(data.roles) ? data.roles : [];
      persistSession();
      goByRole(state.roles);
    } catch (err) {
      const normalized = normalizeError(err);
      setAuthError("login", normalized.message || "Invalid credentials.");
      showPageAlert("error", normalized.message || "Login failed.");
      setStatus(`Login Failed (${normalized.status || "ERR"})`, normalized);
    }
  });
}

function bindRegister() {
  const form = byId("register-form");
  if (!form) {
    return;
  }

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    clearPageAlert();
    setAuthError("register", "");

    const body = {
      fullName: byId("register-name")?.value || "",
      email: byId("register-email")?.value || "",
      password: byId("register-password")?.value || "",
      roles: [byId("register-role")?.value || "BUYER"]
    };

    try {
      await request(API.register, {
        method: "POST",
        body: JSON.stringify(body)
      });

      showPageAlert("success", "Account created successfully. Redirecting to login.");
      setStatus("Registration Success", "Account created. Redirecting to login...");
      setTimeout(() => {
        window.location.href = "/index.html";
      }, 700);
    } catch (err) {
      const normalized = normalizeError(err);
      setAuthError("register", normalized.message || "Registration failed.");
      showPageAlert("error", normalized.message || "Registration failed.");
      setStatus(`Register Failed (${normalized.status || "ERR"})`, normalized);
    }
  });
}

function statusClass(value) {
  const v = String(value || "").toUpperCase();
  if (v === "OPEN") return "open";
  if (v === "IN_PROGRESS") return "inprogress";
  if (v === "COMPLETED") return "completed";
  if (v === "REJECTED") return "rejected";
  return "open";
}

function renderDashboardTasks(tasks) {
  const rows = byId("dashboard-task-rows");
  if (!rows) {
    return;
  }

  rows.innerHTML = "";

  if (!tasks.length) {
    rows.innerHTML = `
      <tr>
        <td colspan="5">
          <div class="mtm-empty">No tasks found yet. Create one from the Create Task page.</div>
        </td>
      </tr>
    `;
  }

  const canApply = hasAnyRole(ROLE_GROUPS.apply);
  tasks.forEach((task) => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${task.id || "-"}</td>
      <td>${task.title || "Untitled"}</td>
      <td>$${task.budget ?? "0"}</td>
      <td><span class="mtm-chip ${statusClass(task.status)}">${task.status || "OPEN"}</span></td>
      <td>${canApply ? `<a href="/apply-task.html?taskId=${task.id || ""}">Apply</a>` : `<span class="mtm-muted">N/A</span>`}</td>
    `;
    rows.appendChild(tr);
  });

  byId("metric-total").textContent = String(tasks.length);
  byId("metric-open").textContent = String(tasks.filter((t) => t.status === "OPEN").length);
  byId("metric-progress").textContent = String(tasks.filter((t) => t.status === "IN_PROGRESS").length);
  byId("metric-completed").textContent = String(tasks.filter((t) => t.status === "COMPLETED").length);
}

async function loadDashboard() {
  clearPageAlert();
  try {
    const tasks = await request(API.tasks, { method: "GET" });
    const normalizedTasks = Array.isArray(tasks) ? tasks : [];
    renderDashboardTasks(normalizedTasks);
    showPageAlert("info", `Dashboard updated with ${normalizedTasks.length} task(s).`);
    setStatus("Dashboard Loaded", tasks);
  } catch (err) {
    const normalized = normalizeError(err);
    showPageAlert("error", normalized.message || "Unable to load dashboard tasks.");
    setStatus(`Dashboard Error (${normalized.status || "ERR"})`, normalized);
  }
}

function bindDashboard() {
  if (!byId("dashboard-task-rows")) {
    return;
  }
  if (!requireAuth()) {
    return;
  }
  if (!enforcePageRole()) {
    return;
  }

  byId("dashboard-refresh")?.addEventListener("click", loadDashboard);
  loadDashboard();
}

function renderAvailableTasks(tasks) {
  const host = byId("available-task-cards");
  if (!host) {
    return;
  }

  host.innerHTML = "";

  if (!tasks.length) {
    host.innerHTML = `<article class="mtm-empty-card">No tasks are available right now. Try refreshing in a moment.</article>`;
    return;
  }

  const canApply = hasAnyRole(ROLE_GROUPS.apply);
  tasks.forEach((task) => {
    const card = document.createElement("article");
    card.className = "mtm-card";
    card.innerHTML = `
      <h3>${task.title || "Untitled Task"}</h3>
      <p>${task.description || "No description provided."}</p>
      <div class="meta">
        <span>Budget: $${task.budget ?? "0"}</span>
        <span class="mtm-chip ${statusClass(task.status)}">${task.status || "OPEN"}</span>
      </div>
      <div class="mtm-actions">
        ${canApply ? `<a class="mtm-btn" href="/apply-task.html?taskId=${task.id || ""}">Apply</a>` : `<span class="mtm-muted">Seller-only action</span>`}
      </div>
    `;
    host.appendChild(card);
  });
}

async function loadAvailableTasks() {
  clearPageAlert();
  try {
    const tasks = await request(API.tasks, { method: "GET" });
    const normalizedTasks = Array.isArray(tasks) ? tasks : [];
    renderAvailableTasks(normalizedTasks);
    showPageAlert("info", `Loaded ${normalizedTasks.length} available task(s).`);
    setStatus("Tasks Loaded", tasks);
  } catch (err) {
    const normalized = normalizeError(err);
    showPageAlert("error", normalized.message || "Unable to load available tasks.");
    setStatus(`Task Load Failed (${normalized.status || "ERR"})`, normalized);
  }
}

function bindAvailableTasks() {
  if (!byId("available-task-cards")) {
    return;
  }
  if (!requireAuth()) {
    return;
  }
  if (!enforcePageRole()) {
    return;
  }

  byId("available-refresh")?.addEventListener("click", loadAvailableTasks);
  loadAvailableTasks();
}

function bindCreateTask() {
  const form = byId("task-form");
  if (!form) {
    return;
  }
  if (!requireAuth()) {
    return;
  }
  if (!enforcePageRole()) {
    return;
  }

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    clearPageAlert();
    const body = {
      title: byId("task-title")?.value || "",
      description: byId("task-description")?.value || "",
      budget: Number(byId("task-budget")?.value || 0)
    };

    try {
      const data = await request(API.tasks, {
        method: "POST",
        body: JSON.stringify(body)
      });
      if (data && data.id) {
        state.lastTaskId = String(data.id);
        persistSession();
      }
      form.reset();
      showPageAlert("success", "Task created successfully.");
      setStatus("Task Created", data);
    } catch (err) {
      const normalized = normalizeError(err);
      showPageAlert("error", normalized.message || "Task creation failed.");
      setStatus(`Create Task Failed (${normalized.status || "ERR"})`, normalized);
    }
  });
}

async function loadTaskDetails(taskId) {
  clearPageAlert();
  if (!taskId) {
    showPageAlert("warning", "Enter a valid Task ID first.");
    setStatus("Input Needed", "Provide a valid Task ID.");
    return;
  }

  try {
    const task = await request(`${API.tasks}/${taskId}`, { method: "GET" });
    const host = byId("apply-selected-task");
    if (host) {
      host.textContent = `${task.title} | Budget $${task.budget ?? "0"} | Status ${task.status || "OPEN"}`;
    }
    showPageAlert("info", `Task #${task.id} loaded.`);
    setStatus("Task Loaded", task);
  } catch (err) {
    const normalized = normalizeError(err);
    showPageAlert("error", normalized.message || "Unable to load task details.");
    setStatus(`Load Task Failed (${normalized.status || "ERR"})`, normalized);
  }
}

function bindApplyTask() {
  const form = byId("apply-form");
  if (!form) {
    return;
  }
  if (!requireAuth()) {
    return;
  }
  if (!enforcePageRole()) {
    return;
  }

  const query = new URLSearchParams(window.location.search);
  const taskIdFromQuery = query.get("taskId");
  if (taskIdFromQuery && byId("apply-task-id")) {
    byId("apply-task-id").value = taskIdFromQuery;
    loadTaskDetails(taskIdFromQuery);
  } else if (state.lastTaskId && byId("apply-task-id")) {
    byId("apply-task-id").value = state.lastTaskId;
  }

  byId("load-task-details")?.addEventListener("click", () => {
    loadTaskDetails(byId("apply-task-id")?.value || "");
  });

  if (byId("apply-selected-task") && !taskIdFromQuery) {
    byId("apply-selected-task").textContent = "Load a task to preview title, budget, and status.";
  }

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    clearPageAlert();
    const body = {
      taskId: Number(byId("apply-task-id")?.value || 0),
      proposedAmount: Number(byId("apply-amount")?.value || 0),
      coverLetter: byId("apply-cover")?.value || ""
    };

    try {
      const data = await request(API.applications, {
        method: "POST",
        body: JSON.stringify(body)
      });
      if (data && data.id) {
        state.lastApplicationId = String(data.id);
        persistSession();
      }
      showPageAlert("success", "Application submitted successfully.");
      setStatus("Application Submitted", data);
    } catch (err) {
      const normalized = normalizeError(err);
      showPageAlert("error", normalized.message || "Application submission failed.");
      setStatus(`Apply Failed (${normalized.status || "ERR"})`, normalized);
    }
  });
}

function renderApplications(applications) {
  const host = byId("applications-list");
  if (!host) {
    return;
  }

  host.innerHTML = "";

  if (!applications.length) {
    host.innerHTML = `<article class="mtm-empty-card">No applications found for this task yet.</article>`;
    return;
  }

  applications.forEach((application) => {
    const item = document.createElement("article");
    item.className = "mtm-card";
    item.innerHTML = `
      <h3>${application.sellerName || "Unknown Seller"}</h3>
      <p>${application.coverLetter || "No cover letter"}</p>
      <div class="meta">
        <span>App #${application.id || "-"} | $${application.proposedAmount ?? "0"}</span>
        <span class="mtm-chip ${statusClass(application.status)}">${application.status || "OPEN"}</span>
      </div>
      <div class="mtm-actions">
        <button class="mtm-btn" data-accept-application-id="${application.id || ""}">Accept</button>
      </div>
    `;
    host.appendChild(item);
  });

  host.querySelectorAll("[data-accept-application-id]").forEach((btn) => {
    btn.addEventListener("click", async () => {
      const id = Number(btn.getAttribute("data-accept-application-id") || 0);
      if (!id) {
        return;
      }
      try {
        const data = await request(`${API.applications}/${id}/accept`, { method: "POST" });
        state.lastApplicationId = String(id);
        persistSession();
        showPageAlert("success", `Application #${id} accepted successfully.`);
        setStatus("Application Accepted", data);
      } catch (err) {
        const normalized = normalizeError(err);
        showPageAlert("error", normalized.message || "Could not accept application.");
        setStatus(`Accept Failed (${normalized.status || "ERR"})`, normalized);
      }
    });
  });
}

function bindApplicationManagement() {
  if (!byId("applications-list")) {
    return;
  }
  if (!requireAuth()) {
    return;
  }
  if (!enforcePageRole()) {
    return;
  }

  const taskInput = byId("fetch-task-id");
  if (taskInput && state.lastTaskId && !taskInput.value) {
    taskInput.value = state.lastTaskId;
  }

  byId("fetch-task-apps")?.addEventListener("click", async () => {
    clearPageAlert();
    const taskId = Number(taskInput?.value || 0);
    if (!taskId) {
      showPageAlert("warning", "Enter a valid Task ID to load applications.");
      setStatus("Input Needed", "Provide a valid Task ID.");
      return;
    }

    try {
      const data = await request(`${API.applications}/task/${taskId}`, { method: "GET" });
      renderApplications(Array.isArray(data) ? data : []);
      showPageAlert("info", `Loaded ${(Array.isArray(data) ? data : []).length} application(s) for task #${taskId}.`);
      setStatus("Applications Loaded", data);
    } catch (err) {
      const normalized = normalizeError(err);
      showPageAlert("error", normalized.message || "Unable to load applications.");
      setStatus(`Load Applications Failed (${normalized.status || "ERR"})`, normalized);
    }
  });
}

function init() {
  loadSession();
  applyRoleAwareCopy();
  updateRoleVisibility();
  attachLogout();
  bindLogin();
  bindRegister();
  bindDashboard();
  bindAvailableTasks();
  bindCreateTask();
  bindApplyTask();
  bindApplicationManagement();
}

init();
