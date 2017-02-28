import config from "../../../config";
import {formatDateTime} from "../../../date";

export default {
	beforeDestroy() {
		clearInterval(this.intervalId);
	},
	data() {
		return {
			intervalId: 0,
			jobs: [],
			serverLoad: -1
		};
	},
	methods: {
		formatDateTime,
		getJobs() {
			const url = config.baseUrl + "/job/find";

			this.$http.get(url).then((response) => {
				response.json().then((data) => {
					this.jobs = data.jobs;
				}, (response) => {
					console.error("jobs: Parsing JSON response failed:", response);
				});
			}, (response) => {
				console.error("jobs: Unexpected response:", response);
			});
		},
		getServerLoad() {
			const url = config.baseUrl + "/job/load";

			this.$http.get(url).then((response) => {
				response.json().then((data) => {
					this.serverLoad = data;
				}, (response) => {
					console.error("jobs: Parsing JSON response failed:", response);
				});
			}, (response) => {
				console.error("jobs: Unexpected response:", response);
			});
		}
	},
	mounted() {
		this.getJobs();
		this.getServerLoad();

		this.intervalId = setInterval(this.getServerLoad, config.serverLoadInterval);
	}
};
