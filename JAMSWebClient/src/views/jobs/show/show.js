import config from "../../../config";
import * as flashes from "../../../flashes";
import {formatDateTime, formatDuration} from "../../../date";

const flashIdLoadingErrorLogFailed = 1;
const flashIdLoadingInfoLogFailed = 2;
const flashIdLoadingJobFailed = 3;
const flashIdRemovingJobFailed = 4;
const flashIdStoppingJobFailed = 5;

export default {
	beforeDestroy() {
		this.clearIntervals();

		window.removeEventListener("online", this.getJob);
		window.removeEventListener("online", this.getErrorLog);
		window.removeEventListener("online", this.getInfoLog);
	},
	computed: {
		formattedStartTime: function() {
			return formatDateTime(this.job.startTime);
		},
		formattedDuration: function() {
			return formatDuration(this.duration);
		}
	},
	created() {
		this.getJob(true);
		this.getLog("error", true);
		this.getLog("info", true);

		this.jobIntervalId = setInterval(this.getJob, config.jobsInterval);
		this.errorLogIntervalId = setInterval(this.getErrorLog, config.jobsInterval);
		this.infoLogIntervalId = setInterval(this.getInfoLog, config.jobsInterval);

		window.addEventListener("online", this.getJob);
		window.addEventListener("online", this.getErrorLog);
		window.addEventListener("online", this.getInfoLog);
	},
	data() {
		return {
			// Job state
			duration: 0,
			isActive: false,
			job: null,
			jobIntervalId: 0,
			progress: 0,
			size: 0,

			// Logs
			errorLog: "",
			errorLogIntervalId: 0,
			infoLog: "",
			infoLogIntervalId: 0
		};
	},
	methods: {
		clearIntervals() {
			clearInterval(this.jobIntervalId);
			clearInterval(this.errorLogIntervalId);
			clearInterval(this.infoLogIntervalId);
		},

		getDownloadUrl(workspaceId) {
			return config.apiBaseUrl + "/workspace/download/" + workspaceId;
		},

		getJob(force = false) {
			if (!this.$store.state.isOnline) {
				return;
			}

			if (!force && !this.$store.state.isConnected) {
				return;
			}

			flashes.clear(flashIdLoadingJobFailed);
			const jobId = this.$route.params.id;
			const url = config.apiBaseUrl + "/job/" + jobId + "/state";

			this.$http.get(url).then((response) => {
				response.json().then((data) => {
					this.duration = data.duration;
					this.isActive = data.active;
					this.job = data.job;
					this.progress = data.progress;
					this.size = data.size;

					this.sort(this.job.workspace.WorkspaceFileAssociation);

					// Stop refreshing if the job completed
					if (this.job.PID === -1) {
						this.clearIntervals();
					}
				}, (response) => {
					console.error("jobs: Parsing JSON response failed:", response);
				});
			}, (response) => {
				flashes.error("Job info couldn’t be loaded", flashIdLoadingJobFailed);
			});
		},

		getErrorLog() {
			this.getLog("error");
		},

		getInfoLog() {
			this.getLog("info");
		},

		getLog(type, force = false) {
			if (!this.$store.state.isOnline) {
				return;
			}

			if (!force && !this.$store.state.isConnected) {
				return;
			}

			if (type !== "error" && type !== "info") {
				console.error("jobs show: unknown log type");
				return;
			}

			const flashId = type === "error" ? flashIdLoadingErrorLogFailed : flashIdLoadingInfoLogFailed;
			flashes.clear(flashId);

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
				flashes.error(logType + " log couldn’t be loaded", flashId);
			});
		},

		removeJob(jobId) {
			flashes.clear(flashIdRemovingJobFailed);
			const message = "Remove job?";

			if (!window.confirm(message)) {
				return;
			}

			const url = config.apiBaseUrl + "/job/" + jobId + "/delete";

			this.$http.get(url).then((response) => {
				this.$router.push({name: "jobs"});
			}, (response) => {
				flashes.error("Job couldn’t be removed", flashIdRemovingJobFailed);
			});
		},

		// sort sorts files alphabetically by file path.
		sort(files) {
			files.sort((a, b) => {
				if (a.path < b.path) {
					return -1;
				} else if (a.path > b.path) {
					return 1;
				}
				return 0;
			});
		},

		stopJob(jobId) {
			flashes.clear(flashIdStoppingJobFailed);
			const message = "Stop job?";

			if (!window.confirm(message)) {
				return;
			}

			const url = config.apiBaseUrl + "/job/" + jobId + "/kill";

			this.$http.get(url).then((response) => {
				response.json().then((data) => {
					console.debug(data);
					this.job.PID = -2;
				}, (response) => {
					console.error("jobs: Parsing JSON response failed:", response);
				});
			}, (response) => {
				flashes.error("Job couldn’t be stopped", flashIdStoppingJobFailed);
			});
		}
	}
};
