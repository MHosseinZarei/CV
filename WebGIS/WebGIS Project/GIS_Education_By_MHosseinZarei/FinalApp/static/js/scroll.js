		function scrollToNewsletter() {
			var newsletterSection = document.querySelector('.newsletter-section');
			if (newsletterSection) {
				newsletterSection.scrollIntoView({ behavior: 'smooth' });

			}
		}
	
		window.addEventListener('DOMContentLoaded', function () {
			var url = window.location.href;
			if (url.includes('/rate_us/')) {
				scrollToNewsletter();
			}
		});