const responseEl = document.getElementById("response");
const tokenEl = document.getElementById("token");
const buyerTokenStateEl = document.getElementById("buyer-token-state");
const sellerTokenStateEl = document.getElementById("seller-token-state");

const STORAGE_KEY = "mtm_session";
const LEGACY_TOKEN_KEY = "mtm_token";

const API = {
  register: "/api/v1/auth/register",
  login: "/api/v1/auth/login",
  tasks: "/api/v1/tasks",
  applications: "/api/v1/applications"
};

const session = {
  activeToken: "",
  buyerToken: "",
  sellerToken: "",
  taskId: "",
  applicationId: ""
};

function showResponse(title, data) {
  const pretty = typeof data === "string" ? data : JSON.stringify(data, null, 2);
  responseEl.textContent = `${title}\n\n${pretty}`;
}

function loadSession() {
  const raw = localStorage.getItem(STORAGE_KEY);
  if (raw) {
    try {
      const parsed = JSON.parse(raw);
      Object.assign(session, {
        activeToken: parsed.activeToken || "",
        buyerToken: parsed.buyerToken || "",
        sellerToken: parsed.sellerToken || "",
        taskId: parsed.taskId || "",
        applicationId: parsed.applicationId || ""
      });
    } catch (e) {
      console.error("Failed to parse session storage", e);
    }
  }

  if (!session.activeToken) {
    const legacyToken = localStorage.getItem(LEGACY_TOKEN_KEY) || "";
    session.activeToken = legacyToken;
  }
}

function persistSession() {
  localStorage.setItem(
    STORAGE_KEY,
    JSON.stringify({
      activeToken: session.activeToken,
      buyerToken: session.buyerToken,
      sellerToken: session.sellerToken,
      taskId: session.taskId,
      applicationId: session.applicationId
    })
  );
  localStorage.setItem(LEGACY_TOKEN_KEY, session.activeToken || "");
}

function updateBadge(element, label, token) {
  const hasToken = !!token;
  element.textContent = `${label}: ${hasToken ? "ready" : "missing"}`;
  element.classList.toggle("ready", hasToken);
}

function setInputIfEmpty(id, value) {
  if (!value) {
    return;
  }
  const el = document.getElementById(id);
  if (el && !el.value) {
    el.value = value;
  }
}

function refreshSessionUi() {
  tokenEl.value = session.activeToken;
  updateBadge(buyerTokenStateEl, "Buyer token", session.buyerToken);
  updateBadge(sellerTokenStateEl, "Seller token", session.sellerToken);
  setInputIfEmpty("apply-task-id", session.taskId);
  setInputIfEmpty("fetch-task-id", session.taskId);
  setInputIfEmpty("accept-application-id", session.applicationId);
}

function setActiveToken(token) {
  session.activeToken = token || "";
  persistSession();
  refreshSessionUi();
}

function saveTokenForRole(role, token) {
  if (!token) {
    return;
  }
  if (role === "BUYER") {
    session.buyerToken = token;
  }
  if (role === "SELLER") {
    session.sellerToken = token;
  }
}

function updateSessionFromAuthResponse(data) {
  if (!data || !data.token) {
    return;
  }
  const roles = Array.isArray(data.roles) ? data.roles : [];
  roles.forEach((role) => saveTokenForRole(role, data.token));
  setActiveToken(data.token);
}

function getToken() {
  return tokenEl.value.trim() || session.activeToken;
}

function normalizeError(err) {
  if (!err) {
    return { message: "Unknown error" };
  }
  const payload = err.payload;
  if (payload && typeof payload === "object") {
    return {
      status: err.status || payload.status || null,
      error: payload.error || "Request failed",
      message: payload.message || "Request failed",
      details: payload.details || [],
      path: payload.path || null
    };
  }
  return {
    status: err.status || null,
    message: payload || err.message || "Request failed"
  };
}

async function request(url, options = {}) {
  const headers = options.headers || {};
  if (!headers["Content-Type"] && options.body) {
    headers["Content-Type"] = "application/json";
  }

  const token = getToken();
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const res = await fetch(url, { ...options, headers });
  if (res.status === 204) {
    return {};
  }

  const contentType = res.headers.get("content-type") || "";
  let payload;
  if (contentType.includes("application/json")) {
    payload = await res.json();
  } else {
    payload = await res.text();
  }

  if (!res.ok) {
    throw { status: res.status, payload };
  }
  return payload;
}

document.getElementById("save-token").addEventListener("click", () => {
  setActiveToken(tokenEl.value.trim());
  showResponse("Token", "Saved active token to localStorage.");
});

document.getElementById("clear-token").addEventListener("click", () => {
  setActiveToken("");
  showResponse("Token", "Cleared active token.");
});

document.getElementById("use-buyer-token").addEventListener("click", () => {
  if (!session.buyerToken) {
    showResponse("Buyer Token", "No buyer token found. Register/Login as BUYER first.");
    return;
  }
  setActiveToken(session.buyerToken);
  showResponse("Buyer Token", "Buyer token is now active.");
});

document.getElementById("use-seller-token").addEventListener("click", () => {
  if (!session.sellerToken) {
    showResponse("Seller Token", "No seller token found. Register/Login as SELLER first.");
    return;
  }
  setActiveToken(session.sellerToken);
  showResponse("Seller Token", "Seller token is now active.");
});

document.getElementById("register-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const body = {
    fullName: document.getElementById("register-name").value,
    email: document.getElementById("register-email").value,
    password: document.getElementById("register-password").value,
    roles: [document.getElementById("register-role").value]
  };

  try {
    const data = await request(API.register, {
      method: "POST",
      body: JSON.stringify(body)
    });
    updateSessionFromAuthResponse(data);
    showResponse("Register Success", data);
  } catch (err) {
    showResponse(`Register Failed (${err.status || "ERR"})`, normalizeError(err));
  }
});

document.getElementById("login-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const body = {
    email: document.getElementById("login-email").value,
    password: document.getElementById("login-password").value
  };

  try {
    const data = await request(API.login, {
      method: "POST",
      body: JSON.stringify(body)
    });
    updateSessionFromAuthResponse(data);
    showResponse("Login Success", data);
  } catch (err) {
    showResponse(`Login Failed (${err.status || "ERR"})`, normalizeError(err));
  }
});

document.getElementById("task-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const body = {
    title: document.getElementById("task-title").value,
    description: document.getElementById("task-description").value,
    budget: Number(document.getElementById("task-budget").value)
  };

  try {
    const data = await request(API.tasks, {
      method: "POST",
      body: JSON.stringify(body)
    });
    if (data && data.id) {
      session.taskId = String(data.id);
      persistSession();
      refreshSessionUi();
    }
    showResponse("Create Task Success", data);
  } catch (err) {
    showResponse(`Create Task Failed (${err.status || "ERR"})`, normalizeError(err));
  }
});

document.getElementById("apply-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const body = {
    taskId: Number(document.getElementById("apply-task-id").value),
    proposedAmount: Number(document.getElementById("apply-amount").value),
    coverLetter: document.getElementById("apply-cover").value
  };

  try {
    const data = await request(API.applications, {
      method: "POST",
      body: JSON.stringify(body)
    });
    if (data && data.id) {
      session.applicationId = String(data.id);
      persistSession();
      refreshSessionUi();
    }
    showResponse("Apply Success", data);
  } catch (err) {
    showResponse(`Apply Failed (${err.status || "ERR"})`, normalizeError(err));
  }
});

document.getElementById("accept-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const applicationId = Number(document.getElementById("accept-application-id").value);

  try {
    const data = await request(`${API.applications}/${applicationId}/accept`, {
      method: "POST"
    });
    showResponse("Accept Success", data);
  } catch (err) {
    showResponse(`Accept Failed (${err.status || "ERR"})`, normalizeError(err));
  }
});

document.getElementById("fetch-tasks").addEventListener("click", async () => {
  try {
    const data = await request(API.tasks, { method: "GET" });
    showResponse("Tasks", data);
  } catch (err) {
    showResponse(`Get Tasks Failed (${err.status || "ERR"})`, normalizeError(err));
  }
});

document.getElementById("fetch-task-apps").addEventListener("click", async () => {
  const taskId = Number(document.getElementById("fetch-task-id").value || session.taskId);
  if (!taskId) {
    showResponse("Input Needed", "Provide a valid Task ID.");
    return;
  }

  try {
    const data = await request(`${API.applications}/task/${taskId}`, { method: "GET" });
    showResponse("Applications", data);
  } catch (err) {
    showResponse(`Get Applications Failed (${err.status || "ERR"})`, normalizeError(err));
  }
});

(function init() {
  loadSession();
  persistSession();
  refreshSessionUi();
})();
