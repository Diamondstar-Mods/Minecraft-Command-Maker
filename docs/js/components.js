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
    { label: "Features", href: "#", dropdown: [
      { label: "Custom Syntax System", href: "syntax-system.html" },
      { label: "Functions System", href: "functions.html" },
      { label: "Function Catalog", href: "function-catalog.html" },
      { label: "GUI System", href: "gui-system.html" },
      { label: "Variables & Substitution", href: "variables.html" },
      { label: "Chat Messages", href: "chat-messages.html" },
      { label: "Advanced Permissions", href: "advanced-permissions-guide.html" },
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
    { label: "🏆 v4.0", href: "#", dropdown: [
      { label: "v4.0 Changelog", href: "v4.0-changelog.html" },
      { label: "v4.0 Overview", href: "v4.0-upcoming.html" },
      { label: "Cooldown System", href: "cooldowns.html" },
      { label: "Condition Blocks", href: "conditions.html" },
      { label: "Event Triggers", href: "events.html" },
      { label: "Custom Scoreboards", href: "scoreboards.html" },
      { label: "Placeholder System", href: "placeholders.html" },
      { label: "Modules (.cmk)", href: "modules.html" }
    ]},
    { label: "Help", href: "#", dropdown: [
      { label: "Commands Reference", href: "commands.html" },
      { label: "FAQ", href: "faq.html" },
      { label: "Best Practices", href: "best-practices.html" },
      { label: "Troubleshooting", href: "troubleshooting.html" },
      { label: "Examples", href: "examples.html" },
      { label: "Forum", href: "forum.html" }
    ]}
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
      title: "Getting Started", icon: "📚",
      links: [
        { label: "Quick Start", href: "quick-start.html" },
        { label: "Getting Started", href: "getting-started.html" },
        { label: "Installation", href: "installation.html" },
        { label: "Updating Guide", href: "updating.html" },
        { label: "Configuration", href: "configuration.html" },
        { label: "🚀 v4.0 — What's New", href: "v4.0-changelog.html" }
      ]
    },
    {
      title: "Aliases & Syntax", icon: "⚡",
      links: [
        { label: "Creating Aliases", href: "aliases.html" },
        { label: "Alias Examples", href: "alias-examples.html" },
        { label: "Custom Syntax System", href: "syntax-system.html" },
        { label: "Syntax Patterns", href: "syntax-patterns.html" }
      ]
    },
    {
      title: "Functions", icon: "📦",
      links: [
        { label: "Functions System", href: "functions.html" },
        { label: "Function Catalog", href: "function-catalog.html" },
        { label: "Writing Functions", href: "function-writing.html" },
        { label: "Function Examples", href: "function-examples.html" }
      ]
    },
    {
      title: "Variables & Formatting", icon: "🔤",
      links: [
        { label: "Variables & Substitution", href: "variables.html" },
        { label: "Variable Reference", href: "variable-reference.html" },
        { label: "Chat Formatting", href: "chat-formatting.html" },
        { label: "Color Codes", href: "color-codes.html" }
      ]
    },
    {
      title: "Commands & Selectors", icon: "🎯",
      links: [
        { label: "Commands Reference", href: "commands.html" },
        { label: "Target Selectors", href: "target-selectors.html" },
        { label: "Timed Commands", href: "timed-commands.html" },
        { label: "Team Commands", href: "team-commands.html" },
        { label: "NBT Commands", href: "nbt-commands.html" }
      ]
    },
    {
      title: "Permissions", icon: "🔐",
      links: [
        { label: "Advanced Permissions", href: "advanced-permissions-guide.html" },
        { label: "Permissions Quick Reference", href: "permissions-quick-reference.html" },
        { label: "Permissions Setup", href: "permissions-setup.html" },
        { label: "LuckPerms Integration", href: "luckperms-integration.html" }
      ]
    },
    {
      title: "Advanced", icon: "🛠️",
      links: [
        { label: "GUI System", href: "gui-system.html" },
        { label: "Chat Messages", href: "chat-messages.html" },
        { label: "Multi-World Setups", href: "multi-world.html" },
        { label: "Performance Tips", href: "performance.html" },
        { label: "Command Safety", href: "command-safety.html" }
      ]
    },
    {
      title: "Server Systems", icon: "🏰",
      links: [
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
      title: "🏆 v4.0 New Features", icon: "",
      links: [
        { label: "v4.0 Changelog", href: "v4.0-changelog.html" },
        { label: "Cooldown System", href: "cooldowns.html" },
        { label: "Condition Blocks", href: "conditions.html" },
        { label: "Event Triggers", href: "events.html" },
        { label: "Custom Scoreboards", href: "scoreboards.html" },
        { label: "Placeholder System", href: "placeholders.html" },
        { label: "Modules (.cmk)", href: "modules.html" }
      ]
    },
    {
      title: "Help & Support", icon: "❓",
      links: [
        { label: "Troubleshooting", href: "troubleshooting.html" },
        { label: "Debugging", href: "debugging.html" },
        { label: "FAQ", href: "faq.html" },
        { label: "Best Practices", href: "best-practices.html" },
        { label: "Backup & Restore", href: "backup-restore.html" },
        { label: "Contributing Guide", href: "contributing-guide.html" }
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
  add("Permissions Quick Reference", "permissions-quick-reference.html");
  add("Updating Guide", "updating.html");

  // Function catalog entries — searchable by function name
  var functions = [
    "spleef_arena", "parkour_course", "hide_and_seek", "pvp_arena",
    "zombie_siege", "mob_army", "boss_arena",
    "bridge_builder", "underground_base", "lighthouse", "treehouse", "campsite", "rainbow_road", "spawn_setup",
    "light_show", "bubble_party", "fireworkshow", "new_year_countdown", "dance_party", "better_dance_party", "danceparty", "halloween_spook",
    "tntrain", "extratntrain", "meteor_shower", "scorched_earth", "gravity_flip", "craycray", "copilot", "funnybutton",
    "lag_cleaner", "time_locker", "mobsoff", "base_protection", "starter_kit",
    "helloworld", "example", "sound",
    "maze_runner", "ice_race", "archery_range", "fishing_derby", "king_of_the_hill", "death_run", "death_swap", "trivia_challenge",
    "skeleton_siege", "dragon_fight", "arena_of_champions", "wave_defense", "survival_island",
    "castle_gate", "windmill", "suspension_bridge", "nether_portal_room", "enchanting_tower", "market_stalls",
    "aurora_borealis", "confetti_storm", "rainbow_trail", "thunder_concert", "fire_tornado",
    "anvil_rain", "potion_storm", "chicken_plague", "ender_invasion", "void_pull",
    "backup_reminder", "night_vision", "clearing", "heal_all", "feed_all", "repair_all",
    "scoreboard_demo", "command_chain",
    "automatic_door", "piston_elevator", "item_sorter", "secret_passage", "tnt_cannon",
    "quest_board", "dungeon_entrance", "treasure_hunt", "boss_drop", "rpg_shop",
    "crop_farm", "animal_pen", "flower_garden"
  ];
  for (var i = 0; i < functions.length; i++) {
    add("Function: " + functions[i], "function-catalog.html");
  }

  return pages;
};

CM.pages = CM.buildSearchIndex();

// Edit button for navbar
CM.renderEditLink = function () {
  const fn = CM.config.currentFile();
  const url = `${CM.config.repoUrl}/edit/${CM.config.editBranch}/docs/${fn}`;
  return `<a href="${url}" target="_blank" rel="noopener noreferrer" class="nav-link edit-nav-link" title="Edit this page on GitHub">✏️ Edit</a>`;
};

// Last commit iframe
CM.renderLastCommit = function () {
  const fn = CM.config.currentFile();
  return `<iframe style="border:none;font-style:italic;opacity:0.7;margin-bottom:1rem;" src="last-commit.html?path=${fn}" width="400" height="70" title="Last commit info"></iframe>`;
};

// Init — inject all chrome into the page
CM.init = function () {
  // Load web fonts (Inter + JetBrains Mono)
  var fontLink = document.createElement("link");
  fontLink.rel = "stylesheet";
  fontLink.href = "https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500;600&display=swap";
  document.head.appendChild(fontLink);

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
          ${CM.renderEditLink()}
          <div class="dropdown theme-dropdown">
            <button class="dropdown-toggle theme-toggle-btn" aria-label="Change theme">🌓 Theme ▼</button>
            <div class="dropdown-menu">
              <a href="#" data-theme="light" class="theme-option">☀️ Light</a>
              <a href="#" data-theme="dark" class="theme-option">🌙 Dark</a>
              <a href="#" data-theme="auto" class="theme-option">💻 System</a>
            </div>
          </div>
          <div class="nav-search">
            <input type="text" id="searchBox" placeholder="Search..." class="search-input" autocomplete="off" role="search">
          </div>
        </div>
      </div>`;
    // Mobile search bar — shown below navbar on narrow screens
    var mobileSearch = document.createElement("div");
    mobileSearch.className = "mobile-search";
    mobileSearch.innerHTML = '<input type="text" id="mobileSearchBox" placeholder="Search..." class="search-input" autocomplete="off" role="search">';
    navbar.parentNode.insertBefore(mobileSearch, navbar.nextSibling);
  }

  if (sidebar) {
    sidebar.innerHTML = CM.sidebar.render();
  }

  if (footer) {
    footer.innerHTML = CM.footer.render();
  }

  // Inject last-commit iframe at top of content
  if (content) {
    const lastCommit = document.createElement("div");
    lastCommit.innerHTML = CM.renderLastCommit();
    const firstEl = content.firstElementChild;
    if (firstEl) {
      content.insertBefore(lastCommit.firstElementChild, firstEl);
    } else {
      content.appendChild(lastCommit.firstElementChild);
    }
  }

  // Inject Vercel Analytics on every page
  var vaScript = document.createElement("script");
  vaScript.textContent = 'window.va = window.va || function () { (window.vaq = window.vaq || []).push(arguments); };';
  document.body.appendChild(vaScript);
  var vaDefer = document.createElement("script");
  vaDefer.src = "/_vercel/insights/script.js";
  vaDefer.defer = true;
  document.body.appendChild(vaDefer);
};

// Auto-init when DOM is ready
document.addEventListener("DOMContentLoaded", CM.init);
