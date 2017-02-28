import config from "../../../config";
import date from "../../../date";

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
		const url = config.baseUrl + "/job/find";

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
