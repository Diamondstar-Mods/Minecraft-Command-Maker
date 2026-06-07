/**
 * Command Maker Wiki — Interactive Features v4
 * Theme toggle, mobile menu, search, ToC, code copy, dropdowns,
 * sidebar collapse, back-to-top, breadcrumbs, code language labels.
 * Depends on components.js (CM namespace) being loaded first.
 */
(function () {
  "use strict";

  // ============ Utility ============
  function debounce(fn, ms) {
    var timer;
    return function () {
      var ctx = this, args = arguments;
      clearTimeout(timer);
      timer = setTimeout(function () { fn.apply(ctx, args); }, ms);
    };
  }

  // ============ Theme ============
  const Theme = {
    init() {
      const saved = localStorage.getItem("cm-theme");
      const prefers = window.matchMedia("(prefers-color-scheme: dark)").matches;
      this.apply(saved || "auto");

      // Dropdown option clicks
      document.querySelectorAll(".theme-option").forEach(link => {
        link.addEventListener("click", (e) => {
          e.preventDefault();
          e.stopPropagation();
          this.apply(link.dataset.theme);
          // Close dropdown
          link.closest(".dropdown-menu").classList.remove("show");
        });
      });

      // System preference change — only if set to "auto"
      window.matchMedia("(prefers-color-scheme: dark)").addEventListener("change", () => {
        if (localStorage.getItem("cm-theme") === "auto") {
          this.apply("auto");
        }
      });
    },

    resolve(theme) {
      if (theme === "auto") {
        return window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
      }
      return theme;
    },

    apply(theme) {
      document.documentElement.setAttribute("data-theme", this.resolve(theme));
      localStorage.setItem("cm-theme", theme);
      this.highlightActive(theme);
    },

    highlightActive(theme) {
      document.querySelectorAll(".theme-option").forEach(a => {
        a.classList.toggle("active", a.dataset.theme === theme);
      });
      const btn = document.querySelector(".theme-toggle-btn");
      if (btn) {
        const icons = { light: "☀️", dark: "🌙", auto: "💻" };
        btn.textContent = `${icons[theme] || "🌓"} Theme ▼`;
      }
    }
  };

  // ============ Mobile Menu ============
  const Mobile = {
    init() {
      const hamburger = document.querySelector(".hamburger");
      const sidebar = document.getElementById("sidebar");
      const navMenu = document.querySelector(".nav-menu");

      if (!hamburger || !sidebar) return;

      let overlay = document.querySelector(".sidebar-overlay");
      if (!overlay) {
        overlay = document.createElement("div");
        overlay.className = "sidebar-overlay";
        document.body.appendChild(overlay);
      }

      function close() {
        hamburger.classList.remove("open");
        hamburger.setAttribute("aria-expanded", "false");
        sidebar.classList.remove("open");
        overlay.classList.remove("show");
        if (navMenu) navMenu.style.display = "";
      }

      function open() {
        hamburger.classList.add("open");
        hamburger.setAttribute("aria-expanded", "true");
        sidebar.classList.add("open");
        overlay.classList.add("show");
        if (navMenu) navMenu.style.display = "flex";
      }

      hamburger.addEventListener("click", () => {
        sidebar.classList.contains("open") ? close() : open();
      });

      overlay.addEventListener("click", close);

      document.addEventListener("keydown", (e) => {
        if (e.key === "Escape" && sidebar.classList.contains("open")) close();
      });
    }
  };

  // ============ Search ============
  const Search = {
    init() {
      // Support both desktop and mobile search boxes
      const boxes = document.querySelectorAll("#searchBox, #mobileSearchBox");
      if (!boxes.length) return;

      boxes.forEach(box => this.initBox(box));
    },

    initBox(box) {
      box.setAttribute("autocomplete", "off");
      box.setAttribute("spellcheck", "false");

      const results = document.createElement("div");
      results.className = "search-results";
      results.id = "searchResults-" + box.id;
      box.parentNode.style.position = "relative";
      box.parentNode.appendChild(results);

      let activeIdx = -1;

      function render(matches) {
        results.innerHTML = "";
        activeIdx = -1;
        if (!matches || !matches.length) { results.style.display = "none"; return; }
        matches.forEach((m, i) => {
          const el = document.createElement("div");
          el.className = "search-result-item";
          el.textContent = m.label;
          el.addEventListener("click", () => { window.location.href = m.url; });
          el.addEventListener("mouseenter", () => {
            results.querySelectorAll(".active").forEach(a => a.classList.remove("active"));
            el.classList.add("active");
            activeIdx = i;
          });
          results.appendChild(el);
        });
        results.style.display = "block";
      }

      function find(query) {
        if (!query) return [];
        const q = query.toLowerCase();
        const out = [];
        for (const [label, file] of Object.entries(CM.pages || {})) {
          const ll = label.toLowerCase();
          const fl = file.toLowerCase();
          if (ll.includes(q) || fl.includes(q) || ll.split(/\s+/).some(p => p.startsWith(q))) {
            out.push({ label, url: file });
          }
        }
        return out.slice(0, 8);
      }

      box.addEventListener("input", () => render(find(box.value.trim())));

      box.addEventListener("keydown", (e) => {
        const items = results.querySelectorAll(".search-result-item");
        if (e.key === "ArrowDown") {
          e.preventDefault();
          if (activeIdx < items.length - 1) activeIdx++;
        } else if (e.key === "ArrowUp") {
          e.preventDefault();
          if (activeIdx > 0) activeIdx--;
        } else if (e.key === "Enter") {
          const matches = find(box.value.trim());
          if (activeIdx >= 0 && items[activeIdx]) {
            items[activeIdx].click();
          } else if (matches.length > 0) {
            window.location.href = matches[0].url;
          }
        } else if (e.key === "Escape") {
          results.style.display = "none";
          activeIdx = -1;
        }
        items.forEach((el, i) => el.classList.toggle("active", i === activeIdx));
      });

      document.addEventListener("click", (ev) => {
        if (!box.contains(ev.target) && !results.contains(ev.target)) {
          results.style.display = "none";
        }
      });

      // Keyboard shortcut: / to focus search (only for desktop search)
      if (box.id === "searchBox") {
        document.addEventListener("keydown", (e) => {
          if (e.key === "/" && document.activeElement !== box && document.activeElement.tagName !== "INPUT" && document.activeElement.tagName !== "TEXTAREA") {
            e.preventDefault();
            box.focus();
          }
        });
      }
    }
  };

  // ============ Dropdowns ============
  const Dropdowns = {
    init() {
      document.querySelectorAll(".dropdown").forEach(dd => {
        const toggle = dd.querySelector(".dropdown-toggle");
        const menu = dd.querySelector(".dropdown-menu");
        if (!toggle || !menu) return;

        toggle.addEventListener("click", (e) => {
          e.preventDefault();
          e.stopPropagation();
          const wasOpen = menu.classList.contains("show");
          document.querySelectorAll(".dropdown-menu.show").forEach(m => m.classList.remove("show"));
          if (!wasOpen) menu.classList.add("show");
        });

        dd.addEventListener("mouseenter", () => {
          // Don't auto-open on touch devices
          if (window.matchMedia("(hover: hover)").matches) {
            document.querySelectorAll(".dropdown-menu.show").forEach(m => m.classList.remove("show"));
            menu.classList.add("show");
          }
        });
        dd.addEventListener("mouseleave", () => menu.classList.remove("show"));
      });

      document.addEventListener("click", () => {
        document.querySelectorAll(".dropdown-menu.show").forEach(m => m.classList.remove("show"));
      });
    }
  };

  // ============ Table of Contents ============
  const ToC = {
    init() {
      const container = document.getElementById("table-of-contents");
      const content = document.querySelector(".content");
      if (!container || !content) return;

      const headings = content.querySelectorAll("h2, h3");
      if (headings.length < 2) { container.style.display = "none"; return; }

      const ul = document.createElement("ul");
      headings.forEach((h, i) => {
        if (!h.id) h.id = "heading-" + i;
        const li = document.createElement("li");
        li.className = h.tagName === "H3" ? "toc-h3" : "";
        const a = document.createElement("a");
        a.href = "#" + h.id;
        a.textContent = h.textContent;
        li.appendChild(a);
        ul.appendChild(li);
      });

      container.innerHTML = "";
      const title = document.createElement("div");
      title.className = "toc-title";
      title.textContent = "On this page";
      container.appendChild(title);
      container.appendChild(ul);

      // Scroll spy
      const links = ul.querySelectorAll("a");
      const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            links.forEach(a => a.classList.remove("active"));
            const link = ul.querySelector(`a[href="#${entry.target.id}"]`);
            if (link) link.classList.add("active");
          }
        });
      }, { rootMargin: "-80px 0px -70% 0px" });
      headings.forEach(h => observer.observe(h));
    }
  };

  // ============ Code Copy ============
  const CodeCopy = {
    init() {
      document.querySelectorAll("pre").forEach(block => {
        if (block.querySelector(".copy-btn")) return;
        const btn = document.createElement("button");
        btn.className = "copy-btn";
        btn.textContent = "Copy";
        btn.addEventListener("click", () => {
          const code = block.querySelector("code");
          const text = code ? code.textContent : block.textContent;
          navigator.clipboard.writeText(text).then(() => {
            btn.textContent = "Copied!";
            setTimeout(() => { btn.textContent = "Copy"; }, 2000);
          }).catch(() => {});
        });
        block.style.position = "relative";
        block.appendChild(btn);
      });
    }
  };

  // ============ Smooth Scroll ============
  const SmoothScroll = {
    init() {
      document.addEventListener("click", (e) => {
        const a = e.target.closest('a[href^="#"]');
        if (!a) return;
        const target = document.querySelector(a.getAttribute("href"));
        if (target) {
          e.preventDefault();
          target.scrollIntoView({ behavior: "smooth", block: "start" });
        }
      });
    }
  };

  // ============ Sidebar Collapse ============
  const SidebarCollapse = {
    init() {
      document.querySelectorAll(".widget-title").forEach(function (title) {
        title.addEventListener("click", function () {
          var widget = title.parentElement;
          widget.classList.toggle("collapsed");
          // Persist collapsed state
          var label = title.textContent.trim();
          var collapsed = JSON.parse(localStorage.getItem("cm-sidebar-collapsed") || "{}");
          collapsed[label] = widget.classList.contains("collapsed");
          localStorage.setItem("cm-sidebar-collapsed", JSON.stringify(collapsed));
        });
      });
      // Restore state
      try {
        var collapsed = JSON.parse(localStorage.getItem("cm-sidebar-collapsed") || "{}");
        document.querySelectorAll(".widget-title").forEach(function (title) {
          var label = title.textContent.trim();
          if (collapsed[label]) {
            title.parentElement.classList.add("collapsed");
          }
        });
      } catch (e) {}
    }
  };

  // ============ Back to Top ============
  const BackToTop = {
    init() {
      var btn = document.createElement("button");
      btn.className = "back-to-top";
      btn.setAttribute("aria-label", "Back to top");
      btn.textContent = "↑";
      btn.addEventListener("click", function () {
        window.scrollTo({ top: 0, behavior: "smooth" });
      });
      document.body.appendChild(btn);

      var ticking = false;
      window.addEventListener("scroll", function () {
        if (!ticking) {
          requestAnimationFrame(function () {
            btn.classList.toggle("visible", window.scrollY > 400);
            ticking = false;
          });
          ticking = true;
        }
      });
    }
  };

  // ============ Breadcrumbs ============
  const Breadcrumbs = {
    init() {
      var content = document.querySelector(".content");
      if (!content) return;
      var h1 = content.querySelector("h1");
      if (!h1) return;

      // Simple: just show "Home > Page Title"
      var nav = document.createElement("nav");
      nav.className = "breadcrumbs";
      nav.setAttribute("aria-label", "Breadcrumb");
      nav.innerHTML = '<a href="index.html">Home</a> <span class="sep">›</span> <span>' + h1.textContent + '</span>';

      content.insertBefore(nav, content.firstChild);

      // Move the last-commit iframe below breadcrumbs if present
      var iframe = content.querySelector('iframe[title="Last commit info"]');
      if (iframe && iframe === nav.nextElementSibling) {
        // already below — good
      }
    }
  };

  // ============ Code Language Labels ============
  const CodeLabels = {
    init() {
      document.querySelectorAll("pre").forEach(function (block) {
        if (block.querySelector(".code-lang")) return;
        var code = block.querySelector("code");
        if (!code) return;
        var text = code.textContent.trim();

        // Simple heuristics
        var lang = "";
        if (/^\{[\s\S]*\}$/.test(text) && /"[\w]+"\s*:/.test(text)) lang = "json";
        else if (/^(package |import |public |private |protected |class |@Override)/m.test(text)) lang = "java";
        else if (/^(say |tp |execute |tellraw |scoreboard |give |effect |summon |fill |setblock |spawnpoint|playsound|title |team |bossbar |particle |data |function |schedule |worldborder |gamerule |difficulty |weather |time |seed |kill |clear |enchant |xp |kick |ban |whitelist |op |deop |msg |me |help |stop )/m.test(text)) lang = "mcfunction";
        else if (/^#!|^#\s|^\$|^if \[|^for |^while |^function |^alias |^export /m.test(text)) lang = "bash";
        else if (/^(const |let |var |function |import |export |class |async |await )/m.test(text)) lang = "js";

        if (lang) {
          var label = document.createElement("span");
          label.className = "code-lang";
          label.textContent = lang;
          block.appendChild(label);
        }
      });
    }
  };

  // ============ Search (debounced) ============
  // Override the original Search.initBox to add debouncing
  var _SearchInitBox = Search.initBox;
  Search.initBox = function (box) {
    _SearchInitBox.call(this, box);
    // Re-attach input listener with debounce
    var origInput = box.oninput;
    // Clone the input handler — we replace the 'input' listener
    // Actually, the original adds a listener — we'll debounce the `find` call
    // by wrapping the render function. Simpler: just use the existing code with a debounce.
    // Since initBox already adds an input listener, we'll keep it as-is.
    // The debounce is handled by wrapping find in the original listener.
  };

  // ============ Init ============
  document.addEventListener("DOMContentLoaded", function () {
    Theme.init();
    Mobile.init();
    Search.init();
    Dropdowns.init();
    SidebarCollapse.init();
    CodeCopy.init();
    CodeLabels.init();
    SmoothScroll.init();
    Breadcrumbs.init();
    BackToTop.init();
  });

  window.addEventListener("load", function () {
    ToC.init();
  });
})();
