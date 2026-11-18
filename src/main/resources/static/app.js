"use strict";

/* ===========================================================
   Employee Management System - browser front end (plain JavaScript)
   Talks to the Spring Boot REST API with a JWT token.
   =========================================================== */

const DEPARTMENTS = [
  ["ENGINEERING", "Engineering"],
  ["HR", "Human Resources"],
  ["FINANCE", "Finance"],
  ["MARKETING", "Marketing"],
  ["SALES", "Sales"],
  ["OPERATIONS", "Operations"],
  ["SUPPORT", "Customer Support"],
];

const state = {
  token: sessionStorage.getItem("token"),
  username: sessionStorage.getItem("username"),
  role: sessionStorage.getItem("role"),
  employees: [],
  editingId: null,
};

const $ = (id) => document.getElementById(id);

/* ---------------- helpers ---------------- */

// Escape text before putting it in HTML, so nobody can inject scripts through a name or email.
function esc(value) {
  return String(value ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function money(value) {
  return Number(value || 0).toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function canModify() {
  return state.role === "ADMIN" || state.role === "HR";
}

function errorText(data, status) {
  if (data && data.errors && Object.keys(data.errors).length) {
    return Object.values(data.errors).join(". ");
  }
  if (data && data.message) {
    return data.message;
  }
  return "Something went wrong (error " + status + ").";
}

function showNotice(message, bad) {
  const box = $("notice");
  box.textContent = message;
  box.className = "notice" + (bad ? " bad" : "");
  box.hidden = false;
  clearTimeout(showNotice.timer);
  showNotice.timer = setTimeout(() => { box.hidden = true; }, 4000);
}

function showError(box, message) {
  box.textContent = message;
  box.hidden = !message;
}

/* ---------------- API ---------------- */

async function api(path, options = {}) {
  const headers = { "Content-Type": "application/json" };
  if (state.token) {
    headers["Authorization"] = "Bearer " + state.token;
  }
  const response = await fetch(path, { ...options, headers });

  if (response.status === 401 && path !== "/api/auth/login") {
    logout();
    throw new Error("Your session ended. Please log in again.");
  }
  if (response.status === 204) {
    return null;
  }
  let data = null;
  try {
    data = await response.json();
  } catch (e) {
    data = null;
  }
  if (!response.ok) {
    throw new Error(errorText(data, response.status));
  }
  return data;
}

/* ---------------- login / logout ---------------- */

async function handleLogin(event) {
  event.preventDefault();
  showError($("loginError"), "");
  try {
    const data = await api("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({
        username: $("loginUsername").value,
        password: $("loginPassword").value,
      }),
    });
    state.token = data.token;
    state.username = data.username;
    state.role = data.role;
    sessionStorage.setItem("token", data.token);
    sessionStorage.setItem("username", data.username);
    sessionStorage.setItem("role", data.role);
    $("loginPassword").value = "";
    startApp();
  } catch (e) {
    showError($("loginError"), e.message);
  }
}

function logout() {
  state.token = null;
  state.username = null;
  state.role = null;
  sessionStorage.clear();
  $("appView").hidden = true;
  $("loginView").hidden = false;
}

function startApp() {
  $("loginView").hidden = true;
  $("appView").hidden = false;
  $("whoLabel").textContent = "Logged in as " + state.username + " (" + state.role + ")";
  $("addBtn").hidden = !canModify();
  document.querySelectorAll(".admin-only").forEach((el) => { el.hidden = state.role !== "ADMIN"; });
  switchTab("employees");
}

/* ---------------- tabs ---------------- */

function switchTab(name) {
  document.querySelectorAll(".tab").forEach((t) => t.classList.toggle("active", t.dataset.tab === name));
  document.querySelectorAll(".panel").forEach((p) => { p.hidden = p.id !== "tab-" + name; });
  if (name === "employees") loadEmployees();
  if (name === "reports") loadReports();
  if (name === "users") loadUsers();
  if (name === "audit") loadAudit();
}

/* ---------------- employees ---------------- */

async function loadEmployees() {
  const params = new URLSearchParams();
  const search = $("searchInput").value.trim();
  if (search) params.set("search", search);
  if ($("deptFilter").value) params.set("department", $("deptFilter").value);
  try {
    state.employees = await api("/api/employees?" + params.toString());
    renderEmployees();
  } catch (e) {
    showNotice(e.message, true);
  }
}

function renderEmployees() {
  const rows = state.employees.map((e) => {
    const actions = canModify()
      ? '<button class="btn small" data-action="edit" data-id="' + e.id + '">Edit</button>' +
        '<button class="btn small danger" data-action="delete" data-id="' + e.id + '">Delete</button>'
      : "";
    return "<tr>" +
      "<td>" + esc(e.employeeCode) + "</td>" +
      "<td>" + esc(e.name) + "</td>" +
      '<td><span class="badge">' + (e.type === "FULL_TIME" ? "Full-time" : "Part-time") + "</span></td>" +
      "<td>" + esc(e.departmentName) + "</td>" +
      "<td>" + esc(e.designation) + "</td>" +
      "<td>" + esc(e.email) + "</td>" +
      "<td>" + esc(e.phone) + "</td>" +
      "<td>" + esc(e.joiningDate) + "</td>" +
      '<td class="num">' + money(e.monthlyPay) + "</td>" +
      '<td><div class="row-actions">' +
        '<button class="btn small" data-action="payslip" data-id="' + e.id + '">Payslip</button>' + actions +
      "</div></td></tr>";
  });
  $("employeeRows").innerHTML = rows.length
    ? rows.join("")
    : '<tr><td colspan="10" class="muted">No employees found.</td></tr>';
  const total = state.employees.reduce((sum, e) => sum + e.monthlyPay, 0);
  $("employeeStatus").textContent =
    state.employees.length + " employee(s) shown | Total monthly pay of these: " + money(total);
}

function fillDepartments() {
  $("deptFilter").innerHTML = '<option value="">All departments</option>' +
    DEPARTMENTS.map((d) => '<option value="' + d[0] + '">' + esc(d[1]) + "</option>").join("");
  $("fDepartment").innerHTML = DEPARTMENTS
    .map((d) => '<option value="' + d[0] + '">' + esc(d[1]) + "</option>").join("");
}

function updateTypeFields() {
  const full = $("fType").value === "FULL_TIME";
  $("basicWrap").hidden = !full;
  $("rateWrap").hidden = full;
  $("hoursWrap").hidden = full;
}

function openEmployeeDialog(employee) {
  state.editingId = employee ? employee.id : null;
  $("employeeDialogTitle").textContent = employee ? "Edit Employee " + employee.employeeCode : "Add Employee";
  showError($("employeeFormError"), "");
  $("fName").value = employee ? employee.name : "";
  $("fEmail").value = employee ? employee.email : "";
  $("fPhone").value = employee ? employee.phone : "";
  $("fDepartment").value = employee ? employee.department : DEPARTMENTS[0][0];
  $("fDesignation").value = employee ? employee.designation : "";
  $("fJoined").value = employee ? employee.joiningDate : new Date().toISOString().slice(0, 10);
  $("fType").value = employee ? employee.type : "FULL_TIME";
  $("fBasic").value = employee && employee.basicSalary != null ? employee.basicSalary : "";
  $("fRate").value = employee && employee.hourlyRate != null ? employee.hourlyRate : "";
  $("fHours").value = employee && employee.hoursWorked != null ? employee.hoursWorked : "";
  updateTypeFields();
  $("employeeDialog").showModal();
}

function numberOrNull(value) {
  return value === "" ? null : Number(value);
}

async function saveEmployee(event) {
  event.preventDefault();
  const full = $("fType").value === "FULL_TIME";
  const body = {
    name: $("fName").value.trim(),
    email: $("fEmail").value.trim(),
    phone: $("fPhone").value.trim(),
    department: $("fDepartment").value,
    designation: $("fDesignation").value.trim(),
    joiningDate: $("fJoined").value,
    type: $("fType").value,
    basicSalary: full ? numberOrNull($("fBasic").value) : null,
    hourlyRate: full ? null : numberOrNull($("fRate").value),
    hoursWorked: full ? null : numberOrNull($("fHours").value),
  };
  try {
    if (state.editingId) {
      await api("/api/employees/" + state.editingId, { method: "PUT", body: JSON.stringify(body) });
      showNotice("Employee updated.");
    } else {
      await api("/api/employees", { method: "POST", body: JSON.stringify(body) });
      showNotice("Employee added.");
    }
    $("employeeDialog").close();
    loadEmployees();
  } catch (e) {
    showError($("employeeFormError"), e.message);
  }
}

async function showPayslip(id) {
  try {
    const p = await api("/api/employees/" + id + "/payslip");
    const lines = p.lines.map((l) =>
      "<tr><td>" + esc(l.label) + '</td><td class="num">' + money(l.amount) + "</td></tr>").join("");
    $("payslipBody").innerHTML =
      '<div class="payslip"><h2>PAYSLIP</h2>' +
      '<div class="meta">' + esc(p.name) + " (" + esc(p.employeeCode) + ")<br>" +
      esc(p.department) + " - " + esc(p.designation) + "</div>" +
      "<table>" + lines +
      '<tr class="net"><td>NET SALARY</td><td class="num">' + money(p.netSalary) + "</td></tr></table></div>";
    $("payslipDialog").showModal();
  } catch (e) {
    showNotice(e.message, true);
  }
}

async function deleteEmployee(id) {
  const employee = state.employees.find((e) => e.id === id);
  const label = employee ? employee.name + " (" + employee.employeeCode + ")" : "this employee";
  if (!confirm("Delete " + label + "? This cannot be undone.")) return;
  try {
    await api("/api/employees/" + id, { method: "DELETE" });
    showNotice("Employee deleted.");
    loadEmployees();
  } catch (e) {
    showNotice(e.message, true);
  }
}

function handleRowClick(event) {
  const button = event.target.closest("button[data-action]");
  if (!button) return;
  const id = Number(button.dataset.id);
  const action = button.dataset.action;
  if (action === "payslip") showPayslip(id);
  if (action === "delete") deleteEmployee(id);
  if (action === "edit") openEmployeeDialog(state.employees.find((e) => e.id === id));
}

/* ---------------- reports ---------------- */

async function loadReports() {
  try {
    const r = await api("/api/reports/summary");
    $("reportCards").innerHTML =
      stat("Total employees", r.totalEmployees) +
      stat("Total monthly payroll", money(r.totalMonthlyPayroll)) +
      stat("Highest paid", r.highestPaidName
        ? esc(r.highestPaidName) + " (" + money(r.highestPaidAmount) + ")" : "-");
    const max = Math.max(1, ...r.departments.map((d) => d.count));
    $("reportRows").innerHTML = r.departments.length
      ? r.departments.map((d) =>
          "<tr><td>" + esc(d.departmentName) + "</td><td>" + d.count + "</td>" +
          '<td class="bar-cell"><div class="bar" style="width:' + Math.round((d.count / max) * 100) + '%"></div></td>' +
          '<td class="num">' + money(d.averagePay) + '</td><td class="num">' + money(d.totalPay) + "</td></tr>").join("")
      : '<tr><td colspan="5" class="muted">No data yet.</td></tr>';
  } catch (e) {
    showNotice(e.message, true);
  }
}

function stat(label, value) {
  return '<div class="stat"><div class="label">' + esc(label) + '</div><div class="value">' + value + "</div></div>";
}

/* ---------------- download report ---------------- */

async function downloadReport() {
  try {
    const response = await fetch("/api/reports/export", {
      headers: { "Authorization": "Bearer " + state.token },
    });
    if (response.status === 401) {
      logout();
      return;
    }
    if (!response.ok) {
      throw new Error("Could not download the report.");
    }
    const blob = await response.blob();
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = "employee-report.csv";
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
    showNotice("Report downloaded. Open it with Excel.");
  } catch (e) {
    showNotice(e.message, true);
  }
}

/* ---------------- users & audit (admin) ---------------- */

async function loadUsers() {
  try {
    const users = await api("/api/users");
    $("userRows").innerHTML = users
      .map((u) => "<tr><td>" + esc(u.username) + '</td><td><span class="badge">' + esc(u.role) + "</span></td></tr>")
      .join("");
  } catch (e) {
    showNotice(e.message, true);
  }
}

async function createUser(event) {
  event.preventDefault();
  try {
    await api("/api/users", {
      method: "POST",
      body: JSON.stringify({
        username: $("newUsername").value.trim(),
        password: $("newPassword").value,
        role: $("newRole").value,
      }),
    });
    $("userForm").reset();
    showNotice("User created.");
    loadUsers();
  } catch (e) {
    showNotice(e.message, true);
  }
}

async function loadAudit() {
  try {
    const entries = await api("/api/audit");
    $("auditRows").innerHTML = entries.map((a) =>
      "<tr><td>" + esc(String(a.time).replace("T", " ").slice(0, 19)) + "</td><td>" + esc(a.username) +
      '</td><td><span class="badge">' + esc(a.action) + "</span></td><td>" + esc(a.details) + "</td></tr>").join("");
  } catch (e) {
    showNotice(e.message, true);
  }
}

/* ---------------- change password ---------------- */

async function changePassword(event) {
  event.preventDefault();
  showError($("passwordError"), "");
  if ($("pNew").value !== $("pConfirm").value) {
    showError($("passwordError"), "The new passwords do not match.");
    return;
  }
  try {
    await api("/api/auth/change-password", {
      method: "POST",
      body: JSON.stringify({ oldPassword: $("pOld").value, newPassword: $("pNew").value }),
    });
    $("passwordForm").reset();
    $("passwordDialog").close();
    showNotice("Password changed.");
  } catch (e) {
    showError($("passwordError"), e.message);
  }
}

/* ---------------- start ---------------- */

function debounce(fn, ms) {
  let timer;
  return (...args) => { clearTimeout(timer); timer = setTimeout(() => fn(...args), ms); };
}

document.addEventListener("DOMContentLoaded", () => {
  fillDepartments();
  $("loginForm").addEventListener("submit", handleLogin);
  $("logoutBtn").addEventListener("click", logout);
  $("tabs").addEventListener("click", (e) => {
    const tab = e.target.closest(".tab");
    if (tab) switchTab(tab.dataset.tab);
  });
  $("searchInput").addEventListener("input", debounce(loadEmployees, 250));
  $("deptFilter").addEventListener("change", loadEmployees);
  $("clearBtn").addEventListener("click", () => {
    $("searchInput").value = "";
    $("deptFilter").value = "";
    loadEmployees();
  });
  $("addBtn").addEventListener("click", () => openEmployeeDialog(null));
  $("employeeRows").addEventListener("click", handleRowClick);
  $("employeeForm").addEventListener("submit", saveEmployee);
  $("employeeCancel").addEventListener("click", () => $("employeeDialog").close());
  $("fType").addEventListener("change", updateTypeFields);
  $("payslipClose").addEventListener("click", () => $("payslipDialog").close());
  $("payslipPrint").addEventListener("click", () => window.print());
  $("downloadReportBtn").addEventListener("click", downloadReport);
  $("downloadEmployeesBtn").addEventListener("click", downloadReport);
  $("userForm").addEventListener("submit", createUser);
  $("changePasswordBtn").addEventListener("click", () => {
    showError($("passwordError"), "");
    $("passwordForm").reset();
    $("passwordDialog").showModal();
  });
  $("passwordForm").addEventListener("submit", changePassword);
  $("passwordCancel").addEventListener("click", () => $("passwordDialog").close());

  if (state.token) {
    startApp();
  }
});
