const responseEl = document.getElementById("response");
const tokenEl = document.getElementById("token");

const API = {
  register: "/api/v1/auth/register",
  login: "/api/v1/auth/login",
  tasks: "/api/v1/tasks",
  applications: "/api/v1/applications"
};

function getToken() {
  return tokenEl.value.trim();
}

function saveToken(token) {
  tokenEl.value = token;
  localStorage.setItem("mtm_token", token);
}

function showResponse(title, data) {
  const pretty = typeof data === "string" ? data : JSON.stringify(data, null, 2);
  responseEl.textContent = `${title}\n\n${pretty}`;
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
  const contentType = res.headers.get("content-type") || "";
  const payload = contentType.includes("application/json")
    ? await res.json()
    : await res.text();

  if (!res.ok) {
    throw { status: res.status, payload };
  }
  return payload;
}

document.getElementById("save-token").addEventListener("click", () => {
  localStorage.setItem("mtm_token", getToken());
  showResponse("Token", "Saved to localStorage.");
});

document.getElementById("clear-token").addEventListener("click", () => {
  tokenEl.value = "";
  localStorage.removeItem("mtm_token");
  showResponse("Token", "Cleared.");
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
    if (data.token) {
      saveToken(data.token);
    }
    showResponse("Register Success", data);
  } catch (err) {
    showResponse(`Register Failed (${err.status || "ERR"})`, err.payload || err);
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
    if (data.token) {
      saveToken(data.token);
    }
    showResponse("Login Success", data);
  } catch (err) {
    showResponse(`Login Failed (${err.status || "ERR"})`, err.payload || err);
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
    showResponse("Create Task Success", data);
  } catch (err) {
    showResponse(`Create Task Failed (${err.status || "ERR"})`, err.payload || err);
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
    showResponse("Apply Success", data);
  } catch (err) {
    showResponse(`Apply Failed (${err.status || "ERR"})`, err.payload || err);
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
    showResponse(`Accept Failed (${err.status || "ERR"})`, err.payload || err);
  }
});

document.getElementById("fetch-tasks").addEventListener("click", async () => {
  try {
    const data = await request(API.tasks, { method: "GET" });
    showResponse("Tasks", data);
  } catch (err) {
    showResponse(`Get Tasks Failed (${err.status || "ERR"})`, err.payload || err);
  }
});

document.getElementById("fetch-task-apps").addEventListener("click", async () => {
  const taskId = Number(document.getElementById("fetch-task-id").value);
  if (!taskId) {
    showResponse("Input Needed", "Provide a valid Task ID.");
    return;
  }

  try {
    const data = await request(`${API.applications}/task/${taskId}`, { method: "GET" });
    showResponse("Applications", data);
  } catch (err) {
    showResponse(`Get Applications Failed (${err.status || "ERR"})`, err.payload || err);
  }
});

(function init() {
  const saved = localStorage.getItem("mtm_token");
  if (saved) {
    tokenEl.value = saved;
  }
})();
