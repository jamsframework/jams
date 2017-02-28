import config from "../../../config";
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
	data() {
		return {
			duration: 0,
			isActive: false,
			job: null,
			progress: 1,
			size: 0
		};
	},
	methods: {

	},
	mounted() {
		const jobId = this.$router.currentRoute.params.id;
		const url = config.baseUrl + "/job/" + jobId + "/state";

		this.$http.get(url).then((response) => {
			response.json().then((data) => {
				console.debug(response.data);
				this.duration = data.duration;
				this.isActive = data.active;
				this.job = data.job;
				this.progress = data.progress;
				this.size = data.size;
			}, (response) => {
				console.error("jobs: Parsing JSON response failed:", response);
			});
		}, (response) => {
			// TODO: Add flash message.
			this.$store.commit("flashes/add", {
				message: "",
				type: 1
			});
			console.error("jobs: Unexpected response:", response);
		});
	}
};
