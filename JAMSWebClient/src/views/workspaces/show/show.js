import config from "../../../config";
import * as flashes from "../../../flashes";
import {formatDateTime} from "../../../date";

const flashId = "errRemoveWorkspace";

export default {
	computed: {
		formattedCreationDate: function() {
			return formatDateTime(this.workspace.creationDate);
		}
	},
	created() {
		const workspaceId = this.$route.params.id;
		const url = config.apiBaseUrl + "/workspace/" + workspaceId;

		this.$http.get(url).then((response) => {
			response.json().then((data) => {
				this.workspace = data.workspaces[0];
				this.sort(this.workspace.WorkspaceFileAssociation);
			}, (response) => {
				console.error("workspace: Parsing JSON response failed:", response);
			});
		}, (response) => {
			console.error("workspace: Unexpected response:", response);
		});
	},
	data() {
		return {
			workspace: null
		};
	},
	methods: {
		getDownloadUrl(workspaceId) {
			return config.apiBaseUrl + "/workspace/download/" + workspaceId;
		},
		removeWorkspace(workspace) {
			flashes.clear(flashId);

			const message = "Remove workspace “" + workspace.name + "”?";

			if (!window.confirm(message)) {
				return;
			}

			const url = config.apiBaseUrl + "/workspace/" + workspace.id + "/delete";

			this.$http.get(url).then((response) => {
				this.$router.push({name: "workspaces"});
			}, (response) => {
				flashes.error("Workspace “" + workspace.name + "” couldn’t be removed", flashId);
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
		}
	}
};
