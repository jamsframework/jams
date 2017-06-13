import config from "../../config";

export default {
	created() {
		this.getVersion(true);
		this.ping();
		this.watchOnlineStatus();
	},
	methods: {
		// getVersion retrieves the JAMS Cloud Server version.
		getVersion(force = false) {
			if (!this.$store.state.isOnline && !force) {
				return;
			}

			const url = config.apiBaseUrl + "/version";

			const options = {
				headers: {
					"Accept": "text/plain"
				}
			};

			this.$http.get(url, options).then((response) => {
				response.text().then((data) => {
					this.$store.commit("setIsConnected", true);
					this.$store.commit("setJamsCloudServerVersion", data);
				}, (response) => {
					console.error("app: Parsing text response failed:", response);
				});
			});
		},

		// ping periodically sends a request to the server to see if it is still
		// reachable.
		ping() {
			let delay = config.pingInterval - (Date.now() - this.$store.state.dateLastRequest);

			if (delay <= 0) {
				this.getVersion();
				delay = config.pingInterval;
			}

			setTimeout(this.ping, delay);
		},

		// watchOnlineStatus observes the online status as reported by the
		// browser.
		watchOnlineStatus() {
			this.$store.commit("setIsOnline", navigator.onLine);

			window.addEventListener("offline", () => {
				this.$store.commit("setIsOnline", false);
			});

			window.addEventListener("online", () => {
				this.$store.commit("setIsOnline", true);
			});
		}
	}
};
