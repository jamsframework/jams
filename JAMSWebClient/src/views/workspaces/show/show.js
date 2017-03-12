import config from "../../../config";
import {formatDateTime} from "../../../date";

export default {
	computed: {
		formattedCreationDate: function() {
			return formatDateTime(this.workspace.creationDate);
		}
	},
	data() {
		return {
			workspace: null
		};
	},
	methods: {

	},
	mounted() {
		const workspaceId = this.$route.params.id;
		const url = config.apiBaseUrl + "/workspace/" + workspaceId;

		this.$http.get(url).then((response) => {
			response.json().then((data) => {
				console.debug(response.data);
				this.workspace = data.workspaces[0];
			}, (response) => {
				console.error("Jobs: Parsing JSON response failed:", response);
			});
		}, (response) => {
			console.error("Jobs: Unexpected response:", response);
		});
	}
};
