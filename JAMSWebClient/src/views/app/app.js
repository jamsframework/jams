import Vue from "vue";

import config from "../../config";

export default {
	beforeCreate() {
		// Allow cookies for requests to API (for cross-origin requests)
		Vue.http.options.credentials = true;

		// Intercept responses
		Vue.http.interceptors.push(function(request, next) {
			next(function(response) {
				this.$store.commit("setIsConnected", response.status !== 0);
			});
		});
	},
	created() {
		this.getVersion();
		setInterval(this.getVersion, config.connectionInterval);
	},
	methods: {
		// Get JAMS Cloud Server version
		getVersion() {
			const url = config.baseUrl + "/version";

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
			}, (response) => {
				console.error("app: Unexpected response:", response);
			});
		}
	}
};
