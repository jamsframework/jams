import config from "../../../config";
import * as flashes from "../../../flashes";
import {formatDateTime, formatDuration} from "../../../date";

export default {
	computed: {
		formattedStartTime: function() {
			return formatDateTime(this.job.startTime);
		},
		formattedDuration: function() {
			return formatDuration(this.duration);
		}
	},
	created() {
		this.getJob();
		this.getLog("error");
		this.getLog("info");
	},
	data() {
		return {
			// Job state
			duration: 0,
			isActive: false,
			job: null,
			progress: 1,
			size: 0,

			// Logs
			errorLog: "",
			infoLog: ""
		};
	},
	methods: {
		getDownloadUrl(workspaceId) {
			return config.apiBaseUrl + "/workspace/download/" + workspaceId;
		},
		getJob() {
			const jobId = this.$route.params.id;
			const url = config.apiBaseUrl + "/job/" + jobId + "/state";

			this.$http.get(url).then((response) => {
				response.json().then((data) => {
					this.duration = data.duration;
					this.isActive = data.active;
					this.job = data.job;
					this.progress = data.progress;
					this.size = data.size;
				}, (response) => {
					console.error("jobs: Parsing JSON response failed:", response);
				});
			}, (response) => {
				flashes.error("Job info couldn’t be loaded");
			});
		},
		getLog(type) {
			if (type !== "error" && type !== "info") {
				console.error("jobs show: unknown log type");
				return;
			}

			const jobId = this.$route.params.id;
			const url = config.apiBaseUrl + "/job/" + jobId + "/" + type + "log";

			this.$http.get(url).then((response) => {
				response.blob().then((data) => {
					const reader = new FileReader();
					reader.addEventListener("loadend", () => {
						if (type === "error") {
							this.errorLog = reader.result;
						} else if (type === "info") {
							this.infoLog = reader.result;
						}
					});
					reader.readAsText(data);
				});
			}, (response) => {
				const logType = type === "error" ? "Error" : "Info";
				flashes.error(logType + " log couldn’t be loaded");
			});
		},
		removeJob(jobId) {
			const message = "Remove job?";

			if (!window.confirm(message)) {
				return;
			}

			const url = config.apiBaseUrl + "/job/" + jobId + "/delete";

			this.$http.get(url).then((response) => {
				this.$router.push({name: "jobs"});
			}, (response) => {
				flashes.error("Job couldn’t be removed");
			});
		}
	}
};
