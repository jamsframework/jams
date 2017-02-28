import config from "../../config";

export default {
	created() {
		const url = config.baseUrl + "/version";

		const options = {
			credentials: true,
			headers: {
				"Accept": "text/plain"
			}
		};

		this.$http.get(url, options).then((response) => {
			response.text().then((data) => {
				this.$store.commit("setJamsCloudServerVersion", data);
			}, (response) => {
				console.error("app: Parsing JSON response failed:", response);
			});
		}, (response) => {
			console.error("app: Unexpected response:", response);
		});
	}
};
