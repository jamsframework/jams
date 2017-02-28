export default {
	data() {
		return {
			workspaces: []
		};
	},
	methods: {
		formatDate(value) {
			let date = new Date(value);
			return date.getFullYear() + "-" +
				((date.getMonth() + 1) < 10 ? "0" : "") + (date.getMonth() + 1) + "-" +
				(date.getDate() < 10 ? "0" : "") + date.getDate() + " ";
		}
	},
	mounted() {
		const url = "http://localhost:8080/jamscloud/webresources/workspace/find";

		const options = {
			credentials: true
		};

		this.$http.get(url, options).then((response) => {
			response.json().then((data) => {
				console.debug(response.data);
				this.workspaces = data.workspaces;
			}, (response) => {
				console.error("Jobs: Parsing JSON response failed:", response);
			});
		}, (response) => {
			console.error("Jobs: Unexpected response:", response);
		});
	}
};
