/**
 * @package OptimaSales
 * @subpackage OptimaSales HTML
 * @since OptimaSales HTML 1.0
 * 
 * Template Scripts
 * Created by Olechka
 * 
 */

$(function(){

	// ---------------------------------------------------------
	// Tabs
	// ---------------------------------------------------------
	$(".tabs").each(function(){

		$(this).find(".tab").hide();
		$(this).find(".tab-menu li:first a").addClass("active").show();
		$(this).find(".tab:first").show();

	});

	$(".tabs").each(function(){

		$(this).find(".tab-menu a").click(function() {

			$(this).parent().parent().find("a").removeClass("active");
			$(this).addClass("active");
			$(this).parent().parent().parent().parent().find(".tab").hide();
			var activeTab = $(this).attr("href");
			$(activeTab).fadeIn();
			return false;

		});

	});
	
	// ---------------------------------------------------------
	// Accordion (Toggle)
	// ---------------------------------------------------------

	(function() {
		var $container = $('.acc-body'),
			$acc_head   = $('.acc-head');

		$container.hide();
		$acc_head.first().addClass('active').next().show();
		$acc_head.last().addClass('last');
		
		$acc_head.on('click', function(e) {
			if( $(this).next().is(':hidden') ) {
				$acc_head.removeClass('active').next().slideUp(300);
				$(this).toggleClass('active').next().slideDown(300);
			}
			e.preventDefault();
		});

	})();
	/* END Accordion (Toggle) */


	// Misc
	$('.home-services li:nth-child(even), .services li:nth-child(even)').addClass('even');
	$('.tabs .tab-menu li:last-child').addClass("last-child");
	
	
	// Styleswitcher Panel
	$themePanel = $('#styleswitcher_panel');
	$theme_control_panel_label = $('#control_label');
	
	$theme_control_panel_label.click(function() {
		if ($themePanel.hasClass('visible')) {
			$themePanel.removeClass('visible');
			$themePanel.animate({left: -114}, 400);
		} else {
			$themePanel.addClass('visible');
			$themePanel.animate({left: 0}, 400);
		}
		return false;
	});
	
});