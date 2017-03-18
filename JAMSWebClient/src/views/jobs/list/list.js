import config from "../../../config";
import * as flashes from "../../../flashes";
import {formatDateTime} from "../../../date";

export default {
	beforeDestroy() {
		clearInterval(this.jobsIntervalId);
		clearInterval(this.serverLoadIntervalId);
	},
	created() {
		this.getJobs();
		this.getServerLoad();

		this.jobsIntervalId = setInterval(this.getJobs, config.jobsInterval);
		this.serverLoadIntervalId = setInterval(this.getServerLoad, config.serverLoadInterval);
	},
	data() {
		return {
			jobs: [],
			jobsIntervalId: 0,
			serverLoad: -1,
			serverLoadIntervalId: 0
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
		removeJob(jobId) {
			const message = "Remove job?";

			if (!window.confirm(message)) {
				return;
			}

			const url = config.apiBaseUrl + "/job/" + jobId + "/delete";

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
		},
		stopJob(jobId) {
			const message = "Stop job?";

			if (!window.confirm(message)) {
				return;
			}

			const url = config.apiBaseUrl + "/job/" + jobId + "/kill";

			this.$http.get(url).then((response) => {
				response.json().then((data) => {
					for (let i = 0, length = this.jobs.length; i < length; i++) {
						if (this.jobs[i].id === data.job.id) {
							this.jobs[i].PID = -2;
							break;
						}
					}
				}, (response) => {
					console.error("jobs: Parsing JSON response failed:", response);
				});
			}, (response) => {
				flashes.error("Job couldn’t be stopped");
			});
		}
	}
};
