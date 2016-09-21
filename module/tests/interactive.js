/* global module, forge, asyncTest, askQuestion, ok, start */

module("share");

asyncTest("Attempt to share a text message", 1, function() {
	forge.share.item({ text: "This is a text share" }, function () {
		askQuestion("Were you able to share a text item?", {
			Yes: function () {
				ok(true, "User claims success");
				start();
			},
			No: function () {
				ok(false, "User claims failure");
				start();
			}
		});
	}, function () {
		ok(false, "API method returned failure");
		start();
	});
});

asyncTest("Attempt to share a text message with subject", 1, function() {
	forge.share.item({
		text: "This is a text share",
		subject: "This is the subject"
	}, function () {
		askQuestion("Were you able to share a text item with a subject ?", {
			Yes: function () {
				ok(true, "User claims success");
				start();
			},
			No: function () {
				ok(false, "User claims failure");
				start();
			}
		});
	}, function () {
		ok(false, "API method returned failure");
		start();
	});
});

asyncTest("Attempt to share a text message with subject via Mail", 1, function() {
	forge.share.item({
		text:    "This is a text share",
		subject: "This is the subject"
	}, ["Mail"], function () {
		askQuestion("Were you able to share a text item with a subject via Mail ?", {
			Yes: function () {
				ok(true, "User claims success");
				start();
			},
			No: function () {
				ok(false, "User claims failure");
				start();
			}
		});
	}, function () {
		ok(false, "API method returned failure");
		start();
	});
});

asyncTest("Attempt to share an url", 1, function() {
	forge.share.item({ url: "https://trigger.io" }, ["PostToFacebook", "PostToTwitter"], function () {
		askQuestion("Were you able to share an URL via Twitter and Facebook?", {
			Yes: function () {
				ok(true, "User claims success");
				start();
			},
			No: function () {
				ok(false, "User claims failure");
				start();
			}
		});
	}, function () {
		ok(false, "API method returned failure");
		start();
	});
});

asyncTest("Attempt to share an image", 1, function() {
	forge.share.item({ image: "https://trigger.io/forge-static/img/trigger-light/trigger-io-command-line.jpg" }, function () {
		askQuestion("Were you able to share an image?", {
			Yes: function () {
				ok(true, "User claims success");
				start();
			},
			No: function () {
				ok(false, "User claims failure");
				start();
			}
		});
	}, function () {
		ok(false, "API method returned failure");
		start();
	});
});

asyncTest("Attempt to share an image, text, subject and URL", 1, function() {
	forge.share.item({
		image: "https://trigger.io/forge-static/img/trigger-light/trigger-io-command-line.jpg",
		text: "Trigger.IO is the simplest way to build amazing mobile apps",
		subject: "Trigger.IO",
		url: "https://trigger.io"
	}, function () {
		askQuestion("Were you able to share an image, text, subject and URL?", {
			Yes: function () {
				ok(true, "User claims success");
				start();
			},
			No: function () {
				ok(false, "User claims failure");
				start();
			}
		});
	}, function () {
		ok(false, "API method returned failure");
		start();
	});
});
