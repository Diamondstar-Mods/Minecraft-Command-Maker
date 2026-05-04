// Search functionality
document.addEventListener("DOMContentLoaded", function () {
  // Theme switching
  const themeSelect = document.getElementById("theme-select");
  const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;
  const savedTheme = localStorage.getItem("theme");

  // Determine initial theme
  let currentTheme = savedTheme || (prefersDark ? "dark" : "light");

  // Apply theme
  function applyTheme(theme) {
    document.body.className = theme + "-theme";
    if (themeSelect) {
      themeSelect.value = theme;
    }
    localStorage.setItem("theme", theme);
  }

  applyTheme(currentTheme);

  // Show dropdown only if user prefers dark mode
  if (prefersDark && !savedTheme) {
    if (themeSelect) {
      themeSelect.style.display = "block";
    }
  } else if (savedTheme) {
    if (themeSelect) {
      themeSelect.style.display = "block";
    }
  }

  // Handle theme change
  if (themeSelect) {
    themeSelect.addEventListener("change", function () {
      applyTheme(this.value);
    });
  }

  // Dropdown menu functionality
  const dropdowns = document.querySelectorAll(".dropdown");
  dropdowns.forEach((dropdown) => {
    const toggle = dropdown.querySelector(".dropdown-toggle");
    const menu = dropdown.querySelector(".dropdown-menu");

    toggle.addEventListener("click", function (e) {
      e.preventDefault();
      menu.classList.toggle("show");
    });

    // Close dropdown when clicking outside
    document.addEventListener("click", function (e) {
      if (!dropdown.contains(e.target)) {
        menu.classList.remove("show");
      }
    });
  });

  const searchBox = document.getElementById("searchBox");

  if (searchBox) {
    // Reduce interference from browser extensions/OS autofill/spellcheck by disabling these features
    try {
      searchBox.setAttribute("autocomplete", "off");
      searchBox.setAttribute("autocorrect", "off");
      searchBox.setAttribute("autocapitalize", "off");
      searchBox.setAttribute("spellcheck", "false");
      searchBox.setAttribute("role", "search");
    } catch (e) {
      // ignore attribute setting failures in older browsers
      console.debug("Could not set search input attributes:", e);
    }
    // Search index: visible label => filename
    const pages = {
      Home: "index.html",
      "Getting Started": "getting-started.html",
      Installation: "installation.html",
      Configuration: "configuration.html",
      "Creating Aliases": "aliases.html",
      "Custom Syntax": "syntax-system.html",
      Functions: "functions.html",
      "Variables & Substitution": "variables.html",
      "GUI System": "gui-system.html",
      "Chat Messages": "chat-messages.html",
      "TPA System": "tpa-system.html",
      "Ban System": "ban-system.html",
      "Warp System": "warp-system.html",
      Commands: "commands.html",
      Examples: "examples.html",
      "Economy System": "economy-system.html",
      "Kit System": "kit-system.html",
      "Home System": "home-system.html",
      "Shop System": "shop-system.html",
      "Jail System": "jail-system.html",
      "Mute System": "mute-system.html",
      "Vote System": "vote-system.html",
      "Rank System": "rank-system.html",
      "Achievement System": "achievement-system.html",
      "Event System": "event-system.html",
      FAQ: "faq.html",
      Troubleshooting: "troubleshooting.html",
      "Best Practices": "best-practices.html",
      "Custom Commands": "custom-commands.html",
      Download: "download.html",
      Forum: "forum.html",
      Donate: "donate.html",
      License: "license.html",
      "Last Commit": "last-commit.html",
      Template: "template.html",
      "Vercel Analytics": "vercel-web-analytics.html",
      "Modrinth Redirect": "redirects/modrinth.html",
      "Telemetry Redirect": "redirects/telementry.html",
    };

    // Create results container
    const results = document.createElement("div");
    results.id = "searchResults";
    results.className = "search-results";
    results.style.display = "none";

    // Insert after the search input
    searchBox.parentNode.style.position = "relative";
    searchBox.parentNode.appendChild(results);

    function renderResults(matches) {
      results.innerHTML = "";
      if (!matches || matches.length === 0) {
        results.style.display = "none";
        return;
      }

      matches.forEach((match) => {
        const item = document.createElement("div");
        item.className = "search-result-item";
        item.textContent = match.label;
        item.onclick = () => {
          window.location.href = match.url;
        };
        results.appendChild(item);
      });
      results.style.display = "block";
    }

    function findMatches(query) {
      if (!query) return [];
      const q = query.toLowerCase();
      const matches = [];
      for (const [label, file] of Object.entries(pages)) {
        const labelLower = label.toLowerCase();
        const fileLower = file.toLowerCase();
        if (labelLower.includes(q) || fileLower.includes(q)) {
          matches.push({ label, url: file });
        } else {
          // also allow splitting words
          const parts = labelLower.split(/\s+/);
          if (parts.some((p) => p.startsWith(q)))
            matches.push({ label, url: file });
        }
      }
      return matches.slice(0, 8);
    }

    // Show suggestions on input
    try {
      searchBox.addEventListener("input", function (e) {
        const q = e.target.value.trim();
        if (q.length === 0) {
          renderResults([]);
          return;
        }
        const matches = findMatches(q);
        renderResults(matches);
      });
    } catch (err) {
      console.warn("Failed to attach input handler for searchBox:", err);
    }

    // Handle keyboard navigation and enter
    try {
      searchBox.addEventListener("keydown", function (e) {
        const visible = results.style.display === "block";
        const items = Array.from(
          results.querySelectorAll(".search-result-item"),
        );
        const active = results.querySelector(".active");

        if (e.key === "ArrowDown" && visible) {
          e.preventDefault();
          if (!active && items.length) {
            items[0].classList.add("active");
          } else if (active) {
            const idx = items.indexOf(active);
            if (idx < items.length - 1) {
              active.classList.remove("active");
              items[idx + 1].classList.add("active");
            }
          }
        } else if (e.key === "ArrowUp" && visible) {
          e.preventDefault();
          if (active) {
            const idx = items.indexOf(active);
            active.classList.remove("active");
            if (idx > 0) items[idx - 1].classList.add("active");
          }
        } else if (e.key === "Enter") {
          const q = searchBox.value.trim();
          const matches = findMatches(q);
          if (visible && active) {
            active.click();
          } else if (matches.length === 1) {
            window.location.href = matches[0].url;
          } else if (matches.length > 0) {
            // go to the first match
            window.location.href = matches[0].url;
          }
        } else if (e.key === "Escape") {
          renderResults([]);
        }
      });
    } catch (err) {
      console.warn("Failed to attach keydown handler for searchBox:", err);
    }

    // Close when clicking outside
    try {
      document.addEventListener("click", function (ev) {
        try {
          if (!searchBox.contains(ev.target) && !results.contains(ev.target)) {
            renderResults([]);
          }
        } catch (inner) {
          // defensive: ignore errors coming from third-party content scripts
        }
      });
    } catch (err) {
      console.warn(
        "Failed to attach global click handler for search results:",
        err,
      );
    }
  }

  // Set active navigation link based on current page
  const currentPage = window.location.pathname.split("/").pop() || "index.html";
  document.querySelectorAll(".nav-link").forEach((link) => {
    const href = link.getAttribute("href");
    if (href === currentPage) {
      link.classList.add("active");
    } else {
      link.classList.remove("active");
    }
  });
});

// Smooth scrolling for anchor links
document.querySelectorAll('a[href^="#"]').forEach((anchor) => {
  anchor.addEventListener("click", function (e) {
    e.preventDefault();
    const target = document.querySelector(this.getAttribute("href"));
    if (target) {
      target.scrollIntoView({
        behavior: "smooth",
        block: "start",
      });
    }
  });
});

// Table of contents generation for long pages
function generateTableOfContents() {
  const headings = document.querySelectorAll("h2, h3");
  const toc = document.getElementById("table-of-contents");

  if (toc && headings.length > 0) {
    const list = document.createElement("ul");

    headings.forEach((heading, index) => {
      const id = heading.id || `heading-${index}`;
      heading.id = id;

      const li = document.createElement("li");
      const level = heading.tagName === "H2" ? 0 : 1;
      li.style.marginLeft = `${level * 20}px`;

      const a = document.createElement("a");
      a.href = `#${id}`;
      a.textContent = heading.textContent;

      li.appendChild(a);
      list.appendChild(li);
    });

    toc.appendChild(list);
  }
}

// Syntax highlighting for code blocks
function highlightCode() {
  document.querySelectorAll("pre code").forEach((block) => {
    // Simple highlighting - can be extended
    let text = block.textContent;

    // Highlight JSON
    if (block.className.includes("json")) {
      text = text.replace(/(".*?")\s*:/g, '<span class="json-key">$1</span>:');
      text = text.replace(
        /:\s*(".*?")/g,
        ': <span class="json-string">$1</span>',
      );
    }

    block.innerHTML = text;
  });
}

// Copy to clipboard for code blocks
function addCopyButtons() {
  document.querySelectorAll("pre").forEach((block) => {
    const button = document.createElement("button");
    button.className = "copy-button";
    button.textContent = "Copy";
    button.onclick = function () {
      const code = block.querySelector("code").textContent;
      navigator.clipboard.writeText(code).then(() => {
        button.textContent = "Copied!";
        setTimeout(() => {
          button.textContent = "Copy";
        }, 2000);
      });
    };
    block.appendChild(button);
  });
}

// Initialize page
window.addEventListener("load", function () {
  generateTableOfContents();
  highlightCode();
  addCopyButtons();
});

// Inject an "Edit" button in the top-right that opens the GitHub edit page for the current file
window.addEventListener("load", function () {
  try {
    // Determine the filename (fallback to index.html)
    const filename = window.location.pathname.split("/").pop() || "index.html";
    const branch = "26.x-fabric-quilt";
    const editUrl = `https://github.com/Diamondstar-Mods/Minecraft-Command-Maker/edit/${branch}/docs/${filename}`;

    const link = document.createElement("a");
    link.className = "edit-button";
    link.href = editUrl;
    link.target = "_blank";
    link.rel = "noopener noreferrer";
    link.textContent = "Edit";

    // Append to body so it floats over page content in the top-right
    document.body.appendChild(link);
  } catch (err) {
    // Fail silently but log to console for debugging
    console.error("Failed to inject edit button:", err);
  }
});
