export default {
	created() {
		const url = "http://localhost:8080/jamscloud/webresources/version";

		const options = {
			credentials: true,
			headers: {
				"Accept": "text/plain"
			}
		};

		this.$http.get(url, options).then((response) => {
			// Parse response as JSON
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
