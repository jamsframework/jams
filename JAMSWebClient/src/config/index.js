export default {
	apiBaseUrl: process.env.NODE_ENV === "production"
		? "http://worf.geogr.uni-jena.de:80/jamscloud/webresources"
		: "http://localhost:8080/jamscloud/webresources",

	// Interval (in milliseconds) between checking jobs
	jobsInterval: 5 * 1000,

	// Interval (in milliseconds) between checking connection to JAMS Cloud Server
	pingInterval: 30 * 1000,

	// Interval (in milliseconds) between checking server load
	serverLoadInterval: 10 * 1000,

	// Interval (in milliseconds) between checking workspaces
	workspacesInterval: 10 * 1000
};
