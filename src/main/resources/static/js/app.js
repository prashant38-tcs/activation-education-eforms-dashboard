document.addEventListener('DOMContentLoaded', function () {
  var toggleBtn = document.getElementById('sidebarToggle');
  var sidebar = document.getElementById('aeSidebar');
  if (toggleBtn && sidebar) {
    toggleBtn.addEventListener('click', function () { sidebar.classList.toggle('collapsed'); });
  }

  var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
  tooltipTriggerList.forEach(function (el) { new bootstrap.Tooltip(el); });

  if (window.jQuery && jQuery.fn.DataTable) {
    jQuery('.ae-datatable').DataTable({ pageLength: 15, order: [], responsive: true });
  }

  document.querySelectorAll('form[data-confirm]').forEach(function (form) {
    form.addEventListener('submit', function (e) {
      if (!confirm(form.getAttribute('data-confirm'))) e.preventDefault();
    });
  });

  var badge = document.getElementById('notifUnreadBadge');
  if (badge) {
    setInterval(function () {
      fetch(window.aeContextPath + '/notifications/api/unread-count')
        .then(function (r) { return r.json(); })
        .then(function (json) {
          var count = json && json.data ? json.data : 0;
          badge.textContent = count;
          badge.style.display = count > 0 ? 'inline-block' : 'none';
        }).catch(function () {});
    }, 60000);
  }

  document.querySelectorAll('[data-toggle-password]').forEach(function (btn) {
    btn.addEventListener('click', function () {
      var target = document.querySelector(btn.getAttribute('data-toggle-password'));
      if (target) target.type = target.type === 'password' ? 'text' : 'password';
    });
  });
});
