import config from "../../config";

export default {
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
