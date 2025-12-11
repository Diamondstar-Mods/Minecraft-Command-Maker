// Search functionality
document.addEventListener('DOMContentLoaded', function() {
    const searchBox = document.getElementById('searchBox');
    
    if (searchBox) {
        searchBox.addEventListener('input', function(e) {
            const query = e.target.value.toLowerCase();
            
            // Simple search - in production this would be more sophisticated
            const pages = {
                'getting started': 'getting-started.html',
                'installation': 'installation.html',
                'configuration': 'configuration.html',
                'aliases': 'aliases.html',
                'syntax': 'syntax-system.html',
                'variables': 'variables.html',
                'gui': 'gui-system.html',
                'chat': 'chat-messages.html',
                'tpa': 'tpa-system.html',
                'ban': 'ban-system.html',
                'warp': 'warp-system.html',
                'commands': 'commands.html',
                'examples': 'examples.html',
                'faq': 'faq.html',
                'troubleshooting': 'troubleshooting.html',
            };
            
            if (query.length > 0) {
                for (let [term, url] of Object.entries(pages)) {
                    if (term.includes(query)) {
                        console.log(`Found match: ${term}`);
                        // Could redirect or show results here
                    }
                }
            }
        });
    }

    // Set active navigation link based on current page
    const currentPage = window.location.pathname.split('/').pop() || 'index.html';
    document.querySelectorAll('.nav-link').forEach(link => {
        const href = link.getAttribute('href');
        if (href === currentPage) {
            link.classList.add('active');
        } else {
            link.classList.remove('active');
        }
    });
});

// Smooth scrolling for anchor links
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        const target = document.querySelector(this.getAttribute('href'));
        if (target) {
            target.scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });
        }
    });
});

// Table of contents generation for long pages
function generateTableOfContents() {
    const headings = document.querySelectorAll('h2, h3');
    const toc = document.getElementById('table-of-contents');
    
    if (toc && headings.length > 0) {
        const list = document.createElement('ul');
        
        headings.forEach((heading, index) => {
            const id = heading.id || `heading-${index}`;
            heading.id = id;
            
            const li = document.createElement('li');
            const level = heading.tagName === 'H2' ? 0 : 1;
            li.style.marginLeft = `${level * 20}px`;
            
            const a = document.createElement('a');
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
    document.querySelectorAll('pre code').forEach(block => {
        // Simple highlighting - can be extended
        let text = block.textContent;
        
        // Highlight JSON
        if (block.className.includes('json')) {
            text = text.replace(/(".*?")\s*:/g, '<span class="json-key">$1</span>:');
            text = text.replace(/:\s*(".*?")/g, ': <span class="json-string">$1</span>');
        }
        
        block.innerHTML = text;
    });
}

// Copy to clipboard for code blocks
function addCopyButtons() {
    document.querySelectorAll('pre').forEach(block => {
        const button = document.createElement('button');
        button.className = 'copy-button';
        button.textContent = 'Copy';
        button.onclick = function() {
            const code = block.querySelector('code').textContent;
            navigator.clipboard.writeText(code).then(() => {
                button.textContent = 'Copied!';
                setTimeout(() => {
                    button.textContent = 'Copy';
                }, 2000);
            });
        };
        block.appendChild(button);
    });
}

// Initialize page
window.addEventListener('load', function() {
    generateTableOfContents();
    highlightCode();
    addCopyButtons();
});

// Inject an "Edit" button in the top-right that opens the GitHub edit page for the current file
window.addEventListener('load', function() {
    try {
        // Determine the filename (fallback to index.html)
        const filename = (window.location.pathname.split('/').pop() || 'index.html');
        const branch = '1.21.10';
        const editUrl = `https://github.com/Diamondstar-Mods/Minecraft-Command-Maker/edit/${branch}/docs/${filename}`;

        const link = document.createElement('a');
        link.className = 'edit-button';
        link.href = editUrl;
        link.target = '_blank';
        link.rel = 'noopener noreferrer';
        link.textContent = 'Edit';

        // Append to body so it floats over page content in the top-right
        document.body.appendChild(link);
    } catch (err) {
        // Fail silently but log to console for debugging
        console.error('Failed to inject edit button:', err);
    }
});
