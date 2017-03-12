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
				this.$store.commit("flashes/add", {
					message: "Job list couldn’t be loaded",
					type: 1
				});
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
			});
		},
		removeJob(job) {
			const message = "Remove job?";

			if (!window.confirm(message)) {
				return;
			}

			const url = config.baseUrl + "/job/" + job.id + "/delete";

			this.$http.get(url).then((response) => {
				response.json().then((data) => {
					for (let i = 0; i < this.jobs.length; i++) {
						if (this.jobs[i].id === data.id) {
							this.jobs.splice(i, 1);
							break;
						}
					}
				}, (response) => {
					console.error("jobs: Parsing JSON response failed:", response);
				});
			}, (response) => {
				console.debug(response);
				this.$store.commit("flashes/add", {
					message: "Job couldn’t be removed",
					type: 1
				});
			});
		}
	},
	mounted() {
		this.getJobs();
		this.getServerLoad();

		this.intervalId = setInterval(this.getServerLoad, config.serverLoadInterval);
	}
};
