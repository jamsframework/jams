import config from "../../../config";
import * as flashes from "../../../flashes";
import {formatDateTime} from "../../../date";

const flashId = "workspaces.error";

export default {
	created() {
		this.getWorkspaces();
	},
	data() {
		return {
			workspaces: []
		};
	},
	methods: {
		getWorkspaces() {
			const url = config.baseUrl + "/workspace/find";

			this.$http.get(url).then((response) => {
				response.json().then((data) => {
					this.workspaces = data.workspaces;
				}, (response) => {
					console.error("workspaces: Parsing JSON response failed:", response);
				});
			}, (response) => {
				flashes.error("Workspace list couldn’t be loaded");
			});
		},
		removeWorkspace(workspace) {
			flashes.clear(flashId);

			const message = "Remove workspace “" + workspace.name + "”?";

			if (!window.confirm(message)) {
				return;
			}

			const url = config.baseUrl + "/workspace/" + workspace.id + "/delete";

			this.$http.get(url).then((response) => {
				response.json().then((data) => {
					for (let i = 0; i < this.workspaces.length; i++) {
						if (this.workspaces[i].id === data.id) {
							this.workspaces.splice(i, 1);
							break;
						}
					}
				}, (response) => {
					console.error("workspaces: Parsing JSON response failed:", response);
				});
			}, (response) => {
				flashes.error("Workspace “" + workspace.name + "” couldn’t be removed", flashId);
			});
		},
		formatDateTime
	}
};
