import config from "../../../config";
import * as flashes from "../../../flashes";
import {formatDateTime, formatDuration} from "../../../date";

const flashIdLoadingErrorLogFailed = 1;
const flashIdLoadingInfoLogFailed = 2;
const flashIdLoadingJobFailed = 3;
const flashIdRemovingJobFailed = 4;
const flashIdStoppingJobFailed = 5;

const ERROR_LOG = "error";
const INFO_LOG = "info";
const JOB = "job";

export default {
	beforeDestroy() {
		if (this.requests[JOB]) {
			this.requests[JOB].abort();
		}

		if (this.requests[ERROR_LOG]) {
			this.requests[ERROR_LOG].abort();
		}

		if (this.requests[INFO_LOG]) {
			this.requests[INFO_LOG].abort();
		}

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
		this.getLog(ERROR_LOG, true);
		this.getLog(INFO_LOG, true);

		this.intervalIds[ERROR_LOG] = setInterval(this.getErrorLog, config.jobsInterval);
		this.intervalIds[INFO_LOG] = setInterval(this.getInfoLog, config.jobsInterval);
		this.intervalIds[JOB] = setInterval(this.getJob, config.jobsInterval);

		window.addEventListener("online", this.getErrorLog);
		window.addEventListener("online", this.getInfoLog);
		window.addEventListener("online", this.getJob);
	},
	data() {
		return {
			// Job
			duration: 0,
			isActive: false,
			job: null,
			progress: 0,
			size: 0,

			// Logs
			logs: {
				ERROR_LOG: "",
				INFO_LOG: ""
			},

			// Requests
			intervalIds: {
				ERROR_LOG: 0,
				INFO_LOG: 0,
				JOB: 0
			},
			isLoading: {
				ERROR_LOG: false,
				INFO_LOG: false,
				JOB: false
			},
			requests: {
				ERROR_LOG: null,
				INFO_LOG: null,
				JOB: null
			}
		};
	},
	methods: {
		clearIntervals() {
			clearInterval(this.intervalIds[ERROR_LOG]);
			clearInterval(this.intervalIds[INFO_LOG]);
			clearInterval(this.intervalIds[JOB]);
		},

		getDownloadUrl(workspaceId) {
			return config.apiBaseUrl + "/workspace/download/" + workspaceId;
		},

		getJob(force = false) {
			if (this.isLoading[JOB]) {
				return;
			}

			if (!this.$store.state.isOnline) {
				return;
			}

			if (!force && !this.$store.state.isConnected) {
				return;
			}

			this.isLoading[JOB] = true;
			flashes.clear(flashIdLoadingJobFailed);

			const jobId = this.$route.params.id;
			const url = config.apiBaseUrl + "/job/" + jobId + "/state";

			const promise = this.$http.get(url, {
				before(request) {
					this.requests[JOB] = request;
				}
			});

			promise.then((response) => {
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
					this.isLoading[JOB] = false;
				}, (response) => {
					console.error("job: Parsing JSON response failed:", response);
					this.isLoading[JOB] = false;
				});
			}, (response) => {
				flashes.error("Job info couldn’t be loaded", flashIdLoadingJobFailed);
				this.isLoading[JOB] = false;
			});
		},

		getErrorLog() {
			this.getLog(ERROR_LOG);
		},

		getInfoLog() {
			this.getLog(INFO_LOG);
		},

		getLog(type, force = false) {
			if (type !== ERROR_LOG && type !== INFO_LOG) {
				console.error("job: unknown log type");
				return;
			}

			if (this.isLoading[type]) {
				return;
			}

			if (!this.$store.state.isOnline) {
				return;
			}

			if (!force && !this.$store.state.isConnected) {
				return;
			}

			this.isLoading[type] = true;
			const flashId = type === ERROR_LOG ? flashIdLoadingErrorLogFailed : flashIdLoadingInfoLogFailed;
			flashes.clear(flashId);

			const jobId = this.$route.params.id;
			const url = config.apiBaseUrl + "/job/" + jobId + "/" + type + "log";

			const promise = this.$http.get(url, {
				before(request) {
					this.requests[type] = request;
				}
			});

			promise.then((response) => {
				response.blob().then((data) => {
					const reader = new FileReader();
					reader.addEventListener("loadend", () => {
						this.logs[type] = reader.result;
						this.isLoading[type] = false;
					});
					reader.readAsText(data);
				}, () => {
					this.isLoading[type] = false;
				});
			}, (response) => {
				const logType = type === ERROR_LOG ? "Error" : "Info";
				flashes.error(logType + " log couldn’t be loaded", flashId);
				this.isLoading[type] = false;
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
					console.error("job: Parsing JSON response failed:", response);
				});
			}, (response) => {
				flashes.error("Job couldn’t be stopped", flashIdStoppingJobFailed);
			});
		}
	}
};
