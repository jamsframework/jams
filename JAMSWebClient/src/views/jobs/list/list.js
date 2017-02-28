import date from "../../../date/date.js";

export default {
	data() {
		return {
			jobs: []
		};
	},
	methods: {
		formatDate: date.formatDate
	},
	mounted() {
		const url = "http://localhost:8080/jamscloud/webresources/job/find";

		const options = {
			credentials: true
		};

		this.$http.get(url, options).then((response) => {
			response.json().then((data) => {
				console.debug(response.data);
				this.jobs = data.jobs;
			}, (response) => {
				console.error("Jobs: Parsing JSON response failed:", response);
			});
		}, (response) => {
			console.error("Jobs: Unexpected response:", response);
		});
	}
};
