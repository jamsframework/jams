export default {
	apiBaseUrl: process.env.NODE_ENV === "production"
		? "http://worf.geogr.uni-jena.de:8080/jamscloud/webresources"
		: "http://localhost:8080/jamscloud/webresources",

	// Interval (in milliseconds) to check connection to JAMS Cloud Server
	pingInterval: 30 * 1000,

	// Interval (in milliseconds) to check server load
	serverLoadInterval: 10 * 1000
};
