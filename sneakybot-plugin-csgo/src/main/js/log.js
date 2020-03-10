module.exports = function(prefix, msg) {
	if (process.argv.includes("-v"))
		console.log("[" + prefix + "] " + msg)
}
