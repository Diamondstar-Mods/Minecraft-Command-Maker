/**
 * Command Maker Wiki — Shared UI Components
 * Single source of truth for navigation, sidebar, footer, and page metadata.
 * Load this before script.js on every page.
 */
const CM = window.CM || {};

CM.config = {
  siteName: "Command Maker Wiki",
  tagline: "The Ultimate Minecraft Command Alias & Custom Syntax Mod",
  repoUrl: "https://github.com/Diamondstar-Mods/Minecraft-Command-Maker",
  editBranch: "3.x-fabric-1.22",
  donateUrl: "https://commandmakerwiki.lucasgeitgey.com/donate.html",
  currentFile() {
    return window.location.pathname.split("/").pop() || "index.html";
  },
  isActive(href) {
    return this.currentFile() === href ? 'active' : '';
  },
  isActiveSection(prefix) {
    return this.currentFile().startsWith(prefix) ? 'active' : '';
  }
};

// Navigation — single definition, renders consistently on every page
CM.nav = {
  items: [
    { label: "Home", href: "index.html" },
    { label: "Getting Started", href: "getting-started.html" },
    { label: "Downloads", href: "download.html" },
    { label: "Commands", href: "commands.html" },
    { label: "Custom Syntax", href: "syntax-system.html" },
    { label: "Functions", href: "functions.html" },
    { label: "Catalog", href: "function-catalog.html" },
    { label: "GUI", href: "gui-system.html" },
    { label: "Admin", href: "#", dropdown: [
      { label: "Advanced Permissions", href: "advanced-permissions-guide.html" },
      { label: "Commands Reference", href: "commands.html" },
      { label: "Best Practices", href: "best-practices.html" }
    ]},
    { label: "Systems", href: "#", dropdown: [
      { label: "Economy System", href: "economy-system.html" },
      { label: "Kit System", href: "kit-system.html" },
      { label: "Home System", href: "home-system.html" },
      { label: "Shop System", href: "shop-system.html" },
      { label: "Jail System", href: "jail-system.html" },
      { label: "Mute System", href: "mute-system.html" },
      { label: "Vote System", href: "vote-system.html" },
      { label: "Rank System", href: "rank-system.html" },
      { label: "Achievement System", href: "achievement-system.html" },
      { label: "Event System", href: "event-system.html" }
    ]},
    { label: "FAQ", href: "faq.html" },
    { label: "Forum", href: "forum.html" }
  ],

  render() {
    const cf = CM.config.currentFile();
    let h = "";
    for (const item of this.items) {
      if (item.dropdown) {
        const isActive = item.dropdown.some(d => d.href === cf);
        h += '<div class="dropdown">';
        h += `<button class="dropdown-toggle${isActive ? ' active' : ''}">${item.label} ▼</button>`;
        h += '<div class="dropdown-menu">';
        for (const d of item.dropdown) {
          h += `<a href="${d.href}" class="${d.href === cf ? 'active' : ''}">${d.label}</a>`;
        }
        h += '</div></div>';
      } else {
        h += `<a href="${item.href}" class="nav-link${item.href === cf ? ' active' : ''}">${item.label}</a>`;
      }
    }
    return h;
  }
};

// Sidebar — single definition
CM.sidebar = {
  sections: [
    {
      title: "Documentation", icon: "📚",
      links: [
        { label: "Getting Started", href: "getting-started.html" },
        { label: "Installation", href: "installation.html" },
        { label: "Configuration", href: "configuration.html" },
        { label: "Creating Aliases", href: "aliases.html" }
      ]
    },
    {
      title: "Features", icon: "⚡",
      links: [
        { label: "Custom Syntax System", href: "syntax-system.html" },
        { label: "Functions System", href: "functions.html" },
        { label: "Function Catalog", href: "function-catalog.html" },
        { label: "Variables & Substitution", href: "variables.html" },
        { label: "GUI System", href: "gui-system.html" },
        { label: "Chat Messages", href: "chat-messages.html" },
        { label: "Advanced Permissions", href: "advanced-permissions-guide.html" }
      ]
    },
    {
      title: "Examples", icon: "🎮",
      links: [
        { label: "TPA System", href: "tpa-system.html" },
        { label: "Ban System", href: "ban-system.html" },
        { label: "Warp System", href: "warp-system.html" },
        { label: "Custom Commands", href: "custom-commands.html" }
      ]
    },
    {
      title: "Help", icon: "❓",
      links: [
        { label: "Troubleshooting", href: "troubleshooting.html" },
        { label: "FAQ", href: "faq.html" },
        { label: "Best Practices", href: "best-practices.html" }
      ]
    }
  ],

  render() {
    const cf = CM.config.currentFile();
    let h = "";
    for (const sec of this.sections) {
      h += '<div class="sidebar-widget">';
      h += `<h3 class="widget-title">${sec.icon} ${sec.title}</h3>`;
      h += '<ul class="widget-list">';
      for (const link of sec.links) {
        h += `<li><a href="${link.href}" class="${link.href === cf ? 'active' : ''}">${link.label}</a></li>`;
      }
      h += '</ul></div>';
    }
    return h;
  }
};

// Footer
CM.footer = {
  sections: [
    {
      title: "Documentation",
      links: [
        { label: "Getting Started", href: "getting-started.html" },
        { label: "Commands", href: "commands.html" },
        { label: "Custom Syntax", href: "syntax-system.html" },
        { label: "Examples", href: "examples.html" }
      ]
    },
    {
      title: "Resources",
      links: [
        { label: "Troubleshooting", href: "troubleshooting.html" },
        { label: "FAQ", href: "faq.html" },
        { label: "Best Practices", href: "best-practices.html" },
        { label: "GitHub", href: "https://github.com/Diamondstar-Mods/Minecraft-Command-Maker", external: true }
      ]
    },
    {
      title: "Community",
      links: [
        { label: "Modrinth", href: "https://modrinth.com/mod/command-maker", external: true },
        { label: "Report Issues", href: "https://github.com/Diamondstar-Mods/Minecraft-Command-Maker/issues", external: true },
        { label: "Contribute", href: "https://github.com/Diamondstar-Mods/Minecraft-Command-Maker", external: true }
      ]
    }
  ],

  render() {
    let h = '<div class="footer-content">';
    for (const sec of this.sections) {
      h += '<div class="footer-section"><h4>' + sec.title + '</h4><ul>';
      for (const link of sec.links) {
        const target = link.external ? ' target="_blank" rel="noopener"' : '';
        h += `<li><a href="${link.href}"${target}>${link.label}</a></li>`;
      }
      h += '</ul></div>';
    }
    h += '</div>';
    h += '<div class="footer-bottom"><p>&copy; 2025–2026 Diamondstar Mods. Built for Minecraft players.</p></div>';
    return h;
  }
};

// Search index — auto-generated from nav + sidebar
CM.buildSearchIndex = function () {
  const seen = {};
  const pages = {};

  function add(label, href) {
    if (href === "#" || seen[href]) return;
    seen[href] = true;
    pages[label] = href;
  }

  add("Home", "index.html");
  for (const item of CM.nav.items) {
    if (item.dropdown) {
      for (const d of item.dropdown) add(d.label, d.href);
    } else {
      add(item.label, item.href);
    }
  }
  for (const sec of CM.sidebar.sections) {
    for (const link of sec.links) add(link.label, link.href);
  }
  // Additional pages not in nav/sidebar
  add("Changelog 3.0.0", "changelog3.0.0.html");
  add("License", "license.html");
  add("Install Video", "install-video.html");
  add("Download (Client)", "downloadClient.html");
  add("Download (Local)", "download-local.html");
  add("Examples", "examples.html");

  return pages;
};

CM.pages = CM.buildSearchIndex();

// Edit button
CM.renderEditButton = function () {
  const fn = CM.config.currentFile();
  const url = `${CM.config.repoUrl}/edit/${CM.config.editBranch}/docs/${fn}`;
  return `<a class="edit-button" href="${url}" target="_blank" rel="noopener noreferrer" title="Edit this page on GitHub">✏️ Edit</a>`;
};

// Last commit iframe
CM.renderLastCommit = function () {
  const fn = CM.config.currentFile();
  return `<iframe style="border:none;font-style:italic;opacity:0.7;margin-bottom:1rem;" src="last-commit.html?path=${fn}" width="300" height="24" title="Last commit info"></iframe>`;
};

// Init — inject all chrome into the page
CM.init = function () {
  const navbar = document.getElementById("navbar");
  const sidebar = document.getElementById("sidebar");
  const footer = document.getElementById("footer");
  const content = document.querySelector(".content");

  if (navbar) {
    navbar.innerHTML = `
      <div class="nav-container">
        <div class="nav-logo">
          <a href="index.html"><img class="logo-icon" src="favicon.ico" alt="Icon" width="28" height="28"> ${CM.config.siteName}</a>
        </div>
        <button class="hamburger" aria-label="Toggle menu" aria-expanded="false">
          <span></span><span></span><span></span>
        </button>
        <div class="nav-menu">${CM.nav.render()}</div>
        <div class="nav-actions">
          <button class="theme-toggle" aria-label="Toggle dark mode" title="Toggle dark mode">🌓</button>
          <div class="nav-search">
            <input type="text" id="searchBox" placeholder="Search..." class="search-input" autocomplete="off" role="search">
          </div>
        </div>
      </div>`;
  }

  if (sidebar) {
    sidebar.innerHTML = CM.sidebar.render();
  }

  if (footer) {
    footer.innerHTML = CM.footer.render();
  }

  // Inject edit button into body
  const editBtn = document.createElement("div");
  editBtn.innerHTML = CM.renderEditButton();
  document.body.appendChild(editBtn.firstElementChild);
};

// Auto-init when DOM is ready
document.addEventListener("DOMContentLoaded", CM.init);
