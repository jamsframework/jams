import config from "../../config";

export default {
	created() {
		this.connect();
		setInterval(this.connect, config.connectionInterval);
	},
	methods: {
		connect() {
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
					console.error("app: Parsing JSON response failed:", response);
				});
			}, (response) => {
				if (response.status === 0) {
					this.$store.commit("setIsConnected", false);
				}
				console.error("app: Unexpected response:", response);
			});
		}
	}
};
