document.addEventListener('DOMContentLoaded', function() {
    // 1. Dropdown Logic
    const dropdownToggles = document.querySelectorAll('.dropdown-toggle');

    dropdownToggles.forEach(toggle => {
        toggle.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            
            const menu = this.nextElementSibling;
            const isHidden = menu.classList.contains('hidden');

            // Close all other dropdowns
            document.querySelectorAll('.dropdown-menu').forEach(dm => {
                if (dm !== menu) dm.classList.add('hidden');
            });

            // Toggle current
            if (isHidden) {
                menu.classList.remove('hidden');
            } else {
                menu.classList.add('hidden');
            }
        });
    });

    // Close dropdowns when clicking outside
    document.addEventListener('click', function() {
        document.querySelectorAll('.dropdown-menu').forEach(menu => {
            menu.classList.add('hidden');
        });
    });

    // 2. Dark Mode Logic
    const themeToggleBtn = document.getElementById('theme-toggle');
    const html = document.documentElement;
    
    // Check local storage or system preference
    if (localStorage.theme === 'dark' || (!('theme' in localStorage) && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
        html.classList.add('dark');
        updateToggleUI(true);
    } else {
        html.classList.remove('dark');
        updateToggleUI(false);
    }

    if (themeToggleBtn) {
        themeToggleBtn.addEventListener('click', function() {
            if (html.classList.contains('dark')) {
                html.classList.remove('dark');
                localStorage.theme = 'light';
                updateToggleUI(false);
            } else {
                html.classList.add('dark');
                localStorage.theme = 'dark';
                updateToggleUI(true);
            }
        });
    }

    function updateToggleUI(isDark) {
        const btn = document.getElementById('theme-toggle');
        if (!btn) return;
        
        // Aria-checked
        btn.setAttribute('aria-checked', isDark);
        
        // Knob position is handled by CSS classes in top.html (translate-x-0 vs dark:translate-x-5)
        // Since we are toggling 'dark' class on html, the Tailwind classes on the elements will react automatically.
        // We don't need manual JS style manipulation if Tailwind 'dark:' prefix is used correctly on the toggle elements.
        // The previous code I injected in top.html uses 'dark:translate-x-5', so it reacts to the html class.
    }

    // 3. Mobile Menu Logic
    const mobileMenuBtn = document.getElementById('mobile-menu-btn');
    const mobileMenu = document.getElementById('mobile-menu');
    const menuIconOpen = document.getElementById('menu-icon-open');
    const menuIconClose = document.getElementById('menu-icon-close');

    if (mobileMenuBtn && mobileMenu) {
        mobileMenuBtn.addEventListener('click', function() {
            const isExpanded = mobileMenuBtn.getAttribute('aria-expanded') === 'true';
            mobileMenuBtn.setAttribute('aria-expanded', !isExpanded);
            
            if (isExpanded) {
                // Close menu
                mobileMenu.classList.add('hidden');
                menuIconOpen.classList.remove('hidden');
                menuIconClose.classList.add('hidden');
            } else {
                // Open menu
                mobileMenu.classList.remove('hidden');
                menuIconOpen.classList.add('hidden');
                menuIconClose.classList.remove('hidden');
            }
        });
    }

    // Mobile Dropdown Logic
    const mobileDropdownToggles = document.querySelectorAll('.mobile-dropdown-toggle');
    mobileDropdownToggles.forEach(toggle => {
        toggle.addEventListener('click', function() {
            const content = this.nextElementSibling;
            const icon = this.querySelector('svg');
            
            if (content.classList.contains('hidden')) {
                content.classList.remove('hidden');
                icon.classList.add('rotate-180');
                this.classList.add('border-stone-300', 'dark:border-stone-600', 'bg-stone-50', 'dark:bg-stone-800');
                this.classList.remove('border-transparent');
            } else {
                content.classList.add('hidden');
                icon.classList.remove('rotate-180');
                this.classList.remove('border-stone-300', 'dark:border-stone-600', 'bg-stone-50', 'dark:bg-stone-800');
                this.classList.add('border-transparent');
            }
        });
    });
});
