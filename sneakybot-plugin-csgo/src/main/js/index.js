const csgo = require("./csgo.js")


const http = require('http')
const port = 7300

const requestHandler = (request, response) => {
	console.log("Serving " + request.url)

	switch (request.url) {
	case "/ranks":
		response.end(JSON.stringify(csgo.ranks))
		break;

	default:
		response.statusCode = 404
		response.end()
	}
}

const server = http.createServer(requestHandler)

server.listen(port, (err) => {
	if (err)
		return console.log("An error occurred:", err)

	console.log("Server is listening on port " + port)

	csgo.start()
})
