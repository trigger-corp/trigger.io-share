/* global forge */

forge.share = {
	item: function (content, targets, success, error) {
		if (typeof targets === "function") {
			error = success;
			success = targets;
			targets = [];
		}

		// sanity check target selection
		targets = targets.filter(function (target) {
			if (forge.share.targets.indexOf(target) === -1) {
				forge.logging.error("The share target: '" + target + "' is not a valid target. See module documentation for a list of valid targets.");
				return false;
			}
			return true;
		});

		// invert target selection to define exclusion set
		var exclusions = (targets.length === 0) ? [] :
			forge.share.targets.filter(function (target) {
				console.log(target + ": " + targets.indexOf(targets));
				return targets.indexOf(target) === -1;
			});

		forge.internal.call("share.item", {content: content, exclusions: exclusions}, success, error);
	},

	targets: [
		"PostToFacebook",
		"PostToTwitter",
		"PostToWeibo",
		"Message",
		"Mail",
		"Print",
		"CopyToPasteboard",
		"AssignToContact",
		"SaveToCameraRoll",
		"AddToReadingList",
		"PostToFlickr",
		"PostToVimeo",
		"PostToTencentWeibo",
		"AirDrop",
		"OpenInIBooks"
	]
};
