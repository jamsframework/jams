import config from "../../../config";
import * as flashes from "../../../flashes";
import {formatDateTime, formatDuration} from "../../../date";

const FLASH_LOADING_ERROR_LOG_FAILED = 1;
const FLASH_LOADING_INFO_LOG_FAILED = 2;
const FLASH_LOADING_JOB_FAILED = 3;
const FLASH_LOADING_JOB_STATE_FAILED = 4;
const FLASH_REMOVING_JOB_FAILED = 5;
const FLASH_STOPPING_JOB_FAILED = 6;

const ERROR_LOG = "error";
const INFO_LOG = "info";
const JOB = "job";
const JOB_STATE = "job-state";

export default {
	beforeDestroy() {
		if (this.requests[ERROR_LOG]) {
			this.requests[ERROR_LOG].abort();
		}

		if (this.requests[INFO_LOG]) {
			this.requests[INFO_LOG].abort();
		}

		if (this.requests[JOB]) {
			this.requests[JOB].abort();
		}

		if (this.requests[JOB_STATE]) {
			this.requests[JOB_STATE].abort();
		}

		this.clearIntervals();

		window.removeEventListener("online", this.getErrorLog);
		window.removeEventListener("online", this.getInfoLog);
		window.removeEventListener("online", this.getJob);
		window.removeEventListener("online", this.getJobState);
	},
	computed: {
		formattedStartTime: function() {
			return formatDateTime(this.startTime);
		}
	},
	created() {
		this.getJob(true);
		this.getJobState(true);
		this.getLog(ERROR_LOG, true);
		this.getLog(INFO_LOG, true);

		this.intervalIds[ERROR_LOG] = setInterval(this.getErrorLog, config.jobsInterval);
		this.intervalIds[INFO_LOG] = setInterval(this.getInfoLog, config.jobsInterval);
		this.intervalIds[JOB_STATE] = setInterval(this.getJobState, config.jobsInterval);

		window.addEventListener("online", this.getErrorLog);
		window.addEventListener("online", this.getInfoLog);
		window.addEventListener("online", this.getJob);
		window.addEventListener("online", this.getJobState);
	},
	data() {
		const data = {
			// Job
			jobId: this.$route.params.id,
			modelFile: null,
			startDate: "",
			workspace: null,

			// Job state
			duration: 0,
			isActive: false,
			isStopping: false,
			progress: 0,
			size: 0,
		};

		// Logs
		data.logs = {};
		data.logs[ERROR_LOG] = "";
		data.logs[INFO_LOG] = "";

		// Requests
		data.intervalIds = {};
		data.intervalIds[ERROR_LOG] = 0;
		data.intervalIds[INFO_LOG] = 0;
		data.intervalIds[JOB] = 0;

		data.isLoading = {};
		data.isLoading[ERROR_LOG] = false;
		data.isLoading[INFO_LOG] = false;
		data.isLoading[JOB] = false;

		data.requests = {};
		data.requests[ERROR_LOG] = null;
		data.requests[INFO_LOG] = null;
		data.requests[JOB] = null;

		return data;
	},
	methods: {
		clearIntervals() {
			clearInterval(this.intervalIds[ERROR_LOG]);
			clearInterval(this.intervalIds[INFO_LOG]);
			clearInterval(this.intervalIds[JOB_STATE]);
		},

		getDownloadUrl(workspaceId) {
			return config.apiBaseUrl + "/workspace/download/" + workspaceId;
		},

		getJob(force = false) {
			if (this.isLoading[JOB] || this.modelFile) {
				return;
			}

			if (!this.$store.state.isOnline) {
				return;
			}

			if (!force && !this.$store.state.isConnected) {
				return;
			}

			this.isLoading[JOB] = true;
			flashes.clear(FLASH_LOADING_JOB_FAILED);

			const url = config.apiBaseUrl + "/job/" + this.jobId;

			const promise = this.$http.get(url, {
				before(request) {
					this.requests[JOB] = request;
				}
			});

			promise.then((response) => {
				response.json().then((data) => {
					this.modelFile = data.modelFile;
					this.startTime = data.startTime;
					this.workspace = data.workspace;

					this.sort(this.workspace.WorkspaceFileAssociation);
					this.isLoading[JOB] = false;
				}, (response) => {
					console.error("job: Parsing JSON response failed:", response);
					this.isLoading[JOB] = false;
				});
			}, (response) => {
				flashes.error("Job info couldn’t be loaded", FLASH_LOADING_JOB_FAILED);
				this.isLoading[JOB] = false;
			});
		},

		getJobState(force = false) {
			if (this.isLoading[JOB_STATE]) {
				return;
			}

			if (!this.$store.state.isOnline) {
				return;
			}

			if (!force && !this.$store.state.isConnected) {
				return;
			}

			this.isLoading[JOB_STATE] = true;
			flashes.clear(FLASH_LOADING_JOB_STATE_FAILED);

			const url = config.apiBaseUrl + "/job/" + this.jobId + "/state";

			const promise = this.$http.get(url, {
				before(request) {
					if (this.requests[JOB_STATE]) {
						this.requests[JOB_STATE].abort();
					}
					this.requests[JOB_STATE] = request;
				}
			});

			promise.then((response) => {
				response.json().then((data) => {
					this.duration = data.duration;
					this.isActive = data.active;
					this.isStopping = false;
					this.progress = data.progress;
					this.size = data.size;

					// Stop refreshing if the job completed
					if (!this.isActive) {
						this.clearIntervals();
					}
					this.isLoading[JOB_STATE] = false;
				}, (response) => {
					console.error("job: Parsing JSON response failed:", response);
					this.isLoading[JOB_STATE] = false;
				});
			}, (response) => {
				flashes.error("Job state couldn’t be loaded", FLASH_LOADING_JOB_STATE_FAILED);
				this.isLoading[JOB_STATE] = false;
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
			const flashId = type === ERROR_LOG ? FLASH_LOADING_ERROR_LOG_FAILED : FLASH_LOADING_INFO_LOG_FAILED;
			flashes.clear(flashId);

			const url = config.apiBaseUrl + "/job/" + this.jobId + "/" + type + "log";

			const promise = this.$http.get(url, {
				before(request) {
					if (this.requests[type]) {
						this.requests[type].abort();
					}
					this.requests[type] = request;
				}
			});

			promise.then((response) => {
				this.logs[type] = response.bodyText;
				this.isLoading[type] = false;
			}, (response) => {
				const logType = type === ERROR_LOG ? "Error" : "Info";
				flashes.error(logType + " log couldn’t be loaded", flashId);
				this.isLoading[type] = false;
			});
		},

		removeJob(jobId) {
			flashes.clear(FLASH_REMOVING_JOB_FAILED);
			const message = "Remove job?";

			if (!window.confirm(message)) {
				return;
			}

			const url = config.apiBaseUrl + "/job/" + jobId + "/delete";

			this.$http.get(url).then((response) => {
				this.$router.push({name: "jobs"});
			}, (response) => {
				flashes.error("Job couldn’t be removed", FLASH_REMOVING_JOB_FAILED);
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
			flashes.clear(FLASH_STOPPING_JOB_FAILED);
			const message = "Stop job?";

			if (!window.confirm(message)) {
				return;
			}

			const url = config.apiBaseUrl + "/job/" + jobId + "/kill";

			this.$http.get(url).then((response) => {
				response.json().then((data) => {
					this.isStopping = true;
				}, (response) => {
					console.error("job: Parsing JSON response failed:", response);
				});
			}, (response) => {
				flashes.error("Job couldn’t be stopped", FLASH_STOPPING_JOB_FAILED);
			});
		}
	}
};
