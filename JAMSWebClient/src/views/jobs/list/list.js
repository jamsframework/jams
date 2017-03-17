import config from "../../../config";
import * as flashes from "../../../flashes";
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
		getDownloadUrl(workspaceId) {
			return config.apiBaseUrl + "/workspace/download/" + workspaceId;
		},
		formatDateTime,
		getJobs() {
			const url = config.apiBaseUrl + "/job/find";

			this.$http.get(url).then((response) => {
				response.json().then((data) => {
					this.jobs = data.jobs;
				}, (response) => {
					console.error("jobs: Parsing JSON response failed:", response);
				});
			}, (response) => {
				flashes.error("Job list couldn’t be loaded");
			});
		},
		getServerLoad() {
			const url = config.apiBaseUrl + "/job/load";

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

			const url = config.apiBaseUrl + "/job/" + job.id + "/delete";

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
				flashes.error("Job couldn’t be removed");
			});
		}
	},
	mounted() {
		this.getJobs();
		this.getServerLoad();

		this.intervalId = setInterval(this.getServerLoad, config.serverLoadInterval);
	}
};
